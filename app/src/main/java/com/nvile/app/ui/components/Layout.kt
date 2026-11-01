package com.nvile.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvMotion
import com.nvile.app.ui.theme.NvType

val TabBarClearance = 120.dp

/**
 * A scrolling page with an iOS large title that hands off to a compact centred title
 * as it scrolls away. Optional [mood] paints an atmosphere behind the page.
 */
@Composable
fun Page(
    title: String?,
    modifier: Modifier = Modifier,
    mood: Mood? = null,
    kicker: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scroll: ScrollState = rememberScrollState(),
    horizontalPadding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val density = LocalDensity.current
    val k = (scroll.value / with(density) { 70.dp.toPx() }).coerceIn(0f, 1f)
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(modifier.fillMaxSize()) {
        if (mood != null) Atmosphere(mood, Modifier.graphicsLayer { translationY = -scroll.value * 0.35f })
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(top = top + 52.dp),
        ) {
            if (title != null) {
                Column(
                    Modifier
                        .padding(horizontal = 20.dp)
                        .graphicsLayer { alpha = 1f - k; translationY = -k * 10.dp.toPx() },
                ) {
                    if (kicker != null) {
                        Text(kicker, style = NvType.Caption.copy(color = Nv.Text2))
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(title, style = NvType.LargeTitle)
                }
                Spacer(Modifier.height(20.dp))
            }
            Column(Modifier.padding(horizontal = horizontalPadding), content = content)
            Spacer(Modifier.height(TabBarClearance + bottom))
        }

        // Compact bar
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Nv.Black.copy(alpha = 0.92f * k), Nv.Black.copy(alpha = 0.7f * k))))
                .padding(top = top),
        ) {
            Row(Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(88.dp)) {
                    if (onBack != null) RoundButton(NvIcons.Back, "Back", onBack, size = 40.dp)
                }
                Text(
                    title.orEmpty(),
                    style = NvType.Headline,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).graphicsLayer { alpha = k },
                )
                Row(Modifier.width(88.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically, content = actions)
            }
            Hairline(Modifier.align(Alignment.BottomCenter).graphicsLayer { alpha = k })
        }
    }
}

/* ================================================================ grouped list */

private val GroupShape = RoundedCornerShape(24.dp)

/** Apple-style inset group: rounded container, hairline separators between rows. */
@Composable
fun Group(modifier: Modifier = Modifier, header: String? = null, footer: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth()) {
        if (header != null) {
            Text(header, style = NvType.Caption.copy(color = Nv.Text2), modifier = Modifier.padding(start = 18.dp, bottom = 8.dp))
        }
        Column(Modifier.fillMaxWidth().clip(GroupShape).background(Nv.Raised2), content = content)
        if (footer != null) {
            Text(footer, style = NvType.Caption.copy(color = Nv.Text3), modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 8.dp))
        }
    }
}

@Composable
fun GroupRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = Nv.Text,
    showDivider: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = { if (onClick != null) NvIcon(NvIcons.Chevron, Nv.Text3, size = 16.dp) },
) {
    Column {
        if (showDivider) Hairline(Modifier.padding(start = if (icon != null) 60.dp else 18.dp))
        Row(
            modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.tappable(pressedScale = 0.985f, onClick = onClick) else Modifier)
                .heightIn(min = 58.dp)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Box(Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(iconTint.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                    NvIcon(icon, iconTint, size = 17.dp)
                }
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = NvType.BodyStrong.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal))
                if (subtitle != null) Text(subtitle, style = NvType.Callout.copy(color = Nv.Text3))
            }
            trailing()
        }
    }
}

/** Section title with an optional action on the right. */
@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier, action: String? = null, onAction: () -> Unit = {}) {
    Row(modifier.fillMaxWidth().padding(bottom = 14.dp), verticalAlignment = Alignment.Bottom) {
        Text(title, style = NvType.Title, modifier = Modifier.weight(1f))
        if (action != null) Text(action, style = NvType.Callout.copy(color = Nv.Mint), modifier = Modifier.tappable(onClick = onAction))
    }
}

@Composable
fun Gap(h: Int = 16) = Spacer(Modifier.height(h.dp))

/* ================================================================ tab bar */

data class TabItem(val route: String, val label: String, val icon: ImageVector?)

/**
 * Floating glass capsule. Four icon tabs flank a live orb that opens Ask; a soft lens
 * glides beneath the active tab.
 */
@Composable
fun TabBar(items: List<TabItem>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val haptics = rememberHaptics()
    Box(
        modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 22.dp, vertical = 10.dp),
    ) {
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .height(66.dp)
                .shadow(30.dp, RoundedCornerShape(33.dp), ambientColor = Color.Black, spotColor = Color.Black)
                .clip(RoundedCornerShape(33.dp))
                .background(Color(0xE61A1A1F))
                .border(0.5.dp, Brush.verticalGradient(listOf(Nv.white(0.22f), Nv.white(0.04f))), RoundedCornerShape(33.dp))
                .padding(5.dp),
        ) {
            val slot = maxWidth / items.size
            val lensX by animateDpAsState(slot * selected.coerceAtLeast(0), NvMotion.settle(), label = "lens")
            val lensA by animateFloatAsState(if (selected >= 0 && items[selected].icon != null) 1f else 0f, label = "lensA")
            Box(
                Modifier
                    .offset(x = lensX)
                    .width(slot)
                    .fillMaxHeight()
                    .graphicsLayer { alpha = lensA }
                    .clip(RoundedCornerShape(28.dp))
                    .background(Nv.white(0.12f)),
            )
            Row(Modifier.fillMaxSize()) {
                items.forEachIndexed { i, item ->
                    val on = i == selected
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .semantics { this.selected = on; contentDescription = item.label }
                            .tappable(pressedScale = 0.86f, role = Role.Tab, haptic = false) { haptics.tick(); onSelect(i) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (item.icon == null) {
                            Orb(Modifier.size(58.dp), mood = if (on) OrbMood.Thinking else OrbMood.Prediction, energy = if (on) 0.6f else 0.15f)
                        } else {
                            val tint by animateColorAsState(if (on) Nv.Text else Nv.Text3, label = "tint")
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                NvIcon(item.icon, tint, size = 23.dp)
                                Spacer(Modifier.height(2.dp))
                                Text(item.label, style = NvType.Caption.copy(fontSize = 10.sp), color = tint)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Large rounded surface used for hero cards. */
@Composable
fun Panel(
    modifier: Modifier = Modifier,
    color: Color = Nv.Raised2,
    radius: Dp = 28.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .then(if (onClick != null) Modifier.tappable(pressedScale = 0.975f, onClick = onClick) else Modifier)
            .clip(RoundedCornerShape(radius))
            .background(color),
        content = content,
    )
}
