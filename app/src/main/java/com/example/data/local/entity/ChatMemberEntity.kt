package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import com.example.data.model.MemberRole

@Entity(
    tableName = "chat_members",
    primaryKeys = ["chatId", "userId"],
    indices = [Index("chatId"), Index("userId")]
)
data class ChatMemberEntity(
    val chatId: String,
    val userId: String,
    val role: MemberRole = MemberRole.MEMBER,
    val joinedAt: Long = System.currentTimeMillis(),
    val canSendMessages: Boolean = true
)
