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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
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
import com.example.data.ReviewReportFactory
import com.example.speech.ApexTtsController
import com.example.ui.ApexIeltsUiState
import com.example.ui.components.AnimatedStreakFlame
import com.example.ui.theme.CardWhite
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.InkOnHero
import com.example.ui.theme.InkOnHeroMuted
import com.example.ui.theme.InkPrimary
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
 * Tab 5: Sửa (Review & Fix) — Closes the full Lộ trình -> Học -> Luyện -> Thi -> Sửa loop:
 * 1. Diagnostic Band Report (count-up Overall Band, 4 skill scores, AI Teacher cheering/encouraging, accuracy bars)
 * 2. Original vs. Band 8.5+ Upgrade Diff View (Rose #E11D48 underlines vs Emerald #10B981 upgrades + Gemini AI rewrite)
 * 3. Error Log -> Action Loop ("Đưa từ sai vào SRS" -> Tab 2; "Luyện lại dạng bài yếu" -> Tab 3 pre-filtered)
 */
@Composable
fun ReviewScreen(
    uiState: ApexIeltsUiState,
    onTeacherTap: () -> Unit,
    onGenerateAiDiff: (skillLabel: String, promptEn: String, originalText: String) -> Unit,
    onResetAiDiff: () -> Unit,
    onPushWordToSrs: (wordId: String) -> Unit,
    onPracticeWeakSkill: (skill: PracticeSkill, focusNoteVi: String) -> Unit,
    onReturnToRoadmap: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onReturnToRoadmap() }

    val context = LocalContext.current
    val ttsController = remember(context) { ApexTtsController(context) { } }
    DisposableEffect(ttsController) {
        onDispose { ttsController.shutdown() }
    }

    val latestAttempt = uiState.examHistory.firstOrNull()
    val allExams = remember(uiState.customExams) {
        ExamBankCurriculum.authenticExams + uiState.customExams
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
            .fillMaxSize()
            .background(SurfaceCanvas)
            .verticalScroll(rememberScrollState())
            .testTag("review_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Vibrant Rose-Violet-Indigo Hero Header
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
                        color = Color.White.copy(alpha = 0.20f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Rounded.AutoFixHigh, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Text(
                                text = "Bước 5 / 5 • Chữa lỗi & Nâng Band 8.5",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
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

            // Switcher between Latest Real Submitted Exam (if any) & Pre-loaded Sample Report
            if (latestAttempt != null) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            false to "Bài thi vừa nộp",
                            true to "Báo cáo mẫu 7.0→8.5"
                        ).forEach { (isSampleOption, label) ->
                            val selected = preferSampleReport == isSampleOption
                            val bg by animateColorAsState(
                                if (selected) CardWhite else Color.Transparent,
                                tween(180),
                                label = "review_src_bg_$isSampleOption"
                            )
                            val isDark = LocalApexColors.current.isDark
                            val fg by animateColorAsState(
                                if (selected) (if (isDark) InkPrimary else RoseDiff) else Color.White,
                                tween(180),
                                label = "review_src_fg_$isSampleOption"
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
                                    .padding(vertical = 8.dp)
                                    .testTag("review_source_tab_$isSampleOption"),
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

        // 3. Error Log -> Action Loop ("Nhật ký lỗi sai")
        ReviewErrorLogSection(
            errors = activeReport.errorLog,
            srsWordIds = uiState.srsWordIds,
            onPushWordToSrs = onPushWordToSrs,
            onPracticeWeakSkill = onPracticeWeakSkill
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
