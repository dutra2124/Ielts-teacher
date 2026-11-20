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

private const val TAG = "PracticeAiService"

// --- 1. AI Syntax Breakdown (Soi Ngữ pháp Câu) ---
data class SyntaxBreakdownResult(
    val sentenceEn: String,
    val mainClauseEn: String,
    val mainClauseExplainVi: String,
    val subordinateClausesVi: List<String>,
    val advancedStructuresVi: List<String>,
    val vietnameseTranslation: String,
    val band85ReuseTipVi: String
)

sealed interface SyntaxBreakdownUiState {
    data object Idle : SyntaxBreakdownUiState
    data class Loading(val sentenceEn: String) : SyntaxBreakdownUiState
    data class Success(val result: SyntaxBreakdownResult) : SyntaxBreakdownUiState
    data class Error(val sentenceEn: String, val messageVi: String) : SyntaxBreakdownUiState
}

// --- 2. AI Speaking Evaluation ---
data class SpeakingAiEvaluation(
    val overallBand: String,
    val fluencyBand: String,
    val lexicalBand: String,
    val grammarBand: String,
    val pronunciationTipVi: String,
    val strengthsVi: List<String>,
    val improvementsVi: List<String>,
    val upgradedBand85Answer: String
)

sealed interface SpeakingAiUiState {
    data object Idle : SpeakingAiUiState
    data object Loading : SpeakingAiUiState
    data class Success(val result: SpeakingAiEvaluation) : SpeakingAiUiState
    data class Error(val messageVi: String) : SpeakingAiUiState
}

// --- 3. AI Writing Evaluation ---
data class WritingAiEvaluation(
    val overallBand: String,
    val taskResponseBand: String,
    val coherenceBand: String,
    val lexicalBand: String,
    val grammarBand: String,
    val summaryFeedbackVi: String,
    val keyFixesVi: List<String>,
    val upgradedBand85Excerpt: String
)

sealed interface WritingAiUiState {
    data object Idle : WritingAiUiState
    data object Loading : WritingAiUiState
    data class Success(val result: WritingAiEvaluation) : WritingAiUiState
    data class Error(val messageVi: String) : WritingAiUiState
}

/**
 * Powered by Firebase AI Logic (gemini-3.5-flash-lite) for fast structured JSON responses
 * across Tab 3 Practice: Syntax Breakdown, Speaking Band Examiner, and Writing Band Examiner.
 */
class PracticeAiService {

    private val syntaxSchema = Schema.obj(
        properties = mapOf(
            "mainClauseEn" to Schema.string(description = "Exact main clause extracted from the sentence"),
            "mainClauseExplainVi" to Schema.string(description = "Concise Vietnamese breakdown of subject, verb, and core meaning"),
            "subordinateClausesVi" to Schema.array(
                items = Schema.string(),
                description = "List of subordinate/relative/adverbial clauses with Vietnamese explanation"
            ),
            "advancedStructuresVi" to Schema.array(
                items = Schema.string(),
                description = "Reduced clauses, inversion, cleft structures, or C1-C2 grammar highlights in Vietnamese"
            ),
            "vietnameseTranslation" to Schema.string(description = "Natural academic Vietnamese translation"),
            "band85ReuseTipVi" to Schema.string(description = "1 concise Vietnamese tip on reusing this pattern in Writing/Speaking")
        )
    )

    private val speakingSchema = Schema.obj(
        properties = mapOf(
            "overallBand" to Schema.string(description = "Estimated Speaking Band e.g. '6.5', '7.0', '7.5', '8.0'"),
            "fluencyBand" to Schema.string(description = "Fluency & Coherence band score"),
            "lexicalBand" to Schema.string(description = "Lexical Resource band score"),
            "grammarBand" to Schema.string(description = "Grammatical Range & Accuracy band score"),
            "pronunciationTipVi" to Schema.string(description = "Actionable pronunciation, stress & intonation tip in Vietnamese"),
            "strengthsVi" to Schema.array(
                items = Schema.string(),
                description = "2 concise strengths in Vietnamese"
            ),
            "improvementsVi" to Schema.array(
                items = Schema.string(),
                description = "2-3 specific collocation/grammar/coherence improvements in Vietnamese"
            ),
            "upgradedBand85Answer" to Schema.string(description = "Natural spoken Band 8.5+ model version preserving the user's ideas")
        )
    )

