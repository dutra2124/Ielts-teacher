package com.example.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import android.os.Build
import androidx.compose.ui.draw.blur
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

/**
 * Copies a gallery image picked via zero-permission Android Photo Picker (`PickVisualMedia`)
 * into private app storage (downsampled to <= 1600px) so the wallpaper persists reliably
 * across device restarts without requiring any broad storage permissions.
 */
object WallpaperStorageHelper {
    private const val DIR_NAME = "custom_wallpapers"
    private const val FILE_NAME = "master_ielts_bg.jpg"

    suspend fun persistPickedWallpaper(context: Context, sourceUri: Uri): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(sourceUri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, boundsOpts)
                }
                val maxDim = max(boundsOpts.outWidth, boundsOpts.outHeight).coerceAtLeast(1)
                var sampleSize = 1
                while (maxDim / sampleSize > 1600) {
                    sampleSize *= 2
                }

                val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                val bitmap = context.contentResolver.openInputStream(sourceUri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOpts)
                } ?: error("Không thể đọc tệp ảnh từ thư viện thiết bị.")

                val dir = File(context.filesDir, DIR_NAME).apply { mkdirs() }
                val outFile = File(dir, "bg_${System.currentTimeMillis()}.jpg")
                // Clean old wallpapers so storage stays tiny
                dir.listFiles()?.forEach { old ->
                    if (old.name != outFile.name) old.delete()
                }
                FileOutputStream(outFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 86, out)
                }
                bitmap.recycle()
                outFile.absolutePath
            }
        }

    fun clearPersistedWallpapers(context: Context) {
        runCatching {
            val dir = File(context.filesDir, DIR_NAME)
            dir.listFiles()?.forEach { it.delete() }
        }
    }

    suspend fun loadBitmapFromPath(path: String): ImageBitmap? = withContext(Dispatchers.IO) {
        if (path.isBlank()) return@withContext null
        val file = File(path)
        if (!file.exists()) return@withContext null
        runCatching {
            BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
        }.getOrNull()
    }
}

/**
 * Renders the reactive Master Ielts backdrop:
 * - Base SurfaceCanvas color
 * - Either the user's custom gallery wallpaper OR one of the 4 built-in subtle wallpaper patterns
 * - Adjustable dim / scrim overlay (0% - 85%) to guarantee foreground legibility
 * - Optional soft blur (Android 12+) & frosted depth veil when Selective Glassmorphism is enabled
 */
@Composable
fun AppBackgroundBackdrop(
    themePrefs: AppThemePreferences,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalApexColors.current
    var galleryBitmap by remember(themePrefs.customBackgroundPath) {
        mutableStateOf<ImageBitmap?>(null)
    }

    LaunchedEffect(themePrefs.customBackgroundPath) {
        galleryBitmap = WallpaperStorageHelper.loadBitmapFromPath(themePrefs.customBackgroundPath)
    }

    val overlayAlpha = themePrefs.backgroundOverlayOpacity.coerceIn(0f, 0.85f)
    val glassActive = themePrefs.glassmorphismEnabled && !palette.isHighContrast
    val blurModifier = if (glassActive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.blur(radius = 12.dp)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.surfaceCanvas)
    ) {
        // Layer 1: Custom Gallery Photo OR Built-in Procedural Pattern (or soft glass aura)
        if (galleryBitmap != null) {
            Image(
                bitmap = galleryBitmap!!,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .then(blurModifier)
            )
            // Adaptive Dim/Overlay Scrim (0% - 85%) for crisp foreground readability
            val effectiveScrim = if (glassActive) {
                (overlayAlpha * 0.82f).coerceIn(0.14f, 0.78f)
            } else {
                overlayAlpha
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(palette.surfaceCanvas.copy(alpha = effectiveScrim))
            )
        } else if (themePrefs.wallpaperPattern != AppWallpaperPattern.NONE) {
            WallpaperPatternCanvas(
                pattern = themePrefs.wallpaperPattern,
                primary = palette.primaryAccent,
                secondary = palette.secondaryAccent,
                surface = palette.surfaceCanvas,
                isDark = palette.isDark,
                strength = (1f - overlayAlpha * 0.72f).coerceIn(0.25f, 1f),
                modifier = Modifier
                    .fillMaxSize()
                    .then(blurModifier)
            )
        } else if (glassActive) {
            // Subtle luminous ambient orbs behind glass cards when no wallpaper is chosen
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .then(blurModifier)
            ) {
                val w = size.width
                val h = size.height
                val orbAlpha = if (palette.isDark) 0.22f else 0.14f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(palette.primaryAccent.copy(alpha = orbAlpha), Color.Transparent),
                        center = Offset(w * 0.14f, h * 0.14f),
                        radius = w * 0.72f
                    ),
                    radius = w * 0.72f,
                    center = Offset(w * 0.14f, h * 0.14f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(palette.secondaryAccent.copy(alpha = orbAlpha * 0.9f), Color.Transparent),
                        center = Offset(w * 0.88f, h * 0.45f),
                        radius = w * 0.68f
                    ),
                    radius = w * 0.68f,
                    center = Offset(w * 0.88f, h * 0.45f)
                )
            }
        }

        // Layer 2: App Foreground Content
        content()
    }
}

