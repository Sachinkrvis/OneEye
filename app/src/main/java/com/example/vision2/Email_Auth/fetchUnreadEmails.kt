package com.example.vision2.Email_Auth

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

suspend fun fetchUnreadEmails(context: Context): List<String> = withContext(Dispatchers.IO) {
    val resultList = mutableListOf<String>()
    try {
        val account = GoogleSignIn.getLastSignedInAccount(context)?.account
        if (account == null) {
            Log.e("GmailFetch", "No signed-in account found.")
            return@withContext emptyList()
        }

        val scope = "oauth2:https://www.googleapis.com/auth/gmail.readonly"
        val accessToken = GoogleAuthUtil.getToken(context, account, scope)

        // Step 1: Get list of unread message IDs
        val listUrl = URL("https://gmail.googleapis.com/gmail/v1/users/me/messages?q=is:unread")
        val listConn = listUrl.openConnection() as HttpURLConnection
        listConn.setRequestProperty("Authorization", "Bearer $accessToken")
        listConn.requestMethod = "GET"

        val listResponse = listConn.inputStream.bufferedReader().readText()
        val messageIds = JSONObject(listResponse).optJSONArray("messages")

        if (messageIds == null || messageIds.length() == 0) {
            return@withContext listOf("No unread messages found.")
        }

        for (i in 0 until minOf(messageIds.length(), 5)) { // Limit to 5 emails
            val msgId = messageIds.getJSONObject(i).getString("id")

            val msgUrl = URL("https://gmail.googleapis.com/gmail/v1/users/me/messages/$msgId?format=metadata")
            val msgConn = msgUrl.openConnection() as HttpURLConnection
            msgConn.setRequestProperty("Authorization", "Bearer $accessToken")
            msgConn.requestMethod = "GET"

            val msgResponse = msgConn.inputStream.bufferedReader().readText()
            val msgJson = JSONObject(msgResponse)
            val headers = msgJson.getJSONObject("payload").getJSONArray("headers")

            var subject = "No Subject"
            var from = "Unknown Sender"

            for (j in 0 until headers.length()) {
                val header = headers.getJSONObject(j)
                when (header.getString("name")) {
                    "Subject" -> subject = header.getString("value")
                    "From" -> from = header.getString("value")
                }
            }

            resultList.add("From: $from\nSubject: $subject")
        }
    } catch (e: UserRecoverableAuthException) {
        Log.e("GmailFetch", "Authorization required: ${e.intent}")
        resultList.add("Permission required. Please sign in again.")
    } catch (e: Exception) {
        Log.e("GmailFetch", "Failed to fetch unread emails", e)
        resultList.add("Failed to fetch unread emails.")
    }

    return@withContext resultList
}
