package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.CallEntity
import com.example.data.model.CallState
import com.example.data.model.CallStatus
import com.example.data.model.CallType
import com.example.data.repository.CallRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CallViewModel(
    private val callRepository: CallRepository
) : ViewModel() {

    val callHistory: StateFlow<List<CallEntity>> = callRepository.getAllCalls()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val missedCalls: StateFlow<List<CallEntity>> = callRepository.getMissedCalls()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentCallState: StateFlow<CallState?> = callRepository.currentCallState

    fun startCall(receiverId: String, receiverName: String, receiverAvatarUrl: String, callType: CallType) {
        viewModelScope.launch {
            callRepository.startOutgoingCall(receiverId, receiverName, receiverAvatarUrl, callType)
        }
    }

    fun simulateIncomingCall(callerId: String, callerName: String, callerAvatarUrl: String, callType: CallType) {
        viewModelScope.launch {
            callRepository.receiveIncomingCall(callerId, callerName, callerAvatarUrl, callType)
        }
    }

    fun acceptCall() {
        callRepository.acceptIncomingCall()
    }

    fun endCall(status: CallStatus = CallStatus.COMPLETED) {
        viewModelScope.launch {
            callRepository.endCurrentCall(status)
        }
    }

    fun toggleMicMute() = callRepository.toggleMicMute()
    fun toggleSpeaker() = callRepository.toggleSpeaker()
    fun toggleVideo() = callRepository.toggleVideo()
    fun switchCamera() = callRepository.switchCamera()

    fun deleteCall(callId: String) {
        viewModelScope.launch { callRepository.deleteCall(callId) }
    }

    fun clearHistory() {
        viewModelScope.launch { callRepository.clearHistory() }
    }
}

class CallViewModelFactory(private val repository: CallRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CallViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CallViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
