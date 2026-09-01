package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.StoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories WHERE expiresAt > :currentTime ORDER BY createdAt DESC")
    fun getActiveStories(currentTime: Long = System.currentTimeMillis()): Flow<List<StoryEntity>>

    @Query("SELECT * FROM stories WHERE userId = :userId AND expiresAt > :currentTime ORDER BY createdAt DESC")
    fun getStoriesForUser(userId: String, currentTime: Long = System.currentTimeMillis()): Flow<List<StoryEntity>>

    @Query("SELECT * FROM stories WHERE id = :storyId LIMIT 1")
    suspend fun getStoryById(storyId: String): StoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<StoryEntity>)

    @Query("UPDATE stories SET isViewedByMe = 1, viewsCount = viewsCount + 1 WHERE id = :storyId")
    suspend fun markStoryAsViewed(storyId: String)

    @Query("DELETE FROM stories WHERE id = :storyId")
    suspend fun deleteStoryById(storyId: String)

    @Query("DELETE FROM stories WHERE expiresAt <= :currentTime")
    suspend fun deleteExpiredStories(currentTime: Long = System.currentTimeMillis())
}
