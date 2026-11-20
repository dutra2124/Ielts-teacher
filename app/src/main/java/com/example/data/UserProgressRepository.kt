package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.AppThemePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.apexProgressDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "apex_ielts_user_state"
)

data class PersistedUserState(
    val activeModuleId: Int = 1,
    val completedModuleIds: Set<Int> = emptySet(),
    val activeTabIndex: Int = StudyTab.ROADMAP.index,
    val reminderEnabled: Boolean = true,
    val streakDays: Int = 1,
    val tipCycleIndex: Int = 0,
    val completedHabitIds: Set<String> = emptySet(),
    val weeklyActiveDays: Set<String> = emptySet(),
    val notificationPrefs: NotificationPreferences = NotificationPreferences(),
    val srsWordIds: Set<String> = emptySet(),
    val learnedVocabIds: Set<String> = emptySet(),
    val customExams: List<ExamPaper> = emptyList(),
    val examHistory: List<ExamAttemptRecord> = emptyList(),
    val themePrefs: AppThemePreferences = AppThemePreferences(),
    val googleSheetUrl: String = "",
    val lastSyncedAtLabel: String = "",
    val dailySelfStudyMinutes: Int = 0,
    val autoGreetDashboardVoice: Boolean = false
)

class UserProgressRepository(private val context: Context) {

    private object Keys {
        val ACTIVE_MODULE_ID = intPreferencesKey("active_module_id")
        val COMPLETED_MODULE_IDS = stringSetPreferencesKey("completed_module_ids")
        val ACTIVE_TAB_INDEX = intPreferencesKey("active_tab_index")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val STREAK_DAYS = intPreferencesKey("streak_days")
        val TIP_CYCLE_INDEX = intPreferencesKey("tip_cycle_index")

        // Step 2: Daily Habit & Custom Notification Scheduler keys
        val COMPLETED_HABIT_IDS = stringSetPreferencesKey("completed_habit_ids")
        val COMPLETED_HABIT_DATE = stringPreferencesKey("completed_habit_date")
        val WEEKLY_ACTIVE_DAYS = stringSetPreferencesKey("weekly_active_days")
        val WEEKLY_KEY = stringPreferencesKey("weekly_key")
        val STREAK_AWARDED_DATE = stringPreferencesKey("streak_awarded_date")
        val REMINDER_SLOTS = stringPreferencesKey("reminder_slots_v1")
        val REMINDER_REPEAT_DAYS = stringSetPreferencesKey("reminder_repeat_days_v1")
        val REMINDER_TONE = stringPreferencesKey("reminder_tone_v1")

        // Step 3: Tab 2 Learn SRS Review Queue & Learned Vocabulary
        val SRS_WORD_IDS = stringSetPreferencesKey("srs_word_ids_v1")
        val LEARNED_VOCAB_IDS = stringSetPreferencesKey("learned_vocab_ids_v1")

        // Step 4: Tab 4 Exam Personal Library & Attempt Score History
        val CUSTOM_EXAMS_JSON = stringPreferencesKey("custom_exams_json_v1")
        val EXAM_HISTORY_JSON = stringPreferencesKey("exam_history_json_v1")

        // Global Theme Manager (Light / Late-Night Study / CD-IELTS Exam Dark / System)
        val THEME_MODE_ID = stringPreferencesKey("app_theme_mode_v1")
        val AUTO_DARK_EXAM_ROOM = booleanPreferencesKey("auto_dark_exam_room_v1")

        // Offline-First Hybrid Architecture: Google Sheets Cloud Sync metadata
        val GOOGLE_SHEET_URL = stringPreferencesKey("google_sheet_url_v1")
        val LAST_SYNCED_AT = stringPreferencesKey("last_synced_at_v1")

        // Personal Library Self-Study session diligence metrics
        val DAILY_SELF_STUDY_MINUTES = intPreferencesKey("daily_self_study_minutes_v1")
        val DAILY_SELF_STUDY_DATE = stringPreferencesKey("daily_self_study_date_v1")
        val AUTO_GREET_DASHBOARD_VOICE = booleanPreferencesKey("auto_greet_dashboard_voice_v1")
    }

