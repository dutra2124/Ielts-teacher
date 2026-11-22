package com.example.auth

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.util.Base64
import android.util.Patterns
import androidx.core.content.ContextCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

enum class AuthMethod(val id: String) {
    NONE("none"),
    LOCAL_EMAIL("local_email"),
    GOOGLE("google");

    companion object {
        fun fromId(raw: String?): AuthMethod =
            entries.firstOrNull { it.id == raw } ?: NONE
    }
}

data class UserAccountProfile(
    val isLoggedIn: Boolean = false,
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val authMethod: AuthMethod = AuthMethod.NONE,
    val isGoogleConnected: Boolean = false,
    val isGoogleFallbackMode: Boolean = false,
    val createdAtEpochMs: Long = 0L,
    val avatarPresetId: String = "",
    val customAvatarPath: String = "",
    val avatarVersion: Long = 0L
) {
    val initials: String
        get() {
            val source = displayName.trim().ifBlank { email.substringBefore("@").trim() }
            if (source.isBlank()) return "MI"
            val parts = source.split(Regex("\\s+")).filter { it.isNotBlank() }
            return when {
                parts.size >= 2 -> "${parts.first().first()}${parts.last().first()}".uppercase(Locale.US)
                else -> source.take(2).uppercase(Locale.US)
            }
        }
}

data class SecurityPreferences(
    val appLockEnabled: Boolean = false,
    val hasPinConfigured: Boolean = false,
    val biometricEnabled: Boolean = false
)

data class LocalRegisteredAccount(
    val email: String,
    val displayName: String,
    val saltB64: String,
    val passwordHashB64: String,
    val createdAtEpochMs: Long
)

/**
 * Cryptographic & validation utilities for local account passwords and 4-digit App Lock PINs.
 * Uses PBKDF2WithHmacSHA256 with per-secret 128-bit random salt and constant-time comparison.
 * Plaintext passwords and PINs are never persisted or logged.
 */
object LocalCryptoHelper {
    private const val ITERATIONS = 65_536
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16
    private val secureRandom = SecureRandom()

    fun normalizeEmail(raw: String): String = raw.trim().lowercase(Locale.US)

    fun isValidEmail(raw: String): Boolean {
        val cleaned = normalizeEmail(raw)
        return cleaned.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(cleaned).matches()
    }

    fun isValidPassword(raw: String): Boolean = raw.length >= 6

    fun isValidPin(raw: String): Boolean = raw.length == 4 && raw.all { it.isDigit() }

    fun hashSecret(secret: String): Pair<String, String> {
        val salt = ByteArray(SALT_BYTES).also { secureRandom.nextBytes(it) }
        val derived = deriveKey(secret, salt)
        val saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hashB64 = Base64.encodeToString(derived, Base64.NO_WRAP)
        return saltB64 to hashB64
    }

    fun verifySecret(secret: String, saltB64: String, expectedHashB64: String): Boolean {
        if (saltB64.isBlank() || expectedHashB64.isBlank()) return false
        return runCatching {
            val salt = Base64.decode(saltB64, Base64.NO_WRAP)
            val expected = Base64.decode(expectedHashB64, Base64.NO_WRAP)
            val actual = deriveKey(secret, salt)
            MessageDigest.isEqual(actual, expected)
        }.getOrDefault(false)
    }

    private fun deriveKey(secret: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(secret.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        return try {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            factory.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    fun serializeAccounts(accounts: List<LocalRegisteredAccount>): String {
        val arr = JSONArray()
        accounts.forEach { acc ->
            val obj = JSONObject()
                .put("email", acc.email)
                .put("displayName", acc.displayName)
                .put("saltB64", acc.saltB64)
                .put("passwordHashB64", acc.passwordHashB64)
                .put("createdAtEpochMs", acc.createdAtEpochMs)
            arr.put(obj)
        }
        return arr.toString()
    }

    fun deserializeAccounts(raw: String?): List<LocalRegisteredAccount> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val email = obj.optString("email", "")
                    val salt = obj.optString("saltB64", "")
                    val hash = obj.optString("passwordHashB64", "")
                    if (email.isNotBlank() && salt.isNotBlank() && hash.isNotBlank()) {
                        add(
                            LocalRegisteredAccount(
                                email = email,
                                displayName = obj.optString("displayName", email.substringBefore("@")),
                                saltB64 = salt,
                                passwordHashB64 = hash,
                                createdAtEpochMs = obj.optLong("createdAtEpochMs", 0L)
                            )
                        )
                    }
                }
            }
        }.getOrDefault(emptyList())
    }
}

sealed interface GoogleSignInResult {
    data class Success(
        val uid: String,
        val displayName: String,
        val email: String
    ) : GoogleSignInResult

