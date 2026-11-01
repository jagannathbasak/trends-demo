package com.nvile.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nvile.app.data.Horizon
import com.nvile.app.data.HorizonForecast
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvMotion
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/* ================================================================ paths */

/** Catmull-Rom spline through points, as cubic Béziers. */
fun smoothPath(points: List<Offset>, path: Path = Path(), continuePath: Boolean = false): Path {
    if (!continuePath) path.reset()
    if (points.isEmpty()) return path
    if (continuePath) path.lineTo(points[0].x, points[0].y) else path.moveTo(points[0].x, points[0].y)
    for (i in 0 until points.size - 1) {
        val p0 = points[(i - 1).coerceAtLeast(0)]
        val p1 = points[i]
        val p2 = points[i + 1]
        val p3 = points[(i + 2).coerceAtMost(points.size - 1)]
        path.cubicTo(
            p1.x + (p2.x - p0.x) / 6f, p1.y + (p2.y - p0.y) / 6f,
            p2.x - (p3.x - p1.x) / 6f, p2.y - (p3.y - p1.y) / 6f,
            p2.x, p2.y,
        )
    }
    return path
}

private fun DrawScope.drawPartial(path: Path, progress: Float, brush: Brush, stroke: Stroke) {
    if (progress >= 0.999f) { drawPath(path, brush, style = stroke); return }
    if (progress <= 0f) return
    val pm = PathMeasure(); pm.setPath(path, false)
    val seg = Path(); pm.getSegment(0f, pm.length * progress, seg, true)
    drawPath(seg, brush, style = stroke)
}

@Composable
fun rememberDrawOn(durationMs: Int = 1600, delayMs: Int = 150): Float {
    val a = remember { Animatable(0f) }
    LaunchedEffect(Unit) { a.animateTo(1f, tween(durationMs, delayMillis = delayMs, easing = NvMotion.Ease)) }
    return a.value
}

/* ================================================================ forecast */

/** A point the user can scrub to. */
data class ScrubPoint(val label: String, val change: Double, val isForecast: Boolean)

private fun reach(h: Horizon) = when (h) { Horizon.D30 -> 0.18f; Horizon.M6 -> 0.48f; else -> 1f }

private val observedLabels = listOf("18 months ago", "15 months ago", "12 months ago", "9 months ago", "6 months ago", "3 months ago", "Today")

/**
 * Edge-to-edge forecast. Observed history in white, the future as a glowing spectrum line
 * inside a soft probability fog. Drag across it to scrub; [onScrub] receives the point under
 * the finger (or null on release) and a haptic tick marks each step.
 */
