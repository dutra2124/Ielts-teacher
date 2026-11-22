package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ExamAttemptRecord
import com.example.data.IeltsModule
import com.example.data.StudyTab
import com.example.data.TeacherMood
import com.example.ui.components.AnimatedAudioWaveform
import com.example.ui.components.AnimatedFloatingGradCap
import com.example.ui.components.AnimatedReminderBell
import com.example.ui.components.AnimatedStreakFlame
import com.example.ui.components.AnimatedTeacher
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardSurfaceTinted
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkOnHero
import com.example.ui.theme.InkOnHeroMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.VividViolet
import java.util.Locale

private val PlaceholderHeroBrush = Brush.linearGradient(
    colors = listOf(ElectricIndigo, VividViolet, CyanGlow),
    start = Offset(0f, 0f),
    end = Offset(1000f, 700f)
)

/**
 * Clean bilingual placeholder screen for Tabs 2–5 (Học, Luyện, Thi, Sửa)
 * wired directly to the globally active IELTS module from Tab 1.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudyTabPlaceholderScreen(
    tab: StudyTab,
    activeModule: IeltsModule,
    isTeacherSpeaking: Boolean,
    latestExamAttempt: ExamAttemptRecord? = null,
    onTeacherTap: () -> Unit,
    onReturnToRoadmap: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Secondary tab BackHandler returns smoothly to Tab 1 (Lộ trình)
    BackHandler {
        onReturnToRoadmap()
    }

    val (titleRes, descRes, mood, teacherLineVi, stepLabelVi) = when (tab) {
        StudyTab.LEARN -> PlaceholderMeta(
            titleRes = R.string.learn_placeholder_title,
            descRes = R.string.learn_placeholder_desc,
            mood = TeacherMood.TEACHING,
            teacherLineVi = "Chúng ta sẽ khai thác sâu bộ từ vựng học thuật và cấu trúc Band 8.5 cho chủ đề \"${activeModule.titleEn}\" ngay tại đây!",
            stepLabelVi = "Bước 2 / 5 • Nạp từ vựng & Cấu trúc"
        )
        StudyTab.PRACTICE -> PlaceholderMeta(
            titleRes = R.string.practice_placeholder_title,
            descRes = R.string.practice_placeholder_desc,
            mood = TeacherMood.LISTENING,
            teacherLineVi = "Cô đã sẵn sàng lắng nghe phần luyện tập 4 kỹ năng của bạn xoay quanh chủ đề \"${activeModule.titleEn}\".",
            stepLabelVi = "Bước 3 / 5 • Luyện phản xạ thực chiến"
        )
        StudyTab.EXAM -> PlaceholderMeta(
            titleRes = R.string.exam_placeholder_title,
            descRes = R.string.exam_placeholder_desc,
            mood = TeacherMood.ENCOURAGING,
            teacherLineVi = "Giữ vững tâm lý nhé! Phòng thi thử & Chữa đề chuẩn Cambridge cho chủ đề \"${activeModule.titleEn}\" sẽ giúp bạn bứt phá Band 8.5.",
            stepLabelVi = "Bước 4 / 4 • Đề thi & Chữa đề"
        )
        StudyTab.ROADMAP -> PlaceholderMeta(
            titleRes = R.string.learn_placeholder_title,
            descRes = R.string.learn_placeholder_desc,
            mood = TeacherMood.TEACHING,
            teacherLineVi = "",
            stepLabelVi = ""
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceCanvas)
            .verticalScroll(rememberScrollState())
            .testTag("placeholder_screen_${tab.routeId}"),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Hero Banner Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(PlaceholderHeroBrush)
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.20f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f))
                ) {
                    Text(
                        text = stepLabelVi,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Text(
                    text = "${tab.labelVi} • ${tab.labelEn}",
                    style = MaterialTheme.typography.titleMedium,
                    color = InkOnHero
                )
            }

            // Teacher & Speech Bubble
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedTeacher(
                    mood = mood,
                    isSpeaking = isTeacherSpeaking,
                    size = 104.dp,
                    onClick = onTeacherTap
                )

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(
                        topStart = 8.dp,
                        topEnd = 20.dp,
                        bottomEnd = 20.dp,
                        bottomStart = 20.dp
                    ),
                    color = CardWhite
                ) {
                    Column(
                        modifier = Modifier.padding(13.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AnimatedAudioWaveform(
                                isActive = isTeacherSpeaking,
                                width = 22.dp,
                                height = 13.dp
                            )
                            Text(
                                text = mood.labelVi,
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricIndigo
                            )
                        }
                        Text(
                            text = teacherLineVi,
                            style = MaterialTheme.typography.bodySmall,
                            color = InkPrimary
                        )
                    }
                }
            }
        }

        // Newly Submitted Mock Exam Result Card on Tab 4 (Đề thi)
        if (tab == StudyTab.EXAM && latestExamAttempt != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("review_latest_exam_result_card"),
                shape = RoundedCornerShape(24.dp),
                color = CardWhite,
                border = BorderStroke(1.5.dp, ElectricIndigo.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bài thi vừa nộp • ${latestExamAttempt.submittedAtLabel}",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricIndigo
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ElectricIndigo
                        ) {
                            Text(
                                text = "Band ${String.format(Locale.US, "%.1f", latestExamAttempt.estimatedBand)}",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                    Text(
                        text = latestExamAttempt.examTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = InkPrimary
                    )
                    Text(
                        text = "Chế độ: ${latestExamAttempt.mode.labelVi} • Đúng ${latestExamAttempt.correctCount}/${latestExamAttempt.totalObjectiveQuestions} câu trắc nghiệm" +
                            (if (latestExamAttempt.writingWordCount > 0) " • Writing: ${latestExamAttempt.writingWordCount} từ" else ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )
                }
            }
        }

        // Globally Synced Active Module Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = ElectricIndigo.copy(alpha = 0.10f),
                    spotColor = ElectricIndigo.copy(alpha = 0.14f)
                ),
            shape = RoundedCornerShape(24.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, CardBorderSoft)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = activeModule.tier.accentPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.placeholder_filtered_by),
                            style = MaterialTheme.typography.labelMedium,
                            color = activeModule.tier.accentPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = activeModule.tier.containerColor
                    ) {
                        Text(
                            text = "Module #${String.format(Locale.US, "%02d", activeModule.id)} • ${activeModule.tier.bandRange}",
                            style = MaterialTheme.typography.labelSmall,
                            color = activeModule.tier.accentPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = activeModule.titleEn,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = InkPrimary
                )
                Text(
                    text = "${activeModule.titleVi} — ${activeModule.focusVi}",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    activeModule.keyCollocations.forEach { phrase ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CardSurfaceTinted
                        ) {
                            Text(
                                text = phrase,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PlusJakartaSansFontFamily
                                ),
                                color = ElectricIndigo,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // Upcoming Feature Showcase Card with Animated Icons
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, CardBorderSoft)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Distinct animated icon per tab
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(CardSurfaceTinted),
                    contentAlignment = Alignment.Center
                ) {
                    when (tab) {
                        StudyTab.LEARN -> AnimatedFloatingGradCap(size = 56.dp)
                        StudyTab.PRACTICE -> AnimatedAudioWaveform(
                            isActive = true,
                            width = 44.dp,
                            height = 24.dp,
                            primaryColor = ElectricIndigo,
                            secondaryColor = CyanGlow
                        )
                        StudyTab.EXAM -> AnimatedReminderBell(
                            size = 38.dp,
                            tint = ElectricIndigo,
                            isActive = true
                        )
                        StudyTab.ROADMAP -> AnimatedFloatingGradCap(size = 56.dp)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = ElectricIndigo.copy(alpha = 0.10f)
                ) {
                    Text(
                        text = stringResource(R.string.placeholder_step_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricIndigo,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.titleLarge,
                    color = InkPrimary
                )

                Text(
                    text = stringResource(descRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSecondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onReturnToRoadmap,
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricIndigo,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("back_to_roadmap_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.placeholder_back_to_roadmap),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private data class PlaceholderMeta(
    val titleRes: Int,
    val descRes: Int,
    val mood: TeacherMood,
    val teacherLineVi: String,
    val stepLabelVi: String
)
