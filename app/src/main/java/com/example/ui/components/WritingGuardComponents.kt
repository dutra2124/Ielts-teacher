package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardWhite
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
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
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.TealOnSurface

data class IeltsWritingCriterionItem(
    val code: String,
    val titleVi: String,
    val descVi: String
)

private val Task1Criteria = listOf(
    IeltsWritingCriterionItem(
        code = "TA",
        titleVi = "Task Achievement (Đủ dữ liệu)",
        descVi = "Có đoạn Overview nêu xu hướng nổi bật nhất, chọn lọc số liệu tiêu biểu & so sánh chính xác (≥ 150 từ)."
    ),
    IeltsWritingCriterionItem(
        code = "CC",
        titleVi = "Coherence & Cohesion (Mạch lạc)",
        descVi = "Nhóm số liệu logic theo 2 đoạn Body, dùng từ nối so sánh tự nhiên (whereas, by contrast, subsequently)."
    ),
    IeltsWritingCriterionItem(
        code = "LR",
        titleVi = "Lexical Resource (Từ vựng mô tả)",
        descVi = "Paraphrase đề bài, dùng động từ/trạng từ biến thiên chuẩn xác (surged markedly, plateaued, marginal uptick)."
    ),
    IeltsWritingCriterionItem(
        code = "GRA",
        titleVi = "Grammatical Range & Accuracy",
        descVi = "Kết hợp câu phức, mệnh đề phân từ, kiểm tra đúng thì quá khứ/tương lai và sự hòa hợp chủ–vị."
    )
)

private val Task2Criteria = listOf(
    IeltsWritingCriterionItem(
        code = "TR",
        titleVi = "Task Response (Trả lời đúng trọng tâm)",
        descVi = "Giải quyết đầy đủ mọi vế câu hỏi, quan điểm nhất quán từ Mở đến Kết bài & có ví dụ cụ thể (≥ 250 từ)."
    ),
    IeltsWritingCriterionItem(
        code = "CC",
        titleVi = "Coherence & Cohesion (Liên kết đoạn)",
        descVi = "Mỗi đoạn Body có 1 Topic Sentence rõ ràng, lập luận theo chuỗi Nguyên nhân → Hệ quả, không lạm dụng liên từ."
    ),
    IeltsWritingCriterionItem(
        code = "LR",
        titleVi = "Lexical Resource (Từ vựng C1–C2)",
        descVi = "Paraphrase từ khóa chủ đề, sử dụng collocations học thuật chuẩn sắc thái & tránh lặp từ đơn điệu."
    ),
    IeltsWritingCriterionItem(
        code = "GRA",
        titleVi = "Grammatical Range & Accuracy",
        descVi = "Đa dạng cấu trúc (nhượng bộ While, đảo ngữ, mệnh đề quan hệ), soát lỗi mạo từ, giới từ & chính tả."
    )
)

