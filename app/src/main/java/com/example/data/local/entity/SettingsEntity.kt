package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AppLanguage
import com.example.data.model.ThemeMode

@Entity(tableName = "app_settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val currentUserId: String = "user_me",
    val language: AppLanguage = AppLanguage.ENGLISH,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColorHex: String = "#0084FF", // Default Messenger blue
    val readReceiptsEnabled: Boolean = true,
    val typingIndicatorEnabled: Boolean = true,
    val activeStatusEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val messageNotificationsEnabled: Boolean = true,
    val callNotificationsEnabled: Boolean = true,
    val groupNotificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val endToEndEncryptionEnabled: Boolean = true,
    val whoCanMessageMe: String = "EVERYONE", // EVERYONE, CONTACTS
    val whoCanCallMe: String = "EVERYONE",
    val whoCanSeeLastSeen: String = "EVERYONE",
    val whoCanSeeProfilePhoto: String = "EVERYONE"
)
