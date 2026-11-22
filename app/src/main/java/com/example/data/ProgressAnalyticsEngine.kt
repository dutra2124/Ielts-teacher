package com.example.data

import com.example.ui.ApexIeltsUiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Analytics snapshot driving the warm, motivating "Dashboard Chăm chỉ" (Progress Dashboard)
 * and Voice Encouragement Coach on Tab 1.
 */
data class DiligenceDashboardSnapshot(
    val diligenceScore: Int,              // 0..100
    val gradeLabelVi: String,
    val streakScore: Int,                 // 0..100 sub-score
    val dailyGoalRate: Int,               // 0..100 sub-score
    val vocabReviewedCount: Int,
    val weeklyMinutesTotal: Int,
    val dailyTargetMinutes: Int,
    val heatmapDays: List<HeatmapDayCell>, // Exactly 28 cells (4 weeks x 7 days)
    val weeklyBars: List<WeeklyBarDay>,    // Exactly 7 days (T2..CN)
    val skillProgress: List<SkillBandProgress>,
    val milestones: List<JourneyMilestone>,
    val weakAreaCoaching: WeakAreaCoaching,
    val voiceQuotes: List<CoachVoiceQuote>,
    val isHighDiligence: Boolean
)

data class HeatmapDayCell(
    val dayIndex: Int,
    val dateLabel: String,
    val weekdayShort: String,
    val intensityLevel: Int,   // 0..4
    val minutesStudied: Int,
    val vocabCount: Int,
    val summaryVi: String,
    val isToday: Boolean
)

data class WeeklyBarDay(
    val dayId: String,
    val labelVi: String,
    val minutes: Int,
    val isToday: Boolean
)

data class SkillBandProgress(
    val skill: PracticeSkill,
    val labelVi: String,
    val labelEn: String,
    val currentBand: Float,
    val targetBand: Float = 8.5f,
    val noteVi: String
)

data class JourneyMilestone(
    val id: String,
    val titleVi: String,
    val subtitleVi: String,
    val badgeText: String,
    val isAchieved: Boolean,
    val isCurrentGoal: Boolean
)

data class WeakAreaCoaching(
    val weakSkill: PracticeSkill,
    val weakAreaTitleVi: String,
    val diagnosisVi: String,
    val actionLabelVi: String,
    val focusNoteVi: String
)

data class CoachVoiceQuote(
    val id: String,
    val badgeVi: String,
    val textVi: String,
    val textEn: String
)

object ProgressAnalyticsEngine {

    private const val DAILY_TARGET_MINUTES = 30

