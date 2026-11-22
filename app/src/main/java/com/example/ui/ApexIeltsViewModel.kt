package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AppCheckDiagnostics
import com.example.ai.AppCheckStatusStore
import com.example.ai.ExamParserAiService
import com.example.ai.ExamParserUiState
import com.example.ai.GeminiResilientClient
import com.example.ai.PersonalMaterialAiService
import com.example.ai.PracticeAiService
import com.example.ai.ReviewDiffAiService
import com.example.ai.ReviewDiffAiUiState
import com.example.ai.SentenceAiUiState
import com.example.ai.SentenceCoachAiService
import com.example.ai.SpeakingAiUiState
import com.example.ai.SyntaxBreakdownUiState
import com.example.ai.WritingAiUiState
import com.example.auth.GoogleAuthHelper
import com.example.auth.SecurityPreferences
import com.example.auth.UserAccountProfile
import com.example.data.ActiveExamSession
import com.example.data.ActivePushBanner
import com.example.data.DailyBandTip
import com.example.data.DeepQuestionAnalysis
import com.example.data.DefaultHabitData
import com.example.data.ExamAttemptRecord
import com.example.data.ExamBankCurriculum
import com.example.data.ExamHubSubTab
import com.example.data.ExamPaper
import com.example.data.ExamSkillMode
import com.example.data.IeltsCurriculum
import com.example.data.IeltsModule
import com.example.data.LearnCurriculum
import com.example.data.NotificationPreferences
import com.example.data.PracticeSkill
import com.example.data.ReminderSlot
import com.example.data.ReminderTone
import com.example.data.ReviewErrorLogItem
import com.example.data.StreakManager
import com.example.data.StreakStatus
import com.example.data.StreakVisualStyle
import com.example.data.StudyTab
import com.example.data.SyntaxExercise
import com.example.data.TeacherMood
import com.example.data.UserProgressRepository
import com.example.data.VocabWordItem
import com.example.data.WeekdayItem
import com.example.data.local.ApexHybridRepository
import com.example.data.local.AudioBlobRecord
import com.example.data.local.BulkImportDataset
import com.example.data.local.BulkImportPreview
import com.example.data.local.CloudSyncUiState
import com.example.data.local.LocalStorageCounts
import com.example.data.local.PersonalMaterial
import com.example.data.local.PersonalMaterialAiUiState
import com.example.data.local.StudyLogEntry
import com.example.data.sync.GoogleSheetsSyncService
import com.example.data.sync.NetworkStatusMonitor
import com.example.notifications.ApexNotificationHelper
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.AppThemePreferences
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.AppWallpaperPattern
import com.example.ui.theme.WallpaperStorageHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.UUID

data class ApexIeltsUiState(
    val activeTab: StudyTab = StudyTab.ROADMAP,
    val activeModuleId: Int = 1,
    val completedModuleIds: Set<Int> = emptySet(),
    val streakDays: Int = 0,
    val streakStatus: StreakStatus = StreakStatus(),
    val isStreakSheetOpen: Boolean = false,
    val reminderEnabled: Boolean = true,
    val teacherMood: TeacherMood = TeacherMood.TEACHING,
    val isTeacherSpeaking: Boolean = true,
    val tipCycleIndex: Int = 0,
    val snackbarMessage: String? = null,
    // Step 2: Daily Habit & Custom Notification Scheduler state
    val completedHabitIds: Set<String> = emptySet(),
    val weeklyActiveDays: Set<String> = emptySet(),
    val todayWeekdayId: String = WeekdayItem.today().id,
    val notificationPrefs: NotificationPreferences = NotificationPreferences(),
    val isNotificationSheetOpen: Boolean = false,
    val activePushBanner: ActivePushBanner? = null,
    // Step 3: Tab 2 Learn (SRS Queue, Learned Vocab & Gemini AI Sentence Builder)
    val srsWordIds: Set<String> = emptySet(),
    val learnedVocabIds: Set<String> = emptySet(),
    val sentenceAiState: SentenceAiUiState = SentenceAiUiState.Idle,
    // Step 4: Tab 3 Practice (Syntax Breakdown, Speaking AI & Writing AI)
    val syntaxBreakdownState: SyntaxBreakdownUiState = SyntaxBreakdownUiState.Idle,
    val speakingAiState: SpeakingAiUiState = SpeakingAiUiState.Idle,
    val writingAiState: WritingAiUiState = WritingAiUiState.Idle,
    // Tab 4: Đề thi (Thi mô phỏng + Chữa đề merged hub)
    val activeExamSubTab: ExamHubSubTab = ExamHubSubTab.SIMULATION,
    val customExams: List<ExamPaper> = emptyList(),
    val examHistory: List<ExamAttemptRecord> = emptyList(),
    val examParserState: ExamParserUiState = ExamParserUiState.Idle,
    val activeExamSession: ActiveExamSession? = null,
    // Chữa đề (Band 8.5+ Diff AI, Deep Question Analysis AI + Error Log -> Tab 3 Focus Action Loop)
    val reviewDiffAiState: ReviewDiffAiUiState = ReviewDiffAiUiState.Idle,
    val deepQuestionAiResults: Map<String, DeepQuestionAnalysis> = emptyMap(),
    val loadingQuestionAnalysisIds: Set<String> = emptySet(),
    val questionAnalysisErrors: Map<String, String> = emptyMap(),
    val pendingPracticeSkill: PracticeSkill? = null,
    val pendingPracticeFocusNote: String? = null,
    // Global Theme Manager (Light / Late-Night Study / CD-IELTS Exam Dark / System)
    val themePrefs: AppThemePreferences = AppThemePreferences(),
    val isThemeSheetOpen: Boolean = false,
    // Offline-First Hybrid Architecture: Room SQLite + Bundled JSON + Google Sheets Cloud Sync
    val modules: List<IeltsModule> = IeltsCurriculum.modules,
    val allVocabulary: List<VocabWordItem> = emptyList(),
    val sentenceDrills: List<SyntaxExercise> = LearnCurriculum.syntaxExercises,
    val authenticExams: List<ExamPaper> = ExamBankCurriculum.authenticExams,
    val personalMaterials: List<PersonalMaterial> = emptyList(),
    val audioBlobs: List<AudioBlobRecord> = emptyList(),
    val studyLogs: List<StudyLogEntry> = emptyList(),
    val storageCounts: LocalStorageCounts = LocalStorageCounts(),
    val isOnline: Boolean = true,
    val googleSheetUrl: String = "",
    val lastSyncedAtLabel: String = "",
    val cloudSyncState: CloudSyncUiState = CloudSyncUiState.Idle,
    val isSettingsSheetOpen: Boolean = false,
    // Personal Library & In-App CSV/JSON Bulk Importer
    val dailySelfStudyMinutes: Int = 0,
    val autoGreetDashboardVoice: Boolean = false,
    val isPersonalLibraryOpen: Boolean = false,
    val isBulkImporterOpen: Boolean = false,
    val bulkImporterDataset: BulkImportDataset = BulkImportDataset.VOCABULARY,
    val personalMaterialAiState: PersonalMaterialAiUiState = PersonalMaterialAiUiState.Idle,
    // Security, Firebase App Check & Sideload Gemini REST Fallback
    val personalGeminiApiKey: String = "",
    val googleCseApiKey: String = "",
    val googleCseCx: String = "",
    val appCheckDiagnostics: AppCheckDiagnostics = AppCheckDiagnostics(),
    // Authentication, Account Sidebar Drawer & App Lock Security
    val isAuthLoaded: Boolean = false,
    val userProfile: UserAccountProfile = UserAccountProfile(),
    val securityPrefs: SecurityPreferences = SecurityPreferences(),
    val isAppUnlocked: Boolean = true,
    val isSecuritySheetOpen: Boolean = false,
    val isAddMaterialsSheetOpen: Boolean = false,
    val isAvatarPickerOpen: Boolean = false,
    val settingsFocusSchedule: Boolean = false
) {
    val activeModule: IeltsModule
        get() = modules.firstOrNull { it.id == activeModuleId } ?: IeltsCurriculum.moduleById(activeModuleId)

    val activeModuleWords: List<VocabWordItem>
        get() {
            val dbWords = allVocabulary.filter { it.moduleId == activeModuleId }
            val fallbackWords = LearnCurriculum.wordsForModule(activeModuleId)
            if (dbWords.isEmpty()) return fallbackWords
            val merged = LinkedHashMap<String, VocabWordItem>()
            dbWords.forEach { merged[it.id] = it }
            fallbackWords.forEach { if (!merged.containsKey(it.id)) merged[it.id] = it }
            return merged.values.toList()
        }

    val srsReviewWords: List<VocabWordItem>
        get() {
            val byId = allVocabulary.associateBy { it.id }
            return srsWordIds.mapNotNull { id -> byId[id] ?: LearnCurriculum.wordById(id) }
        }

    val unlearnedActiveModuleWords: List<VocabWordItem>
        get() = activeModuleWords.filterNot { learnedVocabIds.contains(it.id) }

    val notificationUnreadCount: Int
        get() {
            val vocabDue = (srsReviewWords.map { it.id } + unlearnedActiveModuleWords.map { it.id }).toSet().size
            val isTestDayToday = notificationPrefs.testRepeatDays.contains(todayWeekdayId)
            val testAlerts = if (isTestDayToday) {
                notificationPrefs.testSlots.count { it.enabled }.coerceAtLeast(1)
            } else if (!completedModuleIds.contains(activeModuleId)) {
                1
            } else {
                0
            }
            return vocabDue + testAlerts
        }

    val currentBand: Float
        get() = IeltsCurriculum.calculateEstimatedBand(completedModuleIds)

    val activeDailyTip: DailyBandTip
        get() {
            val tips = activeModule.dailyTips
            return tips[tipCycleIndex % tips.size]
        }
}

class ApexIeltsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserProgressRepository(application.applicationContext)
    private val hybridRepository = ApexHybridRepository(application.applicationContext)
    private val sheetsSyncService = GoogleSheetsSyncService(hybridRepository)
    private val networkMonitor = NetworkStatusMonitor(application.applicationContext)
    private val sentenceCoachService = SentenceCoachAiService()
    private val practiceAiService = PracticeAiService()
    private val examParserAiService = ExamParserAiService()
    private val reviewDiffAiService = ReviewDiffAiService()
    private val personalMaterialAiService = PersonalMaterialAiService()

    private val moodOverride = MutableStateFlow(TeacherMood.TEACHING)
    private val isSpeaking = MutableStateFlow(true)
    private val snackbarMessage = MutableStateFlow<String?>(null)
    private val isNotificationSheetOpen = MutableStateFlow(false)
    private val activePushBanner = MutableStateFlow<ActivePushBanner?>(null)
    private val sentenceAiState = MutableStateFlow<SentenceAiUiState>(SentenceAiUiState.Idle)
    private val syntaxBreakdownState = MutableStateFlow<SyntaxBreakdownUiState>(SyntaxBreakdownUiState.Idle)
    private val speakingAiState = MutableStateFlow<SpeakingAiUiState>(SpeakingAiUiState.Idle)
    private val writingAiState = MutableStateFlow<WritingAiUiState>(WritingAiUiState.Idle)
    private val examParserState = MutableStateFlow<ExamParserUiState>(ExamParserUiState.Idle)
    private val activeExamSession = MutableStateFlow<ActiveExamSession?>(null)
    private val activeExamSubTab = MutableStateFlow(ExamHubSubTab.SIMULATION)
    private val reviewDiffAiState = MutableStateFlow<ReviewDiffAiUiState>(ReviewDiffAiUiState.Idle)
    private val deepQuestionAiResults = MutableStateFlow<Map<String, DeepQuestionAnalysis>>(emptyMap())
    private val loadingQuestionAnalysisIds = MutableStateFlow<Set<String>>(emptySet())
    private val questionAnalysisErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    private val pendingPracticeSkill = MutableStateFlow<PracticeSkill?>(null)
    private val pendingPracticeFocusNote = MutableStateFlow<String?>(null)
    private val isThemeSheetOpen = MutableStateFlow(false)
    private val isSettingsSheetOpen = MutableStateFlow(false)
    private val cloudSyncState = MutableStateFlow<CloudSyncUiState>(CloudSyncUiState.Idle)
    private val isPersonalLibraryOpen = MutableStateFlow(false)
    private val isBulkImporterOpen = MutableStateFlow(false)
    private val bulkImporterDataset = MutableStateFlow(BulkImportDataset.VOCABULARY)
    private val personalMaterialAiState = MutableStateFlow<PersonalMaterialAiUiState>(PersonalMaterialAiUiState.Idle)
    private val isSecuritySheetOpen = MutableStateFlow(false)
    private val isAddMaterialsSheetOpen = MutableStateFlow(false)
    private val isSessionPinUnlocked = MutableStateFlow(false)
    private val isStreakSheetOpen = MutableStateFlow(false)
    private val todayIsoDateFlow = MutableStateFlow(StreakManager.todayIsoDate())
    private val isAvatarPickerOpen = MutableStateFlow(false)
    private val settingsFocusSchedule = MutableStateFlow(false)

    private var speechPulseJob: Job? = null
    private var bannerAutoDismissJob: Job? = null
    private var lastFiredMinuteKey: String? = null

    private data class EphemeralUiOverlay(
        val mood: TeacherMood,
        val speaking: Boolean,
        val snackbar: String?,
        val sheetOpen: Boolean,
        val banner: ActivePushBanner?,
        val sentenceAi: SentenceAiUiState,
        val syntaxBreakdown: SyntaxBreakdownUiState = SyntaxBreakdownUiState.Idle,
        val speakingAi: SpeakingAiUiState = SpeakingAiUiState.Idle,
        val writingAi: WritingAiUiState = WritingAiUiState.Idle,
        val examParser: ExamParserUiState = ExamParserUiState.Idle,
        val examSession: ActiveExamSession? = null,
        val examSubTab: ExamHubSubTab = ExamHubSubTab.SIMULATION,
        val reviewDiffAi: ReviewDiffAiUiState = ReviewDiffAiUiState.Idle,
        val deepQuestionAi: Map<String, DeepQuestionAnalysis> = emptyMap(),
        val loadingQuestionIds: Set<String> = emptySet(),
        val questionErrors: Map<String, String> = emptyMap(),
        val pendingSkill: PracticeSkill? = null,
        val pendingFocusNote: String? = null,
        val themeSheetOpen: Boolean = false
    )

    private data class DeepAnalysisOverlay(
        val subTab: ExamHubSubTab,
        val results: Map<String, DeepQuestionAnalysis>,
        val loadingIds: Set<String>,
        val errors: Map<String, String>
    )

    private val practiceAiFlow = combine(
        syntaxBreakdownState,
        speakingAiState,
        writingAiState
    ) { syntax, speaking, writing ->
        Triple(syntax, speaking, writing)
    }

    private val examOverlayFlow = combine(
        examParserState,
        activeExamSession
    ) { parser, session ->
        parser to session
    }

    private val deepAnalysisOverlayFlow = combine(
        activeExamSubTab,
        deepQuestionAiResults,
        loadingQuestionAnalysisIds,
        questionAnalysisErrors
    ) { subTab, results, loadingIds, errors ->
        DeepAnalysisOverlay(subTab, results, loadingIds, errors)
    }

    private val reviewOverlayFlow = combine(
        reviewDiffAiState,
        pendingPracticeSkill,
        pendingPracticeFocusNote,
        isThemeSheetOpen
    ) { diffAi, pendingSkill, focusNote, themeOpen ->
        listOf(diffAi, pendingSkill, focusNote, themeOpen)
    }

    private val overlayFlow = combine(
        moodOverride,
        isSpeaking,
        snackbarMessage,
        isNotificationSheetOpen,
        activePushBanner
    ) { mood, speaking, snackbar, sheetOpen, banner ->
        EphemeralUiOverlay(mood, speaking, snackbar, sheetOpen, banner, SentenceAiUiState.Idle)
    }.combine(sentenceAiState) { overlay, aiState ->
        overlay.copy(sentenceAi = aiState)
    }.combine(practiceAiFlow) { overlay, (syntax, speaking, writing) ->
        overlay.copy(
            syntaxBreakdown = syntax,
            speakingAi = speaking,
            writingAi = writing
        )
    }.combine(examOverlayFlow) { overlay, (parser, session) ->
        overlay.copy(
            examParser = parser,
            examSession = session
        )
    }.combine(deepAnalysisOverlayFlow) { overlay, deep ->
        overlay.copy(
            examSubTab = deep.subTab,
            deepQuestionAi = deep.results,
            loadingQuestionIds = deep.loadingIds,
            questionErrors = deep.errors
        )
    }.combine(reviewOverlayFlow) { overlay, items ->
        overlay.copy(
            reviewDiffAi = items[0] as ReviewDiffAiUiState,
            pendingSkill = items[1] as PracticeSkill?,
            pendingFocusNote = items[2] as String?,
            themeSheetOpen = items[3] as Boolean
        )
    }

    private data class HybridLocalSnapshot(
        val modules: List<IeltsModule> = IeltsCurriculum.modules,
        val vocabulary: List<VocabWordItem> = emptyList(),
        val drills: List<SyntaxExercise> = LearnCurriculum.syntaxExercises,
        val exams: List<ExamPaper> = ExamBankCurriculum.authenticExams,
        val materials: List<PersonalMaterial> = emptyList(),
        val audioBlobs: List<AudioBlobRecord> = emptyList(),
        val studyLogs: List<StudyLogEntry> = emptyList(),
        val counts: LocalStorageCounts = LocalStorageCounts(),
        val isOnline: Boolean = true,
        val syncState: CloudSyncUiState = CloudSyncUiState.Idle,
        val settingsOpen: Boolean = false,
        val libraryOpen: Boolean = false,
        val importerOpen: Boolean = false,
        val importerDataset: BulkImportDataset = BulkImportDataset.VOCABULARY,
        val materialAi: PersonalMaterialAiUiState = PersonalMaterialAiUiState.Idle,
        val securitySheetOpen: Boolean = false,
        val addMaterialsSheetOpen: Boolean = false,
        val sessionPinUnlocked: Boolean = false,
        val streakSheetOpen: Boolean = false,
        val todayIsoDate: String = StreakManager.todayIsoDate(),
        val avatarPickerOpen: Boolean = false,
        val focusScheduleInSettings: Boolean = false
    )

    private val hybridContentFlow = combine(
        hybridRepository.modulesFlow,
        hybridRepository.vocabularyFlow,
        hybridRepository.sentenceDrillsFlow,
        hybridRepository.examsFlow,
        hybridRepository.personalMaterialsFlow
    ) { mods, vocabs, drills, exams, materials ->
        HybridLocalSnapshot(
            modules = mods.ifEmpty { IeltsCurriculum.modules },
            vocabulary = vocabs,
            drills = drills.ifEmpty { LearnCurriculum.syntaxExercises },
            exams = exams.ifEmpty { ExamBankCurriculum.authenticExams },
            materials = materials
        )
    }.combine(
        combine(
            hybridRepository.audioBlobsFlow,
            hybridRepository.studyLogsFlow,
            hybridRepository.countsFlow,
            networkMonitor.isOnline,
            combine(cloudSyncState, isSettingsSheetOpen) { sync, open -> sync to open }
        ) { blobs, logs, counts, online, (sync, open) ->
            listOf(blobs, logs, counts, online, sync, open)
        }
    ) { snap, extra ->
        @Suppress("UNCHECKED_CAST")
        snap.copy(
            audioBlobs = extra[0] as List<AudioBlobRecord>,
            studyLogs = extra[1] as List<StudyLogEntry>,
            counts = extra[2] as LocalStorageCounts,
            isOnline = extra[3] as Boolean,
            syncState = extra[4] as CloudSyncUiState,
            settingsOpen = extra[5] as Boolean
        )
    }.combine(
        combine(
            isPersonalLibraryOpen,
            isBulkImporterOpen,
            bulkImporterDataset,
            personalMaterialAiState
        ) { libOpen, impOpen, impDataset, matAi ->
            listOf(libOpen, impOpen, impDataset, matAi)
        }
    ) { snap, libExtra ->
        @Suppress("UNCHECKED_CAST")
        snap.copy(
            libraryOpen = libExtra[0] as Boolean,
            importerOpen = libExtra[1] as Boolean,
            importerDataset = libExtra[2] as BulkImportDataset,
            materialAi = libExtra[3] as PersonalMaterialAiUiState
        )
    }.combine(
        combine(
            isSecuritySheetOpen,
            isAddMaterialsSheetOpen,
            isSessionPinUnlocked,
            isStreakSheetOpen,
            todayIsoDateFlow
        ) { secOpen, addMatOpen, unlocked, streakOpen, todayIso ->
            listOf(secOpen, addMatOpen, unlocked, streakOpen, todayIso)
        }
    ) { snap, extra ->
        snap.copy(
            securitySheetOpen = extra[0] as Boolean,
            addMaterialsSheetOpen = extra[1] as Boolean,
            sessionPinUnlocked = extra[2] as Boolean,
            streakSheetOpen = extra[3] as Boolean,
            todayIsoDate = extra[4] as String
        )
    }.combine(
        combine(isAvatarPickerOpen, settingsFocusSchedule) { avatarOpen, focusSchedule ->
            avatarOpen to focusSchedule
        }
    ) { snap, (avatarOpen, focusSchedule) ->
        snap.copy(
            avatarPickerOpen = avatarOpen,
            focusScheduleInSettings = focusSchedule
        )
    }

    val uiState: StateFlow<ApexIeltsUiState> = combine(
        repository.userStateFlow,
        overlayFlow,
        hybridContentFlow,
        AppCheckStatusStore.diagnostics
    ) { persisted, overlay, hybrid, appCheck ->
        GeminiResilientClient.personalGeminiApiKey = persisted.personalGeminiApiKey
        val authenticExams = hybrid.exams.filter { !it.isCustom }.ifEmpty { ExamBankCurriculum.authenticExams }
        val mergedCustomExams = LinkedHashMap<String, ExamPaper>().apply {
            hybrid.exams.filter { it.isCustom }.forEach { put(it.id, it) }
            persisted.customExams.forEach { if (!containsKey(it.id)) put(it.id, it) }
        }.values.toList()

        val resolvedTab = if (persisted.activeTabIndex >= StudyTab.entries.size) {
            StudyTab.EXAM
        } else {
            StudyTab.entries.getOrElse(persisted.activeTabIndex) { StudyTab.ROADMAP }
        }

        val computedStreakStatus = StreakManager.buildStatus(
            storedStreak = persisted.streakDays,
            lastKpiCompletedDate = persisted.lastKpiCompletedDate,
            visualStyle = persisted.streakVisualStyle,
            todayIso = hybrid.todayIsoDate
        )

        ApexIeltsUiState(
            activeTab = resolvedTab,
            activeModuleId = persisted.activeModuleId,
            completedModuleIds = persisted.completedModuleIds,
            streakDays = computedStreakStatus.currentStreak,
            streakStatus = computedStreakStatus,
            isStreakSheetOpen = hybrid.streakSheetOpen,
            reminderEnabled = persisted.reminderEnabled,
            teacherMood = overlay.mood,
            isTeacherSpeaking = overlay.speaking,
            tipCycleIndex = persisted.tipCycleIndex,
            snackbarMessage = overlay.snackbar,
            completedHabitIds = persisted.completedHabitIds,
            weeklyActiveDays = persisted.weeklyActiveDays,
            todayWeekdayId = WeekdayItem.today().id,
            notificationPrefs = persisted.notificationPrefs,
            isNotificationSheetOpen = overlay.sheetOpen,
            activePushBanner = overlay.banner,
            srsWordIds = persisted.srsWordIds,
            learnedVocabIds = persisted.learnedVocabIds,
            sentenceAiState = overlay.sentenceAi,
            syntaxBreakdownState = overlay.syntaxBreakdown,
            speakingAiState = overlay.speakingAi,
            writingAiState = overlay.writingAi,
            activeExamSubTab = overlay.examSubTab,
            customExams = mergedCustomExams,
            examHistory = persisted.examHistory,
            examParserState = overlay.examParser,
            activeExamSession = overlay.examSession,
            reviewDiffAiState = overlay.reviewDiffAi,
            deepQuestionAiResults = overlay.deepQuestionAi,
            loadingQuestionAnalysisIds = overlay.loadingQuestionIds,
            questionAnalysisErrors = overlay.questionErrors,
            pendingPracticeSkill = overlay.pendingSkill,
            pendingPracticeFocusNote = overlay.pendingFocusNote,
            themePrefs = persisted.themePrefs,
            isThemeSheetOpen = overlay.themeSheetOpen,
            modules = hybrid.modules,
            allVocabulary = hybrid.vocabulary,
            sentenceDrills = hybrid.drills,
            authenticExams = authenticExams,
            personalMaterials = hybrid.materials,
            audioBlobs = hybrid.audioBlobs,
            studyLogs = hybrid.studyLogs,
            storageCounts = hybrid.counts,
            isOnline = hybrid.isOnline,
            googleSheetUrl = persisted.googleSheetUrl,
            lastSyncedAtLabel = persisted.lastSyncedAtLabel,
            cloudSyncState = hybrid.syncState,
            isSettingsSheetOpen = hybrid.settingsOpen,
            dailySelfStudyMinutes = persisted.dailySelfStudyMinutes,
            autoGreetDashboardVoice = persisted.autoGreetDashboardVoice,
            isPersonalLibraryOpen = hybrid.libraryOpen,
            isBulkImporterOpen = hybrid.importerOpen,
            bulkImporterDataset = hybrid.importerDataset,
            personalMaterialAiState = hybrid.materialAi,
            personalGeminiApiKey = persisted.personalGeminiApiKey,
            googleCseApiKey = persisted.googleCseApiKey,
            googleCseCx = persisted.googleCseCx,
            appCheckDiagnostics = appCheck,
            isAuthLoaded = true,
            userProfile = persisted.userProfile,
            securityPrefs = persisted.securityPrefs,
            isAppUnlocked = !persisted.securityPrefs.appLockEnabled || hybrid.sessionPinUnlocked,
            isSecuritySheetOpen = hybrid.securitySheetOpen,
            isAddMaterialsSheetOpen = hybrid.addMaterialsSheetOpen,
            isAvatarPickerOpen = hybrid.avatarPickerOpen,
            settingsFocusSchedule = hybrid.focusScheduleInSettings
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ApexIeltsUiState()
    )

    init {
        ApexNotificationHelper.ensureNotificationChannel(application.applicationContext)
        triggerSpeakingPulse(TeacherMood.TEACHING, durationMs = 5_000L)
        startRealtimeReminderWatcher()
        // Boot immediately 100% offline from bundled assets/data/*.json into Room SQLite
        viewModelScope.launch {
            val legacyCustom = runCatching { repository.userStateFlow.first().customExams }.getOrDefault(emptyList())
            hybridRepository.ensureBundledSeedData(legacyCustomExams = legacyCustom)
            checkStreakInactivityForToday()
        }
    }

    private suspend fun checkStreakInactivityForToday() {
        val todayIso = StreakManager.todayIsoDate()
        todayIsoDateFlow.value = todayIso
        val eval = repository.evaluateAndSyncStreakForToday(todayIso)
        if (eval.didResetDueToInactivity && !eval.gentleResetMessageVi.isNullOrBlank()) {
            triggerSpeakingPulse(TeacherMood.ENCOURAGING, durationMs = 5_500L)
            snackbarMessage.value = eval.gentleResetMessageVi
        }
    }

    fun openStreakSheet() {
        isStreakSheetOpen.value = true
    }

    fun closeStreakSheet() {
        isStreakSheetOpen.value = false
    }

    fun selectStreakVisualStyle(style: StreakVisualStyle) {
        viewModelScope.launch {
            repository.setStreakVisualStyle(style)
            snackbarMessage.value = "Đã chuyển biểu tượng chuỗi học sang: ${style.labelVi}!"
        }
    }

    override fun onCleared() {
        super.onCleared()
        networkMonitor.unregister()
    }

    fun selectTab(tab: StudyTab) {
        viewModelScope.launch {
            repository.setActiveTabIndex(tab.index)
        }
        val tabMood = when (tab) {
            StudyTab.ROADMAP -> TeacherMood.TEACHING
            StudyTab.LEARN -> TeacherMood.TEACHING
            StudyTab.PRACTICE -> TeacherMood.LISTENING
            StudyTab.EXAM -> if (activeExamSubTab.value == ExamHubSubTab.CORRECTION) {
                TeacherMood.CHEERING
            } else {
                TeacherMood.ENCOURAGING
            }
        }
        triggerSpeakingPulse(tabMood, durationMs = 3_600L)
    }

    fun selectExamSubTab(subTab: ExamHubSubTab) {
        activeExamSubTab.value = subTab
        val mood = if (subTab == ExamHubSubTab.CORRECTION) TeacherMood.CHEERING else TeacherMood.ENCOURAGING
        triggerSpeakingPulse(mood, durationMs = 3_200L)
    }

    fun selectModule(moduleId: Int) {
        sentenceAiState.value = SentenceAiUiState.Idle
        syntaxBreakdownState.value = SyntaxBreakdownUiState.Idle
        speakingAiState.value = SpeakingAiUiState.Idle
        writingAiState.value = WritingAiUiState.Idle
        viewModelScope.launch {
            repository.setActiveModuleId(moduleId)
        }
        triggerSpeakingPulse(TeacherMood.TEACHING, durationMs = 4_200L)
    }

    fun toggleModuleCompletion(moduleId: Int) {
        viewModelScope.launch {
            val completed = repository.toggleModuleCompleted(moduleId)
            val module = IeltsCurriculum.moduleById(moduleId)
            if (completed) {
                triggerSpeakingPulse(TeacherMood.CHEERING, durationMs = 4_500L)
                snackbarMessage.value = "Tuyệt vời! Đã hoàn thành chủ đề #${module.id}: ${module.titleEn}"
            } else {
                triggerSpeakingPulse(TeacherMood.ENCOURAGING, durationMs = 3_200L)
                snackbarMessage.value = "Đã chuyển chủ đề #${module.id} sang trạng thái đang học"
            }
        }
    }

    fun toggleDailyHabit(habitId: String) {
        viewModelScope.launch {
            val (checked, allCompleted) = repository.toggleDailyHabit(habitId)
            when {
                allCompleted && checked -> {
                    triggerSpeakingPulse(TeacherMood.CHEERING, durationMs = 4_800L)
                    snackbarMessage.value = "Xuất sắc! Bạn đã hoàn thành 100% mục tiêu học hôm nay!"
                }
                checked -> triggerSpeakingPulse(TeacherMood.ENCOURAGING, durationMs = 3_000L)
                else -> triggerSpeakingPulse(TeacherMood.TEACHING, durationMs = 2_400L)
            }
        }
    }

    fun cycleDailyTip() {
        viewModelScope.launch {
            repository.incrementTipCycle()
        }
        triggerSpeakingPulse(TeacherMood.TEACHING, durationMs = 4_000L)
    }

    fun cycleTeacherMood() {
        val allMoods = TeacherMood.entries
        val nextIndex = (moodOverride.value.ordinal + 1) % allMoods.size
        triggerSpeakingPulse(allMoods[nextIndex], durationMs = 4_500L)
    }

    // --- Tab 2 (Học) Learn Actions: Vocab, Gemini AI Sentence Coach, Syntax & Game/SRS ---

    fun markWordStudied(wordId: String) {
        viewModelScope.launch {
            repository.markVocabWordStudied(wordId)
            val studiedCount = uiState.value.learnedVocabIds.size + 1
            if (studiedCount >= 3) {
                repository.markDailyHabitCompleted("habit_vocab_10")
            }
        }
    }

    fun pushWordToSrsQueue(wordId: String, notifyUser: Boolean = false) {
        viewModelScope.launch {
            repository.addWordToSrsQueue(wordId)
            if (notifyUser) {
                val word = LearnCurriculum.wordById(wordId)?.word ?: wordId
                snackbarMessage.value = "Đã thêm \"$word\" vào hàng chờ ôn tập SRS"
            }
        }
    }

    fun markSrsWordMastered(wordId: String) {
        viewModelScope.launch {
            repository.removeWordFromSrsQueue(wordId)
            val word = LearnCurriculum.wordById(wordId)?.word ?: wordId
            triggerSpeakingPulse(TeacherMood.CHEERING, durationMs = 3_200L)
            snackbarMessage.value = "Tuyệt vời! Đã thuộc vững từ \"$word\" trong hàng chờ SRS"
        }
    }

    fun onMiniGameCompleted(score: Int, totalQuestions: Int, bestCombo: Int, missedCount: Int) {
        viewModelScope.launch {
            repository.markDailyHabitCompleted("habit_reflex_1")
            triggerSpeakingPulse(TeacherMood.CHEERING, durationMs = 4_200L)
            snackbarMessage.value = if (missedCount > 0) {
                "Hoàn thành Game ($score/$totalQuestions • Combo x$bestCombo)! Đã thêm $missedCount từ vào SRS & cập nhật Tiến độ hôm nay."
            } else {
                "Hoàn hảo ($score/$totalQuestions • Combo x$bestCombo)! Đã cập nhật Daily Study Tracker!"
            }
        }
    }

    fun onSyntaxExerciseSolved() {
        viewModelScope.launch {
            repository.markDailyHabitCompleted("habit_vocab_10")
        }
        triggerSpeakingPulse(TeacherMood.CHEERING, durationMs = 3_600L)
    }

    fun evaluateUserSentence(targetWord: VocabWordItem, userSentence: String) {
        val trimmed = userSentence.trim()
        if (trimmed.length < 6) {
            sentenceAiState.value = SentenceAiUiState.Error(
                "Vui lòng nhập một câu tiếng Anh hoàn chỉnh có chứa từ \"${targetWord.word}\"."
            )
            return
        }

        val moduleTopic = uiState.value.activeModule.titleEn
        viewModelScope.launch {
            sentenceAiState.value = SentenceAiUiState.Loading
            triggerSpeakingPulse(TeacherMood.TEACHING, durationMs = 8_000L)
            try {
                val evaluation = sentenceCoachService.evaluateSentence(
                    targetWord = targetWord.word,
                    moduleTopicEn = moduleTopic,
                    userSentence = trimmed
                )
                sentenceAiState.value = SentenceAiUiState.Success(evaluation)
                repository.markVocabWordStudied(targetWord.id)
                repository.markDailyHabitCompleted("habit_vocab_10")
                triggerSpeakingPulse(TeacherMood.CHEERING, durationMs = 4_200L)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                sentenceAiState.value = SentenceAiUiState.Error(
                    e.message ?: "Không thể chấm câu lúc này. Vui lòng thử lại."
                )
                triggerSpeakingPulse(TeacherMood.ENCOURAGING, durationMs = 3_000L)
            }
        }
    }

    fun resetSentenceAiState() {
        sentenceAiState.value = SentenceAiUiState.Idle
    }

    // --- Tab 3 (Luyện) Practice Actions: Syntax Breakdown, Speaking AI, Writing AI ---

    fun inspectPracticeSentenceSyntax(sentenceEn: String, sourceContext: String) {
        val cleaned = sentenceEn.trim()
        if (cleaned.isEmpty()) return
        viewModelScope.launch {
            syntaxBreakdownState.value = SyntaxBreakdownUiState.Loading(cleaned)
            triggerSpeakingPulse(TeacherMood.TEACHING, durationMs = 6_000L)
            try {
                val breakdown = practiceAiService.analyzeSentenceSyntax(
                    sentenceEn = cleaned,
                    sourceContext = sourceContext
                )
                syntaxBreakdownState.value = SyntaxBreakdownUiState.Success(breakdown)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                syntaxBreakdownState.value = SyntaxBreakdownUiState.Error(
                    sentenceEn = cleaned,
                    messageVi = e.message ?: "Không thể phân tích cú pháp câu lúc này."
                )
            }
        }
    }

    fun resetSyntaxBreakdownState() {
        syntaxBreakdownState.value = SyntaxBreakdownUiState.Idle
    }

    fun evaluatePracticeSpeaking(
        partLabel: String,
        promptEn: String,
        topicEn: String,
        transcript: String
    ) {
        val cleaned = transcript.trim()
        if (cleaned.length < 10) {
            speakingAiState.value = SpeakingAiUiState.Error(
                "Vui lòng thu âm hoặc nhập câu trả lời Speaking dài hơn một chút để AI chấm chính xác."
            )
            return
        }
        viewModelScope.launch {
            speakingAiState.value = SpeakingAiUiState.Loading
            triggerSpeakingPulse(TeacherMood.LISTENING, durationMs = 7_000L)
            try {
                val result = practiceAiService.evaluateSpeaking(
                    partLabel = partLabel,
                    promptEn = promptEn,
                    topicEn = topicEn,
                    transcript = cleaned
                )
                speakingAiState.value = SpeakingAiUiState.Success(result)
                repository.markDailyHabitCompleted("habit_practice_15")
                triggerSpeakingPulse(TeacherMood.CHEERING, durationMs = 4_200L)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                speakingAiState.value = SpeakingAiUiState.Error(
                    e.message ?: "Không thể chấm IELTS Speaking lúc này. Vui lòng thử lại."
                )
                triggerSpeakingPulse(TeacherMood.ENCOURAGING, durationMs = 3_000L)
            }
        }
    }

    fun resetSpeakingAiState() {
        speakingAiState.value = SpeakingAiUiState.Idle
    }

    fun evaluatePracticeWriting(
        taskLabel: String,
        promptEn: String,
        topicEn: String,
        essay: String,
        wordCount: Int
    ) {
        val cleaned = essay.trim()
        if (wordCount < 12) {
            writingAiState.value = WritingAiUiState.Error(
                "Vui lòng viết ít nhất 12 từ (hoặc bấm Điền nháp Band 6.0) để Gemini AI chấm bài."
            )
            return
        }
        viewModelScope.launch {
            writingAiState.value = WritingAiUiState.Loading
            triggerSpeakingPulse(TeacherMood.TEACHING, durationMs = 7_000L)
            try {
                val result = practiceAiService.evaluateWriting(
                    taskLabel = taskLabel,
                    promptEn = promptEn,
                    topicEn = topicEn,
                    essay = cleaned,
                    wordCount = wordCount
                )
                writingAiState.value = WritingAiUiState.Success(result)
                repository.markDailyHabitCompleted("habit_practice_15")
                triggerSpeakingPulse(TeacherMood.CHEERING, durationMs = 4_200L)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                writingAiState.value = WritingAiUiState.Error(
                    e.message ?: "Không thể chấm IELTS Writing lúc này. Vui lòng thử lại."
                )
                triggerSpeakingPulse(TeacherMood.ENCOURAGING, durationMs = 3_000L)
            }
        }
    }

    fun resetWritingAiState() {
        writingAiState.value = WritingAiUiState.Idle
    }

    fun onPracticeMilestoneCompleted(messageVi: String) {
        viewModelScope.launch {
            repository.markDailyHabitCompleted("habit_practice_15")
            triggerSpeakingPulse(TeacherMood.CHEERING, durationMs = 3_800L)
            snackbarMessage.value = messageVi
        }
    }

    fun showQuickToast(messageVi: String) {
        snackbarMessage.value = messageVi
    }

    // --- Tab 4 (Thi) Exam Hub, AI Exam Parser & Strict Mode Mock Exam ---

    fun startMockExam(exam: ExamPaper, mode: ExamSkillMode) {
        activePushBanner.value = null
        isNotificationSheetOpen.value = false
        activeExamSession.value = ActiveExamSession(exam = exam, mode = mode)
    }

    fun exitMockExamWithoutSubmit() {
        activeExamSession.value = null
        snackbarMessage.value = "Đã thoát phòng thi Strict Mode."
    }

    fun submitMockExam(record: ExamAttemptRecord) {
        activeExamSession.value = null
        activeExamSubTab.value = ExamHubSubTab.CORRECTION
        viewModelScope.launch {
            repository.recordExamAttempt(record)
            repository.markDailyHabitCompleted("habit_practice_15")
            repository.setActiveTabIndex(StudyTab.EXAM.index)
            triggerSpeakingPulse(TeacherMood.CHEERING, durationMs = 4_800L)
            snackbarMessage.value =
                "Đã nộp bài! Dự đoán Band ${String.format(Locale.US, "%.1f", record.estimatedBand)} — Đang mở Đề thi → Chữa đề!"
        }
    }

    fun parseCustomExamWithAi(
        rawText: String,
        pdfBytes: ByteArray?,
        fileName: String?,
        audioUri: String?,
        audioName: String?
    ) {
        if (rawText.isBlank() && pdfBytes == null) {
            examParserState.value = ExamParserUiState.Error(
                "Vui lòng tải file PDF/DOCX/TXT/JSON hoặc dán nội dung đề thi trước khi số hóa."
            )
            return
        }
        viewModelScope.launch {
            examParserState.value = ExamParserUiState.Parsing
            try {
                val parsed = examParserAiService.parseExam(
                    rawText = rawText,
                    pdfBytes = pdfBytes,
                    sourceFileName = fileName,
                    audioUri = audioUri,
                    audioDisplayName = audioName
                )
                examParserState.value = ExamParserUiState.PreviewReady(parsed)
                snackbarMessage.value = "Số hóa thành công! Hãy kiểm tra nhanh đáp án rồi lưu vào Thư viện."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                examParserState.value = ExamParserUiState.Error(
                    e.message ?: "Không thể số hóa đề thi lúc này."
                )
            }
        }
    }

    fun saveCustomExamToLibrary(exam: ExamPaper) {
        viewModelScope.launch {
            hybridRepository.saveCustomExam(exam)
            repository.saveCustomExam(exam)
            examParserState.value = ExamParserUiState.Idle
            snackbarMessage.value = "Đã lưu \"${exam.title}\" vào SQLite & Thư viện đề cá nhân!"
        }
    }

    fun deleteCustomExam(examId: String) {
        viewModelScope.launch {
            hybridRepository.deleteCustomExam(examId)
            repository.deleteCustomExam(examId)
            snackbarMessage.value = "Đã xóa đề thi khỏi Thư viện cá nhân"
        }
    }

    fun resetExamParserState() {
        examParserState.value = ExamParserUiState.Idle
    }

    // --- Tab 5 (Sửa) Review & Fix Actions: Band 8.5+ Diff AI & Error Log -> Practice Loop ---

    fun generateReviewAiDiff(
        skillLabel: String,
        promptEn: String,
        originalText: String
    ) {
        val cleaned = originalText.trim()
        if (cleaned.length < 15) {
            reviewDiffAiState.value = ReviewDiffAiUiState.Error(
                "Nội dung bài làm quá ngắn để Gemini AI tạo bản nâng cấp Band 8.5+."
            )
            return
        }
        viewModelScope.launch {
            reviewDiffAiState.value = ReviewDiffAiUiState.Loading
            triggerSpeakingPulse(TeacherMood.TEACHING, durationMs = 6_500L)
            try {
                val comparison = reviewDiffAiService.generateBand85Diff(
                    skillLabel = skillLabel,
                    promptSummaryEn = promptEn,
                    originalText = cleaned
                )
                reviewDiffAiState.value = ReviewDiffAiUiState.Success(comparison)
                triggerSpeakingPulse(TeacherMood.CHEERING, durationMs = 4_200L)
                snackbarMessage.value = "Gemini AI đã tạo xong bản nâng cấp Band 8.5+ theo đúng dàn ý của bạn!"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                reviewDiffAiState.value = ReviewDiffAiUiState.Error(
                    e.message ?: "Không thể tạo bản so sánh nâng cấp lúc này."
                )
                triggerSpeakingPulse(TeacherMood.ENCOURAGING, durationMs = 3_000L)
            }
        }
    }

    fun resetReviewAiDiff() {
        reviewDiffAiState.value = ReviewDiffAiUiState.Idle
    }

    fun generateDeepQuestionAnalysis(item: ReviewErrorLogItem) {
        if (item.id in loadingQuestionAnalysisIds.value) return
        viewModelScope.launch {
            loadingQuestionAnalysisIds.value = loadingQuestionAnalysisIds.value + item.id
            questionAnalysisErrors.value = questionAnalysisErrors.value - item.id
            triggerSpeakingPulse(TeacherMood.TEACHING, durationMs = 6_000L)
            try {
                val result = reviewDiffAiService.generateDeepQuestionAnalysis(
                    skillLabel = item.skill.labelEn,
                    questionTypeVi = item.questionTypeVi,
                    promptEn = item.promptEn,
                    userAnswer = item.userAnswer,
                    correctAnswer = item.correctAnswer,
                    passageExcerptEn = item.passageExcerptEn
                )
                deepQuestionAiResults.value = deepQuestionAiResults.value + (item.id to result)
                triggerSpeakingPulse(TeacherMood.CHEERING, durationMs = 3_600L)
                snackbarMessage.value = "Gemini AI đã phân tích chuyên sâu Câu ${item.questionNumber}!"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                questionAnalysisErrors.value = questionAnalysisErrors.value + (
                    item.id to (e.message ?: "Không thể phân tích sâu câu này lúc này. Vui lòng thử lại.")
                )
                triggerSpeakingPulse(TeacherMood.ENCOURAGING, durationMs = 3_000L)
            } finally {
                loadingQuestionAnalysisIds.value = loadingQuestionAnalysisIds.value - item.id
            }
        }
    }

    fun practiceWeakSkillFromReview(skill: PracticeSkill, focusNoteVi: String) {
        pendingPracticeSkill.value = skill
        pendingPracticeFocusNote.value = focusNoteVi
        selectTab(StudyTab.PRACTICE)
        snackbarMessage.value = "Đã chuyển sang Tab 3 Luyện (${skill.labelEn}) • $focusNoteVi"
    }

    fun clearPendingPracticeSkill() {
        pendingPracticeSkill.value = null
    }

    fun dismissPracticeFocusNote() {
        pendingPracticeFocusNote.value = null
    }

    // --- Global Theme Manager actions ---

    fun openThemeSheet() {
        isThemeSheetOpen.value = true
    }

    fun closeThemeSheet() {
        isThemeSheetOpen.value = false
    }

    fun selectThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
            snackbarMessage.value = "Đã chuyển giao diện: ${mode.labelVi}"
        }
    }

    fun setAutoDarkInExamRoom(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAutoDarkInExamRoom(enabled)
            snackbarMessage.value = if (enabled) {
                "Đã bật tự động Dark Mode trong Phòng thi CD-IELTS"
            } else {
                "Đã tắt tự động Dark Mode trong Phòng thi CD-IELTS"
            }
        }
    }

    fun selectThemePreset(preset: AppThemePreset) {
        viewModelScope.launch {
            repository.setThemePreset(preset)
            snackbarMessage.value = "Đã áp dụng chủ đề: ${preset.titleVi}"
        }
    }

    fun setCustomPrimaryColor(colorHex: Long?) {
        viewModelScope.launch {
            repository.setCustomPrimaryColorHex(colorHex)
        }
    }

    fun setCustomSurfaceColor(colorHex: Long?) {
        viewModelScope.launch {
            repository.setCustomSurfaceColorHex(colorHex)
        }
    }

    fun selectWallpaperPattern(pattern: AppWallpaperPattern) {
        viewModelScope.launch {
            repository.setWallpaperPattern(pattern)
        }
    }

    fun setCustomBackgroundImageUri(uri: Uri) {
        viewModelScope.launch {
            val ctx = getApplication<Application>().applicationContext
            val result = WallpaperStorageHelper.persistPickedWallpaper(ctx, uri)
            result.onSuccess { savedPath ->
                repository.setCustomBackgroundPath(savedPath)
                snackbarMessage.value = "Đã áp dụng hình nền cá nhân cho Master Ielts!"
            }.onFailure { err ->
                snackbarMessage.value = "Không thể tải ảnh nền: ${err.message ?: "Lỗi đọc tệp"}"
            }
        }
    }

    fun clearCustomBackgroundImage() {
        viewModelScope.launch {
            WallpaperStorageHelper.clearPersistedWallpapers(getApplication<Application>().applicationContext)
            repository.setCustomBackgroundPath("")
            snackbarMessage.value = "Đã xóa hình nền tùy chỉnh."
        }
    }

    fun setBackgroundOverlayOpacity(opacity: Float) {
        viewModelScope.launch {
            repository.setBackgroundOverlayOpacity(opacity)
        }
    }

    fun resetThemeToDefault() {
        viewModelScope.launch {
            WallpaperStorageHelper.clearPersistedWallpapers(getApplication<Application>().applicationContext)
            repository.resetThemeCustomization()
            snackbarMessage.value = "Đã khôi phục giao diện mặc định Indigo Sắc Nét!"
        }
    }

    // --- Offline-First Hybrid Data, Google Sheets Cloud Sync & Backup Actions ---

    fun openSettingsSheet() {
        isSettingsSheetOpen.value = true
    }

    fun closeSettingsSheet() {
        isSettingsSheetOpen.value = false
    }

    fun updateGoogleSheetUrl(url: String) {
        viewModelScope.launch {
            repository.setGoogleSheetUrl(url)
        }
    }

    fun saveGoogleCseConfig(apiKey: String, cx: String) {
        viewModelScope.launch {
            repository.saveGoogleCseConfig(apiKey = apiKey, cx = cx)
            com.example.data.VocabImageRepository.clearCachedUrls(getApplication<Application>().applicationContext)
            snackbarMessage.value = if (apiKey.isNotBlank() && cx.isNotBlank()) {
                "Đã lưu Google Custom Search API — thẻ Từ mới sẽ ưu tiên ảnh từ Google!"
            } else {
                "Đã chuyển về chế độ mặc định miễn phí: Wikipedia & Wikimedia Commons!"
            }
        }
    }

    fun syncGoogleSheetsNow(rawUrl: String) {
        val url = rawUrl.trim()
        if (!networkMonitor.isOnline.value) {
            cloudSyncState.value = CloudSyncUiState.Error(
                "Thiết bị đang Ngoại tuyến (Offline). Ứng dụng vẫn hoạt động 100% từ dữ liệu SQLite & JSON cục bộ — hãy bật mạng khi cần đồng bộ Google Sheets."
            )
            return
        }
        viewModelScope.launch {
            repository.setGoogleSheetUrl(url)
            cloudSyncState.value = CloudSyncUiState.Syncing("Đang kết nối Google Sheets...")
            val result = sheetsSyncService.syncAllTabs(
                rawUrlOrId = url,
                onStepProgress = { step ->
                    cloudSyncState.value = CloudSyncUiState.Syncing(step)
                }
            )
            result.onSuccess { outcome ->
                repository.setLastSyncedAt(outcome.syncedAtLabel)
                cloudSyncState.value = CloudSyncUiState.Success(
                    syncedAtLabel = outcome.syncedAtLabel,
                    totalMerged = outcome.totalMerged,
                    tabSummaries = outcome.tabSummaries
                )
                triggerSpeakingPulse(TeacherMood.CHEERING, durationMs = 4_000L)
                snackbarMessage.value = "Đồng bộ Cloud Database thành công! Đã gộp ${outcome.totalMerged} bản ghi."
            }.onFailure { err ->
                cloudSyncState.value = CloudSyncUiState.Error(
                    err.message ?: "Đồng bộ Google Sheets thất bại. Hãy kiểm tra liên kết và quyền chia sẻ công khai."
                )
            }
        }
    }

    suspend fun exportFullBackupJson(): String {
        return hybridRepository.exportFullBackupJson(sheetUrl = uiState.value.googleSheetUrl)
    }

    fun restoreFullBackupJson(rawJson: String) {
        viewModelScope.launch {
            val result = hybridRepository.restoreFullBackupJson(rawJson)
            result.onSuccess { info ->
                info.restoredSheetUrl?.let { repository.setGoogleSheetUrl(it) }
                triggerSpeakingPulse(TeacherMood.CHEERING, durationMs = 3_800L)
                snackbarMessage.value = "Khôi phục thành công ${info.restoredRecordsCount} bản ghi vào SQLite cục bộ!"
            }.onFailure { err ->
                snackbarMessage.value = "Lỗi khôi phục JSON: ${err.message ?: "Tệp không hợp lệ"}"
            }
        }
    }

    fun importOfflineAudioBlob(uri: Uri) {
        viewModelScope.launch {
            val res = hybridRepository.saveAudioBlobFromUri(uri)
            res.onSuccess { blob ->
                snackbarMessage.value = "Đã lưu tệp audio \"${blob.displayName}\" (${blob.sizeReadable}) vào kho Offline!"
            }.onFailure { err ->
                snackbarMessage.value = "Không thể lưu tệp audio: ${err.message ?: "Lỗi đọc file"}"
            }
        }
    }

    fun deleteOfflineAudioBlob(blob: AudioBlobRecord) {
        viewModelScope.launch {
            hybridRepository.deleteAudioBlob(blob)
            snackbarMessage.value = "Đã xóa tệp audio \"${blob.displayName}\""
        }
    }

    fun savePersonalMaterial(material: PersonalMaterial) {
        viewModelScope.launch {
            hybridRepository.savePersonalMaterial(material)
            snackbarMessage.value = "Đã lưu tài liệu tự học \"${material.title}\""
        }
    }

    fun deletePersonalMaterial(materialId: String) {
        viewModelScope.launch {
            hybridRepository.deletePersonalMaterial(materialId)
            snackbarMessage.value = "Đã xóa tài liệu tự học"
        }
    }

    // --- Personal Library Workspace & In-App CSV/JSON Bulk Importer Actions ---

    fun openPersonalLibrary() {
        isPersonalLibraryOpen.value = true
    }

    fun closePersonalLibrary() {
        isPersonalLibraryOpen.value = false
    }

    fun openBulkImporter(dataset: BulkImportDataset = BulkImportDataset.VOCABULARY) {
        bulkImporterDataset.value = dataset
        isBulkImporterOpen.value = true
    }

    fun closeBulkImporter() {
        isBulkImporterOpen.value = false
    }

    fun commitBulkImport(preview: BulkImportPreview) {
        viewModelScope.launch {
            when (preview.dataset) {
                BulkImportDataset.VOCABULARY -> {
                    hybridRepository.mergeVocabulary(preview.vocabEntities)
                    hybridRepository.addStudyLog(
                        "Import",
                        "Nhập ${preview.validRowCount} từ vựng C1–C2 từ ${preview.formatLabel} (${preview.sourceFileName})"
                    )
                }
                BulkImportDataset.SENTENCE_DRILLS -> {
                    hybridRepository.mergeSentenceDrills(preview.drillEntities)
                    hybridRepository.addStudyLog(
                        "Import",
                        "Nhập ${preview.validRowCount} bài tập câu từ ${preview.formatLabel} (${preview.sourceFileName})"
                    )
                }
                BulkImportDataset.EXAMS -> {
                    hybridRepository.mergeExams(preview.examEntities)
                    hybridRepository.addStudyLog(
                        "Import",
                        "Nhập ${preview.validRowCount} đề thi từ ${preview.formatLabel} (${preview.sourceFileName})"
                    )
                }
                BulkImportDataset.PERSONAL_MATERIALS -> {
                    hybridRepository.mergePersonalMaterials(preview.materialEntities)
                    hybridRepository.addStudyLog(
                        "Import",
                        "Nhập ${preview.validRowCount} tài liệu cá nhân từ ${preview.formatLabel} (${preview.sourceFileName})"
                    )
                }
            }
            isBulkImporterOpen.value = false
            triggerSpeakingPulse(TeacherMood.CHEERING, durationMs = 4_000L)
            snackbarMessage.value =
                "Nhập thành công ${preview.validRowCount} mục ${preview.dataset.shortLabelVi} vào SQLite!"
        }
    }

    fun saveHighlightAsFlashcard(word: VocabWordItem) {
        viewModelScope.launch {
            hybridRepository.saveSingleVocabWord(word)
            repository.markDailyHabitCompleted("habit_vocab_10")
            triggerSpeakingPulse(TeacherMood.CHEERING, durationMs = 3_500L)
            snackbarMessage.value =
                "Đã lưu Flashcard \"${word.word}\" vào Module #${word.moduleId} (sẵn sàng trên Tab 2 Học)!"
        }
    }

    fun summarizePersonalMaterialWithAi(material: PersonalMaterial) {
        viewModelScope.launch {
            personalMaterialAiState.value = PersonalMaterialAiUiState.Loading(material.id)
            try {
                val summary = personalMaterialAiService.summarizeAndExtract(material)
                personalMaterialAiState.value = PersonalMaterialAiUiState.Success(summary)
                triggerSpeakingPulse(TeacherMood.CHEERING, durationMs = 4_000L)
                snackbarMessage.value =
                    "Gemini AI đã tóm tắt & rút ${summary.extractedWords.size} từ vựng Band 8.5+!"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                personalMaterialAiState.value = PersonalMaterialAiUiState.Error(
                    materialId = material.id,
                    messageVi = e.message ?: "Không thể phân tích tài liệu lúc này."
                )
            }
        }
    }

    fun logSelfStudySession(materialTitle: String, minutes: Int) {
        viewModelScope.launch {
            val totalToday = repository.recordSelfStudyMinutes(minutes)
            hybridRepository.addStudyLog(
                category = "Self-Study",
                summaryVi = "Tự học ${minutes} phút: $materialTitle (Tổng hôm nay: ${totalToday} phút)"
            )
            triggerSpeakingPulse(TeacherMood.CHEERING, durationMs = 4_200L)
            snackbarMessage.value =
                "Đã ghi nhận +${minutes} phút tự học (Tổng hôm nay: ${totalToday}p) • Đã cập nhật chỉ số chuyên cần!"
        }
    }

    fun uploadMaterialAudio(uri: Uri, onSaved: (localPath: String, displayName: String) -> Unit) {
        viewModelScope.launch {
            val res = hybridRepository.saveAudioBlobFromUri(uri)
            res.onSuccess { blob ->
                onSaved(blob.localPath, blob.displayName)
                snackbarMessage.value = "Đã đính kèm tệp audio \"${blob.displayName}\" (${blob.sizeReadable})"
            }.onFailure { err ->
                snackbarMessage.value = "Lỗi lưu audio: ${err.message ?: "Không đọc được file"}"
            }
        }
    }

    fun jumpToModuleLearnFromLibrary(moduleId: Int) {
        isPersonalLibraryOpen.value = false
        selectModule(moduleId)
        selectTab(StudyTab.LEARN)
    }

    fun startPersonalExamFromLibrary(exam: ExamPaper, mode: ExamSkillMode) {
        isPersonalLibraryOpen.value = false
        startMockExam(exam, mode)
    }

    fun setAutoGreetDashboardVoice(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAutoGreetDashboardVoice(enabled)
            snackbarMessage.value = if (enabled) {
                "Đã bật tự động chào & động viên bằng giọng nói khi mở Dashboard!"
            } else {
                "Đã tắt tự động phát giọng nói khi mở Dashboard"
            }
        }
    }

    // --- Custom Notification Scheduler actions ---

    fun openNotificationSheet() {
        isNotificationSheetOpen.value = true
    }

    fun closeNotificationSheet() {
        isNotificationSheetOpen.value = false
    }

    fun setMasterReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setMasterReminderEnabled(enabled)
            snackbarMessage.value = if (enabled) {
                "Đã bật hệ thống nhắc học hằng ngày"
            } else {
                "Đã tạm tắt nhắc học hằng ngày"
            }
        }
    }

    fun saveReminderSlot(existingId: String?, hour: Int, minute: Int, labelVi: String) {
        val cleanLabel = labelVi.trim().ifBlank { "Ôn luyện IELTS Band 8.5" }
        val slot = ReminderSlot(
            id = existingId ?: "slot_${UUID.randomUUID().toString().take(8)}",
            hour = hour.coerceIn(0, 23),
            minute = minute.coerceIn(0, 59),
            labelVi = cleanLabel,
            enabled = true
        )
        viewModelScope.launch {
            repository.saveReminderSlot(slot)
            snackbarMessage.value = "Đã lưu khung giờ ${slot.formattedTime()} – ${slot.labelVi}"
        }
    }

    fun toggleReminderSlot(slotId: String) {
        viewModelScope.launch {
            repository.toggleReminderSlot(slotId)
        }
    }

    fun deleteReminderSlot(slotId: String) {
        viewModelScope.launch {
            repository.deleteReminderSlot(slotId)
            snackbarMessage.value = "Đã xóa khung giờ nhắc học"
        }
    }

    fun toggleRepeatDay(dayId: String) {
        viewModelScope.launch {
            repository.toggleRepeatDay(dayId)
        }
    }

    fun selectReminderTone(tone: ReminderTone) {
        viewModelScope.launch {
            repository.setReminderTone(tone)
        }
        triggerSpeakingPulse(tone.teacherMood, durationMs = 3_200L)
    }

    fun saveTestReminderSlot(existingId: String?, hour: Int, minute: Int, labelVi: String) {
        val cleanLabel = labelVi.trim().ifBlank { "Thi thử CD-IELTS & Kiểm tra Module" }
        val slot = ReminderSlot(
            id = existingId ?: "test_slot_${UUID.randomUUID().toString().take(8)}",
            hour = hour.coerceIn(0, 23),
            minute = minute.coerceIn(0, 59),
            labelVi = cleanLabel,
            enabled = true
        )
        viewModelScope.launch {
            repository.saveTestReminderSlot(slot)
            snackbarMessage.value = "Đã lưu lịch kiểm tra ${slot.formattedTime()} – ${slot.labelVi}"
        }
    }

    fun toggleTestReminderSlot(slotId: String) {
        viewModelScope.launch {
            repository.toggleTestReminderSlot(slotId)
        }
    }

    fun deleteTestReminderSlot(slotId: String) {
        viewModelScope.launch {
            repository.deleteTestReminderSlot(slotId)
            snackbarMessage.value = "Đã xóa lịch hẹn kiểm tra"
        }
    }

    fun toggleTestRepeatDay(dayId: String) {
        viewModelScope.launch {
            repository.toggleTestRepeatDay(dayId)
        }
    }

    fun openSettingsToSchedule() {
        isNotificationSheetOpen.value = false
        settingsFocusSchedule.value = true
        isSettingsSheetOpen.value = true
    }

    fun clearSettingsFocusSchedule() {
        settingsFocusSchedule.value = false
    }

    fun openAvatarPicker() {
        isAvatarPickerOpen.value = true
    }

    fun closeAvatarPicker() {
        isAvatarPickerOpen.value = false
    }

    fun selectAvatarPreset(presetId: String) {
        viewModelScope.launch {
            repository.selectAvatarPreset(presetId)
            snackbarMessage.value = if (presetId.isBlank()) {
                "Đã đặt lại ảnh đại diện theo chữ cái tên."
            } else {
                "Đã cập nhật ảnh đại diện mới!"
            }
        }
    }

    fun saveCustomAvatarFromUri(uri: Uri) {
        viewModelScope.launch {
            repository.saveCustomAvatarFromUri(uri)
                .onSuccess {
                    snackbarMessage.value = "Đã lưu ảnh đại diện từ thiết bị thành công!"
                }
                .onFailure { err ->
                    snackbarMessage.value = err.message ?: "Không thể lưu ảnh đại diện. Vui lòng thử ảnh khác."
                }
        }
    }

    fun sendTestNotificationNow() {
        val state = uiState.value
        val sampleSlot = state.notificationPrefs.slots.firstOrNull { it.enabled }
            ?: state.notificationPrefs.testSlots.firstOrNull { it.enabled }
            ?: state.notificationPrefs.slots.firstOrNull()
            ?: DefaultHabitData.defaultSlots.first()

        isNotificationSheetOpen.value = false
        dispatchReminderNotification(
            slotLabelVi = sampleSlot.labelVi,
            timeLabel = "Bây giờ",
            tone = state.notificationPrefs.tone,
            activeModule = state.activeModule
        )
    }

    fun onPushBannerTapped() {
        bannerAutoDismissJob?.cancel()
        activePushBanner.value = null
        selectTab(StudyTab.LEARN)
    }

    fun dismissPushBanner() {
        bannerAutoDismissJob?.cancel()
        activePushBanner.value = null
    }

    fun clearSnackbar() {
        snackbarMessage.value = null
    }

    private fun startRealtimeReminderWatcher() {
        viewModelScope.launch {
            var lastCheckedIso = StreakManager.todayIsoDate()
            while (true) {
                delay(15_000L)
                val currentIso = StreakManager.todayIsoDate()
                if (currentIso != lastCheckedIso) {
                    lastCheckedIso = currentIso
                    checkStreakInactivityForToday()
                }
                val state = uiState.value
                val prefs = state.notificationPrefs
                if (!prefs.masterEnabled) continue

                val todayId = WeekdayItem.today().id
                val cal = Calendar.getInstance()
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                val minute = cal.get(Calendar.MINUTE)
                val minuteKey = "${DefaultHabitData.currentDateKey()}-$hour:$minute"
                if (lastFiredMinuteKey == minuteKey) continue

                val matchingVocabSlot = if (prefs.repeatDays.contains(todayId)) {
                    prefs.slots.firstOrNull { it.enabled && it.hour == hour && it.minute == minute }
                } else {
                    null
                }
                val matchingTestSlot = if (prefs.testRepeatDays.contains(todayId)) {
                    prefs.testSlots.firstOrNull { it.enabled && it.hour == hour && it.minute == minute }
                } else {
                    null
                }
                val matchingSlot = matchingVocabSlot ?: matchingTestSlot
                if (matchingSlot != null) {
                    lastFiredMinuteKey = minuteKey
                    dispatchReminderNotification(
                        slotLabelVi = matchingSlot.labelVi,
                        timeLabel = String.format(Locale.US, "%02d:%02d", hour, minute),
                        tone = prefs.tone,
                        activeModule = state.activeModule
                    )
                }
            }
        }
    }

    private fun dispatchReminderNotification(
        slotLabelVi: String,
        timeLabel: String,
        tone: ReminderTone,
        activeModule: IeltsModule
    ) {
        val (title, body) = ApexNotificationHelper.buildReminderContent(
            tone = tone,
            slotLabelVi = slotLabelVi,
            activeModule = activeModule
        )

        ApexNotificationHelper.postStudyReminderNotification(
            context = getApplication<Application>().applicationContext,
            title = title,
            message = body
        )

        activePushBanner.value = ActivePushBanner(
            id = System.currentTimeMillis(),
            titleVi = title,
            messageVi = body,
            slotLabelVi = slotLabelVi,
            timeLabel = timeLabel,
            tone = tone
        )
        triggerSpeakingPulse(tone.teacherMood, durationMs = 5_000L)

        bannerAutoDismissJob?.cancel()
        bannerAutoDismissJob = viewModelScope.launch {
            delay(6_500L)
            activePushBanner.value = null
        }
    }

    fun savePersonalGeminiApiKey(apiKey: String) {
        val cleaned = apiKey.trim()
        GeminiResilientClient.personalGeminiApiKey = cleaned
        viewModelScope.launch {
            repository.setPersonalGeminiApiKey(cleaned)
        }
        snackbarMessage.value = if (cleaned.isNotEmpty()) {
            "Đã lưu Gemini API Key cá nhân cho chế độ dự phòng Sideload!"
        } else {
            "Đã xóa Gemini API Key cá nhân. Ứng dụng sẽ ưu tiên Firebase AI Logic."
        }
    }

    // =========================================================================
    // Authentication, Account Sidebar Sheets, App Lock Security & Reset Progress
    // =========================================================================

    fun openSecuritySheet() {
        isSecuritySheetOpen.value = true
    }

    fun closeSecuritySheet() {
        isSecuritySheetOpen.value = false
    }

    fun openAddMaterialsSheet() {
        isAddMaterialsSheetOpen.value = true
    }

    fun closeAddMaterialsSheet() {
        isAddMaterialsSheetOpen.value = false
    }

    fun registerWithEmail(
        displayName: String,
        email: String,
        password: String,
        onResult: (Result<UserAccountProfile>) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.registerLocalAccount(displayName, email, password)
            result.onSuccess { profile ->
                isSessionPinUnlocked.value = true
                triggerSpeakingPulse(TeacherMood.CHEERING, durationMs = 4_000L)
                snackbarMessage.value = "Chào mừng ${profile.displayName} đến với Master Ielts!"
            }
            onResult(result)
        }
    }

    fun loginWithEmail(
        email: String,
        password: String,
        onResult: (Result<UserAccountProfile>) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.loginLocalAccount(email, password)
            result.onSuccess { profile ->
                isSessionPinUnlocked.value = true
                triggerSpeakingPulse(TeacherMood.CHEERING, durationMs = 4_000L)
                snackbarMessage.value = "Chào mừng trở lại, ${profile.displayName}!"
            }
            onResult(result)
        }
    }

    fun completeGoogleSignIn(
        uid: String,
        displayName: String,
        email: String,
        isFallbackMode: Boolean
    ) {
        viewModelScope.launch {
            val profile = repository.saveGoogleSignInSession(
                uid = uid,
                displayName = displayName,
                email = email,
                isFallbackMode = isFallbackMode
            )
            isSessionPinUnlocked.value = true
            triggerSpeakingPulse(TeacherMood.CHEERING, durationMs = 4_000L)
            snackbarMessage.value = if (isFallbackMode) {
                "Đã kết nối hồ sơ Google Offline/Dev: ${profile.email}"
            } else {
                "Đã kết nối tài khoản Google AI: ${profile.email}"
            }
        }
    }

    fun logoutAccount() {
        viewModelScope.launch {
            GoogleAuthHelper.signOutGoogle(getApplication<Application>().applicationContext)
            repository.clearLoginSession()
            isSettingsSheetOpen.value = false
            isSecuritySheetOpen.value = false
            isAddMaterialsSheetOpen.value = false
            isPersonalLibraryOpen.value = false
            isNotificationSheetOpen.value = false
            isThemeSheetOpen.value = false
            isStreakSheetOpen.value = false
            isAvatarPickerOpen.value = false
            settingsFocusSchedule.value = false
            snackbarMessage.value = "Đã đăng xuất khỏi tài khoản Master Ielts."
        }
    }

    fun changeAccountPassword(
        currentPassword: String,
        newPassword: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.changeLocalAccountPassword(currentPassword, newPassword)
            res.onSuccess {
                snackbarMessage.value = "Đã cập nhật mật khẩu tài khoản an toàn!"
            }
            onResult(res)
        }
    }

    fun configureAppLockPin(
        pin: String,
        enableLock: Boolean = true,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.configureAppLockPin(pin, enableLock)
            res.onSuccess {
                isSessionPinUnlocked.value = true
                snackbarMessage.value = "Đã lưu mã PIN 4 chữ số & bật Khóa ứng dụng!"
            }
            onResult(res)
        }
    }

    fun toggleAppLock(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAppLockEnabled(enabled)
            if (enabled) {
                isSessionPinUnlocked.value = true
            }
            snackbarMessage.value = if (enabled) {
                "Đã bật Khóa ứng dụng Master Ielts."
            } else {
                "Đã tắt Khóa ứng dụng."
            }
        }
    }

    fun toggleBiometricLock(enabled: Boolean) {
        viewModelScope.launch {
            repository.setBiometricLockEnabled(enabled)
            snackbarMessage.value = if (enabled) {
                "Đã bật mở khóa bằng Sinh trắc học."
            } else {
                "Đã tắt mở khóa Sinh trắc học."
            }
        }
    }

    fun verifyPinAndUnlock(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val valid = repository.verifyAppLockPin(pin)
            if (valid) {
                isSessionPinUnlocked.value = true
                snackbarMessage.value = "Đã mở khóa Master Ielts!"
            }
            onResult(valid)
        }
    }

    fun unlockWithBiometricSuccess() {
        isSessionPinUnlocked.value = true
        snackbarMessage.value = "Xác thực sinh trắc học thành công!"
    }

    fun resetStudyProgress() {
        viewModelScope.launch {
            repository.resetStudyProgress()
            hybridRepository.addStudyLog(
                category = "System",
                summaryVi = "Đặt lại tiến độ học tập (Streak, Module, SRS, Lịch sử thi)"
            )
            triggerSpeakingPulse(TeacherMood.ENCOURAGING, durationMs = 4_000L)
            snackbarMessage.value = "Đã đặt lại tiến độ học tập về trạng thái ban đầu!"
        }
    }

    private fun triggerSpeakingPulse(mood: TeacherMood, durationMs: Long) {
        speechPulseJob?.cancel()
        moodOverride.value = mood
        isSpeaking.value = true
        speechPulseJob = viewModelScope.launch {
            delay(durationMs)
            isSpeaking.value = false
        }
    }
}
