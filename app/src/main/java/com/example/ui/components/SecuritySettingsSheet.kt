package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ai.AppCheckDiagnostics
import com.example.auth.AuthMethod
import com.example.auth.BiometricUnlockHelper
import com.example.auth.LocalCryptoHelper
import com.example.auth.SecurityPreferences
import com.example.auth.UserAccountProfile
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardWhite
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
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.PracticeTeal
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.TealOnSurface
import kotlinx.coroutines.launch

/**
 * Dedicated Security ("Bảo mật") Bottom Sheet opened from the Account Sidebar Drawer:
 * 1. Account Password Management (Change / Set Local Password with salted PBKDF2-SHA256)
 * 2. App Lock (4-digit PIN + Biometric Unlock toggle)
 * 3. Firebase App Check Debug Token + Personal Gemini API Key Fallback (Sideload)
 * 4. Full Local Data Backup Export & Restore (.json)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsSheet(
    userProfile: UserAccountProfile,
    securityPrefs: SecurityPreferences,
    appCheckDiagnostics: AppCheckDiagnostics,
    personalGeminiApiKey: String,
    onChangePassword: (currentPassword: String, newPassword: String, onResult: (Result<Unit>) -> Unit) -> Unit,
    onConfigureAppLockPin: (pin: String, enableLock: Boolean, onResult: (Result<Unit>) -> Unit) -> Unit,
    onToggleAppLock: (Boolean) -> Unit,
    onToggleBiometricLock: (Boolean) -> Unit,
    onSavePersonalGeminiApiKey: (String) -> Unit,
    onExportBackupJson: suspend () -> String,
    onRestoreBackupJson: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showSetupPinDialog by remember { mutableStateOf(false) }
    var draftGeminiKey by remember(personalGeminiApiKey) { mutableStateOf(personalGeminiApiKey) }
    var showGeminiKeyPlain by remember { mutableStateOf(false) }
    var showDebugTokenPlain by remember { mutableStateOf(false) }

    val biometricHardwareReady = remember {
        BiometricUnlockHelper.isBiometricHardwareAvailable(context)
    }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val payload = onExportBackupJson()
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(payload.toByteArray(Charsets.UTF_8))
                    }
                }.onSuccess {
                    onShowMessage("Đã xuất tệp sao lưu toàn bộ dữ liệu (.json) thành công!")
                }.onFailure { err ->
                    onShowMessage("Lỗi xuất tệp sao lưu: ${err.localizedMessage ?: "Không rõ"}")
                }
            }
        }
    }

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    context.contentResolver.openInputStream(uri)
                        ?.bufferedReader(Charsets.UTF_8)
                        ?.use { it.readText() }
                        ?: error("Không đọc được nội dung tệp JSON.")
                }.onSuccess { rawJson ->
                    onRestoreBackupJson(rawJson)
                }.onFailure { err ->
                    onShowMessage("Lỗi đọc tệp khôi phục: ${err.localizedMessage ?: "Không rõ"}")
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("security_settings_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
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
                            .background(GoldMasteryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = null,
                            tint = GoldOnContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.security_sheet_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = InkPrimary
                        )
                        Text(
                            text = stringResource(R.string.security_sheet_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Đóng bảo mật", tint = InkSecondary)
                }
            }

            // =================================================================
            // SECTION 1: Quản lý mật khẩu tài khoản (Account Password)
            // =================================================================
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfaceCanvas,
                border = BorderStroke(1.dp, CardBorderSoft)
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
                        Icon(Icons.Rounded.Password, contentDescription = null, tint = IndigoOnSurface, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Mật khẩu tài khoản (Account Password)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = InkPrimary
                        )
                    }
                    Text(
                        text = if (userProfile.authMethod == AuthMethod.LOCAL_EMAIL) {
                            "Đang bảo vệ tài khoản ${userProfile.email} bằng thuật toán băm PBKDF2-SHA256 cục bộ."
                        } else {
                            "Bạn đang đăng nhập bằng Google (${userProfile.email}). Bạn vẫn có thể đặt thêm mật khẩu cục bộ dự phòng."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )
                    OutlinedButton(
                        onClick = { showChangePasswordDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, ElectricIndigo),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("security_change_password_btn")
                    ) {
                        Icon(Icons.Rounded.Password, contentDescription = null, tint = IndigoOnSurface, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (userProfile.authMethod == AuthMethod.LOCAL_EMAIL) {
                                "Đổi mật khẩu tài khoản"
                            } else {
                                "Đặt mật khẩu cục bộ dự phòng"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = IndigoOnSurface
                        )
                    }
                }
            }

            // =================================================================
            // SECTION 2: Khóa ứng dụng (App Lock: 4-Digit PIN & Biometric)
            // =================================================================
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfaceCanvas,
                border = BorderStroke(1.dp, CardBorderSoft)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = IndigoOnSurface, modifier = Modifier.size(18.dp))
                            Column {
                                Text(
                                    text = "Khóa ứng dụng (App Lock PIN)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = InkPrimary
                                )
                                Text(
                                    text = if (securityPrefs.appLockEnabled) {
                                        "Yêu cầu mã PIN 4 số mỗi khi mở Master Ielts"
                                    } else {
                                        "Bảo vệ hồ sơ & ghi chú học tập bằng mã PIN 4 chữ số"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = InkSecondary
                                )
                            }
                        }
                        Switch(
                            checked = securityPrefs.appLockEnabled,
                            onCheckedChange = { wantEnable ->
                                if (wantEnable && !securityPrefs.hasPinConfigured) {
                                    showSetupPinDialog = true
                                } else {
                                    onToggleAppLock(wantEnable)
                                }
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = ElectricIndigo),
                            modifier = Modifier.testTag("security_applock_switch")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showSetupPinDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CardBorderSoft),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("security_set_pin_btn")
                        ) {
                            Text(
                                text = if (securityPrefs.hasPinConfigured) "Đổi mã PIN 4 số" else "Thiết lập mã PIN 4 số",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = InkPrimary
                            )
                        }
                    }

                    // Biometric Toggle (active when App Lock is enabled)
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
                            Icon(Icons.Rounded.Fingerprint, contentDescription = null, tint = TealOnSurface, modifier = Modifier.size(20.dp))
                            Column {
                                Text(
                                    text = "Mở khóa bằng Sinh trắc học (Biometric)",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = InkPrimary
                                )
                                Text(
                                    text = if (biometricHardwareReady) {
                                        "Dùng vân tay / khuôn mặt thay cho mã PIN 4 số"
                                    } else {
                                        "Thiết bị này không có cảm biến sinh trắc học (chỉ dùng mã PIN)"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = InkMuted
                                )
                            }
                        }
                        Switch(
                            checked = securityPrefs.biometricEnabled && securityPrefs.appLockEnabled,
                            enabled = securityPrefs.appLockEnabled && biometricHardwareReady,
                            onCheckedChange = onToggleBiometricLock,
                            colors = SwitchDefaults.colors(checkedTrackColor = PracticeTeal),
                            modifier = Modifier.testTag("security_biometric_switch")
                        )
                    }
                }
            }

            // =================================================================
            // SECTION 3: Firebase App Check Debug Token & Gemini API Key Fallback
            // =================================================================
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfaceCanvas,
                border = BorderStroke(1.dp, CardBorderSoft),
                modifier = Modifier.testTag("security_appcheck_panel")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                            Icon(Icons.Rounded.Security, contentDescription = null, tint = IndigoOnSurface, modifier = Modifier.size(18.dp))
                            Text(
                                text = stringResource(R.string.security_appcheck_title),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = InkPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (appCheckDiagnostics.isInitialized) EmeraldSuccessLight else GoldMasteryLight,
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Text(
                                text = if (appCheckDiagnostics.isSideloadedOrDebug) "Debug / Sideload" else "Play Store",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (appCheckDiagnostics.isInitialized) EmeraldOnContainer else GoldOnContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = "${appCheckDiagnostics.providerModeLabelVi} • Nguồn: ${appCheckDiagnostics.installSourceLabelVi}",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )

                    val debugToken = appCheckDiagnostics.debugToken
                    if (!debugToken.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = CardWhite,
                            border = BorderStroke(1.dp, CardBorderSoft)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "App Check Debug Token:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = InkPrimary
                                    )
                                    IconButton(
                                        onClick = { showDebugTokenPlain = !showDebugTokenPlain },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (showDebugTokenPlain) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                            contentDescription = "Hiện hoặc ẩn Debug Token",
                                            tint = InkSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (showDebugTokenPlain) debugToken else "${debugToken.take(8)}-••••-••••-••••-${debugToken.takeLast(6)}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = IndigoOnSurface
                                )
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        clipboard?.setPrimaryClip(ClipData.newPlainText("Master Ielts App Check Debug Token", debugToken))
                                        onShowMessage("Đã sao chép App Check Debug Token vào bộ nhớ tạm!")
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, ElectricIndigo),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .minimumInteractiveComponentSize()
                                        .testTag("copy_appcheck_debug_token_btn")
                                ) {
                                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = IndigoOnSurface, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.security_copy_debug_token_btn),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = IndigoOnSurface
                                    )
                                }
                            }
                        }
                    }

                    // Personal Gemini API Key Fallback
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.Key, contentDescription = null, tint = TealOnSurface, modifier = Modifier.size(16.dp))
                        Text(
                            text = stringResource(R.string.security_gemini_key_title),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = InkPrimary
                        )
                    }
                    OutlinedTextField(
                        value = draftGeminiKey,
                        onValueChange = { draftGeminiKey = it },
                        placeholder = {
                            Text("AIzaSy... (tùy chọn, lưu cục bộ trên máy)", style = MaterialTheme.typography.bodySmall)
                        },
                        singleLine = true,
                        visualTransformation = if (showGeminiKeyPlain) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showGeminiKeyPlain = !showGeminiKeyPlain }) {
                                Icon(
                                    imageVector = if (showGeminiKeyPlain) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = "Hiện hoặc ẩn API Key",
                                    tint = InkSecondary
                                )
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PracticeTeal,
                            unfocusedBorderColor = CardBorderSoft,
                            focusedContainerColor = CardWhite,
                            unfocusedContainerColor = CardWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("personal_gemini_key_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onSavePersonalGeminiApiKey(draftGeminiKey) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PracticeTeal, contentColor = Color.White),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("save_personal_gemini_key_btn")
                        ) {
                            Text(stringResource(R.string.security_gemini_key_save_btn))
                        }
                        if (personalGeminiApiKey.isNotBlank() || draftGeminiKey.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    draftGeminiKey = ""
                                    onSavePersonalGeminiApiKey("")
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, RoseAlert.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("clear_personal_gemini_key_btn")
                            ) {
                                Text(stringResource(R.string.security_gemini_key_clear_btn), color = RoseAlert)
                            }
                        }
                    }
                }
            }

            // =================================================================
            // SECTION 4: Sao lưu & Khôi phục dữ liệu (.json)
            // =================================================================
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfaceCanvas,
                border = BorderStroke(1.dp, CardBorderSoft)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.backup_section_title),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = InkPrimary
                    )
                    Text(
                        text = "Xuất hoặc khôi phục toàn bộ dữ liệu học tập cục bộ (.json) qua trình quản lý tệp an toàn của Android.",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { exportBackupLauncher.launch("MasterIelts_FullBackup.json") },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PracticeTeal, contentColor = Color.White),
                            contentPadding = PaddingValues(vertical = 11.dp, horizontal = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("export_full_backup_button")
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(stringResource(R.string.backup_export_btn), style = MaterialTheme.typography.labelMedium, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { restoreBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, PracticeTeal),
                            contentPadding = PaddingValues(vertical = 11.dp, horizontal = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("restore_full_backup_button")
                        ) {
                            Icon(Icons.Rounded.Upload, contentDescription = null, tint = TealOnSurface, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(stringResource(R.string.backup_restore_btn), style = MaterialTheme.typography.labelMedium, color = TealOnSurface, maxLines = 1)
                        }
                    }
                }
            }
        }
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            requireCurrentPassword = userProfile.authMethod == AuthMethod.LOCAL_EMAIL,
            onSave = { currentPw, newPw, onDone ->
                onChangePassword(currentPw, newPw) { res ->
                    onDone(res)
                    if (res.isSuccess) {
                        showChangePasswordDialog = false
                    }
                }
            },
            onDismiss = { showChangePasswordDialog = false }
        )
    }

    if (showSetupPinDialog) {
        SetupAppLockPinDialog(
            onSave = { pin, onDone ->
                onConfigureAppLockPin(pin, true) { res ->
                    onDone(res)
                    if (res.isSuccess) {
                        showSetupPinDialog = false
                    }
                }
            },
            onDismiss = { showSetupPinDialog = false }
        )
    }
}

@Composable
private fun ChangePasswordDialog(
    requireCurrentPassword: Boolean,
    onSave: (currentPassword: String, newPassword: String, onDone: (Result<Unit>) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var currentPw by remember { mutableStateOf("") }
    var newPw by remember { mutableStateOf("") }
    var confirmPw by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardWhite,
        title = {
            Text(
                text = if (requireCurrentPassword) "Đổi mật khẩu tài khoản" else "Đặt mật khẩu cục bộ",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = InkPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (requireCurrentPassword) {
                    OutlinedTextField(
                        value = currentPw,
                        onValueChange = { currentPw = it; errorMsg = null },
                        label = { Text("Mật khẩu hiện tại") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                OutlinedTextField(
                    value = newPw,
                    onValueChange = { newPw = it; errorMsg = null },
                    label = { Text("Mật khẩu mới (tối thiểu 6 ký tự)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPw,
                    onValueChange = { confirmPw = it; errorMsg = null },
                    label = { Text("Xác nhận mật khẩu mới") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg != null) {
                    Text(errorMsg.orEmpty(), style = MaterialTheme.typography.labelSmall, color = RoseAlert)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!LocalCryptoHelper.isValidPassword(newPw)) {
                        errorMsg = "Mật khẩu mới cần tối thiểu 6 ký tự."
                    } else if (newPw != confirmPw) {
                        errorMsg = "Mật khẩu xác nhận chưa khớp."
                    } else {
                        onSave(currentPw, newPw) { res ->
                            res.onFailure { err -> errorMsg = err.message ?: "Lỗi đổi mật khẩu." }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo, contentColor = Color.White)
            ) {
                Text("Lưu mật khẩu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy", color = InkSecondary) }
        }
    )
}

@Composable
private fun SetupAppLockPinDialog(
    onSave: (pin: String, onDone: (Result<Unit>) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardWhite,
        title = {
            Text(
                text = "Thiết lập mã PIN 4 chữ số",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = InkPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Mã PIN được băm bảo mật (PBKDF2) trên thiết bị và dùng để mở khóa ứng dụng Master Ielts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = { input ->
                        if (input.length <= 4 && input.all { it.isDigit() }) {
                            pin = input
                            errorMsg = null
                        }
                    },
                    label = { Text("Mã PIN 4 chữ số") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { input ->
                        if (input.length <= 4 && input.all { it.isDigit() }) {
                            confirmPin = input
                            errorMsg = null
                        }
                    },
                    label = { Text("Nhập lại mã PIN 4 chữ số") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg != null) {
                    Text(errorMsg.orEmpty(), style = MaterialTheme.typography.labelSmall, color = RoseAlert)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!LocalCryptoHelper.isValidPin(pin)) {
                        errorMsg = "Mã PIN phải gồm đúng 4 chữ số."
                    } else if (pin != confirmPin) {
                        errorMsg = "Hai lần nhập mã PIN không khớp."
                    } else {
                        onSave(pin) { res ->
                            res.onFailure { err -> errorMsg = err.message ?: "Không thể lưu mã PIN." }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo, contentColor = Color.White),
                modifier = Modifier.testTag("confirm_save_pin_btn")
            ) {
                Text("Lưu & Bật Khóa")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy", color = InkSecondary) }
        }
    )
}
