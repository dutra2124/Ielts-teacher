package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.notificationCenterDataStore by preferencesDataStore(name = "master_ielts_notif_center_v1")

enum class NotificationSection(val labelVi: String) {
    VOCAB("Từ vựng cần học hôm nay"),
    EXAM("Đến giờ kiểm tra & Đề thi")
}

enum class NotificationSoundPreset(
    val storedUri: String?,
    val titleVi: String,
    val subtitleVi: String
) {
    DEFAULT_NOTIFICATION(
        storedUri = null,
        titleVi = "Âm thông báo mặc định",
        subtitleVi = "Âm chuẩn thông báo của hệ thống Android"
    ),
    GENTLE_REMINDER(
        storedUri = "preset://gentle_reminder",
        titleVi = "Âm nhắc nhở nhẹ nhàng",
        subtitleVi = "Âm điệu êm dịu, thân thiện cho giờ học hằng ngày"
    ),
    ALARM_CHIME(
        storedUri = "preset://alarm_chime",
        titleVi = "Âm chuông báo thức",
        subtitleVi = "Âm thanh rõ ràng, nổi bật cho giờ kiểm tra & thi thử"
    );

    companion object {
        fun fromStoredUri(uri: String?): NotificationSoundPreset? {
            if (uri.isNullOrBlank()) return DEFAULT_NOTIFICATION
            return entries.firstOrNull { it.storedUri == uri }
        }
    }
}

data class NotificationInboxItem(
    val id: String,
    val section: NotificationSection,
    val titleVi: String,
    val subtitleVi: String,
    val badgeVi: String,
    val metaVi: String = "",
    val isUrgent: Boolean = false,
    val isRead: Boolean = false
)

data class NotificationCenterState(
    val dateIso: String = StreakManager.todayIsoDate(),
    val readNotificationIds: Set<String> = emptySet(),
    val dismissedNotificationIds: Set<String> = emptySet(),
    val isNotificationSoundEnabled: Boolean = true,
    val notificationSoundUri: String? = null,
    val notificationSoundTitle: String = NotificationSoundPreset.DEFAULT_NOTIFICATION.titleVi
)

/**
 * Persists daily Notification Center read/dismissed states and Notification Sound preferences
 * using Jetpack Preferences DataStore.
 *
 * - `readNotificationIds` and `dismissedNotificationIds` persist across app restarts and screen rotations
 *   for the current calendar day (`yyyy-MM-dd`), and automatically reset on a new calendar day.
 * - `isNotificationSoundEnabled`, `notificationSoundUri`, and `notificationSoundTitle` persist indefinitely.
 */
class NotificationRepository(private val context: Context) {

    private object Keys {
        val DATE_ISO = stringPreferencesKey("notif_state_date_iso")
        val READ_IDS = stringSetPreferencesKey("notif_read_ids")
        val DISMISSED_IDS = stringSetPreferencesKey("notif_dismissed_ids")
        val SOUND_ENABLED = booleanPreferencesKey("notif_sound_enabled")
        val SOUND_URI = stringPreferencesKey("notif_sound_uri")
        val SOUND_TITLE = stringPreferencesKey("notif_sound_title")
    }

    val stateFlow: Flow<NotificationCenterState> = context.notificationCenterDataStore.data.map { prefs ->
        val todayIso = StreakManager.todayIsoDate()
        val storedDate = prefs[Keys.DATE_ISO].orEmpty()
        val isSameDay = storedDate == todayIso

        val soundUri = prefs[Keys.SOUND_URI]?.takeIf { it.isNotBlank() }
        val soundTitle = prefs[Keys.SOUND_TITLE]?.takeIf { it.isNotBlank() }
            ?: NotificationSoundPreset.fromStoredUri(soundUri)?.titleVi
            ?: NotificationSoundPreset.DEFAULT_NOTIFICATION.titleVi

        NotificationCenterState(
            dateIso = todayIso,
            readNotificationIds = if (isSameDay) prefs[Keys.READ_IDS] ?: emptySet() else emptySet(),
            dismissedNotificationIds = if (isSameDay) prefs[Keys.DISMISSED_IDS] ?: emptySet() else emptySet(),
            isNotificationSoundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            notificationSoundUri = soundUri,
            notificationSoundTitle = soundTitle
        )
    }

