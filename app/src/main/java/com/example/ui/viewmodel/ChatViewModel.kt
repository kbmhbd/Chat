package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ChatEntity
import com.example.data.local.entity.ChatMemberEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.ChatType
import com.example.data.model.MessageType
import com.example.data.repository.ChatRepository
import com.example.data.repository.GeminiAiService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val geminiAiService: GeminiAiService = GeminiAiService()
) : ViewModel() {

    val activeChats: StateFlow<List<ChatEntity>> = chatRepository.getAllActiveChats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedChats: StateFlow<List<ChatEntity>> = chatRepository.getArchivedChats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val typingStatus: StateFlow<Map<String, String?>> = chatRepository.typingStatus

    // Active conversation state
    private val _selectedChatId = MutableStateFlow<String?>(null)
    val selectedChatId: StateFlow<String?> = _selectedChatId.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentChat: StateFlow<ChatEntity?> = _selectedChatId
        .flatMapLatest { id ->
            if (id != null) chatRepository.getChatById(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentMessages: StateFlow<List<MessageEntity>> = _selectedChatId
        .flatMapLatest { id ->
            if (id != null) chatRepository.getMessagesForChat(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val pinnedMessages: StateFlow<List<MessageEntity>> = _selectedChatId
        .flatMapLatest { id ->
            if (id != null) chatRepository.getPinnedMessagesForChat(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val chatMembers: StateFlow<List<ChatMemberEntity>> = _selectedChatId
        .flatMapLatest { id ->
            if (id != null) chatRepository.getChatMembers(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active users for new conversation / direct chat
    val activeUsers: StateFlow<List<UserEntity>> = chatRepository.getActiveUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search query & results
    val searchQuery = MutableStateFlow("")
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val searchChatResults: StateFlow<List<ChatEntity>> = searchQuery
        .debounce(250)
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList()) else chatRepository.searchChats(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val searchUserResults: StateFlow<List<UserEntity>> = searchQuery
        .debounce(250)
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList()) else chatRepository.searchUsers(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val searchMessageResults: StateFlow<List<MessageEntity>> = searchQuery
        .debounce(250)
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList()) else chatRepository.searchAllMessages(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Replying state
    private val _replyingTo = MutableStateFlow<MessageEntity?>(null)
    val replyingTo: StateFlow<MessageEntity?> = _replyingTo.asStateFlow()

    // Editing state
    private val _editingMessage = MutableStateFlow<MessageEntity?>(null)
    val editingMessage: StateFlow<MessageEntity?> = _editingMessage.asStateFlow()

    // Voice recording state
    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice: StateFlow<Boolean> = _isRecordingVoice.asStateFlow()
    private val _voiceRecordingDuration = MutableStateFlow(0)
    val voiceRecordingDuration: StateFlow<Int> = _voiceRecordingDuration.asStateFlow()
    private var voiceTimerJob: Job? = null

    // AI loading state
    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    fun selectChat(chatId: String) {
        _selectedChatId.value = chatId
        viewModelScope.launch {
            chatRepository.markChatAsRead(chatId)
        }
    }

    fun clearSelectedChat() {
        _selectedChatId.value = null
        _replyingTo.value = null
        _editingMessage.value = null
    }

    fun startDirectChat(user: UserEntity, onOpened: (String) -> Unit) {
        viewModelScope.launch {
            val chatId = chatRepository.getOrCreateDirectChat(user)
            selectChat(chatId)
            onOpened(chatId)
        }
    }

    fun createGroup(name: String, desc: String, members: List<String>, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val chatId = chatRepository.createGroupChat(name, desc, members)
            selectChat(chatId)
            onCreated(chatId)
        }
    }

    fun setReplyingTo(message: MessageEntity?) {
        _replyingTo.value = message
        _editingMessage.value = null
    }

    fun setEditingMessage(message: MessageEntity?) {
        _editingMessage.value = message
        _replyingTo.value = null
    }

    fun sendMessage(
        text: String,
        type: MessageType = MessageType.TEXT,
        mediaUrl: String = "",
        mediaName: String = "",
        mediaSize: String = "",
        mediaDurationMs: Long = 0L
    ) {
        val chatId = _selectedChatId.value ?: return
        if (text.isBlank() && mediaUrl.isBlank()) return

        val editing = _editingMessage.value
        if (editing != null) {
            viewModelScope.launch {
                chatRepository.editMessage(editing.id, text.trim())
                _editingMessage.value = null
            }
            return
        }

        val reply = _replyingTo.value
        viewModelScope.launch {
            val sentMsg = chatRepository.sendMessage(
                chatId = chatId,
                text = text.trim(),
                type = type,
                mediaUrl = mediaUrl,
                mediaName = mediaName,
                mediaSize = mediaSize,
                mediaDurationMs = mediaDurationMs,
                replyToMessageId = reply?.id,
                replyToText = reply?.text,
                replyToSenderName = reply?.senderName
            )
            _replyingTo.value = null

            // Check if active chat is AI Assistant
            val chat = currentChat.value
            if (chat?.type == ChatType.AI) {
                handleAiChatTurn(chatId, text)
            }
        }
    }

    private fun handleAiChatTurn(chatId: String, prompt: String) {
        viewModelScope.launch {
            _isAiThinking.value = true
            val history = currentMessages.value.takeLast(6).map {
                (if (it.senderId == "user_me") "user" else "model") to it.text
            }
            val reply = geminiAiService.getAiResponse(prompt, history)
            _isAiThinking.value = false
            chatRepository.sendMessage(
                chatId = chatId,
                text = reply,
                type = MessageType.TEXT,
                senderId = "user_ai_bot",
                senderName = "Messenger AI",
                senderAvatarUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150"
            )
        }
    }

    fun startVoiceRecording() {
        _isRecordingVoice.value = true
        _voiceRecordingDuration.value = 0
        voiceTimerJob?.cancel()
        voiceTimerJob = viewModelScope.launch {
            while (_isRecordingVoice.value) {
                delay(1000)
                _voiceRecordingDuration.value += 1
            }
        }
    }

    fun cancelVoiceRecording() {
        voiceTimerJob?.cancel()
        _isRecordingVoice.value = false
        _voiceRecordingDuration.value = 0
    }

    fun sendVoiceRecording() {
        voiceTimerJob?.cancel()
        val durationSec = _voiceRecordingDuration.value
        _isRecordingVoice.value = false
        _voiceRecordingDuration.value = 0
        if (durationSec >= 1) {
            sendMessage(
                text = "Voice message (${durationSec}s)",
                type = MessageType.VOICE,
                mediaUrl = "audio_record_${System.currentTimeMillis()}.m4a",
                mediaDurationMs = durationSec * 1000L
            )
        }
    }

    fun addReaction(messageId: String, emoji: String) {
        viewModelScope.launch {
            chatRepository.addReaction(messageId, emoji)
        }
    }

    fun togglePinMessage(messageId: String, isPinned: Boolean) {
        viewModelScope.launch {
            chatRepository.togglePinMessage(messageId, isPinned)
        }
    }

    fun toggleSaveMessage(messageId: String, isSaved: Boolean) {
        viewModelScope.launch {
            chatRepository.toggleSaveMessage(messageId, isSaved)
        }
    }

    fun deleteMessage(messageId: String, forEveryone: Boolean) {
        viewModelScope.launch {
            if (forEveryone) {
                chatRepository.deleteMessageForEveryone(messageId)
            } else {
                chatRepository.deleteMessageForMe(messageId)
            }
        }
    }

    fun togglePinChat(chatId: String, isPinned: Boolean) {
        viewModelScope.launch { chatRepository.togglePinChat(chatId, isPinned) }
    }

    fun toggleArchiveChat(chatId: String, isArchived: Boolean) {
        viewModelScope.launch { chatRepository.toggleArchiveChat(chatId, isArchived) }
    }

    fun muteChat(chatId: String, hours: Int) {
        viewModelScope.launch { chatRepository.muteChat(chatId, hours) }
    }

    fun unmuteChat(chatId: String) {
        viewModelScope.launch { chatRepository.unmuteChat(chatId) }
    }

    fun clearChat(chatId: String) {
        viewModelScope.launch { chatRepository.clearChatHistory(chatId) }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            chatRepository.deleteChat(chatId)
            if (_selectedChatId.value == chatId) {
                _selectedChatId.value = null
            }
        }
    }
}

class ChatViewModelFactory(private val repository: ChatRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
