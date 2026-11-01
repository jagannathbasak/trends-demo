package com.nvile.app.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nvile.app.data.Evidence
import com.nvile.app.data.Horizon
import com.nvile.app.data.Market
import com.nvile.app.data.Tone
import com.nvile.app.ui.NvileViewModel
import com.nvile.app.ui.components.Bar
import com.nvile.app.ui.components.DotLabel
import com.nvile.app.ui.components.ForecastField
import com.nvile.app.ui.components.Gap
import com.nvile.app.ui.components.Hairline
import com.nvile.app.ui.components.Label
import com.nvile.app.ui.components.NvIcons
import com.nvile.app.ui.components.Page
import com.nvile.app.ui.components.Panel
import com.nvile.app.ui.components.PillButton
import com.nvile.app.ui.components.RollingText
import com.nvile.app.ui.components.RoundButton
import com.nvile.app.ui.components.ScenarioRange
import com.nvile.app.ui.components.ScrubPoint
import com.nvile.app.ui.components.SectionTitle
import com.nvile.app.ui.components.Tabs
import com.nvile.app.ui.components.TogglePill
import com.nvile.app.ui.components.moodFor
import com.nvile.app.ui.components.pct
import com.nvile.app.ui.components.rememberHaptics
import com.nvile.app.ui.components.reveal
import com.nvile.app.ui.components.toneColor
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvType
import java.util.Locale

@Composable
fun MarketScreen(vm: NvileViewModel, marketId: String, onBack: () -> Unit, openDecision: () -> Unit) {
    val m = vm.repo.market(marketId)
    var h by rememberSaveable { mutableIntStateOf(Horizon.M12.ordinal) }
    var scrub by remember { mutableStateOf<ScrubPoint?>(null) }
    val f = m.horizons.getValue(Horizon.entries[h])
    val accent = toneColor(m.tone)
    val caution = m.tone == Tone.CAUTION
    val context = LocalContext.current
    val haptics = rememberHaptics()

    Page(
        title = m.name,
        mood = moodFor(m.tone),
        onBack = onBack,
        horizontalPadding = 0.dp,
        actions = {
            RoundButton(NvIcons.Share, "Share", { shareMarket(context, m) }, size = 40.dp)
        },
    ) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Row(Modifier.reveal(0), verticalAlignment = Alignment.CenterVertically) {
                DotLabel("${m.stage.label.lowercase().replaceFirstChar { it.uppercase() }} · ${m.momentum.lowercase()} · ${m.scope}", accent, Modifier.weight(1f))
                TogglePill(m.id in vm.tracked, "Watch", "Watching", { haptics.confirm(); vm.toggleTracked(m.id) })
            }
            Gap(18)
            // Hero numeral follows the finger while scrubbing.
            RollingText(
                scrub?.let { pct(it.change) } ?: pct(f.demand),
                NvType.Numeral,
                Modifier.reveal(1),
            )
            RollingText(
                scrub?.label ?: "Predicted demand · ${f.horizon.label} · ${f.confidence}% confidence",
                NvType.Callout.copy(color = if (scrub == null || scrub?.isForecast == true) accent else Nv.Text2),
                Modifier.reveal(2),
            )
        }
        Gap(8)
        ForecastField(m.observed, f, Modifier.reveal(3), caution = caution, onScrub = { scrub = it })
        Gap(4)
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Text("18 months ago", style = NvType.Caption, modifier = Modifier.weight(1f))
            Text("Today", style = NvType.Caption.copy(color = Nv.Text2), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Text("+${f.horizon.label}", style = NvType.Caption, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        }
        Gap(18)
        Column(Modifier.padding(horizontal = 20.dp)) {
            Tabs(Horizon.entries.map { it.label }, h, { h = it }, Modifier.reveal(4))
            Gap(8)
            Text("Drag across the chart to explore.", style = NvType.Caption, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

            Gap(36)
            // The call
            Panel(Modifier.fillMaxWidth().reveal(5), color = Nv.Raised2, radius = 30.dp) {
                Column(Modifier.padding(24.dp)) {
                    Label("The call ◈", color = accent)
                    Gap(10)
                    Text(m.verdict, style = NvType.Verdict)
                    Gap(12)
                    Text(m.decision.next, style = NvType.Body)
                    Gap(22)
                    PillButton("Open the decision", openDecision, icon = NvIcons.Target)
                }
            }

            Gap(36)
            Facts(
                listOf(
                    Triple("Opportunity", "${m.probability}%", Evidence.PREDICTION),
                    Triple("Competition", pct(f.competition), Evidence.PREDICTION),
                    Triple("Entry window", f.window, Evidence.PREDICTION),
                    Triple("Market size '29", m.marketSize, Evidence.ESTIMATE),
                ),
                Modifier.reveal(6),
            )

            Gap(40)
            SectionTitle("Scenarios")
            val bear = m.scenarios.first(); val base = m.scenarios.first { it.isBase }; val bull = m.scenarios.last()
            ScenarioRange(bear.demand, base.demand, bull.demand, caution = caution)
            Gap(10)
            Row(Modifier.fillMaxWidth()) {
                ScenarioLabel("Bear", bear.demand, bear.size, Modifier.weight(1f), Alignment.Start)
                ScenarioLabel("Base", base.demand, base.size, Modifier.weight(1f), Alignment.CenterHorizontally, highlight = true)
                ScenarioLabel("Bull", bull.demand, bull.size, Modifier.weight(1f), Alignment.End)
            }

            Gap(44)
            SectionTitle("Why it's moving")
            m.catalysts.forEachIndexed { i, c ->
                if (i > 0) Gap(22)
                Row(verticalAlignment = Alignment.Top) {
                    Text(c.evidence.glyph, style = NvType.BodyStrong.copy(color = accent), modifier = Modifier.width(22.dp))
                    Column(Modifier.weight(1f)) {
                        Text(c.title, style = NvType.BodyStrong.copy(fontSize = 17.sp, lineHeight = 23.sp))
                        Gap(10)
                        Bar(c.weight.toFloat(), colors = spectrumFor(m.tone), delayMs = 200 + i * 120)
                        Gap(8)
                        Text("${c.evidence.label} · ${c.source} · weight ${"%.2f".format(Locale.US, c.weight)}", style = NvType.Caption)
                    }
                }
            }

            Gap(44)
            SectionTitle("What could break it")
            m.risks.forEachIndexed { i, r ->
                if (i > 0) Hairline()
                Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(r.title, style = NvType.BodyStrong.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal), modifier = Modifier.weight(1f))
                    Text("${r.probability}%", style = NvType.NumeralS.copy(fontSize = 24.sp, color = Nv.Amber))
                }
            }
            Text("If a risk triggers, NVILE re-scores the call and tells you.", style = NvType.Caption)

            Gap(44)
            SectionTitle("Where it grows first", action = m.regionMomentum)
            val max = m.regions.maxOf { it.growth }
            m.regions.forEachIndexed { i, r ->
                if (i > 0) Gap(16)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(r.name, style = NvType.BodyStrong.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal), modifier = Modifier.width(110.dp))
                    Bar((r.growth / max).toFloat(), Modifier.weight(1f), colors = spectrumFor(m.tone), delayMs = 150 * i, thickness = 6.dp)
                    Text(pct(r.growth), style = NvType.Data.copy(color = Nv.Text), modifier = Modifier.width(56.dp), textAlign = TextAlign.End)
                }
            }

            Gap(44)
            SectionTitle("Evidence")
            EvidenceMix(m)
        }
    }
}