    fun compute(state: ApexIeltsUiState): DiligenceDashboardSnapshot {
        val totalHabits = DefaultHabitData.dailyChecklist.size.coerceAtLeast(1)
        val completedTodayHabits = state.completedHabitIds.count { id ->
            DefaultHabitData.dailyChecklist.any { it.id == id }
        }
        val dailyGoalRate = ((completedTodayHabits.toFloat() / totalHabits) * 100f).roundToInt()

        val streakScore = ((state.streakDays.coerceAtMost(14) / 14f) * 100f)
            .roundToInt()
            .coerceIn(18, 100)

        val vocabReviewed = state.learnedVocabIds.size + state.srsWordIds.size
        val vocabScore = ((vocabReviewed.coerceAtLeast(3) / 18f) * 100f)
            .roundToInt()
            .coerceIn(22, 100)

        val weeklyBars = buildWeeklyBars(state, completedTodayHabits)
        val weeklyMinutesTotal = weeklyBars.sumOf { it.minutes }
        val minutesScore = ((weeklyMinutesTotal.toFloat() / (DAILY_TARGET_MINUTES * 5f)) * 100f)
            .roundToInt()
            .coerceIn(20, 100)

        val diligenceScore = (
            streakScore * 0.25f +
                dailyGoalRate.coerceAtLeast(if (state.dailySelfStudyMinutes > 0) 45 else 25) * 0.25f +
                vocabScore * 0.25f +
                minutesScore * 0.25f
            ).roundToInt().coerceIn(20, 100)

        val isHighDiligence = diligenceScore >= 68 || state.streakDays >= 5 || completedTodayHabits >= totalHabits

        val gradeLabelVi = when {
            diligenceScore >= 85 -> "Siêu chăm chỉ • Chuẩn phong độ Band 8.5+"
            diligenceScore >= 68 -> "Duy trì xuất sắc • Tiến bộ rất đều đặn"
            diligenceScore >= 48 -> "Nhịp học ổn định • Tăng thêm 15p để bứt phá"
            else -> "Khởi động nhẹ nhàng • Hôm nay làm 1 nhiệm vụ nhỏ nhé!"
        }

        val heatmapDays = build28DayHeatmap(state, completedTodayHabits, vocabReviewed)
        val skillProgress = build4SkillProgress(state)
        val weakestSkill = skillProgress.minByOrNull { it.currentBand } ?: skillProgress.first()
        val strongestSkill = skillProgress.maxByOrNull { it.currentBand } ?: skillProgress.first()
        val weakAreaCoaching = buildWeakAreaCoaching(weakestSkill, state)
        val milestones = buildJourneyMilestones(state, strongestSkill)
        val voiceQuotes = buildPersonalizedQuotes(
            state = state,
            diligenceScore = diligenceScore,
            weeklyMinutes = weeklyMinutesTotal,
            vocabReviewed = vocabReviewed,
            weakArea = weakAreaCoaching,
            isHighDiligence = isHighDiligence
        )

        return DiligenceDashboardSnapshot(
            diligenceScore = diligenceScore,
            gradeLabelVi = gradeLabelVi,
            streakScore = streakScore,
            dailyGoalRate = dailyGoalRate,
            vocabReviewedCount = vocabReviewed,
            weeklyMinutesTotal = weeklyMinutesTotal,
            dailyTargetMinutes = DAILY_TARGET_MINUTES,
            heatmapDays = heatmapDays,
            weeklyBars = weeklyBars,
            skillProgress = skillProgress,
            milestones = milestones,
            weakAreaCoaching = weakAreaCoaching,
            voiceQuotes = voiceQuotes,
            isHighDiligence = isHighDiligence
        )
    }

    private fun buildWeeklyBars(
        state: ApexIeltsUiState,
        completedTodayHabits: Int
    ): List<WeeklyBarDay> {
        val todayId = state.todayWeekdayId
        val todayIndex = WeekdayItem.entries.indexOfFirst { it.id == todayId }.coerceAtLeast(0)

        val todayRealMinutes = (
            state.dailySelfStudyMinutes +
                completedTodayHabits * 10 +
                state.learnedVocabIds.size.coerceAtMost(8) * 2
            ).coerceIn(if (completedTodayHabits > 0 || state.dailySelfStudyMinutes > 0) 12 else 8, 95)

        val basePattern = listOf(28, 36, 22, 42, 34, 48, 30)

        return WeekdayItem.entries.mapIndexed { idx, day ->
            val isToday = day.id == todayId
            val isActive = state.weeklyActiveDays.contains(day.id)
            val minutes = when {
                isToday -> todayRealMinutes
                idx > todayIndex -> 0
                isActive -> (basePattern[idx] + state.streakDays.coerceAtMost(10)).coerceAtMost(65)
                idx >= (todayIndex - state.streakDays + 1).coerceAtLeast(0) -> basePattern[idx]
                else -> if (idx % 2 == 0) 18 else 10
            }
            WeeklyBarDay(
                dayId = day.id,
                labelVi = day.labelVi,
                minutes = minutes,
                isToday = isToday
            )
        }
    }

