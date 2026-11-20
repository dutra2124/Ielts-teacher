package com.example.data.local

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.ExamPaper
import com.example.data.ExamScoringHelper
import com.example.data.IeltsModule
import com.example.data.SyntaxExercise
import com.example.data.VocabWordItem
import com.example.data.local.LocalEntityMappers.toDomain
import com.example.data.local.LocalEntityMappers.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * High-capacity Offline-First Hybrid Storage Engine backed by Room SQLite + App-private Audio Vault.
 * Eliminates the 5MB DataStore/localStorage ceiling while keeping 100% offline boot speed via
 * bundled JSON seeds in assets/data/.
 */
class ApexHybridRepository(private val context: Context) {

    private val database = ApexLocalDatabase.getInstance(context)
    private val dao = database.localDao()
    private val seedLoader = BundledJsonSeedLoader(context)

    val modulesFlow: Flow<List<IeltsModule>> = dao.observeModules().map { rows ->
        rows.map { it.toDomain() }
    }

    val vocabularyFlow: Flow<List<VocabWordItem>> = dao.observeVocabulary().map { rows ->
        rows.map { it.toDomain() }
    }

    val sentenceDrillsFlow: Flow<List<SyntaxExercise>> = dao.observeSentenceDrills().map { rows ->
        rows.map { it.toDomain() }
    }

    val examsFlow: Flow<List<ExamPaper>> = dao.observeExams().map { rows ->
        rows.map { it.toDomain() }
    }

    val personalMaterialsFlow: Flow<List<PersonalMaterial>> = dao.observePersonalMaterials().map { rows ->
        rows.map { it.toDomain() }
    }

    val audioBlobsFlow: Flow<List<AudioBlobRecord>> = dao.observeAudioBlobs().map { rows ->
        rows.map { it.toDomain() }
    }

    val studyLogsFlow: Flow<List<StudyLogEntry>> = dao.observeStudyLogs().map { rows ->
        rows.map { it.toDomain() }
    }

    val countsFlow: Flow<LocalStorageCounts> = combine(
        dao.observeModules(),
        dao.observeVocabulary(),
        dao.observeSentenceDrills(),
        dao.observeExams(),
        dao.observePersonalMaterials()
    ) { mods, vocabs, drills, exams, materials ->
        LocalStorageCounts(
            modulesCount = mods.size,
            vocabularyCount = vocabs.size,
            sentenceDrillsCount = drills.size,
            examsCount = exams.size,
            personalMaterialsCount = materials.size
        )
    }.combine(dao.observeAudioBlobs()) { counts, blobs ->
        counts.copy(audioBlobsCount = blobs.size)
    }.combine(dao.observeStudyLogs()) { counts, logs ->
        counts.copy(studyLogsCount = logs.size)
    }

    /**
     * Immediately hydrates empty SQLite tables from bundled JSON files in assets/data/.
     * Runs 100% offline and never overwrites previously synced or user-edited records.
     */
    suspend fun ensureBundledSeedData(legacyCustomExams: List<ExamPaper> = emptyList()) = withContext(Dispatchers.IO) {
        if (dao.countModules() == 0) {
            val bundled = seedLoader.loadModules()
            if (bundled.isNotEmpty()) dao.upsertModules(bundled)
        }
        if (dao.countVocabulary() == 0) {
            val bundled = seedLoader.loadVocabulary()
            if (bundled.isNotEmpty()) dao.upsertVocabulary(bundled)
        }
        if (dao.countSentenceDrills() == 0) {
            val bundled = seedLoader.loadSentenceDrills()
            if (bundled.isNotEmpty()) dao.upsertSentenceDrills(bundled)
        }
        if (dao.countExams() == 0) {
            val bundled = seedLoader.loadExams()
            if (bundled.isNotEmpty()) dao.upsertExams(bundled)
        }
        if (legacyCustomExams.isNotEmpty()) {
            val existingIds = dao.getAllExams().map { it.id }.toSet()
            val toMigrate = legacyCustomExams.filter { it.id !in existingIds }.map { it.toEntity() }
            if (toMigrate.isNotEmpty()) dao.upsertExams(toMigrate)
        }
        if (dao.countPersonalMaterials() == 0) {
            val bundled = seedLoader.loadPersonalMaterials()
            if (bundled.isNotEmpty()) dao.upsertPersonalMaterials(bundled)
        }
    }

    // ---- Deduplicated Upserts (used by Google Sheets Cloud Sync & Backup Restore) ----