    suspend fun checkDateRollover() {
        val todayIso = StreakManager.todayIsoDate()
        context.notificationCenterDataStore.edit { prefs ->
            val storedDate = prefs[Keys.DATE_ISO].orEmpty()
            if (storedDate != todayIso) {
                prefs[Keys.DATE_ISO] = todayIso
                prefs[Keys.READ_IDS] = emptySet()
                prefs[Keys.DISMISSED_IDS] = emptySet()
            }
        }
    }

    suspend fun markRead(notificationId: String) {
        if (notificationId.isBlank()) return
        val todayIso = StreakManager.todayIsoDate()
        context.notificationCenterDataStore.edit { prefs ->
            val currentRead = if (prefs[Keys.DATE_ISO] == todayIso) {
                prefs[Keys.READ_IDS] ?: emptySet()
            } else {
                prefs[Keys.DISMISSED_IDS] = emptySet()
                emptySet()
            }
            prefs[Keys.DATE_ISO] = todayIso
            prefs[Keys.READ_IDS] = currentRead + notificationId
        }
    }

    suspend fun markAllRead(notificationIds: Collection<String>) {
        if (notificationIds.isEmpty()) return
        val todayIso = StreakManager.todayIsoDate()
        context.notificationCenterDataStore.edit { prefs ->
            val currentRead = if (prefs[Keys.DATE_ISO] == todayIso) {
                prefs[Keys.READ_IDS] ?: emptySet()
            } else {
                prefs[Keys.DISMISSED_IDS] = emptySet()
                emptySet()
            }
            prefs[Keys.DATE_ISO] = todayIso
            prefs[Keys.READ_IDS] = currentRead + notificationIds
        }
    }

    suspend fun dismissNotification(notificationId: String) {
        if (notificationId.isBlank()) return
        val todayIso = StreakManager.todayIsoDate()
        context.notificationCenterDataStore.edit { prefs ->
            val sameDay = prefs[Keys.DATE_ISO] == todayIso
            val currentDismissed = if (sameDay) prefs[Keys.DISMISSED_IDS] ?: emptySet() else emptySet()
            val currentRead = if (sameDay) prefs[Keys.READ_IDS] ?: emptySet() else emptySet()
            prefs[Keys.DATE_ISO] = todayIso
            prefs[Keys.DISMISSED_IDS] = currentDismissed + notificationId
            prefs[Keys.READ_IDS] = currentRead + notificationId
        }
    }

