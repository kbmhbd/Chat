package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats WHERE isArchived = 0 ORDER BY isPinned DESC, lastMessageTimestamp DESC")
    fun getAllActiveChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE isArchived = 1 ORDER BY lastMessageTimestamp DESC")
    fun getArchivedChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE id = :chatId")
    fun getChatById(chatId: String): Flow<ChatEntity?>

    @Query("SELECT * FROM chats WHERE id = :chatId")
    suspend fun getChatByIdOnce(chatId: String): ChatEntity?

    @Query("SELECT * FROM chats WHERE otherUserId = :otherUserId LIMIT 1")
    suspend fun getDirectChatWithUser(otherUserId: String): ChatEntity?

    @Query("SELECT * FROM chats WHERE name LIKE '%' || :query || '%' OR lastMessageText LIKE '%' || :query || '%'")
    fun searchChats(query: String): Flow<List<ChatEntity>>

    @Query("SELECT COUNT(*) FROM chats")
    fun getTotalChatsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<ChatEntity>)

    @Update
    suspend fun updateChat(chat: ChatEntity)

    @Query("UPDATE chats SET isPinned = :isPinned WHERE id = :chatId")
    suspend fun setPinned(chatId: String, isPinned: Boolean)

    @Query("UPDATE chats SET isArchived = :isArchived WHERE id = :chatId")
    suspend fun setArchived(chatId: String, isArchived: Boolean)

    @Query("UPDATE chats SET isMuted = :isMuted, mutedUntil = :mutedUntil WHERE id = :chatId")
    suspend fun setMuted(chatId: String, isMuted: Boolean, mutedUntil: Long = 0L)

    @Query("UPDATE chats SET unreadCount = 0 WHERE id = :chatId")
    suspend fun markAsRead(chatId: String)

    @Query("UPDATE chats SET lastMessageText = :text, lastMessageTimestamp = :timestamp, lastMessageSenderName = :senderName WHERE id = :chatId")
    suspend fun updateLastMessage(chatId: String, text: String, timestamp: Long, senderName: String)

    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteChatById(chatId: String)
}
