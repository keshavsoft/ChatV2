package com.example.compose.jetchat.feature.chatws.v2

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.compose.jetchat.feature.chatws.v1.ChatMessage

@Composable
fun StudentConnectedCard(msg: ChatMessage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Server • ${msg.timestamp}",
            style = MaterialTheme.typography.labelSmall
        )
        Surface(
            color = Color(0xFFE3F2FD),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = "🎓 Student Connected\n${msg.content}",
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
