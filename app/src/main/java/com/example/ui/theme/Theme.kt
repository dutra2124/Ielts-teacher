package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ApexLightColorScheme = lightColorScheme(
    primary = ElectricIndigo,
    onPrimary = InkOnHero,
    primaryContainer = LightApexColors.electricIndigoLight,
    onPrimaryContainer = ElectricIndigoDark,
    secondary = VividViolet,
    onSecondary = InkOnHero,
    secondaryContainer = LightApexColors.vividVioletLight,
    onSecondaryContainer = ElectricIndigoDark,
    tertiary = CyanGlow,
    onTertiary = InkOnHero,
    tertiaryContainer = LightApexColors.cyanGlowLight,
    onTertiaryContainer = LightApexColors.inkPrimary,
    background = LightApexColors.surfaceCanvas,
    onBackground = LightApexColors.inkPrimary,
    surface = LightApexColors.cardWhite,
    onSurface = LightApexColors.inkPrimary,
    surfaceVariant = LightApexColors.cardSurfaceTinted,
    onSurfaceVariant = LightApexColors.inkSecondary,
    outline = LightApexColors.cardBorderSoft,
    outlineVariant = LightApexColors.cardBorderSoft,
    error = RoseAlert,
    onError = InkOnHero
)

private val ApexNightStudyColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = NightStudyApexColors.electricIndigoLight,
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFFA78BFA),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = NightStudyApexColors.vividVioletLight,
    onSecondaryContainer = Color(0xFFEDE9FE),
    tertiary = Color(0xFF22D3EE),
    onTertiary = Color(0xFF082F49),
    tertiaryContainer = NightStudyApexColors.cyanGlowLight,
    onTertiaryContainer = Color(0xFFCFFAFE),
    background = NightStudyApexColors.surfaceCanvas,
    onBackground = NightStudyApexColors.inkPrimary,
    surface = NightStudyApexColors.cardWhite,
    onSurface = NightStudyApexColors.inkPrimary,
    surfaceVariant = NightStudyApexColors.cardSurfaceTinted,
    onSurfaceVariant = NightStudyApexColors.inkSecondary,
    outline = NightStudyApexColors.cardBorderSoft,
    outlineVariant = NightStudyApexColors.cardBorderSoft,
    error = Color(0xFFFB7185),
    onError = Color(0xFF4C0519)
)

private val ApexCdIeltsDarkColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF0B1120),
    primaryContainer = CdIeltsDarkApexColors.electricIndigoLight,
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF94A3B8),
    onSecondary = Color(0xFF0D1015),
    secondaryContainer = CdIeltsDarkApexColors.cardSurfaceTinted,
    onSecondaryContainer = CdIeltsDarkApexColors.inkPrimary,
    tertiary = Color(0xFF2DD4BF),
    onTertiary = Color(0xFF042F2E),
    tertiaryContainer = CdIeltsDarkApexColors.practiceTealLight,
    onTertiaryContainer = Color(0xFFCCFBF1),
    background = CdIeltsDarkApexColors.surfaceCanvas,
    onBackground = CdIeltsDarkApexColors.inkPrimary,
    surface = CdIeltsDarkApexColors.cardWhite,
    onSurface = CdIeltsDarkApexColors.inkPrimary,
    surfaceVariant = CdIeltsDarkApexColors.cardSurfaceTinted,
    onSurfaceVariant = CdIeltsDarkApexColors.inkSecondary,
    outline = CdIeltsDarkApexColors.cardBorderSoft,
    outlineVariant = CdIeltsDarkApexColors.cardBorderSoft,
    error = Color(0xFFFB7185),
    onError = Color(0xFF4C0519)
)

val ApexShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

/**
 * Resolves the active [ApexColorPalette] from [AppThemeMode] and system dark state.
 */
@Composable
fun resolveApexPalette(mode: AppThemeMode): ApexColorPalette {
    val systemDark = isSystemInDarkTheme()
    return when (mode) {
        AppThemeMode.LIGHT -> LightApexColors
        AppThemeMode.NIGHT_STUDY -> NightStudyApexColors
        AppThemeMode.CD_IELTS_DARK -> CdIeltsDarkApexColors
        AppThemeMode.SYSTEM -> if (systemDark) NightStudyApexColors else LightApexColors
    }
}

@Composable
fun ApexIeltsTheme(
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val palette = resolveApexPalette(themeMode)
    val colorScheme = when (themeMode) {
        AppThemeMode.LIGHT -> ApexLightColorScheme
        AppThemeMode.NIGHT_STUDY -> ApexNightStudyColorScheme
        AppThemeMode.CD_IELTS_DARK -> ApexCdIeltsDarkColorScheme
        AppThemeMode.SYSTEM -> if (systemDark) ApexNightStudyColorScheme else ApexLightColorScheme
    }

    CompositionLocalProvider(LocalApexColors provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ApexTypography,
            shapes = ApexShapes,
            content = content
        )
    }
}