    suspend fun mergeModules(items: List<ModuleEntity>) = withContext(Dispatchers.IO) {
        if (items.isNotEmpty()) dao.upsertModules(items)
    }

    suspend fun mergeVocabulary(items: List<VocabularyEntity>) = withContext(Dispatchers.IO) {
        if (items.isNotEmpty()) dao.upsertVocabulary(items)
    }

    suspend fun mergeSentenceDrills(items: List<SentenceDrillEntity>) = withContext(Dispatchers.IO) {
        if (items.isNotEmpty()) dao.upsertSentenceDrills(items)
    }

    suspend fun mergeExams(items: List<ExamEntity>) = withContext(Dispatchers.IO) {
        if (items.isNotEmpty()) dao.upsertExams(items)
    }

    suspend fun mergePersonalMaterials(items: List<PersonalMaterialEntity>) = withContext(Dispatchers.IO) {
        if (items.isNotEmpty()) dao.upsertPersonalMaterials(items)
    }

    suspend fun saveSingleVocabWord(word: VocabWordItem) = withContext(Dispatchers.IO) {
        dao.upsertVocabulary(listOf(word.toEntity()))
        addStudyLogInternal("Flashcard", "Lưu thẻ từ mới từ Tự học: ${word.word} (Module #${word.moduleId})")
    }

    suspend fun saveCustomExam(exam: ExamPaper) = withContext(Dispatchers.IO) {
        dao.upsertExam(exam.copy(isCustom = true).toEntity())
        addStudyLogInternal("Exam", "Lưu bộ đề cá nhân: ${exam.title}")
    }

    suspend fun deleteCustomExam(examId: String) = withContext(Dispatchers.IO) {
        dao.deleteExamById(examId)
    }

    suspend fun savePersonalMaterial(material: PersonalMaterial) = withContext(Dispatchers.IO) {
        dao.upsertPersonalMaterial(material.toEntity())
        addStudyLogInternal("Material", "Lưu tài liệu tự học: ${material.title}")
    }

    suspend fun deletePersonalMaterial(materialId: String) = withContext(Dispatchers.IO) {
        dao.deletePersonalMaterialById(materialId)
    }

    suspend fun addStudyLog(category: String, summaryVi: String) = withContext(Dispatchers.IO) {
        addStudyLogInternal(category, summaryVi)
    }

    private suspend fun addStudyLogInternal(category: String, summaryVi: String) {
        dao.upsertStudyLog(
            StudyLogEntity(
                id = "log_${UUID.randomUUID().toString().take(8)}",
                timestampLabel = ExamScoringHelper.nowFormatted(),
                category = category,
                summaryVi = summaryVi,
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    /**
     * Copies a user-selected MP3/WAV audio file via SAF into app-private storage and indexes it in Room.
     * Supports large listening tracks cleanly without storage permissions or DataStore size limits.
     */
    suspend fun saveAudioBlobFromUri(
        uri: Uri,
        linkedExamId: String? = null
    ): Result<AudioBlobRecord> = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            val mime = resolver.getType(uri) ?: "audio/mpeg"
            var displayName = "ielts_audio_${System.currentTimeMillis()}.mp3"
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) {
                        val candidate = cursor.getString(idx)
                        if (!candidate.isNullOrBlank()) displayName = candidate
                    }
                }
            }
            val audioDir = File(context.filesDir, "apex_audio_blobs").apply { mkdirs() }
            val blobId = "audio_${UUID.randomUUID().toString().take(8)}"
            val safeExt = displayName.substringAfterLast('.', "mp3").take(6).ifBlank { "mp3" }
            val targetFile = File(audioDir, "$blobId.$safeExt")

