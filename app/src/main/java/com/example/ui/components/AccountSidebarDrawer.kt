package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.LibraryAdd
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.auth.GoogleAuthHelper
import com.example.auth.GoogleSignInResult
import com.example.auth.UserAccountProfile
import com.example.data.local.BulkImportDataset
import com.example.ui.screens.auth.GoogleFallbackSignInDialog
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.GoldMastery
import com.example.ui.theme.GoldMasteryLight
import com.example.ui.theme.GoldOnContainer
import com.example.ui.theme.IndigoOnSurface
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkOnHero
import com.example.ui.theme.InkOnHeroMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.PracticeTeal
import com.example.ui.theme.PracticeTealLight
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseDiffLight
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.TealOnSurface
import com.example.ui.theme.VividViolet
import kotlinx.coroutines.launch
import java.util.Locale

private val DrawerHeaderBrush = Brush.linearGradient(
    colors = listOf(ElectricIndigo, VividViolet, CyanGlow),
    start = Offset(0f, 0f),
    end = Offset(750f, 650f)
)

/**
 * Prominent 44.dp circular top-corner Account Avatar Button placed in the Master Ielts header.
 * Syncs custom photo / academic preset / initials + live Google AI connection indicator dot.
 */
@Composable
fun AccountAvatarButton(
    userProfile: UserAccountProfile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.20f),
        shadowElevation = 4.dp,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(44.dp)
            .tactilePress()
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag("account_avatar_button")
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (userProfile.isLoggedIn) {
                UserAvatarCircle(
                    userProfile = userProfile,
                    size = 44.dp,
                    borderColor = Color.White.copy(alpha = 0.85f),
                    borderWidth = 2.dp,
                    contentDescription = stringResource(R.string.auth_account_avatar_cd)
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.AccountCircle,
                    contentDescription = stringResource(R.string.auth_account_avatar_cd),
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            // Live Google AI connection dot
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 1.dp, end = 1.dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (userProfile.isGoogleConnected) EmeraldSuccess else GoldMastery)
                    .border(2.dp, Color.White, CircleShape)
            )
        }
    }
}

/**
 * Material 3 Slide-Out Account Sidebar Drawer for Master Ielts with 5 structured menu sections:
 * 1. Đăng nhập / Tài khoản Google (Google Account for AI)
 * 2. Tự học (Personal Self-Study Library)
 * 3. Thêm tài liệu (Add Materials Hub)
 * 4. Cài đặt (Settings)
 * 5. Bảo mật (Security)
 */
