package com.example.data.importer

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Xml
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.ZipInputStream

enum class DocumentFileKind(val badgeLabel: String) {
    PDF("PDF"),
    DOCX("DOCX"),
    CSV("CSV"),
    JSON("JSON"),
    TXT("TXT"),
    AUDIO("AUDIO"),
    LINK("LINK"),
    TEXT("Văn bản")
}

data class ExtractedDocumentResult(
    val fileName: String,
    val fileKind: DocumentFileKind,
    val extractedText: String,
    val pageCount: Int = 0,
    val wordCount: Int = 0,
    val charCount: Int = 0,
    val uriString: String? = null
)

/**
 * Compile-safe, zero-permission native document text extractor using Android SAF ContentResolver:
 * - PDF: com.tom-roush:pdfbox-android (with thread-safe PDFBoxResourceLoader.init(context))
 * - DOCX: Pure Kotlin ZipInputStream + Android XmlPullParser reading word/document.xml
 * - CSV / JSON / TXT: UTF-8 BufferedReader with BOM stripping
 */
object DocumentTextExtractor {

    private val pdfBoxInitialized = AtomicBoolean(false)

    val SUPPORTED_DOCUMENT_MIME_TYPES: Array<String> = arrayOf(
        "application/pdf",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "text/csv",
        "text/comma-separated-values",
        "application/json",
        "text/plain",
        "audio/*"
    )

    fun ensurePdfBoxInitialized(context: Context) {
        if (pdfBoxInitialized.compareAndSet(false, true)) {
            runCatching {
                PDFBoxResourceLoader.init(context.applicationContext)
            }.onFailure {
                pdfBoxInitialized.set(false)
            }
        }
    }

    suspend fun extractFromUri(
        context: Context,
        uri: Uri
    ): Result<ExtractedDocumentResult> = withContext(Dispatchers.IO) {
        runCatching {
            val fileName = resolveDisplayName(context, uri)
            val mimeType = context.contentResolver.getType(uri).orEmpty().lowercase(Locale.US)
            val kind = classifyFileKind(fileName, mimeType)

            require(kind != DocumentFileKind.AUDIO) {
                "Tệp bạn chọn là tệp âm thanh ($fileName). Hãy dùng nút \"Đính kèm Audio MP3/WAV\" cho mục Đề thi Listening hoặc Tài liệu Tự học."
            }

            when (kind) {
                DocumentFileKind.PDF -> extractPdf(context, uri, fileName)
                DocumentFileKind.DOCX -> extractDocx(context, uri, fileName)
                else -> extractPlainUtf8(context, uri, fileName, kind)
            }
        }
    }

