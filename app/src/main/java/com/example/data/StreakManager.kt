package com.example.data

import androidx.compose.ui.graphics.Color
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

enum class StreakVisualStyle(
    val id: String,
    val labelVi: String,
    val subtitleVi: String
) {
    TREE(
        id = "tree",
        labelVi = "Cái cây",
        subtitleVi = "Growth Tree • Phát triển từng ngày"
    ),
    FLAME(
        id = "flame",
        labelVi = "Ngọn lửa",
        subtitleVi = "Mastery Flame • Bùng cháy Band 8.5"
    );

    companion object {
        fun fromId(raw: String?): StreakVisualStyle =
            entries.firstOrNull { it.id == raw } ?: TREE
    }
}

enum class StreakMasteryLevel(
    val stageIndex: Int,
    val minDays: Int,
    val previewDays: Int,
    val rangeLabelVi: String,
    val titleVi: String,
    val treeStageVi: String,
    val flameStageVi: String,
    val treeAccent: Color,
    val flameAccent: Color,
    val containerBg: Color
) {
    SEED(
        stageIndex = 0,
        minDays = 0,
        previewDays = 0,
        rangeLabelVi = "0 ngày",
        titleVi = "Khởi đầu",
        treeStageVi = "Hạt giống ngủ yên",
        flameStageVi = "Tia lửa mờ",
        treeAccent = Color(0xFF64748B),
        flameAccent = Color(0xFF64748B),
        containerBg = Color(0xFFF1F5F9)
    ),
    STARTER(
        stageIndex = 1,
        minDays = 1,
        previewDays = 3,
        rangeLabelVi = "1–6 ngày",
        titleVi = "Starter",
        treeStageVi = "Mầm xanh tươi",
        flameStageVi = "Ngọn lửa ấm nhỏ",
        treeAccent = Color(0xFF16A34A),
        flameAccent = Color(0xFFF97316),
        containerBg = Color(0xFFDCFCE7)
    ),
    CONSISTENT(
        stageIndex = 2,
        minDays = 7,
        previewDays = 10,
        rangeLabelVi = "7–14 ngày",
        titleVi = "Consistent",
        treeStageVi = "Cây non sum suê",
        flameStageVi = "Lửa vàng cam rực cháy",
        treeAccent = Color(0xFF059669),
        flameAccent = Color(0xFFEA580C),
        containerBg = Color(0xFFD1FAE5)
    ),
    ADVANCED(
        stageIndex = 3,
        minDays = 15,
        previewDays = 21,
        rangeLabelVi = "15–29 ngày",
        titleVi = "Advanced 7.5",
        treeStageVi = "Cây trưởng thành",
        flameStageVi = "Lửa xanh năng lượng cao",
        treeAccent = Color(0xFF0284C7),
        flameAccent = Color(0xFF0284C7),
        containerBg = Color(0xFFE0F2FE)
    ),
    EXPERT(
        stageIndex = 4,
        minDays = 30,
        previewDays = 45,
        rangeLabelVi = "30–59 ngày",
        titleVi = "Expert 8.0",
        treeStageVi = "Cây nở hoa phát sáng",
        flameStageVi = "Lửa tím rạng rỡ",
        treeAccent = Color(0xFF7C3AED),
        flameAccent = Color(0xFF7C3AED),
        containerBg = Color(0xFFEDE9FE)
    ),
    MASTER(
        stageIndex = 5,
        minDays = 60,
        previewDays = 65,
        rangeLabelVi = "60+ ngày",
        titleVi = "IELTS Master 8.5",
        treeStageVi = "Cây cổ thụ vàng kim",
        flameStageVi = "Phượng hoàng vàng kim",
        treeAccent = Color(0xFFD97706),
        flameAccent = Color(0xFFD97706),
        containerBg = Color(0xFFFEF3C7)
    );

    fun stageNameFor(style: StreakVisualStyle): String =
        if (style == StreakVisualStyle.TREE) treeStageVi else flameStageVi

    fun accentFor(style: StreakVisualStyle): Color =
        if (style == StreakVisualStyle.TREE) treeAccent else flameAccent

    companion object {
        fun fromStreak(days: Int): StreakMasteryLevel = when {
            days >= 60 -> MASTER
            days >= 30 -> EXPERT
            days >= 15 -> ADVANCED
            days >= 7 -> CONSISTENT
            days >= 1 -> STARTER
            else -> SEED
        }
    }
}

