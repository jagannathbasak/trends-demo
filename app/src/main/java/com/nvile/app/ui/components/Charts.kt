package com.nvile.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nvile.app.data.Evidence
import com.nvile.app.data.Horizon
import com.nvile.app.data.HorizonForecast
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvMotion
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/* ------------------------------------------------------------- helpers */

/** Smooth path through points (Catmull-Rom converted to cubic Béziers). */
private fun smoothPath(points: List<Offset>, path: Path = Path(), continuePath: Boolean = false): Path {
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

private fun DrawScope.drawPartial(path: Path, progress: Float, brushColor: Color, stroke: Stroke) {
    if (progress >= 0.999f) { drawPath(path, brushColor, style = stroke); return }
    val pm = PathMeasure(); pm.setPath(path, false)
    val seg = Path()
    pm.getSegment(0f, pm.length * progress, seg, true)
    drawPath(seg, brushColor, style = stroke)
}

/** Remembered 0→1 draw-on progress that starts when the chart first appears. */
@Composable
fun rememberDrawOn(durationMs: Int = 1600, delayMs: Int = 150): Float {
    val a = remember { Animatable(0f) }
    LaunchedEffect(Unit) { a.animateTo(1f, tween(durationMs, delayMillis = delayMs, easing = NvMotion.Ease)) }
    return a.value
}

/* --------------------------------------------------------- Sparkline */

@Composable
fun Sparkline(values: List<Double>, color: Color, modifier: Modifier = Modifier.size(64.dp, 30.dp)) {
    val p = rememberDrawOn(1200, 250)
    Canvas(modifier) {
        if (values.size < 2) return@Canvas
        val min = values.min(); val max = values.max()
        val span = (max - min).takeIf { it > 0.0 } ?: 1.0
        val pts = values.mapIndexed { i, v ->
            Offset(size.width * i / (values.size - 1), (size.height * 0.88f - (size.height * 0.76f * ((v - min) / span))).toFloat())
        }
        val path = smoothPath(pts)
        drawPartial(path, p, color.copy(alpha = 0.22f), Stroke(6.dp.toPx(), cap = StrokeCap.Round))
        drawPartial(path, p, color, Stroke(2.dp.toPx(), cap = StrokeCap.Round))
        if (p > 0.98f) drawCircle(color, 2.6.dp.toPx(), pts.last())
    }
}

/* ------------------------------------------------------- ForecastChart */

private fun horizonReach(h: Horizon) = when (h) {
    Horizon.D30 -> 0.16f
    Horizon.M6 -> 0.45f
    Horizon.M12 -> 1f
    Horizon.Y3 -> 1f
}

/**
 * Observed demand up to today, then the forecast with its confidence band. Changing the
 * horizon morphs the curve and rescales the axis on a spring rather than cutting.
 */
@Composable
fun ForecastChart(
    observed: List<Double>,
    forecast: HorizonForecast,
    modifier: Modifier = Modifier,
    height: Dp = 170.dp,
) {
    val draw = rememberDrawOn(1700, 200)
    val d by animateFloatAsState(forecast.demand.toFloat(), NvMotion.gentle(), label = "d")
    val lo by animateFloatAsState(forecast.bandLow.toFloat(), NvMotion.gentle(), label = "lo")
    val hi by animateFloatAsState(forecast.bandHigh.toFloat(), NvMotion.gentle(), label = "hi")
    val reach by animateFloatAsState(horizonReach(forecast.horizon), NvMotion.gentle(), label = "reach")
    val pulse = rememberInfiniteTransition(label = "pulse")
    val halo by pulse.animateFloat(0f, 1f, infiniteRepeatable(tween(2000), RepeatMode.Restart), label = "halo")

    val desc = "Observed demand to today, then a forecast of ${pct(forecast.demand)} over ${forecast.horizon.label}, " +
        "band ${pct(forecast.bandLow)} to ${pct(forecast.bandHigh)}"

    Canvas(modifier.fillMaxWidth().height(height).semantics { contentDescription = desc }) {
        val w = size.width; val h = size.height
        val top = 14.dp.toPx(); val bottom = h - 10.dp.toPx()
        val todayX = w * 0.54f
        val endX = todayX + (w - todayX - 10.dp.toPx()) * reach

        val yMaxV = maxOf(100f * (1f + hi), 102f) * 1.04f
        val yMinV = (observed.min().toFloat()).coerceAtMost(100f * (1f + minOf(lo, 0f))) * 0.96f
        fun y(v: Float) = bottom - (bottom - top) * ((v - yMinV) / (yMaxV - yMinV))

        // grid
        for (i in 0..3) {
            val gy = top + (bottom - top) * i / 3f
            drawLine(Nv.white(0.05f), Offset(0f, gy), Offset(w, gy), 1.dp.toPx())
        }

        val obsPts = observed.mapIndexed { i, v -> Offset(todayX * i / (observed.size - 1), y(v.toFloat())) }
        val n = 6
        fun f(k: Int) = (k / n.toFloat()).pow(1.12f)
        val fx = { k: Int -> todayX + (endX - todayX) * k / n }
        val fcPts = (0..n).map { k -> Offset(fx(k), y(100f * (1f + d * f(k)))) }
        val hiPts = (0..n).map { k -> Offset(fx(k), y(100f * (1f + hi * f(k)))) }
        val loPts = (0..n).map { k -> Offset(fx(k), y(100f * (1f + lo * f(k)))) }

        // observed area
        val obsLine = smoothPath(obsPts)
        val area = Path().apply {
            addPath(obsLine)
            lineTo(todayX, bottom); lineTo(0f, bottom); close()
        }
        val obsProgress = (draw / 0.55f).coerceIn(0f, 1f)
        if (obsProgress > 0f) {
            drawPath(area, Brush.verticalGradient(listOf(Nv.white(0.07f * obsProgress), Color.Transparent), startY = top, endY = bottom))
        }
        drawPartial(obsLine, obsProgress, Nv.white(0.72f), Stroke(2.4.dp.toPx(), cap = StrokeCap.Round))

        // forecast band, line and glow
        val fcProgress = ((draw - 0.5f) / 0.5f).coerceIn(0f, 1f)
        if (fcProgress > 0f) {
            val band = smoothPath(hiPts)
            smoothPath(loPts.reversed(), band, continuePath = true)
            band.close()
            drawPath(
                band,
                Brush.verticalGradient(listOf(Nv.mint(0.26f * fcProgress), Nv.mint(0.05f * fcProgress)), startY = hiPts.last().y, endY = loPts.first().y),
            )
            val fc = smoothPath(fcPts)
            drawPartial(fc, fcProgress, Nv.mint(0.22f), Stroke(10.dp.toPx(), cap = StrokeCap.Round))
            drawPartial(
                fc, fcProgress, Nv.Mint,
                Stroke(2.6.dp.toPx(), cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(9.dp.toPx(), 6.dp.toPx()))),
            )
        }

        // today marker
        drawLine(
            Nv.mint(0.45f), Offset(todayX, 0f), Offset(todayX, h), 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 5.dp.toPx())),
        )
        if (obsProgress >= 1f) {
            val today = obsPts.last()
            drawCircle(Nv.mint(0.18f), 11.dp.toPx(), today)
            drawCircle(Nv.Mint, 5.dp.toPx(), today)
            drawCircle(Nv.Ink, 2.dp.toPx(), today)
        }
        if (fcProgress >= 1f) {
            val end = fcPts.last()
            drawCircle(Nv.mint(0.35f * (1f - halo)), 6.dp.toPx() + 16.dp.toPx() * halo, end)
            drawCircle(Nv.Mint, 5.5.dp.toPx(), end)
        }
    }
}

