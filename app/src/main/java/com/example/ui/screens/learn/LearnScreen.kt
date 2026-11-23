package com.example.ui.screens.learn

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.LearnSubTab
import com.example.data.Sm2RecallQuality
import com.example.data.VocabWordItem
import com.example.speech.ApexTtsController
import com.example.speech.EnglishAccent
import com.example.ui.ApexIeltsUiState
import com.example.ui.components.AnimatedFloatingGradCap
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.IndigoOnSurface
import com.example.ui.theme.InkOnHero
import com.example.ui.theme.InkOnHeroMuted
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LocalAdaptiveLayoutInfo
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.SurfaceCanvas
import java.util.Locale

private val LearnIndigoCyanGradient = Brush.linearGradient(
    colors = listOf(ElectricIndigo, Color(0xFF0EA5E9), CyanGlow),
    start = Offset(0f, 0f),
    end = Offset(1050f, 700f)
)

/**
 * Tab 2: Học (Learn) — Indigo–Cyan accent theme with 4 Segmented Pill Sub-tabs,
 * dynamically filtered by activeModuleId:
 * 1. Từ mới & Ngữ pháp (3D Flip Flashcards + UK/US TTS + Voice Check + Band 6.0 Pitfalls)
 * 2. Đặt câu AI (Contextual Sentence Builder powered by Firebase AI Logic Gemini 3.8 Flash)
 * 3. Sắp xếp câu (5 Band 8.5+ Complex Syntax Reordering Exercises)
 * 4. Game & SRS (3 Timed Mini-Games + Persisted SRS Review Queue)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LearnScreen(
    uiState: ApexIeltsUiState,
    onMarkWordStudied: (String) -> Unit,
    onAddWordToSrs: (wordId: String, notify: Boolean) -> Unit,
    onMarkSrsMastered: (String) -> Unit,
    onReviewSrsSm2: (wordId: String, quality: Sm2RecallQuality) -> Unit = { _, _ -> },
    onEvaluateSentence: (VocabWordItem, String) -> Unit,
    onResetSentenceAi: () -> Unit,
    onSyntaxSolved: (String) -> Unit,
    onMiniGameWordCorrect: (String) -> Unit = {},
    onMiniGameCompleted: (score: Int, total: Int, bestCombo: Int, missedCount: Int) -> Unit,
    onOpenPersonalLibrary: () -> Unit = {},
    onOpenBulkImporter: () -> Unit = {},
    onShowMessage: (String) -> Unit = {},
    onReturnToRoadmap: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onReturnToRoadmap()
    }

    val context = LocalContext.current
    val layoutInfo = LocalAdaptiveLayoutInfo.current
    val activeModule = uiState.activeModule
    val moduleWords = uiState.activeModuleWords

    var selectedSubTabIndex by rememberSaveable { mutableIntStateOf(LearnSubTab.VOCAB_GRAMMAR.index) }
    var selectedWordIndex by rememberSaveable(activeModule.id) { mutableIntStateOf(0) }
    var speakingAccent by remember { mutableStateOf<EnglishAccent?>(null) }

    val ttsController = remember(context) {
        ApexTtsController(context) { accent -> speakingAccent = accent }
    }
    DisposableEffect(ttsController) {
        onDispose { ttsController.shutdown() }
    }

    val currentSubTab = LearnSubTab.entries.getOrElse(selectedSubTabIndex) { LearnSubTab.VOCAB_GRAMMAR }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceCanvas)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .testTag("learn_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Ultra-compact Indigo–Cyan Top Bar (maximizes active flashcard/exercise viewport)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
                .background(LearnIndigoCyanGradient)
                .statusBarsPadding()
                .displayCutoutPadding()
                .padding(horizontal = layoutInfo.horizontalPadding, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.22f)
                ) {
                    Text(
                        text = "M${String.format(Locale.US, "%02d", activeModule.id)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = activeModule.titleEn,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = InkOnHero,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = "${moduleWords.size} từ",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                com.example.ui.components.CompactHelpTipButton(
                    title = "Mẹo học Module #${String.format(Locale.US, "%02d", activeModule.id)}",
                    tips = listOf(
                        "Chạm thẻ từ để lật 3D xem nghĩa, collocations & lỗi Band 6.0.",
                        "Bấm UK/US để nghe phát âm và thử Micro chấm phát âm ngay.",
                        "Lưu từ khó vào SRS hoặc chuyển sang Đặt câu AI để nhớ lâu hơn."
                    ),
                    testTagName = "learn_help_tip_button"
                )
            }

            // 4 Segmented Pill Sub-tabs
            SegmentedLearnPillBar(
                selectedTab = currentSubTab,
                srsBadgeCount = uiState.srsReviewWords.size,
                onSelectTab = { selectedSubTabIndex = it.index }
            )
        }

        // Sub-tab Content Area with smooth 200ms fade transition
        AnimatedContent(
            targetState = currentSubTab,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
            label = "learn_subtab_content"
        ) { subTab ->
            when (subTab) {
                LearnSubTab.VOCAB_GRAMMAR -> FlashcardSubTab(
                    words = moduleWords,
                    allVocabulary = uiState.allMergedVocabulary,
                    activeModuleId = activeModule.id,
                    selectedIndex = selectedWordIndex,
                    srsWordIds = uiState.srsWordIds,
                    learnedVocabIds = uiState.learnedVocabIds,
                    dailyLearnedWordIds = uiState.autoKpi.dailyLearnedWordIds,
                    speakingAccent = speakingAccent,
                    ttsController = ttsController,
                    topicHintEn = activeModule.titleEn,
                    isOnline = uiState.isOnline,
                    googleCseApiKey = uiState.googleCseApiKey,
                    googleCseCx = uiState.googleCseCx,
                    onSelectIndex = { selectedWordIndex = it },
                    onWordFlipped = { word -> onMarkWordStudied(word.id) },
                    onAddToSrs = { wordId -> onAddWordToSrs(wordId, true) },
                    onBuildSentenceWithWord = { word ->
                        val idx = moduleWords.indexOfFirst { it.id == word.id }.coerceAtLeast(0)
                        selectedWordIndex = idx
                        onResetSentenceAi()
                        selectedSubTabIndex = LearnSubTab.AI_SENTENCE.index
                    },
                    onShowMessage = onShowMessage
                )

                LearnSubTab.AI_SENTENCE -> AiSentenceSubTab(
                    words = moduleWords,
                    selectedWordIndex = selectedWordIndex,
                    aiState = uiState.sentenceAiState,
                    ttsController = ttsController,
                    onSelectWordIndex = {
                        selectedWordIndex = it
                        onResetSentenceAi()
                    },
                    onSubmitSentence = onEvaluateSentence
                )

                LearnSubTab.SYNTAX_REORDER -> SyntaxReorderSubTab(
                    exercises = uiState.sentenceDrills,
                    ttsController = ttsController,
                    onExerciseSolved = onSyntaxSolved
                )

                LearnSubTab.GAME_SRS -> GameAndSrsSubTab(
                    moduleWords = moduleWords,
                    srsReviewWords = uiState.srsReviewWords,
                    srsSchedules = uiState.srsSchedules,
                    ttsController = ttsController,
                    onMissedWord = { wordId -> onAddWordToSrs(wordId, false) },
                    onWordCorrect = onMiniGameWordCorrect,
                    onGameCompleted = onMiniGameCompleted,
                    onMarkSrsMastered = onMarkSrsMastered,
                    onReviewSrsSm2 = onReviewSrsSm2
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))
    }
}

@Composable
private fun SegmentedLearnPillBar(
    selectedTab: LearnSubTab,
    srsBadgeCount: Int,
    onSelectTab: (LearnSubTab) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.20f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.26f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("learn_segmented_tabs")
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val minTabWidth = ((maxWidth - 20.dp) / LearnSubTab.entries.size).coerceAtLeast(78.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LearnSubTab.entries.forEach { tab ->
                    val isSelected = tab == selectedTab
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) CardWhite else Color.Transparent,
                        animationSpec = tween(200),
                        label = "learn_pill_bg_${tab.index}"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) IndigoOnSurface else Color.White,
                        animationSpec = tween(200),
                        label = "learn_pill_text_${tab.index}"
                    )

                    Box(
                        modifier = Modifier
                            .widthIn(min = minTabWidth)
                            .wrapContentWidth()
                            .clip(RoundedCornerShape(50))
                            .background(bgColor)
                            .clickable { onSelectTab(tab) }
                            .padding(vertical = 9.dp, horizontal = 10.dp)
                            .testTag("learn_subtab_${tab.index}"),
                        contentAlignment = Alignment.Center
                    ) {
                        val label = if (tab == LearnSubTab.GAME_SRS && srsBadgeCount > 0) {
                            "${tab.shortLabelVi} ($srsBadgeCount)"
                        } else {
                            tab.shortLabelVi
                        }
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                            ),
                            color = textColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}
