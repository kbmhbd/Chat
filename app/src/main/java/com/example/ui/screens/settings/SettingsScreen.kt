package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SettingsEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.AppLanguage
import com.example.data.model.ThemeMode
import com.example.data.model.UserRole
import com.example.ui.components.UserAvatar
import com.example.ui.theme.parseHexColor
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUser: UserEntity?,
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel,
    language: AppLanguage,
    onProfileClick: () -> Unit,
    onWalletClick: () -> Unit,
    onAiAssistantClick: () -> Unit,
    onAdminClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by settingsViewModel.settings.collectAsState()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAccentDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    val accentColors = listOf(
        "#0084FF" to "Messenger Blue",
        "#A033FF" to "Neon Purple",
        "#00C29A" to "Emerald Teal",
        "#FF5252" to "Sunset Coral",
        "#4F46E5" to "Royal Indigo"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Localization.getString("settings", language),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Profile Preview Item
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProfileClick() }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("settings_profile_row")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(
                            avatarUrl = currentUser?.avatarUrl,
                            name = currentUser?.name ?: "User",
                            size = 64.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser?.name ?: "User",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "@${currentUser?.username ?: "username"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = currentUser?.bio ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                maxLines = 1
                            )
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    }
                }
                HorizontalDivider()
            }

            // Quick App Features (AI Assistant, Messenger Pay, Admin)
            item {
                SectionHeader("Special Features")

                ListItem(
                    headlineContent = { Text(Localization.getString("ai_assistant", language), fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("Smart drafting, summarization & translations") },
                    leadingContent = { Icon(Icons.Filled.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = { Icon(Icons.Filled.ChevronRight, null) },
                    modifier = Modifier.clickable { onAiAssistantClick() }
                )

                ListItem(
                    headlineContent = { Text(Localization.getString("wallet", language), fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("Instant peer-to-peer transfers & balance") },
                    leadingContent = { Icon(Icons.Filled.AccountBalanceWallet, null, tint = Color(0xFF4CAF50)) },
                    trailingContent = { Icon(Icons.Filled.ChevronRight, null) },
                    modifier = Modifier.clickable { onWalletClick() }
                )

                if (currentUser?.role == UserRole.ADMIN) {
                    ListItem(
                        headlineContent = { Text(Localization.getString("admin_panel", language), fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Analytics, reports moderation & user bans") },
                        leadingContent = { Icon(Icons.Filled.AdminPanelSettings, null, tint = MaterialTheme.colorScheme.tertiary) },
                        trailingContent = { Icon(Icons.Filled.ChevronRight, null) },
                        modifier = Modifier.clickable { onAdminClick() }
                    )
                }

                HorizontalDivider()
            }

            // Appearance & Preferences
            item {
                SectionHeader("Preferences")

                // Language
                ListItem(
                    headlineContent = { Text(Localization.getString("language", language)) },
                    supportingContent = { Text(if (settings.language == AppLanguage.BENGALI) "বাংলা (Bengali 🇧🇩)" else "English (🇬🇧)") },
                    leadingContent = { Icon(Icons.Outlined.Translate, null) },
                    modifier = Modifier.clickable { showLanguageDialog = true }
                )

                // Theme Mode
                ListItem(
                    headlineContent = { Text(Localization.getString("theme", language)) },
                    supportingContent = { Text(settings.themeMode.name) },
                    leadingContent = { Icon(Icons.Outlined.DarkMode, null) },
                    modifier = Modifier.clickable { showThemeDialog = true }
                )

                // Accent Color
                ListItem(
                    headlineContent = { Text("Accent Color") },
                    supportingContent = { Text("Customize brand accent theme") },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(settings.accentColorHex, Color.Blue))
                        )
                    },
                    modifier = Modifier.clickable { showAccentDialog = true }
                )

                HorizontalDivider()
            }

            // Privacy & Security
            item {
                SectionHeader(Localization.getString("privacy_security", language))

                ListItem(
                    headlineContent = { Text("Active Status") },
                    supportingContent = { Text(Localization.getString("show_active_status", language)) },
                    leadingContent = { Icon(Icons.Outlined.Visibility, null) },
                    trailingContent = {
                        Switch(
                            checked = currentUser?.isActiveStatusOn ?: true,
                            onCheckedChange = { authViewModel.toggleActiveStatus(it) }
                        )
                    }
                )

                ListItem(
                    headlineContent = { Text("End-to-End Encryption") },
                    supportingContent = { Text("Messages and calls are secured with Signal protocol") },
                    leadingContent = { Icon(Icons.Outlined.Lock, null) }
                )

                HorizontalDivider()
            }

            // Logout
            item {
                ListItem(
                    headlineContent = { Text(Localization.getString("logout", language), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable { showLogoutConfirmDialog = true }
                )
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Language Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(Localization.getString("language", language)) },
            text = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                settingsViewModel.updateLanguage(AppLanguage.ENGLISH)
                                showLanguageDialog = false
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(selected = settings.language == AppLanguage.ENGLISH, onClick = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("English (🇬🇧)")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                settingsViewModel.updateLanguage(AppLanguage.BENGALI)
                                showLanguageDialog = false
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(selected = settings.language == AppLanguage.BENGALI, onClick = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("বাংলা (Bengali 🇧🇩)")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text(Localization.getString("cancel", language)) }
            }
        )
    }

    // Theme Mode Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(Localization.getString("theme", language)) },
            text = {
                Column {
                    ThemeMode.values().forEach { mode ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsViewModel.updateThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(selected = settings.themeMode == mode, onClick = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(mode.name)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text(Localization.getString("cancel", language)) }
            }
        )
    }

    // Accent Color Dialog
    if (showAccentDialog) {
        AlertDialog(
            onDismissRequest = { showAccentDialog = false },
            title = { Text("Select Accent Color") },
            text = {
                Column {
                    accentColors.forEach { (hex, name) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsViewModel.updateAccentColor(hex)
                                    showAccentDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColor(hex, Color.Blue))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(name, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAccentDialog = false }) { Text(Localization.getString("cancel", language)) }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text(Localization.getString("logout", language)) },
            text = { Text("Are you sure you want to log out of your account?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        authViewModel.logout(onLogout)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(Localization.getString("logout", language))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text(Localization.getString("cancel", language))
                }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
    )
}
