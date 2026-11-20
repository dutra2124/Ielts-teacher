package com.example.ui.screens.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DiligenceDashboardSnapshot
import com.example.data.HeatmapDayCell
import com.example.data.JourneyMilestone
import com.example.data.PracticeSkill
import com.example.data.SkillBandProgress
import com.example.data.WeeklyBarDay
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardSurfaceTinted
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanGlowLight
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
import com.example.ui.theme.LocalApexColors
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.PracticeTeal
import com.example.ui.theme.PracticeTealLight
import com.example.ui.theme.StreakBg
import com.example.ui.theme.StreakOrange
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.TealOnSurface
import com.example.ui.theme.VividViolet
import com.example.ui.theme.VividVioletLight
import java.util.Locale

/**
 * 1. Diligence Score Card ("Chỉ số chăm chỉ") with animated 0–100% radial gauge,
 * twinkling vector badge, and 4 soft pastel-tinted metric cards.
 */
@Composable
fun DiligenceScoreCard(
    snapshot: DiligenceDashboardSnapshot,
    streakDays: Int,
    modifier: Modifier = Modifier
) {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    val animatedScore by animateIntAsState(
        targetValue = if (entered) snapshot.diligenceScore else 0,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "diligence_score_int"
    )
    val animatedSweep by animateFloatAsState(
        targetValue = if (entered) snapshot.diligenceScore / 100f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "diligence_gauge_sweep"
    )

    val pulseTransition = rememberInfiniteTransition(label = "diligence_badge_pulse")
    val badgeGlow by pulseTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_glow"
    )

    val trackColor = SurfaceCanvas

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = ElectricIndigo.copy(alpha = 0.08f)
            )
            .testTag("diligence_score_card"),
        shape = RoundedCornerShape(24.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, CardBorderSoft)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Chỉ số chăm chỉ",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        ),
                        color = InkPrimary
                    )
                    Text(
                        text = snapshot.gradeLabelVi,
                        style = MaterialTheme.typography.bodySmall,
                        color = IndigoOnSurface,
                        maxLines = 2
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = GoldMasteryLight.copy(alpha = badgeGlow),
                    border = BorderStroke(1.dp, GoldMastery.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EmojiEvents,
                            contentDescription = null,
                            tint = GoldMastery,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (snapshot.diligenceScore >= 75) "Học viên xuất sắc" else "Bền bỉ mỗi ngày",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GoldOnContainer
                        )
                    }
                }
            }

            // Radial Gauge + 4 Pastel Metric Tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.size(112.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(112.dp)) {
                        val strokeW = 12.dp.toPx()
                        val pad = strokeW / 2f
                        val arcSize = Size(size.width - strokeW, size.height - strokeW)
                        drawArc(
                            color = trackColor,
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            topLeft = Offset(pad, pad),
                            size = arcSize,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(CyanGlow, ElectricIndigo, VividViolet, StreakOrange)
                            ),
                            startAngle = 135f,
                            sweepAngle = 270f * animatedSweep,
                            useCenter = false,
                            topLeft = Offset(pad, pad),
                            size = arcSize,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$animatedScore%",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = InkPrimary
                        )
                        Text(
                            text = "Chuyên cần",
                            style = MaterialTheme.typography.labelSmall,
                            color = InkMuted
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PastelMetricPill(
                            icon = Icons.Rounded.LocalFireDepartment,
                            value = "${streakDays} ngày",
                            label = "Chuỗi học",
                            container = StreakBg,
                            accent = StreakOrange,
                            modifier = Modifier.weight(1f)
                        )
                        PastelMetricPill(
                            icon = Icons.Rounded.TaskAlt,
                            value = "${snapshot.dailyGoalRate}%",
                            label = "Mục tiêu ngày",
                            container = EmeraldSuccessLight,
                            accent = EmeraldOnContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PastelMetricPill(
                            icon = Icons.Rounded.MenuBook,
                            value = "${snapshot.vocabReviewedCount} từ",
                            label = "Từ C1–C2 ôn",
                            container = ElectricIndigoLight,
                            accent = IndigoOnSurface,
                            modifier = Modifier.weight(1f)
                        )
                        PastelMetricPill(
                            icon = Icons.Rounded.Schedule,
                            value = "${snapshot.weeklyMinutesTotal}p",
                            label = "Phút tuần này",
                            container = PracticeTealLight,
                            accent = TealOnSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PastelMetricPill(
    icon: ImageVector,
    value: String,
    label: String,
    container: Color,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = container,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = accent,
                    maxLines = 1
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = InkSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 2. 28-Day Study Consistency Heatmap ("Lịch nhiệt 28 ngày") — GitHub-style friendly
 * rounded squares (light indigo -> deep violet) with interactive tap-to-view daily summary.
 */
@Composable
fun ConsistencyHeatmapCard(
    heatmapDays: List<HeatmapDayCell>,
    modifier: Modifier = Modifier
) {
    val isDark = LocalApexColors.current.isDark
    var selectedCellIndex by remember(heatmapDays.size) {
        mutableIntStateOf((heatmapDays.lastIndex).coerceAtLeast(0))
    }
    val selectedCell = heatmapDays.getOrNull(selectedCellIndex)

    fun heatColor(level: Int): Color = if (isDark) {
        when (level) {
            0 -> Color(0xFF1E293B)
            1 -> Color(0xFF312E81)
            2 -> Color(0xFF4338CA)
            3 -> Color(0xFF6D28D9)
            else -> Color(0xFF9333EA)
        }
    } else {
        when (level) {
            0 -> Color(0xFFE8EDF8)
            1 -> Color(0xFFC7D2FE)
            2 -> Color(0xFF818CF8)
            3 -> Color(0xFF6366F1)
            else -> Color(0xFF6D28D9)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("consistency_heatmap_card"),
        shape = RoundedCornerShape(24.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, CardBorderSoft)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarMonth,
                        contentDescription = null,
                        tint = VividViolet,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Lịch nhiệt 28 ngày",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = InkPrimary
                        )
                        Text(
                            text = "Chạm vào từng ô để xem nhật ký ngày hôm đó",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted
                        )
                    }
                }

                // Compact Intensity Legend
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text("Ít", style = MaterialTheme.typography.labelSmall, color = InkMuted)
                    (0..4).forEach { lvl ->
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(heatColor(lvl))
                        )
                    }
                    Text("Chăm", style = MaterialTheme.typography.labelSmall, color = InkMuted)
                }
            }

            // 4 rows x 7 rounded squares
            val weeks = heatmapDays.chunked(7)
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                weeks.forEachIndexed { wIdx, weekCells ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "W${wIdx + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = InkMuted,
                            modifier = Modifier.width(24.dp)
                        )
                        weekCells.forEach { cell ->
                            val isSelected = cell.dayIndex == selectedCellIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.18f)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(heatColor(cell.intensityLevel))
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(
                                                width = 2.dp,
                                                color = if (isDark) Color.White else StreakOrange,
                                                shape = RoundedCornerShape(9.dp)
                                            )
                                        } else if (cell.isToday) {
                                            Modifier.border(
                                                width = 1.5.dp,
                                                color = CyanGlow,
                                                shape = RoundedCornerShape(9.dp)
                                            )
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .clickable { selectedCellIndex = cell.dayIndex }
                                    .testTag("heatmap_day_${cell.dayIndex}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (cell.isToday) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Selected Day Friendly Summary Strip
            if (selectedCell != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = VividVioletLight.copy(alpha = 0.72f),
                    border = BorderStroke(1.dp, VividViolet.copy(alpha = 0.25f))
                ) {
                    AnimatedContent(
                        targetState = selectedCell,
                        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                        label = "heatmap_day_summary"
                    ) { day ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = VividViolet
                            ) {
                                Text(
                                    text = "${day.weekdayShort} ${day.dateLabel}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                            Text(
                                text = day.summaryVi,
                                style = MaterialTheme.typography.bodySmall,
                                color = InkPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. Weekly Study Activity Bar Chart (T2–CN) with horizontal dashed daily target line.
 */
@Composable
fun WeeklyStudyActivityChartCard(
    bars: List<WeeklyBarDay>,
    dailyTargetMinutes: Int,
    weeklyMinutesTotal: Int,
    modifier: Modifier = Modifier
) {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    val maxMinutes = remember(bars, dailyTargetMinutes) {
        (bars.maxOfOrNull { it.minutes } ?: dailyTargetMinutes)
            .coerceAtLeast(dailyTargetMinutes + 15)
            .toFloat()
    }
    val targetRatio = (dailyTargetMinutes / maxMinutes).coerceIn(0.15f, 0.90f)
    val trackBg = SurfaceCanvas
    val targetLineColor = StreakOrange

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_activity_chart_card"),
        shape = RoundedCornerShape(24.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, CardBorderSoft)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BarChart,
                        contentDescription = null,
                        tint = ElectricIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Hoạt động học 7 ngày (T2–CN)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = InkPrimary
                        )
                        Text(
                            text = "Đường nét đứt cam = Mục tiêu ${dailyTargetMinutes} phút/ngày",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = CyanGlowLight
                ) {
                    Text(
                        text = "${weeklyMinutesTotal}p / tuần",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TealOnSurface,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            // Chart Canvas + Bars
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
            ) {
                // Dashed target line across the bar area
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(116.dp)
                        .align(Alignment.TopCenter)
                ) {
                    val yTarget = size.height * (1f - targetRatio)
                    drawLine(
                        color = targetLineColor.copy(alpha = 0.72f),
                        start = Offset(0f, yTarget),
                        end = Offset(size.width, yTarget),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    bars.forEach { bar ->
                        val ratio = (bar.minutes / maxMinutes).coerceIn(0.05f, 1f)
                        val animatedRatio by animateFloatAsState(
                            targetValue = if (entered) ratio else 0.05f,
                            animationSpec = tween(750, easing = FastOutSlowInEasing),
                            label = "bar_${bar.dayId}"
                        )
                        val reachedTarget = bar.minutes >= dailyTargetMinutes
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = if (bar.minutes > 0) "${bar.minutes}p" else "—",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = if (bar.isToday) FontWeight.ExtraBold else FontWeight.Medium
                                ),
                                color = if (bar.isToday) IndigoOnSurface else InkMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.72f)
                                    .height(94.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(trackBg),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(animatedRatio)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            brush = when {
                                                bar.isToday -> Brush.verticalGradient(listOf(CyanGlow, ElectricIndigo))
                                                reachedTarget -> Brush.verticalGradient(listOf(VividViolet, ElectricIndigo))
                                                else -> Brush.verticalGradient(listOf(ElectricIndigo.copy(alpha = 0.45f), ElectricIndigo.copy(alpha = 0.65f)))
                                            }
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = bar.labelVi,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (bar.isToday) FontWeight.ExtraBold else FontWeight.Medium
                                ),
                                color = if (bar.isToday) IndigoOnSurface else InkSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 4. 4-Skill Band Radar + Progress Bars (6.0 -> 8.5) & Learning Journey Timeline ("Quá trình học của bạn").
 */
@Composable
fun SkillRadarAndJourneyCard(
    skills: List<SkillBandProgress>,
    milestones: List<JourneyMilestone>,
    onSkillClick: (PracticeSkill) -> Unit,
    modifier: Modifier = Modifier
) {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    val gridColor = CardBorderSoft
    val radarFill = ElectricIndigo.copy(alpha = 0.24f)
    val radarStroke = ElectricIndigo
    val targetStroke = EmeraldSuccess.copy(alpha = 0.55f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("skill_radar_and_journey_card"),
        shape = RoundedCornerShape(24.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, CardBorderSoft)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Radar,
                    contentDescription = null,
                    tint = PracticeTeal,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "Biểu đồ 4 kỹ năng (6.0 → 8.5)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = InkPrimary
                    )
                    Text(
                        text = "So sánh phong độ hiện tại với đích đến Band 8.5",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted
                    )
                }
            }

            // 4-Axis Diamond Radar Chart (Top=Listening, Right=Reading, Bottom=Writing, Left=Speaking)
            val lVal = ((skills.firstOrNull { it.skill == PracticeSkill.LISTENING }?.currentBand ?: 6.5f) - 5.0f) / 4.0f
            val rVal = ((skills.firstOrNull { it.skill == PracticeSkill.READING }?.currentBand ?: 6.5f) - 5.0f) / 4.0f
            val wVal = ((skills.firstOrNull { it.skill == PracticeSkill.WRITING }?.currentBand ?: 6.0f) - 5.0f) / 4.0f
            val sVal = ((skills.firstOrNull { it.skill == PracticeSkill.SPEAKING }?.currentBand ?: 6.5f) - 5.0f) / 4.0f

            val animScale by animateFloatAsState(
                targetValue = if (entered) 1f else 0.1f,
                animationSpec = tween(850, easing = FastOutSlowInEasing),
                label = "radar_scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(134.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val radius = size.minDimension * 0.46f

                    // Concentric diamond reference rings (Band 6.0, 7.0, 8.0, 9.0)
                    listOf(0.25f, 0.50f, 0.75f, 1.0f).forEach { frac ->
                        val r = radius * frac
                        val p = Path().apply {
                            moveTo(cx, cy - r)
                            lineTo(cx + r, cy)
                            lineTo(cx, cy + r)
                            lineTo(cx - r, cy)
                            close()
                        }
                        drawPath(p, color = gridColor, style = Stroke(width = 1.dp.toPx()))
                    }

                    // Axes
                    drawLine(gridColor, Offset(cx, cy - radius), Offset(cx, cy + radius), 1.dp.toPx())
                    drawLine(gridColor, Offset(cx - radius, cy), Offset(cx + radius, cy), 1.dp.toPx())

                    // Target 8.5 dashed diamond (8.5 -> (8.5 - 5)/4 = 0.875)
                    val targetR = radius * 0.875f
                    val targetPath = Path().apply {
                        moveTo(cx, cy - targetR)
                        lineTo(cx + targetR, cy)
                        lineTo(cx, cy + targetR)
                        lineTo(cx - targetR, cy)
                        close()
                    }
                    drawPath(
                        targetPath,
                        color = targetStroke,
                        style = Stroke(
                            width = 1.6.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )
                    )

                    // User's actual 4-skill polygon
                    val topY = cy - radius * (lVal.coerceIn(0.15f, 1f) * animScale)
                    val rightX = cx + radius * (rVal.coerceIn(0.15f, 1f) * animScale)
                    val bottomY = cy + radius * (wVal.coerceIn(0.15f, 1f) * animScale)
                    val leftX = cx - radius * (sVal.coerceIn(0.15f, 1f) * animScale)

                    val userPath = Path().apply {
                        moveTo(cx, topY)
                        lineTo(rightX, cy)
                        lineTo(cx, bottomY)
                        lineTo(leftX, cy)
                        close()
                    }
                    drawPath(userPath, color = radarFill)
                    drawPath(userPath, color = radarStroke, style = Stroke(width = 2.4.dp.toPx()))

                    listOf(
                        Offset(cx, topY),
                        Offset(rightX, cy),
                        Offset(cx, bottomY),
                        Offset(leftX, cy)
                    ).forEach { pt ->
                        drawCircle(Color.White, radius = 4.dp.toPx(), center = pt)
                        drawCircle(radarStroke, radius = 2.6.dp.toPx(), center = pt)
                    }
                }

                // Axis Labels around Diamond
                Text(
                    text = "Listening",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = IndigoOnSurface,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
                Text(
                    text = "Writing",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = IndigoOnSurface,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
                Text(
                    text = "Speaking",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TealOnSurface,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 6.dp)
                )
                Text(
                    text = "Reading",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TealOnSurface,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 6.dp)
                )
            }

            // 4 Skill Progress Bars (tap any skill to jump to Tab 3 Practice!)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                skills.forEach { sk ->
                    val frac = ((sk.currentBand - 5.5f) / (sk.targetBand - 5.5f)).coerceIn(0.12f, 1f)
                    val animFrac by animateFloatAsState(
                        targetValue = if (entered) frac else 0.08f,
                        animationSpec = tween(750, easing = FastOutSlowInEasing),
                        label = "skill_bar_${sk.skill.name}"
                    )
                    val barColor = when (sk.skill) {
                        PracticeSkill.LISTENING -> ElectricIndigo
                        PracticeSkill.READING -> PracticeTeal
                        PracticeSkill.WRITING -> VividViolet
                        PracticeSkill.SPEAKING -> StreakOrange
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CardSurfaceTinted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSkillClick(sk.skill) }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = sk.labelVi,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = InkPrimary
                                )
                                Text(
                                    text = "Band ${String.format(Locale.US, "%.1f", sk.currentBand)} / 8.5",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = barColor
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(SurfaceCanvas)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animFrac)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(barColor)
                                )
                            }
                            Text(
                                text = sk.noteVi,
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSecondary
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = CardBorderSoft)

            // Compact Vertical Learning Journey Timeline ("Quá trình học của bạn")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Flag,
                    contentDescription = null,
                    tint = EmeraldSuccess,
                    modifier = Modifier.size(19.dp)
                )
                Text(
                    text = "Quá trình học của bạn",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = InkPrimary
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                milestones.forEachIndexed { idx, ms ->
                    val isLast = idx == milestones.lastIndex
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = CircleShape,
                                color = when {
                                    ms.isAchieved -> EmeraldSuccess
                                    ms.isCurrentGoal -> ElectricIndigo
                                    else -> SurfaceCanvas
                                },
                                border = BorderStroke(
                                    width = 1.5.dp,
                                    color = when {
                                        ms.isAchieved -> EmeraldSuccess
                                        ms.isCurrentGoal -> CyanGlow
                                        else -> CardBorderSoft
                                    }
                                ),
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (ms.isAchieved) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    } else if (ms.isCurrentGoal) {
                                        Icon(
                                            imageVector = Icons.Rounded.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                            if (!isLast) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(34.dp)
                                        .background(if (ms.isAchieved) EmeraldSuccess.copy(alpha = 0.55f) else CardBorderSoft)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = if (isLast) 2.dp else 14.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = ms.titleVi,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (ms.isAchieved || ms.isCurrentGoal) InkPrimary else InkMuted,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = when {
                                        ms.isAchieved -> EmeraldSuccessLight
                                        ms.isCurrentGoal -> ElectricIndigoLight
                                        else -> SurfaceCanvas
                                    }
                                ) {
                                    Text(
                                        text = ms.badgeText,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = when {
                                            ms.isAchieved -> EmeraldOnContainer
                                            ms.isCurrentGoal -> IndigoOnSurface
                                            else -> InkMuted
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = ms.subtitleVi,
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
