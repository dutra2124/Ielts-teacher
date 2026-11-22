package com.example.ui.screens.review

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.BookmarkAdded
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.UnfoldLess
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.DeepQuestionAnalysis
import com.example.data.ExamQuestionSkill
import com.example.data.PracticeSkill
import com.example.data.ReviewErrorLogItem
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
import com.example.ui.theme.LocalAdaptiveLayoutInfo
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.PracticeTeal
import com.example.ui.theme.RoseDiff
import com.example.ui.theme.RoseDiffLight
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.VividViolet

private enum class ReviewQuestionFilter(val labelVi: String) {
    ALL("Tất cả"),
    MISSED("Câu sai"),
    READING("Reading"),
    LISTENING("Listening")
}

/**
 * Enhanced Exam Correction & Deep Diagnostic Toolkit ("Phân tích từng câu & Sửa lỗi Chuyên sâu"):
 * - Quick filter bar (All / Missed / Reading / Listening) + Expand/Collapse all
 * - Expandable Deep Question Analysis Card for every reviewed question with 3 color-coded sections:
 *   1. 💡 Tip / Trick giải nhanh (Exam Strategy)
 *   2. ⚠️ Lỗi sai thường mắc phải (Common Band 6.0–6.5 Mistakes)
 *   3. 🔍 Hiểu nhầm hay gặp (Common Misconceptions & Distractor Traps)
 * - "AI Chữa sâu câu này" button powered by Gemini AI (Firebase.ai)
 * - 1-tap "Đưa từ sai vào SRS" and "Luyện lại dạng bài yếu" action loop
 */