@Composable
fun ForecastField(
    observed: List<Double>,
    forecast: HorizonForecast,
    modifier: Modifier = Modifier,
    height: Dp = 260.dp,
    caution: Boolean = false,
    onScrub: (ScrubPoint?) -> Unit = {},
) {
    val draw = rememberDrawOn(1800, 250)
    val d by animateFloatAsState(forecast.demand.toFloat(), NvMotion.gentle(), label = "d")
    val lo by animateFloatAsState(forecast.bandLow.toFloat(), NvMotion.gentle(), label = "lo")
    val hi by animateFloatAsState(forecast.bandHigh.toFloat(), NvMotion.gentle(), label = "hi")
    val r by animateFloatAsState(reach(forecast.horizon), NvMotion.gentle(), label = "reach")
    val pulse = rememberInfiniteTransition(label = "pulse")
    val halo by pulse.animateFloat(0f, 1f, infiniteRepeatable(tween(2200), RepeatMode.Restart), label = "halo")
    val haptics = rememberHaptics()
    var scrub by remember { mutableIntStateOf(-1) }
    val onScrubState by rememberUpdatedState(onScrub)

    val spectrum = if (caution) Nv.Caution else Nv.Spectrum
    val n = 6
    fun f(k: Int) = (k / n.toFloat()).pow(1.15f)
    val points = remember(observed, forecast) {
        observed.mapIndexed { i, v -> ScrubPoint(observedLabels.getOrElse(i + observedLabels.size - observed.size) { "" }, v / 100.0 - 1.0, false) } +
            (1..n).map { k ->
                val frac = k / n.toFloat()
                ScrubPoint("${forecast.horizon.label} · ${(frac * 100).toInt()}% of horizon", forecast.demand * f(k), true)
            }
    }

    Canvas(
        modifier
            .fillMaxWidth()
            .height(height)
            .semantics { contentDescription = "Forecast ${pct(forecast.demand)} over ${forecast.horizon.label}. Drag to explore." }
            .pointerInput(points) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    fun pick(x: Float) {
                        val w = size.width.toFloat()
                        val todayX = w * 0.5f
                        val endX = todayX + (w - todayX) * reach(forecast.horizon) * 0.94f
                        val xs = observed.indices.map { todayX * it / (observed.size - 1) } + (1..n).map { todayX + (endX - todayX) * it / n }
                        val idx = xs.indices.minByOrNull { abs(xs[it] - x) } ?: -1
                        if (idx != scrub) { scrub = idx; haptics.tick(); onScrubState(points.getOrNull(idx)) }
                    }
                    pick(down.position.x)
                    while (true) {
                        val ev = awaitPointerEvent()
                        val c = ev.changes.firstOrNull() ?: break
                        if (!c.pressed) break
                        if (c.positionChange() != Offset.Zero) c.consume()
                        pick(c.position.x)
                    }
                    scrub = -1
                    onScrubState(null)
                }
            },
    ) {
        val w = size.width; val h = size.height
        val top = h * 0.06f; val bottom = h * 0.94f
        val todayX = w * 0.5f
        val endX = todayX + (w - todayX) * r * 0.94f
        val yMax = maxOf(100f * (1f + hi), 104f)
        val yMin = minOf(observed.min().toFloat(), 100f * (1f + minOf(lo, 0f))) - 4f
        fun y(v: Float) = bottom - (bottom - top) * ((v - yMin) / (yMax - yMin))

        val obs = observed.mapIndexed { i, v -> Offset(todayX * i / (observed.size - 1), y(v.toFloat())) }
        val fx = { k: Int -> todayX + (endX - todayX) * k / n }
        val fc = (0..n).map { Offset(fx(it), y(100f * (1f + d * f(it)))) }
        val up = (0..n).map { Offset(fx(it), y(100f * (1f + hi * f(it)))) }
        val dn = (0..n).map { Offset(fx(it), y(100f * (1f + lo * f(it)))) }

        val pObs = (draw / 0.5f).coerceIn(0f, 1f)
        val pFc = ((draw - 0.45f) / 0.55f).coerceIn(0f, 1f)

        // history: fill + line fading in from the past
        val obsPath = smoothPath(obs)
        val area = Path().apply { addPath(obsPath); lineTo(todayX, h); lineTo(0f, h); close() }
        drawPath(area, Brush.verticalGradient(listOf(Nv.white(0.09f * pObs), Color.Transparent), startY = top, endY = h))
        drawPartial(obsPath, pObs, Brush.horizontalGradient(listOf(Nv.white(0.15f), Nv.white(0.9f)), endX = todayX), Stroke(2.2.dp.toPx(), cap = StrokeCap.Round))

        if (pFc > 0f) {
            // probability fog: three nested bands, each fainter
            val fogBrush = Brush.horizontalGradient(spectrum, startX = todayX, endX = endX)
            for (layer in 0..2) {
                val k = 1f + layer * 0.45f
                val upL = up.mapIndexed { i, p -> Offset(p.x, fc[i].y + (p.y - fc[i].y) * k) }
                val dnL = dn.mapIndexed { i, p -> Offset(p.x, fc[i].y + (p.y - fc[i].y) * k) }
                val band = smoothPath(upL); smoothPath(dnL.reversed(), band, continuePath = true); band.close()
                drawPath(band, fogBrush, alpha = (0.2f - layer * 0.06f) * pFc)
            }
            val fcPath = smoothPath(fc)
            drawPartial(fcPath, pFc, Brush.horizontalGradient(spectrum.map { it.copy(alpha = 0.22f) }, startX = todayX, endX = endX), Stroke(14.dp.toPx(), cap = StrokeCap.Round))
            drawPartial(fcPath, pFc, fogBrush, Stroke(2.6.dp.toPx(), cap = StrokeCap.Round))
            if (pFc >= 1f && scrub < 0) {
                val e = fc.last()
                drawCircle(spectrum.last().copy(alpha = 0.4f * (1f - halo)), 6.dp.toPx() + 22.dp.toPx() * halo, e)
                drawCircle(Color.White, 5.dp.toPx(), e)
            }
        }

        // today
        drawLine(Nv.white(0.18f), Offset(todayX, top - 8.dp.toPx()), Offset(todayX, h), 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 4.dp.toPx())))
        if (pObs >= 1f) {
            drawCircle(Nv.Black, 7.dp.toPx(), obs.last())
            drawCircle(Color.White, 7.dp.toPx(), obs.last(), style = Stroke(2.dp.toPx()))
        }

        // scrubber
        if (scrub >= 0) {
            val all = obs + fc.drop(1)
            val p = all[scrub.coerceIn(0, all.lastIndex)]
            drawLine(Nv.white(0.5f), Offset(p.x, 0f), Offset(p.x, h), 1.dp.toPx())
            drawCircle(Nv.white(0.25f), 14.dp.toPx(), p)
            drawCircle(Color.White, 6.dp.toPx(), p)
        }
    }
}

