package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.IeltsModule
import com.example.data.RoadmapTier
import com.example.ui.components.GlassSurface
import com.example.ui.components.tactilePress
import com.example.ui.theme.CardBorderSoft
import com.example.ui.theme.CardSurfaceTinted
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.IndigoOnSurface
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LocalApexColors
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.PracticeTeal
import com.example.ui.theme.StreakOrange
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.ThemeOnPrimary
import com.example.ui.theme.ThemePrimary
import com.example.ui.theme.ThemeSecondary
import java.util.Locale

data class ModuleVisualMetrics(
    val vocabPercent: Int,
    val drillsPercent: Int,
    val examPercent: Int
) {
    val overallPercent: Int
        get() = ((vocabPercent + drillsPercent + examPercent) / 3).coerceIn(0, 100)
}

/**
 * Animated 3-Tier Journey Progress Strip showing smooth lighting-up progress across
 * Tier 1 (6.0) -> Tier 2 (7.0) -> Tier 3 (8.0–8.5) with a gliding energy orb.
 */
@Composable
internal fun AnimatedTierJourneyStrip(
    completedCount: Int,
    totalCount: Int,
    activeModuleId: Int,
    modifier: Modifier = Modifier
) {
    val apexColors = LocalApexColors.current
    val primary = ThemePrimary
    val secondary = ThemeSecondary
    val trackBg = SurfaceCanvas
    val borderCol = CardBorderSoft

    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    // Combine completed progress with active module position so the user always sees their journey illuminated
    val progressTarget = remember(completedCount, totalCount, activeModuleId) {
        val byCompletion = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
        val byActive = ((activeModuleId - 1).coerceAtLeast(0).toFloat() + 0.5f) / totalCount.coerceAtLeast(1).toFloat()
        maxOf(byCompletion, byActive * 0.85f).coerceIn(0.08f, 1f)
    }

    val animatedFill by animateFloatAsState(
        targetValue = if (entered) progressTarget else 0.04f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "tier_journey_fill"
    )
    val animatedPct by animateIntAsState(
        targetValue = if (entered) (completedCount * 100 / totalCount.coerceAtLeast(1)) else 0,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "tier_journey_pct"
    )

    val infinite = rememberInfiniteTransition(label = "tier_rail_shimmer")
    val orbPhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tier_orb_phase"
    )

    val activeTierIndex = when {
        activeModuleId <= 4 -> 0
        activeModuleId <= 8 -> 1
        else -> 2
    }

    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("animated_tier_journey_strip"),
        shape = RoundedCornerShape(22.dp),
        solidColor = CardWhite,
        solidBorder = BorderStroke(if (apexColors.isHighContrast) 1.5.dp else 1.dp, CardBorderSoft)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Hành trình Band 6.0 → 8.5",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = InkPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = primary.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, primary.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "$completedCount/$totalCount • $animatedPct%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = primary,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }
            }

            // Animated Glowing Progress Rail Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
            ) {
                val y = size.height / 2f
                val startX = 14.dp.toPx()
                val endX = size.width - 14.dp.toPx()
                val span = (endX - startX).coerceAtLeast(1f)
                val litEndX = startX + span * animatedFill

                // Base dashed track
                drawLine(
                    color = trackBg,
                    start = Offset(startX, y),
                    end = Offset(endX, y),
                    strokeWidth = 7.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Lit gradient journey line
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(primary, secondary, CyanGlow),
                        startX = startX,
                        endX = endX
                    ),
                    start = Offset(startX, y),
                    end = Offset(litEndX, y),
                    strokeWidth = 7.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Gliding energy pulse along the lit segment
                val pulseX = startX + (litEndX - startX) * orbPhase
                drawCircle(
                    color = Color.White.copy(alpha = 0.72f),
                    radius = 5.5.dp.toPx(),
                    center = Offset(pulseX, y)
                )

                // 3 Tier milestone nodes along the rail (0.0, 0.5, 1.0)
                listOf(0f, 0.5f, 1f).forEachIndexed { idx, frac ->
                    val cx = startX + span * frac
                    val reached = animatedFill >= frac - 0.02f || idx <= activeTierIndex
                    val isCurrent = idx == activeTierIndex
                    if (isCurrent) {
                        drawCircle(
                            color = primary.copy(alpha = 0.24f),
                            radius = 11.dp.toPx(),
                            center = Offset(cx, y)
                        )
                    }
                    drawCircle(
                        color = if (reached) primary else borderCol,
                        radius = if (isCurrent) 6.5.dp.toPx() else 5.dp.toPx(),
                        center = Offset(cx, y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.4.dp.toPx(),
                        center = Offset(cx, y)
                    )
                }
            }

            // Tier labels aligned underneath nodes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TierMilestoneCaption(
                    tierLabel = "Tier 1",
                    bandLabel = "6.0–6.5",
                    isActive = activeTierIndex == 0,
                    isReached = true,
                    alignment = Alignment.Start
                )
                TierMilestoneCaption(
                    tierLabel = "Tier 2",
                    bandLabel = "7.0–7.5",
                    isActive = activeTierIndex == 1,
                    isReached = activeTierIndex >= 1 || completedCount >= 4,
                    alignment = Alignment.CenterHorizontally
                )
                TierMilestoneCaption(
                    tierLabel = "Tier 3",
                    bandLabel = "8.0–8.5",
                    isActive = activeTierIndex == 2,
                    isReached = activeTierIndex >= 2 || completedCount >= 8,
                    alignment = Alignment.End
                )
            }
        }
    }
}

