package com.nvile.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.nvile.app.data.Evidence
import com.nvile.app.data.Stage
import com.nvile.app.data.Tone
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvMotion
import com.nvile.app.ui.theme.NvType
import kotlin.math.abs
import kotlin.math.roundToInt

/* ---------------------------------------------------------------- Buttons */

private val ButtonShape = RoundedCornerShape(18.dp)

/** Luminous mint primary action with a soft under-glow. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val alpha by animateFloatAsState(if (enabled) 1f else 0.4f, label = "enabled")
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .tappable(enabled = enabled, onClick = onClick)
            .drawBehind {
                drawRoundRect(
                    brush = Brush.radialGradient(
                        listOf(Nv.mint(0.35f * alpha), Color.Transparent),
                        center = Offset(size.width / 2, size.height * 0.9f), radius = size.width * 0.55f,
                    ),
                    topLeft = Offset(-24f, size.height * 0.2f),
                    size = androidx.compose.ui.geometry.Size(size.width + 48f, size.height * 1.2f),
                    cornerRadius = CornerRadius(80f),
                )
            }
            .clip(ButtonShape)
            .background(Brush.verticalGradient(listOf(Nv.MintBright.copy(alpha = alpha), Nv.Mint.copy(alpha = alpha))))
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            NvIcon(icon, Nv.OnMint, size = 18.dp)
            Box(Modifier.width(8.dp))
        }
        Text(text, style = NvType.Button, color = Nv.OnMint)
    }
}

/** Glass outline secondary action. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color = Nv.Text,
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .tappable(onClick = onClick)
            .clip(ButtonShape)
            .background(Brush.verticalGradient(listOf(Nv.white(0.07f), Nv.white(0.025f))))
            .border(1.dp, Brush.verticalGradient(listOf(Nv.white(0.2f), Nv.white(0.07f))), ButtonShape)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            NvIcon(icon, tint, size = 18.dp)
            Box(Modifier.width(8.dp))
        }
        Text(text, style = NvType.Button.copy(fontWeight = FontWeight.Medium), color = tint)
    }
}

/** Toggle-style action that flips between a filled and an outlined state. */
@Composable
fun ToggleActionButton(
    on: Boolean,
    offText: String,
    onText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg by animateColorAsState(if (on) Nv.mint(0.1f) else Nv.Mint, tween(320), label = "bg")
    val fg by animateColorAsState(if (on) Nv.Mint else Nv.OnMint, tween(320), label = "fg")
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .tappable(onClick = onClick)
            .clip(ButtonShape)
            .background(bg)
            .border(1.dp, Nv.mint(if (on) 0.5f else 0f), ButtonShape)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedContent(on, label = "toggle", transitionSpec = {
            (fadeIn(tween(220)) + slideInVertically { it / 2 }) togetherWith (fadeOut(tween(160)) + slideOutVertically { -it / 2 })
        }) { isOn ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isOn) { NvIcon(NvIcons.Check, fg, size = 18.dp); Box(Modifier.width(8.dp)) }
                Text(if (isOn) onText else offText, style = NvType.Button, color = fg)
            }
        }
    }
}

/* ------------------------------------------------------------ Segmented */

/** Pill segmented control with a sliding glass thumb. */
@Composable
fun SegmentedControl(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    mono: Boolean = true,
    height: Dp = 42.dp,
) {
    val haptics = rememberHaptics()
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(height + 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Nv.white(0.045f))
            .border(1.dp, Nv.Hairline, RoundedCornerShape(16.dp))
            .padding(4.dp),
    ) {
        val segment = maxWidth / options.size
        val x by animateDpAsState(segment * selected, NvMotion.settle(), label = "thumb")
        Box(
            Modifier
                .offset(x = x)
                .width(segment)
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.verticalGradient(listOf(Nv.mint(0.24f), Nv.mint(0.12f))))
                .border(1.dp, Nv.mint(0.4f), RoundedCornerShape(12.dp)),
        )
        Row(Modifier.fillMaxWidth().fillMaxHeight()) {
            options.forEachIndexed { i, label ->
                val on = i == selected
                val color by animateColorAsState(if (on) Nv.Mint else Nv.Text2, tween(250), label = "seg")
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .semantics { this.selected = on }
                        .tappable(pressedScale = 0.94f, role = Role.Tab) {
                            if (!on) { haptics.tick(); onSelect(i) }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        style = if (mono) NvType.Mono.copy(fontWeight = FontWeight.Medium) else NvType.BodyStrong.copy(fontSize = 14.sp),
                        color = color,
                    )
                }
            }
        }
    }
}

