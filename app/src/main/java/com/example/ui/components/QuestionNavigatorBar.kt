package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.OutlinedFlag
import androidx.compose.material.icons.rounded.UnfoldLess
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardWhite
import com.example.ui.theme.ElectricIndigo
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
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseDiffLight
import com.example.ui.theme.SurfaceCanvas

/**
 * Lightweight descriptor for a question rendered inside [QuestionNavigatorBar] and [QuestionOverviewGridSheet].
 */
data class QuestionStatusItem(
    val id: String,
    val number: Int,
    val previewText: String,
    val answerSummary: String? = null,
    val isFlagged: Boolean = false,
    val isCorrect: Boolean? = null
) {
    val isAnswered: Boolean get() = !answerSummary.isNullOrBlank()
}

data class QuestionNavigatorPalette(
    val barBg: Color,
    val cardBg: Color,
    val neutralChipBg: Color,
    val borderColor: Color,
    val primaryAccent: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val answeredBg: Color = EmeraldSuccess,
    val answeredText: Color = Color.White,
    val flagAccent: Color = Color(0xFFF59E0B),
    val flagSoftBg: Color = Color(0xFFFEF3C7),
    val flagSoftText: Color = Color(0xFF92400E),
    val wrongBg: Color = RoseAlert
)

@Composable
fun defaultPracticeNavigatorPalette(
    primaryAccent: Color = PracticeTeal
): QuestionNavigatorPalette = QuestionNavigatorPalette(
    barBg = CardWhite,
    cardBg = CardWhite,
    neutralChipBg = SurfaceCanvas,
    borderColor = CardBorderSoft,
    primaryAccent = primaryAccent,
    primaryText = InkPrimary,
    secondaryText = InkSecondary,
    flagSoftBg = GoldMasteryLight,
    flagSoftText = GoldOnContainer
)

private enum class OverviewGridFilter(val labelVi: String) {
    ALL("Tất cả"),
    UNANSWERED("Chưa làm"),
    FLAGGED("Đánh dấu")
}

/**
 * Sticky, compact live Question Status Bar (`LazyRow` synced with the active question index)
 * plus a 1-tap "Bảng câu" (Grid) button opening [QuestionOverviewGridSheet].
 */
