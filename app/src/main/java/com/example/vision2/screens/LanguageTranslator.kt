package com.example.vision2.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LanguageTranslateScreen() {
    val context = LocalContext.current
    val deviceLanguage = Locale.getDefault().language
    var spokenText by remember { mutableStateOf("") }
    var translatedText by remember { mutableStateOf("") }

//    val ttsRef = remember { mutableStateOf<TextToSpeech?>(null) }


    val tts = remember {
        TextToSpeech(context) { clickable ->
            if (clickable == TextToSpeech.SUCCESS) {
                val tts = TextToSpeech(context) {

                }
                tts.language = Locale.US
            }

        }
    }

    fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // Cleanup TTS
    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    // Speech recognizer launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val resultText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            spokenText = resultText ?: ""
            if (spokenText.isNotEmpty()) {
                translateText(spokenText, deviceLanguage) {
                    translatedText = it
                    tts.speak(it, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }
        }
    }
    fun triggerLanguageRecognizer(){
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().language)
        }
        launcher.launch(intent)

    }

    // Trigger recognizer on screen launch
    LaunchedEffect(Unit) {
        triggerLanguageRecognizer()
//        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
//            putExtra(
//                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
//            )
//            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
//            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().language)
//        }
//        launcher.launch(intent)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Language Translator") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Spoken Sentence", style = MaterialTheme.typography.titleMedium)
                    Text(spokenText.ifEmpty { "No speech detected." })
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFBBDEFB))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Translated to ${Locale.getDefault().displayLanguage}", style = MaterialTheme.typography.titleMedium)
                    Text(translatedText.ifEmpty { "No translation yet." })
                }
            }
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp)
                    .combinedClickable(
                        onClick = { speak("Listening Button Selected") },
                        onLongClick = {
                            triggerLanguageRecognizer()
                        }
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("listening")

                }

            }
        }
    }
}

// Translates text using ML Kit
fun translateText(sourceText: String, targetLang: String, onTranslated: (String) -> Unit) {
    val sourceLangCode = TranslateLanguage.fromLanguageTag(detectLanguage(sourceText)) ?: TranslateLanguage.ENGLISH
    val targetLangCode = TranslateLanguage.fromLanguageTag(targetLang) ?: TranslateLanguage.ENGLISH

    val options = TranslatorOptions.Builder()
        .setSourceLanguage(sourceLangCode)
        .setTargetLanguage(targetLangCode)
        .build()

    val translator = Translation.getClient(options)

    translator.downloadModelIfNeeded().addOnSuccessListener {
        translator.translate(sourceText)
            .addOnSuccessListener { translatedText ->
                onTranslated(translatedText)
            }
            .addOnFailureListener {
                onTranslated("Translation failed.")
            }
    }.addOnFailureListener {
        onTranslated("Model download failed.")
    }
}

// Basic fallback language detection (extend with ML Kit Language ID if needed)
fun detectLanguage(text: String): String {
    return if (text.matches(Regex("[а-яА-Я]+"))) "ru"
    else "en" // fallback to English
}
