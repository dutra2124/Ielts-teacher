package com.example.data.local

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BusinessCenter
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.DailyBandTip
import com.example.data.ExamDifficulty
import com.example.data.ExamPaper
import com.example.data.ExamParagraph
import com.example.data.ExamQuestionItem
import com.example.data.ExamQuestionSkill
import com.example.data.ExamQuestionType
import com.example.data.IeltsModule
import com.example.data.RoadmapTier
import com.example.data.SyntaxExercise
import com.example.data.VocabWordItem
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "modules")
data class ModuleEntity(
    @PrimaryKey val id: Int,
    val tier: String,
    val titleEn: String,
    val titleVi: String,
    val focusVi: String,
    val iconKey: String,
    val keyCollocationsJson: String,
    val dailyTipsJson: String
)

@Entity(tableName = "vocabulary")
data class VocabularyEntity(
    @PrimaryKey val id: String,
    val moduleId: Int,
    val word: String,
    val partOfSpeech: String,
    val cefrLevel: String,
    val ipaUk: String,
    val ipaUs: String,
    val meaningVi: String,
    val exampleEn: String,
    val exampleVi: String,
    val wordFamily: String,
    val dependentPrepositions: String,
    val verbPatterns: String,
    val registerNoteVi: String,
    val band6PitfallVi: String,
    val pronunciationFocusVi: String,
    val band6ParaphraseEn: String,
    val collocationStemEn: String,
    val collocationPartnerEn: String,
    val clozeSentenceEn: String
)

@Entity(tableName = "sentence_drills")
data class SentenceDrillEntity(
    @PrimaryKey val id: String,
    val moduleId: Int,
    val structureTagVi: String,
    val structureNameEn: String,
    val meaningVi: String,
    val orderedChunksJson: String,
    val scrambledChunksJson: String,
    val grammarExplanationVi: String
)

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey val id: String,
    val title: String,
    val sourceLabel: String,
    val year: Int,
    val difficulty: String,
    val topicVi: String,
    val topicEn: String,
    val isCustom: Boolean,
    val audioUri: String?,
    val audioDisplayName: String?,
    val listeningTitle: String,
    val listeningScriptJson: String,
    val readingTitle: String,
    val readingParagraphsJson: String,
    val writingTask1Prompt: String,
    val writingTask2Prompt: String,
    val speakingPromptsJson: String,
    val questionsJson: String
)

@Entity(tableName = "personal_materials")
data class PersonalMaterialEntity(
    @PrimaryKey val id: String,
    val moduleId: Int,
    val title: String,
    val category: String,
    val contentEn: String,
    val notesVi: String,
    val tagsJson: String,
    val updatedAt: String,
    val audioUri: String? = null,
    val audioDisplayName: String? = null
)

@Entity(tableName = "audio_blobs")
data class AudioBlobEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val mimeType: String,
    val localPath: String,
    val sizeBytes: Long,
    val linkedExamId: String?,
    val createdAtMillis: Long
)

@Entity(tableName = "study_logs")
data class StudyLogEntity(
    @PrimaryKey val id: String,
    val timestampLabel: String,
    val category: String,
    val summaryVi: String,
    val createdAtMillis: Long
)

object LocalEntityMappers {

    fun iconForKey(iconKey: String): ImageVector = when (iconKey.trim().lowercase()) {
        "school", "education" -> Icons.Rounded.School
        "business", "work" -> Icons.Rounded.BusinessCenter
        "health", "lifestyle" -> Icons.Rounded.SelfImprovement
        "tourism", "travel" -> Icons.Rounded.FlightTakeoff
        "environment", "eco", "climate" -> Icons.Rounded.Eco
        "urban", "city", "infrastructure" -> Icons.Rounded.LocationCity
        "tech", "ai", "memory" -> Icons.Rounded.Memory
        "media", "arts", "palette" -> Icons.Rounded.Palette
        "economy", "finance", "macro" -> Icons.Rounded.AccountBalance
        "law", "crime", "ethics" -> Icons.Rounded.Gavel
        "psychology", "behavior" -> Icons.Rounded.Psychology
        "space", "rocket", "science" -> Icons.Rounded.RocketLaunch
        else -> Icons.Rounded.AutoAwesome
    }

