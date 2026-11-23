package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardWhite
import com.example.ui.theme.LocalApexColors
import com.example.ui.theme.LocalAppThemePrefs

/**
 * Pedagogical guardrail CompositionLocal:
 * Set to `true` inside strict CD-IELTS Mock Exam screens, long Reading passages,
 * Self-Study Article Readers, and Writing essay editors so text legibility remains 100% solid.
 */
val LocalGlassmorphismDisabled = compositionLocalOf { false }

/**
 * Returns true only when the user opted into Selective Glassmorphism AND the current
 * composable is outside pedagogical guardrail zones and not in WCAG AAA High Contrast mode.
 */
@Composable
fun isGlassmorphismActive(): Boolean {
    val prefs = LocalAppThemePrefs.current
    val palette = LocalApexColors.current
    val strictExam = LocalStrictExamMode.current
    val guardrailDisabled = LocalGlassmorphismDisabled.current
    return prefs.glassmorphismEnabled &&
        !strictExam &&
        !guardrailDisabled &&
        !palette.isHighContrast
}

/**
 * Resolves a safe frosted glass fill alpha clamped to [0.72f..0.92f] for guaranteed text contrast.
 */
@Composable
fun resolvedGlassAlpha(boostAlpha: Float = 0f): Float {
    val prefs = LocalAppThemePrefs.current
    return (prefs.glassFrostOpacity + boostAlpha).coerceIn(0.72f, 0.94f)
}

/**
 * Luminous top-left-to-bottom-right frosted glass edge highlight brush.
 */
@Composable
fun glassLuminousBorderBrush(accentTint: Color? = null): Brush {
    val palette = LocalApexColors.current
    val accent = accentTint ?: palette.primaryAccent
    val topLeftGlow = if (palette.isDark) {
        Color.White.copy(alpha = 0.28f)
    } else {
        Color.White.copy(alpha = 0.85f)
    }
    val midGlow = accent.copy(alpha = if (palette.isDark) 0.24f else 0.18f)
    val bottomRightGlow = if (palette.isDark) {
        Color.White.copy(alpha = 0.10f)
    } else {
        accent.copy(alpha = 0.26f)
    }
    return Brush.linearGradient(
        colors = listOf(topLeftGlow, midGlow, bottomRightGlow)
    )
}

/**
 * Pure Jetpack Compose selective glassmorphism modifier (zero third-party libraries, Android 10–16 safe).
 * Automatically falls back to a solid high-contrast container when Glassmorphism is disabled
 * or inside pedagogical guardrail zones.
 */
@Composable
fun Modifier.glassMorphism(
    shape: Shape = RoundedCornerShape(20.dp),
    solidColor: Color = CardWhite,
    solidBorderColor: Color = CardBorderSoft,
    accentTint: Color? = null,
    elevation: Dp = 4.dp,
    alphaBoost: Float = 0f
): Modifier {
    val glassOn = isGlassmorphismActive()
    if (!glassOn) {
        return this
            .clip(shape)
            .background(solidColor, shape)
            .border(1.dp, solidBorderColor, shape)
    }

    val safeAlpha = resolvedGlassAlpha(boostAlpha = alphaBoost)
    val luminousBrush = glassLuminousBorderBrush(accentTint = accentTint)
    val palette = LocalApexColors.current

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = palette.primaryAccent.copy(alpha = if (palette.isDark) 0.22f else 0.12f),
            spotColor = palette.primaryAccent.copy(alpha = if (palette.isDark) 0.24f else 0.14f)
        )
        .clip(shape)
        .background(solidColor.copy(alpha = safeAlpha), shape)
        .border(
            width = 1.dp,
            brush = luminousBrush,
            shape = shape
        )
}

/**
 * Drop-in selective glassmorphism card wrapper (`GlassSurface`) for Dashboard cards,
 * floating navigation chrome, flashcards, and sidebar surfaces.
 *
 * - When Glassmorphism is enabled (and outside strict guardrails): renders a translucent frosted
 *   surface (alpha >= 0.72f), soft drop elevation, and a crisp 1.dp luminous top-left-to-bottom-right
 *   gradient border.
 * - When disabled or inside guardrails (Mock Exam, long Reading passages, Writing editors):
 *   renders a crisp, solid, high-contrast [Surface].
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    solidColor: Color = CardWhite,
    solidBorder: BorderStroke? = BorderStroke(1.dp, CardBorderSoft),
    accentTint: Color? = null,
    solidShadowElevation: Dp = 2.dp,
    glassShadowElevation: Dp = 6.dp,
    alphaBoost: Float = 0f,
    forceGlassPreview: Boolean = false,
    previewFrostAlpha: Float? = null,
    content: @Composable () -> Unit
) {
    val activeGlass = forceGlassPreview || isGlassmorphismActive()
    val palette = LocalApexColors.current

    if (!activeGlass) {
        Surface(
            modifier = modifier,
            shape = shape,
            color = solidColor,
            border = solidBorder,
            shadowElevation = solidShadowElevation,
            content = content
        )
    } else {
        val safeAlpha = (previewFrostAlpha ?: resolvedGlassAlpha(boostAlpha = alphaBoost))
            .coerceIn(0.72f, 0.94f)
        val luminousBrush = glassLuminousBorderBrush(accentTint = accentTint)
        Surface(
            modifier = modifier
                .shadow(
                    elevation = glassShadowElevation,
                    shape = shape,
                    ambientColor = palette.primaryAccent.copy(alpha = if (palette.isDark) 0.24f else 0.12f),
                    spotColor = palette.primaryAccent.copy(alpha = if (palette.isDark) 0.26f else 0.15f)
                ),
            shape = shape,
            color = solidColor.copy(alpha = safeAlpha),
            border = BorderStroke(width = 1.dp, brush = luminousBrush),
            shadowElevation = 0.dp,
            content = content
        )
    }
}
