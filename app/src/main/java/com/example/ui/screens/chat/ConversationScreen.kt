package com.example.ui.screens.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.ChatEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.model.AppLanguage
import com.example.data.model.CallType
import com.example.data.model.ChatType
import com.example.data.model.MessageType
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.CallViewModel
import com.example.ui.viewmodel.ChatViewModel
import com.example.util.DateTimeUtils
import com.example.util.Localization
import com.example.util.SoundVibrationHelper
import kotlinx.coroutines.launch
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    chatId: String,
    chatViewModel: ChatViewModel,
    callViewModel: CallViewModel,
    language: AppLanguage,
    onNavigateBack: () -> Unit,
    onOpenDetails: (String) -> Unit,
    onStartCall: (String, String, String, CallType) -> Unit,
    onOpenMediaGallery: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(chatId) {
        chatViewModel.selectChat(chatId)
    }

    val currentChat by chatViewModel.currentChat.collectAsState()
    val messages by chatViewModel.currentMessages.collectAsState()
    val pinnedMessages by chatViewModel.pinnedMessages.collectAsState()
    val typingMap by chatViewModel.typingStatus.collectAsState()
    val replyingTo by chatViewModel.replyingTo.collectAsState()
    val editingMessage by chatViewModel.editingMessage.collectAsState()
    val isRecordingVoice by chatViewModel.isRecordingVoice.collectAsState()
    val voiceDuration by chatViewModel.voiceRecordingDuration.collectAsState()
    val isAiThinking by chatViewModel.isAiThinking.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var selectedMessageForOptions by remember { mutableStateOf<MessageEntity?>(null) }
    var showMessageOptionsSheet by remember { mutableStateOf(false) }
    var showSendMoneyDialog by remember { mutableStateOf(false) }
    var moneyAmount by remember { mutableStateOf("25.00") }
    var moneyNote by remember { mutableStateOf("Lunch split 🍕") }

    // Synchronize edit text
    LaunchedEffect(editingMessage) {
        if (editingMessage != null) {
            inputText = editingMessage!!.text
        }
    }

    // Auto scroll on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val chat = currentChat
    val isTyping = typingMap[chatId] != null
    val typingName = typingMap[chatId]

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    if (chat != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { onOpenDetails(chat.id) }
                                .testTag("conversation_header_info")
                        ) {
                            UserAvatar(
                                avatarUrl = chat.avatarUrl,
                                name = chat.name,
                                size = 40.dp,
                                isOnline = chat.isOnline,
                                showOnlineBadge = chat.type == ChatType.DIRECT
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = chat.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isTyping) "${typingName ?: "Typing"}..."
                                    else if (chat.type == ChatType.AI) "Instant AI Powered"
                                    else if (chat.isOnline) Localization.getString("online", language)
                                    else Localization.getString("offline", language),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (chat.isOnline || isTyping) OnlineGreen else MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            chatViewModel.clearSelectedChat()
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("conversation_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (chat != null && chat.type != ChatType.AI) {
                        IconButton(
                            onClick = {
                                onStartCall(chat.otherUserId.ifBlank { chat.id }, chat.name, chat.avatarUrl, CallType.VOICE)
                            },
                            modifier = Modifier.testTag("conversation_audio_call_btn")
                        ) {
                            Icon(Icons.Filled.Call, contentDescription = "Voice Call", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(
                            onClick = {
                                onStartCall(chat.otherUserId.ifBlank { chat.id }, chat.name, chat.avatarUrl, CallType.VIDEO)
                            },
                            modifier = Modifier.testTag("conversation_video_call_btn")
                        ) {
                            Icon(Icons.Filled.Videocam, contentDescription = "Video Call", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    IconButton(
                        onClick = { chat?.let { onOpenDetails(it.id) } },
                        modifier = Modifier.testTag("conversation_info_btn")
                    ) {
                        Icon(Icons.Outlined.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            // Pinned Message Banner
            if (pinnedMessages.isNotEmpty()) {
                val pinned = pinnedMessages.first()
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val idx = messages.indexOfFirst { it.id == pinned.id }
                            if (idx >= 0) {
                                scope.launch { listState.animateScrollToItem(idx) }
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.PushPin, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Localization.getString("pinned_message", language),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = pinned.text,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = { chatViewModel.togglePinMessage(pinned.id, false) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Unpin", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // End to end encryption notice
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Localization.getString("e2ee_notice", language),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                items(messages, key = { it.id }) { message ->
                    val isFromMe = message.senderId == "user_me"
                    MessageBubbleItem(
                        message = message,
                        isFromMe = isFromMe,
                        onReactionSelected = { emoji ->
                            chatViewModel.addReaction(message.id, emoji)
                        },
                        onLongClick = {
                            selectedMessageForOptions = message
                            showMessageOptionsSheet = true
                        }
                    )
                }

                if (isAiThinking) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Messenger AI is generating response...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Replying to Banner
            if (replyingTo != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(32.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${Localization.getString("replying_to", language)} ${replyingTo!!.senderName}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = replyingTo!!.text,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = { chatViewModel.setReplyingTo(null) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Editing Banner
            if (editingMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Localization.getString("edit_message", language),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                chatViewModel.setEditingMessage(null)
                                inputText = ""
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Bottom Input Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isRecordingVoice) {
                    // Voice Recording UI
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = DateTimeUtils.formatDuration(voiceDuration),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Text(
                            text = Localization.getString("slide_to_cancel", language),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Row {
                            IconButton(onClick = { chatViewModel.cancelVoiceRecording() }) {
                                Icon(Icons.Default.Delete, contentDescription = "Cancel", tint = MaterialTheme.colorScheme.error)
                            }
                            IconButton(
                                onClick = {
                                    SoundVibrationHelper.playSentSound()
                                    chatViewModel.sendVoiceRecording()
                                }
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                } else {
                    // Standard Message Input
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Plus / Attachments Button
                        IconButton(
                            onClick = { showAttachmentMenu = true },
                            modifier = Modifier.testTag("input_attachment_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Add attachments",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Text Field
                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text(Localization.getString("type_message", language)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                                .testTag("conversation_text_input")
                        )

                        // Send or Like / Mic Button
                        if (inputText.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    SoundVibrationHelper.playSentSound()
                                    chatViewModel.sendMessage(inputText)
                                    inputText = ""
                                },
                                modifier = Modifier.testTag("conversation_send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        } else {
                            // Mic Button
                            IconButton(
                                onClick = {
                                    SoundVibrationHelper.vibrate(context)
                                    chatViewModel.startVoiceRecording()
                                },
                                modifier = Modifier.testTag("conversation_mic_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Mic,
                                    contentDescription = "Record Voice",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            // Quick Thumbs Up Like
                            IconButton(
                                onClick = {
                                    SoundVibrationHelper.playSentSound()
                                    chatViewModel.sendMessage("👍")
                                },
                                modifier = Modifier.testTag("conversation_like_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ThumbUp,
                                    contentDescription = "Like",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Attachment Menu Modal Sheet
    if (showAttachmentMenu) {
        ModalBottomSheet(onDismissRequest = { showAttachmentMenu = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Share Content",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AttachmentOptionItem(
                        icon = Icons.Filled.PhotoCamera,
                        label = "Camera",
                        color = Color(0xFFE91E63),
                        onClick = {
                            chatViewModel.sendMessage(
                                text = "Photo",
                                type = MessageType.IMAGE,
                                mediaUrl = "https://images.unsplash.com/photo-1518791841217-8f162f1e1131?w=600"
                            )
                            showAttachmentMenu = false
                        }
                    )
                    AttachmentOptionItem(
                        icon = Icons.Filled.Image,
                        label = "Gallery",
                        color = Color(0xFF9C27B0),
                        onClick = {
                            chatViewModel.sendMessage(
                                text = "Design mockups",
                                type = MessageType.IMAGE,
                                mediaUrl = "https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?w=600"
                            )
                            showAttachmentMenu = false
                        }
                    )
                    AttachmentOptionItem(
                        icon = Icons.Filled.AttachFile,
                        label = "Document",
                        color = Color(0xFF2196F3),
                        onClick = {
                            chatViewModel.sendMessage(
                                text = "Project_Report_2026.pdf",
                                type = MessageType.FILE,
                                mediaName = "Project_Report_2026.pdf",
                                mediaSize = "2.4 MB"
                            )
                            showAttachmentMenu = false
                        }
                    )
                    AttachmentOptionItem(
                        icon = Icons.Filled.LocationOn,
                        label = "Location",
                        color = Color(0xFF4CAF50),
                        onClick = {
                            chatViewModel.sendMessage(
                                text = "Dhanmondi 27, Dhaka, Bangladesh",
                                type = MessageType.LOCATION
                            )
                            showAttachmentMenu = false
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AttachmentOptionItem(
                        icon = Icons.Filled.AccountBalanceWallet,
                        label = "Transfer",
                        color = Color(0xFFFF9800),
                        onClick = {
                            showAttachmentMenu = false
                            showSendMoneyDialog = true
                        }
                    )
                    AttachmentOptionItem(
                        icon = Icons.Filled.Mood,
                        label = "Sticker",
                        color = Color(0xFF00BCD4),
                        onClick = {
                            chatViewModel.sendMessage(
                                text = "🎉 Party Popper Sticker",
                                type = MessageType.STICKER
                            )
                            showAttachmentMenu = false
                        }
                    )
                    AttachmentOptionItem(
                        icon = Icons.Filled.Audiotrack,
                        label = "Audio",
                        color = Color(0xFF673AB7),
                        onClick = {
                            chatViewModel.sendMessage(
                                text = "voice_recording.mp3",
                                type = MessageType.AUDIO,
                                mediaName = "Song_Snippet.mp3",
                                mediaSize = "3.8 MB",
                                mediaDurationMs = 185000L
                            )
                            showAttachmentMenu = false
                        }
                    )
                    AttachmentOptionItem(
                        icon = Icons.Filled.AutoAwesome,
                        label = "AI Help",
                        color = Color(0xFF0084FF),
                        onClick = {
                            inputText = "Can you help me polish this message for work?"
                            showAttachmentMenu = false
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Send Money in Chat Modal Dialog
    if (showSendMoneyDialog) {
        AlertDialog(
            onDismissRequest = { showSendMoneyDialog = false },
            title = { Text(Localization.getString("send_money", language)) },
            text = {
                Column {
                    Text(
                        text = "Instant transfer via Messenger Pay",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = moneyAmount,
                        onValueChange = { moneyAmount = it },
                        label = { Text(Localization.getString("amount", language) + " ($)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = moneyNote,
                        onValueChange = { moneyNote = it },
                        label = { Text(Localization.getString("note", language)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = moneyAmount.toDoubleOrNull() ?: 10.0
                        chatViewModel.sendMessage(
                            text = "$$amount - $moneyNote",
                            type = MessageType.PAYMENT
                        )
                        showSendMoneyDialog = false
                    }
                ) {
                    Text(Localization.getString("send", language))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSendMoneyDialog = false }) {
                    Text(Localization.getString("cancel", language))
                }
            }
        )
    }

    // Message Long-Press Options Bottom Sheet
    if (showMessageOptionsSheet && selectedMessageForOptions != null) {
        val msg = selectedMessageForOptions!!
        val isMine = msg.senderId == "user_me"

        ModalBottomSheet(
            onDismissRequest = {
                showMessageOptionsSheet = false
                selectedMessageForOptions = null
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                // Top Emoji Quick Reaction Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ReactionBar(
                        onReactionSelected = { emoji ->
                            chatViewModel.addReaction(msg.id, emoji)
                            showMessageOptionsSheet = false
                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Reply
                ListItem(
                    headlineContent = { Text(Localization.getString("reply", language)) },
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.Reply, null) },
                    modifier = Modifier.clickable {
                        chatViewModel.setReplyingTo(msg)
                        showMessageOptionsSheet = false
                    }
                )

                // Copy Text
                if (msg.text.isNotBlank()) {
                    ListItem(
                        headlineContent = { Text(Localization.getString("copy", language)) },
                        leadingContent = { Icon(Icons.Outlined.ContentCopy, null) },
                        modifier = Modifier.clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Messenger Text", msg.text)
                            clipboard.setPrimaryClip(clip)
                            showMessageOptionsSheet = false
                        }
                    )
                }

                // Edit (if from me and text)
                if (isMine && msg.type == MessageType.TEXT && !msg.isDeletedForEveryone) {
                    ListItem(
                        headlineContent = { Text(Localization.getString("edit_message", language)) },
                        leadingContent = { Icon(Icons.Outlined.Edit, null) },
                        modifier = Modifier.clickable {
                            chatViewModel.setEditingMessage(msg)
                            showMessageOptionsSheet = false
                        }
                    )
                }

                // Pin / Unpin
                ListItem(
                    headlineContent = { Text(if (msg.isPinned) Localization.getString("unpin", language) else Localization.getString("pin", language)) },
                    leadingContent = { Icon(Icons.Outlined.PushPin, null) },
                    modifier = Modifier.clickable {
                        chatViewModel.togglePinMessage(msg.id, !msg.isPinned)
                        showMessageOptionsSheet = false
                    }
                )

                // Save / Bookmark
                ListItem(
                    headlineContent = { Text(if (msg.isSaved) Localization.getString("unbookmark", language) else Localization.getString("bookmark", language)) },
                    leadingContent = { Icon(if (msg.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, null) },
                    modifier = Modifier.clickable {
                        chatViewModel.toggleSaveMessage(msg.id, !msg.isSaved)
                        showMessageOptionsSheet = false
                    }
                )

                // Delete Options
                ListItem(
                    headlineContent = { Text(Localization.getString("delete_for_me", language), color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable {
                        chatViewModel.deleteMessage(msg.id, forEveryone = false)
                        showMessageOptionsSheet = false
                    }
                )

                if (isMine) {
                    ListItem(
                        headlineContent = { Text(Localization.getString("delete_for_everyone", language), color = MaterialTheme.colorScheme.error) },
                        leadingContent = { Icon(Icons.Outlined.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
                        modifier = Modifier.clickable {
                            chatViewModel.deleteMessage(msg.id, forEveryone = true)
                            showMessageOptionsSheet = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AttachmentOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubbleItem(
    message: MessageEntity,
    isFromMe: Boolean,
    onReactionSelected: (String) -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bubbleColor = if (isFromMe) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isFromMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val alignment = if (isFromMe) Alignment.End else Alignment.Start

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        // Reply snippet preview inside bubble if any
        if (!message.replyToText.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isFromMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .widthIn(max = 280.dp)
            ) {
                Row(modifier = Modifier.padding(6.dp)) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = message.replyToSenderName ?: "Reply",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = message.replyToText,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Main Bubble Content
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isFromMe) 18.dp else 4.dp,
                bottomEnd = if (isFromMe) 4.dp else 18.dp
            ),
            color = bubbleColor,
            modifier = Modifier
                .widthIn(min = 64.dp, max = 300.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick
                )
                .testTag("message_bubble_${message.id}")
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                when (message.type) {
                    MessageType.TEXT -> {
                        Text(
                            text = message.text,
                            color = textColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    MessageType.IMAGE -> {
                        if (message.mediaUrl.isNotBlank()) {
                            AsyncImage(
                                model = message.mediaUrl,
                                contentDescription = "Photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        if (message.text.isNotBlank() && message.text != "Photo") {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = message.text, color = textColor, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    MessageType.VOICE -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            var isPlaying by remember { mutableStateOf(false) }
                            IconButton(
                                onClick = { isPlaying = !isPlaying },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isFromMe) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = "Play voice message",
                                    tint = if (isFromMe) Color.White else Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            VoiceWaveformBar(
                                amplitudes = DateTimeUtils.generateVoiceWaveform(message.id, 20),
                                progress = if (isPlaying) 0.6f else 0.0f,
                                activeColor = if (isFromMe) Color.White else MaterialTheme.colorScheme.primary,
                                inactiveColor = if (isFromMe) Color.White.copy(alpha = 0.4f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = DateTimeUtils.formatDurationMs(message.mediaDurationMs.takeIf { it > 0 } ?: 4000L),
                                color = textColor,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    MessageType.FILE -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.InsertDriveFile, contentDescription = null, tint = textColor, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = message.mediaName.ifBlank { "Document.pdf" }, color = textColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(text = message.mediaSize.ifBlank { "1.2 MB" }, color = textColor.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    MessageType.LOCATION -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = if (isFromMe) Color.White else MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "Live Location", color = textColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(text = message.text, color = textColor.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    MessageType.PAYMENT -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = if (isFromMe) Color.White else Color(0xFF4CAF50), modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "Messenger Pay", color = textColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(text = message.text, color = textColor, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    else -> {
                        Text(text = message.text, color = textColor, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // Time, Edited status, Read receipts
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (message.isEdited) {
                        Text(text = "edited", fontSize = 10.sp, color = textColor.copy(alpha = 0.7f))
                    }
                    Text(
                        text = DateTimeUtils.formatMessageTime(message.createdAt),
                        fontSize = 10.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    if (isFromMe) {
                        MessageStatusIcon(status = message.status)
                    }
                }
            }
        }

        // Reactions overlay
        val reactions = parseReactions(message.reactionsJson)
        if (reactions.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .offset(y = (-8).dp)
                    .padding(horizontal = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    reactions.take(4).forEach { rx ->
                        Text(text = rx.emoji, fontSize = 12.sp)
                    }
                    if (reactions.size > 1) {
                        Text(text = reactions.size.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun parseReactions(jsonStr: String): List<com.example.data.model.MessageReaction> {
    return try {
        val list = mutableListOf<com.example.data.model.MessageReaction>()
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                com.example.data.model.MessageReaction(
                    emoji = obj.getString("emoji"),
                    userId = obj.getString("userId"),
                    userName = obj.getString("userName")
                )
            )
        }
        list
    } catch (e: Exception) {
        emptyList()
    }
}
