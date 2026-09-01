package com.example.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ChatMemberEntity
import com.example.data.model.AppLanguage
import com.example.data.model.CallType
import com.example.data.model.ChatType
import com.example.data.model.MemberRole
import com.example.ui.components.UserAvatar
import com.example.ui.viewmodel.ChatViewModel
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationDetailsScreen(
    chatId: String,
    chatViewModel: ChatViewModel,
    language: AppLanguage,
    onNavigateBack: () -> Unit,
    onOpenMediaGallery: () -> Unit,
    onStartCall: (String, String, String, CallType) -> Unit,
    onReportUser: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentChat by chatViewModel.currentChat.collectAsState()
    val chatMembers by chatViewModel.chatMembers.collectAsState()

    val chat = currentChat ?: return

    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Profile Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserAvatar(
                        avatarUrl = chat.avatarUrl,
                        name = chat.name,
                        size = 96.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = chat.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (chat.description.isNotBlank()) {
                        Text(
                            text = chat.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Quick Action Buttons (Call, Video, Search, Mute)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        QuickActionItem(
                            icon = Icons.Filled.Call,
                            label = "Audio",
                            onClick = {
                                onStartCall(chat.otherUserId.ifBlank { chat.id }, chat.name, chat.avatarUrl, CallType.VOICE)
                            }
                        )
                        QuickActionItem(
                            icon = Icons.Filled.Videocam,
                            label = "Video",
                            onClick = {
                                onStartCall(chat.otherUserId.ifBlank { chat.id }, chat.name, chat.avatarUrl, CallType.VIDEO)
                            }
                        )
                        QuickActionItem(
                            icon = if (chat.isMuted) Icons.Filled.NotificationsOff else Icons.Outlined.Notifications,
                            label = if (chat.isMuted) "Unmute" else "Mute",
                            onClick = {
                                if (chat.isMuted) chatViewModel.unmuteChat(chat.id) else chatViewModel.muteChat(chat.id, 8)
                            }
                        )
                        QuickActionItem(
                            icon = Icons.Outlined.PermMedia,
                            label = "Media",
                            onClick = onOpenMediaGallery
                        )
                    }
                }
            }

            // End-to-End Encryption Fingerprint
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = Localization.getString("verified_security_code", language),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "2847 9104 3847 2038 4930 1938 4839 2038",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Group Members Section if Group
            if (chat.type == ChatType.GROUP) {
                item {
                    Text(
                        text = "${Localization.getString("members", language)} (${chatMembers.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                items(chatMembers, key = { it.userId }) { member ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = if (member.userId == "user_me") "You" else "Member (${member.userId})"
                            )
                        },
                        supportingContent = {
                            Text(
                                text = when (member.role) {
                                    MemberRole.OWNER -> Localization.getString("owner", language)
                                    MemberRole.ADMIN -> Localization.getString("admin", language)
                                    MemberRole.MEMBER -> "Member"
                                }
                            )
                        },
                        leadingContent = {
                            UserAvatar(avatarUrl = null, name = member.userId, size = 40.dp)
                        },
                        trailingContent = {
                            if (member.role == MemberRole.OWNER || member.role == MemberRole.ADMIN) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = member.role.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    )
                }
            }

            // Chat Action Controls (Archive, Clear, Delete, Block, Report)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()

                ListItem(
                    headlineContent = { Text(if (chat.isPinned) Localization.getString("unpin", language) else Localization.getString("pin", language)) },
                    leadingContent = { Icon(Icons.Outlined.PushPin, null) },
                    modifier = Modifier.clickable { chatViewModel.togglePinChat(chat.id, !chat.isPinned) }
                )

                ListItem(
                    headlineContent = { Text(if (chat.isArchived) Localization.getString("unarchive", language) else Localization.getString("archive", language)) },
                    leadingContent = { Icon(Icons.Outlined.Archive, null) },
                    modifier = Modifier.clickable { chatViewModel.toggleArchiveChat(chat.id, !chat.isArchived) }
                )

                ListItem(
                    headlineContent = { Text(Localization.getString("report", language), color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Icon(Icons.Outlined.ReportProblem, null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable {
                        showReportDialog = true
                    }
                )

                ListItem(
                    headlineContent = { Text(Localization.getString("delete_chat", language), color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Icon(Icons.Outlined.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable {
                        chatViewModel.deleteChat(chat.id)
                        onNavigateBack()
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text(Localization.getString("report", language)) },
            text = {
                Column {
                    Text("Select reason for reporting this user / group:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        placeholder = { Text("Spam, harassment, inappropriate content...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReportUser(chat.otherUserId.ifBlank { chat.id }, chat.name)
                        showReportDialog = false
                    }
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text(Localization.getString("cancel", language))
                }
            }
        )
    }
}

@Composable
fun QuickActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
    }
}
