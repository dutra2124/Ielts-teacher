package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
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
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.border
import com.example.ui.components.LocalGlassmorphismDisabled
import com.example.ui.components.LocalStrictExamMode
import com.example.ui.components.glassLuminousBorderBrush
import com.example.ui.components.isGlassmorphismActive
import com.example.ui.components.resolvedGlassAlpha
import com.example.ui.components.tactilePress
import com.example.data.ExamHubSubTab
import com.example.data.KpiCelebrationBanner
import com.example.data.StreakVisualStyle
import com.example.data.StudyTab
import com.example.ui.components.AccountSidebarDrawerContent
import com.example.ui.components.AndroidPushNotificationBanner
import com.example.ui.components.AvatarPickerDialog
import com.example.ui.components.CloudSyncSettingsSheet
import com.example.ui.components.DataImporterBottomSheet
import com.example.ui.components.MasteryStreakIcon
import com.example.ui.components.NotificationCenterSheet
import com.example.ui.components.SecuritySettingsSheet
import com.example.ui.components.StreakProgressSheet
import com.example.ui.screens.RoadmapScreen
import com.example.ui.screens.auth.AppLockPinScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.exam.ExamHubScreen
import com.example.ui.screens.exam.MockExamScreen
import com.example.ui.screens.learn.LearnScreen
import com.example.ui.screens.library.PersonalLibraryScreen
import com.example.ui.screens.practice.PracticeScreen
import com.example.ui.screens.review.ReviewScreen
import com.example.ui.screens.settings.AddMaterialsScreen
import com.example.ui.screens.settings.ThemeSettingsScreen
import com.example.ui.theme.ApexIeltsTheme
import com.example.ui.theme.AppBackgroundBackdrop
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CdIeltsVisualMode
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.IndigoOnSurface
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.ProvideAdaptiveLayoutInfo
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.ThemeOnPrimary
import com.example.ui.theme.ThemePrimary
import kotlinx.coroutines.launch

