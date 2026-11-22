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

        val raw = GeminiResilientClient.generateJsonWithFallback(
            primaryCall = { diffModel.generateContent(prompt).text },
            prompt = prompt,
            jsonSchemaHint = """
                {
                  "upgradedText": "string (Band 8.5+ rewritten response)",
                  "changes": [
                    {
                      "weakPhraseEn": "exact verbatim phrase from original",
                      "upgradedPhraseEn": "exact verbatim phrase in upgradedText",
                      "categoryVi": "Lexical Resource (C2)",
                      "explanationVi": "string (Vietnamese)"
                    }
                  ]
                }
            """.trimIndent(),
            temperature = 0.30f,
            maxOutputTokens = 950,
            timeoutMs = 60_000L,
            operationLabelVi = "tạo bản so sánh nâng cấp Band 8.5+"
        )
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
    }

    private val deepQuestionSchema = Schema.obj(
        properties = mapOf(
            "quickTipVi" to Schema.string(
                description = "Actionable step-by-step IELTS shortcut / exam strategy in Vietnamese for this exact question type"
            ),
            "commonMistakesVi" to Schema.string(
                description = "Concrete Band 6.0-6.5 mistakes candidates frequently make on this question in Vietnamese"
            ),
            "misconceptionTrapVi" to Schema.string(
                description = "Exact examiner trap, distractor, or paraphrase twist in the passage/audio causing candidates to misread the question in Vietnamese"
            )
        )
    )

    private val deepQuestionModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = generationConfig {
                temperature = 0.25f
                maxOutputTokens = 850
                responseMimeType = "application/json"
                responseSchema = deepQuestionSchema
            }
        )
    }

    /**
     * Powers "AI Chữa sâu câu này" inside Đề thi -> Chữa đề for any bundled or newly imported PDF/DOCX question.
     */
    suspend fun generateDeepQuestionAnalysis(
        skillLabel: String,
        questionTypeVi: String,
        promptEn: String,
        userAnswer: String,
        correctAnswer: String,
        passageExcerptEn: String = ""
    ): com.example.data.DeepQuestionAnalysis = withContext(Dispatchers.IO) {
        val contextBlock = if (passageExcerptEn.isNotBlank()) {
            "\nPassage / Script Context: \"${passageExcerptEn.take(900)}\""
        } else ""

        val prompt = """
            Role: Senior IELTS Band 9 Examiner coaching a Vietnamese learner (target Band 8.5).
            Question Skill: $skillLabel | Type: $questionTypeVi
            Question Prompt: "$promptEn"
            Learner's Answer: "$userAnswer"
            Official Correct Answer: "$correctAnswer"$contextBlock

            Provide 3 concise, super practical, high-clarity diagnostic sections in Vietnamese:
            1. quickTipVi: Step-by-step fast solving tip/trick (scanning anchor keywords, grammar prediction, or signposting cues) specifically for this question.
            2. commonMistakesVi: Concrete Band 6.0-6.5 errors learners typically make here (e.g., confusing False vs Not Given, missing plural -s, exceeding word limit, misreading qualifier words).
            3. misconceptionTrapVi: The exact examiner distractor trap or paraphrase twist that misleads candidates into choosing the wrong answer.
        """.trimIndent()

        val raw = GeminiResilientClient.generateJsonWithFallback(
            primaryCall = { deepQuestionModel.generateContent(prompt).text },
            prompt = prompt,
            jsonSchemaHint = """
                {
                  "quickTipVi": "string (Vietnamese)",
                  "commonMistakesVi": "string (Vietnamese)",
                  "misconceptionTrapVi": "string (Vietnamese)"
                }
            """.trimIndent(),
            temperature = 0.25f,
            maxOutputTokens = 850,
            timeoutMs = 50_000L,
            operationLabelVi = "phân tích chuyên sâu câu hỏi IELTS"
        )
        val json = JSONObject(raw)
        com.example.data.DeepQuestionAnalysis(
            quickTipVi = json.optString("quickTipVi").trim()
                .ifBlank { "Gạch chân từ khóa định vị (tên riêng, số liệu, động từ chính) rồi đối chiếu Paraphrase trực tiếp quanh đáp án \"$correctAnswer\"." },
            commonMistakesVi = json.optString("commonMistakesVi").trim()
                .ifBlank { "Dễ mất điểm do đọc lướt bỏ sót từ hạn định (only, mainly, rarely), sai dạng số ít/số nhiều hoặc nhầm lẫn giữa FALSE và NOT GIVEN." },
            misconceptionTrapVi = json.optString("misconceptionTrapVi").trim()
                .ifBlank { "Bẫy Distractor của giám khảo: câu gốc thường đảo chiều ngữ nghĩa hoặc dùng từ đồng nghĩa học thuật thay vì lặp lại từ khóa trong câu hỏi." },
            isAiGenerated = true
        )
    }
}
