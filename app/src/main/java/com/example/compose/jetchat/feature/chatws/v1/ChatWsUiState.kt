package com.example.compose.jetchat.feature.chatws.v1

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.toMutableStateList
import com.example.compose.jetchat.R
import com.example.compose.jetchat.feature.chatws.v2.WsMessageType

class ChatWsUiState(
    val channelName: String,
    val channelMembers: Int,
    initialMessages: List<ChatMessage>
) {
    private val _messages = initialMessages.toMutableStateList()
    val messages: List<ChatMessage> = _messages

    fun addMessage(msg: ChatMessage) {
        _messages.add(0, msg)
    }
}

@Immutable
data class ChatMessage1(
    val author: String,
    val content: String,
    val timestamp: String,
    val image: Int? = null,
    val authorImage: Int =
        if (author == "me") R.drawable.ali else R.drawable.someone_else
)

data class ChatMessage(
    val author: String,
    val content: String,
    val timestamp: String,
    val authorImage: Int? = null,
    val image: Int? = null,
    val messageType: WsMessageType = WsMessageType.UNKNOWN
)
