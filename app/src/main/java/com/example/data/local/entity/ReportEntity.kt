package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ReportStatus

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val reporterId: String,
    val reportedUserId: String,
    val reportedUserName: String,
    val reportedMessageId: String? = null,
    val reportedMessageContent: String? = null,
    val reason: String,
    val details: String = "",
    val status: ReportStatus = ReportStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis()
)
