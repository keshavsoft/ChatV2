package com.example.compose.jetchat.data
import com.example.compose.jetchat.R
import com.example.compose.jetchat.conversation.ConversationUiState
import com.example.compose.jetchat.conversation.Message
import com.example.compose.jetchat.data.EMOJIS.EMOJI_PINK_HEART
import com.example.compose.jetchat.data.EMOJIS.EMOJI_FLAMINGO
import com.example.compose.jetchat.data.EMOJIS.EMOJI_CLOUDS

val keshavSoftHrMessages = listOf(
    Message(
        "me", // Sender
        "Good Varshita, that was awesome work.",
        "10:00 AM"
    ),
    Message(
        "KeshavSoft HR", // Sender
        "You're welcome! Have a great day! $EMOJI_PINK_HEART",
        "10:10 AM"
    ),
    Message(
        "me", // Sender
        "Thanks again!$EMOJI_FLAMINGO $EMOJI_CLOUDS",
        "10:12 AM"
    )
)

val keshavSoftHrUiState = ConversationUiState(
    initialMessages = keshavSoftHrMessages,
    channelName = "#keshavsoft_hr",
    channelMembers = 1 // Just you and the HR in this conversation
)