@Composable
fun ApexIeltsApp(
    deepLinkTabIndex: Int? = null,
    onDeepLinkConsumed: () -> Unit = {},
    onThemeDarknessChanged: (Boolean) -> Unit = {},
    viewModel: ApexIeltsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerScope = rememberCoroutineScope()
    val systemDark = isSystemInDarkTheme()

    val isExamActive = uiState.activeExamSession != null
    val examRoomOverrideMode: AppThemeMode? = if (isExamActive) {
        if (uiState.themePrefs.autoDarkInExamRoom) AppThemeMode.CD_IELTS_DARK else AppThemeMode.LIGHT
    } else {
        null
    }
    val isCurrentlyDark = when {
        examRoomOverrideMode != null -> examRoomOverrideMode == AppThemeMode.CD_IELTS_DARK
        uiState.themePrefs.mode == AppThemeMode.SYSTEM &&
            uiState.themePrefs.customSurfaceHex == null -> systemDark
        else -> uiState.themePrefs.effectiveIsDark
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

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = viewModel.createAutoKpiLifecycleObserver()
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val isEligibleStudyContext = uiState.isAuthLoaded &&
        uiState.userProfile.isLoggedIn &&
        (!uiState.securityPrefs.appLockEnabled || uiState.isAppUnlocked) &&
        (uiState.activeExamSession != null ||
            uiState.activeTab == StudyTab.LEARN ||
            uiState.activeTab == StudyTab.PRACTICE ||
            uiState.activeTab == StudyTab.EXAM)

    LaunchedEffect(isEligibleStudyContext) {
        viewModel.setInStudyScreenContext(isEligibleStudyContext)
    }

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }

    val interactionObserverModifier = Modifier.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                awaitPointerEvent(PointerEventPass.Initial)
                viewModel.recordUserInteraction()
            }
        }
    }

    ApexIeltsTheme(
        themePrefs = uiState.themePrefs,
        examRoomOverrideMode = examRoomOverrideMode
    ) {
        ProvideAdaptiveLayoutInfo(
            modifier = Modifier
                .fillMaxSize()
                .then(interactionObserverModifier)
        ) { layoutInfo ->
            // Wait briefly for DataStore session load to prevent Login flicker on returning users
            if (!uiState.isAuthLoaded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceCanvas)
                )
                return@ProvideAdaptiveLayoutInfo
            }

            // FIRST-LAUNCH / SIGNED-OUT GATE: Branded Master Ielts Login & Sign-Up Screen
            if (!uiState.userProfile.isLoggedIn) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LoginScreen(
                        onRegisterWithEmail = viewModel::registerWithEmail,
                        onLoginWithEmail = viewModel::loginWithEmail,
                        onGoogleSignInSuccess = viewModel::completeGoogleSignIn,
                        onShowMessage = viewModel::showQuickToast,
                        savedProfile = uiState.userProfile,
                        modifier = Modifier.fillMaxSize()
                    )
                    ApexSnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                    )
                }
                return@ProvideAdaptiveLayoutInfo
            }

            // OPTIONAL SECURITY GATE: 4-Digit PIN & Biometric App Lock Screen
            if (uiState.securityPrefs.appLockEnabled && !uiState.isAppUnlocked) {
                AppLockPinScreen(
                    userProfile = uiState.userProfile,
                    biometricEnabled = uiState.securityPrefs.biometricEnabled,
                    onVerifyPin = viewModel::verifyPinAndUnlock,
                    onBiometricUnlocked = viewModel::unlockWithBiometricSuccess,
                    onLogoutFallback = viewModel::logoutAccount,
                    modifier = Modifier.fillMaxSize()
                )
                return@ProvideAdaptiveLayoutInfo
            }

            // FULLSCREEN CD-IELTS STRICT MODE: hides all Bottom Navigation, Rail, Drawer, Teacher & Banners
            val activeExam = uiState.activeExamSession
            if (activeExam != null) {
                val initialExamVisualMode = if (uiState.themePrefs.autoDarkInExamRoom || isCurrentlyDark) {
                    CdIeltsVisualMode.EXAM_DARK
                } else {
                    CdIeltsVisualMode.LIGHT
                }
                CompositionLocalProvider(
                    LocalStrictExamMode provides true,
                    LocalGlassmorphismDisabled provides true
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        MockExamScreen(
                            session = activeExam,
                            initialVisualMode = initialExamVisualMode,
                            onSubmitExam = viewModel::submitMockExam,
                            onExitWithoutSubmit = viewModel::exitMockExamWithoutSubmit,
                            modifier = Modifier.fillMaxSize()
                        )
                        KpiCelebrationBannerOverlay(
                            banner = uiState.kpiCelebrationBanner,
                            visualStyle = uiState.streakStatus.visualStyle,
                            onDismiss = viewModel::dismissKpiCelebrationBanner,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .widthIn(max = 560.dp)
                        )
                    }
                }
                return@ProvideAdaptiveLayoutInfo
            }

            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = !uiState.isPersonalLibraryOpen && !uiState.isAddMaterialsSheetOpen && !uiState.isThemeSheetOpen,
                drawerContent = {
                    AccountSidebarDrawerContent(
                        userProfile = uiState.userProfile,
                        currentBand = uiState.currentBand,
                        personalMaterialsCount = uiState.personalMaterials.size,
                        appLockEnabled = uiState.securityPrefs.appLockEnabled,
                        isOnline = uiState.isOnline,
                        onGoogleSignInSuccess = viewModel::completeGoogleSignIn,
                        onOpenPersonalLibrary = viewModel::openPersonalLibrary,
                        onOpenAddMaterialsHub = viewModel::openAddMaterialsSheet,
                        onOpenSettings = viewModel::openSettingsSheet,
                        onOpenSecurity = viewModel::openSecuritySheet,
                        onOpenAvatarPicker = viewModel::openAvatarPicker,
                        onLogout = viewModel::logoutAccount,
                        onShowMessage = viewModel::showQuickToast,
                        onCloseDrawer = { drawerScope.launch { drawerState.close() } }
                    )
                }
            ) {
                AppBackgroundBackdrop(
                    themePrefs = uiState.themePrefs,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val scaffoldBg = if (uiState.themePrefs.hasCustomVisualBackground) {
                        Color.Transparent
                    } else {
                        SurfaceCanvas
                    }
                    val openAccountDrawer: () -> Unit = {
                        drawerScope.launch { drawerState.open() }
                    }

                    if (layoutInfo.useNavigationRail) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            ApexNavigationRail(
                                selectedTab = uiState.activeTab,
                                onSelectTab = viewModel::selectTab
                            )
                            Scaffold(
                                modifier = Modifier.weight(1f),
                                containerColor = scaffoldBg,
                                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                                snackbarHost = { ApexSnackbarHost(snackbarHostState) }
                            ) { innerPadding ->
                                ApexTabContent(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    onOpenAccountDrawer = openAccountDrawer,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                )
                            }
                        }
                    } else {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            containerColor = scaffoldBg,
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
                                onOpenAccountDrawer = openAccountDrawer,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
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

                    // Subtle Celebratory Daily KPI Completion & Streak Evolution Banner
                    KpiCelebrationBannerOverlay(
                        banner = uiState.kpiCelebrationBanner,
                        visualStyle = uiState.streakStatus.visualStyle,
                        onDismiss = viewModel::dismissKpiCelebrationBanner,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .widthIn(max = 560.dp)
                    )

                    // Focused Actionable Notification Center ("Trung tâm thông báo") via Header Bell Icon
                    if (uiState.isNotificationSheetOpen) {
                        val totalVocabDue = (uiState.srsReviewWords.map { it.id } + uiState.unlearnedActiveModuleWords.map { it.id }).toSet().size
                        NotificationCenterSheet(
                            activeModule = uiState.activeModule,
                            visibleNotifications = uiState.visibleNotifications,
                            totalVocabDueCount = totalVocabDue,
                            centerState = uiState.notificationCenterState,
                            unreadCount = uiState.notificationUnreadCount,
                            onMarkNotificationRead = viewModel::markNotificationRead,
                            onMarkAllRead = viewModel::markAllNotificationsRead,
                            onDismissNotification = viewModel::dismissNotification,
                            onClearAllNotifications = viewModel::clearAllNotifications,
                            onToggleNotificationSound = viewModel::toggleNotificationSound,
                            onJumpToLearn = { viewModel.selectTab(StudyTab.LEARN) },
                            onJumpToExams = {
                                viewModel.selectExamSubTab(ExamHubSubTab.SIMULATION)
                                viewModel.selectTab(StudyTab.EXAM)
                            },
                            onOpenScheduleSettings = viewModel::openSettingsToSchedule,
                            onDismiss = viewModel::closeNotificationSheet
                        )
                    }

                    // Fullscreen Live-Preview Theme & Background Customizer Screen
                    if (uiState.isThemeSheetOpen) {
                        ThemeSettingsScreen(
                            prefs = uiState.themePrefs,
                            onSelectPreset = viewModel::selectThemePreset,
                            onSelectPrimaryColor = viewModel::setCustomPrimaryColor,
                            onSelectSurfaceColor = viewModel::setCustomSurfaceColor,
                            onSelectWallpaperPattern = viewModel::selectWallpaperPattern,
                            onPickCustomBackgroundUri = viewModel::setCustomBackgroundImageUri,
                            onClearCustomBackground = viewModel::clearCustomBackgroundImage,
                            onOverlayOpacityChange = viewModel::setBackgroundOverlayOpacity,
                            onGlassmorphismChange = viewModel::setGlassmorphismEnabled,
                            onGlassFrostOpacityChange = viewModel::setGlassFrostOpacity,
                            onAutoDarkExamChange = viewModel::setAutoDarkInExamRoom,
                            onResetDefault = viewModel::resetThemeToDefault,
                            onClose = viewModel::closeThemeSheet,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // General Settings Sheet (Profile Avatar, Theme, FULL Lịch học & Nhắc nhở, Google Sheet Cloud Sync, Audio Vault, Reset Progress)
                    if (uiState.isSettingsSheetOpen) {
                        CloudSyncSettingsSheet(
                            isOnline = uiState.isOnline,
                            sheetUrl = uiState.googleSheetUrl,
                            lastSyncedAtLabel = uiState.lastSyncedAtLabel,
                            storageCounts = uiState.storageCounts,
                            cloudSyncState = uiState.cloudSyncState,
                            notificationPrefs = uiState.notificationPrefs,
                            notificationCenterState = uiState.notificationCenterState,
                            userProfile = uiState.userProfile,
                            audioBlobs = uiState.audioBlobs,
                            studyLogs = uiState.studyLogs,
                            googleCseApiKey = uiState.googleCseApiKey,
                            googleCseCx = uiState.googleCseCx,
                            streakVisualStyle = uiState.streakStatus.visualStyle,
                            currentStreakDays = uiState.streakDays,
                            autoKpi = uiState.autoKpi,
                            focusScheduleSection = uiState.settingsFocusSchedule,
                            onSheetUrlChange = viewModel::updateGoogleSheetUrl,
                            onSyncNow = viewModel::syncGoogleSheetsNow,
                            onSaveGoogleCseConfig = viewModel::saveGoogleCseConfig,
                            onSelectStreakVisualStyle = viewModel::selectStreakVisualStyle,
                            onUpdateDailyKpiTargets = viewModel::updateDailyKpiTargets,
                            onOpenAvatarPicker = viewModel::openAvatarPicker,
                            onMasterReminderEnabledChange = viewModel::setMasterReminderEnabled,
                            onNotificationSoundEnabledChange = viewModel::setNotificationSoundEnabled,
                            onSelectNotificationSound = viewModel::updateNotificationSound,
                            onSaveVocabSlot = viewModel::saveReminderSlot,
                            onToggleVocabSlot = viewModel::toggleReminderSlot,
                            onDeleteVocabSlot = viewModel::deleteReminderSlot,
                            onToggleVocabRepeatDay = viewModel::toggleRepeatDay,
                            onSaveTestSlot = viewModel::saveTestReminderSlot,
                            onToggleTestSlot = viewModel::toggleTestReminderSlot,
                            onDeleteTestSlot = viewModel::deleteTestReminderSlot,
                            onToggleTestRepeatDay = viewModel::toggleTestRepeatDay,
                            onSelectReminderTone = viewModel::selectReminderTone,
                            onSendTestNotificationNow = viewModel::sendTestNotificationNow,
                            onImportAudioUri = viewModel::importOfflineAudioBlob,
                            onDeleteAudioBlob = viewModel::deleteOfflineAudioBlob,
                            onOpenNotificationSheet = viewModel::openNotificationSheet,
                            onOpenThemeSheet = viewModel::openThemeSheet,
                            onResetStudyProgress = viewModel::resetStudyProgress,
                            onDismiss = {
                                viewModel.clearSettingsFocusSchedule()
                                viewModel.closeSettingsSheet()
                            }
                        )
                    }

                    // Interactive User Avatar Picker Dialog (8 Academic Presets + Device Photo Picker)
                    if (uiState.isAvatarPickerOpen) {
                        AvatarPickerDialog(
                            userProfile = uiState.userProfile,
                            onSelectPreset = viewModel::selectAvatarPreset,
                            onPickPhotoUri = viewModel::saveCustomAvatarFromUri,
                            onResetToInitials = { viewModel.selectAvatarPreset("") },
                            onDismiss = viewModel::closeAvatarPicker
                        )
                    }

                    // Compact Streak Progress & 6-Stage Evolution Bottom Sheet (Read-Only Auto KPI)
                    if (uiState.isStreakSheetOpen) {
                        StreakProgressSheet(
                            streakStatus = uiState.streakStatus,
                            autoKpi = uiState.autoKpi,
                            onSelectVisualStyle = viewModel::selectStreakVisualStyle,
                            onDismiss = viewModel::closeStreakSheet
                        )
                    }

                    // Centralized Fullscreen "Thêm tài liệu" (Add Materials Hub: 6 IELTS Categories, PDF/DOCX/CSV/JSON/Audio + AI Structuring)
                    if (uiState.isAddMaterialsSheetOpen) {
                        AddMaterialsScreen(
                            activeModuleId = uiState.activeModuleId,
                            onUploadAudioUri = viewModel::uploadMaterialAudio,
                            onConfirmImport = viewModel::commitBulkImport,
                            onOpenPersonalLibrary = viewModel::openPersonalLibrary,
                            onShowMessage = viewModel::showQuickToast,
                            onClose = viewModel::closeAddMaterialsSheet,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Dedicated Security Sheet (Change Password, App Lock 4-Digit PIN & Biometric, App Check + Gemini Key, Backup JSON)
                    if (uiState.isSecuritySheetOpen) {
                        SecuritySettingsSheet(
                            userProfile = uiState.userProfile,
                            securityPrefs = uiState.securityPrefs,
                            appCheckDiagnostics = uiState.appCheckDiagnostics,
                            personalGeminiApiKey = uiState.personalGeminiApiKey,
                            onChangePassword = viewModel::changeAccountPassword,
                            onConfigureAppLockPin = viewModel::configureAppLockPin,
                            onToggleAppLock = viewModel::toggleAppLock,
                            onToggleBiometricLock = viewModel::toggleBiometricLock,
                            onSavePersonalGeminiApiKey = viewModel::savePersonalGeminiApiKey,
                            onExportBackupJson = viewModel::exportFullBackupJson,
                            onRestoreBackupJson = viewModel::restoreFullBackupJson,
                            onShowMessage = viewModel::showQuickToast,
                            onDismiss = viewModel::closeSecuritySheet
                        )
                    }

                    // Dedicated Fullscreen "Tài liệu cá nhân & Tự học" Workspace Overlay (opened from Sidebar Drawer)
                    // Strict pedagogical guardrail: Self-Study Article Reader always uses solid high-contrast surfaces
                    if (uiState.isPersonalLibraryOpen) {
                        CompositionLocalProvider(LocalGlassmorphismDisabled provides true) {
                            PersonalLibraryScreen(
                                uiState = uiState,
                                onClose = viewModel::closePersonalLibrary,
                                onOpenBulkImporter = { viewModel.openAddMaterialsSheet() },
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
    }
}

@Composable
private fun ApexTabContent(
    uiState: ApexIeltsUiState,
    viewModel: ApexIeltsViewModel,
    onOpenAccountDrawer: () -> Unit,
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
                onCycleTip = viewModel::cycleDailyTip,
                onTeacherTap = viewModel::cycleTeacherMood,
                onOpenNotificationSheet = viewModel::openNotificationSheet,
                onOpenScheduleSettings = viewModel::openSettingsToSchedule,
                onOpenStreakSheet = viewModel::openStreakSheet,
                onOpenThemeSheet = viewModel::openThemeSheet,
                onOpenAccountDrawer = onOpenAccountDrawer,
                onToggleAutoGreetVoice = viewModel::setAutoGreetDashboardVoice,
                onPracticeWeakSkill = viewModel::practiceWeakSkillFromReview,
                onOpenLearnTab = { viewModel.selectTab(StudyTab.LEARN) }
            )

            StudyTab.LEARN -> LearnScreen(
                uiState = uiState,
                onMarkWordStudied = viewModel::markWordStudied,
                onAddWordToSrs = viewModel::pushWordToSrsQueue,
                onMarkSrsMastered = viewModel::markSrsWordMastered,
                onReviewSrsSm2 = viewModel::reviewSrsWordSm2,
                onEvaluateSentence = viewModel::evaluateUserSentence,
                onResetSentenceAi = viewModel::resetSentenceAiState,
                onSyntaxSolved = viewModel::onSyntaxExerciseSolved,
                onMiniGameWordCorrect = viewModel::onMiniGameWordCorrect,
                onMiniGameCompleted = viewModel::onMiniGameCompleted,
                onShowMessage = viewModel::showQuickToast,
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
                onSelectExamSubTab = viewModel::selectExamSubTab,
                onStartExam = viewModel::startMockExam,
                onParseCustomExam = viewModel::parseCustomExamWithAi,
                onSaveCustomExam = viewModel::saveCustomExamToLibrary,
                onDeleteCustomExam = viewModel::deleteCustomExam,
                onResetExamParser = viewModel::resetExamParserState,
                onTeacherTap = viewModel::cycleTeacherMood,
                onGenerateAiDiff = viewModel::generateReviewAiDiff,
                onResetAiDiff = viewModel::resetReviewAiDiff,
                onGenerateDeepAnalysis = viewModel::generateDeepQuestionAnalysis,
                onPushWordToSrs = { wordId -> viewModel.pushWordToSrsQueue(wordId, notifyUser = true) },
                onPracticeWeakSkill = viewModel::practiceWeakSkillFromReview,
                onOpenThemeSheet = viewModel::openThemeSheet,
                onOpenSettingsSheet = viewModel::openSettingsSheet,
                onShowQuickToast = viewModel::showQuickToast,
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
    val glassOn = isGlassmorphismActive()
    val navContainerColor = if (glassOn) {
        CardWhite.copy(alpha = resolvedGlassAlpha(boostAlpha = 0.04f))
    } else {
        CardWhite
    }
    val luminousBrush = glassLuminousBorderBrush()

    NavigationBar(
        containerColor = navContainerColor,
        tonalElevation = 0.dp,
        windowInsets = NavigationBarDefaults.windowInsets,
        modifier = Modifier
            .shadow(
                elevation = if (glassOn) 12.dp else 16.dp,
                ambientColor = ThemePrimary.copy(alpha = 0.14f),
                spotColor = ThemePrimary.copy(alpha = 0.18f)
            )
            .then(
                if (glassOn) {
                    Modifier.border(
                        width = 1.dp,
                        brush = luminousBrush,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                } else {
                    Modifier
                }
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
                        contentDescription = "${tab.labelVi} (${tab.labelEn})",
                        modifier = Modifier.size(28.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.labelVi,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ThemePrimary,
                    selectedTextColor = ThemePrimary,
                    indicatorColor = ThemePrimary.copy(alpha = 0.16f),
                    unselectedIconColor = InkMuted,
                    unselectedTextColor = InkMuted
                ),
                modifier = Modifier
                    .tactilePress(pressedScale = 0.94f)
                    .testTag("nav_tab_${tab.routeId}")
            )
        }
    }
}

@Composable
private fun ApexNavigationRail(
    selectedTab: StudyTab,
    onSelectTab: (StudyTab) -> Unit
) {
    val glassOn = isGlassmorphismActive()
    val railContainerColor = if (glassOn) {
        CardWhite.copy(alpha = resolvedGlassAlpha(boostAlpha = 0.04f))
    } else {
        CardWhite
    }

    NavigationRail(
        containerColor = railContainerColor,
        modifier = Modifier
            .statusBarsPadding()
            .displayCutoutPadding()
            .navigationBarsPadding()
            .testTag("navigation_rail")
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
                        contentDescription = "${tab.labelVi} (${tab.labelEn})",
                        modifier = Modifier.size(28.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.labelVi,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = ThemePrimary,
                    selectedTextColor = ThemePrimary,
                    indicatorColor = ThemePrimary.copy(alpha = 0.16f),
                    unselectedIconColor = InkMuted,
                    unselectedTextColor = InkMuted
                ),
                modifier = Modifier
                    .tactilePress(pressedScale = 0.94f)
                    .testTag("rail_tab_${tab.routeId}")
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ApexSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { data ->
        Snackbar(
            snackbarData = data,
            shape = RoundedCornerShape(16.dp),
            containerColor = ThemePrimary,
            contentColor = ThemeOnPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun KpiCelebrationBannerOverlay(
    banner: KpiCelebrationBanner?,
    visualStyle: StreakVisualStyle,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = banner != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(tween(260)),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(tween(220)),
        modifier = modifier
            .statusBarsPadding()
            .displayCutoutPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        if (banner != null) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CardWhite,
                border = BorderStroke(1.5.dp, EmeraldSuccess.copy(alpha = 0.55f)),
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onDismiss)
                    .testTag("kpi_celebration_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldSuccessLight.copy(alpha = 0.42f))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CardWhite,
                        border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.35f)),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            MasteryStreakIcon(
                                streakDays = banner.streakDays,
                                style = visualStyle,
                                size = 34.dp,
                                contentDescriptionText = "Chuỗi ${banner.streakDays} ngày"
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = banner.messageVi,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = EmeraldOnContainer
                        )
                        val stageTitle = banner.evolvedStageTitleVi
                        if (!stageTitle.isNullOrBlank()) {
                            Text(
                                text = "Tiến hóa cấp độ mới: $stageTitle!",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = InkPrimary
                            )
                        } else {
                            Text(
                                text = "KPI tự động 100% • Đã ghi nhận +1 chuỗi hôm nay",
                                style = MaterialTheme.typography.labelSmall,
                                color = InkSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Đóng thông báo chúc mừng",
                            tint = InkSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun tabIcons(tab: StudyTab): Pair<ImageVector, ImageVector> = when (tab) {
    StudyTab.ROADMAP -> Icons.Filled.Map to Icons.Outlined.Map
    StudyTab.LEARN -> Icons.Filled.MenuBook to Icons.Outlined.MenuBook
    StudyTab.PRACTICE -> Icons.Filled.EditNote to Icons.Outlined.EditNote
    StudyTab.EXAM -> Icons.Filled.Assignment to Icons.Outlined.Assignment
}
