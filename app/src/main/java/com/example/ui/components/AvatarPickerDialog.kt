package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.auth.UserAccountProfile
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.ThemeOnPrimary
import com.example.ui.theme.ThemePrimary
import com.example.ui.theme.VividViolet
import java.io.File

/**
 * 8 crisp built-in academic & fun Avatar Presets for Master Ielts learners.
 */
enum class AvatarPreset(
    val id: String,
    val labelVi: String,
    val icon: ImageVector?,
    val startColor: Color,
    val endColor: Color,
    val isOwlIllustration: Boolean = false
) {
    STUDENT(
        id = "student",
        labelVi = "Sinh viên",
        icon = Icons.Rounded.Face,
        startColor = Color(0xFF4F46E5),
        endColor = Color(0xFF06B6D4)
    ),
    SCHOLAR(
        id = "scholar",
        labelVi = "Học giả",
        icon = Icons.Rounded.Psychology,
        startColor = Color(0xFF7C3AED),
        endColor = Color(0xFFEC4899)
    ),
    OWL(
        id = "owl",
        labelVi = "Cú tri thức",
        icon = null,
        startColor = Color(0xFF0F766E),
        endColor = Color(0xFF14B8A6),
        isOwlIllustration = true
    ),
    GRAD_CAP(
        id = "grad_cap",
        labelVi = "Mũ cử nhân",
        icon = Icons.Rounded.School,
        startColor = Color(0xFF2563EB),
        endColor = Color(0xFF6366F1)
    ),
    GLOBE(
        id = "globe",
        labelVi = "Quả địa cầu",
        icon = Icons.Rounded.Public,
        startColor = Color(0xFF059669),
        endColor = Color(0xFF34D399)
    ),
    BOOKS(
        id = "books",
        labelVi = "Thư viện",
        icon = Icons.Rounded.AutoStories,
        startColor = Color(0xFFD97706),
        endColor = Color(0xFFF59E0B)
    ),
    TROPHY(
        id = "trophy",
        labelVi = "Cúp 8.5",
        icon = Icons.Rounded.EmojiEvents,
        startColor = Color(0xFFEA580C),
        endColor = Color(0xFFFACC15)
    ),
    ROCKET(
        id = "rocket",
        labelVi = "Bứt phá",
        icon = Icons.Rounded.RocketLaunch,
        startColor = Color(0xFFE11D48),
        endColor = Color(0xFFF97316)
    );

    companion object {
        fun fromId(raw: String?): AvatarPreset? =
            entries.firstOrNull { it.id == raw }
    }
}

/**
 * Reusable circular User Avatar synced across Top Header, Account Sidebar, General Settings,
 * and AppLock / Profile summaries.
 * Priority: 1. Custom internal file photo (offline via Coil) -> 2. Built-in Preset -> 3. Initials.
 */