    data object Cancelled : GoogleSignInResult

    data class NeedsOfflineFallback(
        val reasonVi: String
    ) : GoogleSignInResult
}

/**
 * Interactive Google Sign-In via Android Jetpack Credential Manager + Firebase Auth Google Provider.
 * Gracefully signals offline/dev fallback when OAuth Web Client ID is absent, Play Services are
 * unavailable (e.g., sideloaded APK / Huawei), or network is offline so testing is never blocked.
 */
object GoogleAuthHelper {

    fun resolveWebClientId(context: Context): String {
        return runCatching {
            val resId = context.resources.getIdentifier(
                "default_web_client_id",
                "string",
                context.packageName
            )
            if (resId != 0) context.getString(resId).trim() else ""
        }.getOrDefault("")
    }

    suspend fun signInWithGoogle(activityContext: Context): GoogleSignInResult {
        val webClientId = resolveWebClientId(activityContext)
        if (webClientId.isBlank()) {
            return GoogleSignInResult.NeedsOfflineFallback(
                reasonVi = "Thiết bị hoặc bản build hiện tại chưa cấu hình OAuth Web Client ID. Bạn có thể đăng nhập nhanh bằng hồ sơ Google Offline/Dev để trải nghiệm đầy đủ Master Ielts."
            )
        }

        val credentialManager = CredentialManager.create(activityContext)
        val googleOption = GetSignInWithGoogleOption.Builder(serverClientId = webClientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()

        return try {
            val response = credentialManager.getCredential(
                context = activityContext,
                request = request
            )
            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(
                    googleIdTokenCredential.idToken,
                    null
                )
                val authResult = Firebase.auth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user
                GoogleSignInResult.Success(
                    uid = user?.uid ?: "google_${googleIdTokenCredential.id.hashCode()}",
                    displayName = user?.displayName
                        ?: googleIdTokenCredential.displayName
                        ?: googleIdTokenCredential.id.substringBefore("@"),
                    email = user?.email ?: googleIdTokenCredential.id
                )
            } else {
                GoogleSignInResult.NeedsOfflineFallback(
                    reasonVi = "Không nhận được Google ID Token hợp lệ. Chuyển sang chế độ hồ sơ Google dự phòng?"
                )
            }
        } catch (_: GetCredentialCancellationException) {
            GoogleSignInResult.Cancelled
        } catch (e: CancellationException) {
            throw e
        } catch (_: GetCredentialException) {
            GoogleSignInResult.NeedsOfflineFallback(
                reasonVi = "Không thể kết nối Google Play Services hoặc đang ngoại tuyến. Bạn vẫn có thể tiếp tục với hồ sơ Google Offline/Dev."
            )
        } catch (_: Exception) {
            GoogleSignInResult.NeedsOfflineFallback(
                reasonVi = "Dịch vụ Google Sign-In tạm thời không khả dụng trên thiết bị này. Dùng hồ sơ Google Offline/Dev để tiếp tục học ngay."
            )
        }
    }

    suspend fun signOutGoogle(context: Context) {
        withContext(NonCancellable) {
            runCatching { Firebase.auth.signOut() }
            runCatching {
                CredentialManager.create(context)
                    .clearCredentialState(ClearCredentialStateRequest())
            }
        }
    }
}

/**
 * Zero-dependency framework BiometricPrompt helper (API 28+) with automatic PIN fallback.
 */
object BiometricUnlockHelper {

    fun isBiometricHardwareAvailable(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        val pm = context.packageManager
        return pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                (pm.hasSystemFeature(PackageManager.FEATURE_FACE) ||
                    pm.hasSystemFeature(PackageManager.FEATURE_IRIS)))
    }

    fun authenticate(
        context: Context,
        title: String,
        subtitle: String,
        negativeButtonText: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            onError("Thiết bị chưa hỗ trợ mở khóa sinh trắc học. Vui lòng nhập mã PIN 4 số.")
            return
        }
        runCatching {
            val executor = ContextCompat.getMainExecutor(context)
            val prompt = BiometricPrompt.Builder(context)
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButton(negativeButtonText, executor) { _, _ ->
                    onError("Đã chuyển sang nhập mã PIN 4 số.")
                }
                .build()

            prompt.authenticate(
                CancellationSignal(),
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                        onSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                        onError(errString?.toString().orEmpty().ifBlank { "Không thể xác thực sinh trắc học." })
                    }

                    override fun onAuthenticationFailed() {
                        onError("Sinh trắc học chưa khớp. Thử lại hoặc dùng mã PIN 4 số.")
                    }
                }
            )
        }.onFailure {
            onError("Không thể khởi chạy sinh trắc học trên thiết bị này. Vui lòng nhập mã PIN.")
        }
    }
}
