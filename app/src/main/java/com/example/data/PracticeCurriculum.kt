package com.example.data

/**
 * Rich academic IELTS 4-skill curriculum for Tab 3 (Luyện), dynamically synced to activeModuleId.
 * Provides bespoke full academic bundles for Module 1 (Education) and Module 5 (Environment),
 * plus tailored topic-specific bundles for all remaining modules in the 12-module roadmap.
 */
object PracticeCurriculum {

    fun bundleForModule(moduleId: Int): ModulePracticeBundle {
        return when (moduleId) {
            1 -> educationBundle()
            5 -> environmentBundle()
            else -> dynamicModuleBundle(IeltsCurriculum.moduleById(moduleId))
        }
    }

    private fun educationBundle(): ModulePracticeBundle {
        val reading = ReadingPracticePack(
            moduleId = 1,
            titleEn = "Reimagining Higher Education in the Age of Adaptive Algorithms",
            subtitleVi = "Đọc hiểu học thuật • Giáo dục đại học & Công nghệ thích ứng",
            estimatedMinutes = 12,
            headingsBank = listOf(
                "i. Why automated grading alone cannot replace human mentorship",
                "ii. Evidence that personalised pacing narrows socioeconomic attainment gaps",
                "iii. Historical origins of standardised university entrance exams",
                "iv. Institutional resistance and hidden costs of algorithmic classrooms",
                "v. Balancing cognitive autonomy with data-driven instruction"
            ),
            sentences = listOf(
                PassageSentence(
                    id = "ed_s1",
                    paragraphLabel = "A",
                    textEn = "Over the past decade, universities worldwide have invested heavily in adaptive learning platforms designed to tailor instructional content to each student's real-time mastery.",
                    isComplexTarget = true
                ),
                PassageSentence(
                    id = "ed_s2",
                    paragraphLabel = "A",
                    textEn = "Drawing on longitudinal data from forty public colleges, researchers observed that first-generation undergraduates using adaptive modules closed nearly a third of the historic achievement gap within two semesters.",
                    isComplexTarget = true
                ),
                PassageSentence(
                    id = "ed_s3",
                    paragraphLabel = "B",
                    textEn = "Not only do these systems accelerate foundational comprehension, but they also free faculty members from repetitive remediation so seminars can focus on critical inquiry.",
                    isComplexTarget = true
                ),
                PassageSentence(
                    id = "ed_s4",
                    paragraphLabel = "B",
                    textEn = "Nevertheless, critics caution that over-reliance on automated feedback loops risks reducing nuanced scholarship to measurable micro-skills.",
                    isComplexTarget = true
                ),
                PassageSentence(
                    id = "ed_s5",
                    paragraphLabel = "C",
                    textEn = "What many administrators underestimate, however, is the substantial expenditure required for continuous teacher training and ethical data governance.",
                    isComplexTarget = true
                ),
                PassageSentence(
                    id = "ed_s6",
                    paragraphLabel = "C",
                    textEn = "Had institutions consulted veteran lecturers before mandating software rollouts, widespread classroom fatigue could have been significantly mitigated.",
                    isComplexTarget = true
                ),
                PassageSentence(
                    id = "ed_s7",
                    paragraphLabel = "D",
                    textEn = "Ultimately, enduring pedagogical reform depends on striking a deliberate equilibrium between algorithmic precision and empathetic human guidance.",
                    isComplexTarget = true
                )
            ),
            questions = listOf(
                ReadingQuestion(
                    id = "ed_q1",
                    number = 1,
                    type = ReadingQuestionType.TFNG,
                    promptEn = "First-generation college students using adaptive modules completely eliminated the historical attainment gap within two semesters.",
                    options = listOf("TRUE", "FALSE", "NOT GIVEN"),
                    correctAnswer = "FALSE",
                    hint = ThreeLevelHint(
                        questionLabel = "Câu 1 • True / False / Not Given",
                        level1LocateVi = "Tập trung đọc câu thứ 2 của Đoạn A (nhắc đến 'first-generation undergraduates').",
                        level2ParaphraseVi = "Paraphrase cần đối chiếu: 'attainment gap' ↔ 'achievement gap'; 'completely eliminated' ↔ 'closed nearly a third'.",
                        level3DistractorVi = "Bẫy từ tuyệt đối (Extreme Qualifier): Câu hỏi dùng 'completely eliminated' (xóa bỏ hoàn toàn), trong khi bài đọc chỉ nói 'nearly a third' (gần 1/3) → Chọn FALSE."
                    ),
                    keywordMapping = KeywordMappingRow(
                        questionNumber = 1,
                        questionKeyword = "completely eliminated the historical attainment gap",
                        passageParaphrase = "closed nearly a third of the historic achievement gap",
                        paragraphRef = "Đoạn A",
                        explanationVi = "Bài đọc chỉ ghi nhận thu hẹp gần 1/3 khoảng cách thành tích chứ không xóa bỏ hoàn toàn → FALSE."
                    )
                ),
                ReadingQuestion(
                    id = "ed_q2",
                    number = 2,
                    type = ReadingQuestionType.TFNG,
                    promptEn = "Adaptive learning platforms have led to higher postgraduate enrolment rates in European universities.",
                    options = listOf("TRUE", "FALSE", "NOT GIVEN"),
                    correctAnswer = "NOT GIVEN",
                    hint = ThreeLevelHint(
                        questionLabel = "Câu 2 • True / False / Not Given",
                        level1LocateVi = "Quét nhanh Đoạn A và B xem có đề cập đến 'postgraduate enrolment' hay 'European universities' không.",
                        level2ParaphraseVi = "Bài đọc chỉ nhắc 'universities worldwide' và 'forty public colleges', hoàn toàn không tách riêng khu vực châu Âu hay bậc sau đại học.",
                        level3DistractorVi = "Bẫy suy diễn hợp lý: Dù nền tảng thích ứng mang lại hiệu quả tích cực, bài đọc không hề đề cập tỉ lệ nhập học sau đại học tại châu Âu → Chọn NOT GIVEN."
                    ),
                    keywordMapping = KeywordMappingRow(
                        questionNumber = 2,
                        questionKeyword = "higher postgraduate enrolment rates in European universities",
                        passageParaphrase = "Không có thông tin tương ứng trong bài",
                        paragraphRef = "Đoạn A–D",
                        explanationVi = "Bài đọc không cung cấp số liệu về tuyển sinh sau đại học hay riêng các trường đại học châu Âu → NOT GIVEN."
                    )
                ),
                ReadingQuestion(
                    id = "ed_q3",
                    number = 3,
                    type = ReadingQuestionType.MATCHING_HEADING,
                    promptEn = "Choose the best heading for Paragraph A:",
                    options = listOf("i", "ii", "iii", "iv", "v"),
                    correctAnswer = "ii",
                    hint = ThreeLevelHint(
                        questionLabel = "Câu 3 • Matching Headings (Đoạn A)",
                        level1LocateVi = "Đọc câu kết đoạn A để nắm ý trọng tâm thay vì chỉ nhìn câu mở đầu.",
                        level2ParaphraseVi = "'personalised pacing' ↔ 'tailor instructional content to each student's real-time mastery'; 'narrows socioeconomic attainment gaps' ↔ 'first-generation undergraduates ... closed nearly a third of the historic achievement gap'.",
                        level3DistractorVi = "Đừng bị đánh lừa bởi tiêu đề iii ('standardised entrance exams') — Đoạn A nói về nền tảng học thích ứng, không phải kỳ thi tuyển sinh."
                    ),
                    keywordMapping = KeywordMappingRow(
                        questionNumber = 3,
                        questionKeyword = "personalised pacing narrows socioeconomic attainment gaps (ii)",
                        passageParaphrase = "tailor instructional content ... first-generation undergraduates closed nearly a third of the achievement gap",
                        paragraphRef = "Đoạn A",
                        explanationVi = "Đoạn A trình bày bằng chứng định lượng cho thấy lộ trình cá nhân hóa giúp thu hẹp khoảng cách thành tích."
                    )
                ),
                ReadingQuestion(
                    id = "ed_q4",
                    number = 4,
                    type = ReadingQuestionType.MATCHING_HEADING,
                    promptEn = "Choose the best heading for Paragraph C:",
                    options = listOf("i", "ii", "iii", "iv", "v"),
                    correctAnswer = "iv",
                    hint = ThreeLevelHint(
                        questionLabel = "Câu 4 • Matching Headings (Đoạn C)",
                        level1LocateVi = "Chú ý hai cụm khóa trong Đoạn C: 'substantial expenditure' và 'widespread classroom fatigue'.",
                        level2ParaphraseVi = "'hidden costs' ↔ 'underestimate ... substantial expenditure'; 'institutional resistance' ↔ 'widespread classroom fatigue' khi không tham vấn giảng viên.",
                        level3DistractorVi = "Tiêu đề v ('Balancing cognitive autonomy') thuộc về kết luận ở Đoạn D, không phải trọng tâm chi phí & phản ứng của Đoạn C."
                    ),
                    keywordMapping = KeywordMappingRow(
                        questionNumber = 4,
                        questionKeyword = "Institutional resistance and hidden costs (iv)",
                        passageParaphrase = "underestimate ... substantial expenditure + widespread classroom fatigue",
                        paragraphRef = "Đoạn C",
                        explanationVi = "Đoạn C nêu rõ chi phí ẩn cho đào tạo giảng viên, quản trị dữ liệu và sự mệt mỏi khi áp đặt phần mềm."
                    )
                ),
                ReadingQuestion(
                    id = "ed_q5",
                    number = 5,
                    type = ReadingQuestionType.GAP_FILL,
                    promptEn = "By handling routine practice, adaptive tools allow lecturers to dedicate seminar sessions to ______________. (NO MORE THAN TWO WORDS)",
                    correctAnswer = "critical inquiry",
                    acceptableAnswers = listOf("critical inquiry"),
                    hint = ThreeLevelHint(
                        questionLabel = "Câu 5 • Điền từ (≤ 2 từ)",
                        level1LocateVi = "Tìm cụm 'seminars' và 'faculty members' trong câu đầu tiên của Đoạn B.",
                        level2ParaphraseVi = "'routine practice' ↔ 'repetitive remediation'; 'allow lecturers to dedicate' ↔ 'free faculty members ... so seminars can focus on'.",
                        level3DistractorVi = "Chỉ điền tối đa 2 từ theo đúng yêu cầu đề bài; không chép thêm giới từ 'on' đứng trước."
                    ),
                    keywordMapping = KeywordMappingRow(
                        questionNumber = 5,
                        questionKeyword = "allow lecturers to dedicate seminar sessions to ___",
                        passageParaphrase = "free faculty members ... so seminars can focus on critical inquiry",
                        paragraphRef = "Đoạn B",
                        explanationVi = "Cụm danh từ đứng sau 'focus on' chính xác là 'critical inquiry' (2 từ)."
                    )
                ),
                ReadingQuestion(
                    id = "ed_q6",
                    number = 6,
                    type = ReadingQuestionType.GAP_FILL,
                    promptEn = "Sustainable educational transformation requires balancing algorithmic accuracy with empathetic ______________. (NO MORE THAN TWO WORDS)",
                    correctAnswer = "human guidance",
                    acceptableAnswers = listOf("human guidance"),
                    hint = ThreeLevelHint(
                        questionLabel = "Câu 6 • Điền từ (≤ 2 từ)",
                        level1LocateVi = "Đọc câu duy nhất của Đoạn D (từ khóa 'empathetic').",
                        level2ParaphraseVi = "'Sustainable educational transformation' ↔ 'enduring pedagogical reform'; 'algorithmic accuracy' ↔ 'algorithmic precision'.",
                        level3DistractorVi = "Từ 'empathetic' đã có sẵn trước chỗ trống, vì vậy chỉ điền 'human guidance' (không lặp lại 'empathetic')."
                    ),
                    keywordMapping = KeywordMappingRow(
                        questionNumber = 6,
                        questionKeyword = "algorithmic accuracy with empathetic ___",
                        passageParaphrase = "algorithmic precision and empathetic human guidance",
                        paragraphRef = "Đoạn D",
                        explanationVi = "Không lặp lại tính từ 'empathetic' đã cho sẵn → đáp án chuẩn là 'human guidance'."
                    )
                )
            )
        )

        val listening = ListeningPracticePack(
            moduleId = 1,
            sectionLabel = "Section 3 • Academic Seminar Discussion",
            titleEn = "Designing a Peer-Mentoring Pilot for First-Year Undergraduates",
            subtitleVi = "Hội thoại học thuật giữa Dr. Harrison và sinh viên Minh Anh về dự án cố vấn học tập",
            segments = listOf(
                ListeningSegment(
                    id = "ed_l1",
                    speaker = "Dr. Harrison",
                    textEn = "Good morning, Minh. Before we submit the ethics form for your peer-mentoring study, let's verify how you plan to recruit senior mentors."
                ),
                ListeningSegment(
                    id = "ed_l2",
                    speaker = "Minh Anh",
                    textEn = "Initially, I thought of selecting only top-scoring honours students, yet research suggests that mentors who once struggled academically build stronger trust.",
                    dictationPromptVi = "Chú ý nối âm 'thought of' và mệnh đề quan hệ 'who once struggled academically'."
                ),
                ListeningSegment(
                    id = "ed_l3",
                    speaker = "Dr. Harrison",
                    textEn = "That is an insightful pivot. Rather than relying solely on grade averages, you should evaluate candidates through structured empathy interviews.",
                    dictationPromptVi = "Chú ý cụm học thuật 'relying solely on' và danh từ ghép 'empathy interviews'."
                ),
                ListeningSegment(
                    id = "ed_l4",
                    speaker = "Minh Anh",
                    textEn = "Agreed. As for session frequency, holding biweekly check-ins during the first term prevents schedule burnout while sustaining steady motivation.",
                    dictationPromptVi = "Chú ý từ dễ nhầm 'biweekly check-ins' và 'schedule burnout'."
                ),
                ListeningSegment(
                    id = "ed_l5",
                    speaker = "Dr. Harrison",
                    textEn = "Make sure you exclude examination weeks from the observation window, otherwise anxiety spikes could distort your retention metrics."
                )
            ),
            dictationSentenceIds = listOf("ed_l2", "ed_l3", "ed_l4"),
            questions = listOf(
                ListeningQuestion(
                    id = "ed_lq1",
                    number = 1,
                    promptEn = "Why did Minh Anh decide NOT to recruit only top-scoring honours students as mentors?",
                    options = listOf(
                        "A. Honours students rarely have spare time in their timetable.",
                        "B. Mentors who overcame past academic difficulties foster greater trust.",
                        "C. The faculty committee rejected grade-based recruitment."
                    ),
                    correctAnswer = "B. Mentors who overcame past academic difficulties foster greater trust.",
                    hint = ThreeLevelHint(
                        questionLabel = "Listening Câu 1",
                        level1LocateVi = "Nghe kỹ lượt nói đầu tiên của Minh Anh (đoạn thoại số 2).",
                        level2ParaphraseVi = "'overcame past academic difficulties' ↔ 'once struggled academically'; 'foster greater trust' ↔ 'build stronger trust'.",
                        level3DistractorVi = "Bẫy Distractor: Minh Anh nhắc 'Initially, I thought of selecting only top-scoring...' nhưng ngay sau từ 'yet' đã đổi hướng."
                    ),
                    keywordMapping = KeywordMappingRow(
                        questionNumber = 1,
                        questionKeyword = "overcame past academic difficulties foster greater trust",
                        passageParaphrase = "mentors who once struggled academically build stronger trust",
                        paragraphRef = "Thoại #2",
                        explanationVi = "Từ nối chuyển hướng 'yet' đánh dấu lý do thực sự Minh Anh thay đổi tiêu chí tuyển chọn."
                    )
                ),
                ListeningQuestion(
                    id = "ed_lq2",
                    number = 2,
                    promptEn = "What does Dr. Harrison recommend leaving out of the data observation period?",
                    options = listOf(
                        "A. Orientation week workshops",
                        "B. Biweekly check-in meetings",
                        "C. Examination weeks"
                    ),
                    correctAnswer = "C. Examination weeks",
                    hint = ThreeLevelHint(
                        questionLabel = "Listening Câu 2",
                        level1LocateVi = "Tập trung vào câu kết cuối cùng của Dr. Harrison (đoạn thoại số 5).",
                        level2ParaphraseVi = "'leaving out' ↔ 'exclude'; 'data observation period' ↔ 'observation window'.",
                        level3DistractorVi = "Đừng nhầm 'biweekly check-ins' ở thoại số 4 — đó là lịch gặp được giữ lại, không phải phần bị loại bỏ."
                    ),
                    keywordMapping = KeywordMappingRow(
                        questionNumber = 2,
                        questionKeyword = "leaving out of the data observation period",
                        passageParaphrase = "exclude examination weeks from the observation window",
                        paragraphRef = "Thoại #5",
                        explanationVi = "Dr. Harrison cảnh báo tuần thi có mức lo âu cao đột biến sẽ làm méo số liệu duy trì học tập."
                    )
                )
            )
        )

        val speaking = SpeakingPracticePack(
            moduleId = 1,
            topicEn = "Education & Lifelong Learning",
            prompts = listOf(
                SpeakingPromptItem(
                    part = SpeakingPart.PART_1,
                    promptEn = "Do you prefer studying independently at home or collaborating in a study group? Why?",
                    strategyTipVi = "Công thức Band 8.5 Part 1: Trả lời trực diện → Nêu sắc thái điều kiện ('It largely hinges on...') → Ví dụ ngắn gọn với 1 collocation học thuật.",
                    sampleBand6Draft = "I usually prefer studying alone at home because it is quiet and I can focus better. However, sometimes when the assignment is difficult, studying with friends helps me understand faster."
                ),
                SpeakingPromptItem(
                    part = SpeakingPart.PART_2,
                    promptEn = "Describe a teacher or mentor who profoundly influenced your approach to learning.",
                    cueBulletsEn = listOf(
                        "Who this mentor was and how you met them",
                        "What subject or skill they taught you",
                        "How their teaching method differed from others",
                        "And explain why their guidance remains meaningful today"
                    ),
                    strategyTipVi = "Dùng 1 phút chuẩn bị ghi nhanh 4 cụm từ khóa (không viết cả câu). Mở đầu bằng một câu kể sinh động, chèn 1 cấu trúc đảo ngữ hoặc mệnh đề phân từ ở phần giải thích cảm xúc.",
                    sampleBand6Draft = "I would like to talk about my high school literature teacher, Ms. Lan. Instead of making us memorize essays, she encouraged us to debate real-life issues in class. Because of her, I became much more confident in expressing my opinions."
                ),
                SpeakingPromptItem(
                    part = SpeakingPart.PART_3,
                    promptEn = "Should universities prioritize practical vocational training over theoretical academic research?",
                    strategyTipVi = "Part 3 đòi hỏi góc nhìn đa chiều (Nuanced Balance): So sánh lợi ích ngắn hạn (employability) với giá trị dài hạn (intellectual innovation), tránh nói một chiều.",
                    sampleBand6Draft = "I think universities should focus more on practical skills because students need good jobs after graduation. Still, theoretical research is also important for long-term scientific progress."
                )
            )
        )

        val writing = WritingPracticePack(
            moduleId = 1,
            topicEn = "Education & Digital Pedagogy",
            prompts = listOf(
                WritingPromptItem(
                    taskType = WritingTaskType.TASK_1,
                    promptEn = "The chart compares the percentage of university students using four digital study modes in 2015 and 2025. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.",
                    chartTitleEn = "Share of University Learners Using Digital Study Modes (2015 vs 2025)",
                    chartData = listOf(
                        WritingDataPoint("Adaptive AI Apps", 14, 68),
                        WritingDataPoint("Recorded Lectures", 42, 79),
                        WritingDataPoint("Digital Textbooks", 55, 74),
                        WritingDataPoint("Print-Only Study", 64, 21)
                    ),
                    outlineSlots = listOf(
                        WritingOutlineSlot(
                            stepTitleVi = "1. Paraphrase đề bài (Introduction)",
                            guideVi = "Đổi cấu trúc động từ & mốc thời gian trong 1 câu súc tích.",
                            starterFrameEn = "The bar chart illustrates shifts in the proportion of university students adopting four distinct study formats between 2015 and 2025."
                        ),
                        WritingOutlineSlot(
                            stepTitleVi = "2. Tổng quan xu hướng chính (Overview)",
                            guideVi = "Không đưa số liệu chi tiết; nêu rõ nhóm tăng mạnh nhất & nhóm sụt giảm mạnh nhất.",
                            starterFrameEn = "Overall, digital modalities experienced substantial expansion—most notably adaptive AI applications—whereas reliance on print-only materials declined precipitously."
                        ),
                        WritingOutlineSlot(
                            stepTitleVi = "3. Thân bài 1 & 2 (Key Comparisons)",
                            guideVi = "Nhóm các hạng mục tăng vượt bậc (AI Apps tăng gần 5 lần) và so sánh với nhóm truyền thống.",
                            starterFrameEn = "Specifically, usage of adaptive AI apps surged almost fivefold from 14% to 68%, while print-only study plummeted by more than two-thirds to just 21%."
                        )
                    ),
                    sampleStarterDraft = "The bar chart illustrates shifts in the proportion of university students adopting four distinct study formats between 2015 and 2025. Overall, digital study methods grew dramatically, whereas print-only study fell sharply. In 2015, print-only study was the highest at 64%, but by 2025 it dropped to 21%. By contrast, adaptive AI apps increased from 14% to 68%."
                ),
                WritingPromptItem(
                    taskType = WritingTaskType.TASK_2,
                    promptEn = "Some educators argue that AI tutoring tools undermine students' independent critical thinking, while others believe they democratise high-quality education. Discuss both views and give your own opinion.",
                    outlineSlots = listOf(
                        WritingOutlineSlot(
                            stepTitleVi = "1. Mở bài & Quan điểm rõ ràng (Thesis)",
                            guideVi = "Nêu hai luồng ý kiến và lập trường cân bằng nhưng dứt khoát.",
                            starterFrameEn = "Although excessive dependence on automated tutors can dull analytical rigour, I contend that thoughtfully integrated AI platforms vastly broaden equitable access to personalised instruction."
                        ),
                        WritingOutlineSlot(
                            stepTitleVi = "2. Thân bài 1 — Mặt trái (Cognitive Offloading)",
                            guideVi = "Phân tích nguy cơ lệ thuộc gợi ý sẵn → giảm sức bền tư duy phản biện.",
                            starterFrameEn = "Admittedly, when learners delegate brainstorming and problem-solving entirely to algorithms, they risk experiencing cognitive atrophy rather than developing intellectual autonomy."
                        ),
                        WritingOutlineSlot(
                            stepTitleVi = "3. Thân bài 2 — Lợi ích bình đẳng giáo dục + Giải pháp",
                            guideVi = "Nhấn mạnh học sinh vùng xa tiếp cận phản hồi tức thì & vai trò định hướng của giảng viên.",
                            starterFrameEn = "Nevertheless, for underserved communities lacking specialist teachers, adaptive tutors provide immediate scaffolding that narrows socioeconomic attainment gaps."
                        )
                    ),
                    sampleStarterDraft = "Nowadays, artificial intelligence tutoring tools are becoming very popular in schools. Some people worry that students will stop thinking critically if they rely too much on AI. On the other hand, many believe these tools make good education affordable for everyone. In my opinion, AI tutors bring more benefits as long as teachers guide students to verify answers independently."
                )
            )
        )

        val vocabHints = listOf(
            Band85VocabHint(
                phraseEn = "pedagogical scaffolding",
                categoryVi = "Academic Collocation",
                meaningVi = "Sự hỗ trợ học tập có cấu trúc từng bước rồi giảm dần khi học viên tự chủ",
                exampleEn = "Adaptive tools deliver timely pedagogical scaffolding for struggling learners."
            ),
            Band85VocabHint(
                phraseEn = "narrow socioeconomic attainment gaps",
                categoryVi = "Academic Collocation",
                meaningVi = "Thu hẹp khoảng cách thành tích học tập do chênh lệch kinh tế - xã hội",
                exampleEn = "Public scholarship funds help narrow socioeconomic attainment gaps."
            ),
            Band85VocabHint(
                phraseEn = "strike a delicate equilibrium between",
                categoryVi = "Idiomatic / Band 8.5 Expression",
                meaningVi = "Đạt được sự cân bằng tinh tế giữa hai yếu tố đối lập",
                exampleEn = "Universities must strike a delicate equilibrium between automation and mentorship."
            ),
            Band85VocabHint(
                phraseEn = "a double-edged sword",
                categoryVi = "Natural Speaking Idiom",
                meaningVi = "Con dao hai lưỡi (vừa có lợi vừa có hại)",
                exampleEn = "Honestly, AI tutoring is a double-edged sword if students skip deep thinking."
            ),
            Band85VocabHint(
                phraseEn = "cognitive atrophy",
                categoryVi = "C2 Lexical Precision",
                meaningVi = "Sự mai một khả năng tư duy do ít rèn luyện chủ động",
                exampleEn = "Passive reliance on instant answers can trigger cognitive atrophy."
            ),
            Band85VocabHint(
                phraseEn = "Granted that..., it remains imperative to...",
                categoryVi = "Band 8.5+ Discourse Frame",
                meaningVi = "Thừa nhận rằng..., nhưng vẫn tối quan trọng phải...",
                exampleEn = "Granted that software scales rapidly, it remains imperative to train educators."
            )
        )

        return ModulePracticeBundle(
            moduleId = 1,
            reading = reading,
            listening = listening,
            speaking = speaking,
            writing = writing,
            band85VocabHints = vocabHints
        )
    }

