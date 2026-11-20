package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.StudyTab
import com.example.ui.components.AndroidPushNotificationBanner
import com.example.ui.components.CloudSyncSettingsSheet
import com.example.ui.components.DataImporterBottomSheet
import com.example.ui.components.NotificationSchedulerSheet
import com.example.ui.components.ThemeManagerSheet
import com.example.ui.screens.RoadmapScreen
import com.example.ui.screens.exam.ExamHubScreen
import com.example.ui.screens.exam.MockExamScreen
import com.example.ui.screens.learn.LearnScreen
import com.example.ui.screens.library.PersonalLibraryScreen
import com.example.ui.screens.practice.PracticeScreen
import com.example.ui.screens.review.ReviewScreen
import com.example.ui.theme.ApexIeltsTheme
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CdIeltsVisualMode
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.IndigoOnSurface
import com.example.ui.theme.InkMuted
import com.example.ui.theme.LocalApexColors
import com.example.ui.theme.SurfaceCanvas

@Composable
fun ApexIeltsApp(
    deepLinkTabIndex: Int? = null,
    onDeepLinkConsumed: () -> Unit = {},
    onThemeDarknessChanged: (Boolean) -> Unit = {},
    viewModel: ApexIeltsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val systemDark = isSystemInDarkTheme()

    val isExamActive = uiState.activeExamSession != null
    val effectiveThemeMode = when {
        isExamActive && uiState.themePrefs.autoDarkInExamRoom -> AppThemeMode.CD_IELTS_DARK
        else -> uiState.themePrefs.mode
    }
    val isCurrentlyDark = when (effectiveThemeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.NIGHT_STUDY, AppThemeMode.CD_IELTS_DARK -> true
        AppThemeMode.SYSTEM -> systemDark
    }

    LaunchedEffect(isCurrentlyDark) {
        onThemeDarknessChanged(isCurrentlyDark)
    }

    LaunchedEffect(deepLinkTabIndex) {
        if (deepLinkTabIndex != null) {
            val targetTab = StudyTab.entries.getOrNull(deepLinkTabIndex)
            if (targetTab != null) {
                viewModel.selectTab(targetTab)
            }
            onDeepLinkConsumed()
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }

    ApexIeltsTheme(themeMode = effectiveThemeMode) {
        // FULLSCREEN CD-IELTS STRICT MODE: hides all Bottom Navigation, Rail, Teacher & Banners
        val activeExam = uiState.activeExamSession
        if (activeExam != null) {
            val initialExamVisualMode = if (uiState.themePrefs.autoDarkInExamRoom || isCurrentlyDark) {
                CdIeltsVisualMode.EXAM_DARK
            } else {
                CdIeltsVisualMode.LIGHT
            }
            MockExamScreen(
                session = activeExam,
                initialVisualMode = initialExamVisualMode,
                onSubmitExam = viewModel::submitMockExam,
                onExitWithoutSubmit = viewModel::exitMockExamWithoutSubmit,
                modifier = Modifier.fillMaxSize()
            )
            return@ApexIeltsTheme
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceCanvas)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavigationRail = maxWidth >= 600.dp

                if (useNavigationRail) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        ApexNavigationRail(
                            selectedTab = uiState.activeTab,
                            onSelectTab = viewModel::selectTab
                        )
                        Scaffold(
                            modifier = Modifier.weight(1f),
                            containerColor = SurfaceCanvas,
                            contentWindowInsets = WindowInsets(0, 0, 0, 0),
                            snackbarHost = { ApexSnackbarHost(snackbarHostState) }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                ApexTabContent(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .widthIn(max = 620.dp)
                                )
                            }
                        }
                    }
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = SurfaceCanvas,
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        snackbarHost = { ApexSnackbarHost(snackbarHostState) },
                        bottomBar = {
                            ApexBottomNavigationBar(
                                selectedTab = uiState.activeTab,
                                onSelectTab = viewModel::selectTab
                            )
                        }
                    ) { innerPadding ->
                        ApexTabContent(
                            uiState = uiState,
                            viewModel = viewModel,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )
                    }
                }
            }

            // Top Android Push Notification Banner Overlay (tapping navigates directly to Tab 2 Học)
            AndroidPushNotificationBanner(
                banner = uiState.activePushBanner,
                onTapBanner = viewModel::onPushBannerTapped,
                onDismiss = viewModel::dismissPushBanner,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 560.dp)
            )

            // Slide-up Notification Settings Bottom Sheet
            if (uiState.isNotificationSheetOpen) {
                NotificationSchedulerSheet(
                    prefs = uiState.notificationPrefs,
                    onDismiss = viewModel::closeNotificationSheet,
                    onMasterEnabledChange = viewModel::setMasterReminderEnabled,
                    onSaveSlot = viewModel::saveReminderSlot,
                    onToggleSlot = viewModel::toggleReminderSlot,
                    onDeleteSlot = viewModel::deleteReminderSlot,
                    onToggleRepeatDay = viewModel::toggleRepeatDay,
                    onSelectTone = viewModel::selectReminderTone,
                    onSendTestNow = viewModel::sendTestNotificationNow
                )
            }

            // Global Theme Manager Bottom Sheet (Light / Late-Night Study / CD-IELTS Exam Dark / System)
            if (uiState.isThemeSheetOpen) {
                ThemeManagerSheet(
                    prefs = uiState.themePrefs,
                    onSelectMode = viewModel::selectThemeMode,
                    onAutoDarkExamChange = viewModel::setAutoDarkInExamRoom,
                    onDismiss = viewModel::closeThemeSheet
                )
            }

            // Offline-First Hybrid Settings & Google Sheets Cloud Database Sync Sheet
            if (uiState.isSettingsSheetOpen) {
                CloudSyncSettingsSheet(
                    isOnline = uiState.isOnline,
                    sheetUrl = uiState.googleSheetUrl,
                    lastSyncedAtLabel = uiState.lastSyncedAtLabel,
                    storageCounts = uiState.storageCounts,
                    cloudSyncState = uiState.cloudSyncState,
                    personalMaterials = uiState.personalMaterials,
                    audioBlobs = uiState.audioBlobs,
                    studyLogs = uiState.studyLogs,
                    activeModuleId = uiState.activeModuleId,
                    onSheetUrlChange = viewModel::updateGoogleSheetUrl,
                    onSyncNow = viewModel::syncGoogleSheetsNow,
                    onExportBackupJson = viewModel::exportFullBackupJson,
                    onRestoreBackupJson = viewModel::restoreFullBackupJson,
                    onImportAudioUri = viewModel::importOfflineAudioBlob,
                    onDeleteAudioBlob = viewModel::deleteOfflineAudioBlob,
                    onSavePersonalMaterial = viewModel::savePersonalMaterial,
                    onDeletePersonalMaterial = viewModel::deletePersonalMaterial,
                    onOpenThemeSheet = viewModel::openThemeSheet,
                    onShowMessage = viewModel::showQuickToast,
                    onDismiss = viewModel::closeSettingsSheet
                )
            }

            // Dedicated Fullscreen "Tài liệu cá nhân & Tự học" Workspace Overlay (keeps 5-tab Bottom Bar clean)
            if (uiState.isPersonalLibraryOpen) {
                PersonalLibraryScreen(
                    uiState = uiState,
                    onClose = viewModel::closePersonalLibrary,
                    onOpenBulkImporter = viewModel::openBulkImporter,
                    onSaveMaterial = viewModel::savePersonalMaterial,
                    onDeleteMaterial = viewModel::deletePersonalMaterial,
                    onUploadMaterialAudio = viewModel::uploadMaterialAudio,
                    onSaveHighlightFlashcard = viewModel::saveHighlightAsFlashcard,
                    onSummarizeMaterialWithAi = viewModel::summarizePersonalMaterialWithAi,
                    onLogSelfStudySession = viewModel::logSelfStudySession,
                    onJumpToModuleLearn = viewModel::jumpToModuleLearnFromLibrary,
                    onStartPersonalExam = viewModel::startPersonalExamFromLibrary,
                    onShowMessage = viewModel::showQuickToast,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // In-App CSV / JSON Bulk Importer Bottom Sheet (4 Datasets + Live Preview Table)
            if (uiState.isBulkImporterOpen) {
                DataImporterBottomSheet(
                    initialDataset = uiState.bulkImporterDataset,
                    onConfirmImport = viewModel::commitBulkImport,
                    onShowMessage = viewModel::showQuickToast,
                    onDismiss = viewModel::closeBulkImporter
                )
            }
        }
    }
}

