package com.example.compose.jetchat.feature.chatws.v2

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun MessageAvatar(
    author: String,
    authorImage: Int?,
    onAuthorClick: (String) -> Unit
) {
    authorImage?.let { res ->
        Image(
            painter = painterResource(res),
            contentDescription = null,
            modifier = Modifier
                .clickable { onAuthorClick(author) }
                .padding(horizontal = 16.dp)
                .size(42.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } ?: Spacer(Modifier.width(74.dp))
}
