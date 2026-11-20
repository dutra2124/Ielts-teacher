package com.example.ui.screens.learn

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.MiniGameMode
import com.example.data.TeacherMood
import com.example.data.VocabWordItem
import com.example.speech.ApexTtsController
import com.example.speech.EnglishAccent
import com.example.ui.components.AnimatedStreakFlame
import com.example.ui.components.AnimatedTeacher
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardSurfaceTinted
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanGlowLight
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.GoldMastery
import com.example.ui.theme.GoldMasteryLight
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.StreakBg
import com.example.ui.theme.StreakOrange
import com.example.ui.theme.SurfaceCanvas
import kotlinx.coroutines.delay

private enum class GamePhase {
    READY,
    PLAYING,
    FINISHED
}

private data class GameQuestion(
    val sourceWord: VocabWordItem,
    val promptTagVi: String,
    val promptText: String,
    val correctOption: String,
    val options: List<String>
)

@Composable
fun GameAndSrsSubTab(
    moduleWords: List<VocabWordItem>,
    srsReviewWords: List<VocabWordItem>,
    ttsController: ApexTtsController,
    onMissedWord: (String) -> Unit,
    onGameCompleted: (score: Int, total: Int, bestCombo: Int, missedCount: Int) -> Unit,
    onMarkSrsMastered: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(MiniGameMode.PARAPHRASING_SPRINT) }
    var phase by remember(moduleWords, selectedMode) { mutableStateOf(GamePhase.READY) }
    var questions by remember(moduleWords, selectedMode) { mutableStateOf<List<GameQuestion>>(emptyList()) }
    var questionIndex by remember { mutableIntStateOf(0) }
    var secondsLeft by remember { mutableIntStateOf(45) }
    var score by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var bestCombo by remember { mutableIntStateOf(0) }
    var missedWordIdsThisRound by remember { mutableStateOf<Set<String>>(emptySet()) }
    var lastChosenOption by remember { mutableStateOf<String?>(null) }

    // Countdown timer while PLAYING
    LaunchedEffect(phase, secondsLeft) {
        if (phase == GamePhase.PLAYING) {
            if (secondsLeft > 0) {
                delay(1_000L)
                secondsLeft -= 1
            } else {
                phase = GamePhase.FINISHED
                onGameCompleted(score, questions.size, bestCombo, missedWordIdsThisRound.size)
            }
        }
    }

    // Brief auto-advance after picking an option
    LaunchedEffect(lastChosenOption, questionIndex, phase) {
        if (phase == GamePhase.PLAYING && lastChosenOption != null) {
            delay(650L)
            lastChosenOption = null
            if (questionIndex + 1 < questions.size) {
                questionIndex += 1
            } else {
                phase = GamePhase.FINISHED
                onGameCompleted(score, questions.size, bestCombo, missedWordIdsThisRound.size)
            }
        }
    }

    val startNewRound: () -> Unit = {
        questions = buildQuestionsForMode(selectedMode, moduleWords)
        questionIndex = 0
        secondsLeft = 45
        score = 0
        combo = 0
        bestCombo = 0
        missedWordIdsThisRound = emptySet()
        lastChosenOption = null
        phase = GamePhase.PLAYING
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 3 Mini-Game Mode Selector Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MiniGameMode.entries.forEach { mode ->
                val isSelected = selectedMode == mode
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) ElectricIndigo else CardWhite,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) ElectricIndigo else CardBorderSoft
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            selectedMode = mode
                            phase = GamePhase.READY
                        }
                        .testTag("game_mode_${mode.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = mode.titleVi,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                            color = if (isSelected) Color.White else InkPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Main Interactive Game Arena Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, CardBorderSoft),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("mini_game_arena")
        ) {
            when (phase) {
                GamePhase.READY -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.linearGradient(listOf(ElectricIndigo, CyanGlow))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SportsEsports,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Text(
                            text = selectedMode.titleVi,
                            style = MaterialTheme.typography.titleLarge,
                            color = InkPrimary
                        )
                        Text(
                            text = "${selectedMode.subtitleVi} • Trả lời nhanh để giữ Combo Streak, từ sai sẽ tự động đưa vào hàng chờ SRS!",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )

                        Button(
                            onClick = startNewRound,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricIndigo,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 13.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("start_mini_game_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.game_start_btn),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                GamePhase.PLAYING -> {
                    val currentQ = questions.getOrNull(questionIndex)
                    if (currentQ != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Timer + Question Progress + Combo Streak Pill
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (secondsLeft <= 10) Color(0xFFFFF1F2) else SurfaceCanvas
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Schedule,
                                            contentDescription = null,
                                            tint = if (secondsLeft <= 10) RoseAlert else ElectricIndigo,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "${secondsLeft}s",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontFamily = PlusJakartaSansFontFamily,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = if (secondsLeft <= 10) RoseAlert else ElectricIndigo
                                        )
                                    }
                                }

                                Text(
                                    text = "Câu ${questionIndex + 1}/${questions.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = InkSecondary
                                )

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = StreakBg,
                                    border = BorderStroke(1.dp, StreakOrange.copy(alpha = 0.35f)),
                                    modifier = Modifier.testTag("game_combo_badge")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        AnimatedStreakFlame(size = 16.dp)
                                        Text(
                                            text = "Combo x$combo",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = StreakOrange
                                        )
                                    }
                                }
                            }

                            LinearProgressIndicator(
                                progress = { (questionIndex + 1).toFloat() / questions.size.coerceAtLeast(1) },
                                color = CyanGlow,
                                trackColor = SurfaceCanvas,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(50))
                            )

                            // Question Prompt Box
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = CardSurfaceTinted,
                                border = BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.16f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = currentQ.promptTagVi,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ElectricIndigo
                                    )
                                    Text(
                                        text = currentQ.promptText,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.5.sp
                                        ),
                                        color = InkPrimary
                                    )
                                }
                            }

                            // 4 Answer Options
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                currentQ.options.forEachIndexed { optIdx, option ->
                                    val isChosen = lastChosenOption == option
                                    val isCorrect = option == currentQ.correctOption
                                    val reveal = lastChosenOption != null

                                    val bgColor by animateColorAsState(
                                        targetValue = when {
                                            reveal && isCorrect -> EmeraldSuccessLight
                                            reveal && isChosen && !isCorrect -> Color(0xFFFFE4E6)
                                            else -> SurfaceCanvas
                                        },
                                        animationSpec = tween(180),
                                        label = "game_opt_bg_$optIdx"
                                    )
                                    val borderColor = when {
                                        reveal && isCorrect -> EmeraldSuccess
                                        reveal && isChosen && !isCorrect -> RoseAlert
                                        else -> CardBorderSoft
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = bgColor,
                                        border = BorderStroke(
                                            if (reveal && (isCorrect || isChosen)) 1.5.dp else 1.dp,
                                            borderColor
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable(enabled = lastChosenOption == null) {
                                                lastChosenOption = option
                                                if (isCorrect) {
                                                    score += 1
                                                    combo += 1
                                                    if (combo > bestCombo) bestCombo = combo
                                                } else {
                                                    combo = 0
                                                    missedWordIdsThisRound = missedWordIdsThisRound + currentQ.sourceWord.id
                                                    onMissedWord(currentQ.sourceWord.id)
                                                }
                                            }
                                            .minimumInteractiveComponentSize()
                                            .testTag("game_option_$optIdx")
                                    ) {
                                        Text(
                                            text = option,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = PlusJakartaSansFontFamily,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = InkPrimary,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                GamePhase.FINISHED -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnimatedTeacher(
                            mood = TeacherMood.CHEERING,
                            isSpeaking = true,
                            size = 86.dp
                        )
                        Text(
                            text = "Hoàn thành ${selectedMode.titleVi}!",
                            style = MaterialTheme.typography.titleLarge,
                            color = ElectricIndigo
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = EmeraldSuccessLight
                            ) {
                                Text(
                                    text = "Đúng $score/${questions.size}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = EmeraldSuccess,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = StreakBg
                            ) {
                                Text(
                                    text = "Max Combo x$bestCombo",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = StreakOrange,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }

                        Text(
                            text = if (missedWordIdsThisRound.isNotEmpty()) {
                                "Đã tự động lưu ${missedWordIdsThisRound.size} từ chưa đúng vào Hàng chờ SRS bên dưới và cập nhật tiến độ hôm nay!"
                            } else {
                                "Phản xạ xuất sắc chuẩn Band 8.5! Đã tự động đánh dấu hoàn thành \"1 Game phản xạ\" trên Tab Lộ trình."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )

                        Button(
                            onClick = startNewRound,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricIndigo,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("replay_mini_game_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Replay,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = stringResource(R.string.game_replay_btn))
                        }
                    }
                }
            }
        }

        // Persisted SRS Review Queue Section
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, CardBorderSoft),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("srs_review_queue_section")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = GoldMastery,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.srs_queue_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = InkPrimary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (srsReviewWords.isEmpty()) EmeraldSuccessLight else GoldMasteryLight
                    ) {
                        Text(
                            text = "${srsReviewWords.size} từ cần ôn",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (srsReviewWords.isEmpty()) EmeraldSuccess else GoldMastery,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                if (srsReviewWords.isEmpty()) {
                    Text(
                        text = stringResource(R.string.srs_queue_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted
                    )
                } else {
                    srsReviewWords.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SurfaceCanvas,
                            border = BorderStroke(1.dp, CardBorderSoft)
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
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { ttsController.speak(item.word, EnglishAccent.UK) }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                        contentDescription = "Nghe từ ${item.word}",
                                        tint = ElectricIndigo,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "${item.word} (${item.cefrLevel})",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = PlusJakartaSansFontFamily,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = ElectricIndigo
                                        )
                                        Text(
                                            text = item.meaningVi,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = InkSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = EmeraldSuccessLight,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onMarkSrsMastered(item.id) }
                                        .testTag("srs_master_${item.id}")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = EmeraldSuccess,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = stringResource(R.string.srs_mastered_btn),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = EmeraldSuccess
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

private fun buildQuestionsForMode(
    mode: MiniGameMode,
    words: List<VocabWordItem>
): List<GameQuestion> {
    if (words.isEmpty()) return emptyList()
    val pool = words.shuffled().take(5)
    return pool.map { word ->
        val distractors = words
            .filter { it.id != word.id }
            .shuffled()
            .take(3)

        when (mode) {
            MiniGameMode.PARAPHRASING_SPRINT -> {
                val options = (distractors.map { it.word } + word.word).shuffled()
                GameQuestion(
                    sourceWord = word,
                    promptTagVi = "Nâng cấp cụm Band 6.0 sau lên C1–C2:",
                    promptText = "“${word.band6ParaphraseEn}”",
                    correctOption = word.word,
                    options = options
                )
            }

            MiniGameMode.COLLOCATION_MATCH -> {
                val options = (distractors.map { it.collocationPartnerEn } + word.collocationPartnerEn)
                    .distinct()
                    .shuffled()
                GameQuestion(
                    sourceWord = word,
                    promptTagVi = "Chọn mảnh ghép Collocation Band 8.5 chuẩn:",
                    promptText = word.collocationStemEn,
                    correctOption = word.collocationPartnerEn,
                    options = options
                )
            }

            MiniGameMode.CONTEXT_CLOZE -> {
                val options = (distractors.map { it.word } + word.word).shuffled()
                GameQuestion(
                    sourceWord = word,
                    promptTagVi = "Chọn từ C1–C2 điền vào chỗ trống ngữ cảnh:",
                    promptText = word.clozeSentenceEn,
                    correctOption = word.word,
                    options = options
                )
            }
        }
    }
}
