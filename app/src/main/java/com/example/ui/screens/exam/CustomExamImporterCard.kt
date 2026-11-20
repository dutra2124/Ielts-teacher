package com.example.ui.screens.exam

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ai.ExamParserUiState
import com.example.data.ExamAttemptRecord
import com.example.data.ExamPaper
import com.example.data.ExamQuestionItem
import com.example.data.ExamSerialization
import com.example.data.ExamSkillMode
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CdIeltsTopbar
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.IndigoOnSurface
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.PracticeTeal
import com.example.ui.theme.PracticeTealDark
import com.example.ui.theme.PracticeTealLight
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseDiffLight
import com.example.ui.theme.SurfaceCanvas
import java.util.Locale

/**
 * Khu vực 2 — Nạp Bộ đề Có sẵn (CustomExamImporter) & Thư viện Đề Cá nhân.
 * Uses Android SAF OpenDocument/CreateDocument (zero storage permissions needed).
 */
@Composable
fun CustomExamImporterSection(
    parserState: ExamParserUiState,
    customExams: List<ExamPaper>,
    examHistory: List<ExamAttemptRecord>,
    selectedMode: ExamSkillMode,
    onParseWithAi: (rawText: String, pdfBytes: ByteArray?, fileName: String?, audioUri: String?, audioName: String?) -> Unit,
    onSaveCustomExam: (ExamPaper) -> Unit,
    onDeleteCustomExam: (String) -> Unit,
    onStartCustomExam: (ExamPaper, ExamSkillMode) -> Unit,
    onResetParser: () -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var pastedText by remember { mutableStateOf("") }
    var attachedDocName by remember { mutableStateOf<String?>(null) }
    var attachedPdfBytes by remember { mutableStateOf<ByteArray?>(null) }
    var attachedAudioUri by remember { mutableStateOf<String?>(null) }
    var attachedAudioName by remember { mutableStateOf<String?>(null) }

    // Zero-permission SAF picker for PDF, DOCX, TXT, JSON
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            ExamFileHelper.readExamDocument(context, uri)
                .onSuccess { doc ->
                    attachedDocName = doc.displayName
                    attachedPdfBytes = doc.pdfBytes
                    if (doc.extractedText.isNotBlank()) {
                        pastedText = doc.extractedText
                    }
                    onShowToast("Đã nạp tệp \"${doc.displayName}\" — Bấm Số hóa bằng AI ngay!")
                }
                .onFailure { err ->
                    onShowToast(err.message ?: "Không thể đọc tệp đã chọn.")
                }
        }
    }

    // Zero-permission SAF picker for MP3 / WAV Listening Audio
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            val name = ExamFileHelper.getDisplayName(context, uri)
            attachedAudioUri = uri.toString()
            attachedAudioName = name
            onShowToast("Đã đính kèm audio Listening: $name")
        }
    }

    // Zero-permission SAF export for Standard JSON Sample Template
    val exportSampleJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            ExamFileHelper.writeTextToUri(context, uri, ExamSerialization.SAMPLE_JSON_TEMPLATE)
                .onSuccess {
                    onShowToast("Đã lưu file mẫu apex_ielts_sample.json thành công!")
                }
                .onFailure {
                    onShowToast("Không thể lưu file mẫu JSON.")
                }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Importer Upload & AI Parser Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, CardBorderSoft),
            modifier = Modifier.testTag("custom_exam_importer_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Nạp Bộ đề Có sẵn & Số hóa AI",
                            style = MaterialTheme.typography.titleMedium,
                            color = InkPrimary
                        )
                        Text(
                            text = "Hỗ trợ PDF, DOCX, TXT, JSON, âm thanh MP3/WAV hoặc dán văn bản đề thi",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                }

                // File Pickers Row 1: Document + Sample JSON Download
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            documentPickerLauncher.launch(
                                arrayOf(
                                    "application/pdf",
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                    "text/plain",
                                    "application/json",
                                    "*/*"
                                )
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, ElectricIndigo),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("importer_pick_document_button")
                    ) {
                        Icon(Icons.Rounded.UploadFile, contentDescription = null, tint = ElectricIndigo, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PDF/DOCX/TXT/JSON",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricIndigo
                        )
                    }

                    OutlinedButton(
                        onClick = { exportSampleJsonLauncher.launch("apex_ielts_sample_exam.json") },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, PracticeTeal),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("importer_download_sample_json")
                    ) {
                        Icon(Icons.Rounded.Download, contentDescription = null, tint = PracticeTealDark, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Mẫu JSON",
                            style = MaterialTheme.typography.labelSmall,
                            color = PracticeTealDark
                        )
                    }
                }

                // File Pickers Row 2: MP3/WAV Audio + Instant Load Sample JSON into text box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            audioPickerLauncher.launch(arrayOf("audio/mpeg", "audio/wav", "audio/x-wav", "audio/*"))
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CardBorderSoft),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("importer_pick_audio_button")
                    ) {
                        Icon(Icons.Rounded.AudioFile, contentDescription = null, tint = CdIeltsTopbar, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = attachedAudioName ?: stringResource(R.string.exam_pick_audio_btn),
                            style = MaterialTheme.typography.labelSmall,
                            color = InkPrimary,
                            maxLines = 1
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ElectricIndigoLight,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                pastedText = ExamSerialization.SAMPLE_JSON_TEMPLATE
                                attachedDocName = "apex_ielts_sample_exam.json"
                                attachedPdfBytes = null
                                onShowToast("Đã nạp sẵn nội dung mẫu JSON — Bấm Số hóa để xem trước!")
                            }
                            .testTag("importer_prefill_sample")
                    ) {
                        Text(
                            text = "Nạp mẫu thử",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricIndigo,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 13.dp)
                        )
                    }
                }

                // Attached File Badges
                if (attachedDocName != null || attachedAudioName != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        attachedDocName?.let { name ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = EmeraldSuccessLight
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                ) {
                                    Icon(Icons.Rounded.Description, contentDescription = null, tint = EmeraldOnContainer, modifier = Modifier.size(14.dp))
                                    Text(
                                        text = if (attachedPdfBytes != null) "$name (PDF)" else name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldOnContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Direct Paste Exam Text Input
                OutlinedTextField(
                    value = pastedText,
                    onValueChange = {
                        pastedText = it
                        if (attachedPdfBytes != null && it.isNotBlank()) {
                            attachedPdfBytes = null
                        }
                    },
                    minLines = 4,
                    maxLines = 7,
                    placeholder = {
                        Text(
                            "Dán trực tiếp văn bản bài đọc, câu hỏi TFNG/MCQ/Điền từ và đáp án Answer Key (hoặc dán JSON chuẩn) vào đây...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricIndigo,
                        unfocusedBorderColor = CardBorderSoft,
                        focusedContainerColor = SurfaceCanvas.copy(alpha = 0.5f),
                        unfocusedContainerColor = SurfaceCanvas.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("importer_paste_text_input")
                )

                Button(
                    onClick = {
                        onParseWithAi(
                            pastedText,
                            attachedPdfBytes,
                            attachedDocName,
                            attachedAudioUri,
                            attachedAudioName
                        )
                    },
                    enabled = parserState !is ExamParserUiState.Parsing &&
                        (pastedText.isNotBlank() || attachedPdfBytes != null),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricIndigo,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 13.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("importer_ai_parse_button")
                ) {
                    if (parserState is ExamParserUiState.Parsing) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini AI Exam Parser đang số hóa đề...")
                    } else {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.exam_ai_parse_btn))
                    }
                }
            }
        }

        // Parser Output: Error OR Preview + Quick-Edit Card
        when (parserState) {
            ExamParserUiState.Idle -> Unit
            ExamParserUiState.Parsing -> Unit
            is ExamParserUiState.Error -> {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = RoseDiffLight,
                    border = BorderStroke(1.dp, RoseAlert.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = RoseAlert)
                        Text(parserState.messageVi, style = MaterialTheme.typography.bodySmall, color = InkPrimary)
                    }
                }
            }

            is ExamParserUiState.PreviewReady -> {
                ParsedExamPreviewEditorCard(
                    parsed = parserState.parsedExam,
                    onSave = { editedExam ->
                        onSaveCustomExam(editedExam)
                        pastedText = ""
                        attachedDocName = null
                        attachedPdfBytes = null
                        attachedAudioUri = null
                        attachedAudioName = null
                    },
                    onDiscard = onResetParser
                )
            }
        }

        // Personal Exam Library (Thư viện đề cá nhân)
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, CardBorderSoft),
            modifier = Modifier.testTag("personal_exam_library_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = IndigoOnSurface, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Thư viện đề cá nhân (${customExams.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = InkPrimary
                        )
                    }
                }

                if (customExams.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceCanvas
                    ) {
                        Text(
                            text = "Chưa có đề cá nhân nào. Hãy tải tệp PDF/DOCX/TXT/JSON hoặc bấm \"Nạp mẫu thử\" rồi \"Số hóa bằng AI\" để lưu bộ đề đầu tiên!",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    customExams.forEach { exam ->
                        val attempts = examHistory.filter { it.examId == exam.id }
                        val bestBand = attempts.maxOfOrNull { it.estimatedBand }
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = SurfaceCanvas,
                            border = BorderStroke(1.dp, CardBorderSoft),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_exam_item_${exam.id}")
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
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = exam.title,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = PlusJakartaSansFontFamily,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = InkPrimary
                                        )
                                        Text(
                                            text = "${exam.topicVi} • ${exam.questions.size} câu hỏi" +
                                                (exam.audioDisplayName?.let { " • Audio: $it" } ?: ""),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = InkSecondary
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteCustomExam(exam.id) },
                                        modifier = Modifier.testTag("delete_custom_exam_${exam.id}")
                                    ) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Xóa đề cá nhân", tint = RoseAlert)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (bestBand != null) {
                                            "Điểm cao nhất: Band ${String.format(Locale.US, "%.1f", bestBand)} (${attempts.size} lần)"
                                        } else {
                                            "Chưa làm bài thi"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (bestBand != null) EmeraldSuccess else InkMuted
                                    )

                                    Button(
                                        onClick = { onStartCustomExam(exam, selectedMode) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = CdIeltsTopbar),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                        modifier = Modifier.testTag("start_custom_exam_${exam.id}")
                                    ) {
                                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Thi ngay", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParsedExamPreviewEditorCard(
    parsed: ExamPaper,
    onSave: (ExamPaper) -> Unit,
    onDiscard: () -> Unit
) {
    var editedTitle by remember(parsed.id) { mutableStateOf(parsed.title) }
    var editedTopicVi by remember(parsed.id) { mutableStateOf(parsed.topicVi) }
    val editedAnswers = remember(parsed.id) {
        mutableStateListOf<ExamQuestionItem>().apply { addAll(parsed.questions) }
    }

    LaunchedEffect(parsed.id) {
        editedTitle = parsed.title
        editedTopicVi = parsed.topicVi
        editedAnswers.clear()
        editedAnswers.addAll(parsed.questions)
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = CardWhite,
        border = BorderStroke(1.5.dp, EmeraldSuccess),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("parsed_exam_preview_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(20.dp))
                Text(
                    text = "Xem trước & Sửa nhanh đề vừa số hóa",
                    style = MaterialTheme.typography.titleMedium,
                    color = EmeraldOnContainer
                )
            }

            OutlinedTextField(
                value = editedTitle,
                onValueChange = { editedTitle = it },
                label = { Text("Tên bộ đề") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = editedTopicVi,
                onValueChange = { editedTopicVi = it },
                label = { Text("Chủ đề (Tiếng Việt)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Surface(shape = RoundedCornerShape(14.dp), color = SurfaceCanvas) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(11.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Bài đọc: ${parsed.readingTitle} (${parsed.readingParagraphs.size} đoạn)",
                        style = MaterialTheme.typography.labelMedium,
                        color = InkPrimary
                    )
                    Text(
                        text = "Số câu hỏi tương tác: ${editedAnswers.size} câu • Script nghe: ${parsed.listeningScript.size} câu",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )
                }
            }

            HorizontalDivider(color = CardBorderSoft)

            Text(
                text = "Kiểm tra nhanh Đáp án (Answer Key):",
                style = MaterialTheme.typography.labelMedium,
                color = InkPrimary
            )

            editedAnswers.forEachIndexed { idx, q ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Câu ${q.number} [${q.skill.labelEn} • ${q.type.labelVi}]: ${q.promptEn}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = PlusJakartaSansFontFamily),
                        color = InkPrimary
                    )
                    OutlinedTextField(
                        value = q.correctAnswer,
                        onValueChange = { newAns ->
                            editedAnswers[idx] = q.copy(
                                correctAnswer = newAns,
                                acceptableAnswers = listOf(newAns)
                            )
                        },
                        label = { Text("Đáp án Câu ${q.number}") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onSave(
                            parsed.copy(
                                title = editedTitle.ifBlank { parsed.title },
                                topicVi = editedTopicVi.ifBlank { parsed.topicVi },
                                questions = editedAnswers.toList()
                            )
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("save_parsed_exam_button")
                ) {
                    Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.exam_save_library_btn))
                }

                OutlinedButton(
                    onClick = onDiscard,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text("Hủy", color = InkSecondary)
                }
            }
        }
    }
}
