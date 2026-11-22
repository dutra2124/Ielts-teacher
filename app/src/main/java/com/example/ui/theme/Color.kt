package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// Static Brand & Gradient Anchors (safe for top-level Brushes & Curriculum enums)
// ============================================================================
val ElectricIndigo = Color(0xFF4338CA)
val ElectricIndigoDark = Color(0xFF312E81)
val VividViolet = Color(0xFF7C3AED)
val CyanGlow = Color(0xFF06B6D4)

val StreakOrange = Color(0xFFF97316)
val StreakAmber = Color(0xFFFBBF24)
val EmeraldSuccess = Color(0xFF10B981)
val PracticeTeal = Color(0xFF0D9488)
val PracticeTealDark = Color(0xFF0F766E)
val PracticeTealDeep = Color(0xFF115E59)
val GoldMastery = Color(0xFFF59E0B)
val RoseAlert = Color(0xFFF43F5E)
val RoseDiff = Color(0xFFE11D48)

val CdIeltsTopbar = Color(0xFF1E293B)
val CdIeltsHighContrastBg = Color(0xFF000000)
val CdIeltsHighContrastText = Color(0xFFFDE047)

val InkOnHero = Color(0xFFFFFFFF)
val InkOnHeroMuted = Color(0xFFE0E7FF)

// Tier Accent Colors (static anchors for RoadmapTier enum)
val Tier1Primary = Color(0xFF4338CA)
val Tier1Secondary = Color(0xFF0EA5E9)
val Tier1Container = Color(0xFFE0E7FF)

val Tier2Primary = Color(0xFF7C3AED)
val Tier2Secondary = Color(0xFF06B6D4)
val Tier2Container = Color(0xFFEDE9FE)

val Tier3Primary = Color(0xFF312E81)
val Tier3Secondary = Color(0xFFF59E0B)
val Tier3Container = Color(0xFFFEF3C7)

// ============================================================================
// Dynamic Theme-Aware Palette (Reactive to Presets, Custom Colors & Exam Mode)
// ============================================================================
@Immutable
data class ApexColorPalette(
    val isDark: Boolean,
    val isHighContrast: Boolean = false,
    val primaryAccent: Color = ElectricIndigo,
    val onPrimaryAccent: Color = Color.White,
    val secondaryAccent: Color = VividViolet,
    val heroStart: Color = ElectricIndigo,
    val heroMid: Color = VividViolet,
    val heroEnd: Color = CyanGlow,
    val heroInk: Color = InkOnHero,
    val heroInkMuted: Color = InkOnHeroMuted,
    val surfaceCanvas: Color,
    val cardWhite: Color,
    val cardBorderSoft: Color,
    val cardSurfaceTinted: Color,
    val inkPrimary: Color,
    val inkSecondary: Color,
    val inkMuted: Color,
    val electricIndigoLight: Color,
    val vividVioletLight: Color,
    val cyanGlowLight: Color,
    val streakBg: Color,
    val emeraldSuccessLight: Color,
    val emeraldOnContainer: Color,
    val practiceTealLight: Color,
    val practiceTealSurface: Color,
    val tealOnSurface: Color,
    val indigoOnSurface: Color,
    val highlightYellow: Color,
    val highlightYellowBorder: Color,
    val goldMasteryLight: Color,
    val goldOnContainer: Color,
    val roseDiffLight: Color,
    val cdIeltsSurface: Color,
    val cdIeltsCard: Color,
    val cdIeltsBorder: Color,
    val cdIeltsPaletteBg: Color,
    val cdIeltsDividerBg: Color,
    val cdIeltsTopbarBar: Color
)

