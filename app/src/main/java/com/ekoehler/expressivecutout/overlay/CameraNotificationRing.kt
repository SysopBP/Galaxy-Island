package com.ekoehler.expressivecutout.overlay

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.ekoehler.expressivecutout.data.BehaviourSettings

/** Draws a camera-centered alert light, including a dark frosted track and rotating silver sheen. */
@Composable
internal fun CameraNotificationRing(settings: BehaviourSettings, accent: Color, modifier: Modifier = Modifier) {
    val phase = if (settings.cameraRingMode == 0) 0f else {
        val transition = rememberInfiniteTransition(label = "cameraRing")
        val value by transition.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Restart),
            label = "cameraRingPhase",
        )
        value
    }
    val color = when (settings.cameraRingColor) {
        1 -> Color(0xFF64B5F6)
        2 -> Color(0xFF69F0AE)
        3 -> Color(0xFFFF80AB)
        4 -> Color(0xFFFFD54F)
        5 -> Color(0xFFB34462)
        6 -> Color(0xFFB388FF)
        7 -> Color(0xFF62E7F0)
        8 -> Color(0xFFFFAB65)
        9 -> Color(0xFFF5F7FA)
        10 -> Color(0xFFB8C3D2)
        11 -> Color(0xFF848F9E)
        else -> accent
    }
    Canvas(modifier.offset(x = settings.cameraRingOffsetX.dp, y = settings.cameraRingOffsetY.dp).size(settings.cameraRingDiameter.dp)) {
        val width = settings.cameraRingThickness.dp.toPx()
        val radius = ((size.minDimension - width) / 2f).coerceAtLeast(0f)
        val pulse = if (settings.cameraRingMode == 1) 0.3f + 0.7f *
            ((kotlin.math.sin(phase * 2f * Math.PI).toFloat() + 1f) / 2f) else 1f
        val brightness = settings.cameraRingBrightness / 100f
        val ink = color.copy(alpha = brightness * pulse)
        if (settings.cameraRingMode == 3) {
            val sheen = if (settings.cameraRingColor == 11) Color(0xFFDBE3EE) else color
            drawCircle(Color(0xFF14181E).copy(alpha = brightness * 0.9f), radius, style = Stroke(width))
            drawCircle(Color.White.copy(alpha = brightness * 0.18f), radius,
                style = Stroke(0.65.dp.toPx()))
            rotate(phase * 360f - 90f) {
                drawCircle(
                    brush = Brush.sweepGradient(listOf(
                        sheen.copy(alpha = 0f), sheen.copy(alpha = brightness * 0.12f),
                        sheen.copy(alpha = brightness * 0.75f),
                        Color.White.copy(alpha = brightness * 0.95f), sheen.copy(alpha = 0f),
                    )),
                    radius = radius,
                    style = Stroke(width),
                )
            }
        } else if (settings.cameraRingMode == 2) {
            drawCircle(ink.copy(alpha = ink.alpha * 0.2f), radius, style = Stroke(width))
            drawArc(ink, phase * 360f - 90f, 100f, false,
                topLeft = Offset(width / 2f, width / 2f),
                size = Size(radius * 2f, radius * 2f), style = Stroke(width, cap = StrokeCap.Round))
        } else {
            drawCircle(ink, radius, style = Stroke(width))
        }
    }
}
