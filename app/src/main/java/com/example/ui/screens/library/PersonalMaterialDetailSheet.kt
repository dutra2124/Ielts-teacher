package com.example.ui.screens.library

import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Highlight
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.IeltsModule
import com.example.data.VocabWordItem
import com.example.data.local.ExtractedAcademicWord
import com.example.data.local.PersonalMaterial
import com.example.data.local.PersonalMaterialAiUiState
import com.example.speech.ApexTtsController
import com.example.speech.EnglishAccent
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
import com.example.ui.theme.HighlightYellow
import com.example.ui.theme.HighlightYellowBorder
import com.example.ui.theme.IndigoOnSurface
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.PracticeTeal
import com.example.ui.theme.PracticeTealLight
import com.example.ui.theme.RoseDiff
import com.example.ui.theme.RoseDiffLight
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.TealOnSurface
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.UUID

/**
 * Active Self-Study Workspace Sheet opened when tapping any Personal Material.
 * Features:
 * 1. Audio Playback (attached local MP3/WAV via MediaPlayer + built-in UK TTS reader).
 * 2. Highlight-to-Flashcard: tap any sentence/phrase to highlight yellow & save as a C1-C2 Flashcard into Tab 2.
 * 3. "AI tóm tắt & Rút từ Band 8.5+" powered by Firebase AI Logic with 1-tap Flashcard saving.
 * 4. Self-Study Session Stopwatch & Duration Logger feeding directly into daily diligence metrics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalMaterialDetailSheet(
    material: PersonalMaterial,
    linkedModule: IeltsModule?,
    aiState: PersonalMaterialAiUiState,
    dailySelfStudyMinutes: Int,
    onEditMaterial: (PersonalMaterial) -> Unit,
    onSaveFlashcard: (VocabWordItem) -> Unit,
    onRequestAiSummary: (PersonalMaterial) -> Unit,
    onLogSelfStudyMinutes: (materialTitle: String, minutes: Int) -> Unit,
    onShowMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    // Highlight-to-Flashcard state
    val sentences = remember(material.contentEn) { splitIntoSentences(material.contentEn) }
    var highlightedIndices by remember(material.id) { mutableStateOf(setOf<Int>()) }
    var flashcardDraftSentence by remember { mutableStateOf<String?>(null) }
    var savedExtractedWords by remember(material.id) { mutableStateOf(setOf<String>()) }

    // Audio & TTS playback state
    var speakingAccent by remember { mutableStateOf<EnglishAccent?>(null) }
    val ttsController = remember(context) {
        ApexTtsController(context) { accent -> speakingAccent = accent }
    }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isAudioPlaying by remember { mutableStateOf(false) }

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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceCanvas,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("personal_material_detail_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Title + Module + Edit/Close Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = PracticeTealLight
                        ) {
                            Text(
                                text = material.category,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TealOnSurface,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }
                        if (linkedModule != null) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = ElectricIndigoLight
                            ) {
                                Text(
                                    text = "Module #${linkedModule.id}: ${linkedModule.titleEn}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IndigoOnSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = material.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = InkPrimary
                    )
                }

                Row {
                    IconButton(
                        onClick = { onEditMaterial(material) },
                        modifier = Modifier.testTag("detail_edit_material_btn")
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Chỉnh sửa", tint = ElectricIndigo)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Đóng", tint = InkSecondary)
                    }
                }
            }

            // 1. Audio & Read-Aloud Strip
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, CardBorderSoft)
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
                                            mp.start()
                                            isAudioPlaying = true
                                        }.onFailure {
                                            onShowMessage("Không thể phát tệp audio này. Đang dùng giọng đọc TTS thay thế.")
                                            ttsController.speak(material.contentEn.ifBlank { material.title }, EnglishAccent.UK)
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PracticeTeal,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isAudioPlaying) Icons.Rounded.Pause else Icons.Rounded.Headphones,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAudioPlaying) "Tạm dừng Audio" else (material.audioDisplayName ?: "Phát Audio đính kèm"),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                if (speakingAccent != null) {
                                    ttsController.stop()
                                } else if (material.contentEn.isNotBlank()) {
                                    ttsController.speak(material.contentEn, EnglishAccent.UK)
                                } else {
                                    onShowMessage("Tài liệu chưa có đoạn tiếng Anh để đọc.")
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("material_tts_read_btn")
                        ) {
                            Icon(
                                imageVector = if (speakingAccent != null) Icons.Rounded.Stop else Icons.Rounded.RecordVoiceOver,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (speakingAccent != null) "Dừng đọc TTS" else "Nghe đọc mẫu (UK)",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            // 2. Interactive Highlight-to-Flashcard Reader
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, CardBorderSoft)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                imageVector = Icons.Rounded.Highlight,
                                contentDescription = null,
                                tint = GoldMastery,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Chạm câu để bôi vàng & Lưu Flashcard",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = InkPrimary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = HighlightYellow,
                            border = BorderStroke(1.dp, HighlightYellowBorder)
                        ) {
                            Text(
                                text = "${highlightedIndices.size} câu bôi",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = GoldOnContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (sentences.isEmpty()) {
                        Text(
                            text = "Chưa có đoạn văn tiếng Anh. Nhấn biểu tượng bút chì ở góc trên để bổ sung nội dung.",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted
                        )
                    } else {
                        sentences.forEachIndexed { idx, sentence ->
                            val isHighlighted = highlightedIndices.contains(idx)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isHighlighted) HighlightYellow else CardSurfaceTinted,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isHighlighted) HighlightYellowBorder else Color.Transparent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        highlightedIndices = if (isHighlighted) {
                                            highlightedIndices - idx
                                        } else {
                                            highlightedIndices + idx
                                        }
                                    }
                                    .testTag("material_sentence_$idx")
                            ) {
                                Column(
                                    modifier = Modifier.padding(11.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = sentence,
                                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                        color = InkPrimary
                                    )
                                    if (isHighlighted) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = ElectricIndigo,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .clickable { flashcardDraftSentence = sentence }
                                                    .testTag("create_flashcard_from_highlight_$idx")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.BookmarkAdd,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = "Lưu làm Flashcard Tab 2",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (material.notesVi.isNotBlank()) {
                        HorizontalDivider(color = CardBorderSoft)
                        Text(
                            text = "Ghi chú tiếng Việt của bạn:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = IndigoOnSurface
                        )
                        Text(
                            text = material.notesVi,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                            color = InkSecondary
                        )
                    }
                }
            }

            // 3. AI Summarization & Band 8.5+ Vocabulary Extraction Panel
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = ElectricIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.library_ai_summarize_btn),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = InkPrimary
                            )
                            Text(
                                text = "Tóm tắt tiếng Việt, rút 4–5 từ C1–C2 & cấu trúc ngữ pháp ăn điểm",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkMuted
                            )
                        }
                    }

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
                            .height(48.dp)
                            .testTag("trigger_ai_summarize_material_btn")
                    ) {
                        if (isLoadingThis) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gemini AI đang phân tích Band 8.5+...")
                        } else {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.library_ai_summarize_btn),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    if (aiState is PersonalMaterialAiUiState.Error && aiState.materialId == material.id) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = RoseDiffLight
                        ) {
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
                                    text = "Tóm tắt học thuật (Tiếng Việt)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = IndigoOnSurface
                                )
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

                        if (res.extractedWords.isNotEmpty()) {
                            Text(
                                text = "Từ vựng Band 8.5+ rút trích (Chạm để lưu vào Tab 2 Học)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = InkPrimary
                            )
                            res.extractedWords.forEach { item ->
                                val alreadySaved = savedExtractedWords.contains(item.word.lowercase(Locale.US))
                                ExtractedWordCard(
                                    item = item,
                                    saved = alreadySaved,
                                    onSave = {
                                        onSaveFlashcard(item.toVocabWordItem(material.moduleId))
                                        savedExtractedWords = savedExtractedWords + item.word.lowercase(Locale.US)
                                    }
                                )
                            }
                        }

                        if (res.grammarHighlights.isNotEmpty()) {
                            Text(
                                text = "Cấu trúc ngữ pháp nổi bật",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = InkPrimary
                            )
                            res.grammarHighlights.forEach { gp ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = CardSurfaceTinted,
                                    border = BorderStroke(1.dp, CardBorderSoft)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = gp.structureNameEn,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TealOnSurface
                                        )
                                        if (gp.exampleQuoteEn.isNotBlank()) {
                                            Text(
                                                text = "\"${gp.exampleQuoteEn}\"",
                                                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                                color = InkPrimary
                                            )
                                        }
                                        Text(
                                            text = gp.explanationVi,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = InkSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Self-Study Session Duration Logger -> Daily Diligence Metrics
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.40f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                imageVector = Icons.Rounded.Timer,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Ghi nhận thời lượng tự học",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = InkPrimary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = EmeraldSuccessLight
                        ) {
                            Text(
                                text = "Hôm nay: ${dailySelfStudyMinutes}p",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldOnContainer,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Live Stopwatch Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTimerMmSs(elapsedSeconds),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = InkPrimary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { isTimerRunning = !isTimerRunning },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("self_study_timer_toggle_btn")
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
                    }

                    // Quick Duration Chips
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
                                    text = "+${mins} phút",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    ),
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldSuccess,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("log_self_study_session_btn")
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ghi nhận buổi tự học (${minutesToLog} phút) → Cập nhật chuyên cần",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }

    // Highlight-to-Flashcard Quick Save Dialog
    val draftSentence = flashcardDraftSentence
    if (draftSentence != null) {
        HighlightToFlashcardDialog(
            sentence = draftSentence,
            moduleId = material.moduleId,
            onSave = { vocab ->
                onSaveFlashcard(vocab)
                flashcardDraftSentence = null
            },
            onDismiss = { flashcardDraftSentence = null }
        )
    }
}

@Composable
private fun ExtractedWordCard(
    item: ExtractedAcademicWord,
    saved: Boolean,
    onSave: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CardSurfaceTinted,
        border = BorderStroke(1.dp, if (saved) EmeraldSuccess.copy(alpha = 0.5f) else CardBorderSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(11.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.word,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = InkPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = GoldMasteryLight
                        ) {
                            Text(
                                text = "${item.cefrLevel} • ${item.partOfSpeech}",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldOnContainer,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (item.ipaUk.isNotBlank()) {
                        Text(
                            text = item.ipaUk,
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted
                        )
                    }
                }

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
                            text = if (saved) "Đã lưu ✓" else "Lưu Tab 2",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (saved) EmeraldOnContainer else Color.White
                        )
                    }
                }
            }

            Text(
                text = item.meaningVi,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = InkPrimary
            )
            if (item.collocationEn.isNotBlank()) {
                Text(
                    text = "Collocation: ${item.collocationEn}",
                    style = MaterialTheme.typography.bodySmall,
                    color = IndigoOnSurface
                )
            }
            if (item.exampleEn.isNotBlank()) {
                Text(
                    text = "\"${item.exampleEn}\"",
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    color = InkSecondary
                )
            }
        }
    }
}

@Composable
private fun HighlightToFlashcardDialog(
    sentence: String,
    moduleId: Int,
    onSave: (VocabWordItem) -> Unit,
    onDismiss: () -> Unit
) {
    // Suggest the longest academic word in the highlighted sentence as default
    val suggestedWord = remember(sentence) {
        sentence.split(Regex("[^A-Za-z-]+"))
            .filter { it.length >= 6 }
            .maxByOrNull { it.length }
            ?.lowercase(Locale.US)
            ?: sentence.take(28)
    }

    var wordInput by remember(sentence) { mutableStateOf(suggestedWord) }
    var meaningVi by remember { mutableStateOf("") }
    var cefrLevel by remember { mutableStateOf("C1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Lưu làm Flashcard C1–C2 (Tab 2)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Ngữ cảnh đã bôi vàng:\n\"$sentence\"",
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    color = InkSecondary,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                OutlinedTextField(
                    value = wordInput,
                    onValueChange = { wordInput = it },
                    label = { Text("Từ / Cụm từ C1–C2 *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = meaningVi,
                    onValueChange = { meaningVi = it },
                    label = { Text("Nghĩa tiếng Việt / Ghi chú") },
                    placeholder = { Text("VD: khử carbon, tiếp cận toàn diện...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("C1", "C2", "Band 8.5").forEach { level ->
                        val selected = cefrLevel == level
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (selected) ElectricIndigo else CardSurfaceTinted,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { cefrLevel = level }
                        ) {
                            Text(
                                text = level,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (selected) Color.White else InkSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanWord = wordInput.trim().ifBlank { suggestedWord }
                    val safeSlug = cleanWord.lowercase(Locale.US).replace(Regex("[^a-z0-9]+"), "_").take(24)
                    onSave(
                        VocabWordItem(
                            id = "hl_m${moduleId}_${safeSlug}_${UUID.randomUUID().toString().take(4)}",
                            moduleId = moduleId,
                            word = cleanWord,
                            partOfSpeech = if (cleanWord.contains(" ")) "phrase" else "academic",
                            cefrLevel = cefrLevel,
                            ipaUk = "",
                            ipaUs = "",
                            meaningVi = meaningVi.trim().ifBlank { "Từ vựng trích từ Tài liệu tự học" },
                            exampleEn = sentence,
                            exampleVi = meaningVi.trim().ifBlank { "Trích từ bài tự học cá nhân" },
                            wordFamily = cleanWord,
                            dependentPrepositions = "Xem ngữ cảnh trong câu ví dụ",
                            verbPatterns = "Dùng trực tiếp trong Writing & Speaking",
                            registerNoteVi = "Tự học cá nhân • Module #$moduleId",
                            band6PitfallVi = "Ghi nhớ dùng trọn cụm theo đúng ngữ cảnh bài đọc gốc.",
                            pronunciationFocusVi = "Nhấn trọng âm rõ và nối âm tự nhiên.",
                            band6ParaphraseEn = cleanWord,
                            collocationStemEn = "______",
                            collocationPartnerEn = cleanWord,
                            clozeSentenceEn = sentence.replace(cleanWord, "______", ignoreCase = true)
                        )
                    )
                },
                enabled = wordInput.isNotBlank()
            ) {
                Text("Lưu Flashcard")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

private fun ExtractedAcademicWord.toVocabWordItem(moduleId: Int): VocabWordItem {
    val slug = word.lowercase(Locale.US).replace(Regex("[^a-z0-9]+"), "_").take(24)
    val ex = exampleEn.ifBlank { "Academic writers frequently use '$collocationEn' to convey precision." }
    return VocabWordItem(
        id = "ai_m${moduleId}_${slug}_${UUID.randomUUID().toString().take(4)}",
        moduleId = moduleId,
        word = word,
        partOfSpeech = partOfSpeech,
        cefrLevel = cefrLevel,
        ipaUk = ipaUk,
        ipaUs = ipaUk,
        meaningVi = meaningVi,
        exampleEn = ex,
        exampleVi = meaningVi,
        wordFamily = word,
        dependentPrepositions = collocationEn.ifBlank { word },
        verbPatterns = collocationEn.ifBlank { word },
        registerNoteVi = "AI trích xuất Band 8.5+ từ Tài liệu cá nhân",
        band6PitfallVi = "Tránh dịch word-by-word từ tiếng Việt; ưu tiên dùng trọn cụm \"$collocationEn\".",
        pronunciationFocusVi = if (ipaUk.isNotBlank()) "Đọc chuẩn theo phiên âm $ipaUk." else "Đọc rõ phụ âm cuối.",
        band6ParaphraseEn = word,
        collocationStemEn = collocationEn.replace(word, "______", ignoreCase = true).ifBlank { "______" },
        collocationPartnerEn = word,
        clozeSentenceEn = ex.replace(word, "______", ignoreCase = true)
    )
}

private fun splitIntoSentences(text: String): List<String> {
    val cleaned = text.trim()
    if (cleaned.isEmpty()) return emptyList()
    return cleaned
        .split(Regex("(?<=[.!?])\\s+|\\n+"))
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}

private fun formatTimerMmSs(totalSeconds: Int): String {
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", mins, secs)
}
