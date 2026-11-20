package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.StreakAmber
import com.example.ui.theme.StreakOrange
import com.example.ui.theme.VividViolet
import kotlin.math.PI
import kotlin.math.sin

/**
 * Flickering multi-layered Streak Flame icon driven by smooth continuous phase animation.
 */
@Composable
fun AnimatedStreakFlame(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    contentDescriptionText: String? = null
) {
    val transition = rememberInfiniteTransition(label = "streak_flame")
    val flickerPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1250, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flame_phase"
    )
    val pulseScale by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 680, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    Canvas(
        modifier = modifier
            .size(size)
            .semantics {
                if (contentDescriptionText != null) {
                    contentDescription = contentDescriptionText
                }
            }
    ) {
        val w = this.size.width
        val h = this.size.height
        val tipSway = sin(flickerPhase) * w * 0.06f
        val innerSway = sin(flickerPhase * 1.6f + 0.8f) * w * 0.045f

        scale(scaleX = 1f, scaleY = pulseScale, pivot = Offset(w * 0.5f, h * 0.9f)) {
            // Soft outer glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(StreakOrange.copy(alpha = 0.32f), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.58f),
                    radius = w * 0.52f
                ),
                radius = w * 0.52f,
                center = Offset(w * 0.5f, h * 0.58f)
            )

            // Outer flame silhouette
            val outerPath = Path().apply {
                moveTo(w * 0.5f, h * 0.92f)
                cubicTo(w * 0.16f, h * 0.88f, w * 0.12f, h * 0.54f, w * 0.30f, h * 0.34f)
                cubicTo(w * 0.34f, h * 0.44f, w * 0.39f, h * 0.36f, w * 0.50f + tipSway, h * 0.08f)
                cubicTo(w * 0.66f, h * 0.28f, w * 0.88f, h * 0.48f, w * 0.80f, h * 0.76f)
                cubicTo(w * 0.74f, h * 0.88f, w * 0.62f, h * 0.92f, w * 0.5f, h * 0.92f)
                close()
            }
            drawPath(
                path = outerPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFB7185), StreakOrange, Color(0xFFEA580C))
                )
            )

            // Inner golden flame
            val innerPath = Path().apply {
                moveTo(w * 0.5f, h * 0.87f)
                cubicTo(w * 0.30f, h * 0.84f, w * 0.30f, h * 0.60f, w * 0.50f + innerSway, h * 0.32f)
                cubicTo(w * 0.70f, h * 0.58f, w * 0.70f, h * 0.84f, w * 0.5f, h * 0.87f)
                close()
            }
            drawPath(
                path = innerPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFEF08A), StreakAmber, StreakOrange)
                )
            )

            // White-hot core droplet
            drawOval(
                color = Color(0xFFFFFBEB),
                topLeft = Offset(w * 0.41f, h * 0.62f),
                size = Size(w * 0.18f, h * 0.21f)
            )
        }
    }
}

/**
 * Gently ringing Reminder Bell icon with swinging body, sound arcs, and optional notification dot.
 */
