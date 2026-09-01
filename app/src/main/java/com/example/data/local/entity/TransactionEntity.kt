package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.TransactionStatus

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val senderId: String,
    val senderName: String,
    val receiverId: String,
    val receiverName: String,
    val amount: Double,
    val currency: String = "USD",
    val note: String = "",
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val timestamp: Long = System.currentTimeMillis()
)
