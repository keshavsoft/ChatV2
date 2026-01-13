package com.example.compose.jetchat.feature.sms

data class SmsGroup(
    val mobile: String,
    val lastMessage: String,
    val lastTimestamp: Long,
    val messageCount: Int,
    val isRead: Boolean = true   // <- new, use true for now
)

// For detail screen (conversation style)
data class SmsMessage(
    val address: String,
    val body: String,
    val timestamp: Long,
    val isSent: Boolean // 👈 true = you sent, false = received
)