@Composable
fun QuestionNavigatorBar(
    items: List<QuestionStatusItem>,
    activeIndex: Int,
    onSelectQuestion: (Int) -> Unit,
    modifier: Modifier = Modifier,
    palette: QuestionNavigatorPalette = defaultPracticeNavigatorPalette(),
    expandAll: Boolean? = null,
    onToggleExpandAll: (() -> Unit)? = null,
    showNavBarInset: Boolean = false,
    testTagPrefix: String = "question_nav"
) {
    if (items.isEmpty()) return

    val rowListState = rememberLazyListState()
    var showGridSheet by remember { mutableStateOf(false) }

    val answeredCount = remember(items) { items.count { it.isAnswered } }
    val unansweredCount = items.size - answeredCount
    val flaggedCount = remember(items) { items.count { it.isFlagged } }

    LaunchedEffect(activeIndex, items.size) {
        if (activeIndex in items.indices) {
            rowListState.animateScrollToItem(
                index = (activeIndex - 1).coerceAtLeast(0)
            )
        }
    }

    Surface(
        color = palette.barBg,
        border = BorderStroke(1.dp, palette.borderColor),
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .then(if (showNavBarInset) Modifier.navigationBarsPadding() else Modifier)
            .testTag("${testTagPrefix}_bar")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Live counter row + Expand All toggle + "Bảng câu" Grid trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = palette.answeredBg.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "Đã làm: $answeredCount/${items.size} • Chưa làm: $unansweredCount",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = palette.primaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    if (flaggedCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = palette.flagAccent.copy(alpha = 0.18f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Flag,
                                    contentDescription = null,
                                    tint = palette.flagAccent,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = "$flaggedCount",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp
                                    ),
                                    color = palette.flagAccent,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (expandAll != null && onToggleExpandAll != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = palette.neutralChipBg,
                            border = BorderStroke(1.dp, palette.borderColor),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(onClick = onToggleExpandAll)
                                .testTag("${testTagPrefix}_expand_all_toggle")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = if (expandAll) Icons.Rounded.UnfoldLess else Icons.Rounded.UnfoldMore,
                                    contentDescription = if (expandAll) "Thu gọn" else "Mở tất cả",
                                    tint = palette.primaryAccent,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (expandAll) "Thu gọn" else "Mở hết",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = palette.primaryAccent,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    // "Bảng câu" (Grid) Overview Sheet trigger
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = palette.primaryAccent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showGridSheet = true }
                            .testTag("${testTagPrefix}_grid_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.GridView,
                                contentDescription = "Mở bảng câu hỏi",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Bảng câu",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // Horizontal numbered pills synced with LazyListState
            LazyRow(
                state = rowListState,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                contentPadding = PaddingValues(vertical = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("${testTagPrefix}_pills_row")
            ) {
                itemsIndexed(items, key = { _, it -> it.id }) { idx, item ->
                    QuestionNumberPill(
                        item = item,
                        isCurrent = idx == activeIndex,
                        palette = palette,
                        onClick = { onSelectQuestion(idx) },
                        modifier = Modifier.testTag("${testTagPrefix}_pill_${item.number}")
                    )
                }
            }
        }
    }

    if (showGridSheet) {
        QuestionOverviewGridSheet(
            items = items,
            activeIndex = activeIndex,
            palette = palette,
            onSelectQuestion = { idx ->
                showGridSheet = false
                onSelectQuestion(idx)
            },
            onDismiss = { showGridSheet = false },
            testTagPrefix = testTagPrefix
        )
    }
}

@Composable
private fun QuestionNumberPill(
    item: QuestionStatusItem,
    isCurrent: Boolean,
    palette: QuestionNavigatorPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            item.isCorrect == true -> palette.answeredBg
            item.isCorrect == false -> palette.wrongBg
            item.isAnswered -> palette.answeredBg
            else -> palette.neutralChipBg
        },
        animationSpec = tween(160),
        label = "q_pill_bg_${item.number}"
    )
    val textColor = when {
        item.isCorrect != null || item.isAnswered -> palette.answeredText
        isCurrent -> palette.primaryAccent
        else -> palette.primaryText
    }
    val borderColor = when {
        isCurrent -> palette.primaryAccent
        item.isFlagged -> palette.flagAccent
        item.isAnswered || item.isCorrect != null -> Color.Transparent
        else -> palette.borderColor
    }

    Box(
        modifier = modifier
            .size(38.dp)
            .then(
                if (isCurrent) {
                    Modifier.shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(11.dp),
                        ambientColor = palette.primaryAccent.copy(alpha = 0.35f),
                        spotColor = palette.primaryAccent.copy(alpha = 0.45f)
                    )
                } else {
                    Modifier
                }
            )
            .clip(RoundedCornerShape(11.dp))
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(11.dp),
            color = Color.Transparent,
            border = BorderStroke(
                width = if (isCurrent) 2.2.dp else if (item.isFlagged) 1.5.dp else 1.dp,
                color = borderColor
            ),
            modifier = Modifier.matchParentSize()
        ) {}

        Text(
            text = "${item.number}",
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = PlusJakartaSansFontFamily,
                fontWeight = if (isCurrent || item.isAnswered) FontWeight.ExtraBold else FontWeight.SemiBold,
                fontSize = 13.sp
            ),
            color = textColor,
            maxLines = 1,
            softWrap = false
        )

        if (item.isFlagged) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(palette.flagAccent)
            )
        }
    }
}