    private fun environmentBundle(): ModulePracticeBundle {
        val reading = ReadingPracticePack(
            moduleId = 5,
            titleEn = "Urban Sponge Cities: Engineering Resilient Metropolitan Watersheds",
            subtitleVi = "Đọc hiểu học thuật • Đô thị bọt biển & Ứng phó biến đổi khí hậu",
            estimatedMinutes = 12,
            headingsBank = listOf(
                "i. Limitations of conventional concrete drainage networks",
                "ii. How permeable urban landscapes absorb extreme rainfall",
                "iii. Why coastal tourism boosts wetland conservation budgets",
                "iv. Financial hurdles and governance fragmentation in retrofitting cities",
                "v. Co-benefits for urban cooling and public wellbeing"
            ),
            sentences = listOf(
                PassageSentence(
                    id = "env_s1",
                    paragraphLabel = "A",
                    textEn = "Confronted with intensifying monsoon downpours, megacities across Asia have realised that impermeable concrete drains frequently fail under peak storm surges.",
                    isComplexTarget = true
                ),
                PassageSentence(
                    id = "env_s2",
                    paragraphLabel = "A",
                    textEn = "Because asphalt surfaces prevent rainwater from infiltrating the soil, runoff accelerates into low-lying districts, overwhelming ageing underground pipes within minutes.",
                    isComplexTarget = true
                ),
                PassageSentence(
                    id = "env_s3",
                    paragraphLabel = "B",
                    textEn = "In contrast, the 'sponge city' blueprint replaces sealed pavements with bioswales, sunken parks, and porous bricks capable of retaining up to seventy percent of storm precipitation on site.",
                    isComplexTarget = true
                ),
                PassageSentence(
                    id = "env_s4",
                    paragraphLabel = "C",
                    textEn = "Seldom do municipal planners encounter a single infrastructure investment that simultaneously curtails flood damage and lowers summer street temperatures by nearly three degrees.",
                    isComplexTarget = true
                ),
                PassageSentence(
                    id = "env_s5",
                    paragraphLabel = "D",
                    textEn = "Yet retrofitting densely populated quarters remains financially daunting, particularly when transport, housing, and water agencies operate under competing budgets.",
                    isComplexTarget = true
                )
            ),
            questions = listOf(
                ReadingQuestion(
                    id = "env_q1",
                    number = 1,
                    type = ReadingQuestionType.TFNG,
                    promptEn = "Conventional concrete drainage systems reliably prevent flooding during extreme monsoon storms.",
                    options = listOf("TRUE", "FALSE", "NOT GIVEN"),
                    correctAnswer = "FALSE",
                    hint = ThreeLevelHint(
                        questionLabel = "Câu 1 • True / False / Not Given",
                        level1LocateVi = "Xem ngay câu đầu tiên của Đoạn A ('impermeable concrete drains').",
                        level2ParaphraseVi = "'reliably prevent flooding' trái ngược hoàn toàn với 'frequently fail under peak storm surges'.",
                        level3DistractorVi = "Chú ý động từ phủ định ý nghĩa 'fail' và 'overwhelming ageing underground pipes' → Chọn FALSE."
                    ),
                    keywordMapping = KeywordMappingRow(
                        questionNumber = 1,
                        questionKeyword = "reliably prevent flooding during extreme storms",
                        passageParaphrase = "frequently fail under peak storm surges",
                        paragraphRef = "Đoạn A",
                        explanationVi = "Hệ thống cống bê tông truyền thống thường xuyên quá tải trong mưa bão cực đoan → FALSE."
                    )
                ),
                ReadingQuestion(
                    id = "env_q2",
                    number = 2,
                    type = ReadingQuestionType.MATCHING_HEADING,
                    promptEn = "Choose the best heading for Paragraph B:",
                    options = listOf("i", "ii", "iii", "iv", "v"),
                    correctAnswer = "ii",
                    hint = ThreeLevelHint(
                        questionLabel = "Câu 2 • Matching Headings (Đoạn B)",
                        level1LocateVi = "Đọc Đoạn B với các vật liệu 'bioswales, sunken parks, porous bricks'.",
                        level2ParaphraseVi = "'permeable urban landscapes' ↔ 'porous bricks, bioswales, sunken parks'; 'absorb extreme rainfall' ↔ 'retaining up to 70% of storm precipitation'.",
                        level3DistractorVi = "Đừng nhầm với tiêu đề v ('urban cooling') — lợi ích giảm nhiệt được nói ở Đoạn C."
                    ),
                    keywordMapping = KeywordMappingRow(
                        questionNumber = 2,
                        questionKeyword = "permeable urban landscapes absorb extreme rainfall (ii)",
                        passageParaphrase = "bioswales, sunken parks, porous bricks ... retaining up to 70% of storm precipitation",
                        paragraphRef = "Đoạn B",
                        explanationVi = "Đoạn B giải thích cơ chế hấp thụ nước mưa của mô hình đô thị bọt biển."
                    )
                ),
                ReadingQuestion(
                    id = "env_q3",
                    number = 3,
                    type = ReadingQuestionType.GAP_FILL,
                    promptEn = "Besides reducing flood losses, sponge infrastructure can reduce summer street temperatures by almost ______________. (NO MORE THAN TWO WORDS)",
                    correctAnswer = "three degrees",
                    acceptableAnswers = listOf("three degrees", "3 degrees"),
                    hint = ThreeLevelHint(
                        questionLabel = "Câu 3 • Điền từ (≤ 2 từ)",
                        level1LocateVi = "Tìm câu đảo ngữ bắt đầu bằng 'Seldom' ở Đoạn C.",
                        level2ParaphraseVi = "'almost' ↔ 'nearly'; 'reduce summer street temperatures' ↔ 'lowers summer street temperatures'.",
                        level3DistractorVi = "Từ 'almost' đã có sẵn trước chỗ trống, chỉ điền 'three degrees'."
                    ),
                    keywordMapping = KeywordMappingRow(
                        questionNumber = 3,
                        questionKeyword = "reduce summer street temperatures by almost ___",
                        passageParaphrase = "lowers summer street temperatures by nearly three degrees",
                        paragraphRef = "Đoạn C",
                        explanationVi = "Cụm số đo sau 'nearly' là 'three degrees'."
                    )
                )
            )
        )

        val listening = ListeningPracticePack(
            moduleId = 5,
            sectionLabel = "Section 4 • Environmental Engineering Lecture",
            titleEn = "Restoring Mangrove Belts as Natural Coastal Defences",
            subtitleVi = "Bài giảng học thuật về phục hồi rừng ngập mặn chống xói lở bờ biển",
            segments = listOf(
                ListeningSegment(
                    id = "env_l1",
                    speaker = "Prof. Bennett",
                    textEn = "Today we examine why coastal planners are shifting away from rigid concrete seawalls toward living mangrove ecosystems."
                ),
                ListeningSegment(
                    id = "env_l2",
                    speaker = "Prof. Bennett",
                    textEn = "Interlocking mangrove roots dissipate up to two-thirds of incoming wave energy while trapping mineral sediment that gradually raises the shoreline.",
                    dictationPromptVi = "Chú ý động từ học thuật 'dissipate' và cụm 'incoming wave energy'."
                ),
                ListeningSegment(
                    id = "env_l3",
                    speaker = "Prof. Bennett",
                    textEn = "Crucially, planting saplings without restoring natural tidal hydrology frequently ends in failure within eighteen months.",
                    dictationPromptVi = "Chú ý cụm 'natural tidal hydrology' và 'within eighteen months'."
                ),
                ListeningSegment(
                    id = "env_l4",
                    speaker = "Prof. Bennett",
                    textEn = "When local fishing cooperatives co-manage these wetlands, both biodiversity and household incomes rebound remarkably quickly."
                )
            ),
            dictationSentenceIds = listOf("env_l2", "env_l3"),
            questions = listOf(
                ListeningQuestion(
                    id = "env_lq1",
                    number = 1,
                    promptEn = "According to the lecturer, why do many mangrove planting projects fail within 18 months?",
                    options = listOf(
                        "A. Local fishing communities harvest the young trees.",
                        "B. Planners neglect to restore natural tidal water flow.",
                        "C. Seawalls block sunlight from reaching the saplings."
                    ),
                    correctAnswer = "B. Planners neglect to restore natural tidal water flow.",
                    hint = ThreeLevelHint(
                        questionLabel = "Listening Câu 1",
                        level1LocateVi = "Tập trung câu bắt đầu bằng 'Crucially' (đoạn thoại số 3).",
                        level2ParaphraseVi = "'neglect to restore natural tidal water flow' ↔ 'without restoring natural tidal hydrology'.",
                        level3DistractorVi = "Đáp án A sai vì hợp tác xã ngư dân giúp phục hồi hệ sinh thái chứ không phá hủy."
                    ),
                    keywordMapping = KeywordMappingRow(
                        questionNumber = 1,
                        questionKeyword = "neglect to restore natural tidal water flow",
                        passageParaphrase = "without restoring natural tidal hydrology",
                        paragraphRef = "Đoạn giảng #3",
                        explanationVi = "Nếu không khôi phục thủy văn thủy triều tự nhiên, cây giống thường chết trong 18 tháng."
                    )
                )
            )
        )

        val speaking = SpeakingPracticePack(
            moduleId = 5,
            topicEn = "Environment & Climate Resilience",
            prompts = listOf(
                SpeakingPromptItem(
                    part = SpeakingPart.PART_1,
                    promptEn = "Are there many green spaces or parks near where you live?",
                    strategyTipVi = "Mở rộng ý bằng cảm nhận thực tế + đối chiếu với tốc độ đô thị hóa ('rapid urban sprawl').",
                    sampleBand6Draft = "Yes, there is a small park near my apartment where people walk every evening. However, I wish the city had more trees because the air feels quite polluted during rush hour."
                ),
                SpeakingPromptItem(
                    part = SpeakingPart.PART_2,
                    promptEn = "Describe an environmental initiative in your city or country that impressed you.",
                    cueBulletsEn = listOf(
                        "What the initiative is and who launched it",
                        "What environmental issue it addresses",
                        "How local residents participate in it",
                        "And explain why you believe it is effective"
                    ),
                    strategyTipVi = "Ghi chú nhanh 3 cụm từ Band 8.5 trong 1 phút chuẩn bị: 'grassroots initiative', 'curb single-use plastics', 'tangible impact'.",
                    sampleBand6Draft = "I want to describe a community tree-planting campaign in my city. Young volunteers collect donations and plant native trees along canals to reduce flooding and heat. I think it works well because residents feel proud of their neighborhood."
                ),
                SpeakingPromptItem(
                    part = SpeakingPart.PART_3,
                    promptEn = "Should governments impose heavy carbon taxes on corporations, or offer subsidies for clean energy instead?",
                    strategyTipVi = "Dùng phép ẩn dụ 'carrot-and-stick approach' (kết hợp thưởng và phạt) để ghi điểm Lexical Resource Band 8.5.",
                    sampleBand6Draft = "I think governments need both policies. Carbon taxes force big companies to reduce pollution immediately, while subsidies help green startups grow faster."
                )
            )
        )

        val writing = WritingPracticePack(
            moduleId = 5,
            topicEn = "Climate Mitigation & Urban Sustainability",
            prompts = listOf(
                WritingPromptItem(
                    taskType = WritingTaskType.TASK_1,
                    promptEn = "The chart compares renewable energy shares across four sectors in 2015 and 2025. Summarise the main features and make relevant comparisons.",
                    chartTitleEn = "Renewable Energy Adoption by Sector (2015 vs 2025)",
                    chartData = listOf(
                        WritingDataPoint("Electricity Grid", 24, 62),
                        WritingDataPoint("Public Transit", 12, 48),
                        WritingDataPoint("Residential Heat", 18, 39),
                        WritingDataPoint("Heavy Industry", 7, 21)
                    ),
                    outlineSlots = listOf(
                        WritingOutlineSlot(
                            stepTitleVi = "1. Introduction",
                            guideVi = "Paraphrase chủ thể & giai đoạn 10 năm.",
                            starterFrameEn = "The bar chart compares the proportion of energy derived from renewable sources across four economic sectors in 2015 and 2025."
                        ),
                        WritingOutlineSlot(
                            stepTitleVi = "2. Overview",
                            guideVi = "Chỉ ra xu hướng tăng đồng loạt, dẫn đầu là lưới điện và tốc độ tăng gấp 4 của giao thông công cộng.",
                            starterFrameEn = "Overall, every sector recorded marked decarbonisation over the decade, with the electricity grid maintaining the highest share and public transit quadrupling its adoption."
                        )
                    ),
                    sampleStarterDraft = "The bar chart compares renewable energy use across four sectors in 2015 and 2025. Overall, all four sectors increased significantly, led by the electricity grid."
                ),
                WritingPromptItem(
                    taskType = WritingTaskType.TASK_2,
                    promptEn = "Individual lifestyle changes are too insignificant to halt climate change; only strict international legislation can make a real difference. To what extent do you agree or disagree?",
                    outlineSlots = listOf(
                        WritingOutlineSlot(
                            stepTitleVi = "1. Thesis Statement",
                            guideVi = "Công nhận vai trò trụ cột của luật pháp nhưng khẳng định hành động cá nhân tạo áp lực thị trường.",
                            starterFrameEn = "While binding international treaties are indispensable for curbing industrial emissions at scale, dismissing individual behavioural shifts overlooks how consumer demand reshapes corporate priorities."
                        ),
                        WritingOutlineSlot(
                            stepTitleVi = "2. Body 1 — Sức mạnh của khung pháp lý",
                            guideVi = "Nêu quy mô phát thải từ năng lượng & sản xuất nặng cần thuế carbon và tiêu chuẩn bắt buộc.",
                            starterFrameEn = "Undeniably, decarbonising power grids and heavy manufacturing requires enforceable emissions caps that individual households cannot achieve alone."
                        ),
                        WritingOutlineSlot(
                            stepTitleVi = "3. Body 2 — Hiệu ứng cộng hưởng từ công dân",
                            guideVi = "Thói quen tiêu dùng xanh tạo động lực thương mại và hậu thuẫn chính trị cho chính sách khí hậu.",
                            starterFrameEn = "Nevertheless, collective civic choices send powerful market signals and build the electoral mandate politicians need to enact ambitious climate legislation."
                        )
                    ),
                    sampleStarterDraft = "Climate change is one of the most urgent problems today. Although strict international laws are clearly needed to control big polluters, I partially disagree that individual actions are meaningless because millions of consumers can push companies to adopt greener practices."
                )
            )
        )

        val vocabHints = listOf(
            Band85VocabHint(
                phraseEn = "mitigate catastrophic climate impacts",
                categoryVi = "Academic Collocation",
                meaningVi = "Giảm thiểu những tác động thảm khốc của biến đổi khí hậu",
                exampleEn = "Coordinated policy action is vital to mitigate catastrophic climate impacts."
            ),
            Band85VocabHint(
                phraseEn = "a carrot-and-stick approach",
                categoryVi = "Band 8.5 Idiom",
                meaningVi = "Chiến lược kết hợp khuyến khích (thưởng) và chế tài (phạt)",
                exampleEn = "Regulators should adopt a carrot-and-stick approach combining subsidies with carbon levies."
            ),
            Band85VocabHint(
                phraseEn = "irreversible ecological degradation",
                categoryVi = "C2 Academic Phrase",
                meaningVi = "Sự suy thoái sinh thái không thể đảo ngược",
                exampleEn = "Unchecked deforestation accelerates irreversible ecological degradation."
            ),
            Band85VocabHint(
                phraseEn = "urban heat-island effect",
                categoryVi = "Topic-Specific Term",
                meaningVi = "Hiệu ứng đảo nhiệt đô thị",
                exampleEn = "Sponge parks visibly alleviate the urban heat-island effect in summer."
            ),
            Band85VocabHint(
                phraseEn = "Only by coupling... can societies...",
                categoryVi = "Band 8.5 Inversion Frame",
                meaningVi = "Chỉ bằng cách kết hợp... xã hội mới có thể...",
                exampleEn = "Only by coupling strict regulation with civic awareness can societies achieve net-zero."
            )
        )

        return ModulePracticeBundle(
            moduleId = 5,
            reading = reading,
            listening = listening,
            speaking = speaking,
            writing = writing,
            band85VocabHints = vocabHints
        )
    }

