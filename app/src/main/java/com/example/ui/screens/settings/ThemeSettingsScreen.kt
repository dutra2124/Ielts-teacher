package com.example.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.AppBackgroundBackdrop
import com.example.ui.theme.AppThemePreferences
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.AppWallpaperPattern
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardSurfaceTinted
import com.example.ui.theme.CardWhite
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.IndigoOnSurface
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LocalApexColors
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.ThemeColorCatalog
import com.example.ui.theme.ThemeContrastUtils
import com.example.ui.theme.ThemeOnPrimary
import com.example.ui.theme.ThemePrimary
import com.example.ui.theme.WallpaperPatternCanvas
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Fullscreen Live-Preview Theme & Background Customizer inside Master Ielts Settings:
 * - 5 Curated High-Contrast Color Presets
 * - Independent Primary Button Color & Background Surface Color pickers (with WCAG contrast ratio badge)
 * - 4 Built-in Subtle Aesthetic Wallpaper Patterns + Zero-Permission Device Gallery Photo Picker
 * - Background Dim/Overlay Opacity Slider (0% - 85%)
 * - CD-IELTS Exam Room realistic scheme guarantee
 */
@Composable
fun ThemeSettingsScreen(
    prefs: AppThemePreferences,
    onSelectPreset: (AppThemePreset) -> Unit,
    onSelectPrimaryColor: (Long?) -> Unit,
    onSelectSurfaceColor: (Long?) -> Unit,
    onSelectWallpaperPattern: (AppWallpaperPattern) -> Unit,
    onPickCustomBackgroundUri: (Uri) -> Unit,
    onClearCustomBackground: () -> Unit,
    onOverlayOpacityChange: (Float) -> Unit,
    onAutoDarkExamChange: (Boolean) -> Unit,
    onResetDefault: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val palette = LocalApexColors.current
    val buttonContrastRatio = ThemeContrastUtils.contrastRatio(
        foreground = palette.onPrimaryAccent,
        background = palette.primaryAccent
    )
    val contrastLevelLabel = when {
        buttonContrastRatio >= 7.0f -> "WCAG AAA"
        buttonContrastRatio >= 4.5f -> "WCAG AA"
        else -> "Tốt"
    }

    // Zero-permission Android Photo Picker (100% Google Play Policy compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onPickCustomBackgroundUri(uri)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceCanvas)
            .testTag("theme_settings_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
        ) {
            // Top App Bar with safe area padding
            Surface(
                color = CardWhite,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, CardBorderSoft)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .displayCutoutPadding()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.testTag("theme_settings_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = InkPrimary
                            )
                        }
                        Column {
                            Text(
                                text = "Tùy chỉnh Giao diện & Hình nền",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = InkPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Xem trước trực tiếp • Áp dụng toàn bộ Master Ielts",
                                style = MaterialTheme.typography.labelSmall,
                                color = InkMuted
                            )
                        }
                    }

                    TextButton(
                        onClick = onResetDefault,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("theme_reset_default_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = null,
                            tint = IndigoOnSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Mặc định",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = IndigoOnSurface
                        )
                    }
                }
            }

            // Scrollable Customizer Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // 1. LIVE INTERACTIVE PREVIEW CARD (shows real wallpaper + dim overlay + high-contrast button)
                LiveThemePreviewCard(
                    prefs = prefs,
                    contrastRatio = buttonContrastRatio,
                    contrastLevel = contrastLevelLabel
                )

                // 2. 5 CURATED HIGH-CONTRAST COLOR PRESETS
                SectionCard(
                    icon = Icons.Rounded.Contrast,
                    title = "5 Bộ màu Tương phản cao (Presets)",
                    subtitle = "Tinh chỉnh riêng cho mắt học IELTS lâu dài & độ rõ chữ tối đa"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppThemePreset.entries.forEach { preset ->
                            val isSelected = prefs.preset == preset &&
                                prefs.customPrimaryHex == null &&
                                prefs.customSurfaceHex == null
                            PresetOptionCard(
                                preset = preset,
                                selected = isSelected,
                                onClick = { onSelectPreset(preset) }
                            )
                        }
                    }
                }

                // 3. INDEPENDENT PRIMARY BUTTON COLOR & BACKGROUND SURFACE COLOR PICKERS
                SectionCard(
                    icon = Icons.Rounded.Palette,
                    title = "Tùy chỉnh Màu nút & Màu nền độc lập",
                    subtitle = "Chữ trên nút chính tự động tối ưu tương phản chuẩn WCAG"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Primary Button Color Picker
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Màu nút hành động chính (Primary Button)",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = InkPrimary
                                )
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = EmeraldSuccessLight
                                ) {
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", buttonContrastRatio)}:1 • $contrastLevelLabel",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = EmeraldOnContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ThemeColorCatalog.primaryButtonSwatches.forEach { swatch ->
                                    val selected = prefs.effectivePrimaryColor == swatch.color
                                    ColorSwatchItem(
                                        label = swatch.nameVi,
                                        color = swatch.color,
                                        selected = selected,
                                        onClick = { onSelectPrimaryColor(swatch.colorHex) },
                                        testTag = "primary_swatch_${swatch.nameVi}"
                                    )
                                }
                            }
                        }

                        // Background Surface Color Picker
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Màu nền bề mặt (Background Surface)",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = InkPrimary
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ThemeColorCatalog.backgroundSurfaceSwatches.forEach { swatch ->
                                    val selected = prefs.effectiveSurfaceColor == swatch.color
                                    ColorSwatchItem(
                                        label = swatch.nameVi,
                                        color = swatch.color,
                                        selected = selected,
                                        onClick = { onSelectSurfaceColor(swatch.colorHex) },
                                        testTag = "surface_swatch_${swatch.nameVi}"
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. CUSTOM BACKGROUND IMAGE & 4 BUILT-IN AESTHETIC WALLPAPER PATTERNS + DIM SLIDER
                SectionCard(
                    icon = Icons.Rounded.Wallpaper,
                    title = "Hình nền & Họa tiết thẩm mỹ",
                    subtitle = "Chọn 1 trong 4 họa tiết có sẵn hoặc ảnh riêng từ thư viện thiết bị"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Built-in Pattern Strip (None + 4 patterns)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AppWallpaperPattern.entries.forEach { pattern ->
                                val selected = prefs.customBackgroundPath.isBlank() && prefs.wallpaperPattern == pattern
                                WallpaperPatternTile(
                                    pattern = pattern,
                                    selected = selected,
                                    onClick = { onSelectWallpaperPattern(pattern) }
                                )
                            }
                        }

                        // Device Gallery Picker Button (PickVisualMedia - zero storage permission)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ThemePrimary,
                                    contentColor = ThemeOnPrimary
                                ),
                                border = if (palette.isHighContrast) BorderStroke(1.5.dp, Color.White) else null,
                                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("pick_gallery_wallpaper_btn")
                            ) {
                                Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (prefs.customBackgroundPath.isNotBlank()) {
                                        "Đổi ảnh từ thư viện"
                                    } else {
                                        "Chọn ảnh nền từ thư viện"
                                    },
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (prefs.customBackgroundPath.isNotBlank()) {
                                OutlinedButton(
                                    onClick = onClearCustomBackground,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, RoseAlert),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("clear_gallery_wallpaper_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Xóa ảnh nền",
                                        tint = RoseAlert,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Background Dim / Overlay Opacity Slider (0% - 85%)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SurfaceCanvas,
                            border = BorderStroke(1.dp, CardBorderSoft)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Opacity,
                                            contentDescription = null,
                                            tint = IndigoOnSurface,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Độ mờ lớp phủ nền (Dim Overlay)",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = InkPrimary
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = CardSurfaceTinted
                                    ) {
                                        Text(
                                            text = "${(prefs.backgroundOverlayOpacity * 100f).roundToInt()}%",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontFamily = PlusJakartaSansFontFamily,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = IndigoOnSurface,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Slider(
                                    value = prefs.backgroundOverlayOpacity.coerceIn(0f, 0.85f),
                                    onValueChange = onOverlayOpacityChange,
                                    valueRange = 0f..0.85f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = ThemePrimary,
                                        activeTrackColor = ThemePrimary,
                                        inactiveTrackColor = CardBorderSoft
                                    ),
                                    modifier = Modifier.testTag("background_overlay_slider")
                                )
                                Text(
                                    text = "Tăng độ mờ (0%–85%) để chữ và thẻ bài học luôn nổi bật rõ nét trên mọi hình nền.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = InkMuted
                                )
                            }
                        }
                    }
                }

                // 5. CD-IELTS EXAM ROOM REALISTIC MODE GUARANTEE
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardWhite,
                    border = BorderStroke(1.dp, CardBorderSoft)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
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
                                text = "Tự động Chế độ Tối trong Phòng thi CD-IELTS",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = InkPrimary
                            )
                            Text(
                                text = "Phòng thi thử luôn giữ chuẩn màu IDP/British Council (không áp dụng hình nền cá nhân) để đảm bảo trải nghiệm thi thật.",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSecondary
                            )
                        }
                        Switch(
                            checked = prefs.autoDarkInExamRoom,
                            onCheckedChange = onAutoDarkExamChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ThemeOnPrimary,
                                checkedTrackColor = ThemePrimary
                            ),
                            modifier = Modifier.testTag("theme_auto_exam_switch")
                        )
                    }
                }

                // Bottom Save & Close High-Visibility CTA
                Button(
                    onClick = onClose,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThemePrimary,
                        contentColor = ThemeOnPrimary
                    ),
                    border = if (palette.isHighContrast) BorderStroke(1.5.dp, Color.White) else null,
                    contentPadding = PaddingValues(vertical = 14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("theme_sheet_done_btn")
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Áp dụng & Quay lại Master Ielts",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveThemePreviewCard(
    prefs: AppThemePreferences,
    contrastRatio: Float,
    contrastLevel: String
) {
    val palette = LocalApexColors.current

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = CardWhite,
        border = BorderStroke(if (palette.isHighContrast) 2.dp else 1.2.dp, CardBorderSoft),
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("theme_live_preview_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(198.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {
            AppBackgroundBackdrop(themePrefs = prefs) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = CardWhite.copy(alpha = 0.94f),
                            border = BorderStroke(1.dp, CardBorderSoft)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = IndigoOnSurface,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Xem trước trực tiếp • ${prefs.preset.titleVi}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = InkPrimary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = EmeraldSuccessLight
                        ) {
                            Text(
                                text = "$contrastLevel (${String.format(Locale.US, "%.1f", contrastRatio)}:1)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldOnContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Sample Foreground Card inside Live Preview
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = CardWhite,
                        border = BorderStroke(if (palette.isHighContrast) 1.75.dp else 1.dp, CardBorderSoft),
                        shadowElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "Module #01 • Environment",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = InkPrimary
                                )
                                Text(
                                    text = "Từ vựng 75% • Câu 8.5 60% • Band 7.5",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ThemePrimary,
                                border = if (palette.isHighContrast) BorderStroke(1.5.dp, Color.White) else null,
                                shadowElevation = 3.dp
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Học ngay",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = ThemeOnPrimary
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                        contentDescription = null,
                                        tint = ThemeOnPrimary,
                                        modifier = Modifier.size(14.dp)
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

@Composable
private fun SectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, CardBorderSoft),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(CardSurfaceTinted),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = IndigoOnSurface,
                        modifier = Modifier.size(19.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = InkPrimary
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun PresetOptionCard(
    preset: AppThemePreset,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) CardSurfaceTinted else SurfaceCanvas,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) ThemePrimary else CardBorderSoft
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .testTag("theme_preset_${preset.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Miniature Theme Swatch
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(preset.surfaceColor)
                    .border(1.5.dp, preset.borderColor, RoundedCornerShape(13.dp))
                    .padding(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(preset.cardColor)
                        .align(Alignment.TopCenter)
                )
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(preset.primaryColor)
                        .align(Alignment.BottomEnd)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = preset.titleVi,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = InkPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = preset.primaryColor
                    ) {
                        Text(
                            text = preset.contrastBadge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = ThemeContrastUtils.bestOnColor(preset.primaryColor),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = preset.subtitleVi,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Đang chọn",
                    tint = ThemePrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun ColorSwatchItem(
    label: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 3.dp, vertical = 2.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(elevation = if (selected) 5.dp else 1.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(color)
                .border(
                    width = if (selected) 3.dp else 1.2.dp,
                    color = if (selected) ThemePrimary else CardBorderSoft,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Đang chọn",
                    tint = ThemeContrastUtils.bestOnColor(color),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (selected) IndigoOnSurface else InkSecondary,
            maxLines = 1
        )
    }
}

@Composable
private fun WallpaperPatternTile(
    pattern: AppWallpaperPattern,
    selected: Boolean,
    onClick: () -> Unit
) {
    val palette = LocalApexColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .width(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("wallpaper_pattern_${pattern.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = if (selected) 2.2.dp else 1.dp,
                    color = if (selected) ThemePrimary else CardBorderSoft,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            WallpaperPatternCanvas(
                pattern = pattern,
                primary = palette.primaryAccent,
                secondary = palette.secondaryAccent,
                surface = palette.surfaceCanvas,
                isDark = palette.isDark,
                strength = 1.0f,
                modifier = Modifier.fillMaxSize()
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(ThemePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = ThemeOnPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
        Text(
            text = pattern.titleVi,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (selected) IndigoOnSurface else InkSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
