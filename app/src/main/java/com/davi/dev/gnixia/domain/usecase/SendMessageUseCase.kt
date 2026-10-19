package com.davi.dev.gnixia.domain.usecase

import com.davi.dev.gnixia.domain.repository.ChatRepository
import com.davi.dev.gnixia.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Streams the model's answer and saves the question/answer pair once it completes. */
class SendMessageUseCase(
    private val chatRepository: ChatRepository,
    private val historyRepository: HistoryRepository
) {
    operator fun invoke(question: String): Flow<String> = flow {
        val answer = StringBuilder()
        chatRepository.reply(question).collect { chunk ->
            answer.append(chunk)
            emit(chunk)
        }
        // Only completed answers go to the history
        historyRepository.save(question, answer.toString())
    }
}