data class StreakStatus(
    val currentStreak: Int = 0,
    val lastKpiCompletedDate: String = "",
    val todayKpiCompleted: Boolean = false,
    val visualStyle: StreakVisualStyle = StreakVisualStyle.TREE,
    val daysBetweenLastKpi: Int = 0,
    val daysRemainingBeforeLoss: Int = 3,
    val graceWarningVi: String? = null
) {
    val masteryLevel: StreakMasteryLevel
        get() = StreakMasteryLevel.fromStreak(currentStreak)

    val nextLevel: StreakMasteryLevel?
        get() = StreakMasteryLevel.entries.firstOrNull { it.minDays > currentStreak }

    val daysToNextLevel: Int
        get() = nextLevel?.let { (it.minDays - currentStreak).coerceAtLeast(0) } ?: 0
}

data class StreakLaunchEvaluation(
    val effectiveStreak: Int,
    val todayKpiCompleted: Boolean,
    val daysBetween: Int,
    val daysRemainingBeforeLoss: Int,
    val graceWarningVi: String?,
    val shouldPersistReset: Boolean,
    val didResetDueToInactivity: Boolean,
    val gentleResetMessageVi: String?
)

data class StreakIncrementOutcome(
    val newStreak: Int,
    val newLastKpiDateIso: String,
    val didIncrementToday: Boolean
)

/**
 * Strict Daily KPI Streak Engine (compatible with API 24+ using normalized local calendar-day arithmetic):
 * - Initializes at 0 days for new users (never increments on login or app launch).
 * - Increments by +1 strictly ONCE per ISO calendar day (yyyy-MM-dd) upon reaching 100% Daily KPI.
 * - Enforces 3-day inactivity reset rule:
 *   - daysBetween > 3 (3 full consecutive missed days): resets currentStreak to 0 with a gentle AI Teacher message.
 *   - daysBetween in 2..3 (1-2 missed days): preserves streak and exposes a clear grace-window warning badge.
 */
object StreakManager {

    private const val MILLIS_PER_DAY = 86_400_000L

