package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min

/**
 * Legacy & Exam-Room Theme Modes preserved for compatibility with Exam Room Auto-Dark & System mode.
 */
enum class AppThemeMode(
    val id: String,
    val labelVi: String,
    val subtitleVi: String,
    val shortBadgeVi: String
) {
    LIGHT(
        id = "light",
        labelVi = "Sáng (Ban ngày)",
        subtitleVi = "Nền sáng học thuật tươi mới, độ tương phản rõ nét cho buổi học ban ngày.",
        shortBadgeVi = "Sáng"
    ),
    NIGHT_STUDY(
        id = "night_study",
        labelVi = "Học đêm (Late-Night)",
        subtitleVi = "Nền xanh đêm dịu mắt, giảm ánh sáng xanh & chói lóa khi ôn từ vựng khuya.",
        shortBadgeVi = "Học đêm"
    ),
    CD_IELTS_DARK(
        id = "cd_ielts_dark",
        labelVi = "Phòng thi Tối (CD-IELTS)",
        subtitleVi = "Tông than chì trung tính chống mỏi mắt, tập trung tối đa cho bài đọc 60 phút.",
        shortBadgeVi = "Thi Tối"
    ),
    SYSTEM(
        id = "system",
        labelVi = "Theo hệ thống Android",
        subtitleVi = "Tự động chuyển giữa Sáng và Học đêm theo cài đặt thiết bị của bạn.",
        shortBadgeVi = "Tự động"
    );

    companion object {
        fun fromId(id: String?): AppThemeMode =
            entries.firstOrNull { it.id == id } ?: LIGHT
    }
}

/**
 * 5 Curated High-Contrast Color Presets for Master Ielts.
 */
enum class AppThemePreset(
    val id: String,
    val titleVi: String,
    val subtitleVi: String,
    val contrastBadge: String,
    val isDark: Boolean,
    val isHighContrast: Boolean,
    val primaryHex: Long,
    val secondaryHex: Long,
    val surfaceHex: Long,
    val cardHex: Long,
    val borderHex: Long,
    val linkedMode: AppThemeMode
) {
    INDIGO_SHARP(
        id = "indigo_sharp",
        titleVi = "Indigo Sắc Nét",
        subtitleVi = "Chàm học thuật sắc nét, nút nổi bật, rõ chữ mọi góc nhìn",
        contrastBadge = "Chuẩn AA+",
        isDark = false,
        isHighContrast = false,
        primaryHex = 0xFF3730A3,
        secondaryHex = 0xFF6D28D9,
        surfaceHex = 0xFFF3F5FB,
        cardHex = 0xFFFFFFFF,
        borderHex = 0xFFDCE3F5,
        linkedMode = AppThemeMode.LIGHT
    ),
    EMERALD_ACADEMIC(
        id = "emerald_academic",
        titleVi = "Emerald Học Thuật",
        subtitleVi = "Xanh ngọc lục bảo thư giãn mắt, lý tưởng cho Reading & Vocab",
        contrastBadge = "Chuẩn AA+",
        isDark = false,
        isHighContrast = false,
        primaryHex = 0xFF047857,
        secondaryHex = 0xFF0D9488,
        surfaceHex = 0xFFF0FDF9,
        cardHex = 0xFFFFFFFF,
        borderHex = 0xFFC7EDE1,
        linkedMode = AppThemeMode.LIGHT
    ),
    SUNSET_DYNAMIC(
        id = "sunset_dynamic",
        titleVi = "Sunset Năng Động",
        subtitleVi = "Cam cháy rực rỡ giàu cảm hứng, khơi nguồn năng lượng luyện thi",
        contrastBadge = "Chuẩn AA+",
        isDark = false,
        isHighContrast = false,
        primaryHex = 0xFFC2410C,
        secondaryHex = 0xFFEA580C,
        surfaceHex = 0xFFFFF8F3,
        cardHex = 0xFFFFFFFF,
        borderHex = 0xFFF7DEC9,
        linkedMode = AppThemeMode.LIGHT
    ),
    DARK_ANTI_GLARE(
        id = "dark_anti_glare",
        titleVi = "Dark Mode Chống Chói",
        subtitleVi = "Nền xanh đêm sâu giảm ánh sáng xanh, bảo vệ mắt buổi tối",
        contrastBadge = "Chống chói",
        isDark = true,
        isHighContrast = false,
        primaryHex = 0xFF6366F1,
        secondaryHex = 0xFF06B6D4,
        surfaceHex = 0xFF0B1120,
        cardHex = 0xFF131C31,
        borderHex = 0xFF283858,
        linkedMode = AppThemeMode.NIGHT_STUDY
    ),
    HIGH_CONTRAST(
        id = "high_contrast",
        titleVi = "Siêu Tương Phản",
        subtitleVi = "Vàng rực trên nền Đen tuyệt đối, đạt chuẩn WCAG AAA > 14:1",
        contrastBadge = "WCAG AAA",
        isDark = true,
        isHighContrast = true,
        primaryHex = 0xFFFACC15,
        secondaryHex = 0xFF38BDF8,
        surfaceHex = 0xFF000000,
        cardHex = 0xFF0D1117,
        borderHex = 0xFFFACC15,
        linkedMode = AppThemeMode.CD_IELTS_DARK
    );

    val primaryColor: Color get() = Color(primaryHex)
    val secondaryColor: Color get() = Color(secondaryHex)
    val surfaceColor: Color get() = Color(surfaceHex)
    val cardColor: Color get() = Color(cardHex)
    val borderColor: Color get() = Color(borderHex)

    companion object {
        fun fromId(id: String?): AppThemePreset =
            entries.firstOrNull { it.id == id } ?: INDIGO_SHARP
    }
}

