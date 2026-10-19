package com.davi.dev.gnixia.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatHistoryDao {

    @Query("SELECT * FROM chat_history ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ChatHistoryEntity>>

    @Query("SELECT * FROM chat_history WHERE id = :id")
    fun observeById(id: Long): Flow<ChatHistoryEntity?>

    @Insert
    suspend fun insert(entry: ChatHistoryEntity): Long

    @Query("DELETE FROM chat_history")
    suspend fun deleteAll()
}
