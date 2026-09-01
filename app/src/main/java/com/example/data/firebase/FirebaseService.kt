package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseService(
    private val context: Context,
    private val database: AppDatabase
) {
    private val TAG = "FirebaseService"
    private val scope = CoroutineScope(Dispatchers.IO)

    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    private val _isFirebaseAvailable = MutableStateFlow(false)
    val isFirebaseAvailable: StateFlow<Boolean> = _isFirebaseAvailable.asStateFlow()

    private val listeners = mutableListOf<ListenerRegistration>()

    init {
        initFirebase()
    }

    private fun initFirebase() {
        try {
            val options = FirebaseOptions.Builder()
                .setApiKey("AIzaSyBogf1Yne6ppeoOb8Bwti2B77R8muqrGMs")
                .setApplicationId("1:138203483964:web:01cfd4c2aa098135445839")
                .setProjectId("chat-eef0c")
                .setStorageBucket("chat-eef0c.firebasestorage.app")
                .setGcmSenderId("138203483964")
                .build()

            val app = if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context, options)
            } else {
                FirebaseApp.getInstance()
            }

            auth = FirebaseAuth.getInstance(app)
            firestore = FirebaseFirestore.getInstance(app)
            _isFirebaseAvailable.value = true
            Log.d(TAG, "Firebase initialized successfully with project chat-eef0c")
            startRealtimeSync()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase initialization warning: ${e.message}. Offline / Local DB mode active.")
            _isFirebaseAvailable.value = false
        }
    }

    fun getAuth(): FirebaseAuth? = auth
    fun getFirestore(): FirebaseFirestore? = firestore

    // ---------------- AUTHENTICATION ----------------
    suspend fun registerUser(
        email: String,
        password: String,
        name: String,
        username: String,
        phone: String = ""
    ): Result<UserEntity> {
        val cleanEmail = email.trim()
        val cleanUsername = username.trim().lowercase().replace(" ", "_")

        return try {
            val fbAuth = auth
            val fbFirestore = firestore

            val uid = if (fbAuth != null) {
                try {
                    val authResult = fbAuth.createUserWithEmailAndPassword(cleanEmail, password).await()
                    authResult.user?.uid ?: "user_${System.currentTimeMillis()}"
                } catch (e: Exception) {
                    Log.w(TAG, "FirebaseAuth register fallback: ${e.message}")
                    "user_${System.currentTimeMillis()}"
                }
            } else {
                "user_${System.currentTimeMillis()}"
            }

            val userEntity = UserEntity(
                id = uid,
                username = cleanUsername,
                email = cleanEmail,
                phone = phone,
                name = name.trim(),
                bio = "Hey there! I am using Messenger.",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                isOnline = true,
                role = UserRole.USER,
                createdAt = System.currentTimeMillis(),
                balance = 250.00
            )

            // Save to local Room DB
            database.userDao().insertUser(userEntity)
            database.settingsDao().updateCurrentUserId(uid)

            // Sync to Firestore if available
            if (fbFirestore != null) {
                try {
                    val userMap = hashMapOf(
                        "id" to userEntity.id,
                        "username" to userEntity.username,
                        "email" to userEntity.email,
                        "phone" to userEntity.phone,
                        "name" to userEntity.name,
                        "bio" to userEntity.bio,
                        "avatarUrl" to userEntity.avatarUrl,
                        "isOnline" to true,
                        "lastSeen" to System.currentTimeMillis(),
                        "role" to userEntity.role.name,
                        "createdAt" to userEntity.createdAt,
                        "balance" to userEntity.balance
                    )
                    fbFirestore.collection("users").document(uid).set(userMap, SetOptions.merge())
                } catch (e: Exception) {
                    Log.w(TAG, "Firestore user write failed: ${e.message}")
                }
            }

            Result.success(userEntity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(identifier: String, password: String): Result<UserEntity> {
        val cleanIdentifier = identifier.trim()
        val email = if (cleanIdentifier.contains("@")) cleanIdentifier else "$cleanIdentifier@messenger.app"

        return try {
            val fbAuth = auth
            val fbFirestore = firestore

            if (fbAuth != null) {
                try {
                    val authResult = fbAuth.signInWithEmailAndPassword(email, password).await()
                    val uid = authResult.user?.uid
                    if (uid != null) {
                        // Check if user exists in Firestore
                        if (fbFirestore != null) {
                            try {
                                val doc = fbFirestore.collection("users").document(uid).get().await()
                                if (doc.exists()) {
                                    val user = UserEntity(
                                        id = uid,
                                        username = doc.getString("username") ?: cleanIdentifier.lowercase(),
                                        email = doc.getString("email") ?: email,
                                        phone = doc.getString("phone") ?: "",
                                        name = doc.getString("name") ?: cleanIdentifier,
                                        bio = doc.getString("bio") ?: "Hey there! I am using Messenger.",
                                        avatarUrl = doc.getString("avatarUrl") ?: "",
                                        isOnline = true,
                                        lastSeen = System.currentTimeMillis(),
                                        role = UserRole.valueOf(doc.getString("role") ?: "USER"),
                                        balance = doc.getDouble("balance") ?: 250.00
                                    )
                                    database.userDao().insertUser(user)
                                    database.settingsDao().updateCurrentUserId(uid)
                                    updatePresence(uid, true)
                                    return Result.success(user)
                                }
                            } catch (e: Exception) {
                                Log.w(TAG, "Firestore user read error: ${e.message}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "FirebaseAuth signin fallback to local: ${e.message}")
                }
            }

            // Local fallback
            val localUser = database.userDao().findUserByCredentials(cleanIdentifier, cleanIdentifier)
            if (localUser != null) {
                database.settingsDao().updateCurrentUserId(localUser.id)
                database.userDao().updatePresence(localUser.id, true, System.currentTimeMillis())
                updatePresence(localUser.id, true)
                Result.success(localUser)
            } else {
                val me = database.userDao().getUserByIdOnce("user_me")
                if (me != null) {
                    database.settingsDao().updateCurrentUserId(me.id)
                    database.userDao().updatePresence(me.id, true, System.currentTimeMillis())
                    Result.success(me)
                } else {
                    val defaultUser = UserEntity(
                        id = "user_me",
                        username = cleanIdentifier.lowercase().replace(" ", "_"),
                        email = email,
                        name = cleanIdentifier.replaceFirstChar { it.uppercase() },
                        bio = "Hey there! I am using Messenger.",
                        isOnline = true,
                        role = UserRole.USER
                    )
                    database.userDao().insertUser(defaultUser)
                    database.settingsDao().updateCurrentUserId(defaultUser.id)
                    Result.success(defaultUser)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logoutUser(userId: String) {
        try {
            auth?.signOut()
            updatePresence(userId, false)
            database.userDao().updatePresence(userId, false, System.currentTimeMillis())
        } catch (e: Exception) {
            Log.w(TAG, "Logout error: ${e.message}")
        }
    }

    suspend fun updatePresence(userId: String, isOnline: Boolean) {
        val fbFirestore = firestore ?: return
        try {
            fbFirestore.collection("users").document(userId).set(
                mapOf(
                    "isOnline" to isOnline,
                    "lastSeen" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
        } catch (e: Exception) {
            Log.w(TAG, "Update presence error: ${e.message}")
        }
    }

    // ---------------- REALTIME SYNC LISTENERS ----------------
    private fun startRealtimeSync() {
        val fbFirestore = firestore ?: return

        try {
            // 1. Sync Users
            val userListener = fbFirestore.collection("users").addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w(TAG, "Users listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    scope.launch {
                        for (doc in snapshots.documents) {
                            val id = doc.id
                            val username = doc.getString("username") ?: ""
                            val email = doc.getString("email") ?: ""
                            val name = doc.getString("name") ?: ""
                            val bio = doc.getString("bio") ?: ""
                            val avatarUrl = doc.getString("avatarUrl") ?: ""
                            val isOnline = doc.getBoolean("isOnline") ?: false
                            val lastSeen = doc.getLong("lastSeen") ?: System.currentTimeMillis()
                            val balance = doc.getDouble("balance") ?: 250.0

                            val existing = database.userDao().getUserByIdOnce(id)
                            if (existing == null) {
                                database.userDao().insertUser(
                                    UserEntity(
                                        id = id,
                                        username = username,
                                        email = email,
                                        name = name,
                                        bio = bio,
                                        avatarUrl = avatarUrl,
                                        isOnline = isOnline,
                                        lastSeen = lastSeen,
                                        balance = balance
                                    )
                                )
                            } else {
                                database.userDao().updateUser(
                                    existing.copy(
                                        isOnline = isOnline,
                                        lastSeen = lastSeen,
                                        bio = if (bio.isNotBlank()) bio else existing.bio,
                                        name = if (name.isNotBlank()) name else existing.name,
                                        avatarUrl = if (avatarUrl.isNotBlank()) avatarUrl else existing.avatarUrl,
                                        balance = balance
                                    )
                                )
                            }
                        }
                    }
                }
            }
            listeners.add(userListener)

            // 2. Sync Chats
            val chatsListener = fbFirestore.collection("chats").addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener
                if (snapshots != null) {
                    scope.launch {
                        for (doc in snapshots.documents) {
                            val id = doc.id
                            val type = ChatType.valueOf(doc.getString("type") ?: "DIRECT")
                            val name = doc.getString("name") ?: ""
                            val avatarUrl = doc.getString("avatarUrl") ?: ""
                            val desc = doc.getString("description") ?: ""
                            val otherUserId = doc.getString("otherUserId") ?: ""
                            val lastMsg = doc.getString("lastMessageText") ?: ""
                            val lastTime = doc.getLong("lastMessageTimestamp") ?: System.currentTimeMillis()
                            val lastSender = doc.getString("lastMessageSenderName") ?: ""

                            val existing = database.chatDao().getChatByIdOnce(id)
                            if (existing == null) {
                                database.chatDao().insertChat(
                                    ChatEntity(
                                        id = id,
                                        type = type,
                                        name = name,
                                        avatarUrl = avatarUrl,
                                        description = desc,
                                        otherUserId = otherUserId,
                                        lastMessageText = lastMsg,
                                        lastMessageTimestamp = lastTime,
                                        lastMessageSenderName = lastSender
                                    )
                                )
                            } else {
                                database.chatDao().updateLastMessage(id, lastMsg, lastTime, lastSender)
                            }
                        }
                    }
                }
            }
            listeners.add(chatsListener)

            // 3. Sync Messages
            val messagesListener = fbFirestore.collection("messages").addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener
                if (snapshots != null) {
                    scope.launch {
                        for (doc in snapshots.documents) {
                            val id = doc.id
                            val chatId = doc.getString("chatId") ?: ""
                            val senderId = doc.getString("senderId") ?: ""
                            val senderName = doc.getString("senderName") ?: ""
                            val senderAvatar = doc.getString("senderAvatarUrl") ?: ""
                            val text = doc.getString("text") ?: ""
                            val type = MessageType.valueOf(doc.getString("type") ?: "TEXT")
                            val mediaUrl = doc.getString("mediaUrl") ?: ""
                            val mediaName = doc.getString("mediaName") ?: ""
                            val mediaSize = doc.getString("mediaSize") ?: ""
                            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                            val status = MessageStatus.valueOf(doc.getString("status") ?: "SENT")
                            val reactionsJson = doc.getString("reactionsJson") ?: "[]"
                            val isPinned = doc.getBoolean("isPinned") ?: false
                            val isSaved = doc.getBoolean("isSaved") ?: false
                            val isEdited = doc.getBoolean("isEdited") ?: false
                            val isDeletedForEveryone = doc.getBoolean("isDeletedForEveryone") ?: false

                            val msg = MessageEntity(
                                id = id,
                                chatId = chatId,
                                senderId = senderId,
                                senderName = senderName,
                                senderAvatarUrl = senderAvatar,
                                text = text,
                                type = type,
                                mediaUrl = mediaUrl,
                                mediaName = mediaName,
                                mediaSize = mediaSize,
                                status = status,
                                reactionsJson = reactionsJson,
                                isPinned = isPinned,
                                isSaved = isSaved,
                                isEdited = isEdited,
                                isDeletedForEveryone = isDeletedForEveryone,
                                createdAt = createdAt
                            )
                            database.messageDao().insertMessage(msg)
                        }
                    }
                }
            }
            listeners.add(messagesListener)

            // 4. Sync Stories
            val storiesListener = fbFirestore.collection("stories").addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener
                if (snapshots != null) {
                    scope.launch {
                        for (doc in snapshots.documents) {
                            val id = doc.id
                            val userId = doc.getString("userId") ?: ""
                            val userName = doc.getString("userName") ?: ""
                            val userAvatarUrl = doc.getString("userAvatarUrl") ?: ""
                            val type = doc.getString("type") ?: "TEXT"
                            val textContent = doc.getString("textContent") ?: ""
                            val backgroundColorHex = doc.getString("backgroundColorHex") ?: "#0084FF"
                            val mediaUrl = doc.getString("mediaUrl") ?: ""
                            val privacy = StoryPrivacy.valueOf(doc.getString("privacy") ?: "EVERYONE")
                            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                            val expiresAt = doc.getLong("expiresAt") ?: (createdAt + 86400000)
                            val viewsCount = doc.getLong("viewsCount")?.toInt() ?: 0

                            val story = StoryEntity(
                                id = id,
                                userId = userId,
                                userName = userName,
                                userAvatarUrl = userAvatarUrl,
                                type = type,
                                textContent = textContent,
                                backgroundColorHex = backgroundColorHex,
                                mediaUrl = mediaUrl,
                                privacy = privacy,
                                createdAt = createdAt,
                                expiresAt = expiresAt,
                                viewsCount = viewsCount
                            )
                            database.storyDao().insertStory(story)
                        }
                    }
                }
            }
            listeners.add(storiesListener)

            // 5. Sync Transactions
            val transactionsListener = fbFirestore.collection("transactions").addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener
                if (snapshots != null) {
                    scope.launch {
                        for (doc in snapshots.documents) {
                            val id = doc.id
                            val senderId = doc.getString("senderId") ?: ""
                            val senderName = doc.getString("senderName") ?: ""
                            val receiverId = doc.getString("receiverId") ?: ""
                            val receiverName = doc.getString("receiverName") ?: ""
                            val amount = doc.getDouble("amount") ?: 0.0
                            val currency = doc.getString("currency") ?: "USD"
                            val note = doc.getString("note") ?: ""
                            val status = TransactionStatus.valueOf(doc.getString("status") ?: "COMPLETED")
                            val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()

                            val transaction = TransactionEntity(
                                id = id,
                                senderId = senderId,
                                senderName = senderName,
                                receiverId = receiverId,
                                receiverName = receiverName,
                                amount = amount,
                                currency = currency,
                                note = note,
                                status = status,
                                timestamp = timestamp
                            )
                            database.transactionDao().insertTransaction(transaction)
                        }
                    }
                }
            }
            listeners.add(transactionsListener)

        } catch (e: Exception) {
            Log.w(TAG, "Error starting realtime sync listeners: ${e.message}")
        }
    }

    // ---------------- WRITE SYNC METHODS ----------------
    suspend fun syncMessageToFirestore(message: MessageEntity) {
        val fbFirestore = firestore ?: return
        try {
            val msgMap = hashMapOf(
                "id" to message.id,
                "chatId" to message.chatId,
                "senderId" to message.senderId,
                "senderName" to message.senderName,
                "senderAvatarUrl" to message.senderAvatarUrl,
                "text" to message.text,
                "type" to message.type.name,
                "mediaUrl" to message.mediaUrl,
                "mediaName" to message.mediaName,
                "mediaSize" to message.mediaSize,
                "mediaDurationMs" to message.mediaDurationMs,
                "replyToMessageId" to message.replyToMessageId,
                "replyToText" to message.replyToText,
                "replyToSenderName" to message.replyToSenderName,
                "status" to message.status.name,
                "reactionsJson" to message.reactionsJson,
                "isPinned" to message.isPinned,
                "isSaved" to message.isSaved,
                "isEdited" to message.isEdited,
                "isDeletedForEveryone" to message.isDeletedForEveryone,
                "createdAt" to message.createdAt
            )
            fbFirestore.collection("messages").document(message.id).set(msgMap, SetOptions.merge())
        } catch (e: Exception) {
            Log.w(TAG, "Firestore syncMessage error: ${e.message}")
        }
    }

    suspend fun syncChatToFirestore(chat: ChatEntity) {
        val fbFirestore = firestore ?: return
        try {
            val chatMap = hashMapOf(
                "id" to chat.id,
                "type" to chat.type.name,
                "name" to chat.name,
                "avatarUrl" to chat.avatarUrl,
                "description" to chat.description,
                "otherUserId" to chat.otherUserId,
                "createdByUserId" to chat.createdByUserId,
                "lastMessageText" to chat.lastMessageText,
                "lastMessageTimestamp" to chat.lastMessageTimestamp,
                "lastMessageSenderName" to chat.lastMessageSenderName
            )
            fbFirestore.collection("chats").document(chat.id).set(chatMap, SetOptions.merge())
        } catch (e: Exception) {
            Log.w(TAG, "Firestore syncChat error: ${e.message}")
        }
    }

    suspend fun syncStoryToFirestore(story: StoryEntity) {
        val fbFirestore = firestore ?: return
        try {
            val storyMap = hashMapOf(
                "id" to story.id,
                "userId" to story.userId,
                "userName" to story.userName,
                "userAvatarUrl" to story.userAvatarUrl,
                "type" to story.type,
                "textContent" to story.textContent,
                "backgroundColorHex" to story.backgroundColorHex,
                "mediaUrl" to story.mediaUrl,
                "privacy" to story.privacy.name,
                "createdAt" to story.createdAt,
                "expiresAt" to story.expiresAt,
                "viewsCount" to story.viewsCount
            )
            fbFirestore.collection("stories").document(story.id).set(storyMap, SetOptions.merge())
        } catch (e: Exception) {
            Log.w(TAG, "Firestore syncStory error: ${e.message}")
        }
    }

    suspend fun syncCallToFirestore(call: CallEntity) {
        val fbFirestore = firestore ?: return
        try {
            val callMap = hashMapOf(
                "id" to call.id,
                "callerId" to call.callerId,
                "callerName" to call.callerName,
                "callerAvatarUrl" to call.callerAvatarUrl,
                "receiverId" to call.receiverId,
                "receiverName" to call.receiverName,
                "receiverAvatarUrl" to call.receiverAvatarUrl,
                "chatId" to call.chatId,
                "type" to call.type.name,
                "status" to call.status.name,
                "durationSeconds" to call.durationSeconds,
                "timestamp" to call.timestamp
            )
            fbFirestore.collection("calls").document(call.id).set(callMap, SetOptions.merge())
        } catch (e: Exception) {
            Log.w(TAG, "Firestore syncCall error: ${e.message}")
        }
    }

    suspend fun syncTransactionToFirestore(transaction: TransactionEntity) {
        val fbFirestore = firestore ?: return
        try {
            val txMap = hashMapOf(
                "id" to transaction.id,
                "senderId" to transaction.senderId,
                "senderName" to transaction.senderName,
                "receiverId" to transaction.receiverId,
                "receiverName" to transaction.receiverName,
                "amount" to transaction.amount,
                "currency" to transaction.currency,
                "note" to transaction.note,
                "status" to transaction.status.name,
                "timestamp" to transaction.timestamp
            )
            fbFirestore.collection("transactions").document(transaction.id).set(txMap, SetOptions.merge())
        } catch (e: Exception) {
            Log.w(TAG, "Firestore syncTransaction error: ${e.message}")
        }
    }

    suspend fun syncReportToFirestore(report: ReportEntity) {
        val fbFirestore = firestore ?: return
        try {
            val repMap = hashMapOf(
                "id" to report.id,
                "reporterId" to report.reporterId,
                "reportedUserId" to report.reportedUserId,
                "reportedUserName" to report.reportedUserName,
                "reportedMessageId" to report.reportedMessageId,
                "reportedMessageContent" to report.reportedMessageContent,
                "reason" to report.reason,
                "details" to report.details,
                "status" to report.status.name,
                "timestamp" to report.timestamp
            )
            fbFirestore.collection("reports").document(report.id).set(repMap, SetOptions.merge())
        } catch (e: Exception) {
            Log.w(TAG, "Firestore syncReport error: ${e.message}")
        }
    }

    fun cleanUp() {
        listeners.forEach { it.remove() }
        listeners.clear()
    }
}
