package com.example.data.local

/**
 * Domain models for the Offline-First Hybrid Data Architecture:
 * - Personal self-study notes & articles (Personal_Materials)
 * - Locally persisted MP3/WAV audio blobs (app-private storage indexed in Room)
 * - Full study & cloud sync activity logs
 * - Google Sheets Cloud Sync status & per-tab record counts
 */
data class PersonalMaterial(
    val id: String,
    val moduleId: Int,
    val title: String,
    val category: String,
    val contentEn: String,
    val notesVi: String,
    val tags: List<String>,
    val updatedAt: String,
    val audioUri: String? = null,
    val audioDisplayName: String? = null
)

/**
 * Filter tabs inside the dedicated Personal Library & Self-Study Workspace.
 */
enum class LibraryCategoryFilter(val id: String, val labelVi: String) {
    ALL("all", "Tất cả"),
    NOTES("notes", "Ghi chú"),
    ARTICLES("articles", "Bài đọc tự học"),
    FLASHCARDS("flashcards", "Bộ Flashcard"),
    EXAMS("exams", "Đề cá nhân");

    companion object {
        fun classifyMaterialCategory(rawCategory: String): LibraryCategoryFilter {
            val norm = rawCategory.trim().lowercase()
            return when {
                norm.contains("bài đọc") || norm.contains("reading") || norm.contains("article") || norm.contains("passage") -> ARTICLES
                norm.contains("flashcard") || norm.contains("vocab") || norm.contains("từ vựng") -> FLASHCARDS
                norm.contains("đề") || norm.contains("exam") || norm.contains("test") -> EXAMS
                else -> NOTES
            }
        }

        val editableMaterialCategories: List<String> = listOf(
            "Ghi chú",
            "Bài đọc tự học",
            "Ghi chú Writing",
            "Ghi chú Speaking"
        )
    }
}

/**
 * Four target datasets supported by the In-App CSV / JSON Bulk Importer.
 */
enum class BulkImportDataset(
    val id: String,
    val labelVi: String,
    val shortLabelVi: String,
    val sampleFilePrefix: String,
    val requiredColumnsHint: String
) {
    VOCABULARY(
        id = "vocabulary",
        labelVi = "Từ vựng (Vocabulary)",
        shortLabelVi = "Từ vựng",
        sampleFilePrefix = "apex_sample_vocabulary",
        requiredColumnsHint = "id, module_id, word, part_of_speech, cefr_level, ipa_uk, meaning_vi, example_en..."
    ),
    SENTENCE_DRILLS(
        id = "sentence_drills",
        labelVi = "Bài tập câu (Sentence Drills)",
        shortLabelVi = "Bài tập câu",
        sampleFilePrefix = "apex_sample_sentence_drills",
        requiredColumnsHint = "id, module_id, structure_tag_vi, structure_name_en, meaning_vi, ordered_chunks..."
    ),
    EXAMS(
        id = "exams",
        labelVi = "Đề thi (Exams)",
        shortLabelVi = "Đề thi",
        sampleFilePrefix = "apex_sample_exams",
        requiredColumnsHint = "id, title, source_label, year, difficulty, topic_vi, reading_paragraphs_json, questions_json..."
    ),
    PERSONAL_MATERIALS(
        id = "personal_materials",
        labelVi = "Tài liệu cá nhân (Personal Materials)",
        shortLabelVi = "Tài liệu",
        sampleFilePrefix = "apex_sample_personal_materials",
        requiredColumnsHint = "id, module_id, title, category, content_en, notes_vi, tags, updated_at"
    )
}

data class BulkImportPreview(
    val dataset: BulkImportDataset,
    val formatLabel: String,
    val sourceFileName: String,
    val headers: List<String>,
    val previewRows: List<List<String>>,
    val validRowCount: Int,
    val skippedRowCount: Int = 0,
    val warningVi: String? = null,
    val vocabEntities: List<VocabularyEntity> = emptyList(),
    val drillEntities: List<SentenceDrillEntity> = emptyList(),
    val examEntities: List<ExamEntity> = emptyList(),
    val materialEntities: List<PersonalMaterialEntity> = emptyList()
)

data class ExtractedAcademicWord(
    val word: String,
    val partOfSpeech: String,
    val cefrLevel: String,
    val ipaUk: String,
    val meaningVi: String,
    val collocationEn: String,
    val exampleEn: String
)

data class ExtractedGrammarPoint(
    val structureNameEn: String,
    val exampleQuoteEn: String,
    val explanationVi: String
)

data class PersonalMaterialAiSummary(
    val materialId: String,
    val summaryVi: String,
    val keyTakeawaysVi: List<String>,
    val extractedWords: List<ExtractedAcademicWord>,
    val grammarHighlights: List<ExtractedGrammarPoint>
)

sealed interface PersonalMaterialAiUiState {
    data object Idle : PersonalMaterialAiUiState
    data class Loading(val materialId: String) : PersonalMaterialAiUiState
    data class Success(val result: PersonalMaterialAiSummary) : PersonalMaterialAiUiState
    data class Error(val materialId: String, val messageVi: String) : PersonalMaterialAiUiState
}

data class AudioBlobRecord(
    val id: String,
    val displayName: String,
    val mimeType: String,
    val localPath: String,
    val sizeBytes: Long,
    val linkedExamId: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    val sizeReadable: String
        get() = when {
            sizeBytes >= 1_048_576L -> String.format(java.util.Locale.US, "%.1f MB", sizeBytes / 1_048_576.0)
            sizeBytes >= 1024L -> "${sizeBytes / 1024L} KB"
            else -> "$sizeBytes B"
        }
}

data class StudyLogEntry(
    val id: String,
    val timestampLabel: String,
    val category: String,
    val summaryVi: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class LocalStorageCounts(
    val modulesCount: Int = 0,
    val vocabularyCount: Int = 0,
    val sentenceDrillsCount: Int = 0,
    val examsCount: Int = 0,
    val personalMaterialsCount: Int = 0,
    val audioBlobsCount: Int = 0,
    val studyLogsCount: Int = 0
) {
    val totalSyncedRecords: Int
        get() = modulesCount + vocabularyCount + sentenceDrillsCount + examsCount + personalMaterialsCount
}

data class TabSyncSummary(
    val tabName: String,
    val labelVi: String,
    val mergedCount: Int,
    val succeeded: Boolean,
    val noteVi: String = ""
)

sealed interface CloudSyncUiState {
    data object Idle : CloudSyncUiState
    data class Syncing(val currentStepVi: String = "Đang tải 5 tab từ Google Sheets...") : CloudSyncUiState
    data class Success(
        val syncedAtLabel: String,
        val totalMerged: Int,
        val tabSummaries: List<TabSyncSummary>
    ) : CloudSyncUiState
    data class Error(val messageVi: String) : CloudSyncUiState
}
