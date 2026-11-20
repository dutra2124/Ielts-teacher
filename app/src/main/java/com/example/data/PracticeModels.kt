package com.example.data

enum class PracticeSkill(
    val index: Int,
    val labelEn: String,
    val shortVi: String,
    val routeKey: String
) {
    READING(0, "Reading", "Đọc", "reading"),
    LISTENING(1, "Listening", "Nghe", "listening"),
    SPEAKING(2, "Speaking", "Nói", "speaking"),
    WRITING(3, "Writing", "Viết", "writing")
}

enum class ReadingQuestionType(val badgeVi: String) {
    TFNG("True / False / Not Given"),
    MATCHING_HEADING("Matching Headings"),
    GAP_FILL("Điền từ (≤ 2 từ)")
}

data class ThreeLevelHint(
    val questionLabel: String,
    val level1LocateVi: String,
    val level2ParaphraseVi: String,
    val level3DistractorVi: String
)

data class KeywordMappingRow(
    val questionNumber: Int,
    val questionKeyword: String,
    val passageParaphrase: String,
    val paragraphRef: String,
    val explanationVi: String
)

data class PassageSentence(
    val id: String,
    val paragraphLabel: String,
    val textEn: String,
    val isComplexTarget: Boolean = false
)

data class ReadingQuestion(
    val id: String,
    val number: Int,
    val type: ReadingQuestionType,
    val promptEn: String,
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val acceptableAnswers: List<String> = listOf(correctAnswer),
    val hint: ThreeLevelHint,
    val keywordMapping: KeywordMappingRow
)

data class ReadingPracticePack(
    val moduleId: Int,
    val titleEn: String,
    val subtitleVi: String,
    val estimatedMinutes: Int,
    val headingsBank: List<String>,
    val sentences: List<PassageSentence>,
    val questions: List<ReadingQuestion>
)

data class ListeningSegment(
    val id: String,
    val speaker: String,
    val textEn: String,
    val dictationPromptVi: String? = null
)

data class ListeningQuestion(
    val id: String,
    val number: Int,
    val promptEn: String,
    val options: List<String>,
    val correctAnswer: String,
    val hint: ThreeLevelHint,
    val keywordMapping: KeywordMappingRow
)

data class ListeningPracticePack(
    val moduleId: Int,
    val sectionLabel: String,
    val titleEn: String,
    val subtitleVi: String,
    val segments: List<ListeningSegment>,
    val dictationSentenceIds: List<String>,
    val questions: List<ListeningQuestion>
)

enum class SpeakingPart(val labelEn: String, val badgeVi: String) {
    PART_1("Part 1", "Phỏng vấn ngắn"),
    PART_2("Part 2", "Cue Card • 1 phút chuẩn bị"),
    PART_3("Part 3", "Thảo luận chuyên sâu")
}

data class SpeakingPromptItem(
    val part: SpeakingPart,
    val promptEn: String,
    val cueBulletsEn: List<String> = emptyList(),
    val strategyTipVi: String,
    val sampleBand6Draft: String
)

data class SpeakingPracticePack(
    val moduleId: Int,
    val topicEn: String,
    val prompts: List<SpeakingPromptItem>
)

enum class WritingTaskType(val labelEn: String, val targetWords: Int, val badgeVi: String) {
    TASK_1("Task 1", 150, "Biểu đồ / Quy trình • ≥150 từ"),
    TASK_2("Task 2", 250, "Bài luận học thuật • ≥250 từ")
}

data class WritingDataPoint(
    val label: String,
    val value2015: Int,
    val value2025: Int,
    val unit: String = "%"
)

data class WritingOutlineSlot(
    val stepTitleVi: String,
    val guideVi: String,
    val starterFrameEn: String
)

data class WritingPromptItem(
    val taskType: WritingTaskType,
    val promptEn: String,
    val chartTitleEn: String? = null,
    val chartData: List<WritingDataPoint> = emptyList(),
    val outlineSlots: List<WritingOutlineSlot>,
    val sampleStarterDraft: String
)

data class WritingPracticePack(
    val moduleId: Int,
    val topicEn: String,
    val prompts: List<WritingPromptItem>
)

data class Band85VocabHint(
    val phraseEn: String,
    val categoryVi: String,
    val meaningVi: String,
    val exampleEn: String
)

data class ModulePracticeBundle(
    val moduleId: Int,
    val reading: ReadingPracticePack,
    val listening: ListeningPracticePack,
    val speaking: SpeakingPracticePack,
    val writing: WritingPracticePack,
    val band85VocabHints: List<Band85VocabHint>
)
