package com.nvile.app.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nvile.app.data.Tone
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvMotion
import com.nvile.app.ui.theme.NvType
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/* ================================================================ Haptics */

class Haptics(private val view: android.view.View) {
    fun tick() { view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK) }
    fun tap() { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
    fun confirm() {
        view.performHapticFeedback(
            if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.CONTEXT_CLICK,
        )
    }
}

@Composable
fun rememberHaptics(): Haptics {
    val view = LocalView.current
    return remember(view) { Haptics(view) }
}

/* ================================================================ Gestures */

/** Clickable with a springy press, a soft haptic and no ripple. */
fun Modifier.tappable(
    enabled: Boolean = true,
    pressedScale: Float = 0.96f,
    role: Role = Role.Button,
    haptic: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) pressedScale else 1f, NvMotion.press(), label = "press")
    val dim by animateFloatAsState(if (pressed) 0.8f else 1f, tween(120), label = "dim")
    val haptics = rememberHaptics()
    this
        .graphicsLayer { scaleX = scale; scaleY = scale; alpha = dim }
        .clickable(interaction, indication = null, enabled = enabled, role = role) {
            if (haptic) haptics.tap()
            onClick()
        }
}

/** Staggered entrance: content rises, sharpens and fades in. */
fun Modifier.reveal(index: Int = 0, step: Long = 65L, distance: Dp = 24.dp): Modifier = composed {
    val a = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * step)
        a.animateTo(1f, tween(820, easing = NvMotion.Ease))
    }
    this.graphicsLayer {
        alpha = a.value
        translationY = (1f - a.value) * distance.toPx()
        val s = 0.98f + 0.02f * a.value
        scaleX = s; scaleY = s
    }
}

/* ================================================================ Atmosphere */

enum class Mood(val top: Color, val mid: Color) {
    Calm(Color(0xFF0E3B33), Color(0xFF111A3B)),
    Rising(Color(0xFF0B5A4A), Color(0xFF16205A)),
    Caution(Color(0xFF5A3208), Color(0xFF3B1414)),
    Night(Color(0xFF0A1A2A), Color(0xFF0A0A16)),
}

fun moodFor(t: Tone) = when (t) {
    Tone.UP -> Mood.Rising
    Tone.NEUTRAL -> Mood.Calm
    Tone.CAUTION -> Mood.Caution
}

/**
 * Full-bleed sky behind a screen: two slow-drifting pools of coloured light that fall
 * off into true black. The mood crossfades when it changes.
 */
@Composable
fun Atmosphere(mood: Mood, modifier: Modifier = Modifier, strength: Float = 1f) {
    val top by animateColorAsState(mood.top, tween(1200), label = "top")
    val mid by animateColorAsState(mood.mid, tween(1200), label = "mid")
    val k by animateFloatAsState(strength, tween(900), label = "k")
    val t = rememberInfiniteTransition(label = "sky")
    val p by t.animateFloat(0f, 6.2832f, infiniteRepeatable(tween(30_000, easing = LinearEasing)), label = "p")
    Canvas(modifier.fillMaxSize().background(Nv.Black)) {
        val w = size.width; val h = size.height
        val c1 = Offset(w * (0.75f + 0.12f * cos(p)), h * (0.02f + 0.03f * sin(p)))
        drawCircle(Brush.radialGradient(listOf(top.copy(alpha = 0.95f * k), top.copy(alpha = 0.3f * k), Color.Transparent), c1, w * 1.05f), w * 1.05f, c1)
        val c2 = Offset(w * (0.1f + 0.1f * sin(p * 0.8f)), h * (0.18f + 0.04f * cos(p)))
        drawCircle(Brush.radialGradient(listOf(mid.copy(alpha = 0.75f * k), Color.Transparent), c2, w * 0.9f), w * 0.9f, c2)
        drawRect(Brush.verticalGradient(0f to Color.Transparent, 0.35f to Nv.Black.copy(alpha = 0.55f), 0.6f to Nv.Black))
    }
}

/* ================================================================ Buttons */

/** The primary action: a solid white capsule, Apple-style. */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    container: Color = Nv.Text,
    content: Color = Nv.OnLight,
) {
    val a by animateFloatAsState(if (enabled) 1f else 0.35f, label = "en")
    Row(
        modifier
            .fillMaxWidth()
            .height(58.dp)
            .tappable(enabled = enabled, onClick = onClick)
            .graphicsLayer { alpha = a }
            .clip(RoundedCornerShape(50))
            .background(container),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) { NvIcon(icon, content, size = 19.dp); Box(Modifier.width(8.dp)) }
        Text(text, style = NvType.Button, color = content)
    }
}

/** Secondary action: translucent capsule. */
@Composable
fun GhostButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, tint: Color = Nv.Text) {
    Row(
        modifier
            .fillMaxWidth()
            .height(58.dp)
            .tappable(onClick = onClick)
            .clip(RoundedCornerShape(50))
            .background(Nv.white(0.1f)),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) { NvIcon(icon, tint, size = 19.dp); Box(Modifier.width(8.dp)) }
        Text(text, style = NvType.Button.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium), color = tint)
    }
}

/** Small round glass button for toolbars. */
@Composable
fun RoundButton(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier, tint: Color = Nv.Text, size: Dp = 44.dp) {
    Box(
        modifier
            .size(size)
            .semantics { contentDescription = label }
            .tappable(pressedScale = 0.88f, onClick = onClick)
            .clip(RoundedCornerShape(50))
            .background(Nv.white(0.12f)),
        contentAlignment = Alignment.Center,
    ) { NvIcon(icon, tint, size = size * 0.45f) }
}

