package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.IeltsModule
import com.example.data.NotificationCenterState
import com.example.data.NotificationInboxItem
import com.example.data.NotificationSection
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
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
import com.example.ui.theme.ThemeOnPrimary
import com.example.ui.theme.ThemePrimary
import com.example.ui.theme.VividViolet
import java.util.Locale

/**
 * Upgraded Notification Center ("Trung tâm thông báo") opened from the Header Bell Icon:
 * - Top Action Toolbar: Unread counter ("Chưa đọc: X"), Quick Sound Toggle, "Đánh dấu đã đọc tất cả", "Xóa tất cả"
 * - Per-Notification Controls: Unread vs Read visual states, compact "Đã đọc" check button, "Xóa" trash button + Swipe-to-Dismiss
 * - Actionable Sections: Section 1 (Từ vựng cần học hôm nay) & Section 2 (Đến giờ kiểm tra & Đề thi)
 * - Footer Shortcut -> General Settings (Lịch học & Âm thanh thông báo)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterSheet(
    activeModule: IeltsModule,
    visibleNotifications: List<NotificationInboxItem>,
    totalVocabDueCount: Int,
    centerState: NotificationCenterState,
    unreadCount: Int,
    onMarkNotificationRead: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    onDismissNotification: (String) -> Unit,
    onClearAllNotifications: () -> Unit,
    onToggleNotificationSound: () -> Unit,
    onJumpToLearn: () -> Unit,
    onJumpToExams: () -> Unit,
    onOpenScheduleSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val vocabItems = visibleNotifications.filter { it.section == NotificationSection.VOCAB }
    val examItems = visibleNotifications.filter { it.section == NotificationSection.EXAM }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceCanvas,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("notification_center_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Header Row
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
                            .size(48.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                Brush.linearGradient(listOf(ElectricIndigo, VividViolet, CyanGlow))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Trung tâm thông báo",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = InkPrimary
                        )
                        Text(
                            text = "Module #${String.format(Locale.US, "%02d", activeModule.id)} • ${activeModule.titleEn}",
                            style = MaterialTheme.typography.labelSmall,
                            color = InkSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_notif_center_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Đóng trung tâm thông báo",
                        tint = InkSecondary
                    )
                }
            }

            // 2. Top Action Toolbar (Unread counter + Quick Sound Toggle + Mark All Read + Clear All)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, CardBorderSoft),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notif_action_toolbar")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Unread Counter Chip ("Chưa đọc: X")
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (unreadCount > 0) RoseDiffLight else EmeraldSuccessLight,
                            border = BorderStroke(
                                1.dp,
                                if (unreadCount > 0) RoseAlert.copy(alpha = 0.35f) else EmeraldSuccess.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier.testTag("notif_unread_counter_chip")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (unreadCount > 0) RoseAlert else EmeraldSuccess)
                                )
                                Text(
                                    text = "Chưa đọc: $unreadCount",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = if (unreadCount > 0) RoseAlert else EmeraldOnContainer,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        // Quick Sound Toggle Button (1-tap ON/OFF)
                        val soundOn = centerState.isNotificationSoundEnabled
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (soundOn) ElectricIndigoLight else SurfaceCanvas,
                            border = BorderStroke(
                                1.dp,
                                if (soundOn) ElectricIndigo.copy(alpha = 0.4f) else CardBorderSoft
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onToggleNotificationSound)
                                .minimumInteractiveComponentSize()
                                .testTag("notif_quick_sound_toggle")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Icon(
                                    imageVector = if (soundOn) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
                                    contentDescription = if (soundOn) "Tắt âm thanh thông báo" else "Bật âm thanh thông báo",
                                    tint = if (soundOn) IndigoOnSurface else InkMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (soundOn) "Âm bật" else "Im lặng",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (soundOn) IndigoOnSurface else InkMuted,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    // Row 2: Mark All Read + Clear All compact buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onMarkAllRead,
                            enabled = unreadCount > 0,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                if (unreadCount > 0) ThemePrimary.copy(alpha = 0.45f) else CardBorderSoft
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1.35f)
                                .minimumInteractiveComponentSize()
                                .testTag("notif_mark_all_read_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DoneAll,
                                contentDescription = null,
                                tint = if (unreadCount > 0) ThemePrimary else InkMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Đánh dấu đã đọc tất cả",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (unreadCount > 0) ThemePrimary else InkMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        OutlinedButton(
                            onClick = onClearAllNotifications,
                            enabled = visibleNotifications.isNotEmpty(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                if (visibleNotifications.isNotEmpty()) RoseAlert.copy(alpha = 0.45f) else CardBorderSoft
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(0.85f)
                                .minimumInteractiveComponentSize()
                                .testTag("notif_clear_all_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteSweep,
                                contentDescription = null,
                                tint = if (visibleNotifications.isNotEmpty()) RoseAlert else InkMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Xóa tất cả",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (visibleNotifications.isNotEmpty()) RoseAlert else InkMuted,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // 3. Empty State OR Actionable Notification Sections
            if (visibleNotifications.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = CardWhite,
                    border = BorderStroke(1.dp, CardBorderSoft),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notif_empty_state")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(EmeraldSuccessLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsNone,
                                contentDescription = null,
                                tint = EmeraldOnContainer,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = "Không có thông báo mới hôm nay",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = InkPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Bạn đã xử lý xong danh sách thông báo hôm nay. Thông báo mới sẽ tự động cập nhật vào ngày mai.",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // SECTION 1: Từ vựng cần học hôm nay
                if (vocabItems.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = CardWhite,
                        border = BorderStroke(1.dp, CardBorderSoft),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notif_section_vocab")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(13.dp))
                                        .background(ElectricIndigoLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MenuBook,
                                        contentDescription = null,
                                        tint = IndigoOnSurface,
                                        modifier = Modifier.size(23.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = NotificationSection.VOCAB.labelVi,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = InkPrimary
                                    )
                                    val unreadVocab = vocabItems.count { !it.isRead }
                                    Text(
                                        text = "${vocabItems.size} thông báo • $unreadVocab chưa đọc",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = InkSecondary
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                vocabItems.forEach { item ->
                                    key(item.id) {
                                        NotificationInboxCard(
                                            item = item,
                                            accentColor = ElectricIndigo,
                                            accentLight = ElectricIndigoLight,
                                            accentOnSurface = IndigoOnSurface,
                                            onCardClick = {
                                                onMarkNotificationRead(item.id)
                                                onDismiss()
                                                onJumpToLearn()
                                            },
                                            onMarkRead = { onMarkNotificationRead(item.id) },
                                            onDelete = { onDismissNotification(item.id) }
                                        )
                                    }
                                }

                                if (totalVocabDueCount > vocabItems.size) {
                                    Text(
                                        text = "+ ${totalVocabDueCount - vocabItems.size} từ vựng mục tiêu khác đang chờ trong Tab Học",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = InkMuted,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    onDismiss()
                                    onJumpToLearn()
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ThemePrimary,
                                    contentColor = ThemeOnPrimary
                                ),
                                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("notif_center_learn_now_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(19.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Học ngay",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }

                // SECTION 2: Đến giờ kiểm tra & Đề thi
                if (examItems.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = CardWhite,
                        border = BorderStroke(1.dp, CardBorderSoft),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notif_section_exams")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(13.dp))
                                        .background(PracticeTealLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Quiz,
                                        contentDescription = null,
                                        tint = TealOnSurface,
                                        modifier = Modifier.size(23.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = NotificationSection.EXAM.labelVi,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = InkPrimary
                                    )
                                    val unreadExam = examItems.count { !it.isRead }
                                    Text(
                                        text = "${examItems.size} thông báo • $unreadExam chưa đọc",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = InkSecondary
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                examItems.forEach { item ->
                                    key(item.id) {
                                        NotificationInboxCard(
                                            item = item,
                                            accentColor = PracticeTeal,
                                            accentLight = PracticeTealLight,
                                            accentOnSurface = TealOnSurface,
                                            onCardClick = {
                                                onMarkNotificationRead(item.id)
                                                onDismiss()
                                                onJumpToExams()
                                            },
                                            onMarkRead = { onMarkNotificationRead(item.id) },
                                            onDelete = { onDismissNotification(item.id) }
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    onDismiss()
                                    onJumpToExams()
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PracticeTeal,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("notif_center_exam_now_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Quiz,
                                    contentDescription = null,
                                    modifier = Modifier.size(19.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Làm bài ngay",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = CardBorderSoft, thickness = 1.dp)

            // 4. Footer Shortcut -> General Settings (Âm thanh & Lịch nhắc)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, CardBorderSoft),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        onDismiss()
                        onOpenScheduleSettings()
                    }
                    .minimumInteractiveComponentSize()
                    .testTag("notif_center_open_schedule_settings")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ElectricIndigoLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = null,
                            tint = IndigoOnSurface,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cài đặt âm thanh & lịch nhắc →",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = IndigoOnSurface
                        )
                        Text(
                            text = if (centerState.isNotificationSoundEnabled) {
                                "Âm thanh: ${centerState.notificationSoundTitle}"
                            } else {
                                "Âm thanh đang tắt (chế độ im lặng)"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = InkSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationInboxCard(
    item: NotificationInboxItem,
    accentColor: Color,
    accentLight: Color,
    accentOnSurface: Color,
    onCardClick: () -> Unit,
    onMarkRead: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            !item.isRead && item.isUrgent -> RoseDiffLight.copy(alpha = 0.55f)
            !item.isRead -> accentLight.copy(alpha = 0.45f)
            else -> SurfaceCanvas
        },
        animationSpec = tween(180),
        label = "notif_card_bg_${item.id}"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            !item.isRead && item.isUrgent -> RoseAlert.copy(alpha = 0.40f)
            !item.isRead -> accentColor.copy(alpha = 0.35f)
            else -> CardBorderSoft
        },
        animationSpec = tween(180),
        label = "notif_card_border_${item.id}"
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(RoseAlert)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Xóa thông báo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Xóa",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notif_card_${item.id}")
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = containerColor,
            border = BorderStroke(width = if (!item.isRead) 1.25.dp else 1.dp, color = borderColor),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onCardClick)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Unread dot indicator (visible only when unread)
                if (!item.isRead) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(if (item.isUrgent) RoseAlert else accentColor)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.titleVi,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = if (!item.isRead) FontWeight.ExtraBold else FontWeight.Medium
                            ),
                            color = if (!item.isRead) InkPrimary else InkSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = when {
                                item.isRead -> CardWhite
                                item.isUrgent -> GoldMasteryLight
                                else -> accentLight
                            },
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Text(
                                text = item.badgeVi,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (!item.isRead) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = when {
                                    item.isRead -> InkMuted
                                    item.isUrgent -> GoldOnContainer
                                    else -> accentOnSurface
                                },
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = item.subtitleVi,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = if (!item.isRead) FontWeight.Medium else FontWeight.Normal
                        ),
                        color = if (!item.isRead) InkSecondary else InkMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Per-card Compact Controls: "Đã đọc" Check button (only on unread cards) + "Xóa" Trash button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (!item.isRead) {
                        IconButton(
                            onClick = onMarkRead,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("notif_mark_read_${item.id}")
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = EmeraldSuccessLight,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Đã đọc",
                                        tint = EmeraldOnContainer,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("notif_delete_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Xóa thông báo",
                            tint = if (!item.isRead) RoseAlert else InkMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