@Composable
fun ReviewErrorLogSection(
    errors: List<ReviewErrorLogItem>,
    srsWordIds: Set<String>,
    deepQuestionAiResults: Map<String, DeepQuestionAnalysis> = emptyMap(),
    loadingQuestionAnalysisIds: Set<String> = emptySet(),
    questionAnalysisErrors: Map<String, String> = emptyMap(),
    onGenerateDeepAnalysis: (ReviewErrorLogItem) -> Unit = {},
    onPushWordToSrs: (wordId: String) -> Unit,
    onPracticeWeakSkill: (skill: PracticeSkill, focusNoteVi: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val layoutInfo = LocalAdaptiveLayoutInfo.current
    var selectedFilterName by rememberSaveable { mutableStateOf(ReviewQuestionFilter.ALL.name) }
    val selectedFilter = remember(selectedFilterName) {
        ReviewQuestionFilter.entries.firstOrNull { it.name == selectedFilterName } ?: ReviewQuestionFilter.ALL
    }

    // Track expanded state per question ID; expand first card automatically for instant visibility
    val expandedStateMap = remember(errors) {
        mutableStateMapOf<String, Boolean>().apply {
            errors.forEachIndexed { idx, item ->
                put(item.id, idx == 0)
            }
        }
    }

    val missedCount = remember(errors) { errors.count { !it.isCorrect } }
    val filteredItems = remember(errors, selectedFilter) {
        when (selectedFilter) {
            ReviewQuestionFilter.ALL -> errors
            ReviewQuestionFilter.MISSED -> errors.filter { !it.isCorrect }
            ReviewQuestionFilter.READING -> errors.filter { it.skill == ExamQuestionSkill.READING }
            ReviewQuestionFilter.LISTENING -> errors.filter { it.skill == ExamQuestionSkill.LISTENING }
        }
    }
    val allExpanded = filteredItems.isNotEmpty() && filteredItems.all { expandedStateMap[it.id] == true }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = layoutInfo.horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Rounded.ReportProblem, contentDescription = null, tint = RoseDiff, modifier = Modifier.size(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = stringResource(R.string.review_error_log_title),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = InkPrimary
                    )
                    Text(
                        text = "Chạm từng câu để mở 3 góc nhìn: Tip giải nhanh • Lỗi Band 6.5 • Bẫy Distractor",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkSecondary
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = if (missedCount > 0) RoseDiffLight else EmeraldSuccessLight
            ) {
                Text(
                    text = if (missedCount > 0) "$missedCount sai / ${errors.size} câu" else "${errors.size} câu đạt",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (missedCount > 0) RoseDiff else EmeraldOnContainer,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                )
            }
        }

        // Quick Filter & Expand-All Diagnostic Toolkit Bar
        if (errors.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReviewQuestionFilter.entries.forEach { filter ->
                    val count = when (filter) {
                        ReviewQuestionFilter.ALL -> errors.size
                        ReviewQuestionFilter.MISSED -> missedCount
                        ReviewQuestionFilter.READING -> errors.count { it.skill == ExamQuestionSkill.READING }
                        ReviewQuestionFilter.LISTENING -> errors.count { it.skill == ExamQuestionSkill.LISTENING }
                    }
                    val active = selectedFilter == filter
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (active) ElectricIndigo else CardWhite,
                        border = BorderStroke(1.dp, if (active) ElectricIndigo else CardBorderSoft),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { selectedFilterName = filter.name }
                            .testTag("review_filter_${filter.name.lowercase()}")
                    ) {
                        Text(
                            text = "${filter.labelVi} ($count)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (active) Color.White else InkSecondary,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }

                // Expand / Collapse All Toggle
                Surface(
                    shape = RoundedCornerShape(50),
                    color = SurfaceCanvas,
                    border = BorderStroke(1.dp, CardBorderSoft),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable {
                            val next = !allExpanded
                            filteredItems.forEach { expandedStateMap[it.id] = next }
                        }
                        .testTag("review_toggle_expand_all")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Icon(
                            imageVector = if (allExpanded) Icons.Rounded.UnfoldLess else Icons.Rounded.UnfoldMore,
                            contentDescription = null,
                            tint = IndigoOnSurface,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (allExpanded) "Thu gọn" else "Mở hết",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = IndigoOnSurface,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        if (filteredItems.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = EmeraldSuccessLight.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(24.dp))
                    Text(
                        text = if (errors.isEmpty()) {
                            "Tuyệt vời! Bạn không mắc lỗi trắc nghiệm nào trong bài thi này. Hãy xem bản nâng cấp Writing/Speaking phía trên để tối ưu Band 8.5+!"
                        } else {
                            "Không có câu hỏi nào khớp bộ lọc \"${selectedFilter.labelVi}\". Chạm \"Tất cả\" để xem đầy đủ từng câu đã chữa."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldOnContainer
                    )
                }
            }
        } else {
            filteredItems.forEach { err ->
                val isInSrs = err.linkedVocabWordId in srsWordIds
                val isExpanded = expandedStateMap[err.id] ?: false
                val activeAnalysis = deepQuestionAiResults[err.id] ?: err.deepAnalysis
                val isAiLoading = err.id in loadingQuestionAnalysisIds
                val aiError = questionAnalysisErrors[err.id]

                DeepQuestionCorrectionCard(
                    err = err,
                    activeAnalysis = activeAnalysis,
                    isExpanded = isExpanded,
                    isInSrs = isInSrs,
                    isAiLoading = isAiLoading,
                    aiError = aiError,
                    onToggleExpand = { expandedStateMap[err.id] = !isExpanded },
                    onGenerateDeepAnalysis = {
                        expandedStateMap[err.id] = true
                        onGenerateDeepAnalysis(err)
                    },
                    onPushWordToSrs = { onPushWordToSrs(err.linkedVocabWordId) },
                    onPracticeWeakSkill = {
                        onPracticeWeakSkill(
                            err.targetPracticeSkill,
                            "Ôn lại dạng yếu từ Chữa đề: ${err.questionTypeVi} (${err.skill.labelEn})"
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun DeepQuestionCorrectionCard(
    err: ReviewErrorLogItem,
    activeAnalysis: DeepQuestionAnalysis,
    isExpanded: Boolean,
    isInSrs: Boolean,
    isAiLoading: Boolean,
    aiError: String?,
    onToggleExpand: () -> Unit,
    onGenerateDeepAnalysis: () -> Unit,
    onPushWordToSrs: () -> Unit,
    onPracticeWeakSkill: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = CardWhite,
        border = BorderStroke(
            width = if (isExpanded) 1.5.dp else 1.dp,
            color = if (isExpanded) ElectricIndigo.copy(alpha = 0.42f) else CardBorderSoft
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("error_log_item_${err.id}")
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Skill + Question Number + Correct/Missed Status + Question Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(8.dp), color = ElectricIndigoLight) {
                    Text(
                        text = "${err.skill.labelEn} • Câu ${err.questionNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = IndigoOnSurface,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (err.isCorrect) EmeraldSuccessLight else RoseDiffLight
                ) {
                    Text(
                        text = if (err.isCorrect) "Đúng ✓" else "Cần sửa",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (err.isCorrect) EmeraldOnContainer else RoseDiff,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceCanvas,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = err.questionTypeVi,
                        style = MaterialTheme.typography.labelSmall,
                        color = InkSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Question Prompt
            Text(
                text = err.promptEn,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.SemiBold
                ),
                color = InkPrimary
            )

            // User Answer vs Correct Answer Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val userCardBg = if (err.isCorrect) EmeraldSuccessLight else RoseDiffLight
                val userCardFg = if (err.isCorrect) EmeraldOnContainer else RoseDiff
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = userCardBg,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(9.dp)) {
                        Text("Bạn chọn:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = userCardFg)
                        Text(
                            text = err.userAnswer,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = userCardFg,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldSuccessLight,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(9.dp)) {
                        Text("Đáp án đúng:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = EmeraldOnContainer)
                        Text(
                            text = err.correctAnswer,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = EmeraldOnContainer,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Expandable Deep Analysis Toggle Header
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isExpanded) ElectricIndigoLight.copy(alpha = 0.65f) else SurfaceCanvas,
                border = BorderStroke(
                    1.dp,
                    if (isExpanded) ElectricIndigo.copy(alpha = 0.35f) else CardBorderSoft
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onToggleExpand)
                    .minimumInteractiveComponentSize()
                    .testTag("deep_analysis_toggle_${err.id}")
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
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Psychology,
                            contentDescription = null,
                            tint = IndigoOnSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isExpanded) "Phân tích chuyên sâu 3 góc nhìn" else "Mở phân tích sâu (Tip • Lỗi sai • Bẫy)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = IndigoOnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (activeAnalysis.isAiGenerated) {
                            Surface(shape = RoundedCornerShape(50), color = VividViolet.copy(alpha = 0.16f)) {
                                Text(
                                    text = "Gemini AI ✓",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = VividViolet,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = if (isExpanded) "Thu gọn phân tích sâu" else "Mở rộng phân tích sâu",
                            tint = IndigoOnSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Expandable 3-Section Deep Diagnostic Body
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    // 1. 💡 Tip / Trick giải nhanh (Exam Strategy)
                    DeepDiagnosticBlock(
                        icon = Icons.Rounded.Lightbulb,
                        title = "💡 Tip / Trick giải nhanh (Exam Strategy)",
                        body = activeAnalysis.quickTipVi,
                        containerColor = EmeraldSuccessLight.copy(alpha = 0.65f),
                        borderColor = EmeraldSuccess.copy(alpha = 0.40f),
                        accentColor = EmeraldOnContainer,
                        testTagName = "deep_tip_${err.id}"
                    )

                    // 2. ⚠️ Lỗi sai thường mắc phải (Common Band 6.0–6.5 Mistakes)
                    DeepDiagnosticBlock(
                        icon = Icons.Rounded.WarningAmber,
                        title = "⚠️ Lỗi sai thường mắc phải (Band 6.0–6.5)",
                        body = activeAnalysis.commonMistakesVi,
                        containerColor = GoldMasteryLight.copy(alpha = 0.72f),
                        borderColor = GoldMastery.copy(alpha = 0.42f),
                        accentColor = GoldOnContainer,
                        testTagName = "deep_mistakes_${err.id}"
                    )

                    // 3. 🔍 Hiểu nhầm hay gặp (Common Misconceptions & Distractor Traps)
                    DeepDiagnosticBlock(
                        icon = Icons.Rounded.Psychology,
                        title = "🔍 Hiểu nhầm hay gặp (Bẫy Distractor)",
                        body = activeAnalysis.misconceptionTrapVi,
                        containerColor = RoseDiffLight.copy(alpha = 0.72f),
                        borderColor = RoseDiff.copy(alpha = 0.35f),
                        accentColor = RoseDiff,
                        testTagName = "deep_misconception_${err.id}"
                    )

                    // Grammar Context Note
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceCanvas
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Icon(Icons.Rounded.MenuBook, contentDescription = null, tint = IndigoOnSurface, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Ngữ pháp câu: ${err.grammarNoteVi}",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSecondary
                            )
                        }
                    }

                    // Inline Error (if AI Deep Analysis failed)
                    if (!aiError.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = RoseDiffLight,
                            border = BorderStroke(1.dp, RoseDiff.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = aiError,
                                style = MaterialTheme.typography.labelSmall,
                                color = RoseDiff,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // "AI Chữa sâu câu này" CTA Button
                    Button(
                        onClick = onGenerateDeepAnalysis,
                        enabled = !isAiLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VividViolet,
                            contentColor = Color.White,
                            disabledContainerColor = VividViolet.copy(alpha = 0.55f),
                            disabledContentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("ai_deep_analysis_${err.id}")
                    ) {
                        if (isAiLoading) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini AI đang chữa sâu câu ${err.questionNumber}...",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (activeAnalysis.isAiGenerated) {
                                    "Phân tích sâu lại bằng AI (Gemini)"
                                } else {
                                    "AI Chữa sâu câu này"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Linked C1-C2 Target Word to Push to SRS
            Text(
                text = "Từ khóa học thuật liên quan: ${err.linkedVocabWordEn}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.SemiBold
                ),
                color = IndigoOnSurface
            )

            // 2 Preserved One-Tap Action Loop Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPushWordToSrs,
                    shape = RoundedCornerShape(13.dp),
                    border = BorderStroke(1.dp, if (isInSrs) EmeraldSuccess else IndigoOnSurface),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 9.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("error_push_srs_${err.id}")
                ) {
                    Icon(
                        imageVector = if (isInSrs) Icons.Rounded.BookmarkAdded else Icons.Rounded.BookmarkAdd,
                        contentDescription = null,
                        tint = if (isInSrs) EmeraldSuccess else IndigoOnSurface,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isInSrs) {
                            stringResource(R.string.review_in_srs_btn)
                        } else {
                            stringResource(R.string.review_push_srs_btn)
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = if (isInSrs) EmeraldOnContainer else IndigoOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onPracticeWeakSkill,
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PracticeTeal,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 9.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("error_practice_weak_${err.id}")
                ) {
                    Icon(Icons.Rounded.FitnessCenter, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.review_practice_weak_btn),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun DeepDiagnosticBlock(
    icon: ImageVector,
    title: String,
    body: String,
    containerColor: Color,
    borderColor: Color,
    accentColor: Color,
    testTagName: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTagName)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(11.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = accentColor
                )
            }
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = InkPrimary
            )
        }
    }
}
