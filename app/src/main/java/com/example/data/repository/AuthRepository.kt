package com.example.data.repository

import com.example.data.firebase.FirebaseService
import com.example.data.local.dao.SettingsDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest

class AuthRepository(
    private val userDao: UserDao,
    private val settingsDao: SettingsDao,
    private val firebaseService: FirebaseService
) {
    private val _currentUserId = MutableStateFlow("user_me")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentUser: Flow<UserEntity?> = _currentUserId.flatMapLatest { uid ->
        userDao.getUserById(uid)
    }

    suspend fun login(identifier: String, password: String): Result<UserEntity> {
        val res = firebaseService.loginUser(identifier, password)
        if (res.isSuccess) {
            val user = res.getOrThrow()
            _currentUserId.value = user.id
            settingsDao.updateCurrentUserId(user.id)
        }
        return res
    }

    suspend fun register(
        name: String,
        username: String,
        email: String,
        phone: String,
        password: String
    ): Result<UserEntity> {
        if (name.isBlank() || username.isBlank() || (email.isBlank() && phone.isBlank()) || password.isBlank()) {
            return Result.failure(IllegalArgumentException("All mandatory fields must be filled"))
        }

        val res = firebaseService.registerUser(
            email = email.ifBlank { "${username.trim().lowercase()}@messenger.app" },
            password = password,
            name = name,
            username = username,
            phone = phone
        )
        if (res.isSuccess) {
            val user = res.getOrThrow()
            _currentUserId.value = user.id
            settingsDao.updateCurrentUserId(user.id)
        }
        return res
    }

    suspend fun logout() {
        firebaseService.logoutUser(_currentUserId.value)
    }

    suspend fun updateProfile(name: String, username: String, bio: String, avatarUrl: String) {
        val current = userDao.getUserByIdOnce(_currentUserId.value) ?: return
        val updated = current.copy(
            name = name.ifBlank { current.name },
            username = username.ifBlank { current.username },
            bio = bio,
            avatarUrl = avatarUrl.ifBlank { current.avatarUrl }
        )
        userDao.updateUser(updated)
        firebaseService.getFirestore()?.collection("users")?.document(updated.id)?.set(
            mapOf(
                "name" to updated.name,
                "username" to updated.username,
                "bio" to updated.bio,
                "avatarUrl" to updated.avatarUrl
            ),
            com.google.firebase.firestore.SetOptions.merge()
        )
    }

    suspend fun toggleActiveStatus(isEnabled: Boolean) {
        val current = userDao.getUserByIdOnce(_currentUserId.value) ?: return
        userDao.updateUser(current.copy(isActiveStatusOn = isEnabled, isOnline = isEnabled))
        firebaseService.updatePresence(current.id, isEnabled)
    }
}
