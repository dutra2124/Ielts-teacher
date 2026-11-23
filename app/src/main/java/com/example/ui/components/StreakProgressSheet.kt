package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AutoKpiSnapshot
import com.example.data.StreakMasteryLevel
import com.example.data.StreakStatus
import com.example.data.StreakVisualStyle
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
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseDiffLight
import com.example.ui.theme.SurfaceCanvas

/**
 * Compact Streak Progress Sheet opened when tapping the Streak badge:
 * - Current Mastery level & large animated Growth Tree / Mastery Flame visual
 * - Today's 100% Automatic Daily KPI read-only progress (strictly zero manual checkboxes)
 * - Days remaining before streak loss (3-day inactivity protection window)
 * - Direct Tree / Flame visual style switcher
 * - Visual preview of all 6 evolution stages
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StreakProgressSheet(
    streakStatus: StreakStatus,
    autoKpi: AutoKpiSnapshot,
    onSelectVisualStyle: (StreakVisualStyle) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val kpiPercent = autoKpi.overallPercent
    val animatedKpiProgress by animateFloatAsState(
        targetValue = autoKpi.overallProgressFraction,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "streak_sheet_kpi_progress"
    )

    val currentLevel = streakStatus.masteryLevel
    val accentColor = currentLevel.accentFor(streakStatus.visualStyle)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("streak_progress_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Chuỗi học & Cấp độ Thành thạo",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = InkPrimary
                    )
                    Text(
                        text = "Tự động +1 chuỗi duy nhất 1 lần/ngày khi đạt 100% KPI thực tế",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Đóng", tint = InkSecondary)
                }
            }

            // Hero Current Mastery Stage Card
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = SurfaceCanvas,
                border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = CardWhite,
                        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.35f)),
                        shadowElevation = 3.dp,
                        modifier = Modifier.size(88.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            MasteryStreakIcon(
                                streakDays = streakStatus.currentStreak,
                                style = streakStatus.visualStyle,
                                size = 68.dp,
                                contentDescriptionText = currentLevel.titleVi
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = accentColor.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "Cấp ${currentLevel.stageIndex + 1}/6 • ${currentLevel.titleVi}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = accentColor,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = "${streakStatus.currentStreak} ngày liên tiếp",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = InkPrimary
                        )

                        val nextStage = streakStatus.nextLevel
                        Text(
                            text = if (nextStage != null) {
                                "${currentLevel.stageNameFor(streakStatus.visualStyle)} • Còn ${streakStatus.daysToNextLevel} ngày lên ${nextStage.titleVi}"
                            } else {
                                "${currentLevel.stageNameFor(streakStatus.visualStyle)} • Đỉnh cao IELTS Master 8.5!"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                }
            }

            // 3-Day Inactivity Grace Window / Protection Status
            val hasWarning = streakStatus.graceWarningVi != null
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = when {
                    hasWarning -> RoseDiffLight
                    streakStatus.todayKpiCompleted -> EmeraldSuccessLight
                    else -> GoldMasteryLight.copy(alpha = 0.72f)
                },
                border = BorderStroke(
                    1.dp,
                    when {
                        hasWarning -> RoseAlert.copy(alpha = 0.45f)
                        streakStatus.todayKpiCompleted -> EmeraldSuccess.copy(alpha = 0.40f)
                        else -> GoldMastery.copy(alpha = 0.40f)
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 13.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Icon(
                        imageVector = if (hasWarning) Icons.Rounded.WarningAmber else Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = when {
                            hasWarning -> RoseAlert
                            streakStatus.todayKpiCompleted -> EmeraldOnContainer
                            else -> GoldOnContainer
                        },
                        modifier = Modifier.size(19.dp)
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = when {
                                hasWarning -> streakStatus.graceWarningVi!!
                                streakStatus.todayKpiCompleted -> "Đã giữ vững chuỗi hôm nay (+1) • Quy tắc nghỉ quá 3 ngày mới mất chuỗi"
                                streakStatus.currentStreak == 0 -> "Đạt đủ 2 mục KPI tự động hôm nay để bắt đầu chuỗi ngày đầu tiên!"
                                else -> "Còn ${streakStatus.daysRemainingBeforeLoss} ngày bảo vệ chuỗi trước khi tự đặt lại về 0"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = when {
                                hasWarning -> RoseAlert
                                streakStatus.todayKpiCompleted -> EmeraldOnContainer
                                else -> GoldOnContainer
                            }
                        )
                        if (streakStatus.lastKpiCompletedDate.isNotBlank()) {
                            Text(
                                text = "Lần đạt 100% KPI gần nhất: ${streakStatus.lastKpiCompletedDate}",
                                style = MaterialTheme.typography.labelSmall,
                                color = InkSecondary
                            )
                        }
                    }
                }
            }

            // Today's 100% Automatic Daily KPI Progress Card (Read-Only)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, CardBorderSoft),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "KPI hôm nay (${autoKpi.completedCriteriaCount}/2 tiêu chí tự động)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = InkPrimary
                        )
                        Text(
                            text = "$kpiPercent%",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = if (kpiPercent >= 100) EmeraldSuccess else IndigoOnSurface
                        )
                    }

                    LinearProgressIndicator(
                        progress = { animatedKpiProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = if (kpiPercent >= 100) EmeraldSuccess else ElectricIndigo,
                        trackColor = SurfaceCanvas
                    )

                    AutoKpiProgressRow(
                        icon = Icons.Rounded.MenuBook,
                        titleVi = "Từ vựng đã học hôm nay",
                        pillText = autoKpi.vocabProgressPillVi,
                        subtitleVi = "Tự cộng khi xác nhận Đã thuộc Flashcard, SRS hoặc Mini-game",
                        progress = autoKpi.vocabProgressFraction,
                        isMet = autoKpi.isVocabTargetMet,
                        accentColor = ElectricIndigo,
                        testTagName = "streak_sheet_auto_kpi_vocab"
                    )

                    AutoKpiProgressRow(
                        icon = Icons.Rounded.Timer,
                        titleVi = "Thời gian học chủ động",
                        pillText = autoKpi.studyTimeProgressPillVi,
                        subtitleVi = if (autoKpi.isPausedDueToInactivity) {
                            "Đang tạm dừng do không tương tác >60 giây (${autoKpi.liveClockFormatted})"
                        } else {
                            "Đếm thực tế trên Học, Luyện, Thi • Đã học ${autoKpi.liveClockFormatted}"
                        },
                        progress = autoKpi.studyTimeProgressFraction,
                        isMet = autoKpi.isStudyTimeTargetMet,
                        accentColor = PracticeTeal,
                        testTagName = "streak_sheet_auto_kpi_time"
                    )

                    Text(
                        text = "Bạn có thể điều chỉnh mục tiêu từ/phút trong Cài đặt → Mục tiêu KPI ngày.",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkMuted
                    )
                }
            }

            // Tree / Flame Visual Style Switcher
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Chọn biểu tượng tiến hóa chuỗi học",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = InkPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StreakVisualStyle.entries.forEach { styleOption ->
                        val selected = streakStatus.visualStyle == styleOption
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (selected) ElectricIndigoLight else SurfaceCanvas,
                            border = BorderStroke(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) ElectricIndigo else CardBorderSoft
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onSelectVisualStyle(styleOption) }
                                .minimumInteractiveComponentSize()
                                .testTag("streak_style_option_${styleOption.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MasteryStreakIcon(
                                    streakDays = streakStatus.currentStreak.coerceAtLeast(7),
                                    style = styleOption,
                                    size = 32.dp
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = styleOption.labelVi,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = if (selected) IndigoOnSurface else InkPrimary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Text(
                                        text = if (styleOption == StreakVisualStyle.TREE) "Growth Tree" else "Mastery Flame",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
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

            // 6-Stage Evolution Roadmap Preview
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "6 Cấp độ Tiến hóa (${streakStatus.visualStyle.labelVi})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = InkPrimary
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    maxItemsInEachRow = 3,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StreakMasteryLevel.entries.forEach { stage ->
                        val isCurrent = stage == currentLevel
                        val isUnlocked = streakStatus.currentStreak >= stage.minDays
                        val stageAccent = stage.accentFor(streakStatus.visualStyle)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isCurrent) stageAccent.copy(alpha = 0.14f) else SurfaceCanvas,
                            border = BorderStroke(
                                width = if (isCurrent) 1.5.dp else 1.dp,
                                color = if (isCurrent) stageAccent else CardBorderSoft
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("streak_stage_preview_${stage.stageIndex}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(contentAlignment = Alignment.TopEnd) {
                                    MasteryStreakIcon(
                                        streakDays = stage.previewDays,
                                        style = streakStatus.visualStyle,
                                        size = 36.dp
                                    )
                                    if (!isUnlocked) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(CardWhite),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Lock,
                                                contentDescription = null,
                                                tint = InkMuted,
                                                modifier = Modifier.size(9.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = stage.titleVi,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp
                                    ),
                                    color = if (isCurrent) stageAccent else InkPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = stage.rangeLabelVi,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                    color = InkSecondary,
                                    maxLines = 1,
                                    softWrap = false,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
