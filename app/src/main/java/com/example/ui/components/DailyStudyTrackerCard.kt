package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AutoKpiSnapshot
import com.example.data.NotificationPreferences
import com.example.data.StreakStatus
import com.example.data.WeekdayItem
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardSurfaceTinted
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
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseDiffLight
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.VividViolet
import kotlin.math.roundToInt

/**
 * 100% Automatic Daily Study Habit Tracker card on Tab 1 (Lộ trình):
 * - Strictly READ-ONLY auto-updating KPI rows (zero manual checkboxes or clickable completions).
 * - Live counters, animated progress bars, and automatic green ticks (`✓ Đạt`) driven by real study behavior.
 * - Streak Counter paired with progressive MasteryStreakIcon.
 * - 7-day weekly dot calendar (T2–CN) + Quick Notification Schedule entry strip.
 */
@Composable
fun DailyStudyTrackerCard(
    autoKpi: AutoKpiSnapshot,
    streakDays: Int,
    weeklyActiveDays: Set<String>,
    todayWeekdayId: String,
    notificationPrefs: NotificationPreferences,
    onOpenNotificationSheet: () -> Unit,
    streakStatus: StreakStatus = StreakStatus(currentStreak = streakDays),
    onOpenStreakSheet: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    val animatedSweep by animateFloatAsState(
        targetValue = if (entered) autoKpi.overallProgressFraction else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "daily_habit_ring_sweep"
    )

    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_study_tracker_card"),
        shape = RoundedCornerShape(24.dp),
        solidColor = CardWhite,
        solidBorder = BorderStroke(1.dp, CardBorderSoft),
        solidShadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Title + Auto-Tracking Subtitle + Streak Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.daily_habit_card_title),
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp),
                            color = InkPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = ElectricIndigoLight
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = IndigoOnSurface,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = "Tự động",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.5.sp
                                    ),
                                    color = IndigoOnSurface,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                    Text(
                        text = if (autoKpi.isAllDailyKpiMet) {
                            "Đã hoàn thành 100% KPI hôm nay • Chuỗi +1 tự động!"
                        } else {
                            "Tự cập nhật khi học từ vựng & tự học trên Học, Luyện, Thi"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (autoKpi.isAllDailyKpiMet) EmeraldSuccess else InkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                val streakAccent = streakStatus.masteryLevel.accentFor(streakStatus.visualStyle)
                val streakPillBg = if (streakStatus.currentStreak == 0) {
                    SurfaceCanvas
                } else {
                    streakStatus.masteryLevel.containerBg
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = streakPillBg,
                    border = BorderStroke(1.dp, streakAccent.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .wrapContentWidth()
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onOpenStreakSheet)
                        .testTag("tracker_streak_pill")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        MasteryStreakIcon(
                            streakDays = streakStatus.currentStreak,
                            style = streakStatus.visualStyle,
                            size = 30.dp,
                            contentDescriptionText = "Chuỗi học liên tiếp • Nhấn để xem chi tiết"
                        )
                        Text(
                            text = stringResource(R.string.streak_days_label, streakStatus.currentStreak),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = streakAccent,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            // 3-Day Inactivity Grace Window Warning Banner (shown when 1-2 days missed)
            val graceWarning = streakStatus.graceWarningVi
            if (graceWarning != null) {
                val isUrgent = streakStatus.daysRemainingBeforeLoss <= 1
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isUrgent) RoseDiffLight else GoldMasteryLight,
                    border = BorderStroke(
                        1.dp,
                        if (isUrgent) RoseAlert.copy(alpha = 0.45f) else GoldMastery.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onOpenStreakSheet)
                        .testTag("tracker_streak_grace_warning")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = "Cảnh báo giữ chuỗi học",
                            tint = if (isUrgent) RoseAlert else GoldOnContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = graceWarning,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isUrgent) RoseAlert else GoldOnContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = if (isUrgent) RoseAlert else GoldOnContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Center Row: Animated Circular KPI Ring + 2 Read-Only Auto-Updating Progress Rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularHabitProgressRing(
                    progress = animatedSweep,
                    completedCount = autoKpi.completedCriteriaCount,
                    totalCount = 2,
                    modifier = Modifier.testTag("daily_habit_progress_ring")
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Criterion 1: Learned Vocabulary (Read-Only)
                    AutoKpiProgressRow(
                        icon = Icons.Rounded.MenuBook,
                        titleVi = "Từ vựng hôm nay",
                        pillText = if (autoKpi.isVocabTargetMet) {
                            "✓ Đạt"
                        } else {
                            "${autoKpi.dailyWordsLearned}/${autoKpi.targetWordsPerDay} từ"
                        },
                        subtitleVi = if (autoKpi.isVocabTargetMet) {
                            "Đã học ${autoKpi.dailyWordsLearned}/${autoKpi.targetWordsPerDay} từ hôm nay"
                        } else {
                            "Flashcard (Đã thuộc), SRS & Mini-game"
                        },
                        progress = autoKpi.vocabProgressFraction,
                        isMet = autoKpi.isVocabTargetMet,
                        accentColor = ElectricIndigo,
                        testTagName = "auto_kpi_vocab_row"
                    )

                    // Criterion 2: Active Study Time (Read-Only)
                    val studySubtitle = when {
                        autoKpi.isStudyTimeTargetMet ->
                            "Đã học ${autoKpi.dailyStudyMinutes}/${autoKpi.targetMinutesPerDay} phút (${autoKpi.liveClockFormatted})"
                        autoKpi.isPausedDueToInactivity ->
                            "Tạm dừng nghỉ >60s • Chạm học để tiếp tục"
                        autoKpi.isTimerActive ->
                            "Đang tự đếm (${autoKpi.liveClockFormatted})"
                        else ->
                            "Tự đếm khi học trên Học, Luyện, Thi (${autoKpi.liveClockFormatted})"
                    }
                    AutoKpiProgressRow(
                        icon = Icons.Rounded.Timer,
                        titleVi = "Thời gian tự học",
                        pillText = if (autoKpi.isStudyTimeTargetMet) {
                            "✓ Đạt"
                        } else {
                            "${autoKpi.dailyStudyMinutes}/${autoKpi.targetMinutesPerDay} phút"
                        },
                        subtitleVi = studySubtitle,
                        progress = autoKpi.studyTimeProgressFraction,
                        isMet = autoKpi.isStudyTimeTargetMet,
                        accentColor = PracticeTeal,
                        testTagName = "auto_kpi_time_row"
                    )
                }
            }

            HorizontalDivider(color = CardBorderSoft, thickness = 1.dp)

            // 7-Day Weekly Dot Calendar (T2–CN)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.weekly_calendar_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = InkSecondary
                    )
                    if (autoKpi.isAllDailyKpiMet) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = EmeraldSuccessLight
                        ) {
                            Text(
                                text = stringResource(R.string.daily_goal_done_badge),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldOnContainer,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weekly_dot_calendar"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WeekdayItem.entries.forEach { day ->
                        val isToday = day.id == todayWeekdayId
                        val isDone = weeklyActiveDays.contains(day.id)
                        val isScheduled = notificationPrefs.masterEnabled &&
                            notificationPrefs.repeatDays.contains(day.id)

                        WeeklyCalendarDayDot(
                            label = day.labelVi,
                            isToday = isToday,
                            isDone = isDone,
                            isScheduled = isScheduled
                        )
                    }
                }
            }

            // Quick Notification Schedule Strip -> Opens Schedule Settings
            val nextSlot = notificationPrefs.slots.firstOrNull { it.enabled }
            val activeCount = notificationPrefs.slots.count { it.enabled }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (notificationPrefs.masterEnabled) CardSurfaceTinted else SurfaceCanvas,
                border = BorderStroke(
                    1.dp,
                    if (notificationPrefs.masterEnabled) ElectricIndigo.copy(alpha = 0.16f) else CardBorderSoft
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onOpenNotificationSheet)
                    .minimumInteractiveComponentSize()
                    .testTag("open_scheduler_from_tracker")
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
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(
                                    if (notificationPrefs.masterEnabled) ElectricIndigoLight else CardWhite
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (notificationPrefs.masterEnabled) {
                                    Icons.Rounded.NotificationsActive
                                } else {
                                    Icons.Rounded.NotificationsOff
                                },
                                contentDescription = null,
                                tint = if (notificationPrefs.masterEnabled) ElectricIndigo else InkMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        val summaryText = if (notificationPrefs.masterEnabled && nextSlot != null) {
                            "${stringResource(R.string.next_reminder_prefix)} ${nextSlot.formattedTime()} • $activeCount khung giờ"
                        } else {
                            stringResource(R.string.reminders_paused_label)
                        }
                        Text(
                            text = summaryText,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (notificationPrefs.masterEnabled) ElectricIndigo else InkSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Cài đặt",
                            style = MaterialTheme.typography.labelSmall,
                            color = VividViolet
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = VividViolet,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Strictly READ-ONLY automatic KPI criterion row (non-clickable):
 * Shows icon/green checkmark, title, live progress pill (`7/10 từ`, `12/15 phút` or crisp green `✓ Đạt`),
 * animated progress bar, and concise helper subtitle.
 */
@Composable
fun AutoKpiProgressRow(
    icon: ImageVector,
    titleVi: String,
    pillText: String,
    subtitleVi: String,
    progress: Float,
    isMet: Boolean,
    accentColor: Color,
    testTagName: String,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isMet) EmeraldSuccessLight.copy(alpha = 0.65f) else SurfaceCanvas,
        animationSpec = tween(260),
        label = "auto_kpi_bg_$testTagName"
    )
    val badgeBg by animateColorAsState(
        targetValue = if (isMet) EmeraldSuccess else accentColor.copy(alpha = 0.14f),
        animationSpec = tween(260),
        label = "auto_kpi_badge_$testTagName"
    )
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "auto_kpi_progress_$testTagName"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                width = 1.dp,
                color = if (isMet) EmeraldSuccess.copy(alpha = 0.35f) else CardBorderSoft,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 10.dp, vertical = 9.dp)
            .testTag(testTagName),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isMet) EmeraldSuccess else accentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isMet) Icons.Rounded.Check else icon,
                    contentDescription = if (isMet) "Đã đạt mục tiêu" else titleVi,
                    tint = if (isMet) Color.White else accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            Text(
                text = titleVi,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = InkPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Surface(
                shape = RoundedCornerShape(50),
                color = badgeBg
            ) {
                Text(
                    text = pillText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.5.sp
                    ),
                    color = if (isMet) Color.White else accentColor,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = if (isMet) EmeraldSuccess else accentColor,
            trackColor = CardWhite
        )

        Text(
            text = subtitleVi,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = if (isMet) EmeraldOnContainer else InkMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CircularHabitProgressRing(
    progress: Float,
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val percent = (progress * 100f).roundToInt().coerceIn(0, 100)
    val trackColor = SurfaceCanvas

    Box(
        modifier = modifier.size(92.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(92.dp)) {
            val strokePx = 10.dp.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(size.width - strokePx, size.height - strokePx)

            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            val sweepAngle = (progress * 360f).coerceIn(0f, 360f)
            if (sweepAngle > 0.5f) {
                val brush = if (percent >= 100) {
                    Brush.sweepGradient(listOf(EmeraldSuccess, CyanGlow, EmeraldSuccess))
                } else {
                    Brush.sweepGradient(listOf(ElectricIndigo, VividViolet, CyanGlow, ElectricIndigo))
                }
                drawArc(
                    brush = brush,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                ),
                color = if (completedCount >= totalCount && totalCount > 0) EmeraldSuccess else IndigoOnSurface
            )
            Text(
                text = "$completedCount/$totalCount KPI",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = InkMuted
            )
        }
    }
}

@Composable
private fun WeeklyCalendarDayDot(
    label: String,
    isToday: Boolean,
    isDone: Boolean,
    isScheduled: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.SemiBold
            ),
            color = if (isToday) ElectricIndigo else InkMuted
        )

        val circleModifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
                when {
                    isDone -> Brush.linearGradient(listOf(ElectricIndigo, CyanGlow))
                    isToday -> Brush.linearGradient(listOf(ElectricIndigoLight, ElectricIndigoLight))
                    else -> Brush.linearGradient(listOf(SurfaceCanvas, SurfaceCanvas))
                }
            )
            .border(
                width = if (isToday) 2.dp else 1.dp,
                color = when {
                    isToday -> ElectricIndigo
                    isDone -> Color.Transparent
                    isScheduled -> ElectricIndigo.copy(alpha = 0.20f)
                    else -> CardBorderSoft
                },
                shape = CircleShape
            )

        Box(
            modifier = circleModifier,
            contentAlignment = Alignment.Center
        ) {
            when {
                isDone -> {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "$label đã học",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                isScheduled -> {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isToday) ElectricIndigo else VividViolet.copy(alpha = 0.45f))
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(InkMuted.copy(alpha = 0.30f))
                    )
                }
            }
        }
    }
}