/* ---------------------------------------------------------------- Chips */

@Composable
fun SelectChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bg by animateColorAsState(if (selected) Nv.mint(0.14f) else Nv.white(0.03f), tween(220), label = "chipBg")
    val bd by animateColorAsState(if (selected) Nv.mint(0.55f) else Nv.white(0.12f), tween(220), label = "chipBd")
    val fg by animateColorAsState(if (selected) Nv.Mint else Nv.Text2, tween(220), label = "chipFg")
    Row(
        modifier
            .heightIn(min = 40.dp)
            .semantics { this.selected = selected }
            .tappable(pressedScale = 0.93f, role = Role.Checkbox, onClick = onClick)
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, bd, RoundedCornerShape(50))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selected) {
            NvIcon(NvIcons.Check, Nv.Mint, size = 14.dp)
            Box(Modifier.width(6.dp))
        }
        Text(label, style = NvType.Callout.copy(fontWeight = FontWeight.Medium), color = fg)
    }
}

/* --------------------------------------------------------------- Switch */

/** iOS-style switch with a springy knob. */
@Composable
fun NvSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, label: String, modifier: Modifier = Modifier) {
    val haptics = rememberHaptics()
    val x by animateDpAsState(if (checked) 22.dp else 2.dp, NvMotion.press(), label = "knob")
    val track by animateColorAsState(if (checked) Nv.Mint else Nv.white(0.14f), tween(220), label = "track")
    Box(
        modifier
            .size(width = 52.dp, height = 32.dp)
            .semantics { contentDescription = label; stateDescription = if (checked) "On" else "Off" }
            .tappable(pressedScale = 0.92f, role = Role.Switch) { haptics.tick(); onCheckedChange(!checked) }
            .clip(RoundedCornerShape(50))
            .background(track),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .offset(x = x)
                .size(28.dp)
                .clip(RoundedCornerShape(50))
                .background(if (checked) Nv.OnMint else Color(0xFFD9E0DE)),
        )
    }
}

/* ----------------------------------------------------------------- Tags */

@Composable
fun StageTag(stage: Stage, modifier: Modifier = Modifier, detail: String? = null) {
    val (bg, fg) = when (stage) {
        Stage.TREND -> Nv.mint(0.13f) to Nv.Mint
        Stage.EMERGING -> Nv.white(0.08f) to Color(0xFFC8D0CD)
        Stage.WEAK_SIGNAL -> Nv.white(0.05f) to Nv.Text2
    }
    Tag(if (detail != null) "${stage.label} · $detail" else stage.label, bg, fg, modifier)
}

@Composable
fun Tag(text: String, bg: Color, fg: Color, modifier: Modifier = Modifier) {
    Text(
        text,
        style = NvType.MonoTag,
        color = fg,
        modifier = modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 9.dp, vertical = 4.dp),
    )
}

@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier, color: Color = Nv.Text3) {
    Text(text, style = NvType.Eyebrow, color = color, modifier = modifier)
}

fun toneColor(t: Tone): Color = when (t) {
    Tone.UP -> Nv.Mint
    Tone.NEUTRAL -> Nv.Text
    Tone.CAUTION -> Nv.Amber
}

