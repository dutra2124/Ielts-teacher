package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.TeacherMood
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricIndigoDark
import com.example.ui.theme.GoldMastery
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.StreakAmber
import com.example.ui.theme.VividViolet
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * Custom crisp vector AI Teacher ("Cô Minh Anh AI") with smooth animations:
 * - Periodic natural eye blinking
 * - Subtle rhythmic head nodding
 * - Hand pointing to a miniature Band 8.5 whiteboard (or mood-specific gestures)
 * - Lip-sync mouth motion when speaking
 * Supports 4 moods: TEACHING, LISTENING, CHEERING, ENCOURAGING.
 */
@Composable
fun AnimatedTeacher(
    mood: TeacherMood,
    isSpeaking: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 112.dp,
    contentDescriptionText: String = "Trợ giảng AI ApexIELTS",
    onClick: (() -> Unit)? = null
) {
    val transition = rememberInfiniteTransition(label = "ai_teacher")

    // Blink cycle: every 3.4s, brief 180ms blink closure
    val blinkCycle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "eye_blink"
    )
    val eyeOpenFraction = when {
        blinkCycle in 0.84f..0.875f -> 1f - ((blinkCycle - 0.84f) / 0.035f)
        blinkCycle in 0.875f..0.91f -> (blinkCycle - 0.875f) / 0.035f
        else -> 1f
    }.coerceIn(0.08f, 1f)

    // Subtle head nod
    val nodPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "head_nod"
    )

    // Pointer hand / expressive arm gesture cycle
    val gesturePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gesture_phase"
    )

    // Lip-sync speech phase
    val lipPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (4f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 820, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "lip_sync"
    )

    // Ambient sparkle/halo breath
    val haloPulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_pulse"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val clickableMod = if (onClick != null) {
        modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else {
        modifier
    }

    Canvas(
        modifier = clickableMod
            .size(size)
            .testTag("animated_ai_teacher")
            .semantics { contentDescription = "$contentDescriptionText - ${mood.labelVi}" }
    ) {
        val w = this.size.width
        val h = this.size.height

        val nodY = sin(nodPhase) * h * 0.014f
        val headTilt = when (mood) {
            TeacherMood.LISTENING -> -6.5f + sin(nodPhase) * 2.2f
            TeacherMood.CHEERING -> sin(gesturePhase) * 3.5f
            TeacherMood.ENCOURAGING -> 3.0f + sin(nodPhase) * 2.4f
            TeacherMood.TEACHING -> sin(nodPhase) * 2.2f
        }
        val pointerShift = sin(gesturePhase) * h * 0.032f

        // 1. Soft luminous avatar backdrop circle
        val haloColor = when (mood) {
            TeacherMood.TEACHING -> CyanGlow
            TeacherMood.LISTENING -> Color(0xFF38BDF8)
            TeacherMood.CHEERING -> StreakAmber
            TeacherMood.ENCOURAGING -> Color(0xFFA78BFA)
        }
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    haloColor.copy(alpha = 0.36f),
                    Color.White.copy(alpha = 0.14f),
                    Color.Transparent
                ),
                center = Offset(w * 0.44f, h * 0.54f),
                radius = w * 0.48f * haloPulse
            ),
            radius = w * 0.48f * haloPulse,
            center = Offset(w * 0.44f, h * 0.54f)
        )

        // 2. Mini Whiteboard (right side — focal point in TEACHING & visible classroom prop)
        val boardLeft = w * 0.61f
        val boardTop = h * 0.14f
        val boardW = w * 0.34f
        val boardH = h * 0.36f

        // Whiteboard stand leg
        drawLine(
            color = Color(0xFFCBD5E1),
            start = Offset(boardLeft + boardW * 0.5f, boardTop + boardH),
            end = Offset(boardLeft + boardW * 0.5f, h * 0.68f),
            strokeWidth = w * 0.025f,
            cap = StrokeCap.Round
        )
        // Whiteboard shadow & frame
        drawRoundRect(
            color = ElectricIndigoDark.copy(alpha = 0.20f),
            topLeft = Offset(boardLeft + w * 0.015f, boardTop + h * 0.02f),
            size = Size(boardW, boardH),
            cornerRadius = CornerRadius(w * 0.045f, w * 0.045f)
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(boardLeft, boardTop),
            size = Size(boardW, boardH),
            cornerRadius = CornerRadius(w * 0.045f, w * 0.045f)
        )
        drawRoundRect(
            color = ElectricIndigo.copy(alpha = 0.55f),
            topLeft = Offset(boardLeft, boardTop),
            size = Size(boardW, boardH),
            cornerRadius = CornerRadius(w * 0.045f, w * 0.045f),
            style = Stroke(width = w * 0.02f)
        )
        // Mini whiteboard header strip + ascending Band 8.5 bars
        drawRoundRect(
            brush = Brush.horizontalGradient(listOf(ElectricIndigo, CyanGlow)),
            topLeft = Offset(boardLeft + boardW * 0.14f, boardTop + boardH * 0.16f),
            size = Size(boardW * 0.72f, boardH * 0.13f),
            cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
        )
        // Chart lines / rising Band steps on mini whiteboard
        val barHeights = listOf(0.24f, 0.38f, 0.52f)
        barHeights.forEachIndexed { idx, frac ->
            val bx = boardLeft + boardW * (0.18f + idx * 0.24f)
            val bh = boardH * frac * 0.68f
            val by = boardTop + boardH * 0.84f - bh
            drawRoundRect(
                color = when (idx) {
                    0 -> ElectricIndigo.copy(alpha = 0.45f)
                    1 -> VividViolet.copy(alpha = 0.72f)
                    else -> CyanGlow
                },
                topLeft = Offset(bx, by),
                size = Size(boardW * 0.16f, bh),
                cornerRadius = CornerRadius(w * 0.015f, w * 0.015f)
            )
        }

        // 3. Teacher Torso & Smart Indigo Blazer
        val bodyCenterX = w * 0.40f
        val torsoPath = Path().apply {
            moveTo(bodyCenterX - w * 0.24f, h * 0.94f)
            cubicTo(
                bodyCenterX - w * 0.23f, h * 0.72f,
                bodyCenterX - w * 0.14f, h * 0.64f,
                bodyCenterX, h * 0.64f
            )
            cubicTo(
                bodyCenterX + w * 0.14f, h * 0.64f,
                bodyCenterX + w * 0.23f, h * 0.72f,
                bodyCenterX + w * 0.24f, h * 0.94f
            )
            close()
        }
        drawPath(
            path = torsoPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF312E81), Color(0xFF1E1B4B)),
                startY = h * 0.63f,
                endY = h * 0.95f
            )
        )
        // White shirt V-collar
        val collarPath = Path().apply {
            moveTo(bodyCenterX - w * 0.085f, h * 0.65f)
            lineTo(bodyCenterX, h * 0.78f)
            lineTo(bodyCenterX + w * 0.085f, h * 0.65f)
            close()
        }
        drawPath(collarPath, color = Color.White)
        // Cyan lapel badge pin
        drawCircle(
            color = CyanGlow,
            radius = w * 0.025f,
            center = Offset(bodyCenterX - w * 0.095f, h * 0.76f)
        )

        // 4. Neck
        drawRoundRect(
            color = Color(0xFFF6C6A5),
            topLeft = Offset(bodyCenterX - w * 0.048f, h * 0.57f),
            size = Size(w * 0.096f, h * 0.10f),
            cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
        )

        // 5. Head Group (with subtle nodding & mood tilt)
        val headCenter = Offset(bodyCenterX, h * 0.40f)
        translate(top = nodY) {
            rotate(degrees = headTilt, pivot = Offset(bodyCenterX, h * 0.56f)) {
                // Back hair silhouette (sleek modern bob)
                drawOval(
                    color = Color(0xFF1E1B4B),
                    topLeft = Offset(headCenter.x - w * 0.21f, headCenter.y - h * 0.21f),
                    size = Size(w * 0.42f, h * 0.40f)
                )

                // Ears
                drawCircle(
                    color = Color(0xFFF6C6A5),
                    radius = w * 0.038f,
                    center = Offset(headCenter.x - w * 0.165f, headCenter.y + h * 0.02f)
                )
                drawCircle(
                    color = Color(0xFFF6C6A5),
                    radius = w * 0.038f,
                    center = Offset(headCenter.x + w * 0.165f, headCenter.y + h * 0.02f)
                )

                // Face oval
                drawOval(
                    color = Color(0xFFFDE0C8),
                    topLeft = Offset(headCenter.x - w * 0.16f, headCenter.y - h * 0.155f),
                    size = Size(w * 0.32f, h * 0.34f)
                )

                // Soft cheek blush
                val blushAlpha = if (mood == TeacherMood.CHEERING || mood == TeacherMood.ENCOURAGING) 0.35f else 0.20f
                drawOval(
                    color = RoseAlert.copy(alpha = blushAlpha),
                    topLeft = Offset(headCenter.x - w * 0.125f, headCenter.y + h * 0.045f),
                    size = Size(w * 0.065f, h * 0.034f)
                )
                drawOval(
                    color = RoseAlert.copy(alpha = blushAlpha),
                    topLeft = Offset(headCenter.x + w * 0.060f, headCenter.y + h * 0.045f),
                    size = Size(w * 0.065f, h * 0.034f)
                )

                // Front bangs sweeping neatly
                val bangsPath = Path().apply {
                    moveTo(headCenter.x - w * 0.17f, headCenter.y - h * 0.04f)
                    cubicTo(
                        headCenter.x - w * 0.16f, headCenter.y - h * 0.20f,
                        headCenter.x + w * 0.16f, headCenter.y - h * 0.21f,
                        headCenter.x + w * 0.17f, headCenter.y - h * 0.03f
                    )
                    cubicTo(
                        headCenter.x + w * 0.09f, headCenter.y - h * 0.10f,
                        headCenter.x - w * 0.02f, headCenter.y - h * 0.12f,
                        headCenter.x - w * 0.17f, headCenter.y - h * 0.04f
                    )
                    close()
                }
                drawPath(bangsPath, color = Color(0xFF1E1B4B))

                // Stylish cyan AI hairpin accent
                drawRoundRect(
                    color = CyanGlow,
                    topLeft = Offset(headCenter.x - w * 0.125f, headCenter.y - h * 0.125f),
                    size = Size(w * 0.068f, h * 0.022f),
                    cornerRadius = CornerRadius(w * 0.01f, w * 0.01f)
                )

                // Eyebrows (expressive per mood)
                val browOffsetY = when (mood) {
                    TeacherMood.CHEERING -> -h * 0.012f
                    TeacherMood.LISTENING -> -h * 0.008f
                    else -> 0f
                }
                val browStroke = Stroke(width = w * 0.018f, cap = StrokeCap.Round)
                drawLine(
                    color = Color(0xFF1E1B4B),
                    start = Offset(headCenter.x - w * 0.095f, headCenter.y - h * 0.045f + browOffsetY),
                    end = Offset(headCenter.x - w * 0.035f, headCenter.y - h * 0.052f + browOffsetY),
                    strokeWidth = browStroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color(0xFF1E1B4B),
                    start = Offset(headCenter.x + w * 0.035f, headCenter.y - h * 0.052f + browOffsetY),
                    end = Offset(headCenter.x + w * 0.095f, headCenter.y - h * 0.045f + browOffsetY),
                    strokeWidth = browStroke.width,
                    cap = StrokeCap.Round
                )

                // Eyes with natural periodic blinking (or happy arcs in CHEERING mood)
                val leftEyeCenter = Offset(headCenter.x - w * 0.065f, headCenter.y + h * 0.004f)
                val rightEyeCenter = Offset(headCenter.x + w * 0.065f, headCenter.y + h * 0.004f)

                if (mood == TeacherMood.CHEERING) {
                    val happyArcStroke = Stroke(width = w * 0.022f, cap = StrokeCap.Round)
                    drawArc(
                        color = Color(0xFF0F172A),
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(leftEyeCenter.x - w * 0.028f, leftEyeCenter.y - h * 0.015f),
                        size = Size(w * 0.056f, h * 0.038f),
                        style = happyArcStroke
                    )
                    drawArc(
                        color = Color(0xFF0F172A),
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(rightEyeCenter.x - w * 0.028f, rightEyeCenter.y - h * 0.015f),
                        size = Size(w * 0.056f, h * 0.038f),
                        style = happyArcStroke
                    )
                } else {
                    val eyeW = w * 0.044f
                    val eyeH = h * 0.044f * eyeOpenFraction
                    listOf(leftEyeCenter, rightEyeCenter).forEach { ec ->
                        drawOval(
                            color = Color(0xFF0F172A),
                            topLeft = Offset(ec.x - eyeW * 0.5f, ec.y - eyeH * 0.5f),
                            size = Size(eyeW, eyeH)
                        )
                        if (eyeOpenFraction > 0.45f) {
                            drawCircle(
                                color = Color.White,
                                radius = w * 0.009f,
                                center = Offset(ec.x - eyeW * 0.16f, ec.y - eyeH * 0.16f)
                            )
                        }
                    }
                }

                // Modern thin-rim academic glasses
                val glassStroke = Stroke(width = w * 0.014f)
                drawRoundRect(
                    color = ElectricIndigo,
                    topLeft = Offset(leftEyeCenter.x - w * 0.046f, leftEyeCenter.y - h * 0.035f),
                    size = Size(w * 0.092f, h * 0.072f),
                    cornerRadius = CornerRadius(w * 0.028f, w * 0.028f),
                    style = glassStroke
                )
                drawRoundRect(
                    color = ElectricIndigo,
                    topLeft = Offset(rightEyeCenter.x - w * 0.046f, rightEyeCenter.y - h * 0.035f),
                    size = Size(w * 0.092f, h * 0.072f),
                    cornerRadius = CornerRadius(w * 0.028f, w * 0.028f),
                    style = glassStroke
                )
                drawLine(
                    color = ElectricIndigo,
                    start = Offset(leftEyeCenter.x + w * 0.046f, leftEyeCenter.y),
                    end = Offset(rightEyeCenter.x - w * 0.046f, rightEyeCenter.y),
                    strokeWidth = w * 0.014f
                )

                // Lip-sync animated mouth
                val mouthCenter = Offset(headCenter.x, headCenter.y + h * 0.095f)
                if (isSpeaking) {
                    val openness = (0.30f + 0.70f * abs(sin(lipPhase) * 0.65f + sin(lipPhase * 1.7f) * 0.35f))
                        .coerceIn(0.22f, 1f)
                    val mw = w * (0.068f + openness * 0.02f)
                    val mh = h * (0.022f + openness * 0.042f)
                    drawOval(
                        color = Color(0xFF9F1239),
                        topLeft = Offset(mouthCenter.x - mw * 0.5f, mouthCenter.y - mh * 0.5f),
                        size = Size(mw, mh)
                    )
                    // Upper teeth highlight when wide open
                    if (openness > 0.55f) {
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(mouthCenter.x - mw * 0.32f, mouthCenter.y - mh * 0.40f),
                            size = Size(mw * 0.64f, mh * 0.25f),
                            cornerRadius = CornerRadius(w * 0.01f, w * 0.01f)
                        )
                    }
                } else {
                    // Warm confident smile
                    drawArc(
                        color = Color(0xFFBE123C),
                        startAngle = 15f,
                        sweepAngle = 150f,
                        useCenter = false,
                        topLeft = Offset(mouthCenter.x - w * 0.048f, mouthCenter.y - h * 0.028f),
                        size = Size(w * 0.096f, h * 0.052f),
                        style = Stroke(width = w * 0.019f, cap = StrokeCap.Round)
                    )
                }
            }
        }

        // 6. Expressive Hands & Props per Mood
        when (mood) {
            TeacherMood.TEACHING -> {
                // Right arm reaching smoothly toward the mini whiteboard with a cyan laser pointer
                val handPos = Offset(w * 0.60f, h * 0.60f + pointerShift)
                val pointerTip = Offset(boardLeft + boardW * 0.28f, boardTop + boardH * 0.42f + pointerShift * 0.7f)

                drawLine(
                    color = Color(0xFF312E81),
                    start = Offset(bodyCenterX + w * 0.16f, h * 0.74f),
                    end = handPos,
                    strokeWidth = w * 0.068f,
                    cap = StrokeCap.Round
                )
                // Pointer wand
                drawLine(
                    color = CyanGlow,
                    start = handPos,
                    end = pointerTip,
                    strokeWidth = w * 0.022f,
                    cap = StrokeCap.Round
                )
                // Glowing pointer tip dot
                drawCircle(
                    color = CyanGlow.copy(alpha = 0.35f),
                    radius = w * 0.036f,
                    center = pointerTip
                )
                drawCircle(
                    color = Color.White,
                    radius = w * 0.016f,
                    center = pointerTip
                )
                // Hand sphere
                drawCircle(
                    color = Color(0xFFFDE0C8),
                    radius = w * 0.042f,
                    center = handPos
                )
            }

            TeacherMood.LISTENING -> {
                // Hand cupped attentively near ear + animated cyan listening waves
                val earHand = Offset(bodyCenterX + w * 0.19f, h * 0.46f + pointerShift * 0.3f)
                drawLine(
                    color = Color(0xFF312E81),
                    start = Offset(bodyCenterX + w * 0.17f, h * 0.74f),
                    end = earHand,
                    strokeWidth = w * 0.064f,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = Color(0xFFFDE0C8),
                    radius = w * 0.040f,
                    center = earHand
                )
                // Listening wave arcs near left side
                val wavePulse = (sin(gesturePhase) + 1f) * 0.5f
                drawArc(
                    color = CyanGlow.copy(alpha = 0.45f + wavePulse * 0.45f),
                    startAngle = 140f,
                    sweepAngle = 80f,
                    useCenter = false,
                    topLeft = Offset(w * 0.04f, h * 0.28f),
                    size = Size(w * 0.18f, h * 0.24f),
                    style = Stroke(width = w * 0.022f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = VividViolet.copy(alpha = 0.30f + (1f - wavePulse) * 0.45f),
                    startAngle = 145f,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = Offset(w * 0.09f, h * 0.32f),
                    size = Size(w * 0.13f, h * 0.17f),
                    style = Stroke(width = w * 0.020f, cap = StrokeCap.Round)
                )
            }

            TeacherMood.CHEERING -> {
                // Both arms raised cheerfully + floating golden sparkles!
                val cheerBounce = abs(sin(gesturePhase)) * h * 0.045f
                val leftHand = Offset(bodyCenterX - w * 0.24f, h * 0.44f - cheerBounce)
                val rightHand = Offset(bodyCenterX + w * 0.25f, h * 0.42f - cheerBounce)

                drawLine(
                    color = Color(0xFF312E81),
                    start = Offset(bodyCenterX - w * 0.16f, h * 0.72f),
                    end = leftHand,
                    strokeWidth = w * 0.064f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color(0xFF312E81),
                    start = Offset(bodyCenterX + w * 0.16f, h * 0.72f),
                    end = rightHand,
                    strokeWidth = w * 0.064f,
                    cap = StrokeCap.Round
                )
                drawCircle(color = Color(0xFFFDE0C8), radius = w * 0.042f, center = leftHand)
                drawCircle(color = Color(0xFFFDE0C8), radius = w * 0.042f, center = rightHand)

                // Golden celebration stars
                drawStarBurst(Offset(w * 0.14f, h * 0.20f - cheerBounce * 0.6f), w * 0.055f, GoldMastery)
                drawStarBurst(Offset(w * 0.56f, h * 0.12f - cheerBounce * 0.8f), w * 0.048f, CyanGlow)
                drawStarBurst(Offset(w * 0.28f, h * 0.11f + cheerBounce * 0.4f), w * 0.036f, StreakAmber)
            }

            TeacherMood.ENCOURAGING -> {
                // Warm raised encouraging hand + floating supportive heart spark
                val handY = h * 0.58f + pointerShift * 0.4f
                val handPos = Offset(bodyCenterX + w * 0.22f, handY)
                drawLine(
                    color = Color(0xFF312E81),
                    start = Offset(bodyCenterX + w * 0.16f, h * 0.74f),
                    end = handPos,
                    strokeWidth = w * 0.064f,
                    cap = StrokeCap.Round
                )
                drawCircle(color = Color(0xFFFDE0C8), radius = w * 0.042f, center = handPos)
                drawStarBurst(
                    center = Offset(w * 0.14f, h * 0.25f + pointerShift * 0.5f),
                    radius = w * 0.05f,
                    color = Color(0xFF34D399)
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStarBurst(
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
    drawPath(path = path, color = color)
}
