package com.example.data

import android.content.Context
import android.os.SystemClock
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.roundToInt

private val Context.autoKpiDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "master_ielts_auto_kpi_v1"
)

/**
 * Lightweight process-level hook so [com.example.MainActivity.onUserInteraction] and audio/TTS
 * events can reset the 60-second inactivity guard without leaking Activity references.
 */
object AutoKpiInteractionBus {
    @Volatile
    private var listener: (() -> Unit)? = null

    fun attach(onInteraction: () -> Unit) {
        listener = onInteraction
    }

    fun detach() {
        listener = null
    }

    fun notifyInteraction() {
        listener?.invoke()
    }
}

/**
 * Celebratory banner payload shown once per day when both automatic KPI targets hit 100%.
 */
data class KpiCelebrationBanner(
    val id: Long = System.currentTimeMillis(),
    val streakDays: Int,
    val messageVi: String,
    val evolvedStageTitleVi: String? = null
)

/**
 * Reactive read-only snapshot of today's 100% automatic Daily KPI metrics.
 * Users cannot manually toggle any KPI item; progress updates strictly from real study behavior.
 */
data class AutoKpiSnapshot(
    val dateIso: String = StreakManager.todayIsoDate(),
    val dailyLearnedWordIds: Set<String> = emptySet(),
    val dailyStudySeconds: Int = 0,
    val targetWordsPerDay: Int = AutoKpiTracker.DEFAULT_TARGET_WORDS,
    val targetMinutesPerDay: Int = AutoKpiTracker.DEFAULT_TARGET_MINUTES,
    val isTimerActive: Boolean = false,
    val isPausedDueToInactivity: Boolean = false
) {
    val dailyWordsLearned: Int
        get() = dailyLearnedWordIds.size

    val dailyStudyMinutes: Int
        get() = dailyStudySeconds / 60

    val targetStudySeconds: Int
        get() = (targetMinutesPerDay * 60).coerceAtLeast(60)

    val isVocabTargetMet: Boolean
        get() = dailyWordsLearned >= targetWordsPerDay

    val isStudyTimeTargetMet: Boolean
        get() = dailyStudySeconds >= targetStudySeconds

    val isAllDailyKpiMet: Boolean
        get() = isVocabTargetMet && isStudyTimeTargetMet

    val vocabProgressFraction: Float
        get() = (dailyWordsLearned.toFloat() / targetWordsPerDay.coerceAtLeast(1).toFloat())
            .coerceIn(0f, 1f)

    val studyTimeProgressFraction: Float
        get() = (dailyStudySeconds.toFloat() / targetStudySeconds.toFloat())
            .coerceIn(0f, 1f)

    val overallProgressFraction: Float
        get() = ((vocabProgressFraction + studyTimeProgressFraction) / 2f).coerceIn(0f, 1f)

    val overallPercent: Int
        get() = (overallProgressFraction * 100f).roundToInt().coerceIn(0, 100)

    val completedCriteriaCount: Int
        get() = (if (isVocabTargetMet) 1 else 0) + (if (isStudyTimeTargetMet) 1 else 0)

    val vocabProgressPillVi: String
        get() = if (isVocabTargetMet) "✓ Đạt ($dailyWordsLearned/$targetWordsPerDay từ)" else "$dailyWordsLearned/$targetWordsPerDay từ"

    val studyTimeProgressPillVi: String
        get() = if (isStudyTimeTargetMet) {
            "✓ Đạt ($dailyStudyMinutes/$targetMinutesPerDay phút)"
        } else {
            "$dailyStudyMinutes/$targetMinutesPerDay phút"
        }

    val liveClockFormatted: String
        get() {
            val mins = dailyStudySeconds / 60
            val secs = dailyStudySeconds % 60
            return String.format(Locale.US, "%02d:%02d", mins, secs)
        }
}

/**
 * 100% Automatic Daily KPI & Active Study Time Tracker backed by Jetpack DataStore:
 * - Lifecycle-aware active timer via [createLifecycleObserver] (pauses immediately on ON_PAUSE/ON_STOP).
 * - 60-second inactivity guard: pauses accumulation if no touch/scroll/audio interaction occurs for 60s.
 * - Counts study time ONLY inside Học, Luyện, or Thi (and fullscreen Mock Exam) screens.
 * - Deduplicates learned vocabulary by `wordId` per ISO calendar date (`yyyy-MM-dd`).
 * - Automatically resets `dailyWordsLearned = 0` and `dailyStudySeconds = 0` on calendar day rollover.
 * - Reactively triggers [onDailyKpiReached] the exact moment BOTH criteria hit 100%.
 */
