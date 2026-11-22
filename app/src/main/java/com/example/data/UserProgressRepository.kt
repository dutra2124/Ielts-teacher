package com.example.data

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import com.example.auth.AuthMethod
import com.example.auth.LocalCryptoHelper
import com.example.auth.LocalRegisteredAccount
import com.example.auth.SecurityPreferences
import com.example.auth.UserAccountProfile
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.AppThemePreferences
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.AppWallpaperPattern
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.apexProgressDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "apex_ielts_user_state"
)

data class PersistedUserState(
    val activeModuleId: Int = 1,
    val completedModuleIds: Set<Int> = emptySet(),
    val activeTabIndex: Int = StudyTab.ROADMAP.index,
    val reminderEnabled: Boolean = true,
    val streakDays: Int = 0,
    val lastKpiCompletedDate: String = "",
    val streakVisualStyle: StreakVisualStyle = StreakVisualStyle.TREE,
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
    val autoGreetDashboardVoice: Boolean = false,
    val personalGeminiApiKey: String = "",
    val googleCseApiKey: String = "",
    val googleCseCx: String = "",
    // Authentication & Account Security
    val userProfile: UserAccountProfile = UserAccountProfile(),
    val securityPrefs: SecurityPreferences = SecurityPreferences()
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
        val LAST_KPI_COMPLETED_DATE = stringPreferencesKey("last_kpi_completed_date_v2")
        val STREAK_VISUAL_STYLE = stringPreferencesKey("streak_visual_style_v1")
        val REMINDER_SLOTS = stringPreferencesKey("reminder_slots_v1")
        val REMINDER_REPEAT_DAYS = stringSetPreferencesKey("reminder_repeat_days_v1")
        val TEST_REMINDER_SLOTS = stringPreferencesKey("test_reminder_slots_v1")
        val TEST_REPEAT_DAYS = stringSetPreferencesKey("test_repeat_days_v1")
        val REMINDER_TONE = stringPreferencesKey("reminder_tone_v1")

        // Step 3: Tab 2 Learn SRS Review Queue & Learned Vocabulary
        val SRS_WORD_IDS = stringSetPreferencesKey("srs_word_ids_v1")
        val LEARNED_VOCAB_IDS = stringSetPreferencesKey("learned_vocab_ids_v1")

        // Step 4: Tab 4 Exam Personal Library & Attempt Score History
        val CUSTOM_EXAMS_JSON = stringPreferencesKey("custom_exams_json_v1")
        val EXAM_HISTORY_JSON = stringPreferencesKey("exam_history_json_v1")

        // Global Theme Manager (High-Contrast Presets, Custom Colors, Wallpaper & Exam Dark Mode)
        val THEME_MODE_ID = stringPreferencesKey("app_theme_mode_v1")
        val AUTO_DARK_EXAM_ROOM = booleanPreferencesKey("auto_dark_exam_room_v1")
        val THEME_PRESET_ID = stringPreferencesKey("app_theme_preset_v2")
        val THEME_CUSTOM_PRIMARY_HEX = stringPreferencesKey("app_theme_custom_primary_hex_v2")
        val THEME_CUSTOM_SURFACE_HEX = stringPreferencesKey("app_theme_custom_surface_hex_v2")
        val THEME_WALLPAPER_PATTERN_ID = stringPreferencesKey("app_theme_wallpaper_pattern_v2")
        val THEME_CUSTOM_BG_PATH = stringPreferencesKey("app_theme_custom_bg_path_v2")
        val THEME_BG_OVERLAY_OPACITY = floatPreferencesKey("app_theme_bg_overlay_opacity_v2")

        // Offline-First Hybrid Architecture: Google Sheets Cloud Sync metadata
        val GOOGLE_SHEET_URL = stringPreferencesKey("google_sheet_url_v1")
        val LAST_SYNCED_AT = stringPreferencesKey("last_synced_at_v1")

        // Personal Library Self-Study session diligence metrics
        val DAILY_SELF_STUDY_MINUTES = intPreferencesKey("daily_self_study_minutes_v1")
        val DAILY_SELF_STUDY_DATE = stringPreferencesKey("daily_self_study_date_v1")
        val AUTO_GREET_DASHBOARD_VOICE = booleanPreferencesKey("auto_greet_dashboard_voice_v1")
        val PERSONAL_GEMINI_API_KEY = stringPreferencesKey("personal_gemini_api_key_v1")
        val GOOGLE_CSE_API_KEY = stringPreferencesKey("google_cse_api_key_v1")
        val GOOGLE_CSE_CX = stringPreferencesKey("google_cse_cx_v1")

        // Authentication & Local Account Session
        val AUTH_IS_LOGGED_IN = booleanPreferencesKey("auth_is_logged_in_v1")
        val AUTH_UID = stringPreferencesKey("auth_uid_v1")
        val AUTH_DISPLAY_NAME = stringPreferencesKey("auth_display_name_v1")
        val AUTH_EMAIL = stringPreferencesKey("auth_email_v1")
        val AUTH_METHOD = stringPreferencesKey("auth_method_v1")
        val AUTH_GOOGLE_CONNECTED = booleanPreferencesKey("auth_google_connected_v1")
        val AUTH_GOOGLE_FALLBACK = booleanPreferencesKey("auth_google_fallback_v1")
        val AUTH_CREATED_AT = stringPreferencesKey("auth_created_at_v1")
        val AUTH_AVATAR_PRESET_ID = stringPreferencesKey("auth_avatar_preset_id_v1")
        val AUTH_CUSTOM_AVATAR_PATH = stringPreferencesKey("auth_custom_avatar_path_v1")
        val AUTH_AVATAR_VERSION = longPreferencesKey("auth_avatar_version_v1")
        val REGISTERED_ACCOUNTS_JSON = stringPreferencesKey("registered_accounts_json_v1")

        // Security & App Lock (4-digit salted PBKDF2 PIN + Biometric option)
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled_v1")
        val APP_LOCK_PIN_SALT = stringPreferencesKey("app_lock_pin_salt_v1")
        val APP_LOCK_PIN_HASH = stringPreferencesKey("app_lock_pin_hash_v1")
        val BIOMETRIC_LOCK_ENABLED = booleanPreferencesKey("biometric_lock_enabled_v1")
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
        val slots = DefaultHabitData.deserializeSlots(prefs[Keys.REMINDER_SLOTS], DefaultHabitData.defaultSlots)
        val repeatDays = prefs[Keys.REMINDER_REPEAT_DAYS] ?: WeekdayItem.allIds
        val testSlots = DefaultHabitData.deserializeSlots(prefs[Keys.TEST_REMINDER_SLOTS], DefaultHabitData.defaultTestSlots)
        val testRepeatDays = prefs[Keys.TEST_REPEAT_DAYS] ?: DefaultHabitData.defaultTestRepeatDays
        val tone = ReminderTone.fromId(prefs[Keys.REMINDER_TONE])

        val storedStreak = (prefs[Keys.STREAK_DAYS] ?: 0).coerceAtLeast(0)
        val lastKpiDate = (prefs[Keys.LAST_KPI_COMPLETED_DATE] ?: prefs[Keys.STREAK_AWARDED_DATE]).orEmpty().trim()
        val streakStyle = StreakVisualStyle.fromId(prefs[Keys.STREAK_VISUAL_STYLE])
        val evaluatedStreak = StreakManager.evaluateOnLaunchOrDateChange(
            storedStreak = storedStreak,
            lastKpiCompletedDate = lastKpiDate,
            todayIso = todayKey
        )

        PersistedUserState(
            activeModuleId = (prefs[Keys.ACTIVE_MODULE_ID] ?: 1).coerceIn(1, 12),
            completedModuleIds = parsedCompleted,
            activeTabIndex = (prefs[Keys.ACTIVE_TAB_INDEX] ?: 0).coerceIn(0, 4),
            reminderEnabled = masterEnabled,
            streakDays = evaluatedStreak.effectiveStreak,
            lastKpiCompletedDate = if (evaluatedStreak.didResetDueToInactivity) "" else lastKpiDate,
            streakVisualStyle = streakStyle,
            tipCycleIndex = prefs[Keys.TIP_CYCLE_INDEX] ?: 0,
            completedHabitIds = todayHabits,
            weeklyActiveDays = weeklyDays,
            notificationPrefs = NotificationPreferences(
                masterEnabled = masterEnabled,
                slots = slots,
                repeatDays = repeatDays,
                testSlots = testSlots,
                testRepeatDays = testRepeatDays,
                tone = tone
            ),
            srsWordIds = prefs[Keys.SRS_WORD_IDS].orEmpty(),
            learnedVocabIds = prefs[Keys.LEARNED_VOCAB_IDS].orEmpty(),
            customExams = ExamSerialization.deserializeCustomExams(prefs[Keys.CUSTOM_EXAMS_JSON]),
            examHistory = ExamSerialization.deserializeHistory(prefs[Keys.EXAM_HISTORY_JSON]),
            themePrefs = AppThemePreferences(
                mode = AppThemeMode.fromId(prefs[Keys.THEME_MODE_ID]),
                autoDarkInExamRoom = prefs[Keys.AUTO_DARK_EXAM_ROOM] ?: true,
                preset = AppThemePreset.fromId(prefs[Keys.THEME_PRESET_ID]),
                customPrimaryHex = prefs[Keys.THEME_CUSTOM_PRIMARY_HEX]?.toLongOrNull(),
                customSurfaceHex = prefs[Keys.THEME_CUSTOM_SURFACE_HEX]?.toLongOrNull(),
                wallpaperPattern = AppWallpaperPattern.fromId(prefs[Keys.THEME_WALLPAPER_PATTERN_ID]),
                customBackgroundPath = prefs[Keys.THEME_CUSTOM_BG_PATH].orEmpty(),
                backgroundOverlayOpacity = (prefs[Keys.THEME_BG_OVERLAY_OPACITY] ?: 0.35f).coerceIn(0f, 0.85f)
            ),
            googleSheetUrl = prefs[Keys.GOOGLE_SHEET_URL].orEmpty(),
            lastSyncedAtLabel = prefs[Keys.LAST_SYNCED_AT].orEmpty(),
            dailySelfStudyMinutes = if (prefs[Keys.DAILY_SELF_STUDY_DATE] == todayKey) {
                (prefs[Keys.DAILY_SELF_STUDY_MINUTES] ?: 0).coerceAtLeast(0)
            } else {
                0
            },
            autoGreetDashboardVoice = prefs[Keys.AUTO_GREET_DASHBOARD_VOICE] ?: false,
            personalGeminiApiKey = prefs[Keys.PERSONAL_GEMINI_API_KEY].orEmpty(),
            googleCseApiKey = prefs[Keys.GOOGLE_CSE_API_KEY].orEmpty(),
            googleCseCx = prefs[Keys.GOOGLE_CSE_CX].orEmpty(),
            userProfile = UserAccountProfile(
                isLoggedIn = prefs[Keys.AUTH_IS_LOGGED_IN] ?: false,
                uid = prefs[Keys.AUTH_UID].orEmpty(),
                displayName = prefs[Keys.AUTH_DISPLAY_NAME].orEmpty(),
                email = prefs[Keys.AUTH_EMAIL].orEmpty(),
                authMethod = AuthMethod.fromId(prefs[Keys.AUTH_METHOD]),
                isGoogleConnected = prefs[Keys.AUTH_GOOGLE_CONNECTED] ?: false,
                isGoogleFallbackMode = prefs[Keys.AUTH_GOOGLE_FALLBACK] ?: false,
                createdAtEpochMs = prefs[Keys.AUTH_CREATED_AT]?.toLongOrNull() ?: 0L,
                avatarPresetId = prefs[Keys.AUTH_AVATAR_PRESET_ID].orEmpty(),
                customAvatarPath = prefs[Keys.AUTH_CUSTOM_AVATAR_PATH].orEmpty(),
                avatarVersion = prefs[Keys.AUTH_AVATAR_VERSION] ?: 0L
            ),
            securityPrefs = SecurityPreferences(
                appLockEnabled = prefs[Keys.APP_LOCK_ENABLED] ?: false,
                hasPinConfigured = !prefs[Keys.APP_LOCK_PIN_HASH].isNullOrBlank(),
                biometricEnabled = prefs[Keys.BIOMETRIC_LOCK_ENABLED] ?: false
            )
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
            }
            prefs[Keys.COMPLETED_MODULE_IDS] = current
        }
        return nowCompleted
    }

    suspend fun evaluateAndSyncStreakForToday(todayIso: String = StreakManager.todayIsoDate()): StreakLaunchEvaluation {
        var result = StreakManager.evaluateOnLaunchOrDateChange(0, "", todayIso)
        context.apexProgressDataStore.edit { prefs ->
            val storedStreak = (prefs[Keys.STREAK_DAYS] ?: 0).coerceAtLeast(0)
            val lastDate = (prefs[Keys.LAST_KPI_COMPLETED_DATE] ?: prefs[Keys.STREAK_AWARDED_DATE]).orEmpty().trim()
            result = StreakManager.evaluateOnLaunchOrDateChange(
                storedStreak = storedStreak,
                lastKpiCompletedDate = lastDate,
                todayIso = todayIso
            )
            if (result.shouldPersistReset) {
                prefs[Keys.STREAK_DAYS] = 0
                if (result.didResetDueToInactivity) {
                    prefs[Keys.LAST_KPI_COMPLETED_DATE] = ""
                    prefs[Keys.STREAK_AWARDED_DATE] = ""
                }
            }
        }
        return result
    }

    suspend fun setStreakVisualStyle(style: StreakVisualStyle) {
        context.apexProgressDataStore.edit { prefs ->
            prefs[Keys.STREAK_VISUAL_STYLE] = style.id
        }
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
                val storedStreak = (prefs[Keys.STREAK_DAYS] ?: 0).coerceAtLeast(0)
                val lastDate = (prefs[Keys.LAST_KPI_COMPLETED_DATE] ?: prefs[Keys.STREAK_AWARDED_DATE]).orEmpty()
                val outcome = StreakManager.applyDailyKpiCompletion(
                    storedStreak = storedStreak,
                    lastKpiCompletedDate = lastDate,
                    todayIso = todayKey
                )
                if (outcome.didIncrementToday) {
                    prefs[Keys.STREAK_DAYS] = outcome.newStreak
                    prefs[Keys.LAST_KPI_COMPLETED_DATE] = outcome.newLastKpiDateIso
                    prefs[Keys.STREAK_AWARDED_DATE] = outcome.newLastKpiDateIso
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
            if (currentHabits.size >= totalTasks) {
                val storedStreak = (prefs[Keys.STREAK_DAYS] ?: 0).coerceAtLeast(0)
                val lastDate = (prefs[Keys.LAST_KPI_COMPLETED_DATE] ?: prefs[Keys.STREAK_AWARDED_DATE]).orEmpty()
                val outcome = StreakManager.applyDailyKpiCompletion(
                    storedStreak = storedStreak,
                    lastKpiCompletedDate = lastDate,
                    todayIso = todayKey
                )
                if (outcome.didIncrementToday) {
                    prefs[Keys.STREAK_DAYS] = outcome.newStreak
                    prefs[Keys.LAST_KPI_COMPLETED_DATE] = outcome.newLastKpiDateIso
                    prefs[Keys.STREAK_AWARDED_DATE] = outcome.newLastKpiDateIso
                }
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

    suspend fun saveTestReminderSlot(slot: ReminderSlot) {
        context.apexProgressDataStore.edit { prefs ->
            val current = DefaultHabitData.deserializeSlots(
                prefs[Keys.TEST_REMINDER_SLOTS],
                DefaultHabitData.defaultTestSlots
            ).toMutableList()
            val idx = current.indexOfFirst { it.id == slot.id }
            if (idx >= 0) {
                current[idx] = slot
            } else {
                current.add(slot)
            }
            val sorted = current.sortedWith(compareBy<ReminderSlot> { it.hour }.thenBy { it.minute })
            prefs[Keys.TEST_REMINDER_SLOTS] = DefaultHabitData.serializeSlots(sorted)
        }
    }

    suspend fun toggleTestReminderSlot(slotId: String) {
        context.apexProgressDataStore.edit { prefs ->
            val current = DefaultHabitData.deserializeSlots(
                prefs[Keys.TEST_REMINDER_SLOTS],
                DefaultHabitData.defaultTestSlots
            ).map { slot ->
                if (slot.id == slotId) slot.copy(enabled = !slot.enabled) else slot
            }
            prefs[Keys.TEST_REMINDER_SLOTS] = DefaultHabitData.serializeSlots(current)
        }
    }

    suspend fun deleteTestReminderSlot(slotId: String) {
        context.apexProgressDataStore.edit { prefs ->
            val current = DefaultHabitData.deserializeSlots(
                prefs[Keys.TEST_REMINDER_SLOTS],
                DefaultHabitData.defaultTestSlots
            ).filterNot { it.id == slotId }
            prefs[Keys.TEST_REMINDER_SLOTS] = DefaultHabitData.serializeSlots(current)
        }
    }

    suspend fun toggleTestRepeatDay(dayId: String) {
        context.apexProgressDataStore.edit { prefs ->
            val current = (prefs[Keys.TEST_REPEAT_DAYS] ?: DefaultHabitData.defaultTestRepeatDays).toMutableSet()
            if (current.contains(dayId)) {
                if (current.size > 1) {
                    current.remove(dayId)
                }
            } else {
                current.add(dayId)
            }
            prefs[Keys.TEST_REPEAT_DAYS] = current
        }
    }

    suspend fun selectAvatarPreset(presetId: String) {
        context.apexProgressDataStore.edit { prefs ->
            prefs[Keys.AUTH_AVATAR_PRESET_ID] = presetId
            prefs[Keys.AUTH_CUSTOM_AVATAR_PATH] = ""
            prefs[Keys.AUTH_AVATAR_VERSION] = System.currentTimeMillis()
        }
    }

    suspend fun saveCustomAvatarFromUri(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val destFile = File(context.filesDir, "user_avatar.jpg")
            val tempFile = File(context.filesDir, "user_avatar_tmp.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: error("Không thể đọc tệp ảnh đã chọn.")
            if (tempFile.length() <= 0L) {
                tempFile.delete()
                error("Ảnh đã chọn bị rỗng hoặc không hợp lệ.")
            }
            if (destFile.exists()) {
                destFile.delete()
            }
            if (!tempFile.renameTo(destFile)) {
                tempFile.copyTo(destFile, overwrite = true)
                tempFile.delete()
            }
            val absolutePath = destFile.absolutePath
            context.apexProgressDataStore.edit { prefs ->
                prefs[Keys.AUTH_CUSTOM_AVATAR_PATH] = absolutePath
                prefs[Keys.AUTH_AVATAR_PRESET_ID] = ""
                prefs[Keys.AUTH_AVATAR_VERSION] = System.currentTimeMillis()
            }
            absolutePath
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
            when (mode) {
                AppThemeMode.LIGHT -> prefs[Keys.THEME_PRESET_ID] = AppThemePreset.INDIGO_SHARP.id
                AppThemeMode.NIGHT_STUDY -> prefs[Keys.THEME_PRESET_ID] = AppThemePreset.DARK_ANTI_GLARE.id
                AppThemeMode.CD_IELTS_DARK -> prefs[Keys.THEME_PRESET_ID] = AppThemePreset.DARK_ANTI_GLARE.id
                AppThemeMode.SYSTEM -> Unit
            }
            prefs.remove(Keys.THEME_CUSTOM_SURFACE_HEX)
        }
    }

    suspend fun setAutoDarkInExamRoom(enabled: Boolean) {
        context.apexProgressDataStore.edit { prefs ->
            prefs[Keys.AUTO_DARK_EXAM_ROOM] = enabled
        }
    }

    suspend fun setThemePreset(preset: AppThemePreset) {
        context.apexProgressDataStore.edit { prefs ->
            prefs[Keys.THEME_PRESET_ID] = preset.id
            prefs[Keys.THEME_MODE_ID] = preset.linkedMode.id
            prefs.remove(Keys.THEME_CUSTOM_PRIMARY_HEX)
            prefs.remove(Keys.THEME_CUSTOM_SURFACE_HEX)
        }
    }

    suspend fun setCustomPrimaryColorHex(colorHex: Long?) {
        context.apexProgressDataStore.edit { prefs ->
            if (colorHex == null) {
                prefs.remove(Keys.THEME_CUSTOM_PRIMARY_HEX)
            } else {
                prefs[Keys.THEME_CUSTOM_PRIMARY_HEX] = colorHex.toString()
            }
        }
    }

    suspend fun setCustomSurfaceColorHex(colorHex: Long?) {
        context.apexProgressDataStore.edit { prefs ->
            if (colorHex == null) {
                prefs.remove(Keys.THEME_CUSTOM_SURFACE_HEX)
            } else {
                prefs[Keys.THEME_CUSTOM_SURFACE_HEX] = colorHex.toString()
            }
        }
    }

    suspend fun setWallpaperPattern(pattern: AppWallpaperPattern) {
        context.apexProgressDataStore.edit { prefs ->
            prefs[Keys.THEME_WALLPAPER_PATTERN_ID] = pattern.id
            prefs[Keys.THEME_CUSTOM_BG_PATH] = ""
        }
    }

    suspend fun setCustomBackgroundPath(path: String) {
        context.apexProgressDataStore.edit { prefs ->
            prefs[Keys.THEME_CUSTOM_BG_PATH] = path
            if (path.isNotBlank()) {
                prefs[Keys.THEME_WALLPAPER_PATTERN_ID] = AppWallpaperPattern.NONE.id
            }
        }
    }

    suspend fun setBackgroundOverlayOpacity(opacity: Float) {
        context.apexProgressDataStore.edit { prefs ->
            prefs[Keys.THEME_BG_OVERLAY_OPACITY] = opacity.coerceIn(0f, 0.85f)
        }
    }

    suspend fun resetThemeCustomization() {
        context.apexProgressDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE_ID] = AppThemeMode.LIGHT.id
            prefs[Keys.THEME_PRESET_ID] = AppThemePreset.INDIGO_SHARP.id
            prefs.remove(Keys.THEME_CUSTOM_PRIMARY_HEX)
            prefs.remove(Keys.THEME_CUSTOM_SURFACE_HEX)
            prefs[Keys.THEME_WALLPAPER_PATTERN_ID] = AppWallpaperPattern.NONE.id
            prefs[Keys.THEME_CUSTOM_BG_PATH] = ""
            prefs[Keys.THEME_BG_OVERLAY_OPACITY] = 0.35f
            prefs[Keys.AUTO_DARK_EXAM_ROOM] = true
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
                if (currentHabits.size >= totalTasks) {
                    val storedStreak = (prefs[Keys.STREAK_DAYS] ?: 0).coerceAtLeast(0)
                    val lastDate = (prefs[Keys.LAST_KPI_COMPLETED_DATE] ?: prefs[Keys.STREAK_AWARDED_DATE]).orEmpty()
                    val outcome = StreakManager.applyDailyKpiCompletion(
                        storedStreak = storedStreak,
                        lastKpiCompletedDate = lastDate,
                        todayIso = todayKey
                    )
                    if (outcome.didIncrementToday) {
                        prefs[Keys.STREAK_DAYS] = outcome.newStreak
                        prefs[Keys.LAST_KPI_COMPLETED_DATE] = outcome.newLastKpiDateIso
                        prefs[Keys.STREAK_AWARDED_DATE] = outcome.newLastKpiDateIso
                    }
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

    suspend fun setPersonalGeminiApiKey(apiKey: String) {
        context.apexProgressDataStore.edit { prefs ->
            prefs[Keys.PERSONAL_GEMINI_API_KEY] = apiKey.trim()
        }
    }

    suspend fun saveGoogleCseConfig(apiKey: String, cx: String) {
        context.apexProgressDataStore.edit { prefs ->
            prefs[Keys.GOOGLE_CSE_API_KEY] = apiKey.trim()
            prefs[Keys.GOOGLE_CSE_CX] = cx.trim()
        }
    }

    // =========================================================================
    // Authentication & Local Account Session Persistence
    // =========================================================================

    suspend fun registerLocalAccount(
        displayName: String,
        rawEmail: String,
        password: String
    ): Result<UserAccountProfile> {
        val email = LocalCryptoHelper.normalizeEmail(rawEmail)
        val cleanName = displayName.trim().ifBlank { email.substringBefore("@") }
        if (!LocalCryptoHelper.isValidEmail(email)) {
            return Result.failure(IllegalArgumentException("Email không hợp lệ. Vui lòng kiểm tra lại."))
        }
        if (!LocalCryptoHelper.isValidPassword(password)) {
            return Result.failure(IllegalArgumentException("Mật khẩu cần tối thiểu 6 ký tự."))
        }

        var errorMsg: String? = null
        var createdProfile: UserAccountProfile? = null

        context.apexProgressDataStore.edit { prefs ->
            val accounts = LocalCryptoHelper.deserializeAccounts(prefs[Keys.REGISTERED_ACCOUNTS_JSON])
                .toMutableList()
            if (accounts.any { it.email.equals(email, ignoreCase = true) }) {
                errorMsg = "Email này đã được đăng ký. Hãy chuyển sang tab Đăng nhập."
                return@edit
            }
            val (salt, hash) = LocalCryptoHelper.hashSecret(password)
            val now = System.currentTimeMillis()
            accounts.add(
                LocalRegisteredAccount(
                    email = email,
                    displayName = cleanName,
                    saltB64 = salt,
                    passwordHashB64 = hash,
                    createdAtEpochMs = now
                )
            )
            prefs[Keys.REGISTERED_ACCOUNTS_JSON] = LocalCryptoHelper.serializeAccounts(accounts)
            val uid = "local_${email.hashCode().toUInt()}"
            prefs[Keys.AUTH_IS_LOGGED_IN] = true
            prefs[Keys.AUTH_UID] = uid
            prefs[Keys.AUTH_DISPLAY_NAME] = cleanName
            prefs[Keys.AUTH_EMAIL] = email
            prefs[Keys.AUTH_METHOD] = AuthMethod.LOCAL_EMAIL.id
            prefs[Keys.AUTH_GOOGLE_CONNECTED] = false
            prefs[Keys.AUTH_GOOGLE_FALLBACK] = false
            prefs[Keys.AUTH_CREATED_AT] = now.toString()

            createdProfile = UserAccountProfile(
                isLoggedIn = true,
                uid = uid,
                displayName = cleanName,
                email = email,
                authMethod = AuthMethod.LOCAL_EMAIL,
                isGoogleConnected = false,
                isGoogleFallbackMode = false,
                createdAtEpochMs = now
            )
        }

        val err = errorMsg
        return if (err != null) {
            Result.failure(IllegalStateException(err))
        } else {
            Result.success(createdProfile!!)
        }
    }

    suspend fun loginLocalAccount(
        rawEmail: String,
        password: String
    ): Result<UserAccountProfile> {
        val email = LocalCryptoHelper.normalizeEmail(rawEmail)
        if (!LocalCryptoHelper.isValidEmail(email)) {
            return Result.failure(IllegalArgumentException("Email không hợp lệ."))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Vui lòng nhập mật khẩu."))
        }

        var errorMsg: String? = null
        var signedInProfile: UserAccountProfile? = null

        context.apexProgressDataStore.edit { prefs ->
            val accounts = LocalCryptoHelper.deserializeAccounts(prefs[Keys.REGISTERED_ACCOUNTS_JSON])
            val found = accounts.firstOrNull { it.email.equals(email, ignoreCase = true) }
            if (found == null) {
                errorMsg = "Không tìm thấy tài khoản với email này. Hãy chọn Đăng ký tài khoản mới."
                return@edit
            }
            val matches = LocalCryptoHelper.verifySecret(password, found.saltB64, found.passwordHashB64)
            if (!matches) {
                errorMsg = "Mật khẩu chưa chính xác. Vui lòng thử lại."
                return@edit
            }
            val uid = "local_${found.email.hashCode().toUInt()}"
            prefs[Keys.AUTH_IS_LOGGED_IN] = true
            prefs[Keys.AUTH_UID] = uid
            prefs[Keys.AUTH_DISPLAY_NAME] = found.displayName
            prefs[Keys.AUTH_EMAIL] = found.email
            prefs[Keys.AUTH_METHOD] = AuthMethod.LOCAL_EMAIL.id
            prefs[Keys.AUTH_GOOGLE_CONNECTED] = false
            prefs[Keys.AUTH_GOOGLE_FALLBACK] = false
            prefs[Keys.AUTH_CREATED_AT] = found.createdAtEpochMs.toString()

            signedInProfile = UserAccountProfile(
                isLoggedIn = true,
                uid = uid,
                displayName = found.displayName,
                email = found.email,
                authMethod = AuthMethod.LOCAL_EMAIL,
                isGoogleConnected = false,
                isGoogleFallbackMode = false,
                createdAtEpochMs = found.createdAtEpochMs
            )
        }

        val err = errorMsg
        return if (err != null) {
            Result.failure(IllegalArgumentException(err))
        } else {
            Result.success(signedInProfile!!)
        }
    }

    suspend fun saveGoogleSignInSession(
        uid: String,
        displayName: String,
        email: String,
        isFallbackMode: Boolean
    ): UserAccountProfile {
        val cleanEmail = LocalCryptoHelper.normalizeEmail(email).ifBlank { "learner@masterielts.app" }
        val cleanName = displayName.trim().ifBlank { cleanEmail.substringBefore("@") }
        val now = System.currentTimeMillis()

        context.apexProgressDataStore.edit { prefs ->
            prefs[Keys.AUTH_IS_LOGGED_IN] = true
            prefs[Keys.AUTH_UID] = uid.ifBlank { "google_${cleanEmail.hashCode().toUInt()}" }
            prefs[Keys.AUTH_DISPLAY_NAME] = cleanName
            prefs[Keys.AUTH_EMAIL] = cleanEmail
            prefs[Keys.AUTH_METHOD] = AuthMethod.GOOGLE.id
            prefs[Keys.AUTH_GOOGLE_CONNECTED] = true
            prefs[Keys.AUTH_GOOGLE_FALLBACK] = isFallbackMode
            prefs[Keys.AUTH_CREATED_AT] = now.toString()
        }

        return UserAccountProfile(
            isLoggedIn = true,
            uid = uid.ifBlank { "google_${cleanEmail.hashCode().toUInt()}" },
            displayName = cleanName,
            email = cleanEmail,
            authMethod = AuthMethod.GOOGLE,
            isGoogleConnected = true,
            isGoogleFallbackMode = isFallbackMode,
            createdAtEpochMs = now
        )
    }

    suspend fun clearLoginSession() {
        context.apexProgressDataStore.edit { prefs ->
            prefs[Keys.AUTH_IS_LOGGED_IN] = false
            prefs[Keys.AUTH_UID] = ""
            prefs[Keys.AUTH_DISPLAY_NAME] = ""
            prefs[Keys.AUTH_EMAIL] = ""
            prefs[Keys.AUTH_METHOD] = AuthMethod.NONE.id
            prefs[Keys.AUTH_GOOGLE_CONNECTED] = false
            prefs[Keys.AUTH_GOOGLE_FALLBACK] = false
        }
    }

    suspend fun changeLocalAccountPassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit> {
        if (!LocalCryptoHelper.isValidPassword(newPassword)) {
            return Result.failure(IllegalArgumentException("Mật khẩu mới cần tối thiểu 6 ký tự."))
        }
        var errorMsg: String? = null
        context.apexProgressDataStore.edit { prefs ->
            val email = prefs[Keys.AUTH_EMAIL].orEmpty()
            val accounts = LocalCryptoHelper.deserializeAccounts(prefs[Keys.REGISTERED_ACCOUNTS_JSON])
                .toMutableList()
            val idx = accounts.indexOfFirst { it.email.equals(email, ignoreCase = true) }
            if (idx >= 0) {
                val acc = accounts[idx]
                if (!LocalCryptoHelper.verifySecret(currentPassword, acc.saltB64, acc.passwordHashB64)) {
                    errorMsg = "Mật khẩu hiện tại không đúng."
                    return@edit
                }
                val (newSalt, newHash) = LocalCryptoHelper.hashSecret(newPassword)
                accounts[idx] = acc.copy(saltB64 = newSalt, passwordHashB64 = newHash)
                prefs[Keys.REGISTERED_ACCOUNTS_JSON] = LocalCryptoHelper.serializeAccounts(accounts)
            } else {
                // Google user setting an optional local backup password for their email
                if (email.isBlank()) {
                    errorMsg = "Chưa có email tài khoản để đặt mật khẩu."
                    return@edit
                }
                val (newSalt, newHash) = LocalCryptoHelper.hashSecret(newPassword)
                accounts.add(
                    LocalRegisteredAccount(
                        email = email,
                        displayName = prefs[Keys.AUTH_DISPLAY_NAME].orEmpty().ifBlank { email.substringBefore("@") },
                        saltB64 = newSalt,
                        passwordHashB64 = newHash,
                        createdAtEpochMs = System.currentTimeMillis()
                    )
                )
                prefs[Keys.REGISTERED_ACCOUNTS_JSON] = LocalCryptoHelper.serializeAccounts(accounts)
            }
        }
        val err = errorMsg
        return if (err != null) Result.failure(IllegalArgumentException(err)) else Result.success(Unit)
    }

    // =========================================================================
    // Security: 4-Digit App Lock PIN (Salted PBKDF2) & Biometric Lock
    // =========================================================================

    suspend fun configureAppLockPin(pin: String, enableLock: Boolean = true): Result<Unit> {
        if (!LocalCryptoHelper.isValidPin(pin)) {
            return Result.failure(IllegalArgumentException("Mã PIN phải gồm đúng 4 chữ số."))
        }
        val (salt, hash) = LocalCryptoHelper.hashSecret(pin)
        context.apexProgressDataStore.edit { prefs ->
            prefs[Keys.APP_LOCK_PIN_SALT] = salt
            prefs[Keys.APP_LOCK_PIN_HASH] = hash
            prefs[Keys.APP_LOCK_ENABLED] = enableLock
        }
        return Result.success(Unit)
    }

    suspend fun verifyAppLockPin(pin: String): Boolean {
        if (!LocalCryptoHelper.isValidPin(pin)) return false
        val prefs = context.apexProgressDataStore.data.first()
        val salt = prefs[Keys.APP_LOCK_PIN_SALT].orEmpty()
        val hash = prefs[Keys.APP_LOCK_PIN_HASH].orEmpty()
        return LocalCryptoHelper.verifySecret(pin, salt, hash)
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.apexProgressDataStore.edit { prefs ->
            val hasPin = !prefs[Keys.APP_LOCK_PIN_HASH].isNullOrBlank()
            prefs[Keys.APP_LOCK_ENABLED] = enabled && hasPin
            if (!enabled) {
                prefs[Keys.BIOMETRIC_LOCK_ENABLED] = false
            }
        }
    }

    suspend fun setBiometricLockEnabled(enabled: Boolean) {
        context.apexProgressDataStore.edit { prefs ->
            prefs[Keys.BIOMETRIC_LOCK_ENABLED] = enabled
        }
    }

    // =========================================================================
    // Settings: Reset Study Progress (Preserves Account, Security & Library)
    // =========================================================================

    suspend fun resetStudyProgress() {
        context.apexProgressDataStore.edit { prefs ->
            prefs[Keys.ACTIVE_MODULE_ID] = 1
            prefs[Keys.COMPLETED_MODULE_IDS] = emptySet()
            prefs[Keys.STREAK_DAYS] = 0
            prefs[Keys.COMPLETED_HABIT_IDS] = emptySet()
            prefs[Keys.WEEKLY_ACTIVE_DAYS] = emptySet()
            prefs[Keys.STREAK_AWARDED_DATE] = ""
            prefs[Keys.LAST_KPI_COMPLETED_DATE] = ""
            prefs[Keys.SRS_WORD_IDS] = emptySet()
            prefs[Keys.LEARNED_VOCAB_IDS] = emptySet()
            prefs[Keys.EXAM_HISTORY_JSON] = "[]"
            prefs[Keys.DAILY_SELF_STUDY_MINUTES] = 0
        }
    }
}
