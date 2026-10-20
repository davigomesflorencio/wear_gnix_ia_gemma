package com.davi.dev.gnixia.data.llm

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ModelDownloader {

    private const val MODEL_ASSET = "gemma-3-270m-it-Q4_0.gguf"

    // Model used by the LiteRT-LM engine in previous versions
    private const val LEGACY_MODEL = "model.litertlm"

    fun modelFile(context: Context): File = File(context.filesDir, MODEL_ASSET)

    suspend fun ensureModel(context: Context): String = withContext(Dispatchers.IO) {
        // Frees ~300 MB left behind by the old engine
        File(context.filesDir, LEGACY_MODEL).delete()

        val modelFile = modelFile(context)
        if (!modelFile.exists() && modelFile.length() == 0L) {
            context.assets.open(MODEL_ASSET).use { input ->
                modelFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        modelFile.absolutePath
    }
}
