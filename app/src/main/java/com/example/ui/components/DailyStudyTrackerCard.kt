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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.DailyHabitTask
import com.example.data.DefaultHabitData
import com.example.data.NotificationPreferences
import com.example.data.WeekdayItem
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardSurfaceTinted
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.IndigoOnSurface
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.StreakBg
import com.example.ui.theme.StreakOrange
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.VividViolet
import kotlin.math.roundToInt

/**
 * Daily Study Habit Tracker card on Tab 1 (Lộ trình):
 * - Animated circular progress ring + interactive daily checklist
 * - Streak Counter paired with flickering AnimatedStreakFlame
 * - 7-day weekly dot calendar (T2–CN)
 * - Quick entry strip into the Custom Notification Scheduler Bottom Sheet
 */
@Composable
fun DailyStudyTrackerCard(
    completedHabitIds: Set<String>,
    streakDays: Int,
    weeklyActiveDays: Set<String>,
    todayWeekdayId: String,
    notificationPrefs: NotificationPreferences,
    onToggleHabit: (String) -> Unit,
    onOpenNotificationSheet: () -> Unit,
    dailySelfStudyMinutes: Int = 0,
    modifier: Modifier = Modifier
) {
    val tasks = DefaultHabitData.dailyChecklist
    val completedCount = completedHabitIds.count { id -> tasks.any { it.id == id } }
    val progress = if (tasks.isEmpty()) 0f else completedCount.toFloat() / tasks.size.toFloat()

    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    val animatedSweep by animateFloatAsState(
        targetValue = if (entered) progress else 0f,
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "daily_habit_ring_sweep"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = ElectricIndigo.copy(alpha = 0.08f),
                spotColor = ElectricIndigo.copy(alpha = 0.14f)
            )
            .testTag("daily_study_tracker_card"),
        shape = RoundedCornerShape(24.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, CardBorderSoft)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Title + Streak Flame Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.daily_habit_card_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.5.sp),
                        color = InkPrimary
                    )
                    Text(
                        text = if (dailySelfStudyMinutes > 0) {
                            "Đã tự học hôm nay: $dailySelfStudyMinutes phút • Duy trì đều đặn nhé!"
                        } else {
                            stringResource(R.string.daily_habit_subtitle)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (dailySelfStudyMinutes > 0) EmeraldSuccess else InkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = StreakBg,
                    border = BorderStroke(1.dp, StreakOrange.copy(alpha = 0.30f)),
                    modifier = Modifier.testTag("tracker_streak_pill")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        AnimatedStreakFlame(size = 18.dp)
                        Text(
                            text = stringResource(R.string.streak_days_label, streakDays),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = StreakOrange
                        )
                    }
                }
            }

            // Center Row: Animated Circular Progress Ring + Interactive Checklist
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CircularHabitProgressRing(
                    progress = animatedSweep,
                    completedCount = completedCount,
                    totalCount = tasks.size,
                    modifier = Modifier.testTag("daily_habit_progress_ring")
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tasks.forEach { task ->
                        val isChecked = completedHabitIds.contains(task.id)
                        DailyChecklistItemRow(
                            task = task,
                            isChecked = isChecked,
                            onClick = { onToggleHabit(task.id) }
                        )
                    }
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
                    if (completedCount == tasks.size && tasks.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = EmeraldSuccessLight
                        ) {
                            Text(
                                text = stringResource(R.string.daily_goal_done_badge),
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldSuccess,
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

            // Quick Notification Schedule Strip -> Opens Notification Settings Bottom Sheet
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
                        Icon(
                            imageVector = if (notificationPrefs.masterEnabled) {
                                Icons.Rounded.NotificationsActive
                            } else {
                                Icons.Rounded.NotificationsOff
                            },
                            contentDescription = null,
                            tint = if (notificationPrefs.masterEnabled) ElectricIndigo else InkMuted,
                            modifier = Modifier.size(18.dp)
                        )
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
        modifier = modifier.size(96.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(96.dp)) {
            val strokePx = 10.dp.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(size.width - strokePx, size.height - strokePx)

            // Soft track ring
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Gradient active sweep arc
            val sweepAngle = (progress * 360f).coerceIn(0f, 360f)
            if (sweepAngle > 0.5f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(ElectricIndigo, VividViolet, CyanGlow, ElectricIndigo)
                    ),
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
                    fontSize = 19.sp
                ),
                color = if (completedCount == totalCount && totalCount > 0) EmeraldSuccess else IndigoOnSurface
            )
            Text(
                text = "$completedCount/$totalCount mục",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = InkMuted
            )
        }
    }
}

@Composable
private fun DailyChecklistItemRow(
    task: DailyHabitTask,
    isChecked: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isChecked) EmeraldSuccessLight.copy(alpha = 0.65f) else SurfaceCanvas,
        animationSpec = tween(200),
        label = "habit_row_bg"
    )
    val checkBg by animateColorAsState(
        targetValue = if (isChecked) EmeraldSuccess else CardWhite,
        animationSpec = tween(200),
        label = "habit_check_bg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("habit_item_${task.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(checkBg)
                .border(
                    width = 1.5.dp,
                    color = if (isChecked) EmeraldSuccess else ElectricIndigo.copy(alpha = 0.35f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isChecked) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Đã hoàn thành",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.titleVi,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 12.5.sp,
                    textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
                ),
                color = if (isChecked) InkSecondary else InkPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = task.subtitleVi,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                color = InkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