@Composable
fun UserAvatarCircle(
    userProfile: UserAccountProfile,
    size: Dp,
    modifier: Modifier = Modifier,
    borderColor: Color = Color.White.copy(alpha = 0.70f),
    borderWidth: Dp = 2.dp,
    contentDescription: String = "Ảnh đại diện tài khoản"
) {
    val context = LocalContext.current
    val customFile = remember(userProfile.customAvatarPath, userProfile.avatarVersion) {
        if (userProfile.customAvatarPath.isNotBlank()) {
            File(userProfile.customAvatarPath).takeIf { it.exists() && it.length() > 0L }
        } else {
            null
        }
    }
    val preset = remember(userProfile.avatarPresetId) {
        AvatarPreset.fromId(userProfile.avatarPresetId)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(borderWidth, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when {
            customFile != null -> {
                val request = remember(customFile.absolutePath, userProfile.avatarVersion) {
                    ImageRequest.Builder(context)
                        .data(customFile)
                        .memoryCacheKey("user_avatar_${userProfile.avatarVersion}")
                        .diskCacheKey("user_avatar_${userProfile.avatarVersion}")
                        .crossfade(true)
                        .build()
                }
                AsyncImage(
                    model = request,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            preset != null -> {
                AvatarPresetBadge(
                    preset = preset,
                    size = size
                )
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(ElectricIndigo, VividViolet, CyanGlow)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val textSize = when {
                        size >= 72.dp -> 26.sp
                        size >= 52.dp -> 19.sp
                        else -> 15.sp
                    }
                    Text(
                        text = userProfile.initials,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = textSize
                        ),
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun AvatarPresetBadge(
    preset: AvatarPreset,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(preset.startColor, preset.endColor)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (preset.isOwlIllustration) {
            WiseOwlVectorArt(modifier = Modifier.size(size * 0.72f))
        } else {
            Icon(
                imageVector = preset.icon ?: Icons.Rounded.School,
                contentDescription = preset.labelVi,
                tint = Color.White,
                modifier = Modifier.size(size * 0.56f)
            )
        }
    }
}

@Composable
private fun WiseOwlVectorArt(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f

        // Owl head silhouette + ear tufts
        val headPath = Path().apply {
            moveTo(w * 0.20f, h * 0.24f)
            lineTo(w * 0.34f, h * 0.34f)
            lineTo(w * 0.66f, h * 0.34f)
            lineTo(w * 0.80f, h * 0.24f)
            lineTo(w * 0.84f, h * 0.66f)
            cubicTo(w * 0.84f, h * 0.90f, w * 0.68f, h * 0.94f, cx, h * 0.94f)
            cubicTo(w * 0.32f, h * 0.94f, w * 0.16f, h * 0.90f, w * 0.16f, h * 0.66f)
            close()
        }
        drawPath(path = headPath, color = Color(0xFFFFFBEB))

        // Academic mini mortarboard hat on top
        val hatPath = Path().apply {
            moveTo(cx, h * 0.06f)
            lineTo(w * 0.78f, h * 0.18f)
            lineTo(cx, h * 0.29f)
            lineTo(w * 0.22f, h * 0.18f)
            close()
        }
        drawPath(path = hatPath, color = Color(0xFF1E293B))

        // Large expressive owl eyes
        val eyeY = h * 0.54f
        val leftEyeX = w * 0.36f
        val rightEyeX = w * 0.64f
        val outerR = w * 0.145f
        val pupilR = w * 0.072f

        drawCircle(color = Color(0xFFFDE68A), radius = outerR, center = Offset(leftEyeX, eyeY))
        drawCircle(color = Color(0xFFFDE68A), radius = outerR, center = Offset(rightEyeX, eyeY))
        drawCircle(color = Color(0xFF0F172A), radius = pupilR, center = Offset(leftEyeX, eyeY))
        drawCircle(color = Color(0xFF0F172A), radius = pupilR, center = Offset(rightEyeX, eyeY))
        drawCircle(color = Color.White, radius = pupilR * 0.36f, center = Offset(leftEyeX - pupilR * 0.28f, eyeY - pupilR * 0.28f))
        drawCircle(color = Color.White, radius = pupilR * 0.36f, center = Offset(rightEyeX - pupilR * 0.28f, eyeY - pupilR * 0.28f))

        // Golden beak
        val beakPath = Path().apply {
            moveTo(cx, h * 0.60f)
            lineTo(cx + w * 0.08f, h * 0.69f)
            lineTo(cx, h * 0.78f)
            lineTo(cx - w * 0.08f, h * 0.69f)
            close()
        }
        drawPath(path = beakPath, color = Color(0xFFF59E0B))

        // Tiny chest feather arc
        drawArc(
            color = Color(0xFF99F6E4),
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(w * 0.32f, h * 0.72f),
            size = Size(w * 0.36f, h * 0.16f)
        )
    }
}

/**
 * Interactive Avatar Customization Dialog:
 * - Built-in 8-preset academic & fun gallery
 * - Zero-permission Android Photo Picker (ActivityResultContracts.PickVisualMedia)
 *   copied into internal storage so it loads 100% offline across restarts
 */
@Composable
fun AvatarPickerDialog(
    userProfile: UserAccountProfile,
    onSelectPreset: (String) -> Unit,
    onPickPhotoUri: (Uri) -> Unit,
    onResetToInitials: () -> Unit,
    onDismiss: () -> Unit
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onPickPhotoUri(uri)
        }
    }

    val fallbackContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onPickPhotoUri(uri)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, CardBorderSoft),
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .testTag("avatar_picker_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tùy chỉnh ảnh đại diện",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = InkPrimary
                        )
                        Text(
                            text = "Chọn ảnh từ máy hoặc biểu tượng học thuật Master Ielts",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Đóng chọn ảnh đại diện",
                            tint = InkSecondary
                        )
                    }
                }

                // Live Current Avatar Preview
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UserAvatarCircle(
                        userProfile = userProfile,
                        size = 88.dp,
                        borderColor = ThemePrimary,
                        borderWidth = 3.dp
                    )
                    Text(
                        text = userProfile.displayName.ifBlank { "Học viên Master Ielts" },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = InkPrimary
                    )
                }

                // Pick Custom Photo from Device (Zero-Permission Photo Picker)
                Button(
                    onClick = {
                        runCatching {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }.onFailure {
                            fallbackContentLauncher.launch("image/*")
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThemePrimary,
                        contentColor = ThemeOnPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 13.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("avatar_pick_photo_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Chọn ảnh từ thiết bị (Lưu offline)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Built-in 8-Preset Academic & Fun Avatar Gallery
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Bộ sưu tập Avatar học thuật (8 mẫu)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = InkPrimary
                    )

                    val rows = AvatarPreset.entries.chunked(4)
                    rows.forEach { rowPresets ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowPresets.forEach { preset ->
                                val isSelected = userProfile.customAvatarPath.isBlank() &&
                                    userProfile.avatarPresetId == preset.id
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) ElectricIndigoLight else SurfaceCanvas,
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) ThemePrimary else CardBorderSoft
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { onSelectPreset(preset.id) }
                                        .testTag("avatar_preset_${preset.id}")
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 9.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.TopEnd) {
                                            AvatarPresetBadge(preset = preset, size = 48.dp)
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Rounded.CheckCircle,
                                                    contentDescription = "Đang chọn",
                                                    tint = ThemePrimary,
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .clip(CircleShape)
                                                        .background(CardWhite)
                                                )
                                            }
                                        }
                                        Text(
                                            text = preset.labelVi,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = if (isSelected) ThemePrimary else InkSecondary,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Row: Reset to Initials + Done
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onResetToInitials,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CardBorderSoft),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("avatar_reset_initials_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = null,
                            tint = InkSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Chữ cái tên",
                            style = MaterialTheme.typography.labelMedium,
                            color = InkSecondary,
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricIndigo,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("avatar_done_btn")
                    ) {
                        Text(
                            text = "Xong",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
