package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
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
// Dynamic Theme-Aware Palette (Light / Late-Night Study / CD-IELTS Exam Dark)
// ============================================================================
@Immutable
data class ApexColorPalette(
    val isDark: Boolean,
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

/**
 * Late-Night Study Dark Palette: deep warm-navy canvas, elevated obsidian cards,
 * low-glare soft off-white ink, and luminous semantic tints for extended night sessions.
 */
val NightStudyApexColors = ApexColorPalette(
    isDark = true,
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

/**
 * CD-IELTS Exam Room Dark Palette: ultra-clean neutral charcoal paper surfaces,
 * minimal chromatic distraction, high-clarity neutral text for 60-minute timed reading & writing.
 */
val CdIeltsDarkApexColors = ApexColorPalette(
    isDark = true,
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

val LocalApexColors = staticCompositionLocalOf { LightApexColors }

// ============================================================================
// Dynamic Composable Color Accessors (automatically recompose across all screens!)
// ============================================================================
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
