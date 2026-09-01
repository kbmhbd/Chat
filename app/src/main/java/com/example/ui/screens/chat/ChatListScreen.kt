package com.example.ui.screens.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ChatEntity
import com.example.data.local.entity.StoryEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.AppLanguage
import com.example.data.model.ChatType
import com.example.ui.components.EmptyStateView
import com.example.ui.components.UserAvatar
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.StoryViewModel
import com.example.util.DateTimeUtils
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    chatViewModel: ChatViewModel,
    storyViewModel: StoryViewModel,
    currentUser: UserEntity?,
    language: AppLanguage,
    onChatClick: (String) -> Unit,
    onStoryClick: (StoryEntity) -> Unit,
    onCreateStoryClick: () -> Unit,
    onNewChatClick: () -> Unit,
    onCreateGroupClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeChats by chatViewModel.activeChats.collectAsState()
    val activeUsers by chatViewModel.activeUsers.collectAsState()
    val stories by storyViewModel.stories.collectAsState()
    val typingMap by chatViewModel.typingStatus.collectAsState()

    var selectedChatForMenu by remember { mutableStateOf<ChatEntity?>(null) }
    var showChatOptionsDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = onProfileClick,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("chat_list_profile_avatar")
                        ) {
                            UserAvatar(
                                avatarUrl = currentUser?.avatarUrl,
                                name = currentUser?.name ?: "JD",
                                size = 38.dp
                            )
                        }
                        Text(
                            text = Localization.getString("chats", language),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier.testTag("chat_list_search_button")
                    ) {
                        Icon(Icons.Outlined.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(
                        onClick = onCreateGroupClick,
                        modifier = Modifier.testTag("chat_list_new_group_button")
                    ) {
                        Icon(Icons.Outlined.GroupAdd, contentDescription = "New Group", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewChatClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .size(56.dp)
                    .testTag("fab_new_chat")
            ) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "New Message",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 1. Stories Tray
            item {
                StoriesHeaderTray(
                    currentUser = currentUser,
                    stories = stories,
                    activeUsers = activeUsers,
                    language = language,
                    onCreateStoryClick = onCreateStoryClick,
                    onStoryClick = onStoryClick,
                    onUserClick = { user ->
                        chatViewModel.startDirectChat(user) { chatId -> onChatClick(chatId) }
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 2. Chat List Items
            if (activeChats.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Outlined.Forum,
                        title = Localization.getString("no_chats", language),
                        subtitle = "Start a new conversation or invite friends",
                        actionButtonText = Localization.getString("create_group", language),
                        onActionClick = onCreateGroupClick,
                        modifier = Modifier.padding(top = 40.dp)
                    )
                }
            } else {
                items(activeChats, key = { it.id }) { chat ->
                    val isTyping = typingMap[chat.id] != null
                    val typingName = typingMap[chat.id]

                    ChatListItem(
                        chat = chat,
                        isTyping = isTyping,
                        typingName = typingName,
                        language = language,
                        onClick = { onChatClick(chat.id) },
                        onLongClick = {
                            selectedChatForMenu = chat
                            showChatOptionsDialog = true
                        }
                    )
                }
            }
        }
    }

    // Long press options bottom sheet
    if (showChatOptionsDialog && selectedChatForMenu != null) {
        val chat = selectedChatForMenu!!
        ModalBottomSheet(
            onDismissRequest = {
                showChatOptionsDialog = false
                selectedChatForMenu = null
            },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).padding(bottom = 32.dp)) {
                ListItem(
                    headlineContent = { Text(if (chat.isPinned) Localization.getString("unpin", language) else Localization.getString("pin", language), fontWeight = FontWeight.SemiBold) },
                    leadingContent = { Icon(if (chat.isPinned) Icons.Outlined.PushPin else Icons.Filled.PushPin, null) },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable {
                        chatViewModel.togglePinChat(chat.id, !chat.isPinned)
                        showChatOptionsDialog = false
                    }
                )
                ListItem(
                    headlineContent = { Text(if (chat.isMuted) Localization.getString("unmute", language) else Localization.getString("mute", language), fontWeight = FontWeight.SemiBold) },
                    leadingContent = { Icon(if (chat.isMuted) Icons.Filled.NotificationsOff else Icons.Outlined.Notifications, null) },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable {
                        if (chat.isMuted) chatViewModel.unmuteChat(chat.id) else chatViewModel.muteChat(chat.id, 8)
                        showChatOptionsDialog = false
                    }
                )
                ListItem(
                    headlineContent = { Text(Localization.getString("archive", language), fontWeight = FontWeight.SemiBold) },
                    leadingContent = { Icon(Icons.Outlined.Archive, null) },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable {
                        chatViewModel.toggleArchiveChat(chat.id, true)
                        showChatOptionsDialog = false
                    }
                )
                ListItem(
                    headlineContent = { Text(Localization.getString("clear_chat", language), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold) },
                    leadingContent = { Icon(Icons.Outlined.DeleteSweep, null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable {
                        chatViewModel.clearChat(chat.id)
                        showChatOptionsDialog = false
                    }
                )
                ListItem(
                    headlineContent = { Text(Localization.getString("delete_chat", language), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold) },
                    leadingContent = { Icon(Icons.Outlined.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable {
                        chatViewModel.deleteChat(chat.id)
                        showChatOptionsDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun StoriesHeaderTray(
    currentUser: UserEntity?,
    stories: List<StoryEntity>,
    activeUsers: List<UserEntity>,
    language: AppLanguage,
    onCreateStoryClick: () -> Unit,
    onStoryClick: (StoryEntity) -> Unit,
    onUserClick: (UserEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Add your story item (High density clean circle with dashed border / plus)
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onCreateStoryClick() }
                    .testTag("add_story_header_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Story",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Localization.getString("my_story", language),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Stories from contacts with 2dp HighDensityPrimary border
        items(stories, key = { it.id }) { story ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onStoryClick(story) }
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    UserAvatar(
                        avatarUrl = story.userAvatarUrl,
                        name = story.userName,
                        size = 48.dp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = story.userName.split(" ").firstOrNull() ?: "",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Active Contacts Avatars
        items(activeUsers.filter { it.id != "user_me" }, key = { it.id }) { user ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onUserClick(user) }
            ) {
                UserAvatar(
                    avatarUrl = user.avatarUrl,
                    name = user.name,
                    size = 54.dp,
                    isOnline = user.isOnline,
                    showOnlineBadge = true
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.name.split(" ").firstOrNull() ?: "",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChatListItem(
    chat: ChatEntity,
    isTyping: Boolean,
    typingName: String?,
    language: AppLanguage,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("chat_item_${chat.id}"),
        color = if (chat.unreadCount > 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chat Avatar with Online Ring
            Box(contentAlignment = Alignment.BottomEnd) {
                UserAvatar(
                    avatarUrl = chat.avatarUrl,
                    name = chat.name,
                    size = 52.dp,
                    isOnline = chat.isOnline,
                    showOnlineBadge = chat.type == ChatType.DIRECT
                )
                if (chat.type == ChatType.AI) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "AI",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = DateTimeUtils.formatChatListTime(chat.lastMessageTimestamp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (chat.unreadCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isTyping) {
                        Text(
                            text = "${typingName ?: "Someone"} is typing...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        val messagePrefix = if (chat.lastMessageSenderName.isNotBlank() && chat.type == ChatType.GROUP) {
                            "${chat.lastMessageSenderName}: "
                        } else ""

                        Text(
                            text = "$messagePrefix${chat.lastMessageText}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            fontWeight = if (chat.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (chat.unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (chat.isPinned) {
                            Icon(
                                imageVector = Icons.Filled.PushPin,
                                contentDescription = "Pinned",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        if (chat.isMuted) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsOff,
                                contentDescription = "Muted",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        if (chat.unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = chat.unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
