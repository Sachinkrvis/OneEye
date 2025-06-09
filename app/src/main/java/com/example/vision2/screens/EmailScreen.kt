package com.example.vision2.screens

import android.app.Activity
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vision2.Email_Auth.GmailAuthManager
import kotlinx.coroutines.launch
import java.util.Locale

private const val TAG = "EmailScreen"

@Composable
fun EmailScreen() {
    val context = LocalContext.current
    val activity = context as? Activity // Use safe cast
    val coroutineScope = rememberCoroutineScope()

    var emailResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSignedIn by remember { mutableStateOf(false) } // Track sign-in state

    // Initialize TextToSpeech
    val tts = remember {
        TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val tts = TextToSpeech(context) { status ->
                }

                tts.setLanguage(Locale.US) // Ensure tts is not null
                Log.d(TAG, "TextToSpeech initialized successfully.")
            } else {
                Log.e(TAG, "TextToSpeech initialization failed with status: $status")
            }
        }
    }
    // Ensure TTS is shut down when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
            Log.d(TAG, "TextToSpeech shutdown.")
        }
    }

    // Launcher for the Google Sign-In intent
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent: Intent? = result.data
                GmailAuthManager.handleSignInResult(
                    intent = intent,
                    onSuccess = { account ->
                        Log.d(TAG, "Sign-in successful via launcher. Account: ${account.email}")
                        isSignedIn = true
                        errorMessage = null
                        coroutineScope.launch {
                            isLoading = true
                            emailResults =
                                GmailAuthManager.fetchUnreadEmails(context, account) // first line
//                            emailResults =
//                                if (allEmails.isNotEmpty()) listOf(allEmails.first()) else emptyList()
                            isLoading = false
                            if (emailResults.isNotEmpty() && !emailResults.first()
                                    .startsWith("Error")
                            ) {
                                val textToSpeak = if (emailResults.first()
                                        .startsWith("No unread emails found.")
                                ) {
                                    "No unread emails found."
                                } else {
                                    "Here are your unread emails. " + emailResults.joinToString(
                                        separator = ". Next email: "
                                    )
                                }
                                tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        }
                    },
                    onError = { errorMsg ->
                        Log.e(TAG, "Sign-in error via launcher: $errorMsg")
                        errorMessage = errorMsg
                        emailResults = listOf("Error: $errorMsg") // Show error in UI
                        isLoading = false
                        isSignedIn = false
                    }
                )
            } else {
                Log.w(
                    TAG,
                    "Google Sign-In flow was cancelled or failed. Result code: ${result.resultCode}"
                )
                // Optionally handle this as an error, e.g. if result.data has error info
                if (result.resultCode != Activity.RESULT_CANCELED) { // Don't show error if user just pressed back
                    errorMessage = "Sign-in process was not completed."
                }
                isLoading = false
            }
        }
    )

    // Attempt to sign in automatically or check existing sign-in when screen launches
    // This uses the traditional GoogleSignInClient to check for an existing session with scopes
    LaunchedEffect(Unit) {
        if (activity == null) {
            errorMessage = "Error: Could not get Activity context."
            return@LaunchedEffect
        }
        val lastSignedInAccount =
            com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(activity)
        val gmailScope =
            com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/gmail.readonly")

        if (lastSignedInAccount != null && com.google.android.gms.auth.api.signin.GoogleSignIn.hasPermissions(
                lastSignedInAccount,
                gmailScope
            )
        ) {
            Log.d(TAG, "Already signed in with Gmail permissions: ${lastSignedInAccount.email}")
            isSignedIn = true
            errorMessage = null
            isLoading = true
            emailResults =
                GmailAuthManager.fetchUnreadEmails(context, lastSignedInAccount)
            isLoading = false
            if (emailResults.isNotEmpty() && !emailResults.first().startsWith("Error")) {
                val textToSpeak = if (emailResults.first().startsWith("No unread emails found.")) {
                    "No unread emails found."
                } else {
                    "Here are your unread emails. " + emailResults.joinToString(separator = ". Next email: ")
                }
                tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        } else {
            Log.d(TAG, "Not signed in or missing Gmail permissions. User needs to sign in.")
            // Don't automatically trigger sign-in here, let the button do it.
            // Or, if you want auto sign-in: GmailAuthManager.signIn(activity, signInLauncher)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        if (!isSignedIn && activity != null) {
            Button(onClick = {
                isLoading = true // Show loader while sign-in intent is being prepared/launched
                errorMessage = null
                GmailAuthManager.signIn(activity, signInLauncher)
                // isLoading will be set to false by the launcher's result or if sign-in fails quickly
            }) {
                Text("Sign In with Google to View Emails")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (isSignedIn && !isLoading && emailResults.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(emailResults) { emailText ->
                    Text(
                        text = emailText,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            ElevatedCard(
                onClick = {
                    Log.d(TAG, "Reply with AI clicked for : $emailResults")
                    tts.speak(
                        "Preparing a Reply based on this email.",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        null
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp) // use height instead of size to maintain width
                    .padding(vertical = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Reply with AI",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

        } else if (isSignedIn && !isLoading && emailResults.isEmpty()) {
            // This case might be covered by "No unread emails found" from fetchUnreadEmails
            Text("No email results to display. Try fetching again if you expected some.")
        }
    }
}
