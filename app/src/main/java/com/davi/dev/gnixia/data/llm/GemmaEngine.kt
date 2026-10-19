package com.davi.dev.gnixia.data.llm

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class GemmaEngine(
    private val engine: Engine,
    private val conversation: Conversation
) {

    fun reply(prompt:String): Flow<String> {
        return conversation.sendMessageAsync(prompt).map { it.toString() }
    }

    fun close(){
        conversation.close()
        engine.close()
    }


    companion object{

        private const val SYSTEM_INSTRUCTION =
            "Você é um assistente prestativo rodando em um relógio Wear OS. " +
                "Responda sempre em português do Brasil, mesmo que a pergunta seja feita em outro idioma. " +
                "Seja breve e direto, pois as respostas são lidas em uma tela pequena."

        suspend fun create(context: Context): GemmaEngine = withContext(Dispatchers.IO){
            val engineConfig = EngineConfig(
                modelPath = ModelDownloader.modelFile(context).absolutePath,
                backend = Backend.CPU(),
                cacheDir = context.cacheDir.absolutePath
            )
            val engine = Engine(engineConfig)

            engine.initialize()

            val conversation = engine.createConversation(
                ConversationConfig(
                    systemInstruction = Contents.of(SYSTEM_INSTRUCTION)
                )
            )
            return@withContext GemmaEngine(engine,conversation)
        }
    }
}