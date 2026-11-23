package com.example.ui.screens.library

import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ai.WordLookupResult
import com.example.ai.WordLookupService
import com.example.data.IeltsModule
import com.example.data.VocabWordItem
import com.example.data.local.ExtractedAcademicWord
import com.example.data.local.PersonalMaterial
import com.example.data.local.PersonalMaterialAiUiState
import com.example.speech.ApexTtsController
import com.example.speech.EnglishAccent
import com.example.ui.components.AnimatedAudioWaveform
import com.example.ui.components.AudioAbRepeatControlBar
import com.example.ui.components.LocalGlassmorphismDisabled
import com.example.ui.components.tactilePress
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardSurfaceTinted
import com.example.ui.theme.CardWhite
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.GoldMastery
import com.example.ui.theme.GoldMasteryLight
import com.example.ui.theme.GoldOnContainer
import com.example.ui.theme.IndigoOnSurface
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LocalAdaptiveLayoutInfo
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.PracticeTeal
import com.example.ui.theme.PracticeTealLight
import com.example.ui.theme.RoseDiff
import com.example.ui.theme.RoseDiffLight
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.TealOnSurface
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

private data class TappedWordRequest(
    val rawWord: String,
    val surroundingSentence: String,
    val paragraphIndex: Int
)

/**
 * Interactive Self-Study Article Reader Screen (`SelfStudyReaderScreen.kt`).
 * Guardrail: Strictly enforces solid high-contrast surfaces (`LocalGlassmorphismDisabled = true`).
 * Features:
 * 1. Clickable English words inside every article paragraph: tap any word to open a compact
 *    Word Lookup Bottom Sheet (IPA, Vietnamese meaning from local dictionary or Gemini contextual
 *    lookup, UK/US TTS audio, and 1-tap "Lưu vào Flashcard SRS" saving the word + context sentence).
 * 2. Audio / UK TTS reader with playback speed (0.75x, 1.0x, 1.25x) and A–B Repeat Loop.
 * 3. Collapsible AI Summarization & Band 8.5+ extraction + Self-Study Session Stopwatch.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfStudyReaderScreen(
    material: PersonalMaterial,
    linkedModule: IeltsModule?,
    allVocabulary: List<VocabWordItem>,
    aiState: PersonalMaterialAiUiState,
    dailySelfStudyMinutes: Int,
    onEditMaterial: (PersonalMaterial) -> Unit,
    onSaveFlashcardToSrs: (VocabWordItem) -> Unit,
    onRequestAiSummary: (PersonalMaterial) -> Unit,
    onLogSelfStudyMinutes: (materialTitle: String, minutes: Int) -> Unit,
    onShowMessage: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onClose() }

    CompositionLocalProvider(LocalGlassmorphismDisabled provides true) {
        val context = LocalContext.current
        val layoutInfo = LocalAdaptiveLayoutInfo.current
        val scope = rememberCoroutineScope()

        val paragraphs = remember(material.contentEn) { splitIntoParagraphs(material.contentEn) }
        val allSentences = remember(material.contentEn) { splitReaderSentences(material.contentEn) }

        // Interactive Tap-to-Lookup state
        var activeTappedWord by remember(material.id) { mutableStateOf<TappedWordRequest?>(null) }
        var lookupResult by remember(material.id) { mutableStateOf<WordLookupResult?>(null) }
        var isLookingUpAi by remember(material.id) { mutableStateOf(false) }
        var lookupErrorVi by remember(material.id) { mutableStateOf<String?>(null) }
        var savedLookupWords by remember(material.id) { mutableStateOf<Set<String>>(emptySet()) }
        var savedExtractedWords by remember(material.id) { mutableStateOf<Set<String>>(emptySet()) }

        // Font scale for comfortable long-form reading
        var readerFontSizeSp by remember { mutableFloatStateOf(16f) }

        // Audio / TTS + Speed (0.75x, 1.0x, 1.25x) + A-B Repeat Loop state
        var speakingAccent by remember { mutableStateOf<EnglishAccent?>(null) }
        val ttsController = remember(context) {
            ApexTtsController(context) { accent -> speakingAccent = accent }
        }
        var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
        var isAudioPlaying by remember { mutableStateOf(false) }
        var isTtsReading by remember { mutableStateOf(false) }
        var activeTtsLineIdx by remember(material.id) { mutableIntStateOf(0) }
        var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
        var loopPointA by remember(material.id) { mutableStateOf<Int?>(null) }
        var loopPointB by remember(material.id) { mutableStateOf<Int?>(null) }
        val isAbLoopActive = loopPointA != null && loopPointB != null

        // Collapsible secondary sections so 80%+ screen focuses on the article
        var showAiPanel by remember(material.id) { mutableStateOf(false) }
        var showTimerPanel by remember(material.id) { mutableStateOf(false) }

        // Live Self-Study Session Timer
        var isTimerRunning by remember(material.id) { mutableStateOf(false) }
        var elapsedSeconds by remember(material.id) { mutableIntStateOf(0) }
        var selectedQuickMinutes by remember(material.id) { mutableIntStateOf(15) }

        LaunchedEffect(isTimerRunning) {
            while (isTimerRunning) {
                delay(1_000L)
                elapsedSeconds += 1
            }
        }

        DisposableEffect(material.id) {
            onDispose {
                ttsController.shutdown()
                runCatching {
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                }
                mediaPlayer = null
            }
        }

        fun startTtsSequence(startIdx: Int, speed: Float = playbackSpeed) {
            if (allSentences.isEmpty()) {
                onShowMessage("Tài liệu chưa có đoạn tiếng Anh để đọc.")
                return
            }
            isTtsReading = true
            ttsController.speakSequence(
                lines = allSentences,
                startIndex = startIdx.coerceIn(0, allSentences.lastIndex),
                accent = EnglishAccent.UK,
                speechRate = speed,
                loopStartIndex = loopPointA,
                loopEndIndex = loopPointB,
                onLineStarted = { idx -> activeTtsLineIdx = idx },
                onComplete = {
                    isTtsReading = false
                    activeTtsLineIdx = 0
                }
            )
        }

        fun triggerLookup(req: TappedWordRequest, forceAi: Boolean = false) {
            activeTappedWord = req
            lookupErrorVi = null
            val localHit = if (!forceAi) {
                WordLookupService.lookupLocal(
                    rawWord = req.rawWord,
                    contextSentenceEn = req.surroundingSentence,
                    allVocabulary = allVocabulary
                )
            } else {
                null
            }

            if (localHit != null) {
                lookupResult = localHit
                isLookingUpAi = false
            } else {
                if (!forceAi) lookupResult = null
                isLookingUpAi = true
                scope.launch {
                    try {
                        val aiRes = WordLookupService.lookupWithGemini(
                            rawWord = req.rawWord,
                            contextSentenceEn = req.surroundingSentence
                        )
                        if (activeTappedWord?.rawWord.equals(req.rawWord, ignoreCase = true)) {
                            lookupResult = aiRes
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        if (activeTappedWord?.rawWord.equals(req.rawWord, ignoreCase = true)) {
                            lookupErrorVi = e.message ?: "Không thể kết nối AI lúc này. Bạn vẫn có thể lưu từ kèm câu gốc vào SRS."
                            if (lookupResult == null) {
                                val clean = WordLookupService.cleanEnglishWord(req.rawWord)
                                lookupResult = WordLookupResult(
                                    word = clean,
                                    partOfSpeech = "word",
                                    cefrLevel = "C1",
                                    ipaUk = "/$clean/",
                                    ipaUs = "/$clean/",
                                    meaningVi = "Từ mới từ bài đọc (tra AI khi có mạng hoặc tự ghi chú trong Tab Học)",
                                    collocationEn = clean,
                                    contextSentenceEn = req.surroundingSentence,
                                    sourceLabelVi = "Chế độ Offline",
                                    isFromAi = false
                                )
                            }
                        }
                    } finally {
                        isLookingUpAi = false
                    }
                }
            }
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(SurfaceCanvas)
                .navigationBarsPadding()
                .imePadding()
                .testTag("self_study_reader_screen")
        ) {
            // Solid High-Contrast Top Bar
            Surface(
                color = CardWhite,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .displayCutoutPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("reader_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Quay lại thư viện",
                            tint = InkPrimary
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = material.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = InkPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = buildString {
                                append(material.category)
                                if (linkedModule != null) append(" • M${linkedModule.id}")
                                append(" • Chạm từ bất kỳ để tra nghĩa")
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = InkMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // A- / A+ comfortable reading size toggle
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceCanvas,
                        border = BorderStroke(1.dp, CardBorderSoft)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "A-",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = InkPrimary,
                                modifier = Modifier
                                    .clickable { readerFontSizeSp = (readerFontSizeSp - 1.5f).coerceAtLeast(13.5f) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                            Text(
                                text = "A+",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ElectricIndigo,
                                modifier = Modifier
                                    .clickable { readerFontSizeSp = (readerFontSizeSp + 1.5f).coerceAtMost(22f) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { onEditMaterial(material) },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("reader_edit_material_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Chỉnh sửa tài liệu",
                            tint = ElectricIndigo,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            // Main Scrollable Solid High-Contrast Reader Body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = layoutInfo.horizontalPadding, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Compact Audio / TTS Player + Speed (0.75x, 1.0x, 1.25x) + A–B Repeat Loop
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CardWhite,
                    border = BorderStroke(1.dp, CardBorderSoft),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!material.audioUri.isNullOrBlank()) {
                                Button(
                                    onClick = {
                                        if (isAudioPlaying) {
                                            runCatching {
                                                mediaPlayer?.pause()
                                                isAudioPlaying = false
                                            }
                                        } else {
                                            runCatching {
                                                val mp = mediaPlayer ?: MediaPlayer().apply {
                                                    setDataSource(context, Uri.parse(material.audioUri))
                                                    setOnCompletionListener { isAudioPlaying = false }
                                                    prepare()
                                                }
                                                mediaPlayer = mp
                                                runCatching {
                                                    mp.playbackParams = mp.playbackParams.setSpeed(playbackSpeed)
                                                }
                                                mp.start()
                                                isAudioPlaying = true
                                            }.onFailure {
                                                onShowMessage("Không thể phát tệp audio này. Đang dùng giọng đọc TTS thay thế.")
                                                startTtsSequence(activeTtsLineIdx)
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PracticeTeal,
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isAudioPlaying) Icons.Rounded.Pause else Icons.Rounded.Headphones,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (isAudioPlaying) "Tạm dừng" else "Audio gốc",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    if (isTtsReading) {
                                        ttsController.stop()
                                        isTtsReading = false
                                    } else {
                                        startTtsSequence(loopPointA ?: activeTtsLineIdx)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reader_tts_toggle_btn")
                            ) {
                                Icon(
                                    imageVector = if (isTtsReading) Icons.Rounded.Stop else Icons.Rounded.RecordVoiceOver,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isTtsReading) {
                                        "Dừng (Câu ${activeTtsLineIdx + 1}/${allSentences.size.coerceAtLeast(1)})"
                                    } else {
                                        "Nghe đọc bài (UK)"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (allSentences.isNotEmpty()) {
                            AudioAbRepeatControlBar(
                                playbackSpeed = playbackSpeed,
                                onSelectSpeed = { newSpeed ->
                                    playbackSpeed = newSpeed
                                    if (isAudioPlaying) {
                                        runCatching {
                                            mediaPlayer?.let { mp ->
                                                mp.playbackParams = mp.playbackParams.setSpeed(newSpeed)
                                            }
                                        }
                                    }
                                    if (isTtsReading) {
                                        startTtsSequence(activeTtsLineIdx, newSpeed)
                                    }
                                },
                                pointALabel = loopPointA?.let { "Câu ${it + 1}" },
                                pointBLabel = loopPointB?.let { "Câu ${it + 1}" },
                                isLoopActive = isAbLoopActive,
                                onSetPointA = {
                                    val a = activeTtsLineIdx.coerceIn(0, allSentences.lastIndex)
                                    loopPointA = a
                                    if (loopPointB != null && loopPointB!! < a) {
                                        loopPointB = a
                                    }
                                    ttsController.updateSequenceLoop(loopPointA, loopPointB)
                                    onShowMessage("Đã đặt Điểm A tại Câu ${a + 1}")
                                },
                                onSetPointB = {
                                    val startA = (loopPointA ?: 0).coerceIn(0, allSentences.lastIndex)
                                    val b = activeTtsLineIdx.coerceIn(startA, allSentences.lastIndex)
                                    loopPointA = startA
                                    loopPointB = b
                                    if (isTtsReading) {
                                        ttsController.updateSequenceLoop(startA, b)
                                    } else {
                                        startTtsSequence(startA)
                                    }
                                    onShowMessage("Đang lặp A–B từ Câu ${startA + 1} đến Câu ${b + 1}")
                                },
                                onClearLoop = {
                                    loopPointA = null
                                    loopPointB = null
                                    ttsController.updateSequenceLoop(null, null)
                                    onShowMessage("Đã tắt lặp A–B")
                                },
                                accentColor = ElectricIndigo,
                                testTagPrefix = "reader_ab"
                            )
                        }
                    }
                }

                // 2. Interactive Tap-to-Lookup Article Body (Solid High-Contrast Card)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardWhite,
                    border = BorderStroke(1.dp, CardBorderSoft),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reader_interactive_article_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                    imageVector = Icons.Rounded.TouchApp,
                                    contentDescription = null,
                                    tint = ElectricIndigo,
                                    modifier = Modifier.size(17.dp)
                                )
                                Text(
                                    text = "Chạm trực tiếp vào bất kỳ từ tiếng Anh nào để tra & lưu SRS",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = IndigoOnSurface
                                )
                            }
                        }

                        HorizontalDivider(color = CardBorderSoft)

                        if (paragraphs.isEmpty()) {
                            Text(
                                text = "Chưa có nội dung tiếng Anh. Nhấn biểu tượng chỉnh sửa ở góc trên để dán bài báo học thuật hoặc bài đọc IELTS.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkMuted
                            )
                        } else {
                            paragraphs.forEachIndexed { pIdx, paragraph ->
                                ClickableWordsParagraph(
                                    paragraph = paragraph,
                                    paragraphIndex = pIdx,
                                    selectedWord = if (activeTappedWord?.paragraphIndex == pIdx) activeTappedWord?.rawWord else null,
                                    savedWords = savedLookupWords,
                                    fontSizeSp = readerFontSizeSp,
                                    onWordTapped = { word, surroundingSentence ->
                                        triggerLookup(
                                            TappedWordRequest(
                                                rawWord = word,
                                                surroundingSentence = surroundingSentence,
                                                paragraphIndex = pIdx
                                            ),
                                            forceAi = false
                                        )
                                    }
                                )
                            }
                        }

                        if (material.notesVi.isNotBlank()) {
                            HorizontalDivider(color = CardBorderSoft)
                            Text(
                                text = "Ghi chú tiếng Việt:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = IndigoOnSurface
                            )
                            Text(
                                text = material.notesVi,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = InkSecondary
                            )
                        }
                    }
                }

                // 3. Collapsible AI Summary & Band 8.5+ Vocab Extraction Panel
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CardWhite,
                    border = BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.30f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAiPanel = !showAiPanel },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = ElectricIndigo,
                                    modifier = Modifier.size(19.dp)
                                )
                                Text(
                                    text = stringResource(R.string.library_ai_summarize_btn),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = InkPrimary
                                )
                            }
                            Icon(
                                imageVector = if (showAiPanel) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = null,
                                tint = InkSecondary
                            )
                        }

                        AnimatedVisibility(visible = showAiPanel) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                val isLoadingThis = aiState is PersonalMaterialAiUiState.Loading && aiState.materialId == material.id
                                Button(
                                    onClick = { onRequestAiSummary(material) },
                                    enabled = !isLoadingThis && (material.contentEn.isNotBlank() || material.notesVi.isNotBlank()),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElectricIndigo,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("reader_trigger_ai_summary")
                                ) {
                                    if (isLoadingThis) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Gemini AI đang phân tích...")
                                    } else {
                                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Tóm tắt & Rút từ C1–C2")
                                    }
                                }

                                if (aiState is PersonalMaterialAiUiState.Error && aiState.materialId == material.id) {
                                    Surface(shape = RoundedCornerShape(12.dp), color = RoseDiffLight) {
                                        Text(
                                            text = aiState.messageVi,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = RoseDiff,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }

                                if (aiState is PersonalMaterialAiUiState.Success && aiState.result.materialId == material.id) {
                                    val res = aiState.result
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = ElectricIndigoLight.copy(alpha = 0.65f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = res.summaryVi,
                                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                                                color = InkPrimary
                                            )
                                            res.keyTakeawaysVi.forEach { bullet ->
                                                Text(
                                                    text = "• $bullet",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = InkSecondary
                                                )
                                            }
                                        }
                                    }

                                    res.extractedWords.forEach { item ->
                                        val alreadySaved = savedExtractedWords.contains(item.word.lowercase(Locale.US))
                                        ReaderExtractedWordRow(
                                            item = item,
                                            saved = alreadySaved,
                                            onSave = {
                                                onSaveFlashcardToSrs(
                                                    WordLookupResult(
                                                        word = item.word,
                                                        partOfSpeech = item.partOfSpeech,
                                                        cefrLevel = item.cefrLevel,
                                                        ipaUk = item.ipaUk,
                                                        ipaUs = item.ipaUk,
                                                        meaningVi = item.meaningVi,
                                                        collocationEn = item.collocationEn,
                                                        contextSentenceEn = item.exampleEn,
                                                        sourceLabelVi = "AI rút từ bài đọc",
                                                        isFromAi = true
                                                    ).toVocabWordItem(material.moduleId)
                                                )
                                                savedExtractedWords = savedExtractedWords + item.word.lowercase(Locale.US)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Collapsible Self-Study Stopwatch Logger
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CardWhite,
                    border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTimerPanel = !showTimerPanel },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Rounded.Timer, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(19.dp))
                                Text(
                                    text = "Bấm giờ buổi tự học (Hôm nay: ${dailySelfStudyMinutes}p)",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = InkPrimary
                                )
                            }
                            Icon(
                                imageVector = if (showTimerPanel) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = null,
                                tint = InkSecondary
                            )
                        }

                        AnimatedVisibility(visible = showTimerPanel) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = String.format(Locale.US, "%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60),
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = InkPrimary
                                    )
                                    OutlinedButton(
                                        onClick = { isTimerRunning = !isTimerRunning },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isTimerRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isTimerRunning) "Tạm dừng" else "Bấm giờ")
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(10, 15, 25, 45).forEach { mins ->
                                        val selected = selectedQuickMinutes == mins
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = if (selected) EmeraldSuccess else CardSurfaceTinted,
                                            border = BorderStroke(1.dp, if (selected) EmeraldSuccess else CardBorderSoft),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .clickable { selectedQuickMinutes = mins }
                                        ) {
                                            Text(
                                                text = "+${mins}p",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (selected) Color.White else InkSecondary,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                val minutesToLog = if (elapsedSeconds >= 60) {
                                    (elapsedSeconds / 60).coerceAtLeast(1)
                                } else {
                                    selectedQuickMinutes
                                }

                                Button(
                                    onClick = {
                                        isTimerRunning = false
                                        elapsedSeconds = 0
                                        onLogSelfStudyMinutes(material.title, minutesToLog)
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                ) {
                                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ghi nhận buổi tự học (${minutesToLog} phút)")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Compact Tap-to-Lookup Bottom Sheet
        val tappedReq = activeTappedWord
        if (tappedReq != null) {
            WordLookupBottomSheet(
                rawWord = tappedReq.rawWord,
                surroundingSentence = tappedReq.surroundingSentence,
                result = lookupResult,
                isLoadingAi = isLookingUpAi,
                errorVi = lookupErrorVi,
                isAlreadySaved = savedLookupWords.contains(
                    WordLookupService.cleanEnglishWord(tappedReq.rawWord).lowercase(Locale.US)
                ),
                speakingAccent = speakingAccent,
                onSpeak = { wordText, accent -> ttsController.speak(wordText, accent) },
                onRequestContextualAi = { triggerLookup(tappedReq, forceAi = true) },
                onSaveToSrs = { resolved ->
                    onSaveFlashcardToSrs(resolved.toVocabWordItem(material.moduleId))
                    savedLookupWords = savedLookupWords + resolved.word.lowercase(Locale.US)
                },
                onDismiss = {
                    activeTappedWord = null
                    lookupResult = null
                    lookupErrorVi = null
                }
            )
        }
    }
}

@Composable
private fun ClickableWordsParagraph(
    paragraph: String,
    paragraphIndex: Int,
    selectedWord: String?,
    savedWords: Set<String>,
    fontSizeSp: Float,
    onWordTapped: (word: String, surroundingSentence: String) -> Unit
) {
    var layoutResult by remember(paragraph, fontSizeSp) { mutableStateOf<TextLayoutResult?>(null) }

    val selectedTextColor = ElectricIndigo
    val selectedBgColor = ElectricIndigoLight
    val savedTextColor = EmeraldOnContainer
    val savedBgColor = EmeraldSuccessLight

    val annotated = remember(
        paragraph,
        selectedWord,
        savedWords,
        selectedTextColor,
        selectedBgColor,
        savedTextColor,
        savedBgColor
    ) {
        buildAnnotatedString {
            append(paragraph)
            val wordRegex = Regex("[A-Za-z][A-Za-z'-]*")
            wordRegex.findAll(paragraph).forEach { match ->
                val token = match.value
                val lower = token.lowercase(Locale.US)
                val isSelected = selectedWord != null && lower == WordLookupService.cleanEnglishWord(selectedWord).lowercase(Locale.US)
                val isSaved = savedWords.contains(lower)
                when {
                    isSelected -> {
                        addStyle(
                            style = SpanStyle(
                                color = selectedTextColor,
                                background = selectedBgColor,
                                fontWeight = FontWeight.Bold
                            ),
                            start = match.range.first,
                            end = match.range.last + 1
                        )
                    }

                    isSaved -> {
                        addStyle(
                            style = SpanStyle(
                                color = savedTextColor,
                                background = savedBgColor,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = TextDecoration.Underline
                            ),
                            start = match.range.first,
                            end = match.range.last + 1
                        )
                    }
                }
            }
        }
    }

    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontFamily = PlusJakartaSansFontFamily,
            fontSize = fontSizeSp.sp,
            lineHeight = (fontSizeSp * 1.55f).sp
        ),
        color = InkPrimary,
        onTextLayout = { layoutResult = it },
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(paragraph) {
                detectTapGestures { tapOffset ->
                    val layout = layoutResult ?: return@detectTapGestures
                    val charOffset = layout.getOffsetForPosition(tapOffset)
                    val extracted = extractWordAndSentenceAt(paragraph, charOffset)
                    if (extracted != null) {
                        onWordTapped(extracted.first, extracted.second)
                    }
                }
            }
            .testTag("reader_paragraph_$paragraphIndex")
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WordLookupBottomSheet(
    rawWord: String,
    surroundingSentence: String,
    result: WordLookupResult?,
    isLoadingAi: Boolean,
    errorVi: String?,
    isAlreadySaved: Boolean,
    speakingAccent: EnglishAccent?,
    onSpeak: (String, EnglishAccent) -> Unit,
    onRequestContextualAi: () -> Unit,
    onSaveToSrs: (WordLookupResult) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val displayWord = result?.word ?: WordLookupService.cleanEnglishWord(rawWord)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardWhite,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        modifier = Modifier.testTag("word_lookup_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Word + CEFR/POS + TTS buttons + Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = displayWord,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = ElectricIndigo
                        )
                        if (result != null) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = GoldMasteryLight
                            ) {
                                Text(
                                    text = "${result.cefrLevel} • ${result.partOfSpeech}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = GoldOnContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    if (result != null) {
                        Text(
                            text = "UK ${result.ipaUk}   •   US ${result.ipaUs}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Medium
                            ),
                            color = InkSecondary
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Đóng tra từ", tint = InkSecondary)
                }
            }

            // UK & US TTS Audio Strip + Contextual Gemini Lookup button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (speakingAccent == EnglishAccent.UK) ElectricIndigoLight else SurfaceCanvas,
                    border = BorderStroke(1.dp, if (speakingAccent == EnglishAccent.UK) ElectricIndigo else CardBorderSoft),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSpeak(displayWord, EnglishAccent.UK) }
                        .testTag("lookup_tts_uk")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Nghe UK", tint = ElectricIndigo, modifier = Modifier.size(16.dp))
                        Text("UK", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = ElectricIndigo)
                        if (speakingAccent == EnglishAccent.UK) {
                            AnimatedAudioWaveform(isActive = true, barCount = 3, width = 14.dp, height = 10.dp, primaryColor = ElectricIndigo)
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (speakingAccent == EnglishAccent.US) ElectricIndigoLight else SurfaceCanvas,
                    border = BorderStroke(1.dp, if (speakingAccent == EnglishAccent.US) ElectricIndigo else CardBorderSoft),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSpeak(displayWord, EnglishAccent.US) }
                        .testTag("lookup_tts_us")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Nghe US", tint = PracticeTeal, modifier = Modifier.size(16.dp))
                        Text("US", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = TealOnSurface)
                        if (speakingAccent == EnglishAccent.US) {
                            AnimatedAudioWaveform(isActive = true, barCount = 3, width = 14.dp, height = 10.dp, primaryColor = PracticeTeal)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (result != null && !result.isFromAi && !isLoadingAi) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = ElectricIndigoLight,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onRequestContextualAi)
                            .testTag("lookup_gemini_context_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = ElectricIndigo, modifier = Modifier.size(14.dp))
                            Text(
                                text = "Tra sâu AI",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ElectricIndigo
                            )
                        }
                    }
                }
            }

            // Meaning & Collocation Panel
            if (isLoadingAi) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceCanvas,
                    border = BorderStroke(1.dp, CardBorderSoft),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp), color = ElectricIndigo)
                        Text(
                            text = "Gemini AI đang tra nghĩa & phiên âm theo đúng ngữ cảnh câu...",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                }
            } else if (result != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceCanvas,
                    border = BorderStroke(1.dp, CardBorderSoft),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(13.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = result.meaningVi,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = InkPrimary
                        )
                        if (result.collocationEn.isNotBlank()) {
                            Text(
                                text = "Collocation: ${result.collocationEn}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = TealOnSurface
                            )
                        }
                        Text(
                            text = result.sourceLabelVi,
                            style = MaterialTheme.typography.labelSmall,
                            color = InkMuted
                        )
                    }
                }
            }

            if (errorVi != null) {
                Text(
                    text = errorVi,
                    style = MaterialTheme.typography.bodySmall,
                    color = RoseDiff
                )
            }

            // Context Sentence from Article
            if (surroundingSentence.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CardSurfaceTinted,
                    border = BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(11.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Câu ngữ cảnh sẽ lưu kèm Flashcard:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = IndigoOnSurface
                        )
                        Text(
                            text = "\"$surroundingSentence\"",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontStyle = FontStyle.Italic,
                                lineHeight = 19.sp
                            ),
                            color = InkPrimary
                        )
                    }
                }
            }

            // 1-Tap "Lưu vào Flashcard SRS" Primary CTA
            Button(
                onClick = {
                    val toSave = result ?: WordLookupResult(
                        word = displayWord,
                        partOfSpeech = "word",
                        cefrLevel = "C1",
                        ipaUk = "/$displayWord/",
                        ipaUs = "/$displayWord/",
                        meaningVi = "Từ vựng trích từ bài đọc tự học",
                        collocationEn = displayWord,
                        contextSentenceEn = surroundingSentence,
                        sourceLabelVi = "Bài đọc Tự học"
                    )
                    onSaveToSrs(toSave)
                },
                enabled = !isLoadingAi && !isAlreadySaved,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAlreadySaved) EmeraldSuccess else ElectricIndigo,
                    contentColor = Color.White,
                    disabledContainerColor = if (isAlreadySaved) EmeraldSuccessLight else CardBorderSoft,
                    disabledContentColor = if (isAlreadySaved) EmeraldOnContainer else InkMuted
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .tactilePress()
                    .testTag("lookup_save_to_srs_btn")
            ) {
                Icon(
                    imageVector = if (isAlreadySaved) Icons.Rounded.CheckCircle else Icons.Rounded.BookmarkAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAlreadySaved) "Đã lưu vào Flashcard SRS ✓" else "Lưu vào Flashcard SRS",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun ReaderExtractedWordRow(
    item: ExtractedAcademicWord,
    saved: Boolean,
    onSave: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CardSurfaceTinted,
        border = BorderStroke(1.dp, if (saved) EmeraldSuccess.copy(alpha = 0.5f) else CardBorderSoft)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "${item.word} (${item.cefrLevel} • ${item.partOfSpeech})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = InkPrimary
                )
                Text(
                    text = item.meaningVi,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = if (saved) EmeraldSuccessLight else ElectricIndigo,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(enabled = !saved, onClick = onSave)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (saved) Icons.Rounded.CheckCircle else Icons.Rounded.AddCircleOutline,
                        contentDescription = null,
                        tint = if (saved) EmeraldOnContainer else Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (saved) "Đã lưu" else "Lưu SRS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (saved) EmeraldOnContainer else Color.White
                    )
                }
            }
        }
    }
}

private fun splitIntoParagraphs(content: String): List<String> {
    return content
        .replace("\r\n", "\n")
        .split(Regex("\n+"))
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}

private fun splitReaderSentences(content: String): List<String> {
    if (content.isBlank()) return emptyList()
    return content
        .replace("\r\n", "\n")
        .split(Regex("(?<=[.!?])\\s+|\n+"))
        .map { it.trim() }
        .filter { it.length >= 6 }
}

private fun extractWordAndSentenceAt(paragraph: String, offset: Int): Pair<String, String>? {
    if (paragraph.isEmpty()) return null
    val idx = offset.coerceIn(0, paragraph.lastIndex)

    fun isWordChar(c: Char): Boolean = c.isLetter() || c == '\'' || c == '-'

    var start = idx
    var end = idx
    if (!isWordChar(paragraph[idx])) {
        // Check immediate left/right neighbor for forgiving tap precision
        when {
            idx > 0 && isWordChar(paragraph[idx - 1]) -> {
                start = idx - 1
                end = idx - 1
            }
            idx + 1 < paragraph.length && isWordChar(paragraph[idx + 1]) -> {
                start = idx + 1
                end = idx + 1
            }
            else -> return null
        }
    }

    while (start > 0 && isWordChar(paragraph[start - 1])) start--
    while (end + 1 < paragraph.length && isWordChar(paragraph[end + 1])) end++

    val rawWord = WordLookupService.cleanEnglishWord(paragraph.substring(start, end + 1))
    if (rawWord.length < 2) return null

    // Extract surrounding sentence bounded by '.', '!', '?' or paragraph start/end
    var sStart = start
    while (sStart > 0 && paragraph[sStart - 1] !in charArrayOf('.', '!', '?', '\n')) {
        sStart--
    }
    var sEnd = end
    while (sEnd + 1 < paragraph.length && paragraph[sEnd] !in charArrayOf('.', '!', '?', '\n')) {
        sEnd++
    }
    val sentence = paragraph.substring(sStart, (sEnd + 1).coerceAtMost(paragraph.length)).trim()
    return rawWord to sentence
}
