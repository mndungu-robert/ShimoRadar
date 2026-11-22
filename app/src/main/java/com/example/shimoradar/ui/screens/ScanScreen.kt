package com.example.shimoradar.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.LifecycleOwner
import com.example.shimoradar.R
import com.example.shimoradar.data.ReportDraft
import com.example.shimoradar.data.SeverityLevels
import com.example.shimoradar.data.fetchLocation
import com.example.shimoradar.data.lookupAddress
import com.example.shimoradar.ui.components.ChoiceRow
import com.example.shimoradar.ui.theme.Coral
import com.example.shimoradar.ui.theme.CoralDark
import com.example.shimoradar.ui.theme.CoralTint
import com.example.shimoradar.ui.theme.GreenFg
import com.example.shimoradar.ui.theme.Muted
import com.example.shimoradar.ui.theme.Navy
import com.example.shimoradar.ui.theme.RedFg
import java.io.File

private const val TAG = "ShimoRadar"

@Composable
fun ScanScreen(
    onBack: () -> Unit,
    onCaptured: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current

    // White status-bar icons on the dark scan background.
    DisposableEffect(Unit) {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)
        val previous = controller.isAppearanceLightStatusBars
        controller.isAppearanceLightStatusBars = false
        onDispose { controller.isAppearanceLightStatusBars = previous }
    }

    var hasCamera by remember { mutableStateOf(isGranted(context, Manifest.permission.CAMERA)) }
    var hasLocation by remember { mutableStateOf(hasLocationPermission(context)) }
    var location by remember { mutableStateOf<Location?>(null) }
    var address by remember { mutableStateOf<String?>(null) }
    var severity by remember { mutableStateOf("Medium") }
    var torchOn by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var capturing by remember { mutableStateOf(false) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasCamera = isGranted(context, Manifest.permission.CAMERA)
        hasLocation = hasLocationPermission(context)
    }

    fun requestMissingPermissions() {
        val missing = mutableListOf<String>()
        if (!hasCamera) missing.add(Manifest.permission.CAMERA)
        if (!hasLocation) {
            missing.add(Manifest.permission.ACCESS_FINE_LOCATION)
            missing.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (missing.isNotEmpty()) permissionLauncher.launch(missing.toTypedArray())
    }

    LaunchedEffect(Unit) { requestMissingPermissions() }
    LaunchedEffect(hasLocation) {
        if (hasLocation) fetchLocation(context) { location = it }
    }
    LaunchedEffect(location) {
        val loc = location
        if (loc != null) address = lookupAddress(context, loc.latitude, loc.longitude)
    }
    LaunchedEffect(torchOn, camera) { camera?.cameraControl?.enableTorch(torchOn) }

    fun proceedWith(file: File) {
        ReportDraft.set(severity, location, address)
        capturing = false
        onCaptured(file.name)
    }

    val pickLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val file = copyToAppStorage(context, uri)
            if (file != null) {
                proceedWith(file)
            } else {
                Toast.makeText(context, "Could not open that photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF312E81))))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScanRoundButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Scan road", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
            ScanRoundButton(onClick = { torchOn = !torchOn }) {
                Icon(
                    painter = painterResource(R.drawable.ic_flash),
                    contentDescription = "Flash",
                    tint = if (torchOn) Coral else Color.White
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            if (hasCamera) {
                CameraPreview(
                    imageCapture = imageCapture,
                    onCameraReady = { camera = it },
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .border(2.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                )
                Text(
                    text = "Keep the damage in the frame",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .background(Color(0xB30F172A), RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                )
            } else {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Camera access is needed to scan roads",
                        color = Color.White,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { requestMissingPermissions() },
                        colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = Navy),
                        modifier = Modifier.padding(top = 16.dp)
                    ) { Text("Allow camera", fontWeight = FontWeight.Bold) }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color(0xFFCBD5E1), RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.height(16.dp))
                LocationRow(
                    hasLocation = hasLocation,
                    location = location,
                    address = address,
                    onAllow = { requestMissingPermissions() }
                )
                Spacer(Modifier.height(16.dp))
                Text("How bad is it?", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Muted)
                Spacer(Modifier.height(8.dp))
                ChoiceRow(options = SeverityLevels, selected = severity, onSelect = { severity = it })
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        capturing = true
                        takePhoto(
                            context = context,
                            imageCapture = imageCapture,
                            onSaved = { proceedWith(it) },
                            onFailed = {
                                capturing = false
                                Toast.makeText(context, "Could not take the photo. Try again.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    enabled = hasCamera && !capturing,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Coral,
                        contentColor = Navy,
                        disabledContainerColor = Color(0xFFCBD5E1),
                        disabledContentColor = Muted
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(painterResource(R.drawable.ic_camera), contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (capturing) "Saving…" else "Capture & report",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                TextButton(
                    onClick = {
                        pickLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Choose from gallery", color = CoralDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(
    imageCapture: ImageCapture,
    onCameraReady: (Camera) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner

    // Release the camera when leaving the screen.
    DisposableEffect(Unit) {
        onDispose {
            try {
                ProcessCameraProvider.getInstance(context).get().unbindAll()
            } catch (e: Exception) {
                Log.w(TAG, "Could not release the camera", e)
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val providerFuture = ProcessCameraProvider.getInstance(ctx)
            providerFuture.addListener({
                val provider = providerFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                try {
                    provider.unbindAll()
                    val bound = provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                    )
                    onCameraReady(bound)
                } catch (e: Exception) {
                    Log.e(TAG, "Camera binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }
    )
}

@Composable
private fun LocationRow(
    hasLocation: Boolean,
    location: Location?,
    address: String?,
    onAllow: () -> Unit
) {
    val title: String
    val subtitle: String
    val subtitleColor: Color
    when {
        !hasLocation -> {
            title = "Location is off"
            subtitle = "Tap to allow location access"
            subtitleColor = RedFg
        }
        location == null -> {
            title = "Finding your location…"
            subtitle = "Searching for GPS signal"
            subtitleColor = Muted
        }
        else -> {
            title = address ?: "%.5f, %.5f".format(location.latitude, location.longitude)
            subtitle = "● Location locked · ±${location.accuracy.toInt()} m"
            subtitleColor = GreenFg
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !hasLocation, onClick = onAllow),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(CoralTint, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Place, contentDescription = null, tint = CoralDark)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Navy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(subtitle, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = subtitleColor)
        }
    }
}

@Composable
private fun ScanRoundButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.16f))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

private fun isGranted(context: Context, permission: String): Boolean =
    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

private fun hasLocationPermission(context: Context): Boolean =
    isGranted(context, Manifest.permission.ACCESS_FINE_LOCATION) ||
            isGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION)

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onSaved: (File) -> Unit,
    onFailed: () -> Unit
) {
    val dir = File(context.filesDir, "reports").apply { mkdirs() }
    val file = File(dir, "report_${System.currentTimeMillis()}.jpg")
    val options = ImageCapture.OutputFileOptions.Builder(file).build()
    imageCapture.takePicture(
        options,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onSaved(file)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed", exception)
                onFailed()
            }
        }
    )
}

private fun copyToAppStorage(context: Context, uri: Uri): File? {
    return try {
        val dir = File(context.filesDir, "reports").apply { mkdirs() }
        val file = File(dir, "report_${System.currentTimeMillis()}.jpg")
        val input = context.contentResolver.openInputStream(uri) ?: return null
        input.use { source -> file.outputStream().use { target -> source.copyTo(target) } }
        file
    } catch (e: Exception) {
        null
    }
}