@Composable
private fun TierMilestoneCaption(
    tierLabel: String,
    bandLabel: String,
    isActive: Boolean,
    isReached: Boolean,
    alignment: Alignment.Horizontal
) {
    val accent = ThemePrimary
    Column(horizontalAlignment = alignment) {
        Text(
            text = tierLabel,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = PlusJakartaSansFontFamily,
                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold
            ),
            color = if (isActive || isReached) accent else InkMuted
        )
        Text(
            text = bandLabel,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = if (isActive) InkPrimary else InkMuted
        )
    }
}

@Composable
internal fun TierHeaderBanner(
    tier: RoadmapTier,
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val apexColors = LocalApexColors.current
    val isDark = apexColors.isDark
    val highContrast = apexColors.isHighContrast
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (isDark) CardSurfaceTinted else tier.containerColor.copy(alpha = 0.62f),
        border = BorderStroke(
            if (highContrast) 1.5.dp else 1.dp,
            if (isDark) CardBorderSoft else tier.accentPrimary.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(
                            Brush.linearGradient(listOf(tier.accentPrimary, tier.accentSecondary))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "T${tier.tierNumber}",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color.White
                    )
                }
                Column {
                    Text(
                        text = "Tier ${tier.tierNumber} • ${tier.titleEn}",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                        color = InkPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = tier.subtitleVi,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CardWhite,
                    border = if (highContrast) BorderStroke(1.dp, CardBorderSoft) else null
                ) {
                    Text(
                        text = tier.bandRange,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isDark) IndigoOnSurface else tier.accentPrimary,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$completedCount/$totalCount xong",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = InkMuted
                )
            }
        }
    }
}

