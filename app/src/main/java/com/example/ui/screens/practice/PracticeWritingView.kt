package com.example.ui.screens.practice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ai.WritingAiEvaluation
import com.example.ai.WritingAiUiState
import com.example.data.TeacherMood
import com.example.data.WritingDataPoint
import com.example.data.WritingPracticePack
import com.example.data.WritingTaskType
import com.example.ui.components.AnimatedTeacher
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LocalAdaptiveLayoutInfo
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.PracticeTeal
import com.example.ui.theme.PracticeTealDark
import com.example.ui.theme.PracticeTealLight
import com.example.ui.theme.PracticeTealSurface
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.TealOnSurface

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PracticeWritingView(
    pack: WritingPracticePack,
    essayDraft: String,
    onEssayDraftChange: (String) -> Unit,
    aiState: WritingAiUiState,
    onEvaluateWriting: (taskLabel: String, promptEn: String, topicEn: String, essay: String, wordCount: Int) -> Unit,
    onResetAi: () -> Unit,
    modifier: Modifier = Modifier
) {
    val layoutInfo = LocalAdaptiveLayoutInfo.current
    var selectedTask by remember(pack.moduleId) { mutableStateOf(WritingTaskType.TASK_2) }
    var showOutline by remember(pack.moduleId) { mutableStateOf(true) }

    val activePrompt = remember(pack, selectedTask) {
        pack.prompts.firstOrNull { it.taskType == selectedTask } ?: pack.prompts.first()
    }

    val wordCount = remember(essayDraft) {
        essayDraft.trim().split(Regex("\\s+")).count { it.isNotBlank() }
    }
    val targetWords = activePrompt.taskType.targetWords
    val wordProgress = (wordCount.toFloat() / targetWords.toFloat()).coerceIn(0f, 1f)
    val reachedTarget = wordCount >= targetWords

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = layoutInfo.horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Task 1 | Task 2 Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            WritingTaskType.entries.forEach { task ->
                val selected = task == selectedTask
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) PracticeTeal else CardWhite,
                    border = BorderStroke(1.dp, if (selected) PracticeTeal else CardBorderSoft),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            selectedTask = task
                            onResetAi()
                        }
                        .testTag("writing_task_${task.name.lowercase()}")
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Writing ${task.labelEn}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = if (selected) Color.White else InkPrimary
                        )
                        Text(
                            text = task.badgeVi,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = if (selected) Color.White.copy(alpha = 0.9f) else InkMuted
                        )
                    }
                }
            }
        }

        val promptAndOutlinePane: @Composable () -> Unit = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
        // Prompt Card (+ Visual Data Chart for Task 1)
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, CardBorderSoft)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PracticeTealLight
                ) {
                    Text(
                        text = "IELTS Academic Writing ${activePrompt.taskType.labelEn} • ${pack.topicEn}",
                        style = MaterialTheme.typography.labelSmall,
                        color = PracticeTealDark,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = activePrompt.promptEn,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp
                    ),
                    color = InkPrimary
                )

                if (activePrompt.chartData.isNotEmpty()) {
                    Task1ComparisonChartCard(
                        title = activePrompt.chartTitleEn.orEmpty(),
                        data = activePrompt.chartData
                    )
                }
            }
        }

        // Quick Outline Builder (Khung lập dàn ý nhanh)
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, CardBorderSoft),
            modifier = Modifier.testTag("writing_outline_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showOutline = !showOutline },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.FormatListBulleted, contentDescription = null, tint = PracticeTealDark, modifier = Modifier.size(18.dp))
                        Text(
                            text = stringResource(R.string.practice_outline_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = PracticeTealDark
                        )
                    }
                    Text(
                        text = if (showOutline) "Thu gọn" else "Mở dàn ý",
                        style = MaterialTheme.typography.labelMedium,
                        color = PracticeTeal
                    )
                }

                AnimatedVisibility(visible = showOutline) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        activePrompt.outlineSlots.forEachIndexed { idx, slot ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = PracticeTealSurface,
                                border = BorderStroke(1.dp, PracticeTeal.copy(alpha = 0.25f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = slot.stepTitleVi,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = PracticeTealDark,
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedButton(
                                            onClick = {
                                                val appended = if (essayDraft.isBlank()) {
                                                    slot.starterFrameEn
                                                } else {
                                                    "${essayDraft.trimEnd()}\n\n${slot.starterFrameEn}"
                                                }
                                                onEssayDraftChange(appended)
                                            },
                                            shape = RoundedCornerShape(50),
                                            border = BorderStroke(1.dp, PracticeTeal),
                                            contentPadding = PaddingValues(horizontal = 9.dp, vertical = 2.dp),
                                            modifier = Modifier
                                                .height(30.dp)
                                                .testTag("writing_insert_outline_$idx")
                                        ) {
                                            Icon(Icons.Rounded.AddCircleOutline, contentDescription = null, tint = PracticeTealDark, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Chèn khung", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = PracticeTealDark)
                                        }
                                    }
                                    Text(
                                        text = slot.guideVi,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = InkSecondary
                                    )
                                    Text(
                                        text = "\"${slot.starterFrameEn}\"",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = InkPrimary
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

        val editorAndFeedbackPane: @Composable () -> Unit = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
        // Essay Editor + Automatic Word Count Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, CardBorderSoft)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (reachedTarget) EmeraldSuccessLight else PracticeTealLight,
                        modifier = Modifier
                            .wrapContentWidth()
                            .testTag("writing_word_count_badge")
                    ) {
                        Text(
                            text = "Word Count: $wordCount / $targetWords từ",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (reachedTarget) EmeraldOnContainer else TealOnSurface,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SurfaceCanvas,
                        modifier = Modifier
                            .wrapContentWidth()
                            .clip(RoundedCornerShape(50))
                            .clickable { onEssayDraftChange(activePrompt.sampleStarterDraft) }
                            .testTag("writing_fill_sample")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = TealOnSurface, modifier = Modifier.size(14.dp))
                            Text(
                                text = "Điền nháp Band 6.0",
                                style = MaterialTheme.typography.labelSmall,
                                color = TealOnSurface,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                LinearProgressIndicator(
                    progress = { wordProgress },
                    color = if (reachedTarget) EmeraldSuccess else PracticeTeal,
                    trackColor = SurfaceCanvas,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                )

                OutlinedTextField(
                    value = essayDraft,
                    onValueChange = onEssayDraftChange,
                    minLines = 6,
                    maxLines = 12,
                    placeholder = {
                        Text("Viết bài IELTS Writing ${activePrompt.taskType.labelEn} tại đây... Bạn có thể mở nút nổi 'Từ vựng 8.5+' để chèn nhanh Collocations & Idioms!")
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PracticeTeal,
                        unfocusedBorderColor = CardBorderSoft,
                        focusedContainerColor = SurfaceCanvas.copy(alpha = 0.5f),
                        unfocusedContainerColor = SurfaceCanvas.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("writing_essay_input")
                )

                Button(
                    onClick = {
                        onEvaluateWriting(
                            activePrompt.taskType.labelEn,
                            activePrompt.promptEn,
                            pack.topicEn,
                            essayDraft,
                            wordCount
                        )
                    },
                    enabled = aiState !is WritingAiUiState.Loading && wordCount >= 12,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PracticeTeal),
                    contentPadding = PaddingValues(vertical = 13.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("writing_submit_ai_button")
                ) {
                    if (aiState is WritingAiUiState.Loading) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini AI đang chấm Writing...")
                    } else {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.practice_writing_submit))
                    }
                }
            }
        }

        // Gemini AI Writing Evaluation Result
        when (aiState) {
            WritingAiUiState.Idle -> Unit
            WritingAiUiState.Loading -> {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PracticeTealSurface,
                    border = BorderStroke(1.dp, PracticeTeal.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnimatedTeacher(mood = TeacherMood.TEACHING, isSpeaking = true, size = 58.dp)
                        Text(
                            text = "Giám khảo AI đang chấm điểm 4 tiêu chí TR/TA, CC, LR, GRA và viết lại đoạn nâng cấp chuẩn Band 8.5+...",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkPrimary
                        )
                    }
                }
            }

            is WritingAiUiState.Error -> {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFF1F2),
                    border = BorderStroke(1.dp, RoseAlert.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = RoseAlert)
                            Text(aiState.messageVi, style = MaterialTheme.typography.bodySmall, color = InkPrimary)
                        }
                        if (wordCount >= 12) {
                            Button(
                                onClick = {
                                    onEvaluateWriting(activePrompt.taskType.labelEn, activePrompt.promptEn, pack.topicEn, essayDraft, wordCount)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RoseAlert),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("writing_retry_ai_button")
                            ) {
                                Text("Thử chấm lại ngay")
                            }
                        }
                    }
                }
            }

            is WritingAiUiState.Success -> {
                WritingEvaluationCard(evaluation = aiState.result)
            }
        }
            }
        }

        if (layoutInfo.useSideBySideSplit) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(modifier = Modifier.weight(0.95f)) { promptAndOutlinePane() }
                Box(modifier = Modifier.weight(1.05f)) { editorAndFeedbackPane() }
            }
        } else {
            promptAndOutlinePane()
            editorAndFeedbackPane()
        }
    }
}

