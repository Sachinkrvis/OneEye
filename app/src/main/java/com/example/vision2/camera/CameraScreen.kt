package com.example.vision2.camera

import android.content.Context
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.vision2.ViewModels.DomainLayer
import com.example.vision2.dataClass.DetectedObjectData
import com.example.vision2.overlay.ObjectDetectionBoxOverlay
import java.util.Locale

@Composable
fun StartNavigationScreen(
    modifier: Modifier,
//    receivedData: Int
) {
    val viewModel: DomainLayer = hiltViewModel()
    CameraContent(modifier = modifier, viewModel = viewModel)
}

@Composable
private fun CameraContent(
    modifier: Modifier,
    viewModel: DomainLayer
) {

    val receivedData by viewModel.receivedData
    val context: Context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val cameraController: LifecycleCameraController =
        remember { LifecycleCameraController(context) }
    var detectedText: String by remember { mutableStateOf("No text detected yet..") }
    var detectedLabel: String by remember { mutableStateOf("No Label detected yet..") }
    var detectedObjects: List<DetectedObjectData> by remember { mutableStateOf(emptyList()) }
    var detectedbarcode: String by remember { mutableStateOf("No barcode detected yet..") }
    var lastSpokenObjects: String? by remember { mutableStateOf(null) }
    var lastSpokenTime by remember { mutableStateOf(0L) }
    val previewView = remember { PreviewView(context) }

    val userLocationListResponse = viewModel.userLocationItem.collectAsState()

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val tts = remember {
        TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val tts = TextToSpeech(context) { status ->

                }
                tts.language = Locale.US
            }
        }
    }

    fun speakText(text: String) {
        val currentTime = System.currentTimeMillis()
        val isDuplicate = text == lastSpokenObjects
        val isTooSoon = (currentTime - lastSpokenTime) < 4000 // 4 seconds delay

        if (!isDuplicate && !isTooSoon) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            lastSpokenObjects = text
            lastSpokenTime = currentTime
        }
    }

    fun onTextUpdated(updatedText: String) {
        detectedText = updatedText
    }

    fun onLabelsUpdated(updatedLabels: List<String>) {
        detectedLabel = updatedLabels.toString()
    }

    fun onButtonSelected() {

        if (userLocationListResponse.value.isEmpty()) {
            speakText("No saved routes found.")
        } else {
            speakText("User has set a route. Would you like to navigate?")
        }
    }


    fun onObjectsDetected(updatedObjects: List<DetectedObjectData>) {
        val distanceText = viewModel.receivedData.value
        val currentDistance = distanceText

        if (currentDistance != null && currentDistance < 125) {
            speakText("Stop! Object is very near.")
            return
        }

        if (updatedObjects.isNotEmpty()) {
            detectedObjects = updatedObjects
            val obj = updatedObjects.first()

            val previewW = previewView.width.takeIf { it > 0 } ?: 1
            val previewH = previewView.height.takeIf { it > 0 } ?: 1
            val canvasW = canvasSize.width.takeIf { it > 0 } ?: 1
            val canvasH = canvasSize.height.takeIf { it > 0 } ?: 1

            val scaleX = canvasW.toFloat() / previewW
            val scaleY = canvasH.toFloat() / previewH
            val scale = minOf(scaleX, scaleY)

            val offsetX = (canvasW - (previewW * scale)) / 2
            val centerX = obj.boundingBox.centerX() * scale + offsetX

            val horizontalPosition = when {
                centerX < canvasW / 3f -> "Left"
                centerX > 2 * canvasW / 3f -> "Right"
                else -> "Center"
            }

            val guidanceMessage = when (horizontalPosition) {
                "Left" -> "Move right slowly, obstacle on the left."
                "Right" -> "Move left slowly, obstacle on the right."
                else -> "Slow down, object ahead."
            }

            speakText(guidanceMessage)
        } else {
            speakText("No obstacle detected.")
        }
    }

//    fun onObjectsDetected(updatedObjects: List<DetectedObjectData>) {
//        val currentDistance = viewModel.receivedData.value
//
//        // Always check receivedData first — regardless of whether objects are detected or not
//        if (currentDistance < 50) {
//            Log.d(
//                "ObjectDetection",
//                "ReceivedData: $receivedData, UpdatedObjects: ${updatedObjects.size}"
//            )
//            speakText("Stop! Object is very near.")
//
//        }
//
//        // Now proceed with object-based feedback only if receivedData is safe
//        if (updatedObjects.isNotEmpty()) {
//            detectedObjects = updatedObjects
//            val obj = updatedObjects.first() // Take the first detected object
//            val centerX = obj.boundingBox.centerX()
//            val boxHeight = obj.boundingBox.height()
//
//            val screenWidth = previewView.width
//
//            // Define left, center, right regions
//            val horizontalPosition = when {
//                centerX < screenWidth / 3 -> "Left"
//                centerX > 2 * screenWidth / 3 -> "Right"
//                else -> "Center"
//            }
//
//            val guidanceMessage = when (horizontalPosition) {
//                "Left" -> "Move right slowly, obstacle on the left."
//                "Right" -> "Move left slowly, obstacle on the right."
//                else -> "Slow down, object ahead."
//            }
//
//            speakText(guidanceMessage)
//        }
//    }


    // Cleanup TTS when Composable is destroyed
    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size -> canvasSize = size }
    ) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                previewView.apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(Color.BLACK)
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FIT_START
                }.also { previewView ->
                    startTextAndImageRecognition(
                        context = context,
                        cameraController = cameraController,
                        lifecycleOwner = lifecycleOwner,
                        previewView = previewView,
                        onDetectedTextUpdated = ::onTextUpdated,
                        onDetectedLabelsUpdated = ::onLabelsUpdated,
                        onDetectedObjectsUpdated = ::onObjectsDetected,
//                            onDetectedObjectsWithBounding = ::CameraWithBoundingBox,
//                            onBarcodesDetected = ::onBarcodesDetected
                    )
                }
            }
        )
        if (detectedObjects.isNotEmpty()) {
            ObjectDetectionBoxOverlay(
                detectedObjects = detectedObjects,
                previewWidth = previewView.width,
                previewHeight = previewView.height
            )
        }
        Column(
            modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Detected Text: $receivedData",
                modifier = Modifier,
                color = androidx.compose.ui.graphics.Color.White
            )
//            Button(onClick = { onButtonSelected() }) {
//                Text("Start Navigation")
//            }
        }
    }
}

private fun startTextAndImageRecognition(
    context: Context,
    cameraController: LifecycleCameraController,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    onDetectedTextUpdated: (String) -> Unit,
    onDetectedLabelsUpdated: (List<String>) -> Unit,
    onDetectedObjectsUpdated: (List<DetectedObjectData>) -> Unit

) {
    cameraController.imageAnalysisTargetSize = CameraController.OutputSize(
        android.util.Size(
            320,
            320
        )
    )

    cameraController.setImageAnalysisAnalyzer(
        ContextCompat.getMainExecutor(context),
        MultiAnalyzer(
            onDetectedTextUpdated = onDetectedTextUpdated,
            onDetectedLabelsUpdated = onDetectedLabelsUpdated,
            onObjectsDetected = onDetectedObjectsUpdated,
        )
    )

    cameraController.bindToLifecycle(lifecycleOwner)
    previewView.controller = cameraController
}

