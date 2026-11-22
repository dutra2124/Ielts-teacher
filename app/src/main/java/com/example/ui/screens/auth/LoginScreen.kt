package com.example.ui.screens.auth

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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.auth.BiometricUnlockHelper
import com.example.auth.GoogleAuthHelper
import com.example.auth.GoogleSignInResult
import com.example.auth.LocalCryptoHelper
import com.example.auth.UserAccountProfile
import com.example.ui.components.UserAvatarCircle
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.IndigoOnSurface
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkOnHero
import com.example.ui.theme.InkOnHeroMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.PracticeTeal
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseDiffLight
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.VividViolet
import kotlinx.coroutines.launch

private val AuthHeroBrush = Brush.linearGradient(
    colors = listOf(ElectricIndigo, VividViolet, CyanGlow),
    start = Offset(0f, 0f),
    end = Offset(900f, 900f)
)

@Composable
fun LoginScreen(
    onRegisterWithEmail: (
        displayName: String,
        email: String,
        password: String,
        onResult: (Result<UserAccountProfile>) -> Unit
    ) -> Unit,
    onLoginWithEmail: (
        email: String,
        password: String,
        onResult: (Result<UserAccountProfile>) -> Unit
    ) -> Unit,
    onGoogleSignInSuccess: (
        uid: String,
        displayName: String,
        email: String,
        isFallbackMode: Boolean
    ) -> Unit,
    onShowMessage: (String) -> Unit,
    savedProfile: UserAccountProfile? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isSignUpMode by rememberSaveable { mutableStateOf(false) }
    var displayName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isBusy by remember { mutableStateOf(false) }
    var inlineError by remember { mutableStateOf<String?>(null) }
    var fallbackReasonVi by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceCanvas)
            .statusBarsPadding()
            .displayCutoutPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("login_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 520.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Branded Master Ielts Hero Header Card
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AuthHeroBrush)
                        .padding(horizontal = 22.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val hasCustomSavedAvatar = savedProfile != null &&
                        (savedProfile.customAvatarPath.isNotBlank() || savedProfile.avatarPresetId.isNotBlank())
                    if (hasCustomSavedAvatar && savedProfile != null) {
                        UserAvatarCircle(
                            userProfile = savedProfile,
                            size = 68.dp,
                            borderColor = Color.White.copy(alpha = 0.85f),
                            borderWidth = 2.5.dp,
                            contentDescription = "Ảnh đại diện hồ sơ Master Ielts"
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.22f),
                            border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.4f))
                        ) {
                            Box(
                                modifier = Modifier.size(64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.School,
                                    contentDescription = "Master Ielts",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Master Ielts",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = InkOnHero
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.24f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "8.5",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.auth_brand_tagline),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkOnHeroMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Main Auth Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, CardBorderSoft),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Segmented Switcher: Sign In | Sign Up
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceCanvas,
                        border = BorderStroke(1.dp, CardBorderSoft),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            AuthModeTabButton(
                                label = stringResource(R.string.auth_tab_signin),
                                selected = !isSignUpMode,
                                onClick = {
                                    isSignUpMode = false
                                    inlineError = null
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("auth_mode_signin_tab")
                            )
                            AuthModeTabButton(
                                label = stringResource(R.string.auth_tab_signup),
                                selected = isSignUpMode,
                                onClick = {
                                    isSignUpMode = true
                                    inlineError = null
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("auth_mode_signup_tab")
                            )
                        }
                    }

                    // Google Sign-In Primary CTA
                    OutlinedButton(
                        onClick = {
                            if (isBusy) return@OutlinedButton
                            inlineError = null
                            isBusy = true
                            scope.launch {
                                when (val result = GoogleAuthHelper.signInWithGoogle(context)) {
                                    is GoogleSignInResult.Success -> {
                                        isBusy = false
                                        onGoogleSignInSuccess(
                                            result.uid,
                                            result.displayName,
                                            result.email,
                                            false
                                        )
                                    }

                                    is GoogleSignInResult.Cancelled -> {
                                        isBusy = false
                                        onShowMessage("Đã hủy đăng nhập Google.")
                                    }

                                    is GoogleSignInResult.NeedsOfflineFallback -> {
                                        isBusy = false
                                        fallbackReasonVi = result.reasonVi
                                    }
                                }
                            }
                        },
                        enabled = !isBusy,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, ElectricIndigo.copy(alpha = 0.45f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = ElectricIndigoLight.copy(alpha = 0.5f),
                            contentColor = IndigoOnSurface
                        ),
                        contentPadding = PaddingValues(vertical = 13.dp, horizontal = 14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("login_google_button")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(1.dp, CardBorderSoft)
                        ) {
                            Text(
                                text = "G",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = ElectricIndigo,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.auth_google_btn),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = IndigoOnSurface
                        )
                    }

                    // Clear Vietnamese explanation about Google Account -> Cloud Sync & AI
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = EmeraldSuccessLight,
                        border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = EmeraldOnContainer,
                                modifier = Modifier
                                    .padding(top = 1.dp)
                                    .size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.auth_google_note),
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldOnContainer
                            )
                        }
                    }

                    // Divider: hoặc dùng tài khoản Email
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = CardBorderSoft)
                        Text(
                            text = "hoặc Email & Mật khẩu",
                            style = MaterialTheme.typography.labelSmall,
                            color = InkMuted
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = CardBorderSoft)
                    }

                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = {
                                displayName = it
                                inlineError = null
                            },
                            label = { Text("Họ và tên (Display Name)") },
                            placeholder = { Text("VD: Minh Anh") },
                            leadingIcon = {
                                Icon(Icons.Rounded.Person, contentDescription = null, tint = IndigoOnSurface)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricIndigo,
                                unfocusedBorderColor = CardBorderSoft,
                                focusedContainerColor = SurfaceCanvas,
                                unfocusedContainerColor = SurfaceCanvas
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_name_input")
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            inlineError = null
                        },
                        label = { Text("Email") },
                        placeholder = { Text("ban@example.com") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Email, contentDescription = null, tint = IndigoOnSurface)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricIndigo,
                            unfocusedBorderColor = CardBorderSoft,
                            focusedContainerColor = SurfaceCanvas,
                            unfocusedContainerColor = SurfaceCanvas
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input")
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            inlineError = null
                        },
                        label = { Text("Mật khẩu (Password)") },
                        placeholder = { Text("Tối thiểu 6 ký tự") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = IndigoOnSurface)
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = "Hiện hoặc ẩn mật khẩu",
                                    tint = InkSecondary
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricIndigo,
                            unfocusedBorderColor = CardBorderSoft,
                            focusedContainerColor = SurfaceCanvas,
                            unfocusedContainerColor = SurfaceCanvas
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                inlineError = null
                            },
                            label = { Text("Xác nhận mật khẩu (Confirm Password)") },
                            leadingIcon = {
                                Icon(Icons.Rounded.Lock, contentDescription = null, tint = IndigoOnSurface)
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricIndigo,
                                unfocusedBorderColor = CardBorderSoft,
                                focusedContainerColor = SurfaceCanvas,
                                unfocusedContainerColor = SurfaceCanvas
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_confirm_password_input")
                        )
                    }

                    if (!inlineError.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = RoseDiffLight,
                            border = BorderStroke(1.dp, RoseAlert.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ErrorOutline,
                                    contentDescription = null,
                                    tint = RoseAlert,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = inlineError.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkPrimary
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (isBusy) return@Button
                            if (!LocalCryptoHelper.isValidEmail(email)) {
                                inlineError = "Vui lòng nhập địa chỉ Email hợp lệ."
                                return@Button
                            }
                            if (!LocalCryptoHelper.isValidPassword(password)) {
                                inlineError = "Mật khẩu phải có ít nhất 6 ký tự."
                                return@Button
                            }
                            if (isSignUpMode && password != confirmPassword) {
                                inlineError = "Mật khẩu xác nhận không khớp."
                                return@Button
                            }

                            isBusy = true
                            inlineError = null
                            if (isSignUpMode) {
                                onRegisterWithEmail(displayName, email, password) { res ->
                                    isBusy = false
                                    res.onFailure { err ->
                                        inlineError = err.message ?: "Không thể đăng ký tài khoản."
                                    }
                                }
                            } else {
                                onLoginWithEmail(email, password) { res ->
                                    isBusy = false
                                    res.onFailure { err ->
                                        inlineError = err.message ?: "Đăng nhập thất bại."
                                    }
                                }
                            }
                        },
                        enabled = !isBusy,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricIndigo,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("login_submit_button")
                    ) {
                        if (isBusy) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isSignUpMode) "Tạo tài khoản Master Ielts" else "Đăng nhập ngay",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Reassuring offline privacy footer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CloudDone,
                    contentDescription = null,
                    tint = InkMuted,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Phiên đăng nhập được lưu cục bộ an toàn (mật khẩu băm PBKDF2-SHA256) • Hỗ trợ học 100% Offline.",
                    style = MaterialTheme.typography.labelSmall,
                    color = InkMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (fallbackReasonVi != null) {
        GoogleFallbackSignInDialog(
            reasonVi = fallbackReasonVi.orEmpty(),
            initialEmail = email,
            initialName = displayName,
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
private fun AuthModeTabButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) ElectricIndigo else Color.Transparent,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (selected) Color.White else InkSecondary,
                maxLines = 1
            )
        }
    }
}

