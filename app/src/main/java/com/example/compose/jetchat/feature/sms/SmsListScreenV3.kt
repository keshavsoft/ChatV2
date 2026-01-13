package com.example.compose.jetchat.feature.sms

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsListScreenV3(
    onBack: (() -> Unit)? = null,
    onSmsClick: (SmsGroup) -> Unit
) {
    val context = LocalContext.current

    var hasPermission by remember { mutableStateOf<Boolean?>(null) }
    var smsList by remember { mutableStateOf<List<SmsGroup>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 🔍 search query (survives rotation like other state)
    var query by rememberSaveable { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            smsList = SmsRepository.getGroupedSms(context)
            if (smsList.isEmpty()) {
                errorMessage = "No SMS found"
            }
        } else {
            errorMessage = "SMS permission denied"
        }
    }

    // Check permission once when screen opens
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            hasPermission = true
            smsList = SmsRepository.getGroupedSms(context)
            if (smsList.isEmpty()) {
                errorMessage = "No SMS found"
            }
        } else {
            permissionLauncher.launch(Manifest.permission.READ_SMS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "SMS Inbox V3") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
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
                hasPermission == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                hasPermission == false -> {
                    Text(
                        text = errorMessage ?: "SMS permission denied",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                smsList.isEmpty() -> {
                    Text(
                        text = errorMessage ?: "No SMS found",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    // ✅ We have SMS → show search + filtered list
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        // 🔍 Search bar
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search"
                                )
                            },
                            trailingIcon = {
                                if (query.isNotBlank()) {
                                    IconButton(onClick = { query = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear search"
                                        )
                                    }
                                }
                            },
                            placeholder = {
                                Text("Search by number or message")
                            }
                        )

                        // Filtered list based on query
                        val filteredList = if (query.isBlank()) {
                            smsList
                        } else {
                            val q = query.trim()
                            smsList.filter { group ->
                                group.mobile.contains(q, ignoreCase = true) ||
                                        group.lastMessage.contains(q, ignoreCase = true)
                            }
                        }

                        if (filteredList.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "No results for \"$query\"",
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredList, key = { it.mobile }) { sms ->
                                    SmsCard(
                                        group = sms,
                                        onClick = { onSmsClick(sms) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SmsCard(
    group: SmsGroup,
    onClick: () -> Unit
) {
    // For now we just use number as display name
    val displayName = group.mobile

    // Read once from composition
    val colorScheme = MaterialTheme.colorScheme

// Avatar color based on mobile hash
    val avatarColor = remember(group.mobile, colorScheme) {
        val palette = listOf(
            colorScheme.primaryContainer,
            colorScheme.secondaryContainer,
            colorScheme.tertiaryContainer
        )
        val index = group.mobile.hashCode().absoluteValue % palette.size
        palette[index]
    }


    // Bold if unread
    val titleFontWeight =
        if (group.isRead) FontWeight.SemiBold else FontWeight.Bold

    val subtitleFontWeight =
        if (group.isRead) FontWeight.Normal else FontWeight.SemiBold

    Surface(
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                shape = MaterialTheme.shapes.large,
                color = avatarColor,
                tonalElevation = 2.dp,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = displayName.firstOrNull()?.toString()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name + count
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = titleFontWeight
                    )
                )

                Text(
                    text = if (group.messageCount == 1)
                        "1 message"
                    else
                        "${group.messageCount} messages",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = subtitleFontWeight
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Time + unread dot
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatTime(group.lastTimestamp),
                    style = MaterialTheme.typography.labelSmall
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (!group.isRead) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

private fun formatTime_Old(ts: Long): String {
    if (ts == 0L) return ""
    val now = System.currentTimeMillis()
    val diff = now - ts
    val minutes = diff / (60 * 1000)
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hrs ago"
        days < 7 -> "$days days ago"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault())
            .format(Date(ts))
    }
}

private fun formatTime(ts: Long): String {
    if (ts == 0L) return ""

    val now = System.currentTimeMillis()
    val diff = now - ts

    val millisInMinute = 60 * 1000
    val millisInHour = 60 * millisInMinute
    val millisInDay = 24 * millisInHour

    if (diff < millisInMinute) {
        return "Just now"
    }

    val msgCal = Calendar.getInstance().apply { timeInMillis = ts }
    val nowCal = Calendar.getInstance()

    val sameYear = msgCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
    val sameDayOfYear = msgCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)

    // Today -> time
    if (sameYear && sameDayOfYear) {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return timeFormat.format(Date(ts))
    }

    // Yesterday
    val yesterdayCal = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }
    val isYesterday = msgCal.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR) &&
            msgCal.get(Calendar.DAY_OF_YEAR) == yesterdayCal.get(Calendar.DAY_OF_YEAR)

    if (isYesterday) {
        return "Yesterday"
    }

    // Within last 7 days
    if (diff < 7 * millisInDay) {
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault()) // Mon, Tue...
        return dayFormat.format(Date(ts))
    }

    // Older
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault()) // 01 Dec
    return dateFormat.format(Date(ts))
}
