package com.example.data.sync

import com.example.data.ExamScoringHelper
import com.example.data.local.ApexHybridRepository
import com.example.data.local.ExamEntity
import com.example.data.local.ModuleEntity
import com.example.data.local.PersonalMaterialEntity
import com.example.data.local.SentenceDrillEntity
import com.example.data.local.TabSyncSummary
import com.example.data.local.VocabularyEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Google Sheets Cloud Database Connector using public CSV/GViz endpoints:
 * https://docs.google.com/spreadsheets/d/{SHEET_ID}/gviz/tq?tqx=out:csv&sheet={TAB_NAME}
 *
 * Syncs all 5 standardized tabs (Modules, Vocabulary, Sentence_Drills, Exams, Personal_Materials)
 * and merges records into local Room SQLite without duplicating existing IDs.
 */
class GoogleSheetsSyncService(
    private val hybridRepository: ApexHybridRepository
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    data class FullSyncOutcome(
        val spreadsheetId: String,
        val syncedAtLabel: String,
        val totalMerged: Int,
        val tabSummaries: List<TabSyncSummary>
    )

    suspend fun syncAllTabs(
        rawUrlOrId: String,
        onStepProgress: (String) -> Unit = {}
    ): Result<FullSyncOutcome> = withContext(Dispatchers.IO) {
        val sheetId = extractSpreadsheetId(rawUrlOrId)
            ?: return@withContext Result.failure(
                IllegalArgumentException("Không tìm thấy Spreadsheet ID hợp lệ. Hãy dán đường dẫn Google Sheet dạng https://docs.google.com/spreadsheets/d/.../edit")
            )

        val summaries = mutableListOf<TabSyncSummary>()
        var totalMerged = 0
        var successfulTabs = 0

        // 1. Modules
        onStepProgress("Đang đồng bộ tab Modules (1/5)...")
        summaries += syncSingleTab(sheetId, TAB_MODULES, "Modules (12 chủ đề)") { rows ->
            val entities = rows.mapNotNull { mapModuleRow(it) }.distinctBy { it.id }
            hybridRepository.mergeModules(entities)
            entities.size
        }.also {
            if (it.succeeded) successfulTabs++
            totalMerged += it.mergedCount
        }

        // 2. Vocabulary
        onStepProgress("Đang đồng bộ tab Vocabulary (2/5)...")
        summaries += syncSingleTab(sheetId, TAB_VOCABULARY, "Vocabulary (Từ vựng C1–C2)") { rows ->
            val entities = rows.mapNotNull { mapVocabRow(it) }.distinctBy { it.id }
            hybridRepository.mergeVocabulary(entities)
            entities.size
        }.also {
            if (it.succeeded) successfulTabs++
            totalMerged += it.mergedCount
        }

        // 3. Sentence_Drills
        onStepProgress("Đang đồng bộ tab Sentence_Drills (3/5)...")
        summaries += syncSingleTab(sheetId, TAB_SENTENCE_DRILLS, "Sentence_Drills (Cấu trúc 8.5+)") { rows ->
            val entities = rows.mapNotNull { mapSentenceDrillRow(it) }.distinctBy { it.id }
            hybridRepository.mergeSentenceDrills(entities)
            entities.size
        }.also {
            if (it.succeeded) successfulTabs++
            totalMerged += it.mergedCount
        }

        // 4. Exams
        onStepProgress("Đang đồng bộ tab Exams (4/5)...")
        summaries += syncSingleTab(sheetId, TAB_EXAMS, "Exams (Kho đề thi)") { rows ->
            val entities = rows.mapNotNull { mapExamRow(it) }.distinctBy { it.id }
            hybridRepository.mergeExams(entities)
            entities.size
        }.also {
            if (it.succeeded) successfulTabs++
            totalMerged += it.mergedCount
        }

        // 5. Personal_Materials
        onStepProgress("Đang đồng bộ tab Personal_Materials (5/5)...")
        summaries += syncSingleTab(sheetId, TAB_PERSONAL_MATERIALS, "Personal_Materials (Tài liệu tự học)") { rows ->
            val entities = rows.mapNotNull { mapPersonalMaterialRow(it) }.distinctBy { it.id }
            hybridRepository.mergePersonalMaterials(entities)
            entities.size
        }.also {
            if (it.succeeded) successfulTabs++
            totalMerged += it.mergedCount
        }

        if (successfulTabs == 0) {
            return@withContext Result.failure(
                IllegalStateException(
                    "Không thể truy cập Google Sheet. Vui lòng bật quyền Chia sẻ → \"Bất kỳ ai có đường liên kết (Người xem)\" và kiểm tra kết nối mạng."
                )
            )
        }

        val nowLabel = ExamScoringHelper.nowFormatted()
        hybridRepository.addStudyLog(
            category = "Cloud Sync",
            summaryVi = "Đồng bộ Google Sheets ($successfulTabs/5 tab, gộp $totalMerged bản ghi không trùng ID)"
        )

        Result.success(
            FullSyncOutcome(
                spreadsheetId = sheetId,
                syncedAtLabel = nowLabel,
                totalMerged = totalMerged,
                tabSummaries = summaries
            )
        )
    }

    private suspend fun syncSingleTab(
        sheetId: String,
        tabName: String,
        labelVi: String,
        onRowsParsed: suspend (List<Map<String, String>>) -> Int
    ): TabSyncSummary {
        return try {
            val encodedTab = URLEncoder.encode(tabName, "UTF-8")
            val url = "https://docs.google.com/spreadsheets/d/$sheetId/gviz/tq?tqx=out:csv&sheet=$encodedTab"
            val request = Request.Builder()
                .url(url)
                .header("Accept", "text/csv,text/plain,*/*")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return TabSyncSummary(
                        tabName = tabName,
                        labelVi = labelVi,
                        mergedCount = 0,
                        succeeded = false,
                        noteVi = "HTTP ${response.code}"
                    )
                }
                val body = response.body?.string().orEmpty()
                if (body.trimStart().startsWith("<!DOCTYPE", ignoreCase = true) ||
                    body.trimStart().startsWith("<html", ignoreCase = true)
                ) {
                    return TabSyncSummary(
                        tabName = tabName,
                        labelVi = labelVi,
                        mergedCount = 0,
                        succeeded = false,
                        noteVi = "Sheet chưa mở công khai"
                    )
                }

                val rows = CsvTableParser.parseToRowMaps(body)
                if (rows.isEmpty()) {
                    return TabSyncSummary(
                        tabName = tabName,
                        labelVi = labelVi,
                        mergedCount = 0,
                        succeeded = true,
                        noteVi = "Tab trống"
                    )
                }
                val merged = onRowsParsed(rows)
                TabSyncSummary(
                    tabName = tabName,
                    labelVi = labelVi,
                    mergedCount = merged,
                    succeeded = true,
                    noteVi = "Đã gộp $merged dòng"
                )
            }
        } catch (e: Exception) {
            TabSyncSummary(
                tabName = tabName,
                labelVi = labelVi,
                mergedCount = 0,
                succeeded = false,
                noteVi = e.localizedMessage?.take(48) ?: "Lỗi tải tab"
            )
        }
    }

    private fun mapModuleRow(row: Map<String, String>): ModuleEntity? {
        val id = row["id"]?.toIntOrNull() ?: return null
        val titleEn = row.firstNonBlank("title_en", "title") ?: return null
        val collocationsRaw = row.firstNonBlank("key_collocations_json", "key_collocations").orEmpty()
        val tipsRaw = row.firstNonBlank("daily_tips_json", "daily_tips").orEmpty()
        return ModuleEntity(
            id = id,
            tier = row.firstNonBlank("tier") ?: "TIER_1_FOUNDATION",
            titleEn = titleEn,
            titleVi = row.firstNonBlank("title_vi") ?: titleEn,
            focusVi = row.firstNonBlank("focus_vi", "description_vi") ?: "",
            iconKey = row.firstNonBlank("icon_key") ?: "school",
            keyCollocationsJson = normalizeJsonArrayColumn(collocationsRaw),
            dailyTipsJson = tipsRaw.ifBlank { "[]" }
        )
    }

    private fun mapVocabRow(row: Map<String, String>): VocabularyEntity? {
        val word = row.firstNonBlank("word", "term") ?: return null
        val moduleId = (row["module_id"]?.toIntOrNull() ?: 1).coerceIn(1, 12)
        val id = row.firstNonBlank("id") ?: "m${moduleId}_${word.lowercase().replace(Regex("[^a-z0-9]+"), "_")}"
        return VocabularyEntity(
            id = id,
            moduleId = moduleId,
            word = word,
            partOfSpeech = row.firstNonBlank("part_of_speech", "pos") ?: "noun",
            cefrLevel = row.firstNonBlank("cefr_level", "band_level", "level") ?: "C1",
            ipaUk = row.firstNonBlank("ipa_uk", "ipa") ?: "",
            ipaUs = row.firstNonBlank("ipa_us", "ipa") ?: "",
            meaningVi = row.firstNonBlank("meaning_vi", "meaning") ?: "",
            exampleEn = row.firstNonBlank("example_en", "model_sentence", "example") ?: "",
            exampleVi = row.firstNonBlank("example_vi") ?: "",
            wordFamily = row.firstNonBlank("word_family") ?: "",
            dependentPrepositions = row.firstNonBlank("dependent_prepositions", "prepositions") ?: "",
            verbPatterns = row.firstNonBlank("verb_patterns") ?: "",
            registerNoteVi = row.firstNonBlank("register_note_vi", "academic_register") ?: "Academic Formal",
            band6PitfallVi = row.firstNonBlank("band6_pitfall_vi", "pitfall_vi") ?: "",
            pronunciationFocusVi = row.firstNonBlank("pronunciation_focus_vi") ?: "Nhấn đúng trọng âm IPA và bật rõ phụ âm cuối.",
            band6ParaphraseEn = row.firstNonBlank("band6_paraphrase_en", "paraphrase") ?: "",
            collocationStemEn = row.firstNonBlank("collocation_stem_en") ?: "academic ______",
            collocationPartnerEn = row.firstNonBlank("collocation_partner_en", "collocations") ?: word,
            clozeSentenceEn = row.firstNonBlank("cloze_sentence_en") ?: ""
        )
    }

    private fun mapSentenceDrillRow(row: Map<String, String>): SentenceDrillEntity? {
        val id = row.firstNonBlank("id") ?: return null
        val orderedRaw = row.firstNonBlank("ordered_chunks_json", "ordered_chunks", "tokens") ?: return null
        val orderedJson = normalizeJsonArrayColumn(orderedRaw)
        val scrambledRaw = row.firstNonBlank("scrambled_chunks_json", "scrambled_chunks").orEmpty()
        val scrambledJson = if (scrambledRaw.isNotBlank()) normalizeJsonArrayColumn(scrambledRaw) else orderedJson
        return SentenceDrillEntity(
            id = id,
            moduleId = row["module_id"]?.toIntOrNull() ?: 0,
            structureTagVi = row.firstNonBlank("structure_tag_vi", "grammar_tag") ?: "Cấu trúc Band 8.5+",
            structureNameEn = row.firstNonBlank("structure_name_en", "structure_name") ?: "Complex Structure",
            meaningVi = row.firstNonBlank("meaning_vi", "instruction_vi") ?: "",
            orderedChunksJson = orderedJson,
            scrambledChunksJson = scrambledJson,
            grammarExplanationVi = row.firstNonBlank("grammar_explanation_vi", "explanation_vi") ?: ""
        )
    }

    private fun mapExamRow(row: Map<String, String>): ExamEntity? {
        val id = row.firstNonBlank("id") ?: return null
        val title = row.firstNonBlank("title") ?: return null
        return ExamEntity(
            id = id,
            title = title,
            sourceLabel = row.firstNonBlank("source_label") ?: "Google Sheets Cloud",
            year = row["year"]?.toIntOrNull() ?: 2025,
            difficulty = row.firstNonBlank("difficulty") ?: "HARD",
            topicVi = row.firstNonBlank("topic_vi") ?: "Tổng hợp học thuật",
            topicEn = row.firstNonBlank("topic_en") ?: "Academic IELTS",
            isCustom = row["is_custom"]?.equals("true", ignoreCase = true) ?: false,
            audioUri = row.firstNonBlank("audio_uri"),
            audioDisplayName = row.firstNonBlank("audio_display_name"),
            listeningTitle = row.firstNonBlank("listening_title") ?: "Listening Section",
            listeningScriptJson = normalizeJsonArrayColumn(row.firstNonBlank("listening_script_json", "listening_script").orEmpty()),
            readingTitle = row.firstNonBlank("reading_title") ?: title,
            readingParagraphsJson = row.firstNonBlank("reading_paragraphs_json", "reading_paragraphs") ?: "[]",
            writingTask1Prompt = row.firstNonBlank("writing_task1_prompt") ?: "",
            writingTask2Prompt = row.firstNonBlank("writing_task2_prompt") ?: "",
            speakingPromptsJson = normalizeJsonArrayColumn(row.firstNonBlank("speaking_prompts_json", "speaking_prompts").orEmpty()),
            questionsJson = row.firstNonBlank("questions_json", "questions") ?: "[]"
        )
    }

    private fun mapPersonalMaterialRow(row: Map<String, String>): PersonalMaterialEntity? {
        val id = row.firstNonBlank("id") ?: return null
        val title = row.firstNonBlank("title") ?: return null
        return PersonalMaterialEntity(
            id = id,
            moduleId = (row["module_id"]?.toIntOrNull() ?: 1).coerceIn(1, 12),
            title = title,
            category = row.firstNonBlank("category") ?: "Self-Study Note",
            contentEn = row.firstNonBlank("content_en", "content") ?: "",
            notesVi = row.firstNonBlank("notes_vi", "notes") ?: "",
            tagsJson = normalizeJsonArrayColumn(row.firstNonBlank("tags_json", "tags").orEmpty()),
            updatedAt = row.firstNonBlank("updated_at") ?: ExamScoringHelper.nowFormatted()
        )
    }

    private fun normalizeJsonArrayColumn(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return "[]"
        if (trimmed.startsWith("[")) return trimmed
        val items = trimmed.split("|", ";", "\n").map { it.trim() }.filter { it.isNotEmpty() }
        return JSONArray(items).toString()
    }

    private fun Map<String, String>.firstNonBlank(vararg keys: String): String? {
        for (k in keys) {
            val v = this[k]?.trim()
            if (!v.isNullOrEmpty()) return v
        }
        return null
    }

    companion object {
        const val TAB_MODULES = "Modules"
        const val TAB_VOCABULARY = "Vocabulary"
        const val TAB_SENTENCE_DRILLS = "Sentence_Drills"
        const val TAB_EXAMS = "Exams"
        const val TAB_PERSONAL_MATERIALS = "Personal_Materials"

        val STANDARD_TABS = listOf(
            TAB_MODULES,
            TAB_VOCABULARY,
            TAB_SENTENCE_DRILLS,
            TAB_EXAMS,
            TAB_PERSONAL_MATERIALS
        )

        private val SHEET_URL_REGEX = Regex("/spreadsheets/d/([a-zA-Z0-9_-]{15,})")
        private val RAW_ID_REGEX = Regex("^[a-zA-Z0-9_-]{20,}$")

        fun extractSpreadsheetId(input: String): String? {
            val trimmed = input.trim()
            if (trimmed.isEmpty()) return null
            SHEET_URL_REGEX.find(trimmed)?.groupValues?.getOrNull(1)?.let { return it }
            if (RAW_ID_REGEX.matches(trimmed)) return trimmed
            return null
        }
    }
}
