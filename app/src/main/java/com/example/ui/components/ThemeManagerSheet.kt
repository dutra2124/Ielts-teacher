package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoMode
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Monitor
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.AppThemePreferences
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardSurfaceTinted
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CdIeltsDarkApexColors
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.IndigoOnSurface
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LightApexColors
import com.example.ui.theme.NightStudyApexColors
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.SurfaceCanvas

/**
 * Compact circular header trigger button for opening the Global Theme Manager.
 */
@Composable
fun ThemeQuickToggleButton(
    mode: AppThemeMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when (mode) {
        AppThemeMode.LIGHT -> Icons.Rounded.LightMode
        AppThemeMode.NIGHT_STUDY -> Icons.Rounded.NightsStay
        AppThemeMode.CD_IELTS_DARK -> Icons.Rounded.Monitor
        AppThemeMode.SYSTEM -> Icons.Rounded.AutoMode
    }
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.18f))
            .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape)
            .clickable(onClick = onClick)
            .testTag("theme_manager_button"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(R.string.theme_manager_cd),
            tint = Color.White,
            modifier = Modifier.size(21.dp)
        )
    }
}

/**
 * Global Theme Manager Modal Bottom Sheet supporting:
 * - Light (Daytime academic study)
 * - Late-Night Study Dark Mode (Warm deep navy, low blue-light glare)
 * - CD-IELTS Exam Dark Mode (Neutral charcoal matte surface for 60-minute timed reading/writing)
 * - Follow Android System
 * - Auto-enable Exam Dark Mode inside fullscreen CD-IELTS Mock Exam Room
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeManagerSheet(
    prefs: AppThemePreferences,
    onSelectMode: (AppThemeMode) -> Unit,
    onAutoDarkExamChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardWhite,
        contentColor = InkPrimary,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("theme_manager_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 18.dp)
                .padding(bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Sheet Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardSurfaceTinted),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DarkMode,
                        contentDescription = null,
                        tint = IndigoOnSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.theme_sheet_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = InkPrimary
                    )
                    Text(
                        text = stringResource(R.string.theme_sheet_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )
                }
            }

            // 4 Theme Mode Selector Cards with Swatch Previews
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppThemeMode.entries.forEach { mode ->
                    val selected = prefs.mode == mode
                    ThemeModeOptionCard(
                        mode = mode,
                        selected = selected,
                        onClick = { onSelectMode(mode) }
                    )
                }
            }

            // Dedicated Exam Room Auto-Dark Toggle
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = SurfaceCanvas,
                border = BorderStroke(1.dp, CardBorderSoft),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bedtime,
                        contentDescription = null,
                        tint = IndigoOnSurface,
                        modifier = Modifier.size(22.dp)
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.theme_auto_exam_title),
                            style = MaterialTheme.typography.labelLarge,
                            color = InkPrimary
                        )
                        Text(
                            text = stringResource(R.string.theme_auto_exam_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                    Switch(
                        checked = prefs.autoDarkInExamRoom,
                        onCheckedChange = onAutoDarkExamChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ElectricIndigo
                        ),
                        modifier = Modifier.testTag("theme_auto_exam_switch")
                    )
                }
            }

            // Live Contrast Preview Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CardSurfaceTinted,
                border = BorderStroke(1.dp, CardBorderSoft),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.theme_preview_card_title),
                            style = MaterialTheme.typography.labelSmall,
                            color = InkMuted
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = EmeraldSuccessLight
                        ) {
                            Text(
                                text = "Band 8.5 • ${prefs.mode.shortBadgeVi}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldOnContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Text(
                        text = "Mitigate cognitive fatigue during prolonged reading comprehension.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = InkPrimary
                    )
                    Text(
                        text = "Giảm mỏi mắt tối đa khi đọc đoạn văn IELTS dài 900 từ và luyện thi buổi tối.",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )
                }
            }

            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricIndigo,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(vertical = 13.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("theme_sheet_done_btn")
            ) {
                Text(
                    text = stringResource(R.string.theme_done_btn),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun ThemeModeOptionCard(
    mode: AppThemeMode,
    selected: Boolean,
    onClick: () -> Unit
) {
    val (swatchCanvas, swatchCard, swatchAccent) = when (mode) {
        AppThemeMode.LIGHT -> Triple(LightApexColors.surfaceCanvas, LightApexColors.cardWhite, ElectricIndigo)
        AppThemeMode.NIGHT_STUDY -> Triple(NightStudyApexColors.surfaceCanvas, NightStudyApexColors.cardWhite, Color(0xFF818CF8))
        AppThemeMode.CD_IELTS_DARK -> Triple(CdIeltsDarkApexColors.surfaceCanvas, CdIeltsDarkApexColors.cardWhite, Color(0xFF60A5FA))
        AppThemeMode.SYSTEM -> Triple(Color(0xFF334155), Color(0xFFE2E8F0), EmeraldSuccess)
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (selected) CardSurfaceTinted else SurfaceCanvas,
        border = BorderStroke(
            width = if (selected) 1.75.dp else 1.dp,
            color = if (selected) IndigoOnSurface else CardBorderSoft
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("theme_option_${mode.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Miniature Theme Palette Swatch
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(swatchCanvas)
                    .border(1.dp, CardBorderSoft, RoundedCornerShape(12.dp))
                    .padding(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(swatchCard)
                        .align(Alignment.TopCenter)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(swatchAccent)
                        .align(Alignment.BottomEnd)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = mode.labelVi,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = InkPrimary
                )
                Text(
                    text = mode.subtitleVi,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary
                )
            }

            Icon(
                imageVector = if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = if (selected) "Đang chọn" else "Chưa chọn",
                tint = if (selected) IndigoOnSurface else InkMuted,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
