package com.example.ai

import android.util.Log
import com.example.data.ExamDifficulty
import com.example.data.ExamPaper
import com.example.data.ExamParagraph
import com.example.data.ExamQuestionItem
import com.example.data.ExamQuestionSkill
import com.example.data.ExamQuestionType
import com.example.data.ExamSerialization
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "ExamParserAiService"

sealed interface ExamParserUiState {
    data object Idle : ExamParserUiState
    data object Parsing : ExamParserUiState
    data class PreviewReady(val parsedExam: ExamPaper) : ExamParserUiState
    data class Error(val messageVi: String) : ExamParserUiState
}

/**
 * AI Exam Parser powered by Firebase AI Logic (gemini-3.5-flash-lite).
 * Converts raw pasted IELTS exam text, TXT/DOCX documents, JSON files, or PDF inline bytes
 * into an interactive ExamPaper (Passage, Questions TFNG/MCQ/Gap-fill, and Answer Key).
 */
class ExamParserAiService {

    private val parsedExamSchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "Clean title for the IELTS exam"),
            "topicVi" to Schema.string(description = "Short topic label in Vietnamese"),
            "topicEn" to Schema.string(description = "Short topic label in English"),
            "readingTitle" to Schema.string(description = "Title of the reading passage"),
            "readingParagraphs" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "label" to Schema.string(description = "Paragraph letter e.g. A, B, C"),
                        "sentences" to Schema.array(
                            items = Schema.string(),
                            description = "Individual English sentences inside this paragraph"
                        )
                    )
                ),
                description = "Reading passage split into labelled paragraphs and sentences"
            ),
            "listeningTitle" to Schema.string(description = "Title for listening section if present"),
            "listeningScript" to Schema.array(
                items = Schema.string(),
                description = "Spoken dialogue or lecture lines if listening content exists"
            ),
            "writingTask2Prompt" to Schema.string(description = "Writing prompt if present or a relevant IELTS Task 2 prompt"),
            "questions" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "number" to Schema.integer(description = "1-based question number"),
                        "skill" to Schema.enumeration(
                            values = listOf("reading", "listening"),
                            description = "Whether question belongs to reading or listening"
                        ),
                        "type" to Schema.enumeration(
                            values = listOf("tfng", "mcq", "gap_fill"),
                            description = "Question format"
                        ),
                        "promptEn" to Schema.string(description = "Question text in English"),
                        "options" to Schema.array(
                            items = Schema.string(),
                            description = "Answer choices for mcq or tfng (empty array for gap_fill)"
                        ),
                        "correctAnswer" to Schema.string(description = "Exact official answer key")
                    )
                ),
                description = "Interactive questions extracted or structured from the uploaded exam"
            )
        )
    )

    private val parserModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = generationConfig {
                temperature = 0.2f
                maxOutputTokens = 1600
                responseMimeType = "application/json"
                responseSchema = parsedExamSchema
            }
        )
    }

    suspend fun parseExam(
        rawText: String,
        pdfBytes: ByteArray? = null,
        sourceFileName: String? = null,
        audioUri: String? = null,
        audioDisplayName: String? = null
    ): ExamPaper = withContext(Dispatchers.IO) {
        // Fast path: if the user uploaded/pasted valid JSON matching our ExamPaper template, import directly
        val trimmedText = rawText.trim()
        if (pdfBytes == null && trimmedText.startsWith("{") && trimmedText.endsWith("}")) {
            val directJson = runCatching {
                ExamSerialization.parseSingleCustomExamJson(
                    rawJson = trimmedText,
                    fallbackId = "custom_${System.currentTimeMillis()}",
                    audioUri = audioUri,
                    audioDisplayName = audioDisplayName
                )
            }.getOrNull()
            if (directJson != null && directJson.questions.isNotEmpty()) {
                return@withContext directJson
            }
        }

        val instructions = """
            Role: IELTS Exam Digitizer (AI Exam Parser).
            Task: Parse the provided IELTS exam material (${sourceFileName ?: "pasted text"}) into a structured interactive test JSON.
            Requirements:
            - Extract the Reading passage split cleanly into paragraphs (A, B, C...) and individual sentences.
            - Extract all objective questions (TFNG, MCQ, or Gap-fill) together with their Answer Key.
            - If the uploaded text contains a passage without explicit questions, generate 4 authentic IELTS questions (mix of tfng, mcq, gap_fill) directly grounded in the passage with verified answer keys.
            - If a Listening script/transcript is present, include it in listeningScript; otherwise include 2 concise academic listening lines tied to the same topic.
            - Provide topicVi in clear Vietnamese and all academic exam text in English.
        """.trimIndent()

        val fallbackPrompt = if (pdfBytes != null) {
            instructions
        } else {
            "$instructions\n\n--- RAW EXAM CONTENT ---\n${trimmedText.take(12_000)}"
        }

        val rawJson = GeminiResilientClient.generateJsonWithFallback(
            primaryCall = {
                val response = if (pdfBytes != null) {
                    parserModel.generateContent(
                        content {
                            inlineData(pdfBytes, "application/pdf")
                            text(instructions)
                        }
                    )
                } else {
                    parserModel.generateContent(fallbackPrompt)
                }
                response.text
            },
            prompt = fallbackPrompt,
            jsonSchemaHint = """
                {
                  "title": "string",
                  "topicVi": "string",
                  "topicEn": "string",
                  "readingTitle": "string",
                  "readingParagraphs": [{"label": "A", "sentences": ["string"]}],
                  "listeningTitle": "string",
                  "listeningScript": ["string"],
                  "writingTask2Prompt": "string",
                  "questions": [{"number": 1, "skill": "reading|listening", "type": "tfng|mcq|gap_fill", "promptEn": "string", "options": ["string"], "correctAnswer": "string"}]
                }
            """.trimIndent(),
            pdfBytes = pdfBytes,
            temperature = 0.2f,
            maxOutputTokens = 1600,
            timeoutMs = 60_000L,
            operationLabelVi = "số hóa đề thi IELTS"
        )

        val json = JSONObject(rawJson)
        val paragraphs = buildList {
            val pArr = json.optJSONArray("readingParagraphs")
            if (pArr != null) {
                for (i in 0 until pArr.length()) {
                    val pObj = pArr.optJSONObject(i) ?: continue
                    val label = pObj.optString("label", ('A' + i).toString())
                    val sArr = pObj.optJSONArray("sentences")
                    val sentences = buildList {
                        if (sArr != null) {
                            for (j in 0 until sArr.length()) {
                                val s = sArr.optString(j).trim()
                                if (s.isNotEmpty()) add(s)
                            }
                        }
                    }
                    if (sentences.isNotEmpty()) add(ExamParagraph(label, sentences))
                }
            }
        }.ifEmpty {
            listOf(ExamParagraph("A", listOf(trimmedText.take(600).ifBlank { "Imported IELTS Reading passage." })))
        }

        val listeningLines = buildList {
            val lArr = json.optJSONArray("listeningScript")
            if (lArr != null) {
                for (i in 0 until lArr.length()) {
                    val line = lArr.optString(i).trim()
                    if (line.isNotEmpty()) add(line)
                }
            }
        }.ifEmpty {
            listOf("Audio track ready. Press play once to complete the IELTS Listening section.")
        }

        val questions = buildList {
            val qArr = json.optJSONArray("questions")
            if (qArr != null) {
                for (i in 0 until qArr.length()) {
                    val qObj = qArr.optJSONObject(i) ?: continue
                    val type = ExamQuestionType.fromId(qObj.optString("type"))
                    val optArr = qObj.optJSONArray("options")
                    val options = buildList {
                        if (optArr != null) {
                            for (j in 0 until optArr.length()) {
                                val opt = optArr.optString(j).trim()
                                if (opt.isNotEmpty()) add(opt)
                            }
                        }
                    }.ifEmpty {
                        if (type == ExamQuestionType.TFNG) listOf("TRUE", "FALSE", "NOT GIVEN") else emptyList()
                    }
                    val answer = qObj.optString("correctAnswer", "").trim()
                    add(
                        ExamQuestionItem(
                            id = "aiq_${System.currentTimeMillis()}_${i + 1}",
                            number = qObj.optInt("number", i + 1),
                            skill = ExamQuestionSkill.fromId(qObj.optString("skill")),
                            type = type,
                            promptEn = qObj.optString("promptEn", "Question ${i + 1}"),
                            options = options,
                            correctAnswer = answer,
                            acceptableAnswers = listOf(answer)
                        )
                    )
                }
            }
        }

        ExamPaper(
            id = "custom_${System.currentTimeMillis()}",
            title = json.optString("title", sourceFileName ?: "Đề IELTS Số hóa AI"),
            sourceLabel = "Số hóa AI • ${sourceFileName ?: "Văn bản dán"}",
            year = 2025,
            difficulty = ExamDifficulty.HARD,
            topicVi = json.optString("topicVi", "Chủ đề cá nhân hóa"),
            topicEn = json.optString("topicEn", "Custom Academic Topic"),
            isCustom = true,
            audioUri = audioUri,
            audioDisplayName = audioDisplayName,
            listeningTitle = json.optString("listeningTitle", "Listening Section"),
            listeningScript = listeningLines,
            readingTitle = json.optString("readingTitle", json.optString("title", "Academic Reading")),
            readingParagraphs = paragraphs,
            writingTask1Prompt = "Summarise the main findings and trends related to ${json.optString("topicEn", "this topic")} in at least 150 words.",
            writingTask2Prompt = json.optString(
                "writingTask2Prompt",
                "Discuss the main arguments surrounding ${json.optString("topicEn", "this issue")} and state your own position (at least 250 words)."
            ),
            speakingPrompts = listOf(
                "Part 1: What interests you most about ${json.optString("topicEn", "this subject")}?",
                "Part 2: Describe an experience or article related to ${json.optString("topicEn", "this topic")} that changed your perspective.",
                "Part 3: How should policymakers balance innovation and ethics in this domain?"
            ),
            questions = questions
        )
    }
}
