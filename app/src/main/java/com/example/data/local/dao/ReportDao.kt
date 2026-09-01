package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.ReportEntity
import com.example.data.model.ReportStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE status = 'PENDING' ORDER BY timestamp DESC")
    fun getPendingReports(): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Query("UPDATE reports SET status = :status WHERE id = :reportId")
    suspend fun updateReportStatus(reportId: String, status: ReportStatus)

    @Query("DELETE FROM reports WHERE id = :reportId")
    suspend fun deleteReport(reportId: String)
}