@Composable
private fun Facts(items: List<Triple<String, String, Evidence>>, modifier: Modifier) {
    Column(modifier) {
        items.chunked(2).forEachIndexed { row, pair ->
            if (row > 0) Hairline()
            Row(Modifier.fillMaxWidth().height(96.dp)) {
                pair.forEachIndexed { i, (label, value, ev) ->
                    if (i > 0) Box(Modifier.width(0.5.dp).fillMaxHeight().padding(vertical = 18.dp).background(Nv.Separator))
                    Column(Modifier.weight(1f).padding(start = if (i > 0) 20.dp else 0.dp), verticalArrangement = Arrangement.Center) {
                        Text("$label ${ev.glyph}", style = NvType.Caption)
                        Gap(6)
                        RollingText(value, if (value.length > 8) NvType.NumeralS.copy(fontSize = 22.sp) else NvType.NumeralS)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScenarioLabel(name: String, demand: Double, size: String, modifier: Modifier, align: Alignment.Horizontal, highlight: Boolean = false) {
    Column(modifier, horizontalAlignment = align) {
        Text(name, style = NvType.Caption.copy(color = if (highlight) Nv.Text else Nv.Text3))
        Text(pct(demand), style = NvType.NumeralS.copy(fontSize = if (highlight) 28.sp else 22.sp, color = if (highlight) Nv.Text else Nv.Text2))
        Text(size, style = NvType.Data.copy(fontSize = 12.sp))
    }
}

@Composable
private fun EvidenceMix(m: Market) {
    val colors = mapOf(Evidence.VERIFIED to Nv.Mint, Evidence.SIGNAL to Nv.Cyan, Evidence.ESTIMATE to Nv.Indigo, Evidence.PREDICTION to Nv.Violet)
    Row(Modifier.fillMaxWidth().height(10.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Evidence.entries.forEach { e ->
            val v = (m.evidenceMix[e] ?: 0).coerceAtLeast(1)
            Box(Modifier.weight(v.toFloat()).fillMaxHeight().background(colors.getValue(e), androidx.compose.foundation.shape.RoundedCornerShape(5.dp)))
        }
    }
    Gap(16)
    Evidence.entries.chunked(2).forEach { pair ->
        Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
            pair.forEach { e -> DotLabel("${e.glyph} ${e.label} ${m.evidenceMix[e] ?: 0}%", colors.getValue(e), Modifier.weight(1f)) }
        }
    }
    Gap(8)
    Text("${m.sources} sources · ${m.signals} signals · updated ${m.updated.lowercase()}", style = NvType.Caption)
}

fun shareMarket(context: Context, m: Market) {
    val f = m.m12
    val text = "NVILE · ${m.name} (${m.scope})\n12-month demand ${pct(f.demand)} (band ${pct(f.bandLow)} to ${pct(f.bandHigh)}, ${f.confidence}% confidence)\n" +
        "Opportunity ${m.probability}% · window ${f.window}\nThe call: ${m.verdict}"
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "NVILE · ${m.name}")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, "Share ${m.name}"))
}
