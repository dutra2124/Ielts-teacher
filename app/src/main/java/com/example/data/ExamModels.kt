package com.example.data

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ExamDifficulty(val labelVi: String) {
    MODERATE_HARD("Trung bình – Khó"),
    HARD("Khó (Band 7.5+)"),
    VERY_HARD("Rất khó (Band 8.5)")
}

enum class ExamSkillMode(
    val id: String,
    val labelVi: String,
    val durationMinutes: Int,
    val badgeVi: String
) {
    FULL_TEST("full_test", "Full Test (4 kỹ năng)", 166, "Full 4 kỹ năng • 2h46p"),
    LISTENING("listening", "Listening", 32, "30+2 phút • Nghe 1 lần"),
    READING("reading", "Reading", 60, "60 phút • Strict"),
    WRITING("writing", "Writing", 60, "60 phút • Task 1 & 2"),
    SPEAKING("speaking", "Speaking", 14, "11–14 phút • 3 Parts");

    companion object {
        fun fromId(id: String?): ExamSkillMode = entries.firstOrNull { it.id == id } ?: FULL_TEST
    }
}

enum class ExamQuestionSkill(val id: String, val labelEn: String) {
    LISTENING("listening", "Listening"),
    READING("reading", "Reading");

    companion object {
        fun fromId(id: String?): ExamQuestionSkill =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: READING
    }
}

enum class ExamQuestionType(val id: String, val labelVi: String) {
    TFNG("tfng", "True / False / Not Given"),
    MCQ("mcq", "Trắc nghiệm (A/B/C/D)"),
    GAP_FILL("gap_fill", "Điền từ ngắn");

    companion object {
        fun fromId(id: String?): ExamQuestionType =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: MCQ
    }
}

data class ExamParagraph(
    val label: String,
    val sentences: List<String>
)

data class ExamQuestionItem(
    val id: String,
    val number: Int,
    val skill: ExamQuestionSkill,
    val type: ExamQuestionType,
    val promptEn: String,
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val acceptableAnswers: List<String> = listOf(correctAnswer)
)

data class ExamPaper(
    val id: String,
    val title: String,
    val sourceLabel: String,
    val year: Int,
    val difficulty: ExamDifficulty,
    val topicVi: String,
    val topicEn: String,
    val isCustom: Boolean = false,
    val audioUri: String? = null,
    val audioDisplayName: String? = null,
    val listeningTitle: String,
    val listeningScript: List<String>,
    val readingTitle: String,
    val readingParagraphs: List<ExamParagraph>,
    val writingTask1Prompt: String,
    val writingTask2Prompt: String,
    val speakingPrompts: List<String>,
    val questions: List<ExamQuestionItem>
)

data class ExamAttemptRecord(
    val id: String,
    val examId: String,
    val examTitle: String,
    val mode: ExamSkillMode,
    val submittedAtLabel: String,
    val correctCount: Int,
    val totalObjectiveQuestions: Int,
    val estimatedBand: Double,
    val durationUsedSeconds: Int,
    val writingWordCount: Int,
    val writingDraft: String,
    val speakingTranscript: String,
    val objectiveAnswers: Map<String, String> = emptyMap(),
    val flaggedQuestionIds: Set<String> = emptySet()
)

data class ActiveExamSession(
    val exam: ExamPaper,
    val mode: ExamSkillMode,
    val candidateId: String = "VN-APEX-8524",
    val startedAtMillis: Long = System.currentTimeMillis()
)

object ExamScoringHelper {
    fun estimateBandFromRatio(correct: Int, total: Int): Double {
        if (total <= 0) return 7.0
        val ratio = correct.toDouble() / total.toDouble()
        return when {
            ratio >= 0.95 -> 9.0
            ratio >= 0.88 -> 8.5
            ratio >= 0.80 -> 8.0
            ratio >= 0.72 -> 7.5
            ratio >= 0.62 -> 7.0
            ratio >= 0.50 -> 6.5
            ratio >= 0.38 -> 6.0
            ratio >= 0.25 -> 5.5
            else -> 5.0
        }
    }

    fun nowFormatted(): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date())
}

object ExamSerialization {

