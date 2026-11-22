package com.example.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.MaterialImportAiService
import com.example.data.importer.DocumentFileKind
import com.example.data.importer.DocumentTextExtractor
import com.example.data.importer.ExtractedDocumentResult
import com.example.data.importer.MaterialDirectConverter
import com.example.data.importer.MaterialImportCategory
import com.example.data.local.BulkImportPreview
import com.example.data.sync.BulkDataImporter
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardSurfaceTinted
import com.example.ui.theme.CardWhite
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LocalAdaptiveLayoutInfo
import com.example.ui.theme.LocalApexColors
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.ThemeHeroBrush
import com.example.ui.theme.ThemeOnPrimary
import com.example.ui.theme.ThemePrimary
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter
import java.util.Locale

/**
 * Centralized "Thêm tài liệu" (Add Materials Hub) Screen accessed from Account Sidebar.
 * Features:
 * 1. Categorized 6-type IELTS Material Selector with exam purpose descriptions.
 * 2. Native SAF Document Picker supporting PDF (pdfbox-android), DOCX (ZipInputStream + XmlPullParser),
 *    CSV, JSON, TXT, Link, and MP3/WAV Audio attachment.
 * 3. Two clear post-extraction options:
 *    - "Lưu trực tiếp vào mục đã chọn" (100% offline local conversion)
 *    - "Dùng AI bóc tách tự động thành Câu hỏi / Từ vựng chuẩn" (Gemini AI structured extraction)
 *    both displaying a Live Preview before persisting into Room SQLite.
 */
