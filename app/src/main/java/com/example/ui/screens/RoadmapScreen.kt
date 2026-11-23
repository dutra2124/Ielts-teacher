package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.DailyBandTip
import com.example.data.IeltsCurriculum
import com.example.data.IeltsModule
import com.example.data.PracticeSkill
import com.example.data.ProgressAnalyticsEngine
import com.example.ui.ApexIeltsUiState
import com.example.ui.components.AccountAvatarButton
import com.example.ui.components.AnimatedAudioWaveform
import com.example.ui.components.AnimatedReminderBell
import com.example.ui.components.AnimatedTeacher
import com.example.ui.components.DailyStudyTrackerCard
import com.example.ui.components.MasteryStreakIcon
import com.example.ui.screens.dashboard.ProgressDashboardContent
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardSurfaceTinted
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricIndigoDark
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.IndigoOnSurface
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LocalAdaptiveLayoutInfo
import com.example.ui.theme.LocalApexColors
import com.example.ui.theme.LocalAppThemePrefs
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.ThemeHeroBrush
import com.example.ui.theme.ThemeOnPrimary
import com.example.ui.theme.ThemePrimary
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.TrackChanges
import com.example.data.AutoKpiSnapshot
import com.example.data.SkillBandProgress
import com.example.data.WeakQuestionDiagnostic
import com.example.ui.components.GlassSurface
import com.example.ui.components.tactilePress
import com.example.ui.screens.dashboard.ConsistencyHeatmapCard
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.GoldMastery
import com.example.ui.theme.GoldMasteryLight
import com.example.ui.theme.GoldOnContainer
import com.example.ui.theme.PracticeTeal
import com.example.ui.theme.PracticeTealLight
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseDiffLight
import com.example.ui.theme.StreakOrange
import com.example.ui.theme.TealOnSurface
import com.example.ui.theme.ThemeSecondary
import java.util.Locale