val LightApexColors = ApexColorPalette(
    isDark = false,
    isHighContrast = false,
    primaryAccent = ElectricIndigo,
    onPrimaryAccent = Color.White,
    secondaryAccent = VividViolet,
    heroStart = ElectricIndigo,
    heroMid = VividViolet,
    heroEnd = CyanGlow,
    heroInk = Color.White,
    heroInkMuted = Color(0xFFE0E7FF),
    surfaceCanvas = Color(0xFFF3F5FB),
    cardWhite = Color(0xFFFFFFFF),
    cardBorderSoft = Color(0xFFE3E8F6),
    cardSurfaceTinted = Color(0xFFEEF2FF),
    inkPrimary = Color(0xFF0F172A),
    inkSecondary = Color(0xFF475569),
    inkMuted = Color(0xFF64748B),
    electricIndigoLight = Color(0xFFE0E7FF),
    vividVioletLight = Color(0xFFEDE9FE),
    cyanGlowLight = Color(0xFFCFFAFE),
    streakBg = Color(0xFFFFF7ED),
    emeraldSuccessLight = Color(0xFFD1FAE5),
    emeraldOnContainer = Color(0xFF065F46),
    practiceTealLight = Color(0xFFCCFBF1),
    practiceTealSurface = Color(0xFFF0FDFA),
    tealOnSurface = Color(0xFF0F766E),
    indigoOnSurface = Color(0xFF4338CA),
    highlightYellow = Color(0xFFFEF08A),
    highlightYellowBorder = Color(0xFFEAB308),
    goldMasteryLight = Color(0xFFFEF3C7),
    goldOnContainer = Color(0xFFB45309),
    roseDiffLight = Color(0xFFFFE4E6),
    cdIeltsSurface = Color(0xFFFFFFFF),
    cdIeltsCard = Color(0xFFFFFFFF),
    cdIeltsBorder = Color(0xFFCBD5E1),
    cdIeltsPaletteBg = Color(0xFFF8FAFC),
    cdIeltsDividerBg = Color(0xFFE2E8F0),
    cdIeltsTopbarBar = Color(0xFF1E293B)
)

val NightStudyApexColors = ApexColorPalette(
    isDark = true,
    isHighContrast = false,
    primaryAccent = Color(0xFF6366F1),
    onPrimaryAccent = Color.White,
    secondaryAccent = Color(0xFF06B6D4),
    heroStart = Color(0xFF1E1B4B),
    heroMid = Color(0xFF312E81),
    heroEnd = Color(0xFF0E7490),
    heroInk = Color.White,
    heroInkMuted = Color(0xFFCBD5E1),
    surfaceCanvas = Color(0xFF0B1120),
    cardWhite = Color(0xFF131C31),
    cardBorderSoft = Color(0xFF243350),
    cardSurfaceTinted = Color(0xFF1A2644),
    inkPrimary = Color(0xFFEAF0FA),
    inkSecondary = Color(0xFFAABBD6),
    inkMuted = Color(0xFF7D91B2),
    electricIndigoLight = Color(0xFF232C5C),
    vividVioletLight = Color(0xFF2D2155),
    cyanGlowLight = Color(0xFF113545),
    streakBg = Color(0xFF3A2312),
    emeraldSuccessLight = Color(0xFF0E342A),
    emeraldOnContainer = Color(0xFF6EE7B7),
    practiceTealLight = Color(0xFF103838),
    practiceTealSurface = Color(0xFF0E292C),
    tealOnSurface = Color(0xFF2DD4BF),
    indigoOnSurface = Color(0xFF818CF8),
    highlightYellow = Color(0xFF4A3B12),
    highlightYellowBorder = Color(0xFFCA8A04),
    goldMasteryLight = Color(0xFF382811),
    goldOnContainer = Color(0xFFFCD34D),
    roseDiffLight = Color(0xFF3F1727),
    cdIeltsSurface = Color(0xFF111827),
    cdIeltsCard = Color(0xFF1A2233),
    cdIeltsBorder = Color(0xFF2D3B54),
    cdIeltsPaletteBg = Color(0xFF0E1525),
    cdIeltsDividerBg = Color(0xFF1F2A40),
    cdIeltsTopbarBar = Color(0xFF0F172A)
)

