package com.davi.dev.gnixia.domain.usecase

import com.davi.dev.gnixia.domain.model.ModelStatus
import com.davi.dev.gnixia.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class InitializeModelUseCase(private val chatRepository: ChatRepository) {
    operator fun invoke(): Flow<ModelStatus> = chatRepository.initialize()
}
