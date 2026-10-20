package com.davi.dev.gnixia.data.llm

internal object LlamaBridge {

    const val ERR_OVERFLOW = -1
    const val ERR_TOKENIZE = -2
    const val ERR_DECODE = -3

    init {
        System.loadLibrary("gnixia-llm")
    }

    /** Retorna um handle nativo, ou 0 em caso de falha. */
    external fun load(modelPath: String, nCtx: Int, nThreads: Int, maxTokens: Int): Long

    /** Processa o turno (prompt em UTF-8). Retorna nº de tokens ou um código ERR_*. */
    external fun completionStart(handle: Long, prompt: ByteArray, addBos: Boolean): Int

    /** Próximo pedaço da resposta em UTF-8 (pode vir vazio); null quando terminou. */
    external fun completionNext(handle: Long): ByteArray?

    external fun reset(handle: Long)

    external fun free(handle: Long)
}
