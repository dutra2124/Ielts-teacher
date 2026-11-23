package com.example.ai

import com.example.data.LearnCurriculum
import com.example.data.VocabWordItem
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

/**
 * Lookup result shown in the Interactive Article Reader tap-to-lookup bottom sheet.
 */
data class WordLookupResult(
    val word: String,
    val partOfSpeech: String,
    val cefrLevel: String,
    val ipaUk: String,
    val ipaUs: String,
    val meaningVi: String,
    val collocationEn: String,
    val contextSentenceEn: String,
    val sourceLabelVi: String,
    val isFromAi: Boolean = false
) {
    fun toVocabWordItem(moduleId: Int): VocabWordItem {
        val cleanWord = word.trim()
        val slug = cleanWord.lowercase(Locale.US).replace(Regex("[^a-z0-9]+"), "_").take(24)
        return VocabWordItem(
            id = "reader_${slug}_${UUID.randomUUID().toString().take(6)}",
            moduleId = moduleId.coerceIn(1, 12),
            word = cleanWord,
            partOfSpeech = partOfSpeech.ifBlank { "noun" },
            cefrLevel = cefrLevel.ifBlank { "C1" },
            ipaUk = ipaUk.ifBlank { "/$cleanWord/" },
            ipaUs = ipaUs.ifBlank { ipaUk.ifBlank { "/$cleanWord/" } },
            meaningVi = meaningVi.ifBlank { "Từ vựng trích từ bài đọc tự học" },
            exampleEn = contextSentenceEn.ifBlank { "Example containing $cleanWord." },
            exampleVi = "Ngữ cảnh trích từ bài đọc Tự học: $meaningVi",
            wordFamily = "$cleanWord ($partOfSpeech)",
            dependentPrepositions = collocationEn.ifBlank { cleanWord },
            verbPatterns = collocationEn.ifBlank { cleanWord },
            registerNoteVi = "Tự học từ bài đọc học thuật ($sourceLabelVi).",
            band6PitfallVi = "Ghi nhớ sắc thái và collocation của \"$cleanWord\" ngay trong câu ngữ cảnh gốc.",
            pronunciationFocusVi = "Nhấn trọng âm rõ và phát âm trọn phụ âm cuối của \"$cleanWord\".",
            band6ParaphraseEn = cleanWord,
            collocationStemEn = collocationEn.ifBlank { cleanWord },
            collocationPartnerEn = cleanWord,
            clozeSentenceEn = contextSentenceEn.replace(cleanWord, "______", ignoreCase = true)
        )
    }
}

/**
 * Dual-layer IELTS Word Lookup Engine:
 * 1. Instant zero-latency lookup from local curriculum, imported SQLite vocabulary,
 *    and built-in C1-C2 academic mini-dictionary.
 * 2. Quick Gemini contextual lookup via Firebase AI Logic when word is not in local dictionary
 *    or when user requests contextual AI nuance for the exact surrounding sentence.
 */
object WordLookupService {

    private const val MODEL_NAME = "gemini-3.5-flash-lite"
    private const val TIMEOUT_MS = 25_000L

    private data class MiniDictEntry(
        val pos: String,
        val cefr: String,
        val ipaUk: String,
        val ipaUs: String,
        val meaningVi: String,
        val collocation: String
    )

