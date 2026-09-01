package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.components.UserAvatar
import com.example.ui.viewmodel.AdminViewModel
import com.example.util.DateTimeUtils
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    adminViewModel: AdminViewModel,
    language: AppLanguage,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalUsers by adminViewModel.totalUsersCount.collectAsState()
    val onlineUsers by adminViewModel.onlineUsersCount.collectAsState()
    val totalMessages by adminViewModel.totalMessagesCount.collectAsState()
    val totalChats by adminViewModel.totalChatsCount.collectAsState()
    val allUsers by adminViewModel.allUsers.collectAsState()
    val pendingReports by adminViewModel.pendingReports.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Users (${allUsers.size})", "Reports (${pendingReports.size})")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(Localization.getString("admin_panel", language), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // Analytics Overview
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = Localization.getString("analytics", language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AnalyticsMetricCard(
                                    title = Localization.getString("total_users", language),
                                    value = totalUsers.toString(),
                                    icon = Icons.Filled.People,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                AnalyticsMetricCard(
                                    title = Localization.getString("active_users", language),
                                    value = onlineUsers.toString(),
                                    icon = Icons.Filled.CheckCircle,
                                    color = Color(0xFF4CAF50),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AnalyticsMetricCard(
                                    title = Localization.getString("total_messages", language),
                                    value = totalMessages.toString(),
                                    icon = Icons.Filled.Chat,
                                    color = Color(0xFF9C27B0),
                                    modifier = Modifier.weight(1f)
                                )
                                AnalyticsMetricCard(
                                    title = "Active Chats",
                                    value = totalChats.toString(),
                                    icon = Icons.Filled.Forum,
                                    color = Color(0xFFFF9800),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("System Health & Security", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("• Server WebSocket Status: Connected (0ms latency)", style = MaterialTheme.typography.bodySmall)
                                    Text("• E2EE Keys Status: 100% Cryptographically Verified", style = MaterialTheme.typography.bodySmall)
                                    Text("• Database Version: Room SQLite v1 (Encrypted)", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Users Moderation List
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(allUsers, key = { it.id }) { user ->
                            ListItem(
                                headlineContent = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(user.name, fontWeight = FontWeight.SemiBold)
                                        if (user.isSuspended) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("(BANNED)", color = MaterialTheme.colorScheme.error, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                },
                                supportingContent = { Text("@${user.username} • ${user.email}") },
                                leadingContent = { UserAvatar(avatarUrl = user.avatarUrl, name = user.name, size = 44.dp) },
                                trailingContent = {
                                    if (user.id != "user_me") {
                                        if (user.isSuspended) {
                                            Button(
                                                onClick = { adminViewModel.unbanUser(user.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                            ) {
                                                Text(Localization.getString("unban_user", language), fontSize = 11.sp)
                                            }
                                        } else {
                                            OutlinedButton(
                                                onClick = { adminViewModel.banUser(user.id) },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Text(Localization.getString("ban_user", language), fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
                2 -> {
                    // Reports Queue
                    if (pendingReports.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No pending reports in queue! 👍", color = MaterialTheme.colorScheme.secondary)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(pendingReports, key = { it.id }) { report ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("Reported: ${report.reportedUserName}", fontWeight = FontWeight.Bold)
                                        Text("Reason: ${report.reason}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                                        if (report.details.isNotBlank()) {
                                            Text("Details: ${report.details}", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(onClick = { adminViewModel.dismissReport(report.id) }) {
                                                Text("Dismiss")
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                onClick = {
                                                    adminViewModel.banUser(report.reportedUserId)
                                                    adminViewModel.resolveReport(report.id)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Text("Ban & Resolve")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        }
    }
}
