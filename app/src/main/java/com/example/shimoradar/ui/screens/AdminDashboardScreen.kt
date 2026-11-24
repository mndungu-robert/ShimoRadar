package com.example.shimoradar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shimoradar.data.MockData
import com.example.shimoradar.data.ReportStatus
import com.example.shimoradar.data.RoadReport
import com.example.shimoradar.ui.components.ReportCard
import com.example.shimoradar.ui.theme.AmberFg
import com.example.shimoradar.ui.theme.CardWhite
import com.example.shimoradar.ui.theme.Coral
import com.example.shimoradar.ui.theme.CoralDark
import com.example.shimoradar.ui.theme.GreenFg
import com.example.shimoradar.ui.theme.Muted
import com.example.shimoradar.ui.theme.Navy
import com.example.shimoradar.ui.theme.RedFg

private fun severityRank(severity: String): Int = when (severity) {
    "High" -> 0
    "Medium" -> 1
    else -> 2
}

@Composable
fun AdminDashboardScreen(
    onReportClick: (Int) -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reports = MockData.reports
    val total = reports.size
    val newCount = reports.count { it.status == ReportStatus.REPORTED }
    val inRepair = reports.count { it.status == ReportStatus.UNDER_REPAIR }
    val fixed = reports.count { it.status == ReportStatus.REPAIRED }
    val open = total - fixed
    val fraction = if (total == 0) 0f else fixed.toFloat() / total

    val byType = reports
        .groupingBy { it.type }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
        .take(5)
    val maxCount = byType.maxOfOrNull { it.value } ?: 1

    val attention: List<RoadReport> = reports
        .filter { it.status != ReportStatus.REPAIRED }
        .sortedWith(compareBy<RoadReport>({ severityRank(it.severity) }, { -it.id }))
        .take(3)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("KURA Admin", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
                Text("Road damage overview", fontSize = 13.sp, color = Muted)
            }
            Text(
                "ADMIN",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .background(Navy, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Spacer(Modifier.height(18.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Navy
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "$open open " + if (open == 1) "report" else "reports",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "$fixed of $total repaired",
                    fontSize = 13.sp,
                    color = Color(0xFFCBD5E1),
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(8.dp)
                            .background(Coral, RoundedCornerShape(4.dp))
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Total", total.toString(), Navy, Modifier.weight(1f))
            StatCard("New", newCount.toString(), RedFg, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("In repair", inRepair.toString(), AmberFg, Modifier.weight(1f))
            StatCard("Repaired", fixed.toString(), GreenFg, Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))
        Text("By damage type", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
        Spacer(Modifier.height(12.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = CardWhite,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (byType.isEmpty()) {
                    Text("No reports yet", color = Muted, fontSize = 14.sp)
                }
                byType.forEach { entry ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            entry.key,
                            fontSize = 13.sp,
                            color = Navy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(120.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp)
                                .background(Color(0xFFE2E8F0), RoundedCornerShape(5.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(entry.value / maxCount.toFloat())
                                    .height(10.dp)
                                    .background(Coral, RoundedCornerShape(5.dp))
                            )
                        }
                        Text(
                            entry.value.toString(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(28.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text("Needs attention", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
            Text(
                "See all",
                color = CoralDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onSeeAllClick)
            )
        }
        Spacer(Modifier.height(12.dp))
        if (attention.isEmpty()) {
            Text("Everything is repaired. Nice work.", color = Muted, fontSize = 14.sp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            attention.forEach { report ->
                ReportCard(
                    report = report,
                    trailingText = "${report.severity} severity",
                    onClick = { onReportClick(report.id) }
                )
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = CardWhite,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Muted)
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = valueColor)
        }
    }
}