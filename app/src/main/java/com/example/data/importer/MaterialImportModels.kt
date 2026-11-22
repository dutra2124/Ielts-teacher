package com.example.data.importer

import com.example.data.local.BulkImportDataset
import com.example.data.local.BulkImportPreview
import com.example.data.local.ExamEntity
import com.example.data.local.PersonalMaterialEntity
import com.example.data.local.SentenceDrillEntity
import com.example.data.local.VocabularyEntity
import com.example.data.sync.BulkDataImporter
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Six structured IELTS Material Categories inside Account Sidebar -> "Thêm tài liệu" (Add Materials Hub).
 * Each entry explicitly explains which IELTS exam section & scoring criterion it serves.
 */
enum class MaterialImportCategory(
    val id: String,
    val titleVi: String,
    val subtitleEn: String,
    val ieltsPurposeVi: String,
    val supportedFormats: List<String>,
    val targetDataset: BulkImportDataset,
    val supportsAudioAttachment: Boolean = false,
    val supportsLinkInput: Boolean = false
) {
    VOCABULARY(
        id = "vocabulary",
        titleVi = "Từ vựng C1–C2",
        subtitleEn = "Vocabulary",
        ieltsPurposeVi = "Phục vụ cả 4 kỹ năng — nâng điểm Lexical Resource cho Writing & Speaking, tăng tốc nhận diện Paraphrase trong Reading.",
        supportedFormats = listOf("CSV", "JSON", "PDF", "DOCX"),
        targetDataset = BulkImportDataset.VOCABULARY
    ),
    SENTENCE_DRILLS(
        id = "sentence_drills",
        titleVi = "Luyện cấu trúc câu",
        subtitleEn = "Sentence Drills",
        ieltsPurposeVi = "Phục vụ Writing Task 1 & 2 và Speaking Part 3 — cải thiện tiêu chí Grammatical Range & Accuracy.",
        supportedFormats = listOf("CSV", "JSON", "DOCX", "PDF"),
        targetDataset = BulkImportDataset.SENTENCE_DRILLS
    ),
    READING_EXAM(
        id = "reading_exam",
        titleVi = "Đề thi Reading",
        subtitleEn = "Reading Exam",
        ieltsPurposeVi = "Phục vụ kỹ năng Reading (3 Passages, 40 câu) — luyện quản lý thời gian 60 phút và xử lý bẫy Distractor.",
        supportedFormats = listOf("PDF", "DOCX", "JSON", "CSV"),
        targetDataset = BulkImportDataset.EXAMS
    ),
    LISTENING_EXAM(
        id = "listening_exam",
        titleVi = "Đề thi Listening",
        subtitleEn = "Listening Exam",
        ieltsPurposeVi = "Phục vụ kỹ năng Listening (4 Sections) — luyện nghe tốc độ cao và Smart Dictation.",
        supportedFormats = listOf("PDF", "DOCX", "JSON", "MP3/WAV"),
        targetDataset = BulkImportDataset.EXAMS,
        supportsAudioAttachment = true
    ),
    WRITING_SPEAKING(
        id = "writing_speaking",
        titleVi = "Đề thi Writing & Speaking",
        subtitleEn = "Writing / Speaking Prompts",
        ieltsPurposeVi = "Phục vụ Writing Task 1-2 và Speaking Part 1-3 — luyện dàn ý, chấm 4 tiêu chí và nâng cấp Band 8.5+.",
        supportedFormats = listOf("PDF", "DOCX", "JSON", "TXT"),
        targetDataset = BulkImportDataset.EXAMS
    ),
    PERSONAL_SELF_STUDY(
        id = "personal_self_study",
        titleVi = "Tài liệu Tự học / Bài báo",
        subtitleEn = "Personal Self-Study",
        ieltsPurposeVi = "Phục vụ đọc mở rộng kiến thức nền cho 12 chủ đề đời sống và tự tạo Flashcard.",
        supportedFormats = listOf("PDF", "DOCX", "TXT", "Link"),
        targetDataset = BulkImportDataset.PERSONAL_MATERIALS,
        supportsAudioAttachment = true,
        supportsLinkInput = true
    )
}