    private val writingSchema = Schema.obj(
        properties = mapOf(
            "overallBand" to Schema.string(description = "Estimated Writing Band e.g. '6.0', '6.5', '7.0', '7.5', '8.0'"),
            "taskResponseBand" to Schema.string(description = "Task Achievement / Task Response band"),
            "coherenceBand" to Schema.string(description = "Coherence & Cohesion band"),
            "lexicalBand" to Schema.string(description = "Lexical Resource band"),
            "grammarBand" to Schema.string(description = "Grammatical Range & Accuracy band"),
            "summaryFeedbackVi" to Schema.string(description = "Concise overall examiner diagnosis in Vietnamese (2 sentences)"),
            "keyFixesVi" to Schema.array(
                items = Schema.string(),
                description = "3 concrete high-impact corrections in Vietnamese"
            ),
            "upgradedBand85Excerpt" to Schema.string(description = "Rewritten Band 8.5+ academic paragraph based on the user's essay")
        )
    )

    private val syntaxModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = generationConfig {
                temperature = 0.25f
                maxOutputTokens = 750
                responseMimeType = "application/json"
                responseSchema = syntaxSchema
            }
        )
    }

    private val speakingModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = generationConfig {
                temperature = 0.35f
                maxOutputTokens = 820
                responseMimeType = "application/json"
                responseSchema = speakingSchema
            }
        )
    }

    private val writingModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = generationConfig {
                temperature = 0.30f
                maxOutputTokens = 860
                responseMimeType = "application/json"
                responseSchema = writingSchema
            }
        )
    }

    suspend fun analyzeSentenceSyntax(
        sentenceEn: String,
        sourceContext: String
    ): SyntaxBreakdownResult = withContext(Dispatchers.IO) {
        val prompt = """
            Role: IELTS Band 9 Grammar Specialist teaching Vietnamese learners.
            Source: $sourceContext
            Sentence: "$sentenceEn"

            Dissect this academic English sentence clearly:
            1. Extract the Main Clause (Mệnh đề chính) and explain Subject/Verb in Vietnamese.
            2. List Subordinate / Relative / Adverbial clauses (Mệnh đề phụ) in Vietnamese.
            3. Highlight Reduced clauses, Inversion, Passive voice, or C1-C2 structures (Cấu trúc rút gọn / đảo ngữ).
            4. Provide a smooth academic Vietnamese translation and 1 short reuse tip.
        """.trimIndent()

        try {
            val response = withTimeout(60_000L) {
                syntaxModel.generateContent(prompt)
            }
            val raw = response.text
                ?: throw IllegalStateException("AI không trả về dữ liệu phân tích cú pháp.")
            val json = JSONObject(raw)
            SyntaxBreakdownResult(
                sentenceEn = sentenceEn,
                mainClauseEn = json.optString("mainClauseEn", sentenceEn),
                mainClauseExplainVi = json.optString("mainClauseExplainVi", ""),
                subordinateClausesVi = json.optStringList("subordinateClausesVi"),
                advancedStructuresVi = json.optStringList("advancedStructuresVi"),
                vietnameseTranslation = json.optString("vietnameseTranslation", ""),
                band85ReuseTipVi = json.optString("band85ReuseTipVi", "")
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Syntax breakdown timed out", e)
            throw IllegalStateException("Phân tích cú pháp quá 60 giây. Vui lòng kiểm tra mạng và chạm Thử lại.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Syntax breakdown failed", e)
            throw IllegalStateException(
                e.localizedMessage?.takeIf { it.isNotBlank() }
                    ?: "Không thể kết nối Gemini AI để soi ngữ pháp. Vui lòng thử lại."
            )
        }
    }

    suspend fun evaluateSpeaking(
        partLabel: String,
        promptEn: String,
        topicEn: String,
        transcript: String
    ): SpeakingAiEvaluation = withContext(Dispatchers.IO) {
        val prompt = """
            Role: Official IELTS Speaking Examiner coaching a Vietnamese learner targeting Band 8.5.
            Part: $partLabel | Topic: $topicEn
            Question: "$promptEn"
            Candidate Transcript: "$transcript"

            Evaluate strictly using IELTS Speaking descriptors (FC, LR, GRA) and provide pronunciation advice based on vocabulary/phrasing. Keep Vietnamese feedback concise, specific, and motivating. Provide a natural spoken Band 8.5+ upgraded version keeping the candidate's ideas.
        """.trimIndent()

        try {
            val response = withTimeout(60_000L) {
                speakingModel.generateContent(prompt)
            }
            val raw = response.text
                ?: throw IllegalStateException("AI không trả về điểm chấm Speaking.")
            val json = JSONObject(raw)
            SpeakingAiEvaluation(
                overallBand = json.optString("overallBand", "6.5"),
                fluencyBand = json.optString("fluencyBand", "6.5"),
                lexicalBand = json.optString("lexicalBand", "6.5"),
                grammarBand = json.optString("grammarBand", "6.5"),
                pronunciationTipVi = json.optString("pronunciationTipVi", ""),
                strengthsVi = json.optStringList("strengthsVi"),
                improvementsVi = json.optStringList("improvementsVi"),
                upgradedBand85Answer = json.optString("upgradedBand85Answer", "")
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Speaking evaluation timed out", e)
            throw IllegalStateException("Chấm Speaking quá 60 giây. Vui lòng kiểm tra mạng và chạm Thử lại.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Speaking evaluation failed", e)
            throw IllegalStateException(
                e.localizedMessage?.takeIf { it.isNotBlank() }
                    ?: "Không thể chấm IELTS Speaking lúc này. Vui lòng thử lại."
            )
        }
    }

    suspend fun evaluateWriting(
        taskLabel: String,
        promptEn: String,
        topicEn: String,
        essay: String,
        wordCount: Int
    ): WritingAiEvaluation = withContext(Dispatchers.IO) {
        val prompt = """
            Role: Official IELTS Writing Examiner coaching a Vietnamese learner targeting Band 8.5.
            Task: $taskLabel | Topic: $topicEn | Word Count: $wordCount
            Prompt: "$promptEn"
            Candidate Essay: "$essay"

            Score all 4 IELTS Writing criteria (TR/TA, CC, LR, GRA). Write clear, high-impact feedback and 3 concrete fixes in Vietnamese. Rewrite the strongest core paragraph into Band 8.5+ academic English preserving the candidate's argument.
        """.trimIndent()

        try {
            val response = withTimeout(60_000L) {
                writingModel.generateContent(prompt)
            }
            val raw = response.text
                ?: throw IllegalStateException("AI không trả về điểm chấm Writing.")
            val json = JSONObject(raw)
            WritingAiEvaluation(
                overallBand = json.optString("overallBand", "6.5"),
                taskResponseBand = json.optString("taskResponseBand", "6.5"),
                coherenceBand = json.optString("coherenceBand", "6.5"),
                lexicalBand = json.optString("lexicalBand", "6.5"),
                grammarBand = json.optString("grammarBand", "6.5"),
                summaryFeedbackVi = json.optString("summaryFeedbackVi", ""),
                keyFixesVi = json.optStringList("keyFixesVi"),
                upgradedBand85Excerpt = json.optString("upgradedBand85Excerpt", "")
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Writing evaluation timed out", e)
            throw IllegalStateException("Chấm Writing quá 60 giây. Vui lòng kiểm tra mạng và chạm Thử lại.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Writing evaluation failed", e)
            throw IllegalStateException(
                e.localizedMessage?.takeIf { it.isNotBlank() }
                    ?: "Không thể chấm IELTS Writing lúc này. Vui lòng thử lại."
            )
        }
    }
}

private fun JSONObject.optStringList(key: String): List<String> {
    val array = optJSONArray(key) ?: return emptyList()
    return buildList {
        for (i in 0 until array.length()) {
            val value = array.optString(i).trim()
            if (value.isNotEmpty()) add(value)
        }
    }
}
