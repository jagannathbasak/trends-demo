package com.nvile.app.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nvile.app.data.Horizon
import com.nvile.app.data.Market
import com.nvile.app.ui.NvileViewModel
import com.nvile.app.ui.components.Eyebrow
import com.nvile.app.ui.components.ForecastChart
import com.nvile.app.ui.components.Gap
import com.nvile.app.ui.components.GlassCard
import com.nvile.app.ui.components.GlassIconButton
import com.nvile.app.ui.components.Meter
import com.nvile.app.ui.components.NvIcon
import com.nvile.app.ui.components.NvIcons
import com.nvile.app.ui.components.NvScreen
import com.nvile.app.ui.components.PrimaryButton
import com.nvile.app.ui.components.RollingText
import com.nvile.app.ui.components.SecondaryButton
import com.nvile.app.ui.components.SegmentedControl
import com.nvile.app.ui.components.StageTag
import com.nvile.app.ui.components.pct
import com.nvile.app.ui.components.rememberHaptics
import com.nvile.app.ui.components.reveal
import com.nvile.app.ui.components.tappable
import com.nvile.app.ui.components.toneColor
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvType

@Composable
fun MarketScreen(
    vm: NvileViewModel,
    marketId: String,
    onBack: () -> Unit,
    openDecision: () -> Unit,
    openDrivers: () -> Unit,
) {
    val m = vm.repo.market(marketId)
    var h by rememberSaveable { mutableIntStateOf(Horizon.M12.ordinal) }
    val f = m.horizons.getValue(Horizon.entries[h])
    val accent = toneColor(m.tone).let { if (it == Nv.Text) Nv.Mint else it }
    val tracked = m.id in vm.tracked
    val context = LocalContext.current
    val haptics = rememberHaptics()

    NvScreen(
        title = m.name,
        eyebrow = "${m.scope} · ${m.sources} SOURCES · ${m.signals} SIGNALS",
        onBack = onBack,
        actions = {
            GlassIconButton(
                onClick = { shareMarket(context, m) },
                size = 40.dp,
                modifier = Modifier.semantics { contentDescription = "Share with team" },
            ) { NvIcon(NvIcons.Share, Nv.Text, size = 18.dp) }
            Spacer(Modifier.width(8.dp))
            TrackPill(tracked) { haptics.confirm(); vm.toggleTracked(m.id) }
        },
    ) {
        Row(Modifier.reveal(0), verticalAlignment = Alignment.CenterVertically) {
            StageTag(m.stage, detail = m.momentum)
            Spacer(Modifier.width(10.dp))
            Eyebrow("UPDATED ${m.updated}")
        }
        Gap(18)
        SegmentedControl(Horizon.entries.map { it.label }, h, { h = it }, Modifier.reveal(1))
        Gap(14)

        GlassCard(Modifier.fillMaxWidth().reveal(2)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Eyebrow("PREDICTED DEMAND ◈")
                    Gap(6)
                    RollingText(pct(f.demand), NvType.BigNumber, accent)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Eyebrow("BAND")
                    RollingText("${pct(f.bandLow)} → ${pct(f.bandHigh)}", NvType.Mono.copy(fontSize = 12.sp), Nv.Text2)
                    Gap(6)
                    Eyebrow("CONFIDENCE")
                    RollingText("${f.confidence}%", NvType.Mono.copy(fontSize = 12.sp), Nv.Text2)
                }
            }
            Gap(12)
            ForecastChart(m.observed, f)
            Gap(8)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Eyebrow("OBSERVED")
                Eyebrow("TODAY", color = Nv.Mint)
                Eyebrow("FORECAST · ${f.horizon.label}")
            }
        }

        Gap(12)
        Row(Modifier.fillMaxWidth().reveal(3), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile("OPPORTUNITY ◈", "${m.probability}%", Modifier.weight(1f), highlight = accent)
            MetricTile("COMPETITION ◈", pct(f.competition), Modifier.weight(1f))
        }
        Gap(12)
        Row(Modifier.fillMaxWidth().reveal(4), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile("ENTRY WINDOW ◈", f.window, Modifier.weight(1f), small = true)
            MetricTile("MARKET SIZE '29 ◇", m.marketSize, Modifier.weight(1f))
        }

        Gap(28)
        Text("Scenarios", style = NvType.Title2, modifier = Modifier.reveal(5))
        Gap(4)
        Eyebrow("12MO DEMAND · MARKET SIZE '29", Modifier.reveal(5))
        Gap(12)
        Row(Modifier.fillMaxWidth().reveal(6), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            m.scenarios.forEach { s ->
                val shape = RoundedCornerShape(18.dp)
                Column(
                    Modifier
                        .weight(1f)
                        .clip(shape)
                        .background(if (s.isBase) Nv.mint(0.1f) else Nv.white(0.035f))
                        .border(1.dp, if (s.isBase) Nv.mint(0.5f) else Nv.Hairline, shape)
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Eyebrow(s.name, color = if (s.isBase) Nv.Mint else Nv.Text3)
                    Gap(8)
                    Text(pct(s.demand), style = NvType.Number.copy(fontSize = 20.sp), color = if (s.isBase) Nv.Mint else Nv.Text)
                    Gap(4)
                    Text(s.size, style = NvType.Mono, color = Nv.Text3)
                }
            }
        }

        Gap(28)
        Row(Modifier.fillMaxWidth().reveal(7), verticalAlignment = Alignment.Bottom) {
            Text("Where it grows first", style = NvType.Title2, modifier = Modifier.weight(1f))
            Text(m.regionMomentum, style = NvType.Callout)
        }
        Gap(12)
        GlassCard(Modifier.fillMaxWidth().reveal(8)) {
            val max = m.regions.maxOf { it.growth }
            m.regions.forEachIndexed { i, r ->
                if (i > 0) Gap(16)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(r.name, style = NvType.BodyStrong.copy(fontSize = 14.sp), modifier = Modifier.widthIn(min = 96.dp))
                    Spacer(Modifier.width(10.dp))
                    Meter((r.growth / max).toFloat(), Modifier.weight(1f), color = accent.copy(alpha = 1f - i * 0.25f), delayMs = 200 + i * 120)
                    Spacer(Modifier.width(12.dp))
                    Text(pct(r.growth), style = NvType.Mono.copy(fontSize = 13.sp), color = if (i == 0) accent else Nv.Text, textAlign = TextAlign.End, modifier = Modifier.widthIn(min = 44.dp))
                }
            }
        }

        Gap(28)
        Column(Modifier.reveal(9), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PrimaryButton("Open Decision Center", openDecision, icon = NvIcons.Target)
            SecondaryButton("Why it is moving · risks", openDrivers)
        }
    }
}

