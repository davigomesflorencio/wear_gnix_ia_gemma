package com.davi.dev.gnixia.domain.usecase

import com.davi.dev.gnixia.domain.model.HistoryEntry
import com.davi.dev.gnixia.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow

class ObserveHistoryEntryUseCase(private val historyRepository: HistoryRepository) {
    operator fun invoke(id: Long): Flow<HistoryEntry?> = historyRepository.observeById(id)
}
