package com.example.shimoradar.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

val DamageTypes = listOf("Pothole", "Crack", "Flooding", "Other")
val SeverityLevels = listOf("Low", "Medium", "High")

data class Detection(
    val type: String,
    val confidence: Float,
    val label: String,
    val boxes: List<DetectedBox>
)

interface DamageDetector {
    suspend fun detect(photoPath: String): Detection?
}

/** Used until the model has finished loading. */
object NoDetector : DamageDetector {
    override suspend fun detect(photoPath: String): Detection? = null
}

object Detector {
    var current: DamageDetector by mutableStateOf(NoDetector)

    /** The YOLO detector, or null while it is loading or if the model file is missing. */
    val yolo: YoloDetector?
        get() = (current as? YoloDetector)?.takeIf { it.isReady }
}