/* ================================================================ small charts */

@Composable
fun Sparkline(values: List<Double>, colors: List<Color>, modifier: Modifier = Modifier.size(72.dp, 32.dp)) {
    val p = rememberDrawOn(1300, 300)
    Canvas(modifier) {
        if (values.size < 2) return@Canvas
        val mn = values.min(); val mx = values.max(); val span = (mx - mn).takeIf { it > 0 } ?: 1.0
        val pts = values.mapIndexed { i, v -> Offset(size.width * i / (values.size - 1), (size.height * (0.85 - 0.7 * (v - mn) / span)).toFloat()) }
        val path = smoothPath(pts)
        val b = Brush.horizontalGradient(colors)
        drawPartial(path, p, Brush.horizontalGradient(colors.map { it.copy(alpha = 0.25f) }), Stroke(7.dp.toPx(), cap = StrokeCap.Round))
        drawPartial(path, p, b, Stroke(2.dp.toPx(), cap = StrokeCap.Round))
        if (p >= 1f) drawCircle(colors.last(), 3.dp.toPx(), pts.last())
    }
}

/** Watch-style dial: 72 ticks light up around the ring in the spectrum. */
@Composable
fun ScoreDial(score: Int, modifier: Modifier = Modifier, size: Dp = 260.dp, caution: Boolean = false) {
    val a = remember { Animatable(0f) }
    LaunchedEffect(score) { a.animateTo(score / 100f, tween(2000, delayMillis = 300, easing = NvMotion.Ease)) }
    val colors = if (caution) Nv.Caution else Nv.Spectrum
    Canvas(modifier.size(size).semantics { contentDescription = "Score $score of 100" }) {
        val ticks = 72
        val rOut = this.size.minDimension / 2 - 4.dp.toPx()
        val rIn = rOut - 22.dp.toPx()
        for (i in 0 until ticks) {
            val f = i / ticks.toFloat()
            val ang = Math.toRadians(f * 360.0 - 90).toFloat()
            val lit = f < a.value
            val col = if (lit) lerpColors(colors, f / a.value.coerceAtLeast(0.01f)) else Nv.white(0.1f)
            val len = if (lit && f > a.value - 1f / ticks) 1.15f else 1f
            drawLine(
                col,
                Offset(center.x + rIn * cos(ang), center.y + rIn * sin(ang)),
                Offset(center.x + rOut * len * cos(ang), center.y + rOut * len * sin(ang)),
                strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round,
            )
        }
        drawCircle(Brush.radialGradient(listOf(colors.first().copy(alpha = 0.18f * a.value), Color.Transparent), center, rIn), rIn, center)
    }
}

private fun lerpColors(colors: List<Color>, t: Float): Color {
    val x = t.coerceIn(0f, 1f) * (colors.size - 1)
    val i = x.toInt().coerceAtMost(colors.size - 2)
    return androidx.compose.ui.graphics.lerp(colors[i], colors[i + 1], x - i)
}

