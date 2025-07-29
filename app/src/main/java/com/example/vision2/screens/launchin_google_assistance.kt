package com.example.vision2.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LaunchAssistantButton() {
    val context = LocalContext.current
    var launchMessage by remember { mutableStateOf("Click to Launch Google Assistant") }

    val tts = remember {
        TextToSpeech(context) { clickable ->
            if (clickable == TextToSpeech.SUCCESS) {
                val tts = TextToSpeech(context) {

                }
                tts.language = Locale.US
            }

        }
    }

    // Cleanup TTS when composable is removed
    DisposableEffect(Unit) {
        launchMessage = "Click to Launch Google Assistant"
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun launchAssistant() {
        val intent = Intent(Intent.ACTION_VOICE_COMMAND).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val fallbackIntent = Intent(Intent.ACTION_VOICE_COMMAND).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(fallbackIntent)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(context, "Google Assistant is not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Automatically launch assistant when this composable is first composed
    LaunchedEffect(Unit) {
        launchAssistant()

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = launchMessage,
            style = MaterialTheme.typography.titleMedium
        )

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .combinedClickable(
                    onClick = { speak("Assistant button selected") },
                    onLongClick = { launchAssistant()
                    launchMessage = "Launching Google Assistant...."
                    }
                ),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tap to hear\nLong press to launch Assistant",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}


