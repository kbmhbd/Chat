package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.*
import org.json.JSONArray
import org.json.JSONObject

class Converters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = try {
        UserRole.valueOf(value)
    } catch (e: Exception) {
        UserRole.USER
    }

    @TypeConverter
    fun fromChatType(value: ChatType): String = value.name

    @TypeConverter
    fun toChatType(value: String): ChatType = try {
        ChatType.valueOf(value)
    } catch (e: Exception) {
        ChatType.DIRECT
    }

    @TypeConverter
    fun fromMemberRole(value: MemberRole): String = value.name

    @TypeConverter
    fun toMemberRole(value: String): MemberRole = try {
        MemberRole.valueOf(value)
    } catch (e: Exception) {
        MemberRole.MEMBER
    }

    @TypeConverter
    fun fromMessageType(value: MessageType): String = value.name

    @TypeConverter
    fun toMessageType(value: String): MessageType = try {
        MessageType.valueOf(value)
    } catch (e: Exception) {
        MessageType.TEXT
    }

    @TypeConverter
    fun fromMessageStatus(value: MessageStatus): String = value.name

    @TypeConverter
    fun toMessageStatus(value: String): MessageStatus = try {
        MessageStatus.valueOf(value)
    } catch (e: Exception) {
        MessageStatus.SENT
    }

    @TypeConverter
    fun fromCallType(value: CallType): String = value.name

    @TypeConverter
    fun toCallType(value: String): CallType = try {
        CallType.valueOf(value)
    } catch (e: Exception) {
        CallType.VOICE
    }

    @TypeConverter
    fun fromCallStatus(value: CallStatus): String = value.name

    @TypeConverter
    fun toCallStatus(value: String): CallStatus = try {
        CallStatus.valueOf(value)
    } catch (e: Exception) {
        CallStatus.COMPLETED
    }

    @TypeConverter
    fun fromStoryPrivacy(value: StoryPrivacy): String = value.name

    @TypeConverter
    fun toStoryPrivacy(value: String): StoryPrivacy = try {
        StoryPrivacy.valueOf(value)
    } catch (e: Exception) {
        StoryPrivacy.EVERYONE
    }

    @TypeConverter
    fun fromTransactionStatus(value: TransactionStatus): String = value.name

    @TypeConverter
    fun toTransactionStatus(value: String): TransactionStatus = try {
        TransactionStatus.valueOf(value)
    } catch (e: Exception) {
        TransactionStatus.COMPLETED
    }

    @TypeConverter
    fun fromReportStatus(value: ReportStatus): String = value.name

    @TypeConverter
    fun toReportStatus(value: String): ReportStatus = try {
        ReportStatus.valueOf(value)
    } catch (e: Exception) {
        ReportStatus.PENDING
    }

    @TypeConverter
    fun fromAppLanguage(value: AppLanguage): String = value.code

    @TypeConverter
    fun toAppLanguage(value: String): AppLanguage = when (value) {
        "bn" -> AppLanguage.BENGALI
        else -> AppLanguage.ENGLISH
    }

    @TypeConverter
    fun fromThemeMode(value: ThemeMode): String = value.name

    @TypeConverter
    fun toThemeMode(value: String): ThemeMode = try {
        ThemeMode.valueOf(value)
    } catch (e: Exception) {
        ThemeMode.SYSTEM
    }
}
