package com.example.ui.screens.exam

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ExamAttemptRecord
import com.example.data.ExamDifficulty
import com.example.data.ExamHubSubTab
import com.example.data.ExamPaper
import com.example.data.ExamSkillMode
import com.example.data.PracticeSkill
import com.example.data.ReviewErrorLogItem
import com.example.ui.ApexIeltsUiState
import com.example.ui.components.SettingsGearButton
import com.example.ui.components.ThemeQuickToggleButton
import com.example.ui.screens.review.ReviewCorrectionContent
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CdIeltsTopbar
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
import com.example.ui.theme.InkOnHero
import com.example.ui.theme.InkOnHeroMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LocalAdaptiveLayoutInfo
import com.example.ui.theme.LocalApexColors
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseDiffLight
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.VividViolet
import java.util.Locale

private val ExamSlateHeroGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF0F172A), CdIeltsTopbar, Color(0xFF334155)),
    start = Offset(0f, 0f),
    end = Offset(1050f, 700f)
)

private val ExamCorrectionHeroGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF312E81), VividViolet, ElectricIndigo),
    start = Offset(0f, 0f),
    end = Offset(1050f, 700f)
)

/**
 * Tab 4: Đề thi (Merged Exam & Review Hub) with a clear top segmented switcher:
 * 1. Thi mô phỏng (Authentic Exam Bank + Strict CD-IELTS Mock Exam launcher + Custom Exam Importer)
 * 2. Chữa đề (Upgraded Band Report + Band 8.5+ Diff + Expandable 3-Section Deep Question Analysis + Gemini AI)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExamHubScreen(
    uiState: ApexIeltsUiState,
    onSelectExamSubTab: (ExamHubSubTab) -> Unit,
    onStartExam: (ExamPaper, ExamSkillMode) -> Unit,
    onParseCustomExam: (rawText: String, pdfBytes: ByteArray?, fileName: String?, audioUri: String?, audioName: String?) -> Unit,
    onSaveCustomExam: (ExamPaper) -> Unit,
    onDeleteCustomExam: (String) -> Unit,
    onResetExamParser: () -> Unit,
    onTeacherTap: () -> Unit,
    onGenerateAiDiff: (skillLabel: String, promptEn: String, originalText: String) -> Unit,
    onResetAiDiff: () -> Unit,
    onGenerateDeepAnalysis: (ReviewErrorLogItem) -> Unit,
    onPushWordToSrs: (wordId: String) -> Unit,
    onPracticeWeakSkill: (skill: PracticeSkill, focusNoteVi: String) -> Unit,
    onOpenThemeSheet: () -> Unit = {},
    onOpenSettingsSheet: () -> Unit = {},
    onShowQuickToast: (String) -> Unit,
    onReturnToRoadmap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeSubTab = uiState.activeExamSubTab
    BackHandler {
        if (activeSubTab == ExamHubSubTab.CORRECTION) {
            onSelectExamSubTab(ExamHubSubTab.SIMULATION)
        } else {
            onReturnToRoadmap()
        }
    }

    val layoutInfo = LocalAdaptiveLayoutInfo.current
    val hPad = layoutInfo.horizontalPadding

    var selectedAreaIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedModeId by rememberSaveable { mutableStateOf(ExamSkillMode.FULL_TEST.id) }
    val selectedMode = ExamSkillMode.fromId(selectedModeId)

    if (activeSubTab == ExamHubSubTab.CORRECTION) {
        // Sub-tab 2: Chữa đề (Clean vertically scrollable surface — zero nested scroll)
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(SurfaceCanvas)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .testTag("exam_hub_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ExamHubHeroHeader(
                uiState = uiState,
                activeSubTab = activeSubTab,
                hPad = hPad,
                onSelectExamSubTab = onSelectExamSubTab,
                onOpenThemeSheet = onOpenThemeSheet,
                onOpenSettingsSheet = onOpenSettingsSheet
            )

            ReviewCorrectionContent(
                uiState = uiState,
                onTeacherTap = onTeacherTap,
                onGenerateAiDiff = onGenerateAiDiff,
                onResetAiDiff = onResetAiDiff,
                onGenerateDeepAnalysis = onGenerateDeepAnalysis,
                onPushWordToSrs = onPushWordToSrs,
                onPracticeWeakSkill = onPracticeWeakSkill
            )
        }
    } else {
        // Sub-tab 1: Thi mô phỏng (100% intact Authentic Exam Bank + Strict CD-IELTS + Custom Importer)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = layoutInfo.adaptiveCardMinWidth),
            modifier = modifier
                .fillMaxSize()
                .background(SurfaceCanvas)
                .imePadding()
                .testTag("exam_hub_screen"),
            contentPadding = PaddingValues(bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Shared Hero Header with primary segmented switcher: Thi mô phỏng | Chữa đề
            item(key = "exam_hub_header", span = { GridItemSpan(maxLineSpan) }) {
                ExamHubHeroHeader(
                    uiState = uiState,
                    activeSubTab = activeSubTab,
                    hPad = hPad,
                    onSelectExamSubTab = onSelectExamSubTab,
                    onOpenThemeSheet = onOpenThemeSheet,
                    onOpenSettingsSheet = onOpenSettingsSheet
                )
            }

            // Secondary Bank vs Custom Importer Switcher inside Thi mô phỏng
            item(key = "exam_area_switcher", span = { GridItemSpan(maxLineSpan) }) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CardWhite,
                    border = BorderStroke(1.dp, CardBorderSoft),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = hPad)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val tabs = listOf(
                            stringResource(R.string.exam_section_bank),
                            "${stringResource(R.string.exam_section_importer)} (${uiState.customExams.size})"
                        )
                        tabs.forEachIndexed { idx, label ->
                            val selected = selectedAreaIndex == idx
                            val bg by animateColorAsState(
                                if (selected) CdIeltsTopbar else Color.Transparent,
                                tween(180),
                                label = "exam_area_bg_$idx"
                            )
                            val fg by animateColorAsState(
                                if (selected) Color.White else InkSecondary,
                                tween(180),
                                label = "exam_area_fg_$idx"
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .background(bg)
                                    .clickable { selectedAreaIndex = idx }
                                    .padding(vertical = 9.dp)
                                    .testTag("exam_area_tab_$idx"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = fg,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            // Shared Exam Mode Selector (Full Test 4 skills OR Single Skill strict timing)
            item(key = "exam_mode_selector", span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = hPad),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.Schedule, contentDescription = null, tint = CdIeltsTopbar, modifier = Modifier.size(17.dp))
                        Text(
                            text = "Chọn chế độ thi & thời gian chuẩn thực tế:",
                            style = MaterialTheme.typography.titleSmall,
                            color = InkPrimary
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExamSkillMode.entries.forEach { mode ->
                            val active = mode == selectedMode
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (active) CdIeltsTopbar else CardWhite,
                                border = BorderStroke(1.dp, if (active) CdIeltsTopbar else CardBorderSoft),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedModeId = mode.id }
                                    .testTag("exam_mode_chip_${mode.id}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = mode.labelVi,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (active) Color.White else InkPrimary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Text(
                                        text = mode.badgeVi,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = if (active) Color(0xFFCBD5E1) else InkMuted,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (selectedAreaIndex == 0) {
                // Khu vực 1: Authentic Exam Bank — adaptive GridCells.Adaptive(300.dp) cards
                items(uiState.authenticExams, key = { it.id }) { exam ->
                    Box(modifier = Modifier.padding(horizontal = hPad)) {
                        AuthenticExamCard(
                            exam = exam,
                            selectedMode = selectedMode,
                            historyForExam = uiState.examHistory.filter { it.examId == exam.id },
                            onStartExam = { onStartExam(exam, selectedMode) }
                        )
                    }
                }
            } else {
                // Khu vực 2: Custom Exam Importer & Personal Library (full-width span)
                item(key = "custom_exam_importer", span = { GridItemSpan(maxLineSpan) }) {
                    CustomExamImporterSection(
                        parserState = uiState.examParserState,
                        customExams = uiState.customExams,
                        examHistory = uiState.examHistory,
                        selectedMode = selectedMode,
                        onParseWithAi = onParseCustomExam,
                        onSaveCustomExam = onSaveCustomExam,
                        onDeleteCustomExam = onDeleteCustomExam,
                        onStartCustomExam = onStartExam,
                        onResetParser = onResetExamParser,
                        onShowToast = onShowQuickToast
                    )
                }
            }
        }
    }
}

@Composable
private fun ExamHubHeroHeader(
    uiState: ApexIeltsUiState,
    activeSubTab: ExamHubSubTab,
    hPad: Dp,
    onSelectExamSubTab: (ExamHubSubTab) -> Unit,
    onOpenThemeSheet: () -> Unit,
    onOpenSettingsSheet: () -> Unit
) {
    val isCorrection = activeSubTab == ExamHubSubTab.CORRECTION
    val heroBrush = if (isCorrection) ExamCorrectionHeroGradient else ExamSlateHeroGradient

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
            .background(heroBrush)
            .statusBarsPadding()
            .displayCutoutPadding()
            .padding(horizontal = hPad, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isCorrection) Icons.Rounded.AutoFixHigh else Icons.Rounded.Shield,
                            contentDescription = null,
                            tint = if (isCorrection) Color(0xFFFDE047) else Color(0xFF38BDF8),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (isCorrection) {
                                "Chữa đề chuyên sâu • AI Examiner 8.5+"
                            } else {
                                "CD-IELTS Strict Mode • IDP / British Council"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }

                Text(
                    text = if (isCorrection) {
                        stringResource(R.string.review_header_title)
                    } else {
                        stringResource(R.string.exam_hub_title)
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = InkOnHero
                )
                Text(
                    text = if (isCorrection) {
                        stringResource(R.string.review_header_subtitle)
                    } else {
                        stringResource(R.string.exam_hub_subtitle)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = InkOnHeroMuted
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeQuickToggleButton(
                    mode = uiState.themePrefs.mode,
                    onClick = onOpenThemeSheet
                )
                SettingsGearButton(
                    isOnline = uiState.isOnline,
                    onClick = onOpenSettingsSheet
                )
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${uiState.examHistory.size}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = "Lượt thi",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = InkOnHeroMuted,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        // Primary Top Segmented Switcher: Thi mô phỏng | Chữa đề
        Surface(
            shape = RoundedCornerShape(50),
            color = Color.White.copy(alpha = 0.16f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.26f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("exam_hub_subtab_switcher")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isDark = LocalApexColors.current.isDark
                ExamHubSubTab.entries.forEach { subTab ->
                    val selected = activeSubTab == subTab
                    val bg by animateColorAsState(
                        if (selected) CardWhite else Color.Transparent,
                        tween(180),
                        label = "exam_subtab_bg_${subTab.name}"
                    )
                    val selectedFg = if (isDark) InkPrimary else {
                        if (subTab == ExamHubSubTab.CORRECTION) ElectricIndigo else CdIeltsTopbar
                    }
                    val fg by animateColorAsState(
                        if (selected) selectedFg else Color.White,
                        tween(180),
                        label = "exam_subtab_fg_${subTab.name}"
                    )
                    val icon = if (subTab == ExamHubSubTab.SIMULATION) {
                        Icons.Rounded.Assignment
                    } else {
                        Icons.Rounded.AutoFixHigh
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(bg)
                            .clickable { onSelectExamSubTab(subTab) }
                            .padding(vertical = 10.dp, horizontal = 8.dp)
                            .testTag("exam_subtab_${subTab.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = fg,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = subTab.labelVi,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                                ),
                                color = fg,
                                maxLines = 1,
                                softWrap = false
                            )
                            if (subTab == ExamHubSubTab.CORRECTION && uiState.examHistory.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (selected) EmeraldSuccessLight else EmeraldSuccess
                                ) {
                                    Text(
                                        text = "${uiState.examHistory.size}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = if (selected) EmeraldOnContainer else Color.White,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AuthenticExamCard(
    exam: ExamPaper,
    selectedMode: ExamSkillMode,
    historyForExam: List<ExamAttemptRecord>,
    onStartExam: () -> Unit
) {
    val latestAttempt = historyForExam.firstOrNull()
    val diffBg = when (exam.difficulty) {
        ExamDifficulty.MODERATE_HARD -> ElectricIndigoLight
        ExamDifficulty.HARD -> GoldMasteryLight
        ExamDifficulty.VERY_HARD -> RoseDiffLight
    }
    val diffText = when (exam.difficulty) {
        ExamDifficulty.MODERATE_HARD -> IndigoOnSurface
        ExamDifficulty.HARD -> GoldOnContainer
        ExamDifficulty.VERY_HARD -> RoseAlert
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, CardBorderSoft),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = CdIeltsTopbar.copy(alpha = 0.08f),
                spotColor = CdIeltsTopbar.copy(alpha = 0.10f)
            )
            .testTag("authentic_exam_card_${exam.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Year + Difficulty + Source tags (FlowRow prevents mid-word wrapping under large font scales)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(shape = RoundedCornerShape(8.dp), color = CdIeltsTopbar, modifier = Modifier.wrapContentWidth()) {
                    Text(
                        text = "${exam.year}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Surface(shape = RoundedCornerShape(8.dp), color = diffBg, modifier = Modifier.wrapContentWidth()) {
                    Text(
                        text = exam.difficulty.labelVi,
                        style = MaterialTheme.typography.labelSmall,
                        color = diffText,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = exam.sourceLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = InkMuted,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.Assignment, contentDescription = null, tint = IndigoOnSurface, modifier = Modifier.size(20.dp))
                Text(
                    text = exam.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = InkPrimary
                )
            }

            Text(
                text = "Chủ đề: ${exam.topicVi} (${exam.topicEn})",
                style = MaterialTheme.typography.bodySmall,
                color = InkSecondary
            )

            // Score History Strip
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (latestAttempt != null) EmeraldSuccessLight else SurfaceCanvas
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 11.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            tint = if (latestAttempt != null) EmeraldOnContainer else InkMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (latestAttempt != null) {
                                "Lịch sử: Band ${String.format(Locale.US, "%.1f", latestAttempt.estimatedBand)} (${latestAttempt.correctCount}/${latestAttempt.totalObjectiveQuestions} đúng) • ${historyForExam.size} lượt"
                            } else {
                                "Lịch sử điểm: Chưa thi đề này"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (latestAttempt != null) EmeraldOnContainer else InkMuted
                        )
                    }
                }
            }

            // Strict Timing Notice + Start CTA (FlowRow prevents wrapping clash on narrow/large-font displays)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = GoldMastery, modifier = Modifier.size(14.dp))
                    Text(
                        text = "${selectedMode.labelVi} • ${selectedMode.durationMinutes} phút",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Button(
                    onClick = onStartExam,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CdIeltsTopbar,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    modifier = Modifier
                        .wrapContentWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("start_exam_button_${exam.id}")
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = stringResource(R.string.exam_start_cta),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
