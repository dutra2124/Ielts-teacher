package com.example.ui.screens.review

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ExamBankCurriculum
import com.example.data.PracticeSkill
import com.example.data.ReviewErrorLogItem
import com.example.data.ReviewReportFactory
import com.example.speech.ApexTtsController
import com.example.ui.ApexIeltsUiState
import com.example.ui.components.AnimatedStreakFlame
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardWhite
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.IndigoOnSurface
import com.example.ui.theme.InkOnHero
import com.example.ui.theme.InkOnHeroMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LocalAdaptiveLayoutInfo
import com.example.ui.theme.LocalApexColors
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.RoseDiff
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.VividViolet

private val ReviewHeroGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFBE123C), VividViolet, ElectricIndigo),
    start = Offset(0f, 0f),
    end = Offset(1050f, 700f)
)

/**
 * Embedded Exam Correction & Deep Diagnostic Toolkit ("Đề thi -> Chữa đề").
 * Renders cleanly inside a parent vertically scrollable Column (no nested verticalScroll).
 */
@Composable
fun ReviewCorrectionContent(
    uiState: ApexIeltsUiState,
    onTeacherTap: () -> Unit,
    onGenerateAiDiff: (skillLabel: String, promptEn: String, originalText: String) -> Unit,
    onResetAiDiff: () -> Unit,
    onGenerateDeepAnalysis: (ReviewErrorLogItem) -> Unit,
    onPushWordToSrs: (wordId: String) -> Unit,
    onPracticeWeakSkill: (skill: PracticeSkill, focusNoteVi: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val layoutInfo = LocalAdaptiveLayoutInfo.current
    val ttsController = remember(context) { ApexTtsController(context) { } }
    DisposableEffect(ttsController) {
        onDispose { ttsController.shutdown() }
    }

    val latestAttempt = uiState.examHistory.firstOrNull()
    val allExams = remember(uiState.authenticExams, uiState.customExams) {
        uiState.authenticExams.ifEmpty { ExamBankCurriculum.authenticExams } + uiState.customExams
    }

    // Default to real submitted attempt if available; otherwise show rich pre-loaded sample report immediately
    var preferSampleReport by rememberSaveable(latestAttempt?.id) {
        mutableStateOf(latestAttempt == null)
    }

    val activeReport = remember(latestAttempt, allExams, preferSampleReport) {
        if (!preferSampleReport && latestAttempt != null) {
            ReviewReportFactory.fromAttempt(latestAttempt, allExams)
        } else {
            ReviewReportFactory.sampleDiagnosticReport()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("review_correction_content"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Compact Source Switcher (Real Submitted Attempt vs Pre-loaded Sample Report)
        if (latestAttempt != null) {
            Surface(
                shape = RoundedCornerShape(50),
                color = CardWhite,
                border = BorderStroke(1.dp, CardBorderSoft),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = layoutInfo.horizontalPadding)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        false to "Bài vừa nộp (${uiState.examHistory.size})",
                        true to "Báo cáo mẫu 7.0→8.5"
                    ).forEach { (isSampleOption, label) ->
                        val selected = preferSampleReport == isSampleOption
                        val bg by animateColorAsState(
                            if (selected) ElectricIndigo else Color.Transparent,
                            tween(180),
                            label = "embedded_review_src_bg_$isSampleOption"
                        )
                        val fg by animateColorAsState(
                            if (selected) Color.White else InkSecondary,
                            tween(180),
                            label = "embedded_review_src_fg_$isSampleOption"
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(bg)
                                .clickable {
                                    preferSampleReport = isSampleOption
                                    onResetAiDiff()
                                }
                                .padding(horizontal = 8.dp, vertical = 7.dp)
                                .testTag("review_source_tab_$isSampleOption"),
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
        } else {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ElectricIndigoLight.copy(alpha = 0.65f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = layoutInfo.horizontalPadding)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Icon(Icons.Rounded.Insights, contentDescription = null, tint = IndigoOnSurface, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Báo cáo mẫu 7.0→8.5 • Nộp bài thi để chấm thật",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = IndigoOnSurface,
                        maxLines = 1
                    )
                }
            }
        }

        // 1. Diagnostic Band Report + Animated AI Teacher + Question Type Accuracy Bars
        ReviewDiagnosticCard(
            report = activeReport,
            isTeacherSpeaking = uiState.isTeacherSpeaking,
            onTeacherTap = onTeacherTap,
            onPracticeWeakSkill = onPracticeWeakSkill
        )

        // 2. Original vs. Band 8.5+ Upgrade Diff View
        ReviewDiffViewCard(
            writingDiff = activeReport.writingDiff,
            speakingDiff = activeReport.speakingDiff,
            aiDiffState = uiState.reviewDiffAiState,
            ttsController = ttsController,
            onGenerateAiDiff = onGenerateAiDiff,
            onResetAiDiff = onResetAiDiff
        )

        // 3. Upgraded Error Log & Expandable 3-Section Deep Question Analysis Cards
        ReviewErrorLogSection(
            errors = activeReport.errorLog,
            srsWordIds = uiState.srsWordIds,
            deepQuestionAiResults = uiState.deepQuestionAiResults,
            loadingQuestionAnalysisIds = uiState.loadingQuestionAnalysisIds,
            questionAnalysisErrors = uiState.questionAnalysisErrors,
            onGenerateDeepAnalysis = onGenerateDeepAnalysis,
            onPushWordToSrs = onPushWordToSrs,
            onPracticeWeakSkill = onPracticeWeakSkill
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Standalone wrapper preserved for compatibility if navigated directly.
 */
@Composable
fun ReviewScreen(
    uiState: ApexIeltsUiState,
    onTeacherTap: () -> Unit,
    onGenerateAiDiff: (skillLabel: String, promptEn: String, originalText: String) -> Unit,
    onResetAiDiff: () -> Unit,
    onGenerateDeepAnalysis: (ReviewErrorLogItem) -> Unit = {},
    onPushWordToSrs: (wordId: String) -> Unit,
    onPracticeWeakSkill: (skill: PracticeSkill, focusNoteVi: String) -> Unit,
    onReturnToRoadmap: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onReturnToRoadmap() }
    val layoutInfo = LocalAdaptiveLayoutInfo.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceCanvas)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .testTag("review_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                .background(ReviewHeroGradient)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                            center = Offset(size.width * 0.88f, size.height * 0.20f),
                            radius = size.width * 0.42f
                        ),
                        radius = size.width * 0.42f,
                        center = Offset(size.width * 0.88f, size.height * 0.20f)
                    )
                }
                .statusBarsPadding()
                .displayCutoutPadding()
                .padding(horizontal = layoutInfo.horizontalPadding, vertical = 14.dp),
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
                        color = Color.White.copy(alpha = 0.20f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f)),
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Rounded.AutoFixHigh, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Text(
                                text = "Chữa đề & Nâng Band 8.5",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.review_header_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = InkOnHero
                    )
                    Text(
                        text = stringResource(R.string.review_header_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkOnHeroMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedStreakFlame(size = 34.dp)
                }
            }
        }

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
}
