package com.example.ui.screens.learn

import android.Manifest
import android.content.pm.PackageManager
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.BookmarkAdded
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Flip
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.TeacherMood
import com.example.data.VocabWordItem
import com.example.speech.ApexPronunciationEvaluator
import com.example.speech.ApexTtsController
import com.example.speech.EnglishAccent
import com.example.speech.PronunciationResult
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.ui.components.AnimatedAudioWaveform
import com.example.ui.components.AnimatedTeacher
import com.example.ui.components.GlassSurface
import com.example.ui.components.shouldReduceMotion
import com.example.ui.components.tactilePress
import kotlinx.coroutines.launch
import kotlin.math.abs
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FilterListOff
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay
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
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.VividViolet

private enum class VocabStatusFilter(val labelVi: String) {
    ALL("Tất cả"),
    UNLEARNED("Chưa thuộc"),
    REVIEWING("Đang ôn"),
    LEARNED("Đã thuộc")
}

@Composable
fun FlashcardSubTab(
    words: List<VocabWordItem>,
    allVocabulary: List<VocabWordItem> = words,
    activeModuleId: Int = 1,
    selectedIndex: Int,
    srsWordIds: Set<String>,
    learnedVocabIds: Set<String> = emptySet(),
    dailyLearnedWordIds: Set<String> = emptySet(),
    speakingAccent: EnglishAccent?,
    ttsController: ApexTtsController,
    topicHintEn: String = "",
    isOnline: Boolean = true,
    googleCseApiKey: String = "",
    googleCseCx: String = "",
    onSelectIndex: (Int) -> Unit,
    onWordFlipped: (VocabWordItem) -> Unit,
    onAddToSrs: (String) -> Unit,
    onBuildSentenceWithWord: (VocabWordItem) -> Unit,
    onShowMessage: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val fullPool = remember(allVocabulary, words) {
        if (allVocabulary.isNotEmpty()) allVocabulary else words
    }
    if (fullPool.isEmpty()) return

    // Fast 300ms debounced search + multi-criteria filters (Status + Module 1–12)
    var rawQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf(VocabStatusFilter.ALL) }
    var selectedModuleFilter by remember(activeModuleId) { mutableStateOf<Int?>(activeModuleId) }
    var showModuleStrip by remember { mutableStateOf(false) }

    LaunchedEffect(rawQuery) {
        delay(300L)
        debouncedQuery = rawQuery.trim()
    }

    val isUsingCustomFilter = debouncedQuery.isNotBlank() ||
        statusFilter != VocabStatusFilter.ALL ||
        selectedModuleFilter != activeModuleId

    val filteredWords = remember(
        fullPool,
        words,
        selectedModuleFilter,
        activeModuleId,
        debouncedQuery,
        statusFilter,
        srsWordIds,
        learnedVocabIds,
        dailyLearnedWordIds
    ) {
        val byModule = when {
            selectedModuleFilter == null -> fullPool
            selectedModuleFilter == activeModuleId -> {
                val matched = fullPool.filter { it.moduleId == activeModuleId }
                matched.ifEmpty { words }
            }
            else -> fullPool.filter { it.moduleId == selectedModuleFilter }
        }
        val byStatus = when (statusFilter) {
            VocabStatusFilter.ALL -> byModule
            VocabStatusFilter.UNLEARNED -> byModule.filter {
                !learnedVocabIds.contains(it.id) && !dailyLearnedWordIds.contains(it.id)
            }
            VocabStatusFilter.REVIEWING -> byModule.filter { srsWordIds.contains(it.id) }
            VocabStatusFilter.LEARNED -> byModule.filter {
                learnedVocabIds.contains(it.id) || dailyLearnedWordIds.contains(it.id)
            }
        }
        if (debouncedQuery.isBlank()) {
            byStatus
        } else {
            val q = debouncedQuery.lowercase()
            byStatus.filter { item ->
                item.word.lowercase().contains(q) ||
                    item.meaningVi.lowercase().contains(q) ||
                    item.exampleEn.lowercase().contains(q) ||
                    item.cefrLevel.lowercase().contains(q) ||
                    item.wordFamily.lowercase().contains(q)
            }
        }
    }

    var localFilteredIndex by remember(selectedModuleFilter, statusFilter, debouncedQuery) {
        mutableIntStateOf(0)
    }

    val effectiveIndex = if (isUsingCustomFilter) {
        localFilteredIndex.coerceIn(0, (filteredWords.size - 1).coerceAtLeast(0))
    } else {
        selectedIndex.coerceIn(0, (filteredWords.size - 1).coerceAtLeast(0))
    }

    fun selectDisplayIndex(newIndex: Int) {
        if (filteredWords.isEmpty()) return
        val clamped = newIndex.coerceIn(0, filteredWords.lastIndex)
        if (isUsingCustomFilter) {
            localFilteredIndex = clamped
        } else {
            onSelectIndex(clamped)
        }
    }

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Fast Debounced Search + Status & Module 1–12 Filter Bar
        VocabSearchAndFilterCard(
            rawQuery = rawQuery,
            onQueryChange = { rawQuery = it },
            statusFilter = statusFilter,
            onSelectStatus = { statusFilter = it },
            selectedModuleFilter = selectedModuleFilter,
            activeModuleId = activeModuleId,
            showModuleStrip = showModuleStrip,
            onToggleModuleStrip = { showModuleStrip = !showModuleStrip },
            onSelectModule = { selectedModuleFilter = it },
            resultCount = filteredWords.size,
            totalCount = fullPool.size
        )

        if (filteredWords.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, CardBorderSoft),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vocab_filter_empty_state")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FilterListOff,
                        contentDescription = null,
                        tint = ElectricIndigo,
                        modifier = Modifier.size(34.dp)
                    )
                    Text(
                        text = "Không có từ vựng khớp bộ lọc",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = InkPrimary
                    )
                    Text(
                        text = if (selectedModuleFilter != null && debouncedQuery.isNotBlank()) {
                            "Thử mở rộng tìm kiếm sang toàn bộ 12 Module hoặc xóa bộ lọc trạng thái."
                        } else {
                            "Hãy đổi từ khóa hoặc chọn lại trạng thái để xem danh sách từ."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (selectedModuleFilter != null && debouncedQuery.isNotBlank()) {
                            OutlinedButton(
                                onClick = { selectedModuleFilter = null },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, ElectricIndigo)
                            ) {
                                Text("Tìm cả 12 Module", color = ElectricIndigo)
                            }
                        }
                        Button(
                            onClick = {
                                rawQuery = ""
                                debouncedQuery = ""
                                statusFilter = VocabStatusFilter.ALL
                                selectedModuleFilter = activeModuleId
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                            modifier = Modifier.testTag("vocab_clear_filters_btn")
                        ) {
                            Text("Xóa bộ lọc")
                        }
                    }
                }
            }
        } else {

        // Virtualized Quick-Jump Strip when browsing large filtered/searched sets
        if (filteredWords.size > 1 && isUsingCustomFilter) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vocab_quick_jump_row")
            ) {
                itemsIndexed(filteredWords, key = { _, item -> item.id }) { idx, item ->
                    val isSelected = idx == effectiveIndex
                    val isLearned = learnedVocabIds.contains(item.id) || dailyLearnedWordIds.contains(item.id)
                    val isSrs = srsWordIds.contains(item.id)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) ElectricIndigo else CardWhite,
                        border = BorderStroke(
                            1.dp,
                            when {
                                isSelected -> ElectricIndigo
                                isSrs -> GoldMastery
                                isLearned -> EmeraldSuccess.copy(alpha = 0.65f)
                                else -> CardBorderSoft
                            }
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { selectDisplayIndex(idx) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = item.word,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) Color.White else InkPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = "M${item.moduleId}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                color = if (isSelected) Color.White.copy(alpha = 0.82f) else InkMuted
                            )
                        }
                    }
                }
            }
        }

        val word = filteredWords[effectiveIndex]

        var isFlipped by remember(word.id) { mutableStateOf(false) }
        var isListeningVoice by remember(word.id) { mutableStateOf(false) }
        var pronunciationResult by remember(word.id) { mutableStateOf<PronunciationResult?>(null) }
        var voiceErrorVi by remember(word.id) { mutableStateOf<String?>(null) }
        var activeRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

        // Clean up SpeechRecognizer when word changes or composable leaves composition
        DisposableEffect(word.id) {
            onDispose {
                activeRecognizer?.destroy()
                activeRecognizer = null
                isListeningVoice = false
            }
        }

        val startVoiceCheck = {
            voiceErrorVi = null
            pronunciationResult = null
            activeRecognizer?.destroy()
            isListeningVoice = true
            activeRecognizer = ApexPronunciationEvaluator.startListening(
                context = context,
                targetWord = word.word,
                pronunciationFocusVi = word.pronunciationFocusVi,
                onReady = { isListeningVoice = true },
                onResult = { result ->
                    isListeningVoice = false
                    pronunciationResult = result
                    if (!result.isStrongPass) {
                        onAddToSrs(word.id)
                    }
                },
                onError = { err ->
                    isListeningVoice = false
                    voiceErrorVi = err
                }
            )
            if (activeRecognizer == null) {
                isListeningVoice = false
            }
        }

        val audioPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                startVoiceCheck()
            } else {
                voiceErrorVi = "Vui lòng cấp quyền Micro để Trợ giảng AI lắng nghe và chấm điểm phát âm của bạn."
            }
        }

        val reduceMotion = shouldReduceMotion()
        val haptic = LocalHapticFeedback.current
        val swipeScope = rememberCoroutineScope()
        val dragOffsetX = remember(word.id) { Animatable(0f) }

        val flipRotation by animateFloatAsState(
            targetValue = if (isFlipped) 180f else 0f,
            animationSpec = tween(
                durationMillis = if (reduceMotion) 0 else 260,
                easing = FastOutSlowInEasing
            ),
            label = "flashcard_3d_flip"
        )

        val swipeThresholdPx = 155f
        val swipeProgress = (dragOffsetX.value / swipeThresholdPx).coerceIn(-1f, 1f)

        // Carousel Counter + SRS Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = CardWhite,
                border = BorderStroke(1.dp, CardBorderSoft)
            ) {
                Text(
                    text = "Thẻ ${effectiveIndex + 1} / ${filteredWords.size} • ${word.cefrLevel} • M${word.moduleId}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = ElectricIndigo,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            val inSrs = srsWordIds.contains(word.id)
            Surface(
                shape = RoundedCornerShape(50),
                color = if (inSrs) GoldMasteryLight else CardWhite,
                border = BorderStroke(1.dp, if (inSrs) GoldMastery else CardBorderSoft),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onAddToSrs(word.id) }
                    .testTag("flashcard_srs_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (inSrs) Icons.Rounded.BookmarkAdded else Icons.Rounded.BookmarkAdd,
                        contentDescription = null,
                        tint = if (inSrs) GoldMastery else ElectricIndigo,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(if (inSrs) R.string.learn_in_srs else R.string.learn_add_srs),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (inSrs) GoldMastery else ElectricIndigo
                    )
                }
            }
        }

        // Smooth 3D Flip + Horizontal Swipe Gesture Flashcard Container
        // Swipe Right -> "Đã thuộc" (Emerald highlight), Swipe Left -> "Ôn lại SRS" (Amber highlight)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(word.id, filteredWords.size, effectiveIndex) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val currentOffset = dragOffsetX.value
                            if (currentOffset > swipeThresholdPx) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                swipeScope.launch {
                                    dragOffsetX.animateTo(520f, tween(if (reduceMotion) 0 else 170))
                                    onWordFlipped(word)
                                    onShowMessage("✓ Đã thuộc \"${word.word}\"!")
                                    selectDisplayIndex((effectiveIndex + 1) % filteredWords.size)
                                }
                            } else if (currentOffset < -swipeThresholdPx) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                swipeScope.launch {
                                    dragOffsetX.animateTo(-520f, tween(if (reduceMotion) 0 else 170))
                                    onAddToSrs(word.id)
                                    onShowMessage("↺ Đã đưa \"${word.word}\" vào hàng chờ ôn lại SRS!")
                                    selectDisplayIndex((effectiveIndex + 1) % filteredWords.size)
                                }
                            } else {
                                swipeScope.launch {
                                    dragOffsetX.animateTo(
                                        0f,
                                        spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium)
                                    )
                                }
                            }
                        },
                        onDragCancel = {
                            swipeScope.launch {
                                dragOffsetX.animateTo(
                                    0f,
                                    spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium)
                                )
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            swipeScope.launch {
                                dragOffsetX.snapTo((dragOffsetX.value + dragAmount).coerceIn(-420f, 420f))
                            }
                        }
                    )
                }
                .graphicsLayer {
                    translationX = dragOffsetX.value
                    rotationZ = if (reduceMotion) 0f else (dragOffsetX.value / 28f).coerceIn(-8f, 8f)
                    rotationY = flipRotation
                    cameraDistance = 12f * density
                }
                .testTag("flashcard_3d_container")
        ) {
            if (flipRotation <= 90f) {
                // FRONT FACE: C1-C2 Word, Auto-Fetched Illustration, UK/US IPA, TTS Audio, External Links & Voice Check
                FlashcardFrontFace(
                    word = word,
                    topicHintEn = topicHintEn,
                    isOnline = isOnline,
                    googleCseApiKey = googleCseApiKey,
                    googleCseCx = googleCseCx,
                    speakingAccent = speakingAccent,
                    isListeningVoice = isListeningVoice,
                    pronunciationResult = pronunciationResult,
                    voiceErrorVi = voiceErrorVi,
                    onShowMessage = onShowMessage,
                    onFlip = {
                        isFlipped = true
                    },
                    onSpeak = { accent -> ttsController.speak(word.word, accent) },
                    onToggleVoiceCheck = {
                        if (isListeningVoice) {
                            activeRecognizer?.stopListening()
                            isListeningVoice = false
                        } else {
                            val hasPerm = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPerm) {
                                startVoiceCheck()
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    }
                )
            } else {
                // BACK FACE (counter-rotated 180° so content reads naturally left-to-right)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { rotationY = 180f }
                ) {
                    FlashcardBackFace(
                        word = word,
                        isLearnedToday = dailyLearnedWordIds.contains(word.id),
                        inSrs = srsWordIds.contains(word.id),
                        onMarkLearned = { onWordFlipped(word) },
                        onAddToSrs = { onAddToSrs(word.id) },
                        onFlipBack = { isFlipped = false },
                        onSpeakExample = { ttsController.speak(word.exampleEn, EnglishAccent.UK) }
                    )
                }
            }

            // Live Emerald / Amber Swipe Action Highlight Overlay
            if (abs(swipeProgress) > 0.14f) {
                val isRight = swipeProgress > 0f
                val badgeAlpha = abs(swipeProgress).coerceIn(0.2f, 0.96f)
                val badgeColor = if (isRight) EmeraldSuccess else GoldMastery
                val badgeBg = if (isRight) EmeraldSuccessLight else GoldMasteryLight
                val badgeText = if (isRight) "✓ Đã thuộc" else "↺ Ôn lại SRS"

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = badgeBg.copy(alpha = badgeAlpha),
                    border = BorderStroke(2.dp, badgeColor),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .align(if (isRight) Alignment.TopStart else Alignment.TopEnd)
                        .padding(16.dp)
                        .graphicsLayer {
                            if (flipRotation > 90f) rotationY = 180f
                        }
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = if (isRight) EmeraldSuccess else GoldOnContainer,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Concise Swipe Gesture Hint Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "← Vuốt trái: Ôn lại SRS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = GoldOnContainer
            )
            Text(
                text = "Vuốt phải: Đã thuộc →",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = EmeraldSuccess
            )
        }

        // Previous / Next Card Navigation + Quick CTA to AI Sentence Builder
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { selectDisplayIndex(if (effectiveIndex > 0) effectiveIndex - 1 else filteredWords.lastIndex) },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorderSoft),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("flashcard_prev_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Từ trước",
                    tint = ElectricIndigo,
                    modifier = Modifier.size(18.dp)
                )
            }

            Button(
                onClick = { onBuildSentenceWithWord(word) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricIndigo,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .tactilePress()
                    .testTag("flashcard_build_sentence_cta")
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Đặt câu AI với từ này",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            OutlinedButton(
                onClick = { selectDisplayIndex((effectiveIndex + 1) % filteredWords.size) },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorderSoft),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("flashcard_next_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = "Từ tiếp theo",
                    tint = ElectricIndigo,
                    modifier = Modifier.size(18.dp)
                )
                }
            }
        }
    }
}

