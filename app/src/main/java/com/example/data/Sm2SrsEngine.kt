package com.example.data

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

/**
 * 4 recall grades for true SM-2 Spaced Repetition.
 */
enum class Sm2RecallQuality(
    val id: String,
    val labelVi: String,
    val qualityScore: Int
) {
    AGAIN("again", "Quên", 1),
    HARD("hard", "Khó", 3),
    GOOD("good", "Nhớ", 4),
    EASY("easy", "Dễ", 5);

    companion object {
        fun fromId(raw: String?): Sm2RecallQuality? =
            entries.firstOrNull { it.id.equals(raw, ignoreCase = true) }
    }
}

/**
 * Persisted SM-2 scheduling record for a single vocabulary word.
 */
data class Sm2CardState(
    val wordId: String,
    val repetitions: Int = 0,
    val intervalDays: Int = 0,
    val easeFactor: Float = 2.5f,
    val nextReviewDateIso: String = "",
    val lastReviewedDateIso: String = "",
    val lastRating: Sm2RecallQuality? = null
) {
    fun isDueOn(todayIso: String = StreakManager.todayIsoDate()): Boolean {
        if (nextReviewDateIso.isBlank()) return true
        if (lastRating == Sm2RecallQuality.AGAIN && lastReviewedDateIso == todayIso) return true
        return nextReviewDateIso <= todayIso
    }

    fun isScheduledFuture(todayIso: String = StreakManager.todayIsoDate()): Boolean =
        !isDueOn(todayIso)
}

/**
 * True SM-2 Spaced Repetition Engine with IELTS-optimized early milestones
 * (1d -> 3d -> 7d -> 21d -> interval * easeFactor).
 */
object Sm2SrsEngine {

    private const val MIN_EASE_FACTOR = 1.30f
    private const val MAX_EASE_FACTOR = 3.20f
    private const val DEFAULT_EASE_FACTOR = 2.50f

    fun review(
        current: Sm2CardState?,
        wordId: String,
        quality: Sm2RecallQuality,
        todayIso: String = StreakManager.todayIsoDate()
    ): Sm2CardState {
        val base = current ?: Sm2CardState(wordId = wordId, nextReviewDateIso = todayIso)
        val oldEf = base.easeFactor.coerceIn(MIN_EASE_FACTOR, MAX_EASE_FACTOR)
        val oldReps = base.repetitions.coerceAtLeast(0)
        val oldInterval = base.intervalDays.coerceAtLeast(0)

        // Standard SM-2 ease factor delta: EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        val q = quality.qualityScore
        val diff = 5 - q
        val rawEf = when (quality) {
            Sm2RecallQuality.AGAIN -> oldEf - 0.20f
            Sm2RecallQuality.HARD -> oldEf - 0.14f
            Sm2RecallQuality.GOOD -> oldEf + (0.10f - diff * (0.08f + diff * 0.02f))
            Sm2RecallQuality.EASY -> oldEf + 0.15f
        }
        val newEf = rawEf.coerceIn(MIN_EASE_FACTOR, MAX_EASE_FACTOR)

        val newReps: Int
        val newInterval: Int
        when (quality) {
            Sm2RecallQuality.AGAIN -> {
                newReps = 0
                newInterval = 1
            }

            Sm2RecallQuality.HARD -> {
                newReps = (oldReps + 1).coerceAtLeast(1)
                newInterval = when (oldReps) {
                    0 -> 1
                    1 -> 3
                    2 -> 5
                    3 -> 14
                    else -> (oldInterval * 1.25f).roundToInt().coerceAtLeast(oldInterval + 1)
                }
            }

            Sm2RecallQuality.GOOD -> {
                newReps = oldReps + 1
                newInterval = when (oldReps) {
                    0 -> 1
                    1 -> 3
                    2 -> 7
                    3 -> 21
                    else -> (oldInterval * newEf).roundToInt().coerceAtLeast(oldInterval + 1)
                }
            }

            Sm2RecallQuality.EASY -> {
                newReps = oldReps + 1
                newInterval = when (oldReps) {
                    0 -> 3
                    1 -> 7
                    2 -> 21
                    else -> (oldInterval * newEf * 1.30f).roundToInt().coerceAtLeast(oldInterval + 2)
                }
            }
        }

        val nextDateIso = addDaysToIso(todayIso, newInterval)

        return Sm2CardState(
            wordId = wordId,
            repetitions = newReps,
            intervalDays = newInterval,
            easeFactor = ((newEf * 100f).roundToInt() / 100f),
            nextReviewDateIso = nextDateIso,
            lastReviewedDateIso = todayIso,
            lastRating = quality
        )
    }

    fun previewIntervalDays(current: Sm2CardState?, quality: Sm2RecallQuality): Int {
        val simulated = review(current = current, wordId = current?.wordId.orEmpty(), quality = quality)
        return simulated.intervalDays
    }

    fun previewIntervalLabel(current: Sm2CardState?, quality: Sm2RecallQuality): String {
        val days = previewIntervalDays(current, quality)
        return "${days}d"
    }

    fun formatIntervalVi(days: Int): String = when {
        days <= 0 -> "Hôm nay"
        days == 1 -> "1 ngày"
        else -> "$days ngày"
    }

    fun addDaysToIso(baseIso: String, daysToAdd: Int): String {
        return runCatching {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cal = Calendar.getInstance()
            val parsed = sdf.parse(baseIso)
            if (parsed != null) {
                cal.time = parsed
            }
            cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
            sdf.format(cal.time)
        }.getOrDefault(baseIso)
    }

    fun serializeMap(map: Map<String, Sm2CardState>): String {
        val root = JSONObject()
        map.forEach { (wordId, state) ->
            val obj = JSONObject()
            obj.put("reps", state.repetitions)
            obj.put("interval", state.intervalDays)
            obj.put("ef", state.easeFactor.toDouble())
            obj.put("nextDate", state.nextReviewDateIso)
            obj.put("lastDate", state.lastReviewedDateIso)
            obj.put("lastRating", state.lastRating?.id.orEmpty())
            root.put(wordId, obj)
        }
        return root.toString()
    }

    fun deserializeMap(rawJson: String?): Map<String, Sm2CardState> {
        if (rawJson.isNullOrBlank()) return emptyMap()
        return runCatching {
            val root = JSONObject(rawJson)
            val result = LinkedHashMap<String, Sm2CardState>()
            val keys = root.keys()
            while (keys.hasNext()) {
                val wordId = keys.next()
                val obj = root.optJSONObject(wordId) ?: continue
                result[wordId] = Sm2CardState(
                    wordId = wordId,
                    repetitions = obj.optInt("reps", 0).coerceAtLeast(0),
                    intervalDays = obj.optInt("interval", 0).coerceAtLeast(0),
                    easeFactor = obj.optDouble("ef", DEFAULT_EASE_FACTOR.toDouble())
                        .toFloat()
                        .coerceIn(MIN_EASE_FACTOR, MAX_EASE_FACTOR),
                    nextReviewDateIso = obj.optString("nextDate", ""),
                    lastReviewedDateIso = obj.optString("lastDate", ""),
                    lastRating = Sm2RecallQuality.fromId(obj.optString("lastRating", ""))
                )
            }
            result
        }.getOrDefault(emptyMap())
    }
}
