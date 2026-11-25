package com.example.shimoradar.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shimoradar.R
import kotlinx.coroutines.delay

private val SplashNavy = Color(0xFF0F172A)
private val SplashCoral = Color(0xFFFF6B35)

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(700)
        )

        delay(300)
        onFinished()
    }

    val pulses = rememberInfiniteTransition(
        label = "radar"
    )

    val pulseA by pulses.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing)
        ),
        label = "pulseA"
    )

    val pulseB by pulses.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            initialStartOffset = StartOffset(1200)
        ),
        label = "pulseB"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SplashNavy)
    ) {
        // Animated radar waves
        Canvas(modifier = Modifier.fillMaxSize()) {
            val logoPx = 288.dp.toPx()

            val centre = Offset(
                x = size.width / 2f,
                y = size.height / 2f +
                    (46f - 54f) / 108f * logoPx
            )

            val minRadius = 30.dp.toPx()
            val maxRadius = 150.dp.toPx()

            listOf(pulseA, pulseB).forEach { p ->
                if (p > 0f) {
                    val alpha = (1f - p) *
                        0.4f * minOf(1f, p * 8f)

                    drawCircle(
                        color = SplashCoral.copy(alpha = alpha),
                        radius = minRadius +
                            (maxRadius - minRadius) * p,
                        center = centre,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }

        // ShimoRadar logo
        Image(
            painter = painterResource(
                R.drawable.ic_launcher_foreground
            ),
            contentDescription = "ShimoRadar logo",
            modifier = Modifier
                .align(Alignment.Center)
                .size(288.dp)
        )

        // Application name and tagline
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 130.dp)
                .graphicsLayer {
                    alpha = textAlpha.value
                    translationY =
                        (1f - textAlpha.value) * 16.dp.toPx()
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.White)) {
                        append("Shimo")
                    }

                    withStyle(SpanStyle(color = SplashCoral)) {
                        append("Radar")
                    }
                },
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Detect. Report. Improve.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                color = Color(0xFFCBD5E1),
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