@Composable
internal fun WindingModuleRow(
    module: IeltsModule,
    metrics: ModuleVisualMetrics,
    anchorFraction: Float,
    isSelected: Boolean,
    isCompleted: Boolean,
    onSelect: () -> Unit,
    onToggleCompleted: () -> Unit,
    onOpenLearnTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nodeOnLeft = anchorFraction < 0.5f

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (nodeOnLeft) {
            WindingNodeEmblem(
                module = module,
                isSelected = isSelected,
                isCompleted = isCompleted,
                onClick = onSelect
            )
            ModuleRoadmapCard(
                module = module,
                metrics = metrics,
                isSelected = isSelected,
                isCompleted = isCompleted,
                onSelect = onSelect,
                onToggleCompleted = onToggleCompleted,
                onOpenLearnTab = onOpenLearnTab,
                modifier = Modifier.weight(1f)
            )
        } else {
            ModuleRoadmapCard(
                module = module,
                metrics = metrics,
                isSelected = isSelected,
                isCompleted = isCompleted,
                onSelect = onSelect,
                onToggleCompleted = onToggleCompleted,
                onOpenLearnTab = onOpenLearnTab,
                modifier = Modifier.weight(1f)
            )
            WindingNodeEmblem(
                module = module,
                isSelected = isSelected,
                isCompleted = isCompleted,
                onClick = onSelect
            )
        }
    }
}

/**
 * Animated Module Node Emblem with dual pulsing concentric rings and a gently bobbing
 * learner/mentor marker pill when this node is the user's current active module.
 */