    private fun dynamicModuleBundle(module: IeltsModule): ModulePracticeBundle {
        val base = if (module.id % 2 == 1) educationBundle() else environmentBundle()
        val topicEn = module.titleEn
        val firstCollocation = module.keyCollocations.firstOrNull() ?: "evidence-based policy"
        val secondCollocation = module.keyCollocations.getOrNull(1) ?: "sustainable transformation"

        val tailoredVocab = module.keyCollocations.map { collocation ->
            Band85VocabHint(
                phraseEn = collocation,
                categoryVi = "Module #${module.id} Core C1–C2",
                meaningVi = "Cụm từ học thuật trọng tâm chủ đề ${module.titleVi}",
                exampleEn = "Policymakers studying $topicEn increasingly emphasise $collocation."
            )
        } + base.band85VocabHints.take(3)

        val tailoredReading = base.reading.copy(
            moduleId = module.id,
            subtitleVi = "Đồng bộ Module #${module.id}: ${module.titleVi} • ${base.reading.subtitleVi}"
        )

        val tailoredSpeaking = base.speaking.copy(
            moduleId = module.id,
            topicEn = topicEn,
            prompts = listOf(
                SpeakingPromptItem(
                    part = SpeakingPart.PART_1,
                    promptEn = "How has $topicEn influenced daily routines in your community over recent years?",
                    strategyTipVi = "Lồng ghép tự nhiên cụm \"$firstCollocation\" và mở rộng bằng so sánh quá khứ – hiện tại.",
                    sampleBand6Draft = "In recent years, $topicEn has changed how people live and work in my city. Many families now rely on modern solutions because they save time and improve convenience."
                ),
                SpeakingPromptItem(
                    part = SpeakingPart.PART_2,
                    promptEn = "Describe an important trend or development related to $topicEn that you have noticed.",
                    cueBulletsEn = listOf(
                        "What this development is and where you observed it",
                        "Who benefits most from it",
                        "What challenges or debates surround it",
                        "And explain how you expect it to evolve in the next decade"
                    ),
                    strategyTipVi = "Chuẩn bị trong 60 giây với cấu trúc: Bối cảnh → Tác động cụ thể → Góc nhìn phản biện → Dự báo tương lai.",
                    sampleBand6Draft = "A notable trend in $topicEn is how rapidly institutions are adopting new methods. People appreciate the efficiency, although some critics worry about unequal access."
                ),
                SpeakingPromptItem(
                    part = SpeakingPart.PART_3,
                    promptEn = "To what extent should governments regulate innovations in $topicEn rather than leaving them to market forces?",
                    strategyTipVi = "Thể hiện tư duy phản biện Band 8.5 bằng cấu trúc nhượng bộ ('Admittedly..., yet without oversight...').",
                    sampleBand6Draft = "I believe government oversight is essential in $topicEn because private companies mainly prioritize profit, whereas regulators protect public welfare."
                )
            )
        )

        val tailoredWriting = base.writing.copy(
            moduleId = module.id,
            topicEn = topicEn,
            prompts = listOf(
                base.writing.prompts.first(),
                WritingPromptItem(
                    taskType = WritingTaskType.TASK_2,
                    promptEn = "Rapid developments in $topicEn have brought unprecedented convenience, yet many fear they deepen social inequalities. Discuss both perspectives and state your position.",
                    outlineSlots = listOf(
                        WritingOutlineSlot(
                            stepTitleVi = "1. Thesis Statement",
                            guideVi = "Khẳng định giá trị tiến bộ đồng thời nhấn mạnh yêu cầu quản trị công bằng.",
                            starterFrameEn = "While rapid advances in $topicEn undeniably unlock remarkable efficiency, I argue that proactive public safeguards are vital to prevent widening disparities."
                        ),
                        WritingOutlineSlot(
                            stepTitleVi = "2. Body 1 — Lợi ích cốt lõi",
                            guideVi = "Tận dụng collocation \"$firstCollocation\" để phân tích lợi ích.",
                            starterFrameEn = "On the one hand, proponents highlight how $firstCollocation streamlines essential services and raises overall living standards."
                        ),
                        WritingOutlineSlot(
                            stepTitleVi = "3. Body 2 — Rủi ro bất bình đẳng & Hướng giải quyết",
                            guideVi = "Kết nối với \"$secondCollocation\" để đề xuất giải pháp cân bằng.",
                            starterFrameEn = "On the other hand, without deliberate investment in $secondCollocation, vulnerable groups risk being left behind."
                        )
                    ),
                    sampleStarterDraft = "Recent breakthroughs in $topicEn have transformed modern life. Although some observers warn that these changes widen social gaps, I believe well-designed public policies can ensure the benefits reach everyone."
                )
            )
        )

        return ModulePracticeBundle(
            moduleId = module.id,
            reading = tailoredReading,
            listening = base.listening.copy(moduleId = module.id),
            speaking = tailoredSpeaking,
            writing = tailoredWriting,
            band85VocabHints = tailoredVocab
        )
    }
}