@Composable
private fun ApexTabContent(
    uiState: ApexIeltsUiState,
    viewModel: ApexIeltsViewModel,
    modifier: Modifier = Modifier
) {
    // Smooth 200ms fade + directional slide transition between the 5 study flow tabs
    AnimatedContent(
        targetState = uiState.activeTab,
        transitionSpec = {
            val forward = targetState.index >= initialState.index
            val slideOffset = { fullWidth: Int ->
                if (forward) fullWidth / 12 else -fullWidth / 12
            }
            (fadeIn(animationSpec = tween(200)) +
                slideInHorizontally(animationSpec = tween(200), initialOffsetX = slideOffset))
                .togetherWith(
                    fadeOut(animationSpec = tween(180)) +
                        slideOutHorizontally(
                            animationSpec = tween(180),
                            targetOffsetX = { fullWidth ->
                                if (forward) -fullWidth / 12 else fullWidth / 12
                            }
                        )
                )
        },
        modifier = modifier,
        label = "study_tab_transition"
    ) { tab ->
        when (tab) {
            StudyTab.ROADMAP -> RoadmapScreen(
                uiState = uiState,
                onSelectModule = viewModel::selectModule,
                onToggleModuleCompletion = viewModel::toggleModuleCompletion,
                onToggleDailyHabit = viewModel::toggleDailyHabit,
                onCycleTip = viewModel::cycleDailyTip,
                onTeacherTap = viewModel::cycleTeacherMood,
                onOpenNotificationSheet = viewModel::openNotificationSheet,
                onOpenThemeSheet = viewModel::openThemeSheet,
                onOpenSettingsSheet = viewModel::openSettingsSheet,
                onOpenPersonalLibrary = viewModel::openPersonalLibrary,
                onOpenBulkImporter = { viewModel.openBulkImporter() },
                onToggleAutoGreetVoice = viewModel::setAutoGreetDashboardVoice,
                onPracticeWeakSkill = viewModel::practiceWeakSkillFromReview,
                onOpenLearnTab = { viewModel.selectTab(StudyTab.LEARN) }
            )

            StudyTab.LEARN -> LearnScreen(
                uiState = uiState,
                onMarkWordStudied = viewModel::markWordStudied,
                onAddWordToSrs = viewModel::pushWordToSrsQueue,
                onMarkSrsMastered = viewModel::markSrsWordMastered,
                onEvaluateSentence = viewModel::evaluateUserSentence,
                onResetSentenceAi = viewModel::resetSentenceAiState,
                onSyntaxSolved = viewModel::onSyntaxExerciseSolved,
                onMiniGameCompleted = viewModel::onMiniGameCompleted,
                onOpenPersonalLibrary = viewModel::openPersonalLibrary,
                onOpenBulkImporter = { viewModel.openBulkImporter() },
                onReturnToRoadmap = { viewModel.selectTab(StudyTab.ROADMAP) }
            )

            StudyTab.PRACTICE -> PracticeScreen(
                uiState = uiState,
                onInspectSyntaxSentence = viewModel::inspectPracticeSentenceSyntax,
                onResetSyntaxBreakdown = viewModel::resetSyntaxBreakdownState,
                onEvaluateSpeaking = viewModel::evaluatePracticeSpeaking,
                onResetSpeakingAi = viewModel::resetSpeakingAiState,
                onEvaluateWriting = viewModel::evaluatePracticeWriting,
                onResetWritingAi = viewModel::resetWritingAiState,
                onPracticeMilestoneCompleted = viewModel::onPracticeMilestoneCompleted,
                onShowQuickToast = viewModel::showQuickToast,
                onConsumePendingSkill = viewModel::clearPendingPracticeSkill,
                onDismissFocusNote = viewModel::dismissPracticeFocusNote,
                onReturnToRoadmap = { viewModel.selectTab(StudyTab.ROADMAP) }
            )

            StudyTab.EXAM -> ExamHubScreen(
                uiState = uiState,
                onStartExam = viewModel::startMockExam,
                onParseCustomExam = viewModel::parseCustomExamWithAi,
                onSaveCustomExam = viewModel::saveCustomExamToLibrary,
                onDeleteCustomExam = viewModel::deleteCustomExam,
                onResetExamParser = viewModel::resetExamParserState,
                onOpenThemeSheet = viewModel::openThemeSheet,
                onOpenSettingsSheet = viewModel::openSettingsSheet,
                onShowQuickToast = viewModel::showQuickToast,
                onReturnToRoadmap = { viewModel.selectTab(StudyTab.ROADMAP) }
            )

            StudyTab.REVIEW -> ReviewScreen(
                uiState = uiState,
                onTeacherTap = viewModel::cycleTeacherMood,
                onGenerateAiDiff = viewModel::generateReviewAiDiff,
                onResetAiDiff = viewModel::resetReviewAiDiff,
                onPushWordToSrs = { wordId -> viewModel.pushWordToSrsQueue(wordId, notifyUser = true) },
                onPracticeWeakSkill = viewModel::practiceWeakSkillFromReview,
                onReturnToRoadmap = { viewModel.selectTab(StudyTab.ROADMAP) }
            )
        }
    }
}

