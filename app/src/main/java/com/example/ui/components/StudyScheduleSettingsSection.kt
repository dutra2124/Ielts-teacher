package com.example.ui.components

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.example.data.NotificationCenterState
import com.example.data.NotificationPreferences
import com.example.data.NotificationSoundPreset
import com.example.data.ReminderSlot
import com.example.data.ReminderTone
import com.example.data.WeekdayItem
import com.example.notifications.ApexNotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardSurfaceTinted
import com.example.ui.theme.CardWhite
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.IndigoOnSurface
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.PracticeTeal
import com.example.ui.theme.PracticeTealLight
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.TealOnSurface
import com.example.ui.theme.ThemeOnPrimary
import com.example.ui.theme.ThemePrimary

private enum class SlotEditorTarget { VOCAB, TEST }

/**
 * Complete "Lịch học & Nhắc nhở" panel hosted directly inside General Settings (Cài đặt chung):
 * 1. Master notification toggle + Android POST_NOTIFICATIONS permission status
 * 2. Đặt lịch hẹn học từ mới (Daily Vocab HH:MM slots + repeat days T2–CN)
 * 3. Đặt lịch hẹn kiểm tra (Weekly/Custom Test & Mock Exam HH:MM slots + repeat days T2–CN)
 * 4. AI Teacher reminder tone selector + "Gửi thông báo thử ngay"
 */
