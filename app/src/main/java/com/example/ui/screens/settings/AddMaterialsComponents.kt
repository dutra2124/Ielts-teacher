package com.example.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalLibrary
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.importer.MaterialImportCategory
import com.example.data.local.BulkImportPreview
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardSurfaceTinted
import com.example.ui.theme.CardWhite
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.GoldMastery
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LocalApexColors
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.PracticeTeal
import com.example.ui.theme.StreakOrange
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.ThemeOnPrimary
import com.example.ui.theme.ThemePrimary
import com.example.ui.theme.VividViolet

internal fun categoryIcon(category: MaterialImportCategory): ImageVector = when (category) {
    MaterialImportCategory.VOCABULARY -> Icons.Rounded.MenuBook
    MaterialImportCategory.SENTENCE_DRILLS -> Icons.Rounded.AutoFixHigh
    MaterialImportCategory.READING_EXAM -> Icons.Rounded.Article
    MaterialImportCategory.LISTENING_EXAM -> Icons.Rounded.Headphones
    MaterialImportCategory.WRITING_SPEAKING -> Icons.Rounded.RecordVoiceOver
    MaterialImportCategory.PERSONAL_SELF_STUDY -> Icons.Rounded.LocalLibrary
}

internal fun categoryAccentColor(category: MaterialImportCategory): Color = when (category) {
    MaterialImportCategory.VOCABULARY -> ElectricIndigo
    MaterialImportCategory.SENTENCE_DRILLS -> PracticeTeal
    MaterialImportCategory.READING_EXAM -> VividViolet
    MaterialImportCategory.LISTENING_EXAM -> StreakOrange
    MaterialImportCategory.WRITING_SPEAKING -> GoldMastery
    MaterialImportCategory.PERSONAL_SELF_STUDY -> EmeraldSuccess
}

/**
 * Selectable IELTS Material Category Card showing icon, bilingual title,
 * supported file format chips, and concise Vietnamese IELTS exam purpose description.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MaterialCategoryCard(
    category: MaterialImportCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val apexColors = LocalApexColors.current
    val primary = ThemePrimary
    val accent = if (apexColors.isHighContrast) primary else categoryAccentColor(category)
    val borderColor = if (selected) primary else CardBorderSoft

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) primary.copy(alpha = if (apexColors.isDark) 0.18f else 0.08f) else CardWhite,
        border = BorderStroke(if (selected || apexColors.isHighContrast) 2.dp else 1.dp, borderColor),
        shadowElevation = if (selected) 5.dp else 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("material_category_${category.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(accent.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon(category),
                        contentDescription = category.titleVi,
                        tint = accent,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.titleVi,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = InkPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = category.subtitleEn,
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (selected) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Đã chọn",
                        tint = primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Concise Vietnamese description explaining which IELTS exam section it serves
            Text(
                text = category.ieltsPurposeVi,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = InkSecondary
            )

            // Supported File Format Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                category.supportedFormats.forEach { fmt ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) primary.copy(alpha = 0.14f) else SurfaceCanvas,
                        border = BorderStroke(1.dp, if (selected) primary.copy(alpha = 0.35f) else CardBorderSoft)
                    ) {
                        Text(
                            text = fmt,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = if (selected) primary else InkSecondary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Live Preview panel rendered before saving into Room SQLite so the user can verify
 * extracted records from either Direct Save or Gemini AI Structured Extraction.
 */
@Composable
internal fun LiveImportPreviewCard(
    preview: BulkImportPreview,
    category: MaterialImportCategory,
    onConfirmSave: () -> Unit,
    onClearPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = ThemePrimary
    val onPrimary = ThemeOnPrimary
    val highContrast = LocalApexColors.current.isHighContrast

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = CardWhite,
        border = BorderStroke(2.dp, EmeraldSuccess.copy(alpha = 0.65f)),
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_import_preview_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(EmeraldSuccessLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Xem trước dữ liệu (${preview.validRowCount} mục sẵn sàng)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = InkPrimary
                        )
                        Text(
                            text = "${category.titleVi} • ${preview.formatLabel} • ${preview.sourceFileName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldOnContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceCanvas,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onClearPreview)
                ) {
                    Text(
                        text = "Ẩn",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = InkMuted,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }

            if (!preview.warningVi.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CardSurfaceTinted,
                    border = BorderStroke(1.dp, primary.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = preview.warningVi,
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                }
            }

            // Horizontally scrollable structured preview table
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = CardSurfaceTinted,
                border = BorderStroke(1.dp, CardBorderSoft)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        preview.headers.forEachIndexed { idx, header ->
                            Text(
                                text = header,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = primary,
                                modifier = Modifier.width(if (idx == 0) 86.dp else 138.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    HorizontalDivider(color = CardBorderSoft)
                    preview.previewRows.take(12).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEachIndexed { idx, cell ->
                                Text(
                                    text = cell,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkPrimary,
                                    modifier = Modifier.width(if (idx == 0) 86.dp else 138.dp),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        HorizontalDivider(color = CardBorderSoft.copy(alpha = 0.45f))
                    }
                    if (preview.previewRows.size > 12) {
                        Text(
                            text = "... và ${preview.previewRows.size - 12} dòng khác sẽ được lưu vào SQLite Offline.",
                            style = MaterialTheme.typography.labelSmall,
                            color = InkMuted
                        )
                    }
                }
            }

            Button(
                onClick = onConfirmSave,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primary,
                    contentColor = onPrimary
                ),
                border = if (highContrast) BorderStroke(1.5.dp, onPrimary.copy(alpha = 0.5f)) else null,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("confirm_save_material_offline_btn")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Save,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Xác nhận lưu ${preview.validRowCount} mục vào kho Offline",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
            }
        }
    }
}
