package com.callguard.history

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CallHistoryDao {
    @Query("SELECT * FROM call_history ORDER BY timestamp DESC")
    fun getAllCalls(): Flow<List<CallLogEntry>>

    @Query("SELECT * FROM call_history WHERE wasAIHandled = 1 ORDER BY timestamp DESC")
    fun getAIHandledCalls(): Flow<List<CallLogEntry>>

    @Query("SELECT * FROM call_history WHERE id = :id")
    suspend fun getCallById(id: Long): CallLogEntry?

    @Insert
    suspend fun insertCall(entry: CallLogEntry): Long

    @Update
    suspend fun updateCall(entry: CallLogEntry)

    @Query("DELETE FROM call_history WHERE id = :id")
    suspend fun deleteCall(id: Long)

    @Query("SELECT * FROM call_history WHERE phoneNumber = :number ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastCallFromNumber(number: String): CallLogEntry?
}
