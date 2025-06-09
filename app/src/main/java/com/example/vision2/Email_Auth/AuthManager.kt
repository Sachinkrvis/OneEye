package com.example.vision2.Email_Auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object GmailAuthManager {

    // VITAL: Replace this with your "Web application" Client ID from Google Cloud Console
    // This is still needed for requesting an ID token if your backend needs it,
    // and it's good practice for Google Sign-In configuration.
    private const val WEB_CLIENT_ID = "767151700139-9v2sq520c53hiouehhju3ujjm1bt3em3.apps.googleusercontent.com" // <<<< IMPORTANT: REPLACE THIS
    private const val TAG = "GmailAuthManager"

    // Define the Gmail API scope
    private val GMAIL_SCOPES = Scope("https://www.googleapis.com/auth/gmail.readonly")

    private fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail() // To get the user's email address
            .requestScopes(GMAIL_SCOPES) // Request permission to read Gmail
            .requestIdToken(WEB_CLIENT_ID) // Optional: if you need ID token for a backend
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    // Call this function from your Composable when you want to initiate sign-in
    fun signIn(activity: Activity, signInLauncher: ActivityResultLauncher<Intent>) {
        val googleSignInClient = getGoogleSignInClient(activity)
        // Check if user is already signed in with required permissions
        val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(activity)
        if (lastSignedInAccount != null && GoogleSignIn.hasPermissions(lastSignedInAccount, GMAIL_SCOPES)) {
            Log.d(TAG, "Already signed in with Gmail permissions: ${lastSignedInAccount.email}")
            // If you want to immediately proceed to fetching emails, you'd call a callback here
            // For now, let's assume the launcher will handle the result which then triggers fetching
        }
        // Always launch the sign-in intent to allow account switching or fresh consent
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    // Call this function from your ActivityResultLauncher callback
    fun handleSignInResult(
        intent: Intent?,
        onSuccess: (account: GoogleSignInAccount) -> Unit,
        onError: (errorMessage: String) -> Unit
    ) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                if (GoogleSignIn.hasPermissions(account, GMAIL_SCOPES)) {
                    Log.d(TAG, "✅ Sign-in successful with Gmail permissions. Account: ${account.email}")
                    onSuccess(account)
                } else {
                    Log.e(TAG, "❌ Sign-in successful but Gmail permissions were NOT granted.")
                    onError("Gmail permissions were not granted. Please try signing in again and grant permission.")
                }
            } else {
                Log.e(TAG, "❌ Sign-in result account is null.")
                onError("Sign-in failed: Account is null.")
            }
        } catch (e: ApiException) {
            Log.e(TAG, "🚨 Sign-in failed with ApiException: ${e.statusCode} - ${e.message}", e)
            var friendlyMessage = "Sign-in failed: ${e.localizedMessage}"
            if (e.statusCode == 10) { // DEVELOPER_ERROR
                friendlyMessage = "Sign-in failed (Developer Error). Check SHA-1, package name, and API console configurations."
            } else if (e.statusCode == 7) { // NETWORK_ERROR
                friendlyMessage = "Sign-in failed: Network error. Please check your internet connection."
            } else if (e.statusCode == 12501) { // SIGN_IN_CANCELLED
                friendlyMessage = "Sign-in was cancelled."
            }
            onError(friendlyMessage)
        } catch (e: Exception) {
            Log.e(TAG, "🚨 An unexpected error occurred during sign-in result handling: ${e.message}", e)
            onError("An unexpected error occurred during sign-in.")
        }
    }

    // Function to fetch unread emails using the GoogleSignInAccount
    suspend fun fetchUnreadEmails(context: Context, account: GoogleSignInAccount): List<String> {
        // Ensure we are on a background thread for network operations
        return withContext(Dispatchers.IO) {
            val results = mutableListOf<String>()
            try {
                // Create a GoogleAccountCredential using the signed-in account
                // This credential will automatically manage the access token.
                val credential = GoogleAccountCredential.usingOAuth2(
                    context,
                    listOf(GMAIL_SCOPES.scopeUri)
                ).setSelectedAccount(account.account) // Use the specific account

                val gmailService = Gmail.Builder(
                    NetHttpTransport(),
//                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
                )
                    .setApplicationName("Your Application Name") // Replace with your app name
                    .build()

                // List unread messages [1]
                val listMessagesResponse = gmailService.users().messages()
                    .list("me") // "me" refers to the authenticated user
                    .setQ("is:unread") // Query for unread messages
                    .setMaxResults(1) // Limit results for this example
                    .execute()

                val messages = listMessagesResponse.messages
                if (messages.isNullOrEmpty()) {
                    results.add("No unread emails found.")
                } else {
                    results.add("Unread Emails (${messages.size}):")
                    for (messageSummary in messages) {
                        // Get the full message [2]
                        val message = gmailService.users().messages()
                            .get("me", messageSummary.id)
                            .setFormat("metadata") // "metadata" for headers, "full" for full content
                            .setFields("id,snippet,payload/headers") // Specify fields to retrieve
                            .execute()

                        val subject = message.payload?.headers?.find { it.name == "Subject" }?.value ?: "No Subject"
                        val from = message.payload?.headers?.find { it.name == "From" }?.value ?: "Unknown Sender"
                        results.add("From: $from\nSubject: $subject\nSnippet: ${message.snippet ?: "N/A"}")
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "🚨 IOException during Gmail API call: ${e.message}", e)
                results.add("Error fetching emails (IO): ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "🚨 Exception during Gmail API call: ${e.message}", e)
                results.add("Error fetching emails: ${e.message}")
            }
            results
        }
    }
}
