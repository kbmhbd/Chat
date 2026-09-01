package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.ChatMemberEntity
import com.example.data.model.MemberRole
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMemberDao {
    @Query("SELECT * FROM chat_members WHERE chatId = :chatId")
    fun getMembersForChat(chatId: String): Flow<List<ChatMemberEntity>>

    @Query("SELECT * FROM chat_members WHERE chatId = :chatId AND userId = :userId LIMIT 1")
    suspend fun getMember(chatId: String, userId: String): ChatMemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: ChatMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<ChatMemberEntity>)

    @Query("UPDATE chat_members SET role = :role WHERE chatId = :chatId AND userId = :userId")
    suspend fun updateMemberRole(chatId: String, userId: String, role: MemberRole)

    @Query("DELETE FROM chat_members WHERE chatId = :chatId AND userId = :userId")
    suspend fun removeMember(chatId: String, userId: String)

    @Query("DELETE FROM chat_members WHERE chatId = :chatId")
    suspend fun removeAllMembersForChat(chatId: String)
}