/**
 * Compact Bottom Sheet Overview Grid showing all questions at a glance with filter chips
 * (`Tất cả`, `Chưa làm`, `Đánh dấu`) so learners never leave a question blank.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionOverviewGridSheet(
    items: List<QuestionStatusItem>,
    activeIndex: Int,
    palette: QuestionNavigatorPalette,
    onSelectQuestion: (Int) -> Unit,
    onDismiss: () -> Unit,
    testTagPrefix: String = "question_nav"
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedFilter by rememberSaveable { mutableStateOf(OverviewGridFilter.ALL) }

    val answeredCount = remember(items) { items.count { it.isAnswered } }
    val unansweredCount = items.size - answeredCount
    val flaggedCount = remember(items) { items.count { it.isFlagged } }

    val indexedFiltered = remember(items, selectedFilter) {
        items.mapIndexed { idx, item -> idx to item }.filter { (_, item) ->
            when (selectedFilter) {
                OverviewGridFilter.ALL -> true
                OverviewGridFilter.UNANSWERED -> !item.isAnswered
                OverviewGridFilter.FLAGGED -> item.isFlagged
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.cardBg,
        modifier = Modifier.testTag("${testTagPrefix}_overview_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Bảng câu hỏi (${items.size} câu)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = palette.primaryText
                    )
                    Text(
                        text = "Đã làm: $answeredCount • Chưa làm: $unansweredCount • Đánh dấu: $flaggedCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.secondaryText
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = palette.neutralChipBg,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onDismiss)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Đóng bảng câu hỏi",
                            tint = palette.primaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Filter Chips: Tất cả | Chưa làm | Đánh dấu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OverviewGridFilter.entries.forEach { filter ->
                    val count = when (filter) {
                        OverviewGridFilter.ALL -> items.size
                        OverviewGridFilter.UNANSWERED -> unansweredCount
                        OverviewGridFilter.FLAGGED -> flaggedCount
                    }
                    val selected = selectedFilter == filter
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (selected) palette.primaryAccent else palette.neutralChipBg,
                        border = BorderStroke(
                            1.dp,
                            if (selected) palette.primaryAccent else palette.borderColor
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { selectedFilter = filter }
                            .testTag("${testTagPrefix}_grid_filter_${filter.name.lowercase()}")
                    ) {
                        Text(
                            text = "${filter.labelVi} ($count)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (selected) Color.White else palette.primaryText,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // Legend row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendSwatch(palette.answeredBg, "Đã làm", palette.secondaryText)
                LegendSwatch(palette.neutralChipBg, "Chưa làm", palette.secondaryText, palette.borderColor)
                LegendSwatch(palette.flagAccent, "Xem lại", palette.secondaryText)
            }

            if (indexedFiltered.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = palette.neutralChipBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = palette.answeredBg,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = when (selectedFilter) {
                                OverviewGridFilter.UNANSWERED -> "Tuyệt vời! Bạn đã trả lời tất cả các câu hỏi."
                                OverviewGridFilter.FLAGGED -> "Chưa có câu hỏi nào được đánh dấu xem lại."
                                OverviewGridFilter.ALL -> "Không có câu hỏi nào."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.primaryText
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 54.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                ) {
                    itemsIndexed(indexedFiltered, key = { _, pair -> pair.second.id }) { _, (origIdx, item) ->
                        val isCurrent = origIdx == activeIndex
                        val bg = when {
                            item.isCorrect == true -> palette.answeredBg
                            item.isCorrect == false -> palette.wrongBg
                            item.isAnswered -> palette.answeredBg
                            else -> palette.neutralChipBg
                        }
                        val fg = if (item.isAnswered || item.isCorrect != null) palette.answeredText else palette.primaryText

                        Box(
                            modifier = Modifier
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bg)
                                .clickable { onSelectQuestion(origIdx) }
                                .testTag("${testTagPrefix}_grid_cell_${item.number}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Transparent,
                                border = BorderStroke(
                                    width = if (isCurrent) 2.2.dp else if (item.isFlagged) 1.5.dp else 1.dp,
                                    color = when {
                                        isCurrent -> palette.primaryAccent
                                        item.isFlagged -> palette.flagAccent
                                        item.isAnswered || item.isCorrect != null -> Color.Transparent
                                        else -> palette.borderColor
                                    }
                                ),
                                modifier = Modifier.matchParentSize()
                            ) {}

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${item.number}",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = fg,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            if (item.isFlagged) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(palette.flagAccent)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendSwatch(
    fill: Color,
    label: String,
    textColor: Color,
    border: Color? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = fill,
            border = border?.let { BorderStroke(1.dp, it) },
            modifier = Modifier.size(10.dp)
        ) {}
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            maxLines = 1,
            softWrap = false
        )
    }
}

/**
 * Smart Collapsible Question Accordion Card:
 * - Collapsed: Compact single-line header row (`Câu N` + 1-line truncated preview + clear status pill).
 * - Expanded: Smoothly reveals full prompt, inputs/options, Flag ("Đánh dấu xem lại") toggle & actions.
 */