/**
 * 100% offline converter for Option A ("Lưu trực tiếp vào mục đã chọn").
 * Converts extracted PDF/DOCX/TXT/CSV/JSON/Link content directly into Room-ready entities
 * with a live preview before saving into the local database.
 */
object MaterialDirectConverter {

    fun buildDirectPreview(
        category: MaterialImportCategory,
        extractedText: String,
        sourceFileName: String,
        fileKind: DocumentFileKind,
        moduleId: Int,
        attachedAudioPath: String? = null,
        attachedAudioName: String? = null
    ): Result<BulkImportPreview> = runCatching {
        val cleaned = extractedText.trim()
        require(cleaned.isNotBlank()) {
            "Chưa có nội dung văn bản để xem trước. Vui lòng chọn tệp PDF/DOCX/CSV/JSON hoặc dán nội dung."
        }

        // Fast structured path when user uploaded genuine CSV or JSON
        val looksJson = cleaned.startsWith("[") || cleaned.startsWith("{") || fileKind == DocumentFileKind.JSON
        val looksCsv = fileKind == DocumentFileKind.CSV
        if (looksJson || looksCsv) {
            val structured = BulkDataImporter.parseAndPreview(
                dataset = category.targetDataset,
                rawContent = cleaned,
                sourceFileName = sourceFileName
            )
            if (structured.isSuccess) {
                val preview = structured.getOrThrow()
                if (attachedAudioPath != null && preview.dataset == BulkImportDataset.EXAMS) {
                    val patchedExams = preview.examEntities.map {
                        it.copy(audioUri = attachedAudioPath, audioDisplayName = attachedAudioName)
                    }
                    return@runCatching preview.copy(examEntities = patchedExams)
                }
                return@runCatching preview
            }
        }

        val nowId = System.currentTimeMillis()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val baseTitle = sourceFileName
            .substringBeforeLast('.')
            .replace('_', ' ')
            .replace('-', ' ')
            .trim()
            .ifBlank { "${category.titleVi} ($todayStr)" }

        when (category) {
            MaterialImportCategory.VOCABULARY -> convertDirectVocabulary(cleaned, sourceFileName, fileKind, moduleId, nowId)
            MaterialImportCategory.SENTENCE_DRILLS -> convertDirectSentenceDrills(cleaned, sourceFileName, fileKind, moduleId, nowId)
            MaterialImportCategory.READING_EXAM -> convertDirectReadingExam(cleaned, baseTitle, sourceFileName, fileKind, nowId)
            MaterialImportCategory.LISTENING_EXAM -> convertDirectListeningExam(
                cleaned, baseTitle, sourceFileName, fileKind, nowId, attachedAudioPath, attachedAudioName
            )
            MaterialImportCategory.WRITING_SPEAKING -> convertDirectWritingSpeakingExam(cleaned, baseTitle, sourceFileName, fileKind, nowId)
            MaterialImportCategory.PERSONAL_SELF_STUDY -> convertDirectSelfStudy(
                cleaned, baseTitle, sourceFileName, fileKind, moduleId, todayStr, nowId, attachedAudioPath, attachedAudioName
            )
        }
    }

