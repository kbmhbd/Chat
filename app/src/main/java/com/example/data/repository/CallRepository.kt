package com.example.data.repository

import com.example.data.firebase.FirebaseService
import com.example.data.local.dao.CallDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.CallEntity
import com.example.data.model.CallState
import com.example.data.model.CallStatus
import com.example.data.model.CallType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CallRepository(
    private val callDao: CallDao,
    private val userDao: UserDao,
    private val firebaseService: FirebaseService
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _currentCallState = MutableStateFlow<CallState?>(null)
    val currentCallState: StateFlow<CallState?> = _currentCallState.asStateFlow()

    fun getAllCalls(): Flow<List<CallEntity>> = callDao.getAllCalls()
    fun getMissedCalls(): Flow<List<CallEntity>> = callDao.getMissedCalls()

    suspend fun startOutgoingCall(
        receiverId: String,
        receiverName: String,
        receiverAvatarUrl: String,
        callType: CallType
    ) {
        val user = userDao.getUserByIdOnce("user_me")
        val callId = "call_${UUID.randomUUID().toString().take(8)}"

        val callState = CallState(
            isInCall = true,
            callId = callId,
            callerId = "user_me",
            callerName = user?.name ?: "You",
            callerAvatar = user?.avatarUrl ?: "",
            receiverId = receiverId,
            receiverName = receiverName,
            receiverAvatar = receiverAvatarUrl,
            callType = callType,
            isIncoming = false,
            isConnected = false,
            durationSeconds = 0
        )
        _currentCallState.value = callState

        // Simulate connecting after 2.5 seconds
        scope.launch {
            delay(2500)
            _currentCallState.value?.let { current ->
                if (current.isInCall && !current.isConnected) {
                    _currentCallState.value = current.copy(isConnected = true)
                    // Start call timer loop
                    while (_currentCallState.value?.isInCall == true) {
                        delay(1000)
                        val st = _currentCallState.value ?: break
                        _currentCallState.value = st.copy(durationSeconds = st.durationSeconds + 1)
                    }
                }
            }
        }
    }

    suspend fun receiveIncomingCall(
        callerId: String,
        callerName: String,
        callerAvatarUrl: String,
        callType: CallType
    ) {
        val user = userDao.getUserByIdOnce("user_me")
        val callId = "call_${UUID.randomUUID().toString().take(8)}"

        _currentCallState.value = CallState(
            isInCall = true,
            callId = callId,
            callerId = callerId,
            callerName = callerName,
            callerAvatar = callerAvatarUrl,
            receiverId = "user_me",
            receiverName = user?.name ?: "You",
            receiverAvatar = user?.avatarUrl ?: "",
            callType = callType,
            isIncoming = true,
            isConnected = false,
            durationSeconds = 0
        )
    }

    fun acceptIncomingCall() {
        val current = _currentCallState.value ?: return
        _currentCallState.value = current.copy(isConnected = true, isIncoming = false)
        scope.launch {
            while (_currentCallState.value?.isInCall == true) {
                delay(1000)
                val st = _currentCallState.value ?: break
                _currentCallState.value = st.copy(durationSeconds = st.durationSeconds + 1)
            }
        }
    }

    suspend fun endCurrentCall(status: CallStatus = CallStatus.COMPLETED) {
        val current = _currentCallState.value ?: return
        val record = CallEntity(
            id = current.callId,
            callerId = current.callerId,
            callerName = current.callerName,
            callerAvatarUrl = current.callerAvatar,
            receiverId = current.receiverId,
            receiverName = current.receiverName,
            receiverAvatarUrl = current.receiverAvatar,
            type = current.callType,
            status = if (!current.isConnected && current.isIncoming) CallStatus.MISSED else status,
            durationSeconds = current.durationSeconds,
            timestamp = System.currentTimeMillis()
        )
        callDao.insertCall(record)
        firebaseService.syncCallToFirestore(record)
        _currentCallState.value = null
    }

    fun toggleMicMute() {
        val current = _currentCallState.value ?: return
        _currentCallState.value = current.copy(isMicMuted = !current.isMicMuted)
    }

    fun toggleSpeaker() {
        val current = _currentCallState.value ?: return
        _currentCallState.value = current.copy(isSpeakerOn = !current.isSpeakerOn)
    }

    fun toggleVideo() {
        val current = _currentCallState.value ?: return
        _currentCallState.value = current.copy(isVideoEnabled = !current.isVideoEnabled)
    }

    fun switchCamera() {
        val current = _currentCallState.value ?: return
        _currentCallState.value = current.copy(isFrontCamera = !current.isFrontCamera)
    }

    suspend fun deleteCall(callId: String) {
        callDao.deleteCallById(callId)
    }

    suspend fun clearHistory() {
        callDao.clearCallHistory()
    }
}
