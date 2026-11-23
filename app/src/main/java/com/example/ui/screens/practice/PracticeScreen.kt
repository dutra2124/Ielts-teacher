package com.example.ui.screens.practice

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ManageSearch
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
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
import com.example.data.PracticeCurriculum
import com.example.data.PracticeSkill
import com.example.speech.ApexTtsController
import com.example.ui.ApexIeltsUiState
import com.example.ui.components.AnimatedAudioWaveform
import com.example.ui.components.LocalGlassmorphismDisabled
import com.example.ui.theme.CardWhite
import com.example.ui.theme.GoldMastery
import com.example.ui.theme.GoldMasteryLight
import com.example.ui.theme.GoldOnContainer
import com.example.ui.theme.InkOnHero
import com.example.ui.theme.InkOnHeroMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.LocalAdaptiveLayoutInfo
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.PracticeTeal
import com.example.ui.theme.PracticeTealDark
import com.example.ui.theme.PracticeTealDeep
import com.example.ui.theme.PracticeTealLight
import com.example.ui.theme.PracticeTealSurface
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.TealOnSurface
import java.util.Locale

private val PracticeEmeraldGradient = Brush.linearGradient(
    colors = listOf(PracticeTealDeep, PracticeTeal, Color(0xFF14B8A6)),
    start = Offset(0f, 0f),
    end = Offset(1050f, 700f)
)

