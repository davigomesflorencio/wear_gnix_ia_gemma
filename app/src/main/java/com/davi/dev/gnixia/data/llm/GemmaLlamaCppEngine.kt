package com.davi.dev.gnixia.data.llm

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class GemmaLlamaCppEngine private constructor(
    private var handle: Long
) : LlmEngine {

    private val mutex = Mutex()
    private var firstTurn = true

    override fun reply(prompt: String): Flow<String> = flow {
        mutex.withLock {
            check(handle != 0L) { "Engine já foi fechada" }

            var result = start(prompt, firstTurn)
            if (result == LlamaBridge.ERR_OVERFLOW) {
                // Contexto cheio: descarta o histórico e recomeça a conversa
                LlamaBridge.reset(handle)
                firstTurn = true
                result = start(prompt, true)
            }
            check(result >= 0) { "Falha ao processar o prompt (código $result)" }
            firstTurn = false

            while (true) {
                currentCoroutineContext().ensureActive()
                val bytes = LlamaBridge.completionNext(handle) ?: break
                if (bytes.isNotEmpty()) emit(String(bytes, Charsets.UTF_8))
            }
        }
    }.flowOn(Dispatchers.IO)

    /** Começa uma conversa nova (apaga o histórico). */
    suspend fun newConversation() = mutex.withLock {
        if (handle != 0L) LlamaBridge.reset(handle)
        firstTurn = true
    }

    override fun close() = runBlocking {
        mutex.withLock {
            if (handle != 0L) {
                LlamaBridge.free(handle)
                handle = 0L
            }
        }
    }

    private fun start(prompt: String, first: Boolean): Int =
        LlamaBridge.completionStart(
            handle,
            buildTurn(prompt, first).toByteArray(Charsets.UTF_8),
            addBos = first
        )

    companion object {

        private const val N_CTX = 2048
        private const val MAX_TOKENS = 256

        private const val SYSTEM_INSTRUCTION =
            "Você é um assistente prestativo rodando em um relógio Wear OS. " +
                "Responda sempre em português do Brasil, mesmo que a pergunta seja feita em outro idioma. " +
                "Seja breve e direto, pois as respostas são lidas em uma tela pequena."

        /**
         * Template de chat do Gemma. Ele não tem papel "system": a instrução
         * vai no primeiro turno do usuário. Nos turnos seguintes, fecha-se
         * antes o turno anterior do modelo.
         */
        private fun buildTurn(prompt: String, first: Boolean): String =
            if (first) {
                "<start_of_turn>user\n$SYSTEM_INSTRUCTION\n\n$prompt<end_of_turn>\n" +
                    "<start_of_turn>model\n"
            } else {
                "<end_of_turn>\n<start_of_turn>user\n$prompt<end_of_turn>\n" +
                    "<start_of_turn>model\n"
            }

        suspend fun create(context: Context): GemmaLlamaCppEngine = withContext(Dispatchers.IO) {
            val file = ModelDownloader.modelFile(context)
            require(file.exists()) { "Modelo não encontrado em ${file.absolutePath}" }

            // Deixa um núcleo livre para a UI; no máximo 4 threads
            val threads = (Runtime.getRuntime().availableProcessors() - 1).coerceIn(1, 4)

            val handle = LlamaBridge.load(file.absolutePath, N_CTX, threads, MAX_TOKENS)
            check(handle != 0L) { "Falha ao carregar o modelo GGUF" }
            GemmaLlamaCppEngine(handle)
        }
    }
}