/** 100 dots; the first [percent] light up in sequence. */
@Composable
fun DotMatrix(percent: Int, modifier: Modifier = Modifier) {
    val a = remember { Animatable(0f) }
    LaunchedEffect(percent) { a.animateTo(percent.toFloat(), tween(1800, delayMillis = 300, easing = NvMotion.Ease)) }
    Canvas(modifier.fillMaxWidth().height(150.dp)) {
        val cols = 20; val rows = 5
        val gap = size.width / cols
        val r = gap * 0.28f
        for (i in 0 until cols * rows) {
            val cx = gap * (i % cols) + gap / 2
            val cy = (size.height / rows) * (i / cols) + size.height / rows / 2
            val lit = i < a.value
            drawCircle(if (lit) lerpColors(Nv.Spectrum, i / 100f) else Nv.white(0.1f), r, Offset(cx, cy))
        }
    }
}

/** Bear, base and bull placed on one line. */
@Composable
fun ScenarioRange(bear: Double, base: Double, bull: Double, modifier: Modifier = Modifier, caution: Boolean = false) {
    val p = rememberDrawOn(1300, 250)
    val colors = if (caution) Nv.Caution else Nv.Spectrum
    Canvas(modifier.fillMaxWidth().height(28.dp)) {
        val mn = minOf(bear, 0.0); val mx = bull
        fun x(v: Double) = (size.width * ((v - mn) / (mx - mn))).toFloat().coerceIn(8.dp.toPx(), size.width - 8.dp.toPx())
        val cy = size.height / 2
        drawRoundRect(Nv.white(0.1f), Offset(0f, cy - 2.dp.toPx()), Size(size.width, 4.dp.toPx()), CornerRadius(4.dp.toPx()))
        val x0 = x(bear); val x1 = x(bull)
        drawRoundRect(Brush.horizontalGradient(colors, startX = x0, endX = x1), Offset(x0, cy - 2.dp.toPx()), Size((x1 - x0) * p, 4.dp.toPx()), CornerRadius(4.dp.toPx()))
        if (p > 0.5f) {
            drawCircle(Nv.white(0.6f), 5.dp.toPx(), Offset(x0, cy))
            drawCircle(Nv.white(0.6f), 5.dp.toPx(), Offset(x1, cy))
            drawCircle(Color.White, 10.dp.toPx(), Offset(x(base), cy))
            drawCircle(colors.first(), 5.dp.toPx(), Offset(x(base), cy))
        }
    }
}

/** Thin weight bar for catalysts and risks. */
@Composable
fun Bar(fraction: Float, modifier: Modifier = Modifier, colors: List<Color> = Nv.Spectrum, delayMs: Int = 0, thickness: Dp = 4.dp) {
    val a = remember { Animatable(0f) }
    LaunchedEffect(fraction) { a.animateTo(fraction.coerceIn(0f, 1f), tween(1200, delayMillis = delayMs, easing = NvMotion.Ease)) }
    Canvas(modifier.fillMaxWidth().height(thickness)) {
        val cr = CornerRadius(size.height / 2)
        drawRoundRect(Nv.white(0.08f), cornerRadius = cr)
        if (a.value > 0f) drawRoundRect(Brush.horizontalGradient(colors, endX = size.width), size = Size(size.width * a.value, size.height), cornerRadius = cr)
    }
}

/** A resolved call: the stated band and where reality landed. */
@Composable
fun BandResult(low: Double, high: Double, actual: Double, modifier: Modifier = Modifier) {
    val p = rememberDrawOn(1400, 300)
    Canvas(modifier.fillMaxWidth().height(40.dp)) {
        val mn = low - (high - low); val mx = high + (high - low)
        fun x(v: Double) = (size.width * ((v - mn) / (mx - mn))).toFloat()
        val cy = size.height / 2
        drawLine(Nv.white(0.12f), Offset(0f, cy), Offset(size.width, cy), 1.dp.toPx())
        drawRoundRect(Brush.horizontalGradient(Nv.Spectrum.map { it.copy(alpha = 0.35f) }), Offset(x(low), cy - 12.dp.toPx()), Size((x(high) - x(low)) * p, 24.dp.toPx()), CornerRadius(12.dp.toPx()))
        if (p > 0.6f) {
            val k = ((p - 0.6f) / 0.4f).coerceIn(0f, 1f)
            drawCircle(Color.White.copy(alpha = k), 7.dp.toPx(), Offset(x(actual), cy))
        }
    }
}