val CdIeltsDarkApexColors = ApexColorPalette(
    isDark = true,
    isHighContrast = false,
    primaryAccent = Color(0xFF60A5FA),
    onPrimaryAccent = Color(0xFF0B1120),
    secondaryAccent = Color(0xFF2DD4BF),
    heroStart = Color(0xFF111827),
    heroMid = Color(0xFF1E293B),
    heroEnd = Color(0xFF0F172A),
    heroInk = Color.White,
    heroInkMuted = Color(0xFFCBD5E1),
    surfaceCanvas = Color(0xFF0D1015),
    cardWhite = Color(0xFF151B23),
    cardBorderSoft = Color(0xFF2B3442),
    cardSurfaceTinted = Color(0xFF1C2430),
    inkPrimary = Color(0xFFE6EDF3),
    inkSecondary = Color(0xFFA2AFBF),
    inkMuted = Color(0xFF788596),
    electricIndigoLight = Color(0xFF202B48),
    vividVioletLight = Color(0xFF2A2245),
    cyanGlowLight = Color(0xFF12303D),
    streakBg = Color(0xFF352214),
    emeraldSuccessLight = Color(0xFF113128),
    emeraldOnContainer = Color(0xFF6EE7B7),
    practiceTealLight = Color(0xFF123435),
    practiceTealSurface = Color(0xFF102629),
    tealOnSurface = Color(0xFF5EEAD4),
    indigoOnSurface = Color(0xFF93C5FD),
    highlightYellow = Color(0xFF4B3D12),
    highlightYellowBorder = Color(0xFFD97706),
    goldMasteryLight = Color(0xFF362812),
    goldOnContainer = Color(0xFFFDE047),
    roseDiffLight = Color(0xFF3D1926),
    cdIeltsSurface = Color(0xFF0E1218),
    cdIeltsCard = Color(0xFF161C25),
    cdIeltsBorder = Color(0xFF2F3A4A),
    cdIeltsPaletteBg = Color(0xFF11161E),
    cdIeltsDividerBg = Color(0xFF202938),
    cdIeltsTopbarBar = Color(0xFF0B0F15)
)

/**
 * Builds a WCAG high-contrast [ApexColorPalette] dynamically from [AppThemePreferences].
 */
fun buildCustomApexPalette(prefs: AppThemePreferences): ApexColorPalette {
    val preset = prefs.preset
    val primary = prefs.effectivePrimaryColor
    val surface = prefs.effectiveSurfaceColor
    val isDark = prefs.effectiveIsDark
    val isHc = preset.isHighContrast
    val onPrimary = ThemeContrastUtils.bestOnColor(primary)

    val base = when {
        isHc -> CdIeltsDarkApexColors
        isDark -> NightStudyApexColors
        else -> LightApexColors
    }

    val cardColor = when {
        isHc -> Color(0xFF0C1017)
        isDark -> ThemeContrastUtils.blend(surface, Color(0xFF1E293B), 0.42f)
        else -> Color.White
    }

    val borderColor = when {
        isHc -> primary.copy(alpha = 0.85f)
        isDark -> ThemeContrastUtils.blend(cardColor, primary, 0.28f)
        else -> ThemeContrastUtils.blend(Color(0xFFD8E0F0), primary, 0.14f)
    }

    val tintedSurface = when {
        isHc -> Color(0xFF151C28)
        isDark -> ThemeContrastUtils.blend(cardColor, primary, 0.18f)
        else -> ThemeContrastUtils.blend(Color.White, primary, 0.08f)
    }

    val accentLightContainer = when {
        isHc -> Color(0xFF262006)
        isDark -> ThemeContrastUtils.blend(cardColor, primary, 0.30f)
        else -> ThemeContrastUtils.blend(Color.White, primary, 0.15f)
    }

    // Ensure readable accent text on card surfaces (WCAG >= 4.5:1)
    val accentOnSurface = if (ThemeContrastUtils.contrastRatio(primary, cardColor) >= 4.2f) {
        primary
    } else if (isDark) {
        ThemeContrastUtils.blend(primary, Color.White, 0.45f)
    } else {
        ThemeContrastUtils.blend(primary, Color(0xFF090D16), 0.35f)
    }

    val heroStart = when {
        isHc -> Color(0xFF090D16)
        isDark -> ThemeContrastUtils.blend(primary, Color(0xFF0B1120), 0.45f)
        else -> primary
    }
    val heroMid = when {
        isHc -> Color(0xFF141B29)
        else -> ThemeContrastUtils.blend(primary, preset.secondaryColor, 0.55f)
    }
    val heroEnd = when {
        isHc -> Color(0xFF1E293B)
        isDark -> ThemeContrastUtils.blend(preset.secondaryColor, Color(0xFF0F172A), 0.35f)
        else -> ThemeContrastUtils.blend(preset.secondaryColor, CyanGlow, 0.38f)
    }
    val heroInk = ThemeContrastUtils.bestOnColor(heroStart)
    val heroInkMuted = heroInk.copy(alpha = 0.84f)

    return base.copy(
        isDark = isDark,
        isHighContrast = isHc,
        primaryAccent = primary,
        onPrimaryAccent = onPrimary,
        secondaryAccent = preset.secondaryColor,
        heroStart = heroStart,
        heroMid = heroMid,
        heroEnd = heroEnd,
        heroInk = heroInk,
        heroInkMuted = heroInkMuted,
        surfaceCanvas = surface,
        cardWhite = cardColor,
        cardBorderSoft = borderColor,
        cardSurfaceTinted = tintedSurface,
        inkPrimary = if (isHc) Color.White else base.inkPrimary,
        inkSecondary = if (isHc) Color(0xFFE2E8F0) else base.inkSecondary,
        inkMuted = if (isHc) Color(0xFFCBD5E1) else base.inkMuted,
        electricIndigoLight = accentLightContainer,
        indigoOnSurface = accentOnSurface
    )
}

