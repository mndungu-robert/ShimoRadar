package com.example.shimoradar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shimoradar.data.MockData
import com.example.shimoradar.data.ReportStatus
import com.example.shimoradar.ui.components.ReportCard
import com.example.shimoradar.ui.components.SegmentedTabs
import com.example.shimoradar.ui.theme.Muted
import com.example.shimoradar.ui.theme.Navy

@Composable
fun AdminReportsScreen(
    onReportClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val all = MockData.reports
    val tabs = listOf("All", "New", "In repair", "Fixed")
    var selected by rememberSaveable { mutableIntStateOf(0) }

    val shown = when (selected) {
        1 -> all.filter { it.status == ReportStatus.REPORTED }
        2 -> all.filter { it.status == ReportStatus.UNDER_REPAIR }
        3 -> all.filter { it.status == ReportStatus.REPAIRED }
        else -> all
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text("All reports", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
        Text(
            "${shown.size} shown · tap a report to update its status",
            fontSize = 13.sp,
            color = Muted,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(Modifier.height(16.dp))
        SegmentedTabs(labels = tabs, selectedIndex = selected, onSelect = { selected = it })
        Spacer(Modifier.height(14.dp))

        if (shown.isEmpty()) {
            Text("Nothing here yet", color = Muted, fontSize = 14.sp, modifier = Modifier.padding(top = 24.dp))
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(shown, key = { it.id }) { report ->
                    ReportCard(
                        report = report,
                        trailingText = "${report.severity} · ${report.whenText}",
                        onClick = { onReportClick(report.id) }
                    )
                }
            }
        }
    }
}