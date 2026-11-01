package com.nvile.app.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvMotion
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/* ---------------------------------------------------------------- Haptics */

class Haptics(private val view: android.view.View) {
    fun tick() { view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK) }
    fun tap() { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
    fun confirm() {
        val c = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.CONTEXT_CLICK
        view.performHapticFeedback(c)
    }
}

@Composable
fun rememberHaptics(): Haptics {
    val view = LocalView.current
    return remember(view) { Haptics(view) }
}

/* -------------------------------------------------------------- Ambient */

/**
 * Slow-drifting aurora of mint and deep-teal light over near-black ink. It is the
 * signature ground of the app and sits behind every screen.
 */
@Composable
fun AuroraBackground(modifier: Modifier = Modifier, intensity: Float = 1f) {
    val t = rememberInfiniteTransition(label = "aurora")
    val phase by t.animateFloat(
        initialValue = 0f, targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(tween(26_000, easing = LinearEasing)), label = "phase",
    )
    val glow by animateFloatAsState(intensity, tween(1200), label = "intensity")
    Canvas(modifier.fillMaxSize().background(Nv.Ink)) {
        val w = size.width; val h = size.height
        fun orb(cx: Float, cy: Float, r: Float, color: Color, alpha: Float) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(color.copy(alpha = alpha * glow), color.copy(alpha = alpha * glow * 0.35f), Color.Transparent),
                    center = Offset(cx, cy), radius = r,
                ),
                radius = r, center = Offset(cx, cy),
            )
        }
        orb(w * (0.82f + 0.08f * cos(phase)), h * (0.06f + 0.04f * sin(phase * 2)), w * 0.95f, Nv.Mint, 0.16f)
        orb(w * (0.08f + 0.10f * sin(phase)), h * (0.34f + 0.05f * cos(phase)), w * 0.85f, Nv.MintDeep, 0.10f)
        orb(w * (0.70f + 0.12f * sin(phase + 1.6f)), h * (0.86f + 0.04f * cos(phase * 1.5f)), w * 0.9f, Color(0xFF1B6F86), 0.10f)
        // Vignette pulls the eye to the centre column.
        drawRect(
            Brush.verticalGradient(
                0f to Color.Transparent, 0.55f to Color.Transparent, 1f to Nv.Ink.copy(alpha = 0.65f),
            ),
        )
    }
}

/* -------------------------------------------------------------- Pressable */

/** Apple-style press: a quick, springy dip in scale while the finger is down. */
fun Modifier.pressable(
    interaction: MutableInteractionSource,
    pressedScale: Float = 0.965f,
): Modifier = composed {
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) pressedScale else 1f, NvMotion.press(), label = "press")
    this.graphicsLayer { scaleX = scale; scaleY = scale }
}

/** Clickable with press-scale, haptic and no ripple. */
fun Modifier.tappable(
    enabled: Boolean = true,
    pressedScale: Float = 0.965f,
    role: Role = Role.Button,
    onClick: () -> Unit,
): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val haptics = rememberHaptics()
    this
        .pressable(interaction, pressedScale)
        .clickable(interaction, indication = null, enabled = enabled, role = role) {
            haptics.tap(); onClick()
        }
}

/* -------------------------------------------------------------- Reveal */

/** Staggered entrance: content rises 28dp and fades in on a soft spring. */
fun Modifier.reveal(index: Int = 0, delayMs: Long = 70L): Modifier = composed {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * delayMs)
        anim.animateTo(1f, tween(720, easing = NvMotion.Ease))
    }
    this.graphicsLayer {
        alpha = anim.value
        translationY = (1f - anim.value) * 28.dp.toPx()
    }
}

/* -------------------------------------------------------------- Glass */

val CardShape = RoundedCornerShape(24.dp)

/**
 * Frosted-glass card: a faint vertical sheen, a hairline rim that catches light at the
 * top edge, and an optional accent tint for verdicts and warnings.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    accent: Color? = null,
    shape: Shape = CardShape,
    padding: PaddingValues = PaddingValues(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val fill = if (accent != null) {
        Brush.verticalGradient(listOf(accent.copy(alpha = 0.13f), accent.copy(alpha = 0.04f)))
    } else {
        Brush.verticalGradient(listOf(Nv.white(0.075f), Nv.white(0.028f)))
    }
    val rim = if (accent != null) {
        Brush.verticalGradient(listOf(accent.copy(alpha = 0.55f), accent.copy(alpha = 0.14f)))
    } else {
        Brush.verticalGradient(listOf(Nv.white(0.16f), Nv.white(0.04f)))
    }
    Column(
        modifier
            .then(if (onClick != null) Modifier.tappable(pressedScale = 0.975f, onClick = onClick) else Modifier)
            .clip(shape)
            .background(Nv.Ink1.copy(alpha = 0.55f))
            .background(fill)
            .border(1.dp, rim, shape)
            .padding(padding),
        content = content,
    )
}

/** Small glass circle for icon buttons in headers. */
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .tappable(pressedScale = 0.9f, onClick = onClick)
            .clip(RoundedCornerShape(50))
            .background(Brush.verticalGradient(listOf(Nv.white(0.09f), Nv.white(0.03f))))
            .border(1.dp, Brush.verticalGradient(listOf(Nv.white(0.18f), Nv.white(0.05f))), RoundedCornerShape(50))
            .size(size),
        contentAlignment = Alignment.Center,
        content = content,
    )
}
