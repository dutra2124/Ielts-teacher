package com.example.ui.components

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseAlert

/**
 * When true (e.g., inside the strict CD-IELTS Mock Exam Room), all decorative press scales,
 * shakes, and motion embellishments are automatically disabled to maintain distraction-free exam focus.
 */
val LocalStrictExamMode = compositionLocalOf { false }

/**
 * Checks whether the Android system has disabled animations (Settings -> Accessibility -> Remove animations).
 */
fun isSystemReducedMotionEnabled(context: Context): Boolean {
    return runCatching {
        val scale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        )
        scale <= 0f
    }.getOrDefault(false)
}

/**
 * True when decorative micro-interactions should be bypassed (either strict Mock Exam mode
 * or Android system reduced motion accessibility setting).
 */
@Composable
fun shouldReduceMotion(): Boolean {
    val strictExam = LocalStrictExamMode.current
    if (strictExam) return true
    val context = LocalContext.current
    return remember(context) { isSystemReducedMotionEnabled(context) }
}

/**
 * Reusable tactile press micro-interaction modifier:
 * - Scales down to [pressedScale] (default 0.96f) with a snappy spring on touch down and rebounds to 1.0f on release.
 * - Triggers subtle haptic feedback (`HapticFeedbackType.TextHandleMove`) on initial press.
 * - Works alongside any `clickable`, `Button`, `Surface(onClick=...)`, or `NavigationBarItem` without consuming events.
 * - Automatically becomes a clean no-op inside strict CD-IELTS Mock Exam mode or system Reduced Motion.
 */
fun Modifier.tactilePress(
    enabled: Boolean = true,
    hapticEnabled: Boolean = true,
    pressedScale: Float = 0.96f,
    interactionSource: InteractionSource? = null
): Modifier = composed {
    val reduceMotion = shouldReduceMotion()
    if (!enabled || reduceMotion) return@composed this

    val haptic = LocalHapticFeedback.current
    var pointerPressed by remember { mutableStateOf(false) }
    val sourcePressed = if (interactionSource != null) {
        interactionSource.collectIsPressedAsState().value
    } else {
        false
    }
    val isPressed = pointerPressed || sourcePressed

    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = 0.58f,
            stiffness = Spring.StiffnessMediumLow * 2.2f
        ),
        label = "tactile_press_scale"
    )

    val pointerMod = if (interactionSource == null) {
        Modifier.pointerInput(enabled, hapticEnabled) {
            awaitPointerEventScope {
                while (true) {
                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    pointerPressed = true
                    if (hapticEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    pointerPressed = false
                }
            }
        }
    } else {
        LaunchedEffect(sourcePressed) {
            if (sourcePressed && hapticEnabled) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
        Modifier
    }

    this
        .then(pointerMod)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
}

/**
 * Reusable gentle 2-cycle horizontal shake (±6.dp over ~220ms) for inline wrong-answer feedback
 * in Drills & Practice without disruptive dialogs or popups.
 * Pass a monotonically changing key (or boolean/attempt counter) that increments when a wrong answer occurs.
 */
fun Modifier.answerFeedbackShake(
    triggerKey: Int,
    enabled: Boolean = true,
    amplitude: Dp = 6.dp
): Modifier = composed {
    val reduceMotion = shouldReduceMotion()
    if (!enabled || reduceMotion) return@composed this

    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(triggerKey) {
        if (triggerKey > 0) {
            val ampPx = with(density) { amplitude.toPx() }
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            offsetX.snapTo(0f)
            offsetX.animateTo(-ampPx, tween(45))
            offsetX.animateTo(ampPx, tween(50))
            offsetX.animateTo(-ampPx * 0.72f, tween(45))
            offsetX.animateTo(ampPx * 0.55f, tween(40))
            offsetX.animateTo(0f, tween(40))
        }
    }

    this.graphicsLayer {
        translationX = offsetX.value
    }
}

/**
 * Smoothly animates inline border color for semantic answer feedback:
 * - Correct (`true`): crisp EmeraldSuccess glow
 * - Incorrect (`false`): RoseAlert inline indicator
 * - Neutral (`null`): [defaultColor]
 */
@Composable
fun rememberSemanticAnswerBorderColor(
    isCorrect: Boolean?,
    defaultColor: Color
): State<Color> {
    val reduceMotion = shouldReduceMotion()
    val target = when (isCorrect) {
        true -> EmeraldSuccess
        false -> RoseAlert
        null -> defaultColor
    }
    return animateColorAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = if (reduceMotion) 0 else 210),
        label = "semantic_answer_border_color"
    )
}
