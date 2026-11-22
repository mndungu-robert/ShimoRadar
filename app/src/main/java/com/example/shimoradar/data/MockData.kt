package com.example.shimoradar.data

import androidx.annotation.DrawableRes
import androidx.compose.runtime.mutableStateListOf
import com.example.shimoradar.R

enum class ReportStatus(val label: String) {
    REPORTED("Reported"),
    UNDER_REPAIR("Under repair"),
    REPAIRED("Repaired")
}

data class RoadReport(
    val id: Int,
    val type: String,
    val location: String,
    val status: ReportStatus,
    val distance: String,
    val whenText: String,
    @DrawableRes val iconRes: Int,
    val severity: String = "Medium",
    val description: String = "",
    val photoPath: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

object MockData {
    // A state list: screens that read it update automatically when a report is added.
    val reports = mutableStateListOf(
        RoadReport(1, "Pothole", "Eastern Bypass, Ruiru Junction", ReportStatus.REPORTED, "0.4 km", "Today, 09:12", R.drawable.ic_pothole),
        RoadReport(2, "Alligator crack", "Lang'ata Rd, near T-Mall", ReportStatus.UNDER_REPAIR, "1.2 km", "Yesterday", R.drawable.ic_crack),
        RoadReport(3, "Longitudinal crack", "Madaraka Est, Ole Sangale Rd", ReportStatus.REPAIRED, "2.8 km", "12 Nov", R.drawable.ic_crack)
    )

    fun nextId(): Int = (reports.maxOfOrNull { it.id } ?: 0) + 1

    @DrawableRes
    fun iconFor(type: String): Int = when (type) {
        "Pothole" -> R.drawable.ic_pothole
        "Flooding" -> R.drawable.ic_flood
        else -> R.drawable.ic_crack
    }
}