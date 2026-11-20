package com.example.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "SentenceCoachAiService"

data class AiSentenceEvaluation(
    val estimatedBand: String,
    val naturalnessScore: Int,
    val naturalnessFeedbackVi: String,
    val correctionsVi: List<String>,
    val upgradedBand85Sentence: String,
    val upgradeHighlightVi: String
)

sealed interface SentenceAiUiState {
    data object Idle : SentenceAiUiState
    data object Loading : SentenceAiUiState
    data class Success(val result: AiSentenceEvaluation) : SentenceAiUiState
    data class Error(val messageVi: String) : SentenceAiUiState
}

/**
 * Fast structured IELTS sentence evaluator via Firebase AI Logic (gemini-3.5-flash-lite).
 * Uses responseMimeType = "application/json" + Schema.obj, Dispatchers.IO, and 60s timeout.
 */
class SentenceCoachAiService {

    private val evaluationSchema = Schema.obj(
        properties = mapOf(
            "estimatedBand" to Schema.string(
                description = "Estimated IELTS band, e.g. '6.0', '6.5', '7.0', '7.5', '8.0', '8.5'"
            ),
            "naturalnessScore" to Schema.integer(
                description = "Naturalness score 0 to 100"
            ),
            "naturalnessFeedbackVi" to Schema.string(
                description = "Short encouraging evaluation in Vietnamese (1-2 sentences)"
            ),
            "correctionsVi" to Schema.array(
                items = Schema.string(),
                description = "2 concise collocation or grammar corrections in Vietnamese"
            ),
            "upgradedBand85Sentence" to Schema.string(
                description = "Polished Band 8.5+ academic English sentence using the target word while keeping original meaning"
            ),
            "upgradeHighlightVi" to Schema.string(
                description = "1 short Vietnamese sentence explaining why the upgraded version reaches Band 8.5+"
            )
        )
    )

    private val fastModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = generationConfig {
                temperature = 0.35f
                maxOutputTokens = 650
                responseMimeType = "application/json"
                responseSchema = evaluationSchema
            }
        )
    }

    suspend fun evaluateSentence(
        targetWord: String,
        moduleTopicEn: String,
        userSentence: String
    ): AiSentenceEvaluation = withContext(Dispatchers.IO) {
        val prompt = """
            Role: IELTS Band 9 Examiner coaching a Vietnamese learner (Band 6.0 -> 8.5).
            Topic: $moduleTopicEn
            Target Word: "$targetWord"
            Sentence: "$userSentence"

            Evaluate usage of "$targetWord" concisely. Write feedback and corrections in Vietnamese. Rewrite into a Band 8.5+ academic English sentence preserving the exact original meaning.
        """.trimIndent()

        try {
            Log.d(TAG, "Starting Gemini evaluation for word='$targetWord'")
            val response = withTimeout(60_000L) {
                fastModel.generateContent(prompt)
            }
            val jsonString = response.text
                ?: throw IllegalStateException("Mô hình AI không trả về dữ liệu. Vui lòng thử lại.")

            val json = JSONObject(jsonString)
            val correctionsJson = json.optJSONArray("correctionsVi")
            val corrections = buildList {
                if (correctionsJson != null) {
                    for (i in 0 until correctionsJson.length()) {
                        val item = correctionsJson.optString(i).trim()
                        if (item.isNotEmpty()) add(item)
                    }
                }
            }

            AiSentenceEvaluation(
                estimatedBand = json.optString("estimatedBand", "6.5"),
                naturalnessScore = json.optInt("naturalnessScore", 72).coerceIn(0, 100),
                naturalnessFeedbackVi = json.optString("naturalnessFeedbackVi", ""),
                correctionsVi = corrections,
                upgradedBand85Sentence = json.optString("upgradedBand85Sentence", ""),
                upgradeHighlightVi = json.optString("upgradeHighlightVi", "")
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini evaluation timed out after 60s", e)
            throw IllegalStateException(
                "Kết nối Gemini AI phản hồi chậm quá 60 giây. Vui lòng kiểm tra mạng và chạm Thử lại."
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI Logic evaluation failed", e)
            throw IllegalStateException(
                e.localizedMessage?.takeIf { it.isNotBlank() }
                    ?: "Không thể kết nối Gemini AI. Vui lòng kiểm tra mạng và thử lại."
            )
        }
    }
}
