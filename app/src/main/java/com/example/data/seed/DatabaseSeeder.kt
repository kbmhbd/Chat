package com.example.data.seed

import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.model.*

object DatabaseSeeder {
    suspend fun seedDatabaseIfEmpty(db: AppDatabase) {
        val existingUser = db.userDao().getUserByIdOnce("user_me")
        if (existingUser != null) return

        // 1. Current logged-in user
        val me = UserEntity(
            id = "user_me",
            username = "rashed_dev",
            email = "rashed@example.com",
            phone = "+880 1712 345678",
            name = "Rashed Miah",
            bio = "Software Engineer & Designer | Building next-gen communication tech 🚀",
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
            coverUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600",
            isOnline = true,
            role = UserRole.ADMIN,
            balance = 480.50
        )
        db.userDao().insertUser(me)

        // 2. Initial Contacts
        val users = listOf(
            UserEntity(
                id = "user_sarah",
                username = "sarah_k",
                email = "sarah@example.com",
                name = "Sarah Khan",
                bio = "Design Lead @ Studio. Coffee & Figma 🎨",
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                isOnline = true,
                lastSeen = System.currentTimeMillis()
            ),
            UserEntity(
                id = "user_tariq",
                username = "tariq_ahmed",
                email = "tariq@example.com",
                name = "Tariq Ahmed",
                bio = "Mobile Architect | Kotlin & Compose enthusiast 📱",
                avatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150",
                isOnline = true,
                lastSeen = System.currentTimeMillis() - 5 * 60 * 1000L
            ),
            UserEntity(
                id = "user_anika",
                username = "anika_r",
                email = "anika@example.com",
                name = "Anika Rahman",
                bio = "Exploring photography & music 🎵📷",
                avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150",
                isOnline = false,
                lastSeen = System.currentTimeMillis() - 45 * 60 * 1000L
            ),
            UserEntity(
                id = "user_tanvir",
                username = "tanvir_h",
                email = "tanvir@example.com",
                name = "Tanvir Hasan",
                bio = "Product Manager | Weekend Cyclist 🚴",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                isOnline = false,
                lastSeen = System.currentTimeMillis() - 3 * 3600 * 1000L
            ),
            UserEntity(
                id = "user_ai_bot",
                username = "messenger_ai",
                email = "ai@messenger.internal",
                name = "Messenger AI Assistant",
                bio = "Your smart real-time AI companion for writing, translations, and ideas 🤖",
                avatarUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150",
                isOnline = true,
                role = UserRole.ADMIN
            )
        )
        db.userDao().insertUsers(users)

        // 3. Initial Chats
        val chats = listOf(
            ChatEntity(
                id = "chat_ai",
                type = ChatType.AI,
                name = "Messenger AI Assistant",
                avatarUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150",
                description = "Ask questions, get help writing or translating messages",
                isPinned = true,
                lastMessageText = "Hello Rashed! How can I assist you with your chats or writing today?",
                lastMessageTimestamp = System.currentTimeMillis() - 2 * 60 * 1000L,
                lastMessageSenderName = "Messenger AI"
            ),
            ChatEntity(
                id = "chat_sarah",
                type = ChatType.DIRECT,
                name = "Sarah Khan",
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                otherUserId = "user_sarah",
                description = "Design Lead @ Studio",
                isPinned = true,
                lastMessageText = "Just sent over the revised design prototype! Let me know what you think.",
                lastMessageTimestamp = System.currentTimeMillis() - 8 * 60 * 1000L,
                lastMessageSenderName = "Sarah",
                unreadCount = 1
            ),
            ChatEntity(
                id = "chat_group_dev",
                type = ChatType.GROUP,
                name = "Product & Engineering",
                avatarUrl = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=150",
                description = "Official project collaboration group for development and launches",
                createdByUserId = "user_me",
                lastMessageText = "Tariq: The build performance is looking super smooth! 🚀",
                lastMessageTimestamp = System.currentTimeMillis() - 25 * 60 * 1000L,
                lastMessageSenderName = "Tariq Ahmed",
                unreadCount = 2
            ),
            ChatEntity(
                id = "chat_tariq",
                type = ChatType.DIRECT,
                name = "Tariq Ahmed",
                avatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150",
                otherUserId = "user_tariq",
                description = "Mobile Architect",
                lastMessageText = "See you at the tech meetup tomorrow at 6 PM.",
                lastMessageTimestamp = System.currentTimeMillis() - 2 * 3600 * 1000L,
                lastMessageSenderName = "Tariq"
            ),
            ChatEntity(
                id = "chat_anika",
                type = ChatType.DIRECT,
                name = "Anika Rahman",
                avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150",
                otherUserId = "user_anika",
                description = "Exploring photography & music",
                lastMessageText = "Thanks for sending the payment receipt! 🙏",
                lastMessageTimestamp = System.currentTimeMillis() - 24 * 3600 * 1000L,
                lastMessageSenderName = "Anika"
            )
        )
        db.chatDao().insertChats(chats)

        // 4. Group Members
        val members = listOf(
            ChatMemberEntity("chat_group_dev", "user_me", MemberRole.OWNER),
            ChatMemberEntity("chat_group_dev", "user_sarah", MemberRole.ADMIN),
            ChatMemberEntity("chat_group_dev", "user_tariq", MemberRole.MEMBER),
            ChatMemberEntity("chat_group_dev", "user_tanvir", MemberRole.MEMBER)
        )
        db.chatMemberDao().insertMembers(members)

        // 5. Initial Messages
        val now = System.currentTimeMillis()
        val messages = listOf(
            // Chat AI
            MessageEntity(
                id = "msg_ai_1",
                chatId = "chat_ai",
                senderId = "user_ai_bot",
                senderName = "Messenger AI",
                senderAvatarUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150",
                text = "Hello Rashed! How can I assist you with your chats or writing today?",
                type = MessageType.TEXT,
                createdAt = now - 2 * 60 * 1000L
            ),
            // Chat Sarah
            MessageEntity(
                id = "msg_sarah_1",
                chatId = "chat_sarah",
                senderId = "user_me",
                senderName = "You",
                text = "Hey Sarah! Have you had a chance to finalize the mobile chat interface?",
                type = MessageType.TEXT,
                status = MessageStatus.READ,
                createdAt = now - 30 * 60 * 1000L
            ),
            MessageEntity(
                id = "msg_sarah_2",
                chatId = "chat_sarah",
                senderId = "user_sarah",
                senderName = "Sarah Khan",
                senderAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                text = "Yes, absolutely! The new layout has crisp bubble radius, smooth animations, and voice waveforms.",
                type = MessageType.TEXT,
                status = MessageStatus.READ,
                createdAt = now - 15 * 60 * 1000L
            ),
            MessageEntity(
                id = "msg_sarah_3",
                chatId = "chat_sarah",
                senderId = "user_sarah",
                senderName = "Sarah Khan",
                senderAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                text = "Just sent over the revised design prototype! Let me know what you think.",
                type = MessageType.TEXT,
                status = MessageStatus.DELIVERED,
                isPinned = true,
                createdAt = now - 8 * 60 * 1000L
            ),
            // Chat Group Dev
            MessageEntity(
                id = "msg_group_1",
                chatId = "chat_group_dev",
                senderId = "user_me",
                senderName = "You",
                text = "Sprint planning is at 11:00 AM today. Please review the roadmap.",
                type = MessageType.TEXT,
                status = MessageStatus.READ,
                isPinned = true,
                createdAt = now - 60 * 60 * 1000L
            ),
            MessageEntity(
                id = "msg_group_2",
                chatId = "chat_group_dev",
                senderId = "user_tariq",
                senderName = "Tariq Ahmed",
                senderAvatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150",
                text = "The build performance is looking super smooth! 🚀",
                type = MessageType.TEXT,
                status = MessageStatus.READ,
                createdAt = now - 25 * 60 * 1000L
            )
        )
        db.messageDao().insertMessages(messages)

        // 6. Initial Stories
        val stories = listOf(
            StoryEntity(
                id = "story_sarah_1",
                userId = "user_sarah",
                userName = "Sarah Khan",
                userAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                type = "TEXT",
                textContent = "Designing new features late into the evening! Creativity in full flow ✨☕",
                backgroundColorHex = "#A033FF",
                createdAt = now - 3 * 3600 * 1000L,
                viewsCount = 14
            ),
            StoryEntity(
                id = "story_tariq_1",
                userId = "user_tariq",
                userName = "Tariq Ahmed",
                userAvatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150",
                type = "TEXT",
                textContent = "Kotlin 2.2 + Compose is blazing fast! Building great experiences ⚡",
                backgroundColorHex = "#0084FF",
                createdAt = now - 6 * 3600 * 1000L,
                viewsCount = 28
            )
        )
        db.storyDao().insertStories(stories)

        // 7. Initial Calls
        val calls = listOf(
            CallEntity(
                id = "call_1",
                callerId = "user_sarah",
                callerName = "Sarah Khan",
                callerAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                receiverId = "user_me",
                receiverName = "You",
                type = CallType.VIDEO,
                status = CallStatus.COMPLETED,
                durationSeconds = 245,
                timestamp = now - 4 * 3600 * 1000L
            ),
            CallEntity(
                id = "call_2",
                callerId = "user_tariq",
                callerName = "Tariq Ahmed",
                callerAvatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150",
                receiverId = "user_me",
                receiverName = "You",
                type = CallType.VOICE,
                status = CallStatus.MISSED,
                durationSeconds = 0,
                timestamp = now - 28 * 3600 * 1000L
            )
        )
        db.callDao().insertCalls(calls)

        // 8. Initial Transactions
        val transactions = listOf(
            TransactionEntity(
                id = "tx_init_1",
                senderId = "user_anika",
                senderName = "Anika Rahman",
                receiverId = "user_me",
                receiverName = "Rashed Miah",
                amount = 50.00,
                currency = "$",
                note = "Dinner split payment 🍕",
                status = TransactionStatus.COMPLETED,
                timestamp = now - 24 * 3600 * 1000L
            )
        )
        db.transactionDao().insertTransactions(transactions)

        // 9. Initial Settings
        val settings = SettingsEntity(
            id = 1,
            currentUserId = "user_me",
            language = AppLanguage.ENGLISH,
            themeMode = ThemeMode.SYSTEM,
            accentColorHex = "#0084FF"
        )
        db.settingsDao().insertOrUpdateSettings(settings)
    }
}
