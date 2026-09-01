package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType

@Entity(
    tableName = "messages",
    indices = [Index("chatId"), Index("createdAt")]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatarUrl: String = "",
    val text: String,
    val type: MessageType = MessageType.TEXT,
    val mediaUrl: String = "",
    val mediaName: String = "",
    val mediaSize: String = "",
    val mediaDurationMs: Long = 0L,
    val replyToMessageId: String? = null,
    val replyToText: String? = null,
    val replyToSenderName: String? = null,
    val status: MessageStatus = MessageStatus.SENT,
    val reactionsJson: String = "[]", // JSON serialized List<MessageReaction>
    val isPinned: Boolean = false,
    val isSaved: Boolean = false,
    val isEdited: Boolean = false,
    val isDeletedForMe: Boolean = false,
    val isDeletedForEveryone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
