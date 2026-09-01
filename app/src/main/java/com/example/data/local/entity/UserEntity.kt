package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val phone: String = "",
    val name: String,
    val bio: String = "",
    val avatarUrl: String = "",
    val coverUrl: String = "",
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val isActiveStatusOn: Boolean = true,
    val isBlocked: Boolean = false,
    val isSuspended: Boolean = false,
    val role: UserRole = UserRole.USER,
    val createdAt: Long = System.currentTimeMillis(),
    val balance: Double = 250.00
)
