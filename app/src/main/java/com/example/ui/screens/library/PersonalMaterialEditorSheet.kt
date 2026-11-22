package com.example.ui.screens.library

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.IeltsModule
import com.example.data.local.LibraryCategoryFilter
import com.example.data.local.PersonalMaterial
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardSurfaceTinted
import com.example.ui.theme.CardWhite
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.IndigoOnSurface
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.PracticeTeal
import com.example.ui.theme.PracticeTealLight
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.TealOnSurface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private val SuggestedMaterialTags = listOf(
    "paraphrase",
    "writing-task2",
    "reading",
    "speaking-part3",
    "band-8.5",
    "collocations"
)

/**
 * Create / Edit Modal Bottom Sheet for Personal Self-Study Materials.
 * Supports title, linked Module 1-12, category, English content, Vietnamese notes,
 * optional Audio URL or local MP3/WAV upload (via SAF -> app-private storage), and #tags.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalMaterialEditorSheet(
    existing: PersonalMaterial?,
    modules: List<IeltsModule>,
    defaultModuleId: Int,
    onUploadAudioUri: (Uri, (localPath: String, displayName: String) -> Unit) -> Unit,
    onSave: (PersonalMaterial) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember(existing) { mutableStateOf(existing?.title.orEmpty()) }
    var selectedModuleId by remember(existing) { mutableIntStateOf(existing?.moduleId ?: defaultModuleId) }
    var category by remember(existing) {
        mutableStateOf(existing?.category?.ifBlank { "Ghi chú" } ?: "Ghi chú")
    }
    var contentEn by remember(existing) { mutableStateOf(existing?.contentEn.orEmpty()) }
    var notesVi by remember(existing) { mutableStateOf(existing?.notesVi.orEmpty()) }
    var tagsRaw by remember(existing) {
        mutableStateOf(existing?.tags?.joinToString(", ") { it.removePrefix("#") }.orEmpty())
    }
    var audioUri by remember(existing) { mutableStateOf(existing?.audioUri.orEmpty()) }
    var audioDisplayName by remember(existing) { mutableStateOf(existing?.audioDisplayName.orEmpty()) }
    var isUploadingAudio by remember { mutableStateOf(false) }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploadingAudio = true
            onUploadAudioUri(uri) { savedPath, displayName ->
                audioUri = savedPath
                audioDisplayName = displayName
                isUploadingAudio = false
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceCanvas,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("personal_material_editor_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (existing == null) "Tạo tài liệu tự học mới" else "Chỉnh sửa tài liệu cá nhân",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = InkPrimary
                    )
                    Text(
                        text = "Lưu cục bộ vào SQLite — hỗ trợ bôi từ tạo Flashcard & AI tóm tắt",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Đóng", tint = InkSecondary)
                }
            }

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tiêu đề tài liệu *") },
                placeholder = { Text("VD: Bài đọc mẫu — Carbon Pricing & Urban Policy") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("material_editor_title_input")
            )

            // Category Pills
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Phân loại tài liệu",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = InkPrimary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LibraryCategoryFilter.editableMaterialCategories.forEach { cat ->
                        val selected = category.equals(cat, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (selected) PracticeTeal else CardWhite,
                            border = BorderStroke(1.dp, if (selected) PracticeTeal else CardBorderSoft),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { category = cat }
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (selected) Color.White else InkSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // Linked Module 1-12 Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Gắn với Chủ đề (Module 1–12)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = InkPrimary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    modules.forEach { mod ->
                        val selected = mod.id == selectedModuleId
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (selected) ElectricIndigo else CardWhite,
                            border = BorderStroke(1.dp, if (selected) ElectricIndigo else CardBorderSoft),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { selectedModuleId = mod.id }
                        ) {
                            Text(
                                text = "#${mod.id} ${mod.titleEn}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (selected) Color.White else InkSecondary,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // Rich English Content
            OutlinedTextField(
                value = contentEn,
                onValueChange = { contentEn = it },
                label = { Text("Nội dung bài đọc / Đoạn văn mẫu (Tiếng Anh)") },
                placeholder = { Text("Dán bài báo, đoạn Writing mẫu, hoặc transcript Speaking tại đây...") },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("material_editor_content_input")
            )

            // Vietnamese Notes
            OutlinedTextField(
                value = notesVi,
                onValueChange = { notesVi = it },
                label = { Text("Ghi chú tự học (Tiếng Việt)") },
                placeholder = { Text("Ghi chú ngữ pháp, cách paraphrase, lưu ý phát âm...") },
                minLines = 2,
                maxLines = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("material_editor_notes_input")
            )

            // Optional Audio Attachment (MP3/WAV upload OR URL)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, CardBorderSoft)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Âm thanh luyện nghe (Tùy chọn)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = InkPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { audioPickerLauncher.launch(arrayOf("audio/*")) },
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isUploadingAudio,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("material_upload_audio_btn")
                        ) {
                            Icon(Icons.Rounded.AudioFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isUploadingAudio) "Đang lưu audio..." else "Tải MP3 / WAV từ máy",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        if (audioUri.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    audioUri = ""
                                    audioDisplayName = ""
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Gỡ tệp audio",
                                    tint = InkMuted
                                )
                            }
                        }
                    }

                    if (audioDisplayName.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PracticeTealLight
                        ) {
                            Text(
                                text = "Đã gắn tệp offline: $audioDisplayName",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = TealOnSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = audioUri,
                        onValueChange = {
                            audioUri = it
                            if (it.startsWith("http")) {
                                audioDisplayName = it.substringAfterLast('/').ifBlank { "Liên kết Audio" }
                            }
                        },
                        label = { Text("Hoặc dán URL tệp audio (.mp3 / .wav)") },
                        leadingIcon = { Icon(Icons.Rounded.Link, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Tags + Quick Tag Chips
            OutlinedTextField(
                value = tagsRaw,
                onValueChange = { tagsRaw = it },
                label = { Text("Thẻ phân loại (#tags, cách nhau dấu phẩy)") },
                placeholder = { Text("VD: paraphrase, writing-task2, education") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("material_editor_tags_input")
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SuggestedMaterialTags.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = ElectricIndigoLight,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                val currentList = tagsRaw.split(",")
                                    .map { it.trim().removePrefix("#") }
                                    .filter { it.isNotEmpty() }
                                    .toMutableList()
                                if (currentList.none { it.equals(tag, ignoreCase = true) }) {
                                    currentList.add(tag)
                                    tagsRaw = currentList.joinToString(", ")
                                }
                            }
                    ) {
                        Text(
                            text = "+ #$tag",
                            style = MaterialTheme.typography.labelSmall,
                            color = IndigoOnSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    val cleanTitle = title.trim().ifBlank { "Tài liệu tự học IELTS" }
                    val parsedTags = tagsRaw.split(",", ";", "|")
                        .map { it.trim().removePrefix("#") }
                        .filter { it.isNotEmpty() }
                        .distinct()
                        .ifEmpty { listOf("tu-hoc") }
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    onSave(
                        PersonalMaterial(
                            id = existing?.id ?: "pm_${UUID.randomUUID().toString().take(8)}",
                            moduleId = selectedModuleId,
                            title = cleanTitle,
                            category = category,
                            contentEn = contentEn.trim(),
                            notesVi = notesVi.trim(),
                            tags = parsedTags,
                            updatedAt = today,
                            audioUri = audioUri.trim().ifBlank { null },
                            audioDisplayName = audioDisplayName.trim().ifBlank { null }
                        )
                    )
                },
                enabled = title.isNotBlank() || contentEn.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricIndigo,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_personal_material_btn")
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (existing == null) "Lưu vào Thư viện cá nhân" else "Cập nhật tài liệu",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
