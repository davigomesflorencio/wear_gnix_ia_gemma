package com.davi.dev.gnixia.data.llm

import kotlinx.coroutines.flow.Flow

interface LlmEngine {
    fun reply(prompt: String): Flow<String>
    fun close()
}
