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

data class DeepQuestionAnalysis(
    val quickTipVi: String,
    val commonMistakesVi: String,
    val misconceptionTrapVi: String,
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
    val targetPracticeSkill: PracticeSkill,
    val isCorrect: Boolean = false,
    val passageExcerptEn: String = "",
    val deepAnalysis: DeepQuestionAnalysis = DeepQuestionAnalysis(
        quickTipVi = "Gạch chân từ khóa định vị (tên riêng, số liệu, động từ chính) trong câu hỏi trước khi quét đoạn văn.",
        commonMistakesVi = "Chọn đáp án vội khi thấy từ khóa trùng khớp mà chưa kiểm tra mệnh đề phủ định hoặc giới hạn từ.",
        misconceptionTrapVi = distractorTrapVi
    )
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
                targetPracticeSkill = PracticeSkill.READING,
                isCorrect = false,
                passageExcerptEn = "Frictionless apps streamline revision, often boosting students' short-term confidence while masking shallow retention.",
                deepAnalysis = DeepQuestionAnalysis(
                    quickTipVi = "Bước 1: Khoanh tròn động từ mang chiều hướng tăng/giảm ('lower') và chủ ngữ ('Frictionless educational tools'). Bước 2: Định vị cụm 'short-term confidence' trong đoạn A rồi đối chiếu trực tiếp chiều hướng: 'boosting' (tăng) ≠ 'lower' (giảm) → chốt ngay FALSE trong 15 giây.",
                    commonMistakesVi = "Thí sinh Band 6.0–6.5 hay đọc thấy vế sau mang nghĩa tiêu cực ('masking shallow retention') nên suy diễn chung rằng công cụ đó làm giảm tự tin và chọn TRUE hoặc nhầm sang NOT GIVEN.",
                    misconceptionTrapVi = "Bẫy đảo chiều kết hợp tương phản kép: Giám khảo cố tình đặt kết quả tích cực ngắn hạn ('boosting confidence') ngay cạnh hệ quả tiêu cực dài hạn ('shallow retention') để thí sinh đọc lướt bị nhiễu cảm tính."
                )
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
                targetPracticeSkill = PracticeSkill.READING,
                isCorrect = false,
                passageExcerptEn = "Engineering students in active-learning cohorts achieved higher exam scores. Yet course surveys revealed they rated lectures as less enjoyable, mistaking cognitive effort for poor instruction.",
                deepAnalysis = DeepQuestionAnalysis(
                    quickTipVi = "Đừng chỉ đọc câu đầu đoạn! Khi làm Matching Headings, hãy quét ngay các từ bẻ lái (Yet / However / Surprisingly / In contrast). Ý chính của cả đoạn gần như luôn nằm ngay sau từ bẻ lái đó.",
                    commonMistakesVi = "Mắc bẫy 'word-matching': Thấy câu đầu có từ 'higher exam scores' liền ghép với Heading nói về thành tích điểm số mà bỏ qua thông điệp nghịch lý xuyên suốt cả đoạn.",
                    misconceptionTrapVi = "Hiểu nhầm rằng câu đầu tiên (Topic Sentence) luôn chứa đáp án. Trong Reading Passage 2–3 chuẩn Cambridge, câu 1 thường chỉ là bối cảnh nền để làm nổi bật sự trớ trêu ('unexpected irony') ở câu 2–3."
                )
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
                targetPracticeSkill = PracticeSkill.LISTENING,
                isCorrect = false,
                passageExcerptEn = "Interestingly, stylus-equipped tablets matched paper retention, provided notifications were strictly disabled throughout the lecture.",
                deepAnalysis = DeepQuestionAnalysis(
                    quickTipVi = "Trong 30 giây đọc đề trước khi băng chạy, luôn nhìn NGAY từ đứng sau chỗ trống ('were') để dự đoán trước loại từ là Danh từ số nhiều (+s/es). Khi nghe thấy cụm chuyển ý 'provided (that)...' (paraphrase của 'only when'), tập trung tối đa vào âm đuôi /z/.",
                    commonMistakesVi = "Rớt mất đuôi '-s' số nhiều ('notification' thay vì 'notifications') hoặc viết thừa mạo từ vượt giới hạn ONE WORD ONLY — lỗi kinh điển khiến thí sinh mất 0.5–1.0 Band Listening dù nghe đúng từ.",
                    misconceptionTrapVi = "Trong băng ghi âm, âm /z/ của 'notifications' nối nhanh sang 'were' nên rất dễ nghe thành số ít. Ngoài ra, câu đề đổi 'disabled' thành 'turned off' và 'provided' thành 'only when' khiến thí sinh chờ cụm 'turned off' sẽ bị trôi qua đáp án."
                )
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
                targetPracticeSkill = PracticeSkill.READING,
                isCorrect = false,
                passageExcerptEn = "Consequently, curriculum designers increasingly advocate explicit metacognitive literacy so students recognise productive struggle as genuine learning.",
                deepAnalysis = DeepQuestionAnalysis(
                    quickTipVi = "Kiểm tra giới hạn từ (NO MORE THAN TWO WORDS) và xác định cụm danh từ hoàn chỉnh đứng sau động từ 'advocate'. Nếu bỏ tính từ đứng trước mà câu bị đổi/loãng nghĩa ('literacy' chung chung ≠ 'metacognitive literacy'), bắt buộc phải lấy cả tính từ.",
                    commonMistakesVi = "Thói quen chỉ điền 1 danh từ chính cuối cùng để 'an toàn', hoặc chép luôn cả tính từ thừa thứ 3 ('explicit metacognitive literacy' = 3 từ) dẫn đến vi phạm Word Limit.",
                    misconceptionTrapVi = "Thí sinh hay hiểu nhầm 'literacy' là đủ nghĩa. Thực tế 'explicit' chỉ mức độ giảng dạy (có thể lược), còn 'metacognitive' là định nghĩa chuyên ngành bắt buộc tạo nên khái niệm 'năng lực siêu nhận thức'."
                )
            ),
            ReviewErrorLogItem(
                id = "err_sample_5",
                questionNumber = 1,
                skill = ExamQuestionSkill.READING,
                questionTypeVi = "Multiple Choice (MCQ)",
                promptEn = "What is the writer's main concern regarding rapid AI-assisted summarisation?",
                userAnswer = "B. It reduces cognitive engagement required for long-term memory.",
                correctAnswer = "B. It reduces cognitive engagement required for long-term memory.",
                distractorTrapVi = "Câu này bạn làm ĐÚNG! Phương án A & C là bẫy 'nửa đúng nửa sai' khi phóng đại chi phí phần cứng.",
                grammarNoteVi = "Mệnh đề quan hệ rút gọn 'required for long-term memory' = 'which is required for long-term memory'.",
                linkedVocabWordId = "ed_1",
                linkedVocabWordEn = "ubiquitous",
                targetPracticeSkill = PracticeSkill.READING,
                isCorrect = true,
                passageExcerptEn = "By automating every synthesis step, learners bypass the deep cognitive engagement required to encode concepts into long-term memory.",
                deepAnalysis = DeepQuestionAnalysis(
                    quickTipVi = "Với câu hỏi 'main concern / main purpose' trong MCQ, hãy loại trừ trước các phương án chứa từ cực đoan (only, always, completely) hoặc chi tiết chỉ xuất hiện ở 1 cụm nhỏ, rồi so sánh ý cốt lõi của phương án còn lại.",
                    commonMistakesVi = "Hay chọn nhầm đáp án có nhiều từ giống hệt bài đọc (word-for-word) thay vì đáp án dùng Paraphrase chuẩn ('bypass deep engagement' = 'reduces cognitive engagement').",
                    misconceptionTrapVi = "Bẫy 'Đúng sự thật ngoài đời nhưng không có trong bài' hoặc bẫy 'Quá hẹp': các đáp án nhiễu thường nêu đúng 1 ví dụ phụ nhưng không phải 'main concern' của tác giả."
                )
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

        val reviewedQuestions = matchedExam?.questions.orEmpty().filter { q ->
            when (attempt.mode) {
                ExamSkillMode.LISTENING -> q.skill == ExamQuestionSkill.LISTENING
                ExamSkillMode.READING -> q.skill == ExamQuestionSkill.READING
                ExamSkillMode.FULL_TEST -> true
                else -> false
            }
        }

        val fallbackWords = listOf(
            "ed_1" to "ubiquitous",
            "ed_2" to "pedagogical",
            "ed_3" to "mitigate",
            "ed_4" to "foster",
            "env_1" to "exacerbate"
        )

        val excerptBase = matchedExam?.readingParagraphs
            ?.firstOrNull()
            ?.sentences
            ?.joinToString(" ")
            ?.take(280)
            .orEmpty()

        val liveErrors = reviewedQuestions.mapIndexed { idx, q ->
            val (wordId, wordEn) = fallbackWords[idx % fallbackWords.size]
            val rawAns = attempt.objectiveAnswers[q.id].orEmpty().trim()
            val userAns = rawAns.ifBlank { "(Bỏ trống)" }
            val isCorrect = rawAns.isNotEmpty() && q.acceptableAnswers.any { it.equals(rawAns, ignoreCase = true) }

            val deep = buildDiagnosticForQuestion(q = q, userAns = userAns, isCorrect = isCorrect)
            ReviewErrorLogItem(
                id = "live_err_${q.id}",
                questionNumber = q.number,
                skill = q.skill,
                questionTypeVi = q.type.labelVi,
                promptEn = q.promptEn,
                userAnswer = userAns,
                correctAnswer = q.correctAnswer,
                distractorTrapVi = deep.misconceptionTrapVi,
                grammarNoteVi = "Đối chiếu cụm Paraphrase của \"${q.correctAnswer}\" trong ngữ cảnh câu gốc để tăng tốc độ quét từ khóa Band 8.5.",
                linkedVocabWordId = wordId,
                linkedVocabWordEn = wordEn,
                targetPracticeSkill = if (q.skill == ExamQuestionSkill.LISTENING) PracticeSkill.LISTENING else PracticeSkill.READING,
                isCorrect = isCorrect,
                passageExcerptEn = if (q.skill == ExamQuestionSkill.LISTENING) {
                    matchedExam?.listeningScript?.joinToString(" ")?.take(280).orEmpty()
                } else {
                    excerptBase
                },
                deepAnalysis = deep
            )
        }.sortedBy { it.isCorrect } // Show missed questions first, then correct ones for complete review

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

    private fun buildDiagnosticForQuestion(
        q: ExamQuestionItem,
        userAns: String,
        isCorrect: Boolean
    ): DeepQuestionAnalysis {
        val statusNote = if (isCorrect) {
            "Bạn đã chọn đúng \"${q.correctAnswer}\" — hãy giữ vững kỹ thuật đối chiếu này ở tốc độ thi thật!"
        } else {
            "Bạn chọn \"$userAns\" thay vì \"${q.correctAnswer}\"."
        }
        return when (q.type) {
            ExamQuestionType.TFNG -> DeepQuestionAnalysis(
                quickTipVi = "Bước 1: Khoanh tròn từ khóa định vị (tên riêng, số liệu) và từ hạn định (only, always, solely, majority) trong mệnh đề. Bước 2: So sánh 3 mức — khớp hoàn toàn = TRUE, trái ngược trực tiếp = FALSE, không đề cập yếu tố so sánh/điều kiện = NOT GIVEN.",
                commonMistakesVi = "$statusNote Thí sinh Band 6.0–6.5 thường nhầm lẫn nghiêm trọng giữa FALSE và NOT GIVEN vì tự suy diễn logic ngoài bài đọc thay vì dựa hoàn toàn vào bằng chứng văn bản.",
                misconceptionTrapVi = "Bẫy Distractor TFNG: Đoạn văn thường lặp lại y nguyên chủ ngữ trong câu hỏi nhưng lồng thêm từ giảm nhẹ (partially, some, in certain cases) hoặc không hề nêu phép so sánh mà câu hỏi khẳng định."
            )

            ExamQuestionType.MCQ -> if (q.skill == ExamQuestionSkill.LISTENING) {
                DeepQuestionAnalysis(
                    quickTipVi = "Đọc trước stem câu hỏi và gạch chân từ khóa phân biệt giữa 3–4 phương án A/B/C/D trước khi băng chạy. Lắng nghe chặt các từ chuyển hướng (however, actually, on second thought) để chốt đáp án \"${q.correctAnswer}\".",
                    commonMistakesVi = "$statusNote Lỗi phổ biến Band 6.0–6.5 là chọn ngay phương án đầu tiên nghe thấy giống từ khóa trên giấy (word-matching) mà bỏ qua phần phủ định hoặc sửa ý ngay sau đó.",
                    misconceptionTrapVi = "Bẫy tự sửa ý (Self-correction Distractor): Người nói thường nhắc đến phương án nhiễu trước như một giả định hoặc kế hoạch cũ, sau đó mới đổi sang đáp án đúng bằng cách diễn đạt đồng nghĩa (Paraphrase)."
                )
            } else {
                DeepQuestionAnalysis(
                    quickTipVi = "Áp dụng phương pháp loại trừ 2 bước: xác định đoạn văn chứa từ khóa gốc, đọc kỹ câu chủ đề + câu kết đoạn, rồi gạch bỏ phương án quá cực đoan (all, never, completely) hoặc sai phạm vi trước khi chọn \"${q.correctAnswer}\".",
                    commonMistakesVi = "$statusNote Thí sinh Band 6.0–6.5 hay bị thu hút bởi phương án có chứa đúng từ vựng xuất hiện trong bài đọc dù ý nghĩa tổng thể đã bị bóp méo hoặc đảo ngược nguyên nhân – kết quả.",
                    misconceptionTrapVi = "Bẫy sao chép từ vựng (Lexical Overlap Trap): Phương án nhiễu mượn nguyên từ khóa bề mặt của bài đọc để tạo cảm giác quen thuộc, trong khi đáp án đúng \"${q.correctAnswer}\" lại được viết lại hoàn toàn bằng từ đồng nghĩa học thuật."
                )
            }

            ExamQuestionType.GAP_FILL -> if (q.skill == ExamQuestionSkill.LISTENING) {
                DeepQuestionAnalysis(
                    quickTipVi = "Dự đoán trước loại từ (Danh từ số ít/nhiều, Tính từ, Số liệu) dựa vào mạo từ/giới từ đứng ngay trước chỗ trống và kiểm tra giới hạn từ (NO MORE THAN TWO WORDS). Chú ý trọng âm giọng người nói khi đọc tới \"${q.correctAnswer}\".",
                    commonMistakesVi = "$statusNote Thí sinh Band 6.0–6.5 rất dễ mất điểm oan do thiếu đuôi số nhiều \"-s/-es\", sai chính tả từ học thuật, hoặc viết thừa từ đã có sẵn cạnh chỗ trống.",
                    misconceptionTrapVi = "Bẫy thông tin nhiễu liên tiếp (Double-Mention Trap): Bài nghe thường đưa ra một con số/từ khóa thử nghiệm trước rồi mới đính chính thông tin chính thức ngay câu kế tiếp — chỉ chốt đáp án sau khi người nói hoàn tất ý."
                )
            } else {
                DeepQuestionAnalysis(
                    quickTipVi = "Xác định cấu trúc ngữ pháp quanh chỗ trống để biết chính xác cần điền danh từ, động từ hay tính từ, sau đó quét từ khóa định vị quanh câu và chép nguyên dạng từ trong bài đọc cho đáp án \"${q.correctAnswer}\".",
                    commonMistakesVi = "$statusNote Lỗi hay gặp ở Band 6.0–6.5 là tự ý biến đổi loại từ không khớp bài gốc, viết vượt giới hạn số từ cho phép hoặc bỏ sót tiền tố/hậu tố ngữ pháp.",
                    misconceptionTrapVi = "Bẫy Paraphrase cấu trúc câu: Câu hỏi thường đổi từ thể chủ động sang bị động hoặc danh từ hóa động từ gốc trong bài đọc, khiến thí sinh tìm sai vị trí từ cần điền nếu chỉ dò theo thứ tự từ."
                )
            }
        }
    }
}

