package com.ekoehler.expressivecutout.overlay

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.ekoehler.expressivecutout.data.BehaviourSettings

/** Draws a camera-centered notification light without adding a touch target. */
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
        else -> accent
    }
    Canvas(modifier.size(settings.cameraRingDiameter.dp)) {
        val width = settings.cameraRingThickness.dp.toPx()
        val radius = ((size.minDimension - width) / 2f).coerceAtLeast(0f)
        val pulse = if (settings.cameraRingMode == 1) 0.3f + 0.7f *
            ((kotlin.math.sin(phase * 2f * Math.PI).toFloat() + 1f) / 2f) else 1f
        val ink = color.copy(alpha = settings.cameraRingBrightness / 100f * pulse)
        if (settings.cameraRingMode == 2) {
            drawCircle(ink.copy(alpha = ink.alpha * 0.2f), radius, style = Stroke(width))
            drawArc(ink, phase * 360f - 90f, 100f, false,
                topLeft = Offset(width / 2f, width / 2f),
                size = Size(radius * 2f, radius * 2f), style = Stroke(width, cap = StrokeCap.Round))
        } else {
            drawCircle(ink, radius, style = Stroke(width))
        }
    }
}
