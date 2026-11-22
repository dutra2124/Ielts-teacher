package com.example.ui.screens.exam

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.speech.SpeechRecognizer
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.BorderColor
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.OutlinedFlag
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.StickyNote2
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Timer
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.ActiveExamSession
import com.example.data.ExamAttemptRecord
import com.example.data.ExamQuestionItem
import com.example.data.ExamQuestionSkill
import com.example.data.ExamQuestionType
import com.example.data.ExamScoringHelper
import com.example.data.ExamSkillMode
import com.example.speech.ApexPronunciationEvaluator
import com.example.speech.ApexTtsController
import com.example.speech.EnglishAccent
import com.example.ui.theme.CdIeltsBorder
import com.example.ui.theme.CdIeltsDarkApexColors
import com.example.ui.theme.CdIeltsHighContrastBg
import com.example.ui.theme.CdIeltsHighContrastText
import com.example.ui.theme.CdIeltsSurface
import com.example.ui.theme.CdIeltsTopbar
import com.example.ui.theme.CdIeltsVisualMode
import com.example.ui.theme.GoldMastery
import com.example.ui.theme.HighlightYellow
import com.example.ui.theme.HighlightYellowBorder
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LightApexColors
import com.example.ui.theme.LocalAdaptiveLayoutInfo
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.RoseAlert
import kotlinx.coroutines.delay
import java.util.Locale

private enum class ActiveExamSection(val labelEn: String) {
    LISTENING("Listening"),
    READING("Reading"),
    WRITING("Writing"),
    SPEAKING("Speaking")
}

private enum class CdReadingTool { HIGHLIGHT, NOTE }

/**
 * Fullscreen Official CD-IELTS Strict Mode Mock Exam Screen (IDP / British Council palette).
 * - Hides Bottom Navigation completely, disables all Animated Teacher/icons & Hints/Dictionary.
 * - Dark slate topbar (#1E293B), Candidate ID, flashing red countdown < 5 min, A-/A+, High Contrast, Submit.
 * - Adaptive split-screen (vertical weight() on Compact portrait; side-by-side Left/Right on >=600dp or landscape)
 *   with draggable resize bar, yellow Highlight (#FEF08A), Notes, Word Count,
 *   play-once Listening lock, and bottom horizontal question palette (Answered / Unanswered / Flagged).
 */
