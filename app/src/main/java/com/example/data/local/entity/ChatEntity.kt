package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ChatType

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val type: ChatType = ChatType.DIRECT,
    val name: String,
    val avatarUrl: String = "",
    val description: String = "",
    val otherUserId: String = "", // for direct chats
    val createdByUserId: String = "",
    val isOnline: Boolean = true,
    val isMuted: Boolean = false,
    val mutedUntil: Long = 0L,
    val isArchived: Boolean = false,
    val isPinned: Boolean = false,
    val isUnread: Boolean = false,
    val lastMessageText: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val lastMessageSenderName: String = "",
    val unreadCount: Int = 0,
    val groupInviteCode: String = "",
    val onlyAdminsCanMessage: Boolean = false,
    val e2eeKeyFingerprint: String = "E2EE-79F8-B24C-89D1"
)
