// JNI bridge between LlamaBridge.kt and llama.cpp.
//
// One handle = one model + one context + one sampler, holding a single chat
// conversation in the KV cache. Calls are serialized by GemmaLlamaCppEngine's
// mutex, so no locking is needed here.

#include <jni.h>
#include <android/log.h>

#include <algorithm>
#include <cstdint>
#include <string>
#include <vector>

#include "llama.h"

#define TAG "GnixiaLlm"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// Must match the ERR_* constants in LlamaBridge.kt
constexpr jint ERR_OVERFLOW = -1;
constexpr jint ERR_TOKENIZE = -2;
constexpr jint ERR_DECODE = -3;

namespace {

struct Engine {
    llama_model *model = nullptr;
    llama_context *ctx = nullptr;
    llama_sampler *sampler = nullptr;
    const llama_vocab *vocab = nullptr;

    int n_ctx = 0;
    int n_batch = 0;
    int max_tokens = 0;

    int n_past = 0;       // tokens in the KV cache
    int n_generated = 0;  // tokens generated in the current turn
    bool done = true;

    // Bytes of a UTF-8 character split across tokens, waiting for the rest
    std::string pending_utf8;
};

Engine *from_handle(jlong handle) { return reinterpret_cast<Engine *>(handle); }

// Length of the longest prefix of `s` that doesn't end in an incomplete UTF-8 sequence
size_t complete_utf8_length(const std::string &s) {
    const size_t n = s.size();
    // A UTF-8 sequence is at most 4 bytes: only the last 3 can belong to an unfinished one
    for (size_t back = 1; back <= std::min<size_t>(3, n); ++back) {
        const auto c = static_cast<unsigned char>(s[n - back]);
        if ((c & 0xC0) == 0x80) continue;  // continuation byte, keep looking for the lead byte
        size_t expected = 1;
        if ((c & 0xE0) == 0xC0) expected = 2;
        else if ((c & 0xF0) == 0xE0) expected = 3;
        else if ((c & 0xF8) == 0xF0) expected = 4;
        return back < expected ? n - back : n;
    }
    return n;
}

jbyteArray to_java_bytes(JNIEnv *env, const std::string &s) {
    jbyteArray array = env->NewByteArray(static_cast<jsize>(s.size()));
    if (array != nullptr && !s.empty()) {
        env->SetByteArrayRegion(array, 0, static_cast<jsize>(s.size()),
                                reinterpret_cast<const jbyte *>(s.data()));
    }
    return array;
}

// Feeds `tokens` to the model in chunks of n_batch
bool decode_tokens(Engine *e, std::vector<llama_token> &tokens) {
    for (size_t i = 0; i < tokens.size(); i += e->n_batch) {
        const auto n = static_cast<int32_t>(std::min<size_t>(e->n_batch, tokens.size() - i));
        if (llama_decode(e->ctx, llama_batch_get_one(tokens.data() + i, n)) != 0) return false;
        e->n_past += n;
    }
    return true;
}

}  // namespace

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_davi_dev_gnixia_data_llm_LlamaBridge_load(JNIEnv *env, jobject, jstring model_path,
                                                   jint n_ctx, jint n_threads, jint max_tokens) {
    static bool backend_ready = false;
    if (!backend_ready) {
        llama_backend_init();
        backend_ready = true;
    }

    const char *path = env->GetStringUTFChars(model_path, nullptr);
    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = 0;  // CPU only
    llama_model *model = llama_model_load_from_file(path, model_params);
    env->ReleaseStringUTFChars(model_path, path);
    if (model == nullptr) {
        LOGE("Failed to load model");
        return 0;
    }

    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = static_cast<uint32_t>(n_ctx);
    ctx_params.n_batch = std::min<uint32_t>(512, ctx_params.n_ctx);
    ctx_params.n_threads = n_threads;
    ctx_params.n_threads_batch = n_threads;
    ctx_params.no_perf = true;
    llama_context *ctx = llama_init_from_model(model, ctx_params);
    if (ctx == nullptr) {
        LOGE("Failed to create context");
        llama_model_free(model);
        return 0;
    }

    const llama_vocab *vocab = llama_model_get_vocab(model);

    // Sampling recommended for Gemma 3, plus a mild repetition penalty
    // (small models tend to loop)
    llama_sampler *sampler = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(sampler, llama_sampler_init_penalties(
            llama_vocab_n_tokens(vocab), 64, 1.1f, 0.0f, 0.0f));
    llama_sampler_chain_add(sampler, llama_sampler_init_top_k(64));
    llama_sampler_chain_add(sampler, llama_sampler_init_top_p(0.95f, 1));
    llama_sampler_chain_add(sampler, llama_sampler_init_temp(1.0f));
    llama_sampler_chain_add(sampler, llama_sampler_init_dist(LLAMA_DEFAULT_SEED));

    auto *e = new Engine();
    e->model = model;
    e->ctx = ctx;
    e->sampler = sampler;
    e->vocab = vocab;
    e->n_ctx = static_cast<int>(llama_n_ctx(ctx));
    e->n_batch = static_cast<int>(llama_n_batch(ctx));
    e->max_tokens = max_tokens;

    LOGI("Model loaded: n_ctx=%d threads=%d", e->n_ctx, n_threads);
    return reinterpret_cast<jlong>(e);
}

