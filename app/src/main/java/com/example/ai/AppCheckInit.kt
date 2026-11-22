package com.example.ai

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

private const val TAG = "AppCheckInit"

private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"
private const val PLAY_STORE_PACKAGE = "com.android.vending"

data class AppCheckDiagnostics(
    val providerModeLabelVi: String = "Đang khởi tạo App Check...",
    val installSourceLabelVi: String = "Đang kiểm tra nguồn cài đặt",
    val debugToken: String? = null,
    val isSideloadedOrDebug: Boolean = true,
    val isInitialized: Boolean = false
)

object AppCheckStatusStore {
    private val _diagnostics = MutableStateFlow(AppCheckDiagnostics())
    val diagnostics: StateFlow<AppCheckDiagnostics> = _diagnostics.asStateFlow()

    internal fun update(snapshot: AppCheckDiagnostics) {
        _diagnostics.value = snapshot
    }
}

/**
 * Configures Firebase App Check before Compose UI, ViewModels, or Firebase operations.
 *
 * Environment-aware setup for both Play Store and sideloaded APKs (e.g., manual APK installs
 * or Huawei devices without Play Integrity):
 * - Uses DebugAppCheckProviderFactory for debug builds, sideloaded/non-Play-Store installs,
 *   or when no reCAPTCHA Enterprise site key is configured.
 * - Uses synchronous SharedPreferences.commit() so DebugAppCheckProviderFactory immediately
 *   reads the exact active debug token without race conditions.
 * - Exposes the active Debug Token via [AppCheckStatusStore] so users can view and copy it
 *   under Settings -> Bảo mật & Firebase App Check.
 */
fun configureAppCheck(context: Context, intent: Intent?) {
    try {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }

        val installerPackage = detectInstallerPackage(context)
        val isPlayStoreInstall = installerPackage == PLAY_STORE_PACKAGE
        val isSideloadedOrDebug = BuildConfig.DEBUG || !isPlayStoreInstall

        val installSourceLabelVi = when {
            BuildConfig.DEBUG -> "Bản Debug / Kiểm thử"
            isPlayStoreInstall -> "Google Play Store"
            installerPackage.isNullOrBlank() -> "Cài trực tiếp APK (Sideload)"
            else -> "Ngoài Google Play ($installerPackage)"
        }

        val siteKey = resolveRecaptchaSiteKey(context)

        if (isSideloadedOrDebug || siteKey.isNullOrBlank()) {
            val activeDebugToken = ensureDebugSecretPersisted(context, intent)

            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Log.d(
                TAG,
                "DebugAppCheckProviderFactory installed (debug=${BuildConfig.DEBUG}, installer=$installerPackage)."
            )

            AppCheckStatusStore.update(
                AppCheckDiagnostics(
                    providerModeLabelVi = "Debug App Check (Hỗ trợ Sideload APK)",
                    installSourceLabelVi = installSourceLabelVi,
                    debugToken = activeDebugToken,
                    isSideloadedOrDebug = true,
                    isInitialized = true
                )
            )
        } else {
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                RecaptchaAppCheckProviderFactory.getInstance(siteKey)
            )
            Log.d(TAG, "RecaptchaAppCheckProviderFactory installed for Play Store build.")

            AppCheckStatusStore.update(
                AppCheckDiagnostics(
                    providerModeLabelVi = "reCAPTCHA Enterprise (Play Store)",
                    installSourceLabelVi = installSourceLabelVi,
                    debugToken = null,
                    isSideloadedOrDebug = false,
                    isInitialized = true
                )
            )
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize Firebase App Check", e)
        AppCheckStatusStore.update(
            AppCheckDiagnostics(
                providerModeLabelVi = "Chưa thể khởi tạo App Check (${e.localizedMessage ?: "lỗi cấu hình"})",
                installSourceLabelVi = "Chế độ dự phòng",
                debugToken = readStoredDebugSecretOrNull(context),
                isSideloadedOrDebug = true,
                isInitialized = false
            )
        )
    }
}

private fun ensureDebugSecretPersisted(context: Context, intent: Intent?): String? {
    val persistenceKey = runCatching { FirebaseApp.getInstance().persistenceKey }.getOrNull()
        ?: return null
    val prefs = context.getSharedPreferences(
        "$DEBUG_STORE_PREFIX$persistenceKey",
        Context.MODE_PRIVATE
    )

    val intentDebugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)?.trim()
    if (!intentDebugToken.isNullOrEmpty()) {
        // CRITICAL: MUST use synchronous commit() before installing DebugAppCheckProviderFactory
        prefs.edit()
            .putString(DEBUG_SECRET_KEY, intentDebugToken)
            .commit()
        Log.d(TAG, "App Check debug secret injected from launch Intent.")
        return intentDebugToken
    }

    val existingToken = prefs.getString(DEBUG_SECRET_KEY, null)?.trim()
    if (!existingToken.isNullOrEmpty()) {
        Log.d(TAG, "Using persisted App Check debug secret.")
        return existingToken
    }

    // Generate & commit a stable debug token so sideloaded APK users can view/copy & register it once
    val generatedToken = UUID.randomUUID().toString()
    prefs.edit()
        .putString(DEBUG_SECRET_KEY, generatedToken)
        .commit()
    Log.d(TAG, "Generated and persisted stable App Check debug secret for sideloaded install.")
    return generatedToken
}

private fun readStoredDebugSecretOrNull(context: Context): String? {
    val persistenceKey = runCatching { FirebaseApp.getInstance().persistenceKey }.getOrNull()
        ?: return null
    val prefs = context.getSharedPreferences(
        "$DEBUG_STORE_PREFIX$persistenceKey",
        Context.MODE_PRIVATE
    )
    return prefs.getString(DEBUG_SECRET_KEY, null)?.trim()?.takeIf { it.isNotEmpty() }
}

private fun resolveRecaptchaSiteKey(context: Context): String? {
    val resId = context.resources.getIdentifier(
        RECAPTCHA_SITE_KEY_RES,
        "string",
        context.packageName
    )
    if (resId == 0) return null
    return runCatching { context.getString(resId).trim() }
        .getOrNull()
        ?.takeIf { it.isNotBlank() }
}

@Suppress("DEPRECATION")
private fun detectInstallerPackage(context: Context): String? {
    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
        } else {
            context.packageManager.getInstallerPackageName(context.packageName)
        }
    }.getOrNull()
}
