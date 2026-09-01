package com.example.data.model

enum class UserRole {
    USER,
    ADMIN
}

enum class ChatType {
    DIRECT,
    GROUP,
    AI
}

enum class MemberRole {
    OWNER,
    ADMIN,
    MEMBER
}

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    VOICE,
    FILE,
    LOCATION,
    CONTACT,
    PAYMENT,
    SYSTEM,
    STICKER
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

enum class CallType {
    VOICE,
    VIDEO
}

enum class CallStatus {
    INCOMING,
    OUTGOING,
    MISSED,
    COMPLETED,
    REJECTED
}

enum class StoryPrivacy {
    EVERYONE,
    CONTACTS,
    ONLY_ME
}

enum class TransactionStatus {
    COMPLETED,
    PENDING,
    FAILED
}

enum class ReportStatus {
    PENDING,
    REVIEWED,
    RESOLVED
}

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English 🇬🇧"),
    BENGALI("bn", "বাংলা 🇧🇩")
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class MessageReaction(
    val emoji: String,
    val userId: String,
    val userName: String
)

data class CallState(
    val isInCall: Boolean = false,
    val callId: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val callerAvatar: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val receiverAvatar: String = "",
    val callType: CallType = CallType.VOICE,
    val isIncoming: Boolean = false,
    val isConnected: Boolean = false,
    val durationSeconds: Int = 0,
    val isMicMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isVideoEnabled: Boolean = true,
    val isFrontCamera: Boolean = true
)