    fun todayIsoDate(nowMillis: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        return String.format(
            Locale.US,
            "%04d-%02d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    /**
     * Computes exact calendar-day difference between two ISO dates (yyyy-MM-dd).
     * Uses local noon (12:00) on both dates to eliminate DST 23h/25h edge-case drift.
     */
    fun daysBetweenIso(fromIso: String, toIso: String): Int {
        val fromNoon = parseIsoToNoonMillis(fromIso) ?: return 0
        val toNoon = parseIsoToNoonMillis(toIso) ?: return 0
        val diffMillis = toNoon - fromNoon
        return (diffMillis.toDouble() / MILLIS_PER_DAY.toDouble()).roundToInt()
    }

    private fun parseIsoToNoonMillis(iso: String): Long? {
        val parts = iso.trim().split("-")
        if (parts.size != 3) return null
        val y = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        val d = parts[2].toIntOrNull() ?: return null
        if (m !in 1..12 || d !in 1..31) return null
        return Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, m - 1)
            set(Calendar.DAY_OF_MONTH, d)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun evaluateOnLaunchOrDateChange(
        storedStreak: Int,
        lastKpiCompletedDate: String,
        todayIso: String = todayIsoDate()
    ): StreakLaunchEvaluation {
        val safeStreak = storedStreak.coerceAtLeast(0)
        val cleanLastDate = lastKpiCompletedDate.trim()

        if (safeStreak == 0 || cleanLastDate.isBlank()) {
            return StreakLaunchEvaluation(
                effectiveStreak = 0,
                todayKpiCompleted = false,
                daysBetween = 0,
                daysRemainingBeforeLoss = 3,
                graceWarningVi = null,
                shouldPersistReset = storedStreak != 0,
                didResetDueToInactivity = false,
                gentleResetMessageVi = null
            )
        }

        val daysBetween = daysBetweenIso(cleanLastDate, todayIso).coerceAtLeast(0)
        val todayDone = cleanLastDate == todayIso

        // Strict 3-day inactivity reset rule: > 3 calendar days since last KPI completion
        if (daysBetween > 3) {
            return StreakLaunchEvaluation(
                effectiveStreak = 0,
                todayKpiCompleted = false,
                daysBetween = daysBetween,
                daysRemainingBeforeLoss = 3,
                graceWarningVi = null,
                shouldPersistReset = true,
                didResetDueToInactivity = true,
                gentleResetMessageVi = "Bạn đã tạm nghỉ hơn 3 ngày nên chuỗi học vừa về 0. Đừng lo nhé — hoàn thành 100% KPI hôm nay để khởi động lại mầm xanh Band 8.5 ngay!"
            )
        }

        // Grace window: daysBetween == 2 (missed 1 full day -> 2 days left) or 3 (missed 2 full days -> 1 day left)
        val daysRemaining = when {
            todayDone -> 3
            daysBetween <= 1 -> 3
            daysBetween == 2 -> 2
            else -> 1
        }
        val warning = if (!todayDone && (daysBetween == 2 || daysBetween == 3)) {
            "Còn $daysRemaining ngày nữa sẽ mất chuỗi — học ngay hôm nay!"
        } else {
            null
        }

        return StreakLaunchEvaluation(
            effectiveStreak = safeStreak,
            todayKpiCompleted = todayDone,
            daysBetween = daysBetween,
            daysRemainingBeforeLoss = daysRemaining,
            graceWarningVi = warning,
            shouldPersistReset = false,
            didResetDueToInactivity = false,
            gentleResetMessageVi = null
        )
    }

    /**
     * Strictly increments streak by +1 ONLY ONCE per calendar day when 100% Daily KPI is reached.
     * Completing extra tasks on the same calendar day returns didIncrementToday = false.
     */
    fun applyDailyKpiCompletion(
        storedStreak: Int,
        lastKpiCompletedDate: String,
        todayIso: String = todayIsoDate()
    ): StreakIncrementOutcome {
        val cleanLast = lastKpiCompletedDate.trim()
        val safeStreak = storedStreak.coerceAtLeast(0)

        // Already completed KPI today -> strictly do NOT increment again
        if (cleanLast == todayIso && safeStreak > 0) {
            return StreakIncrementOutcome(
                newStreak = safeStreak,
                newLastKpiDateIso = todayIso,
                didIncrementToday = false
            )
        }

        val baseStreak = if (cleanLast.isNotBlank() && daysBetweenIso(cleanLast, todayIso) > 3) {
            0
        } else {
            safeStreak
        }

        return StreakIncrementOutcome(
            newStreak = (baseStreak + 1).coerceAtMost(999),
            newLastKpiDateIso = todayIso,
            didIncrementToday = true
        )
    }

    fun buildStatus(
        storedStreak: Int,
        lastKpiCompletedDate: String,
        visualStyle: StreakVisualStyle,
        todayIso: String = todayIsoDate()
    ): StreakStatus {
        val eval = evaluateOnLaunchOrDateChange(
            storedStreak = storedStreak,
            lastKpiCompletedDate = lastKpiCompletedDate,
            todayIso = todayIso
        )
        return StreakStatus(
            currentStreak = eval.effectiveStreak,
            lastKpiCompletedDate = if (eval.didResetDueToInactivity) "" else lastKpiCompletedDate.trim(),
            todayKpiCompleted = eval.todayKpiCompleted,
            visualStyle = visualStyle,
            daysBetweenLastKpi = eval.daysBetween,
            daysRemainingBeforeLoss = eval.daysRemainingBeforeLoss,
            graceWarningVi = eval.graceWarningVi
        )
    }
}
