package com.example.data.repository

import com.example.data.firebase.FirebaseService
import com.example.data.local.dao.TransactionDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.TransactionStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class PaymentRepository(
    private val transactionDao: TransactionDao,
    private val userDao: UserDao,
    private val firebaseService: FirebaseService
) {
    fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    suspend fun sendMoney(receiverId: String, amount: Double, note: String): Result<TransactionEntity> {
        val me = userDao.getUserByIdOnce("user_me") ?: return Result.failure(IllegalStateException("User not found"))
        if (me.balance < amount) {
            return Result.failure(IllegalArgumentException("Insufficient balance ($${String.format("%.2f", me.balance)})"))
        }

        val receiver = userDao.getUserByIdOnce(receiverId)
        val receiverName = receiver?.name ?: "Recipient"

        // Deduct from me, add to receiver
        userDao.addBalance("user_me", -amount)
        if (receiver != null) {
            userDao.addBalance(receiverId, amount)
        }

        val tx = TransactionEntity(
            id = "tx_${UUID.randomUUID().toString().take(8)}",
            senderId = "user_me",
            senderName = me.name,
            receiverId = receiverId,
            receiverName = receiverName,
            amount = amount,
            currency = "$",
            note = note,
            status = TransactionStatus.COMPLETED,
            timestamp = System.currentTimeMillis()
        )

        transactionDao.insertTransaction(tx)
        firebaseService.syncTransactionToFirestore(tx)
        return Result.success(tx)
    }

    suspend fun addFunds(amount: Double) {
        userDao.addBalance("user_me", amount)
        val tx = TransactionEntity(
            id = "tx_topup_${UUID.randomUUID().toString().take(8)}",
            senderId = "bank_gateway",
            senderName = "Bank Card Top-Up",
            receiverId = "user_me",
            receiverName = "My Wallet",
            amount = amount,
            currency = "$",
            note = "Added money via Instant Bank Card",
            status = TransactionStatus.COMPLETED,
            timestamp = System.currentTimeMillis()
        )
        transactionDao.insertTransaction(tx)
        firebaseService.syncTransactionToFirestore(tx)
    }
}
