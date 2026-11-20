package com.example.ui.screens.dashboard

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CoachVoiceQuote
import com.example.data.PracticeSkill
import com.example.data.TeacherMood
import com.example.data.WeakAreaCoaching
import com.example.speech.CoachVoiceLanguage
import com.example.ui.components.AnimatedAudioWaveform
import com.example.ui.components.AnimatedTeacher
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardSurfaceTinted
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CyanGlow
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
import com.example.ui.theme.LocalApexColors
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.PracticeTeal
import com.example.ui.theme.StreakOrange
import com.example.ui.theme.VividViolet

private val WarmCoachHeroLight = Brush.linearGradient(
    colors = listOf(Color(0xFFFFF7ED), Color(0xFFF5F3FF), Color(0xFFECFEFF)),
    start = Offset(0f, 0f),
    end = Offset(980f, 720f)
)

private val WarmCoachHeroDark = Brush.linearGradient(
    colors = listOf(Color(0xFF281F36), Color(0xFF1B2340), Color(0xFF132A34)),
    start = Offset(0f, 0f),
    end = Offset(980f, 720f)
)

/**
 * Warm, motivating Voice Encouragement & Smart Weak-Area Coaching Card.
 * Synchronizes Android TextToSpeech (vi-VN / en-GB / en-US) directly with
 * the Animated AI Teacher's lip-sync animation and animated waveform bars.
 */
