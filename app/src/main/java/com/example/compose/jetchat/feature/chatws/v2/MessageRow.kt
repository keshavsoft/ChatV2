package com.example.compose.jetchat.feature.chatws.v2

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.compose.jetchat.feature.chatws.v1.AuthorAndTextMessage
import com.example.compose.jetchat.feature.chatws.v1.ChatMessage

@Composable
fun MessageRow(
    msg: ChatMessage,
    isUserMe: Boolean,
    isLastMessageByAuthor: Boolean,
    isFirstMessageByAuthor: Boolean,
    onAuthorClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        when (msg.messageType) {
            WsMessageType.SYSTEM ->
                SystemMessage(msg)

            WsMessageType.STUDENT_CONNECTED ->
                StudentConnectedCard(msg)

            else ->
                NormalChatRow(
                    msg,
                    isUserMe,
                    isLastMessageByAuthor,
                    isFirstMessageByAuthor,
                    onAuthorClick
                )
        }
    }
}

@Composable
private fun NormalChatRow(
    msg: ChatMessage,
    isUserMe: Boolean,
    isLastMessageByAuthor: Boolean,
    isFirstMessageByAuthor: Boolean,
    onAuthorClick: (String) -> Unit
) {
    Row(if (isLastMessageByAuthor) Modifier.padding(top = 8.dp) else Modifier) {
        if (isLastMessageByAuthor) {
            MessageAvatar(msg.author, msg.authorImage, onAuthorClick)
        }
        AuthorAndTextMessage(
            msg = msg,
            isUserMe = isUserMe,
            isFirstMessageByAuthor = isFirstMessageByAuthor,
            isLastMessageByAuthor = isLastMessageByAuthor,
            authorClicked = onAuthorClick,
            modifier = Modifier.weight(1f)
        )
    }
}