@Composable
private fun WindingNodeEmblem(
    module: IeltsModule,
    isSelected: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    val primary = ThemePrimary
    val onPrimary = ThemeOnPrimary
    val pulseTransition = rememberInfiniteTransition(label = "node_pulse_${module.id}")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.24f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_ring"
    )
    val markerBob by pulseTransition.animateFloat(
        initialValue = -3f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "marker_bob"
    )

    val nodeGradient = when {
        isCompleted -> Brush.linearGradient(listOf(EmeraldSuccess, CyanGlow))
        isSelected -> Brush.linearGradient(listOf(primary, ThemeSecondary))
        else -> Brush.linearGradient(listOf(CardWhite, SurfaceCanvas))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(68.dp)
    ) {
        // Gently bobbing learner/mentor marker pill floating on the active node
        if (isSelected) {
            Surface(
                shape = RoundedCornerShape(50),
                color = primary,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .offset(y = markerBob.dp)
                    .padding(bottom = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.School,
                        contentDescription = "Vị trí học hiện tại",
                        tint = onPrimary,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "Bạn",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = onPrimary
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(66.dp)
                .tactilePress(pressedScale = 0.93f)
                .clickable(onClick = onClick)
                .testTag("roadmap_node_${module.id}"),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                // Outer glowing halo
                Box(
                    modifier = Modifier
                        .size((56f * pulseScale).dp)
                        .clip(CircleShape)
                        .background(primary.copy(alpha = 0.18f))
                )
                // Inner crisp ring
                Box(
                    modifier = Modifier
                        .size((52f * (0.95f + (pulseScale - 1f) * 0.55f)).dp)
                        .clip(CircleShape)
                        .border(1.5.dp, primary.copy(alpha = 0.48f), CircleShape)
                )
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .shadow(
                        elevation = if (isSelected || isCompleted) 10.dp else 4.dp,
                        shape = CircleShape,
                        ambientColor = primary.copy(alpha = 0.28f),
                        spotColor = primary.copy(alpha = 0.34f)
                    )
                    .clip(CircleShape)
                    .background(nodeGradient)
                    .border(
                        width = if (isSelected) 2.5.dp else 1.5.dp,
                        color = when {
                            isCompleted -> EmeraldSuccess
                            isSelected -> Color.White
                            else -> module.tier.accentPrimary.copy(alpha = 0.40f)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Rounded.Check else module.icon,
                    contentDescription = module.titleEn,
                    tint = when {
                        isCompleted -> Color.White
                        isSelected -> onPrimary
                        else -> module.tier.accentPrimary
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            // Crisp module index badge at bottom-right
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-2).dp, y = (-2).dp),
                shape = CircleShape,
                color = if (isSelected) primary else CardWhite,
                border = BorderStroke(1.dp, if (isSelected) Color.White else module.tier.accentPrimary.copy(alpha = 0.4f))
            ) {
                Text(
                    text = String.format(Locale.US, "%02d", module.id),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = if (isSelected) onPrimary else module.tier.accentPrimary,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                )
            }
        }
    }
}

/**
 * Compact expandable Module card replacing verbose paragraphs with crisp visual
 * progress indicators (Từ vựng % • Câu 8.5 % • Thi %) and high-visibility WCAG buttons.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModuleRoadmapCard(
    module: IeltsModule,
    metrics: ModuleVisualMetrics,
    isSelected: Boolean,
    isCompleted: Boolean,
    onSelect: () -> Unit,
    onToggleCompleted: () -> Unit,
    onOpenLearnTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val apexColors = LocalApexColors.current
    val highContrast = apexColors.isHighContrast
    val primary = ThemePrimary
    val onPrimary = ThemeOnPrimary

    val borderColor = when {
        isSelected -> primary
        isCompleted -> EmeraldSuccess.copy(alpha = if (highContrast) 0.9f else 0.50f)
        else -> CardBorderSoft
    }

    val animatedOverall by animateFloatAsState(
        targetValue = (metrics.overallPercent / 100f).coerceIn(0.04f, 1f),
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "mod_overall_${module.id}"
    )

    GlassSurface(
        modifier = modifier
            .tactilePress(pressedScale = 0.97f)
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onSelect)
            .testTag("roadmap_module_${module.id}"),
        shape = RoundedCornerShape(22.dp),
        solidColor = CardWhite,
        solidBorder = BorderStroke(
            width = if (isSelected || highContrast) 2.dp else 1.dp,
            color = borderColor
        ),
        accentTint = if (isSelected) primary else null,
        solidShadowElevation = if (isSelected) 6.dp else 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // Top micro badge row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isSelected) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = primary
                        ) {
                            Text(
                                text = stringResource(R.string.module_active_badge),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
                            )
                        }
                    }
                    if (isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = EmeraldSuccessLight
                        ) {
                            Text(
                                text = stringResource(R.string.module_completed_badge),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = EmeraldSuccess,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
                            )
                        }
                    }
                    if (!isSelected && !isCompleted) {
                        Text(
                            text = "Band ${module.tier.bandRange}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (apexColors.isDark) IndigoOnSurface else module.tier.accentPrimary
                        )
                    }
                }

                Icon(
                    imageVector = if (isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = if (isCompleted) "Đã hoàn thành" else "Chưa hoàn thành",
                    tint = if (isCompleted) EmeraldSuccess else InkMuted.copy(alpha = 0.55f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Title & concise Vietnamese subtitle
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = module.titleEn,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = InkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = module.titleVi,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Compact Visual Progress Indicators (Từ vựng % • Câu 8.5 % • Thi %)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompactMetricChip(
                    label = "Từ vựng",
                    percent = metrics.vocabPercent,
                    accent = primary,
                    modifier = Modifier.weight(1f)
                )
                CompactMetricChip(
                    label = "Câu 8.5",
                    percent = metrics.drillsPercent,
                    accent = PracticeTeal,
                    modifier = Modifier.weight(1f)
                )
                CompactMetricChip(
                    label = "Thi",
                    percent = metrics.examPercent,
                    accent = StreakOrange,
                    modifier = Modifier.weight(1f)
                )
            }

            // Sleek overall progress track when collapsed
            if (!isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(SurfaceCanvas)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedOverall)
                            .height(5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isCompleted) {
                                    Brush.horizontalGradient(listOf(EmeraldSuccess, CyanGlow))
                                } else {
                                    Brush.horizontalGradient(listOf(primary, ThemeSecondary))
                                }
                            )
                    )
                }
            }

            // Expanded detailed visual bars + collocations + high-visibility WCAG actions
            AnimatedVisibility(
                visible = isSelected,
                enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(tween(200)),
                exit = shrinkVertically(tween(180)) + fadeOut(tween(150))
            ) {
                Column(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CardSurfaceTinted,
                        border = BorderStroke(1.dp, CardBorderSoft)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AnimatedMiniMetricBar(
                                label = "Từ vựng C1–C2",
                                percent = metrics.vocabPercent,
                                barColor = primary
                            )
                            AnimatedMiniMetricBar(
                                label = "Luyện câu Band 8.5",
                                percent = metrics.drillsPercent,
                                barColor = PracticeTeal
                            )
                            AnimatedMiniMetricBar(
                                label = "Thi thử & Chuẩn đầu ra",
                                percent = metrics.examPercent,
                                barColor = StreakOrange
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        module.keyCollocations.take(3).forEach { collocation ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = primary.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, primary.copy(alpha = 0.25f))
                            ) {
                                Text(
                                    text = collocation,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = if (apexColors.isDark) IndigoOnSurface else primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onToggleCompleted,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                if (highContrast) 1.5.dp else 1.dp,
                                if (isCompleted) EmeraldSuccess else primary
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("complete_module_${module.id}")
                        ) {
                            Text(
                                text = if (isCompleted) {
                                    stringResource(R.string.mark_in_progress_action)
                                } else {
                                    stringResource(R.string.mark_completed_action)
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isCompleted) EmeraldSuccess else primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = onOpenLearnTab,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primary,
                                contentColor = onPrimary
                            ),
                            border = if (highContrast) BorderStroke(1.5.dp, onPrimary.copy(alpha = 0.5f)) else null,
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .tactilePress()
                                .testTag("open_learn_module_${module.id}")
                        ) {
                            Text(
                                text = "Học ngay",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactMetricChip(
    label: String,
    percent: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = CardSurfaceTinted,
        border = BorderStroke(1.dp, CardBorderSoft),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                ),
                color = accent,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                color = InkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AnimatedMiniMetricBar(
    label: String,
    percent: Int,
    barColor: Color
) {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val animFrac by animateFloatAsState(
        targetValue = if (entered) (percent / 100f).coerceIn(0.05f, 1f) else 0.05f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "mini_bar_$label"
    )
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = InkSecondary
            )
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = barColor
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(SurfaceCanvas)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animFrac)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(barColor)
            )
        }
    }
}

/**
 * Animated Canvas Winding Path Connector linking vertical modules.
 * Lit segments glow with a flowing animated energy dash traveling smoothly from
 * Tier 1 (6.0) -> Tier 2 (7.0) -> Tier 3 (8.0–8.5).
 */
@Composable
internal fun WindingPathConnector(
    startFraction: Float,
    endFraction: Float,
    accentColor: Color,
    secondaryColor: Color,
    isLit: Boolean,
    modifier: Modifier = Modifier
) {
    val flowTransition = rememberInfiniteTransition(label = "winding_path_flow")
    val dashPhase by flowTransition.animateFloat(
        initialValue = 28f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dash_phase"
    )
    val litAlpha by animateFloatAsState(
        targetValue = if (isLit) 1f else 0.30f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "lit_alpha"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val startX = if (startFraction < 0.5f) 34.dp.toPx() else w - 34.dp.toPx()
        val endX = if (endFraction < 0.5f) 34.dp.toPx() else w - 34.dp.toPx()

        val curvePath = Path().apply {
            moveTo(startX, 0f)
            cubicTo(
                startX, h * 0.58f,
                endX, h * 0.42f,
                endX, h
            )
        }

        if (isLit) {
            // Soft outer glow stroke
            drawPath(
                path = curvePath,
                brush = Brush.verticalGradient(
                    listOf(accentColor.copy(alpha = 0.24f), secondaryColor.copy(alpha = 0.24f))
                ),
                style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)
            )
            // Solid illuminated path
            drawPath(
                path = curvePath,
                brush = Brush.verticalGradient(listOf(accentColor, secondaryColor)),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
            // Flowing luminous dash highlights
            drawPath(
                path = curvePath,
                color = Color.White.copy(alpha = 0.65f),
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 18f), dashPhase)
                )
            )
        } else {
            drawPath(
                path = curvePath,
                color = accentColor.copy(alpha = litAlpha),
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(11f, 11f), 0f)
                )
            )
        }
    }
}
