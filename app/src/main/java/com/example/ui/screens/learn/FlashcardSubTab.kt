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
import com.example.ui.components.AnimatedAudioWaveform
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
import com.example.ui.theme.GoldOnContainer
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.VividViolet

@Composable
fun FlashcardSubTab(
    words: List<VocabWordItem>,
    selectedIndex: Int,
    srsWordIds: Set<String>,
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
    if (words.isEmpty()) return

    val safeIndex = selectedIndex.coerceIn(0, words.lastIndex)
    val word = words[safeIndex]
    val context = LocalContext.current

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

    val flipRotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing),
        label = "flashcard_3d_flip"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
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
                    text = "Thẻ ${safeIndex + 1} / ${words.size} • ${word.cefrLevel}",
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

        // Smooth 3D Flip Flashcard Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
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
                        onWordFlipped(word)
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
                        onFlipBack = { isFlipped = false },
                        onSpeakExample = { ttsController.speak(word.exampleEn, EnglishAccent.UK) }
                    )
                }
            }
        }

        // Previous / Next Card Navigation + Quick CTA to AI Sentence Builder
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { onSelectIndex(if (safeIndex > 0) safeIndex - 1 else words.lastIndex) },
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
                onClick = { onSelectIndex((safeIndex + 1) % words.size) },
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = ElectricIndigo.copy(alpha = 0.14f),
                spotColor = CyanGlow.copy(alpha = 0.22f)
            )
            .clip(RoundedCornerShape(26.dp))
            .clickable(onClick = onFlip)
            .testTag("flashcard_front"),
        shape = RoundedCornerShape(26.dp),
        color = CardWhite,
        border = BorderStroke(1.5.dp, ElectricIndigo.copy(alpha = 0.18f))
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
    onFlipBack: () -> Unit,
    onSpeakExample: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = ElectricIndigo.copy(alpha = 0.14f),
                spotColor = CyanGlow.copy(alpha = 0.22f)
            )
            .clip(RoundedCornerShape(26.dp))
            .clickable(onClick = onFlipBack)
            .testTag("flashcard_back"),
        shape = RoundedCornerShape(26.dp),
        color = CardWhite,
        border = BorderStroke(1.5.dp, CyanGlow.copy(alpha = 0.45f))
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
