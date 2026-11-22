package com.example.ai

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val TAG = "GeminiResilientClient"
private const val DEFAULT_GEMINI_MODEL = "gemini-3.5-flash-lite"

/**
 * Resilient Gemini execution helper for Master Ielts:
 * 1. Primary path: Firebase AI Logic (`Firebase.ai.generativeModel(...)`) without limited-use token lock.
 * 2. Automatic Sideload Fallback: If App Check attestation fails on sideloaded APKs or non-Play devices
 *    (e.g., Huawei P30 Pro or manual APK installs) and the user has stored a personal Gemini API key
 *    in Settings -> Bảo mật, smoothly falls back to the Gemini REST API via OkHttp.
 * 3. Clear Vietnamese guidance: If App Check fails and no personal fallback key is configured yet,
 *    surfaces an actionable Vietnamese explanation instead of an unhandled technical error.
 */
object GeminiResilientClient {

    @Volatile
    var personalGeminiApiKey: String = ""

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(55, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .build()
    }

    suspend fun generateJsonWithFallback(
        primaryCall: suspend () -> String?,
        prompt: String,
        jsonSchemaHint: String,
        pdfBytes: ByteArray? = null,
        temperature: Float = 0.3f,
        maxOutputTokens: Int = 1024,
        timeoutMs: Long = 60_000L,
        operationLabelVi: String = "xử lý AI"
    ): String = withContext(Dispatchers.IO) {
        try {
            val primaryText = withTimeout(timeoutMs) {
                primaryCall()
            }
            if (!primaryText.isNullOrBlank()) {
                return@withContext cleanJsonPayload(primaryText)
            }
            throw IllegalStateException("Mô hình AI không trả về dữ liệu cho tác vụ $operationLabelVi.")
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Primary Firebase AI timed out during $operationLabelVi", e)
            val fallbackKey = personalGeminiApiKey.trim()
            if (fallbackKey.isNotEmpty()) {
                return@withContext runRestFallback(
                    apiKey = fallbackKey,
                    prompt = prompt,
                    jsonSchemaHint = jsonSchemaHint,
                    pdfBytes = pdfBytes,
                    temperature = temperature,
                    maxOutputTokens = maxOutputTokens,
                    timeoutMs = timeoutMs,
                    operationLabelVi = operationLabelVi
                )
            }
            throw IllegalStateException(
                "Kết nối Gemini AI phản hồi quá ${timeoutMs / 1000} giây khi $operationLabelVi. Vui lòng kiểm tra mạng và chạm Thử lại."
            )
        } catch (e: CancellationException) {
            throw e
        } catch (primaryError: Exception) {
            Log.e(TAG, "Primary Firebase AI call failed during $operationLabelVi", primaryError)

            val fallbackKey = personalGeminiApiKey.trim()
            if (fallbackKey.isNotEmpty()) {
                Log.w(TAG, "Switching to personal Gemini REST API fallback for $operationLabelVi.")
                return@withContext runRestFallback(
                    apiKey = fallbackKey,
                    prompt = prompt,
                    jsonSchemaHint = jsonSchemaHint,
                    pdfBytes = pdfBytes,
                    temperature = temperature,
                    maxOutputTokens = maxOutputTokens,
                    timeoutMs = timeoutMs,
                    operationLabelVi = operationLabelVi
                )
            }

            if (isAppCheckOrAttestationFailure(primaryError)) {
                throw IllegalStateException(
                    "Thiết bị cài APK trực tiếp (ngoài Google Play) chưa vượt qua xác thực Firebase App Check. " +
                        "Hãy mở Cài đặt (⚙) → Bảo mật & Firebase App Check để sao chép Debug Token đăng ký 1 lần trên Firebase Console, " +
                        "hoặc nhập Gemini API Key cá nhân để dùng AI dự phòng ngay."
                )
            }

            val rawDetail = primaryError.localizedMessage?.takeIf { it.isNotBlank() }
            throw IllegalStateException(
                if (rawDetail != null) {
                    "Không thể $operationLabelVi qua Gemini AI ($rawDetail). Bạn có thể vào Cài đặt (⚙) → Bảo mật để kiểm tra App Check hoặc bật Gemini API Key dự phòng."
                } else {
                    "Không thể kết nối Gemini AI để $operationLabelVi. Vui lòng kiểm tra mạng và thử lại."
                }
            )
        }
    }

