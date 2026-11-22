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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.StreakMasteryLevel
import com.example.data.StreakVisualStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Evolving Animated Mastery Streak Icon rendered in Compose Canvas.
 * Supports two visual styles:
 * - StreakVisualStyle.TREE (Cái cây — Growth Tree)
 * - StreakVisualStyle.FLAME (Ngọn lửa — Mastery Flame)
 *
 * Visually evolves across all 6 Mastery Levels:
 * 0 days (Khởi đầu), 1–6 days (Starter), 7–14 days (Consistent),
 * 15–29 days (Advanced 7.5), 30–59 days (Expert 8.0), 60+ days (IELTS Master 8.5).
 */
@Composable
fun MasteryStreakIcon(
    streakDays: Int,
    style: StreakVisualStyle = StreakVisualStyle.TREE,
    size: Dp = 24.dp,
    contentDescriptionText: String? = null,
    modifier: Modifier = Modifier
) {
    val level = StreakMasteryLevel.fromStreak(streakDays)
    val transition = rememberInfiniteTransition(label = "mastery_streak_icon")

    val sway by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (level.stageIndex >= 4) 1100 else 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "streak_sway"
    )

    val pulse by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "streak_pulse"
    )

    val orbitAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "streak_orbit"
    )

    val semanticsMod = if (contentDescriptionText != null) {
        modifier.semantics { contentDescription = contentDescriptionText }
    } else {
        modifier
    }

    Canvas(modifier = semanticsMod.size(size)) {
        when (style) {
            StreakVisualStyle.TREE -> drawEvolvingTree(level, sway, pulse, orbitAngle)
            StreakVisualStyle.FLAME -> drawEvolvingFlame(level, sway, pulse, orbitAngle)
        }
    }
}

