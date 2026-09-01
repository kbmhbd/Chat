package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    fun formatMessageTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatChatListTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val oneDay = 24 * 60 * 60 * 1000L

        return when {
            diff < 60 * 1000L -> "Just now"
            diff < 60 * 60 * 1000L -> "${diff / (60 * 1000L)}m"
            diff < oneDay -> {
                val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
            diff < 2 * oneDay -> "Yesterday"
            diff < 7 * oneDay -> {
                val sdf = SimpleDateFormat("EEE", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
            else -> {
                val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }

    fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
    }

    fun formatDurationMs(ms: Long): String {
        val totalSecs = (ms / 1000).toInt()
        return formatDuration(totalSecs)
    }

    fun formatLastSeen(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return when {
            diff < 2 * 60 * 1000L -> "Just now"
            diff < 60 * 60 * 1000L -> "${diff / (60 * 1000L)}m ago"
            diff < 24 * 60 * 60 * 1000L -> "${diff / (60 * 60 * 1000L)}h ago"
            else -> {
                val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }

    fun generateVoiceWaveform(seed: String = "", barsCount: Int = 24): List<Float> {
        val hash = seed.hashCode()
        val random = kotlin.random.Random(hash)
        return (0 until barsCount).map {
            0.2f + random.nextFloat() * 0.8f
        }
    }
}