    val userStateFlow: Flow<PersistedUserState> = context.apexProgressDataStore.data.map { prefs ->
        val rawCompleted = prefs[Keys.COMPLETED_MODULE_IDS].orEmpty()
        val parsedCompleted = rawCompleted.mapNotNull { it.toIntOrNull() }.toSet()

        val todayKey = DefaultHabitData.currentDateKey()
        val savedHabitDate = prefs[Keys.COMPLETED_HABIT_DATE]
        val todayHabits = if (savedHabitDate == todayKey) {
            prefs[Keys.COMPLETED_HABIT_IDS].orEmpty()
        } else {
            emptySet()
        }

        val currentWeek = DefaultHabitData.currentWeekKey()
        val savedWeek = prefs[Keys.WEEKLY_KEY]
        val weeklyDays = if (savedWeek == currentWeek) {
            prefs[Keys.WEEKLY_ACTIVE_DAYS].orEmpty()
        } else {
            emptySet()
        }

        val masterEnabled = prefs[Keys.REMINDER_ENABLED] ?: true
        val slots = DefaultHabitData.deserializeSlots(prefs[Keys.REMINDER_SLOTS])
        val repeatDays = prefs[Keys.REMINDER_REPEAT_DAYS] ?: WeekdayItem.allIds
        val tone = ReminderTone.fromId(prefs[Keys.REMINDER_TONE])

        PersistedUserState(
            activeModuleId = (prefs[Keys.ACTIVE_MODULE_ID] ?: 1).coerceIn(1, 12),
            completedModuleIds = parsedCompleted,
            activeTabIndex = (prefs[Keys.ACTIVE_TAB_INDEX] ?: 0).coerceIn(0, 4),
            reminderEnabled = masterEnabled,
            streakDays = (prefs[Keys.STREAK_DAYS] ?: 1).coerceAtLeast(1),
            tipCycleIndex = prefs[Keys.TIP_CYCLE_INDEX] ?: 0,
            completedHabitIds = todayHabits,
            weeklyActiveDays = weeklyDays,
            notificationPrefs = NotificationPreferences(
                masterEnabled = masterEnabled,
                slots = slots,
                repeatDays = repeatDays,
                tone = tone
            ),
            srsWordIds = prefs[Keys.SRS_WORD_IDS].orEmpty(),
            learnedVocabIds = prefs[Keys.LEARNED_VOCAB_IDS].orEmpty(),
            customExams = ExamSerialization.deserializeCustomExams(prefs[Keys.CUSTOM_EXAMS_JSON]),
            examHistory = ExamSerialization.deserializeHistory(prefs[Keys.EXAM_HISTORY_JSON]),
            themePrefs = AppThemePreferences(
                mode = AppThemeMode.fromId(prefs[Keys.THEME_MODE_ID]),
                autoDarkInExamRoom = prefs[Keys.AUTO_DARK_EXAM_ROOM] ?: true
            ),
            googleSheetUrl = prefs[Keys.GOOGLE_SHEET_URL].orEmpty(),
            lastSyncedAtLabel = prefs[Keys.LAST_SYNCED_AT].orEmpty(),
            dailySelfStudyMinutes = if (prefs[Keys.DAILY_SELF_STUDY_DATE] == todayKey) {
                (prefs[Keys.DAILY_SELF_STUDY_MINUTES] ?: 0).coerceAtLeast(0)
            } else {
                0
            },
            autoGreetDashboardVoice = prefs[Keys.AUTO_GREET_DASHBOARD_VOICE] ?: false
        )
    }

    suspend fun setActiveModuleId(moduleId: Int) {
        context.apexProgressDataStore.edit { prefs ->
            prefs[Keys.ACTIVE_MODULE_ID] = moduleId.coerceIn(1, 12)
        }
    }

    suspend fun setActiveTabIndex(tabIndex: Int) {
        context.apexProgressDataStore.edit { prefs ->
            prefs[Keys.ACTIVE_TAB_INDEX] = tabIndex.coerceIn(0, 4)
        }
    }

    suspend fun toggleModuleCompleted(moduleId: Int): Boolean {
        var nowCompleted = false
        context.apexProgressDataStore.edit { prefs ->
            val current = prefs[Keys.COMPLETED_MODULE_IDS].orEmpty().toMutableSet()
            val key = moduleId.toString()
            if (current.contains(key)) {
                current.remove(key)
                nowCompleted = false
            } else {
                current.add(key)
                nowCompleted = true
                val currentStreak = prefs[Keys.STREAK_DAYS] ?: 1
                prefs[Keys.STREAK_DAYS] = (currentStreak + 1).coerceAtMost(365)
            }
            prefs[Keys.COMPLETED_MODULE_IDS] = current
        }
        return nowCompleted
    }

