package com.example.shimoradar.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColors = lightColorScheme(
    primary = Coral,
    onPrimary = Navy,
    background = Sand,
    onBackground = Navy,
    surface = CardWhite,
    onSurface = Navy,
    onSurfaceVariant = Muted
)

@Composable
fun ShimoradarTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColors,
        typography = Typography,
        content = content
    )
}