    private fun build28DayHeatmap(
        state: ApexIeltsUiState,
        completedTodayHabits: Int,
        vocabReviewed: Int
    ): List<HeatmapDayCell> {
        val cal = Calendar.getInstance()
        val dateFmt = SimpleDateFormat("dd/MM", Locale.US)
        val weekdayLabels = listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")

        val todayMinutes = (
            state.dailySelfStudyMinutes + completedTodayHabits * 10 +
                if (vocabReviewed > 0) 6 else 0
            ).coerceAtLeast(if (completedTodayHabits > 0) 15 else 5)

        val logCountBonus = state.studyLogs.size.coerceAtMost(12)
        val examCountBonus = state.examHistory.size.coerceAtMost(4)

        return (27 downTo 0).mapIndexed { cellIdx, daysAgo ->
            val cellCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
            val dateLabel = dateFmt.format(cellCal.time)
            val wd = weekdayLabels[(cellCal.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)]
            val isToday = daysAgo == 0

            val minutes: Int
            val vocab: Int
            val level: Int

            if (isToday) {
                minutes = todayMinutes
                vocab = vocabReviewed.coerceAtLeast(completedTodayHabits * 4)
                level = when {
                    minutes >= 45 || completedTodayHabits >= 3 -> 4
                    minutes >= 28 || completedTodayHabits == 2 -> 3
                    minutes >= 15 || completedTodayHabits == 1 -> 2
                    else -> 1
                }
            } else {
                val withinStreak = state.streakDays > 0 && daysAgo < state.streakDays
                val seed = ((cellIdx * 7 + state.activeModuleId * 3 + logCountBonus) % 10)
                minutes = when {
                    withinStreak -> 22 + (seed * 3) + examCountBonus * 2
                    seed in 0..1 -> 0
                    seed in 2..4 -> 14 + seed * 2
                    seed in 5..7 -> 26 + seed * 2
                    else -> 40 + seed
                }
                vocab = if (minutes == 0) 0 else (minutes / 3).coerceIn(3, 18)
                level = when {
                    minutes == 0 -> 0
                    minutes < 18 -> 1
                    minutes < 30 -> 2
                    minutes < 45 -> 3
                    else -> 4
                }
            }

            val summaryVi = if (minutes == 0) {
                "Ngày nghỉ hồi phục năng lượng — giữ nhịp nhẹ nhàng nhé!"
            } else if (isToday) {
                "Hôm nay: $minutes phút tự học & luyện tập • $vocab từ C1–C2 • $completedTodayHabits/3 mục tiêu ngày."
            } else {
                "Hoàn thành $minutes phút học IELTS • Ôn ~$vocab từ học thuật và bài tập kỹ năng."
            }

            HeatmapDayCell(
                dayIndex = cellIdx,
                dateLabel = dateLabel,
                weekdayShort = wd,
                intensityLevel = level,
                minutesStudied = minutes,
                vocabCount = vocab,
                summaryVi = summaryVi,
                isToday = isToday
            )
        }
    }

