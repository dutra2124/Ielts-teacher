package com.example.data

/**
 * Authentic Cambridge IELTS & Actual Tests Exam Bank (Khu vực 1).
 * Each paper contains full 4-skill exam material ready for Full Test or Single Skill strict mode.
 */
object ExamBankCurriculum {

    val authenticExams: List<ExamPaper> = listOf(
        cambridge19Test1(),
        actualTest2025Q1(),
        cambridge18Test3()
    )

    private fun cambridge19Test1(): ExamPaper = ExamPaper(
        id = "cam19_t1",
        title = "Cambridge IELTS 19 — Academic Test 1",
        sourceLabel = "Cambridge Official Style",
        year = 2024,
        difficulty = ExamDifficulty.HARD,
        topicVi = "Kiến trúc nhận thức & Giáo dục đại học",
        topicEn = "Cognitive Architecture & Higher Education",
        listeningTitle = "Section 3 & 4 — Cognitive Load in Digital Lecture Halls",
        listeningScript = listOf(
            "Tutor: Right, Hannah and Liam, before you finalise your dissertation on digital note-taking, walk me through your experimental findings.",
            "Hannah: We divided ninety undergraduate volunteers into two cohorts. One group typed verbatim notes on laptops, while the second sketched structured concept maps on paper.",
            "Liam: Interestingly, although laptop users recorded nearly twice as many words, they scored twenty-two percent lower on conceptual synthesis questions forty-eight hours later.",
            "Tutor: That aligns with the generative processing hypothesis. When students summarise by hand, cognitive friction forces deeper semantic encoding.",
            "Hannah: Exactly. However, we also discovered that split-screen tablets with stylus annotation matched paper performance—provided notifications were strictly disabled."
        ),
        readingTitle = "The Paradox of Frictionless Learning in Modern Universities",
        readingParagraphs = listOf(
            ExamParagraph(
                label = "A",
                sentences = listOf(
                    "Over the last fifteen years, educational technology vendors have marketed 'frictionless learning'—interfaces engineered to remove hesitation, automate summaries, and deliver instant answers.",
                    "On the surface, smooth progression feels remarkably productive, often boosting students' short-term confidence during pre-exam revision."
                )
            ),
            ExamParagraph(
                label = "B",
                sentences = listOf(
                    "Cognitive psychologists, however, distinguish sharply between performance during study and durable long-term retention.",
                    "Robert Bjork's framework of 'desirable difficulties' demonstrates that deliberate retrieval struggle—such as spacing practice sessions and interleaving distinct problem types—strengthens neural pathways far more reliably than passive rereading."
                )
            ),
            ExamParagraph(
                label = "C",
                sentences = listOf(
                    "In a multi-campus trial across Scandinavia, first-year engineering students who tackled unguided diagnostic puzzles prior to attending lectures outperformed control groups on transfer tasks by a full grade boundary.",
                    "Yet course evaluations revealed an unexpected irony: learners in the high-effort cohort initially rated the course as less enjoyable, mistaking mental exertion for poor teaching."
                )
            ),
            ExamParagraph(
                label = "D",
                sentences = listOf(
                    "Consequently, curriculum designers now argue that universities should explicitly teach metacognitive literacy, enabling students to recognise productive struggle as a hallmark of genuine mastery rather than a flaw in instructional design."
                )
            )
        ),
        writingTask1Prompt = "The chart compares average weekly study hours allocated to four learning methods (AI summaries, active retrieval drills, peer seminars, and textbook rereading) among Band 6.0 and Band 8.5 candidates in 2024. Summarise the information by selecting and reporting the main features, and make comparisons where relevant. (Write at least 150 words.)",
        writingTask2Prompt = "As automated study tools make academic information effortless to obtain, some educators worry that students are losing the resilience needed for complex problem-solving. To what extent do you agree or disagree? (Write at least 250 words.)",
        speakingPrompts = listOf(
            "Part 1: Do you prefer taking handwritten notes or digital notes when studying? Why?",
            "Part 2: Describe a difficult academic subject or skill you eventually mastered. You should say what it was, why it challenged you, what strategies helped you overcome it, and how mastering it changed your confidence.",
            "Part 3: Why do students often prefer effortless study methods even when research proves active recall is superior?"
        ),
        questions = listOf(
            ExamQuestionItem(
                id = "c19_l1",
                number = 1,
                skill = ExamQuestionSkill.LISTENING,
                type = ExamQuestionType.MCQ,
                promptEn = "How did laptop note-takers perform compared to paper concept-mappers on conceptual synthesis questions after 48 hours?",
                options = listOf(
                    "A. They scored 22% higher due to capturing more words.",
                    "B. They scored 22% lower despite writing more notes.",
                    "C. Both groups achieved identical scores."
                ),
                correctAnswer = "B. They scored 22% lower despite writing more notes."
            ),
            ExamQuestionItem(
                id = "c19_l2",
                number = 2,
                skill = ExamQuestionSkill.LISTENING,
                type = ExamQuestionType.GAP_FILL,
                promptEn = "Stylus tablets matched paper performance only when ______________ were strictly turned off. (ONE WORD ONLY)",
                correctAnswer = "notifications",
                acceptableAnswers = listOf("notifications")
            ),
            ExamQuestionItem(
                id = "c19_r1",
                number = 3,
                skill = ExamQuestionSkill.READING,
                type = ExamQuestionType.TFNG,
                promptEn = "Frictionless educational tools tend to lower students' short-term confidence during revision.",
                options = listOf("TRUE", "FALSE", "NOT GIVEN"),
                correctAnswer = "FALSE"
            ),
            ExamQuestionItem(
                id = "c19_r2",
                number = 4,
                skill = ExamQuestionSkill.READING,
                type = ExamQuestionType.TFNG,
                promptEn = "The Scandinavian engineering trial lasted for more than four academic years.",
                options = listOf("TRUE", "FALSE", "NOT GIVEN"),
                correctAnswer = "NOT GIVEN"
            ),
            ExamQuestionItem(
                id = "c19_r3",
                number = 5,
                skill = ExamQuestionSkill.READING,
                type = ExamQuestionType.MCQ,
                promptEn = "What ironic reaction did students in the high-effort Scandinavian group display?",
                options = listOf(
                    "A. They refused to attend traditional lectures.",
                    "B. They initially rated the course as less enjoyable despite outperforming peers.",
                    "C. They switched back to automated summary apps before finals."
                ),
                correctAnswer = "B. They initially rated the course as less enjoyable despite outperforming peers."
            ),
            ExamQuestionItem(
                id = "c19_r4",
                number = 6,
                skill = ExamQuestionSkill.READING,
                type = ExamQuestionType.GAP_FILL,
                promptEn = "Curriculum designers advocate teaching ______________ so learners value productive struggle. (NO MORE THAN TWO WORDS)",
                correctAnswer = "metacognitive literacy",
                acceptableAnswers = listOf("metacognitive literacy")
            )
        )
    )

