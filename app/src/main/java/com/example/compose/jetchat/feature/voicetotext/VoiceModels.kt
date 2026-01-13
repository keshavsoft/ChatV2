package com.example.compose.jetchat.feature.voicetotext

data class VoiceBubble(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val isMe: Boolean = true
)