val LocalApexColors = staticCompositionLocalOf { LightApexColors }
val LocalAppThemePrefs = staticCompositionLocalOf { AppThemePreferences() }

// ============================================================================
// Dynamic Composable Color Accessors (automatically recompose across all screens!)
// ============================================================================
val ThemePrimary: Color
    @Composable get() = LocalApexColors.current.primaryAccent

val ThemeOnPrimary: Color
    @Composable get() = LocalApexColors.current.onPrimaryAccent

val ThemeSecondary: Color
    @Composable get() = LocalApexColors.current.secondaryAccent

val ThemeHeroBrush: Brush
    @Composable get() {
        val c = LocalApexColors.current
        return Brush.linearGradient(
            colors = listOf(c.heroStart, c.heroMid, c.heroEnd),
            start = Offset(0f, 0f),
            end = Offset(1100f, 950f)
        )
    }

val SurfaceCanvas: Color
    @Composable get() = LocalApexColors.current.surfaceCanvas

val CardWhite: Color
    @Composable get() = LocalApexColors.current.cardWhite

val CardBorderSoft: Color
    @Composable get() = LocalApexColors.current.cardBorderSoft

val CardSurfaceTinted: Color
    @Composable get() = LocalApexColors.current.cardSurfaceTinted

val InkPrimary: Color
    @Composable get() = LocalApexColors.current.inkPrimary

val InkSecondary: Color
    @Composable get() = LocalApexColors.current.inkSecondary

val InkMuted: Color
    @Composable get() = LocalApexColors.current.inkMuted

val ElectricIndigoLight: Color
    @Composable get() = LocalApexColors.current.electricIndigoLight

val VividVioletLight: Color
    @Composable get() = LocalApexColors.current.vividVioletLight

val CyanGlowLight: Color
    @Composable get() = LocalApexColors.current.cyanGlowLight

val StreakBg: Color
    @Composable get() = LocalApexColors.current.streakBg

val EmeraldSuccessLight: Color
    @Composable get() = LocalApexColors.current.emeraldSuccessLight

val EmeraldOnContainer: Color
    @Composable get() = LocalApexColors.current.emeraldOnContainer

val PracticeTealLight: Color
    @Composable get() = LocalApexColors.current.practiceTealLight

val PracticeTealSurface: Color
    @Composable get() = LocalApexColors.current.practiceTealSurface

val TealOnSurface: Color
    @Composable get() = LocalApexColors.current.tealOnSurface

val IndigoOnSurface: Color
    @Composable get() = LocalApexColors.current.indigoOnSurface

val HighlightYellow: Color
    @Composable get() = LocalApexColors.current.highlightYellow

val HighlightYellowBorder: Color
    @Composable get() = LocalApexColors.current.highlightYellowBorder

val GoldMasteryLight: Color
    @Composable get() = LocalApexColors.current.goldMasteryLight

val GoldOnContainer: Color
    @Composable get() = LocalApexColors.current.goldOnContainer

val RoseDiffLight: Color
    @Composable get() = LocalApexColors.current.roseDiffLight

val CdIeltsSurface: Color
    @Composable get() = LocalApexColors.current.cdIeltsSurface

val CdIeltsBorder: Color
    @Composable get() = LocalApexColors.current.cdIeltsBorder
