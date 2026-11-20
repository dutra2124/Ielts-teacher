package com.example.ui.components

import android.net.Uri
import android.provider.OpenableColumns
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.BulkImportDataset
import com.example.data.local.BulkImportPreview
import com.example.data.sync.BulkDataImporter
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardSurfaceTinted
import com.example.ui.theme.CardWhite
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.GoldMastery
import com.example.ui.theme.GoldMasteryLight
import com.example.ui.theme.GoldOnContainer
import com.example.ui.theme.IndigoOnSurface
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.PracticeTeal
import com.example.ui.theme.PracticeTealLight
import com.example.ui.theme.RoseDiff
import com.example.ui.theme.RoseDiffLight
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.TealOnSurface

/**
 * In-App CSV / JSON Bulk Importer Bottom Sheet.
 * Supports 4 target datasets (Vocabulary, Sentence Drills, Exams, Personal Materials),
 * UTF-8 Vietnamese CSV parsing, JSON validation, downloadable sample CSV/JSON templates,
 * and a live horizontal preview table before confirming bulk upsert into Room SQLite.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataImporterBottomSheet(
    initialDataset: BulkImportDataset = BulkImportDataset.VOCABULARY,
    onConfirmImport: (BulkImportPreview) -> Unit,
    onShowMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var selectedDataset by remember(initialDataset) { mutableStateOf(initialDataset) }
    var preview by remember { mutableStateOf<BulkImportPreview?>(null) }
    var parseErrorVi by remember { mutableStateOf<String?>(null) }
    var showPasteEditor by remember { mutableStateOf(false) }
    var pastedText by remember { mutableStateOf("") }

    // SAF File Picker for CSV / JSON (Zero storage permissions required)
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                val fileName = resolveDisplayName(context, uri) ?: "imported_data.csv"
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use {
                    it.readText()
                }.orEmpty()
                val result = BulkDataImporter.parseAndPreview(
                    dataset = selectedDataset,
                    rawContent = content,
                    sourceFileName = fileName
                )
                result.onSuccess { parsed ->
                    preview = parsed
                    parseErrorVi = null
                }.onFailure { err ->
                    preview = null
                    parseErrorVi = err.message ?: "Không thể phân tích tệp đã chọn."
                }
            }.onFailure { err ->
                preview = null
                parseErrorVi = "Lỗi đọc tệp: ${err.localizedMessage ?: "Không xác định"}"
            }
        }
    }

    // SAF CreateDocument for Sample CSV
    val exportSampleCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                val sample = "\uFEFF" + BulkDataImporter.sampleCsvFor(selectedDataset)
                context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use {
                    it.write(sample)
                }
                onShowMessage("Đã lưu file mẫu CSV cho mục ${selectedDataset.shortLabelVi}!")
            }.onFailure {
                onShowMessage("Không thể lưu file mẫu CSV: ${it.localizedMessage ?: "Lỗi ghi file"}")
            }
        }
    }

    // SAF CreateDocument for Sample JSON
    val exportSampleJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                val sample = BulkDataImporter.sampleJsonFor(selectedDataset)
                context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use {
                    it.write(sample)
                }
                onShowMessage("Đã lưu file mẫu JSON cho mục ${selectedDataset.shortLabelVi}!")
            }.onFailure {
                onShowMessage("Không thể lưu file mẫu JSON: ${it.localizedMessage ?: "Lỗi ghi file"}")
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceCanvas,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("bulk_data_importer_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.importer_sheet_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = InkPrimary
                    )
                    Text(
                        text = stringResource(R.string.importer_sheet_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_importer_sheet_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Đóng",
                        tint = InkSecondary
                    )
                }
            }

            // Step 1: Select Target Dataset (4 Datasets)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, CardBorderSoft)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "1. Chọn bảng dữ liệu đích (4 loại)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = InkPrimary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BulkImportDataset.entries.forEach { dataset ->
                            val selected = dataset == selectedDataset
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (selected) ElectricIndigo else CardSurfaceTinted,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (selected) ElectricIndigo else CardBorderSoft
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        selectedDataset = dataset
                                        preview = null
                                        parseErrorVi = null
                                    }
                                    .testTag("importer_dataset_${dataset.id}")
                            ) {
                                Text(
                                    text = dataset.labelVi,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (selected) Color.White else InkSecondary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                                )
                            }
                        }
                    }

                    // Column Schema Hint
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ElectricIndigoLight.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "Cột chuẩn: ${selectedDataset.requiredColumnsHint}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.5.sp
                            ),
                            color = IndigoOnSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }

                    // Download Sample CSV / JSON Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                exportSampleCsvLauncher.launch("${selectedDataset.sampleFilePrefix}.csv")
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("download_sample_csv_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = stringResource(R.string.importer_sample_csv_btn),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                exportSampleJsonLauncher.launch("${selectedDataset.sampleFilePrefix}.json")
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("download_sample_json_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Code,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = stringResource(R.string.importer_sample_json_btn),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            // Step 2: Pick File or Paste Directly
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, CardBorderSoft)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "2. Tải tệp từ máy hoặc dán nhanh CSV / JSON",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = InkPrimary
                    )

                    Button(
                        onClick = {
                            importFileLauncher.launch(
                                arrayOf(
                                    "text/csv",
                                    "text/comma-separated-values",
                                    "application/json",
                                    "text/plain",
                                    "*/*"
                                )
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricIndigo,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("pick_csv_json_file_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.UploadFile,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.importer_pick_file_btn),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showPasteEditor = !showPasteEditor },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("toggle_paste_csv_json_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ContentPaste,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (showPasteEditor) "Ẩn ô dán văn bản" else "Dán trực tiếp CSV / JSON",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val sampleCsv = BulkDataImporter.sampleCsvFor(selectedDataset)
                                BulkDataImporter.parseAndPreview(
                                    dataset = selectedDataset,
                                    rawContent = sampleCsv,
                                    sourceFileName = "${selectedDataset.sampleFilePrefix}.csv"
                                ).onSuccess { parsed ->
                                    preview = parsed
                                    parseErrorVi = null
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("load_sample_preview_btn")
                        ) {
                            Text(
                                text = "Thử mẫu",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    AnimatedVisibility(visible = showPasteEditor) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = pastedText,
                                onValueChange = { pastedText = it },
                                label = { Text("Dán nội dung CSV (UTF-8) hoặc JSON tại đây") },
                                minLines = 4,
                                maxLines = 7,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("paste_csv_json_input")
                            )
                            Button(
                                onClick = {
                                    BulkDataImporter.parseAndPreview(
                                        dataset = selectedDataset,
                                        rawContent = pastedText,
                                        sourceFileName = "pasted_data"
                                    ).onSuccess { parsed ->
                                        preview = parsed
                                        parseErrorVi = null
                                    }.onFailure { err ->
                                        preview = null
                                        parseErrorVi = err.message ?: "Dữ liệu dán vào không hợp lệ."
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PracticeTeal,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("parse_pasted_data_btn")
                            ) {
                                Text("Phân tích & Xem trước bảng dữ liệu")
                            }
                        }
                    }
                }
            }

            // Validation Error Banner
            if (parseErrorVi != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = RoseDiffLight,
                    border = BorderStroke(1.dp, RoseDiff.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = RoseDiff,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = parseErrorVi.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = InkPrimary
                        )
                    }
                }
            }

            // Step 3: Live Preview Table + Confirm Import
            val currentPreview = preview
            if (currentPreview != null) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardWhite,
                    border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.45f)),
                    modifier = Modifier.testTag("importer_preview_table_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.TableChart,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "3. Xem trước dữ liệu (${currentPreview.formatLabel})",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = InkPrimary
                                    )
                                    Text(
                                        text = currentPreview.sourceFileName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = InkMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = EmeraldSuccessLight
                            ) {
                                Text(
                                    text = "${currentPreview.validRowCount} dòng hợp lệ",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldOnContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (!currentPreview.warningVi.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GoldMasteryLight
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.WarningAmber,
                                        contentDescription = null,
                                        tint = GoldMastery,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = currentPreview.warningVi,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GoldOnContainer
                                    )
                                }
                            }
                        }

                        // Horizontal Scrollable Preview Table
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(CardSurfaceTinted)
                                .horizontalScroll(rememberScrollState())
                        ) {
                            // Header Row
                            Row(
                                modifier = Modifier
                                    .background(PracticeTealLight)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                currentPreview.headers.forEach { header ->
                                    Text(
                                        text = header,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = TealOnSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.width(116.dp)
                                    )
                                }
                            }
                            HorizontalDivider(color = CardBorderSoft)

                            // Data Rows
                            currentPreview.previewRows.forEachIndexed { idx, cells ->
                                Row(
                                    modifier = Modifier
                                        .background(if (idx % 2 == 0) CardWhite else CardSurfaceTinted)
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    cells.forEach { cell ->
                                        Text(
                                            text = cell.ifBlank { "—" },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = InkPrimary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.width(116.dp)
                                        )
                                    }
                                }
                                if (idx < currentPreview.previewRows.lastIndex) {
                                    HorizontalDivider(color = CardBorderSoft.copy(alpha = 0.6f))
                                }
                            }
                        }

                        if (currentPreview.validRowCount > currentPreview.previewRows.size) {
                            Text(
                                text = "Đang hiển thị ${currentPreview.previewRows.size}/${currentPreview.validRowCount} dòng đầu tiên...",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkMuted
                            )
                        }

                        Button(
                            onClick = { onConfirmImport(currentPreview) },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldSuccess,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("confirm_bulk_import_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Xác nhận nhập ${currentPreview.validRowCount} mục vào SQLite",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun resolveDisplayName(context: android.content.Context, uri: Uri): String? {
    return runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) cursor.getString(idx) else null
            } else null
        }
    }.getOrNull()
}