@Composable
fun StudyScheduleSettingsSection(
    prefs: NotificationPreferences,
    notificationCenterState: NotificationCenterState = NotificationCenterState(),
    onMasterEnabledChange: (Boolean) -> Unit,
    onSoundEnabledChange: (Boolean) -> Unit = {},
    onSelectNotificationSound: (uriString: String?, title: String) -> Unit = { _, _ -> },
    onSaveVocabSlot: (existingId: String?, hour: Int, minute: Int, labelVi: String) -> Unit,
    onToggleVocabSlot: (String) -> Unit,
    onDeleteVocabSlot: (String) -> Unit,
    onToggleVocabRepeatDay: (String) -> Unit,
    onSaveTestSlot: (existingId: String?, hour: Int, minute: Int, labelVi: String) -> Unit,
    onToggleTestSlot: (String) -> Unit,
    onDeleteTestSlot: (String) -> Unit,
    onToggleTestRepeatDay: (String) -> Unit,
    onSelectTone: (ReminderTone) -> Unit,
    onSendTestNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var hasOsPermission by remember {
        mutableStateOf(ApexNotificationHelper.hasNotificationPermission(context))
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasOsPermission = granted || ApexNotificationHelper.hasNotificationPermission(context)
    }

    var editingSlot by remember { mutableStateOf<ReminderSlot?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var editorTarget by remember { mutableStateOf(SlotEditorTarget.VOCAB) }
    var showSoundPickerDialog by remember { mutableStateOf(false) }

    // Short ~2-second sound preview state with automatic cleanup on dispose
    var activePreviewRingtone by remember { mutableStateOf<Ringtone?>(null) }
    var activePreviewKey by remember { mutableStateOf<String?>(null) }
    var previewStopJob by remember { mutableStateOf<Job?>(null) }

    val stopPreviewSound: () -> Unit = {
        previewStopJob?.cancel()
        previewStopJob = null
        runCatching { activePreviewRingtone?.stop() }
        activePreviewRingtone = null
        activePreviewKey = null
    }

    val playPreviewSound: (soundKey: String, uriString: String?) -> Unit = { soundKey, uriString ->
        if (activePreviewKey == soundKey && activePreviewRingtone != null) {
            stopPreviewSound()
        } else {
            stopPreviewSound()
            val resolvedUri = ApexNotificationHelper.resolveSoundUri(context, uriString)
            val ringtone = runCatching {
                resolvedUri?.let { RingtoneManager.getRingtone(context, it) }
            }.getOrNull()
            if (ringtone != null) {
                activePreviewRingtone = ringtone
                activePreviewKey = soundKey
                runCatching { ringtone.play() }
                previewStopJob = coroutineScope.launch {
                    delay(2_000L)
                    runCatching { ringtone.stop() }
                    if (activePreviewRingtone === ringtone) {
                        activePreviewRingtone = null
                        activePreviewKey = null
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            stopPreviewSound()
        }
    }

    // Android System Ringtone Picker launcher (ACTION_RINGTONE_PICKER)
    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val pickedUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            if (pickedUri != null) {
                val resolvedTitle = ApexNotificationHelper.resolveSoundTitle(context, pickedUri)
                onSelectNotificationSound(pickedUri.toString(), resolvedTitle)
                showSoundPickerDialog = false
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = SurfaceCanvas,
        border = BorderStroke(1.dp, CardBorderSoft),
        modifier = modifier
            .fillMaxWidth()
            .testTag("settings_schedule_section")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section Heading
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(ElectricIndigoLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive,
                        contentDescription = null,
                        tint = IndigoOnSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lịch học & Nhắc nhở",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = InkPrimary
                    )
                    Text(
                        text = "Đặt giờ học từ vựng, lịch kiểm tra định kỳ & giọng AI Teacher",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkSecondary
                    )
                }
            }

            // 1. Master Notification Toggle
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, CardBorderSoft)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.notif_master_title),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = InkPrimary
                        )
                        Text(
                            text = stringResource(R.string.notif_master_desc),
                            style = MaterialTheme.typography.labelSmall,
                            color = InkSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Switch(
                        checked = prefs.masterEnabled,
                        onCheckedChange = onMasterEnabledChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ThemePrimary,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = CardBorderSoft
                        ),
                        modifier = Modifier.testTag("master_reminder_switch")
                    )
                }
            }

            // 2. System Permission Check Card (Android POST_NOTIFICATIONS)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (hasOsPermission) EmeraldSuccessLight.copy(alpha = 0.65f) else CardSurfaceTinted,
                border = BorderStroke(
                    1.dp,
                    if (hasOsPermission) EmeraldSuccess.copy(alpha = 0.35f) else ElectricIndigo.copy(alpha = 0.25f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (hasOsPermission) Icons.Rounded.CheckCircle else Icons.Rounded.NotificationsNone,
                            contentDescription = null,
                            tint = if (hasOsPermission) EmeraldSuccess else ElectricIndigo,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.notif_permission_title),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = InkPrimary
                            )
                            Text(
                                text = stringResource(
                                    if (hasOsPermission) R.string.notif_permission_granted
                                    else R.string.notif_permission_missing
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = InkSecondary
                            )
                        }
                    }

                    if (!hasOsPermission) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    hasOsPermission = ApexNotificationHelper.hasNotificationPermission(context)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ThemePrimary,
                                contentColor = ThemeOnPrimary
                            ),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("request_notification_permission_button")
                        ) {
                            Text(
                                text = stringResource(R.string.notif_permission_btn),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // =================================================================
            // 2B. Âm thanh thông báo & Chọn nhạc chuông thông báo
            // =================================================================
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CardWhite,
                border = BorderStroke(
                    1.dp,
                    if (notificationCenterState.isNotificationSoundEnabled) {
                        ElectricIndigo.copy(alpha = 0.28f)
                    } else {
                        CardBorderSoft
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_notification_sound_card")
            ) {
                val soundEnabled = notificationCenterState.isNotificationSoundEnabled
                val isPreviewingCurrent = activePreviewKey == "current_selected"
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Toggle Row: Âm thanh thông báo (Bật / Tắt)
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
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(if (soundEnabled) ElectricIndigoLight else SurfaceCanvas),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (soundEnabled) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
                                    contentDescription = null,
                                    tint = if (soundEnabled) IndigoOnSurface else InkMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Âm thanh thông báo",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = InkPrimary
                                )
                                Text(
                                    text = if (soundEnabled) "Đang bật âm thanh khi có thông báo" else "Đang tắt (chế độ im lặng)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = InkSecondary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { enabled ->
                                if (!enabled) stopPreviewSound()
                                onSoundEnabledChange(enabled)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ThemePrimary,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = CardBorderSoft
                            ),
                            modifier = Modifier.testTag("settings_notif_sound_switch")
                        )
                    }

                    HorizontalDivider(color = CardBorderSoft, thickness = 1.dp)

                    // Selector Row: Chọn âm thanh thông báo + Nghe thử (2s preview)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (soundEnabled) 1f else 0.48f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = soundEnabled) {
                                    stopPreviewSound()
                                    showSoundPickerDialog = true
                                }
                                .padding(vertical = 4.dp)
                                .testTag("settings_choose_sound_row")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MusicNote,
                                contentDescription = null,
                                tint = if (soundEnabled) IndigoOnSurface else InkMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Chọn âm thanh thông báo",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = InkPrimary
                                )
                                Text(
                                    text = notificationCenterState.notificationSoundTitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (soundEnabled) ThemePrimary else InkMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                                contentDescription = null,
                                tint = InkMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                playPreviewSound(
                                    "current_selected",
                                    notificationCenterState.notificationSoundUri
                                )
                            },
                            enabled = soundEnabled,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                if (soundEnabled) ThemePrimary else CardBorderSoft
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("settings_preview_sound_btn")
                        ) {
                            Icon(
                                imageVector = if (isPreviewingCurrent) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                contentDescription = if (isPreviewingCurrent) "Dừng nghe thử" else "Nghe thử",
                                tint = if (soundEnabled) ThemePrimary else InkMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isPreviewingCurrent) "Dừng" else "Nghe thử",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (soundEnabled) ThemePrimary else InkMuted,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // =================================================================
            // 3. Đặt lịch hẹn học từ mới (Daily Vocab Slots + Repeat Days T2–CN)
            // =================================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (prefs.masterEnabled) 1f else 0.55f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ElectricIndigoLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MenuBook,
                                contentDescription = null,
                                tint = IndigoOnSurface,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Text(
                            text = "Đặt lịch hẹn học từ mới",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = InkPrimary
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            editorTarget = SlotEditorTarget.VOCAB
                            isCreatingNew = true
                            editingSlot = ReminderSlot(
                                id = "",
                                hour = 12,
                                minute = 30,
                                labelVi = "Ôn nhanh từ vựng C1–C2",
                                enabled = true
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ThemePrimary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("add_reminder_slot_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = ThemePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Thêm giờ",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ThemePrimary
                        )
                    }
                }

                if (prefs.slots.isEmpty()) {
                    EmptySlotHintCard("Chưa có khung giờ học từ mới. Nhấn \"Thêm giờ\" để tạo lịch hẹn hằng ngày.")
                } else {
                    prefs.slots.forEach { slot ->
                        SettingsSlotItemCard(
                            slot = slot,
                            accentColor = ElectricIndigo,
                            accentBg = ElectricIndigoLight,
                            masterEnabled = prefs.masterEnabled,
                            onToggle = { onToggleVocabSlot(slot.id) },
                            onEdit = {
                                editorTarget = SlotEditorTarget.VOCAB
                                isCreatingNew = false
                                editingSlot = slot
                            },
                            onDelete = { onDeleteVocabSlot(slot.id) },
                            tagPrefix = "vocab"
                        )
                    }
                }

                WeekdaySelectorCard(
                    title = "Lặp lại lịch học từ mới (T2–CN)",
                    selectedDays = prefs.repeatDays,
                    activeColor = ElectricIndigo,
                    onToggleDay = onToggleVocabRepeatDay,
                    tagPrefix = "repeat_day"
                )
            }

            HorizontalDivider(color = CardBorderSoft, thickness = 1.dp)

            // =================================================================
            // 4. Đặt lịch hẹn kiểm tra (Test Schedule + Weekly Repeat Days)
            // =================================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (prefs.masterEnabled) 1f else 0.55f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PracticeTealLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Quiz,
                                contentDescription = null,
                                tint = TealOnSurface,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Text(
                            text = "Đặt lịch hẹn kiểm tra (Test)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = InkPrimary
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            editorTarget = SlotEditorTarget.TEST
                            isCreatingNew = true
                            editingSlot = ReminderSlot(
                                id = "",
                                hour = 19,
                                minute = 30,
                                labelVi = "Kiểm tra Checkpoint & Mock Test",
                                enabled = true
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, PracticeTeal),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("add_test_slot_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = PracticeTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Thêm lịch thi",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PracticeTeal
                        )
                    }
                }

                if (prefs.testSlots.isEmpty()) {
                    EmptySlotHintCard("Chưa có lịch hẹn kiểm tra định kỳ. Thêm lịch để tự động nhắc làm Mock Test cuối tuần.")
                } else {
                    prefs.testSlots.forEach { slot ->
                        SettingsSlotItemCard(
                            slot = slot,
                            accentColor = PracticeTeal,
                            accentBg = PracticeTealLight,
                            masterEnabled = prefs.masterEnabled,
                            onToggle = { onToggleTestSlot(slot.id) },
                            onEdit = {
                                editorTarget = SlotEditorTarget.TEST
                                isCreatingNew = false
                                editingSlot = slot
                            },
                            onDelete = { onDeleteTestSlot(slot.id) },
                            tagPrefix = "test"
                        )
                    }
                }

                WeekdaySelectorCard(
                    title = "Ngày làm bài kiểm tra định kỳ (T2–CN)",
                    selectedDays = prefs.testRepeatDays,
                    activeColor = PracticeTeal,
                    onToggleDay = onToggleTestRepeatDay,
                    tagPrefix = "test_repeat_day"
                )
            }

            HorizontalDivider(color = CardBorderSoft, thickness = 1.dp)

            // =================================================================
            // 5. AI Teacher Reminder Tone Selector
            // =================================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (prefs.masterEnabled) 1f else 0.55f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.notif_tone_section_title),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = InkPrimary
                )

                ReminderTone.entries.forEach { tone ->
                    val selected = prefs.tone == tone
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (selected) CardSurfaceTinted else CardWhite,
                        border = BorderStroke(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) ThemePrimary else CardBorderSoft
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectTone(tone) }
                            .testTag("reminder_tone_${tone.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AnimatedTeacher(
                                mood = tone.teacherMood,
                                isSpeaking = selected,
                                size = 48.dp
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = tone.titleVi,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (selected) ThemePrimary else InkPrimary
                                    )
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = "Đang chọn",
                                            tint = ThemePrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = tone.subtitleVi,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = InkSecondary
                                )
                                Text(
                                    text = "“${tone.previewQuoteVi}”",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = InkMuted,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // 6. Test Notification Button ("Gửi thông báo thử ngay")
            Button(
                onClick = onSendTestNow,
                shape = RoundedCornerShape(15.dp),
                contentPadding = PaddingValues(vertical = 13.dp, horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ThemePrimary,
                    contentColor = ThemeOnPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("send_test_notification_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Send,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.notif_send_test_btn),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }

    // TimePicker Dialog for Adding / Editing Vocab or Test Reminder Slots
    val activeSlot = editingSlot
    if (activeSlot != null) {
        ScheduleTimePickerDialog(
            initialSlot = activeSlot,
            isNew = isCreatingNew,
            isTestSlot = editorTarget == SlotEditorTarget.TEST,
            onDismiss = { editingSlot = null },
            onSave = { hour, minute, label ->
                val existingId = if (isCreatingNew) null else activeSlot.id
                if (editorTarget == SlotEditorTarget.VOCAB) {
                    onSaveVocabSlot(existingId, hour, minute, label)
                } else {
                    onSaveTestSlot(existingId, hour, minute, label)
                }
                editingSlot = null
            }
        )
    }

    // Notification Sound Picker Dialog (3 built-in system presets + Android system Ringtone Picker)
    if (showSoundPickerDialog) {
        NotificationSoundPickerDialog(
            currentSoundUri = notificationCenterState.notificationSoundUri,
            currentSoundTitle = notificationCenterState.notificationSoundTitle,
            activePreviewKey = activePreviewKey,
            onPreviewPreset = { preset ->
                playPreviewSound("preset_${preset.name}", preset.storedUri)
            },
            onSelectPreset = { preset ->
                stopPreviewSound()
                onSelectNotificationSound(preset.storedUri, preset.titleVi)
                showSoundPickerDialog = false
            },
            onOpenDeviceRingtonePicker = {
                stopPreviewSound()
                val existingUri = ApexNotificationHelper.resolveSoundUri(
                    context,
                    notificationCenterState.notificationSoundUri
                )
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Chọn âm thanh thông báo")
                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, existingUri)
                }
                runCatching { ringtonePickerLauncher.launch(intent) }
            },
            onDismiss = {
                stopPreviewSound()
                showSoundPickerDialog = false
            }
        )
    }
}

@Composable
private fun NotificationSoundPickerDialog(
    currentSoundUri: String?,
    currentSoundTitle: String,
    activePreviewKey: String?,
    onPreviewPreset: (NotificationSoundPreset) -> Unit,
    onSelectPreset: (NotificationSoundPreset) -> Unit,
    onOpenDeviceRingtonePicker: () -> Unit,
    onDismiss: () -> Unit
) {
    val selectedPreset = NotificationSoundPreset.fromStoredUri(currentSoundUri)
    val isCustomDeviceTone = selectedPreset == null && !currentSoundUri.isNullOrBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardWhite,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ElectricIndigoLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = IndigoOnSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Chọn âm thanh thông báo",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = InkPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NotificationSoundPreset.entries.forEach { preset ->
                    val isSelected = selectedPreset == preset
                    val isPlaying = activePreviewKey == "preset_${preset.name}"
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) ElectricIndigoLight.copy(alpha = 0.55f) else SurfaceCanvas,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) ThemePrimary else CardBorderSoft
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectPreset(preset) }
                            .testTag("sound_preset_${preset.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 6.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectPreset(preset) },
                                colors = RadioButtonDefaults.colors(selectedColor = ThemePrimary)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.titleVi,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = InkPrimary
                                )
                                Text(
                                    text = preset.subtitleVi,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = InkSecondary
                                )
                            }
                            IconButton(
                                onClick = { onPreviewPreset(preset) },
                                modifier = Modifier.testTag("preview_preset_${preset.name.lowercase()}")
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                    contentDescription = if (isPlaying) "Dừng nghe thử" else "Nghe thử ${preset.titleVi}",
                                    tint = ThemePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                if (isCustomDeviceTone) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EmeraldSuccessLight.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Đang dùng âm từ máy: $currentSoundTitle",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = InkPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(2.dp))

                OutlinedButton(
                    onClick = onOpenDeviceRingtonePicker,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.25.dp, ThemePrimary),
                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("pick_device_ringtone_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LibraryMusic,
                        contentDescription = null,
                        tint = ThemePrimary,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Chọn từ âm thanh trên điện thoại...",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = ThemePrimary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Đóng", color = InkSecondary, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun EmptySlotHintCard(message: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, CardBorderSoft)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = InkMuted,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        )
    }
}

