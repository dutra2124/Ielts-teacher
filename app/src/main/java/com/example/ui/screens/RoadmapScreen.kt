package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
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
import com.example.data.RoadmapTier
import com.example.ui.ApexIeltsUiState
import com.example.ui.components.AnimatedAudioWaveform
import com.example.ui.components.AnimatedReminderBell
import com.example.ui.components.AnimatedStreakFlame
import com.example.ui.components.AnimatedTeacher
import com.example.ui.components.DailyStudyTrackerCard
import com.example.ui.components.SettingsGearButton
import com.example.ui.components.ThemeQuickToggleButton
import com.example.ui.screens.dashboard.ProgressDashboardContent
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardSurfaceTinted
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanGlowLight
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricIndigoDark
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.IndigoOnSurface
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkOnHero
import com.example.ui.theme.InkOnHeroMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LocalApexColors
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.StreakBg
import com.example.ui.theme.StreakOrange
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.VividViolet
import java.util.Locale

private val HeroGradient = Brush.linearGradient(
    colors = listOf(ElectricIndigo, VividViolet, CyanGlow),
    start = Offset(0f, 0f),
    end = Offset(1100f, 950f)
)

@Composable
fun RoadmapScreen(
    uiState: ApexIeltsUiState,
    onSelectModule: (Int) -> Unit,
    onToggleModuleCompletion: (Int) -> Unit,
    onToggleDailyHabit: (String) -> Unit,
    onCycleTip: () -> Unit,
    onTeacherTap: () -> Unit,
    onOpenNotificationSheet: () -> Unit,
    onOpenThemeSheet: () -> Unit = {},
    onOpenSettingsSheet: () -> Unit = {},
    onOpenPersonalLibrary: () -> Unit = {},
    onOpenBulkImporter: () -> Unit = {},
    onToggleAutoGreetVoice: (Boolean) -> Unit = {},
    onPracticeWeakSkill: (PracticeSkill, String) -> Unit = { _, _ -> },
    onOpenLearnTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 0 = Lộ trình 12 Module, 1 = Dashboard Chăm chỉ
    var selectedTab1View by rememberSaveable { mutableIntStateOf(0) }
    val dashboardSnapshot = remember(
        uiState.streakDays,
        uiState.completedHabitIds,
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

    if (selectedTab1View == 1) {
        BackHandler { selectedTab1View = 0 }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceCanvas)
            .testTag("roadmap_lazy_column"),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        // 1. Vibrant Hero Header + Segmented Switcher + Clickable Diligence Summary Banner
        item(key = "hero_and_band_card") {
            Box(modifier = Modifier.fillMaxWidth()) {
                HeroTeacherHeader(
                    uiState = uiState,
                    selectedTab1View = selectedTab1View,
                    diligenceScore = dashboardSnapshot.diligenceScore,
                    onSelectTab1View = { selectedTab1View = it },
                    onCycleTip = onCycleTip,
                    onTeacherTap = onTeacherTap,
                    onOpenNotificationSheet = onOpenNotificationSheet,
                    onOpenThemeSheet = onOpenThemeSheet,
                    onOpenSettingsSheet = onOpenSettingsSheet,
                    onOpenPersonalLibrary = onOpenPersonalLibrary,
                    onOpenBulkImporter = onOpenBulkImporter
                )
            }
        }

        if (selectedTab1View == 1) {
            item(key = "diligence_progress_dashboard") {
                ProgressDashboardContent(
                    snapshot = dashboardSnapshot,
                    streakDays = uiState.streakDays,
                    autoGreetEnabled = uiState.autoGreetDashboardVoice,
                    onToggleAutoGreet = onToggleAutoGreetVoice,
                    onPracticeWeakSkill = onPracticeWeakSkill,
                    modifier = Modifier.offset(y = (-16).dp)
                )
            }
        } else {
            // 2. Band Progress Card (6.0 -> 8.5 with count-up animation)
            item(key = "band_progress_card") {
                BandProgressCard(
                    currentBand = uiState.currentBand,
                    completedCount = uiState.completedModuleIds.size,
                    totalCount = uiState.modules.size,
                    activeModule = uiState.activeModule,
                    onOpenLearnTab = onOpenLearnTab,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .offset(y = (-24).dp)
                )
            }

            // 2b. Daily Study Habit Tracker Card (Circular Ring + Checklist + Streak + 7-Day T2-CN Calendar)
            item(key = "daily_study_tracker_card") {
                DailyStudyTrackerCard(
                    completedHabitIds = uiState.completedHabitIds,
                    streakDays = uiState.streakDays,
                    weeklyActiveDays = uiState.weeklyActiveDays,
                    todayWeekdayId = uiState.todayWeekdayId,
                    notificationPrefs = uiState.notificationPrefs,
                    onToggleHabit = onToggleDailyHabit,
                    onOpenNotificationSheet = onOpenNotificationSheet,
                    dailySelfStudyMinutes = uiState.dailySelfStudyMinutes,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .offset(y = (-10).dp)
                        .padding(bottom = 8.dp)
                )
            }

            // 3. Roadmap Section Heading
            item(key = "roadmap_heading") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = ElectricIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.roadmap_section_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = InkPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.roadmap_section_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )
                }
            }

            // 4. Vertical Winding 3-Tier / 12-Module Roadmap (live from offline SQLite / Google Sheets sync)
            IeltsCurriculum.tierOrder.forEach { tier ->
            val tierModules = uiState.modules.filter { it.tier == tier }
            val completedInTier = tierModules.count { uiState.completedModuleIds.contains(it.id) }

            item(key = "tier_header_${tier.tierNumber}") {
                TierHeaderBanner(
                    tier = tier,
                    completedCount = completedInTier,
                    totalCount = tierModules.size,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            tierModules.forEachIndexed { indexInTier, module ->
                item(key = "module_node_${module.id}") {
                    val isSelected = uiState.activeModuleId == module.id
                    val isCompleted = uiState.completedModuleIds.contains(module.id)
                    val globalIndex = module.id - 1
                    val nodeAnchor = windingAnchorFor(globalIndex)
                    val hasNextInTier = indexInTier < tierModules.lastIndex
                    val nextAnchor = if (hasNextInTier) windingAnchorFor(globalIndex + 1) else nodeAnchor

                    Column(modifier = Modifier.fillMaxWidth()) {
                        WindingModuleRow(
                            module = module,
                            anchorFraction = nodeAnchor,
                            isSelected = isSelected,
                            isCompleted = isCompleted,
                            onSelect = { onSelectModule(module.id) },
                            onToggleCompleted = { onToggleModuleCompletion(module.id) },
                            onOpenLearnTab = onOpenLearnTab,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        if (hasNextInTier) {
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
                                    .padding(horizontal = 16.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }
            }
        }
        }
    }
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
    onOpenNotificationSheet: () -> Unit,
    onOpenThemeSheet: () -> Unit,
    onOpenSettingsSheet: () -> Unit,
    onOpenPersonalLibrary: () -> Unit,
    onOpenBulkImporter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(HeroGradient)
            .drawBehind {
                // Subtle glowing ambient orbs for depth without clutter
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CyanGlow.copy(alpha = 0.34f), Color.Transparent),
                        center = Offset(size.width * 0.90f, size.height * 0.14f),
                        radius = size.width * 0.45f
                    ),
                    radius = size.width * 0.45f,
                    center = Offset(size.width * 0.90f, size.height * 0.14f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(size.width * 0.10f, size.height * 0.72f),
                        radius = size.width * 0.38f
                    ),
                    radius = size.width * 0.38f,
                    center = Offset(size.width * 0.10f, size.height * 0.72f)
                )
            }
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 44.dp)
    ) {
        // Top Brand + Streak + Reminder Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f))
                ) {
                    Box(
                        modifier = Modifier.size(38.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.TrendingUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "ApexIELTS",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = InkOnHero
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyanGlow.copy(alpha = 0.28f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "8.5",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Lộ trình 6.0 → 8.5 cùng AI Mentor",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkOnHeroMuted
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Streak Pill with flickering AnimatedStreakFlame
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                    modifier = Modifier.testTag("streak_badge")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        AnimatedStreakFlame(
                            size = 18.dp,
                            contentDescriptionText = "Chuỗi học liên tiếp"
                        )
                        Text(
                            text = stringResource(R.string.streak_days_label, uiState.streakDays),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                // Global Theme Manager Button (Light / Late-Night Study / CD-IELTS Dark)
                ThemeQuickToggleButton(
                    mode = uiState.themePrefs.mode,
                    onClick = onOpenThemeSheet
                )

                // Settings & Cloud Database Sync Gear Icon Button (with Online/Offline dot)
                SettingsGearButton(
                    isOnline = uiState.isOnline,
                    onClick = onOpenSettingsSheet
                )

                // Reminder Bell Button -> opens Notification Settings Bottom Sheet
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f))
                        .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape)
                        .clickable(onClick = onOpenNotificationSheet)
                        .testTag("reminder_bell_button"),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedReminderBell(
                        size = 23.dp,
                        tint = Color.White,
                        isActive = uiState.notificationPrefs.masterEnabled,
                        contentDescriptionText = stringResource(R.string.reminder_cd)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Prominent Quick-Access Bar in Top Header: "Tự học & Tài liệu" + "Nhập CSV/JSON"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.22f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.36f)),
                modifier = Modifier
                    .weight(1.35f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenPersonalLibrary)
                    .testTag("header_personal_library_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Book,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.library_quick_access_btn),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (uiState.personalMaterials.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.26f)
                        ) {
                            Text(
                                text = "${uiState.personalMaterials.size}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                modifier = Modifier
                    .weight(0.95f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenBulkImporter)
                    .testTag("header_bulk_importer_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.UploadFile,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Nhập CSV/JSON",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Clean Segmented Switcher at top of Tab 1: "Lộ trình 12 Module" | "Dashboard Chăm chỉ"
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.16f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tab1_segmented_switcher")
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val roadmapSelected = selectedTab1View == 0
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (roadmapSelected) Color.White else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectTab1View(0) }
                        .testTag("tab1_segment_roadmap")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Map,
                            contentDescription = null,
                            tint = if (roadmapSelected) ElectricIndigo else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = stringResource(R.string.tab1_switch_roadmap),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (roadmapSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                            ),
                            color = if (roadmapSelected) ElectricIndigo else Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                val dashboardSelected = selectedTab1View == 1
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (dashboardSelected) Color.White else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectTab1View(1) }
                        .testTag("tab1_segment_dashboard")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Insights,
                            contentDescription = null,
                            tint = if (dashboardSelected) ElectricIndigo else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = stringResource(R.string.tab1_switch_dashboard),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (dashboardSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                            ),
                            color = if (dashboardSelected) ElectricIndigo else Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        if (selectedTab1View == 0) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1-Tap Clickable Diligence Summary Banner on Main Header -> opens Dashboard Chăm chỉ
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.20f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.34f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelectTab1View(1) }
                    .testTag("header_diligence_summary_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.24f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.RecordVoiceOver,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Chỉ số chăm chỉ: $diligenceScore% • Chuỗi ${uiState.streakDays} ngày",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Chạm để mở Dashboard & Nghe cô giáo động viên",
                                style = MaterialTheme.typography.labelSmall,
                                color = InkOnHeroMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Mở Dashboard Chăm chỉ",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Animated AI Teacher + Vietnamese Speech Bubble ("Mẹo Band 8.5 hôm nay")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 6.dp)
            ) {
                AnimatedTeacher(
                    mood = uiState.teacherMood,
                    isSpeaking = uiState.isTeacherSpeaking,
                    size = 112.dp,
                    contentDescriptionText = stringResource(R.string.teacher_cd),
                    onClick = onTeacherTap
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.20f)
                ) {
                    Text(
                        text = uiState.teacherMood.labelVi,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            DailyTipSpeechBubble(
                tip = uiState.activeDailyTip,
                moduleNumber = uiState.activeModule.id,
                isSpeaking = uiState.isTeacherSpeaking,
                onCycleTip = onCycleTip,
                modifier = Modifier.weight(1f)
            )
            }
        }
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
    Surface(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(
                    topStart = 8.dp,
                    topEnd = 22.dp,
                    bottomEnd = 22.dp,
                    bottomStart = 22.dp
                ),
                ambientColor = ElectricIndigoDark.copy(alpha = 0.25f),
                spotColor = ElectricIndigoDark.copy(alpha = 0.28f)
            ),
        shape = RoundedCornerShape(
            topStart = 8.dp,
            topEnd = 22.dp,
            bottomEnd = 22.dp,
            bottomStart = 22.dp
        ),
        color = CardWhite
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Speech bubble header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    AnimatedAudioWaveform(
                        isActive = isSpeaking,
                        width = 24.dp,
                        height = 14.dp,
                        primaryColor = ElectricIndigo,
                        secondaryColor = CyanGlow
                    )
                    Text(
                        text = stringResource(R.string.daily_tip_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = IndigoOnSurface,
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
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = stringResource(R.string.next_tip_button),
                            style = MaterialTheme.typography.labelSmall,
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Chủ đề #${String.format(Locale.US, "%02d", moduleNumber)} • ${currentTip.categoryVi}",
                        style = MaterialTheme.typography.labelSmall,
                        color = IndigoOnSurface
                    )
                    Text(
                        text = currentTip.tipVi,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkPrimary
                    )
                    // Bilingual Band 6.0 -> Band 8.5 Upgrade Box
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CardSurfaceTinted,
                        border = BorderStroke(1.dp, IndigoOnSurface.copy(alpha = 0.22f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "6.0: \"${currentTip.upgradeFromEn}\"",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    textDecoration = TextDecoration.LineThrough
                                ),
                                color = InkMuted
                            )
                            Text(
                                text = "8.5: \"${currentTip.academicPhraseEn}\"",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = IndigoOnSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BandProgressCard(
    currentBand: Float,
    completedCount: Int,
    totalCount: Int,
    activeModule: IeltsModule,
    onOpenLearnTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Trigger smooth count-up animation on initial composition and whenever currentBand changes
    var hasEntered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        hasEntered = true
    }

    val animatedBand by animateFloatAsState(
        targetValue = if (hasEntered) currentBand else 5.0f,
        animationSpec = tween(durationMillis = 1050, easing = FastOutSlowInEasing),
        label = "band_count_up"
    )
    val targetFraction = ((currentBand - 6.0f) / 2.5f).coerceIn(0.05f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = if (hasEntered) targetFraction else 0f,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "band_bar_progress"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = ElectricIndigo.copy(alpha = 0.12f),
                spotColor = ElectricIndigo.copy(alpha = 0.18f)
            )
            .testTag("band_progress_card"),
        shape = RoundedCornerShape(24.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, CardBorderSoft)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Row: Title + Animated Band Score (6.0 -> 8.5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = stringResource(R.string.band_progress_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = InkSecondary
                    )
                    Text(
                        text = stringResource(R.string.modules_completed_label, completedCount, totalCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted
                    )
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.current_band_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = InkMuted
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f", animatedBand),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = ElectricIndigo,
                            modifier = Modifier.testTag("animated_band_score")
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = CyanGlow,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .size(18.dp)
                    )
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = stringResource(R.string.target_band_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = VividViolet
                        )
                        Text(
                            text = "8.5",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = VividViolet
                        )
                    }
                }
            }

            // Gradient Progress Track with 6.0 / 7.0 / 8.0 / 8.5 Milestone Ticks
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(SurfaceCanvas)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(ElectricIndigo, VividViolet, CyanGlow)
                                )
                            )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("6.0", "7.0", "8.0", "8.5").forEach { milestone ->
                        val milestoneVal = milestone.toFloat()
                        val reached = currentBand >= milestoneVal
                        Text(
                            text = milestone,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (reached) ElectricIndigo else InkMuted
                        )
                    }
                }
            }

            // Active Module Filter Strip + Jump to Learn CTA
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceCanvas,
                border = BorderStroke(1.dp, CardBorderSoft)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.active_module_prefix),
                            style = MaterialTheme.typography.labelSmall,
                            color = InkMuted
                        )
                        Text(
                            text = "#${String.format(Locale.US, "%02d", activeModule.id)} ${activeModule.titleEn}",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.5.sp),
                            color = InkPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onOpenLearnTab,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricIndigo,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("start_learning_button")
                    ) {
                        Text(
                            text = stringResource(R.string.start_learning_cta),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TierHeaderBanner(
    tier: RoadmapTier,
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val isDark = LocalApexColors.current.isDark
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (isDark) CardSurfaceTinted else tier.containerColor.copy(alpha = 0.62f),
        border = BorderStroke(1.dp, if (isDark) CardBorderSoft else tier.accentPrimary.copy(alpha = 0.22f))
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
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(listOf(tier.accentPrimary, tier.accentSecondary))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "T${tier.tierNumber}",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color.White
                    )
                }
                Column {
                    Text(
                        text = "Tier ${tier.tierNumber} • ${tier.titleEn}",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                        color = InkPrimary
                    )
                    Text(
                        text = tier.subtitleVi,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CardWhite
                ) {
                    Text(
                        text = tier.bandRange,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDark) IndigoOnSurface else tier.accentPrimary,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "$completedCount/$totalCount chủ đề",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = InkMuted
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WindingModuleRow(
    module: IeltsModule,
    anchorFraction: Float,
    isSelected: Boolean,
    isCompleted: Boolean,
    onSelect: () -> Unit,
    onToggleCompleted: () -> Unit,
    onOpenLearnTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nodeOnLeft = anchorFraction < 0.5f

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (nodeOnLeft) {
            WindingNodeEmblem(
                module = module,
                isSelected = isSelected,
                isCompleted = isCompleted,
                onClick = onSelect
            )
            ModuleRoadmapCard(
                module = module,
                isSelected = isSelected,
                isCompleted = isCompleted,
                onSelect = onSelect,
                onToggleCompleted = onToggleCompleted,
                onOpenLearnTab = onOpenLearnTab,
                modifier = Modifier.weight(1f)
            )
        } else {
            ModuleRoadmapCard(
                module = module,
                isSelected = isSelected,
                isCompleted = isCompleted,
                onSelect = onSelect,
                onToggleCompleted = onToggleCompleted,
                onOpenLearnTab = onOpenLearnTab,
                modifier = Modifier.weight(1f)
            )
            WindingNodeEmblem(
                module = module,
                isSelected = isSelected,
                isCompleted = isCompleted,
                onClick = onSelect
            )
        }
    }
}

@Composable
private fun WindingNodeEmblem(
    module: IeltsModule,
    isSelected: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    val pulseTransition = rememberInfiniteTransition(label = "node_pulse_${module.id}")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_ring"
    )

    val nodeGradient = when {
        isCompleted -> Brush.linearGradient(listOf(EmeraldSuccess, CyanGlow))
        isSelected -> Brush.linearGradient(listOf(module.tier.accentPrimary, module.tier.accentSecondary))
        else -> Brush.linearGradient(listOf(CardWhite, SurfaceCanvas))
    }

    Box(
        modifier = Modifier
            .size(66.dp)
            .clickable(onClick = onClick)
            .testTag("roadmap_node_${module.id}"),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size((58f * pulseScale).dp)
                    .clip(CircleShape)
                    .background(module.tier.accentPrimary.copy(alpha = 0.16f))
            )
        }

        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(
                    elevation = if (isSelected || isCompleted) 10.dp else 4.dp,
                    shape = CircleShape,
                    ambientColor = module.tier.accentPrimary.copy(alpha = 0.28f),
                    spotColor = module.tier.accentPrimary.copy(alpha = 0.34f)
                )
                .clip(CircleShape)
                .background(nodeGradient)
                .border(
                    width = if (isSelected) 2.5.dp else 1.5.dp,
                    color = when {
                        isCompleted -> EmeraldSuccess
                        isSelected -> Color.White
                        else -> module.tier.accentPrimary.copy(alpha = 0.35f)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Rounded.Check else module.icon,
                contentDescription = module.titleEn,
                tint = if (isSelected || isCompleted) Color.White else module.tier.accentPrimary,
                modifier = Modifier.size(26.dp)
            )
        }

        // Crisp module index badge pill at bottom-right of circle
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-2).dp, y = (-2).dp),
            shape = CircleShape,
            color = if (isSelected) ElectricIndigoDark else CardWhite,
            border = BorderStroke(1.dp, module.tier.accentPrimary.copy(alpha = 0.35f))
        ) {
            Text(
                text = String.format(Locale.US, "%02d", module.id),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                color = if (isSelected) Color.White else module.tier.accentPrimary,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModuleRoadmapCard(
    module: IeltsModule,
    isSelected: Boolean,
    isCompleted: Boolean,
    onSelect: () -> Unit,
    onToggleCompleted: () -> Unit,
    onOpenLearnTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isSelected -> module.tier.accentPrimary
        isCompleted -> EmeraldSuccess.copy(alpha = 0.45f)
        else -> CardBorderSoft
    }

    Surface(
        modifier = modifier
            .shadow(
                elevation = if (isSelected) 12.dp else 4.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = module.tier.accentPrimary.copy(alpha = if (isSelected) 0.18f else 0.06f),
                spotColor = module.tier.accentPrimary.copy(alpha = if (isSelected) 0.24f else 0.10f)
            )
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onSelect)
            .testTag("roadmap_module_${module.id}"),
        shape = RoundedCornerShape(24.dp),
        color = CardWhite,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Top micro badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isSelected) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = module.tier.accentPrimary
                        ) {
                            Text(
                                text = stringResource(R.string.module_active_badge),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    if (isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = EmeraldSuccessLight
                        ) {
                            Text(
                                text = stringResource(R.string.module_completed_badge),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = EmeraldSuccess,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    if (!isSelected && !isCompleted) {
                        Text(
                            text = "Band ${module.tier.bandRange}",
                            style = MaterialTheme.typography.labelSmall,
                            color = module.tier.accentPrimary
                        )
                    }
                }

                Icon(
                    imageVector = if (isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = if (isCompleted) "Đã hoàn thành" else "Chưa hoàn thành",
                    tint = if (isCompleted) EmeraldSuccess else InkMuted.copy(alpha = 0.55f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Authentic English IELTS Domain Title
            Text(
                text = module.titleEn,
                style = MaterialTheme.typography.titleMedium,
                color = InkPrimary
            )

            // Clear Vietnamese Subtitle
            Text(
                text = module.titleVi,
                style = MaterialTheme.typography.bodySmall,
                color = InkSecondary
            )

            // Expandable Band 8.5 preview + quick actions when active
            AnimatedVisibility(
                visible = isSelected,
                enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(tween(200)),
                exit = shrinkVertically(tween(180)) + fadeOut(tween(150))
            ) {
                Column(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = module.focusVi,
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Normal),
                        color = InkMuted
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            text = stringResource(R.string.sample_vocab_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = module.tier.accentPrimary
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            module.keyCollocations.forEach { collocation ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = module.tier.containerColor.copy(alpha = 0.7f)
                                ) {
                                    Text(
                                        text = collocation,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PlusJakartaSansFontFamily
                                        ),
                                        color = module.tier.accentPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onToggleCompleted,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isCompleted) EmeraldSuccess else module.tier.accentPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("complete_module_${module.id}")
                        ) {
                            Text(
                                text = if (isCompleted) {
                                    stringResource(R.string.mark_in_progress_action)
                                } else {
                                    stringResource(R.string.mark_completed_action)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCompleted) EmeraldSuccess else module.tier.accentPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = onOpenLearnTab,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = module.tier.accentPrimary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("open_learn_module_${module.id}")
                        ) {
                            Text(
                                text = "Học",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WindingPathConnector(
    startFraction: Float,
    endFraction: Float,
    accentColor: Color,
    secondaryColor: Color,
    isLit: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val startX = if (startFraction < 0.5f) 33.dp.toPx() else w - 33.dp.toPx()
        val endX = if (endFraction < 0.5f) 33.dp.toPx() else w - 33.dp.toPx()

        val curvePath = Path().apply {
            moveTo(startX, 0f)
            cubicTo(
                startX, h * 0.58f,
                endX, h * 0.42f,
                endX, h
            )
        }

        if (isLit) {
            drawPath(
                path = curvePath,
                brush = Brush.verticalGradient(listOf(accentColor, secondaryColor)),
                style = Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        } else {
            drawPath(
                path = curvePath,
                color = accentColor.copy(alpha = 0.28f),
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                )
            )
        }
    }
}
