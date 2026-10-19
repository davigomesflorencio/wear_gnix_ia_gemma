package com.davi.dev.gnixia.data.repository

import android.content.Context
import android.os.Build
import com.davi.dev.gnixia.data.llm.GemmaEngine
import com.davi.dev.gnixia.data.llm.ModelDownloader
import com.davi.dev.gnixia.domain.model.ModelStatus
import com.davi.dev.gnixia.domain.model.UnsupportedDeviceException
import com.davi.dev.gnixia.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ChatRepositoryImpl(private val context: Context) : ChatRepository {

    // The engine is app-scoped so it is loaded only once, even if the ViewModel is recreated
    private var engine: GemmaEngine? = null
    private val mutex = Mutex()

    override fun initialize(): Flow<ModelStatus> = flow {
        mutex.withLock {
            if (engine == null) {
                // Checked before copying the model so unsupported devices don't waste storage
                if (!isDeviceSupported()) {
                    // Frees a copy left behind by older versions that tried to load the model
                    ModelDownloader.modelFile(context).delete()
                    throw UnsupportedDeviceException()
                }

                emit(ModelStatus.Preparing)
                ModelDownloader.ensureModel(context)

                emit(ModelStatus.Loading)
                engine = GemmaEngine.create(context)
            }
        }
        emit(ModelStatus.Ready)
    }

    // LiteRT-LM only ships native libraries for these ABIs; 32-bit watches
    // (e.g. Galaxy Watch 7, armeabi-v7a) can't load it
    private fun isDeviceSupported(): Boolean =
        Build.SUPPORTED_ABIS.any { it in SUPPORTED_ABIS }

    override fun reply(prompt: String): Flow<String> {
        val engine = checkNotNull(engine) { "Model not initialized" }
        return engine.reply(prompt)
    }
}

private val SUPPORTED_ABIS = setOf("arm64-v8a", "x86_64")