    suspend fun toggleDailyHabit(habitId: String): Pair<Boolean, Boolean> {
        var isNowChecked = false
        var allCompletedNow = false
        val todayKey = DefaultHabitData.currentDateKey()
        val currentWeek = DefaultHabitData.currentWeekKey()
        val todayWeekdayId = WeekdayItem.today().id

        context.apexProgressDataStore.edit { prefs ->
            val currentHabits = if (prefs[Keys.COMPLETED_HABIT_DATE] == todayKey) {
                prefs[Keys.COMPLETED_HABIT_IDS].orEmpty().toMutableSet()
            } else {
                mutableSetOf()
            }

            if (currentHabits.contains(habitId)) {
                currentHabits.remove(habitId)
                isNowChecked = false
            } else {
                currentHabits.add(habitId)
                isNowChecked = true
            }

            prefs[Keys.COMPLETED_HABIT_DATE] = todayKey
            prefs[Keys.COMPLETED_HABIT_IDS] = currentHabits

            val currentWeekly = if (prefs[Keys.WEEKLY_KEY] == currentWeek) {
                prefs[Keys.WEEKLY_ACTIVE_DAYS].orEmpty().toMutableSet()
            } else {
                mutableSetOf()
            }
            if (currentHabits.isNotEmpty()) {
                currentWeekly.add(todayWeekdayId)
            } else {
                currentWeekly.remove(todayWeekdayId)
            }
            prefs[Keys.WEEKLY_KEY] = currentWeek
            prefs[Keys.WEEKLY_ACTIVE_DAYS] = currentWeekly

            val totalTasks = DefaultHabitData.dailyChecklist.size
            if (currentHabits.size >= totalTasks) {
                allCompletedNow = true
                if (prefs[Keys.STREAK_AWARDED_DATE] != todayKey) {
                    val currentStreak = prefs[Keys.STREAK_DAYS] ?: 1
                    prefs[Keys.STREAK_DAYS] = (currentStreak + 1).coerceAtMost(365)
                    prefs[Keys.STREAK_AWARDED_DATE] = todayKey
                }
            }
        }
        return isNowChecked to allCompletedNow
    }

    suspend fun markDailyHabitCompleted(habitId: String): Boolean {
        var newlyCompleted = false
        val todayKey = DefaultHabitData.currentDateKey()
        val currentWeek = DefaultHabitData.currentWeekKey()
        val todayWeekdayId = WeekdayItem.today().id

        context.apexProgressDataStore.edit { prefs ->
            val currentHabits = if (prefs[Keys.COMPLETED_HABIT_DATE] == todayKey) {
                prefs[Keys.COMPLETED_HABIT_IDS].orEmpty().toMutableSet()
            } else {
                mutableSetOf()
            }
            if (!currentHabits.contains(habitId)) {
                currentHabits.add(habitId)
                newlyCompleted = true
            }
            prefs[Keys.COMPLETED_HABIT_DATE] = todayKey
            prefs[Keys.COMPLETED_HABIT_IDS] = currentHabits

            val currentWeekly = if (prefs[Keys.WEEKLY_KEY] == currentWeek) {
                prefs[Keys.WEEKLY_ACTIVE_DAYS].orEmpty().toMutableSet()
            } else {
                mutableSetOf()
            }
            currentWeekly.add(todayWeekdayId)
            prefs[Keys.WEEKLY_KEY] = currentWeek
            prefs[Keys.WEEKLY_ACTIVE_DAYS] = currentWeekly

            val totalTasks = DefaultHabitData.dailyChecklist.size
            if (currentHabits.size >= totalTasks && prefs[Keys.STREAK_AWARDED_DATE] != todayKey) {
                val currentStreak = prefs[Keys.STREAK_DAYS] ?: 1
                prefs[Keys.STREAK_DAYS] = (currentStreak + 1).coerceAtMost(365)
                prefs[Keys.STREAK_AWARDED_DATE] = todayKey
            }
        }
        return newlyCompleted
    }

