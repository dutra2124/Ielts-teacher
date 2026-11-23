package com.example.ui.screens.practice

import android.Manifest
import android.content.pm.PackageManager
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.ai.SpeakingAiEvaluation
import com.example.ai.SpeakingAiUiState
import com.example.data.SpeakingPart
import com.example.data.SpeakingPracticePack
import com.example.data.TeacherMood
import androidx.compose.runtime.mutableFloatStateOf
import com.example.speech.ApexPronunciationEvaluator
import com.example.speech.ApexTtsController
import com.example.speech.EnglishAccent
import com.example.ui.components.AnimatedAudioWaveform
import com.example.ui.components.AnimatedTeacher
import com.example.ui.components.AudioAbRepeatControlBar
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardWhite
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldMastery
import com.example.ui.theme.GoldMasteryLight
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.PracticeTeal
import com.example.ui.theme.PracticeTealDark
import com.example.ui.theme.PracticeTealLight
import com.example.ui.theme.PracticeTealSurface
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.SurfaceCanvas
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun PracticeSpeakingView(
    pack: SpeakingPracticePack,
    transcript: String,
    onTranscriptChange: (String) -> Unit,
    aiState: SpeakingAiUiState,
    ttsController: ApexTtsController,
    onEvaluateSpeaking: (partLabel: String, promptEn: String, topicEn: String, transcript: String) -> Unit,
    onResetAi: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPart by remember(pack.moduleId) { mutableStateOf(SpeakingPart.PART_2) }
    val activePrompt = remember(pack, selectedPart) {
        pack.prompts.firstOrNull { it.part == selectedPart } ?: pack.prompts.first()
    }

    // Shadowing audio playback lines (Prompt + Cue bullets, or AI Band 8.5+ model sentences when available)
    val shadowingLines = remember(activePrompt, aiState) {
        val modelAnswer = (aiState as? SpeakingAiUiState.Success)
            ?.result
            ?.upgradedBand85Answer
            ?.trim()
            .orEmpty()
        if (modelAnswer.isNotBlank()) {
            modelAnswer.split(Regex("(?<=[.!?])\\s+"))
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .ifEmpty { listOf(modelAnswer) }
        } else {
            buildList {
                add(activePrompt.promptEn)
                addAll(activePrompt.cueBulletsEn)
            }.filter { it.isNotBlank() }
        }
    }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var isShadowingPlaying by remember(selectedPart, pack.moduleId) { mutableStateOf(false) }
    var activeShadowLineIdx by remember(selectedPart, pack.moduleId, shadowingLines) { mutableIntStateOf(0) }
    var loopPointA by remember(selectedPart, pack.moduleId, shadowingLines) { mutableStateOf<Int?>(null) }
    var loopPointB by remember(selectedPart, pack.moduleId, shadowingLines) { mutableStateOf<Int?>(null) }
    val isAbLoopActive = loopPointA != null && loopPointB != null && loopPointA!! <= loopPointB!!

    fun startShadowingAt(
        startIdx: Int,
        speed: Float = playbackSpeed,
        pointA: Int? = loopPointA,
        pointB: Int? = loopPointB
    ) {
        if (shadowingLines.isEmpty()) return
        val clamped = startIdx.coerceIn(0, shadowingLines.lastIndex)
        isShadowingPlaying = true
        ttsController.speakSequence(
            lines = shadowingLines,
            startIndex = clamped,
            accent = EnglishAccent.UK,
            speechRate = speed,
            loopStartIndex = pointA,
            loopEndIndex = pointB,
            onLineStarted = { idx -> activeShadowLineIdx = idx },
            onComplete = {
                isShadowingPlaying = false
                activeShadowLineIdx = 0
            }
        )
    }

    // Part 2 1-minute (60s) countdown timer
    var prepSecondsLeft by remember(selectedPart, pack.moduleId) { mutableIntStateOf(60) }
    var isPrepTimerRunning by remember(selectedPart, pack.moduleId) { mutableStateOf(false) }

    LaunchedEffect(isPrepTimerRunning) {
        while (isPrepTimerRunning && prepSecondsLeft > 0) {
            delay(1_000L)
            prepSecondsLeft -= 1
        }
        if (prepSecondsLeft == 0) {
            isPrepTimerRunning = false
        }
    }

    // SpeechRecognizer state
    var isRecording by remember { mutableStateOf(false) }
    var speechStatusMessage by remember { mutableStateOf<String?>(null) }
    var activeRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    fun stopRecognition() {
        activeRecognizer?.stopListening()
        activeRecognizer?.destroy()
        activeRecognizer = null
        isRecording = false
    }

    fun startRecognition() {
        stopRecognition()
        speechStatusMessage = null
        activeRecognizer = ApexPronunciationEvaluator.startFreeSpeechRecognition(
            context = context,
            onReady = {
                isRecording = true
                speechStatusMessage = "Đang thu âm tiếng Anh... Hãy nói rõ ràng, tự nhiên nhé!"
            },
            onPartial = { partial ->
                speechStatusMessage = "Đang nghe: \"$partial\""
            },
            onResult = { finalText ->
                isRecording = false
                speechStatusMessage = "Đã chuyển giọng nói thành văn bản! Bạn có thể chỉnh sửa rồi bấm Chấm AI."
                val combined = if (transcript.isBlank()) finalText else "${transcript.trim()} $finalText"
                onTranscriptChange(combined)
                activeRecognizer?.destroy()
                activeRecognizer = null
            },
            onError = { err ->
                isRecording = false
                speechStatusMessage = err
                activeRecognizer?.destroy()
                activeRecognizer = null
            }
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startRecognition()
        } else {
            speechStatusMessage = "Chưa có quyền Micro. Bạn vẫn có thể nhập transcript trực tiếp để Gemini AI chấm Speaking!"
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            stopRecognition()
            ttsController.stop()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Part 1 | Part 2 | Part 3 Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpeakingPart.entries.forEach { part ->
                val selected = part == selectedPart
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) PracticeTeal else CardWhite,
                    border = BorderStroke(1.dp, if (selected) PracticeTeal else CardBorderSoft),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            selectedPart = part
                            onResetAi()
                        }
                        .testTag("speaking_part_${part.name.lowercase()}")
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = part.labelEn,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = if (selected) Color.White else InkPrimary
                        )
                        Text(
                            text = when (part) {
                                SpeakingPart.PART_1 -> "Ngắn gọn"
                                SpeakingPart.PART_2 -> "Cue Card 1p"
                                SpeakingPart.PART_3 -> "Chuyên sâu"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = if (selected) Color.White.copy(alpha = 0.9f) else InkMuted
                        )
                    }
                }
            }
        }

        // Prompt / Cue Card + Part 2 Countdown Timer
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, CardBorderSoft)
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
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PracticeTealLight
                    ) {
                        Text(
                            text = "${activePrompt.part.labelEn} • ${activePrompt.part.badgeVi}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PracticeTealDark,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isShadowingPlaying) RoseAlert.copy(alpha = 0.14f) else SurfaceCanvas,
                        border = BorderStroke(
                            1.dp,
                            if (isShadowingPlaying) RoseAlert.copy(alpha = 0.5f) else CardBorderSoft
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                if (isShadowingPlaying) {
                                    ttsController.stop()
                                    isShadowingPlaying = false
                                } else {
                                    val startIdx = if (isAbLoopActive && loopPointA != null &&
                                        (activeShadowLineIdx < loopPointA!! || activeShadowLineIdx > loopPointB!!)
                                    ) {
                                        loopPointA!!
                                    } else {
                                        activeShadowLineIdx
                                    }
                                    startShadowingAt(startIdx)
                                }
                            }
                            .testTag("speaking_shadow_play_toggle")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = if (isShadowingPlaying) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = if (isShadowingPlaying) "Dừng nghe" else "Nghe Shadowing",
                                tint = if (isShadowingPlaying) RoseAlert else PracticeTealDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isShadowingPlaying) {
                                    "Dừng (${activeShadowLineIdx + 1}/${shadowingLines.size})"
                                } else {
                                    "Nghe mẫu (${shadowingLines.size} câu)"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isShadowingPlaying) RoseAlert else PracticeTealDark
                            )
                        }
                    }
                }

                // Speaking Shadowing Audio Speed (0.75x / 1.0x / 1.25x) & A–B Repeat Loop Controls
                AudioAbRepeatControlBar(
                    playbackSpeed = playbackSpeed,
                    onSelectSpeed = { speed ->
                        playbackSpeed = speed
                        if (isShadowingPlaying) {
                            startShadowingAt(activeShadowLineIdx, speed = speed)
                        }
                    },
                    pointALabel = loopPointA?.let { "Câu ${it + 1}" },
                    pointBLabel = loopPointB?.let { "Câu ${it + 1}" },
                    isLoopActive = isAbLoopActive,
                    onSetPointA = {
                        val newA = activeShadowLineIdx
                        val newB = loopPointB?.takeIf { it >= newA }
                        loopPointA = newA
                        loopPointB = newB
                        ttsController.updateSequenceLoop(newA, newB)
                    },
                    onSetPointB = {
                        val anchorA = loopPointA ?: 0
                        val cur = activeShadowLineIdx
                        val (newA, newB) = if (cur < anchorA) cur to anchorA else anchorA to cur
                        loopPointA = newA
                        loopPointB = newB
                        if (isShadowingPlaying) {
                            ttsController.updateSequenceLoop(newA, newB)
                        } else {
                            startShadowingAt(newA, pointA = newA, pointB = newB)
                        }
                    },
                    onClearLoop = {
                        loopPointA = null
                        loopPointB = null
                        ttsController.updateSequenceLoop(null, null)
                    },
                    accentColor = PracticeTeal,
                    testTagPrefix = "speaking"
                )

                Text(
                    text = activePrompt.promptEn,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = InkPrimary
                )

                if (activePrompt.cueBulletsEn.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PracticeTealSurface,
                        border = BorderStroke(1.dp, PracticeTeal.copy(alpha = 0.25f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "You should say:",
                                style = MaterialTheme.typography.labelMedium,
                                color = PracticeTealDark
                            )
                            activePrompt.cueBulletsEn.forEach { bullet ->
                                Text(
                                    text = "• $bullet",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PlusJakartaSansFontFamily
                                    ),
                                    color = InkPrimary
                                )
                            }
                        }
                    }
                }

                // Part 2 1-Minute Prep Countdown Timer
                if (activePrompt.part == SpeakingPart.PART_2) {
                    val timerColor = if (prepSecondsLeft <= 15) RoseAlert else PracticeTealDark
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (prepSecondsLeft <= 15) Color(0xFFFFF1F2) else GoldMasteryLight.copy(alpha = 0.65f),
                        border = BorderStroke(
                            1.dp,
                            if (prepSecondsLeft <= 15) RoseAlert.copy(alpha = 0.45f) else GoldMastery.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier.testTag("speaking_part2_timer_card")
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
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Rounded.Timer, contentDescription = null, tint = timerColor, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "Đồng hồ chuẩn bị Part 2 (1 phút)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = InkPrimary
                                    )
                                }
                                Text(
                                    text = String.format(Locale.US, "%02d:%02d", prepSecondsLeft / 60, prepSecondsLeft % 60),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = timerColor,
                                    modifier = Modifier.testTag("speaking_timer_value")
                                )
                            }

                            LinearProgressIndicator(
                                progress = { prepSecondsLeft / 60f },
                                color = timerColor,
                                trackColor = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50))
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        if (prepSecondsLeft == 0) prepSecondsLeft = 60
                                        isPrepTimerRunning = !isPrepTimerRunning
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PracticeTeal),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("speaking_timer_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (isPrepTimerRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isPrepTimerRunning) "Tạm dừng" else "Bắt đầu 1 phút")
                                }
                                OutlinedButton(
                                    onClick = {
                                        isPrepTimerRunning = false
                                        prepSecondsLeft = 60
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, PracticeTeal),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Rounded.Refresh, contentDescription = null, tint = PracticeTealDark, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Đặt lại", color = PracticeTealDark)
                                }
                            }
                        }
                    }
                }

                // AI Teacher Strategy Tip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnimatedTeacher(
                        mood = if (isRecording) TeacherMood.LISTENING else TeacherMood.TEACHING,
                        isSpeaking = isRecording,
                        size = 54.dp
                    )
                    Text(
                        text = activePrompt.strategyTipVi,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Voice Recording + Editable Transcript Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, CardBorderSoft)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (isRecording) {
                                stopRecognition()
                            } else {
                                val hasMic = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasMic) startRecognition()
                                else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecording) RoseAlert else PracticeTeal,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("speaking_record_button")
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Rounded.Stop else Icons.Rounded.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRecording) {
                                stringResource(R.string.practice_speaking_stop)
                            } else {
                                stringResource(R.string.practice_speaking_record)
                            }
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceCanvas,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onTranscriptChange(activePrompt.sampleBand6Draft) }
                            .testTag("speaking_fill_sample")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)
                        ) {
                            Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = PracticeTealDark, modifier = Modifier.size(15.dp))
                            Text("Mẫu 6.0", style = MaterialTheme.typography.labelSmall, color = PracticeTealDark)
                        }
                    }
                }

                if (speechStatusMessage != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isRecording) {
                            AnimatedAudioWaveform(
                                isActive = true,
                                width = 22.dp,
                                height = 14.dp,
                                primaryColor = RoseAlert,
                                secondaryColor = PracticeTeal
                            )
                        }
                        Text(
                            text = speechStatusMessage.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isRecording) RoseAlert else InkSecondary
                        )
                    }
                }

                OutlinedTextField(
                    value = transcript,
                    onValueChange = onTranscriptChange,
                    minLines = 4,
                    maxLines = 7,
                    placeholder = {
                        Text("Thu âm giọng nói hoặc nhập/dán transcript câu trả lời tiếng Anh của bạn tại đây...")
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PracticeTeal,
                        unfocusedBorderColor = CardBorderSoft,
                        focusedContainerColor = SurfaceCanvas.copy(alpha = 0.5f),
                        unfocusedContainerColor = SurfaceCanvas.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("speaking_transcript_input")
                )

                Button(
                    onClick = {
                        onEvaluateSpeaking(
                            activePrompt.part.labelEn,
                            activePrompt.promptEn,
                            pack.topicEn,
                            transcript
                        )
                    },
                    enabled = aiState !is SpeakingAiUiState.Loading && transcript.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PracticeTeal),
                    contentPadding = PaddingValues(vertical = 13.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("speaking_submit_ai_button")
                ) {
                    if (aiState is SpeakingAiUiState.Loading) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini AI đang chấm Speaking...")
                    } else {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.practice_speaking_submit))
                    }
                }
            }
        }

        // Gemini AI Speaking Evaluation Result
        when (aiState) {
            SpeakingAiUiState.Idle -> Unit
            SpeakingAiUiState.Loading -> {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PracticeTealSurface,
                    border = BorderStroke(1.dp, PracticeTeal.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnimatedTeacher(mood = TeacherMood.LISTENING, isSpeaking = true, size = 58.dp)
                        Text(
                            text = "Giám khảo AI đang phân tích độ trôi chảy (FC), vốn từ vựng (LR), ngữ pháp (GRA) và nâng cấp câu trả lời Band 8.5+...",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkPrimary
                        )
                    }
                }
            }

            is SpeakingAiUiState.Error -> {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFF1F2),
                    border = BorderStroke(1.dp, RoseAlert.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = RoseAlert)
                            Text(aiState.messageVi, style = MaterialTheme.typography.bodySmall, color = InkPrimary)
                        }
                        if (transcript.isNotBlank()) {
                            Button(
                                onClick = {
                                    onEvaluateSpeaking(activePrompt.part.labelEn, activePrompt.promptEn, pack.topicEn, transcript)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RoseAlert),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("speaking_retry_ai_button")
                            ) {
                                Text("Thử chấm lại ngay")
                            }
                        }
                    }
                }
            }

            is SpeakingAiUiState.Success -> {
                SpeakingEvaluationCard(
                    evaluation = aiState.result,
                    onSpeakSample = {
                        if (isShadowingPlaying) {
                            ttsController.stop()
                            isShadowingPlaying = false
                        } else {
                            startShadowingAt(0)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SpeakingEvaluationCard(
    evaluation: SpeakingAiEvaluation,
    onSpeakSample: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = CardWhite,
        border = BorderStroke(1.5.dp, PracticeTeal.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("speaking_ai_result_card")
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnimatedTeacher(mood = TeacherMood.CHEERING, isSpeaking = true, size = 54.dp)
                    Column {
                        Text(
                            text = "Kết quả Speaking Examiner",
                            style = MaterialTheme.typography.titleMedium,
                            color = PracticeTealDark
                        )
                        Text(
                            text = "Đánh giá nhanh theo 4 tiêu chí IELTS",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted
                        )
                    }
                }
                Surface(shape = RoundedCornerShape(14.dp), color = PracticeTeal) {
                    Text(
                        text = "Band ${evaluation.overallBand}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // 3 Criteria Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BandMiniPill("Fluency (FC)", evaluation.fluencyBand, Modifier.weight(1f))
                BandMiniPill("Lexical (LR)", evaluation.lexicalBand, Modifier.weight(1f))
                BandMiniPill("Grammar (GRA)", evaluation.grammarBand, Modifier.weight(1f))
            }

            if (evaluation.pronunciationTipVi.isNotBlank()) {
                Surface(shape = RoundedCornerShape(14.dp), color = PracticeTealSurface) {
                    Text(
                        text = "Phát âm & Ngữ điệu: ${evaluation.pronunciationTipVi}",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkPrimary,
                        modifier = Modifier.padding(11.dp)
                    )
                }
            }

            if (evaluation.improvementsVi.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = "Điểm cần nâng cấp để chạm Band 8.5:",
                        style = MaterialTheme.typography.labelMedium,
                        color = PracticeTealDark
                    )
                    evaluation.improvementsVi.forEach { fix ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.padding(top = 2.dp).size(15.dp))
                            Text(fix, style = MaterialTheme.typography.bodySmall, color = InkPrimary)
                        }
                    }
                }
            }

            // Upgraded Band 8.5+ Answer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(PracticeTeal, EmeraldSuccess)))
                    .padding(1.5.dp)
            ) {
                Surface(shape = RoundedCornerShape(17.dp), color = CardWhite) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = PracticeTealDark, modifier = Modifier.size(16.dp))
                                Text("Phiên bản nói Band 8.5+", style = MaterialTheme.typography.labelMedium, color = PracticeTealDark)
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = PracticeTealLight,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable(onClick = onSpeakSample)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Nghe mẫu Band 8.5", tint = PracticeTealDark, modifier = Modifier.size(14.dp))
                                    Text("Nghe UK", style = MaterialTheme.typography.labelSmall, color = PracticeTealDark)
                                }
                            }
                        }
                        Text(
                            text = evaluation.upgradedBand85Answer,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 21.sp
                            ),
                            color = InkPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun BandMiniPill(label: String, score: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceCanvas,
        border = BorderStroke(1.dp, CardBorderSoft),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = InkMuted)
            Text(
                text = score,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = PracticeTealDark
            )
        }
    }
}
