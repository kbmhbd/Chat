package com.example.ui.screens.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.ui.components.UserAvatar
import com.example.ui.viewmodel.ChatViewModel
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    chatViewModel: ChatViewModel,
    language: AppLanguage,
    onNavigateBack: () -> Unit,
    onGroupCreated: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var groupName by remember { mutableStateOf("") }
    var groupDesc by remember { mutableStateOf("") }
    val selectedUserIds = remember { mutableStateListOf<String>() }

    val activeUsers by chatViewModel.activeUsers.collectAsState()
    val selectableUsers = remember(activeUsers) {
        activeUsers.filter { it.id != "user_me" }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(Localization.getString("create_group", language)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (groupName.isNotBlank() && selectedUserIds.isNotEmpty()) {
                                chatViewModel.createGroup(
                                    name = groupName.trim(),
                                    desc = groupDesc.trim(),
                                    members = selectedUserIds.toList()
                                ) { newChatId ->
                                    onGroupCreated(newChatId)
                                }
                            }
                        },
                        enabled = groupName.isNotBlank() && selectedUserIds.isNotEmpty(),
                        modifier = Modifier.testTag("create_group_confirm_btn")
                    ) {
                        Text(Localization.getString("save", language), fontWeight = FontWeight.Bold)
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
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text(Localization.getString("group_name", language)) },
                        leadingIcon = { Icon(Icons.Filled.Group, null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("group_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = groupDesc,
                        onValueChange = { groupDesc = it },
                        label = { Text(Localization.getString("group_desc", language)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "${Localization.getString("add_members", language)} (${selectedUserIds.size} selected)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            items(selectableUsers, key = { it.id }) { user ->
                val isSelected = selectedUserIds.contains(user.id)
                ListItem(
                    headlineContent = { Text(user.name, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text(user.bio, maxLines = 1) },
                    leadingContent = {
                        UserAvatar(avatarUrl = user.avatarUrl, name = user.name, size = 44.dp)
                    },
                    trailingContent = {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                if (checked) selectedUserIds.add(user.id) else selectedUserIds.remove(user.id)
                            }
                        )
                    },
                    modifier = Modifier.clickable {
                        if (isSelected) selectedUserIds.remove(user.id) else selectedUserIds.add(user.id)
                    }
                )
            }
        }
    }
}