    suspend fun addWordToSrsQueue(wordId: String) {
        context.apexProgressDataStore.edit { prefs ->
            val updated = prefs[Keys.SRS_WORD_IDS].orEmpty().toMutableSet()
            updated.add(wordId)
            prefs[Keys.SRS_WORD_IDS] = updated
        }
    }

    suspend fun removeWordFromSrsQueue(wordId: String) {
        context.apexProgressDataStore.edit { prefs ->
            val updated = prefs[Keys.SRS_WORD_IDS].orEmpty().toMutableSet()
            updated.remove(wordId)
            prefs[Keys.SRS_WORD_IDS] = updated
        }
    }

    suspend fun markVocabWordStudied(wordId: String) {
        context.apexProgressDataStore.edit { prefs ->
            val updated = prefs[Keys.LEARNED_VOCAB_IDS].orEmpty().toMutableSet()
            updated.add(wordId)
            prefs[Keys.LEARNED_VOCAB_IDS] = updated
        }
    }

    suspend fun setMasterReminderEnabled(enabled: Boolean) {
        context.apexProgressDataStore.edit { prefs ->
            prefs[Keys.REMINDER_ENABLED] = enabled
        }
    }

    suspend fun saveReminderSlot(slot: ReminderSlot) {
        context.apexProgressDataStore.edit { prefs ->
            val current = DefaultHabitData.deserializeSlots(prefs[Keys.REMINDER_SLOTS]).toMutableList()
            val idx = current.indexOfFirst { it.id == slot.id }
            if (idx >= 0) {
                current[idx] = slot
            } else {
                current.add(slot)
            }
            val sorted = current.sortedWith(compareBy<ReminderSlot> { it.hour }.thenBy { it.minute })
            prefs[Keys.REMINDER_SLOTS] = DefaultHabitData.serializeSlots(sorted)
        }
    }

    suspend fun toggleReminderSlot(slotId: String) {
        context.apexProgressDataStore.edit { prefs ->
            val current = DefaultHabitData.deserializeSlots(prefs[Keys.REMINDER_SLOTS]).map { slot ->
                if (slot.id == slotId) slot.copy(enabled = !slot.enabled) else slot
            }
            prefs[Keys.REMINDER_SLOTS] = DefaultHabitData.serializeSlots(current)
        }
    }

    suspend fun deleteReminderSlot(slotId: String) {
        context.apexProgressDataStore.edit { prefs ->
            val current = DefaultHabitData.deserializeSlots(prefs[Keys.REMINDER_SLOTS])
                .filterNot { it.id == slotId }
            prefs[Keys.REMINDER_SLOTS] = DefaultHabitData.serializeSlots(current)
        }
    }

    suspend fun toggleRepeatDay(dayId: String) {
        context.apexProgressDataStore.edit { prefs ->
            val current = (prefs[Keys.REMINDER_REPEAT_DAYS] ?: WeekdayItem.allIds).toMutableSet()
            if (current.contains(dayId)) {
                // Keep at least one repeat day active so schedule remains valid
                if (current.size > 1) {
                    current.remove(dayId)
                }
            } else {
                current.add(dayId)
            }
            prefs[Keys.REMINDER_REPEAT_DAYS] = current
        }
    }

    suspend fun setReminderTone(tone: ReminderTone) {
        context.apexProgressDataStore.edit { prefs ->
            prefs[Keys.REMINDER_TONE] = tone.id
        }
    }

    suspend fun incrementTipCycle() {
        context.apexProgressDataStore.edit { prefs ->
            val current = prefs[Keys.TIP_CYCLE_INDEX] ?: 0
            prefs[Keys.TIP_CYCLE_INDEX] = current + 1
        }
    }

    suspend fun saveCustomExam(exam: ExamPaper) {
        context.apexProgressDataStore.edit { prefs ->
            val current = ExamSerialization.deserializeCustomExams(prefs[Keys.CUSTOM_EXAMS_JSON]).toMutableList()
            val idx = current.indexOfFirst { it.id == exam.id }
            if (idx >= 0) current[idx] = exam else current.add(0, exam)
            prefs[Keys.CUSTOM_EXAMS_JSON] = ExamSerialization.serializeCustomExams(current)
        }
    }

