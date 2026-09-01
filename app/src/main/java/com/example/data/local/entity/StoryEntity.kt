package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.StoryPrivacy

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val userAvatarUrl: String = "",
    val type: String = "TEXT", // "TEXT", "IMAGE"
    val textContent: String = "",
    val backgroundColorHex: String = "#0084FF",
    val mediaUrl: String = "",
    val privacy: StoryPrivacy = StoryPrivacy.EVERYONE,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000), // 24 hours
    val viewsCount: Int = 0,
    val isViewedByMe: Boolean = false
)
