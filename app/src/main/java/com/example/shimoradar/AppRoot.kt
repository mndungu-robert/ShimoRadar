package com.example.shimoradar

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.shimoradar.ui.screens.SplashScreen

@Composable
fun AppRoot() {
    var showSplash by rememberSaveable {
        mutableStateOf(true)
    }

    Crossfade(
        targetState = showSplash,
        animationSpec = tween(350),
        label = "splash"
    ) { splash ->
        if (splash) {
            SplashScreen(
                onFinished = {
                    showSplash = false
                }
            )
        } else {
            SmartRoadApp()
        }
    }
}
