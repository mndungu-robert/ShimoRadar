package com.example.shimoradar.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shimoradar.R
import com.example.shimoradar.data.MockData
import com.example.shimoradar.ui.components.ReportCard
import com.example.shimoradar.ui.theme.AmberBg
import com.example.shimoradar.ui.theme.AmberFg
import com.example.shimoradar.ui.theme.CardWhite
import com.example.shimoradar.ui.theme.Coral
import com.example.shimoradar.ui.theme.CoralDark
import com.example.shimoradar.ui.theme.IndigoBg
import com.example.shimoradar.ui.theme.IndigoFg
import com.example.shimoradar.ui.theme.Muted
import com.example.shimoradar.ui.theme.Navy
import com.example.shimoradar.ui.theme.RedBg
import com.example.shimoradar.ui.theme.RedFg
import com.example.shimoradar.ui.theme.Sand
import com.example.shimoradar.ui.theme.ShimoradarTheme

@Composable
fun HomeScreen(
    onScanClick: () -> Unit,
    onSeeAllClick: () -> Unit,
    onReportClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp)
    ) {
        HomeHeader()
        Spacer(Modifier.height(22.dp))
        ScanBanner(onScanClick)
        Spacer(Modifier.height(24.dp))

        SectionTitle("What did you spot?")
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CategoryTile("Pothole", R.drawable.ic_pothole, RedBg, RedFg, Modifier.weight(1f))
            CategoryTile("Cracks", R.drawable.ic_crack, AmberBg, AmberFg, Modifier.weight(1f))
            CategoryTile("Flooding", R.drawable.ic_flood, IndigoBg, IndigoFg, Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            SectionTitle("Near you")
            Text(
                text = "See all",
                color = CoralDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onSeeAllClick)
            )
        }
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            MockData.reports.take(2).forEach { report ->
                ReportCard(
                    report = report,
                    trailingText = report.distance,
                    onClick = { onReportClick(report.id) }
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
}

@Composable
private fun HomeHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Coral, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("R", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Hi, Robert", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
            Text("Ruiru · 3 reports near you today", fontSize = 13.sp, color = Muted)
        }
        IconButton(
            onClick = { },
            modifier = Modifier
                .size(44.dp)
                .shadow(2.dp, CircleShape)
                .background(CardWhite, CircleShape)
        ) {
            Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = Navy)
        }
    }
}

@Composable
private fun ScanBanner(onScanClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = Coral
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "See a damaged road?",
                    fontSize = 22.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Navy
                )
                Text(
                    "Point your camera and we log it with your location.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Navy,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Button(
                    onClick = onScanClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Navy, contentColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .height(44.dp)
                ) {
                    Icon(painterResource(R.drawable.ic_camera), null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Scan road", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(8.dp))
            RoadIllustration()
        }
    }
}

@Composable
private fun RoadIllustration() {
    Canvas(modifier = Modifier.size(104.dp)) {
        val s = size.width / 104f
        drawRoundRect(
            color = Navy,
            topLeft = Offset(8f * s, 8f * s),
            size = Size(88f * s, 88f * s),
            cornerRadius = CornerRadius(24f * s)
        )
        drawLine(
            color = Sand,
            start = Offset(52f * s, 20f * s),
            end = Offset(52f * s, 84f * s),
            strokeWidth = 4f * s,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f * s, 8f * s))
        )
        drawOval(
            color = Coral,
            topLeft = Offset(36f * s, 45f * s),
            size = Size(32f * s, 18f * s)
        )
    }
}

@Composable
private fun CategoryTile(
    label: String,
    @DrawableRes icon: Int,
    bg: Color,
    fg: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = CardWhite,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(bg, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(icon), null, tint = fg, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Navy)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2E9E0, widthDp = 390, heightDp = 780)
@Composable
private fun HomeScreenPreview() {
    ShimoradarTheme {
        HomeScreen(onScanClick = {}, onSeeAllClick = {}, onReportClick = {})
    }
}