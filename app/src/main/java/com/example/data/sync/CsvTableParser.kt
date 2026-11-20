package com.example.data.sync

/**
 * RFC-4180 compliant CSV parser tailored for Google Sheets GViz CSV exports.
 * Safely handles:
 * - Quoted commas inside text fields
 * - Escaped double-quotes ("") inside embedded JSON columns (e.g. questions_json)
 * - Multi-line newlines inside quoted cells
 * - UTF-8 BOM prefix
 */
object CsvTableParser {

    fun parseToRowMaps(csvText: String): List<Map<String, String>> {
        val cleaned = csvText.removePrefix("\uFEFF")
        val rows = parseRows(cleaned)
        if (rows.size < 2) return emptyList()

        val headers = rows.first().map { normalizeHeader(it) }
        if (headers.all { it.isBlank() }) return emptyList()

        return buildList {
            for (r in 1 until rows.size) {
                val values = rows[r]
                if (values.all { it.isBlank() }) continue
                val map = LinkedHashMap<String, String>(headers.size)
                headers.forEachIndexed { idx, header ->
                    if (header.isNotBlank()) {
                        map[header] = values.getOrElse(idx) { "" }.trim()
                    }
                }
                add(map)
            }
        }
    }

    private fun normalizeHeader(raw: String): String =
        raw.trim()
            .lowercase()
            .replace(" ", "_")
            .replace("-", "_")

    fun parseRows(text: String): List<List<String>> {
        val result = mutableListOf<List<String>>()
        val currentRow = mutableListOf<String>()
        val currentField = StringBuilder()

        var inQuotes = false
        var i = 0
        val length = text.length

        while (i < length) {
            val c = text[i]
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < length && text[i + 1] == '"') {
                        // Escaped quote inside quoted field (very common in questions_json)
                        currentField.append('"')
                        i += 2
                        continue
                    } else {
                        inQuotes = false
                    }
                } else {
                    currentField.append(c)
                }
            } else {
                when (c) {
                    '"' -> {
                        if (currentField.isEmpty()) {
                            inQuotes = true
                        } else {
                            currentField.append(c)
                        }
                    }
                    ',' -> {
                        currentRow.add(currentField.toString())
                        currentField.clear()
                    }
                    '\r' -> {
                        if (i + 1 < length && text[i + 1] == '\n') {
                            i++
                        }
                        currentRow.add(currentField.toString())
                        currentField.clear()
                        result.add(currentRow.toList())
                        currentRow.clear()
                    }
                    '\n' -> {
                        currentRow.add(currentField.toString())
                        currentField.clear()
                        result.add(currentRow.toList())
                        currentRow.clear()
                    }
                    else -> currentField.append(c)
                }
            }
            i++
        }

        if (currentField.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentField.toString())
            result.add(currentRow.toList())
        }

        return result
    }
}
