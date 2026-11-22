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
import androidx.compose.material.icons.rounded.BorderColor
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.HighlightOff
import androidx.compose.material.icons.rounded.ManageSearch
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.StickyNote2
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.KeywordMappingRow
import com.example.data.PassageSentence
import com.example.data.ReadingPracticePack
import com.example.data.ReadingQuestion
import com.example.data.ReadingQuestionType
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardWhite
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.GoldMastery
import com.example.ui.theme.GoldMasteryLight
import com.example.ui.theme.GoldOnContainer
import com.example.ui.theme.HighlightYellow
import com.example.ui.theme.HighlightYellowBorder
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

private enum class ReadingToolMode(val labelVi: String) {
    HIGHLIGHT("Highlight vàng"),
    NOTE("Ghi chú (Note)")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PracticeReadingView(
    pack: ReadingPracticePack,
    isSyntaxInspectMode: Boolean,
    onInspectSentence: (String) -> Unit,
    onOpenHintForQuestion: (Int) -> Unit,
    onReadingChecked: (correctCount: Int, totalCount: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTool by remember(pack.moduleId) { mutableStateOf(ReadingToolMode.HIGHLIGHT) }
    val highlightedIds = remember(pack.moduleId) { mutableStateMapOf<String, Boolean>() }
    val sentenceNotes = remember(pack.moduleId) { mutableStateMapOf<String, String>() }
    var editingNoteSentence by remember { mutableStateOf<PassageSentence?>(null) }
    var noteDraft by remember { mutableStateOf("") }

    val userAnswers = remember(pack.moduleId) { mutableStateMapOf<String, String>() }
    var isChecked by remember(pack.moduleId) { mutableStateOf(false) }

    val paragraphs = remember(pack.sentences) {
        pack.sentences.groupBy { it.paragraphLabel }.entries.toList()
    }

    val layoutInfo = LocalAdaptiveLayoutInfo.current

    val passagePane: @Composable () -> Unit = {
        // Passage Header & Reading Tools Bar (Highlight / Note / Syntax Inspect status)
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
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PracticeTealLight,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Text(
                            text = "IELTS Academic Reading • ~${pack.estimatedMinutes} phút",
                            style = MaterialTheme.typography.labelSmall,
                            color = PracticeTealDark,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "${highlightedIds.count { it.value }} highlight • ${sentenceNotes.count { it.value.isNotBlank() }} note",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkMuted,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }

                Text(
                    text = pack.titleEn,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = InkPrimary
                )
                Text(
                    text = pack.subtitleVi,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary
                )

                // Tool Switcher: Highlight vs Note (disabled visually when Syntax Inspect Mode overrides tap)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReadingToolMode.entries.forEach { mode ->
                        val selected = !isSyntaxInspectMode && activeTool == mode
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = when {
                                selected && mode == ReadingToolMode.HIGHLIGHT -> HighlightYellow
                                selected -> PracticeTealLight
                                else -> SurfaceCanvas
                            },
                            border = BorderStroke(
                                1.dp,
                                when {
                                    selected && mode == ReadingToolMode.HIGHLIGHT -> HighlightYellowBorder
                                    selected -> PracticeTeal
                                    else -> CardBorderSoft
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { activeTool = mode }
                                .testTag("reading_tool_${mode.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (mode == ReadingToolMode.HIGHLIGHT) {
                                        Icons.Rounded.BorderColor
                                    } else {
                                        Icons.Rounded.StickyNote2
                                    },
                                    contentDescription = null,
                                    tint = if (selected && mode == ReadingToolMode.HIGHLIGHT) {
                                        Color(0xFF854D0E)
                                    } else if (selected) {
                                        PracticeTealDark
                                    } else {
                                        InkSecondary
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = mode.labelVi,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) InkPrimary else InkSecondary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                Text(
                    text = if (isSyntaxInspectMode) {
                        "Chế độ Soi Ngữ pháp đang bật: Chạm vào bất kỳ câu nào bên dưới để Gemini AI bóc tách!"
                    } else if (activeTool == ReadingToolMode.HIGHLIGHT) {
                        "Mẹo: Chạm vào từng câu trong đoạn văn để bôi đen Highlight vàng từ khóa quan trọng."
                    } else {
                        "Mẹo: Chạm vào câu bất kỳ để viết Ghi chú (Note) ý chính hoặc từ đồng nghĩa."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSyntaxInspectMode) PracticeTealDark else InkMuted
                )

                HorizontalDivider(color = CardBorderSoft)

                // Interactive Paragraphs & Tappable Sentences
                paragraphs.forEach { (paragraphLabel, sentenceList) ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PracticeTeal
                            ) {
                                Text(
                                    text = "Đoạn $paragraphLabel",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        sentenceList.forEach { sentence ->
                            val isHighlighted = highlightedIds[sentence.id] == true
                            val existingNote = sentenceNotes[sentence.id].orEmpty()

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = when {
                                        isSyntaxInspectMode -> PracticeTealSurface
                                        isHighlighted -> HighlightYellow.copy(alpha = 0.85f)
                                        else -> Color.Transparent
                                    },
                                    border = when {
                                        isSyntaxInspectMode -> BorderStroke(1.dp, PracticeTeal.copy(alpha = 0.45f))
                                        isHighlighted -> BorderStroke(1.dp, HighlightYellowBorder)
                                        else -> null
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            if (isSyntaxInspectMode) {
                                                onInspectSentence(sentence.textEn)
                                            } else if (activeTool == ReadingToolMode.HIGHLIGHT) {
                                                highlightedIds[sentence.id] = !isHighlighted
                                            } else {
                                                editingNoteSentence = sentence
                                                noteDraft = existingNote
                                            }
                                        }
                                        .testTag("reading_sentence_${sentence.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = if (isSyntaxInspectMode || isHighlighted) 10.dp else 4.dp,
                                            vertical = 6.dp
                                        ),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (isSyntaxInspectMode) {
                                            Icon(
                                                imageVector = Icons.Rounded.ManageSearch,
                                                contentDescription = "Soi ngữ pháp câu này",
                                                tint = PracticeTealDark,
                                                modifier = Modifier
                                                    .padding(top = 2.dp)
                                                    .size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = sentence.textEn,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = PlusJakartaSansFontFamily,
                                                lineHeight = 22.sp
                                            ),
                                            color = InkPrimary
                                        )
                                    }
                                }

                                if (existingNote.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = GoldMasteryLight.copy(alpha = 0.75f),
                                        border = BorderStroke(1.dp, GoldMastery.copy(alpha = 0.45f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 10.dp)
                                            .clickable {
                                                editingNoteSentence = sentence
                                                noteDraft = existingNote
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.EditNote,
                                                contentDescription = null,
                                                tint = GoldOnContainer,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Text(
                                                text = "Ghi chú: $existingNote",
                                                style = MaterialTheme.typography.bodySmall,
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
    }

    val questionsPane: @Composable () -> Unit = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        // Headings Bank Reference Card for Matching Headings questions
        if (pack.headingsBank.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PracticeTealSurface,
                border = BorderStroke(1.dp, PracticeTeal.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "List of Headings (Dùng cho câu Matching Headings)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TealOnSurface
                    )
                    pack.headingsBank.forEach { heading ->
                        Text(
                            text = heading,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PlusJakartaSansFontFamily
                            ),
                            color = InkPrimary
                        )
                    }
                }
            }
        }

        // Questions List
        pack.questions.forEachIndexed { qIdx, question ->
            ReadingQuestionCard(
                question = question,
                userAnswer = userAnswers[question.id].orEmpty(),
                isChecked = isChecked,
                onAnswerChange = { value -> userAnswers[question.id] = value },
                onOpenHint = { onOpenHintForQuestion(qIdx) }
            )
        }

        // Check Answers / Reset CTA Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    isChecked = true
                    val correct = pack.questions.count { q ->
                        val ans = userAnswers[q.id].orEmpty().trim().lowercase()
                        q.acceptableAnswers.any { it.trim().lowercase() == ans }
                    }
                    onReadingChecked(correct, pack.questions.size)
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PracticeTeal,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(vertical = 13.dp, horizontal = 14.dp),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("reading_check_button")
            ) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.practice_reading_check),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            if (isChecked) {
                OutlinedButton(
                    onClick = {
                        userAnswers.clear()
                        isChecked = false
                    },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, PracticeTeal),
                    contentPadding = PaddingValues(vertical = 13.dp, horizontal = 12.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("reading_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = stringResource(R.string.practice_reading_reset),
                        tint = TealOnSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Keyword Mapping Table revealed after checking answers
        AnimatedVisibility(visible = isChecked) {
            val score = pack.questions.count { q ->
                val ans = userAnswers[q.id].orEmpty().trim().lowercase()
                q.acceptableAnswers.any { it.trim().lowercase() == ans }
            }
            KeywordMappingTableCard(
                scoreText = "Kết quả: $score / ${pack.questions.size} câu đúng",
                rows = pack.questions.map { it.keywordMapping }
            )
        }
        }
    }

    if (layoutInfo.useSideBySideSplit) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = layoutInfo.horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(modifier = Modifier.weight(1.05f)) { passagePane() }
            Box(modifier = Modifier.weight(0.95f)) { questionsPane() }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = layoutInfo.horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            passagePane()
            questionsPane()
        }
    }

    // Inline Note Editor Dialog
    editingNoteSentence?.let { sentence ->
        AlertDialog(
            onDismissRequest = { editingNoteSentence = null },
            containerColor = CardWhite,
            title = {
                Text(
                    text = "Ghi chú cho Đoạn ${sentence.paragraphLabel}",
                    style = MaterialTheme.typography.titleMedium,
                    color = InkPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "\"${sentence.textEn.take(110)}...\"",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = PlusJakartaSansFontFamily
                        ),
                        color = InkMuted
                    )
                    OutlinedTextField(
                        value = noteDraft,
                        onValueChange = { noteDraft = it },
                        placeholder = { Text("VD: Paraphrase của 'attainment gap'...") },
                        minLines = 2,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reading_note_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = noteDraft.trim()
                        if (trimmed.isEmpty()) sentenceNotes.remove(sentence.id)
                        else sentenceNotes[sentence.id] = trimmed
                        editingNoteSentence = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PracticeTeal)
                ) {
                    Text("Lưu ghi chú")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingNoteSentence = null }) {
                    Text("Đóng", color = InkSecondary)
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReadingQuestionCard(
    question: ReadingQuestion,
    userAnswer: String,
    isChecked: Boolean,
    onAnswerChange: (String) -> Unit,
    onOpenHint: () -> Unit
) {
    val isCorrect = isChecked && question.acceptableAnswers.any {
        it.trim().equals(userAnswer.trim(), ignoreCase = true)
    }
    val borderColor = when {
        !isChecked -> CardBorderSoft
        isCorrect -> EmeraldSuccess
        else -> RoseAlert
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = CardWhite,
        border = BorderStroke(if (isChecked) 1.5.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reading_question_${question.number}")
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PracticeTealLight
                    ) {
                        Text(
                            text = "Câu ${question.number} • ${question.type.badgeVi}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TealOnSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = GoldMasteryLight,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onOpenHint)
                        .testTag("reading_q_hint_${question.number}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.TipsAndUpdates,
                            contentDescription = null,
                            tint = GoldOnContainer,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Gợi ý 3 cấp",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldOnContainer
                        )
                    }
                }
            }

            Text(
                text = question.promptEn,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Medium
                ),
                color = InkPrimary
            )

            if (question.type == ReadingQuestionType.GAP_FILL) {
                OutlinedTextField(
                    value = userAnswer,
                    onValueChange = onAnswerChange,
                    singleLine = true,
                    placeholder = {
                        Text("Nhập tối đa 2 từ từ bài đọc...", style = MaterialTheme.typography.bodySmall)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PracticeTeal,
                        unfocusedBorderColor = CardBorderSoft
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reading_gap_input_${question.number}")
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    question.options.forEach { option ->
                        val selected = userAnswer.equals(option, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) PracticeTeal else SurfaceCanvas,
                            border = BorderStroke(
                                1.dp,
                                if (selected) PracticeTeal else CardBorderSoft
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onAnswerChange(option) }
                                .testTag("reading_opt_${question.number}_$option")
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (selected) Color.White else InkPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                            )
                        }
                    }
                }
            }

            if (isChecked) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.HighlightOff,
                        contentDescription = null,
                        tint = if (isCorrect) EmeraldSuccess else RoseAlert,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isCorrect) {
                            "Chính xác! Đáp án: ${question.correctAnswer}"
                        } else {
                            "Đáp án chuẩn: ${question.correctAnswer} (${question.keywordMapping.paragraphRef})"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isCorrect) EmeraldSuccess else RoseAlert
                    )
                }
            }
        }
    }
}