@Composable
private fun SettingsSlotItemCard(
    slot: ReminderSlot,
    accentColor: Color,
    accentBg: Color,
    masterEnabled: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    tagPrefix: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardWhite,
        border = BorderStroke(
            1.dp,
            if (slot.enabled && masterEnabled) accentColor.copy(alpha = 0.30f) else CardBorderSoft
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("${tagPrefix}_slot_card_${slot.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onEdit)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (slot.enabled) accentBg else SurfaceCanvas
                ) {
                    Text(
                        text = slot.formattedTime(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        ),
                        color = if (slot.enabled) accentColor else InkMuted,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = slot.labelVi,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (slot.enabled) InkPrimary else InkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (slot.enabled) "Đang bật • Chạm để sửa giờ" else "Đang tắt",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkMuted
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("edit_${tagPrefix}_slot_${slot.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Chỉnh sửa khung giờ",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_${tagPrefix}_slot_${slot.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Xóa khung giờ",
                        tint = RoseAlert,
                        modifier = Modifier.size(19.dp)
                    )
                }
                Switch(
                    checked = slot.enabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = accentColor,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = CardBorderSoft
                    ),
                    modifier = Modifier.testTag("toggle_${tagPrefix}_slot_${slot.id}")
                )
            }
        }
    }
}