@Composable
fun MockExamScreen(
    session: ActiveExamSession,
    initialVisualMode: CdIeltsVisualMode = CdIeltsVisualMode.LIGHT,
    onSubmitExam: (ExamAttemptRecord) -> Unit,
    onExitWithoutSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exam = session.exam

    val availableSections = remember(session.mode) {
        when (session.mode) {
            ExamSkillMode.FULL_TEST -> listOf(
                ActiveExamSection.LISTENING,
                ActiveExamSection.READING,
                ActiveExamSection.WRITING,
                ActiveExamSection.SPEAKING
            )
            ExamSkillMode.LISTENING -> listOf(ActiveExamSection.LISTENING)
            ExamSkillMode.READING -> listOf(ActiveExamSection.READING)
            ExamSkillMode.WRITING -> listOf(ActiveExamSection.WRITING)
            ExamSkillMode.SPEAKING -> listOf(ActiveExamSection.SPEAKING)
        }
    }

    var currentSection by remember(session) { mutableStateOf(availableSections.first()) }
    val totalDurationSeconds = remember(session.mode) { session.mode.durationMinutes * 60 }
    var remainingSeconds by remember(session) { mutableIntStateOf(totalDurationSeconds) }
    var flashWarningPhase by remember { mutableStateOf(false) }

    // Accessibility & Eye-Care Controls: Font Scale (A- / A+) & 3-Mode Exam Theme (Light / CD-IELTS Dark / High Contrast)
    var fontScale by remember { mutableFloatStateOf(1.0f) }
    var visualMode by remember(session) { mutableStateOf(initialVisualMode) }

    // Draggable vertical split-screen ratio (0.28f..0.72f)
    var splitRatio by remember { mutableFloatStateOf(0.47f) }

    // Reading Highlight (#FEF08A) & Notes state
    var readingTool by remember { mutableStateOf(CdReadingTool.HIGHLIGHT) }
    val highlightedSentences = remember(session) { mutableStateMapOf<String, Boolean>() }
    val sentenceNotes = remember(session) { mutableStateMapOf<String, String>() }
    var noteDialogKey by remember { mutableStateOf<String?>(null) }
    var noteDialogPreview by remember { mutableStateOf("") }
    var noteDialogText by remember { mutableStateOf("") }

    // Candidate responses & Flags
    val answersMap = remember(session) { mutableStateMapOf<String, String>() }
    val flaggedIds = remember(session) { mutableStateMapOf<String, Boolean>() }
    var writingTaskIndex by remember { mutableIntStateOf(0) }
    var writingTask1Draft by remember(session) { mutableStateOf("") }
    var writingTask2Draft by remember(session) { mutableStateOf("") }
    var speakingPromptIndex by remember { mutableIntStateOf(0) }
    var speakingTranscript by remember(session) { mutableStateOf("") }

    // Strict Listening Play-Once state (supports user uploaded MP3/WAV or TTS fallback)
    var listeningHasPlayedOnce by remember(session) { mutableStateOf(false) }
    var listeningIsPlaying by remember(session) { mutableStateOf(false) }
    var listeningLineIndex by remember(session) { mutableIntStateOf(0) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val ttsController = remember(context) { ApexTtsController(context) { } }

    // Speaking recorder in exam room
    var isRecordingSpeaking by remember { mutableStateOf(false) }
    var speakingStatus by remember { mutableStateOf<String?>(null) }
    var activeRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    // Dialogs
    var showSubmitDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    val objectiveQuestions = remember(exam, session.mode) {
        when (session.mode) {
            ExamSkillMode.LISTENING -> exam.questions.filter { it.skill == ExamQuestionSkill.LISTENING }
            ExamSkillMode.READING -> exam.questions.filter { it.skill == ExamQuestionSkill.READING }
            ExamSkillMode.FULL_TEST -> exam.questions
            else -> emptyList()
        }
    }

    val sectionQuestions = remember(objectiveQuestions, currentSection) {
        when (currentSection) {
            ActiveExamSection.LISTENING -> objectiveQuestions.filter { it.skill == ExamQuestionSkill.LISTENING }
            ActiveExamSection.READING -> objectiveQuestions.filter { it.skill == ExamQuestionSkill.READING }
            else -> emptyList()
        }
    }
    var activeQuestionIndex by remember(currentSection) { mutableIntStateOf(0) }

    fun stopAllAudio() {
        ttsController.stop()
        runCatching {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
        mediaPlayer = null
        listeningIsPlaying = false
        activeRecognizer?.stopListening()
        activeRecognizer?.destroy()
        activeRecognizer = null
        isRecordingSpeaking = false
    }

    fun finalizeAndSubmit() {
        stopAllAudio()
        val correctCount = objectiveQuestions.count { q ->
            val ans = answersMap[q.id].orEmpty().trim()
            ans.isNotEmpty() && q.acceptableAnswers.any { it.trim().equals(ans, ignoreCase = true) }
        }
        val combinedWriting = buildString {
            if (writingTask1Draft.isNotBlank()) {
                append("[Task 1]\n").append(writingTask1Draft.trim())
            }
            if (writingTask2Draft.isNotBlank()) {
                if (isNotEmpty()) append("\n\n")
                append("[Task 2]\n").append(writingTask2Draft.trim())
            }
        }
        val writingWords = combinedWriting.trim().split(Regex("\\s+")).count { it.isNotBlank() }

        val estimatedBand = when {
            objectiveQuestions.isNotEmpty() -> ExamScoringHelper.estimateBandFromRatio(correctCount, objectiveQuestions.size)
            session.mode == ExamSkillMode.WRITING -> when {
                writingWords >= 250 -> 7.5
                writingWords >= 150 -> 7.0
                writingWords >= 60 -> 6.5
                else -> 6.0
            }
            else -> if (speakingTranscript.length >= 120) 7.0 else 6.5
        }

        val record = ExamAttemptRecord(
            id = "attempt_${System.currentTimeMillis()}",
            examId = exam.id,
            examTitle = exam.title,
            mode = session.mode,
            submittedAtLabel = ExamScoringHelper.nowFormatted(),
            correctCount = correctCount,
            totalObjectiveQuestions = objectiveQuestions.size,
            estimatedBand = estimatedBand,
            durationUsedSeconds = (totalDurationSeconds - remainingSeconds).coerceAtLeast(1),
            writingWordCount = writingWords,
            writingDraft = combinedWriting,
            speakingTranscript = speakingTranscript.trim(),
            objectiveAnswers = answersMap.toMap(),
            flaggedQuestionIds = flaggedIds.filterValues { it }.keys.toSet()
        )
        onSubmitExam(record)
    }

    // Strict Countdown Timer (flashes red < 5 minutes; auto-submits at 00:00)
    LaunchedEffect(session) {
        while (remainingSeconds > 0) {
            delay(1_000L)
            remainingSeconds -= 1
            if (remainingSeconds < 300) {
                flashWarningPhase = !flashWarningPhase
            }
        }
        if (remainingSeconds == 0) {
            finalizeAndSubmit()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            stopAllAudio()
            ttsController.shutdown()
        }
    }

    // Back press asks confirmation instead of leaving exam silently
    BackHandler { showExitDialog = true }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            activeRecognizer = ApexPronunciationEvaluator.startFreeSpeechRecognition(
                context = context,
                onReady = {
                    isRecordingSpeaking = true
                    speakingStatus = "Đang thu âm Speaking..."
                },
                onPartial = { speakingStatus = "Đang ghi nhận: \"$it\"" },
                onResult = { spoken ->
                    isRecordingSpeaking = false
                    speakingStatus = "Đã ghi nhận câu trả lời."
                    speakingTranscript = if (speakingTranscript.isBlank()) spoken else "${speakingTranscript.trim()} $spoken"
                },
                onError = { err ->
                    isRecordingSpeaking = false
                    speakingStatus = err
                }
            )
        } else {
            speakingStatus = "Chưa cấp quyền Micro. Bạn có thể gõ câu trả lời trực tiếp."
        }
    }

    // Dynamic CD-IELTS Palette (Official Light vs Tailored CD-IELTS Dark vs High Contrast)
    val isDarkOrHc = visualMode != CdIeltsVisualMode.LIGHT
    val topbarBg = when (visualMode) {
        CdIeltsVisualMode.LIGHT -> CdIeltsTopbar
        CdIeltsVisualMode.EXAM_DARK -> CdIeltsDarkApexColors.cdIeltsTopbarBar
        CdIeltsVisualMode.HIGH_CONTRAST -> Color(0xFF0A0A0A)
    }
    val pageBg = when (visualMode) {
        CdIeltsVisualMode.LIGHT -> LightApexColors.cdIeltsSurface
        CdIeltsVisualMode.EXAM_DARK -> CdIeltsDarkApexColors.cdIeltsSurface
        CdIeltsVisualMode.HIGH_CONTRAST -> CdIeltsHighContrastBg
    }
    val cardBg = when (visualMode) {
        CdIeltsVisualMode.LIGHT -> LightApexColors.cdIeltsCard
        CdIeltsVisualMode.EXAM_DARK -> CdIeltsDarkApexColors.cdIeltsCard
        CdIeltsVisualMode.HIGH_CONTRAST -> Color(0xFF111111)
    }
    val borderColor = when (visualMode) {
        CdIeltsVisualMode.LIGHT -> LightApexColors.cdIeltsBorder
        CdIeltsVisualMode.EXAM_DARK -> CdIeltsDarkApexColors.cdIeltsBorder
        CdIeltsVisualMode.HIGH_CONTRAST -> CdIeltsHighContrastText
    }
    val primaryText = when (visualMode) {
        CdIeltsVisualMode.LIGHT -> LightApexColors.inkPrimary
        CdIeltsVisualMode.EXAM_DARK -> CdIeltsDarkApexColors.inkPrimary
        CdIeltsVisualMode.HIGH_CONTRAST -> CdIeltsHighContrastText
    }
    val secondaryText = when (visualMode) {
        CdIeltsVisualMode.LIGHT -> LightApexColors.inkSecondary
        CdIeltsVisualMode.EXAM_DARK -> CdIeltsDarkApexColors.inkSecondary
        CdIeltsVisualMode.HIGH_CONTRAST -> Color(0xFFFEF08A)
    }
    val highlightFill = when (visualMode) {
        CdIeltsVisualMode.LIGHT -> LightApexColors.highlightYellow
        CdIeltsVisualMode.EXAM_DARK -> CdIeltsDarkApexColors.highlightYellow
        CdIeltsVisualMode.HIGH_CONTRAST -> Color(0xFF422006)
    }
    val highlightText = when (visualMode) {
        CdIeltsVisualMode.LIGHT -> Color(0xFF0F172A)
        CdIeltsVisualMode.EXAM_DARK -> Color(0xFFFEF9C3)
        CdIeltsVisualMode.HIGH_CONTRAST -> CdIeltsHighContrastText
    }
    val noteChipBg = when (visualMode) {
        CdIeltsVisualMode.LIGHT -> Color(0xFFFEF9C3)
        CdIeltsVisualMode.EXAM_DARK -> Color(0xFF262D3B)
        CdIeltsVisualMode.HIGH_CONTRAST -> Color(0xFF1F2937)
    }
    val noteChipText = when (visualMode) {
        CdIeltsVisualMode.LIGHT -> Color(0xFF1E293B)
        CdIeltsVisualMode.EXAM_DARK -> Color(0xFFFDE68A)
        CdIeltsVisualMode.HIGH_CONTRAST -> CdIeltsHighContrastText
    }
    val splitHandleBg = when (visualMode) {
        CdIeltsVisualMode.LIGHT -> LightApexColors.cdIeltsDividerBg
        CdIeltsVisualMode.EXAM_DARK -> CdIeltsDarkApexColors.cdIeltsDividerBg
        CdIeltsVisualMode.HIGH_CONTRAST -> Color(0xFF27272A)
    }
    val paletteBg = when (visualMode) {
        CdIeltsVisualMode.LIGHT -> LightApexColors.cdIeltsPaletteBg
        CdIeltsVisualMode.EXAM_DARK -> CdIeltsDarkApexColors.cdIeltsPaletteBg
        CdIeltsVisualMode.HIGH_CONTRAST -> Color(0xFF111111)
    }
    val selectedAccentBg = when (visualMode) {
        CdIeltsVisualMode.LIGHT -> CdIeltsTopbar
        CdIeltsVisualMode.EXAM_DARK -> Color(0xFF2563EB)
        CdIeltsVisualMode.HIGH_CONTRAST -> Color(0xFF334155)
    }
    val isUnderFiveMinutes = remainingSeconds < 300

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(pageBg)
            .testTag("mock_exam_screen")
    ) {
        // OFFICIAL CD-IELTS TOPBAR (Dark Slate / Charcoal, White Text)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(topbarBg)
                .statusBarsPadding()
                .displayCutoutPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Candidate ID & Strict Mode
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(13.dp))
                        Text(
                            text = session.candidateId,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            modifier = Modifier.testTag("exam_candidate_id")
                        )
                    }
                    Text(
                        text = stringResource(R.string.exam_strict_mode_badge),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                        color = Color(0xFF94A3B8)
                    )
                }

                // Flashing Red Countdown Timer (< 5:00)
                val timerBg = when {
                    isUnderFiveMinutes && flashWarningPhase -> RoseAlert
                    isUnderFiveMinutes -> Color(0xFF991B1B)
                    else -> Color(0xFF334155)
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = timerBg,
                    modifier = Modifier
                        .wrapContentWidth()
                        .testTag("exam_countdown_timer")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Rounded.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Text(
                            text = formatExamTimer(remainingSeconds),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Submit Button
                Button(
                    onClick = { showSubmitDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .wrapContentWidth()
                        .testTag("exam_submit_topbar_button")
                ) {
                    Text(
                        text = stringResource(R.string.exam_submit_btn),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // Second Topbar Row: Section Tabs (if Full Test) + A- / A+ + High Contrast
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableSections.forEach { sec ->
                        val active = sec == currentSection
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (active) Color.White else Color(0xFF334155),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { currentSection = sec }
                                .testTag("exam_section_tab_${sec.name.lowercase()}")
                        ) {
                            Text(
                                text = sec.labelEn,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (active) CdIeltsTopbar else Color.White,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // A- / A+ / 3-Mode Exam Theme Controls (Light / CD-IELTS Dark / High Contrast)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TopbarControlChip("A-", "exam_font_decrease") {
                        fontScale = (fontScale - 0.10f).coerceAtLeast(0.85f)
                    }
                    TopbarControlChip("A+", "exam_font_increase") {
                        fontScale = (fontScale + 0.10f).coerceAtMost(1.35f)
                    }
                    val modeChipBg = when (visualMode) {
                        CdIeltsVisualMode.LIGHT -> Color(0xFF334155)
                        CdIeltsVisualMode.EXAM_DARK -> Color(0xFF2563EB)
                        CdIeltsVisualMode.HIGH_CONTRAST -> CdIeltsHighContrastText
                    }
                    val modeChipFg = if (visualMode == CdIeltsVisualMode.HIGH_CONTRAST) Color.Black else Color.White
                    val modeIcon = when (visualMode) {
                        CdIeltsVisualMode.LIGHT -> Icons.Rounded.LightMode
                        CdIeltsVisualMode.EXAM_DARK -> Icons.Rounded.DarkMode
                        CdIeltsVisualMode.HIGH_CONTRAST -> Icons.Rounded.Contrast
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = modeChipBg,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { visualMode = visualMode.next() }
                            .testTag("exam_high_contrast_toggle")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = modeIcon,
                                contentDescription = "Chuyển giao diện phòng thi (${visualMode.fullLabelVi})",
                                tint = modeChipFg,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = visualMode.shortLabelVi,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = modeChipFg
                            )
                        }
                    }
                }
            }
        }

        // ADAPTIVE SPLIT-SCREEN BODY:
        // Compact portrait -> proportional vertical weight() split
        // Medium/Expanded (>= 600dp) or landscape -> side-by-side Left/Right split panes
        val layoutInfo = LocalAdaptiveLayoutInfo.current
        val paneHorizontalPadding = if (layoutInfo.isNarrowCompact) 12.dp else 14.dp
        val primaryPaneContent: @Composable () -> Unit = {
            when (currentSection) {
                        ActiveExamSection.READING -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Reading Highlight & Note Tool Strip
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = exam.readingTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = (16f * fontScale).sp
                                        ),
                                        color = primaryText,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (readingTool == CdReadingTool.HIGHLIGHT) highlightFill else cardBg,
                                        border = BorderStroke(
                                            1.dp,
                                            if (readingTool == CdReadingTool.HIGHLIGHT) HighlightYellowBorder else borderColor
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { readingTool = CdReadingTool.HIGHLIGHT }
                                            .testTag("exam_reading_tool_highlight")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                Icons.Rounded.BorderColor,
                                                contentDescription = null,
                                                tint = if (isDarkOrHc) Color(0xFFFDE047) else Color(0xFF854D0E),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                "Highlight vàng",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (readingTool == CdReadingTool.HIGHLIGHT) highlightText else primaryText
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (readingTool == CdReadingTool.NOTE) splitHandleBg else cardBg,
                                        border = BorderStroke(1.dp, borderColor),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { readingTool = CdReadingTool.NOTE }
                                            .testTag("exam_reading_tool_note")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Rounded.StickyNote2, contentDescription = null, tint = primaryText, modifier = Modifier.size(14.dp))
                                            Text("Ghi chú (Note)", style = MaterialTheme.typography.labelSmall, color = primaryText)
                                        }
                                    }
                                }

                                HorizontalDivider(color = borderColor)

                                exam.readingParagraphs.forEach { para ->
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "Paragraph ${para.label}",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = secondaryText
                                        )
                                        para.sentences.forEachIndexed { sIdx, sentence ->
                                            val key = "${para.label}_$sIdx"
                                            val isHl = highlightedSentences[key] == true
                                            val note = sentenceNotes[key].orEmpty()
                                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (isHl) highlightFill else Color.Transparent)
                                                        .clickable {
                                                            if (readingTool == CdReadingTool.HIGHLIGHT) {
                                                                highlightedSentences[key] = !isHl
                                                            } else {
                                                                noteDialogKey = key
                                                                noteDialogPreview = sentence
                                                                noteDialogText = note
                                                            }
                                                        }
                                                        .padding(horizontal = 4.dp, vertical = 3.dp)
                                                        .testTag("exam_passage_sentence_$key")
                                                ) {
                                                    Text(
                                                        text = sentence,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontFamily = PlusJakartaSansFontFamily,
                                                            fontSize = (14.5f * fontScale).sp,
                                                            lineHeight = (22f * fontScale).sp
                                                        ),
                                                        color = if (isHl) highlightText else primaryText
                                                    )
                                                }
                                                if (note.isNotBlank()) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = noteChipBg,
                                                        border = BorderStroke(1.dp, HighlightYellowBorder)
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Rounded.EditNote,
                                                                contentDescription = null,
                                                                tint = if (isDarkOrHc) Color(0xFFFDE047) else Color(0xFF854D0E),
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                            Text(
                                                                text = "Note: $note",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = noteChipText
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

                        ActiveExamSection.LISTENING -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Rounded.Headphones, contentDescription = null, tint = primaryText, modifier = Modifier.size(20.dp))
                                    Text(
                                        text = exam.listeningTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = (15.5f * fontScale).sp
                                        ),
                                        color = primaryText
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = cardBg,
                                    border = BorderStroke(1.dp, borderColor)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "Quy định thi IELTS Listening thật: Âm thanh chỉ được phát DUY NHẤT 1 LẦN (30 phút nghe + 2 phút kiểm tra đáp án).",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = (12.5f * fontScale).sp),
                                            color = secondaryText
                                        )

                                        if (exam.audioDisplayName != null) {
                                            Text(
                                                text = "Tệp audio đính kèm: ${exam.audioDisplayName}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = primaryText
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                if (!listeningHasPlayedOnce && !listeningIsPlaying) {
                                                    listeningHasPlayedOnce = true
                                                    listeningIsPlaying = true
                                                    val uriStr = exam.audioUri
                                                    if (!uriStr.isNullOrBlank()) {
                                                        val started = runCatching {
                                                            val mp = MediaPlayer().apply {
                                                                setDataSource(context, Uri.parse(uriStr))
                                                                setOnCompletionListener {
                                                                    listeningIsPlaying = false
                                                                    release()
                                                                    mediaPlayer = null
                                                                }
                                                                prepare()
                                                                start()
                                                            }
                                                            mediaPlayer = mp
                                                            true
                                                        }.getOrDefault(false)
                                                        if (!started) {
                                                            ttsController.speakSequence(
                                                                lines = exam.listeningScript,
                                                                accent = EnglishAccent.UK,
                                                                speechRate = 1.0f,
                                                                onLineStarted = { listeningLineIndex = it },
                                                                onComplete = { listeningIsPlaying = false }
                                                            )
                                                        }
                                                    } else {
                                                        ttsController.speakSequence(
                                                            lines = exam.listeningScript,
                                                            accent = EnglishAccent.UK,
                                                            speechRate = 1.0f,
                                                            onLineStarted = { listeningLineIndex = it },
                                                            onComplete = { listeningIsPlaying = false }
                                                        )
                                                    }
                                                }
                                            },
                                            enabled = !listeningHasPlayedOnce,
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = CdIeltsTopbar,
                                                contentColor = Color.White,
                                                disabledContainerColor = Color(0xFF94A3B8)
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .minimumInteractiveComponentSize()
                                                .testTag("exam_listening_play_once_btn")
                                        ) {
                                            Icon(
                                                imageVector = if (listeningHasPlayedOnce) Icons.Rounded.Lock else Icons.Rounded.PlayArrow,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = when {
                                                    listeningIsPlaying -> "Đang phát bài nghe (${listeningLineIndex + 1}/${exam.listeningScript.size}) — Không thể tua lại"
                                                    listeningHasPlayedOnce -> "Đã phát xong 1 lần (Khóa Strict Mode)"
                                                    else -> "Bắt đầu phát Audio Listening (Chỉ nghe 1 lần)"
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        ActiveExamSection.WRITING -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Task 1 (≥150 từ)", "Task 2 (≥250 từ)").forEachIndexed { idx, label ->
                                        val active = writingTaskIndex == idx
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (active) CdIeltsTopbar else cardBg,
                                            border = BorderStroke(1.dp, borderColor),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { writingTaskIndex = idx }
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (active) Color.White else primaryText,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = if (writingTaskIndex == 0) exam.writingTask1Prompt else exam.writingTask2Prompt,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = (14.5f * fontScale).sp,
                                        lineHeight = (22f * fontScale).sp
                                    ),
                                    color = primaryText
                                )
                            }
                        }

                        ActiveExamSection.SPEAKING -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    exam.speakingPrompts.forEachIndexed { idx, _ ->
                                        val active = speakingPromptIndex == idx
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (active) CdIeltsTopbar else cardBg,
                                            border = BorderStroke(1.dp, borderColor),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { speakingPromptIndex = idx }
                                        ) {
                                            Text(
                                                text = "Part ${idx + 1}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (active) Color.White else primaryText,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = exam.speakingPrompts.getOrElse(speakingPromptIndex) { exam.speakingPrompts.firstOrNull().orEmpty() },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = (15f * fontScale).sp,
                                        lineHeight = (22f * fontScale).sp
                                    ),
                                    color = primaryText
                                )
                            }
                        }
            }
        }

        val secondaryPaneContent: @Composable () -> Unit = {
            when (currentSection) {
                        ActiveExamSection.READING, ActiveExamSection.LISTENING -> {
                            val currentQ = sectionQuestions.getOrNull(
                                activeQuestionIndex.coerceIn(0, (sectionQuestions.size - 1).coerceAtLeast(0))
                            )
                            if (currentQ != null) {
                                CdIeltsQuestionPane(
                                    question = currentQ,
                                    answerValue = answersMap[currentQ.id].orEmpty(),
                                    isFlagged = flaggedIds[currentQ.id] == true,
                                    hasPrev = activeQuestionIndex > 0,
                                    hasNext = activeQuestionIndex < sectionQuestions.lastIndex,
                                    fontScale = fontScale,
                                    cardBg = cardBg,
                                    borderColor = borderColor,
                                    primaryText = primaryText,
                                    secondaryText = secondaryText,
                                    onAnswerChange = { answersMap[currentQ.id] = it },
                                    onToggleFlag = { flaggedIds[currentQ.id] = !(flaggedIds[currentQ.id] == true) },
                                    onPrev = { activeQuestionIndex = (activeQuestionIndex - 1).coerceAtLeast(0) },
                                    onNext = { activeQuestionIndex = (activeQuestionIndex + 1).coerceAtMost(sectionQuestions.lastIndex) }
                                )
                            } else {
                                Text(
                                    text = "Phần thi này không có câu hỏi trắc nghiệm. Hãy chuyển sang phần tiếp theo trên thanh trên cùng.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = secondaryText
                                )
                            }
                        }

                        ActiveExamSection.WRITING -> {
                            val currentDraft = if (writingTaskIndex == 0) writingTask1Draft else writingTask2Draft
                            val wordCount = currentDraft.trim().split(Regex("\\s+")).count { it.isNotBlank() }
                            val target = if (writingTaskIndex == 0) 150 else 250
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Khung viết bài — Writing Task ${writingTaskIndex + 1}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = primaryText
                                    )
                                    val wordPillBg = when {
                                        wordCount >= target && isDarkOrHc -> Color(0xFF0E3A2F)
                                        wordCount >= target -> Color(0xFFDCFCE7)
                                        isDarkOrHc -> Color(0xFF1F2937)
                                        else -> Color(0xFFE2E8F0)
                                    }
                                    val wordPillFg = when {
                                        wordCount >= target && isDarkOrHc -> Color(0xFF6EE7B7)
                                        isDarkOrHc -> primaryText
                                        else -> Color(0xFF0F172A)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = wordPillBg,
                                        modifier = Modifier
                                            .wrapContentWidth()
                                            .testTag("exam_writing_word_count")
                                    ) {
                                        Text(
                                            text = "Words: $wordCount / $target",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontFamily = PlusJakartaSansFontFamily,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = wordPillFg,
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = currentDraft,
                                    onValueChange = {
                                        if (writingTaskIndex == 0) writingTask1Draft = it else writingTask2Draft = it
                                    },
                                    minLines = 6,
                                    placeholder = { Text("Type your IELTS Writing response here...", color = secondaryText) },
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = (14.5f * fontScale).sp,
                                        color = primaryText
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = borderColor,
                                        unfocusedBorderColor = borderColor,
                                        focusedContainerColor = cardBg,
                                        unfocusedContainerColor = cardBg
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("exam_writing_editor")
                                )
                            }
                        }

                        ActiveExamSection.SPEAKING -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = {
                                        if (isRecordingSpeaking) {
                                            activeRecognizer?.stopListening()
                                            isRecordingSpeaking = false
                                        } else {
                                            val granted = ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.RECORD_AUDIO
                                            ) == PackageManager.PERMISSION_GRANTED
                                            if (granted) {
                                                activeRecognizer = ApexPronunciationEvaluator.startFreeSpeechRecognition(
                                                    context = context,
                                                    onReady = {
                                                        isRecordingSpeaking = true
                                                        speakingStatus = "Đang thu âm Speaking..."
                                                    },
                                                    onPartial = { speakingStatus = "Đang nghe: \"$it\"" },
                                                    onResult = { spoken ->
                                                        isRecordingSpeaking = false
                                                        speakingStatus = "Đã ghi nhận giọng nói."
                                                        speakingTranscript = if (speakingTranscript.isBlank()) spoken else "${speakingTranscript.trim()} $spoken"
                                                    },
                                                    onError = { err ->
                                                        isRecordingSpeaking = false
                                                        speakingStatus = err
                                                    }
                                                )
                                            } else {
                                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isRecordingSpeaking) RoseAlert else CdIeltsTopbar
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        imageVector = if (isRecordingSpeaking) Icons.Rounded.Stop else Icons.Rounded.Mic,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isRecordingSpeaking) "Dừng thu âm Speaking" else "Bắt đầu thu âm trả lời")
                                }

                                speakingStatus?.let { msg ->
                                    Text(msg, style = MaterialTheme.typography.bodySmall, color = secondaryText)
                                }

                                OutlinedTextField(
                                    value = speakingTranscript,
                                    onValueChange = { speakingTranscript = it },
                                    minLines = 4,
                                    placeholder = { Text("Transcript câu trả lời Speaking của thí sinh...", color = secondaryText) },
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = (14f * fontScale).sp,
                                        color = primaryText
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = borderColor,
                                        unfocusedBorderColor = borderColor,
                                        focusedContainerColor = cardBg,
                                        unfocusedContainerColor = cardBg
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
            }
        }

        val secondaryInsetModifier = if (sectionQuestions.isEmpty()) {
            Modifier.navigationBarsPadding().imePadding()
        } else {
            Modifier.imePadding()
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val density = LocalDensity.current
            val totalHeightPx = with(density) { maxHeight.toPx().coerceAtLeast(1f) }
            val totalWidthPx = with(density) { maxWidth.toPx().coerceAtLeast(1f) }

            if (layoutInfo.useSideBySideSplit) {
                // SIDE-BY-SIDE LEFT/RIGHT SPLIT PANES (Tablets / Medium / Expanded / Landscape)
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(splitRatio)
                            .background(pageBg)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = paneHorizontalPadding, vertical = 10.dp)
                            .testTag("exam_top_pane")
                    ) {
                        primaryPaneContent()
                    }

                    // Draggable vertical divider handle
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(24.dp)
                            .background(splitHandleBg)
                            .pointerInput(totalWidthPx) {
                                detectHorizontalDragGestures { change, dragAmount ->
                                    change.consume()
                                    splitRatio = (splitRatio + dragAmount / totalWidthPx).coerceIn(0.28f, 0.72f)
                                }
                            }
                            .testTag("exam_split_drag_handle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(42.dp)
                                .clip(RoundedCornerShape(50))
                                .background(secondaryText.copy(alpha = 0.55f))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f - splitRatio)
                            .background(pageBg)
                            .then(secondaryInsetModifier)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = paneHorizontalPadding, vertical = 10.dp)
                            .testTag("exam_bottom_pane")
                    ) {
                        secondaryPaneContent()
                    }
                }
            } else {
                // PROPORTIONAL VERTICAL SPLIT-SCREEN (Compact Portrait: 16:9, 18:9, 19.5:9, 20:9, 21:9)
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(splitRatio)
                            .background(pageBg)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = paneHorizontalPadding, vertical = 10.dp)
                            .testTag("exam_top_pane")
                    ) {
                        primaryPaneContent()
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                            .background(splitHandleBg)
                            .pointerInput(totalHeightPx) {
                                detectVerticalDragGestures { change, dragAmount ->
                                    change.consume()
                                    splitRatio = (splitRatio + dragAmount / totalHeightPx).coerceIn(0.25f, 0.75f)
                                }
                            }
                            .testTag("exam_split_drag_handle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DragHandle,
                                contentDescription = "Kéo để thay đổi tỷ lệ màn hình trên dưới",
                                tint = secondaryText,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Kéo lên / xuống để chỉnh tỷ lệ",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = secondaryText,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f - splitRatio)
                            .background(pageBg)
                            .then(secondaryInsetModifier)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = paneHorizontalPadding, vertical = 10.dp)
                            .testTag("exam_bottom_pane")
                    ) {
                        secondaryPaneContent()
                    }
                }
            }
        }

        // BOTTOM HORIZONTAL QUESTION PALETTE (Answered / Unanswered / Flagged)
        if (sectionQuestions.isNotEmpty()) {
            Surface(
                color = paletteBg,
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .testTag("exam_question_palette")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val answeredCount = sectionQuestions.count { answersMap[it.id]?.isNotBlank() == true }
                        val flaggedCount = sectionQuestions.count { flaggedIds[it.id] == true }
                        Text(
                            text = "Đã làm: $answeredCount/${sectionQuestions.size} • Cắm cờ: $flaggedCount",
                            style = MaterialTheme.typography.labelSmall,
                            color = secondaryText
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PaletteLegendDot(selectedAccentBg, "Đã làm", secondaryText)
                            PaletteLegendDot(GoldMastery, "Cờ", secondaryText)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sectionQuestions.forEachIndexed { idx, q ->
                            val isCurrent = idx == activeQuestionIndex
                            val isAnswered = answersMap[q.id]?.isNotBlank() == true
                            val isFlagged = flaggedIds[q.id] == true

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            isCurrent -> Color(0xFF2563EB)
                                            isAnswered -> selectedAccentBg
                                            else -> cardBg
                                        }
                                    )
                                    .clickable { activeQuestionIndex = idx }
                                    .testTag("exam_palette_q_${q.number}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.Transparent,
                                    border = BorderStroke(
                                        width = if (isFlagged || isCurrent) 2.dp else 1.dp,
                                        color = when {
                                            isFlagged -> GoldMastery
                                            isCurrent -> Color(0xFF93C5FD)
                                            else -> borderColor
                                        }
                                    ),
                                    modifier = Modifier.fillMaxSize()
                                ) {}

                                Text(
                                    text = "${q.number}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isCurrent || isAnswered) Color.White else primaryText
                                )

                                if (isFlagged) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(3.dp)
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(GoldMastery)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }

    // Passage Note Editor Dialog
    noteDialogKey?.let { key ->
        AlertDialog(
            onDismissRequest = { noteDialogKey = null },
            containerColor = cardBg,
            titleContentColor = primaryText,
            textContentColor = secondaryText,
            title = { Text("Ghi chú bài thi (CD-IELTS Note)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "\"${noteDialogPreview.take(90)}...\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryText
                    )
                    OutlinedTextField(
                        value = noteDialogText,
                        onValueChange = { noteDialogText = it },
                        placeholder = { Text("Nhập ghi chú nhanh...", color = secondaryText) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = primaryText),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = borderColor,
                            unfocusedBorderColor = borderColor,
                            focusedContainerColor = pageBg,
                            unfocusedContainerColor = pageBg
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = noteDialogText.trim()
                        if (trimmed.isEmpty()) sentenceNotes.remove(key) else sentenceNotes[key] = trimmed
                        noteDialogKey = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = selectedAccentBg, contentColor = Color.White)
                ) { Text("Lưu") }
            },
            dismissButton = {
                TextButton(onClick = { noteDialogKey = null }) { Text("Đóng", color = secondaryText) }
            }
        )
    }

    // Submit Confirmation Dialog
    if (showSubmitDialog) {
        val answered = objectiveQuestions.count { answersMap[it.id]?.isNotBlank() == true }
        val flagged = objectiveQuestions.count { flaggedIds[it.id] == true }
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            containerColor = cardBg,
            titleContentColor = primaryText,
            textContentColor = secondaryText,
            title = { Text("Xác nhận Nộp bài thi?") },
            text = {
                Text(
                    "Bạn đã hoàn thành $answered / ${objectiveQuestions.size} câu trắc nghiệm/điền từ" +
                        (if (flagged > 0) " (còn $flagged câu đang cắm cờ)." else ".") +
                        "\n\nHệ thống sẽ tự động chấm điểm, lưu lịch sử và chuyển thẳng sang Tab 5: SỬA."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        finalizeAndSubmit()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB), contentColor = Color.White),
                    modifier = Modifier.testTag("exam_confirm_submit_btn")
                ) { Text("Nộp bài ngay") }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) { Text("Tiếp tục làm", color = secondaryText) }
            }
        )
    }

    // Exit Confirmation Dialog (Hardware Back)
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            containerColor = cardBg,
            titleContentColor = primaryText,
            textContentColor = secondaryText,
            title = { Text("Thoát phòng thi Strict Mode?") },
            text = { Text("Nếu thoát mà chưa nộp bài, tiến trình lượt thi này sẽ không được lưu. Bạn muốn nộp bài luôn hay thoát?") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        finalizeAndSubmit()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB), contentColor = Color.White)
                ) { Text("Nộp bài & Chấm điểm") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        stopAllAudio()
                        onExitWithoutSubmit()
                    }
                ) { Text("Thoát không lưu", color = RoseAlert) }
            }
        )
    }
}