    val SAMPLE_JSON_TEMPLATE: String = """
        {
          "title": "ApexIELTS Custom Test — Urban Mobility & AI",
          "sourceLabel": "Đề cá nhân JSON",
          "year": 2025,
          "topicVi": "Giao thông đô thị & Trí tuệ nhân tạo",
          "topicEn": "Urban Mobility & Artificial Intelligence",
          "listeningTitle": "Section 4: Autonomous Public Transit Systems",
          "listeningScript": [
            "Welcome to today's transport engineering seminar on autonomous shuttle corridors in European cities.",
            "Unlike private driverless cars, dedicated autonomous shuttles run along predictable routes and cut peak commute times by twenty-eight percent.",
            "However, municipal planners note that winter sensor calibration remains the primary operational bottleneck."
          ],
          "readingTitle": "The Quiet Economics of Fifteen-Minute Cities",
          "readingParagraphs": [
            {
              "label": "A",
              "sentences": [
                "Across rapidly densifying metropolitan regions, urban theorists have revived the fifteen-minute city model, in which essential services lie within a short walk or cycle ride.",
                "Empirical audits from Paris and Melbourne reveal that neighbourhood retail revenues rose by nearly eighteen percent after kerbside lanes were converted into pedestrian plazas."
              ]
            },
            {
              "label": "B",
              "sentences": [
                "Critics nonetheless warn that without strict affordable-housing quotas, walkable districts risk accelerating gentrification and displacing lower-income families."
              ]
            }
          ],
          "writingTask1Prompt": "The table compares daily commuting modes in three capital cities between 2015 and 2025. Summarise the main features and make relevant comparisons.",
          "writingTask2Prompt": "Private cars should be banned from historic city centres to reduce emissions and improve liveability. To what extent do you agree or disagree?",
          "speakingPrompts": [
            "Part 1: How do you usually commute to work or university?",
            "Part 2: Describe a memorable journey you made by public transport.",
            "Part 3: Should governments subsidise electric buses rather than expanding highways?"
          ],
          "questions": [
            {
              "id": "q_l1",
              "number": 1,
              "skill": "listening",
              "type": "gap_fill",
              "promptEn": "What operational factor remains the main challenge for autonomous shuttles in winter? (NO MORE THAN TWO WORDS)",
              "options": [],
              "correctAnswer": "sensor calibration"
            },
            {
              "id": "q_r1",
              "number": 2,
              "skill": "reading",
              "type": "tfng",
              "promptEn": "Converting kerbside lanes into pedestrian areas reduced local shop earnings in Paris and Melbourne.",
              "options": ["TRUE", "FALSE", "NOT GIVEN"],
              "correctAnswer": "FALSE"
            },
            {
              "id": "q_r2",
              "number": 3,
              "skill": "reading",
              "type": "mcq",
              "promptEn": "What risk do critics associate with walkable districts lacking housing quotas?",
              "options": [
                "A. Higher maintenance costs for cycle paths",
                "B. Accelerating gentrification that displaces low-income residents",
                "C. Slower emergency vehicle response times"
              ],
              "correctAnswer": "B. Accelerating gentrification that displaces low-income residents"
            }
          ]
        }
    """.trimIndent()

