package com.davi.dev.gnixia.domain.usecase

import com.davi.dev.gnixia.domain.repository.HistoryRepository

class ClearHistoryUseCase(private val historyRepository: HistoryRepository) {
    suspend operator fun invoke() = historyRepository.clear()
}
