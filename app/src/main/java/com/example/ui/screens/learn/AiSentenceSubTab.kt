package com.example.ui.screens.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ai.AiSentenceEvaluation
import com.example.ai.SentenceAiUiState
import com.example.data.TeacherMood
import com.example.data.VocabWordItem
import com.example.speech.ApexTtsController
import com.example.speech.EnglishAccent
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
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.VividViolet

@Composable
fun AiSentenceSubTab(
    words: List<VocabWordItem>,
    selectedWordIndex: Int,
    aiState: SentenceAiUiState,
    ttsController: ApexTtsController,
    onSelectWordIndex: (Int) -> Unit,
    onSubmitSentence: (VocabWordItem, String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (words.isEmpty()) return
    val safeIndex = selectedWordIndex.coerceIn(0, words.lastIndex)
    val targetWord = words[safeIndex]

    var draftSentence by rememberSaveable(targetWord.id) { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Horizontal Target Word Picker
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Từ mục tiêu:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = InkSecondary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                words.forEachIndexed { idx, item ->
                    val isSelected = idx == safeIndex
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) ElectricIndigo else CardWhite,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) ElectricIndigo else CardBorderSoft
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onSelectWordIndex(idx) }
                            .testTag("ai_sentence_word_chip_${item.word}")
                    ) {
                        Text(
                            text = item.word,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isSelected) Color.White else InkPrimary,
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Target Word Reference & Sentence Input Card
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = targetWord.word,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = ElectricIndigo
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CyanGlowLight
                            ) {
                                Text(
                                    text = targetWord.cefrLevel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricIndigo,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${targetWord.meaningVi} • ${targetWord.verbPatterns}",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                }

                OutlinedTextField(
                    value = draftSentence,
                    onValueChange = { draftSentence = it },
                    minLines = 3,
                    maxLines = 5,
                    placeholder = {
                        Text(
                            text = "Viết 1 câu tiếng Anh có chứa từ \"${targetWord.word}\"...",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted
                        )
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricIndigo,
                        unfocusedBorderColor = CardBorderSoft,
                        focusedContainerColor = SurfaceCanvas.copy(alpha = 0.55f),
                        unfocusedContainerColor = SurfaceCanvas.copy(alpha = 0.55f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_sentence_input")
                )

                // Quick sample starter helper so learners can test or edit immediately
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SurfaceCanvas,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                draftSentence = "Many experts think ${targetWord.word} is very important for modern society."
                            }
                            .testTag("ai_sentence_sample_fill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lightbulb,
                                contentDescription = null,
                                tint = VividViolet,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Điền câu mẫu Band 6.0",
                                style = MaterialTheme.typography.labelSmall,
                                color = VividViolet
                            )
                        }
                    }

                    Text(
                        text = "${draftSentence.trim().split(Regex("\\s+")).count { it.isNotBlank() }} từ",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkMuted
                    )
                }

                Button(
                    onClick = { onSubmitSentence(targetWord, draftSentence) },
                    enabled = aiState !is SentenceAiUiState.Loading && draftSentence.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricIndigo,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 13.dp, horizontal = 18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("ai_sentence_submit_button")
                ) {
                    if (aiState is SentenceAiUiState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gemini AI đang chấm & nâng cấp...",
                            style = MaterialTheme.typography.labelLarge
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.ai_sentence_submit),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // AI Evaluation Result Area
        when (aiState) {
            SentenceAiUiState.Idle -> {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = CardSurfaceTinted,
                    border = BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.14f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnimatedTeacher(
                            mood = TeacherMood.TEACHING,
                            isSpeaking = false,
                            size = 64.dp
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = stringResource(R.string.ai_sentence_title),
                                style = MaterialTheme.typography.titleSmall,
                                color = ElectricIndigo
                            )
                            Text(
                                text = stringResource(R.string.ai_sentence_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSecondary
                            )
                        }
                    }
                }
            }

            SentenceAiUiState.Loading -> {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = CardWhite,
                    border = BorderStroke(1.dp, CyanGlow.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnimatedTeacher(
                            mood = TeacherMood.TEACHING,
                            isSpeaking = true,
                            size = 68.dp
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AnimatedAudioWaveform(isActive = true, width = 26.dp, height = 14.dp)
                                Text(
                                    text = "Giám khảo Gemini AI đang phân tích...",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ElectricIndigo
                                )
                            }
                            Text(
                                text = "Kiểm tra độ tự nhiên, Collocations, giới từ đi kèm và viết lại phiên bản Band 8.5+ giữ nguyên ý của bạn.",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSecondary
                            )
                        }
                    }
                }
            }

            is SentenceAiUiState.Error -> {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFF1F2),
                    border = BorderStroke(1.dp, RoseAlert.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = RoseAlert,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = aiState.messageVi,
                                style = MaterialTheme.typography.bodySmall,
                                color = InkPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (draftSentence.isNotBlank()) {
                            Button(
                                onClick = { onSubmitSentence(targetWord, draftSentence) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RoseAlert,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("ai_sentence_retry_button")
                            ) {
                                Text(
                                    text = "Thử chấm lại ngay",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }

            is SentenceAiUiState.Success -> {
                AiEvaluationResultCard(
                    evaluation = aiState.result,
                    onSpeakUpgraded = {
                        ttsController.speak(aiState.result.upgradedBand85Sentence, EnglishAccent.UK)
                    }
                )
            }
        }
    }
}

@Composable
private fun AiEvaluationResultCard(
    evaluation: AiSentenceEvaluation,
    onSpeakUpgraded: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = CardWhite,
        border = BorderStroke(1.5.dp, ElectricIndigo.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_sentence_result_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Score & Naturalness Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnimatedTeacher(
                        mood = TeacherMood.CHEERING,
                        isSpeaking = true,
                        size = 58.dp
                    )
                    Column {
                        Text(
                            text = "Độ tự nhiên: ${evaluation.naturalnessScore}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = ElectricIndigo
                        )
                        Text(
                            text = "Nhận xét từ Gemini AI Examiner",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ElectricIndigoLight
                ) {
                    Text(
                        text = "Band ${evaluation.estimatedBand}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = ElectricIndigo,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                    )
                }
            }

            // Naturalness Feedback in Vietnamese
            Text(
                text = evaluation.naturalnessFeedbackVi,
                style = MaterialTheme.typography.bodyMedium,
                color = InkPrimary
            )

            // Corrections & Collocation Notes in Vietnamese
            if (evaluation.correctionsVi.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceCanvas
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Gợi ý sửa Collocation & Ngữ pháp:",
                            style = MaterialTheme.typography.labelMedium,
                            color = VividViolet
                        )
                        evaluation.correctionsVi.forEach { correction ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(15.dp)
                                )
                                Text(
                                    text = correction,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Band 8.5+ Upgraded Sentence Highlight Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(listOf(ElectricIndigo, CyanGlow))
                    )
                    .padding(1.5.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(17.dp),
                    color = CardWhite
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
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
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = ElectricIndigo,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(R.string.ai_sentence_upgraded_label),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ElectricIndigo
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = ElectricIndigoLight,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable(onClick = onSpeakUpgraded)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                        contentDescription = "Nghe câu Band 8.5+",
                                        tint = ElectricIndigo,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Nghe UK",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ElectricIndigo
                                    )
                                }
                            }
                        }

                        Text(
                            text = evaluation.upgradedBand85Sentence,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.5.sp
                            ),
                            color = InkPrimary
                        )

                        if (evaluation.upgradeHighlightVi.isNotBlank()) {
                            Text(
                                text = evaluation.upgradeHighlightVi,
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
