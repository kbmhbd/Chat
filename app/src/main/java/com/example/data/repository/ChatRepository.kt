package com.example.data.repository

import com.example.data.firebase.FirebaseService
import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.ChatMemberDao
import com.example.data.local.dao.MessageDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.ChatEntity
import com.example.data.local.entity.ChatMemberEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class ChatRepository(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val userDao: UserDao,
    private val chatMemberDao: ChatMemberDao,
    private val firebaseService: FirebaseService
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    // Global typing indicator map chatId -> (typingUserName or null)
    private val _typingStatus = MutableStateFlow<Map<String, String?>>(emptyMap())
    val typingStatus: StateFlow<Map<String, String?>> = _typingStatus.asStateFlow()

    fun getAllActiveChats(): Flow<List<ChatEntity>> = chatDao.getAllActiveChats()
    fun getArchivedChats(): Flow<List<ChatEntity>> = chatDao.getArchivedChats()
    fun getChatById(chatId: String): Flow<ChatEntity?> = chatDao.getChatById(chatId)
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>> = messageDao.getMessagesForChat(chatId)
    fun getPinnedMessagesForChat(chatId: String): Flow<List<MessageEntity>> = messageDao.getPinnedMessagesForChat(chatId)
    fun getSavedMessages(): Flow<List<MessageEntity>> = messageDao.getSavedMessages()
    fun getMediaMessagesForChat(chatId: String, types: List<MessageType>): Flow<List<MessageEntity>> =
        messageDao.getMediaMessagesForChat(chatId, types)
    fun getChatMembers(chatId: String): Flow<List<ChatMemberEntity>> = chatMemberDao.getMembersForChat(chatId)
    fun searchAllMessages(query: String): Flow<List<MessageEntity>> = messageDao.searchAllMessages(query)
    fun searchChats(query: String): Flow<List<ChatEntity>> = chatDao.searchChats(query)
    fun searchUsers(query: String): Flow<List<UserEntity>> = userDao.searchUsers(query)
    fun getActiveUsers(): Flow<List<UserEntity>> = userDao.getActiveUsers()

    suspend fun getOrCreateDirectChat(user: UserEntity): String {
        val existing = chatDao.getDirectChatWithUser(user.id)
        if (existing != null) {
            return existing.id
        }
        val chatId = "chat_direct_${user.id}_${System.currentTimeMillis()}"
        val newChat = ChatEntity(
            id = chatId,
            type = ChatType.DIRECT,
            name = user.name,
            avatarUrl = user.avatarUrl,
            otherUserId = user.id,
            description = user.bio,
            lastMessageText = "Started a conversation",
            lastMessageTimestamp = System.currentTimeMillis()
        )
        chatDao.insertChat(newChat)
        firebaseService.syncChatToFirestore(newChat)
        return chatId
    }

    suspend fun createGroupChat(name: String, description: String, memberUserIds: List<String>, avatarUrl: String = ""): String {
        val chatId = "group_${UUID.randomUUID().toString().take(8)}"
        val chat = ChatEntity(
            id = chatId,
            type = ChatType.GROUP,
            name = name,
            avatarUrl = avatarUrl.ifBlank { "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=150" },
            description = description,
            createdByUserId = "user_me",
            lastMessageText = "Group created",
            lastMessageTimestamp = System.currentTimeMillis(),
            lastMessageSenderName = "You"
        )
        chatDao.insertChat(chat)
        firebaseService.syncChatToFirestore(chat)

        // Add creator as owner
        chatMemberDao.insertMember(
            ChatMemberEntity(chatId = chatId, userId = "user_me", role = MemberRole.OWNER)
        )
        // Add members
        memberUserIds.forEach { uid ->
            chatMemberDao.insertMember(
                ChatMemberEntity(chatId = chatId, userId = uid, role = MemberRole.MEMBER)
            )
        }

        // Add system message
        sendMessage(
            chatId = chatId,
            text = "created the group \"$name\"",
            type = MessageType.SYSTEM,
            senderId = "user_me",
            senderName = "You"
        )

        return chatId
    }

    suspend fun sendMessage(
        chatId: String,
        text: String,
        type: MessageType = MessageType.TEXT,
        mediaUrl: String = "",
        mediaName: String = "",
        mediaSize: String = "",
        mediaDurationMs: Long = 0L,
        replyToMessageId: String? = null,
        replyToText: String? = null,
        replyToSenderName: String? = null,
        senderId: String = "user_me",
        senderName: String = "You",
        senderAvatarUrl: String = ""
    ): MessageEntity {
        val messageId = "msg_${UUID.randomUUID().toString().take(10)}"
        val message = MessageEntity(
            id = messageId,
            chatId = chatId,
            senderId = senderId,
            senderName = senderName,
            senderAvatarUrl = senderAvatarUrl,
            text = text,
            type = type,
            mediaUrl = mediaUrl,
            mediaName = mediaName,
            mediaSize = mediaSize,
            mediaDurationMs = mediaDurationMs,
            replyToMessageId = replyToMessageId,
            replyToText = replyToText,
            replyToSenderName = replyToSenderName,
            status = MessageStatus.SENT,
            createdAt = System.currentTimeMillis()
        )

        messageDao.insertMessage(message)

        val displayText = when (type) {
            MessageType.VOICE -> "🎤 Voice message"
            MessageType.IMAGE -> "📷 Photo"
            MessageType.VIDEO -> "🎥 Video"
            MessageType.AUDIO -> "🎵 Audio"
            MessageType.FILE -> "📄 $mediaName"
            MessageType.LOCATION -> "📍 Location shared"
            MessageType.PAYMENT -> "💳 Money transfer: $text"
            MessageType.STICKER -> "🎨 Sticker"
            MessageType.SYSTEM -> text
            else -> text
        }

        chatDao.updateLastMessage(
            chatId = chatId,
            text = displayText,
            timestamp = System.currentTimeMillis(),
            senderName = senderName
        )

        // Sync to Firebase
        firebaseService.syncMessageToFirestore(message)
        val currentChat = chatDao.getChatByIdOnce(chatId)
        if (currentChat != null) {
            firebaseService.syncChatToFirestore(currentChat)
        }

        // Trigger smart conversational simulated reply if offline/local demo
        if (senderId == "user_me") {
            triggerSimulatedReplyIfNeeded(chatId, text, type)
        }

        return message
    }

    private fun triggerSimulatedReplyIfNeeded(chatId: String, userMessage: String, messageType: MessageType) {
        repositoryScope.launch {
            val chat = chatDao.getChatByIdOnce(chatId) ?: return@launch
            if (chat.type == ChatType.AI) {
                // Handled separately by Gemini
                return@launch
            }

            // Simulate typing indicator
            delay(1200)
            val responderName = if (chat.type == ChatType.DIRECT) chat.name else "Rahim"
            _typingStatus.value = _typingStatus.value + (chatId to responderName)

            delay(2000)
            _typingStatus.value = _typingStatus.value - chatId

            // Generate smart conversational reply
            val replyText = generateContextualReply(userMessage, chat.name)
            val replySenderId = if (chat.type == ChatType.DIRECT) chat.otherUserId.ifBlank { "user_contact" } else "user_1"
            val replyAvatar = chat.avatarUrl

            val replyMsg = MessageEntity(
                id = "msg_reply_${System.currentTimeMillis()}",
                chatId = chatId,
                senderId = replySenderId,
                senderName = responderName,
                senderAvatarUrl = replyAvatar,
                text = replyText,
                type = MessageType.TEXT,
                status = MessageStatus.READ,
                createdAt = System.currentTimeMillis()
            )
            messageDao.insertMessage(replyMsg)
            chatDao.updateLastMessage(chatId, replyText, System.currentTimeMillis(), responderName)
            firebaseService.syncMessageToFirestore(replyMsg)
        }
    }

    private fun generateContextualReply(message: String, contactName: String): String {
        val lower = message.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") || lower.contains("কেমন") ->
                "Hey! Doing great, thanks for reaching out. How have you been?"
            lower.contains("how are you") || lower.contains("how's it going") ->
                "I'm doing well! Just finishing up some work. What are your plans for today?"
            lower.contains("where") || lower.contains("location") ->
                "I'm around Dhanmondi right now! Are we still meeting up later?"
            lower.contains("call") || lower.contains("video") ->
                "Sure! Give me 5 minutes, then we can jump on a call."
            lower.contains("thanks") || lower.contains("thank you") || lower.contains("ধন্যবাদ") ->
                "You're very welcome! Anytime! 😊"
            lower.contains("bye") || lower.contains("good night") || lower.contains("see you") ->
                "Take care! Talk to you soon! 👋"
            lower.contains("send") || lower.contains("money") || lower.contains("pay") ->
                "Got it! Thanks for the update. Let me check my Messenger Pay wallet."
            else -> "Awesome, thanks for the message! Let me get back to you in just a bit. 👍"
        }
    }

    suspend fun addReaction(messageId: String, emoji: String, userId: String = "user_me", userName: String = "You") {
        val msg = messageDao.getMessageById(messageId) ?: return
        val reactions = parseReactions(msg.reactionsJson).toMutableList()

        // Toggle or change reaction
        val existingIndex = reactions.indexOfFirst { it.userId == userId }
        if (existingIndex >= 0) {
            if (reactions[existingIndex].emoji == emoji) {
                reactions.removeAt(existingIndex)
            } else {
                reactions[existingIndex] = MessageReaction(emoji, userId, userName)
            }
        } else {
            reactions.add(MessageReaction(emoji, userId, userName))
        }

        val json = serializeReactions(reactions)
        messageDao.updateReactions(messageId, json)
        val updatedMsg = msg.copy(reactionsJson = json)
        firebaseService.syncMessageToFirestore(updatedMsg)
    }

    private fun parseReactions(jsonStr: String): List<MessageReaction> {
        return try {
            val list = mutableListOf<MessageReaction>()
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    MessageReaction(
                        emoji = obj.getString("emoji"),
                        userId = obj.getString("userId"),
                        userName = obj.getString("userName")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun serializeReactions(list: List<MessageReaction>): String {
        val array = JSONArray()
        list.forEach { reaction ->
            val obj = JSONObject()
            obj.put("emoji", reaction.emoji)
            obj.put("userId", reaction.userId)
            obj.put("userName", reaction.userName)
            array.put(obj)
        }
        return array.toString()
    }

    suspend fun editMessage(messageId: String, newText: String) {
        messageDao.editMessage(messageId, newText)
        val msg = messageDao.getMessageById(messageId)
        if (msg != null) {
            firebaseService.syncMessageToFirestore(msg)
        }
    }

    suspend fun togglePinMessage(messageId: String, isPinned: Boolean) {
        messageDao.setPinned(messageId, isPinned)
        val msg = messageDao.getMessageById(messageId)
        if (msg != null) {
            firebaseService.syncMessageToFirestore(msg)
        }
    }

    suspend fun toggleSaveMessage(messageId: String, isSaved: Boolean) {
        messageDao.setSaved(messageId, isSaved)
        val msg = messageDao.getMessageById(messageId)
        if (msg != null) {
            firebaseService.syncMessageToFirestore(msg)
        }
    }

    suspend fun deleteMessageForMe(messageId: String) {
        messageDao.deleteForMe(messageId)
    }

    suspend fun deleteMessageForEveryone(messageId: String) {
        messageDao.deleteForEveryone(messageId)
        val msg = messageDao.getMessageById(messageId)
        if (msg != null) {
            firebaseService.syncMessageToFirestore(msg)
        }
    }

    suspend fun togglePinChat(chatId: String, isPinned: Boolean) {
        chatDao.setPinned(chatId, isPinned)
        val chat = chatDao.getChatByIdOnce(chatId)
        if (chat != null) {
            firebaseService.syncChatToFirestore(chat)
        }
    }

    suspend fun toggleArchiveChat(chatId: String, isArchived: Boolean) {
        chatDao.setArchived(chatId, isArchived)
        val chat = chatDao.getChatByIdOnce(chatId)
        if (chat != null) {
            firebaseService.syncChatToFirestore(chat)
        }
    }

    suspend fun muteChat(chatId: String, durationHours: Int) {
        val mutedUntil = if (durationHours > 0) System.currentTimeMillis() + (durationHours * 3600 * 1000L) else -1L
        chatDao.setMuted(chatId, isMuted = true, mutedUntil = mutedUntil)
    }

    suspend fun unmuteChat(chatId: String) {
        chatDao.setMuted(chatId, isMuted = false, mutedUntil = 0L)
    }

    suspend fun markChatAsRead(chatId: String) {
        chatDao.markAsRead(chatId)
    }

    suspend fun clearChatHistory(chatId: String) {
        messageDao.clearChatMessages(chatId)
        chatDao.updateLastMessage(chatId, "Chat cleared", System.currentTimeMillis(), "")
    }

    suspend fun deleteChat(chatId: String) {
        messageDao.clearChatMessages(chatId)
        chatMemberDao.removeAllMembersForChat(chatId)
        chatDao.deleteChatById(chatId)
    }
}
