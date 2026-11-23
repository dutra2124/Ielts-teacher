package com.example.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.AutoKpiInteractionBus
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

enum class EnglishAccent(val label: String, val locale: Locale) {
    UK("UK", Locale.UK),
    US("US", Locale.US)
}

data class PronunciationResult(
    val score: Int,
    val recognizedText: String,
    val feedbackVi: String,
    val consonantTipVi: String,
    val isStrongPass: Boolean
)

/**
 * Wraps Android TextToSpeech with UK & US accent switching and speaking callbacks.
 */
class ApexTtsController(
    context: Context,
    private val onSpeakingChanged: (EnglishAccent?) -> Unit
) {
    private var tts: TextToSpeech? = null
    private var isReady = false
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var sequenceLines: List<String> = emptyList()
    private var sequenceIndex: Int = -1
    private var sequenceAccent: EnglishAccent = EnglishAccent.UK
    private var sequenceRate: Float = 1.0f
    private var sequenceOnLine: ((Int) -> Unit)? = null
    private var sequenceOnComplete: (() -> Unit)? = null
    private var isPlayingSequence: Boolean = false
    private var sequenceLoopStart: Int? = null
    private var sequenceLoopEnd: Int? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            isReady = (status == TextToSpeech.SUCCESS)
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    AutoKpiInteractionBus.notifyInteraction()
                }

                override fun onDone(utteranceId: String?) {
                    AutoKpiInteractionBus.notifyInteraction()
                    mainHandler.post {
                        if (isPlayingSequence && utteranceId?.startsWith("seq_") == true) {
                            val loopA = sequenceLoopStart
                            val loopB = sequenceLoopEnd
                            val hasAbLoop = loopA != null && loopB != null &&
                                loopA in sequenceLines.indices &&
                                loopB in sequenceLines.indices &&
                                loopA <= loopB
                            val nextIdx = sequenceIndex + 1
                            if (hasAbLoop && loopA != null && loopB != null) {
                                if (nextIdx <= loopB) {
                                    playSequenceIndex(nextIdx)
                                } else {
                                    playSequenceIndex(loopA)
                                }
                            } else if (nextIdx < sequenceLines.size) {
                                playSequenceIndex(nextIdx)
                            } else {
                                isPlayingSequence = false
                                sequenceIndex = -1
                                onSpeakingChanged(null)
                                sequenceOnComplete?.invoke()
                            }
                        } else if (!isPlayingSequence) {
                            onSpeakingChanged(null)
                        }
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    mainHandler.post {
                        isPlayingSequence = false
                        sequenceIndex = -1
                        onSpeakingChanged(null)
                        sequenceOnComplete?.invoke()
                    }
                }
            })
        }
    }

    fun speak(text: String, accent: EnglishAccent, speechRate: Float = 0.90f) {
        AutoKpiInteractionBus.notifyInteraction()
        isPlayingSequence = false
        sequenceIndex = -1
        val engine = tts
        if (!isReady || engine == null) {
            onSpeakingChanged(null)
            return
        }
        engine.language = accent.locale
        engine.setSpeechRate(speechRate.coerceIn(0.65f, 1.5f))
        onSpeakingChanged(accent)
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "apex_${System.currentTimeMillis()}")
    }

    fun speakSequence(
        lines: List<String>,
        startIndex: Int = 0,
        accent: EnglishAccent = EnglishAccent.UK,
        speechRate: Float = 1.0f,
        loopStartIndex: Int? = null,
        loopEndIndex: Int? = null,
        onLineStarted: (Int) -> Unit,
        onComplete: () -> Unit
    ) {
        val engine = tts
        if (!isReady || engine == null || lines.isEmpty()) {
            onSpeakingChanged(null)
            onComplete()
            return
        }
        sequenceLines = lines
        sequenceAccent = accent
        sequenceRate = speechRate.coerceIn(0.65f, 1.5f)
        sequenceOnLine = onLineStarted
        sequenceOnComplete = onComplete
        sequenceLoopStart = loopStartIndex?.coerceIn(0, lines.lastIndex)
        sequenceLoopEnd = loopEndIndex?.coerceIn(0, lines.lastIndex)
        isPlayingSequence = true
        playSequenceIndex(startIndex.coerceIn(0, lines.lastIndex))
    }

    fun updateSequenceLoop(loopStartIndex: Int?, loopEndIndex: Int?) {
        if (sequenceLines.isEmpty()) {
            sequenceLoopStart = null
            sequenceLoopEnd = null
            return
        }
        sequenceLoopStart = loopStartIndex?.coerceIn(0, sequenceLines.lastIndex)
        sequenceLoopEnd = loopEndIndex?.coerceIn(0, sequenceLines.lastIndex)
    }

    private fun playSequenceIndex(index: Int) {
        val engine = tts ?: return
        if (!isPlayingSequence || index !in sequenceLines.indices) return
        AutoKpiInteractionBus.notifyInteraction()
        sequenceIndex = index
        engine.language = sequenceAccent.locale
        engine.setSpeechRate(sequenceRate)
        onSpeakingChanged(sequenceAccent)
        sequenceOnLine?.invoke(index)
        engine.speak(
            sequenceLines[index],
            TextToSpeech.QUEUE_FLUSH,
            null,
            "seq_${index}_${System.currentTimeMillis()}"
        )
    }

    fun stop() {
        isPlayingSequence = false
        sequenceIndex = -1
        sequenceLoopStart = null
        sequenceLoopEnd = null
        tts?.stop()
        onSpeakingChanged(null)
    }

    fun shutdown() {
        isPlayingSequence = false
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

enum class CoachVoiceLanguage(
    val id: String,
    val shortLabel: String,
    val fullLabelVi: String,
    val locale: Locale
) {
    VIETNAMESE("vi", "Tiếng Việt", "Giọng Việt (vi-VN)", Locale.forLanguageTag("vi-VN")),
    ENGLISH_UK("en_gb", "UK", "English UK (en-GB)", Locale.UK),
    ENGLISH_US("en_us", "US", "English US (en-US)", Locale.US)
}

/**
 * Dedicated Voice Encouragement TTS Controller for the Diligence Dashboard.
 * Prioritizes a natural Vietnamese voice (vi-VN) with adjustable speed (0.9x / 1.0x / 1.1x),
 * optional English UK/US coaching, sentence-level Play / Pause / Resume, and synchronized
 * speaking callbacks for the Animated AI Teacher's lip-sync and waveform bars.
 */
class CoachVoiceTtsController(
    context: Context,
    private val onSpeakingChanged: (Boolean) -> Unit,
    private val onReadyChanged: (Boolean) -> Unit = {}
) {
    private var tts: TextToSpeech? = null
    private var isReady: Boolean = false
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

    private var sentences: List<String> = emptyList()
    private var currentIndex: Int = 0
    private var activeLanguage: CoachVoiceLanguage = CoachVoiceLanguage.VIETNAMESE
    private var activeRate: Float = 1.0f
    private var isPlaying: Boolean = false
    private var isPaused: Boolean = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            isReady = (status == TextToSpeech.SUCCESS)
            mainHandler.post { onReadyChanged(isReady) }
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    mainHandler.post {
                        if (isPlaying) onSpeakingChanged(true)
                    }
                }

                override fun onDone(utteranceId: String?) {
                    mainHandler.post {
                        if (!isPlaying || isPaused) return@post
                        val nextIdx = currentIndex + 1
                        if (nextIdx < sentences.size) {
                            playSentenceAt(nextIdx)
                        } else {
                            isPlaying = false
                            isPaused = false
                            currentIndex = 0
                            onSpeakingChanged(false)
                        }
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    mainHandler.post {
                        isPlaying = false
                        isPaused = false
                        onSpeakingChanged(false)
                    }
                }
            })
        }
    }

    fun speakQuote(
        text: String,
        language: CoachVoiceLanguage,
        speechRate: Float
    ): Boolean {
        val engine = tts
        if (!isReady || engine == null || text.isBlank()) {
            onSpeakingChanged(false)
            return false
        }
        val chunks = text.trim()
            .split(Regex("(?<=[.!?])\\s+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { listOf(text.trim()) }

        sentences = chunks
        currentIndex = 0
        activeLanguage = language
        activeRate = speechRate.coerceIn(0.8f, 1.3f)
        isPlaying = true
        isPaused = false
        applyLanguageAndVoice(engine, language)
        playSentenceAt(0)
        return true
    }

    fun pause() {
        if (!isPlaying) return
        isPaused = true
        isPlaying = false
        tts?.stop()
        onSpeakingChanged(false)
    }

    fun resume(): Boolean {
        val engine = tts
        if (!isPaused || !isReady || engine == null || sentences.isEmpty()) return false
        isPaused = false
        isPlaying = true
        applyLanguageAndVoice(engine, activeLanguage)
        playSentenceAt(currentIndex.coerceIn(0, sentences.lastIndex))
        return true
    }

    fun hasPausedQuote(): Boolean = isPaused && sentences.isNotEmpty()

    fun stop() {
        isPlaying = false
        isPaused = false
        currentIndex = 0
        tts?.stop()
        onSpeakingChanged(false)
    }

    fun shutdown() {
        isPlaying = false
        isPaused = false
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    private fun applyLanguageAndVoice(engine: TextToSpeech, language: CoachVoiceLanguage) {
        val targetLocale = language.locale
        val result = engine.setLanguage(targetLocale)
        // Prefer a natural female / high-quality voice matching the target language if available
        runCatching {
            val matchingVoices = engine.voices.orEmpty().filter { v ->
                v.locale?.language.equals(targetLocale.language, ignoreCase = true) &&
                    !v.isNetworkConnectionRequired
            }
            val preferred = matchingVoices.firstOrNull { v ->
                v.name.contains("female", ignoreCase = true) ||
                    v.name.contains("vif", ignoreCase = true) ||
                    v.name.contains("gba", ignoreCase = true)
            } ?: matchingVoices.firstOrNull()
            if (preferred != null) {
                engine.voice = preferred
            } else if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                engine.language = Locale.US
            }
        }
        engine.setPitch(1.03f)
        engine.setSpeechRate(activeRate)
    }

    private fun playSentenceAt(index: Int) {
        val engine = tts ?: return
        if (!isPlaying || index !in sentences.indices) return
        currentIndex = index
        engine.setSpeechRate(activeRate)
        onSpeakingChanged(true)
        engine.speak(
            sentences[index],
            TextToSpeech.QUEUE_FLUSH,
            null,
            "coach_${index}_${System.currentTimeMillis()}"
        )
    }
}

/**
 * Evaluates pronunciation accuracy from Android SpeechRecognizer output against a target C1-C2 word,
 * providing actionable Vietnamese feedback on final consonants, stress, and connected speech.
 */
object ApexPronunciationEvaluator {

    fun createRecognizerIntent(locale: Locale = Locale.UK): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
    }

    fun startFreeSpeechRecognition(
        context: Context,
        locale: Locale = Locale.UK,
        onReady: () -> Unit,
        onPartial: (String) -> Unit,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ): SpeechRecognizer? {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Thiết bị hiện chưa bật dịch vụ nhận diện giọng nói Google. Bạn có thể gõ hoặc dán transcript trực tiếp vào ô bên dưới để Gemini AI chấm điểm ngay!")
            return null
        }

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = onReady()
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                if (partial.isNotBlank()) onPartial(partial)
            }

            override fun onError(error: Int) {
                val message = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "Chưa nhận diện được câu nói rõ ràng. Hãy nói gần micro hơn hoặc nhập transcript để AI chấm."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Hết thời gian chờ giọng nói. Nhấn Thu âm lại và trả lời ngay nhé."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Vui lòng cấp quyền Micro để thu âm IELTS Speaking."
                    else -> "Dịch vụ thu âm tạm gián đoạn (mã $error). Bạn có thể nhập transcript trực tiếp."
                }
                onError(message)
            }

            override fun onResults(results: Bundle?) {
                val best = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                if (best.isNotBlank()) {
                    onResult(best)
                } else {
                    onError("Không ghi nhận được nội dung nói. Hãy thử lại nhé.")
                }
            }
        })

        recognizer.startListening(createRecognizerIntent(locale))
        return recognizer
    }

    fun startListening(
        context: Context,
        targetWord: String,
        pronunciationFocusVi: String,
        onReady: () -> Unit,
        onResult: (PronunciationResult) -> Unit,
        onError: (String) -> Unit
    ): SpeechRecognizer? {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Thiết bị/trình giả lập hiện chưa bật dịch vụ nhận diện giọng nói Google. Hãy thử nút nghe UK/US để luyện phát âm mẫu nhé!")
            return null
        }

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = onReady()
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onPartialResults(partialResults: Bundle?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit

            override fun onError(error: Int) {
                val message = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "Cô chưa nghe rõ từ \"$targetWord\". Hãy nói chậm, nhấn trọng âm và bật rõ phụ âm cuối nhé!"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Hết thời gian thu âm. Chạm micro và đọc to từ \"$targetWord\" ngay nhé."
                    SpeechRecognizer.ERROR_AUDIO -> "Không thể thu âm từ micro. Kiểm tra micro thiết bị và thử lại."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Vui lòng cấp quyền Micro để dùng tính năng Kiểm tra phát âm."
                    else -> "Dịch vụ nhận diện giọng nói tạm gián đoạn (mã $error). Bạn có thể thử lại ngay."
                }
                onError(message)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
                onResult(evaluate(targetWord, matches, pronunciationFocusVi))
            }
        })

        recognizer.startListening(createRecognizerIntent())
        return recognizer
    }

    fun evaluate(
        targetWord: String,
        candidates: List<String>,
        pronunciationFocusVi: String
    ): PronunciationResult {
        val cleanTarget = targetWord.lowercase(Locale.US).replace(Regex("[^a-z]"), "")
        if (candidates.isEmpty()) {
            return PronunciationResult(
                score = 0,
                recognizedText = "",
                feedbackVi = "Chưa ghi nhận được giọng đọc. Hãy nhấn lại và đọc rõ từng âm tiết.",
                consonantTipVi = pronunciationFocusVi,
                isStrongPass = false
            )
        }

        // Pick closest candidate to targetWord
        var bestScore = 0
        var bestCandidate = candidates.first()

        candidates.forEach { raw ->
            val tokens = raw.lowercase(Locale.US).split(Regex("\\s+")).filter { it.isNotBlank() }
            val fullClean = raw.lowercase(Locale.US).replace(Regex("[^a-z]"), "")
            val candidateStrings = listOf(fullClean) + tokens.map { it.replace(Regex("[^a-z]"), "") }

            candidateStrings.forEach { cand ->
                if (cand.isNotEmpty()) {
                    val sim = normalizedSimilarity(cleanTarget, cand)
                    if (sim > bestScore) {
                        bestScore = sim
                        bestCandidate = raw
                    }
                }
            }
        }

        val cleanBest = bestCandidate.lowercase(Locale.US).replace(Regex("[^a-z]"), "")
        val targetEndsWithStop = cleanTarget.lastOrNull() in setOf('t', 'd', 'k', 's', 'z', 'v', 'm', 'l')
        val matchedEnding = cleanBest.isNotEmpty() && cleanTarget.isNotEmpty() &&
            cleanBest.last() == cleanTarget.last()

        val consonantNote = when {
            bestScore >= 90 -> "Phụ âm cuối và nối âm rất sắc nét! $pronunciationFocusVi"
            targetEndsWithStop && !matchedEnding ->
                "Lưu ý phụ âm cuối \"-${cleanTarget.last()}\" bị nhẹ hoặc nuốt âm. $pronunciationFocusVi"
            else -> pronunciationFocusVi
        }

        val feedbackVi = when {
            bestScore >= 90 -> "Xuất sắc chuẩn Band 8.5! Bạn phát âm rõ trọng âm và ngữ điệu tự nhiên."
            bestScore >= 72 -> "Khá tốt (Band 7.0–7.5)! Chỉ cần giữ hơi chắc hơn ở âm tiết nhấn và phụ âm cuối."
            else -> "Cô nghe gần giống \"$bestCandidate\". Hãy nghe lại giọng mẫu UK/US và đọc chậm rãi từng âm tiết nhé!"
        }

        return PronunciationResult(
            score = bestScore,
            recognizedText = bestCandidate,
            feedbackVi = feedbackVi,
            consonantTipVi = consonantNote,
            isStrongPass = bestScore >= 85
        )
    }

    private fun normalizedSimilarity(a: String, b: String): Int {
        if (a == b) return 98
        val maxLen = max(a.length, b.length)
        if (maxLen == 0) return 0
        val dist = levenshtein(a, b)
        val raw = ((1f - dist.toFloat() / maxLen.toFloat()) * 100f).toInt()
        return raw.coerceIn(15, 96)
    }

    private fun levenshtein(a: String, b: String): Int {
        val dp = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            var prev = dp[0]
            dp[0] = i
            for (j in 1..b.length) {
                val temp = dp[j]
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[j] = min(min(dp[j] + 1, dp[j - 1] + 1), prev + cost)
                prev = temp
            }
        }
        return dp[b.length]
    }
}