@Composable
private fun MetricTile(label: String, value: String, modifier: Modifier, highlight: androidx.compose.ui.graphics.Color? = null, small: Boolean = false) {
    GlassCard(modifier, accent = highlight, padding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
        Eyebrow(label)
        Gap(10)
        RollingText(
            value,
            if (small) NvType.Headline.copy(fontSize = 17.sp) else NvType.Number,
            highlight ?: Nv.Text,
        )
    }
}

@Composable
fun TrackPill(on: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (on) Nv.mint(0.12f) else Nv.Mint, label = "trackBg")
    val fg by animateColorAsState(if (on) Nv.Mint else Nv.OnMint, label = "trackFg")
    Row(
        Modifier
            .height(40.dp)
            .tappable(pressedScale = 0.92f, onClick = onClick)
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, Nv.mint(if (on) 0.45f else 0f), RoundedCornerShape(50))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NvIcon(if (on) NvIcons.Check else NvIcons.Plus, fg, size = 15.dp)
        Spacer(Modifier.width(6.dp))
        Text(if (on) "Tracking" else "Track", style = NvType.Callout.copy(fontSize = 13.sp), color = fg)
    }
}

fun shareMarket(context: Context, m: Market) {
    val f = m.m12
    val text = buildString {
        appendLine("NVILE · ${m.name} (${m.scope.lowercase().replaceFirstChar { it.uppercase() }})")
        appendLine("12-month demand ${pct(f.demand)} (band ${pct(f.bandLow)} to ${pct(f.bandHigh)}, confidence ${f.confidence}%)")
        appendLine("Opportunity probability ${m.probability}% · Entry window ${f.window}")
        append("Verdict: ${m.verdict}")
    }
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "NVILE · ${m.name}")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, "Share ${m.name}"))
}
