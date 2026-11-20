package com.example.ui.theme

/**
 * Global Theme Modes for ApexIELTS 8.5:
 * - LIGHT: Crisp daytime academic palette
 * - NIGHT_STUDY: Warm, low-glare deep midnight palette engineered for late-night IELTS sessions
 * - CD_IELTS_DARK: Ultra-focused neutral charcoal palette calibrated for long CD-IELTS exam rooms
 * - SYSTEM: Automatically tracks the device's Android system dark/light theme
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
 * Persisted global theme configuration managed via DataStore Preferences.
 */
data class AppThemePreferences(
    val mode: AppThemeMode = AppThemeMode.LIGHT,
    val autoDarkInExamRoom: Boolean = true
)

/**
 * Live visual mode inside the fullscreen CD-IELTS Mock Exam Room.
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
