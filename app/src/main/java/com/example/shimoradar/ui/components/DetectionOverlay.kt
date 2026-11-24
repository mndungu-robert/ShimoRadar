package com.example.shimoradar.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shimoradar.data.ClassInfo
import com.example.shimoradar.data.DetectedBox
import com.example.shimoradar.ui.theme.Coral
import com.example.shimoradar.ui.theme.Navy
import kotlin.math.max

/**
 * Draws detection boxes over an image that fills this area with "centre crop" scaling.
 * [imageWidth] and [imageHeight] are the size of the image the boxes were measured on.
 */
@Composable
fun DetectionOverlay(
    boxes: List<DetectedBox>,
    imageWidth: Int,
    imageHeight: Int,
    modifier: Modifier = Modifier
) {
    val measurer = rememberTextMeasurer()
    Canvas(modifier = modifier) {
        if (imageWidth <= 0 || imageHeight <= 0) return@Canvas
        val scale = max(size.width / imageWidth, size.height / imageHeight)
        val dx = (size.width - imageWidth * scale) / 2f
        val dy = (size.height - imageHeight * scale) / 2f

        boxes.forEach { box ->
            val left = box.rect.left * imageWidth * scale + dx
            val top = box.rect.top * imageHeight * scale + dy
            val right = box.rect.right * imageWidth * scale + dx
            val bottom = box.rect.bottom * imageHeight * scale + dy

            drawRoundRect(
                color = Coral,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                cornerRadius = CornerRadius(10.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )

            val text = "${ClassInfo.shortLabel(box.classId)} ${(box.score * 100).toInt()}%"
            val layout = measurer.measure(
                text,
                TextStyle(color = Navy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            )
            val labelTop = max(0f, top - layout.size.height - 6.dp.toPx())
            drawRoundRect(
                color = Coral,
                topLeft = Offset(left, labelTop),
                size = Size(layout.size.width + 16.dp.toPx(), layout.size.height + 6.dp.toPx()),
                cornerRadius = CornerRadius(6.dp.toPx())
            )
            drawText(layout, topLeft = Offset(left + 8.dp.toPx(), labelTop + 3.dp.toPx()))
        }
    }
}