    fun parseTier(raw: String): RoadmapTier {
        val norm = raw.trim().uppercase()
        return when {
            norm.contains("1") || norm.contains("FOUNDATION") -> RoadmapTier.TIER_1_FOUNDATION
            norm.contains("2") || norm.contains("EXPANSION") -> RoadmapTier.TIER_2_EXPANSION
            norm.contains("3") || norm.contains("MASTERY") -> RoadmapTier.TIER_3_MASTERY
            else -> RoadmapTier.TIER_1_FOUNDATION
        }
    }

    fun ModuleEntity.toDomain(): IeltsModule {
        val collocations = parseStringArrayJson(keyCollocationsJson)
        val tips = parseDailyTipsJson(dailyTipsJson).ifEmpty {
            listOf(
                DailyBandTip(
                    id = "m${id}_fallback_tip",
                    categoryVi = "Lexical Resource • Band 8.5",
                    tipVi = focusVi,
                    upgradeFromEn = "basic phrasing in $titleEn",
                    academicPhraseEn = collocations.firstOrNull() ?: titleEn
                )
            )
        }
        return IeltsModule(
            id = id,
            tier = parseTier(tier),
            titleEn = titleEn,
            titleVi = titleVi,
            focusVi = focusVi,
            icon = iconForKey(iconKey),
            keyCollocations = collocations,
            dailyTips = tips
        )
    }

    fun VocabularyEntity.toDomain(): VocabWordItem = VocabWordItem(
        id = id,
        moduleId = moduleId,
        word = word,
        partOfSpeech = partOfSpeech,
        cefrLevel = cefrLevel,
        ipaUk = ipaUk,
        ipaUs = ipaUs,
        meaningVi = meaningVi,
        exampleEn = exampleEn,
        exampleVi = exampleVi,
        wordFamily = wordFamily,
        dependentPrepositions = dependentPrepositions,
        verbPatterns = verbPatterns,
        registerNoteVi = registerNoteVi,
        band6PitfallVi = band6PitfallVi,
        pronunciationFocusVi = pronunciationFocusVi,
        band6ParaphraseEn = band6ParaphraseEn,
        collocationStemEn = collocationStemEn,
        collocationPartnerEn = collocationPartnerEn,
        clozeSentenceEn = clozeSentenceEn
    )

    fun VocabWordItem.toEntity(): VocabularyEntity = VocabularyEntity(
        id = id,
        moduleId = moduleId,
        word = word,
        partOfSpeech = partOfSpeech,
        cefrLevel = cefrLevel,
        ipaUk = ipaUk,
        ipaUs = ipaUs,
        meaningVi = meaningVi,
        exampleEn = exampleEn,
        exampleVi = exampleVi,
        wordFamily = wordFamily,
        dependentPrepositions = dependentPrepositions,
        verbPatterns = verbPatterns,
        registerNoteVi = registerNoteVi,
        band6PitfallVi = band6PitfallVi,
        pronunciationFocusVi = pronunciationFocusVi,
        band6ParaphraseEn = band6ParaphraseEn,
        collocationStemEn = collocationStemEn,
        collocationPartnerEn = collocationPartnerEn,
        clozeSentenceEn = clozeSentenceEn
    )

    fun SentenceDrillEntity.toDomain(): SyntaxExercise {
        val ordered = parseStringArrayJson(orderedChunksJson)
        val scrambled = parseStringArrayJson(scrambledChunksJson).ifEmpty { ordered.reversed() }
        return SyntaxExercise(
            id = id,
            moduleId = moduleId.takeIf { it > 0 },
            structureTagVi = structureTagVi,
            structureNameEn = structureNameEn,
            meaningVi = meaningVi,
            orderedChunks = ordered,
            scrambledChunks = scrambled,
            grammarExplanationVi = grammarExplanationVi
        )
    }

