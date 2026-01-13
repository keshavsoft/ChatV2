package com.example.compose.jetchat.feature.chatws.v2

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.compose.jetchat.feature.chatws.v1.ChatMessage

@Composable
fun Messages(
    modifier: Modifier = Modifier,
    messages: List<ChatMessage>,
    me: String,
    scrollState: LazyListState,
    onAuthorClick: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        reverseLayout = true,
        state = scrollState
    ) {
    messages.forEachIndexed { i, msg ->
            val prev = messages.getOrNull(i - 1)?.author
            val next = messages.getOrNull(i + 1)?.author

            item {
                MessageRow(
                    msg = msg,
                    isUserMe = msg.author == me,
                    isFirstMessageByAuthor = prev != msg.author,
                    isLastMessageByAuthor = next != msg.author,
                    onAuthorClick = onAuthorClick
                )
            }
        }
    }
}
