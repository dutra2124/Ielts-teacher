package com.example.ui.screens.review

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PracticeSkill
import com.example.data.QuestionTypeAccuracy
import com.example.data.ReviewReportBundle
import com.example.data.TeacherMood
import com.example.ui.components.AnimatedTeacher
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardSurfaceTinted
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
import com.example.ui.theme.LocalAdaptiveLayoutInfo
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.RoseDiff
import com.example.ui.theme.RoseDiffLight
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.VividViolet
import java.util.Locale

/**
 * Diagnostic Band Report Card:
 * - Animated count-up Overall Band (1.0–9.0) + 4 individual skill Band scores
 * - Animated AI Teacher in cheering (if target met) or encouraging (if below target) mood
 * - Horizontal animated accuracy bars broken down by IELTS question types / criteria
 */
@Composable
fun ReviewDiagnosticCard(
    report: ReviewReportBundle,
    isTeacherSpeaking: Boolean,
    onTeacherTap: () -> Unit,
    onPracticeWeakSkill: (PracticeSkill, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var playAnimation by remember(report.reportId) { mutableStateOf(false) }
    LaunchedEffect(report.reportId) {
        playAnimation = true
    }

    val animatedOverall by animateFloatAsState(
        targetValue = if (playAnimation) report.bands.overall else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "review_overall_countup"
    )

    val teacherMood = if (report.bands.meetsTarget) TeacherMood.CHEERING else TeacherMood.ENCOURAGING

    val layoutInfo = LocalAdaptiveLayoutInfo.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = layoutInfo.horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Overall Band + 4 Skill Scores + AI Teacher Diagnostic Card
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, CardBorderSoft),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(26.dp),
                    ambientColor = ElectricIndigo.copy(alpha = 0.10f),
                    spotColor = ElectricIndigo.copy(alpha = 0.14f)
                )
                .testTag("review_diagnostic_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Report Title & Overall Count-up Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (report.bands.meetsTarget) EmeraldSuccessLight else ElectricIndigoLight
                        ) {
                            Text(
                                text = if (report.isSampleReport) "Báo cáo Mẫu sẵn có" else "Kết quả Bài thi Thực tế",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (report.bands.meetsTarget) EmeraldOnContainer else IndigoOnSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = report.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = InkPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = report.subtitleVi,
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }

                    // Animated Count-up Overall Band Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    if (report.bands.meetsTarget) {
                                        listOf(EmeraldSuccess, Color(0xFF0D9488))
                                    } else {
                                        listOf(ElectricIndigo, VividViolet)
                                    }
                                )
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("review_overall_band_badge"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFDE047),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "OVERALL",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                    color = Color.White.copy(alpha = 0.88f)
                                )
                            }
                            Text(
                                text = String.format(Locale.US, "%.1f", animatedOverall),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Mục tiêu ${String.format(Locale.US, "%.1f", report.bands.targetBand)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                color = Color.White.copy(alpha = 0.88f)
                            )
                        }
                    }
                }

                // 4 Individual Skill Band Score Pills (1.0 - 9.0)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SkillBandTile("Listening", report.bands.listening, Modifier.weight(1f))
                    SkillBandTile("Reading", report.bands.reading, Modifier.weight(1f))
                    SkillBandTile("Writing", report.bands.writing, Modifier.weight(1f))
                    SkillBandTile("Speaking", report.bands.speaking, Modifier.weight(1f))
                }

                // Animated AI Teacher (cheering vs encouraging) + Concise Vietnamese Diagnostic Summary
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (report.bands.meetsTarget) EmeraldSuccessLight.copy(alpha = 0.55f) else CardSurfaceTinted,
                    border = BorderStroke(
                        1.dp,
                        if (report.bands.meetsTarget) EmeraldSuccess.copy(alpha = 0.35f) else ElectricIndigo.copy(alpha = 0.18f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnimatedTeacher(
                            mood = teacherMood,
                            isSpeaking = isTeacherSpeaking,
                            size = 66.dp,
                            onClick = onTeacherTap
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (report.bands.meetsTarget) {
                                    "Chẩn đoán AI • Đạt mục tiêu!"
                                } else {
                                    "Chẩn đoán AI • Kế hoạch kéo Band 8.5"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (report.bands.meetsTarget) EmeraldOnContainer else IndigoOnSurface
                            )
                            Text(
                                text = report.teacherDiagnosisVi,
                                style = MaterialTheme.typography.bodySmall,
                                color = InkPrimary
                            )
                        }
                    }
                }
            }
        }

        // Horizontal Accuracy Bars by IELTS Question Types / Criteria
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, CardBorderSoft),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("review_accuracy_bars_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.BarChart, contentDescription = null, tint = ElectricIndigo, modifier = Modifier.size(20.dp))
                    Column {
                        Text(
                            text = "Độ chính xác theo Dạng bài & Tiêu chí IELTS",
                            style = MaterialTheme.typography.titleSmall,
                            color = InkPrimary
                        )
                        Text(
                            text = "Chạm vào thanh điểm yếu để nhảy thẳng sang Tab 3 luyện lại ngay",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted
                        )
                    }
                }

                report.accuracyBreakdown.forEach { item ->
                    AccuracyBreakdownRow(
                        item = item,
                        playAnimation = playAnimation,
                        onClick = {
                            onPracticeWeakSkill(
                                item.targetPracticeSkill,
                                "Đang tập trung cải thiện dạng bài yếu: ${item.labelVi} (${item.accuracyPercent}%)"
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillBandTile(label: String, band: Float, modifier: Modifier = Modifier) {
    val accent = when {
        band >= 8.0f -> EmeraldSuccess
        band >= 7.0f -> ElectricIndigo
        band >= 6.5f -> GoldMastery
        else -> RoseDiff
    }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceCanvas,
        border = BorderStroke(1.dp, CardBorderSoft),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = InkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = String.format(Locale.US, "%.1f", band),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = accent
            )
        }
    }
}

@Composable
private fun AccuracyBreakdownRow(
    item: QuestionTypeAccuracy,
    playAnimation: Boolean,
    onClick: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (playAnimation) item.accuracyPercent / 100f else 0f,
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "accuracy_bar_${item.id}"
    )

    val (barColor, badgeBg, badgeFg) = when {
        item.accuracyPercent >= 78 -> Triple(EmeraldSuccess, EmeraldSuccessLight, EmeraldOnContainer)
        item.accuracyPercent >= 65 -> Triple(GoldMastery, GoldMasteryLight, GoldOnContainer)
        else -> Triple(RoseDiff, RoseDiffLight, RoseDiff)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag("accuracy_row_${item.id}"),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.labelVi,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = InkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.skillTagEn,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = InkMuted
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(shape = RoundedCornerShape(8.dp), color = badgeBg) {
                    Text(
                        text = "${item.accuracyPercent}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = badgeFg,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = "Luyện lại ${item.labelVi}",
                    tint = InkMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        LinearProgressIndicator(
            progress = { animatedProgress.coerceIn(0f, 1f) },
            color = barColor,
            trackColor = SurfaceCanvas,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
        )
    }
}