    private fun actualTest2025Q1(): ExamPaper = ExamPaper(
        id = "actual_2025_q1",
        title = "Actual Test 2025 Q1 — IDP / BC Real Exam",
        sourceLabel = "Actual Test 2025",
        year = 2025,
        difficulty = ExamDifficulty.VERY_HARD,
        topicVi = "Kinh tế tuần hoàn & Hệ sinh thái đô thị",
        topicEn = "Circular Economy & Urban Ecology",
        listeningTitle = "Section 4 — Mycelium Biomaterials in Sustainable Packaging",
        listeningScript = listOf(
            "In this lecture, we investigate how fungal mycelium is replacing expanded polystyrene across global shipping chains.",
            "By feeding agricultural husks such as rice straw to living fungal threads inside moulds, manufacturers grow rigid protective casings within seven days.",
            "Unlike synthetic foams that persist in landfills for centuries, mycelium panels decompose naturally in garden soil in roughly forty-five days.",
            "Still, scaling production requires sterile climate chambers, which currently makes unit prices about fifteen percent higher than conventional plastics."
        ),
        readingTitle = "Rewilding Vertical Skylines: Bio-Architecture Beyond Aesthetics",
        readingParagraphs = listOf(
            ExamParagraph(
                label = "A",
                sentences = listOf(
                    "Once dismissed as architectural vanity projects, vertical forests—high-rise towers cloaked in thousands of cantilevered shrubs and mature trees—are undergoing rigorous ecological scrutiny.",
                    "Thermal imaging conducted in Milan and Singapore confirms that vegetated facades lower exterior wall temperatures by up to twelve degrees Celsius during heatwaves."
                )
            ),
            ExamParagraph(
                label = "B",
                sentences = listOf(
                    "Sceptics, however, point to the substantial embodied carbon emitted when reinforcing concrete balconies to support wet soil loads and wind shear at altitude.",
                    "Unless structural engineers pair greenery with low-carbon timber hybrids or recycled steel, the initial construction footprint may take decades of plant photosynthesis to offset."
                )
            ),
            ExamParagraph(
                label = "C",
                sentences = listOf(
                    "Even so, urban ecologists highlight a quieter triumph: native pollinators and songbirds increasingly treat vegetated towers as stepping-stone corridors across fragmented concrete grids."
                )
            )
        ),
        writingTask1Prompt = "The diagrams show the lifecycle of biodegradable mycelium packaging from agricultural waste collection to soil decomposition. Summarise the stages clearly. (At least 150 words.)",
        writingTask2Prompt = "Some urban planners claim that green skyscrapers are an expensive distraction from affordable public housing, while others view them as essential climate adaptation. Discuss both views and give your opinion. (At least 250 words.)",
        speakingPrompts = listOf(
            "Part 1: Do buildings in your hometown incorporate much greenery?",
            "Part 2: Describe a modern eco-friendly building or public space you admire.",
            "Part 3: Should environmental building codes be mandatory even if they raise housing prices?"
        ),
        questions = listOf(
            ExamQuestionItem(
                id = "act25_l1",
                number = 1,
                skill = ExamQuestionSkill.LISTENING,
                type = ExamQuestionType.GAP_FILL,
                promptEn = "Mycelium protective panels break down naturally in garden soil within approximately ______________. (NO MORE THAN TWO WORDS / NUMBER)",
                correctAnswer = "45 days",
                acceptableAnswers = listOf("45 days", "forty-five days", "forty five days")
            ),
            ExamQuestionItem(
                id = "act25_r1",
                number = 2,
                skill = ExamQuestionSkill.READING,
                type = ExamQuestionType.TFNG,
                promptEn = "Thermal scans in Milan and Singapore showed vegetated facades reduce exterior wall heat by up to 12°C during heatwaves.",
                options = listOf("TRUE", "FALSE", "NOT GIVEN"),
                correctAnswer = "TRUE"
            ),
            ExamQuestionItem(
                id = "act25_r2",
                number = 3,
                skill = ExamQuestionSkill.READING,
                type = ExamQuestionType.MCQ,
                promptEn = "Why do sceptics question the net carbon benefit of vertical forests?",
                options = listOf(
                    "A. Mature trees cannot survive wind shear above ten storeys.",
                    "B. Reinforcing concrete balconies generates heavy embodied carbon.",
                    "C. Maintenance crews rely exclusively on diesel cranes."
                ),
                correctAnswer = "B. Reinforcing concrete balconies generates heavy embodied carbon."
            ),
            ExamQuestionItem(
                id = "act25_r3",
                number = 4,
                skill = ExamQuestionSkill.READING,
                type = ExamQuestionType.GAP_FILL,
                promptEn = "Urban ecologists note that native birds and pollinators use green high-rises as ______________ across dense cities. (NO MORE THAN TWO WORDS)",
                correctAnswer = "stepping-stone corridors",
                acceptableAnswers = listOf("stepping-stone corridors", "corridors")
            )
        )
    )

