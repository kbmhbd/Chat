package com.example.ui.screens.call

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.CallStatus
import com.example.data.model.CallType
import com.example.ui.components.UserAvatar
import com.example.ui.components.VoiceWaveformBar
import com.example.ui.viewmodel.CallViewModel
import com.example.util.DateTimeUtils
import com.example.util.Localization

@Composable
fun ActiveCallScreen(
    callViewModel: CallViewModel,
    language: AppLanguage,
    onCallEnded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val callState by callViewModel.currentCallState.collectAsState()

    val state = callState
    if (state == null || !state.isInCall) {
        LaunchedEffect(Unit) {
            onCallEnded()
        }
        return
    }

    val otherName = if (state.isIncoming) state.callerName else state.receiverName
    val otherAvatar = if (state.isIncoming) state.callerAvatar else state.receiverAvatar

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section (Call info & timer)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (state.callType == CallType.VIDEO) Localization.getString("video_call", language) else Localization.getString("voice_call", language),
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = otherName,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (!state.isConnected) {
                        if (state.isIncoming) Localization.getString("incoming_call", language) else Localization.getString("ringing", language)
                    } else {
                        DateTimeUtils.formatDuration(state.durationSeconds)
                    },
                    color = if (state.isConnected) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Center Section (Avatar or Simulated Video)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (state.callType == CallType.VIDEO && state.isVideoEnabled && state.isConnected) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(300.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            UserAvatar(avatarUrl = otherAvatar, name = otherName, size = 100.dp)
                            Text(
                                text = "HD Live Video Feed",
                                color = Color.White.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(8.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        UserAvatar(avatarUrl = otherAvatar, name = otherName, size = 120.dp)
                    }

                    if (state.isConnected) {
                        Spacer(modifier = Modifier.height(24.dp))
                        VoiceWaveformBar(
                            amplitudes = listOf(0.4f, 0.8f, 0.3f, 0.9f, 0.6f, 0.2f, 0.7f, 0.5f, 0.9f, 0.4f),
                            progress = 0.5f,
                            activeColor = Color(0xFF0084FF),
                            inactiveColor = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            // Bottom Section (Call Controls)
            if (state.isIncoming && !state.isConnected) {
                // Incoming Call Accept / Decline Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Decline Button
                    IconButton(
                        onClick = {
                            callViewModel.endCall(CallStatus.REJECTED)
                            onCallEnded()
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935))
                            .testTag("call_decline_btn")
                    ) {
                        Icon(Icons.Filled.CallEnd, contentDescription = "Decline", tint = Color.White, modifier = Modifier.size(32.dp))
                    }

                    // Accept Button
                    IconButton(
                        onClick = { callViewModel.acceptCall() },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                            .testTag("call_accept_btn")
                    ) {
                        Icon(Icons.Filled.Call, contentDescription = "Accept", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            } else {
                // Active In-Call Controls
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mic Mute
                        IconButton(
                            onClick = { callViewModel.toggleMicMute() },
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(if (state.isMicMuted) Color.White else Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = if (state.isMicMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                                contentDescription = "Mic",
                                tint = if (state.isMicMuted) Color.Black else Color.White
                            )
                        }

                        // Video Toggle
                        if (state.callType == CallType.VIDEO) {
                            IconButton(
                                onClick = { callViewModel.toggleVideo() },
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(if (!state.isVideoEnabled) Color.White else Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = if (state.isVideoEnabled) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
                                    contentDescription = "Video",
                                    tint = if (!state.isVideoEnabled) Color.Black else Color.White
                                )
                            }
                        }

                        // Speaker Toggle
                        IconButton(
                            onClick = { callViewModel.toggleSpeaker() },
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(if (state.isSpeakerOn) Color.White else Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = if (state.isSpeakerOn) Icons.Filled.VolumeUp else Icons.Filled.VolumeMute,
                                contentDescription = "Speaker",
                                tint = if (state.isSpeakerOn) Color.Black else Color.White
                            )
                        }

                        // Switch Camera
                        if (state.callType == CallType.VIDEO) {
                            IconButton(
                                onClick = { callViewModel.switchCamera() },
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(Icons.Filled.Cameraswitch, contentDescription = "Switch Camera", tint = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // End Call Button
                    IconButton(
                        onClick = {
                            callViewModel.endCall()
                            onCallEnded()
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935))
                            .testTag("call_end_btn")
                    ) {
                        Icon(Icons.Filled.CallEnd, contentDescription = "End Call", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}