private fun DrawScope.drawEvolvingTree(
    level: StreakMasteryLevel,
    sway: Float,
    pulse: Float,
    orbitAngle: Float
) {
    val w = size.width
    val h = size.height
    val cx = w * 0.5f
    val groundY = h * 0.86f
    val swayPx = sway * w * 0.032f

    // Ground mound
    val soilColor = when (level) {
        StreakMasteryLevel.SEED -> Color(0xFF94A3B8)
        StreakMasteryLevel.MASTER -> Color(0xFFB45309)
        else -> Color(0xFF854D0E)
    }
    drawRoundRect(
        color = soilColor.copy(alpha = 0.72f),
        topLeft = Offset(w * 0.20f, groundY),
        size = Size(w * 0.60f, h * 0.11f),
        cornerRadius = CornerRadius(h * 0.06f, h * 0.06f)
    )

    when (level) {
        // 0 days: Dormant Seed (muted gray)
        StreakMasteryLevel.SEED -> {
            drawCircle(
                color = Color(0xFFCBD5E1).copy(alpha = 0.28f * pulse),
                radius = w * 0.24f,
                center = Offset(cx, h * 0.68f)
            )
            val seedPath = Path().apply {
                moveTo(cx, h * 0.50f)
                cubicTo(w * 0.68f, h * 0.60f, w * 0.66f, h * 0.84f, cx, h * 0.85f)
                cubicTo(w * 0.34f, h * 0.84f, w * 0.32f, h * 0.60f, cx, h * 0.50f)
                close()
            }
            drawPath(
                path = seedPath,
                brush = Brush.verticalGradient(listOf(Color(0xFF94A3B8), Color(0xFF64748B)))
            )
            // Subtle seed crack highlight
            drawLine(
                color = Color(0xFFE2E8F0),
                start = Offset(cx, h * 0.58f),
                end = Offset(cx + w * 0.04f, h * 0.72f),
                strokeWidth = (w * 0.045f).coerceAtLeast(1.2f),
                cap = StrokeCap.Round
            )
        }

        // 1–6 days: Fresh Green Sprout
        StreakMasteryLevel.STARTER -> {
            val stemTop = Offset(cx + swayPx * 0.8f, h * 0.44f)
            drawLine(
                color = Color(0xFF16A34A),
                start = Offset(cx, groundY),
                end = stemTop,
                strokeWidth = (w * 0.085f).coerceAtLeast(2f),
                cap = StrokeCap.Round
            )
            // Left tender leaf
            val leftLeaf = Path().apply {
                moveTo(stemTop.x, stemTop.y + h * 0.06f)
                quadraticTo(w * 0.16f + swayPx, h * 0.32f, w * 0.24f + swayPx, h * 0.22f)
                quadraticTo(w * 0.44f + swayPx, h * 0.24f, stemTop.x, stemTop.y + h * 0.06f)
                close()
            }
            drawPath(leftLeaf, Brush.linearGradient(listOf(Color(0xFF4ADE80), Color(0xFF16A34A))))
            // Right tender leaf
            val rightLeaf = Path().apply {
                moveTo(stemTop.x, stemTop.y + h * 0.03f)
                quadraticTo(w * 0.84f + swayPx, h * 0.26f, w * 0.76f + swayPx, h * 0.18f)
                quadraticTo(w * 0.56f + swayPx, h * 0.20f, stemTop.x, stemTop.y + h * 0.03f)
                close()
            }
            drawPath(rightLeaf, Brush.linearGradient(listOf(Color(0xFF86EFAC), Color(0xFF22C55E))))
        }

        // 7–14 days: Leafy Sapling
        StreakMasteryLevel.CONSISTENT -> {
            drawLine(
                color = Color(0xFF92400E),
                start = Offset(cx, groundY),
                end = Offset(cx + swayPx * 0.5f, h * 0.40f),
                strokeWidth = (w * 0.10f).coerceAtLeast(2.4f),
                cap = StrokeCap.Round
            )
            // Side twig
            drawLine(
                color = Color(0xFF92400E),
                start = Offset(cx + swayPx * 0.25f, h * 0.60f),
                end = Offset(cx + w * 0.16f + swayPx * 0.7f, h * 0.48f),
                strokeWidth = (w * 0.06f).coerceAtLeast(1.5f),
                cap = StrokeCap.Round
            )
            // Leafy clusters
            drawCircle(Color(0xFF059669), radius = w * 0.20f, center = Offset(cx - w * 0.14f + swayPx, h * 0.42f))
            drawCircle(Color(0xFF10B981), radius = w * 0.21f, center = Offset(cx + w * 0.15f + swayPx, h * 0.40f))
            drawCircle(
                brush = Brush.verticalGradient(listOf(Color(0xFF6EE7B7), Color(0xFF10B981))),
                radius = w * 0.22f,
                center = Offset(cx + swayPx, h * 0.27f)
            )
        }

        // 15–29 days: Full Mature Tree
        StreakMasteryLevel.ADVANCED -> {
            // Subtle sky aura
            drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = 0.22f * pulse),
                radius = w * 0.42f,
                center = Offset(cx, h * 0.36f)
            )
            // Strong trunk
            val trunk = Path().apply {
                moveTo(cx - w * 0.10f, groundY)
                lineTo(cx - w * 0.055f + swayPx * 0.3f, h * 0.46f)
                lineTo(cx + w * 0.055f + swayPx * 0.3f, h * 0.46f)
                lineTo(cx + w * 0.10f, groundY)
                close()
            }
            drawPath(trunk, Color(0xFF78350F))

            // Lush layered canopy
            drawCircle(Color(0xFF047857), radius = w * 0.24f, center = Offset(cx - w * 0.17f + swayPx * 0.8f, h * 0.42f))
            drawCircle(Color(0xFF047857), radius = w * 0.24f, center = Offset(cx + w * 0.17f + swayPx * 0.8f, h * 0.42f))
            drawCircle(Color(0xFF0D9488), radius = w * 0.25f, center = Offset(cx + swayPx, h * 0.33f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF67E8F9), Color(0xFF10B981)),
                    center = Offset(cx - w * 0.06f + swayPx, h * 0.20f),
                    radius = w * 0.24f
                ),
                radius = w * 0.21f,
                center = Offset(cx + swayPx, h * 0.24f)
            )
        }

        // 30–59 days: Blooming Glowing Tree
        StreakMasteryLevel.EXPERT -> {
            // Radiant violet/pink aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFC084FC).copy(alpha = 0.48f * pulse), Color.Transparent),
                    center = Offset(cx, h * 0.36f),
                    radius = w * 0.48f
                ),
                radius = w * 0.48f,
                center = Offset(cx, h * 0.36f)
            )
            // Sturdy trunk
            val trunk = Path().apply {
                moveTo(cx - w * 0.11f, groundY)
                lineTo(cx - w * 0.06f + swayPx * 0.3f, h * 0.46f)
                lineTo(cx + w * 0.06f + swayPx * 0.3f, h * 0.46f)
                lineTo(cx + w * 0.11f, groundY)
                close()
            }
            drawPath(trunk, Color(0xFF6B21A8).copy(alpha = 0.88f))

            // Glowing blossom canopy
            drawCircle(Color(0xFF7C3AED), radius = w * 0.24f, center = Offset(cx - w * 0.18f + swayPx * 0.8f, h * 0.41f))
            drawCircle(Color(0xFF8B5CF6), radius = w * 0.24f, center = Offset(cx + w * 0.18f + swayPx * 0.8f, h * 0.41f))
            drawCircle(
                brush = Brush.verticalGradient(listOf(Color(0xFFF472B6), Color(0xFF9333EA))),
                radius = w * 0.27f,
                center = Offset(cx + swayPx, h * 0.30f)
            )
            // Luminous blossoms
            val blossomCenters = listOf(
                Offset(cx - w * 0.14f + swayPx, h * 0.34f),
                Offset(cx + w * 0.14f + swayPx, h * 0.30f),
                Offset(cx + swayPx, h * 0.20f),
                Offset(cx + w * 0.04f + swayPx, h * 0.42f)
            )
            blossomCenters.forEach { pos ->
                drawCircle(Color(0xFFFDE047), radius = w * 0.052f * pulse, center = pos)
                drawCircle(Color.White, radius = w * 0.022f, center = pos)
            }
        }

        // 60+ days: Golden Ancient Tree with animated aura particles
        StreakMasteryLevel.MASTER -> {
            // Golden pulsing halo ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFDE047).copy(alpha = 0.55f * pulse), Color.Transparent),
                    center = Offset(cx, h * 0.38f),
                    radius = w * 0.50f
                ),
                radius = w * 0.50f,
                center = Offset(cx, h * 0.38f)
            )
            drawCircle(
                color = Color(0xFFFBBF24).copy(alpha = 0.50f),
                radius = w * 0.42f * pulse.coerceAtMost(1.04f),
                center = Offset(cx, h * 0.38f),
                style = Stroke(width = (w * 0.032f).coerceAtLeast(1f))
            )

            // Grand golden-brown ancient trunk
            val trunk = Path().apply {
                moveTo(cx - w * 0.14f, groundY)
                quadraticTo(cx - w * 0.06f, h * 0.65f, cx - w * 0.065f + swayPx * 0.2f, h * 0.45f)
                lineTo(cx + w * 0.065f + swayPx * 0.2f, h * 0.45f)
                quadraticTo(cx + w * 0.06f, h * 0.65f, cx + w * 0.14f, groundY)
                close()
            }
            drawPath(trunk, Brush.verticalGradient(listOf(Color(0xFFB45309), Color(0xFF78350F))))

            // Golden Ancient Crown
            drawCircle(Color(0xFFD97706), radius = w * 0.24f, center = Offset(cx - w * 0.19f + swayPx * 0.7f, h * 0.40f))
            drawCircle(Color(0xFFD97706), radius = w * 0.24f, center = Offset(cx + w * 0.19f + swayPx * 0.7f, h * 0.40f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFEF9C3), Color(0xFFFBBF24), Color(0xFFD97706)),
                    center = Offset(cx + swayPx, h * 0.24f),
                    radius = w * 0.34f
                ),
                radius = w * 0.28f,
                center = Offset(cx + swayPx, h * 0.29f)
            )

            // Animated orbiting golden aura particles
            for (i in 0 until 4) {
                val rad = ((orbitAngle + i * 90f) * PI / 180.0)
                val px = cx + (cos(rad) * w * 0.40f).toFloat()
                val py = h * 0.36f + (sin(rad) * h * 0.28f).toFloat()
                drawCircle(Color(0xFFFEF08A), radius = (w * 0.045f).coerceAtLeast(1.4f), center = Offset(px, py))
            }
        }
    }
}

