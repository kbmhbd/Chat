package com.example.ui.screens.call

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.UserEntity
import com.example.data.model.AppLanguage
import com.example.data.model.CallStatus
import com.example.data.model.CallType
import com.example.ui.components.EmptyStateView
import com.example.ui.components.UserAvatar
import com.example.ui.viewmodel.CallViewModel
import com.example.ui.viewmodel.ChatViewModel
import com.example.util.DateTimeUtils
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallListScreen(
    callViewModel: CallViewModel,
    chatViewModel: ChatViewModel,
    language: AppLanguage,
    onStartCall: (String, String, String, CallType) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(0) } // 0: All, 1: Missed
    val calls by callViewModel.callHistory.collectAsState()
    val missedCalls by callViewModel.missedCalls.collectAsState()
    val activeUsers by chatViewModel.activeUsers.collectAsState()

    val displayedCalls = if (selectedFilter == 0) calls else missedCalls

    var showNewCallDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Localization.getString("calls", language),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showNewCallDialog = true },
                        modifier = Modifier.testTag("calls_start_new_btn")
                    ) {
                        Icon(Icons.Filled.Call, contentDescription = "New Call", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick = { callViewModel.clearHistory() },
                        modifier = Modifier.testTag("calls_clear_history_btn")
                    ) {
                        Icon(Icons.Outlined.DeleteSweep, contentDescription = "Clear History")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == 0,
                    onClick = { selectedFilter = 0 },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = selectedFilter == 1,
                    onClick = { selectedFilter = 1 },
                    label = { Text("Missed") }
                )
            }

            if (displayedCalls.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.PhoneMissed,
                    title = Localization.getString("no_calls", language),
                    subtitle = "Calls with your friends and groups will appear here",
                    actionButtonText = "Start a Call",
                    onActionClick = { showNewCallDialog = true },
                    modifier = Modifier.padding(top = 40.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(displayedCalls, key = { it.id }) { call ->
                        val isOutgoing = call.callerId == "user_me"
                        val displayName = if (isOutgoing) call.receiverName else call.callerName
                        val displayAvatar = if (isOutgoing) call.receiverAvatarUrl else call.callerAvatarUrl
                        val otherId = if (isOutgoing) call.receiverId else call.callerId

                        ListItem(
                            headlineContent = {
                                Text(
                                    text = displayName,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (call.status == CallStatus.MISSED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            supportingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isOutgoing) Icons.Default.CallMade else Icons.Default.CallReceived,
                                        contentDescription = null,
                                        tint = if (call.status == CallStatus.MISSED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${DateTimeUtils.formatChatListTime(call.timestamp)}${if (call.durationSeconds > 0) " (${DateTimeUtils.formatDuration(call.durationSeconds)})" else ""}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            },
                            leadingContent = {
                                UserAvatar(avatarUrl = displayAvatar, name = displayName, size = 48.dp)
                            },
                            trailingContent = {
                                Row {
                                    IconButton(
                                        onClick = {
                                            onStartCall(otherId, displayName, displayAvatar, CallType.VOICE)
                                        }
                                    ) {
                                        Icon(Icons.Filled.Call, contentDescription = "Voice Call", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(
                                        onClick = {
                                            onStartCall(otherId, displayName, displayAvatar, CallType.VIDEO)
                                        }
                                    ) {
                                        Icon(Icons.Filled.Videocam, contentDescription = "Video Call", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            },
                            modifier = Modifier.clickable {
                                onStartCall(otherId, displayName, displayAvatar, call.type)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showNewCallDialog) {
        AlertDialog(
            onDismissRequest = { showNewCallDialog = false },
            title = { Text("Call Contact") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                    items(activeUsers.filter { it.id != "user_me" }, key = { it.id }) { user ->
                        ListItem(
                            headlineContent = { Text(user.name) },
                            leadingContent = { UserAvatar(avatarUrl = user.avatarUrl, name = user.name, size = 36.dp) },
                            trailingContent = {
                                Row {
                                    IconButton(
                                        onClick = {
                                            showNewCallDialog = false
                                            onStartCall(user.id, user.name, user.avatarUrl, CallType.VOICE)
                                        }
                                    ) {
                                        Icon(Icons.Filled.Call, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(
                                        onClick = {
                                            showNewCallDialog = false
                                            onStartCall(user.id, user.name, user.avatarUrl, CallType.VIDEO)
                                        }
                                    ) {
                                        Icon(Icons.Filled.Videocam, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showNewCallDialog = false }) {
                    Text(Localization.getString("cancel", language))
                }
            }
        )
    }
}
