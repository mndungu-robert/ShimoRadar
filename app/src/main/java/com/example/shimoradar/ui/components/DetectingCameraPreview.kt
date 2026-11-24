package com.example.shimoradar.ui.components


import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.shimoradar.data.DetectedBox
import com.example.shimoradar.data.Detector
import java.util.concurrent.Executors

private const val TAG = "ShimoRadar"

@Composable
fun DetectingCameraPreview(
    imageCapture: ImageCapture,
    onCameraReady: (Camera) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner

    var boxes by remember { mutableStateOf<List<DetectedBox>>(emptyList()) }
    var frameSize by remember { mutableStateOf(IntSize(1, 1)) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val analysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
    }

    DisposableEffect(analysis) {
        var lastRun = 0L
        analysis.setAnalyzer(executor) { image ->
            val detector = Detector.yolo
            val now = SystemClock.elapsedRealtime()
            if (detector == null || now - lastRun < 350L) {
                image.close()
                return@setAnalyzer
            }
            lastRun = now
            try {
                val rotation = image.imageInfo.rotationDegrees
                val raw = image.toBitmap()
                val upright = if (rotation == 0) {
                    raw
                } else {
                    Bitmap.createBitmap(
                        raw, 0, 0, raw.width, raw.height,
                        Matrix().apply { postRotate(rotation.toFloat()) }, true
                    )
                }
                val found = detector.detectBoxes(upright)
                frameSize = IntSize(upright.width, upright.height)
                boxes = found
            } catch (e: Exception) {
                Log.e(TAG, "Live detection failed", e)
            } finally {
                image.close()
            }
        }
        onDispose {
            analysis.clearAnalyzer()
            executor.shutdown()
        }
    }

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

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
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
                            imageCapture,
                            analysis
                        )
                        onCameraReady(bound)
                    } catch (e: Exception) {
                        Log.e(TAG, "Camera binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            }
        )

        DetectionOverlay(
            boxes = boxes,
            imageWidth = frameSize.width,
            imageHeight = frameSize.height,
            modifier = Modifier.fillMaxSize()
        )

        if (Detector.yolo == null) {
            Text(
                text = "Loading AI model…",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .background(Color(0xB30F172A), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}