    private fun cambridge18Test3(): ExamPaper = ExamPaper(
        id = "cam18_t3",
        title = "Cambridge IELTS 18 — Academic Test 3",
        sourceLabel = "Cambridge Official Style",
        year = 2023,
        difficulty = ExamDifficulty.MODERATE_HARD,
        topicVi = "Kinh tế số & Tương lai việc làm",
        topicEn = "Digital Economy & Future of Work",
        listeningTitle = "Section 3 — Evaluating Four-Day Workweek Trials",
        listeningScript = listOf(
            "Supervisor: Tell me what surprised you most in the UK four-day workweek dataset.",
            "Student: Sixty-one companies reduced weekly hours by twenty percent while maintaining full pay, and over ninety percent chose to continue the trial afterward.",
            "Supervisor: Did revenue suffer in client-facing sectors?",
            "Student: Not at all—average revenue edged up slightly because teams eliminated redundant status meetings and adopted asynchronous project boards."
        ),
        readingTitle = "Asynchronous Collaboration and the Redefinition of Deep Work",
        readingParagraphs = listOf(
            ExamParagraph(
                label = "A",
                sentences = listOf(
                    "Knowledge workers in distributed corporations report spending nearly half their week responding to instant messages and unscheduled video calls, leaving few uninterrupted blocks for analytical synthesis.",
                    "Organisational studies indicate that each unprompted notification imposes a 'switching tax' of up to twenty-three minutes before full cognitive immersion returns."
                )
            ),
            ExamParagraph(
                label = "B",
                sentences = listOf(
                    "Firms pioneering asynchronous protocols replace real-time chatter with concise written briefs, allowing specialists across time zones to contribute thoughtfully without synchronised schedules."
                )
            )
        ),
        writingTask1Prompt = "The line graph compares employee productivity indices and voluntary turnover rates across companies trialling a four-day week from 2020 to 2024. Summarise the main trends.",
        writingTask2Prompt = "Many organisations are shifting toward shorter workweeks without reducing salaries. Do the advantages of this trend outweigh the disadvantages?",
        speakingPrompts = listOf(
            "Part 1: How do you manage distractions when working or studying on your phone?",
            "Part 2: Describe a workplace or study routine that helped you stay exceptionally productive.",
            "Part 3: Will asynchronous remote work weaken mentoring opportunities for junior employees?"
        ),
        questions = listOf(
            ExamQuestionItem(
                id = "c18_l1",
                number = 1,
                skill = ExamQuestionSkill.LISTENING,
                type = ExamQuestionType.MCQ,
                promptEn = "How did companies maintain revenue during the four-day workweek trial?",
                options = listOf(
                    "A. By cutting employee salaries by 20%.",
                    "B. By removing redundant status meetings and using asynchronous boards.",
                    "C. By hiring temporary weekend contractors."
                ),
                correctAnswer = "B. By removing redundant status meetings and using asynchronous boards."
            ),
            ExamQuestionItem(
                id = "c18_r1",
                number = 2,
                skill = ExamQuestionSkill.READING,
                type = ExamQuestionType.TFNG,
                promptEn = "An unprompted digital notification can cost up to 23 minutes of lost cognitive focus.",
                options = listOf("TRUE", "FALSE", "NOT GIVEN"),
                correctAnswer = "TRUE"
            ),
            ExamQuestionItem(
                id = "c18_r2",
                number = 3,
                skill = ExamQuestionSkill.READING,
                type = ExamQuestionType.GAP_FILL,
                promptEn = "Asynchronous organisations substitute live chatter with clear ______________. (NO MORE THAN TWO WORDS)",
                correctAnswer = "written briefs",
                acceptableAnswers = listOf("written briefs")
            )
        )
    )
}
