package com.example.data.sync

import com.example.data.local.BulkImportDataset
import com.example.data.local.BulkImportPreview
import com.example.data.local.BundledJsonSeedLoader
import com.example.data.local.ExamEntity
import com.example.data.local.PersonalMaterialEntity
import com.example.data.local.SentenceDrillEntity
import com.example.data.local.VocabularyEntity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * In-App CSV / JSON Bulk Importer & Validator for ApexIELTS 8.5.
 * Supports UTF-8 Vietnamese text, RFC-4180 quoted commas, and structured JSON arrays/objects
 * across all 4 target datasets: Vocabulary, Sentence Drills, Exams, and Personal Materials.
 */
object BulkDataImporter {

    fun parseAndPreview(
        dataset: BulkImportDataset,
        rawContent: String,
        sourceFileName: String
    ): Result<BulkImportPreview> = runCatching {
        val cleaned = rawContent.removePrefix("\uFEFF").trim()
        require(cleaned.isNotEmpty()) {
            "Nội dung tệp trống. Vui lòng chọn tệp CSV / JSON có dữ liệu hoặc dán văn bản trực tiếp."
        }

        val isJson = cleaned.startsWith("[") || cleaned.startsWith("{") ||
            sourceFileName.lowercase(Locale.US).endsWith(".json")

        if (isJson) {
            parseJsonDataset(dataset, cleaned, sourceFileName)
        } else {
            parseCsvDataset(dataset, cleaned, sourceFileName)
        }
    }

    // ---- JSON Validator & Parser ----