@Composable
fun KeywordMappingTableCard(
    scoreText: String,
    rows: List<KeywordMappingRow>,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = CardWhite,
        border = BorderStroke(1.5.dp, PracticeTeal.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("keyword_mapping_table")
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.TableChart,
                        contentDescription = null,
                        tint = TealOnSurface,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Bảng Đối Chiếu Từ Khóa (Keyword Mapping)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TealOnSurface
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = EmeraldSuccessLight
            ) {
                Text(
                    text = scoreText,
                    style = MaterialTheme.typography.labelLarge,
                    color = EmeraldOnContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }

            rows.forEach { row ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceCanvas,
                    border = BorderStroke(1.dp, CardBorderSoft)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Câu ${row.questionNumber}",
                                style = MaterialTheme.typography.labelMedium,
                                color = PracticeTealDark
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PracticeTealLight
                            ) {
                                Text(
                                    text = row.paragraphRef,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PracticeTealDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Từ khóa câu hỏi",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = InkMuted
                                )
                                Text(
                                    text = row.questionKeyword,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = InkPrimary
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Paraphrase trong bài",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PracticeTealDark
                                )
                                Text(
                                    text = row.passageParaphrase,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = PracticeTealDark
                                )
                            }
                        }

                        Text(
                            text = row.explanationVi,
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                }
            }
        }
    }
}