/**
 * Reusable miniature or fullscreen renderer for the 4 built-in subtle aesthetic wallpaper patterns.
 */
@Composable
fun WallpaperPatternCanvas(
    pattern: AppWallpaperPattern,
    primary: Color,
    secondary: Color,
    surface: Color,
    isDark: Boolean,
    strength: Float = 0.85f,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.background(surface)) {
        val w = size.width
        val h = size.height
        val baseAlpha = (if (isDark) 0.22f else 0.14f) * strength

        when (pattern) {
            AppWallpaperPattern.NONE -> Unit

            AppWallpaperPattern.ACADEMIC_GRID -> {
                val step = 28.dp.toPx().coerceAtLeast(16f)
                val dotColor = primary.copy(alpha = baseAlpha * 1.4f)
                val lineColor = primary.copy(alpha = baseAlpha * 0.45f)
                var x = step
                while (x < w) {
                    drawLine(lineColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                    x += step
                }
                var y = step
                while (y < h) {
                    drawLine(lineColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                    y += step
                }
                var gx = step
                while (gx < w) {
                    var gy = step
                    while (gy < h) {
                        drawCircle(dotColor, radius = 2.dp.toPx(), center = Offset(gx, gy))
                        gy += step
                    }
                    gx += step
                }
            }

            AppWallpaperPattern.AURORA_WAVES -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primary.copy(alpha = baseAlpha * 2.0f), Color.Transparent),
                        center = Offset(w * 0.15f, h * 0.18f),
                        radius = w * 0.78f
                    ),
                    radius = w * 0.78f,
                    center = Offset(w * 0.15f, h * 0.18f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(secondary.copy(alpha = baseAlpha * 1.9f), Color.Transparent),
                        center = Offset(w * 0.88f, h * 0.62f),
                        radius = w * 0.75f
                    ),
                    radius = w * 0.75f,
                    center = Offset(w * 0.88f, h * 0.62f)
                )
                val wave1 = Path().apply {
                    moveTo(0f, h * 0.56f)
                    cubicTo(w * 0.30f, h * 0.44f, w * 0.65f, h * 0.70f, w, h * 0.54f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(wave1, color = primary.copy(alpha = baseAlpha * 0.65f))
                val wave2 = Path().apply {
                    moveTo(0f, h * 0.74f)
                    cubicTo(w * 0.38f, h * 0.86f, w * 0.70f, h * 0.64f, w, h * 0.78f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(wave2, color = secondary.copy(alpha = baseAlpha * 0.55f))
            }

            AppWallpaperPattern.GEOMETRIC_PRISM -> {
                val strokeW = 1.6.dp.toPx()
                val step = (w.coerceAtLeast(120f) / 4.5f)
                var offset = -h
                while (offset < w + h) {
                    drawLine(
                        color = primary.copy(alpha = baseAlpha * 0.85f),
                        start = Offset(offset, 0f),
                        end = Offset(offset + h * 0.65f, h),
                        strokeWidth = strokeW
                    )
                    offset += step
                }
                val prism = Path().apply {
                    moveTo(w * 0.68f, 0f)
                    lineTo(w, 0f)
                    lineTo(w, h * 0.42f)
                    close()
                }
                drawPath(prism, color = secondary.copy(alpha = baseAlpha * 0.9f))
                val prism2 = Path().apply {
                    moveTo(0f, h * 0.62f)
                    lineTo(w * 0.45f, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(prism2, color = primary.copy(alpha = baseAlpha * 0.75f))
            }

            AppWallpaperPattern.BOTANICAL_RINGS -> {
                val cx1 = w * 0.86f
                val cy1 = h * 0.16f
                listOf(0.18f, 0.32f, 0.46f, 0.62f).forEachIndexed { i, frac ->
                    drawCircle(
                        color = if (i % 2 == 0) primary.copy(alpha = baseAlpha * 0.95f) else secondary.copy(alpha = baseAlpha * 0.8f),
                        radius = w * frac,
                        center = Offset(cx1, cy1),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
                val cx2 = w * 0.10f
                val cy2 = h * 0.85f
                listOf(0.16f, 0.30f, 0.45f).forEachIndexed { i, frac ->
                    drawCircle(
                        color = if (i % 2 == 0) secondary.copy(alpha = baseAlpha * 0.9f) else primary.copy(alpha = baseAlpha * 0.75f),
                        radius = w * frac,
                        center = Offset(cx2, cy2),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }
        }
    }
}
