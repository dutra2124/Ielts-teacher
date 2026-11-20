package com.example.ui.screens.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CompareArrows
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ai.ReviewDiffAiUiState
import com.example.data.UpgradeDiffChange
import com.example.data.UpgradeDiffComparison
import com.example.speech.ApexTtsController
import com.example.speech.EnglishAccent
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardWhite
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.IndigoOnSurface
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseDiff
import com.example.ui.theme.RoseDiffLight
import com.example.ui.theme.SurfaceCanvas

/**
 * 20:9 Vertical-Friendly Original vs. Band 8.5+ Upgrade Diff View:
 * - Supports Writing & Speaking submissions
 * - Toggle between Stacked view (Trên/Dưới) and Tabbed Quick-Flip view (Gốc / Band 8.5+)
 * - Original weak phrases underlined in Rose (#E11D48); Band 8.5+ upgrades highlighted in Emerald (#10B981)
 * - Tap any change item to read detailed Vietnamese examiner explanation + on-demand Gemini AI rewrite
 */
@Composable
fun ReviewDiffViewCard(
    writingDiff: UpgradeDiffComparison,
    speakingDiff: UpgradeDiffComparison,
    aiDiffState: ReviewDiffAiUiState,
    ttsController: ApexTtsController,
    onGenerateAiDiff: (skillLabel: String, promptEn: String, originalText: String) -> Unit,
    onResetAiDiff: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSkillTab by remember(writingDiff.id) { mutableIntStateOf(0) } // 0 = Writing, 1 = Speaking
    var isStackedMode by remember { mutableStateOf(true) }
    var activeSinglePane by remember { mutableIntStateOf(0) } // 0 = Original, 1 = Upgraded

    val baseComparison = if (selectedSkillTab == 0) writingDiff else speakingDiff
    val displayedComparison = when (aiDiffState) {
        is ReviewDiffAiUiState.Success -> {
            if (aiDiffState.comparison.originalText == baseComparison.originalText) {
                aiDiffState.comparison
            } else {
                baseComparison
            }
        }
        else -> baseComparison
    }

    var selectedChangeId by remember(displayedComparison.id) {
        mutableStateOf(displayedComparison.changes.firstOrNull()?.id)
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, CardBorderSoft),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("review_diff_view_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.CompareArrows, contentDescription = null, tint = ElectricIndigo, modifier = Modifier.size(20.dp))
                    Text(
                        text = stringResource(R.string.review_diff_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = InkPrimary
                    )
                }
            }

            // Writing vs Speaking Switcher + Stacked/Tabbed Layout Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Writing", "Speaking").forEachIndexed { idx, label ->
                        val active = selectedSkillTab == idx
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (active) ElectricIndigo else SurfaceCanvas,
                            border = BorderStroke(1.dp, if (active) ElectricIndigo else CardBorderSoft),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    selectedSkillTab = idx
                                    onResetAiDiff()
                                }
                                .testTag("review_diff_skill_$idx")
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (active) Color.White else InkSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = SurfaceCanvas,
                    border = BorderStroke(1.dp, CardBorderSoft),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { isStackedMode = !isStackedMode }
                        .testTag("review_diff_layout_toggle")
                ) {
                    Text(
                        text = if (isStackedMode) "Dạng xếp chồng" else "Dạng chuyển thẻ",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricIndigo,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Text(
                text = "Đề bài: ${displayedComparison.promptSummaryEn}",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = PlusJakartaSansFontFamily),
                color = InkMuted
            )

            // Tabbed Toggle Strip when not in Stacked Mode
            if (!isStackedMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (activeSinglePane == 0) RoseDiffLight else SurfaceCanvas,
                        border = BorderStroke(1.dp, if (activeSinglePane == 0) RoseDiff else CardBorderSoft),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { activeSinglePane = 0 }
                    ) {
                        Text(
                            text = "Bài làm gốc",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (activeSinglePane == 0) RoseDiff else InkSecondary,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (activeSinglePane == 1) EmeraldSuccessLight else SurfaceCanvas,
                        border = BorderStroke(1.dp, if (activeSinglePane == 1) EmeraldSuccess else CardBorderSoft),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { activeSinglePane = 1 }
                    ) {
                        Text(
                            text = "Bản Band 8.5+",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (activeSinglePane == 1) EmeraldOnContainer else InkSecondary,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)
                        )
                    }
                }
            }

            // Original Response Box (Weak phrases underlined in Rose #E11D48)
            if (isStackedMode || activeSinglePane == 0) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = RoseDiffLight.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, RoseDiff.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("review_diff_original_box")
                ) {
                    Column(
                        modifier = Modifier.padding(13.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.review_diff_original_label),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = RoseDiff
                        )
                        Text(
                            text = buildHighlightedAnnotatedString(
                                fullText = displayedComparison.originalText,
                                targets = displayedComparison.changes.map { it.weakPhraseEn },
                                isUpgrade = false
                            ),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                lineHeight = 22.sp
                            ),
                            color = InkPrimary
                        )
                    }
                }
            }

            // Band 8.5+ Upgraded Version Box (Upgrades highlighted in Emerald #10B981)
            if (isStackedMode || activeSinglePane == 1) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = EmeraldSuccessLight.copy(alpha = 0.38f),
                    border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("review_diff_upgraded_box")
                ) {
                    Column(
                        modifier = Modifier.padding(13.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.review_diff_upgraded_label),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldOnContainer,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = CardWhite,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        ttsController.speak(displayedComparison.upgradedText, EnglishAccent.UK)
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Nghe bản Band 8.5+", tint = EmeraldOnContainer, modifier = Modifier.size(14.dp))
                                    Text("Nghe UK", style = MaterialTheme.typography.labelSmall, color = EmeraldOnContainer)
                                }
                            }
                        }

                        Text(
                            text = buildHighlightedAnnotatedString(
                                fullText = displayedComparison.upgradedText,
                                targets = displayedComparison.changes.map { it.upgradedPhraseEn },
                                isUpgrade = true
                            ),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                lineHeight = 22.sp
                            ),
                            color = InkPrimary
                        )
                    }
                }
            }

            // Tap-to-Read Vietnamese Explanations for Each Upgrade
            if (displayedComparison.changes.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(Icons.Rounded.TouchApp, contentDescription = null, tint = ElectricIndigo, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Chạm từng điểm nâng cấp để xem giải thích tiếng Việt:",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricIndigo
                        )
                    }

                    displayedComparison.changes.forEachIndexed { idx, change ->
                        val isExpanded = selectedChangeId == change.id
                        DiffChangeExplanationItem(
                            index = idx + 1,
                            change = change,
                            isExpanded = isExpanded,
                            onClick = {
                                selectedChangeId = if (isExpanded) null else change.id
                            }
                        )
                    }
                }
            }

            // On-Demand Gemini AI Rewrite CTA (works for both live submitted exams & re-upgrading samples)
            if (aiDiffState is ReviewDiffAiUiState.Error) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = RoseDiffLight,
                    border = BorderStroke(1.dp, RoseAlert.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = RoseDiff, modifier = Modifier.size(18.dp))
                        Text(aiDiffState.messageVi, style = MaterialTheme.typography.bodySmall, color = InkPrimary)
                    }
                }
            }

            Button(
                onClick = {
                    onGenerateAiDiff(
                        baseComparison.skillLabel,
                        baseComparison.promptSummaryEn,
                        baseComparison.originalText
                    )
                },
                enabled = aiDiffState !is ReviewDiffAiUiState.Loading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricIndigo,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("review_generate_ai_diff_btn")
            ) {
                if (aiDiffState is ReviewDiffAiUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gemini AI đang nâng cấp theo dàn ý của bạn...")
                } else {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.review_diff_ai_btn),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun DiffChangeExplanationItem(
    index: Int,
    change: UpgradeDiffChange,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isExpanded) SurfaceCanvas else CardWhite,
        border = BorderStroke(
            1.dp,
            if (isExpanded) ElectricIndigo.copy(alpha = 0.45f) else CardBorderSoft
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("diff_change_item_$index")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ElectricIndigoLight,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = "#$index • ${change.categoryVi}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricIndigo,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(0.01f))
                Text(
                    text = if (isExpanded) "Ẩn giải thích" else "Xem giải thích",
                    style = MaterialTheme.typography.labelSmall,
                    color = InkMuted,
                    maxLines = 1
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = change.weakPhraseEn,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        textDecoration = TextDecoration.Underline
                    ),
                    color = RoseDiff
                )
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = EmeraldSuccess,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(14.dp)
                    )
                    Text(
                        text = change.upgradedPhraseEn,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = EmeraldOnContainer
                    )
                }
            }

            if (isExpanded && change.explanationVi.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CardWhite,
                    border = BorderStroke(1.dp, CardBorderSoft)
                ) {
                    Text(
                        text = change.explanationVi,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkPrimary,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun buildHighlightedAnnotatedString(
    fullText: String,
    targets: List<String>,
    isUpgrade: Boolean
): AnnotatedString {
    val upgradeFg = EmeraldOnContainer
    val upgradeBg = EmeraldSuccessLight
    val weakBg = RoseDiffLight
    return buildAnnotatedString {
        append(fullText)
        val lowerFull = fullText.lowercase()
        targets.forEach { target ->
            if (target.isBlank()) return@forEach
            val idx = lowerFull.indexOf(target.lowercase())
            if (idx >= 0) {
                val style = if (isUpgrade) {
                    SpanStyle(
                        color = upgradeFg,
                        background = upgradeBg,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    SpanStyle(
                        color = RoseDiff,
                        background = weakBg,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                addStyle(style, start = idx, end = (idx + target.length).coerceAtMost(fullText.length))
            }
        }
    }
}
