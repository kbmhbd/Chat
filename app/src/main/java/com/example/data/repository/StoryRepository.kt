package com.example.data.repository

import com.example.data.firebase.FirebaseService
import com.example.data.local.dao.StoryDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.StoryEntity
import com.example.data.model.StoryPrivacy
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class StoryRepository(
    private val storyDao: StoryDao,
    private val userDao: UserDao,
    private val firebaseService: FirebaseService
) {
    fun getActiveStories(): Flow<List<StoryEntity>> = storyDao.getActiveStories()

    suspend fun createTextStory(textContent: String, backgroundColorHex: String, privacy: StoryPrivacy = StoryPrivacy.EVERYONE) {
        val user = userDao.getUserByIdOnce("user_me")
        val story = StoryEntity(
            id = "story_${UUID.randomUUID().toString().take(8)}",
            userId = "user_me",
            userName = user?.name ?: "You",
            userAvatarUrl = user?.avatarUrl ?: "",
            type = "TEXT",
            textContent = textContent,
            backgroundColorHex = backgroundColorHex,
            privacy = privacy,
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000L),
            isViewedByMe = true
        )
        storyDao.insertStory(story)
        firebaseService.syncStoryToFirestore(story)
    }

    suspend fun createMediaStory(mediaUrl: String, textCaption: String = "", privacy: StoryPrivacy = StoryPrivacy.EVERYONE) {
        val user = userDao.getUserByIdOnce("user_me")
        val story = StoryEntity(
            id = "story_${UUID.randomUUID().toString().take(8)}",
            userId = "user_me",
            userName = user?.name ?: "You",
            userAvatarUrl = user?.avatarUrl ?: "",
            type = "IMAGE",
            textContent = textCaption,
            mediaUrl = mediaUrl,
            privacy = privacy,
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000L),
            isViewedByMe = true
        )
        storyDao.insertStory(story)
        firebaseService.syncStoryToFirestore(story)
    }

    suspend fun markStoryAsViewed(storyId: String) {
        storyDao.markStoryAsViewed(storyId)
    }

    suspend fun deleteStory(storyId: String) {
        storyDao.deleteStoryById(storyId)
    }
}
