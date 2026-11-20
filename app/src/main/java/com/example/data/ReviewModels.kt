package com.example.data

import kotlin.math.roundToInt

data class ReviewBandBreakdown(
    val listening: Float,
    val reading: Float,
    val writing: Float,
    val speaking: Float,
    val overall: Float,
    val targetBand: Float = 8.5f
) {
    val meetsTarget: Boolean
        get() = overall >= targetBand - 0.5f
}

data class QuestionTypeAccuracy(
    val id: String,
    val labelVi: String,
    val skillTagEn: String,
    val accuracyPercent: Int,
    val targetPracticeSkill: PracticeSkill
)

data class UpgradeDiffChange(
    val id: String,
    val weakPhraseEn: String,
    val upgradedPhraseEn: String,
    val categoryVi: String,
    val explanationVi: String
)

data class UpgradeDiffComparison(
    val id: String,
    val skillLabel: String,
    val promptSummaryEn: String,
    val originalText: String,
    val upgradedText: String,
    val changes: List<UpgradeDiffChange>,
    val isAiGenerated: Boolean = false
)

data class ReviewErrorLogItem(
    val id: String,
    val questionNumber: Int,
    val skill: ExamQuestionSkill,
    val questionTypeVi: String,
    val promptEn: String,
    val userAnswer: String,
    val correctAnswer: String,
    val distractorTrapVi: String,
    val grammarNoteVi: String,
    val linkedVocabWordId: String,
    val linkedVocabWordEn: String,
    val targetPracticeSkill: PracticeSkill
)

data class ReviewReportBundle(
    val reportId: String,
    val title: String,
    val subtitleVi: String,
    val isSampleReport: Boolean,
    val bands: ReviewBandBreakdown,
    val teacherDiagnosisVi: String,
    val accuracyBreakdown: List<QuestionTypeAccuracy>,
    val writingDiff: UpgradeDiffComparison,
    val speakingDiff: UpgradeDiffComparison,
    val errorLog: List<ReviewErrorLogItem>
)

/**
 * Provides 1 rich pre-loaded Sample Diagnostic Report for immediate exploration on Tab 5
 * AND builds real diagnostic reports dynamically from any submitted Tab 4 ExamAttemptRecord.
 */
object ReviewReportFactory {

