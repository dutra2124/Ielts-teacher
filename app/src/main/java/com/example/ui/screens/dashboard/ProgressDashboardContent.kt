package com.example.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.DiligenceDashboardSnapshot
import com.example.data.PracticeSkill
import com.example.speech.CoachVoiceLanguage
import com.example.speech.CoachVoiceTtsController

/**
 * Full "Dashboard Chăm chỉ" view rendered smoothly inside Tab 1 when the user switches
 * to the Dashboard segment or taps the clickable summary banner in the header.
 * Owns the CoachVoiceTtsController lifecycle so Voice Encouragement automatically stops on leave.
 */
@Composable
fun ProgressDashboardContent(
    snapshot: DiligenceDashboardSnapshot,
    streakDays: Int,
    autoGreetEnabled: Boolean,
    onToggleAutoGreet: (Boolean) -> Unit,
    onPracticeWeakSkill: (PracticeSkill, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var quoteIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedLanguage by remember { mutableStateOf(CoachVoiceLanguage.VIETNAMESE) }
    var speechSpeed by rememberSaveable { mutableFloatStateOf(1.0f) }
    var isCoachSpeaking by remember { mutableStateOf(false) }
    var isCoachPaused by remember { mutableStateOf(false) }
    var isTtsReady by remember { mutableStateOf(false) }
    var hasAutoGreetedThisVisit by remember { mutableStateOf(false) }

    val voiceController = remember(context) {
        CoachVoiceTtsController(
            context = context,
            onSpeakingChanged = { speaking ->
                isCoachSpeaking = speaking
                if (speaking) isCoachPaused = false
            },
            onReadyChanged = { ready -> isTtsReady = ready }
        )
    }

    DisposableEffect(voiceController) {
        onDispose { voiceController.shutdown() }
    }

    val safeIndex = quoteIndex % snapshot.voiceQuotes.size.coerceAtLeast(1)
    val currentQuote = snapshot.voiceQuotes[safeIndex]

    fun speakCurrentQuote(lang: CoachVoiceLanguage = selectedLanguage, speed: Float = speechSpeed, idx: Int = safeIndex) {
        val quote = snapshot.voiceQuotes[idx % snapshot.voiceQuotes.size.coerceAtLeast(1)]
        val text = if (lang == CoachVoiceLanguage.VIETNAMESE) quote.textVi else quote.textEn
        isCoachPaused = false
        voiceController.speakQuote(text = text, language = lang, speechRate = speed)
    }

    // Optional Auto-Greet when opening Dashboard (if user enabled the toggle)
    LaunchedEffect(autoGreetEnabled, isTtsReady) {
        if (autoGreetEnabled && isTtsReady && !hasAutoGreetedThisVisit) {
            hasAutoGreetedThisVisit = true
            speakCurrentQuote()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("progress_dashboard_content"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Warm Voice Encouragement & Weak-Area Coaching Card
        VoiceCoachCard(
            quote = currentQuote,
            quoteIndex = safeIndex,
            totalQuotes = snapshot.voiceQuotes.size,
            weakArea = snapshot.weakAreaCoaching,
            isHighDiligence = snapshot.isHighDiligence,
            isSpeaking = isCoachSpeaking,
            isPaused = isCoachPaused,
            selectedLanguage = selectedLanguage,
            speechSpeed = speechSpeed,
            autoGreetEnabled = autoGreetEnabled,
            onPlayOrResume = {
                if (isCoachPaused && voiceController.hasPausedQuote()) {
                    voiceController.resume()
                } else {
                    speakCurrentQuote()
                }
            },
            onPause = {
                voiceController.pause()
                isCoachPaused = true
            },
            onNextQuote = {
                val nextIdx = (safeIndex + 1) % snapshot.voiceQuotes.size.coerceAtLeast(1)
                quoteIndex = nextIdx
                speakCurrentQuote(idx = nextIdx)
            },
            onSelectLanguage = { lang ->
                selectedLanguage = lang
                if (isCoachSpeaking) {
                    speakCurrentQuote(lang = lang)
                }
            },
            onSelectSpeed = { rate ->
                speechSpeed = rate
                if (isCoachSpeaking) {
                    speakCurrentQuote(speed = rate)
                }
            },
            onToggleAutoGreet = onToggleAutoGreet,
            onPracticeWeakSkill = { skill, note ->
                voiceController.stop()
                onPracticeWeakSkill(skill, note)
            }
        )

        // 2. Diligence Score Card ("Chỉ số chăm chỉ" 0-100% Animated Radial Gauge)
        DiligenceScoreCard(
            snapshot = snapshot,
            streakDays = streakDays
        )

        // 3. 28-Day Study Consistency Heatmap ("Lịch nhiệt 28 ngày")
        ConsistencyHeatmapCard(
            heatmapDays = snapshot.heatmapDays
        )

        // 4. Weekly Study Activity Vertical Bar Chart (T2-CN vs Daily Target)
        WeeklyStudyActivityChartCard(
            bars = snapshot.weeklyBars,
            dailyTargetMinutes = snapshot.dailyTargetMinutes,
            weeklyMinutesTotal = snapshot.weeklyMinutesTotal
        )

        // 5. 4-Skill Band Radar + Progress Bars & Learning Journey Timeline
        SkillRadarAndJourneyCard(
            skills = snapshot.skillProgress,
            milestones = snapshot.milestones,
            onSkillClick = { skill ->
                voiceController.stop()
                onPracticeWeakSkill(
                    skill,
                    "Luyện chuyên sâu ${skill.labelEn} từ Dashboard Chăm chỉ"
                )
            }
        )
    }
}