@Composable
fun RoadmapScreen(
    uiState: ApexIeltsUiState,
    onSelectModule: (Int) -> Unit,
    onToggleModuleCompletion: (Int) -> Unit,
    onCycleTip: () -> Unit,
    onTeacherTap: () -> Unit,
    onOpenNotificationSheet: () -> Unit,
    onOpenScheduleSettings: () -> Unit = onOpenNotificationSheet,
    onOpenStreakSheet: () -> Unit = {},
    onOpenThemeSheet: () -> Unit = {},
    onOpenAccountDrawer: () -> Unit = {},
    onToggleAutoGreetVoice: (Boolean) -> Unit = {},
    onPracticeWeakSkill: (PracticeSkill, String) -> Unit = { _, _ -> },
    onOpenLearnTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val layoutInfo = LocalAdaptiveLayoutInfo.current
    val hPad = layoutInfo.horizontalPadding
    val hasCustomBg = LocalAppThemePrefs.current.hasCustomVisualBackground

    // 0 = Lộ trình 12 Module, 1 = Dashboard Chăm chỉ
    var selectedTab1View by rememberSaveable { mutableIntStateOf(0) }
    val dashboardSnapshot = remember(
        uiState.streakDays,
        uiState.autoKpi,
        uiState.weeklyActiveDays,
        uiState.learnedVocabIds,
        uiState.srsWordIds,
        uiState.completedModuleIds,
        uiState.examHistory,
        uiState.studyLogs,
        uiState.dailySelfStudyMinutes,
        uiState.activeModuleId
    ) {
        ProgressAnalyticsEngine.compute(uiState)
    }

    val bestExamPercent = remember(uiState.examHistory) {
        val bestBand = uiState.examHistory.maxOfOrNull { it.estimatedBand } ?: 6.0
        ((bestBand / 8.5) * 100.0).toInt().coerceIn(0, 100)
    }

    if (selectedTab1View == 1) {
        BackHandler { selectedTab1View = 0 }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = layoutInfo.adaptiveCardMinWidth),
        modifier = modifier
            .fillMaxSize()
            .background(if (hasCustomBg) Color.Transparent else SurfaceCanvas)
            .testTag("roadmap_lazy_column"),
        contentPadding = PaddingValues(bottom = 28.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Clean Decluttered Hero Header + Segmented Switcher
        item(key = "hero_and_band_card", span = { GridItemSpan(maxLineSpan) }) {
            Box(modifier = Modifier.fillMaxWidth()) {
                HeroTeacherHeader(
                    uiState = uiState,
                    selectedTab1View = selectedTab1View,
                    diligenceScore = dashboardSnapshot.diligenceScore,
                    onSelectTab1View = { selectedTab1View = it },
                    onCycleTip = onCycleTip,
                    onTeacherTap = onTeacherTap,
                    onOpenStreakSheet = onOpenStreakSheet,
                    onOpenNotificationSheet = onOpenNotificationSheet,
                    onOpenAccountDrawer = onOpenAccountDrawer
                )
            }
        }

        if (selectedTab1View == 1) {
            item(key = "diligence_progress_dashboard", span = { GridItemSpan(maxLineSpan) }) {
                ProgressDashboardContent(
                    snapshot = dashboardSnapshot,
                    streakDays = uiState.streakDays,
                    streakStatus = uiState.streakStatus,
                    autoGreetEnabled = uiState.autoGreetDashboardVoice,
                    onToggleAutoGreet = onToggleAutoGreetVoice,
                    onOpenStreakSheet = onOpenStreakSheet,
                    onPracticeWeakSkill = onPracticeWeakSkill,
                    modifier = Modifier.offset(y = (-16).dp)
                )
            }
        } else {
            // 2. High-Contrast "Tiếp tục học ngay" (1-Tap Smart Resume) Hero Card
            item(
                key = "smart_resume_hero_card",
                span = { GridItemSpan(if (layoutInfo.useMultiColumnGrid) 1 else maxLineSpan) }
            ) {
                SmartResumeHeroCard(
                    currentBand = uiState.currentBand,
                    completedCount = uiState.completedModuleIds.size,
                    totalCount = uiState.modules.size,
                    activeModule = uiState.activeModule,
                    srsDueCount = uiState.srsReviewWords.size,
                    autoKpi = uiState.autoKpi,
                    onResumeNow = onOpenLearnTab,
                    modifier = Modifier
                        .padding(horizontal = hPad)
                        .offset(y = (-18).dp)
                )
            }

            // 3. IELTS 4-Skill Estimated Band & Top-2 Weakness Diagnostic Card
            item(
                key = "four_skill_diagnostic_card",
                span = { GridItemSpan(if (layoutInfo.useMultiColumnGrid) 1 else maxLineSpan) }
            ) {
                FourSkillDiagnosticCard(
                    skills = dashboardSnapshot.skillProgress,
                    weakQuestionTypes = dashboardSnapshot.topWeakQuestionTypes,
                    onPracticeWeakSkill = onPracticeWeakSkill,
                    modifier = Modifier
                        .padding(horizontal = hPad)
                        .offset(y = if (layoutInfo.useMultiColumnGrid) (-18).dp else (-8).dp)
                )
            }

            // 4. Compact Collapsible 28-Day Consistency Heatmap (collapsed by default on compact phones)
            item(
                key = "compact_28d_heatmap",
                span = { GridItemSpan(if (layoutInfo.useMultiColumnGrid) 1 else maxLineSpan) }
            ) {
                ConsistencyHeatmapCard(
                    heatmapDays = dashboardSnapshot.heatmapDays,
                    collapsible = true,
                    initiallyExpanded = layoutInfo.useMultiColumnGrid,
                    modifier = Modifier
                        .padding(horizontal = hPad)
                        .padding(top = 2.dp, bottom = 8.dp)
                )
            }

            // 5. Read-Only Automatic Daily Study KPI Tracker
            item(
                key = "daily_study_tracker_card",
                span = { GridItemSpan(if (layoutInfo.useMultiColumnGrid) 1 else maxLineSpan) }
            ) {
                DailyStudyTrackerCard(
                    autoKpi = uiState.autoKpi,
                    streakDays = uiState.streakDays,
                    weeklyActiveDays = uiState.weeklyActiveDays,
                    todayWeekdayId = uiState.todayWeekdayId,
                    notificationPrefs = uiState.notificationPrefs,
                    onOpenNotificationSheet = onOpenScheduleSettings,
                    streakStatus = uiState.streakStatus,
                    onOpenStreakSheet = onOpenStreakSheet,
                    modifier = Modifier
                        .padding(horizontal = hPad)
                        .padding(bottom = 10.dp)
                )
            }

            // 6. Prominent Roadmap Section Heading
            item(key = "roadmap_heading", span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = hPad + 2.dp)
                        .padding(bottom = 8.dp),
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
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = ThemePrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.roadmap_section_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        ),
                        color = InkPrimary
                    )
                }
            }

            // 5. Adaptive 3-Tier / 12-Module Animated Vertical Journey
            IeltsCurriculum.tierOrder.forEach { tier ->
                val tierModules = uiState.modules.filter { it.tier == tier }
                val completedInTier = tierModules.count { uiState.completedModuleIds.contains(it.id) }

                item(key = "tier_header_${tier.tierNumber}", span = { GridItemSpan(maxLineSpan) }) {
                    TierHeaderBanner(
                        tier = tier,
                        completedCount = completedInTier,
                        totalCount = tierModules.size,
                        modifier = Modifier.padding(horizontal = hPad, vertical = 6.dp)
                    )
                }

                tierModules.forEachIndexed { indexInTier, module ->
                    item(
                        key = "module_node_${module.id}",
                        span = { GridItemSpan(if (layoutInfo.useMultiColumnGrid) 1 else maxLineSpan) }
                    ) {
                        val isSelected = uiState.activeModuleId == module.id
                        val isCompleted = uiState.completedModuleIds.contains(module.id)
                        val globalIndex = module.id - 1
                        val nodeAnchor = if (layoutInfo.useMultiColumnGrid) 0.16f else windingAnchorFor(globalIndex)
                        val hasNextInTier = indexInTier < tierModules.lastIndex
                        val nextAnchor = if (hasNextInTier) windingAnchorFor(globalIndex + 1) else nodeAnchor

                        val metrics = remember(
                            module.id,
                            isCompleted,
                            isSelected,
                            uiState.allVocabulary,
                            uiState.learnedVocabIds,
                            uiState.completedHabitIds,
                            bestExamPercent
                        ) {
                            computeModuleMetrics(
                                moduleId = module.id,
                                isCompleted = isCompleted,
                                isSelected = isSelected,
                                uiState = uiState,
                                bestExamPercent = bestExamPercent
                            )
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            WindingModuleRow(
                                module = module,
                                metrics = metrics,
                                anchorFraction = nodeAnchor,
                                isSelected = isSelected,
                                isCompleted = isCompleted,
                                onSelect = { onSelectModule(module.id) },
                                onToggleCompleted = { onToggleModuleCompletion(module.id) },
                                onOpenLearnTab = onOpenLearnTab,
                                modifier = Modifier.padding(horizontal = hPad)
                            )

                            if (!layoutInfo.useMultiColumnGrid && hasNextInTier) {
                                val nextCompleted = uiState.completedModuleIds.contains(module.id + 1)
                                WindingPathConnector(
                                    startFraction = nodeAnchor,
                                    endFraction = nextAnchor,
                                    accentColor = tier.accentPrimary,
                                    secondaryColor = tier.accentSecondary,
                                    isLit = isCompleted || isSelected || nextCompleted,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(34.dp)
                                        .padding(horizontal = hPad)
                                )
                            } else {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun computeModuleMetrics(
    moduleId: Int,
    isCompleted: Boolean,
    isSelected: Boolean,
    uiState: ApexIeltsUiState,
    bestExamPercent: Int
): ModuleVisualMetrics {
    if (isCompleted) {
        return ModuleVisualMetrics(vocabPercent = 100, drillsPercent = 100, examPercent = 100)
    }
    val moduleWords = uiState.allVocabulary.filter { it.moduleId == moduleId }
    val learnedInModule = moduleWords.count { uiState.learnedVocabIds.contains(it.id) }
    val vocabPct = if (moduleWords.isNotEmpty()) {
        (learnedInModule * 100 / moduleWords.size).coerceIn(0, 100)
    } else {
        if (isSelected) 25 else 0
    }

    val habitRatio = (uiState.completedHabitIds.size * 25).coerceAtMost(75)
    val drillsPct = when {
        vocabPct >= 70 -> (vocabPct * 0.85f).toInt().coerceIn(20, 95)
        isSelected -> maxOf(habitRatio, (vocabPct * 0.7f).toInt()).coerceIn(15, 90)
        else -> (vocabPct / 2).coerceIn(0, 60)
    }

    val examPct = when {
        isSelected -> bestExamPercent.coerceAtLeast(20)
        moduleId < uiState.activeModuleId -> bestExamPercent
        else -> 0
    }
    return ModuleVisualMetrics(
        vocabPercent = vocabPct,
        drillsPercent = drillsPct,
        examPercent = examPct
    )
}

private fun windingAnchorFor(index: Int): Float = when (index % 4) {
    0 -> 0.16f
    1 -> 0.84f
    2 -> 0.16f
    else -> 0.84f
}

@Composable
private fun HeroTeacherHeader(
    uiState: ApexIeltsUiState,
    selectedTab1View: Int,
    diligenceScore: Int,
    onSelectTab1View: (Int) -> Unit,
    onCycleTip: () -> Unit,
    onTeacherTap: () -> Unit,
    onOpenStreakSheet: () -> Unit,
    onOpenNotificationSheet: () -> Unit,
    onOpenAccountDrawer: () -> Unit
) {
    val apexColors = LocalApexColors.current
    val heroInk = apexColors.heroInk
    val heroInkMuted = heroInk.copy(alpha = 0.80f)
    val primary = ThemePrimary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
            .background(ThemeHeroBrush)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CyanGlow.copy(alpha = 0.28f), Color.Transparent),
                        center = Offset(size.width * 0.90f, size.height * 0.14f),
                        radius = size.width * 0.44f
                    ),
                    radius = size.width * 0.44f,
                    center = Offset(size.width * 0.90f, size.height * 0.14f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(heroInk.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(size.width * 0.10f, size.height * 0.76f),
                        radius = size.width * 0.36f
                    ),
                    radius = size.width * 0.36f,
                    center = Offset(size.width * 0.10f, size.height * 0.76f)
                )
            }
            .statusBarsPadding()
            .displayCutoutPadding()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 28.dp)
    ) {
        // Decluttered Top Bar: Brand + Streak + Reminder Bell + Account Avatar
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isCompactWidth = maxWidth < 360.dp

            val brandRow: @Composable (Modifier) -> Unit = { rowMod ->
                Row(
                    modifier = rowMod,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(15.dp),
                        color = heroInk.copy(alpha = 0.22f),
                        border = BorderStroke(1.2.dp, heroInk.copy(alpha = 0.36f)),
                        shadowElevation = 3.dp
                    ) {
                        Box(
                            modifier = Modifier.size(46.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TrendingUp,
                                contentDescription = null,
                                tint = heroInk,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Master Ielts",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = if (isCompactWidth) 18.sp else 20.sp
                                ),
                                color = heroInk,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = heroInk.copy(alpha = 0.20f),
                                border = BorderStroke(1.dp, heroInk.copy(alpha = 0.35f)),
                                modifier = Modifier.wrapContentWidth()
                            ) {
                                Text(
                                    text = "8.5",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = heroInk,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Lộ trình 6.0 → 8.5 • AI Mentor",
                            style = MaterialTheme.typography.bodySmall,
                            color = heroInkMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            val actionControls: @Composable () -> Unit = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Prominent Evolving Mastery Streak Pill (32dp Tree/Flame icon — tap to open Streak Progress Sheet)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = heroInk.copy(alpha = 0.20f),
                        border = BorderStroke(
                            width = 1.2.dp,
                            color = if (uiState.streakStatus.graceWarningVi != null) {
                                Color(0xFFFBBF24)
                            } else {
                                heroInk.copy(alpha = 0.38f)
                            }
                        ),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .wrapContentWidth()
                            .tactilePress()
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onOpenStreakSheet)
                            .testTag("streak_badge")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            MasteryStreakIcon(
                                streakDays = uiState.streakStatus.currentStreak,
                                style = uiState.streakStatus.visualStyle,
                                size = 32.dp,
                                contentDescriptionText = "Chuỗi học liên tiếp • Nhấn để xem tiến trình"
                            )
                            Text(
                                text = stringResource(R.string.streak_days_label, uiState.streakStatus.currentStreak),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = heroInk,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Prominent High-Contrast Notification Center Bell Button (26dp Bell + Unread Count Badge)
                    val unreadCount = uiState.notificationUnreadCount
                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(46.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = heroInk.copy(alpha = 0.24f),
                            border = BorderStroke(1.5.dp, heroInk.copy(alpha = 0.45f)),
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .size(44.dp)
                                .tactilePress()
                                .clip(CircleShape)
                                .clickable(onClick = onOpenNotificationSheet)
                                .testTag("reminder_bell_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                AnimatedReminderBell(
                                    size = 26.dp,
                                    tint = heroInk,
                                    isActive = unreadCount > 0 || uiState.notificationPrefs.masterEnabled,
                                    contentDescriptionText = stringResource(R.string.reminder_cd)
                                )
                            }
                        }

                        if (unreadCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFE11D48),
                                border = BorderStroke(1.5.dp, Color.White),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .wrapContentWidth()
                                    .testTag("notification_unread_badge")
                            ) {
                                Text(
                                    text = if (unreadCount > 9) "9+" else "$unreadCount",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 9.5.sp
                                    ),
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    // Top-Corner Circular Account Avatar -> Sidebar Drawer (includes Theme Settings)
                    AccountAvatarButton(
                        userProfile = uiState.userProfile,
                        onClick = onOpenAccountDrawer
                    )
                }
            }

            if (isCompactWidth) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    brandRow(Modifier.fillMaxWidth())
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        actionControls()
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    brandRow(Modifier.weight(1f))
                    actionControls()
                }
            }
        }

        // Subtle 3-Day Inactivity Grace Window Warning Banner on Hero Header
        val heroGraceWarning = uiState.streakStatus.graceWarningVi
        if (heroGraceWarning != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF59E0B).copy(alpha = 0.24f),
                border = BorderStroke(1.dp, Color(0xFFFDE68A).copy(alpha = 0.65f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenStreakSheet)
                    .testTag("hero_streak_grace_warning")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WarningAmber,
                        contentDescription = "Cảnh báo giữ chuỗi học",
                        tint = Color(0xFFFEF08A),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = heroGraceWarning,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = heroInk,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Clean 2-Segment Switcher: "Lộ trình 12 Module" | "Dashboard Chăm chỉ (X%)"
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = heroInk.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, heroInk.copy(alpha = 0.28f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tab1_segmented_switcher")
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val halfMinWidth = ((maxWidth - 12.dp) / 2f).coerceAtLeast(136.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val roadmapSelected = selectedTab1View == 0
                    val activePillBg = if (apexColors.isHighContrast) Color.Black else Color.White
                    val activePillFg = if (apexColors.isHighContrast) primary else primary

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (roadmapSelected) activePillBg else Color.Transparent,
                        border = if (roadmapSelected && apexColors.isHighContrast) {
                            BorderStroke(1.5.dp, primary)
                        } else null,
                        modifier = Modifier
                            .widthIn(min = halfMinWidth)
                            .wrapContentWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectTab1View(0) }
                            .testTag("tab1_segment_roadmap")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Map,
                                contentDescription = null,
                                tint = if (roadmapSelected) activePillFg else heroInk,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = stringResource(R.string.tab1_switch_roadmap),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (roadmapSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                                ),
                                color = if (roadmapSelected) activePillFg else heroInk,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    val dashboardSelected = selectedTab1View == 1
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (dashboardSelected) activePillBg else Color.Transparent,
                        border = if (dashboardSelected && apexColors.isHighContrast) {
                            BorderStroke(1.5.dp, primary)
                        } else null,
                        modifier = Modifier
                            .widthIn(min = halfMinWidth)
                            .wrapContentWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectTab1View(1) }
                            .testTag("tab1_segment_dashboard")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Insights,
                                contentDescription = null,
                                tint = if (dashboardSelected) activePillFg else heroInk,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${stringResource(R.string.tab1_switch_dashboard)} ($diligenceScore%)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (dashboardSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                                ),
                                color = if (dashboardSelected) activePillFg else heroInk,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }

        // Decluttered top area: redundant teacher tip banner removed so Smart Resume & 12 Modules surface immediately
    }
}

