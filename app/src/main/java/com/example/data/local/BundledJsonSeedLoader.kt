package com.example.data.local

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Loads the 5 bundled static JSON seed files from assets/data/ so the app boots
 * immediately and runs 100% offline with zero internet connectivity.
 */
class BundledJsonSeedLoader(private val context: Context) {

    private fun readAssetJsonArray(assetPath: String): JSONArray {
        val raw = context.assets.open(assetPath).bufferedReader(Charsets.UTF_8).use { it.readText() }
        return JSONArray(raw)
    }

    fun loadModules(): List<ModuleEntity> = runCatching {
        val arr = readAssetJsonArray("data/modules.json")
        buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                add(parseModuleObject(obj))
            }
        }
    }.getOrDefault(emptyList())

    fun loadVocabulary(): List<VocabularyEntity> = runCatching {
        val arr = readAssetJsonArray("data/vocabulary.json")
        buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                add(parseVocabObject(obj))
            }
        }
    }.getOrDefault(emptyList())

    fun loadSentenceDrills(): List<SentenceDrillEntity> = runCatching {
        val arr = readAssetJsonArray("data/sentenceDrills.json")
        buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                add(parseSentenceDrillObject(obj))
            }
        }
    }.getOrDefault(emptyList())

    fun loadExams(): List<ExamEntity> = runCatching {
        val arr = readAssetJsonArray("data/exams.json")
        buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                add(parseExamObject(obj))
            }
        }
    }.getOrDefault(emptyList())

    fun loadPersonalMaterials(): List<PersonalMaterialEntity> = runCatching {
        val arr = readAssetJsonArray("data/personalMaterials.json")
        buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                add(parsePersonalMaterialObject(obj))
            }
        }
    }.getOrDefault(emptyList())

    companion object {
        fun parseModuleObject(obj: JSONObject): ModuleEntity {
            val collocationsRaw = obj.optJSONArray("key_collocations")?.toString()
                ?: obj.optString("key_collocations_json", obj.optString("key_collocations", "[]"))
            val tipsRaw = obj.optJSONArray("daily_tips")?.toString()
                ?: obj.optString("daily_tips_json", obj.optString("daily_tips", "[]"))
            return ModuleEntity(
                id = obj.optInt("id", 1),
                tier = obj.optString("tier", "TIER_1_FOUNDATION"),
                titleEn = obj.optString("title_en", obj.optString("titleEn", "IELTS Module")),
                titleVi = obj.optString("title_vi", obj.optString("titleVi", "Chủ đề IELTS")),
                focusVi = obj.optString("focus_vi", obj.optString("focusVi", "")),
                iconKey = obj.optString("icon_key", obj.optString("iconKey", "school")),
                keyCollocationsJson = collocationsRaw,
                dailyTipsJson = tipsRaw
            )
        }

        fun parseVocabObject(obj: JSONObject): VocabularyEntity {
            val word = obj.optString("word", "").trim()
            val moduleId = obj.optInt("module_id", obj.optInt("moduleId", 1)).coerceIn(1, 12)
            val fallbackId = "m${moduleId}_${word.lowercase().replace(Regex("[^a-z0-9]+"), "_")}"
            return VocabularyEntity(
                id = obj.optString("id", fallbackId).ifBlank { fallbackId },
                moduleId = moduleId,
                word = word,
                partOfSpeech = obj.optString("part_of_speech", obj.optString("partOfSpeech", "noun")),
                cefrLevel = obj.optString("cefr_level", obj.optString("cefrLevel", "C1")),
                ipaUk = obj.optString("ipa_uk", obj.optString("ipaUk", "")),
                ipaUs = obj.optString("ipa_us", obj.optString("ipaUs", "")),
                meaningVi = obj.optString("meaning_vi", obj.optString("meaningVi", "")),
                exampleEn = obj.optString("example_en", obj.optString("exampleEn", "")),
                exampleVi = obj.optString("example_vi", obj.optString("exampleVi", "")),
                wordFamily = obj.optString("word_family", obj.optString("wordFamily", "")),
                dependentPrepositions = obj.optString("dependent_prepositions", obj.optString("dependentPrepositions", "")),
                verbPatterns = obj.optString("verb_patterns", obj.optString("verbPatterns", "")),
                registerNoteVi = obj.optString("register_note_vi", obj.optString("registerNoteVi", "Academic Formal")),
                band6PitfallVi = obj.optString("band6_pitfall_vi", obj.optString("band6PitfallVi", "")),
                pronunciationFocusVi = obj.optString("pronunciation_focus_vi", obj.optString("pronunciationFocusVi", "")),
                band6ParaphraseEn = obj.optString("band6_paraphrase_en", obj.optString("band6ParaphraseEn", "")),
                collocationStemEn = obj.optString("collocation_stem_en", obj.optString("collocationStemEn", "______")),
                collocationPartnerEn = obj.optString("collocation_partner_en", obj.optString("collocationPartnerEn", word)),
                clozeSentenceEn = obj.optString("cloze_sentence_en", obj.optString("clozeSentenceEn", ""))
            )
        }

        fun parseSentenceDrillObject(obj: JSONObject): SentenceDrillEntity {
            val orderedRaw = obj.optJSONArray("ordered_chunks")?.toString()
                ?: obj.optString("ordered_chunks_json", obj.optString("ordered_chunks", "[]"))
            val scrambledRaw = obj.optJSONArray("scrambled_chunks")?.toString()
                ?: obj.optString("scrambled_chunks_json", obj.optString("scrambled_chunks", orderedRaw))
            return SentenceDrillEntity(
                id = obj.optString("id", "syn_${System.currentTimeMillis()}"),
                moduleId = obj.optInt("module_id", obj.optInt("moduleId", 0)),
                structureTagVi = obj.optString("structure_tag_vi", obj.optString("structureTagVi", "Cấu trúc Band 8.5")),
                structureNameEn = obj.optString("structure_name_en", obj.optString("structureNameEn", "Academic Structure")),
                meaningVi = obj.optString("meaning_vi", obj.optString("meaningVi", "")),
                orderedChunksJson = orderedRaw,
                scrambledChunksJson = scrambledRaw,
                grammarExplanationVi = obj.optString("grammar_explanation_vi", obj.optString("grammarExplanationVi", ""))
            )
        }

        fun parseExamObject(obj: JSONObject): ExamEntity {
            val scriptRaw = obj.optJSONArray("listening_script")?.toString()
                ?: obj.optString("listening_script_json", obj.optString("listening_script", "[]"))
            val paragraphsRaw = obj.optJSONArray("reading_paragraphs")?.toString()
                ?: obj.optString("reading_paragraphs_json", obj.optString("reading_paragraphs", "[]"))
            val speakingRaw = obj.optJSONArray("speaking_prompts")?.toString()
                ?: obj.optString("speaking_prompts_json", obj.optString("speaking_prompts", "[]"))
            val questionsRaw = obj.optJSONArray("questions")?.toString()
                ?: obj.optString("questions_json", "[]")
            return ExamEntity(
                id = obj.optString("id", "exam_${System.currentTimeMillis()}"),
                title = obj.optString("title", "IELTS Academic Test"),
                sourceLabel = obj.optString("source_label", obj.optString("sourceLabel", "Official Style")),
                year = obj.optInt("year", 2025),
                difficulty = obj.optString("difficulty", "HARD"),
                topicVi = obj.optString("topic_vi", obj.optString("topicVi", "Tổng hợp học thuật")),
                topicEn = obj.optString("topic_en", obj.optString("topicEn", "Academic IELTS")),
                isCustom = obj.optBoolean("is_custom", obj.optBoolean("isCustom", false)),
                audioUri = obj.optString("audio_uri", obj.optString("audioUri", "")).ifBlank { null },
                audioDisplayName = obj.optString("audio_display_name", obj.optString("audioDisplayName", "")).ifBlank { null },
                listeningTitle = obj.optString("listening_title", obj.optString("listeningTitle", "Listening Section")),
                listeningScriptJson = scriptRaw,
                readingTitle = obj.optString("reading_title", obj.optString("readingTitle", "Reading Passage")),
                readingParagraphsJson = paragraphsRaw,
                writingTask1Prompt = obj.optString("writing_task1_prompt", obj.optString("writingTask1Prompt", "")),
                writingTask2Prompt = obj.optString("writing_task2_prompt", obj.optString("writingTask2Prompt", "")),
                speakingPromptsJson = speakingRaw,
                questionsJson = questionsRaw
            )
        }

        fun parsePersonalMaterialObject(obj: JSONObject): PersonalMaterialEntity {
            val tagsRaw = obj.optJSONArray("tags")?.toString()
                ?: obj.optString("tags_json", obj.optString("tags", "[]"))
            return PersonalMaterialEntity(
                id = obj.optString("id", "pm_${System.currentTimeMillis()}"),
                moduleId = obj.optInt("module_id", obj.optInt("moduleId", 1)).coerceIn(1, 12),
                title = obj.optString("title", "Ghi chú tự học IELTS"),
                category = obj.optString("category", "Ghi chú"),
                contentEn = obj.optString("content_en", obj.optString("contentEn", "")),
                notesVi = obj.optString("notes_vi", obj.optString("notesVi", "")),
                tagsJson = tagsRaw,
                updatedAt = obj.optString("updated_at", obj.optString("updatedAt", "2026-11-19")),
                audioUri = obj.optString("audio_uri", obj.optString("audioUri", "")).ifBlank { null },
                audioDisplayName = obj.optString("audio_display_name", obj.optString("audioDisplayName", "")).ifBlank { null }
            )
        }
    }
}
