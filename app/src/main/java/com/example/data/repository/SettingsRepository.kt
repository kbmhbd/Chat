package com.example.data.repository

import com.example.data.local.dao.SettingsDao
import com.example.data.local.entity.SettingsEntity
import com.example.data.model.AppLanguage
import com.example.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val settingsDao: SettingsDao
) {
    val settings: Flow<SettingsEntity> = settingsDao.getSettings().map { it ?: SettingsEntity() }

    suspend fun getSettingsOnce(): SettingsEntity {
        return settingsDao.getSettingsOnce() ?: SettingsEntity().also {
            settingsDao.insertOrUpdateSettings(it)
        }
    }

    suspend fun updateLanguage(language: AppLanguage) {
        settingsDao.updateLanguage(language)
    }

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        settingsDao.updateThemeMode(themeMode)
    }

    suspend fun updateAccentColor(accentHex: String) {
        settingsDao.updateAccentColor(accentHex)
    }

    suspend fun updateSettings(settingsEntity: SettingsEntity) {
        settingsDao.insertOrUpdateSettings(settingsEntity)
    }
}
