@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.compose.jetchat.feature.chatws.v2

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.compose.jetchat.FunctionalityNotAvailablePopup
import com.example.compose.jetchat.R
import com.example.compose.jetchat.components.JetchatAppBar
import com.example.compose.jetchat.conversation.JumpToBottom
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
        Column(Modifier.fillMaxSize().padding(padding)) {
            Messages(
                messages = uiState.messages,
                navigateToProfile = navigateToProfile,
                modifier = Modifier.weight(1f),
                scrollState = scrollState
            )
            UserInput(
                onMessageSent = {
                    uiState.addMessage(ChatMessage(authorMe, it, timeNow))
                },
                resetScroll = {
                    scope.launch { scrollState.scrollToItem(0) }
                },
                modifier = Modifier.navigationBarsPadding().imePadding()
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

const val ConversationTestTag = "ConversationTestTag"

@Composable
fun Messages(
    messages: List<ChatMessage>,
    navigateToProfile: (String) -> Unit,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    Box(modifier) {
        val authorMe = stringResource(R.string.author_me)

        LazyColumn(
            reverseLayout = true,
            state = scrollState,
            modifier = Modifier.fillMaxSize().testTag(ConversationTestTag)
        ) {
            for (i in messages.indices) {
                val prev = messages.getOrNull(i - 1)?.author
                val next = messages.getOrNull(i + 1)?.author
                val msg = messages[i]

                if (i == messages.size - 1) item { DayHeader("20 Aug") }
                else if (i == 2) item { DayHeader("Today") }

                item {
                    Message(
                        onAuthorClick = navigateToProfile,
                        msg = msg,
                        isUserMe = msg.author == authorMe,
                        isFirstMessageByAuthor = prev != msg.author,
                        isLastMessageByAuthor = next != msg.author
                    )
                }
            }
        }

        val threshold = with(LocalDensity.current) { JumpToBottomThreshold.toPx() }
        val showJump by remember {
            derivedStateOf {
                scrollState.firstVisibleItemIndex != 0 ||
                        scrollState.firstVisibleItemScrollOffset > threshold
            }
        }

        JumpToBottom(
            enabled = showJump,
            onClicked = { scope.launch { scrollState.animateScrollToItem(0) } },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun Message(
    onAuthorClick: (String) -> Unit,
    msg: ChatMessage,
    isUserMe: Boolean,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean
) {
    val border = if (isUserMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
    Row(if (isLastMessageByAuthor) Modifier.padding(top = 8.dp) else Modifier) {
        if (isLastMessageByAuthor) {
            msg.authorImage?.let { imageRes ->
                Image(
                    painter = painterResource(imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .clickable { onAuthorClick(msg.author) }
                        .padding(horizontal = 16.dp)
                        .size(42.dp)
                        .clip(CircleShape)
                )
            } ?: Spacer(Modifier.width(74.dp))

        } else Spacer(Modifier.width(74.dp))

        AuthorAndTextMessage(
            msg,
            isUserMe,
            isFirstMessageByAuthor,
            isLastMessageByAuthor,
            onAuthorClick,
            Modifier.weight(1f).padding(end = 16.dp)
        )
    }
}

@Composable
fun AuthorAndTextMessage(
    msg: ChatMessage,
    isUserMe: Boolean,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
    authorClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        if (isLastMessageByAuthor) AuthorNameTimestamp(msg)
        ChatItemBubble(msg, isUserMe, authorClicked)
        Spacer(Modifier.height(if (isFirstMessageByAuthor) 8.dp else 4.dp))
    }
}

@Composable
private fun AuthorNameTimestamp(msg: ChatMessage) {
    Row(Modifier.semantics(mergeDescendants = true) {}) {
        Text(msg.author, modifier = Modifier.alignBy(LastBaseline))
        Spacer(Modifier.width(8.dp))
        Text(
            msg.timestamp,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.alignBy(LastBaseline),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private val ChatBubbleShape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)

@Composable
fun DayHeader(day: String) {
    Row(Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
        Divider(Modifier.weight(1f))
        Text(day, Modifier.padding(horizontal = 16.dp))
        Divider(Modifier.weight(1f))
    }
}

@Composable
fun ChatItemBubble1(
    message: ChatMessage,
    isUserMe: Boolean,
    authorClicked: (String) -> Unit
) {
    val bg = if (isUserMe) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.surfaceVariant

    Surface(color = bg, shape = ChatBubbleShape) {
        ClickableMessage(message, isUserMe, authorClicked)
    }
}

@Composable
fun ChatItemBubble(
    message: ChatMessage,
    isUserMe: Boolean,
    authorClicked: (String) -> Unit
) {
    when (message.messageType) {

        WsMessageType.IS_STUDENT -> {
            Surface(
                color = Color(0xFFE3F2FD),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "🎓 ${message.content}",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        WsMessageType.PHONE -> {
            Surface(
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "📞 ${message.content}",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        WsMessageType.UNKNOWN -> {
            Surface(
                color = if (isUserMe)
                    MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            ) {
                ClickableMessage(message, isUserMe, authorClicked)
            }
        }
    }
}


@Composable
fun ClickableMessage(
    message: ChatMessage,
    isUserMe: Boolean,
    authorClicked: (String) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val styled = messageFormatter(message.content, isUserMe)

    ClickableText(
        styled,
        Modifier.padding(16.dp),
        MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current)
    ) {
        styled.getStringAnnotations(it, it).firstOrNull()?.let {
            when (it.tag) {
                SymbolAnnotationType.LINK.name -> uriHandler.openUri(it.item)
                SymbolAnnotationType.PERSON.name -> authorClicked(it.item)
            }
        }
    }
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
fun ChatWsV2Screen1(onNavIconPressed: () -> Unit) {
    val uiState = remember {
        ChatWsUiState("#chat-ws-v2", 42, chatWsV2InitialMessages)
    }
    ChatWsV2Content(uiState, {}, onNavIconPressed = onNavIconPressed)
}

@Composable
fun ChatWsV2Screen2(onNavIconPressed: () -> Unit) {
    val timeNow = stringResource(R.string.now)
    val uiState = remember { ChatWsUiState("#chat-ws-v2", 42, emptyList()) }

    LaunchedEffect(Unit) {
        connectToServer.connect()
        connectToServer.incomingMessages.collect { msg ->
            uiState.addMessage(ChatMessage(author = "Server", content = msg, timestamp = timeNow))
        }
    }

    ChatWsV2Content(uiState, {}, onNavIconPressed = onNavIconPressed)
}

@Composable
fun ChatWsV2Screen(onNavIconPressed: () -> Unit) {
    val timeNow = stringResource(R.string.now)
    val uiState = remember {
        ChatWsUiState("#chat-ws-v2", 42, emptyList())
    }

    LaunchedEffect(Unit) {
        connectToServer.connect()
        connectToServer.incomingMessages.collect { raw ->
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
            }

            uiState.addMessage(
                ChatMessage(
                    author = "Server",
                    content = text,
                    timestamp = timeNow,
                    messageType = type    // 🔴 ADD THIS FIELD
                )
            )
        }
    }

    ChatWsV2Content(uiState, {}, onNavIconPressed = onNavIconPressed)
}

enum class WsMessageType {
    IS_STUDENT,
    PHONE,
    UNKNOWN
}

data class WsChatMessage(
    val author: String,
    val content: String,
    val timestamp: String,
    val type: WsMessageType
)
