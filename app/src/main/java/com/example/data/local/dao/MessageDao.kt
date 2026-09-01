package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.MessageEntity
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatId = :chatId AND isDeletedForMe = 0 ORDER BY createdAt ASC")
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND isPinned = 1 AND isDeletedForMe = 0 ORDER BY createdAt DESC")
    fun getPinnedMessagesForChat(chatId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE isSaved = 1 AND isDeletedForMe = 0 ORDER BY createdAt DESC")
    fun getSavedMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND type IN (:types) AND isDeletedForMe = 0 ORDER BY createdAt DESC")
    fun getMediaMessagesForChat(chatId: String, types: List<MessageType>): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE text LIKE '%' || :query || '%' AND isDeletedForMe = 0 ORDER BY createdAt DESC")
    fun searchAllMessages(query: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND text LIKE '%' || :query || '%' AND isDeletedForMe = 0 ORDER BY createdAt DESC")
    fun searchMessagesInChat(chatId: String, query: String): Flow<List<MessageEntity>>

    @Query("SELECT COUNT(*) FROM messages")
    fun getTotalMessagesCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET text = :newText, isEdited = 1 WHERE id = :messageId")
    suspend fun editMessage(messageId: String, newText: String)

    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: MessageStatus)

    @Query("UPDATE messages SET reactionsJson = :reactionsJson WHERE id = :messageId")
    suspend fun updateReactions(messageId: String, reactionsJson: String)

    @Query("UPDATE messages SET isPinned = :isPinned WHERE id = :messageId")
    suspend fun setPinned(messageId: String, isPinned: Boolean)

    @Query("UPDATE messages SET isSaved = :isSaved WHERE id = :messageId")
    suspend fun setSaved(messageId: String, isSaved: Boolean)

    @Query("UPDATE messages SET isDeletedForMe = 1 WHERE id = :messageId")
    suspend fun deleteForMe(messageId: String)

    @Query("UPDATE messages SET isDeletedForEveryone = 1, text = 'This message was deleted' WHERE id = :messageId")
    suspend fun deleteForEveryone(messageId: String)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun clearChatMessages(chatId: String)
}