    private val localAcademicDict: Map<String, MiniDictEntry> = mapOf(
        "mitigate" to MiniDictEntry("verb", "C1", "/ˈmɪt.ɪ.ɡeɪt/", "/ˈmɪt̬.ə.ɡeɪt/", "giảm nhẹ, làm dịu bớt (tác hại, rủi ro)", "mitigate the adverse effects"),
        "exacerbate" to MiniDictEntry("verb", "C2", "/ɪɡˈzæs.ə.beɪt/", "/ɪɡˈzæs.ɚ.beɪt/", "làm trầm trọng thêm (vấn đề, khủng hoảng)", "exacerbate social inequality"),
        "ubiquitous" to MiniDictEntry("adj", "C2", "/juːˈbɪk.wɪ.təs/", "/juːˈbɪk.wə.t̬əs/", "phổ biến khắp nơi, có mặt ở mọi nơi", "ubiquitous digital technology"),
        "unprecedented" to MiniDictEntry("adj", "C1", "/ʌnˈpres.ɪ.den.tɪd/", "/ʌnˈpres.ə.den.t̬ɪd/", "chưa từng có tiền lệ", "at an unprecedented pace"),
        "sustainable" to MiniDictEntry("adj", "C1", "/səˈsteɪ.nə.bəl/", "/səˈsteɪ.nə.bəl/", "bền vững, duy trì được lâu dài", "sustainable urban development"),
        "infrastructure" to MiniDictEntry("noun", "C1", "/ˈɪn.frə.strʌk.tʃər/", "/ˈɪn.frə.strʌk.tʃɚ/", "cơ sở hạ tầng", "public transport infrastructure"),
        "proliferation" to MiniDictEntry("noun", "C2", "/prəˌlɪf.ərˈeɪ.ʃən/", "/prəˌlɪf.əˈreɪ.ʃən/", "sự gia tăng nhanh chóng, lan rộng", "the proliferation of misinformation"),
        "alleviate" to MiniDictEntry("verb", "C1", "/əˈliː.vi.eɪt/", "/əˈliː.vi.eɪt/", "làm giảm bớt, xoa dịu (khó khăn, tắc nghẽn)", "alleviate traffic congestion"),
        "deteriorate" to MiniDictEntry("verb", "C1", "/dɪˈtɪə.ri.ə.reɪt/", "/dɪˈtɪr.i.ə.reɪt/", "xấu đi, suy thoái dần", "deteriorate rapidly"),
        "empirical" to MiniDictEntry("adj", "C2", "/ɪmˈpɪr.ɪ.kəl/", "/ɪmˈpɪr.ɪ.kəl/", "dựa trên thực nghiệm, bằng chứng thực tế", "empirical evidence"),
        "paradigm" to MiniDictEntry("noun", "C2", "/ˈpær.ə.daɪm/", "/ˈper.ə.daɪm/", "mô hình, hệ tư tưởng chuẩn mực", "a paradigm shift"),
        "disparity" to MiniDictEntry("noun", "C2", "/dɪˈspær.ə.ti/", "/dɪˈsper.ə.t̬i/", "sự chênh lệch, bất bình đẳng rõ rệt", "socioeconomic disparity"),
        "resilience" to MiniDictEntry("noun", "C1", "/rɪˈzɪl.jəns/", "/rɪˈzɪl.jəns/", "khả năng phục hồi, sức chống chịu", "economic resilience"),
        "intervention" to MiniDictEntry("noun", "C1", "/ˌɪn.təˈven.ʃən/", "/ˌɪn.t̬ɚˈven.ʃən/", "sự can thiệp (của chính phủ/y tế)", "state intervention"),
        "allocation" to MiniDictEntry("noun", "C1", "/ˌæl.əˈkeɪ.ʃən/", "/ˌæl.əˈkeɪ.ʃən/", "sự phân bổ (nguồn lực, ngân sách)", "efficient resource allocation"),
        "automation" to MiniDictEntry("noun", "C1", "/ˌɔː.təˈmeɪ.ʃən/", "/ˌɑː.t̬əˈmeɪ.ʃən/", "sự tự động hóa", "workplace automation"),
        "sedentary" to MiniDictEntry("adj", "C2", "/ˈsed.ən.tər.i/", "/ˈsed.ən.ter.i/", "ít vận động, ngồi nhiều", "a sedentary lifestyle"),
        "cognitive" to MiniDictEntry("adj", "C1", "/ˈkɒɡ.nə.tɪv/", "/ˈkɑːɡ.nə.t̬ɪv/", "thuộc về nhận thức, tư duy", "cognitive development"),
        "pedagogy" to MiniDictEntry("noun", "C2", "/ˈped.ə.ɡɒdʒ.i/", "/ˈped.ə.ɡɑː.dʒi/", "khoa học sư phạm, phương pháp giảng dạy", "modern pedagogy"),
        "curriculum" to MiniDictEntry("noun", "C1", "/kəˈrɪk.jə.ləm/", "/kəˈrɪk.jə.ləm/", "chương trình giảng dạy", "a holistic curriculum"),
        "emissions" to MiniDictEntry("noun", "C1", "/iˈmɪʃ.ənz/", "/iˈmɪʃ.ənz/", "khí thải (carbon, nhà kính)", "curb greenhouse gas emissions"),
        "conservation" to MiniDictEntry("noun", "C1", "/ˌkɒn.səˈveɪ.ʃən/", "/ˌkɑːn.sɚˈveɪ.ʃən/", "sự bảo tồn (thiên nhiên, di sản)", "wildlife conservation"),
        "urbanization" to MiniDictEntry("noun", "C1", "/ˌɜː.bən.aɪˈzeɪ.ʃən/", "/ˌɝː.bən.əˈzeɪ.ʃən/", "quá trình đô thị hóa", "rapid urbanization"),
        "globalization" to MiniDictEntry("noun", "C1", "/ˌɡləʊ.bəl.aɪˈzeɪ.ʃən/", "/ˌɡloʊ.bəl.əˈzeɪ.ʃən/", "toàn cầu hóa", "economic globalization"),
        "innovation" to MiniDictEntry("noun", "B2", "/ˌɪn.əˈveɪ.ʃən/", "/ˌɪn.əˈveɪ.ʃən/", "sự đổi mới, sáng tạo đột phá", "technological innovation"),
        "productivity" to MiniDictEntry("noun", "C1", "/ˌprɒd.ʌkˈtɪv.ə.ti/", "/ˌproʊ.dʌkˈtɪv.ə.t̬i/", "năng suất lao động", "boost workplace productivity"),
        "incentive" to MiniDictEntry("noun", "C1", "/ɪnˈsen.tɪv/", "/ɪnˈsen.t̬ɪv/", "động lực khuyến khích, ưu đãi", "financial incentives"),
        "subsidy" to MiniDictEntry("noun", "C1", "/ˈsʌb.sɪ.di/", "/ˈsʌb.sə.di/", "khoản trợ cấp của nhà nước", "government subsidies"),
        "legislation" to MiniDictEntry("noun", "C1", "/ˌledʒ.ɪˈsleɪ.ʃən/", "/ˌledʒ.əˈsleɪ.ʃən/", "luật pháp, hệ thống văn bản pháp luật", "strict environmental legislation"),
        "rehabilitation" to MiniDictEntry("noun", "C1", "/ˌriː.həˌbɪl.ɪˈteɪ.ʃən/", "/ˌriː.həˌbɪl.əˈteɪ.ʃən/", "sự cải tạo, phục hồi chức năng/tái hòa nhập", "offender rehabilitation"),
        "deterrent" to MiniDictEntry("noun", "C2", "/dɪˈter.ənt/", "/dɪˈtɝː.ənt/", "biện pháp răn đe", "an effective deterrent"),
        "inequality" to MiniDictEntry("noun", "C1", "/ˌɪn.ɪˈkwɒl.ə.ti/", "/ˌɪn.ɪˈkwɑː.lə.t̬i/", "sự bất bình đẳng", "income inequality"),
        "biodiversity" to MiniDictEntry("noun", "C1", "/ˌbaɪ.əʊ.daɪˈvɜː.sə.ti/", "/ˌbaɪ.oʊ.dɪˈvɝː.sə.t̬i/", "đa dạng sinh học", "preserve marine biodiversity"),
        "phenomenon" to MiniDictEntry("noun", "C1", "/fəˈnɒm.ɪ.nən/", "/fəˈnɑː.mə.nɑːn/", "hiện tượng (xã hội, tự nhiên)", "a widespread phenomenon"),
        "predominantly" to MiniDictEntry("adv", "C1", "/prɪˈdɒm.ɪ.nənt.li/", "/prɪˈdɑː.mə.nənt.li/", "chủ yếu, phần lớn", "predominantly driven by"),
        "substantially" to MiniDictEntry("adv", "C1", "/səbˈstæn.ʃəl.i/", "/səbˈstæn.ʃəl.i/", "đáng kể, một cách rõ rệt", "substantially reduce costs"),
        "imperative" to MiniDictEntry("adj", "C2", "/ɪmˈper.ə.tɪv/", "/ɪmˈper.ə.t̬ɪv/", "cấp bách, tối quan trọng", "it is imperative that"),
        "detrimental" to MiniDictEntry("adj", "C2", "/ˌdet.rɪˈmen.təl/", "/ˌdet.rəˈmen.t̬əl/", "có hại, gây tổn hại", "detrimental to mental health"),
        "lucrative" to MiniDictEntry("adj", "C2", "/ˈluː.krə.tɪv/", "/ˈluː.krə.t̬ɪv/", "sinh lời cao, béo bở", "a lucrative industry"),
        "viable" to MiniDictEntry("adj", "C1", "/ˈvaɪ.ə.bəl/", "/ˈvaɪ.ə.bəl/", "khả thi, có thể thực hiện bền vững", "an economically viable alternative"),
        "foster" to MiniDictEntry("verb", "C1", "/ˈfɒs.tər/", "/ˈfɑː.stɚ/", "thúc đẩy, nuôi dưỡng", "foster critical thinking"),
        "curb" to MiniDictEntry("verb", "C1", "/kɜːb/", "/kɝːb/", "kiềm chế, cắt giảm mạnh", "curb carbon emissions"),
        "advocate" to MiniDictEntry("verb", "C1", "/ˈæd.və.keɪt/", "/ˈæd.və.keɪt/", "ủng hộ, kêu gọi công khai", "advocate for educational reform"),
        "undermine" to MiniDictEntry("verb", "C1", "/ˌʌn.dəˈmaɪn/", "/ˌʌn.dɚˈmaɪn/", "làm suy yếu, hủy hoại ngầm", "undermine public trust"),
        "catalyze" to MiniDictEntry("verb", "C2", "/ˈkæt.əl.aɪz/", "/ˈkæt̬.əl.aɪz/", "thúc đẩy, làm chất xúc tác", "catalyze economic growth"),
        "disrupt" to MiniDictEntry("verb", "C1", "/dɪsˈrʌpt/", "/dɪsˈrʌpt/", "làm gián đoạn, đảo lộn", "disrupt traditional markets"),
        "enhance" to MiniDictEntry("verb", "B2", "/ɪnˈhɑːns/", "/ɪnˈhæns/", "nâng cao, cải thiện chất lượng", "enhance cognitive performance"),
        "implement" to MiniDictEntry("verb", "C1", "/ˈɪm.plɪ.ment/", "/ˈɪm.plə.ment/", "triển khai, thực thi (chính sách)", "implement stringent regulations"),
        "inevitable" to MiniDictEntry("adj", "C1", "/ɪˈnev.ɪ.tə.bəl/", "/ɪˈnev.ə.t̬ə.bəl/", "tất yếu, không thể tránh khỏi", "an inevitable consequence"),
        "prevalent" to MiniDictEntry("adj", "C2", "/ˈprev.əl.ənt/", "/ˈprev.əl.ənt/", "thịnh hành, phổ biến rộng rãi", "increasingly prevalent among youth"),
        "indigenous" to MiniDictEntry("adj", "C2", "/ɪnˈdɪdʒ.ɪ.nəs/", "/ɪnˈdɪdʒ.ə.nəs/", "bản địa, bản xứ", "preserve indigenous heritage"),
        "stringent" to MiniDictEntry("adj", "C2", "/ˈstrɪn.dʒənt/", "/ˈstrɪn.dʒənt/", "nghiêm ngặt, chặt chẽ", "stringent safety standards"),
        "autonomous" to MiniDictEntry("adj", "C1", "/ɔːˈtɒn.ə.məs/", "/ɑːˈtɑː.nə.məs/", "tự chủ, tự hành", "autonomous vehicles"),
        "algorithmic" to MiniDictEntry("adj", "C2", "/ˌæl.ɡəˈrɪð.mɪk/", "/ˌæl.ɡəˈrɪð.mɪk/", "thuộc về thuật toán", "algorithmic bias"),
        "empirical" to MiniDictEntry("adj", "C2", "/ɪmˈpɪr.ɪ.kəl/", "/ɪmˈpɪr.ɪ.kəl/", "dựa trên thực nghiệm", "empirical data"),
        "consensus" to MiniDictEntry("noun", "C1", "/kənˈsen.səs/", "/kənˈsen.səs/", "sự đồng thuận chung", "scientific consensus"),
        "consequence" to MiniDictEntry("noun", "B2", "/ˈkɒn.sɪ.kwəns/", "/ˈkɑːn.sə.kwəns/", "hệ quả, hậu quả", "far-reaching consequences"),
        "equilibrium" to MiniDictEntry("noun", "C2", "/ˌek.wɪˈlɪb.ri.əm/", "/ˌiː.kwəˈlɪb.ri.əm/", "trạng thái cân bằng", "work-life equilibrium")
    )

