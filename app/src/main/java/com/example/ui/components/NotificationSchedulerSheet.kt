package com.example.ui.components

import android.Manifest
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.NotificationPreferences
import com.example.data.ReminderSlot
import com.example.data.ReminderTone
import com.example.data.WeekdayItem
import com.example.notifications.ApexNotificationHelper
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardSurfaceTinted
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.VividViolet

/**
 * Android Material 3 slide-up Bottom Sheet for Custom Notification Scheduling:
 * - Master enable/disable switch
 * - Android POST_NOTIFICATIONS runtime permission requester
 * - Multi-slot HH:MM scheduler (add, edit, toggle, delete)
 * - Flexible repeat weekday selector (T2–CN)
 * - AI Teacher tone selector (Gentle vs Strict Band 8.5)
 * - Instant test notification trigger ("Gửi thông báo thử ngay")
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSchedulerSheet(
    prefs: NotificationPreferences,
    onDismiss: () -> Unit,
    onMasterEnabledChange: (Boolean) -> Unit,
    onSaveSlot: (existingId: String?, hour: Int, minute: Int, labelVi: String) -> Unit,
    onToggleSlot: (String) -> Unit,
    onDeleteSlot: (String) -> Unit,
    onToggleRepeatDay: (String) -> Unit,
    onSelectTone: (ReminderTone) -> Unit,
    onSendTestNow: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var hasOsPermission by remember {
        mutableStateOf(ApexNotificationHelper.hasNotificationPermission(context))
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasOsPermission = granted || ApexNotificationHelper.hasNotificationPermission(context)
    }

    // Slot editor dialog state: null = closed, ReminderSlot = editing existing or creating new
    var editingSlot by remember { mutableStateOf<ReminderSlot?>(null) }
    var isCreatingNewSlot by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceCanvas,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("notification_scheduler_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sheet Header
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
                            .size(46.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(listOf(ElectricIndigo, VividViolet, CyanGlow))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedReminderBell(
                            size = 24.dp,
                            tint = Color.White,
                            isActive = prefs.masterEnabled
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.notif_sheet_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = InkPrimary
                        )
                        Text(
                            text = stringResource(R.string.notif_sheet_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_scheduler_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Đóng cài đặt thông báo",
                        tint = InkSecondary
                    )
                }
            }

            // 1. Master Enable / Disable Card
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, CardBorderSoft)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.notif_master_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = InkPrimary
                        )
                        Text(
                            text = stringResource(R.string.notif_master_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = prefs.masterEnabled,
                        onCheckedChange = onMasterEnabledChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ElectricIndigo,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = CardBorderSoft
                        ),
                        modifier = Modifier.testTag("master_reminder_switch")
                    )
                }
            }

            // 2. Device Notification Permission Status Card (Android POST_NOTIFICATIONS)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (hasOsPermission) EmeraldSuccessLight.copy(alpha = 0.55f) else CardSurfaceTinted,
                border = BorderStroke(
                    1.dp,
                    if (hasOsPermission) EmeraldSuccess.copy(alpha = 0.35f) else ElectricIndigo.copy(alpha = 0.22f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (hasOsPermission) {
                                Icons.Rounded.CheckCircle
                            } else {
                                Icons.Rounded.NotificationsNone
                            },
                            contentDescription = null,
                            tint = if (hasOsPermission) EmeraldSuccess else ElectricIndigo,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.notif_permission_title),
                                style = MaterialTheme.typography.titleSmall,
                                color = InkPrimary
                            )
                            Text(
                                text = stringResource(
                                    if (hasOsPermission) R.string.notif_permission_granted
                                    else R.string.notif_permission_missing
                                ),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
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
                                containerColor = ElectricIndigo,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("request_notification_permission_button")
                        ) {
                            Text(
                                text = stringResource(R.string.notif_permission_btn),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // 3. Multi-Slot Time Scheduler
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
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = ElectricIndigo,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.notif_slots_section_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = InkPrimary
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            isCreatingNewSlot = true
                            editingSlot = ReminderSlot(
                                id = "",
                                hour = 12,
                                minute = 30,
                                labelVi = "Ôn nhanh Collocations buổi trưa",
                                enabled = true
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ElectricIndigo),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("add_reminder_slot_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = ElectricIndigo,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.notif_add_slot_btn),
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricIndigo
                        )
                    }
                }

                if (prefs.slots.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = CardWhite,
                        border = BorderStroke(1.dp, CardBorderSoft)
                    ) {
                        Text(
                            text = stringResource(R.string.notif_empty_slots_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                } else {
                    prefs.slots.forEach { slot ->
                        ReminderSlotCard(
                            slot = slot,
                            masterEnabled = prefs.masterEnabled,
                            onToggle = { onToggleSlot(slot.id) },
                            onEdit = {
                                isCreatingNewSlot = false
                                editingSlot = slot
                            },
                            onDelete = { onDeleteSlot(slot.id) }
                        )
                    }
                }
            }

            // 4. Flexible Repeat Day Selector (T2–CN)
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, CardBorderSoft),
                modifier = Modifier.alpha(if (prefs.masterEnabled) 1f else 0.55f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.notif_repeat_days_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = InkPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        WeekdayItem.entries.forEach { day ->
                            val selected = prefs.repeatDays.contains(day.id)
                            val bgColor by animateColorAsState(
                                targetValue = if (selected) ElectricIndigo else SurfaceCanvas,
                                animationSpec = tween(180),
                                label = "repeat_day_bg_${day.id}"
                            )
                            Box(
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(bgColor)
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) ElectricIndigo else CardBorderSoft,
                                        shape = CircleShape
                                    )
                                    .clickable { onToggleRepeatDay(day.id) }
                                    .testTag("repeat_day_${day.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.labelVi,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) Color.White else InkSecondary
                                )
                            }
                        }
                    }
                }
            }

            // 5. AI Teacher Reminder Tone Selector (Gentle vs Strict)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (prefs.masterEnabled) 1f else 0.55f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.notif_tone_section_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = InkPrimary
                )

                ReminderTone.entries.forEach { tone ->
                    val selected = prefs.tone == tone
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (selected) CardSurfaceTinted else CardWhite,
                        border = BorderStroke(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) ElectricIndigo else CardBorderSoft
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onSelectTone(tone) }
                            .testTag("reminder_tone_${tone.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AnimatedTeacher(
                                mood = tone.teacherMood,
                                isSpeaking = selected,
                                size = 56.dp
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = tone.titleVi,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (selected) ElectricIndigo else InkPrimary
                                    )
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = "Đang chọn",
                                            tint = ElectricIndigo,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = tone.subtitleVi,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
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

            // 6. Instant Test Notification CTA ("Gửi thông báo thử ngay")
            Button(
                onClick = onSendTestNow,
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(vertical = 15.dp, horizontal = 20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricIndigo,
                    contentColor = Color.White
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
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }

    // TimePicker + Label Dialog for Adding / Editing a Reminder Slot
    val slotInDialog = editingSlot
    if (slotInDialog != null) {
        ReminderSlotEditorDialog(
            initialSlot = slotInDialog,
            isNew = isCreatingNewSlot,
            onDismiss = { editingSlot = null },
            onSave = { hour, minute, label ->
                onSaveSlot(
                    if (isCreatingNewSlot) null else slotInDialog.id,
                    hour,
                    minute,
                    label
                )
                editingSlot = null
            }
        )
    }
}

@Composable
private fun ReminderSlotCard(
    slot: ReminderSlot,
    masterEnabled: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CardWhite,
        border = BorderStroke(
            1.dp,
            if (slot.enabled && masterEnabled) ElectricIndigo.copy(alpha = 0.24f) else CardBorderSoft
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reminder_slot_card_${slot.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onEdit)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (slot.enabled) ElectricIndigoLight else SurfaceCanvas
                ) {
                    Text(
                        text = slot.formattedTime(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        ),
                        color = if (slot.enabled) ElectricIndigo else InkMuted,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = slot.labelVi,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (slot.enabled) InkPrimary else InkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = if (slot.enabled) CyanGlow else InkMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (slot.enabled) "Đang bật • Chạm để sửa" else "Đang tắt",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = InkMuted
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("edit_slot_${slot.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Chỉnh sửa khung giờ",
                        tint = ElectricIndigo,
                        modifier = Modifier.size(19.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_slot_${slot.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Xóa khung giờ",
                        tint = RoseAlert,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Switch(
                    checked = slot.enabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ElectricIndigo,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = CardBorderSoft
                    ),
                    modifier = Modifier.testTag("toggle_slot_${slot.id}")
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderSlotEditorDialog(
    initialSlot: ReminderSlot,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (hour: Int, minute: Int, labelVi: String) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialSlot.hour,
        initialMinute = initialSlot.minute,
        is24Hour = true
    )
    var labelText by remember { mutableStateOf(initialSlot.labelVi) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardWhite,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = stringResource(
                    if (isNew) R.string.notif_dialog_add_title else R.string.notif_dialog_edit_title
                ),
                style = MaterialTheme.typography.titleLarge,
                color = InkPrimary
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = SurfaceCanvas,
                        selectorColor = ElectricIndigo,
                        timeSelectorSelectedContainerColor = ElectricIndigoLight,
                        timeSelectorSelectedContentColor = ElectricIndigo,
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
                            text = stringResource(R.string.notif_dialog_label_hint),
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricIndigo,
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
                onClick = {
                    onSave(timePickerState.hour, timePickerState.minute, labelText)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricIndigo,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("save_slot_dialog_button")
            ) {
                Text(text = stringResource(R.string.notif_dialog_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.notif_dialog_cancel),
                    color = InkSecondary
                )
            }
        }
    )
}