    fun parseSingleCustomExamJson(
        rawJson: String,
        fallbackId: String = "custom_${System.currentTimeMillis()}",
        audioUri: String? = null,
        audioDisplayName: String? = null
    ): ExamPaper {
        val obj = JSONObject(rawJson)
        val paragraphs = buildList {
            val arr = obj.optJSONArray("readingParagraphs")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val pObj = arr.optJSONObject(i) ?: continue
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
            listOf(
                ExamParagraph(
                    label = "A",
                    sentences = listOf(obj.optString("readingPassage", "Academic passage imported by user."))
                )
            )
        }

        val listeningScript = obj.optStringList("listeningScript").ifEmpty {
            listOf("Listening script ready. Press play to begin your single-play IELTS Listening test.")
        }

        val speakingPrompts = obj.optStringList("speakingPrompts").ifEmpty {
            listOf(
                "Part 1: Tell me about your daily study routine.",
                "Part 2: Describe a topic related to this exam that interests you.",
                "Part 3: How might this field evolve over the next decade?"
            )
        }

        val questions = buildList {
            val qArr = obj.optJSONArray("questions")
            if (qArr != null) {
                for (i in 0 until qArr.length()) {
                    val qObj = qArr.optJSONObject(i) ?: continue
                    val type = ExamQuestionType.fromId(qObj.optString("type"))
                    val answer = qObj.optString("correctAnswer", "").trim()
                    val options = qObj.optStringList("options").ifEmpty {
                        if (type == ExamQuestionType.TFNG) listOf("TRUE", "FALSE", "NOT GIVEN") else emptyList()
                    }
                    add(
                        ExamQuestionItem(
                            id = qObj.optString("id", "cq_${i + 1}"),
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

        val diffName = obj.optString("difficulty", ExamDifficulty.HARD.name)
        val difficulty = ExamDifficulty.entries.firstOrNull { it.name == diffName } ?: ExamDifficulty.HARD

        return ExamPaper(
            id = obj.optString("id", fallbackId),
            title = obj.optString("title", "Đề IELTS Cá Nhân"),
            sourceLabel = obj.optString("sourceLabel", "Thư viện cá nhân"),
            year = obj.optInt("year", 2025),
            difficulty = difficulty,
            topicVi = obj.optString("topicVi", "Tổng hợp học thuật"),
            topicEn = obj.optString("topicEn", "Academic IELTS"),
            isCustom = true,
            audioUri = audioUri ?: obj.optString("audioUri", "").ifBlank { null },
            audioDisplayName = audioDisplayName ?: obj.optString("audioDisplayName", "").ifBlank { null },
            listeningTitle = obj.optString("listeningTitle", "Listening Section"),
            listeningScript = listeningScript,
            readingTitle = obj.optString("readingTitle", obj.optString("title", "Academic Reading Passage")),
            readingParagraphs = paragraphs,
            writingTask1Prompt = obj.optString(
                "writingTask1Prompt",
                "Summarise the key trends in the academic data for this topic in at least 150 words."
            ),
            writingTask2Prompt = obj.optString(
                "writingTask2Prompt",
                "Discuss both perspectives on the central issue raised in this exam and present your own reasoned conclusion (at least 250 words)."
            ),
            speakingPrompts = speakingPrompts,
            questions = questions
        )
    }

    fun serializeExamPaper(exam: ExamPaper): JSONObject = JSONObject().apply {
        put("id", exam.id)
        put("title", exam.title)
        put("sourceLabel", exam.sourceLabel)
        put("year", exam.year)
        put("difficulty", exam.difficulty.name)
        put("topicVi", exam.topicVi)
        put("topicEn", exam.topicEn)
        put("isCustom", exam.isCustom)
        put("audioUri", exam.audioUri.orEmpty())
        put("audioDisplayName", exam.audioDisplayName.orEmpty())
        put("listeningTitle", exam.listeningTitle)
        put("listeningScript", JSONArray(exam.listeningScript))
        put("readingTitle", exam.readingTitle)
        put("readingParagraphs", JSONArray().apply {
            exam.readingParagraphs.forEach { p ->
                put(JSONObject().apply {
                    put("label", p.label)
                    put("sentences", JSONArray(p.sentences))
                })
            }
        })
        put("writingTask1Prompt", exam.writingTask1Prompt)
        put("writingTask2Prompt", exam.writingTask2Prompt)
        put("speakingPrompts", JSONArray(exam.speakingPrompts))
        put("questions", JSONArray().apply {
            exam.questions.forEach { q ->
                put(JSONObject().apply {
                    put("id", q.id)
                    put("number", q.number)
                    put("skill", q.skill.id)
                    put("type", q.type.id)
                    put("promptEn", q.promptEn)
                    put("options", JSONArray(q.options))
                    put("correctAnswer", q.correctAnswer)
                })
            }
        })
    }

    fun serializeCustomExams(exams: List<ExamPaper>): String =
        JSONArray().apply { exams.forEach { put(serializeExamPaper(it)) } }.toString()

    fun deserializeCustomExams(raw: String?): List<ExamPaper> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    add(parseSingleCustomExamJson(obj.toString()))
                }
            }
        }.getOrDefault(emptyList())
    }

    fun serializeHistory(records: List<ExamAttemptRecord>): String = JSONArray().apply {
        records.forEach { rec ->
            put(JSONObject().apply {
                put("id", rec.id)
                put("examId", rec.examId)
                put("examTitle", rec.examTitle)
                put("mode", rec.mode.id)
                put("submittedAtLabel", rec.submittedAtLabel)
                put("correctCount", rec.correctCount)
                put("totalObjectiveQuestions", rec.totalObjectiveQuestions)
                put("estimatedBand", rec.estimatedBand)
                put("durationUsedSeconds", rec.durationUsedSeconds)
                put("writingWordCount", rec.writingWordCount)
                put("writingDraft", rec.writingDraft)
                put("speakingTranscript", rec.speakingTranscript)
                put("objectiveAnswers", JSONObject(rec.objectiveAnswers))
                put("flaggedQuestionIds", JSONArray(rec.flaggedQuestionIds.toList()))
            })
        }
    }.toString()

    fun deserializeHistory(raw: String?): List<ExamAttemptRecord> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val answersObj = obj.optJSONObject("objectiveAnswers")
                    val answers = buildMap {
                        if (answersObj != null) {
                            val keys = answersObj.keys()
                            while (keys.hasNext()) {
                                val k = keys.next()
                                put(k, answersObj.optString(k, ""))
                            }
                        }
                    }
                    add(
                        ExamAttemptRecord(
                            id = obj.optString("id", "att_$i"),
                            examId = obj.optString("examId", ""),
                            examTitle = obj.optString("examTitle", "IELTS Mock Test"),
                            mode = ExamSkillMode.fromId(obj.optString("mode")),
                            submittedAtLabel = obj.optString("submittedAtLabel", ""),
                            correctCount = obj.optInt("correctCount", 0),
                            totalObjectiveQuestions = obj.optInt("totalObjectiveQuestions", 0),
                            estimatedBand = obj.optDouble("estimatedBand", 6.5),
                            durationUsedSeconds = obj.optInt("durationUsedSeconds", 0),
                            writingWordCount = obj.optInt("writingWordCount", 0),
                            writingDraft = obj.optString("writingDraft", ""),
                            speakingTranscript = obj.optString("speakingTranscript", ""),
                            objectiveAnswers = answers,
                            flaggedQuestionIds = obj.optStringList("flaggedQuestionIds").toSet()
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }
}

private fun JSONObject.optStringList(key: String): List<String> {
    val arr = optJSONArray(key) ?: return emptyList()
    return buildList {
        for (i in 0 until arr.length()) {
            val v = arr.optString(i).trim()
            if (v.isNotEmpty()) add(v)
        }
    }
}
