package com.example.ui.screens.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserEntity
import com.example.data.model.AppLanguage
import com.example.ui.components.UserAvatar
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.PaymentViewModel
import com.example.util.DateTimeUtils
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentWalletScreen(
    currentUser: UserEntity?,
    paymentViewModel: PaymentViewModel,
    chatViewModel: ChatViewModel,
    language: AppLanguage,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transactions by paymentViewModel.transactions.collectAsState()
    val activeUsers by chatViewModel.activeUsers.collectAsState()
    val paymentResult by paymentViewModel.paymentResult.collectAsState()

    var showSendMoneyDialog by remember { mutableStateOf(false) }
    var showTopUpDialog by remember { mutableStateOf(false) }
    var selectedReceiverId by remember { mutableStateOf("") }
    var sendAmount by remember { mutableStateOf("20.00") }
    var sendNote by remember { mutableStateOf("Split bill ☕") }
    var topUpAmount by remember { mutableStateOf("100.00") }

    val selectableUsers = remember(activeUsers) {
        activeUsers.filter { it.id != "user_me" }
    }

    LaunchedEffect(selectableUsers) {
        if (selectedReceiverId.isBlank() && selectableUsers.isNotEmpty()) {
            selectedReceiverId = selectableUsers.first().id
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(Localization.getString("wallet", language)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Result Banner
            if (paymentResult != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = paymentResult!!,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(onClick = { paymentViewModel.clearResult() }) {
                                Text("OK")
                            }
                        }
                    }
                }
            }

            // Wallet Balance Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        Color(0xFFA033FF)
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column {
                            Text(
                                text = Localization.getString("balance", language),
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$${String.format("%.2f", currentUser?.balance ?: 0.0)}",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { showSendMoneyDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("wallet_send_money_btn")
                                ) {
                                    Icon(Icons.Filled.Send, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(Localization.getString("send_money", language), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { showTopUpDialog = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("wallet_top_up_btn")
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Top-Up", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Transaction History Title
            item {
                Text(
                    text = Localization.getString("transactions", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Text(
                        text = "No recent transactions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    val isSentByMe = tx.senderId == "user_me"
                    ListItem(
                        headlineContent = {
                            Text(
                                text = if (isSentByMe) "Sent to ${tx.receiverName}" else "Received from ${tx.senderName}",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        supportingContent = {
                            Text(
                                text = "${tx.note.ifBlank { "Direct transfer" }} • ${DateTimeUtils.formatChatListTime(tx.timestamp)}"
                            )
                        },
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isSentByMe) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSentByMe) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (isSentByMe) Color(0xFFE53935) else Color(0xFF4CAF50),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        trailingContent = {
                            Text(
                                text = "${if (isSentByMe) "-" else "+"}$${String.format("%.2f", tx.amount)}",
                                fontWeight = FontWeight.Bold,
                                color = if (isSentByMe) MaterialTheme.colorScheme.onSurface else Color(0xFF4CAF50),
                                fontSize = 15.sp
                            )
                        }
                    )
                }
            }
        }
    }

    // Send Money Dialog
    if (showSendMoneyDialog) {
        AlertDialog(
            onDismissRequest = { showSendMoneyDialog = false },
            title = { Text(Localization.getString("send_money", language)) },
            text = {
                Column {
                    Text("Select Recipient:")
                    Spacer(modifier = Modifier.height(8.dp))
                    selectableUsers.forEach { user ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedReceiverId == user.id,
                                onClick = { selectedReceiverId = user.id }
                            )
                            UserAvatar(avatarUrl = user.avatarUrl, name = user.name, size = 32.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(user.name, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = sendAmount,
                        onValueChange = { sendAmount = it },
                        label = { Text("Amount ($)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sendNote,
                        onValueChange = { sendNote = it },
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
                        val amount = sendAmount.toDoubleOrNull() ?: 0.0
                        if (selectedReceiverId.isNotBlank() && amount > 0) {
                            paymentViewModel.sendMoney(selectedReceiverId, amount, sendNote)
                            showSendMoneyDialog = false
                        }
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

    // Top-Up Funds Dialog
    if (showTopUpDialog) {
        AlertDialog(
            onDismissRequest = { showTopUpDialog = false },
            title = { Text("Instant Top-Up") },
            text = {
                Column {
                    Text("Add funds to your Messenger Pay balance via Instant Bank Gateway:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = topUpAmount,
                        onValueChange = { topUpAmount = it },
                        label = { Text("Amount ($)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = topUpAmount.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            paymentViewModel.addFunds(amount)
                            showTopUpDialog = false
                        }
                    }
                ) {
                    Text("Add Funds")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTopUpDialog = false }) {
                    Text(Localization.getString("cancel", language))
                }
            }
        )
    }
}
