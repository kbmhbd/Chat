package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.data.local.entity.UserEntity
import com.example.data.model.AppLanguage
import com.example.data.model.UserRole
import com.example.ui.components.UserAvatar
import com.example.ui.viewmodel.AuthViewModel
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    authViewModel: AuthViewModel,
    language: AppLanguage,
    onNavigateBack: () -> Unit,
    onEditProfile: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenAdmin: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val user = currentUser ?: return

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(Localization.getString("profile", language)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEditProfile, modifier = Modifier.testTag("edit_profile_btn")) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit Profile")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Avatar with Status
            Box(contentAlignment = Alignment.BottomEnd) {
                UserAvatar(
                    avatarUrl = user.avatarUrl,
                    name = user.name,
                    size = 100.dp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = user.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            if (user.bio.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = user.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Wallet card shortcut
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenWallet() }
                    .testTag("profile_wallet_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = Localization.getString("wallet", language), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(text = "$${String.format("%.2f", user.balance)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Button(
                        onClick = onOpenWallet,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(Localization.getString("send_money", language))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile info list
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text(user.email.ifBlank { "Not set" }) },
                        supportingContent = { Text(Localization.getString("email", language)) },
                        leadingContent = { Icon(Icons.Outlined.Email, null) }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text(user.phone.ifBlank { "Not set" }) },
                        supportingContent = { Text(Localization.getString("phone", language)) },
                        leadingContent = { Icon(Icons.Outlined.Phone, null) }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text(Localization.getString("show_active_status", language)) },
                        supportingContent = { Text(Localization.getString("active_status", language)) },
                        leadingContent = { Icon(Icons.Outlined.CheckCircle, null) },
                        trailingContent = {
                            Switch(
                                checked = user.isActiveStatusOn,
                                onCheckedChange = { authViewModel.toggleActiveStatus(it) }
                            )
                        }
                    )
                }
            }

            // Admin panel access if user role is ADMIN
            if (user.role == UserRole.ADMIN) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenAdmin() }
                        .testTag("admin_panel_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    ListItem(
                        headlineContent = { Text(Localization.getString("admin_panel", language), fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("System analytics, reports & user moderation") },
                        leadingContent = { Icon(Icons.Filled.AdminPanelSettings, null, tint = MaterialTheme.colorScheme.tertiary) },
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(18.dp)) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            OutlinedButton(
                onClick = { authViewModel.logout(onLogout) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("logout_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(Localization.getString("logout", language), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