    fun resolveDisplayName(context: Context, uri: Uri): String {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) {
                    val name = cursor.getString(idx)
                    if (!name.isNullOrBlank()) return name
                }
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/')?.ifBlank { "tai_lieu_ielts" } ?: "tai_lieu_ielts"
    }

    fun classifyFileKind(fileName: String, mimeType: String): DocumentFileKind {
        val lowerName = fileName.lowercase(Locale.US)
        return when {
            mimeType.startsWith("audio/") || lowerName.endsWith(".mp3") || lowerName.endsWith(".wav") || lowerName.endsWith(".m4a") ->
                DocumentFileKind.AUDIO
            mimeType == "application/pdf" || lowerName.endsWith(".pdf") ->
                DocumentFileKind.PDF
            mimeType.contains("wordprocessingml") || lowerName.endsWith(".docx") ->
                DocumentFileKind.DOCX
            mimeType.contains("json") || lowerName.endsWith(".json") ->
                DocumentFileKind.JSON
            mimeType.contains("csv") || lowerName.endsWith(".csv") ->
                DocumentFileKind.CSV
            else -> DocumentFileKind.TXT
        }
    }

    private fun extractPdf(
        context: Context,
        uri: Uri,
        fileName: String
    ): ExtractedDocumentResult {
        ensurePdfBoxInitialized(context)
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Không thể mở tệp PDF từ thiết bị.")

        var pageCount = 0
        val rawText = inputStream.use { stream ->
            PDDocument.load(stream).use { document ->
                pageCount = document.numberOfPages
                val stripper = PDFTextStripper().apply {
                    sortByPosition = true
                }
                stripper.getText(document).orEmpty()
            }
        }

        val cleaned = normalizeExtractedText(rawText)
        require(cleaned.isNotBlank()) {
            "Tệp PDF \"$fileName\" không chứa lớp văn bản (có thể là ảnh scan). Vui lòng dùng tệp PDF có chữ hoặc dán nội dung trực tiếp."
        }
        return buildResult(
            fileName = fileName,
            kind = DocumentFileKind.PDF,
            text = cleaned,
            pageCount = pageCount,
            uriString = uri.toString()
        )
    }

    /**
     * Lightweight pure-Kotlin .docx extractor using standard java.util.zip.ZipInputStream
     * + Android XmlPullParser reading word/document.xml (zero Apache POI dependency).
     */
    private fun extractDocx(
        context: Context,
        uri: Uri,
        fileName: String
    ): ExtractedDocumentResult {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Không thể mở tệp DOCX từ thiết bị.")

        val extracted = inputStream.use { stream ->
            extractTextFromDocxStream(stream)
        }
        val cleaned = normalizeExtractedText(extracted)
        require(cleaned.isNotBlank()) {
            "Không tìm thấy nội dung văn bản trong tệp DOCX \"$fileName\" (word/document.xml trống)."
        }
        return buildResult(
            fileName = fileName,
            kind = DocumentFileKind.DOCX,
            text = cleaned,
            pageCount = 0,
            uriString = uri.toString()
        )
    }

    internal fun extractTextFromDocxStream(stream: InputStream): String {
        val sb = StringBuilder()
        var foundDocumentXml = false
        ZipInputStream(stream).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val entryName = entry.name.lowercase(Locale.US)
                if (!entry.isDirectory && entryName == "word/document.xml") {
                    foundDocumentXml = true
                    parseWordDocumentXml(zis, sb)
                    break
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        if (!foundDocumentXml) {
            throw IllegalArgumentException("Tệp không phải định dạng .docx hợp lệ (không tìm thấy word/document.xml).")
        }
        return sb.toString()
    }

    private fun parseWordDocumentXml(inputStream: InputStream, out: StringBuilder) {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
        parser.setInput(inputStream, "UTF-8")

        var eventType = parser.eventType
        var insideTextNode = false
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "t" -> insideTextNode = true
                        "tab" -> out.append("\t")
                        "br", "cr" -> out.append("\n")
                    }
                }
                XmlPullParser.TEXT -> {
                    if (insideTextNode) {
                        out.append(parser.text.orEmpty())
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "t" -> insideTextNode = false
                        "tc" -> out.append(" | ")
                        "p" -> out.append("\n")
                    }
                }
            }
            eventType = parser.next()
        }
    }

    private fun extractPlainUtf8(
        context: Context,
        uri: Uri,
        fileName: String,
        kind: DocumentFileKind
    ): ExtractedDocumentResult {
        val raw = context.contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).readText()
        }.orEmpty().removePrefix("\uFEFF").trim()

        require(raw.isNotBlank()) {
            "Tệp \"$fileName\" hiện đang trống."
        }
        return buildResult(
            fileName = fileName,
            kind = kind,
            text = raw,
            pageCount = 0,
            uriString = uri.toString()
        )
    }

    private fun normalizeExtractedText(raw: String): String {
        return raw
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .replace(Regex("[ \\t]+\n"), "\n")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }

    private fun buildResult(
        fileName: String,
        kind: DocumentFileKind,
        text: String,
        pageCount: Int,
        uriString: String?
    ): ExtractedDocumentResult {
        val words = text.split(Regex("\\s+")).count { it.isNotBlank() }
        return ExtractedDocumentResult(
            fileName = fileName,
            fileKind = kind,
            extractedText = text,
            pageCount = pageCount,
            wordCount = words,
            charCount = text.length,
            uriString = uriString
        )
    }
}
