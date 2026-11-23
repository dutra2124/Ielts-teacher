package com.example.ui.screens.practice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.HighlightOff
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.ManageSearch
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ListeningPracticePack
import com.example.speech.ApexTtsController
import com.example.speech.EnglishAccent
import com.example.ui.components.AnimatedAudioWaveform
import com.example.ui.components.AudioAbRepeatControlBar
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
import com.example.ui.theme.RoseDiffLight
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.TealOnSurface
import kotlinx.coroutines.launch
import java.util.Locale

private enum class ListeningMode(val labelVi: String) {
    COMPREHENSION("Nghe hiểu"),
    SMART_DICTATION("Dictation")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PracticeListeningView(
    pack: ListeningPracticePack,
    ttsController: ApexTtsController,
    isSyntaxInspectMode: Boolean,
    onInspectSentence: (String) -> Unit,
    onOpenHintForQuestion: (Int) -> Unit,
    onListeningCompleted: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val layoutInfo = LocalAdaptiveLayoutInfo.current
    val scope = rememberCoroutineScope()

    var mode by remember(pack.moduleId) { mutableStateOf(ListeningMode.COMPREHENSION) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var isPlaying by remember(pack.moduleId) { mutableStateOf(false) }
    var activeSegmentIndex by remember(pack.moduleId) { mutableIntStateOf(0) }
    var loopPointA by remember(pack.moduleId) { mutableStateOf<Int?>(null) }
    var loopPointB by remember(pack.moduleId) { mutableStateOf<Int?>(null) }
    var showTranscript by remember(pack.moduleId) { mutableStateOf(false) }

    val isAbLoopActive = loopPointA != null && loopPointB != null && loopPointA!! <= loopPointB!!

    fun startSequenceAt(
        startIdx: Int,
        speed: Float = playbackSpeed,
        pointA: Int? = loopPointA,
        pointB: Int? = loopPointB
    ) {
        if (pack.segments.isEmpty()) return
        val clampedStart = startIdx.coerceIn(0, pack.segments.lastIndex)
        isPlaying = true
        ttsController.speakSequence(
            lines = pack.segments.map { it.textEn },
            startIndex = clampedStart,
            accent = EnglishAccent.UK,
            speechRate = speed,
            loopStartIndex = pointA,
            loopEndIndex = pointB,
            onLineStarted = { idx -> activeSegmentIndex = idx },
            onComplete = {
                isPlaying = false
                activeSegmentIndex = 0
            }
        )
    }

    // Auto-open transcript when Syntax Inspect mode is enabled
    LaunchedEffect(isSyntaxInspectMode) {
        if (isSyntaxInspectMode) showTranscript = true
    }

    // Comprehension accordion & navigator state
    val selectedAnswers = remember(pack.moduleId) { mutableStateMapOf<String, String>() }
    val flaggedIds = remember(pack.moduleId) { mutableStateMapOf<String, Boolean>() }
    var activeQuestionIndex by remember(pack.moduleId) { mutableIntStateOf(0) }
    var expandAllQuestions by remember(pack.moduleId) { mutableStateOf(false) }
    var quizChecked by remember(pack.moduleId) { mutableStateOf(false) }
    val questionListState = rememberLazyListState()

    // Smart Dictation state
    val dictationSegments = remember(pack) {
        pack.segments.filter { it.id in pack.dictationSentenceIds }
            .ifEmpty { pack.segments.take(3) }
    }
    var dictationIndex by remember(pack.moduleId) { mutableIntStateOf(0) }
    var dictationInput by remember(pack.moduleId, dictationIndex) { mutableStateOf("") }
    var dictationChecked by remember(pack.moduleId, dictationIndex) { mutableStateOf(false) }
    var showDictationAnswer by remember(pack.moduleId, dictationIndex) { mutableStateOf(false) }

    DisposableEffect(pack.moduleId) {
        onDispose {
            ttsController.stop()
        }
    }

    val statusItems = pack.questions.map { q ->
        val ans = selectedAnswers[q.id].orEmpty()
        val correctFlag = if (quizChecked) ans == q.correctAnswer else null
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
            // +1 because transcript card sits at index 0 inside LazyColumn
            questionListState.animateScrollToItem(idx + 1)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Compact Top Controls: Mode Switcher + Slim Audio Player Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = layoutInfo.horizontalPadding)
                .padding(top = 8.dp, bottom = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Slim Mode Switcher
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, CardBorderSoft)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ListeningMode.entries.forEach { item ->
                        val active = mode == item
                        Surface(
                            shape = RoundedCornerShape(11.dp),
                            color = if (active) PracticeTeal else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(11.dp))
                                .clickable {
                                    ttsController.stop()
                                    isPlaying = false
                                    mode = item
                                }
                                .testTag("listening_mode_${item.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (item == ListeningMode.COMPREHENSION) {
                                        Icons.Rounded.Headphones
                                    } else {
                                        Icons.Rounded.Keyboard
                                    },
                                    contentDescription = null,
                                    tint = if (active) Color.White else InkSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = item.labelVi,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (active) Color.White else InkSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Compact Audio Player Card (1 concise layout, no verbose subtitle)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, CardBorderSoft),
                modifier = Modifier.testTag("listening_audio_player")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PracticeTeal,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .clickable {
                                    if (isPlaying) {
                                        ttsController.stop()
                                        isPlaying = false
                                    } else {
                                        val startIdx = if (isAbLoopActive && loopPointA != null &&
                                            (activeSegmentIndex < loopPointA!! || activeSegmentIndex > loopPointB!!)
                                        ) {
                                            loopPointA!!
                                        } else {
                                            activeSegmentIndex
                                        }
                                        startSequenceAt(startIdx)
                                    }
                                }
                                .testTag("listening_play_toggle")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                    contentDescription = if (isPlaying) {
                                        stringResource(R.string.practice_listening_stop)
                                    } else {
                                        stringResource(R.string.practice_listening_play)
                                    },
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${activeSegmentIndex + 1}/${pack.segments.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = InkMuted
                                )
                            }
                            LinearProgressIndicator(
                                progress = {
                                    (activeSegmentIndex + 1).toFloat() / pack.segments.size.coerceAtLeast(1)
                                },
                                color = PracticeTeal,
                                trackColor = PracticeTealLight,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50))
                            )
                        }

                        AnimatedAudioWaveform(
                            isActive = isPlaying,
                            width = 24.dp,
                            height = 14.dp,
                            primaryColor = PracticeTeal,
                            secondaryColor = PracticeTealDark
                        )
                    }

                    // Playback Speed (0.75x, 1.0x, 1.25x) + A–B Repeat Loop Control Bar
                    AudioAbRepeatControlBar(
                        playbackSpeed = playbackSpeed,
                        onSelectSpeed = { speed ->
                            playbackSpeed = speed
                            if (isPlaying) {
                                startSequenceAt(activeSegmentIndex, speed = speed)
                            }
                        },
                        pointALabel = loopPointA?.let { "Đoạn ${it + 1}" },
                        pointBLabel = loopPointB?.let { "Đoạn ${it + 1}" },
                        isLoopActive = isAbLoopActive,
                        onSetPointA = {
                            val newA = activeSegmentIndex
                            val newB = loopPointB?.takeIf { it >= newA }
                            loopPointA = newA
                            loopPointB = newB
                            ttsController.updateSequenceLoop(newA, newB)
                        },
                        onSetPointB = {
                            val anchorA = loopPointA ?: 0
                            val cur = activeSegmentIndex
                            val (newA, newB) = if (cur < anchorA) cur to anchorA else anchorA to cur
                            loopPointA = newA
                            loopPointB = newB
                            if (isPlaying) {
                                ttsController.updateSequenceLoop(newA, newB)
                            } else {
                                startSequenceAt(newA, pointA = newA, pointB = newB)
                            }
                        },
                        onClearLoop = {
                            loopPointA = null
                            loopPointB = null
                            ttsController.updateSequenceLoop(null, null)
                        },
                        accentColor = PracticeTeal,
                        testTagPrefix = "listening"
                    )
                }
            }
        }

        if (mode == ListeningMode.COMPREHENSION) {
            // Scrollable Transcript + Collapsible Accordion Questions
            LazyColumn(
                state = questionListState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = layoutInfo.horizontalPadding),
                contentPadding = PaddingValues(top = 2.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Collapsible Transcript Card (item 0)
                item(key = "listening_transcript") {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CardWhite,
                        border = BorderStroke(1.dp, CardBorderSoft)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showTranscript = !showTranscript },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Subtitles,
                                        contentDescription = null,
                                        tint = PracticeTealDark,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Text(
                                        text = if (isSyntaxInspectMode) "Transcript (Chạm để soi)" else "Transcript (${pack.segments.size} đoạn)",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = PracticeTealDark
                                    )
                                }
                                Icon(
                                    imageVector = if (showTranscript) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                    contentDescription = if (showTranscript) "Thu gọn Transcript" else "Mở Transcript",
                                    tint = PracticeTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            AnimatedVisibility(visible = showTranscript) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    pack.segments.forEachIndexed { idx, segment ->
                                        val isCurrent = isPlaying && idx == activeSegmentIndex
                                        val inAbRange = isAbLoopActive && idx in loopPointA!!..loopPointB!!
                                        val isPointA = loopPointA == idx
                                        val isPointB = loopPointB == idx
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = when {
                                                isSyntaxInspectMode -> PracticeTealSurface
                                                isCurrent -> PracticeTealLight.copy(alpha = 0.7f)
                                                inAbRange -> GoldMasteryLight.copy(alpha = 0.45f)
                                                else -> SurfaceCanvas
                                            },
                                            border = BorderStroke(
                                                1.dp,
                                                when {
                                                    isSyntaxInspectMode -> PracticeTeal.copy(alpha = 0.5f)
                                                    isCurrent -> PracticeTeal
                                                    inAbRange || isPointA -> GoldMastery
                                                    else -> CardBorderSoft
                                                }
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable {
                                                    if (isSyntaxInspectMode) {
                                                        onInspectSentence(segment.textEn)
                                                    } else {
                                                        activeSegmentIndex = idx
                                                        startSequenceAt(idx)
                                                    }
                                                }
                                                .testTag("listening_transcript_seg_$idx")
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(9.dp),
                                                verticalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(
                                                            text = "#${idx + 1} • ${segment.speaker}",
                                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                            color = PracticeTealDark
                                                        )
                                                        if (isPointA || isPointB || inAbRange) {
                                                            val abTag = when {
                                                                isPointA && isPointB -> "A–B"
                                                                isPointA -> "Điểm A"
                                                                isPointB -> "Điểm B"
                                                                else -> "Lặp A–B"
                                                            }
                                                            Surface(
                                                                shape = RoundedCornerShape(50),
                                                                color = GoldMasteryLight
                                                            ) {
                                                                Text(
                                                                    text = abTag,
                                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                                        fontWeight = FontWeight.Bold,
                                                                        fontSize = 9.5.sp
                                                                    ),
                                                                    color = GoldOnContainer,
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                    if (isSyntaxInspectMode) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.ManageSearch,
                                                            contentDescription = "Soi ngữ pháp",
                                                            tint = PracticeTealDark,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = segment.textEn,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = PlusJakartaSansFontFamily,
                                                        lineHeight = 19.sp
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

                // Smart Collapsible Accordion Question Cards
                itemsIndexed(pack.questions, key = { _, q -> q.id }) { qIdx, question ->
                    val chosen = selectedAnswers[question.id].orEmpty()
                    val isFlagged = flaggedIds[question.id] == true
                    val isExpanded = expandAllQuestions || activeQuestionIndex == qIdx
                    val isCorrect = if (quizChecked) chosen == question.correctAnswer else null

                    CollapsibleQuestionAccordionCard(
                        questionNumber = question.number,
                        previewText = question.promptEn,
                        answerSummary = chosen.takeIf { it.isNotBlank() },
                        isExpanded = isExpanded,
                        isFlagged = isFlagged,
                        isCorrect = isCorrect,
                        badgeLabel = "Listening",
                        accentColor = PracticeTeal,
                        onToggleExpand = {
                            if (expandAllQuestions) {
                                expandAllQuestions = false
                                activeQuestionIndex = qIdx
                            } else {
                                activeQuestionIndex = if (activeQuestionIndex == qIdx) -1 else qIdx
                            }
                        },
                        onToggleFlag = { flaggedIds[question.id] = !isFlagged },
                        headerTrailingAction = {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = GoldMasteryLight,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { onOpenHintForQuestion(qIdx) }
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
                        modifier = Modifier.testTag("listening_question_${question.number}")
                    ) {
                        Text(
                            text = question.promptEn,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = InkPrimary
                        )

                        question.options.forEach { opt ->
                            val selected = chosen == opt
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) PracticeTealSurface else SurfaceCanvas,
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) PracticeTeal else CardBorderSoft
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedAnswers[question.id] = opt }
                            ) {
                                Text(
                                    text = opt,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (selected) TealOnSurface else InkPrimary,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp)
                                )
                            }
                        }

                        if (quizChecked) {
                            val ok = isCorrect == true
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = if (ok) Icons.Rounded.CheckCircle else Icons.Rounded.HighlightOff,
                                    contentDescription = null,
                                    tint = if (ok) EmeraldSuccess else RoseAlert,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (ok) "Chính xác!" else "Đáp án: ${question.correctAnswer}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (ok) EmeraldSuccess else RoseAlert
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(
                                onClick = { jumpToQuestion(qIdx - 1) },
                                enabled = qIdx > 0,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Câu trước", style = MaterialTheme.typography.labelMedium)
                            }
                            TextButton(
                                onClick = { jumpToQuestion(qIdx + 1) },
                                enabled = qIdx < pack.questions.lastIndex,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Câu tiếp", style = MaterialTheme.typography.labelMedium)
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(15.dp))
                            }
                        }
                    }
                }

                // Check / Reset CTA
                item(key = "listening_check_row") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                quizChecked = true
                                val score = pack.questions.count { selectedAnswers[it.id] == it.correctAnswer }
                                onListeningCompleted("Hoàn thành bài Nghe hiểu ($score/${pack.questions.size} đúng)!")
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PracticeTeal),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("listening_check_button")
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kiểm tra đáp án", style = MaterialTheme.typography.labelLarge)
                        }

                        if (quizChecked) {
                            OutlinedButton(
                                onClick = {
                                    selectedAnswers.clear()
                                    flaggedIds.clear()
                                    quizChecked = false
                                    activeQuestionIndex = 0
                                },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, PracticeTeal),
                                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 12.dp),
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Rounded.Refresh, contentDescription = "Làm lại", tint = TealOnSurface, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                if (quizChecked) {
                    item(key = "listening_keyword_mapping") {
                        val score = pack.questions.count { selectedAnswers[it.id] == it.correctAnswer }
                        KeywordMappingTableCard(
                            scoreText = "$score / ${pack.questions.size} đúng",
                            rows = pack.questions.map { it.keywordMapping }
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
                testTagPrefix = "practice_listening_nav"
            )
        } else {
            // SMART DICTATION MODE (Scrollable full-height body)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = layoutInfo.horizontalPadding)
                    .padding(bottom = 20.dp)
            ) {
                val currentDictation = dictationSegments[dictationIndex.coerceIn(0, dictationSegments.lastIndex)]
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardWhite,
                    border = BorderStroke(1.5.dp, PracticeTeal.copy(alpha = 0.35f)),
                    modifier = Modifier.testTag("smart_dictation_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dictation • Câu ${dictationIndex + 1}/${dictationSegments.size}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TealOnSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = PracticeTealLight
                            ) {
                                Text(
                                    text = currentDictation.speaker,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TealOnSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    isPlaying = true
                                    ttsController.speak(
                                        text = currentDictation.textEn,
                                        accent = EnglishAccent.UK,
                                        speechRate = playbackSpeed
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PracticeTeal),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("dictation_play_sentence")
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("Nghe (${String.format(Locale.US, "%.2fx", playbackSpeed).replace(".00x", ".0x")})")
                            }

                            OutlinedButton(
                                onClick = { showDictationAnswer = !showDictationAnswer },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, PracticeTeal),
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Text(
                                    text = if (showDictationAnswer) "Ẩn gốc" else "Xem gốc",
                                    color = TealOnSurface
                                )
                            }
                        }

                        OutlinedTextField(
                            value = dictationInput,
                            onValueChange = {
                                dictationInput = it
                                dictationChecked = false
                            },
                            minLines = 3,
                            placeholder = {
                                Text("Chép lại câu tiếng Anh bạn nghe được...")
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PracticeTeal,
                                unfocusedBorderColor = CardBorderSoft
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dictation_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    dictationChecked = true
                                    onListeningCompleted("Đã chấm Smart Dictation câu ${dictationIndex + 1}!")
                                },
                                enabled = dictationInput.isNotBlank(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PracticeTeal),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("dictation_check_button")
                            ) {
                                Text("Chấm chính tả")
                            }

                            OutlinedButton(
                                onClick = {
                                    dictationIndex = (dictationIndex + 1) % dictationSegments.size
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, PracticeTeal),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("dictation_next_button")
                            ) {
                                Text("Câu tiếp", color = TealOnSurface)
                            }
                        }

                        if (showDictationAnswer) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PracticeTealSurface,
                                border = BorderStroke(1.dp, PracticeTeal.copy(alpha = 0.30f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = currentDictation.textEn,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = InkPrimary
                                    )
                                    OutlinedButton(
                                        onClick = { onInspectSentence(currentDictation.textEn) },
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(1.dp, PracticeTeal),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Rounded.ManageSearch, contentDescription = null, tint = TealOnSurface, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Soi ngữ pháp", style = MaterialTheme.typography.labelSmall, color = TealOnSurface)
                                    }
                                }
                            }
                        }

                        if (dictationChecked) {
                            val expectedWords = currentDictation.textEn
                                .split(Regex("\\s+"))
                                .filter { it.isNotBlank() }
                            val typedSet = dictationInput
                                .lowercase(Locale.US)
                                .replace(Regex("[^a-z0-9'\\-\\s]"), "")
                                .split(Regex("\\s+"))
                                .filter { it.isNotBlank() }
                                .toSet()
                            val matchedCount = expectedWords.count { word ->
                                val norm = word.lowercase(Locale.US).replace(Regex("[^a-z0-9'\\-]"), "")
                                norm in typedSet
                            }
                            val accuracy = ((matchedCount.toFloat() / expectedWords.size.coerceAtLeast(1)) * 100).toInt()

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SurfaceCanvas,
                                border = BorderStroke(1.dp, CardBorderSoft)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Chính xác: $accuracy% ($matchedCount/${expectedWords.size} từ)",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = if (accuracy >= 80) EmeraldSuccess else TealOnSurface
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        expectedWords.forEach { token ->
                                            val norm = token.lowercase(Locale.US).replace(Regex("[^a-z0-9'\\-]"), "")
                                            val matched = norm in typedSet
                                            Surface(
                                                shape = RoundedCornerShape(7.dp),
                                                color = if (matched) EmeraldSuccessLight else RoseDiffLight
                                            ) {
                                                Text(
                                                    text = token,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontFamily = PlusJakartaSansFontFamily,
                                                        fontWeight = FontWeight.SemiBold
                                                    ),
                                                    color = if (matched) EmeraldOnContainer else RoseAlert,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
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
    }
}
