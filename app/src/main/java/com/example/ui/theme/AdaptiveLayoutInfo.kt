package com.example.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 Window Width Size Classes for Master Ielts adaptive layouts.
 */
enum class AdaptiveWindowSizeClass {
    COMPACT,  // < 600dp (phones in portrait, narrow split-screen)
    MEDIUM,   // 600dp .. 839dp (foldables, small tablets, phone landscape)
    EXPANDED  // >= 840dp (tablets, large landscape, desktop/ChromeOS)
}

/**
 * Lightweight adaptive layout descriptor derived from live container constraints
 * (`BoxWithConstraints`) and `LocalConfiguration.current`.
 *
 * Ensures Master Ielts adapts cleanly to 16:9, 18:9, 19.5:9, 20:9, 21:9,
 * landscape, multi-window split-screen, foldables, and tablets without fixed aspect locks.
 */
@Immutable
data class AdaptiveLayoutInfo(
    val widthDp: Dp = 412.dp,
    val heightDp: Dp = 892.dp,
    val sizeClass: AdaptiveWindowSizeClass = AdaptiveWindowSizeClass.COMPACT,
    val isLandscape: Boolean = false
) {
    /** Very narrow portrait phones or half split-screen panes (< 380dp). */
    val isNarrowCompact: Boolean
        get() = widthDp < 380.dp

    /** Short aspect ratio screens (e.g., 16:9 phones, landscape, or multi-window split). */
    val isShortHeight: Boolean
        get() = heightDp < 640.dp

    /** Switch bottom navigation bar to left-side NavigationRail on >= 600dp or landscape. */
    val useNavigationRail: Boolean
        get() = sizeClass != AdaptiveWindowSizeClass.COMPACT || isLandscape

    /** Switch vertical stacked panes to side-by-side Left/Right panes on >= 600dp or landscape. */
    val useSideBySideSplit: Boolean
        get() = sizeClass != AdaptiveWindowSizeClass.COMPACT || isLandscape

    /** Switch single-column card stacks to adaptive multi-column grids on >= 600dp or landscape. */
    val useMultiColumnGrid: Boolean
        get() = sizeClass != AdaptiveWindowSizeClass.COMPACT || isLandscape

    /** Tighter 12.dp outer padding when width < 380dp; generous 16.dp–20.dp on wider displays. */
    val horizontalPadding: Dp
        get() = when {
            widthDp < 380.dp -> 12.dp
            sizeClass == AdaptiveWindowSizeClass.COMPACT -> 16.dp
            else -> 20.dp
        }

    /** Adaptive card minimum width for GridCells.Adaptive(minSize = 300.dp). */
    val adaptiveCardMinWidth: Dp
        get() = 300.dp
}

val LocalAdaptiveLayoutInfo = staticCompositionLocalOf { AdaptiveLayoutInfo() }

/**
 * Measures the available viewport via [BoxWithConstraints] combined with [LocalConfiguration]
 * and provides [AdaptiveLayoutInfo] down the entire Compose tree.
 */
@Composable
fun ProvideAdaptiveLayoutInfo(
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable (AdaptiveLayoutInfo) -> Unit
) {
    val configuration = LocalConfiguration.current
    BoxWithConstraints(modifier = modifier) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight
        val sizeClass = when {
            availableWidth >= 840.dp -> AdaptiveWindowSizeClass.EXPANDED
            availableWidth >= 600.dp -> AdaptiveWindowSizeClass.MEDIUM
            else -> AdaptiveWindowSizeClass.COMPACT
        }
        val isLandscape = availableWidth > availableHeight ||
            (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE && availableWidth >= 480.dp)

        val info = AdaptiveLayoutInfo(
            widthDp = availableWidth,
            heightDp = availableHeight,
            sizeClass = sizeClass,
            isLandscape = isLandscape
        )

        CompositionLocalProvider(LocalAdaptiveLayoutInfo provides info) {
            content(info)
        }
    }
}
