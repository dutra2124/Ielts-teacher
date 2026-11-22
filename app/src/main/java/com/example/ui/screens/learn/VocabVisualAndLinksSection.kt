package com.example.ui.screens.learn

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.ImageSearch
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.VocabImageRepository
import com.example.data.VocabImageUiState
import com.example.data.VocabWordItem
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanGlowLight
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricIndigoLight
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.IndigoOnSurface
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.PracticeTeal
import com.example.ui.theme.PracticeTealLight
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.TealOnSurface
import com.example.ui.theme.VividViolet
import java.util.Locale

private enum class DictionaryProvider(val shortLabel: String, val fullName: String) {
    CAMBRIDGE("Cambridge", "Cambridge Dictionary"),
    OXFORD("Oxford", "Oxford Learner's")
}

/**
 * Rounded Auto-Fetched Illustration Thumbnail for a Vocabulary Flashcard:
 * - Resolves image URL via VocabImageRepository (Google CSE if configured, else zero-config Wikipedia/Wikimedia Commons)
 * - Displays smooth animated Shimmer placeholder while loading
 * - Displays crisp Coil SubcomposeAsyncImage with source badge when loaded
 * - Displays clean thematic vector fallback when offline or unavailable
 */
@Composable
fun VocabIllustrationPreview(
    word: VocabWordItem,
    topicHintEn: String,
    isOnline: Boolean,
    googleCseApiKey: String,
    googleCseCx: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var retryNonce by remember(word.id) { mutableIntStateOf(0) }
    var imageState by remember(word.id, googleCseApiKey, googleCseCx) {
        mutableStateOf<VocabImageUiState>(VocabImageUiState.Loading)
    }

    LaunchedEffect(word.id, topicHintEn, googleCseApiKey, googleCseCx, retryNonce) {
        imageState = VocabImageUiState.Loading
        imageState = VocabImageRepository.resolveWordImage(
            context = context,
            wordEn = word.word,
            topicHintEn = topicHintEn,
            googleCseApiKey = googleCseApiKey,
            googleCseCx = googleCseCx,
            forceRefresh = retryNonce > 0
        )
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = SurfaceCanvas,
        border = BorderStroke(1.dp, CardBorderSoft),
        modifier = modifier
            .fillMaxWidth()
            .height(152.dp)
            .clip(RoundedCornerShape(18.dp))
            .testTag("vocab_illustration_${word.id}")
    ) {
        when (val state = imageState) {
            is VocabImageUiState.Loading -> {
                VocabShimmerPlaceholder(wordEn = word.word)
            }

            is VocabImageUiState.Loaded -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(state.image.imageUrl)
                            .crossfade(true)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        contentDescription = "Hình minh họa từ vựng ${word.word}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            VocabShimmerPlaceholder(wordEn = word.word)
                        },
                        error = {
                            VocabVectorFallbackCard(
                                word = word,
                                topicHintEn = topicHintEn,
                                isOffline = !isOnline,
                                onRetry = { retryNonce++ }
                            )
                        }
                    )

                    // Subtle bottom scrim so source badge stays legible over bright photos
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.58f))
                                )
                            )
                    )

                    // Source Pill + Subtle Concept Label
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.Black.copy(alpha = 0.52f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Image,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Minh họa • ${state.image.sourceLabelVi}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.48f),
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .clickable { retryNonce++ }
                                .testTag("vocab_illustration_refresh_${word.id}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Tải lại ảnh minh họa",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            is VocabImageUiState.Fallback -> {
                VocabVectorFallbackCard(
                    word = word,
                    topicHintEn = topicHintEn,
                    isOffline = !isOnline,
                    onRetry = { retryNonce++ }
                )
            }
        }
    }
}