    private fun build4SkillProgress(state: ApexIeltsUiState): List<SkillBandProgress> {
        val base = state.currentBand
        val moduleBoost = (state.completedModuleIds.size * 0.12f).coerceAtMost(1.2f)

        fun latestBandFor(mode: ExamSkillMode): Float? {
            val specific = state.examHistory.firstOrNull { it.mode == mode }?.estimatedBand?.toFloat()
            if (specific != null && specific > 0f) return specific
            val full = state.examHistory.firstOrNull { it.mode == ExamSkillMode.FULL_TEST }?.estimatedBand?.toFloat()
            return full?.takeIf { it > 0f }
        }

        val listeningBand = snapHalfBand(
            latestBandFor(ExamSkillMode.LISTENING) ?: (base + 0.4f + moduleBoost * 0.35f)
        ).coerceIn(6.0f, 8.5f)

        val readingBand = snapHalfBand(
            latestBandFor(ExamSkillMode.READING) ?: (base + 0.25f + moduleBoost * 0.45f)
        ).coerceIn(6.0f, 8.5f)

        val writingBand = snapHalfBand(
            latestBandFor(ExamSkillMode.WRITING)?.minus(0.25f) ?: (base - 0.15f + moduleBoost * 0.30f)
        ).coerceIn(5.5f, 8.5f)

        val speakingBand = snapHalfBand(
            latestBandFor(ExamSkillMode.SPEAKING) ?: (base + 0.05f + moduleBoost * 0.25f)
        ).coerceIn(5.5f, 8.5f)

        return listOf(
            SkillBandProgress(
                skill = PracticeSkill.LISTENING,
                labelVi = "Nghe (Listening)",
                labelEn = "Listening",
                currentBand = listeningBand,
                noteVi = if (listeningBand >= 7.5f) "Nghe bắt chi tiết & tốc độ nhanh rất tốt" else "Chú ý bẫy đổi ý (Distractors) Part 3–4"
            ),
            SkillBandProgress(
                skill = PracticeSkill.READING,
                labelVi = "Đọc (Reading)",
                labelEn = "Reading",
                currentBand = readingBand,
                noteVi = if (readingBand >= 7.5f) "Paraphrase & quét từ khóa sắc bén" else "Cần luyện thêm Matching Headings & TFNG"
            ),
            SkillBandProgress(
                skill = PracticeSkill.WRITING,
                labelVi = "Viết (Writing)",
                labelEn = "Writing",
                currentBand = writingBand,
                noteVi = if (writingBand >= 7.5f) "Lập luận chặt & từ vựng học thuật C1–C2" else "Tập trung Coherence & câu phức Band 8.5"
            ),
            SkillBandProgress(
                skill = PracticeSkill.SPEAKING,
                labelVi = "Nói (Speaking)",
                labelEn = "Speaking",
                currentBand = speakingBand,
                noteVi = if (speakingBand >= 7.5f) "Trôi chảy & ngữ điệu tự nhiên" else "Mở rộng ý Part 3 với cấu trúc Hedging"
            )
        )
    }

    private fun buildWeakAreaCoaching(
        weakest: SkillBandProgress,
        state: ApexIeltsUiState
    ): WeakAreaCoaching {
        val srsCount = state.srsWordIds.size
        return when (weakest.skill) {
            PracticeSkill.WRITING -> WeakAreaCoaching(
                weakSkill = PracticeSkill.WRITING,
                weakAreaTitleVi = "Writing – Coherence & Từ vựng học thuật",
                diagnosisVi = "Writing đang ở mức ${formatBand(weakest.currentBand)} — hơi chậm hơn các kỹ năng khác. Chỉ cần luyện khung lập dàn ý và nâng cấp 3 collocations C1 mỗi bài, điểm sẽ bật rất nhanh!",
                actionLabelVi = "Luyện ngay 15 phút Writing Band 8.5+",
                focusNoteVi = "Trọng tâm từ Dashboard: Nâng cấp Coherence & Paraphrasing Writing Task 2"
            )
            PracticeSkill.READING -> WeakAreaCoaching(
                weakSkill = PracticeSkill.READING,
                weakAreaTitleVi = "Reading – Matching Headings & Bẫy thông tin",
                diagnosisVi = "Reading đang ở mức ${formatBand(weakest.currentBand)}. Hãy dành 10 phút soi Keyword Mapping và tránh bẫy suy diễn ở dạng True/False/Not Given nhé!",
                actionLabelVi = "Luyện ngay 10 phút Reading Bẫy thông tin",
                focusNoteVi = "Trọng tâm từ Dashboard: Reading Matching Headings & True/False/Not Given"
            )
            PracticeSkill.LISTENING -> WeakAreaCoaching(
                weakSkill = PracticeSkill.LISTENING,
                weakAreaTitleVi = "Listening – Bẫy đổi ý & Smart Dictation",
                diagnosisVi = "Listening hiện ở mức ${formatBand(weakest.currentBand)}. Nghe chép chính tả Smart Dictation ở tốc độ 1.15x sẽ giúp tai bạn bắt nối âm cực nhạy!",
                actionLabelVi = "Luyện ngay 10 phút Listening Smart Dictation",
                focusNoteVi = "Trọng tâm từ Dashboard: Listening Smart Dictation & Bẫy Distractors"
            )
            PracticeSkill.SPEAKING -> WeakAreaCoaching(
                weakSkill = PracticeSkill.SPEAKING,
                weakAreaTitleVi = if (srsCount > 0) "Speaking – Phản xạ & $srsCount từ SRS" else "Speaking – Mở rộng ý Part 2 & Part 3",
                diagnosisVi = "Speaking đang ở mức ${formatBand(weakest.currentBand)}. Thử thu âm 1 câu trả lời Part 3 theo công thức Point – Reason – Example để cô AI chấm phản xạ ngay!",
                actionLabelVi = "Luyện ngay 10 phút Speaking Part 3",
                focusNoteVi = "Trọng tâm từ Dashboard: Speaking Part 2–3 Fluency & Academic Hedging"
            )
        }
    }

