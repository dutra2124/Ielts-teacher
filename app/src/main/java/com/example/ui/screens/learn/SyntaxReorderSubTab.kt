package com.example.ui.screens.learn

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.LearnCurriculum
import com.example.data.SyntaxExercise
import com.example.data.TeacherMood
import com.example.speech.ApexTtsController
import com.example.speech.EnglishAccent
import com.example.ui.components.AnimatedTeacher
import com.example.ui.components.answerFeedbackShake
import com.example.ui.components.rememberSemanticAnswerBorderColor
import com.example.ui.components.tactilePress
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
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class SyntaxCheckStatus {
    BUILDING,
    ERROR,
    SUCCESS
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SyntaxReorderSubTab(
    ttsController: ApexTtsController,
    onExerciseSolved: (String) -> Unit,
    modifier: Modifier = Modifier,
    exercises: List<SyntaxExercise> = LearnCurriculum.syntaxExercises
) {
    val activeExercises = exercises.ifEmpty { LearnCurriculum.syntaxExercises }
    var currentIndex by remember { mutableIntStateOf(0) }
    val exercise = activeExercises[currentIndex.coerceIn(0, activeExercises.lastIndex)]

    var selectedIndices by remember(exercise.id) { mutableStateOf<List<Int>>(emptyList()) }
    var checkStatus by remember(exercise.id) { mutableStateOf(SyntaxCheckStatus.BUILDING) }
    var solvedIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    var errorShakeTrigger by remember(exercise.id) { mutableIntStateOf(0) }

    val boxBorderColor by rememberSemanticAnswerBorderColor(
        isCorrect = when (checkStatus) {
            SyntaxCheckStatus.SUCCESS -> true
            SyntaxCheckStatus.ERROR -> false
            SyntaxCheckStatus.BUILDING -> null
        },
        defaultColor = ElectricIndigo.copy(alpha = 0.25f)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 5-Exercise Number Pill Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Câu phức (${solvedIds.size}/${activeExercises.size})",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = InkSecondary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                activeExercises.forEachIndexed { idx, item ->
                    val isCurrent = idx == currentIndex
                    val isSolved = solvedIds.contains(item.id)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            isCurrent -> ElectricIndigo
                            isSolved -> EmeraldSuccessLight
                            else -> CardWhite
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                isCurrent -> ElectricIndigo
                                isSolved -> EmeraldSuccess
                                else -> CardBorderSoft
                            }
                        ),
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { currentIndex = idx }
                            .testTag("syntax_exercise_tab_${idx + 1}")
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${idx + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                color = when {
                                    isCurrent -> Color.White
                                    isSolved -> EmeraldSuccess
                                    else -> InkSecondary
                                }
                            )
                        }
                    }
                }
            }
        }

        // Exercise Prompt Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, CardBorderSoft)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ElectricIndigoLight
                    ) {
                        Text(
                            text = exercise.structureTagVi,
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricIndigo,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CyanGlowLight
                    ) {
                        Text(
                            text = exercise.structureNameEn,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PlusJakartaSansFontFamily
                            ),
                            color = ElectricIndigo,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = exercise.meaningVi,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = InkPrimary
                )
            }
        }

        // Answer Drop-Zone with Shake on Error & Emerald Glow on Success
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .answerFeedbackShake(triggerKey = errorShakeTrigger)
                .shadow(
                    elevation = if (checkStatus == SyntaxCheckStatus.SUCCESS) 14.dp else 4.dp,
                    shape = RoundedCornerShape(22.dp),
                    ambientColor = if (checkStatus == SyntaxCheckStatus.SUCCESS) {
                        EmeraldSuccess.copy(alpha = 0.35f)
                    } else {
                        ElectricIndigo.copy(alpha = 0.08f)
                    },
                    spotColor = if (checkStatus == SyntaxCheckStatus.SUCCESS) {
                        EmeraldSuccess.copy(alpha = 0.45f)
                    } else {
                        ElectricIndigo.copy(alpha = 0.12f)
                    }
                )
                .testTag("syntax_answer_tray"),
            shape = RoundedCornerShape(22.dp),
            color = when (checkStatus) {
                SyntaxCheckStatus.SUCCESS -> EmeraldSuccessLight.copy(alpha = 0.50f)
                SyntaxCheckStatus.ERROR -> Color(0xFFFFF1F2)
                SyntaxCheckStatus.BUILDING -> CardWhite
            },
            border = BorderStroke(
                width = if (checkStatus == SyntaxCheckStatus.BUILDING) 1.5.dp else 2.dp,
                color = boxBorderColor
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 112.dp)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedIndices.isEmpty()) {
                    Text(
                        text = "Chạm vào các mảnh cụm từ bên dưới để ghép thành câu Band 8.5+ hoàn chỉnh...",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedIndices.forEach { chunkIdx ->
                            val chunkText = exercise.scrambledChunks[chunkIdx]
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = when (checkStatus) {
                                    SyntaxCheckStatus.SUCCESS -> EmeraldSuccess
                                    SyntaxCheckStatus.ERROR -> RoseAlert
                                    SyntaxCheckStatus.BUILDING -> ElectricIndigo
                                },
                                modifier = Modifier
                                    .tactilePress(enabled = checkStatus != SyntaxCheckStatus.SUCCESS)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable(enabled = checkStatus != SyntaxCheckStatus.SUCCESS) {
                                        selectedIndices = selectedIndices - chunkIdx
                                        checkStatus = SyntaxCheckStatus.BUILDING
                                    }
                            ) {
                                Text(
                                    text = chunkText,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontFamily = PlusJakartaSansFontFamily
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Scrambled Phrase Bank
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("syntax_word_bank"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            exercise.scrambledChunks.forEachIndexed { idx, chunk ->
                val alreadyUsed = selectedIndices.contains(idx)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (alreadyUsed) SurfaceCanvas else CardWhite,
                    border = BorderStroke(
                        1.dp,
                        if (alreadyUsed) CardBorderSoft.copy(alpha = 0.5f) else ElectricIndigo.copy(alpha = 0.30f)
                    ),
                    modifier = Modifier
                        .tactilePress(enabled = !alreadyUsed && checkStatus != SyntaxCheckStatus.SUCCESS)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(enabled = !alreadyUsed && checkStatus != SyntaxCheckStatus.SUCCESS) {
                            selectedIndices = selectedIndices + idx
                            checkStatus = SyntaxCheckStatus.BUILDING
                        }
                        .testTag("syntax_chip_$idx")
                ) {
                    Text(
                        text = chunk,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = PlusJakartaSansFontFamily
                        ),
                        color = if (alreadyUsed) InkMuted.copy(alpha = 0.35f) else InkPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                    )
                }
            }
        }

        // Action Buttons (Reset + Check / Next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    selectedIndices = emptyList()
                    checkStatus = SyntaxCheckStatus.BUILDING
                },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorderSoft),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("syntax_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    tint = InkSecondary,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.syntax_reset_btn),
                    color = InkSecondary
                )
            }

            if (checkStatus == SyntaxCheckStatus.SUCCESS) {
                Button(
                    onClick = { currentIndex = (currentIndex + 1) % activeExercises.size },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldSuccess,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .tactilePress()
                        .testTag("syntax_next_button")
                ) {
                    Text(text = stringResource(R.string.syntax_next_btn))
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                }
            } else {
                Button(
                    onClick = {
                        val assembled = selectedIndices.map { exercise.scrambledChunks[it] }
                        if (assembled == exercise.orderedChunks) {
                            checkStatus = SyntaxCheckStatus.SUCCESS
                            solvedIds = solvedIds + exercise.id
                            onExerciseSolved(exercise.id)
                        } else {
                            checkStatus = SyntaxCheckStatus.ERROR
                            errorShakeTrigger++
                        }
                    },
                    enabled = selectedIndices.size == exercise.orderedChunks.size,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricIndigo,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .tactilePress(enabled = selectedIndices.size == exercise.orderedChunks.size)
                        .testTag("syntax_check_button")
                ) {
                    Text(text = stringResource(R.string.syntax_check_btn))
                }
            }
        }

        // Error Hint Feedback
        AnimatedVisibility(visible = checkStatus == SyntaxCheckStatus.ERROR) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFFFF1F2),
                border = BorderStroke(1.dp, RoseAlert.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnimatedTeacher(
                        mood = TeacherMood.ENCOURAGING,
                        isSpeaking = true,
                        size = 54.dp
                    )
                    Text(
                        text = "Chưa khớp trật tự ngữ pháp rồi! Mẹo: hãy bắt đầu bằng cụm \"${exercise.orderedChunks.first()}\" để kích hoạt cấu trúc ${exercise.structureTagVi} nhé.",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Success Celebration + Vietnamese Grammar Explanation + AI Teacher CHEERING mood
        AnimatedVisibility(visible = checkStatus == SyntaxCheckStatus.SUCCESS) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = EmeraldSuccessLight.copy(alpha = 0.72f),
                border = BorderStroke(1.5.dp, EmeraldSuccess),
                modifier = Modifier.testTag("syntax_success_card")
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
                        AnimatedTeacher(
                            mood = TeacherMood.CHEERING,
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
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Chính xác chuẩn Band 8.5+!",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = EmeraldSuccess
                                )
                            }
                            Text(
                                text = exercise.grammarExplanationVi,
                                style = MaterialTheme.typography.bodySmall,
                                color = InkPrimary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CardWhite,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                ttsController.speak(
                                    exercise.orderedChunks.joinToString(" "),
                                    EnglishAccent.UK
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = "Nghe câu hoàn chỉnh",
                                tint = ElectricIndigo,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = exercise.orderedChunks.joinToString(" "),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = ElectricIndigo
                            )
                        }
                    }
                }
            }
        }
    }
}