/**
 * Live Word-Count Guard pill (>= 150 Task 1 / >= 250 Task 2 turns Emerald Green)
 * paired with Auto-Draft save timestamp and optional clear-draft button.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WritingWordCountAndDraftBar(
    wordCount: Int,
    targetWords: Int,
    lastSavedTimeLabel: String?,
    restoredFromDraft: Boolean,
    onClearDraft: (() -> Unit)? = null,
    isDarkOrHighContrast: Boolean = false,
    darkPrimaryText: Color = InkPrimary,
    darkSecondaryText: Color = InkSecondary,
    modifier: Modifier = Modifier
) {
    val reachedTarget = wordCount >= targetWords
    val remaining = (targetWords - wordCount).coerceAtLeast(0)

    val pillBg = when {
        reachedTarget && isDarkOrHighContrast -> Color(0xFF0E3A2F)
        reachedTarget -> EmeraldSuccessLight
        wordCount > 0 && isDarkOrHighContrast -> Color(0xFF2D2512)
        wordCount > 0 -> GoldMasteryLight
        isDarkOrHighContrast -> Color(0xFF1F2937)
        else -> PracticeTealLight
    }
    val pillBorder = when {
        reachedTarget -> EmeraldSuccess
        wordCount > 0 -> GoldMastery.copy(alpha = 0.7f)
        else -> PracticeTeal.copy(alpha = 0.4f)
    }
    val pillFg = when {
        reachedTarget && isDarkOrHighContrast -> Color(0xFF6EE7B7)
        reachedTarget -> EmeraldOnContainer
        wordCount > 0 && isDarkOrHighContrast -> Color(0xFFFDE68A)
        wordCount > 0 -> GoldOnContainer
        isDarkOrHighContrast -> darkPrimaryText
        else -> TealOnSurface
    }

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = pillBg,
            border = BorderStroke(1.dp, pillBorder),
            modifier = Modifier
                .wrapContentWidth()
                .testTag("writing_word_count_guard_pill")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
            ) {
                if (reachedTarget) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = if (isDarkOrHighContrast) Color(0xFF6EE7B7) else EmeraldSuccess,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = if (reachedTarget) {
                        "$wordCount / $targetWords từ • Đạt chuẩn"
                    } else {
                        "$wordCount / $targetWords từ (thiếu $remaining)"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = pillFg,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        // Auto-Draft Protection Indicator Pill
        val draftChipBg = if (isDarkOrHighContrast) Color(0xFF1E293B) else SurfaceCanvas
        val draftChipBorder = if (isDarkOrHighContrast) Color(0xFF334155) else CardBorderSoft
        val draftChipFg = if (isDarkOrHighContrast) darkSecondaryText else InkMuted
        Surface(
            shape = RoundedCornerShape(50),
            color = draftChipBg,
            border = BorderStroke(1.dp, draftChipBorder),
            modifier = Modifier
                .wrapContentWidth()
                .testTag("writing_auto_draft_badge")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Save,
                    contentDescription = null,
                    tint = if (lastSavedTimeLabel != null) EmeraldSuccess else draftChipFg,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = when {
                        restoredFromDraft && lastSavedTimeLabel != null -> "Đã khôi phục ($lastSavedTimeLabel)"
                        lastSavedTimeLabel != null -> "Tự lưu $lastSavedTimeLabel"
                        else -> "Tự lưu mỗi 5s"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = draftChipFg,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        if (onClearDraft != null && wordCount > 0) {
            Surface(
                shape = RoundedCornerShape(50),
                color = draftChipBg,
                border = BorderStroke(1.dp, draftChipBorder),
                modifier = Modifier
                    .wrapContentWidth()
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onClearDraft)
                    .testTag("writing_clear_draft_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteSweep,
                        contentDescription = "Xóa nháp",
                        tint = draftChipFg,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Xóa nháp",
                        style = MaterialTheme.typography.labelSmall,
                        color = draftChipFg,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

/**
 * Collapsible 4-Criteria IELTS Writing Self-Checklist (TR/TA, CC, LR, GRA)
 * rendered strictly on solid high-contrast surfaces before AI submission or exam hand-in.
 */
@Composable
fun WritingCriteriaChecklistCard(
    isTask1: Boolean,
    resetKey: Any,
    cardBg: Color = CardWhite,
    borderColor: Color = CardBorderSoft,
    primaryText: Color = InkPrimary,
    secondaryText: Color = InkSecondary,
    accentColor: Color = PracticeTeal,
    modifier: Modifier = Modifier
) {
    val criteria = remember(isTask1) { if (isTask1) Task1Criteria else Task2Criteria }
    val checkedMap = remember(resetKey, isTask1) { mutableStateMapOf<String, Boolean>() }
    var expanded by remember(resetKey) { mutableStateOf(false) }

    val checkedCount = criteria.count { checkedMap[it.code] == true }
    val allChecked = checkedCount == criteria.size

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(1.dp, if (allChecked) EmeraldSuccess else borderColor),
        modifier = modifier
            .fillMaxWidth()
            .testTag("writing_4criteria_checklist_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .testTag("writing_checklist_toggle"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Checklist,
                        contentDescription = null,
                        tint = if (allChecked) EmeraldSuccess else accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Checklist 4 tiêu chí IELTS (${if (isTask1) "TA" else "TR"}, CC, LR, GRA)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = primaryText
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (allChecked) EmeraldSuccessLight else PracticeTealLight
                    ) {
                        Text(
                            text = "$checkedCount/4",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (allChecked) EmeraldOnContainer else PracticeTealDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = if (expanded) "Thu gọn checklist" else "Mở checklist",
                        tint = secondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    criteria.forEach { item ->
                        val isChecked = checkedMap[item.code] == true
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isChecked) EmeraldSuccessLight.copy(alpha = 0.35f) else Color.Transparent,
                            border = BorderStroke(
                                1.dp,
                                if (isChecked) EmeraldSuccess.copy(alpha = 0.65f) else borderColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { checkedMap[item.code] = !isChecked }
                                .testTag("writing_checklist_${item.code.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isChecked) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isChecked) EmeraldSuccess else secondaryText,
                                    modifier = Modifier
                                        .padding(top = 1.dp)
                                        .size(18.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "${item.code} • ${item.titleVi}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = primaryText
                                    )
                                    Text(
                                        text = item.descVi,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.5.sp,
                                            lineHeight = 16.sp
                                        ),
                                        color = secondaryText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