    private fun convertDirectVocabulary(
        text: String,
        sourceFileName: String,
        fileKind: DocumentFileKind,
        moduleId: Int,
        nowId: Long
    ): BulkImportPreview {
        val lines = text.lines()
            .map { it.trim().replace(Regex("^\\d+[.)]\\s*"), "") }
            .filter { it.length >= 3 }

        val entities = lines.mapIndexedNotNull { idx, line ->
            val sepRegex = Regex("\\s*(?:\\||:|—|–| - )\\s*")
            val parts = line.split(sepRegex).map { it.trim() }.filter { it.isNotEmpty() }
            val rawWord = parts.firstOrNull().orEmpty()
                .replace(Regex("\\([^)]*\\)"), "")
                .trim()
            if (rawWord.length !in 2..42) return@mapIndexedNotNull null
            val meaning = parts.drop(1).joinToString(" • ").ifBlank { "Từ vựng học thuật trích từ $sourceFileName" }
            VocabularyEntity(
                id = "doc_vocab_${nowId}_$idx",
                moduleId = moduleId.coerceIn(1, 12),
                word = rawWord,
                partOfSpeech = "academic",
                cefrLevel = "C1",
                ipaUk = "",
                ipaUs = "",
                meaningVi = meaning,
                exampleEn = if (parts.size >= 3) parts[2] else "Academic usage of \"$rawWord\" in IELTS contexts.",
                exampleVi = meaning,
                wordFamily = rawWord,
                dependentPrepositions = "",
                verbPatterns = "",
                registerNoteVi = "Nhập trực tiếp từ ${fileKind.badgeLabel}",
                band6PitfallVi = "",
                pronunciationFocusVi = "",
                band6ParaphraseEn = "",
                collocationStemEn = "______",
                collocationPartnerEn = rawWord,
                clozeSentenceEn = "This academic passage highlights the role of ______."
            )
        }.take(60)

        require(entities.isNotEmpty()) {
            "Chưa tách được dòng từ vựng nào theo định dạng 'word - nghĩa' từ tài liệu. Hãy dùng nút \"Dùng AI bóc tách tự động\" để AI lọc từ C1–C2 chuẩn."
        }

        return BulkImportPreview(
            dataset = BulkImportDataset.VOCABULARY,
            formatLabel = "${fileKind.badgeLabel} (Lưu trực tiếp)",
            sourceFileName = sourceFileName,
            headers = listOf("Module", "Từ vựng", "CEFR", "Nghĩa / Ghi chú"),
            previewRows = entities.take(25).map {
                listOf("#${it.moduleId}", it.word, it.cefrLevel, it.meaningVi)
            },
            validRowCount = entities.size,
            vocabEntities = entities
        )
    }

    private fun convertDirectSentenceDrills(
        text: String,
        sourceFileName: String,
        fileKind: DocumentFileKind,
        moduleId: Int,
        nowId: Long
    ): BulkImportPreview {
        val candidateSentences = text
            .split(Regex("(?<=[.!?])\\s+|\n+"))
            .map { it.trim().replace(Regex("^\\d+[.)]\\s*"), "") }
            .filter { s ->
                val words = s.split(Regex("\\s+")).filter { it.isNotBlank() }
                words.size in 7..36
            }
            .distinct()
            .take(25)

        require(candidateSentences.isNotEmpty()) {
            "Tài liệu chưa có câu tiếng Anh hoàn chỉnh (7–36 từ) để tạo bài luyện cấu trúc. Hãy thử \"Dùng AI bóc tách tự động\"."
        }

        val entities = candidateSentences.mapIndexed { idx, sentence ->
            val words = sentence.split(Regex("\\s+")).filter { it.isNotBlank() }
            val chunkCount = when {
                words.size <= 10 -> 4
                words.size <= 18 -> 5
                else -> 6
            }
            val step = (words.size.toFloat() / chunkCount).coerceAtLeast(1f)
            val ordered = buildList {
                var cursor = 0
                for (c in 0 until chunkCount) {
                    val end = if (c == chunkCount - 1) words.size else ((c + 1) * step).toInt().coerceAtMost(words.size)
                    if (cursor < end) {
                        add(words.subList(cursor, end).joinToString(" "))
                        cursor = end
                    }
                }
            }
            val scrambled = if (ordered.size >= 2) ordered.reversed() else ordered
            SentenceDrillEntity(
                id = "doc_drill_${nowId}_$idx",
                moduleId = moduleId.coerceIn(1, 12),
                structureTagVi = "Cấu trúc học thuật #${idx + 1}",
                structureNameEn = "Academic Clause (${fileKind.badgeLabel})",
                meaningVi = "Sắp xếp cụm từ thành câu hoàn chỉnh chuẩn Band 8.5",
                orderedChunksJson = JSONArray(ordered).toString(),
                scrambledChunksJson = JSONArray(scrambled).toString(),
                grammarExplanationVi = "Trích trực tiếp từ tệp $sourceFileName để luyện Grammatical Range & Accuracy."
            )
        }

        return BulkImportPreview(
            dataset = BulkImportDataset.SENTENCE_DRILLS,
            formatLabel = "${fileKind.badgeLabel} (Lưu trực tiếp)",
            sourceFileName = sourceFileName,
            headers = listOf("Module", "Nhãn cấu trúc", "Số cụm từ", "Câu luyện"),
            previewRows = entities.take(25).mapIndexed { i, it ->
                listOf("#${it.moduleId}", it.structureTagVi, "${JSONArray(it.orderedChunksJson).length()} cụm", candidateSentences[i].take(65))
            },
            validRowCount = entities.size,
            drillEntities = entities
        )
    }

