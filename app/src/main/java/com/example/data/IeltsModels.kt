package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BusinessCenter
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.Tier1Container
import com.example.ui.theme.Tier1Primary
import com.example.ui.theme.Tier1Secondary
import com.example.ui.theme.Tier2Container
import com.example.ui.theme.Tier2Primary
import com.example.ui.theme.Tier2Secondary
import com.example.ui.theme.Tier3Container
import com.example.ui.theme.Tier3Primary
import com.example.ui.theme.Tier3Secondary

enum class StudyTab(
    val index: Int,
    val labelVi: String,
    val labelEn: String,
    val routeId: String
) {
    ROADMAP(0, "Lộ trình", "Roadmap", "roadmap"),
    LEARN(1, "Học", "Learn", "learn"),
    PRACTICE(2, "Luyện", "Practice", "practice"),
    EXAM(3, "Thi", "Exam", "exam"),
    REVIEW(4, "Sửa", "Review", "review")
}

enum class TeacherMood(val labelVi: String) {
    TEACHING("Đang giảng bài"),
    LISTENING("Đang lắng nghe"),
    CHEERING("Chúc mừng"),
    ENCOURAGING("Động viên")
}

enum class RoadmapTier(
    val tierNumber: Int,
    val titleEn: String,
    val subtitleVi: String,
    val bandRange: String,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val containerColor: Color
) {
    TIER_1_FOUNDATION(
        tierNumber = 1,
        titleEn = "Foundation & Precision",
        subtitleVi = "Nền tảng & Chính xác học thuật",
        bandRange = "6.0 → 7.0",
        accentPrimary = Tier1Primary,
        accentSecondary = Tier1Secondary,
        containerColor = Tier1Container
    ),
    TIER_2_EXPANSION(
        tierNumber = 2,
        titleEn = "Expansion & Fluency",
        subtitleVi = "Mở rộng & Diễn đạt tự nhiên",
        bandRange = "7.0 → 8.0",
        accentPrimary = Tier2Primary,
        accentSecondary = Tier2Secondary,
        containerColor = Tier2Container
    ),
    TIER_3_MASTERY(
        tierNumber = 3,
        titleEn = "Mastery & Nuance",
        subtitleVi = "Làm chủ & Sắc thái học thuật",
        bandRange = "8.0 → 8.5",
        accentPrimary = Tier3Primary,
        accentSecondary = Tier3Secondary,
        containerColor = Tier3Container
    )
}

data class DailyBandTip(
    val id: String,
    val categoryVi: String,
    val tipVi: String,
    val upgradeFromEn: String,
    val academicPhraseEn: String
)

data class IeltsModule(
    val id: Int,
    val tier: RoadmapTier,
    val titleEn: String,
    val titleVi: String,
    val focusVi: String,
    val icon: ImageVector,
    val keyCollocations: List<String>,
    val dailyTips: List<DailyBandTip>
)

object IeltsCurriculum {

