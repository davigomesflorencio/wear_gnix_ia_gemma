package com.davi.dev.gnixia.data.llm

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ModelDownloader {
    fun modelFile(context: Context): File = File(context.filesDir,"model.litertlm")

    suspend fun ensureModel(context: Context): String = withContext(Dispatchers.IO) {
        val modelFile = modelFile(context)
        if (!modelFile.exists() && modelFile.length() == 0L) {
            context.assets.open("model.litertlm").use { input ->
                modelFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        modelFile.absolutePath
    }
}