@Composable
fun AnimatedReminderBell(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Color.White,
    isActive: Boolean = true,
    contentDescriptionText: String? = null
) {
    val transition = rememberInfiniteTransition(label = "reminder_bell")
    val ringPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bell_phase"
    )

    // Periodic gentle ring burst during first 35% of cycle, calm pause afterward
    val swingAngle = if (isActive && ringPhase < 0.38f) {
        val local = ringPhase / 0.38f
        val damping = 1f - local
        sin(local * 4f * PI).toFloat() * 14f * damping
    } else {
        0f
    }
    val waveAlpha = if (isActive && ringPhase < 0.38f) {
        (sin((ringPhase / 0.38f) * PI).toFloat()).coerceIn(0f, 1f) * 0.85f
    } else {
        0f
    }

    Canvas(
        modifier = modifier
            .size(size)
            .semantics {
                if (contentDescriptionText != null) {
                    contentDescription = contentDescriptionText
                }
            }
    ) {
        val w = this.size.width
        val h = this.size.height

        // Sound wave arcs on left & right when ringing
        if (waveAlpha > 0.05f) {
            val arcStroke = Stroke(width = w * 0.065f, cap = StrokeCap.Round)
            drawArc(
                color = CyanGlow.copy(alpha = waveAlpha),
                startAngle = 148f,
                sweepAngle = 58f,
                useCenter = false,
                topLeft = Offset(w * 0.05f, h * 0.18f),
                size = Size(w * 0.42f, h * 0.48f),
                style = arcStroke
            )
            drawArc(
                color = CyanGlow.copy(alpha = waveAlpha),
                startAngle = -26f,
                sweepAngle = 58f,
                useCenter = false,
                topLeft = Offset(w * 0.53f, h * 0.18f),
                size = Size(w * 0.42f, h * 0.48f),
                style = arcStroke
            )
        }

        rotate(degrees = swingAngle, pivot = Offset(w * 0.5f, h * 0.16f)) {
            // Top loop
            drawCircle(
                color = tint,
                radius = w * 0.075f,
                center = Offset(w * 0.5f, h * 0.16f),
                style = Stroke(width = w * 0.065f)
            )

            // Bell dome
            val bellPath = Path().apply {
                moveTo(w * 0.5f, h * 0.22f)
                cubicTo(w * 0.28f, h * 0.22f, w * 0.25f, h * 0.46f, w * 0.22f, h * 0.65f)
                lineTo(w * 0.16f, h * 0.74f)
                lineTo(w * 0.84f, h * 0.74f)
                lineTo(w * 0.78f, h * 0.65f)
                cubicTo(w * 0.75f, h * 0.46f, w * 0.72f, h * 0.22f, w * 0.5f, h * 0.22f)
                close()
            }
            drawPath(
                path = bellPath,
                color = tint.copy(alpha = if (isActive) 0.96f else 0.5f),
                style = Stroke(width = w * 0.075f, join = StrokeJoin.Round, cap = StrokeCap.Round)
            )

            // Clapper
            drawArc(
                color = tint.copy(alpha = if (isActive) 0.96f else 0.5f),
                startAngle = 15f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(w * 0.40f, h * 0.71f),
                size = Size(w * 0.20f, h * 0.15f),
                style = Stroke(width = w * 0.07f, cap = StrokeCap.Round)
            )
        }

        // Active status badge dot
        if (isActive) {
            drawCircle(
                color = Color.White,
                radius = w * 0.135f,
                center = Offset(w * 0.76f, h * 0.24f)
            )
            drawCircle(
                color = RoseAlert,
                radius = w * 0.095f,
                center = Offset(w * 0.76f, h * 0.24f)
            )
        }
    }
}

/**
 * Floating Graduation Cap & Open Book vector icon with bobbing motion and twinkling stars.
 */
