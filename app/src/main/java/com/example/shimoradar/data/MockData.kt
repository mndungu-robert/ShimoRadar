package com.example.shimoradar.data

import androidx.annotation.DrawableRes
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
    @DrawableRes val iconRes: Int
)

object MockData {
    val reports = listOf(
        RoadReport(1, "Pothole", "Eastern Bypass, Ruiru Junction", ReportStatus.REPORTED, "0.4 km", "Today, 09:12", R.drawable.ic_pothole),
        RoadReport(2, "Alligator crack", "Lang'ata Rd, near T-Mall", ReportStatus.UNDER_REPAIR, "1.2 km", "Yesterday", R.drawable.ic_crack),
        RoadReport(3, "Longitudinal crack", "Madaraka Est, Ole Sangale Rd", ReportStatus.REPAIRED, "2.8 km", "12 Nov", R.drawable.ic_crack)
    )
}