    fun sampleDiagnosticReport(): ReviewReportBundle {
        val sampleWritingDiff = UpgradeDiffComparison(
            id = "sample_writing_diff",
            skillLabel = "Writing Task 2",
            promptSummaryEn = "Some educators argue that AI tutoring tools undermine independent critical thinking, while others believe they democratise education.",
            originalText = "Nowadays, AI tutoring apps are very popular in many schools. Some teachers worry that students will become lazy and stop thinking by themselves if they use AI too much. However, I think these tools are very good because poor students can get cheap lessons at home.",
            upgradedText = "In contemporary academia, adaptive AI tutors have become increasingly ubiquitous across educational institutions. Although critics caution that over-reliance on automated guidance may erode independent critical inquiry, I contend that such platforms play a pivotal role in democratising instruction by granting underserved learners equitable access to personalised scaffolding.",
            changes = listOf(
                UpgradeDiffChange(
                    id = "w_c1",
                    weakPhraseEn = "are very popular in many schools",
                    upgradedPhraseEn = "have become increasingly ubiquitous across educational institutions",
                    categoryVi = "Lexical Resource (C2)",
                    explanationVi = "Thay cụm phổ thông Band 6.0 'very popular' bằng tính từ C2 'increasingly ubiquitous' và dùng thì Hiện tại hoàn thành để diễn tả xu hướng học thuật."
                ),
                UpgradeDiffChange(
                    id = "w_c2",
                    weakPhraseEn = "become lazy and stop thinking by themselves",
                    upgradedPhraseEn = "over-reliance on automated guidance may erode independent critical inquiry",
                    categoryVi = "Academic Register & Precision",
                    explanationVi = "Tránh văn nói cảm tính ('become lazy'). Danh từ hóa (Nominalisation) với 'over-reliance' + động từ học thuật 'erode' nâng GRA và LR thẳng lên Band 8.5."
                ),
                UpgradeDiffChange(
                    id = "w_c3",
                    weakPhraseEn = "I think these tools are very good because poor students can get cheap lessons",
                    upgradedPhraseEn = "I contend that such platforms play a pivotal role in democratising instruction by granting underserved learners equitable access",
                    categoryVi = "Collocations & Complex Clause",
                    explanationVi = "Dùng động từ lập luận 'contend that', collocation chuẩn giám khảo 'democratising instruction' và cụm trung tính lịch sự 'underserved learners' thay cho 'poor students'."
                )
            )
        )

        val sampleSpeakingDiff = UpgradeDiffComparison(
            id = "sample_speaking_diff",
            skillLabel = "Speaking Part 2",
            promptSummaryEn = "Describe a teacher or mentor who profoundly influenced your approach to learning.",
            originalText = "I want to talk about my high school teacher, Ms. Lan. She was very nice and her class was never boring. Instead of telling us to remember everything, she let us discuss real problems. Because of her, I became more confident when speaking.",
            upgradedText = "I'd like to highlight an extraordinary mentor from my secondary school years, Ms. Lan, whose seminars were genuinely thought-provoking. Rather than pushing rote memorisation, she encouraged us to dissect real-world dilemmas through lively debate. Consequently, I gained tremendous articulacy and composure whenever expressing nuanced viewpoints.",
            changes = listOf(
                UpgradeDiffChange(
                    id = "s_c1",
                    weakPhraseEn = "was very nice and her class was never boring",
                    upgradedPhraseEn = "an extraordinary mentor ... whose seminars were genuinely thought-provoking",
                    categoryVi = "Relative Clause & Idiomatic Precision",
                    explanationVi = "Kết hợp mệnh đề quan hệ sở hữu 'whose seminars were...' cùng tính từ ghép 'thought-provoking' giúp câu nói tự nhiên và giàu sắc thái hơn hẳn 'very nice / never boring'."
                ),
                UpgradeDiffChange(
                    id = "s_c2",
                    weakPhraseEn = "remember everything",
                    upgradedPhraseEn = "rote memorisation",
                    categoryVi = "Topic Collocation (Education)",
                    explanationVi = "Cụm từ chuyên ngành Giáo dục Band 8.5 'rote memorisation' (học vẹt thuộc lòng) ngắn gọn, đúng trọng tâm và ghi điểm Lexical Resource ngay lập tức."
                ),
                UpgradeDiffChange(
                    id = "s_c3",
                    weakPhraseEn = "became more confident when speaking",
                    upgradedPhraseEn = "gained tremendous articulacy and composure whenever expressing nuanced viewpoints",
                    categoryVi = "Natural Spoken Elevation",
                    explanationVi = "Nâng cấp cấu trúc trạng ngữ rút gọn 'whenever expressing...' kết hợp cặp danh từ tự nhiên 'articulacy and composure' (sự lưu loát và điềm tĩnh)."
                )
            )
        )

        val sampleErrors = listOf(
            ReviewErrorLogItem(
                id = "err_sample_1",
                questionNumber = 3,
                skill = ExamQuestionSkill.READING,
                questionTypeVi = "True / False / Not Given",
                promptEn = "Frictionless educational tools tend to lower students' short-term confidence during revision.",
                userAnswer = "TRUE",
                correctAnswer = "FALSE",
                distractorTrapVi = "Bẫy đảo nghĩa động từ: Đoạn A viết 'boosting students' short-term confidence' (tăng sự tự tin ngắn hạn), trái ngược với 'lower' trong câu hỏi.",
                grammarNoteVi = "Cụm phân từ hiện tại '..., often boosting students' short-term confidence' bổ sung kết quả trực tiếp cho mệnh đề chính.",
                linkedVocabWordId = "ed_2",
                linkedVocabWordEn = "pedagogical",
                targetPracticeSkill = PracticeSkill.READING
            ),
            ReviewErrorLogItem(
                id = "err_sample_2",
                questionNumber = 4,
                skill = ExamQuestionSkill.READING,
                questionTypeVi = "Matching Headings",
                promptEn = "Paragraph C: Unexpected irony in how high-effort engineering cohorts evaluated course enjoyment.",
                userAnswer = "Heading iii",
                correctAnswer = "Heading iv",
                distractorTrapVi = "Bẫy chi tiết phụ: Bạn chọn tiêu đề dựa trên câu đầu về điểm số, bỏ qua từ chuyển hướng 'Yet' mở đầu ý trọng tâm ở câu 2.",
                grammarNoteVi = "Cấu trúc 'mistaking A for B' (nhầm lẫn nỗ lực trí tuệ là giảng dạy kém) diễn tả nguyên nhân cốt lõi của nghịch lý.",
                linkedVocabWordId = "ed_1",
                linkedVocabWordEn = "ubiquitous",
                targetPracticeSkill = PracticeSkill.READING
            ),
            ReviewErrorLogItem(
                id = "err_sample_3",
                questionNumber = 2,
                skill = ExamQuestionSkill.LISTENING,
                questionTypeVi = "Listening Gap-Fill (1 từ)",
                promptEn = "Stylus tablets matched paper performance only when ______________ were strictly turned off.",
                userAnswer = "notification",
                correctAnswer = "notifications",
                distractorTrapVi = "Bẫy số ít / số nhiều: Động từ 'were' ngay sau chỗ trống buộc danh từ phải ở dạng số nhiều 'notifications' (-s cuối từ).",
                grammarNoteVi = "Liên từ điều kiện học thuật 'provided (that) notifications were strictly disabled' = 'miễn là thông báo được tắt hoàn toàn'.",
                linkedVocabWordId = "ed_3",
                linkedVocabWordEn = "mitigate",
                targetPracticeSkill = PracticeSkill.LISTENING
            ),
            ReviewErrorLogItem(
                id = "err_sample_4",
                questionNumber = 6,
                skill = ExamQuestionSkill.READING,
                questionTypeVi = "Summary Completion",
                promptEn = "Curriculum designers advocate teaching ______________ so learners value productive struggle.",
                userAnswer = "literacy",
                correctAnswer = "metacognitive literacy",
                distractorTrapVi = "Bẫy thiếu tính từ bổ nghĩa: Chỉ điền 'literacy' làm mất nghĩa cốt lõi; đề cho phép NO MORE THAN TWO WORDS nên cần trọn cụm 'metacognitive literacy'.",
                grammarNoteVi = "Động từ 'advocate + V-ing' (advocate teaching...) là cấu trúc cực mạnh để tái sử dụng trong IELTS Writing Task 2.",
                linkedVocabWordId = "ed_4",
                linkedVocabWordEn = "foster",
                targetPracticeSkill = PracticeSkill.READING
            )
        )

        return ReviewReportBundle(
            reportId = "sample_report_v1",
            title = "Báo cáo Mẫu • Cambridge IELTS 19 Test 1",
            subtitleVi = "Dữ liệu mẫu chi tiết sẵn sàng khám phá ngay • Mục tiêu Band 8.5",
            isSampleReport = true,
            bands = ReviewBandBreakdown(
                listening = 7.5f,
                reading = 7.0f,
                writing = 6.5f,
                speaking = 7.0f,
                overall = 7.0f,
                targetBand = 8.5f
            ),
            teacherDiagnosisVi = "Bạn đang ở nền tảng rất tốt (Overall 7.0), chỉ còn thiếu 1.5 Band nữa để chạm 8.5! Điểm rơi lớn nhất nằm ở bẫy True/False/Not Given, số nhiều Listening và từ vựng học thuật (Lexical Resource) trong Writing Task 2.",
            accuracyBreakdown = listOf(
                QuestionTypeAccuracy("acc_mh", "Matching Headings", "Reading", 62, PracticeSkill.READING),
                QuestionTypeAccuracy("acc_tfng", "True / False / Not Given", "Reading", 55, PracticeSkill.READING),
                QuestionTypeAccuracy("acc_gap", "Listening Gap-Fill & Số nhiều", "Listening", 74, PracticeSkill.LISTENING),
                QuestionTypeAccuracy("acc_lr", "Lexical Resource (C1–C2)", "Writing", 64, PracticeSkill.WRITING),
                QuestionTypeAccuracy("acc_cc", "Coherence & Cohesion", "Writing / Speaking", 78, PracticeSkill.SPEAKING)
            ),
            writingDiff = sampleWritingDiff,
            speakingDiff = sampleSpeakingDiff,
            errorLog = sampleErrors
        )
    }