@Composable
private fun VocabShimmerPlaceholder(wordEn: String) {
    val transition = rememberInfiniteTransition(label = "vocab_img_shimmer")
    val shimmerOffset by transition.animateFloat(
        initialValue = -350f,
        targetValue = 950f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vocab_shimmer_x"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            ElectricIndigoLight.copy(alpha = 0.45f),
            CardWhite.copy(alpha = 0.85f),
            CyanGlowLight.copy(alpha = 0.55f)
        ),
        start = Offset(shimmerOffset, 0f),
        end = Offset(shimmerOffset + 320f, 180f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(shimmerBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Image,
                contentDescription = null,
                tint = IndigoOnSurface.copy(alpha = 0.55f),
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "Đang tìm ảnh minh họa cho \"$wordEn\"...",
                style = MaterialTheme.typography.labelSmall,
                color = IndigoOnSurface.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun VocabVectorFallbackCard(
    word: VocabWordItem,
    topicHintEn: String,
    isOffline: Boolean,
    onRetry: () -> Unit
) {
    val fallbackBrush = Brush.linearGradient(
        colors = listOf(
            ElectricIndigo.copy(alpha = 0.14f),
            VividViolet.copy(alpha = 0.12f),
            CyanGlow.copy(alpha = 0.18f)
        ),
        start = Offset(0f, 0f),
        end = Offset(600f, 300f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(fallbackBrush)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ElectricIndigoLight,
                border = BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.28f)),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isOffline) Icons.Rounded.WifiOff else Icons.Rounded.AutoStories,
                        contentDescription = null,
                        tint = IndigoOnSurface,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = topicHintEn.ifBlank { "IELTS Academic Concept" },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = IndigoOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = InkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isOffline) {
                        "Đang ngoại tuyến • Kết nối mạng để tải ảnh Wikipedia"
                    } else {
                        "Minh họa ngữ cảnh ${word.cefrLevel} • Bấm Ảnh Google để xem thêm"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = InkSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                shape = CircleShape,
                color = CardWhite,
                border = BorderStroke(1.dp, CardBorderSoft),
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onRetry)
                    .testTag("vocab_fallback_retry_${word.id}")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Thử tải lại ảnh",
                        tint = IndigoOnSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Compact, responsive 1-tap External Reference Links row on each Vocabulary Card:
 * - Wikipedia: https://en.wikipedia.org/wiki/{word}
 * - Tra từ điển (Cambridge / Oxford quick toggle): Cambridge or Oxford Learner's Dictionary
 * - Ảnh Google: https://www.google.com/search?tbm=isch&q={word}+meaning+illustration
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VocabExternalLinksRow(
    word: VocabWordItem,
    onShowMessage: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val toolbarColorInt = ElectricIndigo.toArgb()
    var selectedDictName by rememberSaveable { mutableStateOf(DictionaryProvider.CAMBRIDGE.name) }
    val selectedDict = remember(selectedDictName) {
        DictionaryProvider.entries.firstOrNull { it.name == selectedDictName } ?: DictionaryProvider.CAMBRIDGE
    }

    val cleanTerm = remember(word.word) {
        word.word.substringBefore("(").substringBefore("/").trim()
    }
    val wikiSlug = remember(cleanTerm) { cleanTerm.replace(" ", "_") }
    val dictSlug = remember(cleanTerm) {
        cleanTerm.lowercase(Locale.US).replace(Regex("[^a-z0-9\\s-]"), "").trim().replace(Regex("\\s+"), "-")
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceCanvas,
        border = BorderStroke(1.dp, CardBorderSoft),
        modifier = modifier
            .fillMaxWidth()
            .testTag("vocab_external_links_${word.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tra cứu mở rộng 1 chạm",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = InkSecondary
                )

                // Compact Cambridge <-> Oxford Dictionary Toggle
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CardWhite,
                    border = BorderStroke(1.dp, CardBorderSoft)
                ) {
                    Row(
                        modifier = Modifier.padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DictionaryProvider.entries.forEach { provider ->
                            val active = provider == selectedDict
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (active) ElectricIndigo else Color.Transparent)
                                    .clickable { selectedDictName = provider.name }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                    .testTag("dict_toggle_${provider.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = provider.shortLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (active) Color.White else InkMuted,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                ExternalRefChip(
                    icon = Icons.Rounded.Public,
                    label = "Wikipedia",
                    containerColor = ElectricIndigoLight,
                    contentColor = IndigoOnSurface,
                    tag = "vocab_link_wikipedia_${word.id}",
                    onClick = {
                        val url = "https://en.wikipedia.org/wiki/${Uri.encode(wikiSlug)}"
                        openReferenceInBrowser(context, url, toolbarColorInt, onShowMessage)
                    }
                )

                ExternalRefChip(
                    icon = Icons.Rounded.MenuBook,
                    label = "Từ điển (${selectedDict.shortLabel})",
                    containerColor = PracticeTealLight,
                    contentColor = TealOnSurface,
                    tag = "vocab_link_dictionary_${word.id}",
                    onClick = {
                        val url = when (selectedDict) {
                            DictionaryProvider.CAMBRIDGE ->
                                "https://dictionary.cambridge.org/dictionary/english/${Uri.encode(dictSlug)}"
                            DictionaryProvider.OXFORD ->
                                "https://www.oxfordlearnersdictionaries.com/definition/english/${Uri.encode(dictSlug)}"
                        }
                        openReferenceInBrowser(context, url, toolbarColorInt, onShowMessage)
                    }
                )

                ExternalRefChip(
                    icon = Icons.Rounded.ImageSearch,
                    label = "Ảnh Google",
                    containerColor = EmeraldSuccessLight,
                    contentColor = EmeraldOnContainer,
                    tag = "vocab_link_google_images_${word.id}",
                    onClick = {
                        val query = Uri.encode("$cleanTerm meaning illustration")
                        val url = "https://www.google.com/search?tbm=isch&q=$query"
                        openReferenceInBrowser(context, url, toolbarColorInt, onShowMessage)
                    }
                )
            }
        }
    }
}

@Composable
private fun ExternalRefChip(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.22f)),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .testTag(tag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp
                ),
                color = contentColor,
                maxLines = 1,
                softWrap = false
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.72f),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

/**
 * Launches the target HTTPS reference URL using Chrome Custom Tabs protocol extras when supported,
 * seamlessly falling back to Android's default browser via Intent.ACTION_VIEW.
 */
private fun openReferenceInBrowser(
    context: Context,
    url: String,
    toolbarColorArgb: Int,
    onShowMessage: ((String) -> Unit)?
) {
    val uri = Uri.parse(url)
    val customTabIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val extras = Bundle().apply {
            putBinder("android.support.customtabs.extra.SESSION", null)
        }
        putExtras(extras)
        putExtra("android.support.customtabs.extra.TOOLBAR_COLOR", toolbarColorArgb)
        putExtra("android.support.customtabs.extra.TITLE_VISIBILITY", 1)
    }

    val opened = runCatching {
        context.startActivity(customTabIntent)
        true
    }.getOrElse {
        runCatching {
            val fallback = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
            true
        }.getOrDefault(false)
    }

    if (!opened) {
        onShowMessage?.invoke("Không tìm thấy trình duyệt để mở liên kết tra cứu.")
    }
}
