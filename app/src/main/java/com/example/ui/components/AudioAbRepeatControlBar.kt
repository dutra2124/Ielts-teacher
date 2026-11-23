package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.GoldMastery
import com.example.ui.theme.GoldMasteryLight
import com.example.ui.theme.GoldOnContainer
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.PracticeTeal
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseDiffLight
import com.example.ui.theme.SurfaceCanvas
import java.util.Locale

val StandardAudioSpeeds: List<Float> = listOf(0.75f, 1.0f, 1.25f)

/**
 * Compact, high-contrast Playback Speed (0.75x, 1.0x, 1.25x) and A–B Repeat Loop
 * (Điểm A, Điểm B, Tắt lặp) control bar for Listening, Speaking, and Self-Study audio players.
 */
@Composable
fun AudioAbRepeatControlBar(
    playbackSpeed: Float,
    onSelectSpeed: (Float) -> Unit,
    pointALabel: String?,
    pointBLabel: String?,
    isLoopActive: Boolean,
    onSetPointA: () -> Unit,
    onSetPointB: () -> Unit,
    onClearLoop: () -> Unit,
    accentColor: Color = PracticeTeal,
    testTagPrefix: String = "audio_ab",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Speed Selector Pills (0.75x, 1.0x, 1.25x)
        Icon(
            imageVector = Icons.Rounded.Speed,
            contentDescription = "Tốc độ phát",
            tint = InkMuted,
            modifier = Modifier.size(15.dp)
        )
        StandardAudioSpeeds.forEach { speed ->
            val selected = kotlin.math.abs(playbackSpeed - speed) < 0.01f
            val label = formatSpeedLabel(speed)
            Surface(
                shape = RoundedCornerShape(50),
                color = if (selected) accentColor else SurfaceCanvas,
                border = BorderStroke(1.dp, if (selected) accentColor else CardBorderSoft),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .tactilePress(pressedScale = 0.94f)
                    .clickable { onSelectSpeed(speed) }
                    .testTag("${testTagPrefix}_speed_$label")
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = if (selected) Color.White else InkPrimary,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // A-B Repeat Controls: Điểm A, Điểm B, Tắt lặp
        val hasA = pointALabel != null
        val hasB = pointBLabel != null

        Surface(
            shape = RoundedCornerShape(50),
            color = if (hasA) GoldMasteryLight else SurfaceCanvas,
            border = BorderStroke(1.dp, if (hasA) GoldMastery else CardBorderSoft),
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .tactilePress(pressedScale = 0.94f)
                .clickable(onClick = onSetPointA)
                .testTag("${testTagPrefix}_point_a")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.RepeatOne,
                    contentDescription = null,
                    tint = if (hasA) GoldOnContainer else accentColor,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = if (hasA) "A: $pointALabel" else "Điểm A",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = if (hasA) GoldOnContainer else InkPrimary,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(50),
            color = if (isLoopActive && hasB) EmeraldSuccessLight else SurfaceCanvas,
            border = BorderStroke(
                1.dp,
                if (isLoopActive && hasB) EmeraldSuccess else CardBorderSoft
            ),
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .tactilePress(pressedScale = 0.94f)
                .clickable(onClick = onSetPointB)
                .testTag("${testTagPrefix}_point_b")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Repeat,
                    contentDescription = null,
                    tint = if (isLoopActive && hasB) EmeraldOnContainer else accentColor,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = if (hasB) "B: $pointBLabel" else "Điểm B",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = if (isLoopActive && hasB) EmeraldOnContainer else InkPrimary,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        if (hasA || hasB || isLoopActive) {
            Surface(
                shape = RoundedCornerShape(50),
                color = RoseDiffLight,
                border = BorderStroke(1.dp, RoseAlert.copy(alpha = 0.45f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .tactilePress(pressedScale = 0.94f)
                    .clickable(onClick = onClearLoop)
                    .testTag("${testTagPrefix}_clear_loop")
            ) {
                Text(
                    text = "Tắt lặp",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = RoseAlert,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                )
            }
        }
    }
}

private fun formatSpeedLabel(speed: Float): String {
    return when {
        kotlin.math.abs(speed - 0.75f) < 0.01f -> "0.75x"
        kotlin.math.abs(speed - 1.0f) < 0.01f -> "1.0x"
        kotlin.math.abs(speed - 1.25f) < 0.01f -> "1.25x"
        else -> String.format(Locale.US, "%.2fx", speed).replace(".00x", ".0x")
    }
}