@Composable
fun AccountSidebarDrawerContent(
    userProfile: UserAccountProfile,
    currentBand: Float,
    personalMaterialsCount: Int,
    appLockEnabled: Boolean,
    isOnline: Boolean,
    onGoogleSignInSuccess: (uid: String, displayName: String, email: String, isFallbackMode: Boolean) -> Unit,
    onOpenPersonalLibrary: () -> Unit,
    onOpenAddMaterialsHub: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSecurity: () -> Unit,
    onOpenAvatarPicker: () -> Unit = {},
    onLogout: () -> Unit,
    onShowMessage: (String) -> Unit,
    onCloseDrawer: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var fallbackReasonVi by remember { mutableStateOf<String?>(null) }
    var isGoogleBusy by remember { mutableStateOf(false) }

    val glassOn = isGlassmorphismActive()
    val drawerBg = if (glassOn) {
        CardWhite.copy(alpha = resolvedGlassAlpha(boostAlpha = 0.06f))
    } else {
        CardWhite
    }

    ModalDrawerSheet(
        drawerContainerColor = drawerBg,
        modifier = Modifier
            .widthIn(min = 290.dp, max = 340.dp)
            .fillMaxHeight()
            .testTag("account_sidebar_drawer")
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            // Top Gradient Profile Header with Safe Area Padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DrawerHeaderBrush)
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .clickable { onOpenAvatarPicker() }
                                .testTag("drawer_avatar_edit_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            UserAvatarCircle(
                                userProfile = userProfile,
                                size = 58.dp,
                                borderColor = Color.White.copy(alpha = 0.85f),
                                borderWidth = 2.5.dp,
                                contentDescription = "Đổi ảnh đại diện"
                            )
                            Surface(
                                shape = CircleShape,
                                color = CardWhite,
                                border = BorderStroke(1.5.dp, ElectricIndigo),
                                shadowElevation = 3.dp,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(22.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.PhotoCamera,
                                        contentDescription = "Chỉnh sửa ảnh đại diện",
                                        tint = ElectricIndigo,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userProfile.displayName.ifBlank { "Học viên Master Ielts" },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = InkOnHero,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = userProfile.email.ifBlank { "Chưa có email" },
                                style = MaterialTheme.typography.bodySmall,
                                color = InkOnHeroMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(onClick = onCloseDrawer) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Đóng menu tài khoản",
                            tint = Color.White
                        )
                    }
                }

                // Band Badge + Google AI Status Chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.36f))
                    ) {
                        Text(
                            text = "Band ${String.format(Locale.US, "%.1f", currentBand)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (userProfile.isGoogleConnected) EmeraldSuccess else GoldMastery
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (userProfile.isGoogleConnected) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = stringResource(
                                    if (userProfile.isGoogleConnected) {
                                        R.string.drawer_google_connected
                                    } else {
                                        R.string.drawer_google_disconnected
                                    }
                                ),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // MENU ITEM 1: Đăng nhập / Tài khoản Google (Google Account for AI)
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (userProfile.isGoogleConnected) EmeraldSuccessLight else SurfaceCanvas,
                    border = BorderStroke(
                        1.dp,
                        if (userProfile.isGoogleConnected) EmeraldSuccess.copy(alpha = 0.35f) else CardBorderSoft
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("drawer_menu_google_account")
                ) {
                    Column(
                        modifier = Modifier.padding(13.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(if (userProfile.isGoogleConnected) EmeraldSuccess else ElectricIndigo),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.drawer_menu_google_title),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = InkPrimary
                                )
                                Text(
                                    text = if (userProfile.isGoogleConnected) {
                                        if (userProfile.isGoogleFallbackMode) {
                                            "Đã kết nối (Offline/Dev): ${userProfile.email}"
                                        } else {
                                            "Đã kết nối Google AI: ${userProfile.email}"
                                        }
                                    } else {
                                        "Kết nối Google để bật chấm chữa AI & đồng bộ đám mây"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (userProfile.isGoogleConnected) EmeraldOnContainer else InkSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (isGoogleBusy) return@Button
                                    isGoogleBusy = true
                                    scope.launch {
                                        when (val res = GoogleAuthHelper.signInWithGoogle(context)) {
                                            is GoogleSignInResult.Success -> {
                                                isGoogleBusy = false
                                                onGoogleSignInSuccess(res.uid, res.displayName, res.email, false)
                                            }

                                            is GoogleSignInResult.Cancelled -> {
                                                isGoogleBusy = false
                                                onShowMessage("Đã hủy chọn tài khoản Google.")
                                            }

                                            is GoogleSignInResult.NeedsOfflineFallback -> {
                                                isGoogleBusy = false
                                                fallbackReasonVi = res.reasonVi
                                            }
                                        }
                                    }
                                },
                                enabled = !isGoogleBusy,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (userProfile.isGoogleConnected) PracticeTeal else ElectricIndigo,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("drawer_google_connect_btn")
                            ) {
                                Icon(
                                    imageVector = if (userProfile.isGoogleConnected) Icons.Rounded.SwapHoriz else Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (userProfile.isGoogleConnected) "Đổi tài khoản Google" else "Kết nối Google AI",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // MENU ITEM 2: Tự học (Personal Self-Study Library)
                DrawerMenuRowItem(
                    icon = Icons.Rounded.Book,
                    iconBg = PracticeTealLight,
                    iconTint = TealOnSurface,
                    title = stringResource(R.string.drawer_menu_self_study_title),
                    subtitle = stringResource(R.string.drawer_menu_self_study_sub),
                    badgeText = if (personalMaterialsCount > 0) "$personalMaterialsCount" else null,
                    onClick = {
                        onCloseDrawer()
                        onOpenPersonalLibrary()
                    },
                    testTag = "drawer_menu_self_study"
                )

                // MENU ITEM 3: Thêm tài liệu (Add Materials Hub)
                DrawerMenuRowItem(
                    icon = Icons.Rounded.LibraryAdd,
                    iconBg = ElectricIndigoLight,
                    iconTint = IndigoOnSurface,
                    title = stringResource(R.string.drawer_menu_add_materials_title),
                    subtitle = stringResource(R.string.drawer_menu_add_materials_sub),
                    badgeText = "Hub",
                    onClick = {
                        onCloseDrawer()
                        onOpenAddMaterialsHub()
                    },
                    testTag = "drawer_menu_add_materials"
                )

                // MENU ITEM 4: Cài đặt (Settings)
                DrawerMenuRowItem(
                    icon = Icons.Rounded.Settings,
                    iconBg = if (isOnline) EmeraldSuccessLight else GoldMasteryLight,
                    iconTint = if (isOnline) EmeraldOnContainer else GoldOnContainer,
                    title = stringResource(R.string.drawer_menu_settings_title),
                    subtitle = stringResource(R.string.drawer_menu_settings_sub),
                    badgeText = if (isOnline) "Online" else "Offline",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettings()
                    },
                    testTag = "drawer_menu_settings"
                )

                // MENU ITEM 5: Bảo mật (Security)
                DrawerMenuRowItem(
                    icon = Icons.Rounded.Security,
                    iconBg = GoldMasteryLight,
                    iconTint = GoldOnContainer,
                    title = stringResource(R.string.drawer_menu_security_title),
                    subtitle = stringResource(R.string.drawer_menu_security_sub),
                    badgeText = if (appLockEnabled) "PIN Bật" else null,
                    onClick = {
                        onCloseDrawer()
                        onOpenSecurity()
                    },
                    testTag = "drawer_menu_security"
                )

                HorizontalDivider(
                    color = CardBorderSoft,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Logout Button
                OutlinedButton(
                    onClick = { showLogoutConfirm = true },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, RoseAlert.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = RoseDiffLight.copy(alpha = 0.55f),
                        contentColor = RoseAlert
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("drawer_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.drawer_logout_btn),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            containerColor = CardWhite,
            title = {
                Text(
                    text = "Đăng xuất Master Ielts?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = InkPrimary
                )
            },
            text = {
                Text(
                    text = "Tiến độ học tập và thư viện tài liệu cục bộ trên máy vẫn được giữ nguyên an toàn. Bạn có thể đăng nhập lại bất cứ lúc nào.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        onCloseDrawer()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseAlert, contentColor = Color.White),
                    modifier = Modifier.testTag("confirm_logout_btn")
                ) {
                    Text("Đăng xuất")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Hủy", color = InkSecondary)
                }
            }
        )
    }

    if (fallbackReasonVi != null) {
        GoogleFallbackSignInDialog(
            reasonVi = fallbackReasonVi.orEmpty(),
            initialEmail = userProfile.email,
            initialName = userProfile.displayName,
            onConfirm = { name, chosenEmail ->
                fallbackReasonVi = null
                onGoogleSignInSuccess(
                    "google_dev_${chosenEmail.hashCode().toUInt()}",
                    name,
                    chosenEmail,
                    true
                )
            },
            onDismiss = { fallbackReasonVi = null }
        )
    }
}

@Composable
private fun DrawerMenuRowItem(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    badgeText: String?,
    onClick: () -> Unit,
    testTag: String
) {
    GlassSurface(
        shape = RoundedCornerShape(16.dp),
        solidColor = SurfaceCanvas,
        solidBorder = BorderStroke(1.dp, CardBorderSoft),
        solidShadowElevation = 0.dp,
        glassShadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .tactilePress()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = InkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = InkSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!badgeText.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = iconBg
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = iconTint,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                contentDescription = null,
                tint = InkMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/**
 * Centralized "Thêm tài liệu (Add Materials Hub)" Bottom Sheet.
 * Replaces scattered Import CSV/JSON & Add Material buttons previously on Roadmap and Learn screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMaterialsHubSheet(
    onOpenBulkImporter: (BulkImportDataset) -> Unit,
    onOpenPersonalLibrary: () -> Unit,
    onImportAudioUri: (Uri) -> Unit,
    onJumpToExamParser: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val audioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportAudioUri(uri)
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("add_materials_hub_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
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
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ElectricIndigoLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LibraryAdd,
                            contentDescription = null,
                            tint = IndigoOnSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Trung tâm Thêm tài liệu",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = InkPrimary
                        )
                        Text(
                            text = "Add Materials Hub • Nhập CSV/JSON, Ghi chú & Audio Offline",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Đóng trung tâm thêm tài liệu", tint = InkSecondary)
                }
            }

            Text(
                text = "1. Nhập hàng loạt từ tệp CSV / JSON (Hỗ trợ UTF-8 Tiếng Việt)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = InkPrimary
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HubActionTile(
                        icon = Icons.Rounded.MenuBook,
                        tint = IndigoOnSurface,
                        bg = ElectricIndigoLight,
                        title = "Từ vựng (Vocab)",
                        subtitle = "CSV / JSON C1–C2",
                        onClick = {
                            onDismiss()
                            onOpenBulkImporter(BulkImportDataset.VOCABULARY)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hub_import_vocab_btn")
                    )
                    HubActionTile(
                        icon = Icons.Rounded.EditNote,
                        tint = TealOnSurface,
                        bg = PracticeTealLight,
                        title = "Bài tập câu",
                        subtitle = "Sentence Drills 8.5+",
                        onClick = {
                            onDismiss()
                            onOpenBulkImporter(BulkImportDataset.SENTENCE_DRILLS)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hub_import_drills_btn")
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HubActionTile(
                        icon = Icons.Rounded.Description,
                        tint = GoldOnContainer,
                        bg = GoldMasteryLight,
                        title = "Đề thi IELTS",
                        subtitle = "Authentic / Custom",
                        onClick = {
                            onDismiss()
                            onOpenBulkImporter(BulkImportDataset.EXAMS)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hub_import_exams_btn")
                    )
                    HubActionTile(
                        icon = Icons.Rounded.Book,
                        tint = EmeraldOnContainer,
                        bg = EmeraldSuccessLight,
                        title = "Tài liệu cá nhân",
                        subtitle = "Bài đọc & Ghi chú",
                        onClick = {
                            onDismiss()
                            onOpenBulkImporter(BulkImportDataset.PERSONAL_MATERIALS)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hub_import_materials_btn")
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "2. Thêm trực tiếp tài nguyên tự học",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = InkPrimary
            )

            DrawerMenuRowItem(
                icon = Icons.Rounded.AddCircleOutline,
                iconBg = PracticeTealLight,
                iconTint = TealOnSurface,
                title = "Tạo tài liệu / Ghi chú Tự học mới",
                subtitle = "Mở Thư viện Cá nhân để soạn bài đọc, highlight & tóm tắt AI",
                badgeText = "Tự học",
                onClick = {
                    onDismiss()
                    onOpenPersonalLibrary()
                },
                testTag = "hub_create_personal_note_btn"
            )

            DrawerMenuRowItem(
                icon = Icons.Rounded.AudioFile,
                iconBg = ElectricIndigoLight,
                iconTint = IndigoOnSurface,
                title = "Tải tệp âm thanh MP3 / WAV (Listening)",
                subtitle = "Lưu trực tiếp vào kho âm thanh Offline trên thiết bị",
                badgeText = "Audio",
                onClick = { audioLauncher.launch(arrayOf("audio/*")) },
                testTag = "hub_import_audio_btn"
            )

            DrawerMenuRowItem(
                icon = Icons.Rounded.UploadFile,
                iconBg = EmeraldSuccessLight,
                iconTint = EmeraldOnContainer,
                title = "Số hóa đề thi bằng AI (AI Exam Parser)",
                subtitle = "Mở Tab Thi để dán đề hoặc tải PDF/DOCX/TXT/JSON",
                badgeText = "AI",
                onClick = {
                    onDismiss()
                    onJumpToExamParser()
                },
                testTag = "hub_ai_exam_parser_btn"
            )
        }
    }
}

@Composable
private fun HubActionTile(
    icon: ImageVector,
    tint: Color,
    bg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceCanvas,
        border = BorderStroke(1.dp, CardBorderSoft),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = InkPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = InkSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