@Composable
private fun WeekdaySelectorCard(
    title: String,
    selectedDays: Set<String>,
    activeColor: Color,
    onToggleDay: (String) -> Unit,
    tagPrefix: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, CardBorderSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = InkPrimary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeekdayItem.entries.forEach { day ->
                    val selected = selectedDays.contains(day.id)
                    val bgColor by animateColorAsState(
                        targetValue = if (selected) activeColor else SurfaceCanvas,
                        animationSpec = tween(180),
                        label = "${tagPrefix}_${day.id}"
                    )
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(bgColor)
                            .border(
                                width = 1.dp,
                                color = if (selected) activeColor else CardBorderSoft,
                                shape = CircleShape
                            )
                            .clickable { onToggleDay(day.id) }
                            .testTag("${tagPrefix}_${day.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.labelVi,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (selected) Color.White else InkSecondary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleTimePickerDialog(
    initialSlot: ReminderSlot,
    isNew: Boolean,
    isTestSlot: Boolean,
    onDismiss: () -> Unit,
    onSave: (hour: Int, minute: Int, labelVi: String) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialSlot.hour,
        initialMinute = initialSlot.minute,
        is24Hour = true
    )
    var labelText by remember { mutableStateOf(initialSlot.labelVi) }
    val accentColor = if (isTestSlot) PracticeTeal else ElectricIndigo

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardWhite,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = when {
                    isTestSlot && isNew -> "Thêm lịch hẹn kiểm tra"
                    isTestSlot -> "Sửa lịch hẹn kiểm tra"
                    isNew -> stringResource(R.string.notif_dialog_add_title)
                    else -> stringResource(R.string.notif_dialog_edit_title)
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = InkPrimary
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = SurfaceCanvas,
                        selectorColor = accentColor,
                        timeSelectorSelectedContainerColor = accentColor.copy(alpha = 0.16f),
                        timeSelectorSelectedContentColor = accentColor,
                        timeSelectorUnselectedContainerColor = SurfaceCanvas,
                        timeSelectorUnselectedContentColor = InkSecondary
                    )
                )

                OutlinedTextField(
                    value = labelText,
                    onValueChange = { labelText = it },
                    singleLine = true,
                    label = {
                        Text(
                            text = if (isTestSlot) "Tên bài kiểm tra / Đề thi" else stringResource(R.string.notif_dialog_label_hint),
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = CardBorderSoft
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slot_label_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(timePickerState.hour, timePickerState.minute, labelText) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("save_slot_dialog_button")
            ) {
                Text(text = stringResource(R.string.notif_dialog_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.notif_dialog_cancel), color = InkSecondary)
            }
        }
    )
}
