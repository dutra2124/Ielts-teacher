package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.ImageSearch
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.auth.UserAccountProfile
import com.example.data.AutoKpiSnapshot
import com.example.data.AutoKpiTracker
import com.example.data.NotificationCenterState
import com.example.data.NotificationPreferences
import com.example.data.ReminderTone
import com.example.data.StreakVisualStyle
import com.example.data.local.AudioBlobRecord
import com.example.data.local.CloudSyncUiState
import com.example.data.local.LocalStorageCounts
import com.example.data.local.StudyLogEntry
import com.example.data.sync.GoogleSheetsSyncService
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardWhite
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.GoldMastery
import com.example.ui.theme.GoldMasteryLight
import com.example.ui.theme.GoldOnContainer
import com.example.ui.theme.IndigoOnSurface
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.PracticeTeal
import com.example.ui.theme.PracticeTealLight
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseDiffLight
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.TealOnSurface

/**
 * Compact Gear Icon Button with live Online/Offline status indicator dot (retained for ExamHubScreen).
 */
@Composable
fun SettingsGearButton(
    isOnline: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag("settings_gear_button")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = stringResource(R.string.settings_gear_cd),
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 7.dp, end = 7.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) EmeraldSuccess else GoldMastery)
            )
        }
    }
}

/**
 * Reorganized Settings ("Cài đặt") Bottom Sheet accessed from the Account Sidebar Drawer:
 * 1. Notification Scheduler Entry (Lịch nhắc học hằng ngày)
 * 2. Link Google Sheet Cloud Database (URL input, sync status, "Đồng bộ dữ liệu ngay")
 * 3. Theme Customizer Entry (Giao diện & Dark Mode)
 * 4. Offline Audio Vault (MP3/WAV) & Study Logs
 * 5. Reset Study Progress (with confirmation dialog)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CloudSyncSettingsSheet(
    isOnline: Boolean,
    sheetUrl: String,
    lastSyncedAtLabel: String,
    storageCounts: LocalStorageCounts,
    cloudSyncState: CloudSyncUiState,
    notificationPrefs: NotificationPreferences = NotificationPreferences(),
    notificationCenterState: NotificationCenterState = NotificationCenterState(),
    userProfile: UserAccountProfile = UserAccountProfile(),
    audioBlobs: List<AudioBlobRecord>,
    studyLogs: List<StudyLogEntry>,
    googleCseApiKey: String = "",
    googleCseCx: String = "",
    streakVisualStyle: StreakVisualStyle = StreakVisualStyle.TREE,
    currentStreakDays: Int = 0,
    autoKpi: AutoKpiSnapshot = AutoKpiSnapshot(),
    focusScheduleSection: Boolean = false,
    onSheetUrlChange: (String) -> Unit,
    onSyncNow: (String) -> Unit,
    onSaveGoogleCseConfig: (apiKey: String, cx: String) -> Unit = { _, _ -> },
    onSelectStreakVisualStyle: (StreakVisualStyle) -> Unit = {},
    onUpdateDailyKpiTargets: (targetWords: Int, targetMinutes: Int) -> Unit = { _, _ -> },
    onOpenAvatarPicker: () -> Unit = {},
    onMasterReminderEnabledChange: (Boolean) -> Unit = {},
    onNotificationSoundEnabledChange: (Boolean) -> Unit = {},
    onSelectNotificationSound: (uriString: String?, title: String) -> Unit = { _, _ -> },
    onSaveVocabSlot: (existingId: String?, hour: Int, minute: Int, labelVi: String) -> Unit = { _, _, _, _ -> },
    onToggleVocabSlot: (String) -> Unit = {},
    onDeleteVocabSlot: (String) -> Unit = {},
    onToggleVocabRepeatDay: (String) -> Unit = {},
    onSaveTestSlot: (existingId: String?, hour: Int, minute: Int, labelVi: String) -> Unit = { _, _, _, _ -> },
    onToggleTestSlot: (String) -> Unit = {},
    onDeleteTestSlot: (String) -> Unit = {},
    onToggleTestRepeatDay: (String) -> Unit = {},
    onSelectReminderTone: (ReminderTone) -> Unit = {},
    onSendTestNotificationNow: () -> Unit = {},
    onImportAudioUri: (Uri) -> Unit,
    onDeleteAudioBlob: (AudioBlobRecord) -> Unit,
    onOpenNotificationSheet: () -> Unit = {},
    onOpenThemeSheet: () -> Unit,
    onResetStudyProgress: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    LaunchedEffect(focusScheduleSection) {
        if (focusScheduleSection) {
            scrollState.animateScrollTo(320)
        }
    }

    var draftUrl by remember(sheetUrl) { mutableStateOf(sheetUrl) }
    var draftCseKey by remember(googleCseApiKey) { mutableStateOf(googleCseApiKey) }
    var draftCseCx by remember(googleCseCx) { mutableStateOf(googleCseCx) }
    var showSchemaGuide by remember { mutableStateOf(false) }
    var showVocabImageApiExpanded by remember { mutableStateOf(false) }
    var showLogsExpanded by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val extractedSheetId = remember(draftUrl) {
        GoogleSheetsSyncService.extractSpreadsheetId(draftUrl)
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportAudioUri(uri)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("cloud_sync_settings_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(ElectricIndigoLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = null,
                            tint = IndigoOnSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Cài đặt Master Ielts",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = InkPrimary
                        )
                        Text(
                            text = "Hồ sơ, Lịch học & Nhắc nhở, Giao diện & Google Sheet",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Đóng cài đặt", tint = InkSecondary)
                }
            }

            // Live Online / Offline Status & Last Synced Strip
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isOnline) EmeraldSuccessLight else GoldMasteryLight,
                border = BorderStroke(
                    1.dp,
                    if (isOnline) EmeraldSuccess.copy(alpha = 0.35f) else GoldMastery.copy(alpha = 0.45f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isOnline) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                            contentDescription = null,
                            tint = if (isOnline) EmeraldOnContainer else GoldOnContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = stringResource(
                                    if (isOnline) R.string.sync_status_online else R.string.sync_status_offline
                                ),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (isOnline) EmeraldOnContainer else GoldOnContainer
                            )
                            Text(
                                text = if (lastSyncedAtLabel.isNotBlank()) {
                                    "Đồng bộ gần nhất: $lastSyncedAtLabel"
                                } else {
                                    "Đang chạy dữ liệu JSON tích hợp + SQLite cục bộ"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = InkSecondary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = CardWhite,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Text(
                            text = "${storageCounts.totalSyncedRecords} mục",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = IndigoOnSurface,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // ================================================================
            // PANEL 1: Hồ sơ (Avatar Customization), Theme & Streak Icon Style
            // ================================================================
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Profile & Avatar Customizer Card (Cài đặt -> Hồ sơ)
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = SurfaceCanvas,
                    border = BorderStroke(1.dp, CardBorderSoft),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onOpenAvatarPicker() }
                        .minimumInteractiveComponentSize()
                        .testTag("settings_profile_avatar_btn")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                            UserAvatarCircle(
                                userProfile = userProfile,
                                size = 52.dp,
                                borderColor = ElectricIndigo.copy(alpha = 0.45f),
                                borderWidth = 2.dp
                            )
                            Surface(
                                shape = CircleShape,
                                color = ElectricIndigo,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.PhotoCamera,
                                        contentDescription = "Đổi ảnh đại diện",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hồ sơ & Ảnh đại diện (Avatar)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = InkPrimary
                            )
                            Text(
                                text = "${userProfile.displayName.ifBlank { "Học viên Master Ielts" }} • Chạm để đổi ảnh hoặc chọn 8 mẫu Avatar",
                                style = MaterialTheme.typography.labelSmall,
                                color = InkSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                            contentDescription = null,
                            tint = InkMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Theme Customizer Entry
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = SurfaceCanvas,
                    border = BorderStroke(1.dp, CardBorderSoft),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable {
                            onDismiss()
                            onOpenThemeSheet()
                        }
                        .minimumInteractiveComponentSize()
                        .testTag("settings_open_theme_btn")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(PracticeTealLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Palette,
                                contentDescription = null,
                                tint = TealOnSurface,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Giao diện & Chế độ tối (Theme Customizer)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = InkPrimary
                            )
                            Text(
                                text = "Chế độ Sáng, Học đêm & Phòng thi CD-IELTS Dark Mode",
                                style = MaterialTheme.typography.labelSmall,
                                color = InkSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                            contentDescription = null,
                            tint = InkMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // ================================================================
                // PANEL 1B: FULL "Lịch học & Nhắc nhở" Section inside General Settings
                // ================================================================
                StudyScheduleSettingsSection(
                    prefs = notificationPrefs,
                    notificationCenterState = notificationCenterState,
                    onMasterEnabledChange = onMasterReminderEnabledChange,
                    onSoundEnabledChange = onNotificationSoundEnabledChange,
                    onSelectNotificationSound = onSelectNotificationSound,
                    onSaveVocabSlot = onSaveVocabSlot,
                    onToggleVocabSlot = onToggleVocabSlot,
                    onDeleteVocabSlot = onDeleteVocabSlot,
                    onToggleVocabRepeatDay = onToggleVocabRepeatDay,
                    onSaveTestSlot = onSaveTestSlot,
                    onToggleTestSlot = onToggleTestSlot,
                    onDeleteTestSlot = onDeleteTestSlot,
                    onToggleTestRepeatDay = onToggleTestRepeatDay,
                    onSelectTone = onSelectReminderTone,
                    onSendTestNow = onSendTestNotificationNow
                )

                // Streak Icon Visual Style Toggle: Cái cây (Growth Tree) <-> Ngọn lửa (Mastery Flame)
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = SurfaceCanvas,
                    border = BorderStroke(1.dp, CardBorderSoft),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_streak_style_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Biểu tượng chuỗi học (Streak Mastery)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = InkPrimary
                            )
                            Text(
                                text = "Tiến hóa 6 cấp từ 0 ngày đến IELTS Master 8.5 (60+ ngày)",
                                style = MaterialTheme.typography.labelSmall,
                                color = InkSecondary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StreakVisualStyle.entries.forEach { style ->
                                val isSelected = style == streakVisualStyle
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) ElectricIndigoLight else CardWhite,
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) ElectricIndigo else CardBorderSoft
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { onSelectStreakVisualStyle(style) }
                                        .minimumInteractiveComponentSize()
                                        .testTag("settings_streak_style_${style.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        MasteryStreakIcon(
                                            streakDays = currentStreakDays,
                                            style = style,
                                            size = 24.dp,
                                            contentDescriptionText = style.labelVi
                                        )
                                        Text(
                                            text = style.labelVi,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                                            ),
                                            color = if (isSelected) IndigoOnSurface else InkPrimary,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ================================================================
                // PANEL 1C: Mục tiêu KPI ngày (Tự động 100% — 5–30 từ & 10–60 phút)
                // ================================================================
                DailyKpiTargetsSettingsCard(
                    autoKpi = autoKpi,
                    onUpdateTargets = onUpdateDailyKpiTargets
                )
            }

            // ================================================================
            // PANEL 2: Link Google Sheet Cloud Database (Strictly inside Settings)
            // ================================================================
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = SurfaceCanvas,
                border = BorderStroke(1.dp, CardBorderSoft)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Storage, contentDescription = null, tint = IndigoOnSurface, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Liên kết Google Sheet Cloud Database",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = InkPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = ElectricIndigoLight,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { showSchemaGuide = !showSchemaGuide }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "5 tab chuẩn",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IndigoOnSurface
                                )
                                Icon(
                                    imageVector = if (showSchemaGuide) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                    contentDescription = null,
                                    tint = IndigoOnSurface,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TabCountBadge("Modules", storageCounts.modulesCount)
                        TabCountBadge("Vocabulary", storageCounts.vocabularyCount)
                        TabCountBadge("Sentence_Drills", storageCounts.sentenceDrillsCount)
                        TabCountBadge("Exams", storageCounts.examsCount)
                        TabCountBadge("Personal_Materials", storageCounts.personalMaterialsCount)
                    }

                    AnimatedVisibility(visible = showSchemaGuide) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = CardWhite,
                            border = BorderStroke(1.dp, CardBorderSoft)
                        ) {
                            Column(
                                modifier = Modifier.padding(11.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Cấu trúc Google Sheet công khai (Người xem):",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = InkPrimary
                                )
                                Text(
                                    text = "• 5 tên tab: Modules, Vocabulary, Sentence_Drills, Exams, Personal_Materials\n" +
                                        "• Định dạng cột snake_case (hỗ trợ dấu phẩy trong ngoặc kép & chuỗi JSON trong questions_json)\n" +
                                        "• Tự động gộp theo ID vào SQLite, không tạo bản ghi trùng lặp.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkSecondary
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = draftUrl,
                        onValueChange = {
                            draftUrl = it
                            onSheetUrlChange(it)
                        },
                        label = { Text(stringResource(R.string.sync_sheet_url_label), style = MaterialTheme.typography.labelSmall) },
                        placeholder = {
                            Text(
                                stringResource(R.string.sync_sheet_url_placeholder),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricIndigo,
                            unfocusedBorderColor = CardBorderSoft,
                            focusedContainerColor = CardWhite,
                            unfocusedContainerColor = CardWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("google_sheet_url_input")
                    )

                    if (draftUrl.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (extractedSheetId != null) EmeraldSuccessLight else RoseDiffLight
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (extractedSheetId != null) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
                                    contentDescription = null,
                                    tint = if (extractedSheetId != null) EmeraldOnContainer else RoseAlert,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (extractedSheetId != null) {
                                        "Spreadsheet ID: $extractedSheetId"
                                    } else {
                                        "Chưa nhận diện được Spreadsheet ID hợp lệ từ đường dẫn này."
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (extractedSheetId != null) EmeraldOnContainer else RoseAlert,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    val isSyncing = cloudSyncState is CloudSyncUiState.Syncing
                    Button(
                        onClick = { onSyncNow(draftUrl) },
                        enabled = !isSyncing && extractedSheetId != null,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricIndigo,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(vertical = 13.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("sync_cloud_now_button")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = (cloudSyncState as CloudSyncUiState.Syncing).currentStepVi,
                                style = MaterialTheme.typography.labelMedium
                            )
                        } else {
                            Icon(Icons.Rounded.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.sync_now_btn),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    when (cloudSyncState) {
                        is CloudSyncUiState.Success -> {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = EmeraldSuccessLight,
                                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.35f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(11.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Đồng bộ thành công lúc ${cloudSyncState.syncedAtLabel} • Đã gộp ${cloudSyncState.totalMerged} bản ghi",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = EmeraldOnContainer
                                    )
                                    cloudSyncState.tabSummaries.forEach { tab ->
                                        Text(
                                            text = "${if (tab.succeeded) "✓" else "⚠"} ${tab.tabName}: ${tab.noteVi}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = InkPrimary
                                        )
                                    }
                                }
                            }
                        }

                        is CloudSyncUiState.Error -> {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = RoseDiffLight,
                                border = BorderStroke(1.dp, RoseAlert.copy(alpha = 0.40f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(11.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = RoseAlert, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = cloudSyncState.messageVi,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = InkPrimary
                                    )
                                }
                            }
                        }

                        else -> Unit
                    }
                }
            }

            // ================================================================
            // PANEL 2B: Vocabulary Illustration Source (Zero-Config Wikipedia / Optional Google CSE)
            // ================================================================
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfaceCanvas,
                border = BorderStroke(1.dp, CardBorderSoft)
            ) {
                val hasCustomSearch = googleCseApiKey.isNotBlank() && googleCseCx.isNotBlank()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showVocabImageApiExpanded = !showVocabImageApiExpanded }
                            .testTag("settings_vocab_image_source_toggle"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.ImageSearch, contentDescription = null, tint = IndigoOnSurface, modifier = Modifier.size(18.dp))
                            Column {
                                Text(
                                    text = "Ảnh minh họa từ vựng (Học -> Từ mới)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = InkPrimary
                                )
                                Text(
                                    text = if (hasCustomSearch) {
                                        "Đang ưu tiên Google Custom Search API"
                                    } else {
                                        "Mặc định tự động miễn phí: Wikipedia & Wikimedia Commons"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (hasCustomSearch) EmeraldOnContainer else InkSecondary
                                )
                            }
                        }
                        Icon(
                            imageVector = if (showVocabImageApiExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = null,
                            tint = InkSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(visible = showVocabImageApiExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Không cần cấu hình để dùng ngay. Nếu muốn ưu tiên ảnh Google Image Search, nhập tùy chọn API Key và Search Engine ID (CX) bên dưới:",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSecondary
                            )
                            OutlinedTextField(
                                value = draftCseKey,
                                onValueChange = { draftCseKey = it },
                                label = { Text("Google Custom Search API Key (tùy chọn)", style = MaterialTheme.typography.labelSmall) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricIndigo,
                                    unfocusedBorderColor = CardBorderSoft,
                                    focusedContainerColor = CardWhite,
                                    unfocusedContainerColor = CardWhite
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("google_cse_api_key_input")
                            )
                            OutlinedTextField(
                                value = draftCseCx,
                                onValueChange = { draftCseCx = it },
                                label = { Text("Search Engine ID / CX (tùy chọn)", style = MaterialTheme.typography.labelSmall) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricIndigo,
                                    unfocusedBorderColor = CardBorderSoft,
                                    focusedContainerColor = CardWhite,
                                    unfocusedContainerColor = CardWhite
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("google_cse_cx_input")
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        draftCseKey = ""
                                        draftCseCx = ""
                                        onSaveGoogleCseConfig("", "")
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, CardBorderSoft),
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize()
                                        .testTag("google_cse_reset_btn")
                                ) {
                                    Text("Dùng Wikipedia", style = MaterialTheme.typography.labelSmall, color = InkSecondary)
                                }
                                Button(
                                    onClick = { onSaveGoogleCseConfig(draftCseKey, draftCseCx) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElectricIndigo,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize()
                                        .testTag("google_cse_save_btn")
                                ) {
                                    Text("Lưu cấu hình", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            // ================================================================
            // PANEL 3: Offline MP3 / WAV Audio Vault
            // ================================================================
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfaceCanvas,
                border = BorderStroke(1.dp, CardBorderSoft)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.AudioFile, contentDescription = null, tint = IndigoOnSurface, modifier = Modifier.size(18.dp))
                            Text(
                                text = "${stringResource(R.string.audio_vault_title)} (${audioBlobs.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = InkPrimary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = ElectricIndigoLight,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { audioPickerLauncher.launch(arrayOf("audio/*")) }
                                .testTag("import_offline_audio_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, tint = IndigoOnSurface, modifier = Modifier.size(14.dp))
                                Text(
                                    text = stringResource(R.string.audio_vault_import_btn),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = IndigoOnSurface
                                )
                            }
                        }
                    }

                    if (audioBlobs.isEmpty()) {
                        Text(
                            text = "Chưa có tệp âm thanh MP3/WAV ngoại tuyến nào được tải lên.",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted
                        )
                    } else {
                        audioBlobs.take(4).forEach { blob ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CardWhite,
                                border = BorderStroke(1.dp, CardBorderSoft)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = blob.displayName,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = InkPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${blob.sizeReadable} • ${blob.mimeType}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = InkMuted
                                        )
                                    }
                                    IconButton(onClick = { onDeleteAudioBlob(blob) }) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Xóa tệp audio", tint = RoseAlert, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recent Study & Sync Logs (Collapsible)
            if (studyLogs.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = SurfaceCanvas,
                    border = BorderStroke(1.dp, CardBorderSoft),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { showLogsExpanded = !showLogsExpanded }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Rounded.History, contentDescription = null, tint = InkSecondary, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Nhật ký dữ liệu & học tập (${studyLogs.size})",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = InkPrimary
                                )
                            }
                            Icon(
                                imageVector = if (showLogsExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = null,
                                tint = InkSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        AnimatedVisibility(visible = showLogsExpanded) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                studyLogs.take(8).forEach { log ->
                                    Text(
                                        text = "• [${log.timestampLabel}] (${log.category}) ${log.summaryVi}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = InkSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ================================================================
            // PANEL 4: Reset Study Progress (with Confirmation Dialog)
            // ================================================================
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = RoseDiffLight.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, RoseAlert.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.RestartAlt, contentDescription = null, tint = RoseAlert, modifier = Modifier.size(18.dp))
                        Text(
                            text = stringResource(R.string.settings_reset_progress_title),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = InkPrimary
                        )
                    }
                    Text(
                        text = "Xóa tiến độ 12 Module, chuỗi ngày học (Streak), thói quen hôm nay, hàng đợi SRS và lịch sử thi thử. Tài khoản và Thư viện tài liệu cá nhân vẫn được giữ lại.",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )
                    OutlinedButton(
                        onClick = { showResetConfirmDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, RoseAlert),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseAlert),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("settings_reset_progress_button")
                    ) {
                        Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.settings_reset_progress_btn),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo, contentColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
            ) {
                Text(stringResource(R.string.theme_done_btn))
            }
        }
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            containerColor = CardWhite,
            title = {
                Text(
                    text = "Xác nhận đặt lại tiến độ?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = InkPrimary
                )
            },
            text = {
                Text(
                    text = "Hành động này sẽ đưa chuỗi Streak về 1 ngày và xóa đánh dấu hoàn thành 12 Module, từ đã học, hàng đợi SRS và lịch sử thi thử. Bạn có chắc chắn muốn tiếp tục?",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmDialog = false
                        onResetStudyProgress()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseAlert, contentColor = Color.White),
                    modifier = Modifier.testTag("confirm_reset_progress_btn")
                ) {
                    Text("Đặt lại ngay")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Hủy", color = InkSecondary)
                }
            }
        )
    }
}

@Composable
private fun TabCountBadge(tabName: String, count: Int) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, CardBorderSoft)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Text(
                text = tabName,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = InkSecondary
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = ElectricIndigoLight
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = IndigoOnSurface,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DailyKpiTargetsSettingsCard(
    autoKpi: AutoKpiSnapshot,
    onUpdateTargets: (targetWords: Int, targetMinutes: Int) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = SurfaceCanvas,
        border = BorderStroke(1.dp, CardBorderSoft),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("settings_daily_kpi_targets_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Mục tiêu KPI ngày (Tự động)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = InkPrimary
                    )
                    Text(
                        text = "Hôm nay: ${autoKpi.dailyWordsLearned}/${autoKpi.targetWordsPerDay} từ • ${autoKpi.dailyStudyMinutes}/${autoKpi.targetMinutesPerDay} phút",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (autoKpi.isAllDailyKpiMet) EmeraldSuccess else InkSecondary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (autoKpi.isAllDailyKpiMet) EmeraldSuccessLight else ElectricIndigoLight
                ) {
                    Text(
                        text = if (autoKpi.isAllDailyKpiMet) "✓ Đạt 100%" else "${autoKpi.overallPercent}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (autoKpi.isAllDailyKpiMet) EmeraldOnContainer else IndigoOnSurface,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            // 1. Vocabulary Words per Day (5..30)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, CardBorderSoft),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MenuBook,
                                contentDescription = null,
                                tint = ElectricIndigo,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Từ vựng / ngày",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = InkPrimary
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            KpiStepperIconButton(
                                icon = Icons.Rounded.Remove,
                                contentDescription = "Giảm mục tiêu từ vựng",
                                enabled = autoKpi.targetWordsPerDay > AutoKpiTracker.MIN_TARGET_WORDS,
                                testTagName = "kpi_words_minus_btn",
                                onClick = {
                                    onUpdateTargets(
                                        (autoKpi.targetWordsPerDay - 1).coerceAtLeast(AutoKpiTracker.MIN_TARGET_WORDS),
                                        autoKpi.targetMinutesPerDay
                                    )
                                }
                            )
                            Text(
                                text = "${autoKpi.targetWordsPerDay} từ",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = IndigoOnSurface,
                                modifier = Modifier.testTag("kpi_words_target_value")
                            )
                            KpiStepperIconButton(
                                icon = Icons.Rounded.Add,
                                contentDescription = "Tăng mục tiêu từ vựng",
                                enabled = autoKpi.targetWordsPerDay < AutoKpiTracker.MAX_TARGET_WORDS,
                                testTagName = "kpi_words_plus_btn",
                                onClick = {
                                    onUpdateTargets(
                                        (autoKpi.targetWordsPerDay + 1).coerceAtMost(AutoKpiTracker.MAX_TARGET_WORDS),
                                        autoKpi.targetMinutesPerDay
                                    )
                                }
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(5, 10, 15, 20, 30).forEach { preset ->
                            val selected = autoKpi.targetWordsPerDay == preset
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (selected) ElectricIndigo else SurfaceCanvas,
                                border = BorderStroke(1.dp, if (selected) ElectricIndigo else CardBorderSoft),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { onUpdateTargets(preset, autoKpi.targetMinutesPerDay) }
                                    .testTag("kpi_words_preset_$preset")
                            ) {
                                Text(
                                    text = "$preset từ",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (selected) Color.White else InkSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Study Minutes per Day (10..60)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, CardBorderSoft),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Timer,
                                contentDescription = null,
                                tint = PracticeTeal,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Thời gian học / ngày",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = InkPrimary
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            KpiStepperIconButton(
                                icon = Icons.Rounded.Remove,
                                contentDescription = "Giảm mục tiêu phút học",
                                enabled = autoKpi.targetMinutesPerDay > AutoKpiTracker.MIN_TARGET_MINUTES,
                                testTagName = "kpi_minutes_minus_btn",
                                onClick = {
                                    onUpdateTargets(
                                        autoKpi.targetWordsPerDay,
                                        (autoKpi.targetMinutesPerDay - 5).coerceAtLeast(AutoKpiTracker.MIN_TARGET_MINUTES)
                                    )
                                }
                            )
                            Text(
                                text = "${autoKpi.targetMinutesPerDay} phút",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = TealOnSurface,
                                modifier = Modifier.testTag("kpi_minutes_target_value")
                            )
                            KpiStepperIconButton(
                                icon = Icons.Rounded.Add,
                                contentDescription = "Tăng mục tiêu phút học",
                                enabled = autoKpi.targetMinutesPerDay < AutoKpiTracker.MAX_TARGET_MINUTES,
                                testTagName = "kpi_minutes_plus_btn",
                                onClick = {
                                    onUpdateTargets(
                                        autoKpi.targetWordsPerDay,
                                        (autoKpi.targetMinutesPerDay + 5).coerceAtMost(AutoKpiTracker.MAX_TARGET_MINUTES)
                                    )
                                }
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(10, 15, 20, 30, 45, 60).forEach { preset ->
                            val selected = autoKpi.targetMinutesPerDay == preset
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (selected) PracticeTeal else SurfaceCanvas,
                                border = BorderStroke(1.dp, if (selected) PracticeTeal else CardBorderSoft),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { onUpdateTargets(autoKpi.targetWordsPerDay, preset) }
                                    .testTag("kpi_minutes_preset_$preset")
                            ) {
                                Text(
                                    text = "${preset}p",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (selected) Color.White else InkSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiStepperIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean,
    testTagName: String,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = if (enabled) SurfaceCanvas else SurfaceCanvas.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, CardBorderSoft),
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTagName)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (enabled) InkPrimary else InkMuted.copy(alpha = 0.45f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