    private fun convertDirectReadingExam(
        text: String,
        baseTitle: String,
        sourceFileName: String,
        fileKind: DocumentFileKind,
        nowId: Long
    ): BulkImportPreview {
        val rawBlocks = text.split(Regex("\n{2,}")).map { it.trim() }.filter { it.length >= 20 }
        val paragraphBlocks = rawBlocks.ifEmpty { listOf(text) }.take(8)
        val paragraphsJson = JSONArray().apply {
            paragraphBlocks.forEachIndexed { idx, block ->
                val label = ('A' + idx).toString()
                val sentences = block.split(Regex("(?<=[.!?])\\s+"))
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                put(
                    JSONObject().apply {
                        put("label", label)
                        put("sentences", JSONArray(sentences.ifEmpty { listOf(block) }))
                    }
                )
            }
        }

        val detectedQuestions = extractHeuristicQuestions(text, skill = "reading")
        val exam = ExamEntity(
            id = "doc_reading_$nowId",
            title = "Reading: $baseTitle",
            sourceLabel = "${fileKind.badgeLabel} Reading Import",
            year = 2026,
            difficulty = "HARD",
            topicVi = "Đề Reading tự nạp (${paragraphBlocks.size} đoạn)",
            topicEn = baseTitle,
            isCustom = true,
            audioUri = null,
            audioDisplayName = null,
            listeningTitle = "Listening Section",
            listeningScriptJson = "[]",
            readingTitle = baseTitle,
            readingParagraphsJson = paragraphsJson.toString(),
            writingTask1Prompt = "",
            writingTask2Prompt = "",
            speakingPromptsJson = "[]",
            questionsJson = detectedQuestions.toString()
        )

        return BulkImportPreview(
            dataset = BulkImportDataset.EXAMS,
            formatLabel = "${fileKind.badgeLabel} Reading",
            sourceFileName = sourceFileName,
            headers = listOf("Tiêu đề đề Reading", "Số đoạn", "Câu hỏi tách được", "Chế độ"),
            previewRows = listOf(
                listOf(exam.title, "${paragraphBlocks.size} đoạn (A–${('A' + paragraphBlocks.lastIndex)})", "${detectedQuestions.length()} câu", "Offline Ready")
            ),
            validRowCount = 1,
            warningVi = if (detectedQuestions.length() <= 1) {
                "Mẹo: Nhấn \"Dùng AI bóc tách tự động\" nếu bạn muốn AI tạo đủ bộ câu hỏi TFNG / MCQ / Gap-fill kèm Đáp án từ tệp này."
            } else null,
            examEntities = listOf(exam)
        )
    }

