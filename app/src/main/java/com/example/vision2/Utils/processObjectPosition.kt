package com.example.vision2.Utils
//
//var lastBoxHeight: Float? = null
//
//fun processObjectPosition(
//    centerX: Float,
//    centerY: Float,
//    boxHeight: Float,
//    screenWidth: Int,
//    screenHeight: Int
//) {
//    val horizontalPosition = when {
//        centerX < screenWidth / 3 -> "Left"
//        centerX > 2 * screenWidth / 3 -> "Right"
//        else -> "Center"
//    }
//
//    val estimatedDistance = when {
//        boxHeight > screenHeight * 0.6 -> "Very Near"
//        boxHeight > screenHeight * 0.4 -> "Near"
//        boxHeight > screenHeight * 0.2 -> "Medium Distance"
//        else -> "Far"
//    }
//
//    val movingCloser = lastBoxHeight?.let { boxHeight > it } ?: false
//    lastBoxHeight = boxHeight  // Update the last height
//
//    // Generate guidance based on movement
//    val guidanceMessage = when {
//        estimatedDistance == "Very Near" && movingCloser -> "Stop! Danger ahead."
//        estimatedDistance == "Near" -> when (horizontalPosition) {
//            "Left" -> "Move right carefully, object on the left."
//            "Right" -> "Move left carefully, object on the right."
//            else -> "Slow down, object ahead."
//        }
//        estimatedDistance == "Medium Distance" -> when (horizontalPosition) {
//            "Left" -> "Move right 2 steps."
//            "Right" -> "Move left 2 steps."
//            else -> "Move forward 2 steps."
//        }
//        else -> "Path is clear, move forward."
//    }
//
//    speakText(guidanceMessage)
//}