@Composable
private fun ApexBottomNavigationBar(
    selectedTab: StudyTab,
    onSelectTab: (StudyTab) -> Unit
) {
    NavigationBar(
        containerColor = CardWhite,
        tonalElevation = 0.dp,
        modifier = Modifier
            .shadow(
                elevation = 16.dp,
                ambientColor = ElectricIndigo.copy(alpha = 0.14f),
                spotColor = ElectricIndigo.copy(alpha = 0.18f)
            )
            .testTag("bottom_navigation_bar")
    ) {
        StudyTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            val (filledIcon, outlinedIcon) = tabIcons(tab)
            NavigationBarItem(
                selected = selected,
                onClick = { onSelectTab(tab) },
                icon = {
                    Icon(
                        imageVector = if (selected) filledIcon else outlinedIcon,
                        contentDescription = "${tab.labelVi} (${tab.labelEn})"
                    )
                },
                label = {
                    Text(
                        text = tab.labelVi,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        ),
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = IndigoOnSurface,
                    selectedTextColor = IndigoOnSurface,
                    indicatorColor = ElectricIndigoLight,
                    unselectedIconColor = InkMuted,
                    unselectedTextColor = InkMuted
                ),
                modifier = Modifier.testTag("nav_tab_${tab.routeId}")
            )
        }
    }
}

