package com.example.compose.jetchat.conversation.newchat

data class NewChatMessage(
    val author: String,
    val content: String,
    val timestamp: String,
)