/* ------------------------------------------------------------ Hero preview */

/** Compact, self-drawing forecast used on the welcome screen. */
@Composable
fun HeroChart(modifier: Modifier = Modifier) {
    ForecastChart(
        observed = listOf(71.0, 74.0, 79.0, 77.0, 85.0, 93.0, 100.0),
        forecast = HorizonForecast(Horizon.M12, 0.68, 0.52, 0.79, 0.34, "Q1–Q2 2027", 82),
        modifier = modifier,
        height = 132.dp,
    )
}

/* ---------------------------------------------------------- Score ring */

@Composable
fun ScoreRing(score: Int, modifier: Modifier = Modifier, ringSize: Dp = 200.dp, color: Color = Nv.Mint) {
    val p = remember { Animatable(0f) }
    LaunchedEffect(score) { p.animateTo(score / 100f, tween(1800, delayMillis = 250, easing = NvMotion.Ease)) }
    val spin = rememberInfiniteTransition(label = "spin")
    val glow by spin.animateFloat(0.55f, 1f, infiniteRepeatable(tween(2400), RepeatMode.Reverse), label = "glow")
    Canvas(modifier.size(ringSize).semantics { contentDescription = "Opportunity score $score of 100" }) {
        val stroke = 14.dp.toPx()
        val inset = stroke / 2 + 10.dp.toPx()
        val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
        val topLeft = Offset(inset, inset)
        // soft bloom
        drawCircle(
            Brush.radialGradient(listOf(color.copy(alpha = 0.16f * glow), Color.Transparent), radius = size.width * 0.62f),
            radius = size.width * 0.62f,
        )
        // tick marks
        for (i in 0 until 60) {
            val a = Math.toRadians(i * 6.0 - 90).toFloat()
            val r1 = size.width / 2 - 2.dp.toPx(); val r2 = r1 - (if (i % 5 == 0) 5.dp.toPx() else 2.5.dp.toPx())
            drawLine(
                Nv.white(if (i / 60f <= p.value) 0.35f else 0.1f),
                Offset(center.x + r1 * cos(a), center.y + r1 * sin(a)),
                Offset(center.x + r2 * cos(a), center.y + r2 * sin(a)), 1.dp.toPx(),
            )
        }
        drawArc(Nv.white(0.07f), 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
        rotate(-90f) {
            drawArc(
                Brush.sweepGradient(listOf(Nv.MintDeep, color, Nv.MintBright, Nv.MintDeep)),
                0f, 360f * p.value, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        val a = Math.toRadians(360.0 * p.value - 90).toFloat()
        val r = arcSize.width / 2
        val tip = Offset(center.x + r * cos(a), center.y + r * sin(a))
        drawCircle(color.copy(alpha = 0.35f * glow), stroke * 1.2f, tip)
        drawCircle(Color.White, stroke * 0.32f, tip)
    }
}

/* ---------------------------------------------------------- Stacked bar */

@Composable
fun EvidenceBar(mix: Map<Evidence, Int>, modifier: Modifier = Modifier) {
    val p = rememberDrawOn(1300, 200)
    val colors = mapOf(
        Evidence.VERIFIED to Nv.Mint,
        Evidence.SIGNAL to Nv.white(0.5f),
        Evidence.ESTIMATE to Nv.white(0.25f),
        Evidence.PREDICTION to Nv.mint(0.45f),
    )
    Canvas(modifier.fillMaxWidth().height(10.dp)) {
        val total = mix.values.sum().toFloat().coerceAtLeast(1f)
        val gap = 3.dp.toPx()
        var x = 0f
        Evidence.entries.forEach { e ->
            val v = mix[e] ?: 0
            val w = (size.width - gap * 3) * (v / total) * p
            drawRoundRect(colors.getValue(e), Offset(x, 0f), Size(w.coerceAtLeast(0f), size.height), CornerRadius(size.height / 2))
            x += w + gap * p
        }
    }
}

/* -------------------------------------------------------- Band vs actual */

/** Visualises a resolved call: the stated band and where the actual landed. */
@Composable
fun BandResult(low: Double, high: Double, actual: Double, modifier: Modifier = Modifier) {
    val p = rememberDrawOn(1400, 300)
    Canvas(modifier.fillMaxWidth().height(36.dp)) {
        val min = low - (high - low) * 0.8
        val max = high + (high - low) * 0.8
        fun x(v: Double) = (size.width * ((v - min) / (max - min))).toFloat()
        val cy = size.height / 2
        drawLine(Nv.white(0.1f), Offset(0f, cy), Offset(size.width, cy), 2.dp.toPx(), cap = StrokeCap.Round)
        val bx0 = x(low); val bx1 = x(high)
        drawRoundRect(
            Nv.mint(0.22f), Offset(bx0, cy - 9.dp.toPx()), Size((bx1 - bx0) * p, 18.dp.toPx()), CornerRadius(9.dp.toPx()),
        )
        if (p > 0.6f) {
            val ax = x(actual)
            val a = ((p - 0.6f) / 0.4f).coerceIn(0f, 1f)
            drawCircle(Nv.mint(0.3f * a), 13.dp.toPx() * a, Offset(ax, cy))
            drawCircle(Nv.Mint.copy(alpha = a), 6.dp.toPx(), Offset(ax, cy))
        }
    }
}
