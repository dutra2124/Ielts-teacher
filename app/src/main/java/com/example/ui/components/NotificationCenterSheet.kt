package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExamPaper
import com.example.data.IeltsModule
import com.example.data.NotificationPreferences
import com.example.data.VocabWordItem
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CyanGlow
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
import com.example.ui.theme.ThemeOnPrimary
import com.example.ui.theme.ThemePrimary
import com.example.ui.theme.VividViolet
import java.util.Locale

/**
 * Focused, actionable Notification Center ("Trung tâm thông báo") opened from the Header Bell Icon.
 * Contains ONLY actionable tasks due right now (zero toggles, time pickers, or schedule editors):
 * 1. Từ vựng cần học hôm nay (Due SRS words + unlearned active Module vocabulary -> 1-tap "Học ngay")
 * 2. Đến giờ kiểm tra & Đề thi (Scheduled tests, Module checkpoint, due Mock Exams -> 1-tap "Làm bài ngay")
 * Footer provides a single subtle shortcut link into General Settings -> Lịch học & Nhắc nhở.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterSheet(
    activeModule: IeltsModule,
    dueSrsWords: List<VocabWordItem>,
    unlearnedModuleWords: List<VocabWordItem>,
    notificationPrefs: NotificationPreferences,
    todayWeekdayId: String,
    recommendedExam: ExamPaper?,
    hasCompletedActiveModule: Boolean,
    unreadCount: Int,
    onJumpToLearn: () -> Unit,
    onJumpToExams: () -> Unit,
    onOpenScheduleSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val combinedVocabDue = (dueSrsWords + unlearnedModuleWords)
        .distinctBy { it.id }

    val enabledTestSlots = notificationPrefs.testSlots.filter { it.enabled }
    val isTestDayToday = notificationPrefs.testRepeatDays.contains(todayWeekdayId)

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Header
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
                            .background(
                                Brush.linearGradient(listOf(ElectricIndigo, VividViolet, CyanGlow))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Trung tâm thông báo",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = InkPrimary
                            )
                            if (unreadCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = RoseAlert,
                                    modifier = Modifier.wrapContentWidth()
                                ) {
                                    Text(
                                        text = "$unreadCount mới",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Những việc cần làm ngay hôm nay để giữ vững đà Band 8.5",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
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

            // =================================================================
            // SECTION 1: Từ vựng cần học hôm nay
            // =================================================================
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, CardBorderSoft),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notif_section_vocab")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                                imageVector = Icons.Rounded.MenuBook,
                                contentDescription = null,
                                tint = IndigoOnSurface,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Từ vựng cần học hôm nay",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = InkPrimary
                            )
                            Text(
                                text = if (combinedVocabDue.isNotEmpty()) {
                                    "${dueSrsWords.size} từ ôn SRS • ${unlearnedModuleWords.size} từ mới Module #${String.format(Locale.US, "%02d", activeModule.id)}"
                                } else {
                                    "Bạn đã hoàn thành hết từ mục tiêu hôm nay!"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSecondary
                            )
                        }
                    }

                    if (combinedVocabDue.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = EmeraldSuccessLight,
                            border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "Xuất sắc! Không còn từ SRS tồn đọng và đã học đủ từ trọng tâm của Module hiện tại.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = EmeraldOnContainer
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val srsIdSet = dueSrsWords.map { it.id }.toSet()
                            combinedVocabDue.take(4).forEach { word ->
                                val isSrs = srsIdSet.contains(word.id)
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = SurfaceCanvas,
                                    border = BorderStroke(1.dp, CardBorderSoft)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = word.word,
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        fontFamily = PlusJakartaSansFontFamily,
                                                        fontWeight = FontWeight.ExtraBold
                                                    ),
                                                    color = InkPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = word.ipaUk,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = InkMuted,
                                                    maxLines = 1
                                                )
                                            }
                                            Text(
                                                text = word.meaningVi,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = InkSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSrs) GoldMasteryLight else ElectricIndigoLight,
                                            modifier = Modifier.wrapContentWidth()
                                        ) {
                                            Text(
                                                text = if (isSrs) "Ôn SRS" else "Từ mới",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSrs) GoldOnContainer else IndigoOnSurface,
                                                maxLines = 1,
                                                softWrap = false,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (combinedVocabDue.size > 4) {
                                Text(
                                    text = "+ ${combinedVocabDue.size - 4} từ vựng mục tiêu khác đang chờ trong Tab Học",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = InkMuted,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onJumpToLearn()
                        },
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ThemePrimary,
                            contentColor = ThemeOnPrimary
                        ),
                        contentPadding = PaddingValues(vertical = 13.dp, horizontal = 16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("notif_center_learn_now_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
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
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // =================================================================
            // SECTION 2: Đến giờ kiểm tra & Đề thi
            // =================================================================
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, CardBorderSoft),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notif_section_exams")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .background(PracticeTealLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Quiz,
                                contentDescription = null,
                                tint = TealOnSurface,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Đến giờ kiểm tra & Đề thi",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = InkPrimary
                            )
                            Text(
                                text = if (isTestDayToday && enabledTestSlots.isNotEmpty()) {
                                    "Hôm nay có lịch kiểm tra định kỳ — vào phòng thi ngay!"
                                } else {
                                    "Checkpoint Module & Đề thi mô phỏng CD-IELTS sẵn sàng"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSecondary
                            )
                        }
                    }

                    // Scheduled Test Slot Alerts (if configured)
                    if (enabledTestSlots.isNotEmpty()) {
                        enabledTestSlots.take(2).forEach { slot ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isTestDayToday) RoseDiffLight.copy(alpha = 0.72f) else SurfaceCanvas,
                                border = BorderStroke(
                                    1.dp,
                                    if (isTestDayToday) RoseAlert.copy(alpha = 0.35f) else CardBorderSoft
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(11.dp))
                                            .background(if (isTestDayToday) RoseAlert else ElectricIndigoLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Schedule,
                                            contentDescription = null,
                                            tint = if (isTestDayToday) Color.White else IndigoOnSurface,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = slot.labelVi,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = InkPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (isTestDayToday) {
                                                "Đến hạn hôm nay lúc ${slot.formattedTime()} ($todayWeekdayId)"
                                            } else {
                                                "Lịch định kỳ lúc ${slot.formattedTime()} (${notificationPrefs.testRepeatDays.joinToString(", ")})"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isTestDayToday) RoseAlert else InkSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Active Module Checkpoint & Recommended Mock Exam Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceCanvas,
                        border = BorderStroke(1.dp, CardBorderSoft)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(GoldMasteryLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AssignmentTurnedIn,
                                    contentDescription = null,
                                    tint = GoldOnContainer,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = recommendedExam?.title ?: "Checkpoint Module #${String.format(Locale.US, "%02d", activeModule.id)}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = InkPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (!hasCompletedActiveModule) {
                                        "Kiểm tra năng lực ${activeModule.titleEn} • ${activeModule.tier.bandRange}"
                                    } else {
                                        recommendedExam?.topicVi?.takeIf { it.isNotBlank() }?.let {
                                            "Chủ đề: $it • Thi mô phỏng CD-IELTS + Chữa đề AI"
                                        } ?: "Thi mô phỏng CD-IELTS chuẩn phòng thi thật + Chữa đề AI"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onJumpToExams()
                        },
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PracticeTeal,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(vertical = 13.dp, horizontal = 16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("notif_center_exam_now_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Quiz,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
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
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = CardBorderSoft, thickness = 1.dp)

            // Subtle Footer Shortcut Link -> General Settings ("Lịch học & Nhắc nhở")
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = null,
                            tint = IndigoOnSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Cài đặt lịch học & nhắc nhở →",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = IndigoOnSurface
                        )
                    }
                }
            }
        }
    }
}
