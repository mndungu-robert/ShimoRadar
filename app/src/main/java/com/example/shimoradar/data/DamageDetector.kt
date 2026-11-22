package com.example.shimoradar.data

val DamageTypes = listOf("Pothole", "Crack", "Flooding")
val SeverityLevels = listOf("Low", "Medium", "High")

data class Detection(val type: String, val confidence: Float)

interface DamageDetector {
    suspend fun detect(photoPath: String): Detection?
}

/** Placeholder until the AI model is integrated. */
object NoDetector : DamageDetector {
    override suspend fun detect(photoPath: String): Detection? = null
}

object Detector {
    var current: DamageDetector = NoDetector
}