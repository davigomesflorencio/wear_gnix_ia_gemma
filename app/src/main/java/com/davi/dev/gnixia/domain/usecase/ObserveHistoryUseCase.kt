package com.davi.dev.gnixia.domain.usecase

import com.davi.dev.gnixia.domain.model.HistoryEntry
import com.davi.dev.gnixia.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow

class ObserveHistoryUseCase(private val historyRepository: HistoryRepository) {
    operator fun invoke(): Flow<List<HistoryEntry>> = historyRepository.observeAll()
}
