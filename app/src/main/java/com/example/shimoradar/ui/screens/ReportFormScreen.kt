package com.example.shimoradar.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shimoradar.data.DamageTypes
import com.example.shimoradar.data.Detection
import com.example.shimoradar.data.Detector
import com.example.shimoradar.data.MockData
import com.example.shimoradar.data.ReportDraft
import com.example.shimoradar.data.ReportStatus
import com.example.shimoradar.data.RoadReport
import com.example.shimoradar.data.SeverityLevels
import com.example.shimoradar.ui.components.ChoiceRow
import com.example.shimoradar.ui.components.DetectionOverlay
import com.example.shimoradar.ui.theme.CardWhite
import com.example.shimoradar.ui.theme.Coral
import com.example.shimoradar.ui.theme.CoralDark
import com.example.shimoradar.ui.theme.CoralTint
import com.example.shimoradar.ui.theme.Muted
import com.example.shimoradar.ui.theme.Navy
import com.example.shimoradar.ui.util.loadPhoto
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun ReportFormScreen(
    photoName: String,
    onBack: () -> Unit,
    onSubmitted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val photoPath = remember(photoName) { File(File(context.filesDir, "reports"), photoName).path }
    val photo = remember(photoName) { loadPhoto(photoPath) }

    var type by remember { mutableStateOf(DamageTypes.first()) }
    var severity by remember { mutableStateOf(ReportDraft.severity) }
    var notes by remember { mutableStateOf("") }
    var detection by remember { mutableStateOf<Detection?>(null) }
    var analysing by remember { mutableStateOf(true) }
    var aiReady by remember { mutableStateOf(true) }
    val locationText = ReportDraft.locationText()

    // Run the AI model on the photo. The model loads in the background at app start,
    // so wait up to 5 seconds for it if the user was very quick.
    LaunchedEffect(photoPath) {
        var waited = 0
        while (Detector.yolo == null && waited < 5000) {
            delay(250)
            waited += 250
        }
        aiReady = Detector.yolo != null
        val result = Detector.current.detect(photoPath)
        detection = result
        analysing = false
        if (result != null) type = result.type
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(2.dp, CircleShape)
                    .background(CardWhite, CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Navy)
            }
            Spacer(Modifier.width(14.dp))
            Text("New report", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
        }

        Spacer(Modifier.height(20.dp))
        if (photo != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(22.dp))
            ) {
                Image(
                    bitmap = photo,
                    contentDescription = "Photo of the damage",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                detection?.let {
                    DetectionOverlay(
                        boxes = it.boxes,
                        imageWidth = photo.width,
                        imageHeight = photo.height,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(Color(0xFFCBD5E1), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) { Text("Photo unavailable", color = Muted) }
        }

        Spacer(Modifier.height(12.dp))
        AiResultCard(analysing = analysing, aiReady = aiReady, detection = detection)

        Spacer(Modifier.height(20.dp))
        FieldLabel("What is it?")
        ChoiceRow(options = DamageTypes, selected = type, onSelect = { type = it })

        Spacer(Modifier.height(16.dp))
        FieldLabel("How bad is it?")
        ChoiceRow(options = SeverityLevels, selected = severity, onSelect = { severity = it })

        Spacer(Modifier.height(16.dp))
        FieldLabel("Location")
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = CardWhite,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Place, contentDescription = null, tint = CoralDark)
                Spacer(Modifier.width(10.dp))
                Text(locationText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Navy)
            }
        }

        Spacer(Modifier.height(16.dp))
        FieldLabel("Notes (optional)")
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            placeholder = { Text("e.g. Deep hole near the junction") },
            minLines = 3,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CoralDark,
                unfocusedBorderColor = Color(0xFFCBD5E1),
                focusedContainerColor = CardWhite,
                unfocusedContainerColor = CardWhite
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                val report = RoadReport(
                    id = MockData.nextId(),
                    type = type,
                    location = locationText,
                    status = ReportStatus.REPORTED,
                    distance = "Nearby",
                    whenText = "Just now",
                    iconRes = MockData.iconFor(type),
                    severity = severity,
                    description = notes.trim(),
                    photoPath = photoPath,
                    latitude = ReportDraft.latitude,
                    longitude = ReportDraft.longitude,
                    aiLabel = detection?.let { "${it.label} · ${(it.confidence * 100).toInt()}%" }
                )
                MockData.reports.add(0, report)
                ReportDraft.clear()
                Toast.makeText(context, "Report submitted", Toast.LENGTH_SHORT).show()
                onSubmitted()
            },
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = Navy),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Submit report", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun AiResultCard(analysing: Boolean, aiReady: Boolean, detection: Detection?) {
    val title: String
    val subtitle: String
    when {
        analysing -> {
            title = "Analysing photo…"
            subtitle = "The AI model is checking the road surface"
        }
        !aiReady -> {
            title = "AI model not available"
            subtitle = "Check that best.tflite is in app/src/main/assets. You can still report manually."
        }
        detection == null -> {
            title = "No damage detected"
            subtitle = "You can still submit this report manually"
        }
        else -> {
            title = "AI detected: ${detection.label}"
            subtitle = "${(detection.confidence * 100).toInt()}% confident. Type pre-selected; change it if it's wrong."
        }
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = CoralTint
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Navy)
            Text(subtitle, fontSize = 13.sp, color = Muted, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Muted,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}