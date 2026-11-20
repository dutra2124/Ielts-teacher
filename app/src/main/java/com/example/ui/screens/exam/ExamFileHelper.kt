package com.example.ui.screens.exam

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

data class ImportedExamDocument(
    val displayName: String,
    val mimeType: String,
    val extractedText: String,
    val pdfBytes: ByteArray? = null
)

/**
 * Zero-permission Storage Access Framework (SAF) document reader for PDF, DOCX, TXT, and JSON.
 */
object ExamFileHelper {

    private const val MAX_PDF_BYTES = 18 * 1024 * 1024 // Strictly under 20MB inline limit

    fun getDisplayName(context: Context, uri: Uri): String {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) {
                    val name = cursor.getString(idx)
                    if (!name.isNullOrBlank()) return name
                }
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/') ?: "exam_file"
    }

    fun readExamDocument(context: Context, uri: Uri): Result<ImportedExamDocument> = runCatching {
        val displayName = getDisplayName(context, uri)
        val mime = context.contentResolver.getType(uri).orEmpty()
        val lowerName = displayName.lowercase()

        when {
            mime == "application/pdf" || lowerName.endsWith(".pdf") -> {
                val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
                    val buffer = ByteArrayOutputStream()
                    val chunk = ByteArray(8192)
                    var total = 0
                    while (true) {
                        val read = input.read(chunk)
                        if (read <= 0) break
                        total += read
                        if (total > MAX_PDF_BYTES) {
                            throw IllegalArgumentException("File PDF vượt quá giới hạn 18MB. Vui lòng chọn file nhỏ hơn.")
                        }
                        buffer.write(chunk, 0, read)
                    }
                    buffer.toByteArray()
                } ?: throw IllegalStateException("Không thể đọc dữ liệu file PDF.")

                ImportedExamDocument(
                    displayName = displayName,
                    mimeType = "application/pdf",
                    extractedText = "",
                    pdfBytes = bytes
                )
            }

            lowerName.endsWith(".docx") ||
                mime == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> {
                val extracted = extractDocxText(context, uri)
                if (extracted.isBlank()) {
                    throw IllegalStateException("Không tìm thấy văn bản trong file DOCX này.")
                }
                ImportedExamDocument(
                    displayName = displayName,
                    mimeType = mime.ifBlank { "application/vnd.openxmlformats-officedocument.wordprocessingml.document" },
                    extractedText = extracted
                )
            }

            else -> {
                val text = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use {
                    it.readText()
                } ?: throw IllegalStateException("Không thể đọc nội dung tệp văn bản.")
                ImportedExamDocument(
                    displayName = displayName,
                    mimeType = mime.ifBlank { "text/plain" },
                    extractedText = text
                )
            }
        }
    }

    fun writeTextToUri(context: Context, uri: Uri, content: String): Result<Unit> = runCatching {
        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(content.toByteArray(Charsets.UTF_8))
            out.flush()
        } ?: throw IllegalStateException("Không thể ghi file mẫu JSON.")
    }

    private fun extractDocxText(context: Context, uri: Uri): String {
        val sb = StringBuilder()
        context.contentResolver.openInputStream(uri)?.use { raw ->
            ZipInputStream(raw).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (entry.name == "word/document.xml") {
                        val xml = zip.bufferedReader(Charsets.UTF_8).readText()
                        val cleaned = xml
                            .replace(Regex("</w:p>"), "\n")
                            .replace(Regex("<[^>]+>"), " ")
                            .replace("&amp;", "&")
                            .replace("&lt;", "<")
                            .replace("&gt;", ">")
                            .replace("&quot;", "\"")
                            .replace(Regex("[ \\t]+"), " ")
                            .replace(Regex("\\n\\s*\\n+"), "\n\n")
                            .trim()
                        sb.append(cleaned)
                        break
                    }
                    entry = zip.nextEntry
                }
            }
        }
        return sb.toString()
    }
}
