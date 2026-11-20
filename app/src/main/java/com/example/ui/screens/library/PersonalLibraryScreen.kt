package com.example.ui.screens.library

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ExamPaper
import com.example.data.ExamSkillMode
import com.example.data.VocabWordItem
import com.example.data.local.BulkImportDataset
import com.example.data.local.LibraryCategoryFilter
import com.example.data.local.PersonalMaterial
import com.example.ui.ApexIeltsUiState
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardSurfaceTinted
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.GoldMasteryLight
import com.example.ui.theme.GoldOnContainer
import com.example.ui.theme.IndigoOnSurface
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkOnHero
import com.example.ui.theme.InkOnHeroMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.PracticeTeal
import com.example.ui.theme.PracticeTealLight
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.TealOnSurface

private val LibraryHeroGradient = Brush.linearGradient(
    colors = listOf(ElectricIndigo, PracticeTeal, CyanGlow),
    start = Offset(0f, 0f),
    end = Offset(1080f, 680f)
)

/**
 * Dedicated "Tài liệu cá nhân & Tự học" (Personal Library / Self-Study Hub) optimized for 20:9 portrait Android layout.
 * Opens as a full-screen overlay without adding a 6th tab to the Bottom Navigation Bar.
 */
@Composable
fun PersonalLibraryScreen(
    uiState: ApexIeltsUiState,
    onClose: () -> Unit,
    onOpenBulkImporter: (BulkImportDataset) -> Unit,
    onSaveMaterial: (PersonalMaterial) -> Unit,
    onDeleteMaterial: (String) -> Unit,
    onUploadMaterialAudio: (Uri, (String, String) -> Unit) -> Unit,
    onSaveHighlightFlashcard: (VocabWordItem) -> Unit,
    onSummarizeMaterialWithAi: (PersonalMaterial) -> Unit,
    onLogSelfStudySession: (materialTitle: String, minutes: Int) -> Unit,
    onJumpToModuleLearn: (Int) -> Unit,
    onStartPersonalExam: (ExamPaper, ExamSkillMode) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onClose() }

    var selectedCategory by rememberSaveable { mutableStateOf(LibraryCategoryFilter.ALL) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedTag by rememberSaveable { mutableStateOf<String?>(null) }
    var openedMaterialId by remember { mutableStateOf<String?>(null) }
    var editingMaterial by remember { mutableStateOf<PersonalMaterial?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

    val allTags = remember(uiState.personalMaterials) {
        uiState.personalMaterials
            .flatMap { it.tags }
            .map { it.trim().removePrefix("#") }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .take(14)
    }

    val filteredMaterials = remember(uiState.personalMaterials, selectedCategory, searchQuery, selectedTag) {
        val q = searchQuery.trim().lowercase()
        uiState.personalMaterials.filter { pm ->
            val catMatch = when (selectedCategory) {
                LibraryCategoryFilter.ALL -> true
                LibraryCategoryFilter.NOTES -> LibraryCategoryFilter.classifyMaterialCategory(pm.category) == LibraryCategoryFilter.NOTES
                LibraryCategoryFilter.ARTICLES -> LibraryCategoryFilter.classifyMaterialCategory(pm.category) == LibraryCategoryFilter.ARTICLES
                else -> false
            }
            val tagMatch = selectedTag == null || pm.tags.any {
                it.removePrefix("#").equals(selectedTag, ignoreCase = true)
            }
            val queryMatch = q.isEmpty() ||
                pm.title.lowercase().contains(q) ||
                pm.contentEn.lowercase().contains(q) ||
                pm.notesVi.lowercase().contains(q) ||
                pm.tags.any { it.lowercase().contains(q) }
            catMatch && tagMatch && queryMatch
        }
    }

    val flashcardDecksByModule = remember(uiState.allVocabulary, uiState.modules, searchQuery) {
        val q = searchQuery.trim().lowercase()
        val matchingWords = if (q.isEmpty()) {
            uiState.allVocabulary
        } else {
            uiState.allVocabulary.filter {
                it.word.lowercase().contains(q) || it.meaningVi.lowercase().contains(q)
            }
        }
        matchingWords.groupBy { it.moduleId }.entries
            .sortedBy { it.key }
            .map { (modId, words) ->
                val mod = uiState.modules.firstOrNull { it.id == modId }
                Triple(modId, mod?.titleEn ?: "Module #$modId", words)
            }
    }

    val filteredPersonalExams = remember(uiState.customExams, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) uiState.customExams
        else uiState.customExams.filter {
            it.title.lowercase().contains(q) || it.topicVi.lowercase().contains(q) || it.topicEn.lowercase().contains(q)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceCanvas)
            .testTag("personal_library_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Vibrant Hero Top Bar + Quick Actions
            item(key = "library_hero_header") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                        .background(LibraryHeroGradient)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.20f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f)),
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onClose)
                                    .testTag("library_back_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = "Quay lại",
                                        tint = Color.White
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.library_hub_title),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp
                                    ),
                                    color = InkOnHero,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = stringResource(R.string.library_hub_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkOnHeroMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Today's Self-Study Diligence Pill
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.22f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.34f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Timer,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${uiState.dailySelfStudyMinutes}p",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Dual Action Row: Bulk CSV/JSON Import + Create New Material
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onOpenBulkImporter(BulkImportDataset.VOCABULARY) }
                                .testTag("library_open_bulk_importer_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.UploadFile,
                                    contentDescription = null,
                                    tint = ElectricIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Nhập CSV / JSON",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ElectricIndigo,
                                    maxLines = 1
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.20f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.42f)),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { isCreatingNew = true }
                                .testTag("library_create_material_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tạo tài liệu mới",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // 2. Instant Search Bar + Category Filter Pills + Tag Chips
            item(key = "library_search_and_filters") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Tìm tiêu đề, bài đọc, từ vựng, #tags...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = InkMuted) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Xóa tìm kiếm", tint = InkMuted)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardWhite,
                            unfocusedContainerColor = CardWhite,
                            focusedBorderColor = ElectricIndigo,
                            unfocusedBorderColor = CardBorderSoft
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("library_search_input")
                    )

                    // 5 Category Pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LibraryCategoryFilter.entries.forEach { cat ->
                            val selected = cat == selectedCategory
                            val countBadge = when (cat) {
                                LibraryCategoryFilter.ALL -> uiState.personalMaterials.size
                                LibraryCategoryFilter.NOTES -> uiState.personalMaterials.count {
                                    LibraryCategoryFilter.classifyMaterialCategory(it.category) == LibraryCategoryFilter.NOTES
                                }
                                LibraryCategoryFilter.ARTICLES -> uiState.personalMaterials.count {
                                    LibraryCategoryFilter.classifyMaterialCategory(it.category) == LibraryCategoryFilter.ARTICLES
                                }
                                LibraryCategoryFilter.FLASHCARDS -> uiState.allVocabulary.size
                                LibraryCategoryFilter.EXAMS -> uiState.customExams.size
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (selected) ElectricIndigo else CardWhite,
                                border = BorderStroke(1.dp, if (selected) ElectricIndigo else CardBorderSoft),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { selectedCategory = cat }
                                    .testTag("library_cat_${cat.id}")
                            ) {
                                Text(
                                    text = "${cat.labelVi} ($countBadge)",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (selected) Color.White else InkSecondary,
                                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    // Tag Filter Chips (shown for notes/articles/all)
                    if (allTags.isNotEmpty() && selectedCategory in setOf(
                            LibraryCategoryFilter.ALL,
                            LibraryCategoryFilter.NOTES,
                            LibraryCategoryFilter.ARTICLES
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            allTags.forEach { tag ->
                                val isSelected = selectedTag.equals(tag, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (isSelected) PracticeTeal else CardSurfaceTinted,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) PracticeTeal else CardBorderSoft
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            selectedTag = if (isSelected) null else tag
                                        }
                                ) {
                                    Text(
                                        text = "#$tag",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) Color.White else TealOnSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3A. Personal Materials List (Tất cả / Ghi chú / Bài đọc tự học)
            if (selectedCategory in setOf(
                    LibraryCategoryFilter.ALL,
                    LibraryCategoryFilter.NOTES,
                    LibraryCategoryFilter.ARTICLES
                )
            ) {
                if (filteredMaterials.isEmpty()) {
                    item(key = "empty_materials_card") {
                        EmptyLibraryPlaceholder(
                            title = "Chưa có tài liệu nào khớp bộ lọc",
                            subtitle = "Tạo bài đọc/ghi chú tự học mới hoặc nhập hàng loạt từ tệp CSV / JSON.",
                            primaryLabel = "Tạo tài liệu mới",
                            onPrimaryClick = { isCreatingNew = true },
                            secondaryLabel = "Nhập CSV / JSON",
                            onSecondaryClick = { onOpenBulkImporter(BulkImportDataset.PERSONAL_MATERIALS) }
                        )
                    }
                } else {
                    items(filteredMaterials, key = { it.id }) { material ->
                        val mod = uiState.modules.firstOrNull { it.id == material.moduleId }
                        PersonalMaterialItemCard(
                            material = material,
                            moduleLabel = mod?.let { "#${it.id} ${it.titleEn}" } ?: "Module #${material.moduleId}",
                            onOpen = { openedMaterialId = material.id },
                            onEdit = { editingMaterial = material },
                            onDelete = { onDeleteMaterial(material.id) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            // 3B. Bộ Flashcard Tab (Grouped by Module from Room SQLite)
            if (selectedCategory == LibraryCategoryFilter.FLASHCARDS) {
                item(key = "flashcard_import_banner") {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = ElectricIndigoLight.copy(alpha = 0.72f),
                        border = BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.28f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Kho Flashcard SQLite (${uiState.allVocabulary.size} từ C1–C2)",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = IndigoOnSurface
                                )
                                Text(
                                    text = "Bao gồm từ gốc, từ nhập CSV/JSON và từ bạn bôi vàng lưu từ bài tự học.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { onOpenBulkImporter(BulkImportDataset.VOCABULARY) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Nhập CSV")
                            }
                        }
                    }
                }

                items(flashcardDecksByModule, key = { "deck_${it.first}" }) { (modId, titleEn, words) ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CardWhite,
                        border = BorderStroke(1.dp, CardBorderSoft),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = ElectricIndigoLight
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.MenuBook,
                                            contentDescription = null,
                                            tint = ElectricIndigo,
                                            modifier = Modifier.padding(8.dp).size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Module #$modId • $titleEn",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = InkPrimary
                                        )
                                        Text(
                                            text = "${words.size} thẻ Flashcard sẵn sàng offline",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = InkMuted
                                        )
                                    }
                                }
                                Button(
                                    onClick = { onJumpToModuleLearn(modId) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElectricIndigo,
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Học Tab 2", style = MaterialTheme.typography.labelMedium)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                words.take(6).forEach { w ->
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = CardSurfaceTinted,
                                        border = BorderStroke(1.dp, CardBorderSoft)
                                    ) {
                                        Text(
                                            text = "${w.word} (${w.cefrLevel})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = InkPrimary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3C. Đề cá nhân Tab (Custom & Imported Exams)
            if (selectedCategory == LibraryCategoryFilter.EXAMS) {
                if (filteredPersonalExams.isEmpty()) {
                    item(key = "empty_personal_exams") {
                        EmptyLibraryPlaceholder(
                            title = "Chưa có Đề thi cá nhân nào",
                            subtitle = "Nhập nhanh bộ đề IELTS bằng file CSV / JSON hoặc dùng AI Exam Parser tại Tab 4 Thi.",
                            primaryLabel = "Nhập Đề CSV / JSON",
                            onPrimaryClick = { onOpenBulkImporter(BulkImportDataset.EXAMS) },
                            secondaryLabel = "Xem tất cả tài liệu",
                            onSecondaryClick = { selectedCategory = LibraryCategoryFilter.ALL }
                        )
                    }
                } else {
                    items(filteredPersonalExams, key = { "pex_${it.id}" }) { exam ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = CardWhite,
                            border = BorderStroke(1.dp, CardBorderSoft),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Assignment,
                                        contentDescription = null,
                                        tint = PracticeTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = exam.title,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = InkPrimary
                                        )
                                        Text(
                                            text = "${exam.sourceLabel} • ${exam.year} • ${exam.topicVi}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = InkMuted
                                        )
                                    }
                                }
                                Button(
                                    onClick = { onStartPersonalExam(exam, ExamSkillMode.FULL_TEST) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PracticeTeal,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Vào phòng thi CD-IELTS ngay")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Active Self-Study Detail Sheet
    val openedMaterial = uiState.personalMaterials.firstOrNull { it.id == openedMaterialId }
    if (openedMaterial != null) {
        val linkedModule = uiState.modules.firstOrNull { it.id == openedMaterial.moduleId }
        PersonalMaterialDetailSheet(
            material = openedMaterial,
            linkedModule = linkedModule,
            aiState = uiState.personalMaterialAiState,
            dailySelfStudyMinutes = uiState.dailySelfStudyMinutes,
            onEditMaterial = { mat ->
                openedMaterialId = null
                editingMaterial = mat
            },
            onSaveFlashcard = onSaveHighlightFlashcard,
            onRequestAiSummary = onSummarizeMaterialWithAi,
            onLogSelfStudyMinutes = onLogSelfStudySession,
            onShowMessage = onShowMessage,
            onDismiss = { openedMaterialId = null }
        )
    }

    // Create / Edit Material Sheet
    if (isCreatingNew || editingMaterial != null) {
        PersonalMaterialEditorSheet(
            existing = editingMaterial,
            modules = uiState.modules,
            defaultModuleId = uiState.activeModuleId,
            onUploadAudioUri = onUploadMaterialAudio,
            onSave = { saved ->
                onSaveMaterial(saved)
                isCreatingNew = false
                editingMaterial = null
            },
            onDismiss = {
                isCreatingNew = false
                editingMaterial = null
            }
        )
    }
}

@Composable
private fun PersonalMaterialItemCard(
    material: PersonalMaterial,
    moduleLabel: String,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, CardBorderSoft),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onOpen)
            .testTag("personal_material_card_${material.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = PracticeTealLight
                    ) {
                        Text(
                            text = material.category,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TealOnSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = ElectricIndigoLight
                    ) {
                        Text(
                            text = moduleLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = IndigoOnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    if (!material.audioUri.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = GoldMasteryLight
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Headphones,
                                contentDescription = "Có tệp Audio",
                                tint = GoldOnContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp).size(13.dp)
                            )
                        }
                    }
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Chỉnh sửa tài liệu",
                            tint = InkSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Xóa tài liệu",
                            tint = InkMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = material.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = InkPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (material.contentEn.isNotBlank()) {
                Text(
                    text = material.contentEn,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
                    color = InkSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    material.tags.take(4).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = CardSurfaceTinted
                        ) {
                            Text(
                                text = "#${tag.removePrefix("#")}",
                                style = MaterialTheme.typography.labelSmall,
                                color = InkMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = EmeraldSuccessLight
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = EmeraldOnContainer,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Mở tự học",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldOnContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyLibraryPlaceholder(
    title: String,
    subtitle: String,
    primaryLabel: String,
    onPrimaryClick: () -> Unit,
    secondaryLabel: String,
    onSecondaryClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, CardBorderSoft),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = ElectricIndigoLight
            ) {
                Icon(
                    imageVector = Icons.Rounded.Book,
                    contentDescription = null,
                    tint = ElectricIndigo,
                    modifier = Modifier.padding(14.dp).size(26.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = InkPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = InkSecondary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onPrimaryClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricIndigo,
                        contentColor = Color.White
                    )
                ) {
                    Text(primaryLabel)
                }
                OutlinedButton(
                    onClick = onSecondaryClick,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(secondaryLabel)
                }
            }
        }
    }
}