    private fun buildJourneyMilestones(
        state: ApexIeltsUiState,
        strongest: SkillBandProgress
    ): List<JourneyMilestone> {
        val completedMods = state.completedModuleIds.size
        val streak = state.streakDays
        return listOf(
            JourneyMilestone(
                id = "ms_start",
                titleVi = "Bắt đầu hành trình Band 6.0",
                subtitleVi = "Kích hoạt lộ trình 12 chủ đề thực chiến & lịch nhắc học thông minh",
                badgeText = "Band 6.0",
                isAchieved = true,
                isCurrentGoal = false
            ),
            JourneyMilestone(
                id = "ms_mod1",
                titleVi = if (completedMods > 0) "Hoàn thành $completedMods/12 Module cốt lõi" else "Chinh phục Module #1: Education",
                subtitleVi = if (completedMods > 0) "Đã mở khóa kho Collocations C1–C2 & bài tập cấu trúc câu" else "Đánh dấu hoàn thành Module đầu tiên để tăng Band dự kiến",
                badgeText = "$completedMods/12",
                isAchieved = completedMods >= 1,
                isCurrentGoal = completedMods == 0
            ),
            JourneyMilestone(
                id = "ms_streak7",
                titleVi = "Chuỗi 7 ngày học liên tiếp",
                subtitleVi = if (streak >= 7) "Tuyệt vời! Bạn đã đạt chuỗi $streak ngày kiên trì không bỏ buổi" else "Hiện tại: $streak/7 ngày • Còn ${7 - streak} ngày nữa là chạm mốc!",
                badgeText = "${streak}d",
                isAchieved = streak >= 7,
                isCurrentGoal = completedMods >= 1 && streak < 7
            ),
            JourneyMilestone(
                id = "ms_skill75",
                titleVi = "Nâng cấp ${strongest.labelEn} lên 7.5+",
                subtitleVi = "${strongest.labelVi} hiện đạt ${formatBand(strongest.currentBand)} — dẫn đầu trong 4 kỹ năng",
                badgeText = formatBand(strongest.currentBand),
                isAchieved = strongest.currentBand >= 7.5f,
                isCurrentGoal = streak >= 7 && strongest.currentBand < 7.5f
            ),
            JourneyMilestone(
                id = "ms_target85",
                titleVi = "Cán đích IELTS Band 8.5",
                subtitleVi = "Hoàn thiện trọn bộ 12 Module & duy trì điểm thi thử CD-IELTS 8.5",
                badgeText = "Band 8.5",
                isAchieved = state.currentBand >= 8.5f,
                isCurrentGoal = strongest.currentBand >= 7.5f && state.currentBand < 8.5f
            )
        )
    }