/**
 * Graceful offline / dev Google Sign-In profile dialog used when OAuth Web Client ID is absent
 * or Google Play Services are unreachable (e.g., offline emulator, sideloaded APK, Huawei).
 */
@Composable
fun GoogleFallbackSignInDialog(
    reasonVi: String,
    initialEmail: String = "",
    initialName: String = "",
    onConfirm: (displayName: String, email: String) -> Unit,
    onDismiss: () -> Unit
) {
    var draftName by remember { mutableStateOf(initialName.ifBlank { "IELTS Learner" }) }
    var draftEmail by remember { mutableStateOf(initialEmail.ifBlank { "learner@gmail.com" }) }
    var errText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardWhite,
        title = {
            Text(
                text = "Kết nối Google (Chế độ Offline / Dev)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = InkPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = reasonVi,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary
                )
                OutlinedTextField(
                    value = draftName,
                    onValueChange = {
                        draftName = it
                        errText = null
                    },
                    label = { Text("Tên hiển thị Google") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = draftEmail,
                    onValueChange = {
                        draftEmail = it
                        errText = null
                    },
                    label = { Text("Email tài khoản Google") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (errText != null) {
                    Text(
                        text = errText.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = RoseAlert
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanEmail = LocalCryptoHelper.normalizeEmail(draftEmail)
                    if (!LocalCryptoHelper.isValidEmail(cleanEmail)) {
                        errText = "Vui lòng nhập email hợp lệ."
                    } else {
                        onConfirm(draftName.trim().ifBlank { cleanEmail.substringBefore("@") }, cleanEmail)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo, contentColor = Color.White),
                modifier = Modifier.testTag("google_fallback_confirm_btn")
            ) {
                Text("Tiếp tục với Google")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = InkSecondary)
            }
        }
    )
}

/**
 * Fullscreen 4-Digit PIN & Optional Biometric App Lock Gate.
 */
@Composable
fun AppLockPinScreen(
    userProfile: UserAccountProfile,
    biometricEnabled: Boolean,
    onVerifyPin: (pin: String, onResult: (Boolean) -> Unit) -> Unit,
    onBiometricUnlocked: () -> Unit,
    onLogoutFallback: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var enteredPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val canUseBiometric = remember(biometricEnabled) {
        biometricEnabled && BiometricUnlockHelper.isBiometricHardwareAvailable(context)
    }

    LaunchedEffect(canUseBiometric) {
        if (canUseBiometric) {
            BiometricUnlockHelper.authenticate(
                context = context,
                title = "Mở khóa Master Ielts",
                subtitle = "Xác thực sinh trắc học cho ${userProfile.displayName.ifBlank { userProfile.email }}",
                negativeButtonText = "Dùng mã PIN",
                onSuccess = onBiometricUnlocked,
                onError = { msg -> errorMsg = msg }
            )
        }
    }

    fun appendDigit(digit: String) {
        if (enteredPin.length >= 4) return
        errorMsg = null
        val updated = enteredPin + digit
        enteredPin = updated
        if (updated.length == 4) {
            onVerifyPin(updated) { valid ->
                if (!valid) {
                    enteredPin = ""
                    errorMsg = "Mã PIN chưa chính xác. Vui lòng thử lại."
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceCanvas)
            .statusBarsPadding()
            .displayCutoutPadding()
            .navigationBarsPadding()
            .testTag("applock_pin_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                UserAvatarCircle(
                    userProfile = userProfile,
                    size = 76.dp,
                    borderColor = ElectricIndigo.copy(alpha = 0.55f),
                    borderWidth = 2.5.dp,
                    contentDescription = "Hồ sơ đang khóa"
                )
                Surface(
                    shape = CircleShape,
                    color = ElectricIndigo,
                    border = BorderStroke(2.dp, CardWhite),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.applock_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = InkPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.applock_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // 4 PIN Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { idx ->
                    val filled = idx < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (filled) ElectricIndigo else CardWhite)
                            .border(
                                width = 2.dp,
                                color = if (filled) ElectricIndigo else CardBorderSoft,
                                shape = CircleShape
                            )
                    )
                }
            }

            if (errorMsg != null) {
                Text(
                    text = errorMsg.orEmpty(),
                    style = MaterialTheme.typography.labelMedium,
                    color = RoseAlert,
                    textAlign = TextAlign.Center
                )
            }

            // Numeric Keypad
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9")
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rows.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { digit ->
                            PinKeyButton(
                                label = digit,
                                onClick = { appendDigit(digit) }
                            )
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (canUseBiometric) {
                        Surface(
                            shape = CircleShape,
                            color = ElectricIndigoLight,
                            modifier = Modifier
                                .size(66.dp)
                                .clip(CircleShape)
                                .clickable {
                                    BiometricUnlockHelper.authenticate(
                                        context = context,
                                        title = "Mở khóa Master Ielts",
                                        subtitle = "Dùng vân tay hoặc khuôn mặt để mở khóa",
                                        negativeButtonText = "Hủy",
                                        onSuccess = onBiometricUnlocked,
                                        onError = { msg -> errorMsg = msg }
                                    )
                                }
                                .testTag("applock_biometric_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Fingerprint,
                                    contentDescription = "Mở khóa bằng sinh trắc học",
                                    tint = IndigoOnSurface,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.size(66.dp))
                    }

                    PinKeyButton(
                        label = "0",
                        onClick = { appendDigit("0") }
                    )

                    Surface(
                        shape = CircleShape,
                        color = SurfaceCanvas,
                        border = BorderStroke(1.dp, CardBorderSoft),
                        modifier = Modifier
                            .size(66.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (enteredPin.isNotEmpty()) {
                                    enteredPin = enteredPin.dropLast(1)
                                }
                            }
                            .testTag("applock_backspace_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                contentDescription = "Xóa chữ số PIN",
                                tint = InkSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            TextButton(
                onClick = onLogoutFallback,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = null,
                    tint = InkMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Quên mã PIN? Đăng xuất tài khoản",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkMuted
                )
            }
        }
    }
}

@Composable
private fun PinKeyButton(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = CardWhite,
        border = BorderStroke(1.dp, CardBorderSoft),
        shadowElevation = 1.dp,
        modifier = Modifier
            .size(66.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag("pin_key_$label")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = InkPrimary
            )
        }
    }
}