@Composable
private fun CdIeltsQuestionPane(
    question: ExamQuestionItem,
    answerValue: String,
    isFlagged: Boolean,
    hasPrev: Boolean,
    hasNext: Boolean,
    fontScale: Float,
    cardBg: Color,
    borderColor: Color,
    primaryText: Color,
    secondaryText: Color,
    onAnswerChange: (String) -> Unit,
    onToggleFlag: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CdIeltsTopbar
            ) {
                Text(
                    text = "Question ${question.number} • ${question.type.labelVi}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                )
            }

            val isDarkCard = cardBg != LightApexColors.cdIeltsCard
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when {
                    isFlagged && isDarkCard -> Color(0xFF3A2910)
                    isFlagged -> Color(0xFFFEF3C7)
                    else -> cardBg
                },
                border = BorderStroke(1.dp, if (isFlagged) GoldMastery else borderColor),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onToggleFlag)
                    .testTag("exam_flag_q_${question.number}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    val flagTint = when {
                        isFlagged && isDarkCard -> Color(0xFFFCD34D)
                        isFlagged -> Color(0xFFB45309)
                        else -> secondaryText
                    }
                    Icon(
                        imageVector = if (isFlagged) Icons.Rounded.Flag else Icons.Rounded.OutlinedFlag,
                        contentDescription = "Cắm cờ câu hỏi",
                        tint = flagTint,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = if (isFlagged) "Đã cắm cờ" else "Cắm cờ (Flag)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isFlagged) flagTint else primaryText
                    )
                }
            }
        }

        Text(
            text = question.promptEn,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = PlusJakartaSansFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = (15f * fontScale).sp,
                lineHeight = (22f * fontScale).sp
            ),
            color = primaryText
        )

        if (question.type == ExamQuestionType.GAP_FILL) {
            OutlinedTextField(
                value = answerValue,
                onValueChange = onAnswerChange,
                singleLine = true,
                placeholder = { Text("Nhập đáp án điền từ...", color = secondaryText) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = (14.5f * fontScale).sp,
                    color = primaryText
                ),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = borderColor,
                    unfocusedBorderColor = borderColor,
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("exam_gap_input_${question.number}")
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                question.options.forEach { option ->
                    val selected = answerValue.equals(option, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) CdIeltsTopbar else cardBg,
                        border = BorderStroke(1.dp, if (selected) CdIeltsTopbar else borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onAnswerChange(option) }
                            .testTag("exam_opt_${question.number}_$option")
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = (14f * fontScale).sp
                            ),
                            color = if (selected) Color.White else primaryText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onPrev,
                enabled = hasPrev,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Câu trước", color = primaryText)
            }
            OutlinedButton(
                onClick = onNext,
                enabled = hasNext,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Text("Câu tiếp", color = primaryText)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun TopbarControlChip(label: String, tag: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF334155),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = PlusJakartaSansFontFamily,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun PaletteLegendDot(color: Color, label: String, textColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = textColor)
    }
}

private fun formatExamTimer(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val hrs = safe / 3600
    val mins = (safe % 3600) / 60
    val secs = safe % 60
    return if (hrs > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format(Locale.US, "%02d:%02d", mins, secs)
    }
}
