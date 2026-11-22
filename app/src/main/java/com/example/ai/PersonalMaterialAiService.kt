package com.example.ai

import android.util.Log
import com.example.data.local.ExtractedAcademicWord
import com.example.data.local.ExtractedGrammarPoint
import com.example.data.local.PersonalMaterial
import com.example.data.local.PersonalMaterialAiSummary
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

/**
 * Firebase AI Logic service powering "AI tóm tắt & Rút từ Band 8.5+" inside
 * the Personal Library & Self-Study Workspace.
 * Summarizes user articles/notes in Vietnamese and extracts ready-to-save C1–C2 Flashcards
 * plus Band 8.5+ academic grammar highlights.
 */
class PersonalMaterialAiService {

    companion object {
        private const val TAG = "PersonalMaterialAi"
        private const val MODEL_NAME = "gemini-3.5-flash-lite"
        private const val TIMEOUT_MS = 60_000L
    }

    private val responseSchema = Schema.obj(
        mapOf(
            "summary_vi" to Schema.string("Concise 2-3 sentence summary in natural Vietnamese explaining the core academic argument."),
            "key_takeaways_vi" to Schema.array(
                items = Schema.string("Actionable Vietnamese IELTS Band 8.5 study takeaway"),
                description = "Exactly 3 concise Vietnamese takeaways for Writing/Reading/Speaking."
            ),
            "extracted_words" to Schema.array(
                items = Schema.obj(
                    mapOf(
                        "word" to Schema.string("Academic C1-C2 English word or collocation from or directly inspired by the text"),
                        "part_of_speech" to Schema.string("noun, verb, adjective, adverb, or phrase"),
                        "cefr_level" to Schema.string("C1 or C2"),
                        "ipa_uk" to Schema.string("UK IPA transcription like /ˌep.ɪˈstiː.mɪk/"),
                        "meaning_vi" to Schema.string("Clear Vietnamese academic meaning"),
                        "collocation_en" to Schema.string("High-scoring IELTS academic collocation using this word"),
                        "example_en" to Schema.string("One crisp Band 8.5+ IELTS example sentence")
                    )
                ),
                description = "4 to 5 high-value C1-C2 academic vocabulary items."
            ),
            "grammar_highlights" to Schema.array(
                items = Schema.obj(
                    mapOf(
                        "structure_name_en" to Schema.string("Name of the Band 8.5+ grammar structure (e.g., Concession While-Clause, Participle Clause)"),
                        "example_quote_en" to Schema.string("Quote from the text or upgraded Band 8.5+ model sentence"),
                        "explanation_vi" to Schema.string("Clear Vietnamese explanation of how to reuse this structure in IELTS")
                    )
                ),
                description = "2 to 3 notable Band 8.5+ grammatical structures."
            )
        )
    )

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                temperature = 0.25f
                maxOutputTokens = 1400
                responseMimeType = "application/json"
                responseSchema = this@PersonalMaterialAiService.responseSchema
            }
        )
    }

    suspend fun summarizeAndExtract(material: PersonalMaterial): PersonalMaterialAiSummary =
        withContext(Dispatchers.IO) {
            val combinedInput = buildString {
                appendLine("Title: ${material.title}")
                appendLine("Category: ${material.category}")
                if (material.contentEn.isNotBlank()) {
                    appendLine("English Content:")
                    appendLine(material.contentEn.take(3500))
                }
                if (material.notesVi.isNotBlank()) {
                    appendLine("User Vietnamese Notes:")
                    appendLine(material.notesVi.take(1200))
                }
            }

            val prompt = """
                You are an expert IELTS Band 8.5+ Vietnamese-English Academic Coach.
                Analyze the learner's personal self-study material below.
                Respond strictly as JSON matching the schema:
                1. summary_vi: Summarize the core message & IELTS exam relevance in natural Vietnamese (2-3 sentences).
                2. key_takeaways_vi: 3 concise, practical Vietnamese bullet takeaways for IELTS Reading/Writing/Speaking.
                3. extracted_words: Extract 4-5 top C1-C2 academic words or collocations from the material (or closely upgraded Band 8.5 equivalents if the input is short) with accurate UK IPA, Vietnamese meaning, collocation, and Band 8.5 example sentence.
                4. grammar_highlights: Identify 2-3 high-scoring Band 8.5+ grammatical structures in or applicable to the text with Vietnamese explanations.

                Material:
                $combinedInput
            """.trimIndent()

            val rawText = GeminiResilientClient.generateJsonWithFallback(
                primaryCall = { model.generateContent(prompt).text },
                prompt = prompt,
                jsonSchemaHint = """
                    {
                      "summary_vi": "string (Vietnamese)",
                      "key_takeaways_vi": ["string", "string", "string"],
                      "extracted_words": [
                        {
                          "word": "string",
                          "part_of_speech": "noun|verb|adjective|adverb|phrase",
                          "cefr_level": "C1|C2",
                          "ipa_uk": "/.../",
                          "meaning_vi": "string",
                          "collocation_en": "string",
                          "example_en": "string"
                        }
                      ],
                      "grammar_highlights": [
                        {
                          "structure_name_en": "string",
                          "example_quote_en": "string",
                          "explanation_vi": "string"
                        }
                      ]
                    }
                """.trimIndent(),
                temperature = 0.25f,
                maxOutputTokens = 1400,
                timeoutMs = TIMEOUT_MS,
                operationLabelVi = "tóm tắt & rút từ tài liệu cá nhân"
            )
            parseResponse(material.id, rawText)
        }

    private fun parseResponse(materialId: String, rawJson: String): PersonalMaterialAiSummary {
        val cleaned = rawJson
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val root = JSONObject(cleaned)

        val takeaways = buildList {
            val arr = root.optJSONArray("key_takeaways_vi")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val s = arr.optString(i).trim()
                    if (s.isNotEmpty()) add(s)
                }
            }
        }

        val words = buildList {
            val arr = root.optJSONArray("extracted_words")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val w = obj.optString("word", "").trim()
                    if (w.isEmpty()) continue
                    add(
                        ExtractedAcademicWord(
                            word = w,
                            partOfSpeech = obj.optString("part_of_speech", "noun").ifBlank { "noun" },
                            cefrLevel = obj.optString("cefr_level", "C1").ifBlank { "C1" },
                            ipaUk = obj.optString("ipa_uk", ""),
                            meaningVi = obj.optString("meaning_vi", "Từ học thuật Band 8.5+"),
                            collocationEn = obj.optString("collocation_en", w),
                            exampleEn = obj.optString("example_en", "")
                        )
                    )
                }
            }
        }

        val grammar = buildList {
            val arr = root.optJSONArray("grammar_highlights")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val name = obj.optString("structure_name_en", "").trim()
                    if (name.isEmpty()) continue
                    add(
                        ExtractedGrammarPoint(
                            structureNameEn = name,
                            exampleQuoteEn = obj.optString("example_quote_en", ""),
                            explanationVi = obj.optString("explanation_vi", "")
                        )
                    )
                }
            }
        }

        return PersonalMaterialAiSummary(
            materialId = materialId,
            summaryVi = root.optString("summary_vi", "Đã hoàn tất phân tích tài liệu Band 8.5+."),
            keyTakeawaysVi = takeaways,
            extractedWords = words,
            grammarHighlights = grammar
        )
    }
}
