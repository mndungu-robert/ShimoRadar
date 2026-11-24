package com.example.shimoradar.ui.screens

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.shimoradar.data.MockData
import com.example.shimoradar.data.ReportStatus
import com.example.shimoradar.ui.components.ReportCard
import com.example.shimoradar.ui.theme.Muted
import com.example.shimoradar.ui.theme.Navy
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File

private val PinRed = Color(0xFFDC2626)
private val PinAmber = Color(0xFFF59E0B)
private val PinGreen = Color(0xFF10B981)

@Composable
fun MapScreen(
    onReportClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner

    val reports = MockData.reports.filter { it.latitude != null && it.longitude != null }
    var selectedId by remember { mutableStateOf<Int?>(null) }
    val selected = reports.firstOrNull { it.id == selectedId }

    val mapView = remember {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            userAgentValue = context.packageName
            osmdroidBasePath = File(context.cacheDir, "osmdroid")
            osmdroidTileCache = File(context.cacheDir, "osmdroid/tiles")
        }
        val start = reports.firstOrNull()
            ?.let { GeoPoint(it.latitude!!, it.longitude!!) }
            ?: GeoPoint(-1.1500, 36.9600)
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(14.0)
            controller.setCenter(start)
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onDetach()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            "Map",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Navy,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp)
        )
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendDot(PinRed, "Reported")
            Spacer(Modifier.width(14.dp))
            LegendDot(PinAmber, "Under repair")
            Spacer(Modifier.width(14.dp))
            LegendDot(PinGreen, "Repaired")
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 104.dp)
                .clip(RoundedCornerShape(28.dp))
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { mapView },
                update = { map ->
                    map.overlays.clear()
                    reports.forEach { report ->
                        val marker = Marker(map).apply {
                            position = GeoPoint(report.latitude!!, report.longitude!!)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            icon = markerIcon(report.status)
                            setOnMarkerClickListener { _, _ ->
                                selectedId = report.id
                                true
                            }
                        }
                        map.overlays.add(marker)
                    }
                    map.invalidate()
                }
            )
            selected?.let { report ->
                ReportCard(
                    report = report,
                    trailingText = report.severity,
                    onClick = { onReportClick(report.id) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 12.sp, color = Muted, fontWeight = FontWeight.SemiBold)
    }
}

private fun markerIcon(status: ReportStatus): Drawable {
    val color = when (status) {
        ReportStatus.REPORTED -> 0xFFDC2626.toInt()
        ReportStatus.UNDER_REPAIR -> 0xFFF59E0B.toInt()
        ReportStatus.REPAIRED -> 0xFF10B981.toInt()
    }
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
        setStroke(4, android.graphics.Color.WHITE)
        setSize(48, 48)
    }
}