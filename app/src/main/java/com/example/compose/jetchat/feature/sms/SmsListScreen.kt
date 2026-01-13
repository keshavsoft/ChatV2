package com.example.compose.jetchat.feature.sms

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsListScreen(
    onBack: (() -> Unit)? = null,
    onSmsClick: (SmsGroup) -> Unit
) {
    val context = LocalContext.current

    var hasPermission by remember { mutableStateOf<Boolean?>(null) }
    var smsList by remember { mutableStateOf<List<SmsGroup>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                title = { Text(text = "SMS Inbox") },
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
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(smsList, key = { it.mobile }) { sms ->
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

@Composable
private fun SmsCard(
    group: SmsGroup,
    onClick: () -> Unit
) {
    Surface(
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (group.messageCount > 1)
                        "${group.mobile} (${group.messageCount})"
                    else group.mobile,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Text(
                    text = formatTime(group.lastTimestamp),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = group.lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )
        }
    }
}

private fun formatTime(ts: Long): String {
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
