package com.example.compose.jetchat.feature.voicetotext

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

// NOTE: VoiceBubble data class is defined in VoiceModels.kt and reused here.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceToTextScreenV6(onBack: () -> Unit = {}) {

    val context = LocalContext.current
    val activity = context as? Activity

    var isListening by remember { mutableStateOf(false) }
    var partialText by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Tap the mic and start speaking") }

    var bubbles by remember { mutableStateOf(listOf<VoiceBubble>()) }

    // which bubble's 3-dot menu is open (Long? because VoiceBubble.id is Long)
    var expandedBubbleId by remember { mutableStateOf<Long?>(null) }

    // edit dialog state
    var editingBubble by remember { mutableStateOf<VoiceBubble?>(null) }
    var editText by remember { mutableStateOf("") }

    // Speech recognizer
    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else null
    }

    DisposableEffect(Unit) {
        // Connect WebSocket when this screen enters composition
        VoiceWsClient.connect()

        onDispose {
            // Cleanup when leaving the screen
            speechRecognizer?.destroy()
            VoiceWsClient.close()
        }
    }

    fun addBubbleFromText(text: String) {
        if (text.isBlank()) return
        bubbles = bubbles + VoiceBubble(text = text)

        // Send final recognized text to WebSocket
        VoiceWsClient.sendFinal(text)
    }

    fun startListening() {
        if (speechRecognizer == null) {
            status = "Speech recognition not available on this device"
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                status = "Listening…"
                partialText = ""
            }

            override fun onBeginningOfSpeech() {
                status = "Speak now"
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                status = "Finishing…"
            }

            override fun onError(error: Int) {
                isListening = false
                partialText = ""
                status = "Tap the mic and start speaking"
            }

            override fun onResults(resultsBundle: Bundle?) {
                isListening = false
                val matches =
                    resultsBundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.joinToString(" ") ?: ""
                addBubbleFromText(text)
                partialText = ""
                status = "Tap the mic and start speaking"
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial =
                    partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                partialText = partial?.firstOrNull() ?: ""

                // Send live partial text to WebSocket
                VoiceWsClient.sendPartial(partialText)
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(intent)
        isListening = true
        status = "Preparing mic…"
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
        partialText = ""
        status = "Tap the mic and start speaking"
    }

    fun ensureAudioPermissionAndStart() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            if (activity == null) {
                status = "Cannot request mic permission"
                return
            }

            status = "Requesting mic permission…"

            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1001
            )
            return
        }

        startListening()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Voice to Text – V6") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (bubbles.isNotEmpty() || partialText.isNotBlank()) {
                            val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE)
                                        as ClipboardManager
                            val fullText = buildString {
                                bubbles.forEach { appendLine(it.text) }
                                if (partialText.isNotBlank()) {
                                    appendLine(partialText)
                                }
                            }
                            clipboard.setPrimaryClip(
                                ClipData.newPlainText("VoiceTextV5", fullText)
                            )
                            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy all"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isListening) stopListening() else ensureAudioPermissionAndStart()
                }
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = "Mic"
                )
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
        ) {

            // Status line
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                textAlign = TextAlign.Center
            )

            // Bubbles list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(bubbles, key = { _, item -> item.id }) { _, bubble ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        // Bubble
                        ChatBubble(text = bubble.text)

                        // 3-dot icon + anchored dropdown
                        Box(
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            IconButton(
                                onClick = {
                                    expandedBubbleId = bubble.id
                                    editText = bubble.text
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "Options"
                                )
                            }

                            DropdownMenu(
                                expanded = expandedBubbleId == bubble.id,
                                onDismissRequest = { expandedBubbleId = null }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = {
                                        expandedBubbleId = null
                                        editingBubble = bubble
                                        editText = bubble.text
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                        bubbles = bubbles.filterNot { it.id == bubble.id }
                                        expandedBubbleId = null
                                    }
                                )
                            }
                        }
                    }
                }

                // Live partial text as fading bubble at bottom
                if (partialText.isNotBlank()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            ChatBubble(text = partialText, isLive = true)
                        }
                    }
                }
            }

            // Bottom actions row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    bubbles = emptyList()
                    partialText = ""
                }) {
                    Text("Clear")
                }

                Text(
                    text = if (isListening) "Listening…" else "Idle",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isListening)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Edit dialog
        if (editingBubble != null) {
            AlertDialog(
                onDismissRequest = { editingBubble = null },
                title = { Text("Edit text") },
                text = {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val target = editingBubble
                        if (target != null) {
                            bubbles = bubbles.map {
                                if (it.id == target.id) it.copy(text = editText)
                                else it
                            }
                        }
                        editingBubble = null
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingBubble = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun ChatBubble(
    text: String,
    isLive: Boolean = false
) {
    val background: Color
    val contentColor: Color

    if (isLive) {
        background = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        contentColor = MaterialTheme.colorScheme.onSurface
    } else {
        background = MaterialTheme.colorScheme.primary
        contentColor = MaterialTheme.colorScheme.onPrimary
    }

    Box(
        modifier = Modifier
            .padding(start = 48.dp, end = 4.dp)
            // ⭐ limit bubble width so the 3-dot icon always fits
            .fillMaxWidth(0.8f)   // bubble can use up to 80% of row width
            .clip(
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 0.dp
                )
            )
            .background(background)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VoiceToTextScreenV6Preview() {
    MaterialTheme {
        VoiceToTextScreenV6()
    }
}
