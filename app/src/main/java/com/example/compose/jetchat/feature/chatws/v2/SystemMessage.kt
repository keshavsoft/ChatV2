package com.example.compose.jetchat.feature.chatws.v2

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.compose.jetchat.feature.chatws.v1.ChatMessage

@Composable
fun SystemMessage(msg: ChatMessage) {
    Text(
        text = msg.content,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}
