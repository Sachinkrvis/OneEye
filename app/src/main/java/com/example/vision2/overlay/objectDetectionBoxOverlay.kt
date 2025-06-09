package com.example.vision2.overlay

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vision2.dataClass.DetectedObjectData


@Composable
fun ObjectDetectionBoxOverlay(
    detectedObjects: List<DetectedObjectData>,
    modifier: Modifier = Modifier,
    previewWidth: Int,
    previewHeight: Int,
    modelInputSize: Int = 320
) {
    Canvas(modifier = modifier.fillMaxSize()) {

        val scaleX = size.width / previewWidth.toFloat()
        val scaleY = size.height / previewHeight.toFloat()
        val scale = minOf(scaleX, scaleY)
        val offsetX = (size.width - (previewWidth * scale)) / 2
        val offsetY = (size.height - (previewHeight * scale)) / 2

        detectedObjects.forEach { obj ->
            val left = obj.boundingBox.left * scale + offsetX
            val top = obj.boundingBox.top * scale + offsetY
            val width = obj.boundingBox.width() * scale
            val height = obj.boundingBox.height() * scale

            // Draw bounding box
            drawRect(
                color = Color.Red,
                topLeft = Offset(left, top),
                size = Size(width, height),
                style = Stroke(width = 4.dp.toPx())
            )

            // Draw label and position
            val labelText = "${obj.label} (${left.toInt()}, ${top.toInt()})"
            drawContext.canvas.nativeCanvas.drawText(
                labelText,
                left,
                top - 10, // Offset above the bounding box
                android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 40f
                    isAntiAlias = true
                }
            )
        }
    }
}