@Composable
private fun DailyTipSpeechBubble(
    tip: DailyBandTip,
    moduleNumber: Int,
    isSpeaking: Boolean,
    onCycleTip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = ThemePrimary
    Surface(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(
                    topStart = 8.dp,
                    topEnd = 20.dp,
                    bottomEnd = 20.dp,
                    bottomStart = 20.dp
                ),
                ambientColor = ElectricIndigoDark.copy(alpha = 0.20f)
            ),
        shape = RoundedCornerShape(
            topStart = 8.dp,
            topEnd = 20.dp,
            bottomEnd = 20.dp,
            bottomStart = 20.dp
        ),
        color = CardWhite,
        border = BorderStroke(1.dp, CardBorderSoft)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    AnimatedAudioWaveform(
                        isActive = isSpeaking,
                        width = 20.dp,
                        height = 13.dp,
                        primaryColor = primary,
                        secondaryColor = CyanGlow
                    )
                    Text(
                        text = "Mẹo #${String.format(Locale.US, "%02d", moduleNumber)}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = ElectricIndigoLight,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onCycleTip)
                        .testTag("next_tip_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            tint = IndigoOnSurface,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = stringResource(R.string.next_tip_button),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = IndigoOnSurface
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = tip,
                transitionSpec = {
                    fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                },
                label = "daily_tip_content"
            ) { currentTip ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CardSurfaceTinted,
                    border = BorderStroke(1.dp, primary.copy(alpha = 0.22f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "6.0: \"${currentTip.upgradeFromEn}\"",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                textDecoration = TextDecoration.LineThrough
                            ),
                            color = InkMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "8.5: \"${currentTip.academicPhraseEn}\"",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = primary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Action-First "Tiếp tục học ngay" (1-Tap Smart Resume) Hero Card:
 * Dynamically surfaces the learner's most urgent task (SRS words due or active Module KPI)
 * paired with compact Band 6.0 -> 8.5 status and a single high-contrast CTA button.
 */
@Composable
private fun SmartResumeHeroCard(
    currentBand: Float,
    completedCount: Int,
    totalCount: Int,
    activeModule: IeltsModule,
    srsDueCount: Int,
    autoKpi: AutoKpiSnapshot,
    onResumeNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val apexColors = LocalApexColors.current
    val highContrast = apexColors.isHighContrast
    val primary = ThemePrimary
    val onPrimary = ThemeOnPrimary
    val secondary = ThemeSecondary

    var hasEntered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { hasEntered = true }

    val animatedBand by animateFloatAsState(
        targetValue = if (hasEntered) currentBand else 5.5f,
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "smart_resume_band_anim"
    )
    val kpiProgress by animateFloatAsState(
        targetValue = if (hasEntered) autoKpi.overallProgressFraction.coerceIn(0.06f, 1f) else 0.06f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "smart_resume_kpi_progress"
    )

    val urgentTagVi: String
    val urgentTitleVi: String
    val urgentSubtitleVi: String
    val ctaLabelVi: String
    val tagBg: Color
    val tagFg: Color

    when {
        srsDueCount > 0 -> {
            urgentTagVi = "ƯU TIÊN SRS • $srsDueCount TỪ ĐẾN HẠN"
            urgentTitleVi = "Ôn ngay $srsDueCount thẻ SRS trước khi quên"
            urgentSubtitleVi = "KPI hôm nay: ${autoKpi.dailyWordsLearned}/${autoKpi.targetWordsPerDay} từ • ${autoKpi.dailyStudyMinutes}/${autoKpi.targetMinutesPerDay} phút"
            ctaLabelVi = "Tiếp tục học ngay"
            tagBg = GoldMasteryLight
            tagFg = GoldOnContainer
        }
        !autoKpi.isAllDailyKpiMet -> {
            urgentTagVi = "MODULE #${String.format(Locale.US, "%02d", activeModule.id)} • KPI ${autoKpi.overallPercent}%"
            urgentTitleVi = activeModule.titleEn
            urgentSubtitleVi = "${activeModule.titleVi} • ${autoKpi.dailyWordsLearned}/${autoKpi.targetWordsPerDay} từ • ${autoKpi.dailyStudyMinutes}/${autoKpi.targetMinutesPerDay}p"
            ctaLabelVi = "Tiếp tục học ngay"
            tagBg = ElectricIndigoLight
            tagFg = IndigoOnSurface
        }
        else -> {
            urgentTagVi = "✓ ĐÃ ĐẠT 100% KPI HÔM NAY"
            urgentTitleVi = "Module #${String.format(Locale.US, "%02d", activeModule.id)} • ${activeModule.titleEn}"
            urgentSubtitleVi = "Xuất sắc! Học tiếp để bứt phá sớm mốc Band 8.5 ($completedCount/$totalCount Module)"
            ctaLabelVi = "Tiếp tục học ngay"
            tagBg = EmeraldSuccessLight
            tagFg = EmeraldOnContainer
        }
    }

    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("smart_resume_hero_card"),
        shape = RoundedCornerShape(24.dp),
        solidColor = CardWhite,
        solidBorder = BorderStroke(if (highContrast) 1.75.dp else 1.2.dp, primary.copy(alpha = 0.28f)),
        accentTint = primary,
        solidShadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = tagBg
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = tagFg,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = urgentTagVi,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            ),
                            color = tagFg,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Compact Band Status Pill (6.0 -> 8.5)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CardSurfaceTinted,
                    border = BorderStroke(1.dp, CardBorderSoft)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Band ${String.format(Locale.US, "%.1f", animatedBand)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = primary,
                            modifier = Modifier.testTag("animated_band_score")
                        )
                        Text(
                            text = "→ 8.5",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = secondary
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = urgentTitleVi,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = InkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = urgentSubtitleVi,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Sleek KPI Progress Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(SurfaceCanvas)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(kpiProgress)
                        .height(7.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (autoKpi.isAllDailyKpiMet) {
                                Brush.horizontalGradient(listOf(EmeraldSuccess, CyanGlow))
                            } else {
                                Brush.horizontalGradient(listOf(primary, secondary, CyanGlow))
                            }
                        )
                )
            }

            // Single Prominent High-Contrast Solid CTA Button with Tactile Press
            Button(
                onClick = onResumeNow,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primary,
                    contentColor = onPrimary
                ),
                border = if (highContrast) BorderStroke(1.5.dp, onPrimary.copy(alpha = 0.6f)) else null,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .tactilePress()
                    .testTag("start_learning_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = ctaLabelVi,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Compact IELTS 4-Skill Estimated Band & Top-2 Weakness Diagnostic Card:
 * - Shows estimated reference scores ("Band ước lượng tham khảo") for Listening, Reading, Writing, Speaking.
 * - Surfaces top 2 weak question types detected from Chữa đề; tapping launches a targeted 5-minute practice drill.
 */
@Composable
private fun FourSkillDiagnosticCard(
    skills: List<SkillBandProgress>,
    weakQuestionTypes: List<WeakQuestionDiagnostic>,
    onPracticeWeakSkill: (PracticeSkill, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = ThemePrimary
    val apexColors = LocalApexColors.current

    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("four_skill_diagnostic_card"),
        shape = RoundedCornerShape(22.dp),
        solidColor = CardWhite,
        solidBorder = BorderStroke(if (apexColors.isHighContrast) 1.5.dp else 1.dp, CardBorderSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header + explicit "Band ước lượng tham khảo" disclaimer badge
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
                        imageVector = Icons.Rounded.School,
                        contentDescription = null,
                        tint = primary,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = "4 Kỹ năng IELTS",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = InkPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = CardSurfaceTinted,
                    border = BorderStroke(1.dp, CardBorderSoft)
                ) {
                    Text(
                        text = "Band ước lượng tham khảo",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = InkSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // 4-Skill Compact Score Pills (L / R / W / S)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                skills.forEach { skillItem ->
                    val skillAccent = when (skillItem.skill) {
                        PracticeSkill.LISTENING -> CyanGlow
                        PracticeSkill.READING -> PracticeTeal
                        PracticeSkill.WRITING -> primary
                        PracticeSkill.SPEAKING -> StreakOrange
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceCanvas,
                        border = BorderStroke(1.dp, skillAccent.copy(alpha = 0.28f)),
                        modifier = Modifier
                            .weight(1f)
                            .tactilePress(pressedScale = 0.96f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                onPracticeWeakSkill(
                                    skillItem.skill,
                                    "Luyện nhanh 5 phút: Nâng cấp ${skillItem.labelEn} từ ${String.format(Locale.US, "%.1f", skillItem.currentBand)}"
                                )
                            }
                            .testTag("skill_band_pill_${skillItem.skill.name.lowercase()}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1f", skillItem.currentBand),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = skillAccent
                            )
                            Text(
                                text = skillItem.labelEn,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = InkSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Top 2 Weak Question Types from Chữa đề (1-tap -> 5-minute targeted practice)
            if (weakQuestionTypes.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.TrackChanges,
                            contentDescription = null,
                            tint = RoseAlert,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Top 2 dạng bài cần luyện nhanh (từ Chữa đề)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = InkSecondary
                        )
                    }

                    weakQuestionTypes.forEach { diag ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = RoseDiffLight.copy(alpha = 0.42f),
                            border = BorderStroke(1.dp, RoseAlert.copy(alpha = 0.28f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .tactilePress(pressedScale = 0.97f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    onPracticeWeakSkill(diag.skill, diag.quickPracticeFocusVi)
                                }
                                .minimumInteractiveComponentSize()
                                .testTag("weak_diagnostic_${diag.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 11.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(1.dp)
                                ) {
                                    Text(
                                        text = diag.questionTypeVi,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = InkPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = diag.badgeVi,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                        color = RoseAlert,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = PracticeTealLight,
                                    border = BorderStroke(1.dp, PracticeTeal.copy(alpha = 0.35f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Luyện 5p",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                            color = TealOnSurface,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                            contentDescription = null,
                                            tint = TealOnSurface,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
