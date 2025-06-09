package com.example.vision2.dataClass

import android.graphics.Rect

data class DetectedObjectData(
    val boundingBox: Rect,
    val label: String,
    val trackingId: Int? = null
)
