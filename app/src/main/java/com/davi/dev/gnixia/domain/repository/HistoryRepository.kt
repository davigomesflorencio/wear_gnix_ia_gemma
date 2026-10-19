package com.davi.dev.gnixia.domain.repository

import com.davi.dev.gnixia.domain.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {

    fun observeAll(): Flow<List<HistoryEntry>>

    fun observeById(id: Long): Flow<HistoryEntry?>

    suspend fun save(question: String, answer: String)

    suspend fun clear()
}