@Composable
private fun ApexNavigationRail(
    selectedTab: StudyTab,
    onSelectTab: (StudyTab) -> Unit
) {
    NavigationRail(
        containerColor = CardWhite,
        modifier = Modifier.testTag("navigation_rail")
    ) {
        Spacer(modifier = Modifier.weight(0.2f))
        StudyTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            val (filledIcon, outlinedIcon) = tabIcons(tab)
            NavigationRailItem(
                selected = selected,
                onClick = { onSelectTab(tab) },
                icon = {
                    Icon(
                        imageVector = if (selected) filledIcon else outlinedIcon,
                        contentDescription = "${tab.labelVi} (${tab.labelEn})"
                    )
                },
                label = {
                    Text(
                        text = tab.labelVi,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = IndigoOnSurface,
                    selectedTextColor = IndigoOnSurface,
                    indicatorColor = ElectricIndigoLight,
                    unselectedIconColor = InkMuted,
                    unselectedTextColor = InkMuted
                ),
                modifier = Modifier.testTag("rail_tab_${tab.routeId}")
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ApexSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(hostState = hostState) { data ->
        Snackbar(
            snackbarData = data,
            shape = RoundedCornerShape(16.dp),
            containerColor = ElectricIndigo,
            contentColor = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

private fun tabIcons(tab: StudyTab): Pair<ImageVector, ImageVector> = when (tab) {
    StudyTab.ROADMAP -> Icons.Filled.Map to Icons.Outlined.Map
    StudyTab.LEARN -> Icons.Filled.MenuBook to Icons.Outlined.MenuBook
    StudyTab.PRACTICE -> Icons.Filled.EditNote to Icons.Outlined.EditNote
    StudyTab.EXAM -> Icons.Filled.Assignment to Icons.Outlined.Assignment
    StudyTab.REVIEW -> Icons.Filled.AutoFixHigh to Icons.Outlined.AutoFixHigh
}