@Composable
private fun Task1ComparisonChartCard(
    title: String,
    data: List<WritingDataPoint>
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = SurfaceCanvas,
        border = BorderStroke(1.dp, CardBorderSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Rounded.BarChart, contentDescription = null, tint = PracticeTealDark, modifier = Modifier.size(18.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = PlusJakartaSansFontFamily),
                    color = InkPrimary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(CyanGlow))
                    Text("2015", style = MaterialTheme.typography.labelSmall, color = InkSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(PracticeTeal))
                    Text("2025", style = MaterialTheme.typography.labelSmall, color = InkSecondary)
                }
            }

            data.forEach { point ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = point.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = InkPrimary
                        )
                        Text(
                            text = "${point.value2015}${point.unit} → ${point.value2025}${point.unit}",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = PlusJakartaSansFontFamily),
                            color = PracticeTealDark
                        )
                    }
                    LinearProgressIndicator(
                        progress = { (point.value2015 / 100f).coerceIn(0f, 1f) },
                        color = CyanGlow,
                        trackColor = CardWhite,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                    )
                    LinearProgressIndicator(
                        progress = { (point.value2025 / 100f).coerceIn(0f, 1f) },
                        color = PracticeTeal,
                        trackColor = CardWhite,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                    )
                }
            }
        }
    }
}