    suspend fun dismissAll(notificationIds: Collection<String>) {
        if (notificationIds.isEmpty()) return
        val todayIso = StreakManager.todayIsoDate()
        context.notificationCenterDataStore.edit { prefs ->
            val sameDay = prefs[Keys.DATE_ISO] == todayIso
            val currentDismissed = if (sameDay) prefs[Keys.DISMISSED_IDS] ?: emptySet() else emptySet()
            val currentRead = if (sameDay) prefs[Keys.READ_IDS] ?: emptySet() else emptySet()
            prefs[Keys.DATE_ISO] = todayIso
            prefs[Keys.DISMISSED_IDS] = currentDismissed + notificationIds
            prefs[Keys.READ_IDS] = currentRead + notificationIds
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.notificationCenterDataStore.edit { prefs ->
            prefs[Keys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setNotificationSound(uriString: String?, title: String) {
        val cleanTitle = title.trim().ifBlank { NotificationSoundPreset.DEFAULT_NOTIFICATION.titleVi }
        context.notificationCenterDataStore.edit { prefs ->
            if (uriString.isNullOrBlank()) {
                prefs.remove(Keys.SOUND_URI)
            } else {
                prefs[Keys.SOUND_URI] = uriString
            }
            prefs[Keys.SOUND_TITLE] = cleanTitle
        }
    }

    suspend fun resetDailyInbox() {
        val todayIso = StreakManager.todayIsoDate()
        context.notificationCenterDataStore.edit { prefs ->
            prefs[Keys.DATE_ISO] = todayIso
            prefs[Keys.READ_IDS] = emptySet()
            prefs[Keys.DISMISSED_IDS] = emptySet()
        }
    }

    companion object {
        /**
         * Builds today's deterministic actionable inbox items from SRS due words, active Module
         * unlearned words, scheduled test slots, and active Module checkpoint / recommended exam,
         * then applies persisted read/dismissed filters.
         */
        fun buildVisibleNotifications(
            activeModule: IeltsModule,
            dueSrsWords: List<VocabWordItem>,
            unlearnedModuleWords: List<VocabWordItem>,
            notificationPrefs: NotificationPreferences,
            todayWeekdayId: String,
            recommendedExam: ExamPaper?,
            hasCompletedActiveModule: Boolean,
            centerState: NotificationCenterState
        ): List<NotificationInboxItem> {
            val generated = mutableListOf<NotificationInboxItem>()
            val srsIdSet = dueSrsWords.map { it.id }.toSet()

            // Section 1: Vocab due today (prioritize due SRS words first, then active module unlearned words)
            val combinedVocab = (dueSrsWords + unlearnedModuleWords)
                .distinctBy { it.id }
                .take(6)

            combinedVocab.forEach { word ->
                val isSrs = srsIdSet.contains(word.id)
                val id = if (isSrs) "vocab_srs_${word.id}" else "vocab_mod_${word.id}"
                generated += NotificationInboxItem(
                    id = id,
                    section = NotificationSection.VOCAB,
                    titleVi = word.word,
                    subtitleVi = word.meaningVi,
                    badgeVi = if (isSrs) "Ôn SRS" else "Từ mới",
                    metaVi = word.ipaUk,
                    isUrgent = isSrs,
                    isRead = centerState.readNotificationIds.contains(id)
                )
            }

            // Section 2: Scheduled test slots & active module checkpoint / mock exam
            val enabledTestSlots = notificationPrefs.testSlots.filter { it.enabled }
            val isTestDayToday = notificationPrefs.testRepeatDays.contains(todayWeekdayId)

            enabledTestSlots.take(3).forEach { slot ->
                val id = "test_slot_${slot.id}"
                val subtitle = if (isTestDayToday) {
                    "Đến hạn hôm nay lúc ${slot.formattedTime()} ($todayWeekdayId)"
                } else {
                    "Lịch định kỳ ${slot.formattedTime()} (${notificationPrefs.testRepeatDays.joinToString(", ")})"
                }
                generated += NotificationInboxItem(
                    id = id,
                    section = NotificationSection.EXAM,
                    titleVi = slot.labelVi,
                    subtitleVi = subtitle,
                    badgeVi = if (isTestDayToday) "Hôm nay" else slot.formattedTime(),
                    metaVi = slot.formattedTime(),
                    isUrgent = isTestDayToday,
                    isRead = centerState.readNotificationIds.contains(id)
                )
            }

            // Active Module Checkpoint / Recommended Mock Exam
            val checkpointId = "exam_checkpoint_m${activeModule.id}"
            val moduleNum = String.format(Locale.US, "%02d", activeModule.id)
            val checkpointTitle = recommendedExam?.title ?: "Checkpoint Module #$moduleNum"
            val checkpointSubtitle = if (!hasCompletedActiveModule) {
                "Kiểm tra năng lực ${activeModule.titleEn} • ${activeModule.tier.bandRange}"
            } else {
                recommendedExam?.topicVi?.takeIf { it.isNotBlank() }?.let {
                    "Chủ đề: $it • Thi mô phỏng CD-IELTS + Chữa đề AI"
                } ?: "Thi mô phỏng CD-IELTS chuẩn phòng thi thật + Chữa đề AI"
            }
            generated += NotificationInboxItem(
                id = checkpointId,
                section = NotificationSection.EXAM,
                titleVi = checkpointTitle,
                subtitleVi = checkpointSubtitle,
                badgeVi = if (!hasCompletedActiveModule) "Checkpoint" else "Đề thi gợi ý",
                metaVi = activeModule.tier.bandRange,
                isUrgent = !hasCompletedActiveModule,
                isRead = centerState.readNotificationIds.contains(checkpointId)
            )

            return generated.filterNot { centerState.dismissedNotificationIds.contains(it.id) }
        }
    }
}