class AutoKpiTracker(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onDailyKpiReached: suspend (dateIso: String) -> Unit
) {
    companion object {
        const val DEFAULT_TARGET_WORDS = 10
        const val DEFAULT_TARGET_MINUTES = 15
        const val MIN_TARGET_WORDS = 5
        const val MAX_TARGET_WORDS = 30
        const val MIN_TARGET_MINUTES = 10
        const val MAX_TARGET_MINUTES = 60
        const val INACTIVITY_TIMEOUT_MS = 60_000L
        private const val PERSIST_INTERVAL_SECONDS = 5
    }

    private object Keys {
        val KPI_DATE_ISO = stringPreferencesKey("kpi_date_iso_v1")
        val DAILY_LEARNED_WORD_IDS = stringSetPreferencesKey("daily_learned_word_ids_v1")
        val DAILY_STUDY_SECONDS = intPreferencesKey("daily_study_seconds_v1")
        val TARGET_WORDS_PER_DAY = intPreferencesKey("target_words_per_day_v1")
        val TARGET_MINUTES_PER_DAY = intPreferencesKey("target_minutes_per_day_v1")
    }

    private val _snapshotFlow = MutableStateFlow(AutoKpiSnapshot())
    val snapshotFlow: StateFlow<AutoKpiSnapshot> = _snapshotFlow.asStateFlow()

    private val isAppForeground = AtomicBoolean(true)
    private val isInStudyScreen = AtomicBoolean(false)
    private val lastInteractionElapsedMs = AtomicLong(SystemClock.elapsedRealtime())
    private val isInitialized = AtomicBoolean(false)

    @Volatile
    private var lastAwardedCallbackDateIso: String = ""

    @Volatile
    private var pendingDirtySeconds: Int = 0

    private var tickerJob: Job? = null

    init {
        AutoKpiInteractionBus.attach { recordUserInteraction() }
        scope.launch {
            loadInitialState()
            isInitialized.set(true)
            startActiveStudyTicker()
        }
    }

    private suspend fun loadInitialState() {
        val todayIso = StreakManager.todayIsoDate()
        val prefs = context.autoKpiDataStore.data.first()
        val savedDate = prefs[Keys.KPI_DATE_ISO].orEmpty()
        val targetWords = (prefs[Keys.TARGET_WORDS_PER_DAY] ?: DEFAULT_TARGET_WORDS)
            .coerceIn(MIN_TARGET_WORDS, MAX_TARGET_WORDS)
        val targetMinutes = (prefs[Keys.TARGET_MINUTES_PER_DAY] ?: DEFAULT_TARGET_MINUTES)
            .coerceIn(MIN_TARGET_MINUTES, MAX_TARGET_MINUTES)

        val isSameDay = savedDate == todayIso
        val learnedIds = if (isSameDay) prefs[Keys.DAILY_LEARNED_WORD_IDS].orEmpty() else emptySet()
        val studySeconds = if (isSameDay) (prefs[Keys.DAILY_STUDY_SECONDS] ?: 0).coerceAtLeast(0) else 0

        if (!isSameDay && savedDate.isNotBlank()) {
            persistCurrentToDataStore(
                dateIso = todayIso,
                learnedWordIds = emptySet(),
                studySeconds = 0,
                targetWords = targetWords,
                targetMinutes = targetMinutes
            )
        }

        val initial = AutoKpiSnapshot(
            dateIso = todayIso,
            dailyLearnedWordIds = learnedIds,
            dailyStudySeconds = studySeconds,
            targetWordsPerDay = targetWords,
            targetMinutesPerDay = targetMinutes,
            isTimerActive = false,
            isPausedDueToInactivity = false
        )
        _snapshotFlow.value = initial
        if (initial.isAllDailyKpiMet) {
            triggerKpiReachedIfNeeded(initial)
        }
    }

    /**
     * Lifecycle observer attached to the Activity/Compose LifecycleOwner:
     * immediately pauses and flushes study seconds when the app goes to background or screen locks.
     */
    fun createLifecycleObserver(): LifecycleEventObserver = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                isAppForeground.set(true)
                lastInteractionElapsedMs.set(SystemClock.elapsedRealtime())
                scope.launch { checkDateRollover() }
            }
            Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                isAppForeground.set(false)
                val current = _snapshotFlow.value
                if (current.isTimerActive || current.isPausedDueToInactivity) {
                    _snapshotFlow.value = current.copy(
                        isTimerActive = false,
                        isPausedDueToInactivity = false
                    )
                }
                flushPendingSecondsAsync()
            }
            else -> Unit
        }
    }

    /**
     * Enables/disables active timer eligibility depending on whether the user is currently
     * inside Học, Luyện, or Thi (including fullscreen CD-IELTS Mock Exam).
     */
    fun setInStudyContext(inStudy: Boolean) {
        val changed = isInStudyScreen.getAndSet(inStudy) != inStudy
        if (inStudy) {
            lastInteractionElapsedMs.set(SystemClock.elapsedRealtime())
        } else if (changed) {
            val current = _snapshotFlow.value
            if (current.isTimerActive || current.isPausedDueToInactivity) {
                _snapshotFlow.value = current.copy(
                    isTimerActive = false,
                    isPausedDueToInactivity = false
                )
            }
            flushPendingSecondsAsync()
        }
    }

    /**
     * Resets the 60-second inactivity guard on any touch, scroll, audio playback, or study action.
     */
    fun recordUserInteraction() {
        lastInteractionElapsedMs.set(SystemClock.elapsedRealtime())
        val current = _snapshotFlow.value
        if (current.isPausedDueToInactivity && isAppForeground.get() && isInStudyScreen.get()) {
            _snapshotFlow.value = current.copy(
                isPausedDueToInactivity = false,
                isTimerActive = true
            )
        }
    }

    private fun startActiveStudyTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (true) {
                delay(1_000L)
                val todayIso = StreakManager.todayIsoDate()
                val current = _snapshotFlow.value

                // Automatic calendar day rollover reset
                if (current.dateIso != todayIso) {
                    pendingDirtySeconds = 0
                    lastAwardedCallbackDateIso = ""
                    val rolled = current.copy(
                        dateIso = todayIso,
                        dailyLearnedWordIds = emptySet(),
                        dailyStudySeconds = 0,
                        isTimerActive = false,
                        isPausedDueToInactivity = false
                    )
                    _snapshotFlow.value = rolled
                    persistSnapshot(rolled)
                    continue
                }

                val eligible = isAppForeground.get() && isInStudyScreen.get()
                if (!eligible) {
                    if (current.isTimerActive || current.isPausedDueToInactivity) {
                        _snapshotFlow.value = current.copy(
                            isTimerActive = false,
                            isPausedDueToInactivity = false
                        )
                    }
                    if (pendingDirtySeconds > 0) {
                        pendingDirtySeconds = 0
                        persistSnapshot(_snapshotFlow.value)
                    }
                    continue
                }

                val idleMs = SystemClock.elapsedRealtime() - lastInteractionElapsedMs.get()
                if (idleMs >= INACTIVITY_TIMEOUT_MS) {
                    if (current.isTimerActive || !current.isPausedDueToInactivity) {
                        _snapshotFlow.value = current.copy(
                            isTimerActive = false,
                            isPausedDueToInactivity = true
                        )
                    }
                    if (pendingDirtySeconds > 0) {
                        pendingDirtySeconds = 0
                        persistSnapshot(_snapshotFlow.value)
                    }
                    continue
                }

                // Actively studying: +1 second
                val wasTimeMet = current.isStudyTimeTargetMet
                val nextSeconds = (current.dailyStudySeconds + 1).coerceAtMost(86_400)
                val updated = current.copy(
                    dailyStudySeconds = nextSeconds,
                    isTimerActive = true,
                    isPausedDueToInactivity = false
                )
                _snapshotFlow.value = updated
                pendingDirtySeconds += 1

                val justHitTimeTarget = !wasTimeMet && updated.isStudyTimeTargetMet
                if (pendingDirtySeconds >= PERSIST_INTERVAL_SECONDS || justHitTimeTarget) {
                    pendingDirtySeconds = 0
                    persistSnapshot(updated)
                }

                if (updated.isAllDailyKpiMet) {
                    triggerKpiReachedIfNeeded(updated)
                }
            }
        }
    }

    /**
     * Automatically increments `dailyWordsLearned` deduplicated by `wordId` per calendar day.
     * Returns true if this `wordId` was newly counted today.
     */
    suspend fun recordWordLearned(wordId: String): Boolean {
        val cleanId = wordId.trim()
        if (cleanId.isEmpty()) return false
        recordUserInteraction()
        checkDateRollover()

        val current = _snapshotFlow.value
        if (current.dailyLearnedWordIds.contains(cleanId)) {
            if (current.isAllDailyKpiMet) {
                triggerKpiReachedIfNeeded(current)
            }
            return false
        }

        val updated = current.copy(
            dailyLearnedWordIds = current.dailyLearnedWordIds + cleanId
        )
        _snapshotFlow.value = updated
        pendingDirtySeconds = 0
        persistSnapshot(updated)

        if (updated.isAllDailyKpiMet) {
            triggerKpiReachedIfNeeded(updated)
        }
        return true
    }

    /**
     * Adds verified study seconds from logged Personal Library self-study sessions.
     */
    suspend fun addVerifiedStudySeconds(seconds: Int) {
        val safeSec = seconds.coerceIn(1, 18_000)
        recordUserInteraction()
        checkDateRollover()

        val current = _snapshotFlow.value
        val updated = current.copy(
            dailyStudySeconds = (current.dailyStudySeconds + safeSec).coerceAtMost(86_400)
        )
        _snapshotFlow.value = updated
        pendingDirtySeconds = 0
        persistSnapshot(updated)

        if (updated.isAllDailyKpiMet) {
            triggerKpiReachedIfNeeded(updated)
        }
    }

    /**
     * Updates configurable Daily KPI targets (5–30 words/day, 10–60 minutes/day) persisted in DataStore.
     */
    suspend fun updateDailyTargets(targetWords: Int, targetMinutes: Int) {
        val clampedWords = targetWords.coerceIn(MIN_TARGET_WORDS, MAX_TARGET_WORDS)
        val clampedMinutes = targetMinutes.coerceIn(MIN_TARGET_MINUTES, MAX_TARGET_MINUTES)
        checkDateRollover()

        val updated = _snapshotFlow.value.copy(
            targetWordsPerDay = clampedWords,
            targetMinutesPerDay = clampedMinutes
        )
        _snapshotFlow.value = updated
        pendingDirtySeconds = 0
        persistSnapshot(updated)

        if (updated.isAllDailyKpiMet) {
            triggerKpiReachedIfNeeded(updated)
        }
    }

    suspend fun checkDateRollover() {
        val todayIso = StreakManager.todayIsoDate()
        val current = _snapshotFlow.value
        if (current.dateIso != todayIso) {
            pendingDirtySeconds = 0
            lastAwardedCallbackDateIso = ""
            val rolled = current.copy(
                dateIso = todayIso,
                dailyLearnedWordIds = emptySet(),
                dailyStudySeconds = 0,
                isTimerActive = false,
                isPausedDueToInactivity = false
            )
            _snapshotFlow.value = rolled
            persistSnapshot(rolled)
        }
    }

    suspend fun resetTodayProgress() {
        val todayIso = StreakManager.todayIsoDate()
        pendingDirtySeconds = 0
        lastAwardedCallbackDateIso = ""
        val cleared = _snapshotFlow.value.copy(
            dateIso = todayIso,
            dailyLearnedWordIds = emptySet(),
            dailyStudySeconds = 0,
            isTimerActive = false,
            isPausedDueToInactivity = false
        )
        _snapshotFlow.value = cleared
        persistSnapshot(cleared)
    }

    fun shutdown() {
        AutoKpiInteractionBus.detach()
        tickerJob?.cancel()
        flushPendingSecondsAsync()
    }

    private fun flushPendingSecondsAsync() {
        if (pendingDirtySeconds <= 0) return
        pendingDirtySeconds = 0
        val snap = _snapshotFlow.value
        scope.launch {
            persistSnapshot(snap)
        }
    }

    private suspend fun triggerKpiReachedIfNeeded(snapshot: AutoKpiSnapshot) {
        if (!snapshot.isAllDailyKpiMet) return
        if (lastAwardedCallbackDateIso == snapshot.dateIso) return
        lastAwardedCallbackDateIso = snapshot.dateIso
        onDailyKpiReached(snapshot.dateIso)
    }

    private suspend fun persistSnapshot(snapshot: AutoKpiSnapshot) {
        persistCurrentToDataStore(
            dateIso = snapshot.dateIso,
            learnedWordIds = snapshot.dailyLearnedWordIds,
            studySeconds = snapshot.dailyStudySeconds,
            targetWords = snapshot.targetWordsPerDay,
            targetMinutes = snapshot.targetMinutesPerDay
        )
    }

    private suspend fun persistCurrentToDataStore(
        dateIso: String,
        learnedWordIds: Set<String>,
        studySeconds: Int,
        targetWords: Int,
        targetMinutes: Int
    ) {
        context.autoKpiDataStore.edit { prefs ->
            prefs[Keys.KPI_DATE_ISO] = dateIso
            prefs[Keys.DAILY_LEARNED_WORD_IDS] = learnedWordIds
            prefs[Keys.DAILY_STUDY_SECONDS] = studySeconds
            prefs[Keys.TARGET_WORDS_PER_DAY] = targetWords
            prefs[Keys.TARGET_MINUTES_PER_DAY] = targetMinutes
        }
    }
}
