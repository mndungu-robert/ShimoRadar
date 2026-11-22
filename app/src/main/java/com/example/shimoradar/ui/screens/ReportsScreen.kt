package com.example.shimoradar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shimoradar.data.MockData
import com.example.shimoradar.data.ReportStatus
import com.example.shimoradar.ui.components.SegmentedTabs
import com.example.shimoradar.ui.theme.Coral
import com.example.shimoradar.ui.theme.Muted
import com.example.shimoradar.ui.theme.Navy
import com.example.shimoradar.ui.components.ReportCard

@Composable
fun ReportsScreen(
    onReportClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val all = MockData.reports
    val tabs = listOf("All", "Active", "Fixed")
    var selected by rememberSaveable { mutableIntStateOf(0) }

    val shown = when (selected) {
        1 -> all.filter { it.status != ReportStatus.REPAIRED }
        2 -> all.filter { it.status == ReportStatus.REPAIRED }
        else -> all
    }
    val repaired = all.count { it.status == ReportStatus.REPAIRED }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text("My reports", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
        Spacer(Modifier.height(16.dp))
        SummaryCard(total = all.size, repaired = repaired)
        Spacer(Modifier.height(16.dp))
        SegmentedTabs(labels = tabs, selectedIndex = selected, onSelect = { selected = it })
        Spacer(Modifier.height(14.dp))

        if (shown.isEmpty()) {
            Text(
                "Nothing here yet",
                color = Muted,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 24.dp)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(shown, key = { it.id }) { report ->
                    ReportCard(
                        report = report,
                        trailingText = report.whenText,
                        onClick = { onReportClick(report.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(total: Int, repaired: Int) {
    val fraction = if (total == 0) 0f else repaired.toFloat() / total
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Navy
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$total " + if (total == 1) "report sent" else "reports sent",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "$repaired already repaired by KURA",
                        fontSize = 13.sp,
                        color = Color(0xFFCBD5E1),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Coral, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Navy)
                }
            }
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
}