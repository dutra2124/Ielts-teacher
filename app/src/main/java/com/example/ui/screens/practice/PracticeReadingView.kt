package com.example.ui.screens.practice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.BorderColor
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.HighlightOff
import androidx.compose.material.icons.rounded.ManageSearch
import androidx.compose.material.icons.rounded.MenuBook
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.KeywordMappingRow
import com.example.data.PassageSentence
import com.example.data.ReadingPracticePack
import com.example.data.ReadingQuestion
import com.example.data.ReadingQuestionType
import com.example.ui.components.CollapsibleQuestionAccordionCard
import com.example.ui.components.QuestionNavigatorBar
import com.example.ui.components.QuestionStatusItem
import com.example.ui.components.defaultPracticeNavigatorPalette
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
import kotlinx.coroutines.launch

private enum class ReadingToolMode(val shortLabelVi: String) {
    HIGHLIGHT("Highlight"),
    NOTE("Note")
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
    val layoutInfo = LocalAdaptiveLayoutInfo.current
    val scope = rememberCoroutineScope()

    var activeTool by remember(pack.moduleId) { mutableStateOf(ReadingToolMode.HIGHLIGHT) }
    val highlightedIds = remember(pack.moduleId) { mutableStateMapOf<String, Boolean>() }
    val sentenceNotes = remember(pack.moduleId) { mutableStateMapOf<String, String>() }
    var editingNoteSentence by remember { mutableStateOf<PassageSentence?>(null) }
    var noteDraft by remember { mutableStateOf("") }

    // Collapsible / resizable reading passage controls
    var isPassageCollapsed by remember(pack.moduleId) { mutableStateOf(false) }
    var passageSplitRatio by remember(pack.moduleId) { mutableFloatStateOf(0.46f) }
    var showHeadingsBank by remember(pack.moduleId) { mutableStateOf(false) }

    // Accordion + Live Question Status state
    val userAnswers = remember(pack.moduleId) { mutableStateMapOf<String, String>() }
    val flaggedIds = remember(pack.moduleId) { mutableStateMapOf<String, Boolean>() }
    var activeQuestionIndex by remember(pack.moduleId) { mutableIntStateOf(0) }
    var expandAllQuestions by remember(pack.moduleId) { mutableStateOf(false) }
    var isChecked by remember(pack.moduleId) { mutableStateOf(false) }

    val questionListState = rememberLazyListState()
    val hasHeadingsHeader = pack.headingsBank.isNotEmpty()
    val questionItemOffset = if (hasHeadingsHeader) 1 else 0

    val paragraphs = remember(pack.sentences) {
        pack.sentences.groupBy { it.paragraphLabel }.entries.toList()
    }

    val statusItems = pack.questions.map { q ->
        val ans = userAnswers[q.id].orEmpty()
        val correctFlag = if (isChecked) {
            q.acceptableAnswers.any { it.trim().equals(ans.trim(), ignoreCase = true) }
        } else {
            null
        }
        QuestionStatusItem(
            id = q.id,
            number = q.number,
            previewText = q.promptEn,
            answerSummary = ans.takeIf { it.isNotBlank() },
            isFlagged = flaggedIds[q.id] == true,
            isCorrect = correctFlag
        )
    }

    fun jumpToQuestion(idx: Int) {
        if (idx !in pack.questions.indices) return
        activeQuestionIndex = idx
        scope.launch {
            questionListState.animateScrollToItem(idx + questionItemOffset)
        }
    }

