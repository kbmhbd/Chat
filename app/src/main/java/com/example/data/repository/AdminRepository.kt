package com.example.data.repository

import com.example.data.firebase.FirebaseService
import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.MessageDao
import com.example.data.local.dao.ReportDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.ReportEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.ReportStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AdminRepository(
    private val userDao: UserDao,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val reportDao: ReportDao,
    private val firebaseService: FirebaseService
) {
    val totalUsersCount: Flow<Int> = userDao.getTotalUsersCount()
    val onlineUsersCount: Flow<Int> = userDao.getOnlineUsersCount()
    val totalMessagesCount: Flow<Int> = messageDao.getTotalMessagesCount()
    val totalChatsCount: Flow<Int> = chatDao.getTotalChatsCount()
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val pendingReports: Flow<List<ReportEntity>> = reportDao.getPendingReports()
    val allReports: Flow<List<ReportEntity>> = reportDao.getAllReports()

    suspend fun banUser(userId: String) {
        userDao.setSuspendedStatus(userId, true)
    }

    suspend fun unbanUser(userId: String) {
        userDao.setSuspendedStatus(userId, false)
    }

    suspend fun deleteUser(userId: String) {
        userDao.deleteUserById(userId)
    }

    suspend fun reportUserOrMessage(
        reportedUserId: String,
        reportedUserName: String,
        reason: String,
        details: String = "",
        messageId: String? = null,
        messageContent: String? = null
    ) {
        val report = ReportEntity(
            id = "rep_${UUID.randomUUID().toString().take(8)}",
            reporterId = "user_me",
            reportedUserId = reportedUserId,
            reportedUserName = reportedUserName,
            reportedMessageId = messageId,
            reportedMessageContent = messageContent,
            reason = reason,
            details = details,
            status = ReportStatus.PENDING,
            timestamp = System.currentTimeMillis()
        )
        reportDao.insertReport(report)
        firebaseService.syncReportToFirestore(report)
    }

    suspend fun resolveReport(reportId: String) {
        reportDao.updateReportStatus(reportId, ReportStatus.RESOLVED)
    }

    suspend fun dismissReport(reportId: String) {
        reportDao.updateReportStatus(reportId, ReportStatus.REVIEWED)
    }
}
