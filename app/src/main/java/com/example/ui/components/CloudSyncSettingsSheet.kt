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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Upload
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ExamScoringHelper
import com.example.data.local.AudioBlobRecord
import com.example.data.local.CloudSyncUiState
import com.example.data.local.LocalStorageCounts
import com.example.data.local.PersonalMaterial
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
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Compact Gear Icon Button in the header with live Online/Offline status indicator dot.
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
            // Online / Offline indicator dot
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
 * Settings & Offline-First Hybrid Data Architecture Bottom Sheet:
 * 1. Online/Offline status & Local SQLite engine summary
 * 2. "Đồng bộ Cloud Database" (Google Sheets public CSV/GViz sync across 5 standardized tabs)
 * 3. Full Backup Export / Restore (.json) via zero-permission Android SAF
 * 4. Offline MP3/WAV Audio Vault
 * 5. Personal Self-Study Materials (Personal_Materials) viewer & quick editor
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CloudSyncSettingsSheet(
    isOnline: Boolean,
    sheetUrl: String,
    lastSyncedAtLabel: String,
    storageCounts: LocalStorageCounts,
    cloudSyncState: CloudSyncUiState,
    personalMaterials: List<PersonalMaterial>,
    audioBlobs: List<AudioBlobRecord>,
    studyLogs: List<StudyLogEntry>,
    activeModuleId: Int,
    onSheetUrlChange: (String) -> Unit,
    onSyncNow: (String) -> Unit,
    onExportBackupJson: suspend () -> String,
    onRestoreBackupJson: (String) -> Unit,
    onImportAudioUri: (Uri) -> Unit,
    onDeleteAudioBlob: (AudioBlobRecord) -> Unit,
    onSavePersonalMaterial: (PersonalMaterial) -> Unit,
    onDeletePersonalMaterial: (String) -> Unit,
    onOpenThemeSheet: () -> Unit,
    onShowMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var draftUrl by remember(sheetUrl) { mutableStateOf(sheetUrl) }
    var showSchemaGuide by remember { mutableStateOf(false) }
    var showAddMaterialDialog by remember { mutableStateOf(false) }
    var showLogsExpanded by remember { mutableStateOf(false) }

    val extractedSheetId = remember(draftUrl) {
        GoogleSheetsSyncService.extractSpreadsheetId(draftUrl)
    }

    // SAF Launcher 1: Export Full Backup (.json)
    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val payload = onExportBackupJson()
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(payload.toByteArray(Charsets.UTF_8))
                    }
                }.onSuccess {
                    onShowMessage("Đã xuất tệp sao lưu toàn bộ dữ liệu (.json) thành công!")
                }.onFailure { err ->
                    onShowMessage("Lỗi xuất tệp sao lưu: ${err.localizedMessage ?: "Không rõ"}")
                }
            }
        }
    }

    // SAF Launcher 2: Restore Full Backup (.json)
    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    context.contentResolver.openInputStream(uri)
                        ?.bufferedReader(Charsets.UTF_8)
                        ?.use { it.readText() }
                        ?: error("Không đọc được nội dung tệp JSON.")
                }.onSuccess { rawJson ->
                    onRestoreBackupJson(rawJson)
                }.onFailure { err ->
                    onShowMessage("Lỗi đọc tệp khôi phục: ${err.localizedMessage ?: "Không rõ"}")
                }
            }
        }
    }

    // SAF Launcher 3: Import MP3/WAV Audio Blob into Offline Vault
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
                .verticalScroll(rememberScrollState())
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ElectricIndigoLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CloudSync,
                            contentDescription = null,
                            tint = IndigoOnSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.settings_sheet_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = InkPrimary
                        )
                        Text(
                            text = stringResource(R.string.settings_sheet_subtitle),
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
                        color = CardWhite
                    ) {
                        Text(
                            text = "${storageCounts.totalSyncedRecords} mục",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = IndigoOnSurface,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // ================================================================
            // PANEL 1: Đồng bộ Cloud Database (Google Sheets 5-Tab Connector)
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
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.Storage, contentDescription = null, tint = IndigoOnSurface, modifier = Modifier.size(18.dp))
                            Text(
                                text = stringResource(R.string.sync_panel_title),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = InkPrimary
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

                    // 5-Tab Live Record Count Badges
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
                                        "• Tự động gộp theo ID vào SQLite, không bao giờ tạo bản ghi trùng lặp.",
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

                    // Sync Result Feedback Card
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
            // PANEL 2: Full Backup Export / Restore (.json) & Offline Audio Vault
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.backup_section_title),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = InkPrimary
                    )
                    Text(
                        text = "Xuất toàn bộ dữ liệu SQLite cục bộ (chủ đề, từ vựng, câu trúc 8.5+, đề thi, tài liệu cá nhân & nhật ký học) ra tệp .json hoặc khôi phục lại bất cứ lúc nào.",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { exportBackupLauncher.launch("ApexIELTS_85_FullBackup.json") },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PracticeTeal, contentColor = Color.White),
                            contentPadding = PaddingValues(vertical = 11.dp, horizontal = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("export_full_backup_button")
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = stringResource(R.string.backup_export_btn),
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        OutlinedButton(
                            onClick = { restoreBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, PracticeTeal),
                            contentPadding = PaddingValues(vertical = 11.dp, horizontal = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("restore_full_backup_button")
                        ) {
                            Icon(Icons.Rounded.Upload, contentDescription = null, tint = TealOnSurface, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = stringResource(R.string.backup_restore_btn),
                                style = MaterialTheme.typography.labelMedium,
                                color = TealOnSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Offline Audio Blob Storage (MP3 / WAV)
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
                            text = "Lưu tệp nghe MP3/WAV dung lượng lớn trực tiếp vào bộ nhớ cục bộ (không bị giới hạn 5MB như localStorage).",
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
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = blob.displayName,
                                            style = MaterialTheme.typography.labelMedium,
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
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Xóa audio", tint = RoseAlert, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ================================================================
            // PANEL 3: Personal Self-Study Materials (Personal_Materials)
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
                            Icon(Icons.Rounded.MenuBook, contentDescription = null, tint = TealOnSurface, modifier = Modifier.size(18.dp))
                            Text(
                                text = "${stringResource(R.string.personal_materials_title)} (${personalMaterials.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = InkPrimary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = PracticeTealLight,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { showAddMaterialDialog = true }
                                .testTag("add_personal_material_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, tint = TealOnSurface, modifier = Modifier.size(14.dp))
                                Text(
                                    text = stringResource(R.string.personal_materials_add_btn),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TealOnSurface
                                )
                            }
                        }
                    }

                    personalMaterials.take(5).forEach { mat ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = CardWhite,
                            border = BorderStroke(1.dp, CardBorderSoft)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(11.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = mat.title,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = InkPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { onDeletePersonalMaterial(mat.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Xóa tài liệu", tint = RoseAlert, modifier = Modifier.size(16.dp))
                                    }
                                }
                                if (mat.contentEn.isNotBlank()) {
                                    Text(
                                        text = mat.contentEn,
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = PlusJakartaSansFontFamily),
                                        color = IndigoOnSurface,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (mat.notesVi.isNotBlank()) {
                                    Text(
                                        text = mat.notesVi,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = InkSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
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

            // Quick shortcut to Global Theme Manager + Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenThemeSheet()
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CardBorderSoft),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                ) {
                    Text("Giao diện & Dark Mode", color = InkPrimary, style = MaterialTheme.typography.labelMedium)
                }
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo, contentColor = Color.White),
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text(stringResource(R.string.theme_done_btn))
                }
            }
        }
    }

    if (showAddMaterialDialog) {
        AddPersonalMaterialDialog(
            moduleId = activeModuleId,
            onSave = { material ->
                onSavePersonalMaterial(material)
                showAddMaterialDialog = false
            },
            onDismiss = { showAddMaterialDialog = false }
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

@Composable
private fun AddPersonalMaterialDialog(
    moduleId: Int,
    onSave: (PersonalMaterial) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Writing Notes") }
    var contentEn by remember { mutableStateOf("") }
    var notesVi by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardWhite,
        title = {
            Text(
                text = "Thêm tài liệu tự học Offline",
                style = MaterialTheme.typography.titleMedium,
                color = InkPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tiêu đề ghi chú / bài đọc") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Phân loại (VD: Writing Notes, Reading Article)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contentEn,
                    onValueChange = { contentEn = it },
                    label = { Text("Đoạn tiếng Anh học thuật mẫu") },
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notesVi,
                    onValueChange = { notesVi = it },
                    label = { Text("Ghi chú & mẹo áp dụng (Tiếng Việt)") },
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            PersonalMaterial(
                                id = "pm_${UUID.randomUUID().toString().take(8)}",
                                moduleId = moduleId,
                                title = title.trim(),
                                category = category.trim().ifBlank { "Self-Study Note" },
                                contentEn = contentEn.trim(),
                                notesVi = notesVi.trim(),
                                tags = listOf("Module #$moduleId", category.trim().ifBlank { "Note" }),
                                updatedAt = ExamScoringHelper.nowFormatted()
                            )
                        )
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PracticeTeal)
            ) {
                Text("Lưu vào SQLite")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = InkSecondary)
            }
        }
    )
}