@Composable
fun AnimatedFloatingGradCap(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    contentDescriptionText: String? = null
) {
    val transition = rememberInfiniteTransition(label = "floating_grad_cap")
    val floatOffset by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cap_float"
    )
    val twinkle by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cap_twinkle"
    )

    Canvas(
        modifier = modifier
            .size(size)
            .semantics {
                if (contentDescriptionText != null) {
                    contentDescription = contentDescriptionText
                }
            }
    ) {
        val w = this.size.width
        val h = this.size.height
        val dy = floatOffset * h * 0.055f

        // Ground shadow reacts inversely to levitation height
        val shadowScale = 1f - (floatOffset * 0.08f)
        drawOval(
            color = ElectricIndigo.copy(alpha = 0.14f),
            topLeft = Offset(w * (0.5f - 0.28f * shadowScale), h * 0.87f),
            size = Size(w * 0.56f * shadowScale, h * 0.08f)
        )

        translate(top = dy) {
            // Open Book base
            val leftPage = Path().apply {
                moveTo(w * 0.50f, h * 0.62f)
                quadraticTo(w * 0.32f, h * 0.56f, w * 0.18f, h * 0.63f)
                lineTo(w * 0.18f, h * 0.79f)
                quadraticTo(w * 0.34f, h * 0.73f, w * 0.50f, h * 0.79f)
                close()
            }
            val rightPage = Path().apply {
                moveTo(w * 0.50f, h * 0.62f)
                quadraticTo(w * 0.68f, h * 0.56f, w * 0.82f, h * 0.63f)
                lineTo(w * 0.82f, h * 0.79f)
                quadraticTo(w * 0.66f, h * 0.73f, w * 0.50f, h * 0.79f)
                close()
            }
            drawPath(leftPage, color = Color(0xFFE0E7FF))
            drawPath(rightPage, color = Color(0xFFCFFAFE))
            drawPath(
                leftPage,
                color = ElectricIndigo,
                style = Stroke(width = w * 0.032f, join = StrokeJoin.Round)
            )
            drawPath(
                rightPage,
                color = ElectricIndigo,
                style = Stroke(width = w * 0.032f, join = StrokeJoin.Round)
            )

            // Graduation Cap mortarboard diamond
            val capBoard = Path().apply {
                moveTo(w * 0.50f, h * 0.16f)
                lineTo(w * 0.86f, h * 0.34f)
                lineTo(w * 0.50f, h * 0.51f)
                lineTo(w * 0.14f, h * 0.34f)
                close()
            }
            drawPath(
                path = capBoard,
                brush = Brush.linearGradient(
                    colors = listOf(ElectricIndigo, VividViolet, CyanGlow),
                    start = Offset(w * 0.15f, h * 0.18f),
                    end = Offset(w * 0.85f, h * 0.50f)
                )
            )

            // Cap skull dome underneath
            val capBase = Path().apply {
                moveTo(w * 0.31f, h * 0.42f)
                lineTo(w * 0.31f, h * 0.54f)
                quadraticTo(w * 0.50f, h * 0.62f, w * 0.69f, h * 0.54f)
                lineTo(w * 0.69f, h * 0.42f)
                lineTo(w * 0.50f, h * 0.51f)
                close()
            }
            drawPath(path = capBase, color = ElectricIndigo)

            // Tassel cord & bead
            drawLine(
                color = StreakAmber,
                start = Offset(w * 0.50f, h * 0.33f),
                end = Offset(w * 0.79f, h * 0.42f),
                strokeWidth = w * 0.035f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = StreakAmber,
                start = Offset(w * 0.79f, h * 0.42f),
                end = Offset(w * 0.79f, h * 0.57f),
                strokeWidth = w * 0.035f,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = StreakOrange,
                radius = w * 0.045f,
                center = Offset(w * 0.79f, h * 0.58f)
            )
        }

        // Twinkling 4-point sparkles
        val sparkleSize = w * 0.065f * twinkle
        drawSparkle(Offset(w * 0.16f, h * 0.20f), sparkleSize, CyanGlow.copy(alpha = twinkle))
        drawSparkle(Offset(w * 0.86f, h * 0.18f), sparkleSize * 0.85f, StreakAmber.copy(alpha = twinkle))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSparkle(
    center: Offset,
    radius: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x, center.y, center.x + radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + radius)
        quadraticTo(center.x, center.y, center.x - radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - radius)
        close()
    }
    drawPath(path, color = color)
}

/**
 * Rhythmic 5-bar Audio Waveform visualizer using staggered harmonic sine phases.
 */
@Composable
fun AnimatedAudioWaveform(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    barCount: Int = 5,
    primaryColor: Color = CyanGlow,
    secondaryColor: Color = VividViolet,
    width: Dp = 36.dp,
    height: Dp = 18.dp,
    contentDescriptionText: String? = null
) {
    val transition = rememberInfiniteTransition(label = "audio_waveform")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1050, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Canvas(
        modifier = modifier
            .size(width = width, height = height)
            .semantics {
                if (contentDescriptionText != null) {
                    contentDescription = contentDescriptionText
                }
            }
    ) {
        val totalW = this.size.width
        val totalH = this.size.height
        val barWidth = totalW / (barCount * 2f - 1f)
        val brush = Brush.horizontalGradient(listOf(primaryColor, secondaryColor))

        for (i in 0 until barCount) {
            val wave = if (isActive) {
                val harmonic = (sin(phase + i * 0.95f) + 1f) * 0.5f
                0.24f + harmonic * 0.72f
            } else {
                0.24f
            }
            val barH = totalH * wave
            val x = i * barWidth * 2f
            val y = (totalH - barH) * 0.5f

            drawRoundRect(
                brush = brush,
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