    fun fromAttempt(
        attempt: ExamAttemptRecord,
        allExams: List<ExamPaper>
    ): ReviewReportBundle {
        val sample = sampleDiagnosticReport()
        val matchedExam = allExams.firstOrNull { it.id == attempt.examId }
        val baseBand = attempt.estimatedBand.toFloat().coerceIn(4.5f, 9.0f)

        val lQuestions = matchedExam?.questions?.filter { it.skill == ExamQuestionSkill.LISTENING }.orEmpty()
        val rQuestions = matchedExam?.questions?.filter { it.skill == ExamQuestionSkill.READING }.orEmpty()

        val lCorrect = lQuestions.count { q ->
            val a = attempt.objectiveAnswers[q.id].orEmpty().trim()
            a.isNotEmpty() && q.acceptableAnswers.any { it.equals(a, ignoreCase = true) }
        }
        val rCorrect = rQuestions.count { q ->
            val a = attempt.objectiveAnswers[q.id].orEmpty().trim()
            a.isNotEmpty() && q.acceptableAnswers.any { it.equals(a, ignoreCase = true) }
        }

        val listeningBand = if (lQuestions.isNotEmpty()) {
            ExamScoringHelper.estimateBandFromRatio(lCorrect, lQuestions.size).toFloat()
        } else baseBand
        val readingBand = if (rQuestions.isNotEmpty()) {
            ExamScoringHelper.estimateBandFromRatio(rCorrect, rQuestions.size).toFloat()
        } else baseBand
        val writingBand = when {
            attempt.writingWordCount >= 220 -> (baseBand + 0.5f).coerceAtMost(8.5f)
            attempt.writingWordCount >= 80 -> baseBand
            attempt.writingWordCount > 0 -> (baseBand - 0.5f).coerceAtLeast(5.5f)
            else -> baseBand
        }
        val speakingBand = if (attempt.speakingTranscript.length >= 90) {
            baseBand
        } else {
            (baseBand - 0.5f).coerceAtLeast(5.5f)
        }

        val overallRaw = (listeningBand + readingBand + writingBand + speakingBand) / 4f
        val overallRounded = (overallRaw * 2f).roundToInt() / 2f

        val missedQuestions = matchedExam?.questions.orEmpty().filter { q ->
            val includeSkill = when (attempt.mode) {
                ExamSkillMode.LISTENING -> q.skill == ExamQuestionSkill.LISTENING
                ExamSkillMode.READING -> q.skill == ExamQuestionSkill.READING
                ExamSkillMode.FULL_TEST -> true
                else -> false
            }
            if (!includeSkill) return@filter false
            val userAns = attempt.objectiveAnswers[q.id].orEmpty().trim()
            userAns.isEmpty() || q.acceptableAnswers.none { it.equals(userAns, ignoreCase = true) }
        }

        val fallbackWords = listOf(
            "ed_1" to "ubiquitous",
            "ed_2" to "pedagogical",
            "ed_3" to "mitigate",
            "ed_4" to "foster",
            "env_1" to "exacerbate"
        )

        val liveErrors = missedQuestions.mapIndexed { idx, q ->
            val (wordId, wordEn) = fallbackWords[idx % fallbackWords.size]
            val userAns = attempt.objectiveAnswers[q.id].orEmpty().ifBlank { "(Bỏ trống)" }
            val trap = when (q.type) {
                ExamQuestionType.TFNG -> "Bẫy đối lập/suy diễn TFNG: Hãy kiểm tra kỹ trạng từ chỉ mức độ và đối chiếu từng vế với đoạn gốc."
                ExamQuestionType.MCQ -> "Bẫy Distractor trắc nghiệm: Phương án nhiễu thường lặp lại từ khóa bài đọc nhưng bẻ cong quan hệ nguyên nhân – kết quả."
                ExamQuestionType.GAP_FILL -> "Bẫy giới hạn từ & số ít/số nhiều: Kiểm tra từ loại đứng trước/sau chỗ trống và số từ quy định."
            }
            ReviewErrorLogItem(
                id = "live_err_${q.id}",
                questionNumber = q.number,
                skill = q.skill,
                questionTypeVi = q.type.labelVi,
                promptEn = q.promptEn,
                userAnswer = userAns,
                correctAnswer = q.correctAnswer,
                distractorTrapVi = trap,
                grammarNoteVi = "Đối chiếu cụm Paraphrase của \"${q.correctAnswer}\" trong ngữ cảnh câu gốc để tăng tốc độ quét từ khóa Band 8.5.",
                linkedVocabWordId = wordId,
                linkedVocabWordEn = wordEn,
                targetPracticeSkill = if (q.skill == ExamQuestionSkill.LISTENING) PracticeSkill.LISTENING else PracticeSkill.READING
            )
        }

        val tfngQs = matchedExam?.questions?.filter { it.type == ExamQuestionType.TFNG }.orEmpty()
        val mcqQs = matchedExam?.questions?.filter { it.type == ExamQuestionType.MCQ }.orEmpty()
        val gapQs = matchedExam?.questions?.filter { it.type == ExamQuestionType.GAP_FILL }.orEmpty()

        fun pct(list: List<ExamQuestionItem>, defaultPct: Int): Int {
            if (list.isEmpty()) return defaultPct
            val ok = list.count { q ->
                val a = attempt.objectiveAnswers[q.id].orEmpty().trim()
                a.isNotEmpty() && q.acceptableAnswers.any { it.equals(a, ignoreCase = true) }
            }
            return ((ok.toFloat() / list.size.toFloat()) * 100f).toInt()
        }

        val writingDiff = if (attempt.writingDraft.isNotBlank()) {
            UpgradeDiffComparison(
                id = "live_writing_${attempt.id}",
                skillLabel = "Writing (Bài thi vừa nộp)",
                promptSummaryEn = matchedExam?.writingTask2Prompt ?: "Bài viết IELTS Writing bạn vừa nộp từ Phòng thi Strict Mode.",
                originalText = attempt.writingDraft,
                upgradedText = "Chạm nút \"Tạo bản nâng cấp Band 8.5+ bằng Gemini AI\" bên dưới để Giám khảo AI gạch chân lỗi yếu và viết lại bài làm thực tế của bạn lên chuẩn Band 8.5+!",
                changes = emptyList(),
                isAiGenerated = false
            )
        } else {
            sample.writingDiff
        }

        val speakingDiff = if (attempt.speakingTranscript.isNotBlank()) {
            UpgradeDiffComparison(
                id = "live_speaking_${attempt.id}",
                skillLabel = "Speaking (Bài thi vừa nộp)",
                promptSummaryEn = matchedExam?.speakingPrompts?.firstOrNull() ?: "Bài nói IELTS Speaking bạn vừa nộp từ Phòng thi Strict Mode.",
                originalText = attempt.speakingTranscript,
                upgradedText = "Chạm nút \"Tạo bản nâng cấp Band 8.5+ bằng Gemini AI\" bên dưới để AI nâng cấp bài nói của bạn lên chuẩn Band 8.5+!",
                changes = emptyList(),
                isAiGenerated = false
            )
        } else {
            sample.speakingDiff
        }

        val diagnosis = if (overallRounded >= 8.0f) {
            "Xuất sắc! Bài thi \"${attempt.examTitle}\" đạt Overall ${overallRounded} — rất sát mốc Band 8.5. Hãy rà nhanh các câu cắm cờ và tinh chỉnh diễn đạt C2 để giữ phong độ ổn định!"
        } else {
            "Hoàn thành bài thi \"${attempt.examTitle}\" (${attempt.correctCount}/${attempt.totalObjectiveQuestions} đúng, dự kiến Overall ${overallRounded}). Hãy đưa ngay từ sai vào SRS và bấm Luyện lại dạng bài yếu bên dưới để kéo Band nhanh nhất!"
        }

        return ReviewReportBundle(
            reportId = attempt.id,
            title = attempt.examTitle,
            subtitleVi = "Nộp lúc ${attempt.submittedAtLabel} • Chế độ ${attempt.mode.labelVi}",
            isSampleReport = false,
            bands = ReviewBandBreakdown(
                listening = listeningBand,
                reading = readingBand,
                writing = writingBand,
                speaking = speakingBand,
                overall = overallRounded,
                targetBand = 8.5f
            ),
            teacherDiagnosisVi = diagnosis,
            accuracyBreakdown = listOf(
                QuestionTypeAccuracy("live_tfng", "True / False / Not Given", "Reading", pct(tfngQs, 65), PracticeSkill.READING),
                QuestionTypeAccuracy("live_mcq", "Trắc nghiệm & Matching", "Reading / Listening", pct(mcqQs, 70), PracticeSkill.READING),
                QuestionTypeAccuracy("live_gap", "Điền từ ngắn (Gap-Fill)", "Listening / Reading", pct(gapQs, 68), PracticeSkill.LISTENING),
                QuestionTypeAccuracy(
                    "live_lr",
                    "Lexical Resource",
                    "Writing",
                    if (attempt.writingWordCount >= 180) 76 else 62,
                    PracticeSkill.WRITING
                ),
                QuestionTypeAccuracy(
                    "live_cc",
                    "Coherence & Cohesion",
                    "Speaking / Writing",
                    if (attempt.speakingTranscript.length >= 80 || attempt.writingWordCount >= 150) 74 else 64,
                    PracticeSkill.SPEAKING
                )
            ),
            writingDiff = writingDiff,
            speakingDiff = speakingDiff,
            errorLog = liveErrors
        )
    }
}