@Composable
fun VoiceCoachCard(
    quote: CoachVoiceQuote,
    quoteIndex: Int,
    totalQuotes: Int,
    weakArea: WeakAreaCoaching,
    isHighDiligence: Boolean,
    isSpeaking: Boolean,
    isPaused: Boolean,
    selectedLanguage: CoachVoiceLanguage,
    speechSpeed: Float,
    autoGreetEnabled: Boolean,
    onPlayOrResume: () -> Unit,
    onPause: () -> Unit,
    onNextQuote: () -> Unit,
    onSelectLanguage: (CoachVoiceLanguage) -> Unit,
    onSelectSpeed: (Float) -> Unit,
    onToggleAutoGreet: (Boolean) -> Unit,
    onPracticeWeakSkill: (PracticeSkill, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalApexColors.current.isDark
    val teacherMood = if (isHighDiligence) TeacherMood.CHEERING else TeacherMood.ENCOURAGING
    val activeSpeechText = if (selectedLanguage == CoachVoiceLanguage.VIETNAMESE) {
        quote.textVi
    } else {
        quote.textEn
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = VividViolet.copy(alpha = 0.10f),
                spotColor = StreakOrange.copy(alpha = 0.14f)
            )
            .testTag("voice_coach_card"),
        shape = RoundedCornerShape(26.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, if (isDark) VividViolet.copy(alpha = 0.35f) else StreakOrange.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isDark) WarmCoachHeroDark else WarmCoachHeroLight)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header + Animated AI Teacher + Speech Quote Bubble
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AnimatedTeacher(
                        mood = teacherMood,
                        isSpeaking = isSpeaking,
                        size = 98.dp,
                        contentDescriptionText = "Cô giáo AI đang động viên học tập",
                        onClick = if (isSpeaking) onPause else onPlayOrResume
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isHighDiligence) EmeraldSuccessLight else GoldMasteryLight
                    ) {
                        Text(
                            text = if (isHighDiligence) "Tự hào" else "Tiếp lửa",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isHighDiligence) EmeraldOnContainer else GoldOnContainer,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = ElectricIndigoLight
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = IndigoOnSurface,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "${quote.badgeVi} (${quoteIndex + 1}/$totalQuotes)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = IndigoOnSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        AnimatedAudioWaveform(
                            isActive = isSpeaking,
                            barCount = 5,
                            primaryColor = StreakOrange,
                            secondaryColor = VividViolet,
                            width = 34.dp,
                            height = 18.dp,
                            contentDescriptionText = "Sóng âm giọng cô giáo"
                        )
                    }

                    // Dynamic Speech Quote Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CardWhite.copy(alpha = if (isDark) 0.88f else 0.96f),
                        border = BorderStroke(1.dp, CardBorderSoft)
                    ) {
                        AnimatedContent(
                            targetState = activeSpeechText,
                            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(140)) },
                            label = "coach_quote_transition"
                        ) { textToDisplay ->
                            Text(
                                text = "“$textToDisplay”",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    lineHeight = 20.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = InkPrimary,
                                modifier = Modifier.padding(11.dp)
                            )
                        }
                    }
                }
            }

            // Prominent Primary CTA: "Nghe cô giáo động viên" + Pause / Next Quote
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = if (isSpeaking) onPause else onPlayOrResume,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSpeaking) StreakOrange else ElectricIndigo,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("coach_voice_play_pause_btn")
                ) {
                    Icon(
                        imageVector = when {
                            isSpeaking -> Icons.Rounded.Pause
                            isPaused -> Icons.Rounded.PlayArrow
                            else -> Icons.Rounded.VolumeUp
                        },
                        contentDescription = null,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = when {
                            isSpeaking -> "Tạm dừng giọng đọc"
                            isPaused -> "Nghe tiếp lời động viên"
                            else -> stringResource(R.string.dashboard_listen_coach_btn)
                        },
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = onNextQuote,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("coach_voice_next_quote_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Lời động viên tiếp theo",
                        tint = IndigoOnSurface,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Câu khác",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = IndigoOnSurface
                    )
                }
            }

            // Voice Controls Strip: Language (vi-VN / UK / US) + Speed (0.9x / 1.0x / 1.1x)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CoachVoiceLanguage.entries.forEach { lang ->
                    val selected = lang == selectedLanguage
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (selected) VividViolet else CardWhite,
                        border = BorderStroke(1.dp, if (selected) VividViolet else CardBorderSoft),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onSelectLanguage(lang) }
                            .testTag("coach_voice_lang_${lang.id}")
                    ) {
                        Text(
                            text = lang.shortLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (selected) Color.White else InkSecondary,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Rounded.Speed,
                    contentDescription = null,
                    tint = InkMuted,
                    modifier = Modifier.size(15.dp)
                )

                listOf(0.9f to "0.9x", 1.0f to "1.0x", 1.1f to "1.1x").forEach { (rate, label) ->
                    val selected = kotlin.math.abs(speechSpeed - rate) < 0.02f
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (selected) PracticeTeal else CardWhite,
                        border = BorderStroke(1.dp, if (selected) PracticeTeal else CardBorderSoft),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onSelectSpeed(rate) }
                            .testTag("coach_voice_speed_$label")
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (selected) Color.White else InkSecondary,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Auto-greet on Dashboard open toggle (persisted)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = CardWhite.copy(alpha = if (isDark) 0.72f else 0.85f),
                border = BorderStroke(1.dp, CardBorderSoft)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_auto_greet_toggle),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = InkPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = autoGreetEnabled,
                        onCheckedChange = onToggleAutoGreet,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ElectricIndigo
                        ),
                        modifier = Modifier.testTag("dashboard_auto_greet_switch")
                    )
                }
            }

            HorizontalDivider(color = CardBorderSoft)

            // Smart Weak-Area Coaching + Concrete 1-Tap Action Button
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, PracticeTeal.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(13.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GoldMasteryLight
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TipsAndUpdates,
                                contentDescription = null,
                                tint = GoldMastery,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(16.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Điểm cần cải thiện nhất hôm nay",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = GoldOnContainer
                            )
                            Text(
                                text = weakArea.weakAreaTitleVi,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = InkPrimary
                            )
                        }
                    }

                    Text(
                        text = weakArea.diagnosisVi,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
                        color = InkSecondary
                    )

                    Button(
                        onClick = { onPracticeWeakSkill(weakArea.weakSkill, weakArea.focusNoteVi) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PracticeTeal,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("dashboard_weak_area_cta_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = weakArea.actionLabelVi,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
