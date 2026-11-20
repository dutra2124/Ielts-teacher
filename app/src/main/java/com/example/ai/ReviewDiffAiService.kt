package com.example.ai

import android.util.Log
import com.example.data.UpgradeDiffChange
import com.example.data.UpgradeDiffComparison
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

private const val TAG = "ReviewDiffAiService"

sealed interface ReviewDiffAiUiState {
    data object Idle : ReviewDiffAiUiState
    data object Loading : ReviewDiffAiUiState
    data class Success(val comparison: UpgradeDiffComparison) : ReviewDiffAiUiState
    data class Error(val messageVi: String) : ReviewDiffAiUiState
}

/**
 * Generates an interactive Original vs. Band 8.5+ Diff Upgrade using Firebase AI Logic
 * (gemini-3.5-flash-lite) preserving the learner's own outline while isolating exact weak
 * phrases (underlined in Rose #E11D48) and upgraded academic replacements (Emerald #10B981).
 */
class ReviewDiffAiService {

    private val diffSchema = Schema.obj(
        properties = mapOf(
            "upgradedText" to Schema.string(
                description = "Cohesive Band 8.5+ upgraded version preserving the learner's exact outline and ideas"
            ),
            "changes" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "weakPhraseEn" to Schema.string(
                            description = "Exact verbatim weak phrase copied from the user's original response"
                        ),
                        "upgradedPhraseEn" to Schema.string(
                            description = "Exact verbatim Band 8.5+ phrase inside upgradedText that replaces weakPhraseEn"
                        ),
                        "categoryVi" to Schema.string(
                            description = "Short IELTS criterion label e.g. 'Lexical Resource (C2)' or 'Grammatical Range'"
                        ),
                        "explanationVi" to Schema.string(
                            description = "Concise Vietnamese examiner explanation (1-2 sentences) of why this upgrade boosts the band score"
                        )
                    )
                ),
                description = "3 to 4 key phrase-level upgrades linking original text to upgradedText"
            )
        )
    )

    private val diffModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = generationConfig {
                temperature = 0.30f
                maxOutputTokens = 950
                responseMimeType = "application/json"
                responseSchema = diffSchema
            }
        )
    }

    suspend fun generateBand85Diff(
        skillLabel: String,
        promptSummaryEn: String,
        originalText: String
    ): UpgradeDiffComparison = withContext(Dispatchers.IO) {
        val prompt = """
            Role: Senior IELTS Band 9 Examiner editing a Vietnamese candidate's $skillLabel response.
            Prompt: "$promptSummaryEn"
            Candidate's Original Response: "$originalText"

            Task:
            1. Rewrite the response into a cohesive Band 8.5+ version while strictly keeping the candidate's original ideas and outline.
            2. Return 3-4 exact phrase-level changes:
               - weakPhraseEn MUST appear verbatim inside the Original Response.
               - upgradedPhraseEn MUST appear verbatim inside your upgradedText.
               - Write categoryVi and explanationVi in clear, actionable Vietnamese.
        """.trimIndent()

        try {
            val response = withTimeout(60_000L) {
                diffModel.generateContent(prompt)
            }
            val raw = response.text
                ?: throw IllegalStateException("AI không trả về bản so sánh nâng cấp.")
            val json = JSONObject(raw)
            val upgradedText = json.optString("upgradedText", "").trim()
            val changesArr = json.optJSONArray("changes")
            val changes = buildList {
                if (changesArr != null) {
                    for (i in 0 until changesArr.length()) {
                        val c = changesArr.optJSONObject(i) ?: continue
                        val weak = c.optString("weakPhraseEn").trim()
                        val up = c.optString("upgradedPhraseEn").trim()
                        if (weak.isNotEmpty() && up.isNotEmpty()) {
                            add(
                                UpgradeDiffChange(
                                    id = "ai_diff_${System.currentTimeMillis()}_$i",
                                    weakPhraseEn = weak,
                                    upgradedPhraseEn = up,
                                    categoryVi = c.optString("categoryVi", "Band 8.5+ Upgrade"),
                                    explanationVi = c.optString("explanationVi", "")
                                )
                            )
                        }
                    }
                }
            }

            UpgradeDiffComparison(
                id = "ai_comp_${System.currentTimeMillis()}",
                skillLabel = "$skillLabel • Gemini AI Diff",
                promptSummaryEn = promptSummaryEn,
                originalText = originalText,
                upgradedText = upgradedText.ifBlank { originalText },
                changes = changes,
                isAiGenerated = true
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Review diff generation timed out", e)
            throw IllegalStateException("Tạo bản so sánh nâng cấp quá 60 giây. Vui lòng thử lại.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Review diff generation failed", e)
            throw IllegalStateException(
                e.localizedMessage?.takeIf { it.isNotBlank() }
                    ?: "Không thể tạo bản so sánh Band 8.5+ lúc này. Vui lòng thử lại."
            )
        }
    }
}
