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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.ManageSearch
import androidx.compose.material.icons.rounded.PlayArrow
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
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.ListeningPracticePack
import com.example.speech.ApexTtsController
import com.example.speech.EnglishAccent
import com.example.ui.components.AnimatedAudioWaveform
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardWhite
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.GoldMasteryLight
import com.example.ui.theme.GoldOnContainer
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.PracticeTeal
import com.example.ui.theme.PracticeTealDark
import com.example.ui.theme.PracticeTealLight
import com.example.ui.theme.PracticeTealSurface
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseDiffLight
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.TealOnSurface
import java.util.Locale

private enum class ListeningMode(val labelVi: String) {
    COMPREHENSION("Nghe hiểu IELTS"),
    SMART_DICTATION("Smart Dictation")
}

private val SpeedOptions = listOf(0.85f, 1.0f, 1.15f, 1.25f)

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
    var mode by remember(pack.moduleId) { mutableStateOf(ListeningMode.COMPREHENSION) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var isPlaying by remember(pack.moduleId) { mutableStateOf(false) }
    var activeSegmentIndex by remember(pack.moduleId) { mutableIntStateOf(0) }
    var showTranscript by remember(pack.moduleId) { mutableStateOf(true) }

    // Comprehension quiz state
    val selectedAnswers = remember(pack.moduleId) { mutableStateMapOf<String, String>() }
    var quizChecked by remember(pack.moduleId) { mutableStateOf(false) }

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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mode Switcher: Nghe hiểu IELTS vs Smart Dictation
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, CardBorderSoft)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ListeningMode.entries.forEach { item ->
                    val active = mode == item
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (active) PracticeTeal else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                ttsController.stop()
                                isPlaying = false
                                mode = item
                            }
                            .testTag("listening_mode_${item.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
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
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.labelVi,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (active) Color.White else InkSecondary
                            )
                        }
                    }
                }
            }
        }

        // Simulated Audio Player Card (powered by Android TextToSpeech with speed controls)
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, CardBorderSoft),
            modifier = Modifier.testTag("listening_audio_player")
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
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PracticeTealLight
                    ) {
                        Text(
                            text = pack.sectionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = PracticeTealDark,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                    AnimatedAudioWaveform(
                        isActive = isPlaying,
                        width = 30.dp,
                        height = 16.dp,
                        primaryColor = PracticeTeal,
                        secondaryColor = PracticeTealDark
                    )
                }

                Text(
                    text = pack.titleEn,
                    style = MaterialTheme.typography.titleMedium.copy(
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

                // Play / Stop + Segment Progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PracticeTeal,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (isPlaying) {
                                    ttsController.stop()
                                    isPlaying = false
                                } else {
                                    isPlaying = true
                                    ttsController.speakSequence(
                                        lines = pack.segments.map { it.textEn },
                                        startIndex = activeSegmentIndex,
                                        accent = EnglishAccent.UK,
                                        speechRate = playbackSpeed,
                                        onLineStarted = { idx -> activeSegmentIndex = idx },
                                        onComplete = {
                                            isPlaying = false
                                            activeSegmentIndex = 0
                                        }
                                    )
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
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isPlaying) {
                                    "Đang đọc: ${pack.segments.getOrNull(activeSegmentIndex)?.speaker ?: ""}"
                                } else {
                                    "Audio học thuật (SpeechSynthesis UK)"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = PracticeTealDark
                            )
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
                                .height(7.dp)
                                .clip(RoundedCornerShape(50))
                        )
                    }
                }

                // Speed Controls: 0.85x, 1.0x, 1.15x, 1.25x
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tốc độ phát:",
                        style = MaterialTheme.typography.labelMedium,
                        color = InkSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SpeedOptions.forEach { speed ->
                            val selected = playbackSpeed == speed
                            val label = String.format(Locale.US, "%.2fx", speed).replace(".00x", ".0x")
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (selected) PracticeTeal else SurfaceCanvas,
                                border = BorderStroke(1.dp, if (selected) PracticeTeal else CardBorderSoft),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        playbackSpeed = speed
                                        if (isPlaying) {
                                            ttsController.speakSequence(
                                                lines = pack.segments.map { it.textEn },
                                                startIndex = activeSegmentIndex,
                                                accent = EnglishAccent.UK,
                                                speechRate = speed,
                                                onLineStarted = { idx -> activeSegmentIndex = idx },
                                                onComplete = { isPlaying = false }
                                            )
                                        }
                                    }
                                    .testTag("listening_speed_$label")
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (selected) Color.White else InkPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (mode == ListeningMode.COMPREHENSION) {
            // Interactive Transcript Card (Supports Syntax Inspect Mode!)
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, CardBorderSoft)
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
                            .clickable { showTranscript = !showTranscript },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Subtitles,
                                contentDescription = null,
                                tint = PracticeTealDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isSyntaxInspectMode) {
                                    "Transcript (Chạm câu để Soi Ngữ pháp)"
                                } else {
                                    "Transcript đối chiếu & Soi cấu trúc"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = PracticeTealDark
                            )
                        }
                        Text(
                            text = if (showTranscript) "Thu gọn" else "Mở rộng",
                            style = MaterialTheme.typography.labelMedium,
                            color = PracticeTeal
                        )
                    }

                    AnimatedVisibility(visible = showTranscript) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            pack.segments.forEachIndexed { idx, segment ->
                                val isCurrent = isPlaying && idx == activeSegmentIndex
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = when {
                                        isSyntaxInspectMode -> PracticeTealSurface
                                        isCurrent -> PracticeTealLight.copy(alpha = 0.7f)
                                        else -> SurfaceCanvas
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        when {
                                            isSyntaxInspectMode -> PracticeTeal.copy(alpha = 0.5f)
                                            isCurrent -> PracticeTeal
                                            else -> CardBorderSoft
                                        }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            if (isSyntaxInspectMode) {
                                                onInspectSentence(segment.textEn)
                                            } else {
                                                activeSegmentIndex = idx
                                                isPlaying = true
                                                ttsController.speakSequence(
                                                    lines = pack.segments.map { it.textEn },
                                                    startIndex = idx,
                                                    accent = EnglishAccent.UK,
                                                    speechRate = playbackSpeed,
                                                    onLineStarted = { i -> activeSegmentIndex = i },
                                                    onComplete = { isPlaying = false }
                                                )
                                            }
                                        }
                                        .testTag("listening_transcript_seg_$idx")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(11.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = segment.speaker,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = PracticeTealDark
                                            )
                                            if (isSyntaxInspectMode) {
                                                Icon(
                                                    imageVector = Icons.Rounded.ManageSearch,
                                                    contentDescription = "Soi ngữ pháp",
                                                    tint = PracticeTealDark,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = segment.textEn,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = PlusJakartaSansFontFamily,
                                                lineHeight = 20.sp
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

            // Listening Comprehension Questions
            pack.questions.forEachIndexed { qIdx, question ->
                val chosen = selectedAnswers[question.id].orEmpty()
                val isCorrect = quizChecked && chosen == question.correctAnswer
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = CardWhite,
                    border = BorderStroke(
                        if (quizChecked) 1.5.dp else 1.dp,
                        when {
                            !quizChecked -> CardBorderSoft
                            isCorrect -> EmeraldSuccess
                            else -> RoseAlert
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
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
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PracticeTealLight
                            ) {
                                Text(
                                    text = "Listening Câu ${question.number}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PracticeTealDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = GoldMasteryLight,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { onOpenHintForQuestion(qIdx) }
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
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = InkPrimary
                        )

                        question.options.forEach { opt ->
                            val selected = chosen == opt
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) PracticeTealSurface else SurfaceCanvas,
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) PracticeTeal else CardBorderSoft
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedAnswers[question.id] = opt }
                            ) {
                                Text(
                                    text = opt,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (selected) TealOnSurface else InkPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    quizChecked = true
                    val score = pack.questions.count { selectedAnswers[it.id] == it.correctAnswer }
                    onListeningCompleted("Hoàn thành bài Nghe hiểu ($score/${pack.questions.size} đúng)!")
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PracticeTeal),
                contentPadding = PaddingValues(vertical = 13.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("listening_check_button")
            ) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kiểm tra đáp án Nghe hiểu", style = MaterialTheme.typography.labelLarge)
            }

            AnimatedVisibility(visible = quizChecked) {
                val score = pack.questions.count { selectedAnswers[it.id] == it.correctAnswer }
                KeywordMappingTableCard(
                    scoreText = "Kết quả Nghe hiểu: $score / ${pack.questions.size} câu đúng",
                    rows = pack.questions.map { it.keywordMapping }
                )
            }
        } else {
            // SMART DICTATION MODE (Nghe chép chính tả từng câu ngắn)
            val currentDictation = dictationSegments[dictationIndex.coerceIn(0, dictationSegments.lastIndex)]
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = CardWhite,
                border = BorderStroke(1.5.dp, PracticeTeal.copy(alpha = 0.35f)),
                modifier = Modifier.testTag("smart_dictation_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Smart Dictation • Câu ${dictationIndex + 1} / ${dictationSegments.size}",
                            style = MaterialTheme.typography.titleSmall,
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
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }
                    }

                    currentDictation.dictationPromptVi?.let { tip ->
                        Text(
                            text = "Mẹo bắt âm: $tip",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
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
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PracticeTeal),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("dictation_play_sentence")
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nghe câu (${String.format(Locale.US, "%.2fx", playbackSpeed).replace(".00x", ".0x")})")
                        }

                        OutlinedButton(
                            onClick = { showDictationAnswer = !showDictationAnswer },
                            shape = RoundedCornerShape(14.dp),
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
                            Text("Nghe và chép lại chính xác từng từ tiếng Anh bạn nghe được...")
                        },
                        shape = RoundedCornerShape(16.dp),
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
                                onListeningCompleted("Đã hoàn thành chấm Smart Dictation câu ${dictationIndex + 1}!")
                            },
                            enabled = dictationInput.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PracticeTeal),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("dictation_check_button")
                        ) {
                            Text("Chấm chính tả từng từ")
                        }

                        OutlinedButton(
                            onClick = {
                                dictationIndex = (dictationIndex + 1) % dictationSegments.size
                            },
                            shape = RoundedCornerShape(14.dp),
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
                            shape = RoundedCornerShape(14.dp),
                            color = PracticeTealSurface,
                            border = BorderStroke(1.dp, PracticeTeal.copy(alpha = 0.30f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Câu gốc chuẩn:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TealOnSurface
                                )
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
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Rounded.ManageSearch, contentDescription = null, tint = TealOnSurface, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("Soi ngữ pháp câu này", style = MaterialTheme.typography.labelSmall, color = TealOnSurface)
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
                            shape = RoundedCornerShape(16.dp),
                            color = SurfaceCanvas,
                            border = BorderStroke(1.dp, CardBorderSoft)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Độ chính xác Dictation: $accuracy% ($matchedCount/${expectedWords.size} từ)",
                                    style = MaterialTheme.typography.titleSmall,
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
                                            shape = RoundedCornerShape(8.dp),
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