@Composable
fun CollapsibleQuestionAccordionCard(
    questionNumber: Int,
    previewText: String,
    answerSummary: String?,
    isExpanded: Boolean,
    isFlagged: Boolean,
    onToggleExpand: () -> Unit,
    onToggleFlag: (() -> Unit)? = null,
    isCorrect: Boolean? = null,
    badgeLabel: String? = null,
    accentColor: Color = PracticeTeal,
    cardBg: Color = CardWhite,
    borderColor: Color = CardBorderSoft,
    primaryText: Color = InkPrimary,
    secondaryText: Color = InkSecondary,
    neutralSurface: Color = SurfaceCanvas,
    headerTrailingAction: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isAnswered = !answerSummary.isNullOrBlank()
    var wrongShakeCount by remember { mutableIntStateOf(0) }
    var previousCorrectState by remember { mutableStateOf(isCorrect) }
    LaunchedEffect(isCorrect) {
        if (isCorrect == false && previousCorrectState != false) {
            wrongShakeCount++
        }
        previousCorrectState = isCorrect
    }

    val neutralBorder = when {
        isExpanded -> accentColor
        isFlagged -> Color(0xFFF59E0B)
        else -> borderColor
    }
    val resolvedBorderColor by rememberSemanticAnswerBorderColor(
        isCorrect = isCorrect,
        defaultColor = neutralBorder
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = BorderStroke(
            width = if (isExpanded || isCorrect != null) 1.75.dp else 1.dp,
            color = resolvedBorderColor
        ),
        shadowElevation = if (isCorrect == true) 4.dp else 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .answerFeedbackShake(triggerKey = wrongShakeCount)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Single-line compact clickable header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
                    .minimumInteractiveComponentSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Question Number Chip
                Surface(
                    shape = RoundedCornerShape(9.dp),
                    color = when {
                        isExpanded -> accentColor
                        isCorrect == true -> EmeraldSuccess
                        isCorrect == false -> RoseAlert
                        isAnswered -> EmeraldSuccessLight
                        else -> neutralSurface
                    }
                ) {
                    Text(
                        text = "Câu $questionNumber",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = when {
                            isExpanded || isCorrect != null -> Color.White
                            isAnswered -> EmeraldOnContainer
                            else -> primaryText
                        },
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // 1-line truncated preview when collapsed (or badge when expanded)
                if (!isExpanded) {
                    Text(
                        text = previewText,
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!badgeLabel.isNullOrBlank()) {
                            Text(
                                text = badgeLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = secondaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Status Pill (Collapsed shows compact status; Expanded shows Flag + optional action)
                if (!isExpanded) {
                    QuestionCompactStatusPill(
                        answerSummary = answerSummary,
                        isFlagged = isFlagged,
                        isCorrect = isCorrect,
                        neutralBg = neutralSurface
                    )
                } else {
                    if (onToggleFlag != null) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isFlagged) GoldMasteryLight else neutralSurface,
                            border = BorderStroke(
                                1.dp,
                                if (isFlagged) Color(0xFFF59E0B) else borderColor
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onToggleFlag)
                                .testTag("question_flag_toggle_$questionNumber")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFlagged) Icons.Rounded.Flag else Icons.Rounded.OutlinedFlag,
                                    contentDescription = "Đánh dấu xem lại",
                                    tint = if (isFlagged) Color(0xFFD97706) else secondaryText,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isFlagged) "Xem lại" else "Đánh dấu",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp
                                    ),
                                    color = if (isFlagged) GoldOnContainer else secondaryText,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                    headerTrailingAction?.invoke()
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (isExpanded) "Thu gọn câu $questionNumber" else "Mở câu $questionNumber",
                    tint = secondaryText,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Expanded question body
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(200)) + fadeIn(tween(180)),
                exit = shrinkVertically(tween(180)) + fadeOut(tween(140))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun QuestionCompactStatusPill(
    answerSummary: String?,
    isFlagged: Boolean,
    isCorrect: Boolean?,
    neutralBg: Color
) {
    val isAnswered = !answerSummary.isNullOrBlank()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (isFlagged) {
            Surface(
                shape = RoundedCornerShape(50),
                color = GoldMasteryLight
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Flag,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "Xem lại",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = GoldOnContainer,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        val (pillBg, pillFg, pillText) = when {
            isCorrect == true -> Triple(EmeraldSuccessLight, EmeraldOnContainer, "Đúng ✓")
            isCorrect == false -> Triple(RoseDiffLight, RoseAlert, "Sai")
            isAnswered -> {
                val shortAns = answerSummary!!.trim().let {
                    if (it.length > 10) "${it.take(9)}…" else it
                }
                Triple(EmeraldSuccessLight, EmeraldOnContainer, "Đã làm: $shortAns")
            }
            !isFlagged -> Triple(neutralBg, InkMuted, "Chưa làm")
            else -> Triple(Color.Transparent, Color.Transparent, "")
        }

        if (pillText.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(50),
                color = pillBg
            ) {
                Text(
                    text = pillText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = pillFg,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
        }
    }
}

/**
 * Compact `?` icon button that hides optional usage instructions behind a lightweight dialog
 * so primary learning viewports stay spacious and clutter-free.
 */
@Composable
fun CompactHelpTipButton(
    title: String,
    tips: List<String>,
    tint: Color = Color.White,
    containerColor: Color = Color.White.copy(alpha = 0.18f),
    testTagName: String = "compact_help_tip_button"
) {
    var open by remember { mutableStateOf(false) }

    Surface(
        shape = CircleShape,
        color = containerColor,
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .clickable { open = true }
            .testTag(testTagName)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                contentDescription = "Hướng dẫn nhanh",
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
    }

    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            containerColor = CardWhite,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = InkPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tips.forEach { tip ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("•", color = ElectricIndigo, fontWeight = FontWeight.Bold)
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSecondary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { open = false }) {
                    Text("Đã hiểu", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