    private val lookupSchema = Schema.obj(
        mapOf(
            "part_of_speech" to Schema.string("noun, verb, adjective, adverb, or phrase"),
            "cefr_level" to Schema.string("B2, C1, or C2"),
            "ipa_uk" to Schema.string("UK IPA transcription e.g. /ˈmɪt.ɪ.ɡeɪt/"),
            "ipa_us" to Schema.string("US IPA transcription e.g. /ˈmɪt̬.ə.ɡeɪt/"),
            "meaning_vi" to Schema.string("Concise, accurate Vietnamese meaning in the given sentence context"),
            "collocation_en" to Schema.string("1 natural Band 8.5+ IELTS academic collocation using this word")
        )
    )

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                temperature = 0.15f
                maxOutputTokens = 320
                responseMimeType = "application/json"
                responseSchema = this@WordLookupService.lookupSchema
            }
        )
    }

    /**
     * Instant offline lookup from SQLite imported vocabulary, LearnCurriculum, and local academic dictionary.
     */
    fun lookupLocal(
        rawWord: String,
        contextSentenceEn: String,
        allVocabulary: List<VocabWordItem>
    ): WordLookupResult? {
        val cleaned = cleanEnglishWord(rawWord)
        if (cleaned.length < 2) return null
        val lower = cleaned.lowercase(Locale.US)
        val stems = candidateStems(lower)

        // 1. Search user/SQLite vocabulary + built-in curriculum
        val combinedPool = if (allVocabulary.isNotEmpty()) {
            allVocabulary
        } else {
            (1..12).flatMap { LearnCurriculum.wordsForModule(it) }
        }
        val matchedVocab = combinedPool.firstOrNull { item ->
            val w = item.word.lowercase(Locale.US)
            w == lower || stems.any { it == w }
        } ?: (1..12).asSequence()
            .flatMap { LearnCurriculum.wordsForModule(it).asSequence() }
            .firstOrNull { item ->
                val w = item.word.lowercase(Locale.US)
                w == lower || stems.any { it == w }
            }

        if (matchedVocab != null) {
            return WordLookupResult(
                word = matchedVocab.word,
                partOfSpeech = matchedVocab.partOfSpeech,
                cefrLevel = matchedVocab.cefrLevel,
                ipaUk = matchedVocab.ipaUk,
                ipaUs = matchedVocab.ipaUs,
                meaningVi = matchedVocab.meaningVi,
                collocationEn = matchedVocab.collocationStemEn.replace("______", matchedVocab.collocationPartnerEn),
                contextSentenceEn = contextSentenceEn.ifBlank { matchedVocab.exampleEn },
                sourceLabelVi = "Từ điển Master Ielts (Offline)",
                isFromAi = false
            )
        }

        // 2. Search built-in C1-C2 academic mini-dictionary
        val dictKey = stems.firstOrNull { localAcademicDict.containsKey(it) }
        val entry = dictKey?.let { localAcademicDict[it] }
        if (entry != null && dictKey != null) {
            return WordLookupResult(
                word = cleaned,
                partOfSpeech = entry.pos,
                cefrLevel = entry.cefr,
                ipaUk = entry.ipaUk,
                ipaUs = entry.ipaUs,
                meaningVi = entry.meaningVi,
                collocationEn = entry.collocation,
                contextSentenceEn = contextSentenceEn,
                sourceLabelVi = "Từ điển học thuật nội bộ (Offline)",
                isFromAi = false
            )
        }

        return null
    }

    /**
     * Quick contextual lookup powered by Gemini via Firebase AI Logic.
     */
    suspend fun lookupWithGemini(
        rawWord: String,
        contextSentenceEn: String
    ): WordLookupResult = withContext(Dispatchers.IO) {
        val cleaned = cleanEnglishWord(rawWord)
        val prompt = """
            You are an IELTS Band 8.5+ English-Vietnamese contextual dictionary.
            Explain the English word "$cleaned" in the exact context of this sentence:
            "$contextSentenceEn"
            Return strictly JSON with:
            - part_of_speech (noun, verb, adjective, adverb, or phrase)
            - cefr_level (B2, C1, or C2)
            - ipa_uk (UK IPA between slashes)
            - ipa_us (US IPA between slashes)
            - meaning_vi (clear, concise Vietnamese meaning matching this sentence)
            - collocation_en (1 natural IELTS Band 8.5 collocation using this word)
        """.trimIndent()

        val rawJson = GeminiResilientClient.generateJsonWithFallback(
            primaryCall = { model.generateContent(prompt).text },
            prompt = prompt,
            jsonSchemaHint = """
                {
                  "part_of_speech": "noun|verb|adjective|adverb|phrase",
                  "cefr_level": "B2|C1|C2",
                  "ipa_uk": "/.../",
                  "ipa_us": "/.../",
                  "meaning_vi": "string (Vietnamese)",
                  "collocation_en": "string"
                }
            """.trimIndent(),
            temperature = 0.15f,
            maxOutputTokens = 320,
            timeoutMs = TIMEOUT_MS,
            operationLabelVi = "tra từ nhanh theo ngữ cảnh"
        )

        val cleanJson = rawJson
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val obj = JSONObject(cleanJson)
        val ipaUk = obj.optString("ipa_uk", "/$cleaned/").ifBlank { "/$cleaned/" }
        val ipaUs = obj.optString("ipa_us", ipaUk).ifBlank { ipaUk }

        WordLookupResult(
            word = cleaned,
            partOfSpeech = obj.optString("part_of_speech", "word").ifBlank { "word" },
            cefrLevel = obj.optString("cefr_level", "C1").ifBlank { "C1" },
            ipaUk = ipaUk,
            ipaUs = ipaUs,
            meaningVi = obj.optString("meaning_vi", "Nghĩa ngữ cảnh của từ \"$cleaned\""),
            collocationEn = obj.optString("collocation_en", cleaned).ifBlank { cleaned },
            contextSentenceEn = contextSentenceEn,
            sourceLabelVi = "Gemini AI theo ngữ cảnh",
            isFromAi = true
        )
    }

    fun cleanEnglishWord(raw: String): String =
        raw.trim().replace(Regex("^[^A-Za-z]+|[^A-Za-z'-]+$"), "")

    private fun candidateStems(lower: String): List<String> = buildList {
        add(lower)
        if (lower.endsWith("ies") && lower.length > 4) add(lower.dropLast(3) + "y")
        if (lower.endsWith("es") && lower.length > 4) add(lower.dropLast(2))
        if (lower.endsWith("s") && lower.length > 3) add(lower.dropLast(1))
        if (lower.endsWith("ied") && lower.length > 4) add(lower.dropLast(3) + "y")
        if (lower.endsWith("ed") && lower.length > 4) {
            add(lower.dropLast(2))
            add(lower.dropLast(1))
        }
        if (lower.endsWith("ing") && lower.length > 5) {
            add(lower.dropLast(3))
            add(lower.dropLast(3) + "e")
        }
        if (lower.endsWith("ly") && lower.length > 4) add(lower.dropLast(2))
    }.distinct()
}
