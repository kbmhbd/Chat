package com.example.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.ui.components.UserAvatar
import com.example.ui.viewmodel.ChatViewModel
import com.example.util.DateTimeUtils
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    chatViewModel: ChatViewModel,
    language: AppLanguage,
    onNavigateBack: () -> Unit,
    onOpenChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val query by chatViewModel.searchQuery.collectAsState()
    val chatResults by chatViewModel.searchChatResults.collectAsState()
    val userResults by chatViewModel.searchUserResults.collectAsState()
    val messageResults by chatViewModel.searchMessageResults.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = query,
                        onValueChange = { chatViewModel.searchQuery.value = it },
                        placeholder = { Text(Localization.getString("search_hint", language)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("global_search_input")
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { chatViewModel.searchQuery.value = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear")
                        }
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
            // Contacts Section
            if (userResults.isNotEmpty()) {
                item {
                    Text(
                        text = "People",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(userResults, key = { it.id }) { user ->
                    ListItem(
                        headlineContent = { Text(user.name, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("@${user.username} • ${user.bio}") },
                        leadingContent = { UserAvatar(avatarUrl = user.avatarUrl, name = user.name, size = 44.dp) },
                        modifier = Modifier.clickable {
                            chatViewModel.startDirectChat(user) { chatId -> onOpenChat(chatId) }
                        }
                    )
                }
            }

            // Chats / Groups Section
            if (chatResults.isNotEmpty()) {
                item {
                    Text(
                        text = "Conversations",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(chatResults, key = { it.id }) { chat ->
                    ListItem(
                        headlineContent = { Text(chat.name, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(chat.lastMessageText) },
                        leadingContent = { UserAvatar(avatarUrl = chat.avatarUrl, name = chat.name, size = 44.dp) },
                        modifier = Modifier.clickable { onOpenChat(chat.id) }
                    )
                }
            }

            // Messages Section
            if (messageResults.isNotEmpty()) {
                item {
                    Text(
                        text = "Messages",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(messageResults, key = { it.id }) { msg ->
                    ListItem(
                        headlineContent = { Text(msg.senderName, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(msg.text) },
                        trailingContent = { Text(DateTimeUtils.formatChatListTime(msg.createdAt), style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.clickable { onOpenChat(msg.chatId) }
                    )
                }
            }
        }
    }
}