    private fun convertDirectListeningExam(
        text: String,
        baseTitle: String,
        sourceFileName: String,
        fileKind: DocumentFileKind,
        nowId: Long,
        audioPath: String?,
        audioName: String?
    ): BulkImportPreview {
        val scriptLines = text.lines()
            .map { it.trim() }
            .filter { it.length >= 6 }
            .take(40)
        val detectedQuestions = extractHeuristicQuestions(text, skill = "listening")

        val exam = ExamEntity(
            id = "doc_listening_$nowId",
            title = "Listening: $baseTitle",
            sourceLabel = "${fileKind.badgeLabel} Listening Import",
            year = 2026,
            difficulty = "HARD",
            topicVi = "Đề Listening tự nạp${if (audioName != null) " + Audio" else ""}",
            topicEn = baseTitle,
            isCustom = true,
            audioUri = audioPath,
            audioDisplayName = audioName,
            listeningTitle = baseTitle,
            listeningScriptJson = JSONArray(scriptLines).toString(),
            readingTitle = baseTitle,
            readingParagraphsJson = "[]",
            writingTask1Prompt = "",
            writingTask2Prompt = "",
            speakingPromptsJson = "[]",
            questionsJson = detectedQuestions.toString()
        )

        return BulkImportPreview(
            dataset = BulkImportDataset.EXAMS,
            formatLabel = "${fileKind.badgeLabel} Listening",
            sourceFileName = sourceFileName,
            headers = listOf("Tiêu đề Listening", "Số dòng Script", "Tệp Audio", "Câu hỏi"),
            previewRows = listOf(
                listOf(exam.title, "${scriptLines.size} dòng", audioName ?: "TTS tự động", "${detectedQuestions.length()} câu")
            ),
            validRowCount = 1,
            examEntities = listOf(exam)
        )
    }

    private fun convertDirectWritingSpeakingExam(
        text: String,
        baseTitle: String,
        sourceFileName: String,
        fileKind: DocumentFileKind,
        nowId: Long
    ): BulkImportPreview {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val task1 = lines.firstOrNull { it.contains("task 1", ignoreCase = true) || it.contains("chart", ignoreCase = true) || it.contains("graph", ignoreCase = true) }
            ?: "Task 1: Summarise the key trends and make relevant comparisons from the provided prompt."
        val task2 = lines.firstOrNull { it.contains("task 2", ignoreCase = true) || it.contains("to what extent", ignoreCase = true) || it.contains("discuss both", ignoreCase = true) }
            ?: text.take(420)
        val speakingLines = lines.filter {
            it.contains("part 1", ignoreCase = true) ||
                it.contains("part 2", ignoreCase = true) ||
                it.contains("part 3", ignoreCase = true) ||
                it.contains("describe ", ignoreCase = true) ||
                it.endsWith("?")
        }.take(6).ifEmpty {
            listOf(
                "Part 1: What aspects of this topic interest you most?",
                "Part 2: Describe a real-life example related to $baseTitle.",
                "Part 3: How might this issue evolve over the next decade?"
            )
        }

        val exam = ExamEntity(
            id = "doc_ws_$nowId",
            title = "Writing & Speaking: $baseTitle",
            sourceLabel = "${fileKind.badgeLabel} Prompts",
            year = 2026,
            difficulty = "HARD",
            topicVi = "Bộ đề Writing & Speaking tự nạp",
            topicEn = baseTitle,
            isCustom = true,
            audioUri = null,
            audioDisplayName = null,
            listeningTitle = "Listening Section",
            listeningScriptJson = "[]",
            readingTitle = baseTitle,
            readingParagraphsJson = "[]",
            writingTask1Prompt = task1,
            writingTask2Prompt = task2,
            speakingPromptsJson = JSONArray(speakingLines).toString(),
            questionsJson = "[]"
        )

        return BulkImportPreview(
            dataset = BulkImportDataset.EXAMS,
            formatLabel = "${fileKind.badgeLabel} Writing/Speaking",
            sourceFileName = sourceFileName,
            headers = listOf("Bộ đề", "Writing Task 2", "Speaking Prompts"),
            previewRows = listOf(
                listOf(exam.title, task2.take(65), "${speakingLines.size} câu hỏi Part 1–3")
            ),
            validRowCount = 1,
            examEntities = listOf(exam)
        )
    }

