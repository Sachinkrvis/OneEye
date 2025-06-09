package com.example.vision2.screens

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vision2.R
import com.example.vision2.dataClass.Destination
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Home_Layout(
    modifier: Modifier,
    navController: NavController
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
//    val screenHeight = configuration.screenHeightDp.dp
    val totalArea = screenWidth * 0.8f
    val cardWidth = totalArea / 2
    val cardHeight = 300.dp
    val context: Context = LocalContext.current


    // Function for OnClick Feedback
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


    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            ElevatedCard(
                modifier = Modifier
                    .width(cardWidth)
                    .height(cardHeight)
                    .combinedClickable(
                        onClick = { speak("Translate Button is Selected") },
                        onLongClick = {
                            speak("Initiating Translate")
                            navController.navigate(Destination.TRANSLATE.route)
                        }
                    ),

                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_translate_24),
                            contentDescription = "Translate"
                        )
                        Text(text = "Translate", modifier = Modifier.padding(4.dp))
                    }

                }

            }
            ElevatedCard(
                modifier = Modifier
                    .width(cardWidth)
                    .height(cardHeight)
                    .combinedClickable(
                        onClick = { speak("Email Button is Selected") },
                        onLongClick = {
                            speak("Initiating Email")
                            navController.navigate(Destination.EMAIL.route)
                        }
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Filled.Email, contentDescription = "Email")
                        Text(text = "Email")
                    }

                }

            }

        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            ElevatedCard(
                modifier = Modifier
                    .width(cardWidth)
                    .height(cardHeight)
                    .combinedClickable(
                        onClick = { speak("Phone Call button is Selected") },
                        onLongClick = {
                            speak("Phone Call Initiated")
                            navController.navigate(Destination.PHONE_CALL.route)
                        }
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Filled.Call, contentDescription = "Phone Call")
                        Text(text = "Phone Call")
                    }
                }
            }
            ElevatedCard(
                modifier = Modifier
                    .width(cardWidth)
                    .height(cardHeight)
                    .combinedClickable(
                        onClick = { speak("Navigation Button is Selected") },
                        onLongClick = {
                            speak("Initiated Navigation")
                            navController.navigate(Destination.NAVIGATION.route)
                        }
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "Navigation"
                        )
                        Text(text = "Navigation")
                    }
                }

            }


        }
    }

}

//@Preview(showBackground = true)
//@Composable
//private fun Preview() {
//    Home_Layout(modifier = Modifier)
//}