/**
 * 4 Built-in Subtle Aesthetic Wallpaper Patterns (plus None).
 */
enum class AppWallpaperPattern(
    val id: String,
    val titleVi: String,
    val subtitleVi: String
) {
    NONE("none", "Màu trơn", "Sạch sẽ, tối giản tuyệt đối"),
    ACADEMIC_GRID("academic_grid", "Lưới Học Thuật", "Chấm lưới tinh tế như sổ tay luyện thi"),
    AURORA_WAVES("aurora_waves", "Sóng Cực Quang", "Dải màu mềm chuyển động nhẹ nhàng"),
    GEOMETRIC_PRISM("geometric_prism", "Lăng Kính Sáng", "Đường hình học chéo thanh lịch"),
    BOTANICAL_RINGS("botanical_rings", "Vòng Tập Trung", "Vòng tròn đồng tâm êm dịu cho trí nhớ");

    companion object {
        fun fromId(id: String?): AppWallpaperPattern =
            entries.firstOrNull { it.id == id } ?: NONE
    }
}

data class ColorSwatchOption(
    val nameVi: String,
    val colorHex: Long
) {
    val color: Color get() = Color(colorHex)
}

object ThemeColorCatalog {
    val primaryButtonSwatches = listOf(
        ColorSwatchOption("Indigo", 0xFF3730A3),
        ColorSwatchOption("Emerald", 0xFF047857),
        ColorSwatchOption("Cobalt", 0xFF1D4ED8),
        ColorSwatchOption("Violet", 0xFF6D28D9),
        ColorSwatchOption("Sunset", 0xFFC2410C),
        ColorSwatchOption("Crimson", 0xFFBE123C),
        ColorSwatchOption("Teal", 0xFF0F766E),
        ColorSwatchOption("Vàng AAA", 0xFFFACC15)
    )

    val backgroundSurfaceSwatches = listOf(
        ColorSwatchOption("Mây sáng", 0xFFF3F5FB),
        ColorSwatchOption("Trắng tinh", 0xFFFFFFFF),
        ColorSwatchOption("Giấy ấm", 0xFFFAF6EE),
        ColorSwatchOption("Bạc hà", 0xFFF0FDF9),
        ColorSwatchOption("Tím phấn", 0xFFF5F3FF),
        ColorSwatchOption("Đêm xanh", 0xFF0B1120),
        ColorSwatchOption("Than chì", 0xFF111827),
        ColorSwatchOption("Đen tuyền", 0xFF000000)
    )
}

/**
 * WCAG 2.1 contrast ratio & automatic foreground color resolver so every primary button
 * and card remains sharply readable regardless of user customization.
 */
object ThemeContrastUtils {
    fun contrastRatio(foreground: Color, background: Color): Float {
        val l1 = foreground.luminance()
        val l2 = background.luminance()
        val brighter = max(l1, l2)
        val darker = min(l1, l2)
        return (brighter + 0.05f) / (darker + 0.05f)
    }

    fun bestOnColor(background: Color): Color {
        val whiteContrast = contrastRatio(Color.White, background)
        val darkInk = Color(0xFF090D16)
        val darkContrast = contrastRatio(darkInk, background)
        return if (whiteContrast >= darkContrast) Color.White else darkInk
    }

    fun isDarkColor(color: Color): Boolean = color.luminance() < 0.28f

    fun blend(a: Color, b: Color, ratio: Float): Color {
        val t = ratio.coerceIn(0f, 1f)
        return Color(
            red = a.red * (1f - t) + b.red * t,
            green = a.green * (1f - t) + b.green * t,
            blue = a.blue * (1f - t) + b.blue * t,
            alpha = 1f
        )
    }
}

/**
 * Reactive persisted global theme & background state managed via DataStore Preferences.
 */
data class AppThemePreferences(
    val mode: AppThemeMode = AppThemeMode.LIGHT,
    val autoDarkInExamRoom: Boolean = true,
    val preset: AppThemePreset = AppThemePreset.INDIGO_SHARP,
    val customPrimaryHex: Long? = null,
    val customSurfaceHex: Long? = null,
    val wallpaperPattern: AppWallpaperPattern = AppWallpaperPattern.NONE,
    val customBackgroundPath: String = "",
    val backgroundOverlayOpacity: Float = 0.35f,
    val glassmorphismEnabled: Boolean = false,
    val glassFrostOpacity: Float = 0.82f
) {
    val effectivePrimaryColor: Color
        get() = customPrimaryHex?.let { Color(it) } ?: preset.primaryColor

    val effectiveSurfaceColor: Color
        get() = customSurfaceHex?.let { Color(it) } ?: preset.surfaceColor

    val effectiveIsDark: Boolean
        get() = customSurfaceHex?.let { ThemeContrastUtils.isDarkColor(Color(it)) } ?: preset.isDark

    val hasCustomVisualBackground: Boolean
        get() = customBackgroundPath.isNotBlank() ||
            wallpaperPattern != AppWallpaperPattern.NONE ||
            glassmorphismEnabled
}

typealias AppThemeState = AppThemePreferences

/**
 * Live visual mode inside the fullscreen CD-IELTS Mock Exam Room (preserved untouched).
 */
enum class CdIeltsVisualMode(
    val shortLabelVi: String,
    val fullLabelVi: String
) {
    LIGHT("Sáng", "Giấy thi sáng chuẩn IDP/BC"),
    EXAM_DARK("Tối", "Phòng thi Tối CD-IELTS"),
    HIGH_CONTRAST("HC", "Tương phản cao (Vàng trên Đen)");

    fun next(): CdIeltsVisualMode = when (this) {
        LIGHT -> EXAM_DARK
        EXAM_DARK -> HIGH_CONTRAST
        HIGH_CONTRAST -> LIGHT
    }
}
