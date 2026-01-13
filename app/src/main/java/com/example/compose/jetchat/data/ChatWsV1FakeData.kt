package com.example.compose.jetchat.data

import com.example.compose.jetchat.feature.chatws.v1.ChatMessage

val chatWsV1InitialMessages = listOf(
    ChatMessage(
        author = "me",
        content = "Hello 👋",
        timestamp = "8:06 PM"
    ),
    ChatMessage(
        author = "Keshav Nalam",
        content = "You can call me back on 9848163021",
        timestamp = "8:16 PM"
    ),
    ChatMessage(
        author = "me",
        content = "Greetings from KeshavSoft!",
        timestamp = "8:08 AM"
    )
)
