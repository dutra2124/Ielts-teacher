package com.example.ui.screens.review

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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.BookmarkAdded
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.components.CollapsibleQuestionAccordionCard
import com.example.ui.components.QuestionNavigatorBar
import com.example.ui.components.QuestionStatusItem
import com.example.ui.components.defaultPracticeNavigatorPalette
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardWhite
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.GoldMastery
import com.example.ui.theme.GoldMasteryLight
import com.example.ui.theme.GoldOnContainer
import com.example.ui.theme.IndigoOnSurface
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
 * Compact, Accordion-based Exam Correction & Deep Diagnostic Toolkit ("Chữa đề"):
 * - Live Question Status Bar (`QuestionNavigatorBar`) + "Bảng câu" Overview Grid
 * - Single-line collapsed question headers (`Câu N` + 1-line preview + Đúng/Sai/Xem lại status)
 * - Active expanded question shows 3 color-coded Deep Analysis sections + Gemini AI + SRS/Practice CTAs
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

    val missedCount = remember(errors) { errors.count { !it.isCorrect } }
    val filteredItems = remember(errors, selectedFilter) {
        when (selectedFilter) {
            ReviewQuestionFilter.ALL -> errors
            ReviewQuestionFilter.MISSED -> errors.filter { !it.isCorrect }
            ReviewQuestionFilter.READING -> errors.filter { it.skill == ExamQuestionSkill.READING }
            ReviewQuestionFilter.LISTENING -> errors.filter { it.skill == ExamQuestionSkill.LISTENING }
        }
    }

    // Smart Accordion state controlled by activeQuestionIndex + expandAll toggle + user review flags
    var activeQuestionIndex by remember(filteredItems) { mutableIntStateOf(0) }
    var expandAll by remember(selectedFilter) { mutableStateOf(false) }
    val flaggedIds = remember(errors) { mutableStateMapOf<String, Boolean>() }

    val statusItems = filteredItems.map { err ->
        QuestionStatusItem(
            id = err.id,
            number = err.questionNumber,
            previewText = err.promptEn,
            answerSummary = err.userAnswer.takeIf { it.isNotBlank() && it != "(Bỏ trống)" },
            isFlagged = flaggedIds[err.id] == true,
            isCorrect = err.isCorrect
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = layoutInfo.horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Compact Section Header
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
                Icon(Icons.Rounded.ReportProblem, contentDescription = null, tint = RoseDiff, modifier = Modifier.size(19.dp))
                Text(
                    text = stringResource(R.string.review_error_log_title),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = InkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = if (missedCount > 0) RoseDiffLight else EmeraldSuccessLight
            ) {
                Text(
                    text = if (missedCount > 0) "$missedCount sai / ${errors.size}" else "${errors.size} đạt",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (missedCount > 0) RoseDiff else EmeraldOnContainer,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                )
            }
        }

        // Quick Filter Chips Row
        if (errors.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                            .clickable {
                                selectedFilterName = filter.name
                                activeQuestionIndex = 0
                            }
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
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Live Question Navigator Bar + Expand All + Overview Grid
        if (statusItems.isNotEmpty()) {
            QuestionNavigatorBar(
                items = statusItems,
                activeIndex = activeQuestionIndex,
                onSelectQuestion = { idx ->
                    if (expandAll) expandAll = false
                    activeQuestionIndex = idx
                },
                palette = defaultPracticeNavigatorPalette(ElectricIndigo),
                expandAll = expandAll,
                onToggleExpandAll = { expandAll = !expandAll },
                testTagPrefix = "review_nav",
                modifier = Modifier.clip(RoundedCornerShape(16.dp))
            )
        }

        if (filteredItems.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = EmeraldSuccessLight.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(22.dp))
                    Text(
                        text = if (errors.isEmpty()) {
                            "Không có lỗi trắc nghiệm nào!"
                        } else {
                            "Không có câu khớp mục \"${selectedFilter.labelVi}\"."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldOnContainer
                    )
                }
            }
        } else {
            filteredItems.forEachIndexed { idx, err ->
                val isInSrs = err.linkedVocabWordId in srsWordIds
                val isExpanded = expandAll || activeQuestionIndex == idx
                val isFlagged = flaggedIds[err.id] == true
                val activeAnalysis = deepQuestionAiResults[err.id] ?: err.deepAnalysis
                val isAiLoading = err.id in loadingQuestionAnalysisIds
                val aiError = questionAnalysisErrors[err.id]

                DeepQuestionAccordionCard(
                    err = err,
                    activeAnalysis = activeAnalysis,
                    isExpanded = isExpanded,
                    isFlagged = isFlagged,
                    isInSrs = isInSrs,
                    isAiLoading = isAiLoading,
                    aiError = aiError,
                    hasPrev = idx > 0,
                    hasNext = idx < filteredItems.lastIndex,
                    onToggleExpand = {
                        if (expandAll) {
                            expandAll = false
                            activeQuestionIndex = idx
                        } else {
                            activeQuestionIndex = if (activeQuestionIndex == idx) -1 else idx
                        }
                    },
                    onToggleFlag = { flaggedIds[err.id] = !isFlagged },
                    onPrev = { activeQuestionIndex = (idx - 1).coerceAtLeast(0) },
                    onNext = { activeQuestionIndex = (idx + 1).coerceAtMost(filteredItems.lastIndex) },
                    onGenerateDeepAnalysis = {
                        activeQuestionIndex = idx
                        onGenerateDeepAnalysis(err)
                    },
                    onPushWordToSrs = { onPushWordToSrs(err.linkedVocabWordId) },
                    onPracticeWeakSkill = {
                        onPracticeWeakSkill(
                            err.targetPracticeSkill,
                            "Ôn lại: ${err.questionTypeVi} (${err.skill.labelEn})"
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun DeepQuestionAccordionCard(
    err: ReviewErrorLogItem,
    activeAnalysis: DeepQuestionAnalysis,
    isExpanded: Boolean,
    isFlagged: Boolean,
    isInSrs: Boolean,
    isAiLoading: Boolean,
    aiError: String?,
    hasPrev: Boolean,
    hasNext: Boolean,
    onToggleExpand: () -> Unit,
    onToggleFlag: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onGenerateDeepAnalysis: () -> Unit,
    onPushWordToSrs: () -> Unit,
    onPracticeWeakSkill: () -> Unit
) {
    CollapsibleQuestionAccordionCard(
        questionNumber = err.questionNumber,
        previewText = err.promptEn,
        answerSummary = err.userAnswer.takeIf { it.isNotBlank() && it != "(Bỏ trống)" },
        isExpanded = isExpanded,
        isFlagged = isFlagged,
        isCorrect = err.isCorrect,
        badgeLabel = "${err.skill.labelEn} • ${err.questionTypeVi}",
        accentColor = ElectricIndigo,
        onToggleExpand = onToggleExpand,
        onToggleFlag = onToggleFlag,
        modifier = Modifier.testTag("error_log_item_${err.id}")
    ) {
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
                shape = RoundedCornerShape(10.dp),
                color = userCardBg,
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
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
                shape = RoundedCornerShape(10.dp),
                color = EmeraldSuccessLight,
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
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

        // 3 Color-Coded Deep Diagnostic Sections
        DeepDiagnosticBlock(
            icon = Icons.Rounded.Lightbulb,
            title = "Tip giải nhanh",
            body = activeAnalysis.quickTipVi,
            containerColor = EmeraldSuccessLight.copy(alpha = 0.65f),
            borderColor = EmeraldSuccess.copy(alpha = 0.40f),
            accentColor = EmeraldOnContainer,
            testTagName = "deep_tip_${err.id}"
        )

        DeepDiagnosticBlock(
            icon = Icons.Rounded.WarningAmber,
            title = "Lỗi Band 6.0–6.5",
            body = activeAnalysis.commonMistakesVi,
            containerColor = GoldMasteryLight.copy(alpha = 0.72f),
            borderColor = GoldMastery.copy(alpha = 0.42f),
            accentColor = GoldOnContainer,
            testTagName = "deep_mistakes_${err.id}"
        )

        DeepDiagnosticBlock(
            icon = Icons.Rounded.Psychology,
            title = "Bẫy Distractor",
            body = activeAnalysis.misconceptionTrapVi,
            containerColor = RoseDiffLight.copy(alpha = 0.72f),
            borderColor = RoseDiff.copy(alpha = 0.35f),
            accentColor = RoseDiff,
            testTagName = "deep_misconception_${err.id}"
        )

        // Grammar Context Note
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = SurfaceCanvas
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(9.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Rounded.MenuBook, contentDescription = null, tint = IndigoOnSurface, modifier = Modifier.size(15.dp))
                Text(
                    text = err.grammarNoteVi,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary
                )
            }
        }

        if (!aiError.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = RoseDiffLight,
                border = BorderStroke(1.dp, RoseDiff.copy(alpha = 0.4f))
            ) {
                Text(
                    text = aiError,
                    style = MaterialTheme.typography.labelSmall,
                    color = RoseDiff,
                    modifier = Modifier.padding(9.dp)
                )
            }
        }

        // "AI Chữa sâu câu này" CTA Button
        Button(
            onClick = onGenerateDeepAnalysis,
            enabled = !isAiLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VividViolet,
                contentColor = Color.White,
                disabledContainerColor = VividViolet.copy(alpha = 0.55f),
                disabledContentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("ai_deep_analysis_${err.id}")
        ) {
            if (isAiLoading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    color = Color.White,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AI đang chữa câu ${err.questionNumber}...",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (activeAnalysis.isAiGenerated) "Chữa sâu lại (Gemini AI)" else "AI Chữa sâu câu này",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 2 Preserved One-Tap Action Loop Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onPushWordToSrs,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isInSrs) EmeraldSuccess else IndigoOnSurface),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
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
                        "SRS: ${err.linkedVocabWordEn}"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = if (isInSrs) EmeraldOnContainer else IndigoOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = onPracticeWeakSkill,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PracticeTeal,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
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

        // Quick Prev / Next question switchers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = onPrev,
                enabled = hasPrev,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Câu trước", style = MaterialTheme.typography.labelMedium)
            }
            TextButton(
                onClick = onNext,
                enabled = hasNext,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("Câu tiếp", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.width(3.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(15.dp))
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
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTagName)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
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