    private fun parseJsonDataset(
        dataset: BulkImportDataset,
        cleanedJson: String,
        sourceFileName: String
    ): BulkImportPreview {
        val jsonArray = extractJsonArray(cleanedJson, dataset)
        require(jsonArray.length() > 0) {
            "Tệp JSON hợp lệ nhưng chưa có bản ghi nào cho mục ${dataset.shortLabelVi}."
        }

        val objects = buildList {
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i)
                if (obj != null) add(obj)
            }
        }
        require(objects.isNotEmpty()) {
            "Mỗi phần tử trong mảng JSON phải là một đối tượng { ... }."
        }

        val skipped = jsonArray.length() - objects.size
        return when (dataset) {
            BulkImportDataset.VOCABULARY -> {
                val entities = objects.mapNotNull { obj ->
                    val parsed = BundledJsonSeedLoader.parseVocabObject(obj)
                    if (parsed.word.isBlank()) null else parsed
                }
                require(entities.isNotEmpty()) {
                    "Không tìm thấy cột 'word' hợp lệ trong tệp JSON Từ vựng."
                }
                val invalid = objects.size - entities.size + skipped
                BulkImportPreview(
                    dataset = dataset,
                    formatLabel = "JSON",
                    sourceFileName = sourceFileName,
                    headers = listOf("id", "module_id", "word", "cefr", "meaning_vi"),
                    previewRows = entities.take(25).map {
                        listOf(it.id, "#${it.moduleId}", it.word, it.cefrLevel, it.meaningVi.ifBlank { "—" })
                    },
                    validRowCount = entities.size,
                    skippedRowCount = invalid,
                    warningVi = if (invalid > 0) "Đã bỏ qua $invalid dòng thiếu trường 'word'." else null,
                    vocabEntities = entities
                )
            }

            BulkImportDataset.SENTENCE_DRILLS -> {
                val entities = objects.mapNotNull { obj ->
                    val parsed = BundledJsonSeedLoader.parseSentenceDrillObject(obj)
                    val hasChunks = parsed.orderedChunksJson.trim().length > 2
                    if (!hasChunks) null else parsed
                }
                require(entities.isNotEmpty()) {
                    "Không tìm thấy mảng 'ordered_chunks' hợp lệ trong tệp JSON Bài tập câu."
                }
                val invalid = objects.size - entities.size + skipped
                BulkImportPreview(
                    dataset = dataset,
                    formatLabel = "JSON",
                    sourceFileName = sourceFileName,
                    headers = listOf("id", "module_id", "structure_tag_vi", "structure_name_en", "meaning_vi"),
                    previewRows = entities.take(25).map {
                        listOf(it.id, "#${it.moduleId}", it.structureTagVi, it.structureNameEn, it.meaningVi.ifBlank { "—" })
                    },
                    validRowCount = entities.size,
                    skippedRowCount = invalid,
                    warningVi = if (invalid > 0) "Đã bỏ qua $invalid dòng thiếu 'ordered_chunks'." else null,
                    drillEntities = entities
                )
            }

            BulkImportDataset.EXAMS -> {
                val entities = objects.mapNotNull { obj ->
                    val parsed = BundledJsonSeedLoader.parseExamObject(obj)
                    if (parsed.title.isBlank()) null else parsed
                }
                require(entities.isNotEmpty()) {
                    "Không tìm thấy trường 'title' hợp lệ trong tệp JSON Đề thi."
                }
                val invalid = objects.size - entities.size + skipped
                BulkImportPreview(
                    dataset = dataset,
                    formatLabel = "JSON",
                    sourceFileName = sourceFileName,
                    headers = listOf("id", "title", "year", "difficulty", "topic_vi"),
                    previewRows = entities.take(25).map {
                        listOf(it.id, it.title, it.year.toString(), it.difficulty, it.topicVi)
                    },
                    validRowCount = entities.size,
                    skippedRowCount = invalid,
                    warningVi = if (invalid > 0) "Đã bỏ qua $invalid đề thiếu tiêu đề." else null,
                    examEntities = entities
                )
            }

            BulkImportDataset.PERSONAL_MATERIALS -> {
                val entities = objects.mapNotNull { obj ->
                    val parsed = BundledJsonSeedLoader.parsePersonalMaterialObject(obj)
                    if (parsed.title.isBlank() && parsed.contentEn.isBlank()) null else parsed
                }
                require(entities.isNotEmpty()) {
                    "Không tìm thấy trường 'title' hoặc 'content_en' hợp lệ trong tệp JSON Tài liệu."
                }
                val invalid = objects.size - entities.size + skipped
                BulkImportPreview(
                    dataset = dataset,
                    formatLabel = "JSON",
                    sourceFileName = sourceFileName,
                    headers = listOf("id", "module_id", "title", "category", "updated_at"),
                    previewRows = entities.take(25).map {
                        listOf(it.id, "#${it.moduleId}", it.title, it.category, it.updatedAt)
                    },
                    validRowCount = entities.size,
                    skippedRowCount = invalid,
                    warningVi = if (invalid > 0) "Đã bỏ qua $invalid dòng trống." else null,
                    materialEntities = entities
                )
            }
        }
    }

    private fun extractJsonArray(raw: String, dataset: BulkImportDataset): JSONArray {
        return try {
            if (raw.startsWith("[")) {
                JSONArray(raw)
            } else {
                val root = JSONObject(raw)
                val preferredKeys = when (dataset) {
                    BulkImportDataset.VOCABULARY -> listOf("vocabulary", "words", "items", "data")
                    BulkImportDataset.SENTENCE_DRILLS -> listOf("sentence_drills", "drills", "exercises", "items", "data")
                    BulkImportDataset.EXAMS -> listOf("exams", "tests", "items", "data")
                    BulkImportDataset.PERSONAL_MATERIALS -> listOf("personal_materials", "materials", "notes", "items", "data")
                }
                preferredKeys.firstNotNullOfOrNull { key -> root.optJSONArray(key) }
                    ?: JSONArray().put(root)
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Cú pháp JSON không hợp lệ: ${e.message ?: "kiểm tra lại dấu ngoặc và dấu phẩy."}")
        }
    }

    // ---- UTF-8 CSV Parser & Mapper ----

    private fun parseCsvDataset(
        dataset: BulkImportDataset,
        cleanedCsv: String,
        sourceFileName: String
    ): BulkImportPreview {
        val rows = CsvTableParser.parseToRowMaps(cleanedCsv)
        require(rows.isNotEmpty()) {
            "Không đọc được dòng dữ liệu CSV nào. Hãy đảm bảo dòng đầu tiên là tiêu đề cột (VD: id, module_id, ...)."
        }

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        return when (dataset) {
            BulkImportDataset.VOCABULARY -> {
                val entities = rows.mapIndexedNotNull { idx, row ->
                    val word = row["word"].orEmpty().trim()
                    if (word.isBlank()) return@mapIndexedNotNull null
                    val modId = (row["module_id"]?.toIntOrNull() ?: 1).coerceIn(1, 12)
                    val id = row["id"].orEmpty().ifBlank {
                        "csv_m${modId}_${word.lowercase(Locale.US).replace(Regex("[^a-z0-9]+"), "_")}_$idx"
                    }
                    VocabularyEntity(
                        id = id,
                        moduleId = modId,
                        word = word,
                        partOfSpeech = row["part_of_speech"].orEmpty().ifBlank { "noun" },
                        cefrLevel = row["cefr_level"].orEmpty().ifBlank { "C1" },
                        ipaUk = row["ipa_uk"].orEmpty(),
                        ipaUs = row["ipa_us"].orEmpty().ifBlank { row["ipa_uk"].orEmpty() },
                        meaningVi = row["meaning_vi"].orEmpty().ifBlank { "Từ học thuật C1–C2" },
                        exampleEn = row["example_en"].orEmpty(),
                        exampleVi = row["example_vi"].orEmpty(),
                        wordFamily = row["word_family"].orEmpty(),
                        dependentPrepositions = row["dependent_prepositions"].orEmpty(),
                        verbPatterns = row["verb_patterns"].orEmpty(),
                        registerNoteVi = row["register_note_vi"].orEmpty().ifBlank { "Academic Formal" },
                        band6PitfallVi = row["band6_pitfall_vi"].orEmpty(),
                        pronunciationFocusVi = row["pronunciation_focus_vi"].orEmpty(),
                        band6ParaphraseEn = row["band6_paraphrase_en"].orEmpty(),
                        collocationStemEn = row["collocation_stem_en"].orEmpty().ifBlank { "______" },
                        collocationPartnerEn = row["collocation_partner_en"].orEmpty().ifBlank { word },
                        clozeSentenceEn = row["cloze_sentence_en"].orEmpty()
                    )
                }
                require(entities.isNotEmpty()) {
                    "Tệp CSV thiếu cột 'word' hoặc tất cả các dòng đều trống. Hãy tải file mẫu CSV để xem đúng tên cột."
                }
                val skipped = rows.size - entities.size
                BulkImportPreview(
                    dataset = dataset,
                    formatLabel = "CSV (UTF-8)",
                    sourceFileName = sourceFileName,
                    headers = listOf("id", "module_id", "word", "cefr_level", "meaning_vi"),
                    previewRows = entities.take(25).map {
                        listOf(it.id, "#${it.moduleId}", it.word, it.cefrLevel, it.meaningVi)
                    },
                    validRowCount = entities.size,
                    skippedRowCount = skipped,
                    warningVi = if (skipped > 0) "Đã bỏ qua $skipped dòng thiếu cột 'word'." else null,
                    vocabEntities = entities
                )
            }

            BulkImportDataset.SENTENCE_DRILLS -> {
                val entities = rows.mapIndexedNotNull { idx, row ->
                    val orderedRaw = row["ordered_chunks_json"].orEmpty()
                        .ifBlank { row["ordered_chunks"].orEmpty() }
                    if (orderedRaw.isBlank()) return@mapIndexedNotNull null
                    val orderedJson = toJsonArrayString(orderedRaw)
                    val scrambledRaw = row["scrambled_chunks_json"].orEmpty()
                        .ifBlank { row["scrambled_chunks"].orEmpty() }
                    val scrambledJson = if (scrambledRaw.isNotBlank()) toJsonArrayString(scrambledRaw) else orderedJson
                    SentenceDrillEntity(
                        id = row["id"].orEmpty().ifBlank { "csv_syn_${System.currentTimeMillis()}_$idx" },
                        moduleId = row["module_id"]?.toIntOrNull() ?: 1,
                        structureTagVi = row["structure_tag_vi"].orEmpty().ifBlank { "Cấu trúc Band 8.5" },
                        structureNameEn = row["structure_name_en"].orEmpty().ifBlank { "Academic Syntax" },
                        meaningVi = row["meaning_vi"].orEmpty(),
                        orderedChunksJson = orderedJson,
                        scrambledChunksJson = scrambledJson,
                        grammarExplanationVi = row["grammar_explanation_vi"].orEmpty()
                    )
                }
                require(entities.isNotEmpty()) {
                    "Tệp CSV thiếu cột 'ordered_chunks' (hoặc 'ordered_chunks_json'). Hãy tải file mẫu CSV."
                }
                val skipped = rows.size - entities.size
                BulkImportPreview(
                    dataset = dataset,
                    formatLabel = "CSV (UTF-8)",
                    sourceFileName = sourceFileName,
                    headers = listOf("id", "module_id", "structure_tag_vi", "structure_name_en", "meaning_vi"),
                    previewRows = entities.take(25).map {
                        listOf(it.id, "#${it.moduleId}", it.structureTagVi, it.structureNameEn, it.meaningVi.ifBlank { "—" })
                    },
                    validRowCount = entities.size,
                    skippedRowCount = skipped,
                    warningVi = if (skipped > 0) "Đã bỏ qua $skipped dòng thiếu 'ordered_chunks'." else null,
                    drillEntities = entities
                )
            }

            BulkImportDataset.EXAMS -> {
                val entities = rows.mapIndexedNotNull { idx, row ->
                    val title = row["title"].orEmpty().trim()
                    if (title.isBlank()) return@mapIndexedNotNull null
                    ExamEntity(
                        id = row["id"].orEmpty().ifBlank { "csv_exam_${System.currentTimeMillis()}_$idx" },
                        title = title,
                        sourceLabel = row["source_label"].orEmpty().ifBlank { "CSV Imported" },
                        year = row["year"]?.toIntOrNull() ?: 2026,
                        difficulty = row["difficulty"].orEmpty().ifBlank { "HARD" },
                        topicVi = row["topic_vi"].orEmpty().ifBlank { "Đề tổng hợp tự nạp" },
                        topicEn = row["topic_en"].orEmpty().ifBlank { "Academic IELTS" },
                        isCustom = row["is_custom"]?.equals("false", ignoreCase = true) != true,
                        audioUri = row["audio_uri"].orEmpty().ifBlank { null },
                        audioDisplayName = row["audio_display_name"].orEmpty().ifBlank { null },
                        listeningTitle = row["listening_title"].orEmpty().ifBlank { "Listening Section" },
                        listeningScriptJson = toJsonArrayString(
                            row["listening_script_json"].orEmpty().ifBlank { row["listening_script"].orEmpty() }
                        ),
                        readingTitle = row["reading_title"].orEmpty().ifBlank { title },
                        readingParagraphsJson = row["reading_paragraphs_json"].orEmpty()
                            .ifBlank { row["reading_paragraphs"].orEmpty() }
                            .ifBlank { "[]" },
                        writingTask1Prompt = row["writing_task1_prompt"].orEmpty(),
                        writingTask2Prompt = row["writing_task2_prompt"].orEmpty(),
                        speakingPromptsJson = toJsonArrayString(
                            row["speaking_prompts_json"].orEmpty().ifBlank { row["speaking_prompts"].orEmpty() }
                        ),
                        questionsJson = row["questions_json"].orEmpty().ifBlank { row["questions"].orEmpty() }.ifBlank { "[]" }
                    )
                }
                require(entities.isNotEmpty()) {
                    "Tệp CSV Đề thi thiếu cột 'title'. Hãy tải file mẫu CSV để khớp đúng cột."
                }
                val skipped = rows.size - entities.size
                BulkImportPreview(
                    dataset = dataset,
                    formatLabel = "CSV (UTF-8)",
                    sourceFileName = sourceFileName,
                    headers = listOf("id", "title", "year", "difficulty", "topic_vi"),
                    previewRows = entities.take(25).map {
                        listOf(it.id, it.title, it.year.toString(), it.difficulty, it.topicVi)
                    },
                    validRowCount = entities.size,
                    skippedRowCount = skipped,
                    warningVi = if (skipped > 0) "Đã bỏ qua $skipped dòng thiếu cột 'title'." else null,
                    examEntities = entities
                )
            }

            BulkImportDataset.PERSONAL_MATERIALS -> {
                val entities = rows.mapIndexedNotNull { idx, row ->
                    val title = row["title"].orEmpty().trim()
                    val content = row["content_en"].orEmpty().trim()
                    if (title.isBlank() && content.isBlank()) return@mapIndexedNotNull null
                    PersonalMaterialEntity(
                        id = row["id"].orEmpty().ifBlank { "csv_pm_${System.currentTimeMillis()}_$idx" },
                        moduleId = (row["module_id"]?.toIntOrNull() ?: 1).coerceIn(1, 12),
                        title = title.ifBlank { "Tài liệu tự học #${idx + 1}" },
                        category = row["category"].orEmpty().ifBlank { "Ghi chú" },
                        contentEn = content,
                        notesVi = row["notes_vi"].orEmpty(),
                        tagsJson = toJsonArrayString(row["tags"].orEmpty().ifBlank { row["tags_json"].orEmpty() }),
                        updatedAt = row["updated_at"].orEmpty().ifBlank { todayStr },
                        audioUri = row["audio_uri"].orEmpty().ifBlank { null },
                        audioDisplayName = row["audio_display_name"].orEmpty().ifBlank { null }
                    )
                }
                require(entities.isNotEmpty()) {
                    "Tệp CSV Tài liệu thiếu cột 'title' hoặc 'content_en'. Hãy tải file mẫu CSV."
                }
                val skipped = rows.size - entities.size
                BulkImportPreview(
                    dataset = dataset,
                    formatLabel = "CSV (UTF-8)",
                    sourceFileName = sourceFileName,
                    headers = listOf("id", "module_id", "title", "category", "updated_at"),
                    previewRows = entities.take(25).map {
                        listOf(it.id, "#${it.moduleId}", it.title, it.category, it.updatedAt)
                    },
                    validRowCount = entities.size,
                    skippedRowCount = skipped,
                    warningVi = if (skipped > 0) "Đã bỏ qua $skipped dòng trống." else null,
                    materialEntities = entities
                )
            }
        }
    }

    private fun toJsonArrayString(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return "[]"
        if (trimmed.startsWith("[")) return trimmed
        val parts = trimmed.split("|", ";", "\n")
            .map { it.trim().removePrefix("#") }
            .filter { it.isNotEmpty() }
        return JSONArray(parts).toString()
    }

    // ---- Downloadable Sample CSV / JSON Templates for Each Dataset ----

    fun sampleCsvFor(dataset: BulkImportDataset): String = when (dataset) {
        BulkImportDataset.VOCABULARY -> """
            id,module_id,word,part_of_speech,cefr_level,ipa_uk,ipa_us,meaning_vi,example_en,example_vi,word_family,dependent_prepositions,verb_patterns,register_note_vi,band6_pitfall_vi,pronunciation_focus_vi,band6_paraphrase_en,collocation_stem_en,collocation_partner_en,cloze_sentence_en
            custom_m1_holistic,1,holistic,adjective,C1,/həˈlɪs.tɪk/,/hoʊˈlɪs.tɪk/,toàn diện (nhìn nhận tổng thể thay vì tách rời),"A holistic curriculum nurtures both analytical rigour and emotional resilience.","Một chương trình học toàn diện nuôi dưỡng cả tư duy phân tích và sức bền cảm xúc.","holism (n), holistically (adv)","approach to / view of","adopt a holistic approach to + V-ing","Academic Formal — rất ăn điểm ở Education & Healthcare","Band 6.0 hay lạm dụng 'complete' hoặc 'all-round' kém trang trọng.","Nhấn âm 2 /ˈlɪs/, bật rõ âm cuối /k/.","complete / overall","adopt a ______ approach","holistic","Policymakers must adopt a ______ approach to curriculum reform."
            custom_m5_decarbonise,5,decarbonise,verb,C2,/ˌdiːˈkɑː.bə.naɪz/,/ˌdiːˈkɑːr.bə.naɪz/,khử carbon (giảm phát thải khí nhà kính),"Municipalities are racing to decarbonise public transit networks before 2035.","Các đô thị đang chạy đua khử carbon cho mạng lưới giao thông công cộng trước năm 2035.","decarbonisation (n), decarbonised (adj)","decarbonise + sector / grid","seek to decarbonise + N","Formal Policy — chuẩn Writing Task 2 & Reading","Band 6.0 hay viết dài dòng 'reduce carbon pollution in'.","Trọng âm chính rơi vào /ˈkɑː/, âm cuối /z/ rung nhẹ.","reduce carbon emissions","______ public transit","decarbonise","Targeted subsidies can accelerate efforts to ______ heavy industry."
        """.trimIndent()

        BulkImportDataset.SENTENCE_DRILLS -> """
            id,module_id,structure_tag_vi,structure_name_en,meaning_vi,ordered_chunks,scrambled_chunks,grammar_explanation_vi
            custom_syn_01,1,Đảo ngữ Not only... but also,Not only... but also Inversion,"Không chỉ thu hẹp bất bình đẳng học thuật, giáo dục mở còn thúc đẩy học tập suốt đời.","Not only does|open education|narrow academic disparities,|but it also|fosters lifelong learning.","fosters lifelong learning.|Not only does|but it also|open education|narrow academic disparities,","Đảo trợ động từ 'does' lên trước chủ ngữ 'open education' để đạt điểm Grammatical Range Band 8.5."
            custom_syn_02,5,Mệnh đề phân từ hoàn thành,Perfect Participle Clause,"Sau khi áp dụng thuế carbon, thành phố đã giảm 28% lượng khí thải giao thông.","Having enacted|a progressive carbon levy,|the municipality|curbed transit emissions|by nearly 28 percent.","the municipality|by nearly 28 percent.|Having enacted|curbed transit emissions|a progressive carbon levy,","'Having + V3' thể hiện hành động nguyên nhân hoàn tất trước kết quả ở mệnh đề chính."
        """.trimIndent()

        BulkImportDataset.EXAMS -> """
            id,title,source_label,year,difficulty,topic_vi,topic_en,is_custom,listening_title,listening_script,reading_title,reading_paragraphs_json,writing_task1_prompt,writing_task2_prompt,speaking_prompts,questions_json
            custom_exam_csv_01,"Mini Mock Test — Urban Green Corridors","CSV Personal Pack",2026,HARD,"Đô thị & Môi trường","Urban Climate Adaptation",true,"Part 4: Sponge Cities Lecture","Good morning. Today we examine how permeable pavements and urban wetlands absorb monsoon runoff.|Cities that restore floodplains cut drainage repair costs by nearly forty percent.","Sponge Cities and Urban Resilience","[{""label"":""A"",""sentences"":[""Permeable urban surfaces allow rainwater to infiltrate underlying aquifers rather than overwhelming stormwater drains."",""Empirical studies across East Asia show a 35% drop in flash-flood damage within five years.""]}]","The chart compares urban green space per capita across four megacities from 2010 to 2025.","Some planners argue that high-density vertical housing is the only sustainable urban model. To what extent do you agree?","Part 1: Do you have many parks near your home?|Part 2: Describe a green public space you enjoy visiting.","[{""id"":""cq1"",""number"":1,""skill"":""reading"",""type"":""tfng"",""prompt_en"":""Permeable pavements increase pressure on city stormwater drains."",""options"":[""TRUE"",""FALSE"",""NOT GIVEN""],""correct_answer"":""FALSE"",""acceptable_answers"":[""FALSE""]}]"
        """.trimIndent()

        BulkImportDataset.PERSONAL_MATERIALS -> """
            id,module_id,title,category,content_en,notes_vi,tags,updated_at
            custom_pm_01,1,"Ghi chú Paraphrase chủ đề Education Band 8.5","Ghi chú","Rote memorisation often undermines intrinsic motivation, whereas inquiry-based pedagogy cultivates autonomous problem-solving.","Dùng cặp đối lập 'rote memorisation' vs 'inquiry-based pedagogy' để mở rộng Lexical Resource trong Writing Task 2.","paraphrase|writing-task2|education",2026-11-19
            custom_pm_02,5,"Bài đọc tự học — Circular Economy in Megacities","Bài đọc tự học","Transitioning from linear consumption to a circular economy requires manufacturers to design modular products that can be refurbished rather than discarded. Cities that mandate right-to-repair policies report sharp declines in municipal e-waste.","Đọc kỹ cụm 'modular products', 'refurbished', và 'right-to-repair policies' — rất hay xuất hiện trong Reading Passage 3.","reading|environment|paraphrase",2026-11-19
        """.trimIndent()
    }

    fun sampleJsonFor(dataset: BulkImportDataset): String = when (dataset) {
        BulkImportDataset.VOCABULARY -> """
            [
              {
                "id": "custom_m1_epistemic",
                "module_id": 1,
                "word": "epistemic",
                "part_of_speech": "adjective",
                "cefr_level": "C2",
                "ipa_uk": "/ˌep.ɪˈstiː.mɪk/",
                "ipa_us": "/ˌep.ɪˈstiː.mɪk/",
                "meaning_vi": "thuộc về tri thức luận, liên quan đến cách tiếp nhận tri thức",
                "example_en": "Excessive testing can erode students' epistemic curiosity over time.",
                "example_vi": "Việc thi cử quá mức có thể làm xói mòn sự tò mò tri thức của học sinh theo thời gian.",
                "word_family": "epistemology (n), epistemically (adv)",
                "dependent_prepositions": "epistemic curiosity / epistemic humility",
                "verb_patterns": "foster epistemic curiosity",
                "register_note_vi": "Academic C2 — cực kỳ ấn tượng cho IELTS Writing Task 2",
                "band6_pitfall_vi": "Tránh viết chung chung 'learning curiosity' khi muốn diễn đạt sắc thái học thuật.",
                "pronunciation_focus_vi": "Nhấn trọng âm vào /ˈstiː/, đọc rõ 4 âm tiết.",
                "band6_paraphrase_en": "knowledge-related",
                "collocation_stem_en": "______ curiosity",
                "collocation_partner_en": "epistemic",
                "cloze_sentence_en": "Open-ended inquiry fosters ______ curiosity in young learners."
              }
            ]
        """.trimIndent()

        BulkImportDataset.SENTENCE_DRILLS -> """
            [
              {
                "id": "custom_syn_json_01",
                "module_id": 7,
                "structure_tag_vi": "Câu chẻ nhấn mạnh (Cleft Sentence)",
                "structure_name_en": "It-Cleft for Emphasis",
                "meaning_vi": "Chính sự thiếu minh bạch thuật toán mới là nguyên nhân gây xói mòn niềm tin công chúng.",
                "ordered_chunks": [
                  "It is",
                  "algorithmic opacity,",
                  "rather than automation itself,",
                  "that erodes",
                  "public trust."
                ],
                "scrambled_chunks": [
                  "that erodes",
                  "public trust.",
                  "It is",
                  "rather than automation itself,",
                  "algorithmic opacity,"
                ],
                "grammar_explanation_vi": "Cấu trúc 'It is X, rather than Y, that + V' giúp lập luận sắc bén và nâng điểm GRA lên Band 8.5+."
              }
            ]
        """.trimIndent()

        BulkImportDataset.EXAMS -> """
            [
              {
                "id": "custom_exam_json_01",
                "title": "Personal Practice — AI Ethics & Workforce",
                "source_label": "JSON Personal Pack",
                "year": 2026,
                "difficulty": "HARD",
                "topic_vi": "Công nghệ & Đạo đức AI",
                "topic_en": "AI Ethics & Labour Market",
                "is_custom": true,
                "listening_title": "Section 4: Auditing Automated Decisions",
                "listening_script": [
                  "Independent algorithm audits help organisations detect hidden demographic bias before deployment.",
                  "Regulators increasingly require transparent impact reports for high-stakes medical AI."
                ],
                "reading_title": "Human-in-the-Loop Governance",
                "reading_paragraphs": [
                  {
                    "label": "A",
                    "sentences": [
                      "Human-in-the-loop oversight ensures that algorithmic recommendations remain subject to expert accountability.",
                      "Evidence from clinical trials indicates that hybrid clinician-AI workflows outperform either humans or models working alone."
                    ]
                  }
                ],
                "writing_task1_prompt": "The table shows automation adoption rates across five industries between 2018 and 2026.",
                "writing_task2_prompt": "Governments should require independent audits of all AI systems used in healthcare and hiring. Do you agree or disagree?",
                "speaking_prompts": [
                  "Part 1: Which digital tools help you study most effectively?",
                  "Part 2: Describe a piece of technology that improved your productivity."
                ],
                "questions": [
                  {
                    "id": "jq_1",
                    "number": 1,
                    "skill": "reading",
                    "type": "tfng",
                    "prompt_en": "Hybrid human-AI teams achieved better clinical outcomes than standalone AI systems.",
                    "options": ["TRUE", "FALSE", "NOT GIVEN"],
                    "correct_answer": "TRUE",
                    "acceptable_answers": ["TRUE"]
                  }
                ]
              }
            ]
        """.trimIndent()

        BulkImportDataset.PERSONAL_MATERIALS -> """
            [
              {
                "id": "custom_pm_json_01",
                "module_id": 5,
                "title": "Bài đọc tự học — Urban Heat Islands & Bio-Architecture",
                "category": "Bài đọc tự học",
                "content_en": "Retrofitting dense metropolitan districts with reflective rooftops and vertical greenery can lower ambient summer temperatures by up to three degrees Celsius. Beyond thermal comfort, these nature-based interventions substantially curb peak electricity demand during heatwaves.",
                "notes_vi": "Ghi nhớ cụm 'retrofitting dense metropolitan districts' (cải tạo các quận đô thị đông đúc) và 'nature-based interventions' (các giải pháp dựa vào tự nhiên).",
                "tags": ["reading", "environment", "paraphrase", "writing-task2"],
                "updated_at": "2026-11-19"
              }
            ]
        """.trimIndent()
    }
}