private fun DrawScope.drawEvolvingFlame(
    level: StreakMasteryLevel,
    sway: Float,
    pulse: Float,
    orbitAngle: Float
) {
    val w = size.width
    val h = size.height
    val cx = w * 0.5f
    val tipDrift = sway * w * 0.05f

    when (level) {
        // 0 days: Dim Spark (muted gray)
        StreakMasteryLevel.SEED -> {
            drawCircle(
                color = Color(0xFFCBD5E1).copy(alpha = 0.28f * pulse),
                radius = w * 0.26f,
                center = Offset(cx, h * 0.62f)
            )
            val spark = Path().apply {
                moveTo(cx, h * 0.38f)
                quadraticTo(w * 0.64f, h * 0.60f, cx, h * 0.84f)
                quadraticTo(w * 0.36f, h * 0.60f, cx, h * 0.38f)
                close()
            }
            drawPath(spark, Brush.verticalGradient(listOf(Color(0xFFCBD5E1), Color(0xFF64748B))))
        }

        // 1–6 days: Small Warm Flame
        StreakMasteryLevel.STARTER -> {
            drawFlameBody(
                cx = cx,
                topY = h * 0.26f,
                bottomY = h * 0.90f,
                halfWidth = w * 0.28f,
                tipDrift = tipDrift,
                outerColors = listOf(Color(0xFFFDE047), Color(0xFFF97316)),
                innerColors = listOf(Color(0xFFFEF9C3), Color(0xFFF59E0B))
            )
        }

        // 7–14 days: Bright Orange-Gold Blazing Flame
        StreakMasteryLevel.CONSISTENT -> {
            drawCircle(
                color = Color(0xFFF97316).copy(alpha = 0.24f * pulse),
                radius = w * 0.40f,
                center = Offset(cx, h * 0.56f)
            )
            drawFlameBody(
                cx = cx,
                topY = h * 0.12f,
                bottomY = h * 0.92f,
                halfWidth = w * 0.35f,
                tipDrift = tipDrift,
                outerColors = listOf(Color(0xFFFEF08A), Color(0xFFF59E0B), Color(0xFFEA580C)),
                innerColors = listOf(Color.White, Color(0xFFFDE047))
            )
        }

        // 15–29 days: High-Energy Blue Flame
        StreakMasteryLevel.ADVANCED -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.42f * pulse), Color.Transparent),
                    center = Offset(cx, h * 0.52f),
                    radius = w * 0.46f
                ),
                radius = w * 0.46f,
                center = Offset(cx, h * 0.52f)
            )
            drawFlameBody(
                cx = cx,
                topY = h * 0.10f,
                bottomY = h * 0.92f,
                halfWidth = w * 0.36f,
                tipDrift = tipDrift,
                outerColors = listOf(Color(0xFFBAE6FD), Color(0xFF38BDF8), Color(0xFF0284C7)),
                innerColors = listOf(Color.White, Color(0xFF7DD3FC))
            )
        }

        // 30–59 days: Radiant Violet Flame
        StreakMasteryLevel.EXPERT -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFC084FC).copy(alpha = 0.48f * pulse), Color.Transparent),
                    center = Offset(cx, h * 0.50f),
                    radius = w * 0.48f
                ),
                radius = w * 0.48f,
                center = Offset(cx, h * 0.50f)
            )
            drawFlameBody(
                cx = cx,
                topY = h * 0.08f,
                bottomY = h * 0.92f,
                halfWidth = w * 0.38f,
                tipDrift = tipDrift,
                outerColors = listOf(Color(0xFFF5D0FE), Color(0xFFA855F7), Color(0xFF6D28D9)),
                innerColors = listOf(Color.White, Color(0xFFE9D5FF))
            )
        }

        // 60+ days: Golden Phoenix Master Flame with halo pulse & orbiting embers
        StreakMasteryLevel.MASTER -> {
            // Halo pulse ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFDE047).copy(alpha = 0.55f * pulse), Color.Transparent),
                    center = Offset(cx, h * 0.50f),
                    radius = w * 0.50f
                ),
                radius = w * 0.50f,
                center = Offset(cx, h * 0.50f)
            )
            drawCircle(
                color = Color(0xFFFBBF24).copy(alpha = 0.58f),
                radius = w * 0.43f * pulse.coerceAtMost(1.04f),
                center = Offset(cx, h * 0.50f),
                style = Stroke(width = (w * 0.032f).coerceAtLeast(1f))
            )

            // Stylized Phoenix Wings behind golden flame
            val leftWing = Path().apply {
                moveTo(cx - w * 0.10f, h * 0.66f)
                quadraticTo(w * 0.06f, h * 0.42f + sway * h * 0.03f, w * 0.14f, h * 0.24f)
                quadraticTo(w * 0.28f, h * 0.40f, cx - w * 0.06f, h * 0.56f)
                close()
            }
            val rightWing = Path().apply {
                moveTo(cx + w * 0.10f, h * 0.66f)
                quadraticTo(w * 0.94f, h * 0.42f - sway * h * 0.03f, w * 0.86f, h * 0.24f)
                quadraticTo(w * 0.72f, h * 0.40f, cx + w * 0.06f, h * 0.56f)
                close()
            }
            val wingBrush = Brush.verticalGradient(listOf(Color(0xFFFEF08A), Color(0xFFD97706)))
            drawPath(leftWing, wingBrush)
            drawPath(rightWing, wingBrush)

            drawFlameBody(
                cx = cx,
                topY = h * 0.08f,
                bottomY = h * 0.90f,
                halfWidth = w * 0.34f,
                tipDrift = tipDrift,
                outerColors = listOf(Color(0xFFFEF9C3), Color(0xFFFBBF24), Color(0xFFB45309)),
                innerColors = listOf(Color.White, Color(0xFFFEF08A))
            )

            // Orbiting golden ember particles
            for (i in 0 until 4) {
                val rad = ((orbitAngle + i * 90f) * PI / 180.0)
                val px = cx + (cos(rad) * w * 0.40f).toFloat()
                val py = h * 0.48f + (sin(rad) * h * 0.32f).toFloat()
                drawCircle(Color(0xFFFEF08A), radius = (w * 0.044f).coerceAtLeast(1.4f), center = Offset(px, py))
            }
        }
    }
}

