package com.example.shimoradar.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shimoradar.data.MockData
import com.example.shimoradar.data.ReportStatus
import com.example.shimoradar.data.RoadReport
import com.example.shimoradar.ui.components.StatusChip
import com.example.shimoradar.ui.components.colors
import com.example.shimoradar.ui.theme.CardWhite
import com.example.shimoradar.ui.theme.Coral
import com.example.shimoradar.ui.theme.Muted
import com.example.shimoradar.ui.theme.Navy
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import com.example.shimoradar.ui.util.loadPhoto

private val StepGrey = Color(0xFFCBD5E1)

@Composable
fun ReportDetailScreen(
    reportId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val report = MockData.reports.firstOrNull { it.id == reportId }
    if (report == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Report not found", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Navy)
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Navy, contentColor = Color.White),
                modifier = Modifier.padding(top = 16.dp)
            ) { Text("Back") }
        }
        return
    }

    val context = LocalContext.current
    var menuOpen by remember { mutableStateOf(false) }
    val (bg, fg) = report.status.colors()
    val steps = ReportStatus.values()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundIconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Navy)
            }
            Box {
                RoundIconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More options", tint = Navy)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Share report") },
                        leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            shareReport(context, report)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Open in Maps") },
                        leadingIcon = { Icon(Icons.Filled.Place, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            openInMaps(context, report)
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .size(76.dp)
                .background(bg, RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(report.iconRes),
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(38.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(report.type, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
        Spacer(Modifier.height(8.dp))
        StatusChip(report.status)
        report.photoPath?.let { path ->
            val photo = remember(path) { loadPhoto(path) }
            if (photo != null) {
                Spacer(Modifier.height(16.dp))
                Image(
                    bitmap = photo,
                    contentDescription = "Photo of the damage",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(22.dp))
                )
            }
        }

        Spacer(Modifier.height(24.dp))
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
                InfoRow("Location", report.location)
                InfoRow("Reported", report.whenText)
                InfoRow("Distance from you", report.distance)
                InfoRow("Severity", report.severity)
                if (report.description.isNotBlank()) InfoRow("Notes", report.description)
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Progress", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
        Spacer(Modifier.height(12.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = CardWhite,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                steps.forEachIndexed { index, step ->
                    val reached = index <= report.status.ordinal
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(if (reached) Coral else StepGrey, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (reached) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Navy,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = step.label,
                            fontSize = 15.sp,
                            fontWeight = if (index == report.status.ordinal) FontWeight.Bold else FontWeight.Normal,
                            color = if (reached) Navy else Muted
                        )
                    }
                    if (index < steps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .padding(start = 9.dp)
                                .width(2.dp)
                                .height(22.dp)
                                .background(if (index < report.status.ordinal) Coral else StepGrey)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun RoundIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .shadow(2.dp, CircleShape)
            .background(CardWhite, CircleShape),
        content = content
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Muted)
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Navy)
    }
}

private fun shareReport(context: Context, report: RoadReport) {
    val text = "${report.type} reported at ${report.location} (status: ${report.status.label})"
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, "Share report"))
}

private fun openInMaps(context: Context, report: RoadReport) {
    val uri = Uri.parse("geo:0,0?q=" + Uri.encode(report.location))
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No maps app found", Toast.LENGTH_SHORT).show()
    }
}