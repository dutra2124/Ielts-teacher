package com.example.ui.screens.review

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.BookmarkAdded
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PracticeSkill
import com.example.data.ReviewErrorLogItem
import com.example.ui.theme.CardBorderSoft
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
import com.example.ui.theme.PracticeTealDark
import com.example.ui.theme.RoseDiff
import com.example.ui.theme.RoseDiffLight
import com.example.ui.theme.SurfaceCanvas

/**
 * Error Log -> Action Loop ("Nhật ký lỗi sai"):
 * Lists all missed Reading & Listening questions with Distractor trap analysis,
 * sentence grammar notes, and 2 one-tap action buttons:
 * - "Đưa từ sai vào SRS" (pushes directly to Tab 2 SRS Queue)
 * - "Luyện lại dạng bài yếu" (jumps straight to Tab 3 pre-filtered to weak skill)
 */
@Composable
fun ReviewErrorLogSection(
    errors: List<ReviewErrorLogItem>,
    srsWordIds: Set<String>,
    onPushWordToSrs: (wordId: String) -> Unit,
    onPracticeWeakSkill: (skill: PracticeSkill, focusNoteVi: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                Icon(Icons.Rounded.ReportProblem, contentDescription = null, tint = RoseDiff, modifier = Modifier.size(20.dp))
                Text(
                    text = stringResource(R.string.review_error_log_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = InkPrimary
                )
            }
            Surface(shape = RoundedCornerShape(50), color = RoseDiffLight) {
                Text(
                    text = "${errors.size} lỗi",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = RoseDiff,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                )
            }
        }

        if (errors.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = EmeraldSuccessLight.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(24.dp))
                    Text(
                        text = "Tuyệt vời! Bạn không mắc lỗi trắc nghiệm nào trong bài thi này. Hãy xem bản nâng cấp Writing/Speaking phía trên để tối ưu Band 8.5+!",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldOnContainer
                    )
                }
            }
        } else {
            errors.forEach { err ->
                val isInSrs = err.linkedVocabWordId in srsWordIds
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = CardWhite,
                    border = BorderStroke(1.dp, CardBorderSoft),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("error_log_item_${err.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Skill + Question Type Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = RoundedCornerShape(8.dp), color = ElectricIndigoLight) {
                                Text(
                                    text = "${err.skill.labelEn} • Câu ${err.questionNumber}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ElectricIndigo,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SurfaceCanvas,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Text(
                                    text = err.questionTypeVi,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = InkSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = err.promptEn,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = InkPrimary
                        )

                        // User Answer vs Correct Answer Strip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = RoseDiffLight,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(9.dp)) {
                                    Text("Bạn chọn:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = RoseDiff)
                                    Text(
                                        text = err.userAnswer,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = RoseDiff,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = EmeraldSuccessLight,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(9.dp)) {
                                    Text("Đáp án đúng:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = EmeraldOnContainer)
                                    Text(
                                        text = err.correctAnswer,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = EmeraldOnContainer,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Distractor Trap Warning
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = GoldMasteryLight.copy(alpha = 0.65f),
                            border = BorderStroke(1.dp, GoldMastery.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = GoldOnContainer, modifier = Modifier.size(16.dp))
                                Text(
                                    text = err.distractorTrapVi,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkPrimary
                                )
                            }
                        }

                        // Sentence Grammar Note
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SurfaceCanvas
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Icon(Icons.Rounded.MenuBook, contentDescription = null, tint = ElectricIndigo, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Ngữ pháp câu: ${err.grammarNoteVi}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkSecondary
                                )
                            }
                        }

                        // Linked C1-C2 Target Word to Push to SRS
                        Text(
                            text = "Từ khóa học thuật liên quan: ${err.linkedVocabWordEn}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = IndigoOnSurface
                        )

                        // 2 One-Tap Action Loop Buttons (Zero Horizontal Overflow)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onPushWordToSrs(err.linkedVocabWordId) },
                                shape = RoundedCornerShape(13.dp),
                                border = BorderStroke(1.dp, if (isInSrs) EmeraldSuccess else IndigoOnSurface),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 9.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("error_push_srs_${err.id}")
                            ) {
                                Icon(
                                    imageVector = if (isInSrs) Icons.Rounded.BookmarkAdded else Icons.Rounded.BookmarkAdd,
                                    contentDescription = null,
                                    tint = if (isInSrs) EmeraldSuccess else IndigoOnSurface,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isInSrs) {
                                        stringResource(R.string.review_in_srs_btn)
                                    } else {
                                        stringResource(R.string.review_push_srs_btn)
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = if (isInSrs) EmeraldOnContainer else IndigoOnSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Button(
                                onClick = {
                                    onPracticeWeakSkill(
                                        err.targetPracticeSkill,
                                        "Ôn lại dạng yếu từ Nhật ký lỗi: ${err.questionTypeVi} (${err.skill.labelEn})"
                                    )
                                },
                                shape = RoundedCornerShape(13.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PracticeTeal,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 9.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("error_practice_weak_${err.id}")
                            ) {
                                Icon(Icons.Rounded.FitnessCenter, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.review_practice_weak_btn),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
