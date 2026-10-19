package com.davi.dev.gnixia.data.repository

import com.davi.dev.gnixia.data.local.ChatHistoryDao
import com.davi.dev.gnixia.data.local.ChatHistoryEntity
import com.davi.dev.gnixia.domain.model.HistoryEntry
import com.davi.dev.gnixia.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HistoryRepositoryImpl(private val dao: ChatHistoryDao) : HistoryRepository {

    override fun observeAll(): Flow<List<HistoryEntry>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeById(id: Long): Flow<HistoryEntry?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun save(question: String, answer: String) {
        dao.insert(ChatHistoryEntity(question = question, answer = answer))
    }

    override suspend fun clear() = dao.deleteAll()
}

private fun ChatHistoryEntity.toDomain() = HistoryEntry(
    id = id,
    question = question,
    answer = answer,
    createdAt = createdAt
)
