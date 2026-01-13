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
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceToTextScreenV4(onBack: () -> Unit = {}) {

    val context = LocalContext.current
    val activity = context as? Activity

    var isListening by remember { mutableStateOf(false) }
    var partialText by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Tap the mic and start speaking") }

    // List of finalized bubbles
    var bubbles by remember { mutableStateOf(listOf<VoiceBubble>()) }

    // Create SpeechRecognizer once
    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else null
    }

    // Destroy when composable leaves
    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.destroy()
        }
    }

    fun addBubbleFromText(text: String) {
        if (text.isBlank()) return
        bubbles = bubbles + VoiceBubble(text = text, isMe = true)
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
                title = { Text("Voice to Text – V4") },
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
                                ClipData.newPlainText("VoiceTextV4", fullText)
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
                verticalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = false
            ) {
                itemsIndexed(bubbles) { index, bubble ->
                    val isMe = bubble.isMe
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        ChatBubble(
                            text = bubble.text,
                            isMe = isMe
                        )
                    }
                }

                // Show live partial text as a temporary bubble at bottom
                if (partialText.isNotBlank()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            ChatBubble(
                                text = partialText,
                                isMe = true,
                                isLive = true
                            )
                        }
                    }
                }
            }

            // Bottom actions row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
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
    }
}

@Composable
private fun ChatBubble(
    text: String,
    isMe: Boolean,
    isLive: Boolean = false
) {
    val background: Color
    val contentColor: Color
    val alignmentPadding: PaddingValues

    if (isMe) {
        background = if (isLive) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        } else {
            MaterialTheme.colorScheme.primary
        }
        contentColor = if (isLive) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onPrimary
        }
        alignmentPadding = PaddingValues(start = 48.dp, end = 8.dp)
    } else {
        background = MaterialTheme.colorScheme.surfaceVariant
        contentColor = MaterialTheme.colorScheme.onSurface
        alignmentPadding = PaddingValues(start = 8.dp, end = 48.dp)
    }

    Box(
        modifier = Modifier
            .padding(alignmentPadding)
            .clip(
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomEnd = if (isMe) 0.dp else 16.dp,
                    bottomStart = if (isMe) 16.dp else 0.dp
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
fun VoiceToTextScreenV4Preview() {
    MaterialTheme {
        VoiceToTextScreenV4()
    }
}