    private suspend fun runRestFallback(
        apiKey: String,
        prompt: String,
        jsonSchemaHint: String,
        pdfBytes: ByteArray?,
        temperature: Float,
        maxOutputTokens: Int,
        timeoutMs: Long,
        operationLabelVi: String
    ): String {
        val fullPrompt = buildString {
            append(prompt.trim())
            append("\n\nCRITICAL OUTPUT FORMAT: Return ONLY valid raw JSON (no markdown fences) matching this exact structure:\n")
            append(jsonSchemaHint.trim())
        }

        val partsArray = JSONArray().apply {
            if (pdfBytes != null) {
                put(
                    JSONObject().put(
                        "inline_data",
                        JSONObject()
                            .put("mime_type", "application/pdf")
                            .put("data", Base64.encodeToString(pdfBytes, Base64.NO_WRAP))
                    )
                )
            }
            put(JSONObject().put("text", fullPrompt))
        }

        val bodyJson = JSONObject().apply {
            put(
                "contents",
                JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put("parts", partsArray)
                )
            )
            put(
                "generationConfig",
                JSONObject()
                    .put("temperature", temperature.toDouble())
                    .put("maxOutputTokens", maxOutputTokens)
                    .put("responseMimeType", "application/json")
            )
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$DEFAULT_GEMINI_MODEL:generateContent"
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("x-goog-api-key", apiKey)
            .post(bodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        return try {
            withTimeout(timeoutMs) {
                httpClient.newCall(request).execute().use { response ->
                    val rawBody = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        val errMsg = runCatching {
                            JSONObject(rawBody).optJSONObject("error")?.optString("message")
                        }.getOrNull()
                        throw IllegalStateException(
                            "Gemini API dự phòng trả về lỗi HTTP ${response.code}${if (!errMsg.isNullOrBlank()) ": $errMsg" else ""}. " +
                                "Hãy kiểm tra lại Gemini API Key cá nhân trong Cài đặt (⚙) → Bảo mật."
                        )
                    }
                    val root = JSONObject(rawBody)
                    val text = root
                        .optJSONArray("candidates")
                        ?.optJSONObject(0)
                        ?.optJSONObject("content")
                        ?.optJSONArray("parts")
                        ?.optJSONObject(0)
                        ?.optString("text")
                        .orEmpty()
                    if (text.isBlank()) {
                        throw IllegalStateException("Gemini API dự phòng trả về dữ liệu trống khi $operationLabelVi.")
                    }
                    cleanJsonPayload(text)
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini REST fallback timed out during $operationLabelVi", e)
            throw IllegalStateException("Gemini API dự phòng phản hồi quá ${timeoutMs / 1000} giây. Vui lòng thử lại.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini REST fallback failed during $operationLabelVi", e)
            throw IllegalStateException(
                e.localizedMessage?.takeIf { it.isNotBlank() }
                    ?: "Không thể kết nối Gemini API dự phòng. Vui lòng kiểm tra mạng hoặc API Key trong Cài đặt."
            )
        }
    }

    private fun isAppCheckOrAttestationFailure(error: Throwable): Boolean {
        val chain = generateSequence(error) { it.cause }
            .mapNotNull { it.message ?: it.localizedMessage }
            .joinToString(" | ")
            .lowercase()

        return chain.contains("app check") ||
            chain.contains("appcheck") ||
            chain.contains("403") ||
            chain.contains("forbidden") ||
            chain.contains("attestation") ||
            chain.contains("play integrity") ||
            chain.contains("recaptcha") ||
            chain.contains("debug token") ||
            chain.contains("firebaseappcheck") ||
            (AppCheckStatusStore.diagnostics.value.isSideloadedOrDebug &&
                (chain.contains("unauthorized") || chain.contains("permission_denied") || chain.contains("token")))
    }

    private fun cleanJsonPayload(raw: String): String {
        val trimmed = raw.trim()
        if (!trimmed.startsWith("```")) return trimmed
        return trimmed
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }
}
