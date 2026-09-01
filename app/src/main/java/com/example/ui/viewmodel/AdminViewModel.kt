package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ReportEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.AdminRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(
    private val adminRepository: AdminRepository
) : ViewModel() {

    val totalUsersCount: StateFlow<Int> = adminRepository.totalUsersCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val onlineUsersCount: StateFlow<Int> = adminRepository.onlineUsersCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalMessagesCount: StateFlow<Int> = adminRepository.totalMessagesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalChatsCount: StateFlow<Int> = adminRepository.totalChatsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allUsers: StateFlow<List<UserEntity>> = adminRepository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingReports: StateFlow<List<ReportEntity>> = adminRepository.pendingReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun banUser(userId: String) {
        viewModelScope.launch { adminRepository.banUser(userId) }
    }

    fun unbanUser(userId: String) {
        viewModelScope.launch { adminRepository.unbanUser(userId) }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch { adminRepository.deleteUser(userId) }
    }

    fun submitReport(
        reporterId: String = "user_me",
        reportedUserId: String,
        reportedUserName: String,
        reason: String,
        details: String = "",
        messageId: String? = null,
        messageContent: String? = null
    ) {
        viewModelScope.launch {
            adminRepository.reportUserOrMessage(
                reportedUserId,
                reportedUserName,
                reason,
                details,
                messageId,
                messageContent
            )
        }
    }

    fun resolveReport(reportId: String) {
        viewModelScope.launch { adminRepository.resolveReport(reportId) }
    }

    fun dismissReport(reportId: String) {
        viewModelScope.launch { adminRepository.dismissReport(reportId) }
    }
}

class AdminViewModelFactory(private val repository: AdminRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
