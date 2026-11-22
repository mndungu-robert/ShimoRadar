package com.example.shimoradar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shimoradar.data.ReportStatus
import com.example.shimoradar.data.RoadReport
import com.example.shimoradar.ui.theme.AmberBg
import com.example.shimoradar.ui.theme.AmberFg
import com.example.shimoradar.ui.theme.CardWhite
import com.example.shimoradar.ui.theme.GreenBg
import com.example.shimoradar.ui.theme.GreenFg
import com.example.shimoradar.ui.theme.Muted
import com.example.shimoradar.ui.theme.Navy
import com.example.shimoradar.ui.theme.RedBg
import com.example.shimoradar.ui.theme.RedFg

/** Returns (background, foreground) colours for a status. */
fun ReportStatus.colors(): Pair<Color, Color> = when (this) {
    ReportStatus.REPORTED -> RedBg to RedFg
    ReportStatus.UNDER_REPAIR -> AmberBg to AmberFg
    ReportStatus.REPAIRED -> GreenBg to GreenFg
}

@Composable
fun StatusChip(status: ReportStatus, modifier: Modifier = Modifier) {
    val (bg, fg) = status.colors()
    Text(
        text = status.label,
        color = fg,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
fun ReportCard(
    report: RoadReport,
    trailingText: String = report.distance,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val (bg, fg) = report.status.colors()
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = CardWhite,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(bg, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(report.iconRes),
                    contentDescription = null,
                    tint = fg,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(report.type, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy)
                Text(
                    report.location,
                    fontSize = 13.sp,
                    color = Muted,
                    modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(report.status)
                    Spacer(Modifier.weight(1f))
                    Text(trailingText, fontSize = 12.sp, color = Muted)
                }
            }
        }
    }
}