    suspend fun deleteCustomExam(examId: String) {
        context.apexProgressDataStore.edit { prefs ->
            val current = ExamSerialization.deserializeCustomExams(prefs[Keys.CUSTOM_EXAMS_JSON])
                .filterNot { it.id == examId }
            prefs[Keys.CUSTOM_EXAMS_JSON] = ExamSerialization.serializeCustomExams(current)
        }
    }

    suspend fun recordExamAttempt(attempt: ExamAttemptRecord) {
        context.apexProgressDataStore.edit { prefs ->
            val current = ExamSerialization.deserializeHistory(prefs[Keys.EXAM_HISTORY_JSON]).toMutableList()
            current.add(0, attempt)
            prefs[Keys.EXAM_HISTORY_JSON] = ExamSerialization.serializeHistory(current.take(25))
        }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.apexProgressDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE_ID] = mode.id
        }
    }

    suspend fun setAutoDarkInExamRoom(enabled: Boolean) {
        context.apexProgressDataStore.edit { prefs ->
            prefs[Keys.AUTO_DARK_EXAM_ROOM] = enabled
        }
    }

    suspend fun setGoogleSheetUrl(url: String) {
        context.apexProgressDataStore.edit { prefs ->
            prefs[Keys.GOOGLE_SHEET_URL] = url.trim()
        }
    }

    suspend fun setLastSyncedAt(label: String) {
        context.apexProgressDataStore.edit { prefs ->
            prefs[Keys.LAST_SYNCED_AT] = label
        }
    }

    /**
     * Logs self-study minutes for today and feeds directly into daily diligence metrics
     * (marks today active on the 7-day calendar and completes the 15-min practice habit once >= 10 mins).
     */
    suspend fun recordSelfStudyMinutes(addedMinutes: Int): Int {
        val safeMinutes = addedMinutes.coerceIn(1, 300)
        val todayKey = DefaultHabitData.currentDateKey()
        val currentWeek = DefaultHabitData.currentWeekKey()
        val todayWeekdayId = WeekdayItem.today().id
        var updatedTotal = safeMinutes

        context.apexProgressDataStore.edit { prefs ->
            val currentMinutes = if (prefs[Keys.DAILY_SELF_STUDY_DATE] == todayKey) {
                prefs[Keys.DAILY_SELF_STUDY_MINUTES] ?: 0
            } else {
                0
            }
            updatedTotal = (currentMinutes + safeMinutes).coerceAtMost(999)
            prefs[Keys.DAILY_SELF_STUDY_DATE] = todayKey
            prefs[Keys.DAILY_SELF_STUDY_MINUTES] = updatedTotal

            val currentWeekly = if (prefs[Keys.WEEKLY_KEY] == currentWeek) {
                prefs[Keys.WEEKLY_ACTIVE_DAYS].orEmpty().toMutableSet()
            } else {
                mutableSetOf()
            }
            currentWeekly.add(todayWeekdayId)
            prefs[Keys.WEEKLY_KEY] = currentWeek
            prefs[Keys.WEEKLY_ACTIVE_DAYS] = currentWeekly

            if (updatedTotal >= 10) {
                val currentHabits = if (prefs[Keys.COMPLETED_HABIT_DATE] == todayKey) {
                    prefs[Keys.COMPLETED_HABIT_IDS].orEmpty().toMutableSet()
                } else {
                    mutableSetOf()
                }
                currentHabits.add("habit_practice_15")
                prefs[Keys.COMPLETED_HABIT_DATE] = todayKey
                prefs[Keys.COMPLETED_HABIT_IDS] = currentHabits

                val totalTasks = DefaultHabitData.dailyChecklist.size
                if (currentHabits.size >= totalTasks && prefs[Keys.STREAK_AWARDED_DATE] != todayKey) {
                    val currentStreak = prefs[Keys.STREAK_DAYS] ?: 1
                    prefs[Keys.STREAK_DAYS] = (currentStreak + 1).coerceAtMost(365)
                    prefs[Keys.STREAK_AWARDED_DATE] = todayKey
                }
            }
        }
        return updatedTotal
    }

    suspend fun setAutoGreetDashboardVoice(enabled: Boolean) {
        context.apexProgressDataStore.edit { prefs ->
            prefs[Keys.AUTO_GREET_DASHBOARD_VOICE] = enabled
        }
    }
}
