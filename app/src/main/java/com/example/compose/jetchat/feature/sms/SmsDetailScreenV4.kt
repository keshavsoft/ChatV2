package com.example.compose.jetchat.feature.sms

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.BadgedBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import java.text.SimpleDateFormat
import java.util.*

/**
 * NEW enhanced SMS list screen (Step 2).
 * Old SmsListScreen remains untouched.
 *
 * Assumes SMS permission is already granted.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsDetailScreenV4(
    onBack: () -> Unit,
    onConversationClick: (mobile: String) -> Unit
) {
    val context = LocalContext.current
    var smsGroups by remember { mutableStateOf<List<SmsGroup>>(emptyList()) }

    // Simple loading state
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        smsGroups = SmsRepository.getGroupedSms(context)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SMS Inbox V4") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    // simple loading indicator
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                smsGroups.isEmpty() -> {
                    Text(
                        text = "No SMS found",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(smsGroups, key = { it.mobile }) { group ->
                            SmsListItemV4(
                                group = group,
                                onClick = { onConversationClick(group.mobile) }
                            )
                            Divider(
                                modifier = Modifier.padding(start = 72.dp) // after avatar
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Single row in enhanced SMS list.
 */
@Composable
fun SmsListItemV4(
    group: SmsGroup,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with first letter
            AvatarCircle(initial = group.mobile.firstOrNull()?.uppercaseChar()?.toString() ?: "#")

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = group.mobile,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = group.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTimeShort(group.lastTimestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (group.messageCount > 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                    BadgedBox(
                        badge = {
                            Badge {
                                Text(
                                    text = group.messageCount.toString(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    ) {
                        // empty anchor – badge by itself
                        Spacer(modifier = Modifier.size(1.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarCircle(initial: String) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@SuppressLint("SimpleDateFormat")
private fun formatTimeShort(ts: Long): String {
    val now = Calendar.getInstance()
    val msgCal = Calendar.getInstance().apply { timeInMillis = ts }

    return when {
        isSameDay(now, msgCal) -> {
            SimpleDateFormat("hh:mm a").format(Date(ts))
        }
        isYesterday(now, msgCal) -> {
            "Yesterday"
        }
        else -> {
            SimpleDateFormat("dd MMM").format(Date(ts))
        }
    }
}

private fun isSameDay(c1: Calendar, c2: Calendar): Boolean {
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}

private fun isYesterday(now: Calendar, other: Calendar): Boolean {
    val yesterday = now.clone() as Calendar
    yesterday.add(Calendar.DAY_OF_YEAR, -1)
    return isSameDay(yesterday, other)
}
