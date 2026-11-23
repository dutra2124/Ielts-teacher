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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.runtime.mutableStateMapOf
import com.example.R
import com.example.data.MiniGameMode
import com.example.data.Sm2CardState
import com.example.data.Sm2RecallQuality
import com.example.data.Sm2SrsEngine
import com.example.data.StreakManager
import com.example.data.TeacherMood
import com.example.data.VocabWordItem
import com.example.speech.ApexTtsController
import com.example.speech.EnglishAccent
import com.example.ui.components.AnimatedStreakFlame
import com.example.ui.components.AnimatedTeacher
import com.example.ui.components.tactilePress
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardSurfaceTinted
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanGlowLight
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.GoldMastery
import com.example.ui.theme.GoldMasteryLight
import com.example.ui.theme.GoldOnContainer
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseDiffLight
import com.example.ui.theme.StreakBg
import com.example.ui.theme.StreakOrange
import com.example.ui.theme.SurfaceCanvas
import kotlinx.coroutines.delay
import java.util.Locale

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
    srsSchedules: Map<String, Sm2CardState> = emptyMap(),
    ttsController: ApexTtsController,
    onMissedWord: (String) -> Unit,
    onWordCorrect: (String) -> Unit = {},
    onGameCompleted: (score: Int, total: Int, bestCombo: Int, missedCount: Int) -> Unit,
    onMarkSrsMastered: (String) -> Unit,
    onReviewSrsSm2: (String, Sm2RecallQuality) -> Unit = { _, _ -> },
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
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = InkPrimary
                        )
                        Text(
                            text = selectedMode.subtitleVi,
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                                                    onWordCorrect(currentQ.sourceWord.id)
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

        // True SM-2 Spaced Repetition Review Queue Section
        Sm2SrsReviewQueueCard(
            srsReviewWords = srsReviewWords,
            srsSchedules = srsSchedules,
            ttsController = ttsController,
            onReviewSrsSm2 = onReviewSrsSm2,
            onMarkSrsMastered = onMarkSrsMastered
        )
    }
}

