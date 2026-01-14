@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.compose.jetchat.feature.chatws.v2

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.compose.jetchat.FunctionalityNotAvailablePopup
import com.example.compose.jetchat.R
import com.example.compose.jetchat.components.JetchatAppBar
import com.example.compose.jetchat.conversation.SymbolAnnotationType
import com.example.compose.jetchat.conversation.UserInput
import com.example.compose.jetchat.conversation.messageFormatter
import com.example.compose.jetchat.data.chatWsV2InitialMessages
import com.example.compose.jetchat.feature.chatws.v1.ChatMessage
import com.example.compose.jetchat.feature.chatws.v1.ChatWsUiState
import com.example.compose.jetchat.feature.webSocketCode.connectToServer
import com.example.compose.jetchat.theme.JetchatTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ChatWsV2Content(
    uiState: ChatWsUiState,
    navigateToProfile: (String) -> Unit,
    modifier: Modifier = Modifier,
    onNavIconPressed: () -> Unit = { }
) {
    val authorMe = stringResource(R.string.author_me)
    val timeNow = stringResource(R.string.now)

    val scrollState = rememberLazyListState()
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            ChannelNameBar(
                channelName = uiState.channelName,
                channelMembers = uiState.channelMembers,
                onNavIconPressed = onNavIconPressed,
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .exclude(WindowInsets.navigationBars)
            .exclude(WindowInsets.ime),
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        Column(Modifier
            .fillMaxSize()
            .padding(padding)) {

            Messages(
                modifier = Modifier.weight(1f),
                messages = uiState.messages,
                me = authorMe,
                scrollState = scrollState,
                onAuthorClick = {}
            )

            UserInput(
                onMessageSent = { text ->
                    connectToServer.send(text)   // ✅ SEND TO WS
                    uiState.addMessage(          // optional: echo locally
                        ChatMessage(authorMe, text, timeNow)
                    )
                },
                resetScroll = {
                    scope.launch { scrollState.scrollToItem(0) }
                },
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            )

        }
    }
}

@Composable
fun ChannelNameBar(
    channelName: String,
    channelMembers: Int,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onNavIconPressed: () -> Unit = { }
) {
    var popup by remember { mutableStateOf(false) }
    if (popup) FunctionalityNotAvailablePopup { popup = false }

    JetchatAppBar(
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        onNavIconPressed = onNavIconPressed,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(channelName, style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.members, channelMembers),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                modifier = Modifier
                    .clickable { popup = true }
                    .padding(16.dp)
            )
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier
                    .clickable { popup = true }
                    .padding(16.dp)
            )
        }
    )
}

@Preview
@Composable
fun ChannelBarPrev() {
    JetchatTheme {
        ChannelNameBar("Chat Ws V2", 42)
    }
}

private val JumpToBottomThreshold = 56.dp

@Composable
fun ChatWsV2Screen(onNavIconPressed: () -> Unit) {
    val timeNow = stringResource(R.string.now)
    val uiState = remember {
        ChatWsUiState("#chat-ws-v2", 42, emptyList())
    }

    LaunchedEffect(Unit) {
        connectToServer.connect()
        connectToServer.incomingMessages.collect { raw ->
            val isJson = raw.trim().startsWith("{") && raw.trim().endsWith("}")

            if (!isJson){
    uiState.addMessage(
        ChatMessage(
            author = "Server",
            content = raw,
            timestamp = timeNow,
            messageType = WsMessageType.SYSTEM
        )
    )
}else{
            val json = runCatching { org.json.JSONObject(raw) }.getOrNull()

            val type = when (json?.optString("Type")) {
                "IsStudent" -> WsMessageType.IS_STUDENT
                "Phone" -> WsMessageType.PHONE
                else -> WsMessageType.UNKNOWN
            }

            val text = when (type) {
                WsMessageType.IS_STUDENT -> "Student Connected\n${json?.optString("webSocketId")}"
                WsMessageType.PHONE -> json?.optString("number") ?: raw
                WsMessageType.UNKNOWN -> raw
                else -> {}
            }

            uiState.addMessage(
                ChatMessage(
                    author = "Server",
                    content = text as String,
                    timestamp = timeNow,
                    messageType = type    // 🔴 ADD THIS FIELD
                )
            )
}

        }
    }

    ChatWsV2Content(uiState, {}, onNavIconPressed = onNavIconPressed)
}

enum class WsMessageType {
    IS_STUDENT,
    PHONE,
    UNKNOWN,
    SYSTEM,
    STUDENT_CONNECTED
}

data class WsChatMessage(
    val author: String,
    val content: String,
    val timestamp: String,
    val type: WsMessageType
)