fun evidenceColor(e: Evidence): Color = when (e) {
    Evidence.VERIFIED, Evidence.PREDICTION -> Nv.Mint
    Evidence.SIGNAL, Evidence.ESTIMATE -> Nv.Text2
}

/* --------------------------------------------------------------- Meters */

/** Thin rounded meter whose fill eases in when it first appears. */
@Composable
fun Meter(
    fraction: Float,
    modifier: Modifier = Modifier,
    color: Color = Nv.Mint,
    height: Dp = 6.dp,
    delayMs: Int = 0,
) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(fraction) { anim.animateTo(fraction.coerceIn(0f, 1f), tween(1100, delayMillis = delayMs, easing = NvMotion.Ease)) }
    Canvas(modifier.fillMaxWidth().height(height)) {
        val r = CornerRadius(size.height / 2)
        drawRoundRect(Nv.white(0.08f), cornerRadius = r)
        val w = size.width * anim.value
        if (w > 0f) {
            drawRoundRect(
                Brush.horizontalGradient(listOf(color.copy(alpha = color.alpha * 0.55f), color), endX = w),
                size = androidx.compose.ui.geometry.Size(w, size.height), cornerRadius = r,
            )
        }
    }
}

/** Pulsing live indicator. */
@Composable
fun LiveDot(color: Color = Nv.Mint, dotSize: Dp = 7.dp) {
    val t = rememberInfiniteTransition(label = "live")
    val p by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1800), RepeatMode.Restart), label = "p")
    Canvas(Modifier.size(dotSize * 2.6f)) {
        val c = center
        val r = dotSize.toPx() / 2
        drawCircle(color.copy(alpha = (1f - p) * 0.45f), radius = r + r * 2.2f * p, center = c)
        drawCircle(color, radius = r, center = c)
    }
}

@Composable
fun LivePill(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(Nv.mint(0.07f))
            .border(1.dp, Nv.mint(0.3f), RoundedCornerShape(50))
            .padding(start = 4.dp, end = 12.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LiveDot(dotSize = 6.dp)
        Text(text, style = NvType.MonoTag, color = Nv.Mint)
    }
}

/* -------------------------------------------------------------- Numbers */

/** Formats a fraction as a signed percent, e.g. 0.68 to "+68%". */
fun pct(v: Double): String {
    val n = (v * 100).roundToInt()
    return if (n >= 0) "+$n%" else "−${abs(n)}%"
}

/** Number that counts up from zero on first show, then glides between values. */
@Composable
fun CountUp(
    target: Float,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    durationMs: Int = 1400,
    delayMs: Int = 0,
    format: (Float) -> String = { it.roundToInt().toString() },
) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(target) { anim.animateTo(target, tween(durationMs, delayMillis = delayMs, easing = NvMotion.Ease)) }
    Text(format(anim.value), style = style, color = color, modifier = modifier, textAlign = TextAlign.Start)
}

/** Value that rolls vertically when it changes, like a mechanical counter. */
@Composable
fun RollingText(value: String, style: TextStyle, color: Color, modifier: Modifier = Modifier) {
    AnimatedContent(
        value, modifier = modifier, label = "roll",
        transitionSpec = {
            (slideInVertically(NvMotion.settle()) { it } + fadeIn(tween(220))) togetherWith
                (slideOutVertically(NvMotion.settle()) { -it } + fadeOut(tween(160)))
        },
    ) { v -> Text(v, style = style, color = color) }
}

/* ---------------------------------------------------------------- Rows */

/** Row with a centred-vertical layout and consistent gap. */
@Composable
fun HRow(
    modifier: Modifier = Modifier,
    gap: Dp = 10.dp,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit,
) = Row(modifier, horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = verticalAlignment, content = content)

/** Lower-cases the first letter for mid-sentence use, leaving acronyms like "EHR" intact. */
fun String.decap(): String =
    if (length > 1 && this[1].isUpperCase()) this else replaceFirstChar { it.lowercase() }
