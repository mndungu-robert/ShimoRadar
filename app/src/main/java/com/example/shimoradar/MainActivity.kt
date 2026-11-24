package com.example.shimoradar

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.shimoradar.ui.theme.ShimoradarTheme
import androidx.lifecycle.lifecycleScope
import com.example.shimoradar.data.Detector
import com.example.shimoradar.data.YoloDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Load the AI model in the background
        lifecycleScope.launch {
            try {
                val detector = withContext(Dispatchers.Default) {
                    YoloDetector(applicationContext)
                }

                if (detector.isReady) {
                    Detector.current = detector
                    Log.d("ShimoRadar", "AI detector initialized successfully")
                } else {
                    Log.e("ShimoRadar", "AI detector is not ready")
                }

            } catch (e: Exception) {
                Log.e("ShimoRadar", "AI initialization failed", e)
            }
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent {
            ShimoradarTheme {
                SmartRoadApp()
            }
        }
    }
}