    fun ExamEntity.toDomain(): ExamPaper {
        val diff = ExamDifficulty.entries.firstOrNull { it.name.equals(difficulty, ignoreCase = true) }
            ?: when {
                difficulty.contains("VERY", ignoreCase = true) -> ExamDifficulty.VERY_HARD
                difficulty.contains("MODERATE", ignoreCase = true) -> ExamDifficulty.MODERATE_HARD
                else -> ExamDifficulty.HARD
            }
        return ExamPaper(
            id = id,
            title = title,
            sourceLabel = sourceLabel,
            year = year,
            difficulty = diff,
            topicVi = topicVi,
            topicEn = topicEn,
            isCustom = isCustom,
            audioUri = audioUri?.ifBlank { null },
            audioDisplayName = audioDisplayName?.ifBlank { null },
            listeningTitle = listeningTitle,
            listeningScript = parseStringArrayJson(listeningScriptJson).ifEmpty {
                listOf("Listening audio ready.")
            },
            readingTitle = readingTitle,
            readingParagraphs = parseParagraphsJson(readingParagraphsJson),
            writingTask1Prompt = writingTask1Prompt,
            writingTask2Prompt = writingTask2Prompt,
            speakingPrompts = parseStringArrayJson(speakingPromptsJson),
            questions = parseQuestionsJson(questionsJson)
        )
    }

    fun ExamPaper.toEntity(): ExamEntity = ExamEntity(
        id = id,
        title = title,
        sourceLabel = sourceLabel,
        year = year,
        difficulty = difficulty.name,
        topicVi = topicVi,
        topicEn = topicEn,
        isCustom = isCustom,
        audioUri = audioUri,
        audioDisplayName = audioDisplayName,
        listeningTitle = listeningTitle,
        listeningScriptJson = JSONArray(listeningScript).toString(),
        readingTitle = readingTitle,
        readingParagraphsJson = JSONArray().apply {
            readingParagraphs.forEach { p ->
                put(JSONObject().apply {
                    put("label", p.label)
                    put("sentences", JSONArray(p.sentences))
                })
            }
        }.toString(),
        writingTask1Prompt = writingTask1Prompt,
        writingTask2Prompt = writingTask2Prompt,
        speakingPromptsJson = JSONArray(speakingPrompts).toString(),
        questionsJson = JSONArray().apply {
            questions.forEach { q ->
                put(JSONObject().apply {
                    put("id", q.id)
                    put("number", q.number)
                    put("skill", q.skill.id)
                    put("type", q.type.id)
                    put("prompt_en", q.promptEn)
                    put("options", JSONArray(q.options))
                    put("correct_answer", q.correctAnswer)
                    put("acceptable_answers", JSONArray(q.acceptableAnswers))
                })
            }
        }.toString()
    )

    fun PersonalMaterialEntity.toDomain(): PersonalMaterial = PersonalMaterial(
        id = id,
        moduleId = moduleId,
        title = title,
        category = category,
        contentEn = contentEn,
        notesVi = notesVi,
        tags = parseStringArrayJson(tagsJson),
        updatedAt = updatedAt,
        audioUri = audioUri?.ifBlank { null },
        audioDisplayName = audioDisplayName?.ifBlank { null }
    )

    fun PersonalMaterial.toEntity(): PersonalMaterialEntity = PersonalMaterialEntity(
        id = id,
        moduleId = moduleId,
        title = title,
        category = category,
        contentEn = contentEn,
        notesVi = notesVi,
        tagsJson = JSONArray(tags).toString(),
        updatedAt = updatedAt,
        audioUri = audioUri?.ifBlank { null },
        audioDisplayName = audioDisplayName?.ifBlank { null }
    )

    fun AudioBlobEntity.toDomain(): AudioBlobRecord = AudioBlobRecord(
        id = id,
        displayName = displayName,
        mimeType = mimeType,
        localPath = localPath,
        sizeBytes = sizeBytes,
        linkedExamId = linkedExamId,
        createdAtMillis = createdAtMillis
    )

    fun StudyLogEntity.toDomain(): StudyLogEntry = StudyLogEntry(
        id = id,
        timestampLabel = timestampLabel,
        category = category,
        summaryVi = summaryVi,
        createdAtMillis = createdAtMillis
    )