    val passageHeaderBar: @Composable () -> Unit = {
        Surface(
            color = CardWhite,
            border = BorderStroke(1.dp, CardBorderSoft),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MenuBook,
                    contentDescription = null,
                    tint = TealOnSurface,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = pack.titleEn,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = InkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Compact Highlight / Note tool chips when passage is visible
                if (!isPassageCollapsed) {
                    ReadingToolMode.entries.forEach { mode ->
                        val selected = !isSyntaxInspectMode && activeTool == mode
                        Surface(
                            shape = RoundedCornerShape(8.dp),
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
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { activeTool = mode }
                                .testTag("reading_tool_${mode.name.lowercase()}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = if (mode == ReadingToolMode.HIGHLIGHT) {
                                        Icons.Rounded.BorderColor
                                    } else {
                                        Icons.Rounded.StickyNote2
                                    },
                                    contentDescription = mode.shortLabelVi,
                                    tint = if (selected && mode == ReadingToolMode.HIGHLIGHT) {
                                        Color(0xFF854D0E)
                                    } else if (selected) {
                                        PracticeTealDark
                                    } else {
                                        InkSecondary
                                    },
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = mode.shortLabelVi,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                    color = if (selected) InkPrimary else InkSecondary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                // 1-tap Collapse / Expand Passage toggle
                if (!layoutInfo.useSideBySideSplit) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PracticeTealSurface,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isPassageCollapsed = !isPassageCollapsed }
                            .testTag("reading_toggle_passage")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = if (isPassageCollapsed) Icons.Rounded.ExpandMore else Icons.Rounded.ExpandLess,
                                contentDescription = if (isPassageCollapsed) "Mở bài đọc" else "Thu gọn bài đọc",
                                tint = TealOnSurface,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (isPassageCollapsed) "Mở bài" else "Thu gọn",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                ),
                                color = TealOnSurface,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }

    val passageBodyScrollable: @Composable (Modifier) -> Unit = { boxModifier ->
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, CardBorderSoft),
            modifier = boxModifier
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                paragraphs.forEach { (paragraphLabel, sentenceList) ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PracticeTeal
                        ) {
                            Text(
                                text = "Đoạn $paragraphLabel",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }

                        sentenceList.forEach { sentence ->
                            val isHighlighted = highlightedIds[sentence.id] == true
                            val existingNote = sentenceNotes[sentence.id].orEmpty()

                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
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
                                        .clip(RoundedCornerShape(10.dp))
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
                                            horizontal = if (isSyntaxInspectMode || isHighlighted) 8.dp else 2.dp,
                                            vertical = 4.dp
                                        ),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        if (isSyntaxInspectMode) {
                                            Icon(
                                                imageVector = Icons.Rounded.ManageSearch,
                                                contentDescription = "Soi ngữ pháp câu này",
                                                tint = PracticeTealDark,
                                                modifier = Modifier
                                                    .padding(top = 2.dp)
                                                    .size(15.dp)
                                            )
                                        }
                                        Text(
                                            text = sentence.textEn,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = PlusJakartaSansFontFamily,
                                                lineHeight = 21.sp
                                            ),
                                            color = InkPrimary
                                        )
                                    }
                                }

