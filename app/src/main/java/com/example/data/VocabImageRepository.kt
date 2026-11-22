package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

data class ResolvedVocabImage(
    val imageUrl: String,
    val sourceLabelVi: String
)

sealed interface VocabImageUiState {
    data object Loading : VocabImageUiState
    data class Loaded(val image: ResolvedVocabImage) : VocabImageUiState
    data class Fallback(val reasonVi: String) : VocabImageUiState
}

/**
 * Automatic zero-config Vocabulary Illustration Resolver & Local Cache:
 * 1. Checks in-memory + SharedPreferences persistent cache first (instant load, works offline once cached).
 * 2. If user optionally configured Google Custom Search API Key + CX in Settings, queries Google Image Search API first.
 * 3. Otherwise (default automatic mode), queries Wikipedia REST Summary API, Wikipedia Search PageImages API,
 *    and Wikimedia Commons Search API to retrieve a clean educational illustration URL.
 */
object VocabImageRepository {

    private const val PREFS_NAME = "master_ielts_vocab_image_cache_v1"
    private const val USER_AGENT = "MasterIeltsAndroid/1.0 (Educational IELTS Vocabulary App)"
    private const val CACHE_DELIMITER = "|||"

    private val memoryCache = ConcurrentHashMap<String, ResolvedVocabImage>()

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(7, TimeUnit.SECONDS)
            .readTimeout(9, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun cacheKey(wordEn: String, useCustomSearch: Boolean): String {
        val norm = wordEn.trim().lowercase(Locale.US)
        return if (useCustomSearch) "gcse_$norm" else "wiki_$norm"
    }

    fun clearCachedUrls(context: Context) {
        memoryCache.clear()
        prefs(context).edit().clear().apply()
    }

    suspend fun resolveWordImage(
        context: Context,
        wordEn: String,
        topicHintEn: String = "",
        googleCseApiKey: String = "",
        googleCseCx: String = "",
        forceRefresh: Boolean = false
    ): VocabImageUiState = withContext(Dispatchers.IO) {
        val cleanedWord = wordEn
            .substringBefore("(")
            .substringBefore("/")
            .trim()
        if (cleanedWord.isBlank()) {
            return@withContext VocabImageUiState.Fallback("Minh họa theo chủ đề học thuật")
        }

        val hasGoogleCse = googleCseApiKey.isNotBlank() && googleCseCx.isNotBlank()
        val key = cacheKey(cleanedWord, hasGoogleCse)

        // 1. Check local cache first (instantaneous on repeat visits & offline)
        if (!forceRefresh) {
            memoryCache[key]?.let { return@withContext VocabImageUiState.Loaded(it) }
            readPersisted(context, key)?.let { cached ->
                memoryCache[key] = cached
                return@withContext VocabImageUiState.Loaded(cached)
            }
        }

        // 2. Optional Google Custom Search Image API (if user configured API Key + CX in Settings)
        if (hasGoogleCse) {
            val cseResult = runCatching {
                queryGoogleCustomSearchImage(
                    query = "$cleanedWord ${topicHintEn.take(28)} illustration".trim(),
                    apiKey = googleCseApiKey.trim(),
                    cx = googleCseCx.trim()
                )
            }.getOrNull()
            if (cseResult != null) {
                persist(context, key, cseResult)
                return@withContext VocabImageUiState.Loaded(cseResult)
            }
        }

        // 3. Default Zero-Config Mode: Wikipedia REST Summary -> Wikipedia PageImages -> Wikimedia Commons
        val wikiSummary = runCatching {
            queryWikipediaRestSummary(cleanedWord)
        }.getOrNull()
        if (wikiSummary != null) {
            persist(context, key, wikiSummary)
            return@withContext VocabImageUiState.Loaded(wikiSummary)
        }

        val wikiSearchWithTopic = runCatching {
            val q = if (topicHintEn.isNotBlank()) "$cleanedWord ${topicHintEn.substringBefore("&").trim()}" else cleanedWord
            queryWikipediaSearchThumbnail(q)
        }.getOrNull()
        if (wikiSearchWithTopic != null) {
            persist(context, key, wikiSearchWithTopic)
            return@withContext VocabImageUiState.Loaded(wikiSearchWithTopic)
        }

        val wikiSearchPlain = runCatching {
            queryWikipediaSearchThumbnail(cleanedWord)
        }.getOrNull()
        if (wikiSearchPlain != null) {
            persist(context, key, wikiSearchPlain)
            return@withContext VocabImageUiState.Loaded(wikiSearchPlain)
        }

        val commonsImage = runCatching {
            queryWikimediaCommonsThumbnail(cleanedWord)
        }.getOrNull()
        if (commonsImage != null) {
            persist(context, key, commonsImage)
            return@withContext VocabImageUiState.Loaded(commonsImage)
        }

        VocabImageUiState.Fallback("Chế độ ngoại tuyến • Minh họa ngữ cảnh")
    }

    private fun readPersisted(context: Context, key: String): ResolvedVocabImage? {
        val raw = prefs(context).getString(key, null) ?: return null
        val parts = raw.split(CACHE_DELIMITER)
        val url = parts.firstOrNull()?.trim().orEmpty()
        if (!url.startsWith("https://")) return null
        val source = parts.getOrNull(1)?.trim().orEmpty().ifBlank { "Wikipedia" }
        return ResolvedVocabImage(imageUrl = url, sourceLabelVi = source)
    }

    private fun persist(context: Context, key: String, value: ResolvedVocabImage) {
        memoryCache[key] = value
        prefs(context).edit()
            .putString(key, "${value.imageUrl}$CACHE_DELIMITER${value.sourceLabelVi}")
            .apply()
    }

    private fun executeGetJson(url: String): JSONObject? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .get()
            .build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val body = response.body?.string().orEmpty()
            if (body.isBlank()) return null
            return runCatching { JSONObject(body) }.getOrNull()
        }
    }

    private fun queryGoogleCustomSearchImage(
        query: String,
        apiKey: String,
        cx: String
    ): ResolvedVocabImage? {
        val url = "https://www.googleapis.com/customsearch/v1" +
            "?key=${Uri.encode(apiKey)}" +
            "&cx=${Uri.encode(cx)}" +
            "&searchType=image" +
            "&num=3" +
            "&safe=active" +
            "&q=${Uri.encode(query)}"
        val json = executeGetJson(url) ?: return null
        val items = json.optJSONArray("items") ?: return null
        for (i in 0 until items.length()) {
            val item = items.optJSONObject(i) ?: continue
            val link = item.optString("link").trim()
            if (isUsableRasterImageUrl(link)) {
                return ResolvedVocabImage(imageUrl = link, sourceLabelVi = "Google Images")
            }
        }
        return null
    }

    private fun queryWikipediaRestSummary(term: String): ResolvedVocabImage? {
        val titleSlug = term.trim().replace(" ", "_")
        val url = "https://en.wikipedia.org/api/rest_v1/page/summary/${Uri.encode(titleSlug)}"
        val json = executeGetJson(url) ?: return null
        val type = json.optString("type")
        if (type.equals("disambiguation", ignoreCase = true)) return null
        val thumb = json.optJSONObject("thumbnail")?.optString("source").orEmpty().trim()
        val orig = json.optJSONObject("originalimage")?.optString("source").orEmpty().trim()
        val chosen = when {
            isUsableRasterImageUrl(thumb) -> thumb
            isUsableRasterImageUrl(orig) -> orig
            else -> ""
        }
        return if (chosen.isNotBlank()) {
            ResolvedVocabImage(imageUrl = chosen, sourceLabelVi = "Wikipedia")
        } else {
            null
        }
    }

    private fun queryWikipediaSearchThumbnail(query: String): ResolvedVocabImage? {
        val url = "https://en.wikipedia.org/w/api.php" +
            "?action=query" +
            "&generator=search" +
            "&gsrsearch=${Uri.encode(query)}" +
            "&gsrlimit=5" +
            "&prop=pageimages" +
            "&piprop=thumbnail" +
            "&pithumbsize=640" +
            "&format=json" +
            "&origin=*"
        val json = executeGetJson(url) ?: return null
        val pages = json.optJSONObject("query")?.optJSONObject("pages") ?: return null
        val keys = pages.keys()
        while (keys.hasNext()) {
            val pageObj = pages.optJSONObject(keys.next()) ?: continue
            val source = pageObj.optJSONObject("thumbnail")?.optString("source").orEmpty().trim()
            if (isUsableRasterImageUrl(source)) {
                return ResolvedVocabImage(imageUrl = source, sourceLabelVi = "Wikipedia")
            }
        }
        return null
    }

    private fun queryWikimediaCommonsThumbnail(query: String): ResolvedVocabImage? {
        val url = "https://commons.wikimedia.org/w/api.php" +
            "?action=query" +
            "&generator=search" +
            "&gsrnamespace=6" +
            "&gsrsearch=${Uri.encode(query)}" +
            "&gsrlimit=6" +
            "&prop=imageinfo" +
            "&iiprop=url|mime" +
            "&iiurlwidth=640" +
            "&format=json" +
            "&origin=*"
        val json = executeGetJson(url) ?: return null
        val pages = json.optJSONObject("query")?.optJSONObject("pages") ?: return null
        val keys = pages.keys()
        while (keys.hasNext()) {
            val pageObj = pages.optJSONObject(keys.next()) ?: continue
            val infoArr = pageObj.optJSONArray("imageinfo") ?: continue
            val info = infoArr.optJSONObject(0) ?: continue
            val mime = info.optString("mime").lowercase(Locale.US)
            if (mime != "image/jpeg" && mime != "image/png" && mime != "image/webp") continue
            val thumbUrl = info.optString("thumburl").trim()
            val fullUrl = info.optString("url").trim()
            val chosen = if (isUsableRasterImageUrl(thumbUrl)) thumbUrl else fullUrl
            if (isUsableRasterImageUrl(chosen)) {
                return ResolvedVocabImage(imageUrl = chosen, sourceLabelVi = "Wikimedia")
            }
        }
        return null
    }

    private fun isUsableRasterImageUrl(url: String): Boolean {
        if (!url.startsWith("https://")) return false
        val lower = url.lowercase(Locale.US)
        if (lower.endsWith(".svg") || lower.endsWith(".tif") || lower.endsWith(".tiff") || lower.endsWith(".pdf")) {
            return false
        }
        // Avoid generic tiny UI icons / wiki logos / maps when better photos exist
        if (lower.contains("wiki_letter") || lower.contains("question_book") || lower.contains("commons-logo")) {
            return false
        }
        return true
    }
}