/** Capsule toggle, e.g. Track / Tracking. */
@Composable
fun TogglePill(on: Boolean, offText: String, onText: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bg by animateColorAsState(if (on) Nv.white(0.12f) else Nv.Text, label = "bg")
    val fg by animateColorAsState(if (on) Nv.Text else Nv.OnLight, label = "fg")
    Row(
        modifier
            .height(36.dp)
            .tappable(pressedScale = 0.92f, onClick = onClick)
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedContent(on, label = "pill", transitionSpec = {
            (fadeIn(tween(200)) + slideInVertically { it / 2 }) togetherWith (fadeOut(tween(120)) + slideOutVertically { -it / 2 })
        }) { isOn ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                NvIcon(if (isOn) NvIcons.Check else NvIcons.Plus, fg, size = 15.dp)
                Box(Modifier.width(6.dp))
                Text(if (isOn) onText else offText, style = NvType.Callout.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = fg)
            }
        }
    }
}

/* ================================================================ Selection */

/** Text-only tabs with a gliding capsule behind the active one. */
@Composable
fun Tabs(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier, style: TextStyle = NvType.Callout) {
    val haptics = rememberHaptics()
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(50))
            .background(Nv.white(0.07f))
            .padding(3.dp),
    ) {
        val w = maxWidth / options.size
        val x by animateDpAsState(w * selected, NvMotion.settle(), label = "tab")
        Box(Modifier.offset(x = x).width(w).fillMaxHeight().clip(RoundedCornerShape(50)).background(Nv.white(0.16f)))
        Row(Modifier.fillMaxSize()) {
            options.forEachIndexed { i, label ->
                val on = i == selected
                val c by animateColorAsState(if (on) Nv.Text else Nv.Text2, label = "c")
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .semantics { this.selected = on }
                        .tappable(pressedScale = 0.94f, role = Role.Tab, haptic = false) { if (!on) { haptics.tick(); onSelect(i) } },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(label, style = style.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = c)
                }
            }
        }
    }
}

@Composable
fun Chip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bg by animateColorAsState(if (selected) Nv.Text else Nv.white(0.08f), label = "bg")
    val fg by animateColorAsState(if (selected) Nv.OnLight else Nv.Text, label = "fg")
    Box(
        modifier
            .heightIn(min = 40.dp)
            .semantics { this.selected = selected }
            .tappable(pressedScale = 0.92f, role = Role.Checkbox, onClick = onClick)
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) { Text(label, style = NvType.Callout.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium), color = fg) }
}

/** iOS-style switch. */
@Composable
fun NvSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, label: String) {
    val haptics = rememberHaptics()
    val x by animateDpAsState(if (checked) 22.dp else 2.dp, NvMotion.press(), label = "knob")
    val track by animateColorAsState(if (checked) Nv.Mint else Nv.white(0.16f), label = "track")
    Box(
        Modifier
            .size(width = 52.dp, height = 32.dp)
            .semantics { contentDescription = label; stateDescription = if (checked) "On" else "Off" }
            .tappable(pressedScale = 0.94f, role = Role.Switch, haptic = false) { haptics.tick(); onCheckedChange(!checked) }
            .clip(RoundedCornerShape(50))
            .background(track),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(Modifier.offset(x = x).size(28.dp).clip(RoundedCornerShape(50)).background(Color.White))
    }
}

/* ================================================================ Text bits */

@Composable
fun Label(text: String, modifier: Modifier = Modifier, color: Color = Nv.Text3) =
    Text(text.uppercase(), style = NvType.Label, color = color, modifier = modifier)

/** Small coloured dot + label, e.g. stage or evidence. */
@Composable
fun DotLabel(text: String, color: Color, modifier: Modifier = Modifier, textColor: Color = Nv.Text2) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).clip(RoundedCornerShape(50)).background(color))
        Box(Modifier.width(7.dp))
        Text(text, style = NvType.Caption.copy(color = textColor))
    }
}

@Composable
fun NvIcon(icon: ImageVector, tint: Color, modifier: Modifier = Modifier, size: Dp = 22.dp, contentDescription: String? = null) {
    Icon(icon, contentDescription, modifier.size(size), tint)
}

/** Number that counts up on first show, then glides between values. */
@Composable
fun CountUp(
    target: Float,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    durationMs: Int = 1500,
    delayMs: Int = 0,
    format: (Float) -> String = { it.roundToInt().toString() },
) {
    val a = remember { Animatable(0f) }
    LaunchedEffect(target) { a.animateTo(target, tween(durationMs, delayMillis = delayMs, easing = NvMotion.Ease)) }
    Text(format(a.value), style = style, color = color, modifier = modifier)
}

/** Text that rolls vertically when its value changes, like a split-flap. */
@Composable
fun RollingText(value: String, style: TextStyle, modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    AnimatedContent(value, modifier = modifier, label = "roll", transitionSpec = {
        (slideInVertically(NvMotion.settle()) { it / 2 } + fadeIn(tween(260))) togetherWith
            (slideOutVertically(NvMotion.settle()) { -it / 2 } + fadeOut(tween(180)))
    }) { Text(it, style = style, color = color) }
}

/* ================================================================ Formatting */

fun pct(v: Double): String {
    val n = (v * 100).roundToInt()
    return if (n >= 0) "+$n%" else "−${abs(n)}%"
}

fun toneColor(t: Tone): Color = when (t) {
    Tone.UP -> Nv.Mint
    Tone.NEUTRAL -> Nv.Cyan
    Tone.CAUTION -> Nv.Amber
}

/** Lower-cases the first letter for mid-sentence use, leaving acronyms like "EHR" intact. */
fun String.decap(): String = if (length > 1 && this[1].isUpperCase()) this else replaceFirstChar { it.lowercase() }

@Composable
fun Hairline(modifier: Modifier = Modifier) = Box(modifier.fillMaxWidth().height(0.5.dp).background(Nv.Separator))