@Composable
private fun VocabSearchAndFilterCard(
    rawQuery: String,
    onQueryChange: (String) -> Unit,
    statusFilter: VocabStatusFilter,
    onSelectStatus: (VocabStatusFilter) -> Unit,
    selectedModuleFilter: Int?,
    activeModuleId: Int,
    showModuleStrip: Boolean,
    onToggleModuleStrip: () -> Unit,
    onSelectModule: (Int?) -> Unit,
    resultCount: Int,
    totalCount: Int
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, CardBorderSoft),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vocab_search_filter_bar")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = rawQuery,
                onValueChange = onQueryChange,
                singleLine = true,
                placeholder = {
                    Text(
                        text = "Tìm từ, nghĩa Việt, ví dụ ($totalCount từ)...",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = ElectricIndigo,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (rawQuery.isNotBlank()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Xóa từ khóa",
                                tint = InkMuted,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricIndigo,
                    unfocusedBorderColor = CardBorderSoft,
                    focusedContainerColor = SurfaceCanvas,
                    unfocusedContainerColor = SurfaceCanvas
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vocab_search_input")
            )

            // Status Filter Chips + Module Filter Toggle Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                VocabStatusFilter.entries.forEach { status ->
                    val selected = status == statusFilter
                    val chipBg = when {
                        selected && status == VocabStatusFilter.LEARNED -> EmeraldSuccess
                        selected && status == VocabStatusFilter.REVIEWING -> GoldMastery
                        selected -> ElectricIndigo
                        else -> SurfaceCanvas
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = chipBg,
                        border = BorderStroke(
                            1.dp,
                            if (selected) chipBg else CardBorderSoft
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onSelectStatus(status) }
                            .testTag("vocab_status_${status.name.lowercase()}")
                    ) {
                        Text(
                            text = status.labelVi,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (selected) Color.White else InkPrimary,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                // Module Filter Toggle Pill
                val moduleLabel = when (selectedModuleFilter) {
                    null -> "Module: Tất cả"
                    else -> "Module $selectedModuleFilter"
                }
                val isCustomModule = selectedModuleFilter != activeModuleId
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (showModuleStrip || isCustomModule) ElectricIndigoLight else SurfaceCanvas,
                    border = BorderStroke(
                        1.dp,
                        if (showModuleStrip || isCustomModule) ElectricIndigo else CardBorderSoft
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onToggleModuleStrip)
                        .testTag("vocab_module_filter_toggle")
                ) {
                    Text(
                        text = "$moduleLabel ($resultCount)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ElectricIndigo,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Expandable Module 1–12 Selector Strip
            AnimatedVisibility(visible = showModuleStrip) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val allSelected = selectedModuleFilter == null
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (allSelected) ElectricIndigo else SurfaceCanvas,
                        border = BorderStroke(1.dp, if (allSelected) ElectricIndigo else CardBorderSoft),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onSelectModule(null) }
                            .testTag("vocab_module_all")
                    ) {
                        Text(
                            text = "Tất cả (1–12)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (allSelected) Color.White else InkPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    (1..12).forEach { modId ->
                        val selected = selectedModuleFilter == modId
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (selected) ElectricIndigo else SurfaceCanvas,
                            border = BorderStroke(1.dp, if (selected) ElectricIndigo else CardBorderSoft),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { onSelectModule(modId) }
                                .testTag("vocab_module_$modId")
                        ) {
                            Text(
                                text = "M$modId",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (selected) Color.White else InkPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlashcardFrontFace(
    word: VocabWordItem,
    topicHintEn: String,
    isOnline: Boolean,
    googleCseApiKey: String,
    googleCseCx: String,
    speakingAccent: EnglishAccent?,
    isListeningVoice: Boolean,
    pronunciationResult: PronunciationResult?,
    voiceErrorVi: String?,
    onShowMessage: (String) -> Unit,
    onFlip: () -> Unit,
    onSpeak: (EnglishAccent) -> Unit,
    onToggleVoiceCheck: () -> Unit
) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .clickable(onClick = onFlip)
            .testTag("flashcard_front"),
        shape = RoundedCornerShape(26.dp),
        solidColor = CardWhite,
        solidBorder = BorderStroke(1.5.dp, ElectricIndigo.copy(alpha = 0.18f)),
        solidShadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Badges + Flip Hint Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ElectricIndigoLight
                    ) {
                        Text(
                            text = word.cefrLevel,
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricIndigo,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CyanGlowLight
                    ) {
                        Text(
                            text = word.partOfSpeech,
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricIndigo,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Flip,
                        contentDescription = null,
                        tint = CyanGlow,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Lật thẻ",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanGlow
                    )
                }
            }

            // Target C1-C2 Academic Word
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 31.sp
                    ),
                    color = ElectricIndigo
                )
                Text(
                    text = word.meaningVi,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = InkSecondary
                )
            }

            // Auto-Fetched Rounded Vocabulary Illustration (Coil + Wikipedia/Wikimedia or Google CSE + Shimmer/Vector Fallback)
            VocabIllustrationPreview(
                word = word,
                topicHintEn = topicHintEn,
                isOnline = isOnline,
                googleCseApiKey = googleCseApiKey,
                googleCseCx = googleCseCx
            )

            // UK & US IPA + Text-to-Speech Audio Buttons with Animated Waveform
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AccentAudioButton(
                    accent = EnglishAccent.UK,
                    ipa = word.ipaUk,
                    isSpeaking = speakingAccent == EnglishAccent.UK,
                    onClick = { onSpeak(EnglishAccent.UK) },
                    modifier = Modifier.weight(1f)
                )
                AccentAudioButton(
                    accent = EnglishAccent.US,
                    ipa = word.ipaUs,
                    isSpeaking = speakingAccent == EnglishAccent.US,
                    onClick = { onSpeak(EnglishAccent.US) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Quick 1-Tap External Reference Links (Wikipedia, Cambridge/Oxford Dictionary, Google Images)
            VocabExternalLinksRow(
                word = word,
                onShowMessage = onShowMessage
            )

            HorizontalDivider(color = CardBorderSoft)

            // Voice Pronunciation Check Section + AI Teacher in LISTENING mood
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isListeningVoice) CyanGlowLight.copy(alpha = 0.55f) else SurfaceCanvas,
                border = BorderStroke(
                    1.dp,
                    if (isListeningVoice) CyanGlow else CardBorderSoft
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnimatedTeacher(
                            mood = when {
                                isListeningVoice -> TeacherMood.LISTENING
                                pronunciationResult?.isStrongPass == true -> TeacherMood.CHEERING
                                pronunciationResult != null -> TeacherMood.ENCOURAGING
                                else -> TeacherMood.LISTENING
                            },
                            isSpeaking = isListeningVoice || pronunciationResult != null,
                            size = 62.dp
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isListeningVoice) {
                                    stringResource(R.string.learn_listening_now)
                                } else {
                                    "Luyện phát âm cùng Trợ giảng AI"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = InkPrimary
                            )
                            Text(
                                text = word.pronunciationFocusVi,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = InkSecondary
                            )
                        }
                    }

                    Button(
                        onClick = onToggleVoiceCheck,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isListeningVoice) RoseAlert else CyanGlow,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("voice_pronunciation_check_button")
                    ) {
                        if (isListeningVoice) {
                            AnimatedAudioWaveform(
                                isActive = true,
                                width = 26.dp,
                                height = 14.dp,
                                primaryColor = Color.White,
                                secondaryColor = Color(0xFFFFE4E6)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Rounded.Stop,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Dừng ghi âm", style = MaterialTheme.typography.labelLarge)
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.learn_pronunciation_check),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    // Pronunciation accuracy score & final-consonant feedback
                    AnimatedVisibility(visible = pronunciationResult != null) {
                        if (pronunciationResult != null) {
                            val pass = pronunciationResult.isStrongPass
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (pass) EmeraldSuccessLight else GoldMasteryLight,
                                border = BorderStroke(
                                    1.dp,
                                    if (pass) EmeraldSuccess else GoldMastery
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(11.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Bạn đọc: \"${pronunciationResult.recognizedText}\"",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = InkPrimary
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = if (pass) EmeraldSuccess else GoldMastery
                                        ) {
                                            Text(
                                                text = "${pronunciationResult.score}%",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = pronunciationResult.feedbackVi,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = InkPrimary
                                    )
                                    Text(
                                        text = pronunciationResult.consonantTipVi,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                        color = InkSecondary
                                    )
                                }
                            }
                        }
                    }

                    if (voiceErrorVi != null) {
                        Text(
                            text = voiceErrorVi,
                            style = MaterialTheme.typography.bodySmall,
                            color = RoseAlert
                        )
                    }
                }
            }

            // Bottom flip prompt
            Text(
                text = stringResource(R.string.learn_flip_hint),
                style = MaterialTheme.typography.labelSmall,
                color = InkMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun AccentAudioButton(
    accent: EnglishAccent,
    ipa: String,
    isSpeaking: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSpeaking) ElectricIndigoLight else SurfaceCanvas,
        border = BorderStroke(
            1.dp,
            if (isSpeaking) ElectricIndigo else CardBorderSoft
        ),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .testTag("tts_button_${accent.label.lowercase()}")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = "Phát âm giọng ${accent.label}",
                        tint = ElectricIndigo,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = accent.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricIndigo
                    )
                }
                AnimatedAudioWaveform(
                    isActive = isSpeaking,
                    barCount = 4,
                    width = 20.dp,
                    height = 12.dp,
                    primaryColor = ElectricIndigo,
                    secondaryColor = CyanGlow
                )
            }
            Text(
                text = ipa,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 11.5.sp
                ),
                color = InkSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FlashcardBackFace(
    word: VocabWordItem,
    isLearnedToday: Boolean,
    inSrs: Boolean,
    onMarkLearned: () -> Unit,
    onAddToSrs: () -> Unit,
    onFlipBack: () -> Unit,
    onSpeakExample: () -> Unit
) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .clickable(onClick = onFlipBack)
            .testTag("flashcard_back"),
        shape = RoundedCornerShape(26.dp),
        solidColor = CardWhite,
        solidBorder = BorderStroke(1.5.dp, CyanGlow.copy(alpha = 0.45f)),
        accentTint = CyanGlow,
        solidShadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Word header + flip back cue
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${word.word} (${word.partOfSpeech})",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = ElectricIndigo
                    )
                    Text(
                        text = word.meaningVi,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = SurfaceCanvas
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Flip,
                        contentDescription = "Quay lại mặt trước",
                        tint = ElectricIndigo,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(18.dp)
                    )
                }
            }

            // Academic Example Box
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardSurfaceTinted,
                border = BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.16f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onSpeakExample)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = "Nghe câu ví dụ",
                            tint = ElectricIndigo,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Ví dụ Band 8.5 (Chạm để nghe)",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricIndigo
                        )
                    }
                    Text(
                        text = word.exampleEn,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = InkPrimary
                    )
                    Text(
                        text = word.exampleVi,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )
                }
            }

            // Embedded Word-Level Grammar Breakdown
            GrammarDetailRow(label = "Word Family", value = word.wordFamily)
            GrammarDetailRow(label = "Dependent Prepositions", value = word.dependentPrepositions)
            GrammarDetailRow(label = "Verb Patterns (V-ing / to-V / Collocation)", value = word.verbPatterns)
            GrammarDetailRow(label = "Sắc thái trang trọng (Register)", value = word.registerNoteVi)

            // Highlighted "Lỗi Band 6.0 hay gặp" box narrated by AI Teacher (TEACHING mood)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = GoldMasteryLight.copy(alpha = 0.72f),
                border = BorderStroke(1.dp, GoldMastery.copy(alpha = 0.55f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnimatedTeacher(
                        mood = TeacherMood.TEACHING,
                        isSpeaking = true,
                        size = 64.dp
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WarningAmber,
                                contentDescription = null,
                                tint = GoldOnContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.learn_pitfall_title),
                                style = MaterialTheme.typography.titleSmall,
                                color = GoldOnContainer
                            )
                        }
                        Text(
                            text = word.band6PitfallVi,
                            style = MaterialTheme.typography.bodySmall,
                            color = InkPrimary
                        )
                    }
                }
            }

            // Mastery confirmation action row (triggers automatic daily vocabulary KPI counter)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onAddToSrs,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, if (inSrs) GoldMastery else CardBorderSoft),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("flashcard_back_srs_btn")
                ) {
                    Icon(
                        imageVector = if (inSrs) Icons.Rounded.BookmarkAdded else Icons.Rounded.BookmarkAdd,
                        contentDescription = null,
                        tint = if (inSrs) GoldOnContainer else InkSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (inSrs) "Đang ôn SRS" else "Ôn lại SRS",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (inSrs) GoldOnContainer else InkSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Button(
                    onClick = onMarkLearned,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLearnedToday) EmeraldSuccessLight else EmeraldSuccess,
                        contentColor = if (isLearnedToday) EmeraldSuccess else Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .tactilePress()
                        .testTag("flashcard_mark_learned_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLearnedToday) "✓ Đã nhớ từ này" else "Đã thuộc / Nhớ từ",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Text(
                text = stringResource(R.string.learn_flip_back_hint),
                style = MaterialTheme.typography.labelSmall,
                color = InkMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun GrammarDetailRow(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceCanvas
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = VividViolet
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = InkPrimary
            )
        }
    }
}
