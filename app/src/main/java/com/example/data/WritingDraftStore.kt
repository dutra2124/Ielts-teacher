package com.example.data

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SavedWritingDraft(
    val text: String,
    val savedAtEpochMs: Long
) {
    fun formattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(savedAtEpochMs))
    }
}

/**
 * Lightweight local auto-draft protection for IELTS Writing Task 1 & Task 2
 * across both Practice (Luyện) and Mock Exam (Thi).
 * Saves automatically every 5 seconds and restores unsaved work on accidental exit.
 */
object WritingDraftStore {

    private const val PREF_NAME = "master_ielts_writing_drafts_v1"

    fun practiceKey(moduleId: Int, taskType: WritingTaskType): String =
        "practice_m${moduleId}_${taskType.name.lowercase(Locale.US)}"

    fun examKey(examId: String, taskIndex: Int): String =
        "exam_${examId}_task_${taskIndex + 1}"

    fun saveDraft(context: Context, key: String, text: String): Long {
        val now = System.currentTimeMillis()
        val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (text.isBlank()) {
            prefs.edit()
                .remove("${key}_text")
                .remove("${key}_ts")
                .apply()
        } else {
            prefs.edit()
                .putString("${key}_text", text)
                .putLong("${key}_ts", now)
                .apply()
        }
        return now
    }

    fun loadDraft(context: Context, key: String): SavedWritingDraft? {
        val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val text = prefs.getString("${key}_text", null) ?: return null
        if (text.isBlank()) return null
        val ts = prefs.getLong("${key}_ts", System.currentTimeMillis())
        return SavedWritingDraft(text = text, savedAtEpochMs = ts)
    }

    fun clearDraft(context: Context, key: String) {
        val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove("${key}_text")
            .remove("${key}_ts")
            .apply()
    }
}
