package com.nvile.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvMotion
import com.nvile.app.ui.theme.NvType

/** Space reserved at the bottom of scrolling content so the floating tab bar never covers it. */
val TabBarClearance = 118.dp

/**
 * Scrolling screen with an iOS-style large title. As the title scrolls away a compact,
 * frosted bar fades in carrying the same title.
 */
@Composable
fun NvScreen(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
    scroll: ScrollState = rememberScrollState(),
    showLargeTitle: Boolean = true,
    bottomClearance: androidx.compose.ui.unit.Dp = TabBarClearance,
    content: @Composable ColumnScope.() -> Unit,
) {
    val density = LocalDensity.current
    val threshold = with(density) { 64.dp.toPx() }
    val compact = (scroll.value / threshold).coerceIn(0f, 1f)
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(top = statusTop + 56.dp)
                .padding(horizontal = 20.dp),
        ) {
            if (showLargeTitle) {
                Column(Modifier.graphicsLayer { alpha = 1f - compact; translationY = -compact * 12.dp.toPx() }) {
                    if (eyebrow != null) {
                        Eyebrow(eyebrow)
                        Spacer(Modifier.height(8.dp))
                    }
                    Text(title, style = NvType.LargeTitle)
                }
                Spacer(Modifier.height(22.dp))
            }
            content()
            Spacer(Modifier.height(bottomClearance + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
        }

        // Compact bar
        Box(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Nv.Ink.copy(alpha = 0.94f * compact + 0.0f), Nv.Ink.copy(alpha = 0.78f * compact)),
                    ),
                )
                .padding(top = statusTop),
        ) {
            Row(
                Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onBack != null) {
                    GlassIconButton(onClick = onBack, size = 40.dp, modifier = Modifier.semantics { contentDescription = "Back" }) {
                        NvIcon(NvIcons.Back, Nv.Text, size = 20.dp)
                    }
                } else {
                    Spacer(Modifier.width(8.dp))
                }
                val showTitle = if (showLargeTitle) compact else 1f
                Text(
                    title,
                    style = NvType.Headline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                        .graphicsLayer { alpha = showTitle; translationY = (1f - showTitle) * 8.dp.toPx() },
                )
                actions()
            }
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Nv.white(0.06f * compact)),
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp), verticalAlignment = Alignment.Bottom) {
        Text(title, style = NvType.Title2, modifier = Modifier.weight(1f))
        if (trailing != null) Eyebrow(trailing)
    }
}

@Composable
fun Gap(h: Int = 14) = Spacer(Modifier.height(h.dp))

/* ----------------------------------------------------------- Tab bar */

data class TabItem(val route: String, val label: String, val icon: ImageVector)

/**
 * Detached, floating glass tab bar. A luminous capsule slides between tabs on a spring.
 */
@Composable
fun FloatingTabBar(
    items: List<TabItem>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberHaptics()
    Box(
        modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(28.dp, RoundedCornerShape(34.dp), ambientColor = Color.Black, spotColor = Color.Black)
                .clip(RoundedCornerShape(34.dp))
                .background(Nv.Ink2.copy(alpha = 0.92f))
                .background(Brush.verticalGradient(listOf(Nv.white(0.09f), Nv.white(0.02f))))
                .border(1.dp, Brush.verticalGradient(listOf(Nv.white(0.18f), Nv.white(0.05f))), RoundedCornerShape(34.dp))
                .padding(6.dp),
        ) {
            val slot = maxWidth / items.size
            val x by animateDpAsState(slot * selected.coerceAtLeast(0), NvMotion.settle(), label = "tab")
            val capsuleAlpha by animateFloatAsState(if (selected >= 0) 1f else 0f, label = "capsule")
            Box(
                Modifier
                    .offset(x = x)
                    .width(slot)
                    .fillMaxHeight()
                    .graphicsLayer { alpha = capsuleAlpha }
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.verticalGradient(listOf(Nv.mint(0.2f), Nv.mint(0.08f))))
                    .border(1.dp, Nv.mint(0.28f), RoundedCornerShape(28.dp)),
            )
            Row(Modifier.fillMaxSize()) {
                items.forEachIndexed { i, item ->
                    val on = i == selected
                    val tint by animateColorAsState(if (on) Nv.Mint else Nv.Text3, tween(220), label = "tint")
                    val lift by animateFloatAsState(if (on) 1f else 0f, NvMotion.press(), label = "lift")
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .semantics { this.selected = on }
                            .tappable(pressedScale = 0.86f, role = Role.Tab) {
                                if (!on) haptics.tick()
                                onSelect(i)
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        NvIcon(
                            item.icon, tint, size = 22.dp,
                            modifier = Modifier.graphicsLayer {
                                val s = 1f + 0.08f * lift
                                scaleX = s; scaleY = s
                                translationY = -1.5.dp.toPx() * lift
                            },
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(item.label, style = NvType.TabLabel, color = tint)
                    }
                }
            }
        }
    }
}

/** Ordered numeral badge used by the Decision Center and report parts. */
@Composable
fun NumberBadge(n: Int, active: Boolean = false) {
    Box(
        Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (active) Nv.Mint else Nv.white(0.06f))
            .border(1.dp, if (active) Nv.Mint else Nv.Hairline, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(n.toString().padStart(2, '0'), style = NvType.MonoTag.copy(fontSize = NvType.Mono.fontSize), color = if (active) Nv.OnMint else Nv.Text2)
    }
}