                                if (existingNote.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = GoldMasteryLight.copy(alpha = 0.75f),
                                        border = BorderStroke(1.dp, GoldMastery.copy(alpha = 0.45f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 8.dp)
                                            .clickable {
                                                editingNoteSentence = sentence
                                                noteDraft = existingNote
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.EditNote,
                                                contentDescription = null,
                                                tint = GoldOnContainer,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Note: $existingNote",
                                                style = MaterialTheme.typography.labelSmall,
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

    val questionsLazyPane: @Composable (Modifier) -> Unit = { listModifier ->
        LazyColumn(
            state = questionListState,
            modifier = listModifier,
            contentPadding = PaddingValues(top = 6.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Optional Collapsible Headings Bank for Matching Headings
            if (hasHeadingsHeader) {
                item(key = "headings_bank") {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = PracticeTealSurface,
                        border = BorderStroke(1.dp, PracticeTeal.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showHeadingsBank = !showHeadingsBank }
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "List of Headings (${pack.headingsBank.size})",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = TealOnSurface
                                )
                                Icon(
                                    imageVector = if (showHeadingsBank) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                    contentDescription = null,
                                    tint = TealOnSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            AnimatedVisibility(visible = showHeadingsBank) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                    }
                }
            }

            // Smart Collapsible Accordion Question Cards
            itemsIndexed(pack.questions, key = { _, q -> q.id }) { qIdx, question ->
                val userAns = userAnswers[question.id].orEmpty()
                val isFlagged = flaggedIds[question.id] == true
                val isExpanded = expandAllQuestions || activeQuestionIndex == qIdx
                val isCorrect = if (isChecked) {
                    question.acceptableAnswers.any { it.trim().equals(userAns.trim(), ignoreCase = true) }
                } else {
                    null
                }

                ReadingAccordionQuestionCard(
                    question = question,
                    userAnswer = userAns,
                    isExpanded = isExpanded,
                    isFlagged = isFlagged,
                    isChecked = isChecked,
                    isCorrect = isCorrect,
                    hasPrev = qIdx > 0,
                    hasNext = qIdx < pack.questions.lastIndex,
                    onToggleExpand = {
                        if (expandAllQuestions) {
                            expandAllQuestions = false
                            activeQuestionIndex = qIdx
                        } else {
                            activeQuestionIndex = if (activeQuestionIndex == qIdx) -1 else qIdx
                        }
                    },
                    onToggleFlag = { flaggedIds[question.id] = !isFlagged },
                    onAnswerChange = { value -> userAnswers[question.id] = value },
                    onOpenHint = { onOpenHintForQuestion(qIdx) },
                    onPrev = { jumpToQuestion(qIdx - 1) },
                    onNext = { jumpToQuestion(qIdx + 1) }
                )
            }

            // Check Answers / Reset CTA Row
            item(key = "reading_check_cta") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PracticeTeal,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 14.dp),
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
                                flaggedIds.clear()
                                isChecked = false
                                activeQuestionIndex = 0
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, PracticeTeal),
                            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 12.dp),
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
            }

            // Keyword Mapping Table revealed after checking answers
            if (isChecked) {
                item(key = "keyword_mapping") {
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
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (layoutInfo.useSideBySideSplit) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = layoutInfo.horizontalPadding, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.05f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    passageHeaderBar()
                    passageBodyScrollable(Modifier.weight(1f).fillMaxWidth())
                }
                questionsLazyPane(Modifier.weight(0.95f).fillMaxHeight())
            }
        } else {
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = layoutInfo.horizontalPadding)
                    .padding(top = 8.dp)
            ) {
                val density = LocalDensity.current
                val totalHeightPx = with(density) { maxHeight.toPx().coerceAtLeast(1f) }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    passageHeaderBar()

                    if (!isPassageCollapsed) {
                        passageBodyScrollable(
                            Modifier
                                .fillMaxWidth()
                                .weight(passageSplitRatio)
                        )

                        // Slim draggable resize bar between Passage and Questions
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(18.dp)
                                .clip(RoundedCornerShape(50))
                                .background(CardBorderSoft.copy(alpha = 0.55f))
                                .pointerInput(totalHeightPx) {
                                    detectVerticalDragGestures { change, dragAmount ->
                                        change.consume()
                                        passageSplitRatio =
                                            (passageSplitRatio + dragAmount / totalHeightPx).coerceIn(0.24f, 0.72f)
                                    }
                                }
                                .testTag("reading_split_drag_handle"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DragHandle,
                                contentDescription = "Kéo thay đổi chiều cao bài đọc",
                                tint = InkSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    questionsLazyPane(
                        Modifier
                            .fillMaxWidth()
                            .weight(if (isPassageCollapsed) 1f else 1f - passageSplitRatio)
                    )
                }
            }
        }

        // Sticky Live Question Status Bar + Overview Grid
        QuestionNavigatorBar(
            items = statusItems,
            activeIndex = activeQuestionIndex,
            onSelectQuestion = { idx ->
                if (expandAllQuestions) expandAllQuestions = false
                jumpToQuestion(idx)
            },
            palette = defaultPracticeNavigatorPalette(PracticeTeal),
            expandAll = expandAllQuestions,
            onToggleExpandAll = { expandAllQuestions = !expandAllQuestions },
            testTagPrefix = "practice_reading_nav"
        )
    }

    // Inline Note Editor Dialog
    editingNoteSentence?.let { sentence ->
        AlertDialog(
            onDismissRequest = { editingNoteSentence = null },
            containerColor = CardWhite,
            title = {
                Text(
                    text = "Ghi chú • Đoạn ${sentence.paragraphLabel}",
                    style = MaterialTheme.typography.titleMedium,
                    color = InkPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "\"${sentence.textEn.take(90)}...\"",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = PlusJakartaSansFontFamily
                        ),
                        color = InkMuted
                    )
                    OutlinedTextField(
                        value = noteDraft,
                        onValueChange = { noteDraft = it },
                        placeholder = { Text("Ghi chú từ đồng nghĩa / ý chính...") },
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
                    Text("Lưu")
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
private fun ReadingAccordionQuestionCard(
    question: ReadingQuestion,
    userAnswer: String,
    isExpanded: Boolean,
    isFlagged: Boolean,
    isChecked: Boolean,
    isCorrect: Boolean?,
    hasPrev: Boolean,
    hasNext: Boolean,
    onToggleExpand: () -> Unit,
    onToggleFlag: () -> Unit,
    onAnswerChange: (String) -> Unit,
    onOpenHint: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    CollapsibleQuestionAccordionCard(
        questionNumber = question.number,
        previewText = question.promptEn,
        answerSummary = userAnswer.takeIf { it.isNotBlank() },
        isExpanded = isExpanded,
        isFlagged = isFlagged,
        isCorrect = isCorrect,
        badgeLabel = question.type.badgeVi,
        accentColor = PracticeTeal,
        onToggleExpand = onToggleExpand,
        onToggleFlag = onToggleFlag,
        headerTrailingAction = {
            Surface(
                shape = RoundedCornerShape(50),
                color = GoldMasteryLight,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onOpenHint)
                    .testTag("reading_q_hint_${question.number}")
            ) {
                Icon(
                    imageVector = Icons.Rounded.TipsAndUpdates,
                    contentDescription = "Gợi ý câu ${question.number}",
                    tint = GoldOnContainer,
                    modifier = Modifier
                        .padding(5.dp)
                        .size(15.dp)
                )
            }
        },
        modifier = Modifier.testTag("reading_question_${question.number}")
    ) {
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
                    Text("Nhập tối đa 2 từ...", style = MaterialTheme.typography.bodySmall)
                },
                shape = RoundedCornerShape(12.dp),
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
                        shape = RoundedCornerShape(11.dp),
                        color = if (selected) PracticeTeal else SurfaceCanvas,
                        border = BorderStroke(
                            1.dp,
                            if (selected) PracticeTeal else CardBorderSoft
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(11.dp))
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
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp)
                        )
                    }
                }
            }
        }

        if (isChecked) {
            val ok = isCorrect == true
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (ok) Icons.Rounded.CheckCircle else Icons.Rounded.HighlightOff,
                    contentDescription = null,
                    tint = if (ok) EmeraldSuccess else RoseAlert,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (ok) {
                        "Đúng • ${question.correctAnswer}"
                    } else {
                        "Đáp án: ${question.correctAnswer} (${question.keywordMapping.paragraphRef})"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (ok) EmeraldSuccess else RoseAlert
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
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Câu trước", style = MaterialTheme.typography.labelMedium)
            }
            TextButton(
                onClick = onNext,
                enabled = hasNext,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Câu tiếp", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.width(3.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(15.dp))
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
        shape = RoundedCornerShape(20.dp),
        color = CardWhite,
        border = BorderStroke(1.5.dp, PracticeTeal.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("keyword_mapping_table")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
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
                    Icon(
                        imageVector = Icons.Rounded.TableChart,
                        contentDescription = null,
                        tint = TealOnSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Keyword Mapping",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TealOnSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = EmeraldSuccessLight
                ) {
                    Text(
                        text = scoreText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldOnContainer,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            rows.forEach { row ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceCanvas,
                    border = BorderStroke(1.dp, CardBorderSoft)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
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
                                Text("Từ khóa", style = MaterialTheme.typography.labelSmall, color = InkMuted)
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
                                Text("Paraphrase", style = MaterialTheme.typography.labelSmall, color = PracticeTealDark)
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
