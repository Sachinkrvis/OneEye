package com.example.vision2.camera

import android.annotation.SuppressLint
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.vision2.dataClass.DetectedObjectData
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class MultiAnalyzer(
    private val onDetectedTextUpdated: (String) -> Unit,
    private val onDetectedLabelsUpdated: (List<String>) -> Unit,
    private val onObjectsDetected: (List<DetectedObjectData>) -> Unit,

) : ImageAnalysis.Analyzer {
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val imageLabeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder().setConfidenceThreshold(0.8f).build()
    )
    private val localModel = LocalModel.Builder()
        .setAssetFilePath("1.tflite")
        .build()
    private val objectDetector = ObjectDetection.getClient(
        CustomObjectDetectorOptions.Builder(localModel)
            .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE) // Continuous detection
            .enableMultipleObjects()
            .enableClassification() // object Detection Chalu karna
            .setClassificationConfidenceThreshold(0.6f)
            .setMaxPerObjectLabelCount(3) // 3 tak Label hona
            .build()
    )
// here
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val detectedText = visionText.text
                    onDetectedTextUpdated(detectedText)
                }
                .addOnFailureListener { e ->
                    Log.e("MultiAnalyzer", "Text Recognition failed: ${e.message}")
                }
            // Process image start karna
            imageLabeler.process(image)
                .addOnSuccessListener { labels ->
                    val detectedLabels = labels.map { it.text }
                    onDetectedLabelsUpdated(detectedLabels)
                }
                .addOnFailureListener { e ->
                    Log.e("MultiAnalyzer", "Image Labeling failed: ${e.message}")
                }
            // Process object detection Start karna
            objectDetector.process(image)
                .addOnSuccessListener { objects ->
                    val detectedList = objects.map { obj ->
                        val label = obj.labels.firstOrNull()?.text ?: "Unknown"
                        DetectedObjectData(obj.boundingBox, label, obj.trackingId)
                    }
//                    _detectedObjects.value = detectedList // Update StateFlow with full object details
                    onObjectsDetected(detectedList) // Send detected objects for further processing
                }
                .addOnFailureListener { e ->
                    Log.e("MLKit", "Object Detection failed: ${e.message}")
                }
//here2
                .addOnCompleteListener {
                    imageProxy.close()
                }

        } else {
            imageProxy.close()
        }
    }
}