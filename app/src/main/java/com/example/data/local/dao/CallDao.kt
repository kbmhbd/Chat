package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.CallEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallDao {
    @Query("SELECT * FROM calls ORDER BY timestamp DESC")
    fun getAllCalls(): Flow<List<CallEntity>>

    @Query("SELECT * FROM calls WHERE status = 'MISSED' ORDER BY timestamp DESC")
    fun getMissedCalls(): Flow<List<CallEntity>>

    @Query("SELECT COUNT(*) FROM calls")
    fun getTotalCallsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalls(calls: List<CallEntity>)

    @Query("DELETE FROM calls WHERE id = :callId")
    suspend fun deleteCallById(callId: String)

    @Query("DELETE FROM calls")
    suspend fun clearCallHistory()
}
