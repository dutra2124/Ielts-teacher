package com.example.ui.screens.exam

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assignment
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ExamAttemptRecord
import com.example.data.ExamBankCurriculum
import com.example.data.ExamDifficulty
import com.example.data.ExamPaper
import com.example.data.ExamSkillMode
import com.example.ui.ApexIeltsUiState
import com.example.ui.components.SettingsGearButton
import com.example.ui.components.ThemeQuickToggleButton
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
import com.example.ui.theme.LocalApexColors
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseDiffLight
import com.example.ui.theme.SurfaceCanvas
import java.util.Locale

private val ExamSlateHeroGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF0F172A), CdIeltsTopbar, Color(0xFF334155)),
    start = Offset(0f, 0f),
    end = Offset(1050f, 700f)
)

/**
 * Tab 4: THI (Exam Hub) with 2 clear sections:
 * 1. Kho đề Thi Thực tế (Authentic Exam Bank — Cambridge & Actual Tests)
 * 2. Nạp Bộ đề Có sẵn (CustomExamImporter & Personal Exam Library)
 */
@Composable
fun ExamHubScreen(
    uiState: ApexIeltsUiState,
    onStartExam: (ExamPaper, ExamSkillMode) -> Unit,
    onParseCustomExam: (rawText: String, pdfBytes: ByteArray?, fileName: String?, audioUri: String?, audioName: String?) -> Unit,
    onSaveCustomExam: (ExamPaper) -> Unit,
    onDeleteCustomExam: (String) -> Unit,
    onResetExamParser: () -> Unit,
    onOpenThemeSheet: () -> Unit = {},
    onOpenSettingsSheet: () -> Unit = {},
    onShowQuickToast: (String) -> Unit,
    onReturnToRoadmap: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onReturnToRoadmap() }

    var selectedAreaIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedModeId by rememberSaveable { mutableStateOf(ExamSkillMode.FULL_TEST.id) }
    val selectedMode = ExamSkillMode.fromId(selectedModeId)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceCanvas)
            .verticalScroll(rememberScrollState())
            .testTag("exam_hub_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Dark Slate CD-IELTS Hub Hero Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                .background(ExamSlateHeroGradient)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
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
                            Icon(Icons.Rounded.Shield, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(13.dp))
                            Text(
                                text = "CD-IELTS Strict Mode • IDP / British Council",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.exam_hub_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = InkOnHero
                    )
                    Text(
                        text = stringResource(R.string.exam_hub_subtitle),
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
                        color = Color.White.copy(alpha = 0.12f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${uiState.examHistory.size}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Lượt thi",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = InkOnHeroMuted
                            )
                        }
                    }
                }
            }

            // 2-Area Segmented Selector: Khu vực 1 (Kho đề Thực tế) | Khu vực 2 (Nạp đề & Thư viện)
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isDark = LocalApexColors.current.isDark
                    val tabs = listOf(
                        stringResource(R.string.exam_section_bank),
                        "${stringResource(R.string.exam_section_importer)} (${uiState.customExams.size})"
                    )
                    tabs.forEachIndexed { idx, label ->
                        val selected = selectedAreaIndex == idx
                        val bg by animateColorAsState(
                            if (selected) CardWhite else Color.Transparent,
                            tween(180),
                            label = "exam_area_bg_$idx"
                        )
                        val fg by animateColorAsState(
                            if (selected) (if (isDark) InkPrimary else CdIeltsTopbar) else Color.White,
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
                                color = fg
                            )
                        }
                    }
                }
            }
        }

        // Shared Exam Mode Selector (Full Test 4 skills OR Single Skill strict timing)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
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
                                color = if (active) Color.White else InkPrimary
                            )
                            Text(
                                text = mode.badgeVi,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (active) Color(0xFFCBD5E1) else InkMuted
                            )
                        }
                    }
                }
            }
        }

        AnimatedContent(
            targetState = selectedAreaIndex,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
            label = "exam_hub_area_content"
        ) { areaIdx ->
            if (areaIdx == 0) {
                // Khu vực 1: Authentic Exam Bank
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    uiState.authenticExams.forEach { exam ->
                        AuthenticExamCard(
                            exam = exam,
                            selectedMode = selectedMode,
                            historyForExam = uiState.examHistory.filter { it.examId == exam.id },
                            onStartExam = { onStartExam(exam, selectedMode) }
                        )
                    }
                }
            } else {
                // Khu vực 2: Custom Exam Importer & Personal Library
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

        Spacer(modifier = Modifier.height(22.dp))
    }
}

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
            // Year + Difficulty + Source tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(8.dp), color = CdIeltsTopbar) {
                    Text(
                        text = "${exam.year}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Surface(shape = RoundedCornerShape(8.dp), color = diffBg) {
                    Text(
                        text = exam.difficulty.labelVi,
                        style = MaterialTheme.typography.labelSmall,
                        color = diffText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = exam.sourceLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = InkMuted
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

            // Strict Timing Notice + Start CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = GoldMastery, modifier = Modifier.size(14.dp))
                    Text(
                        text = "${selectedMode.labelVi} • ${selectedMode.durationMinutes} phút",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkSecondary
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
                        .minimumInteractiveComponentSize()
                        .testTag("start_exam_button_${exam.id}")
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = stringResource(R.string.exam_start_cta),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
