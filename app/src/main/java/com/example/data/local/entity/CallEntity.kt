package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.CallStatus
import com.example.data.model.CallType

@Entity(tableName = "calls")
data class CallEntity(
    @PrimaryKey val id: String,
    val callerId: String,
    val callerName: String,
    val callerAvatarUrl: String = "",
    val receiverId: String,
    val receiverName: String,
    val receiverAvatarUrl: String = "",
    val chatId: String = "",
    val type: CallType = CallType.VOICE,
    val status: CallStatus = CallStatus.COMPLETED,
    val durationSeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