    val modules: List<IeltsModule> = listOf(
        // Tier 1 — Foundation & Precision (6.0 -> 7.0)
        IeltsModule(
            id = 1,
            tier = RoadmapTier.TIER_1_FOUNDATION,
            titleEn = "Education & Lifelong Learning",
            titleVi = "Giáo dục & Học tập suốt đời",
            focusVi = "Nâng cấp từ vựng trường lớp phổ thông sang cụm danh từ học thuật chính xác.",
            icon = Icons.Rounded.School,
            keyCollocations = listOf(
                "holistic curriculum",
                "foster critical thinking",
                "pedagogical innovation"
            ),
            dailyTips = listOf(
                DailyBandTip(
                    id = "m1_t1",
                    categoryVi = "Lexical Resource • Writing Task 2",
                    tipVi = "Thay vì lặp lại \"good education helps students\", hãy dùng động từ học thuật chỉ tác động nhận thức để chạm mốc Band 8.5.",
                    upgradeFromEn = "good education helps students think better",
                    academicPhraseEn = "a holistic curriculum cultivates analytical acumen"
                ),
                DailyBandTip(
                    id = "m1_t2",
                    categoryVi = "Speaking Part 3 • Coherence",
                    tipVi = "Khi mở rộng ý về học tập suốt đời, dùng cấu trúc nhượng bộ (Concession) giúp câu trả lời cân bằng và tự nhiên hơn.",
                    upgradeFromEn = "online courses are useful but hard to finish",
                    academicPhraseEn = "While self-paced MOOCs democratize access, they demand sustained self-discipline"
                )
            )
        ),
        IeltsModule(
            id = 2,
            tier = RoadmapTier.TIER_1_FOUNDATION,
            titleEn = "Work & Modern Business",
            titleVi = "Công việc & Kinh doanh hiện đại",
            focusVi = "Diễn đạt xu hướng làm việc linh hoạt, năng suất và văn hóa doanh nghiệp.",
            icon = Icons.Rounded.BusinessCenter,
            keyCollocations = listOf(
                "hybrid work paradigm",
                "occupational burnout",
                "entrepreneurial resilience"
            ),
            dailyTips = listOf(
                DailyBandTip(
                    id = "m2_t1",
                    categoryVi = "Lexical Resource • Task 2",
                    tipVi = "Tránh dùng cụm quá chung chung như \"work too hard and feel tired\". Thay bằng thuật ngữ chính xác về sức khỏe nghề nghiệp.",
                    upgradeFromEn = "workers get very tired from long hours",
                    academicPhraseEn = "prolonged overtime precipitates occupational burnout"
                ),
                DailyBandTip(
                    id = "m2_t2",
                    categoryVi = "Speaking Part 2 • Fluency",
                    tipVi = "Khi mô tả công việc lý tưởng, chèn cụm \"strike a sustainable work-life equilibrium\" để tạo ấn tượng tự nhiên.",
                    upgradeFromEn = "have a good balance between work and life",
                    academicPhraseEn = "strike a sustainable work-life equilibrium"
                )
            )
        ),
        IeltsModule(
            id = 3,
            tier = RoadmapTier.TIER_1_FOUNDATION,
            titleEn = "Health & Lifestyle",
            titleVi = "Sức khỏe & Lối sống",
            focusVi = "Phân biệt chuẩn xác y tế dự phòng, lối sống tĩnh tại và sức khỏe tinh thần.",
            icon = Icons.Rounded.SelfImprovement,
            keyCollocations = listOf(
                "sedentary lifestyle",
                "preventive healthcare",
                "metabolic wellbeing"
            ),
            dailyTips = listOf(
                DailyBandTip(
                    id = "m3_t1",
                    categoryVi = "Writing Task 2 • Precision",
                    tipVi = "Giám khảo đánh giá rất cao khả năng dùng danh từ hóa (Nominalization) khi phân tích nguyên nhân bệnh lý hiện đại.",
                    upgradeFromEn = "people sit too much and get sick easily",
                    academicPhraseEn = "the proliferation of sedentary lifestyles exacerbates chronic illnesses"
                ),
                DailyBandTip(
                    id = "m3_t2",
                    categoryVi = "Reading • Paraphrasing",
                    tipVi = "Trong bài Reading, \"government health campaigns\" thường được paraphrase thành \"public health interventions\".",
                    upgradeFromEn = "government health programs",
                    academicPhraseEn = "state-led preventive healthcare interventions"
                )
            )
        ),
        IeltsModule(
            id = 4,
            tier = RoadmapTier.TIER_1_FOUNDATION,
            titleEn = "Tourism & Cultural Exchange",
            titleVi = "Du lịch & Giao lưu văn hóa",
            focusVi = "Cân bằng lập luận giữa lợi ích kinh tế du lịch và bảo tồn bản sắc địa phương.",
            icon = Icons.Rounded.FlightTakeoff,
            keyCollocations = listOf(
                "cultural homogenization",
                "sustainable ecotourism",
                "preserve indigenous heritage"
            ),
            dailyTips = listOf(
                DailyBandTip(
                    id = "m4_t1",
                    categoryVi = "Writing Task 2 • Nuance",
                    tipVi = "Thay vì viết \"tourism destroys local culture\", hãy diễn đạt sắc thái xói mòn dần dần bằng collocation chuẩn Band 8.5.",
                    upgradeFromEn = "mass tourism destroys local traditions",
                    academicPhraseEn = "unchecked mass tourism accelerates cultural homogenization"
                ),
                DailyBandTip(
                    id = "m4_t2",
                    categoryVi = "Speaking Part 3 • Collocation",
                    tipVi = "Dùng \"cross-cultural empathy\" khi nói về lợi ích của việc du lịch quốc tế giúp câu trả lời sắc sảo hơn hẳn.",
                    upgradeFromEn = "tourists understand other people better",
                    academicPhraseEn = "immersive travel fosters genuine cross-cultural empathy"
                )
            )
        ),

        // Tier 2 — Expansion & Fluency (7.0 -> 8.0)
        IeltsModule(
            id = 5,
            tier = RoadmapTier.TIER_2_EXPANSION,
            titleEn = "Environment & Climate Change",
            titleVi = "Môi trường & Biến đổi khí hậu",
            focusVi = "Sử dụng động từ học thuật về giảm phát thải, chuyển dịch xanh và đa dạng sinh học.",
            icon = Icons.Rounded.Eco,
            keyCollocations = listOf(
                "mitigate carbon emissions",
                "ecological degradation",
                "renewable energy transition"
            ),
            dailyTips = listOf(
                DailyBandTip(
                    id = "m5_t1",
                    categoryVi = "Lexical Resource • Band 8.0+",
                    tipVi = "Đừng chỉ dùng \"reduce pollution\". Hãy ghép động từ \"mitigate\" hoặc \"curb\" với \"greenhouse gas emissions\".",
                    upgradeFromEn = "we must reduce air pollution quickly",
                    academicPhraseEn = "concerted policies are imperative to mitigate carbon emissions"
                ),
                DailyBandTip(
                    id = "m5_t2",
                    categoryVi = "Grammar Range • Inversion",
                    tipVi = "Dùng đảo ngữ với \"Only by + V-ing\" ở câu kết luận Task 2 giúp ăn trọn điểm Grammatical Range & Accuracy.",
                    upgradeFromEn = "we can save nature only if countries work together",
                    academicPhraseEn = "Only through binding global treaties can ecological degradation be halted"
                )
            )
        ),
        IeltsModule(
            id = 6,
            tier = RoadmapTier.TIER_2_EXPANSION,
            titleEn = "Urbanization & Infrastructure",
            titleVi = "Đô thị hóa & Hạ tầng",
            focusVi = "Lập luận chặt chẽ về quy hoạch giao thông công cộng, mật độ dân số và đô thị xanh.",
            icon = Icons.Rounded.LocationCity,
            keyCollocations = listOf(
                "urban sprawl",
                "transit-oriented development",
                "alleviate traffic congestion"
            ),
            dailyTips = listOf(
                DailyBandTip(
                    id = "m6_t1",
                    categoryVi = "Writing Task 2 • Urban Topics",
                    tipVi = "Cụm \"alleviate chronic traffic congestion\" chuẩn xác và tự nhiên hơn nhiều so với \"solve traffic jam problems\".",
                    upgradeFromEn = "subways help solve traffic jams in big cities",
                    academicPhraseEn = "integrated rail networks substantially alleviate chronic traffic congestion"
                ),
                DailyBandTip(
                    id = "m6_t2",
                    categoryVi = "Task 1 Maps • Spatial Verbs",
                    tipVi = "Khi tả bản đồ đô thị hóa ở Task 1, dùng \"supplanted by\" và \"redeveloped into\" thay cho \"changed into\".",
                    upgradeFromEn = "the old factory was changed into apartments",
                    academicPhraseEn = "the former industrial complex was supplanted by residential high-rises"
                )
            )
        ),
        IeltsModule(
            id = 7,
            tier = RoadmapTier.TIER_2_EXPANSION,
            titleEn = "Technology & AI",
            titleVi = "Công nghệ & Trí tuệ nhân tạo",
            focusVi = "Phân tích tự động hóa, quyền riêng tư dữ liệu và đạo đức thuật toán ở mức độ chuyên sâu.",
            icon = Icons.Rounded.Memory,
            keyCollocations = listOf(
                "algorithmic bias",
                "workforce displacement",
                "augment human capability"
            ),
            dailyTips = listOf(
                DailyBandTip(
                    id = "m7_t1",
                    categoryVi = "Writing Task 2 • Hedging",
                    tipVi = "Khi dự đoán tác động của AI, dùng ngôn ngữ rào đón (Hedging) để lập luận khách quan thay vì khẳng định cực đoan.",
                    upgradeFromEn = "AI will definitely take all human jobs",
                    academicPhraseEn = "generative automation is poised to reshape rather than wholly displace labor markets"
                ),
                DailyBandTip(
                    id = "m7_t2",
                    categoryVi = "Speaking Part 3 • Nuance",
                    tipVi = "Dùng \"double-edged sword\" kèm giải thích cụ thể hai mặt lợi–hại để tránh sáo rỗng.",
                    upgradeFromEn = "AI has both good and bad sides",
                    academicPhraseEn = "AI acts as a double-edged sword, augmenting productivity while raising privacy concerns"
                )
            )
        ),
        IeltsModule(
            id = 8,
            tier = RoadmapTier.TIER_2_EXPANSION,
            titleEn = "Media & The Arts",
            titleVi = "Truyền thông & Nghệ thuật",
            focusVi = "Thảo luận về thuật toán mạng xã hội, tin giả và giá trị xã hội của nghệ thuật công cộng.",
            icon = Icons.Rounded.Palette,
            keyCollocations = listOf(
                "algorithmic echo chambers",
                "cultural enrichment",
                "sensationalist journalism"
            ),
            dailyTips = listOf(
                DailyBandTip(
                    id = "m8_t1",
                    categoryVi = "Lexical Resource • Media",
                    tipVi = "Cụm \"algorithmic echo chambers\" mô tả chính xác hiện tượng người dùng chỉ thấy quan điểm giống mình trên mạng xã hội.",
                    upgradeFromEn = "social media makes people only see one opinion",
                    academicPhraseEn = "curated feeds trap citizens inside algorithmic echo chambers"
                ),
                DailyBandTip(
                    id = "m8_t2",
                    categoryVi = "Speaking Part 2 • Expressive",
                    tipVi = "Thay vì nói \"this painting is very nice\", hãy dùng tính từ giàu sức gợi như \"evocative\" hoặc \"thought-provoking\".",
                    upgradeFromEn = "the exhibition was very interesting",
                    academicPhraseEn = "the exhibition was deeply evocative and thought-provoking"
                )
            )
        ),

        // Tier 3 — Mastery & Nuance (8.0 -> 8.5)
        IeltsModule(
            id = 9,
            tier = RoadmapTier.TIER_3_MASTERY,
            titleEn = "Macroeconomics & Globalization",
            titleVi = "Kinh tế vĩ mô & Toàn cầu hóa",
            focusVi = "Làm chủ ngôn ngữ kinh tế: chuỗi cung ứng, bất bình đẳng thu nhập và chính sách tài khóa.",
            icon = Icons.Rounded.AccountBalance,
            keyCollocations = listOf(
                "socioeconomic disparity",
                "fiscal sustainability",
                "global supply chain resilience"
            ),
            dailyTips = listOf(
                DailyBandTip(
                    id = "m9_t1",
                    categoryVi = "Band 8.5 • Academic Register",
                    tipVi = "Thay \"the gap between rich and poor gets bigger\" bằng \"widen socioeconomic disparities\" để nâng tầm văn phong học thuật.",
                    upgradeFromEn = "the gap between rich and poor is getting bigger",
                    academicPhraseEn = "unregulated capital flows can widen socioeconomic disparities"
                ),
                DailyBandTip(
                    id = "m9_t2",
                    categoryVi = "Task 1 Data • Precision",
                    tipVi = "Mô tả biến động kinh tế tinh tế hơn bằng \"plateaued\", \"experienced a marginal uptick\" thay cho \"went up a little\".",
                    upgradeFromEn = "GDP went up a little then stayed the same",
                    academicPhraseEn = "GDP recorded a marginal uptick before plateauing through Q4"
                )
            )
        ),
        IeltsModule(
            id = 10,
            tier = RoadmapTier.TIER_3_MASTERY,
            titleEn = "Law, Crime & Ethics",
            titleVi = "Pháp luật, Tội phạm & Đạo đức",
            focusVi = "Phân biệt răn đe hình phạt (deterrence) với cải tạo tái hòa nhập (rehabilitation) ở chuẩn Band 8.5.",
            icon = Icons.Rounded.Gavel,
            keyCollocations = listOf(
                "rehabilitative justice",
                "deterrent effect",
                "recidivism rates"
            ),
            dailyTips = listOf(
                DailyBandTip(
                    id = "m10_t1",
                    categoryVi = "Writing Task 2 • Legal Lexis",
                    tipVi = "Từ \"recidivism\" (tỷ lệ tái phạm) là từ khóa vàng giúp bài viết chủ đề tội phạm lập tức đạt độ chính xác của Band 8.5.",
                    upgradeFromEn = "prisoners do crimes again after leaving jail",
                    academicPhraseEn = "vocational rehabilitation programs markedly curtail recidivism rates"
                ),
                DailyBandTip(
                    id = "m10_t2",
                    categoryVi = "Coherence • Referencing",
                    tipVi = "Dùng \"This punitive paradigm\" để liên kết đoạn mượt mà thay vì lặp lại \"This punishment method\".",
                    upgradeFromEn = "this strict punishment method does not work well",
                    academicPhraseEn = "this purely punitive paradigm rarely addresses underlying root causes"
                )
            )
        ),
        IeltsModule(
            id = 11,
            tier = RoadmapTier.TIER_3_MASTERY,
            titleEn = "Psychology & Human Behavior",
            titleVi = "Tâm lý học & Hành vi con người",
            focusVi = "Phân tích thiên kiến nhận thức, động lực nội tại và ảnh hưởng xã hội bằng ngôn ngữ tinh tế.",
            icon = Icons.Rounded.Psychology,
            keyCollocations = listOf(
                "cognitive dissonance",
                "intrinsic motivation",
                "delayed gratification"
            ),
            dailyTips = listOf(
                DailyBandTip(
                    id = "m11_t1",
                    categoryVi = "Band 8.5 • Precision",
                    tipVi = "Phân biệt \"intrinsic motivation\" (động lực nội tại) và \"extrinsic rewards\" (phần thưởng bên ngoài) để lập luận sâu sắc vượt trội.",
                    upgradeFromEn = "people work better when they actually enjoy it",
                    academicPhraseEn = "intrinsic motivation sustains long-term mastery far beyond extrinsic incentives"
                ),
                DailyBandTip(
                    id = "m11_t2",
                    categoryVi = "Speaking Part 3 • Idiomatic",
                    tipVi = "Dùng cụm \"instant gratification\" khi nói về thói quen xem video ngắn giúp diễn đạt tự nhiên như người bản xứ.",
                    upgradeFromEn = "young people want fun right away",
                    academicPhraseEn = "short-form platforms condition users toward instant gratification"
                )
            )
        ),
        IeltsModule(
            id = 12,
            tier = RoadmapTier.TIER_3_MASTERY,
            titleEn = "Space Science & Future Frontiers",
            titleVi = "Khoa học Vũ trụ & Tương lai",
            focusVi = "Tranh luận thuyết phục về ngân sách thám hiểm vũ trụ, công nghệ phụ trợ và tương lai nhân loại.",
            icon = Icons.Rounded.RocketLaunch,
            keyCollocations = listOf(
                "technological spin-offs",
                "interplanetary exploration",
                "allocation of public funds"
            ),
            dailyTips = listOf(
                DailyBandTip(
                    id = "m12_t1",
                    categoryVi = "Writing Task 2 • High-Band Ideas",
                    tipVi = "Lập luận đắt giá cho chủ đề vũ trụ: nhắc tới \"technological spin-offs\" (công nghệ phái sinh ứng dụng ngược lại cho Trái Đất).",
                    upgradeFromEn = "space research makes useful inventions for daily life",
                    academicPhraseEn = "aerospace funding yields transformative technological spin-offs for terrestrial medicine"
                ),
                DailyBandTip(
                    id = "m12_t2",
                    categoryVi = "Grammar • Participle Clause",
                    tipVi = "Mở đầu câu bằng mệnh đề phân từ (Participle Clause) giúp câu văn cô đọng và nhịp điệu uyển chuyển chuẩn 8.5.",
                    upgradeFromEn = "because we face climate risks, we should explore space",
                    academicPhraseEn = "Confronted with planetary fragility, humanity increasingly views space research as long-term insurance"
                )
            )
        )
    )

    fun moduleById(id: Int): IeltsModule =
        modules.firstOrNull { it.id == id } ?: modules.first()

    fun modulesByTier(tier: RoadmapTier): List<IeltsModule> =
        modules.filter { it.tier == tier }

    fun calculateEstimatedBand(completedIds: Set<Int>): Float {
        // Maps 0..12 completed modules smoothly across 6.0 -> 8.5
        val ratio = (completedIds.size.coerceIn(0, modules.size)).toFloat() / modules.size.toFloat()
        val rawBand = 6.0f + ratio * 2.5f
        return (Math.round(rawBand * 10f) / 10f).coerceIn(6.0f, 8.5f)
    }

    val tierOrder: List<RoadmapTier> = listOf(
        RoadmapTier.TIER_1_FOUNDATION,
        RoadmapTier.TIER_2_EXPANSION,
        RoadmapTier.TIER_3_MASTERY
    )

    val defaultIconFallback: ImageVector = Icons.Rounded.AutoAwesome
}