            resolver.openInputStream(uri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: error("Không thể đọc tệp âm thanh đã chọn.")

            val entity = AudioBlobEntity(
                id = blobId,
                displayName = displayName,
                mimeType = mime,
                localPath = Uri.fromFile(targetFile).toString(),
                sizeBytes = targetFile.length(),
                linkedExamId = linkedExamId,
                createdAtMillis = System.currentTimeMillis()
            )
            dao.upsertAudioBlob(entity)
            addStudyLogInternal("Audio", "Lưu tệp âm thanh offline: $displayName (${entity.toDomain().sizeReadable})")
            entity.toDomain()
        }
    }

    suspend fun deleteAudioBlob(blob: AudioBlobRecord) = withContext(Dispatchers.IO) {
        runCatching {
            val parsed = Uri.parse(blob.localPath)
            val path = parsed.path
            if (!path.isNullOrBlank()) {
                File(path).takeIf { it.exists() }?.delete()
            }
        }
        dao.deleteAudioBlobById(blob.id)
    }

    // ---- Full Backup Export & Restore (.json) ----

    suspend fun exportFullBackupJson(sheetUrl: String): String = withContext(Dispatchers.IO) {
        val root = JSONObject().apply {
            put("app", "ApexIELTS 8.5")
            put("schema_version", 1)
            put("exported_at", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date()))
            put("google_sheet_url", sheetUrl)

            put("modules", JSONArray().apply {
                dao.getAllModules().forEach { m ->
                    put(JSONObject().apply {
                        put("id", m.id)
                        put("tier", m.tier)
                        put("title_en", m.titleEn)
                        put("title_vi", m.titleVi)
                        put("focus_vi", m.focusVi)
                        put("icon_key", m.iconKey)
                        put("key_collocations", runCatching { JSONArray(m.keyCollocationsJson) }.getOrDefault(JSONArray()))
                        put("daily_tips", runCatching { JSONArray(m.dailyTipsJson) }.getOrDefault(JSONArray()))
                    })
                }
            })

            put("vocabulary", JSONArray().apply {
                dao.getAllVocabulary().forEach { v ->
                    put(JSONObject().apply {
                        put("id", v.id)
                        put("module_id", v.moduleId)
                        put("word", v.word)
                        put("part_of_speech", v.partOfSpeech)
                        put("cefr_level", v.cefrLevel)
                        put("ipa_uk", v.ipaUk)
                        put("ipa_us", v.ipaUs)
                        put("meaning_vi", v.meaningVi)
                        put("example_en", v.exampleEn)
                        put("example_vi", v.exampleVi)
                        put("word_family", v.wordFamily)
                        put("dependent_prepositions", v.dependentPrepositions)
                        put("verb_patterns", v.verbPatterns)
                        put("register_note_vi", v.registerNoteVi)
                        put("band6_pitfall_vi", v.band6PitfallVi)
                        put("pronunciation_focus_vi", v.pronunciationFocusVi)
                        put("band6_paraphrase_en", v.band6ParaphraseEn)
                        put("collocation_stem_en", v.collocationStemEn)
                        put("collocation_partner_en", v.collocationPartnerEn)
                        put("cloze_sentence_en", v.clozeSentenceEn)
                    })
                }
            })

            put("sentence_drills", JSONArray().apply {
                dao.getAllSentenceDrills().forEach { s ->
                    put(JSONObject().apply {
                        put("id", s.id)
                        put("module_id", s.moduleId)
                        put("structure_tag_vi", s.structureTagVi)
                        put("structure_name_en", s.structureNameEn)
                        put("meaning_vi", s.meaningVi)
                        put("ordered_chunks", runCatching { JSONArray(s.orderedChunksJson) }.getOrDefault(JSONArray()))
                        put("scrambled_chunks", runCatching { JSONArray(s.scrambledChunksJson) }.getOrDefault(JSONArray()))
                        put("grammar_explanation_vi", s.grammarExplanationVi)
                    })
                }
            })

            put("exams", JSONArray().apply {
                dao.getAllExams().forEach { e ->
                    put(JSONObject().apply {
                        put("id", e.id)
                        put("title", e.title)
                        put("source_label", e.sourceLabel)
                        put("year", e.year)
                        put("difficulty", e.difficulty)
                        put("topic_vi", e.topicVi)
                        put("topic_en", e.topicEn)
                        put("is_custom", e.isCustom)
                        put("audio_uri", e.audioUri.orEmpty())
                        put("audio_display_name", e.audioDisplayName.orEmpty())
                        put("listening_title", e.listeningTitle)
                        put("listening_script", runCatching { JSONArray(e.listeningScriptJson) }.getOrDefault(JSONArray()))
                        put("reading_title", e.readingTitle)
                        put("reading_paragraphs", runCatching { JSONArray(e.readingParagraphsJson) }.getOrDefault(JSONArray()))
                        put("writing_task1_prompt", e.writingTask1Prompt)
                        put("writing_task2_prompt", e.writingTask2Prompt)
                        put("speaking_prompts", runCatching { JSONArray(e.speakingPromptsJson) }.getOrDefault(JSONArray()))
                        put("questions", runCatching { JSONArray(e.questionsJson) }.getOrDefault(JSONArray()))
                    })
                }
            })

            put("personal_materials", JSONArray().apply {
                dao.getAllPersonalMaterials().forEach { pm ->
                    put(JSONObject().apply {
                        put("id", pm.id)
                        put("module_id", pm.moduleId)
                        put("title", pm.title)
                        put("category", pm.category)
                        put("content_en", pm.contentEn)
                        put("notes_vi", pm.notesVi)
                        put("tags", runCatching { JSONArray(pm.tagsJson) }.getOrDefault(JSONArray()))
                        put("updated_at", pm.updatedAt)
                        put("audio_uri", pm.audioUri.orEmpty())
                        put("audio_display_name", pm.audioDisplayName.orEmpty())
                    })
                }
            })

            put("study_logs", JSONArray().apply {
                dao.getAllStudyLogs().forEach { log ->
                    put(JSONObject().apply {
                        put("id", log.id)
                        put("timestamp_label", log.timestampLabel)
                        put("category", log.category)
                        put("summary_vi", log.summaryVi)
                        put("created_at_millis", log.createdAtMillis)
                    })
                }
            })
        }
        addStudyLogInternal("Backup", "Xuất tệp sao lưu toàn bộ dữ liệu (.json)")
        root.toString(2)
    }

    data class BackupRestoreResult(
        val restoredRecordsCount: Int,
        val restoredSheetUrl: String?
    )

    suspend fun restoreFullBackupJson(rawJson: String): Result<BackupRestoreResult> = withContext(Dispatchers.IO) {
        runCatching {
            val root = JSONObject(rawJson)
            var count = 0

            root.optJSONArray("modules")?.let { arr ->
                val list = (0 until arr.length()).mapNotNull { i ->
                    arr.optJSONObject(i)?.let { BundledJsonSeedLoader.parseModuleObject(it) }
                }
                if (list.isNotEmpty()) {
                    dao.upsertModules(list)
                    count += list.size
                }
            }

            root.optJSONArray("vocabulary")?.let { arr ->
                val list = (0 until arr.length()).mapNotNull { i ->
                    arr.optJSONObject(i)?.let { BundledJsonSeedLoader.parseVocabObject(it) }
                }
                if (list.isNotEmpty()) {
                    dao.upsertVocabulary(list)
                    count += list.size
                }
            }

            root.optJSONArray("sentence_drills")?.let { arr ->
                val list = (0 until arr.length()).mapNotNull { i ->
                    arr.optJSONObject(i)?.let { BundledJsonSeedLoader.parseSentenceDrillObject(it) }
                }
                if (list.isNotEmpty()) {
                    dao.upsertSentenceDrills(list)
                    count += list.size
                }
            }

            root.optJSONArray("exams")?.let { arr ->
                val list = (0 until arr.length()).mapNotNull { i ->
                    arr.optJSONObject(i)?.let { BundledJsonSeedLoader.parseExamObject(it) }
                }
                if (list.isNotEmpty()) {
                    dao.upsertExams(list)
                    count += list.size
                }
            }

            root.optJSONArray("personal_materials")?.let { arr ->
                val list = (0 until arr.length()).mapNotNull { i ->
                    arr.optJSONObject(i)?.let { BundledJsonSeedLoader.parsePersonalMaterialObject(it) }
                }
                if (list.isNotEmpty()) {
                    dao.upsertPersonalMaterials(list)
                    count += list.size
                }
            }

            root.optJSONArray("study_logs")?.let { arr ->
                val list = (0 until arr.length()).mapNotNull { i ->
                    arr.optJSONObject(i)?.let { obj ->
                        StudyLogEntity(
                            id = obj.optString("id", "log_r_$i"),
                            timestampLabel = obj.optString("timestamp_label", ""),
                            category = obj.optString("category", "Restore"),
                            summaryVi = obj.optString("summary_vi", ""),
                            createdAtMillis = obj.optLong("created_at_millis", System.currentTimeMillis())
                        )
                    }
                }
                if (list.isNotEmpty()) dao.upsertStudyLogs(list)
            }

            if (count == 0) {
                error("Tệp JSON không chứa dữ liệu ApexIELTS hợp lệ (thiếu modules / vocabulary / exams / personal_materials).")
            }

            addStudyLogInternal("Backup", "Khôi phục $count bản ghi từ tệp sao lưu (.json)")
            BackupRestoreResult(
                restoredRecordsCount = count,
                restoredSheetUrl = root.optString("google_sheet_url", "").ifBlank { null }
            )
        }
    }
}
