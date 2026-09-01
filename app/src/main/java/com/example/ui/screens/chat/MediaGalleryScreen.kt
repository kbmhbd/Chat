package com.example.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.MessageType
import com.example.ui.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaGalleryScreen(
    chatId: String,
    chatViewModel: ChatViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Media", "Files", "Audio")

    val messages by chatViewModel.currentMessages.collectAsState()

    val mediaMessages = remember(messages, selectedTab) {
        when (selectedTab) {
            0 -> messages.filter { it.type == MessageType.IMAGE || it.type == MessageType.VIDEO }
            1 -> messages.filter { it.type == MessageType.FILE }
            2 -> messages.filter { it.type == MessageType.AUDIO || it.type == MessageType.VOICE }
            else -> emptyList()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Shared Content") },
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

            if (mediaMessages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No shared ${tabs[selectedTab].lowercase()} yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                when (selectedTab) {
                    0 -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(mediaMessages) { msg ->
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    if (msg.mediaUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = msg.mediaUrl,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    if (msg.type == MessageType.VIDEO) {
                                        Icon(
                                            Icons.Filled.Videocam,
                                            contentDescription = null,
                                            tint = androidx.compose.ui.graphics.Color.White,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(mediaMessages) { msg ->
                                ListItem(
                                    headlineContent = { Text(msg.mediaName.ifBlank { "Document.pdf" }) },
                                    supportingContent = { Text(msg.mediaSize.ifBlank { "1.2 MB" }) },
                                    leadingContent = { Icon(Icons.Filled.InsertDriveFile, null) }
                                )
                            }
                        }
                    }
                    2 -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(mediaMessages) { msg ->
                                ListItem(
                                    headlineContent = { Text(msg.text) },
                                    supportingContent = { Text("Voice / Audio note") },
                                    leadingContent = { Icon(Icons.Filled.MusicNote, null) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