    private fun convertDirectSelfStudy(
        text: String,
        baseTitle: String,
        sourceFileName: String,
        fileKind: DocumentFileKind,
        moduleId: Int,
        todayStr: String,
        nowId: Long,
        audioPath: String?,
        audioName: String?
    ): BulkImportPreview {
        val isLink = fileKind == DocumentFileKind.LINK ||
            text.startsWith("http://", ignoreCase = true) ||
            text.startsWith("https://", ignoreCase = true)

        val entity = PersonalMaterialEntity(
            id = "doc_pm_$nowId",
            moduleId = moduleId.coerceIn(1, 12),
            title = baseTitle,
            category = if (isLink) "Bài báo & Link" else "Bài đọc tự học",
            contentEn = text,
            notesVi = if (isLink) {
                "Liên kết tự học đã lưu offline — mở trong Thư viện cá nhân để đọc và tạo Flashcard."
            } else {
                "Nhập trực tiếp từ ${fileKind.badgeLabel} ($sourceFileName). Bôi đen từ học thuật trong Thư viện cá nhân để tạo Flashcard."
            },
            tagsJson = JSONArray(listOf(fileKind.badgeLabel.lowercase(Locale.US), "self-study", "module-$moduleId")).toString(),
            updatedAt = todayStr,
            audioUri = audioPath,
            audioDisplayName = audioName
        )

        return BulkImportPreview(
            dataset = BulkImportDataset.PERSONAL_MATERIALS,
            formatLabel = "${fileKind.badgeLabel} Tự học",
            sourceFileName = sourceFileName,
            headers = listOf("Module", "Tiêu đề tài liệu", "Loại", "Độ dài"),
            previewRows = listOf(
                listOf("#${entity.moduleId}", entity.title, entity.category, "${text.split(Regex("\\s+")).size} từ")
            ),
            validRowCount = 1,
            materialEntities = listOf(entity)
        )
    }

    private fun extractHeuristicQuestions(text: String, skill: String): JSONArray {
        val qRegex = Regex("^(?:Q(?:uestion)?\\s*)?(\\d{1,2})[.):]\\s+(.{12,240})$")
        val matches = text.lines()
            .mapNotNull { line ->
                val m = qRegex.find(line.trim()) ?: return@mapNotNull null
                val num = m.groupValues[1].toIntOrNull() ?: return@mapNotNull null
                val prompt = m.groupValues[2].trim()
                num to prompt
            }
            .take(15)

        val arr = JSONArray()
        if (matches.isEmpty()) {
            arr.put(
                JSONObject().apply {
                    put("id", "${skill}_q_1")
                    put("number", 1)
                    put("skill", skill)
                    put("type", "tfng")
                    put("prompt_en", "The author presents evidence supporting the main argument of this passage.")
                    put("options", JSONArray(listOf("TRUE", "FALSE", "NOT GIVEN")))
                    put("correct_answer", "TRUE")
                    put("acceptable_answers", JSONArray(listOf("TRUE")))
                }
            )
            return arr
        }

        matches.forEachIndexed { idx, (num, prompt) ->
            val isGap = prompt.contains("____") || prompt.contains("...")
            val type = if (isGap) "gap_fill" else "tfng"
            arr.put(
                JSONObject().apply {
                    put("id", "${skill}_q_${idx + 1}")
                    put("number", num)
                    put("skill", skill)
                    put("type", type)
                    put("prompt_en", prompt)
                    put("options", if (isGap) JSONArray() else JSONArray(listOf("TRUE", "FALSE", "NOT GIVEN")))
                    put("correct_answer", if (isGap) "answer" else "TRUE")
                    put("acceptable_answers", JSONArray(listOf(if (isGap) "answer" else "TRUE")))
                }
            )
        }
        return arr
    }
}