private fun DrawScope.drawFlameBody(
    cx: Float,
    topY: Float,
    bottomY: Float,
    halfWidth: Float,
    tipDrift: Float,
    outerColors: List<Color>,
    innerColors: List<Color>
) {
    val spanH = bottomY - topY
    val outer = Path().apply {
        moveTo(cx + tipDrift, topY)
        cubicTo(
            cx + halfWidth * 0.85f,
            topY + spanH * 0.34f,
            cx + halfWidth * 1.15f,
            bottomY - spanH * 0.14f,
            cx,
            bottomY
        )
        cubicTo(
            cx - halfWidth * 1.15f,
            bottomY - spanH * 0.14f,
            cx - halfWidth * 0.85f,
            topY + spanH * 0.34f,
            cx + tipDrift,
            topY
        )
        close()
    }
    drawPath(outer, Brush.verticalGradient(outerColors, startY = topY, endY = bottomY))

    val innerTop = topY + spanH * 0.36f
    val innerHalf = halfWidth * 0.50f
    val inner = Path().apply {
        moveTo(cx + tipDrift * 0.45f, innerTop)
        cubicTo(
            cx + innerHalf,
            innerTop + spanH * 0.25f,
            cx + innerHalf,
            bottomY - spanH * 0.12f,
            cx,
            bottomY - spanH * 0.06f
        )
        cubicTo(
            cx - innerHalf,
            bottomY - spanH * 0.12f,
            cx - innerHalf,
            innerTop + spanH * 0.25f,
            cx + tipDrift * 0.45f,
            innerTop
        )
        close()
    }
    drawPath(inner, Brush.verticalGradient(innerColors, startY = innerTop, endY = bottomY))
}
