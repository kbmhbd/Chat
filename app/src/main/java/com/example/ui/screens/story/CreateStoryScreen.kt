package com.example.ui.screens.story

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.theme.parseHexColor
import com.example.ui.viewmodel.StoryViewModel
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStoryScreen(
    storyViewModel: StoryViewModel,
    language: AppLanguage,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var storyText by remember { mutableStateOf("") }
    var selectedColorIndex by remember { mutableStateOf(0) }
    var isMediaMode by remember { mutableStateOf(false) }

    val colorHexes = listOf(
        "#0084FF", // Blue
        "#A033FF", // Purple
        "#FF5252", // Sunset Coral
        "#00C29A", // Emerald
        "#FF9800", // Warm Orange
        "#212121"  // Sleek Dark
    )

    val currentBgColor = parseHexColor(colorHexes[selectedColorIndex], Color(0xFF0084FF))

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(Localization.getString("create_story", language)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (isMediaMode) {
                                storyViewModel.createMediaStory(
                                    mediaUrl = "https://images.unsplash.com/photo-1518791841217-8f162f1e1131?w=800",
                                    caption = storyText
                                ) {
                                    onNavigateBack()
                                }
                            } else {
                                storyViewModel.createTextStory(
                                    text = storyText.ifBlank { "Sharing moments with friends ✨" },
                                    colorHex = colorHexes[selectedColorIndex]
                                ) {
                                    onNavigateBack()
                                }
                            }
                        },
                        modifier = Modifier.testTag("publish_story_btn")
                    ) {
                        Text(Localization.getString("save", language), fontWeight = FontWeight.Bold)
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
            // Story Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(currentBgColor, currentBgColor.copy(alpha = 0.8f))
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                TextField(
                    value = storyText,
                    onValueChange = { storyText = it },
                    placeholder = {
                        Text(
                            text = "Tap to type your story...",
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                    },
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("story_text_input")
                )
            }

            // Bottom Palette / Mode selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color circles
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    colorHexes.forEachIndexed { index, hex ->
                        val col = parseHexColor(hex, Color.Blue)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(col)
                                .clickable { selectedColorIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColorIndex == index) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
