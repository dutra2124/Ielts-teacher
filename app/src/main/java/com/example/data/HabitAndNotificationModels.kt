package com.example.data

import java.util.Calendar
import java.util.Locale

enum class ReminderTone(
    val id: String,
    val titleVi: String,
    val subtitleVi: String,
    val previewQuoteVi: String,
    val teacherMood: TeacherMood
) {
    GENTLE(
        id = "gentle",
        titleVi = "Nhẹ nhàng khích lệ",
        subtitleVi = "Ấm áp, tạo thói quen bền bỉ mỗi ngày",
        previewQuoteVi = "Chào bạn! Dành 15 phút nhẹ nhàng ôn từ vựng Band 8.5 cùng cô nhé!",
        teacherMood = TeacherMood.ENCOURAGING
    ),
    STRICT(
        id = "strict",
        titleVi = "Nghiêm khắc thúc đẩy Band 8.5",
        subtitleVi = "Kỷ luật cao, bám sát mục tiêu điểm số",
        previewQuoteVi = "Đến giờ học rồi! Không trì hoãn — mục tiêu Band 8.5 đòi hỏi kỷ luật mỗi ngày!",
        teacherMood = TeacherMood.TEACHING
    );

    companion object {
        fun fromId(id: String?): ReminderTone =
            entries.firstOrNull { it.id == id } ?: GENTLE
    }
}

enum class WeekdayItem(
    val id: String,
    val labelVi: String,
    val calendarDayOfWeek: Int
) {
    MON("T2", "T2", Calendar.MONDAY),
    TUE("T3", "T3", Calendar.TUESDAY),
    WED("T4", "T4", Calendar.WEDNESDAY),
    THU("T5", "T5", Calendar.THURSDAY),
    FRI("T6", "T6", Calendar.FRIDAY),
    SAT("T7", "T7", Calendar.SATURDAY),
    SUN("CN", "CN", Calendar.SUNDAY);

    companion object {
        fun today(): WeekdayItem {
            val dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            return entries.firstOrNull { it.calendarDayOfWeek == dow } ?: MON
        }

        val allIds: Set<String> = entries.map { it.id }.toSet()
    }
}

data class ReminderSlot(
    val id: String,
    val hour: Int,
    val minute: Int,
    val labelVi: String,
    val enabled: Boolean = true
) {
    fun formattedTime(): String = String.format(Locale.US, "%02d:%02d", hour.coerceIn(0, 23), minute.coerceIn(0, 59))
}

data class DailyHabitTask(
    val id: String,
    val titleVi: String,
    val subtitleVi: String
)

data class NotificationPreferences(
    val masterEnabled: Boolean = true,
    val slots: List<ReminderSlot> = DefaultHabitData.defaultSlots,
    val repeatDays: Set<String> = WeekdayItem.allIds,
    val tone: ReminderTone = ReminderTone.GENTLE
)

data class ActivePushBanner(
    val id: Long,
    val titleVi: String,
    val messageVi: String,
    val slotLabelVi: String,
    val timeLabel: String,
    val tone: ReminderTone
)

object DefaultHabitData {
    val dailyChecklist: List<DailyHabitTask> = listOf(
        DailyHabitTask(
            id = "habit_vocab_10",
            titleVi = "Học 10 từ C1–C2",
            subtitleVi = "Collocations & Paraphrasing học thuật"
        ),
        DailyHabitTask(
            id = "habit_reflex_1",
            titleVi = "1 Game phản xạ",
            subtitleVi = "Tăng tốc độ nhận diện từ khóa IELTS"
        ),
        DailyHabitTask(
            id = "habit_practice_15",
            titleVi = "15 phút Luyện đề",
            subtitleVi = "Thực chiến 4 kỹ năng chuẩn Cambridge"
        )
    )

    val defaultSlots: List<ReminderSlot> = listOf(
        ReminderSlot(
            id = "slot_morning_vocab",
            hour = 7,
            minute = 30,
            labelVi = "Nạp từ mới buổi sáng",
            enabled = true
        ),
        ReminderSlot(
            id = "slot_evening_practice",
            hour = 21,
            minute = 0,
            labelVi = "Luyện đề 4 kỹ năng",
            enabled = true
        )
    )

    fun serializeSlots(slots: List<ReminderSlot>): String {
        return slots.joinToString(separator = "\n") { slot ->
            val safeLabel = slot.labelVi.replace("|", " ").replace("\n", " ").trim()
            "${slot.id}|${slot.hour}|${slot.minute}|$safeLabel|${slot.enabled}"
        }
    }

    fun deserializeSlots(raw: String?): List<ReminderSlot> {
        if (raw == null) return defaultSlots
        if (raw.isBlank()) return emptyList()
        return raw.lines()
            .mapNotNull { line ->
                val parts = line.split("|")
                if (parts.size < 5) return@mapNotNull null
                val hour = parts[1].toIntOrNull()?.coerceIn(0, 23) ?: return@mapNotNull null
                val minute = parts[2].toIntOrNull()?.coerceIn(0, 59) ?: return@mapNotNull null
                val enabled = parts[4].toBooleanStrictOrNull() ?: true
                ReminderSlot(
                    id = parts[0],
                    hour = hour,
                    minute = minute,
                    labelVi = parts[3].ifBlank { "Nhắc học IELTS" },
                    enabled = enabled
                )
            }
            .sortedWith(compareBy<ReminderSlot> { it.hour }.thenBy { it.minute })
    }

    fun currentDateKey(): String {
        val cal = Calendar.getInstance()
        return String.format(
            Locale.US,
            "%04d-%02d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun currentWeekKey(): String {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        return "${cal.get(Calendar.YEAR)}-W${cal.get(Calendar.WEEK_OF_YEAR)}"
    }
}
