package com.davi.dev.gnixia.domain.repository

import com.davi.dev.gnixia.domain.model.ModelStatus
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    /** Prepares and loads the model, emitting each step until [ModelStatus.Ready]. */
    fun initialize(): Flow<ModelStatus>

    /** Streams the answer to [prompt] in chunks. */
    fun reply(prompt: String): Flow<String>
}