@Composable
fun AddMaterialsScreen(
    activeModuleId: Int,
    onUploadAudioUri: (Uri, (localPath: String, displayName: String) -> Unit) -> Unit,
    onConfirmImport: (BulkImportPreview) -> Unit,
    onOpenPersonalLibrary: () -> Unit,
    onShowMessage: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onClose() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val layoutInfo = LocalAdaptiveLayoutInfo.current
    val apexColors = LocalApexColors.current
    val primary = ThemePrimary
    val onPrimary = ThemeOnPrimary
    val highContrast = apexColors.isHighContrast

    val aiExtractor = remember { MaterialImportAiService() }

    var selectedCategory by rememberSaveable { mutableStateOf(MaterialImportCategory.VOCABULARY) }
    var targetModuleId by rememberSaveable { mutableIntStateOf(activeModuleId.coerceIn(1, 12)) }
    var pastedOrExtractedText by rememberSaveable { mutableStateOf("") }
    var extractedDoc by remember { mutableStateOf<ExtractedDocumentResult?>(null) }
    var attachedAudioPath by rememberSaveable { mutableStateOf<String?>(null) }
    var attachedAudioName by rememberSaveable { mutableStateOf<String?>(null) }

    var isExtractingFile by remember { mutableStateOf(false) }
    var isAiStructuring by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var livePreview by remember { mutableStateOf<BulkImportPreview?>(null) }

    // Android Storage Access Framework (SAF) Document Picker (zero storage permissions!)
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        errorMessage = null
        livePreview = null

        val mime = context.contentResolver.getType(uri).orEmpty().lowercase(Locale.US)
        val displayName = DocumentTextExtractor.resolveDisplayName(context, uri)
        val kind = DocumentTextExtractor.classifyFileKind(displayName, mime)

        if (kind == DocumentFileKind.AUDIO) {
            onUploadAudioUri(uri) { localPath, savedName ->
                attachedAudioPath = localPath
                attachedAudioName = savedName
            }
            return@rememberLauncherForActivityResult
        }

        scope.launch {
            isExtractingFile = true
            val result = DocumentTextExtractor.extractFromUri(context, uri)
            isExtractingFile = false
            result.onSuccess { doc ->
                extractedDoc = doc
                pastedOrExtractedText = doc.extractedText
                onShowMessage("Đã đọc thành công ${doc.fileKind.badgeLabel}: ${doc.fileName} (${doc.wordCount} từ)")
            }.onFailure { err ->
                errorMessage = err.message ?: "Không thể trích xuất nội dung từ tệp này."
            }
        }
    }

    // Dedicated Audio Picker (MP3/WAV) for Listening Exam & Personal Self-Study
    val openAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onUploadAudioUri(uri) { localPath, savedName ->
                attachedAudioPath = localPath
                attachedAudioName = savedName
            }
        }
    }

    // Download sample CSV / JSON templates cleanly via SAF CreateDocument
    val saveSampleCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            runCatching {
                val sample = "\uFEFF" + BulkDataImporter.sampleCsvFor(selectedCategory.targetDataset)
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    OutputStreamWriter(out, Charsets.UTF_8).use { it.write(sample) }
                }
            }.onSuccess {
                onShowMessage("Đã tải file mẫu CSV cho ${selectedCategory.titleVi}!")
            }.onFailure {
                onShowMessage("Không thể lưu file mẫu CSV: ${it.message}")
            }
        }
    }

    val saveSampleJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            runCatching {
                val sample = BulkDataImporter.sampleJsonFor(selectedCategory.targetDataset)
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    OutputStreamWriter(out, Charsets.UTF_8).use { it.write(sample) }
                }
            }.onSuccess {
                onShowMessage("Đã tải file mẫu JSON cho ${selectedCategory.titleVi}!")
            }.onFailure {
                onShowMessage("Không thể lưu file mẫu JSON: ${it.message}")
            }
        }
    }

    val effectiveFileName = extractedDoc?.fileName ?: "${selectedCategory.id}_nhap_tay.txt"
    val effectiveKind = extractedDoc?.fileKind ?: run {
        val t = pastedOrExtractedText.trim()
        when {
            t.startsWith("http://", ignoreCase = true) || t.startsWith("https://", ignoreCase = true) -> DocumentFileKind.LINK
            t.startsWith("[") || t.startsWith("{") -> DocumentFileKind.JSON
            else -> DocumentFileKind.TEXT
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("add_materials_screen"),
        color = SurfaceCanvas
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Top Branded Header Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ThemeHeroBrush)
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("add_materials_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = apexColors.heroInk
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Thêm tài liệu",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = apexColors.heroInk
                        )
                        Text(
                            text = "Master Ielts • PDF, DOCX, CSV, JSON & Audio Offline",
                            style = MaterialTheme.typography.bodySmall,
                            color = apexColors.heroInk.copy(alpha = 0.82f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = apexColors.heroInk.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, apexColors.heroInk.copy(alpha = 0.34f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                onClose()
                                onOpenPersonalLibrary()
                            }
                            .testTag("add_materials_open_library_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Book,
                                contentDescription = null,
                                tint = apexColors.heroInk,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Thư viện",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = apexColors.heroInk
                            )
                        }
                    }
                }
            }

            // Scrollable 3-Step Workspace
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = layoutInfo.horizontalPadding, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // STEP 1: Categorized Material Type Selector (6 IELTS Categories)
                SectionStepHeading(
                    stepNumber = "1",
                    title = "Chọn loại tài liệu IELTS muốn nạp",
                    subtitle = "Mỗi mục được tối ưu riêng cho từng phần thi và tiêu chí chấm điểm IELTS"
                )

                val categories = MaterialImportCategory.entries
                if (layoutInfo.useMultiColumnGrid) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        categories.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                pair.forEach { cat ->
                                    MaterialCategoryCard(
                                        category = cat,
                                        selected = cat == selectedCategory,
                                        onClick = {
                                            selectedCategory = cat
                                            livePreview = null
                                            errorMessage = null
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        categories.forEach { cat ->
                            MaterialCategoryCard(
                                category = cat,
                                selected = cat == selectedCategory,
                                onClick = {
                                    selectedCategory = cat
                                    livePreview = null
                                    errorMessage = null
                                }
                            )
                        }
                    }
                }

                // Target Module Picker for Vocabulary / Sentence Drills / Personal Self-Study
                if (selectedCategory == MaterialImportCategory.VOCABULARY ||
                    selectedCategory == MaterialImportCategory.SENTENCE_DRILLS ||
                    selectedCategory == MaterialImportCategory.PERSONAL_SELF_STUDY
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = CardWhite,
                        border = BorderStroke(1.dp, CardBorderSoft)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Gắn vào Module lộ trình (Module #${String.format(Locale.US, "%02d", targetModuleId)})",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = InkPrimary
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                (1..12).forEach { mId ->
                                    val selected = mId == targetModuleId
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (selected) primary else SurfaceCanvas,
                                        border = BorderStroke(1.dp, if (selected) primary else CardBorderSoft),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { targetModuleId = mId }
                                    ) {
                                        Text(
                                            text = "M${String.format(Locale.US, "%02d", mId)}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (selected) onPrimary else InkSecondary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // STEP 2: Native File Picker (PDF, DOCX, CSV, JSON, TXT, Audio) + Direct Paste/Link
                SectionStepHeading(
                    stepNumber = "2",
                    title = "Nạp tệp PDF, DOCX, CSV, JSON hoặc dán nội dung",
                    subtitle = "Trích xuất văn bản PDF & DOCX trực tiếp trên thiết bị — không cần cấp quyền bộ nhớ"
                )

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = CardWhite,
                    border = BorderStroke(if (highContrast) 1.5.dp else 1.dp, CardBorderSoft)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                openDocumentLauncher.launch(DocumentTextExtractor.SUPPORTED_DOCUMENT_MIME_TYPES)
                            },
                            enabled = !isExtractingFile && !isAiStructuring,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primary,
                                contentColor = onPrimary
                            ),
                            border = if (highContrast) BorderStroke(1.5.dp, onPrimary.copy(alpha = 0.5f)) else null,
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 13.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("pick_material_document_btn")
                        ) {
                            if (isExtractingFile) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Đang trích xuất nội dung tệp...",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.FolderOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Chọn tệp PDF, DOCX, CSV, JSON, TXT",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                                )
                            }
                        }

                        // Dedicated MP3/WAV Audio Picker for Listening & Personal Self-Study
                        if (selectedCategory.supportsAudioAttachment) {
                            OutlinedButton(
                                onClick = { openAudioLauncher.launch(arrayOf("audio/*")) },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.2.dp, primary),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("attach_material_audio_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AudioFile,
                                    contentDescription = null,
                                    tint = primary,
                                    modifier = Modifier.size(19.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (attachedAudioName != null) {
                                        "Audio đính kèm: $attachedAudioName"
                                    } else {
                                        "Đính kèm Audio MP3 / WAV (cho Listening / Tự học)"
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Extracted File Status Chip
                        AnimatedVisibility(visible = extractedDoc != null) {
                            extractedDoc?.let { doc ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = EmeraldSuccessLight,
                                    border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Description,
                                            contentDescription = null,
                                            tint = EmeraldSuccess,
                                            modifier = Modifier.size(19.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = doc.fileName,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                color = InkPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            val meta = buildString {
                                                append("Định dạng ${doc.fileKind.badgeLabel}")
                                                if (doc.pageCount > 0) append(" • ${doc.pageCount} trang")
                                                append(" • ${doc.wordCount} từ trích xuất")
                                            }
                                            Text(
                                                text = meta,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = InkSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Paste / Edit Extracted Text or Link
                        OutlinedTextField(
                            value = pastedOrExtractedText,
                            onValueChange = {
                                pastedOrExtractedText = it
                                livePreview = null
                                errorMessage = null
                            },
                            label = {
                                Text(
                                    if (selectedCategory.supportsLinkInput) {
                                        "Nội dung đã trích xuất hoặc dán văn bản / Link bài báo"
                                    } else {
                                        "Nội dung đã trích xuất (có thể chỉnh sửa hoặc dán trực tiếp)"
                                    }
                                )
                            },
                            placeholder = {
                                Text(
                                    "Nội dung từ PDF / DOCX sẽ hiển thị tại đây ngay khi bạn chọn tệp...",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            minLines = 4,
                            maxLines = 8,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primary,
                                unfocusedBorderColor = CardBorderSoft
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("material_extracted_text_field")
                        )

                        // Quick CSV/JSON Templates Utility Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TemplateChipButton(
                                icon = Icons.Rounded.Download,
                                label = "Tải mẫu CSV",
                                onClick = {
                                    saveSampleCsvLauncher.launch("${selectedCategory.targetDataset.sampleFilePrefix}.csv")
                                }
                            )
                            TemplateChipButton(
                                icon = Icons.Rounded.Download,
                                label = "Tải mẫu JSON",
                                onClick = {
                                    saveSampleJsonLauncher.launch("${selectedCategory.targetDataset.sampleFilePrefix}.json")
                                }
                            )
                            TemplateChipButton(
                                icon = Icons.Rounded.UploadFile,
                                label = "Điền mẫu thử nhanh",
                                onClick = {
                                    val sample = BulkDataImporter.sampleCsvFor(selectedCategory.targetDataset)
                                    pastedOrExtractedText = sample
                                    extractedDoc = ExtractedDocumentResult(
                                        fileName = "${selectedCategory.targetDataset.sampleFilePrefix}.csv",
                                        fileKind = DocumentFileKind.CSV,
                                        extractedText = sample,
                                        wordCount = sample.split(Regex("\\s+")).size,
                                        charCount = sample.length
                                    )
                                    livePreview = null
                                    errorMessage = null
                                }
                            )
                        }
                    }
                }

                // Error Banner if any
                AnimatedVisibility(visible = errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = RoseAlert.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, RoseAlert.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = RoseAlert,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = errorMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = RoseAlert
                            )
                        }
                    }
                }

                // STEP 3: Two Clear Post-Extraction Processing Options
                SectionStepHeading(
                    stepNumber = "3",
                    title = "Chọn cách xử lý & Xem trước trước khi lưu",
                    subtitle = "Xem trước trực tiếp mọi bản ghi rồi mới lưu vào cơ sở dữ liệu Offline"
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Option 1: Direct Save into Selected Category (100% Offline)
                    OutlinedButton(
                        onClick = {
                            errorMessage = null
                            val res = MaterialDirectConverter.buildDirectPreview(
                                category = selectedCategory,
                                extractedText = pastedOrExtractedText,
                                sourceFileName = effectiveFileName,
                                fileKind = effectiveKind,
                                moduleId = targetModuleId,
                                attachedAudioPath = attachedAudioPath,
                                attachedAudioName = attachedAudioName
                            )
                            res.onSuccess { preview ->
                                livePreview = preview
                            }.onFailure { err ->
                                errorMessage = err.message ?: "Không thể tạo bản xem trước trực tiếp."
                            }
                        },
                        enabled = pastedOrExtractedText.isNotBlank() && !isAiStructuring && !isExtractingFile,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(if (highContrast) 2.dp else 1.5.dp, primary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 13.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("option_direct_save_preview_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SaveAlt,
                            contentDescription = null,
                            tint = primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lưu trực tiếp vào mục đã chọn",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = primary
                        )
                    }

                    // Option 2: AI Auto-Structure into Standard Questions / C1-C2 Vocabulary
                    Button(
                        onClick = {
                            errorMessage = null
                            livePreview = null
                            scope.launch {
                                isAiStructuring = true
                                runCatching {
                                    aiExtractor.extractStructuredPreview(
                                        category = selectedCategory,
                                        rawText = pastedOrExtractedText,
                                        sourceFileName = effectiveFileName,
                                        fileKind = effectiveKind,
                                        moduleId = targetModuleId,
                                        attachedAudioPath = attachedAudioPath,
                                        attachedAudioName = attachedAudioName
                                    )
                                }.onSuccess { preview ->
                                    livePreview = preview
                                    onShowMessage("AI đã bóc tách xong ${preview.validRowCount} mục chuẩn — kiểm tra bản xem trước bên dưới!")
                                }.onFailure { err ->
                                    errorMessage = err.message ?: "AI chưa thể bóc tách tài liệu lúc này."
                                }
                                isAiStructuring = false
                            }
                        },
                        enabled = pastedOrExtractedText.isNotBlank() && !isAiStructuring && !isExtractingFile,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primary,
                            contentColor = onPrimary
                        ),
                        border = if (highContrast) BorderStroke(1.5.dp, onPrimary.copy(alpha = 0.55f)) else null,
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 13.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("option_ai_extract_preview_btn")
                    ) {
                        if (isAiStructuring) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI đang bóc tách Câu hỏi / Từ vựng chuẩn...",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Dùng AI bóc tách tự động thành Câu hỏi / Từ vựng chuẩn",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                maxLines = 2
                            )
                        }
                    }
                }

                // Live Preview Panel before saving to Room DB
                AnimatedVisibility(visible = livePreview != null) {
                    livePreview?.let { preview ->
                        LiveImportPreviewCard(
                            preview = preview,
                            category = selectedCategory,
                            onConfirmSave = {
                                onConfirmImport(preview)
                                livePreview = null
                                pastedOrExtractedText = ""
                                extractedDoc = null
                                attachedAudioPath = null
                                attachedAudioName = null
                                onClose()
                            },
                            onClearPreview = { livePreview = null }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SectionStepHeading(
    stepNumber: String,
    title: String,
    subtitle: String
) {
    val primary = ThemePrimary
    val onPrimary = ThemeOnPrimary
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = onPrimary
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = InkPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted
            )
        }
    }
}

@Composable
private fun TemplateChipButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardSurfaceTinted,
        border = BorderStroke(1.dp, CardBorderSoft),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ThemePrimary,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = InkPrimary
            )
        }
    }
}