JNIEXPORT jint JNICALL
Java_com_davi_dev_gnixia_data_llm_LlamaBridge_completionStart(JNIEnv *env, jobject, jlong handle,
                                                              jbyteArray prompt, jboolean add_bos) {
    Engine *e = from_handle(handle);

    const jsize len = env->GetArrayLength(prompt);
    std::string text(static_cast<size_t>(len), '\0');
    env->GetByteArrayRegion(prompt, 0, len, reinterpret_cast<jbyte *>(text.data()));

    // First call with an empty buffer returns the needed size as a negative number.
    // parse_special: the turn markers (<start_of_turn>, ...) must become special tokens
    const int32_t needed = -llama_tokenize(e->vocab, text.data(), static_cast<int32_t>(text.size()),
                                           nullptr, 0, add_bos, true);
    if (needed <= 0) return ERR_TOKENIZE;
    std::vector<llama_token> tokens(static_cast<size_t>(needed));
    if (llama_tokenize(e->vocab, text.data(), static_cast<int32_t>(text.size()), tokens.data(),
                       needed, add_bos, true) < 0) {
        return ERR_TOKENIZE;
    }

    // The prompt must fit with room for at least part of the answer
    if (e->n_past + needed >= e->n_ctx) return ERR_OVERFLOW;

    if (!decode_tokens(e, tokens)) return ERR_DECODE;

    e->n_generated = 0;
    e->pending_utf8.clear();
    e->done = false;
    return needed;
}

JNIEXPORT jbyteArray JNICALL
Java_com_davi_dev_gnixia_data_llm_LlamaBridge_completionNext(JNIEnv *env, jobject, jlong handle) {
    Engine *e = from_handle(handle);
    if (e->done) return nullptr;

    const bool out_of_room = e->n_generated >= e->max_tokens || e->n_past >= e->n_ctx;
    llama_token token = 0;
    if (!out_of_room) token = llama_sampler_sample(e->sampler, e->ctx, -1);

    // End of turn (EOG) isn't decoded into the cache: the next turn starts by
    // closing the model's turn itself (see GemmaLlamaCppEngine.buildTurn)
    if (out_of_room || llama_vocab_is_eog(e->vocab, token)) {
        e->done = true;
        if (e->pending_utf8.empty()) return nullptr;
        std::string rest;
        rest.swap(e->pending_utf8);
        return to_java_bytes(env, rest);
    }

    char piece[256];
    const int32_t n = llama_token_to_piece(e->vocab, token, piece, sizeof(piece), 0, false);
    if (n > 0) e->pending_utf8.append(piece, static_cast<size_t>(n));

    if (llama_decode(e->ctx, llama_batch_get_one(&token, 1)) != 0) {
        LOGE("Decode failed while generating");
        e->done = true;
        return nullptr;
    }
    e->n_past += 1;
    e->n_generated += 1;

    // Only hand complete UTF-8 characters to Kotlin; keep the tail for the next call
    const size_t complete = complete_utf8_length(e->pending_utf8);
    std::string out = e->pending_utf8.substr(0, complete);
    e->pending_utf8.erase(0, complete);
    return to_java_bytes(env, out);
}

JNIEXPORT void JNICALL
Java_com_davi_dev_gnixia_data_llm_LlamaBridge_reset(JNIEnv *, jobject, jlong handle) {
    Engine *e = from_handle(handle);
    llama_memory_clear(llama_get_memory(e->ctx), true);
    llama_sampler_reset(e->sampler);
    e->n_past = 0;
    e->n_generated = 0;
    e->pending_utf8.clear();
    e->done = true;
}

JNIEXPORT void JNICALL
Java_com_davi_dev_gnixia_data_llm_LlamaBridge_free(JNIEnv *, jobject, jlong handle) {
    Engine *e = from_handle(handle);
    llama_sampler_free(e->sampler);
    llama_free(e->ctx);
    llama_model_free(e->model);
    delete e;
}

}  // extern "C"