@Composable
private fun Sm2SrsReviewQueueCard(
    srsReviewWords: List<VocabWordItem>,
    srsSchedules: Map<String, Sm2CardState>,
    ttsController: ApexTtsController,
    onReviewSrsSm2: (String, Sm2RecallQuality) -> Unit,
    onMarkSrsMastered: (String) -> Unit
) {
    val todayIso = remember { StreakManager.todayIsoDate() }
    val revealedMap = remember { mutableStateMapOf<String, Boolean>() }
    var earlyReviewWordId by remember { mutableStateOf<String?>(null) }
    var showUpcoming by remember { mutableStateOf(false) }

    val dueWords = remember(srsReviewWords, srsSchedules, todayIso, earlyReviewWordId) {
        srsReviewWords.filter { word ->
            word.id == earlyReviewWordId || srsSchedules[word.id]?.isDueOn(todayIso) != false
        }
    }
    val upcomingWords = remember(srsReviewWords, srsSchedules, todayIso, earlyReviewWordId) {
        srsReviewWords.filter { word ->
            word.id != earlyReviewWordId && srsSchedules[word.id]?.isScheduledFuture(todayIso) == true
        }.sortedBy { srsSchedules[it.id]?.nextReviewDateIso.orEmpty() }
    }

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
                        text = "Ôn tập SM-2 (Spaced Repetition)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = InkPrimary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (dueWords.isEmpty()) EmeraldSuccessLight else GoldMasteryLight
                    ) {
                        Text(
                            text = "Đến hạn: ${dueWords.size}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (dueWords.isEmpty()) EmeraldOnContainer else GoldOnContainer,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                    if (upcomingWords.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = ElectricIndigoLight
                        ) {
                            Text(
                                text = "Đã hẹn: ${upcomingWords.size}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ElectricIndigo,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            if (srsReviewWords.isEmpty()) {
                Text(
                    text = stringResource(R.string.srs_queue_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted
                )
            } else if (dueWords.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = EmeraldSuccessLight.copy(alpha = 0.65f),
                    border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Tuyệt vời! Bạn đã hoàn thành toàn bộ thẻ đến hạn hôm nay (${upcomingWords.size} thẻ đã lên lịch SM-2).",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = EmeraldOnContainer
                        )
                    }
                }
            } else {
                dueWords.forEach { item ->
                    val sm2 = srsSchedules[item.id]
                    val isRevealed = revealedMap[item.id] == true
                    val efText = String.format(Locale.US, "%.2f", sm2?.easeFactor ?: 2.5f)
                    val repCount = sm2?.repetitions ?: 0

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = SurfaceCanvas,
                        border = BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.18f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sm2_due_card_${item.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
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
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = ElectricIndigo
                                        )
                                        Text(
                                            text = "${item.ipaUk} • Ôn #$repCount • EF $efText",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = InkMuted
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = CardWhite,
                                        border = BorderStroke(1.dp, CardBorderSoft),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { revealedMap[item.id] = !isRevealed }
                                            .testTag("sm2_reveal_${item.id}")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isRevealed) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                                contentDescription = null,
                                                tint = ElectricIndigo,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = if (isRevealed) "Ẩn" else "Đáp án",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ElectricIndigo
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = EmeraldSuccessLight,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { onMarkSrsMastered(item.id) }
                                            .testTag("srs_master_${item.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = "Gỡ khỏi hàng chờ SRS",
                                            tint = EmeraldSuccess,
                                            modifier = Modifier
                                                .padding(6.dp)
                                                .size(15.dp)
                                        )
                                    }
                                }
                            }

                            AnimatedVisibility(visible = isRevealed) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = CardWhite,
                                    border = BorderStroke(1.dp, CardBorderSoft)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = item.meaningVi,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = InkPrimary
                                        )
                                        if (item.exampleEn.isNotBlank()) {
                                            Text(
                                                text = "“${item.exampleEn}”",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = PlusJakartaSansFontFamily,
                                                    fontSize = 11.5.sp
                                                ),
                                                color = InkSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            // 4 True SM-2 Recall Rating Buttons (Quên / Khó / Nhớ / Dễ)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Sm2RecallQuality.entries.forEach { quality ->
                                    val intervalLabel = Sm2SrsEngine.previewIntervalLabel(sm2, quality)
                                    val (btnBg, btnBorder, btnFg) = when (quality) {
                                        Sm2RecallQuality.AGAIN -> Triple(RoseDiffLight, RoseAlert.copy(alpha = 0.55f), RoseAlert)
                                        Sm2RecallQuality.HARD -> Triple(GoldMasteryLight, GoldMastery.copy(alpha = 0.65f), GoldOnContainer)
                                        Sm2RecallQuality.GOOD -> Triple(ElectricIndigoLight, ElectricIndigo.copy(alpha = 0.55f), ElectricIndigo)
                                        Sm2RecallQuality.EASY -> Triple(EmeraldSuccessLight, EmeraldSuccess.copy(alpha = 0.65f), EmeraldOnContainer)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = btnBg,
                                        border = BorderStroke(1.dp, btnBorder),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .tactilePress(pressedScale = 0.95f)
                                            .clickable {
                                                revealedMap.remove(item.id)
                                                if (earlyReviewWordId == item.id) earlyReviewWordId = null
                                                onReviewSrsSm2(item.id, quality)
                                            }
                                            .testTag("sm2_rate_${item.id}_${quality.id}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(1.dp)
                                        ) {
                                            Text(
                                                text = quality.labelVi,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                color = btnFg,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = intervalLabel,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = PlusJakartaSansFontFamily,
                                                    fontSize = 10.sp
                                                ),
                                                color = btnFg.copy(alpha = 0.85f),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Collapsible Upcoming SM-2 Scheduled Words
            if (upcomingWords.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceCanvas,
                    border = BorderStroke(1.dp, CardBorderSoft)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showUpcoming = !showUpcoming }
                                .testTag("sm2_toggle_upcoming"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarMonth,
                                    contentDescription = null,
                                    tint = ElectricIndigo,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Lịch ôn SM-2 sắp tới (${upcomingWords.size} từ)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = InkPrimary
                                )
                            }
                            Icon(
                                imageVector = if (showUpcoming) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = null,
                                tint = InkSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        AnimatedVisibility(visible = showUpcoming) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                upcomingWords.forEach { item ->
                                    val sm2 = srsSchedules[item.id]
                                    val nextDate = sm2?.nextReviewDateIso.orEmpty()
                                    val intervalVi = Sm2SrsEngine.formatIntervalVi(sm2?.intervalDays ?: 1)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.word,
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontFamily = PlusJakartaSansFontFamily,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = InkPrimary
                                            )
                                            Text(
                                                text = "Ôn lại: $nextDate • Chu kỳ $intervalVi",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = InkMuted
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = ElectricIndigoLight,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .clickable { earlyReviewWordId = item.id }
                                                .testTag("sm2_early_${item.id}")
                                        ) {
                                            Text(
                                                text = "Ôn sớm",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = ElectricIndigo,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