    fun parseStringArrayJson(raw: String): List<String> {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return emptyList()
        if (trimmed.startsWith("[")) {
            return runCatching {
                val arr = JSONArray(trimmed)
                buildList {
                    for (i in 0 until arr.length()) {
                        val s = arr.optString(i).trim()
                        if (s.isNotEmpty()) add(s)
                    }
                }
            }.getOrDefault(emptyList())
        }
        return trimmed.split("|", ";", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    fun parseDailyTipsJson(raw: String): List<DailyBandTip> {
        val trimmed = raw.trim()
        if (trimmed.isEmpty() || !trimmed.startsWith("[")) return emptyList()
        return runCatching {
            val arr = JSONArray(trimmed)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    add(
                        DailyBandTip(
                            id = obj.optString("id", "tip_$i"),
                            categoryVi = obj.optString("category_vi", obj.optString("categoryVi", "Lexical Resource")),
                            tipVi = obj.optString("tip_vi", obj.optString("tipVi", "")),
                            upgradeFromEn = obj.optString("upgrade_from_en", obj.optString("upgradeFromEn", "")),
                            academicPhraseEn = obj.optString("academic_phrase_en", obj.optString("academicPhraseEn", ""))
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun parseParagraphsJson(raw: String): List<ExamParagraph> {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return emptyList()
        if (!trimmed.startsWith("[")) {
            return listOf(ExamParagraph(label = "A", sentences = listOf(trimmed)))
        }
        return runCatching {
            val arr = JSONArray(trimmed)
            buildList {
                for (i in 0 until arr.length()) {
                    val item = arr.opt(i)
                    when (item) {
                        is JSONObject -> {
                            val label = item.optString("label", ('A' + i).toString())
                            val sArr = item.optJSONArray("sentences")
                            val sentences = buildList {
                                if (sArr != null) {
                                    for (j in 0 until sArr.length()) {
                                        val s = sArr.optString(j).trim()
                                        if (s.isNotEmpty()) add(s)
                                    }
                                } else {
                                    val single = item.optString("text", "").trim()
                                    if (single.isNotEmpty()) add(single)
                                }
                            }
                            if (sentences.isNotEmpty()) add(ExamParagraph(label, sentences))
                        }
                        is String -> {
                            if (item.isNotBlank()) add(ExamParagraph(('A' + i).toString(), listOf(item.trim())))
                        }
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    fun parseQuestionsJson(raw: String): List<ExamQuestionItem> {
        val trimmed = raw.trim()
        if (trimmed.isEmpty() || !trimmed.startsWith("[")) return emptyList()
        return runCatching {
            val arr = JSONArray(trimmed)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val type = ExamQuestionType.fromId(obj.optString("type", "mcq"))
                    val answer = obj.optString("correct_answer", obj.optString("correctAnswer", "")).trim()
                    val options = buildList {
                        val optArr = obj.optJSONArray("options")
                        if (optArr != null) {
                            for (j in 0 until optArr.length()) {
                                val v = optArr.optString(j).trim()
                                if (v.isNotEmpty()) add(v)
                            }
                        }
                    }.ifEmpty {
                        if (type == ExamQuestionType.TFNG) listOf("TRUE", "FALSE", "NOT GIVEN") else emptyList()
                    }
                    val acceptable = buildList {
                        val accArr = obj.optJSONArray("acceptable_answers") ?: obj.optJSONArray("acceptableAnswers")
                        if (accArr != null) {
                            for (j in 0 until accArr.length()) {
                                val v = accArr.optString(j).trim()
                                if (v.isNotEmpty()) add(v)
                            }
                        }
                    }.ifEmpty { listOf(answer) }

                    add(
                        ExamQuestionItem(
                            id = obj.optString("id", "q_${i + 1}"),
                            number = obj.optInt("number", i + 1),
                            skill = ExamQuestionSkill.fromId(obj.optString("skill", "reading")),
                            type = type,
                            promptEn = obj.optString("prompt_en", obj.optString("promptEn", "Question ${i + 1}")),
                            options = options,
                            correctAnswer = answer,
                            acceptableAnswers = acceptable
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }
}
