package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.SettingsEntity
import com.example.data.model.AppLanguage
import com.example.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettings(): Flow<SettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettingsOnce(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: SettingsEntity)

    @Query("UPDATE app_settings SET language = :language WHERE id = 1")
    suspend fun updateLanguage(language: AppLanguage)

    @Query("UPDATE app_settings SET themeMode = :themeMode WHERE id = 1")
    suspend fun updateThemeMode(themeMode: ThemeMode)

    @Query("UPDATE app_settings SET accentColorHex = :accentHex WHERE id = 1")
    suspend fun updateAccentColor(accentHex: String)

    @Query("UPDATE app_settings SET currentUserId = :userId WHERE id = 1")
    suspend fun updateCurrentUserId(userId: String)
}