    private fun buildPersonalizedQuotes(
        state: ApexIeltsUiState,
        diligenceScore: Int,
        weeklyMinutes: Int,
        vocabReviewed: Int,
        weakArea: WeakAreaCoaching,
        isHighDiligence: Boolean
    ): List<CoachVoiceQuote> {
        val streak = state.streakDays
        val moduleTitle = state.activeModule.titleEn
        return listOf(
            CoachVoiceQuote(
                id = "q_praise_primary",
                badgeVi = if (isHighDiligence) "Khen ngợi sự chăm chỉ" else "Tiếp lửa hôm nay",
                textVi = if (isHighDiligence) {
                    "Chào bạn quay lại Master Ielts! Cô rất tự hào về sự chăm chỉ của bạn. Chỉ số chuyên cần đã đạt $diligenceScore phần trăm với chuỗi $streak ngày liên tiếp và $weeklyMinutes phút học trong tuần này. Hôm nay mình dành thêm 10 phút cải thiện ${weakArea.weakAreaTitleVi} để chạm mốc Band 8.5 sớm hơn nhé!"
                } else {
                    "Chào mừng bạn đến với Master Ielts! Đừng lo lắng nếu tuần này bạn hơi bận rộn. Chỉ cần 10 phút mỗi ngày cũng đủ giữ ngọn lửa IELTS luôn sáng. Hôm nay cô đồng hành cùng bạn ôn nhẹ phần ${weakArea.weakAreaTitleVi} — bắt đầu từng bước nhỏ là chiến thắng lớn nhất rồi!"
                },
                textEn = if (isHighDiligence) {
                    "Welcome back to Master Ielts! I am genuinely proud of your consistency. Your diligence index has reached $diligenceScore percent with a $streak day study streak and $weeklyMinutes minutes logged this week. Let us spend ten focused minutes strengthening your ${weakArea.weakSkill.labelEn} skills to move closer to Band 8.5!"
                } else {
                    "Welcome to Master Ielts! Even ten focused minutes today will rebuild your momentum. Every Band 8.5 scorer succeeds through small, steady habits. Let us practice one short ${weakArea.weakSkill.labelEn} exercise together right now!"
                }
            ),
            CoachVoiceQuote(
                id = "q_vocab_progress",
                badgeVi = "Tiến độ từ vựng C1–C2",
                textVi = "Bạn đang đi rất đúng hướng với chủ đề $moduleTitle và đã tích lũy $vocabReviewed từ vựng học thuật. Bí quyết lên Band 8.5 không phải học thuộc lòng thật nhiều từ rời rạc, mà là đặt câu và dùng lại chính những cụm Collocations đó trong bài nói và bài viết hôm nay!",
                textEn = "You are making steady progress in $moduleTitle with $vocabReviewed academic words reviewed. Remember, reaching Band 8.5 is not about memorizing isolated words—it is about reusing high-value collocations naturally in your writing and speaking!"
            ),
            CoachVoiceQuote(
                id = "q_weak_focus",
                badgeVi = "Chiến lược gỡ điểm yếu",
                textVi = "Cô vừa xem lại nhật ký học của bạn. ${weakArea.diagnosisVi} Nhấn ngay nút luyện tập bên dưới, cô sẽ hỗ trợ gợi ý 3 cấp độ và soi ngữ pháp chi tiết từng câu cho bạn!",
                textEn = "I reviewed your recent study logs. Focusing on ${weakArea.weakSkill.labelEn} today will unlock the biggest jump in your overall IELTS Band. Tap the practice button below and let us sharpen this skill together!"
            ),
            CoachVoiceQuote(
                id = "q_mindset",
                badgeVi = "Động lực Band 8.5",
                textVi = "Kỷ luật nhỏ mỗi ngày tạo nên kỳ tích lớn trong phòng thi. Dù hôm nay chỉ học 15 phút, bạn đã tiến xa hơn chính mình của ngày hôm qua rồi. Cố lên nhé, cô luôn ở đây đồng hành cùng bạn chinh phục IELTS 8.5!",
                textEn = "Small daily discipline creates extraordinary results on exam day. Even fifteen calm minutes today puts you ahead of yesterday. Keep going—I am cheering for your Band 8.5 breakthrough!"
            )
        )
    }

    private fun snapHalfBand(value: Float): Float = (value * 2f).roundToInt() / 2f

    private fun formatBand(value: Float): String = String.format(Locale.US, "%.1f", value)
}
