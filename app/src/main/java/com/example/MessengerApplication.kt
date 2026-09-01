package com.example

import android.app.Application
import com.example.data.firebase.FirebaseService
import com.example.data.local.AppDatabase
import com.example.data.repository.*
import com.example.data.seed.DatabaseSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MessengerApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { AppDatabase.getDatabase(this) }

    val firebaseService by lazy {
        FirebaseService(this, database)
    }

    val authRepository by lazy {
        AuthRepository(database.userDao(), database.settingsDao(), firebaseService)
    }

    val chatRepository by lazy {
        ChatRepository(
            chatDao = database.chatDao(),
            messageDao = database.messageDao(),
            userDao = database.userDao(),
            chatMemberDao = database.chatMemberDao(),
            firebaseService = firebaseService
        )
    }

    val storyRepository by lazy {
        StoryRepository(database.storyDao(), database.userDao(), firebaseService)
    }

    val callRepository by lazy {
        CallRepository(database.callDao(), database.userDao(), firebaseService)
    }

    val paymentRepository by lazy {
        PaymentRepository(database.transactionDao(), database.userDao(), firebaseService)
    }

    val settingsRepository by lazy {
        SettingsRepository(database.settingsDao())
    }

    val adminRepository by lazy {
        AdminRepository(
            userDao = database.userDao(),
            chatDao = database.chatDao(),
            messageDao = database.messageDao(),
            reportDao = database.reportDao(),
            firebaseService = firebaseService
        )
    }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            DatabaseSeeder.seedDatabaseIfEmpty(database)
        }
    }
}
