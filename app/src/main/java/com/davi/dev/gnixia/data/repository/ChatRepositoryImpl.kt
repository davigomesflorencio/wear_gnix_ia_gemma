package com.davi.dev.gnixia.data.repository

import android.content.Context
import com.davi.dev.gnixia.data.llm.GemmaLlamaCppEngine
import com.davi.dev.gnixia.data.llm.LlmEngine
import com.davi.dev.gnixia.data.llm.ModelDownloader
import com.davi.dev.gnixia.domain.model.ModelStatus
import com.davi.dev.gnixia.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ChatRepositoryImpl(private val context: Context) : ChatRepository {

    // The engine is app-scoped so it is loaded only once, even if the ViewModel is recreated
    private var engine: LlmEngine? = null
    private val mutex = Mutex()

    override fun initialize(): Flow<ModelStatus> = flow {
        mutex.withLock {
            if (engine == null) {
                emit(ModelStatus.Preparing)
                ModelDownloader.ensureModel(context)

                emit(ModelStatus.Loading)
                engine = GemmaLlamaCppEngine.create(context)
            }
        }
        emit(ModelStatus.Ready)
    }

    override fun reply(prompt: String): Flow<String> {
        val engine = checkNotNull(engine) { "Model not initialized" }
        return engine.reply(prompt)
    }
}