@Composable
private fun WritingEvaluationCard(evaluation: WritingAiEvaluation) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = CardWhite,
        border = BorderStroke(1.5.dp, PracticeTeal.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("writing_ai_result_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnimatedTeacher(mood = TeacherMood.CHEERING, isSpeaking = true, size = 54.dp)
                    Column {
                        Text("Kết quả Writing Examiner", style = MaterialTheme.typography.titleMedium, color = PracticeTealDark)
                        Text("Chấm chuẩn 4 tiêu chí IELTS", style = MaterialTheme.typography.bodySmall, color = InkMuted)
                    }
                }
                Surface(shape = RoundedCornerShape(14.dp), color = PracticeTeal) {
                    Text(
                        text = "Band ${evaluation.overallBand}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BandMiniPill("TR / TA", evaluation.taskResponseBand, Modifier.weight(1f))
                BandMiniPill("CC", evaluation.coherenceBand, Modifier.weight(1f))
                BandMiniPill("LR", evaluation.lexicalBand, Modifier.weight(1f))
                BandMiniPill("GRA", evaluation.grammarBand, Modifier.weight(1f))
            }

            if (evaluation.summaryFeedbackVi.isNotBlank()) {
                Text(
                    text = evaluation.summaryFeedbackVi,
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkPrimary
                )
            }

            if (evaluation.keyFixesVi.isNotEmpty()) {
                Surface(shape = RoundedCornerShape(16.dp), color = SurfaceCanvas) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "3 điểm sửa đắt giá nhất để bứt phá Band 8.5:",
                            style = MaterialTheme.typography.labelMedium,
                            color = PracticeTealDark
                        )
                        evaluation.keyFixesVi.forEach { fix ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.padding(top = 2.dp).size(15.dp))
                                Text(fix, style = MaterialTheme.typography.bodySmall, color = InkPrimary)
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(PracticeTeal, EmeraldSuccess)))
                    .padding(1.5.dp)
            ) {
                Surface(shape = RoundedCornerShape(17.dp), color = CardWhite) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = PracticeTealDark, modifier = Modifier.size(16.dp))
                            Text("Đoạn văn nâng cấp chuẩn Band 8.5+", style = MaterialTheme.typography.labelMedium, color = PracticeTealDark)
                        }
                        Text(
                            text = evaluation.upgradedBand85Excerpt,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 21.sp
                            ),
                            color = InkPrimary
                        )
                    }
                }
            }
        }
    }
}