/**
 * Tab 3: Luyện (Practice) — Emerald Teal (#0D9488) theme featuring:
 * - Quick 4-skill segmented switcher: Reading | Listening | Speaking | Writing
 * - Full academic IELTS sample bundles synced to activeModuleId
 * - Two compact floating assistant pills opening Material 3 Bottom Sheets:
 *   1. Soi Ngữ pháp Câu (AI Syntax Breakdown via Firebase AI Logic)
 *   2. Hệ thống Gợi ý Thông minh (3-Level Progressive Hints for R/L; Band 8.5+ Vocab & Idioms for W/S)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PracticeScreen(
    uiState: ApexIeltsUiState,
    onInspectSyntaxSentence: (sentenceEn: String, sourceContext: String) -> Unit,
    onResetSyntaxBreakdown: () -> Unit,
    onEvaluateSpeaking: (partLabel: String, promptEn: String, topicEn: String, transcript: String) -> Unit,
    onResetSpeakingAi: () -> Unit,
    onEvaluateWriting: (taskLabel: String, promptEn: String, topicEn: String, essay: String, wordCount: Int) -> Unit,
    onResetWritingAi: () -> Unit,
    onPracticeMilestoneCompleted: (messageVi: String) -> Unit,
    onShowQuickToast: (messageVi: String) -> Unit,
    onConsumePendingSkill: () -> Unit = {},
    onDismissFocusNote: () -> Unit = {},
    onReturnToRoadmap: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onReturnToRoadmap()
    }

    val context = LocalContext.current
    val layoutInfo = LocalAdaptiveLayoutInfo.current
    val activeModule = uiState.activeModule
    val bundle = remember(activeModule.id) { PracticeCurriculum.bundleForModule(activeModule.id) }

    var selectedSkillIndex by rememberSaveable { mutableIntStateOf(PracticeSkill.READING.index) }
    val currentSkill = PracticeSkill.entries.getOrElse(selectedSkillIndex) { PracticeSkill.READING }

    // Pre-filter to weak skill when jumping from Tab 5 ("Luyện lại dạng bài yếu")
    LaunchedEffect(uiState.pendingPracticeSkill) {
        val target = uiState.pendingPracticeSkill
        if (target != null) {
            selectedSkillIndex = target.index
            onConsumePendingSkill()
        }
    }

    // Drafts hoisted so Band 8.5+ Vocab Bottom Sheet can insert phrases directly into Speaking/Writing
    var speakingTranscript by rememberSaveable(activeModule.id) { mutableStateOf("") }
    var writingEssayDraft by rememberSaveable(activeModule.id) { mutableStateOf("") }

    // Smart Assistant 1: Syntax Breakdown inspect mode & bottom sheet
    var isSyntaxInspectMode by rememberSaveable { mutableStateOf(false) }
    var showSyntaxSheet by remember { mutableStateOf(false) }

    // Smart Assistant 2: 3-Level Smart Hints (Reading/Listening) & Band 8.5+ Vocab Drawer (Speaking/Writing)
    var showThreeLevelHintsSheet by remember { mutableStateOf(false) }
    var activeHintQuestionIndex by remember(currentSkill, activeModule.id) { mutableIntStateOf(0) }
    val unlockedHintLevels = remember(currentSkill, activeModule.id) { mutableStateMapOf<Int, Int>() }
    var showVocabDrawerSheet by remember { mutableStateOf(false) }

    val ttsController = remember(context) {
        ApexTtsController(context) { _ -> }
    }
    DisposableEffect(ttsController) {
        onDispose { ttsController.shutdown() }
    }

    val isReceptiveSkill = currentSkill == PracticeSkill.READING || currentSkill == PracticeSkill.LISTENING

    val activeHints = remember(bundle, currentSkill) {
        when (currentSkill) {
            PracticeSkill.READING -> bundle.reading.questions.map { it.hint }
            PracticeSkill.LISTENING -> bundle.listening.questions.map { it.hint }
            else -> emptyList()
        }
    }

    val quickSyntaxSentences = remember(bundle, currentSkill, speakingTranscript, writingEssayDraft) {
        when (currentSkill) {
            PracticeSkill.READING -> bundle.reading.sentences.map { it.textEn }
            PracticeSkill.LISTENING -> bundle.listening.segments.map { it.textEn }
            PracticeSkill.SPEAKING -> {
                val userSentences = speakingTranscript
                    .split(Regex("[.!?]+"))
                    .map { it.trim() }
                    .filter { it.length >= 18 }
                (userSentences + bundle.reading.sentences.take(3).map { it.textEn }).distinct()
            }
            PracticeSkill.WRITING -> {
                val userSentences = writingEssayDraft
                    .split(Regex("[.!?]+"))
                    .map { it.trim() }
                    .filter { it.length >= 18 }
                (userSentences + bundle.reading.sentences.take(3).map { it.textEn }).distinct()
            }
        }
    }

    fun triggerSyntaxBreakdown(sentenceEn: String) {
        val source = when (currentSkill) {
            PracticeSkill.READING -> "IELTS Reading (${bundle.reading.titleEn})"
            PracticeSkill.LISTENING -> "IELTS Listening Transcript (${bundle.listening.titleEn})"
            PracticeSkill.SPEAKING -> "IELTS Speaking (${bundle.speaking.topicEn})"
            PracticeSkill.WRITING -> "IELTS Writing (${bundle.writing.topicEn})"
        }
        showSyntaxSheet = true
        onInspectSyntaxSentence(sentenceEn, source)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceCanvas)
            .imePadding()
            .testTag("practice_screen")
    ) {
        // Ultra-compact Emerald Teal Header (#0D9488) — dedicates 80%+ viewport to active practice
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(PracticeEmeraldGradient)
                .statusBarsPadding()
                .displayCutoutPadding()
                .padding(horizontal = layoutInfo.horizontalPadding, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = InkOnHero,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Compact Assistant Pill 1: Soi câu (Syntax Breakdown)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isSyntaxInspectMode) GoldMasteryLight else Color.White.copy(alpha = 0.20f),
                    border = BorderStroke(
                        1.dp,
                        if (isSyntaxInspectMode) GoldMastery else Color.White.copy(alpha = 0.30f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable {
                            if (isReceptiveSkill) {
                                if (!isSyntaxInspectMode) {
                                    isSyntaxInspectMode = true
                                    onShowQuickToast("Đã bật Soi câu — Chạm câu bất kỳ trong bài đọc / transcript!")
                                } else {
                                    showSyntaxSheet = true
                                }
                            } else {
                                onResetSyntaxBreakdown()
                                showSyntaxSheet = true
                            }
                        }
                        .testTag("fab_syntax_breakdown")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ManageSearch,
                            contentDescription = stringResource(R.string.practice_fab_syntax),
                            tint = if (isSyntaxInspectMode) GoldOnContainer else Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (isSyntaxInspectMode) "Đang soi" else "Soi câu",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSyntaxInspectMode) GoldOnContainer else Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Compact Assistant Pill 2: Gợi ý / Từ 8.5+
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable {
                            if (isReceptiveSkill) {
                                if (unlockedHintLevels[activeHintQuestionIndex] == null) {
                                    unlockedHintLevels[activeHintQuestionIndex] = 1
                                }
                                showThreeLevelHintsSheet = true
                            } else {
                                showVocabDrawerSheet = true
                            }
                        }
                        .testTag("fab_smart_hints")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = if (isReceptiveSkill) Icons.Rounded.TipsAndUpdates else Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = TealOnSurface,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (isReceptiveSkill) "Gợi ý" else "Từ 8.5+",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TealOnSurface,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Compact `?` Help Tip Dialog Button
                com.example.ui.components.CompactHelpTipButton(
                    title = "Mẹo Luyện 4 Kỹ năng",
                    tips = listOf(
                        "Reading/Listening: Chạm tiêu đề câu hỏi để mở/thu gọn, dùng thanh số câu ở dưới để nhảy nhanh.",
                        "Bấm 'Thu gọn bài đọc' hoặc kéo thanh chia để mở rộng không gian đọc/làm bài.",
                        "Bật 'Soi câu' rồi chạm vào bất kỳ câu phức nào để AI phân tích ngữ pháp.",
                        "Dùng nút 'Bảng câu' để kiểm tra các câu chưa làm hoặc đã đánh dấu xem lại."
                    ),
                    testTagName = "practice_help_tip_button"
                )
            }

            // 4-Skill Quick Segmented Selector: Reading | Listening | Speaking | Writing
            PracticeSkillSegmentedBar(
                selectedSkill = currentSkill,
                onSelectSkill = { skill ->
                    ttsController.stop()
                    selectedSkillIndex = skill.index
                    if (skill == PracticeSkill.SPEAKING || skill == PracticeSkill.WRITING) {
                        isSyntaxInspectMode = false
                    }
                }
            )
        }

        // Compact 1-line Focus Strip when arriving from Error Log
        AnimatedVisibility(visible = !uiState.pendingPracticeFocusNote.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = GoldMasteryLight,
                border = BorderStroke(1.dp, GoldMastery.copy(alpha = 0.55f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = layoutInfo.horizontalPadding, vertical = 5.dp)
                    .testTag("practice_weak_focus_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.TipsAndUpdates,
                        contentDescription = null,
                        tint = GoldOnContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = uiState.pendingPracticeFocusNote.orEmpty(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = InkPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = CardWhite,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onDismissFocusNote() }
                            .testTag("dismiss_practice_focus_banner")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Đóng",
                            tint = InkPrimary,
                            modifier = Modifier
                                .padding(4.dp)
                                .size(13.dp)
                        )
                    }
                }
            }
        }

        // Compact 1-line Active Syntax Inspect Mode Strip
        AnimatedVisibility(visible = isSyntaxInspectMode && isReceptiveSkill) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PracticeTealSurface,
                border = BorderStroke(1.dp, PracticeTeal),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = layoutInfo.horizontalPadding, vertical = 5.dp)
                    .testTag("syntax_inspect_active_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ManageSearch,
                        contentDescription = null,
                        tint = TealOnSurface,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = "Chạm câu bất kỳ để AI soi ngữ pháp",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TealOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = PracticeTeal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { isSyntaxInspectMode = false }
                            .testTag("syntax_inspect_turn_off")
                    ) {
                        Text(
                            text = "Tắt",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // Full-height Skill Content Area (80%+ viewport)
        AnimatedContent(
            targetState = currentSkill,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
            label = "practice_skill_content",
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { skill ->
            when (skill) {
                PracticeSkill.READING -> CompositionLocalProvider(LocalGlassmorphismDisabled provides true) {
                    PracticeReadingView(
                        pack = bundle.reading,
                        isSyntaxInspectMode = isSyntaxInspectMode,
                        onInspectSentence = { sentence -> triggerSyntaxBreakdown(sentence) },
                        onOpenHintForQuestion = { qIdx ->
                            activeHintQuestionIndex = qIdx
                            if (unlockedHintLevels[qIdx] == null) unlockedHintLevels[qIdx] = 1
                            showThreeLevelHintsSheet = true
                        },
                        onReadingChecked = { correct, total ->
                            onPracticeMilestoneCompleted(
                                "Đã hoàn thành Reading ($correct/$total câu đúng) & mở Keyword Mapping!"
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                PracticeSkill.LISTENING -> PracticeListeningView(
                    pack = bundle.listening,
                    ttsController = ttsController,
                    isSyntaxInspectMode = isSyntaxInspectMode,
                    onInspectSentence = { sentence -> triggerSyntaxBreakdown(sentence) },
                    onOpenHintForQuestion = { qIdx ->
                        activeHintQuestionIndex = qIdx
                        if (unlockedHintLevels[qIdx] == null) unlockedHintLevels[qIdx] = 1
                        showThreeLevelHintsSheet = true
                    },
                    onListeningCompleted = onPracticeMilestoneCompleted,
                    modifier = Modifier.fillMaxSize()
                )

                PracticeSkill.SPEAKING -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 10.dp, bottom = 20.dp)
                ) {
                    PracticeSpeakingView(
                        pack = bundle.speaking,
                        transcript = speakingTranscript,
                        onTranscriptChange = { speakingTranscript = it },
                        aiState = uiState.speakingAiState,
                        ttsController = ttsController,
                        onEvaluateSpeaking = onEvaluateSpeaking,
                        onResetAi = onResetSpeakingAi
                    )
                }

                PracticeSkill.WRITING -> CompositionLocalProvider(LocalGlassmorphismDisabled provides true) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(top = 10.dp, bottom = 20.dp)
                    ) {
                        PracticeWritingView(
                            pack = bundle.writing,
                            essayDraft = writingEssayDraft,
                            onEssayDraftChange = { writingEssayDraft = it },
                            aiState = uiState.writingAiState,
                            onEvaluateWriting = onEvaluateWriting,
                            onResetAi = onResetWritingAi
                        )
                    }
                }
            }
        }
    }

    // Bottom Sheet 1: AI Syntax Breakdown
    if (showSyntaxSheet) {
        SyntaxBreakdownBottomSheet(
            syntaxState = uiState.syntaxBreakdownState,
            quickPickSentences = quickSyntaxSentences,
            onPickSentence = { sentence -> triggerSyntaxBreakdown(sentence) },
            onRetry = { sentence -> triggerSyntaxBreakdown(sentence) },
            onDismiss = { showSyntaxSheet = false }
        )
    }

    // Bottom Sheet 2A: 3-Level Smart Hints (Reading / Listening)
    if (showThreeLevelHintsSheet && activeHints.isNotEmpty()) {
        ThreeLevelHintsBottomSheet(
            hints = activeHints,
            selectedQuestionIndex = activeHintQuestionIndex,
            unlockedLevelByQuestion = unlockedHintLevels,
            onSelectQuestion = { idx ->
                activeHintQuestionIndex = idx
                if (unlockedHintLevels[idx] == null) unlockedHintLevels[idx] = 1
            },
            onUnlockNextLevel = { idx ->
                val cur = unlockedHintLevels[idx] ?: 1
                unlockedHintLevels[idx] = (cur + 1).coerceAtMost(3)
            },
            onDismiss = { showThreeLevelHintsSheet = false }
        )
    }

    // Bottom Sheet 2B: Band 8.5+ Vocabulary & Idioms Drawer (Speaking / Writing)
    if (showVocabDrawerSheet) {
        Band85VocabHintsBottomSheet(
            topicEn = activeModule.titleEn,
            skillLabelVi = if (currentSkill == PracticeSkill.SPEAKING) "Speaking" else "Writing",
            vocabHints = bundle.band85VocabHints,
            ttsController = ttsController,
            onInsertPhrase = { phrase ->
                if (currentSkill == PracticeSkill.SPEAKING) {
                    speakingTranscript = if (speakingTranscript.isBlank()) phrase else "${speakingTranscript.trimEnd()} $phrase"
                } else {
                    writingEssayDraft = if (writingEssayDraft.isBlank()) phrase else "${writingEssayDraft.trimEnd()} $phrase"
                }
                showVocabDrawerSheet = false
                onShowQuickToast("Đã chèn cụm Band 8.5+ \"$phrase\" vào bài làm!")
            },
            onDismiss = { showVocabDrawerSheet = false }
        )
    }
}

@Composable
private fun PracticeSkillSegmentedBar(
    selectedSkill: PracticeSkill,
    onSelectSkill: (PracticeSkill) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.20f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("practice_skill_tabs")
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val minSkillWidth = ((maxWidth - 20.dp) / PracticeSkill.entries.size).coerceAtLeast(78.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PracticeSkill.entries.forEach { skill ->
                    val isSelected = skill == selectedSkill
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) CardWhite else Color.Transparent,
                        animationSpec = tween(200),
                        label = "practice_pill_bg_${skill.index}"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) TealOnSurface else Color.White,
                        animationSpec = tween(200),
                        label = "practice_pill_text_${skill.index}"
                    )

                    Box(
                        modifier = Modifier
                            .widthIn(min = minSkillWidth)
                            .wrapContentWidth()
                            .clip(RoundedCornerShape(50))
                            .background(bgColor)
                            .clickable { onSelectSkill(skill) }
                            .padding(vertical = 9.dp, horizontal = 10.dp)
                            .testTag("practice_skill_${skill.routeKey}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = skill.labelEn,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
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
