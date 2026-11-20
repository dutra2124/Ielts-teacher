package com.example.ui.screens.practice

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.ManageSearch
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ai.SyntaxBreakdownResult
import com.example.ai.SyntaxBreakdownUiState
import com.example.data.Band85VocabHint
import com.example.data.TeacherMood
import com.example.data.ThreeLevelHint
import com.example.speech.ApexTtsController
import com.example.speech.EnglishAccent
import com.example.ui.components.AnimatedAudioWaveform
import com.example.ui.components.AnimatedTeacher
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardWhite
import com.example.ui.theme.GoldMastery
import com.example.ui.theme.GoldMasteryLight
import com.example.ui.theme.GoldOnContainer
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
import com.example.ui.theme.TealOnSurface
import com.example.ui.theme.VividViolet
import com.example.ui.theme.VividVioletLight

/**
 * Assistant 1 Bottom Sheet: Soi Ngữ pháp Câu (AI Syntax Breakdown).
 * Displays Gemini AI structural dissection of any tapped complex sentence from Reading / Listening
 * (or selectable complex sentences when opened directly).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyntaxBreakdownBottomSheet(
    syntaxState: SyntaxBreakdownUiState,
    quickPickSentences: List<String>,
    onPickSentence: (String) -> Unit,
    onRetry: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("syntax_breakdown_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PracticeTealLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ManageSearch,
                            contentDescription = null,
                            tint = PracticeTealDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.practice_syntax_sheet_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = InkPrimary
                        )
                        Text(
                            text = "Bóc tách Mệnh đề chính • Mệnh đề phụ • Đảo ngữ / Rút gọn",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("syntax_sheet_close")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Đóng cửa sổ soi ngữ pháp",
                        tint = InkSecondary
                    )
                }
            }

            when (syntaxState) {
                SyntaxBreakdownUiState.Idle -> {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = PracticeTealSurface,
                        border = BorderStroke(1.dp, PracticeTeal.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AnimatedTeacher(mood = TeacherMood.TEACHING, isSpeaking = false, size = 56.dp)
                            Text(
                                text = "Chạm trực tiếp vào câu phức trong bài Reading / Transcript hoặc chọn nhanh 1 câu tiêu biểu bên dưới để Gemini AI bóc tách ngay!",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkPrimary
                            )
                        }
                    }

                    quickPickSentences.take(4).forEachIndexed { idx, sentence ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SurfaceCanvas,
                            border = BorderStroke(1.dp, CardBorderSoft),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onPickSentence(sentence) }
                                .testTag("syntax_quick_pick_$idx")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Câu phức #${idx + 1} • Chạm để soi",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PracticeTealDark
                                )
                                Text(
                                    text = sentence,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PlusJakartaSansFontFamily
                                    ),
                                    color = InkPrimary
                                )
                            }
                        }
                    }
                }

                is SyntaxBreakdownUiState.Loading -> {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = PracticeTealSurface,
                        border = BorderStroke(1.dp, PracticeTeal.copy(alpha = 0.30f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "\"${syntaxState.sentenceEn}\"",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = InkPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = PracticeTeal,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                                AnimatedAudioWaveform(
                                    isActive = true,
                                    width = 24.dp,
                                    height = 14.dp,
                                    primaryColor = PracticeTeal,
                                    secondaryColor = PracticeTealDark
                                )
                                Text(
                                    text = "Gemini AI đang bóc tách cấu trúc câu...",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PracticeTealDark
                                )
                            }
                        }
                    }
                }

                is SyntaxBreakdownUiState.Error -> {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
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
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ErrorOutline,
                                    contentDescription = null,
                                    tint = RoseAlert,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = syntaxState.messageVi,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkPrimary
                                )
                            }
                            Button(
                                onClick = { onRetry(syntaxState.sentenceEn) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoseAlert),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("syntax_retry_button")
                            ) {
                                Text("Thử bóc tách lại")
                            }
                        }
                    }
                }

                is SyntaxBreakdownUiState.Success -> {
                    SyntaxBreakdownResultBody(result = syntaxState.result)
                }
            }
        }
    }
}

@Composable
private fun SyntaxBreakdownResultBody(result: SyntaxBreakdownResult) {
    // Original Sentence Quote
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = SurfaceCanvas,
        border = BorderStroke(1.dp, CardBorderSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Câu đang phân tích",
                style = MaterialTheme.typography.labelSmall,
                color = InkMuted
            )
            Text(
                text = result.sentenceEn,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.SemiBold
                ),
                color = InkPrimary
            )
        }
    }

    // 1. Mệnh đề chính (Main Clause)
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = PracticeTealSurface,
        border = BorderStroke(1.dp, PracticeTeal.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "1. MỆNH ĐỀ CHÍNH (MAIN CLAUSE)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = PracticeTealDark
            )
            Text(
                text = result.mainClauseEn,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = InkPrimary
            )
            if (result.mainClauseExplainVi.isNotBlank()) {
                Text(
                    text = result.mainClauseExplainVi,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary
                )
            }
        }
    }

    // 2. Mệnh đề phụ (Subordinate Clauses)
    if (result.subordinateClausesVi.isNotEmpty()) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = VividVioletLight.copy(alpha = 0.55f),
            border = BorderStroke(1.dp, VividViolet.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "2. MỆNH ĐỀ PHỤ / QUAN HỆ / TRẠNG NGỮ",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = VividViolet
                )
                result.subordinateClausesVi.forEach { clause ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = VividViolet,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(15.dp)
                        )
                        Text(
                            text = clause,
                            style = MaterialTheme.typography.bodySmall,
                            color = InkPrimary
                        )
                    }
                }
            }
        }
    }

    // 3. Cấu trúc rút gọn / đảo ngữ (Advanced Structures)
    if (result.advancedStructuresVi.isNotEmpty()) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = GoldMasteryLight.copy(alpha = 0.65f),
            border = BorderStroke(1.dp, GoldMastery.copy(alpha = 0.40f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "3. CẤU TRÚC RÚT GỌN / ĐẢO NGỮ / C1–C2",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = GoldOnContainer
                )
                result.advancedStructuresVi.forEach { struct ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = GoldMastery,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(15.dp)
                        )
                        Text(
                            text = struct,
                            style = MaterialTheme.typography.bodySmall,
                            color = InkPrimary
                        )
                    }
                }
            }
        }
    }

    // 4. Nghĩa tiếng Việt & Mẹo tái sử dụng Band 8.5
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, CardBorderSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Dịch nghĩa học thuật tiếng Việt",
                style = MaterialTheme.typography.labelMedium,
                color = TealOnSurface
            )
            Text(
                text = result.vietnameseTranslation,
                style = MaterialTheme.typography.bodyMedium,
                color = InkPrimary
            )
            if (result.band85ReuseTipVi.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PracticeTealSurface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Psychology,
                            contentDescription = null,
                            tint = TealOnSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Mẹo áp dụng Writing/Speaking: ${result.band85ReuseTipVi}",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkPrimary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Assistant 2A Bottom Sheet (Reading / Listening): 3-Level Smart Hints.
 * Unlocks progressively: Level 1 (Locate paragraph) -> Level 2 (Paraphrase pair) -> Level 3 (Distractor trap).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreeLevelHintsBottomSheet(
    hints: List<ThreeLevelHint>,
    selectedQuestionIndex: Int,
    unlockedLevelByQuestion: Map<Int, Int>,
    onSelectQuestion: (Int) -> Unit,
    onUnlockNextLevel: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val safeIndex = selectedQuestionIndex.coerceIn(0, (hints.size - 1).coerceAtLeast(0))
    val currentHint = hints.getOrNull(safeIndex)
    val unlockedLevel = unlockedLevelByQuestion[safeIndex] ?: 1

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("three_level_hints_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(GoldMasteryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.TipsAndUpdates,
                            contentDescription = null,
                            tint = GoldOnContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.practice_hints_sheet_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = InkPrimary
                        )
                        Text(
                            text = "Mở khóa từng bước để tự tư duy không bị lộ đáp án sớm",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Đóng gợi ý", tint = InkSecondary)
                }
            }

            // Question Selector Pills
            if (hints.size > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    hints.forEachIndexed { idx, _ ->
                        val active = idx == safeIndex
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (active) PracticeTeal else SurfaceCanvas,
                            border = BorderStroke(1.dp, if (active) PracticeTeal else CardBorderSoft),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { onSelectQuestion(idx) }
                                .testTag("hint_q_pill_$idx")
                        ) {
                            Text(
                                text = "Câu ${idx + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (active) Color.White else InkSecondary,
                                modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            if (currentHint != null) {
                Text(
                    text = currentHint.questionLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = PracticeTealDark
                )

                // Level 1: Always unlocked when sheet opens
                HintLevelCard(
                    level = 1,
                    title = "Cấp 1 • Khoanh vùng đoạn chứa đáp án",
                    body = currentHint.level1LocateVi,
                    isUnlocked = unlockedLevel >= 1,
                    accentColor = PracticeTeal,
                    bgColor = PracticeTealSurface
                )

                // Level 2: Paraphrase Pair
                HintLevelCard(
                    level = 2,
                    title = "Cấp 2 • Chỉ ra cụm Paraphrase đối chiếu",
                    body = currentHint.level2ParaphraseVi,
                    isUnlocked = unlockedLevel >= 2,
                    accentColor = VividViolet,
                    bgColor = VividVioletLight.copy(alpha = 0.55f)
                )

                // Level 3: Distractor Trap Warning
                HintLevelCard(
                    level = 3,
                    title = "Cấp 3 • Cảnh báo bẫy Distractor",
                    body = currentHint.level3DistractorVi,
                    isUnlocked = unlockedLevel >= 3,
                    accentColor = RoseAlert,
                    bgColor = Color(0xFFFFF1F2),
                    isWarning = true
                )

                if (unlockedLevel < 3) {
                    Button(
                        onClick = { onUnlockNextLevel(safeIndex) },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PracticeTeal,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(vertical = 13.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("unlock_next_hint_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LockOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mở khóa Gợi ý Cấp ${unlockedLevel + 1} / 3",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HintLevelCard(
    level: Int,
    title: String,
    body: String,
    isUnlocked: Boolean,
    accentColor: Color,
    bgColor: Color,
    isWarning: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isUnlocked) bgColor else SurfaceCanvas,
        border = BorderStroke(
            1.dp,
            if (isUnlocked) accentColor.copy(alpha = 0.40f) else CardBorderSoft
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = when {
                        !isUnlocked -> Icons.Rounded.Lock
                        isWarning -> Icons.Rounded.WarningAmber
                        else -> Icons.Rounded.LockOpen
                    },
                    contentDescription = null,
                    tint = if (isUnlocked) accentColor else InkMuted,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isUnlocked) accentColor else InkMuted
                )
            }
            AnimatedVisibility(visible = isUnlocked) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkPrimary
                )
            }
            if (!isUnlocked) {
                Text(
                    text = "Đang khóa — Nhấn nút bên dưới nếu bạn cần thêm gợi ý Cấp $level.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted
                )
            }
        }
    }
}

/**
 * Assistant 2B Bottom Sheet (Writing / Speaking): Band 8.5+ Topic Vocabulary & Idioms Drawer.
 * Curated by AI Teacher for the active topic with 1-tap insert into Writing/Speaking draft.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Band85VocabHintsBottomSheet(
    topicEn: String,
    skillLabelVi: String,
    vocabHints: List<Band85VocabHint>,
    ttsController: ApexTtsController,
    onInsertPhrase: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("band85_vocab_hints_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    AnimatedTeacher(mood = TeacherMood.TEACHING, isSpeaking = true, size = 56.dp)
                    Column {
                        Text(
                            text = stringResource(R.string.practice_vocab_sheet_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = PracticeTealDark
                        )
                        Text(
                            text = "Gợi ý chuẩn chủ đề \"$topicEn\" • Bấm để chèn vào bài $skillLabelVi",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Đóng ngăn từ vựng", tint = InkSecondary)
                }
            }

            vocabHints.forEachIndexed { idx, item ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = SurfaceCanvas,
                    border = BorderStroke(1.dp, CardBorderSoft),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PracticeTealLight
                            ) {
                                Text(
                                    text = item.categoryVi,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PracticeTealDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = CircleShape,
                                    color = CardWhite,
                                    border = BorderStroke(1.dp, CardBorderSoft),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .clickable { ttsController.speak(item.phraseEn, EnglishAccent.UK) }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                            contentDescription = "Nghe phát âm ${item.phraseEn}",
                                            tint = PracticeTealDark,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                OutlinedButton(
                                    onClick = { onInsertPhrase(item.phraseEn) },
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, PracticeTeal),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(34.dp)
                                        .testTag("insert_vocab_hint_$idx")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AddCircleOutline,
                                        contentDescription = null,
                                        tint = PracticeTealDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Chèn",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = PracticeTealDark
                                    )
                                }
                            }
                        }

                        Text(
                            text = item.phraseEn,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = InkPrimary
                        )
                        Text(
                            text = item.meaningVi,
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                        Text(
                            text = "VD: ${item.exampleEn}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PlusJakartaSansFontFamily
                            ),
                            color = PracticeTealDark
                        )
                    }
                }
            }
        }
    }
}
