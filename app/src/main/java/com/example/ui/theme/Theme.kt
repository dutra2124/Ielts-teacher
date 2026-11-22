package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val ApexShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

/**
 * Resolves the active [ApexColorPalette]. When [examRoomOverrideMode] is non-null,
 * strictly uses the realistic IDP/British Council CD-IELTS Exam Room palette untouched.
 */
@Composable
fun resolveApexPalette(
    themePrefs: AppThemePreferences,
    examRoomOverrideMode: AppThemeMode? = null
): ApexColorPalette {
    val systemDark = isSystemInDarkTheme()
    if (examRoomOverrideMode != null) {
        return when (examRoomOverrideMode) {
            AppThemeMode.LIGHT -> LightApexColors
            AppThemeMode.NIGHT_STUDY -> NightStudyApexColors
            AppThemeMode.CD_IELTS_DARK -> CdIeltsDarkApexColors
            AppThemeMode.SYSTEM -> if (systemDark) NightStudyApexColors else LightApexColors
        }
    }
    if (themePrefs.mode == AppThemeMode.SYSTEM &&
        themePrefs.customPrimaryHex == null &&
        themePrefs.customSurfaceHex == null
    ) {
        return if (systemDark) {
            buildCustomApexPalette(themePrefs.copy(preset = AppThemePreset.DARK_ANTI_GLARE))
        } else {
            buildCustomApexPalette(themePrefs)
        }
    }
    return buildCustomApexPalette(themePrefs)
}

private fun buildMaterialColorScheme(palette: ApexColorPalette): ColorScheme {
    return if (palette.isDark) {
        darkColorScheme(
            primary = palette.primaryAccent,
            onPrimary = palette.onPrimaryAccent,
            primaryContainer = palette.electricIndigoLight,
            onPrimaryContainer = palette.inkPrimary,
            secondary = palette.secondaryAccent,
            onSecondary = ThemeContrastUtils.bestOnColor(palette.secondaryAccent),
            secondaryContainer = palette.vividVioletLight,
            onSecondaryContainer = palette.inkPrimary,
            tertiary = CyanGlow,
            onTertiary = Color(0xFF082F49),
            tertiaryContainer = palette.cyanGlowLight,
            onTertiaryContainer = palette.inkPrimary,
            background = palette.surfaceCanvas,
            onBackground = palette.inkPrimary,
            surface = palette.cardWhite,
            onSurface = palette.inkPrimary,
            surfaceVariant = palette.cardSurfaceTinted,
            onSurfaceVariant = palette.inkSecondary,
            outline = palette.cardBorderSoft,
            outlineVariant = palette.cardBorderSoft,
            error = Color(0xFFFB7185),
            onError = Color(0xFF4C0519)
        )
    } else {
        lightColorScheme(
            primary = palette.primaryAccent,
            onPrimary = palette.onPrimaryAccent,
            primaryContainer = palette.electricIndigoLight,
            onPrimaryContainer = palette.indigoOnSurface,
            secondary = palette.secondaryAccent,
            onSecondary = ThemeContrastUtils.bestOnColor(palette.secondaryAccent),
            secondaryContainer = palette.vividVioletLight,
            onSecondaryContainer = palette.indigoOnSurface,
            tertiary = CyanGlow,
            onTertiary = Color.White,
            tertiaryContainer = palette.cyanGlowLight,
            onTertiaryContainer = palette.inkPrimary,
            background = palette.surfaceCanvas,
            onBackground = palette.inkPrimary,
            surface = palette.cardWhite,
            onSurface = palette.inkPrimary,
            surfaceVariant = palette.cardSurfaceTinted,
            onSurfaceVariant = palette.inkSecondary,
            outline = palette.cardBorderSoft,
            outlineVariant = palette.cardBorderSoft,
            error = RoseAlert,
            onError = Color.White
        )
    }
}

@Composable
fun ApexIeltsTheme(
    themePrefs: AppThemePreferences = AppThemePreferences(),
    examRoomOverrideMode: AppThemeMode? = null,
    content: @Composable () -> Unit
) {
    val palette = resolveApexPalette(
        themePrefs = themePrefs,
        examRoomOverrideMode = examRoomOverrideMode
    )
    val colorScheme = buildMaterialColorScheme(palette)

    CompositionLocalProvider(
        LocalApexColors provides palette,
        LocalAppThemePrefs provides themePrefs
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ApexTypography,
            shapes = ApexShapes,
            content = content
        )
    }
}
