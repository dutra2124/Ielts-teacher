package com.example.ai

import com.example.data.importer.DocumentFileKind
import com.example.data.importer.MaterialImportCategory
import com.example.data.local.BulkImportDataset
import com.example.data.local.BulkImportPreview
import com.example.data.local.ExamEntity
import com.example.data.local.PersonalMaterialEntity
import com.example.data.local.SentenceDrillEntity
import com.example.data.local.VocabularyEntity
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gemini AI Structured Material Extractor powering Option B in AddMaterialsScreen:
 * "Dùng AI bóc tách tự động thành Câu hỏi / Từ vựng chuẩn"
 * Uses Firebase AI Logic (gemini-3.5-flash-lite) + GeminiResilientClient fallback,
 * producing a structured BulkImportPreview ready for live review and offline Room persistence.
 */
class MaterialImportAiService {

    private val extractorModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = generationConfig {
                temperature = 0.2f
                maxOutputTokens = 2048
                responseMimeType = "application/json"
            }
        )
    }

    suspend fun extractStructuredPreview(
        category: MaterialImportCategory,
        rawText: String,
        sourceFileName: String,
        fileKind: DocumentFileKind,
        moduleId: Int,
        attachedAudioPath: String? = null,
        attachedAudioName: String? = null
    ): BulkImportPreview = withContext(Dispatchers.IO) {
        val clipped = rawText.trim().take(12_000)
        require(clipped.length >= 15) {
            "Nội dung trích xuất quá ngắn để AI bóc tách. Vui lòng chọn tệp có nội dung dài hơn."
        }

        val nowId = System.currentTimeMillis()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        when (category) {
            MaterialImportCategory.VOCABULARY ->
                extractVocabularyWithAi(clipped, sourceFileName, fileKind, moduleId, nowId)
            MaterialImportCategory.SENTENCE_DRILLS ->
                extractSentenceDrillsWithAi(clipped, sourceFileName, fileKind, moduleId, nowId)
            MaterialImportCategory.READING_EXAM ->
                extractExamWithAi(
                    category = category,
                    rawText = clipped,
                    sourceFileName = sourceFileName,
                    fileKind = fileKind,
                    nowId = nowId,
                    audioPath = null,
                    audioName = null
                )
            MaterialImportCategory.LISTENING_EXAM ->
                extractExamWithAi(
                    category = category,
                    rawText = clipped,
                    sourceFileName = sourceFileName,
                    fileKind = fileKind,
                    nowId = nowId,
                    audioPath = attachedAudioPath,
                    audioName = attachedAudioName
                )
            MaterialImportCategory.WRITING_SPEAKING ->
                extractWritingSpeakingWithAi(clipped, sourceFileName, fileKind, nowId)
            MaterialImportCategory.PERSONAL_SELF_STUDY ->
                extractSelfStudyWithAi(clipped, sourceFileName, fileKind, moduleId, todayStr, nowId, attachedAudioPath, attachedAudioName)
        }
    }

    private suspend fun extractVocabularyWithAi(
        rawText: String,
        sourceFileName: String,
        fileKind: DocumentFileKind,
        moduleId: Int,
        nowId: Long
    ): BulkImportPreview {
        val schemaHint = """
            {
              "words": [
                {
                  "word": "string (C1-C2 academic English word or collocation)",
                  "part_of_speech": "noun|verb|adjective|adverb|phrase",
                  "cefr_level": "C1|C2",
                  "ipa_uk": "/.../",
                  "meaning_vi": "concise accurate Vietnamese meaning",
                  "example_en": "Band 8.5 academic sentence using the word",
                  "example_vi": "Vietnamese translation of example",
                  "collocation_stem_en": "short collocation with ______ blank",
                  "collocation_partner_en": "word that fills the blank",
                  "band6_paraphrase_en": "simpler Band 6.0 equivalent"
                }
              ]
            }
        """.trimIndent()

        val prompt = """
            Role: IELTS Band 8.5+ Lexical Resource Curator for Vietnamese learners.
            Task: Extract 6 to 12 highest-value C1–C2 academic vocabulary items or collocations from the document "$sourceFileName".
            Return STRICT JSON matching this schema:
            $schemaHint
            
            --- DOCUMENT TEXT ---
            $rawText
        """.trimIndent()

        val rawJson = callGeminiJson(prompt, schemaHint, "bóc tách từ vựng C1–C2")
        val root = JSONObject(rawJson)
        val arr = root.optJSONArray("words") ?: JSONArray()
        val entities = buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val word = obj.optString("word").trim()
                if (word.isBlank()) continue
                val ipa = obj.optString("ipa_uk").trim()
                val meaning = obj.optString("meaning_vi").trim().ifBlank { "Từ học thuật C1–C2" }
                val exampleEn = obj.optString("example_en").trim()
                val stem = obj.optString("collocation_stem_en").trim().ifBlank { "______" }
                val partner = obj.optString("collocation_partner_en").trim().ifBlank { word }
                add(
                    VocabularyEntity(
                        id = "ai_vocab_${nowId}_$i",
                        moduleId = moduleId.coerceIn(1, 12),
                        word = word,
                        partOfSpeech = obj.optString("part_of_speech").trim().ifBlank { "academic" },
                        cefrLevel = obj.optString("cefr_level").trim().ifBlank { "C1" },
                        ipaUk = ipa,
                        ipaUs = ipa,
                        meaningVi = meaning,
                        exampleEn = exampleEn,
                        exampleVi = obj.optString("example_vi").trim(),
                        wordFamily = word,
                        dependentPrepositions = "",
                        verbPatterns = "",
                        registerNoteVi = "Bóc tách bởi AI từ ${fileKind.badgeLabel}",
                        band6PitfallVi = "",
                        pronunciationFocusVi = "",
                        band6ParaphraseEn = obj.optString("band6_paraphrase_en").trim(),
                        collocationStemEn = stem,
                        collocationPartnerEn = partner,
                        clozeSentenceEn = exampleEn.replace(word, "______", ignoreCase = true)
                    )
                )
            }
        }
        require(entities.isNotEmpty()) {
            "AI chưa trích xuất được từ vựng nào từ tài liệu. Vui lòng thử lại hoặc dùng Lưu trực tiếp."
        }
        return BulkImportPreview(
            dataset = BulkImportDataset.VOCABULARY,
            formatLabel = "AI • ${fileKind.badgeLabel}",
            sourceFileName = sourceFileName,
            headers = listOf("Module", "Từ vựng", "CEFR / IPA", "Nghĩa tiếng Việt"),
            previewRows = entities.map {
                listOf("#${it.moduleId}", it.word, "${it.cefrLevel} ${it.ipaUk}".trim(), it.meaningVi)
            },
            validRowCount = entities.size,
            vocabEntities = entities
        )
    }

    private suspend fun extractSentenceDrillsWithAi(
        rawText: String,
        sourceFileName: String,
        fileKind: DocumentFileKind,
        moduleId: Int,
        nowId: Long
    ): BulkImportPreview {
        val schemaHint = """
            {
              "drills": [
                {
                  "structure_tag_vi": "short Vietnamese grammar tag e.g. Đảo ngữ Not only... but also",
                  "structure_name_en": "English grammar structure name",
                  "meaning_vi": "Vietnamese meaning of the complete sentence",
                  "ordered_chunks": ["chunk 1", "chunk 2", "chunk 3", "chunk 4", "chunk 5"],
                  "grammar_explanation_vi": "concise Vietnamese Band 8.5 GRA tip"
                }
              ]
            }
        """.trimIndent()

        val prompt = """
            Role: IELTS Band 8.5+ Grammatical Range & Accuracy Coach.
            Task: Extract or craft 4 to 8 high-scoring complex academic sentences (inversion, participle clauses, cleft sentences, concession, passive reporting) grounded in "$sourceFileName".
            Split each sentence into 4 to 6 clean syntactic chunks in "ordered_chunks".
            Return STRICT JSON matching this schema:
            $schemaHint
            
            --- DOCUMENT TEXT ---
            $rawText
        """.trimIndent()

        val rawJson = callGeminiJson(prompt, schemaHint, "bóc tách cấu trúc câu Band 8.5+")
        val arr = JSONObject(rawJson).optJSONArray("drills") ?: JSONArray()
        val entities = buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val chunksArr = obj.optJSONArray("ordered_chunks") ?: continue
                val ordered = (0 until chunksArr.length())
                    .map { chunksArr.optString(it).trim() }
                    .filter { it.isNotEmpty() }
                if (ordered.size < 3) continue
                val scrambled = ordered.reversed()
                add(
                    SentenceDrillEntity(
                        id = "ai_drill_${nowId}_$i",
                        moduleId = moduleId.coerceIn(1, 12),
                        structureTagVi = obj.optString("structure_tag_vi").trim().ifBlank { "Cấu trúc Band 8.5" },
                        structureNameEn = obj.optString("structure_name_en").trim().ifBlank { "Complex Syntax" },
                        meaningVi = obj.optString("meaning_vi").trim(),
                        orderedChunksJson = JSONArray(ordered).toString(),
                        scrambledChunksJson = JSONArray(scrambled).toString(),
                        grammarExplanationVi = obj.optString("grammar_explanation_vi").trim()
                    )
                )
            }
        }
        require(entities.isNotEmpty()) {
            "AI chưa bóc tách được bài tập câu nào. Vui lòng thử lại."
        }
        return BulkImportPreview(
            dataset = BulkImportDataset.SENTENCE_DRILLS,
            formatLabel = "AI • ${fileKind.badgeLabel}",
            sourceFileName = sourceFileName,
            headers = listOf("Module", "Cấu trúc GRA", "Số cụm", "Nghĩa tiếng Việt"),
            previewRows = entities.map {
                listOf("#${it.moduleId}", it.structureTagVi, "${JSONArray(it.orderedChunksJson).length()} cụm", it.meaningVi.ifBlank { it.structureNameEn })
            },
            validRowCount = entities.size,
            drillEntities = entities
        )
    }

    private suspend fun extractExamWithAi(
        category: MaterialImportCategory,
        rawText: String,
        sourceFileName: String,
        fileKind: DocumentFileKind,
        nowId: Long,
        audioPath: String?,
        audioName: String?
    ): BulkImportPreview {
        val isListening = category == MaterialImportCategory.LISTENING_EXAM
        val skillKey = if (isListening) "listening" else "reading"

        val schemaHint = """
            {
              "title": "Clean IELTS exam title",
              "topic_vi": "Short Vietnamese topic label",
              "topic_en": "Short English topic label",
              "reading_title": "Passage title",
              "reading_paragraphs": [{"label": "A", "sentences": ["sentence 1", "sentence 2"]}],
              "listening_title": "Listening section title",
              "listening_script": ["line 1", "line 2"],
              "questions": [
                {
                  "number": 1,
                  "skill": "$skillKey",
                  "type": "tfng|mcq|gap_fill",
                  "prompt_en": "Question prompt",
                  "options": ["TRUE", "FALSE", "NOT GIVEN"],
                  "correct_answer": "Exact answer key"
                }
              ]
            }
        """.trimIndent()

        val prompt = """
            Role: IELTS Exam Digitizer for ${category.subtitleEn}.
            Task: Parse "$sourceFileName" into an interactive IELTS $skillKey test JSON with 5 to 10 accurate questions (tfng, mcq, gap_fill) and verified answer keys.
            - If this is Reading: split the passage into labelled paragraphs (A, B, C...) with individual sentences.
            - If this is Listening: extract clean spoken dialogue/lecture lines into listening_script.
            - If the document lacks explicit questions, create 5 authentic IELTS $skillKey questions strictly grounded in the text.
            Return STRICT JSON matching this schema:
            $schemaHint
            
            --- DOCUMENT TEXT ---
            $rawText
        """.trimIndent()

        val rawJson = callGeminiJson(prompt, schemaHint, "bóc tách đề thi ${category.subtitleEn}")
        val obj = JSONObject(rawJson)
        val title = obj.optString("title").trim().ifBlank { sourceFileName.substringBeforeLast('.') }
        val paragraphs = obj.optJSONArray("reading_paragraphs") ?: JSONArray()
        val script = obj.optJSONArray("listening_script") ?: JSONArray()
        val rawQuestions = obj.optJSONArray("questions") ?: JSONArray()
        val normalizedQuestions = JSONArray()
        for (i in 0 until rawQuestions.length()) {
            val q = rawQuestions.optJSONObject(i) ?: continue
            val ans = q.optString("correct_answer").trim().ifBlank { "TRUE" }
            normalizedQuestions.put(
                JSONObject().apply {
                    put("id", "ai_${skillKey}_q_${i + 1}")
                    put("number", q.optInt("number", i + 1))
                    put("skill", q.optString("skill", skillKey).lowercase(Locale.US))
                    put("type", q.optString("type", "tfng").lowercase(Locale.US))
                    put("prompt_en", q.optString("prompt_en").trim())
                    put("options", q.optJSONArray("options") ?: JSONArray())
                    put("correct_answer", ans)
                    put("acceptable_answers", JSONArray(listOf(ans)))
                }
            )
        }

        val entity = ExamEntity(
            id = "ai_${skillKey}_$nowId",
            title = title,
            sourceLabel = "AI • ${fileKind.badgeLabel} ${if (isListening) "Listening" else "Reading"}",
            year = 2026,
            difficulty = "HARD",
            topicVi = obj.optString("topic_vi").trim().ifBlank { category.titleVi },
            topicEn = obj.optString("topic_en").trim().ifBlank { title },
            isCustom = true,
            audioUri = audioPath,
            audioDisplayName = audioName,
            listeningTitle = obj.optString("listening_title").trim().ifBlank { title },
            listeningScriptJson = script.toString(),
            readingTitle = obj.optString("reading_title").trim().ifBlank { title },
            readingParagraphsJson = paragraphs.toString(),
            writingTask1Prompt = "",
            writingTask2Prompt = "",
            speakingPromptsJson = "[]",
            questionsJson = normalizedQuestions.toString()
        )

        val previewRows = buildList {
            add(
                listOf(
                    entity.title,
                    if (isListening) "${script.length()} dòng script" else "${paragraphs.length()} đoạn",
                    "${normalizedQuestions.length()} câu hỏi chuẩn",
                    audioName ?: "Sẵn sàng thi Offline"
                )
            )
            for (i in 0 until minOf(normalizedQuestions.length(), 4)) {
                val q = normalizedQuestions.optJSONObject(i) ?: continue
                add(
                    listOf(
                        "Câu ${q.optInt("number", i + 1)}",
                        q.optString("type").uppercase(Locale.US),
                        q.optString("prompt_en").take(55),
                        "Đáp án: ${q.optString("correct_answer")}"
                    )
                )
            }
        }

        return BulkImportPreview(
            dataset = BulkImportDataset.EXAMS,
            formatLabel = "AI • ${fileKind.badgeLabel}",
            sourceFileName = sourceFileName,
            headers = listOf("Mục / Câu", "Chi tiết", "Nội dung bóc tách", "Đáp án / Audio"),
            previewRows = previewRows,
            validRowCount = 1,
            examEntities = listOf(entity)
        )
    }

    private suspend fun extractWritingSpeakingWithAi(
        rawText: String,
        sourceFileName: String,
        fileKind: DocumentFileKind,
        nowId: Long
    ): BulkImportPreview {
        val schemaHint = """
            {
              "title": "Clean IELTS Writing & Speaking set title",
              "topic_vi": "Vietnamese topic name",
              "topic_en": "English topic name",
              "writing_task1_prompt": "Official style Writing Task 1 prompt",
              "writing_task2_prompt": "Official style Writing Task 2 essay prompt",
              "speaking_prompts": [
                "Part 1: ...",
                "Part 2: Describe ...",
                "Part 3: ..."
              ]
            }
        """.trimIndent()

        val prompt = """
            Role: IELTS Writing & Speaking Examiner.
            Task: Extract or structure Writing Task 1, Writing Task 2, and Speaking Part 1–3 prompts from "$sourceFileName".
            Return STRICT JSON matching this schema:
            $schemaHint
            
            --- DOCUMENT TEXT ---
            $rawText
        """.trimIndent()

        val rawJson = callGeminiJson(prompt, schemaHint, "bóc tách đề Writing & Speaking")
        val obj = JSONObject(rawJson)
        val title = obj.optString("title").trim().ifBlank { "Writing & Speaking: ${sourceFileName.substringBeforeLast('.')}" }
        val task1 = obj.optString("writing_task1_prompt").trim()
        val task2 = obj.optString("writing_task2_prompt").trim()
        val speakingArr = obj.optJSONArray("speaking_prompts") ?: JSONArray()

        val entity = ExamEntity(
            id = "ai_ws_$nowId",
            title = title,
            sourceLabel = "AI • ${fileKind.badgeLabel} Prompts",
            year = 2026,
            difficulty = "HARD",
            topicVi = obj.optString("topic_vi").trim().ifBlank { "Writing & Speaking Band 8.5+" },
            topicEn = obj.optString("topic_en").trim().ifBlank { title },
            isCustom = true,
            audioUri = null,
            audioDisplayName = null,
            listeningTitle = "Listening Section",
            listeningScriptJson = "[]",
            readingTitle = title,
            readingParagraphsJson = "[]",
            writingTask1Prompt = task1,
            writingTask2Prompt = task2,
            speakingPromptsJson = speakingArr.toString(),
            questionsJson = "[]"
        )

        return BulkImportPreview(
            dataset = BulkImportDataset.EXAMS,
            formatLabel = "AI • ${fileKind.badgeLabel}",
            sourceFileName = sourceFileName,
            headers = listOf("Phần thi", "Đề bài đã bóc tách"),
            previewRows = listOf(
                listOf("Writing Task 1", task1.ifBlank { "—" }.take(90)),
                listOf("Writing Task 2", task2.ifBlank { "—" }.take(90)),
                listOf("Speaking Part 1–3", "${speakingArr.length()} câu hỏi Speaking")
            ),
            validRowCount = 1,
            examEntities = listOf(entity)
        )
    }

    private suspend fun extractSelfStudyWithAi(
        rawText: String,
        sourceFileName: String,
        fileKind: DocumentFileKind,
        moduleId: Int,
        todayStr: String,
        nowId: Long,
        audioPath: String?,
        audioName: String?
    ): BulkImportPreview {
        val schemaHint = """
            {
              "title": "Clean bilingual or English article title",
              "category": "Bài đọc tự học",
              "content_en": "Cleaned, well-formatted English reading text",
              "notes_vi": "Concise Vietnamese study notes + key Band 8.5 vocabulary takeaways",
              "tags": ["tag1", "tag2", "tag3"]
            }
        """.trimIndent()

        val prompt = """
            Role: IELTS Band 8.5+ Self-Study Curator.
            Task: Clean and format the self-study material from "$sourceFileName", write concise Vietnamese study notes highlighting key arguments & useful Band 8.5 expressions, and suggest 3–5 topic tags.
            Return STRICT JSON matching this schema:
            $schemaHint
            
            --- DOCUMENT TEXT ---
            $rawText
        """.trimIndent()

        val rawJson = callGeminiJson(prompt, schemaHint, "chuẩn hóa tài liệu tự học")
        val obj = JSONObject(rawJson)
        val tagsArr = obj.optJSONArray("tags") ?: JSONArray(listOf("self-study", "ielts-8.5"))
        val entity = PersonalMaterialEntity(
            id = "ai_pm_$nowId",
            moduleId = moduleId.coerceIn(1, 12),
            title = obj.optString("title").trim().ifBlank { sourceFileName.substringBeforeLast('.') },
            category = obj.optString("category").trim().ifBlank { "Bài đọc tự học" },
            contentEn = obj.optString("content_en").trim().ifBlank { rawText },
            notesVi = obj.optString("notes_vi").trim(),
            tagsJson = tagsArr.toString(),
            updatedAt = todayStr,
            audioUri = audioPath,
            audioDisplayName = audioName
        )

        return BulkImportPreview(
            dataset = BulkImportDataset.PERSONAL_MATERIALS,
            formatLabel = "AI • ${fileKind.badgeLabel}",
            sourceFileName = sourceFileName,
            headers = listOf("Module", "Tiêu đề chuẩn hóa", "Ghi chú AI"),
            previewRows = listOf(
                listOf("#${entity.moduleId}", entity.title, entity.notesVi.take(90))
            ),
            validRowCount = 1,
            materialEntities = listOf(entity)
        )
    }

    private suspend fun callGeminiJson(
        prompt: String,
        schemaHint: String,
        operationLabelVi: String
    ): String {
        return GeminiResilientClient.generateJsonWithFallback(
            primaryCall = {
                extractorModel.generateContent(prompt).text
            },
            prompt = prompt,
            jsonSchemaHint = schemaHint,
            temperature = 0.2f,
            maxOutputTokens = 2048,
            timeoutMs = 55_000L,
            operationLabelVi = operationLabelVi
        )
    }
}
