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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import com.example.ui.theme.CardWhite
import com.example.ui.theme.GoldMastery
import com.example.ui.theme.GoldMasteryLight
import com.example.ui.theme.GoldOnContainer
import com.example.ui.theme.InkOnHero
import com.example.ui.theme.InkOnHeroMuted
import com.example.ui.theme.InkPrimary
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceCanvas)
            .testTag("practice_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Emerald Teal Hero Header (#0D9488)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                    .background(PracticeEmeraldGradient)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                                center = Offset(size.width * 0.88f, size.height * 0.22f),
                                radius = size.width * 0.44f
                            ),
                            radius = size.width * 0.44f,
                            center = Offset(size.width * 0.88f, size.height * 0.22f)
                        )
                    }
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.22f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.34f))
                            ) {
                                Text(
                                    text = "Module #${String.format(Locale.US, "%02d", activeModule.id)} • ${activeModule.tier.bandRange}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.18f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Text(
                                        text = "4 Kỹ năng AI",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Text(
                            text = activeModule.titleEn,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = InkOnHero,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${stringResource(R.string.practice_header_title)} • ${activeModule.titleVi}",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkOnHeroMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedAudioWaveform(
                            isActive = true,
                            width = 30.dp,
                            height = 18.dp,
                            primaryColor = Color.White,
                            secondaryColor = PracticeTealLight
                        )
                    }
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

            // Focus Banner when arriving from Tab 5 Error Log ("Luyện lại dạng bài yếu")
            AnimatedVisibility(visible = !uiState.pendingPracticeFocusNote.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = GoldMasteryLight,
                    border = BorderStroke(1.dp, GoldMastery.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("practice_weak_focus_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.TipsAndUpdates,
                            contentDescription = null,
                            tint = GoldOnContainer,
                            modifier = Modifier.size(19.dp)
                        )
                        Text(
                            text = "Ôn tập trọng điểm từ Nhật ký lỗi sai: ${uiState.pendingPracticeFocusNote.orEmpty()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = InkPrimary,
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
                                contentDescription = "Đóng ghi chú ôn tập",
                                tint = InkPrimary,
                                modifier = Modifier
                                    .padding(5.dp)
                                    .size(14.dp)
                            )
                        }
                    }
                }
            }

            // Active Syntax Inspect Mode Banner (when turned ON for Reading / Listening)
            AnimatedVisibility(visible = isSyntaxInspectMode && isReceptiveSkill) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = PracticeTealSurface,
                    border = BorderStroke(1.5.dp, PracticeTeal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("syntax_inspect_active_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ManageSearch,
                            contentDescription = null,
                            tint = TealOnSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.practice_inspect_banner),
                            style = MaterialTheme.typography.labelMedium,
                            color = TealOnSurface,
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = "Tắt chế độ soi", tint = Color.White, modifier = Modifier.size(13.dp))
                                Text("Tắt", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Skill Content Area
            AnimatedContent(
                targetState = currentSkill,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                label = "practice_skill_content"
            ) { skill ->
                when (skill) {
                    PracticeSkill.READING -> PracticeReadingView(
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
                                "Đã hoàn thành Reading ($correct/$total câu đúng) & mở Keyword Mapping! Đã cập nhật mục tiêu Luyện đề."
                            )
                        }
                    )

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
                        onListeningCompleted = onPracticeMilestoneCompleted
                    )

                    PracticeSkill.SPEAKING -> PracticeSpeakingView(
                        pack = bundle.speaking,
                        transcript = speakingTranscript,
                        onTranscriptChange = { speakingTranscript = it },
                        aiState = uiState.speakingAiState,
                        ttsController = ttsController,
                        onEvaluateSpeaking = onEvaluateSpeaking,
                        onResetAi = onResetSpeakingAi
                    )

                    PracticeSkill.WRITING -> PracticeWritingView(
                        pack = bundle.writing,
                        essayDraft = writingEssayDraft,
                        onEssayDraftChange = { writingEssayDraft = it },
                        aiState = uiState.writingAiState,
                        onEvaluateWriting = onEvaluateWriting,
                        onResetAi = onResetWritingAi
                    )
                }
            }

            // Generous bottom spacer so content clears the 2 floating assistant pills
            Spacer(modifier = Modifier.height(92.dp))
        }

        // Two Neat Floating Assistant Pills anchored cleanly at bottom-end
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pill 1: Soi Ngữ pháp Câu (Syntax Breakdown)
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isSyntaxInspectMode) PracticeTealDeep else CardWhite,
                border = BorderStroke(1.5.dp, PracticeTeal),
                modifier = Modifier
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(50),
                        ambientColor = PracticeTeal.copy(alpha = 0.25f),
                        spotColor = PracticeTeal.copy(alpha = 0.30f)
                    )
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        if (isReceptiveSkill) {
                            if (!isSyntaxInspectMode) {
                                isSyntaxInspectMode = true
                                onShowQuickToast("Đã bật Soi Ngữ pháp — Chạm vào bất kỳ câu phức nào trong bài đọc / transcript!")
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ManageSearch,
                        contentDescription = stringResource(R.string.practice_fab_syntax),
                        tint = if (isSyntaxInspectMode) Color.White else TealOnSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isSyntaxInspectMode) {
                            stringResource(R.string.practice_fab_syntax_active)
                        } else {
                            stringResource(R.string.practice_fab_syntax)
                        },
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isSyntaxInspectMode) Color.White else TealOnSurface
                    )
                }
            }

            // Pill 2: Smart Hints (Gợi ý 3 cấp for Reading/Listening OR Từ vựng 8.5+ for Writing/Speaking)
            Surface(
                shape = RoundedCornerShape(50),
                color = PracticeTeal,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                modifier = Modifier
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(50),
                        ambientColor = PracticeTeal.copy(alpha = 0.30f),
                        spotColor = PracticeTeal.copy(alpha = 0.38f)
                    )
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)
                ) {
                    Icon(
                        imageVector = if (isReceptiveSkill) Icons.Rounded.TipsAndUpdates else Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = if (isReceptiveSkill) GoldMasteryLight else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isReceptiveSkill) {
                            stringResource(R.string.practice_fab_hints)
                        } else {
                            stringResource(R.string.practice_fab_vocab)
                        },
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(bgColor)
                        .clickable { onSelectSkill(skill) }
                        .padding(vertical = 9.dp, horizontal = 4.dp)
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
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
