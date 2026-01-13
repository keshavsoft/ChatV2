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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceToTextScreen(onBack: () -> Unit = {}) {

    val context = LocalContext.current
    val activity = context as? Activity

    var status by remember { mutableStateOf("Tap the mic to start speaking") }
    var result by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }

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
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                status = "Listening..."
            }

            override fun onBeginningOfSpeech() {
                status = "Speak now"
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                status = "Processing..."
            }

            override fun onError(error: Int) {
                status = "Tap the mic to start speaking"
                isListening = false
            }

            override fun onResults(resultsBundle: Bundle?) {
                isListening = false
                val matches =
                    resultsBundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.joinToString(" ") ?: ""
                if (text.isNotBlank()) {
                    result = if (result.isBlank()) text else result + "\n" + text
                }
                status = "Tap the mic to start speaking"
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(intent)
        isListening = true
        status = "Preparing mic..."
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
        status = "Tap the mic to start speaking"
    }

    fun ensureAudioPermissionAndStart1() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    1001
                )
            }
            // user will tap again after granting
            return
        }
        startListening()
    }

    fun ensureAudioPermissionAndStart() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            if (activity == null) {
                // Should not normally happen, but show something instead of doing nothing
                status = "Cannot request mic permission (no Activity)"
                return
            }

            status = "Requesting mic permission…"

            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1001
            )

            // User must tap again after accepting permission
            return
        }

        // Permission already granted → start listening
        startListening()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice to Text") },
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Mic button
            ExtendedFloatingActionButton(
                onClick = {
                    if (isListening) stopListening() else ensureAudioPermissionAndStart()
                },
                icon = {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = null
                    )
                },
                text = {
                    Text(if (isListening) "Stop Listening" else "Start Listening")
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Text card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = if (result.isBlank())
                            "Your text will appear here..."
                        else result,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { result = "" }) {
                    Text("Clear")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = {
                    if (result.isNotBlank()) {
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(
                            ClipData.newPlainText("VoiceText", result)
                        )
                        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Copy")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VoiceToTextScreenPreview() {
    VoiceToTextScreen()
}
