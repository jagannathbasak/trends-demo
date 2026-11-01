package com.nvile.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import android.content.Intent
import com.nvile.app.data.Horizon
import com.nvile.app.data.Tone
import com.nvile.app.ui.NvileViewModel
import com.nvile.app.ui.components.CountUp
import com.nvile.app.ui.components.Eyebrow
import com.nvile.app.ui.components.Gap
import com.nvile.app.ui.components.GlassCard
import com.nvile.app.ui.components.NvIcon
import com.nvile.app.ui.components.NvIcons
import com.nvile.app.ui.components.NvScreen
import com.nvile.app.ui.components.PrimaryButton
import com.nvile.app.ui.components.ScoreRing
import com.nvile.app.ui.components.SecondaryButton
import com.nvile.app.ui.components.SegmentedControl
import com.nvile.app.ui.components.SelectChip
import com.nvile.app.ui.components.ToggleActionButton
import com.nvile.app.ui.components.pct
import com.nvile.app.ui.components.rememberHaptics
import com.nvile.app.ui.components.reveal
import com.nvile.app.ui.components.tappable
import com.nvile.app.ui.components.toneColor
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScoreIdeaScreen(vm: NvileViewModel, onScored: () -> Unit) {
    val scope = rememberCoroutineScope()
    var scoring by rememberSaveable { mutableStateOf(false) }

    NvScreen(title = "Score an idea", eyebrow = "IDEAS") {
        Text(
            "Describe it in a sentence. NVILE scores demand, competition and opportunity against live signals.",
            style = NvType.Lead, modifier = Modifier.reveal(0),
        )
        Gap(22)
        GlassCard(Modifier.fillMaxWidth().reveal(1), padding = androidx.compose.foundation.layout.PaddingValues(18.dp)) {
            Eyebrow("YOUR IDEA")
            Gap(10)
            BasicTextField(
                value = vm.ideaText,
                onValueChange = { vm.ideaText = it },
                textStyle = NvType.Lead.copy(color = Nv.Text, fontSize = 17.sp),
                cursorBrush = SolidColor(Nv.Mint),
                modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp),
            )
        }
        Gap(22)
        Eyebrow("WHO BUYS IT", Modifier.reveal(2))
        Gap(10)
        SegmentedControl(listOf("Businesses", "Consumers"), vm.ideaBuyer, { vm.ideaBuyer = it }, Modifier.reveal(2), mono = false)
        Gap(22)
        Eyebrow("WHERE", Modifier.reveal(3))
        Gap(10)
        FlowRow(
            Modifier.reveal(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            vm.repo.regions().forEach { r -> SelectChip(r, r in vm.ideaRegions, onClick = { vm.toggleRegion(r) }) }
        }
        Gap(22)
        Eyebrow("HORIZON", Modifier.reveal(4))
        Gap(10)
        SegmentedControl(Horizon.entries.map { it.label }, vm.ideaHorizon.ordinal, { vm.ideaHorizon = Horizon.entries[it] }, Modifier.reveal(4))
        Gap(26)
        PrimaryButton(
            text = if (scoring) "Scoring against live signals…" else "Score this idea",
            onClick = {
                if (!scoring) {
                    scoring = true
                    scope.launch {
                        vm.scoreIdea()
                        delay(900)
                        scoring = false
                        onScored()
                    }
                }
            },
            modifier = Modifier.reveal(5),
            icon = NvIcons.Sparkle,
            enabled = vm.ideaText.isNotBlank(),
        )

        Gap(32)
        Eyebrow("RECENT SCORES", Modifier.reveal(6))
        Gap(12)
        GlassCard(Modifier.fillMaxWidth().reveal(7), padding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
            vm.repo.recentScores().forEachIndexed { i, s ->
                if (i > 0) Box(Modifier.fillMaxWidth().padding(start = 18.dp).height(1.dp).background(Nv.white(0.06f)))
                Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(s.title, style = NvType.BodyStrong)
                        Gap(5)
                        Text(s.verdict, style = NvType.MonoTag, color = toneColor(s.tone))
                    }
                    Text(s.score.toString(), style = NvType.Number, color = toneColor(s.tone))
                }
            }
        }
    }
}

private val reportParts = listOf(
    "Verdict", "Current state", "Evidence trail", "Forecast",
    "Competitive intensity", "Timing window", "Invalidation risks", "Recommendation",
)

@Composable
fun IdeaResultScreen(vm: NvileViewModel, onBack: () -> Unit, openMarket: (String) -> Unit) {
    val r = vm.ideaResult ?: run { LaunchedEffect(Unit) { vm.scoreIdea() }; return }
    val m = r.market
    val f = m.horizons.getValue(r.horizon)
    val color = if (m.tone == Tone.CAUTION) Nv.Amber else Nv.Mint
    val haptics = rememberHaptics()
    val context = LocalContext.current
    var open by rememberSaveable { mutableStateOf(-1) }

    NvScreen(title = "Idea score", eyebrow = "IDEA SCORE · ${r.horizon.label}", onBack = onBack, showLargeTitle = false) {
        Text(r.idea, style = NvType.Title2, modifier = Modifier.reveal(0))
        Gap(24)
        Box(Modifier.fillMaxWidth().reveal(1), contentAlignment = Alignment.Center) {
            ScoreRing(r.score, ringSize = 220.dp, color = color)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CountUp(r.score.toFloat(), NvType.BigNumber.copy(fontSize = 68.sp, lineHeight = 68.sp), color, durationMs = 1800, delayMs = 250)
                Eyebrow("OF 100")
            }
        }
        Gap(12)
        Eyebrow("OPPORTUNITY SCORE ◈ · ${r.label}", Modifier.align(Alignment.CenterHorizontally).reveal(2), color = color)
        Gap(24)
        Row(Modifier.fillMaxWidth().reveal(3), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ScoreStat("DEMAND", pct(f.demand), if (f.demand > 0.3) "rising" else "flat", Modifier.weight(1f))
            ScoreStat("COMPETITION", pct(f.competition), if (f.competition > 0.25) "rising fast" else "moderate", Modifier.weight(1f))
            ScoreStat("CONFIDENCE", "${f.confidence}%", "band ±${(f.bandHigh - f.bandLow).times(50).toInt()} pts", Modifier.weight(1f))
        }
        Gap(14)
        GlassCard(Modifier.fillMaxWidth().reveal(4), accent = color) {
            Eyebrow("VERDICT ◈", color = color)
            Gap(8)
            Text(r.verdict, style = NvType.Title2)
            Gap(8)
            Text(r.verdictDetail, style = NvType.Body)
            Gap(14)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Eyebrow("TOP RISK", color = Nv.Amber)
                Spacer(Modifier.width(10.dp))
                Text("${m.risks.first().title} · ${m.risks.first().probability}%", style = NvType.Callout.copy(color = Nv.Text))
            }
        }

        Gap(28)
        Row(Modifier.fillMaxWidth().reveal(5), verticalAlignment = Alignment.Bottom) {
            Text("Opportunity Report", style = NvType.Title2, modifier = Modifier.weight(1f))
            Eyebrow("8 PARTS")
        }
        Gap(12)
        GlassCard(Modifier.fillMaxWidth().reveal(6), padding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
            reportParts.forEachIndexed { i, part ->
                if (i > 0) Box(Modifier.fillMaxWidth().padding(start = 18.dp).height(1.dp).background(Nv.white(0.06f)))
                val expanded = open == i
                val rot by animateFloatAsState(if (expanded) 90f else 0f, label = "chev")
                Row(
                    Modifier
                        .fillMaxWidth()
                        .tappable(pressedScale = 0.985f) { open = if (expanded) -1 else i }
                        .heightIn(min = 56.dp)
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text((i + 1).toString().padStart(2, '0'), style = NvType.Mono, color = Nv.Text4)
                    Spacer(Modifier.width(14.dp))
                    Text(part, style = NvType.BodyStrong, modifier = Modifier.weight(1f))
                    NvIcon(NvIcons.Chevron, Nv.Text3, size = 16.dp, modifier = Modifier.graphicsLayer { rotationZ = rot })
                }
                AnimatedVisibility(expanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    Text(
                        partBody(i, r.verdict, r.verdictDetail, m),
                        style = NvType.Body,
                        modifier = Modifier.padding(start = 52.dp, end = 18.dp, bottom = 18.dp),
                    )
                }
            }
        }

        Gap(24)
        Column(Modifier.reveal(7), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ToggleActionButton(vm.trackedIdea, "Track this idea", "Tracking", { haptics.confirm(); vm.trackedIdea = !vm.trackedIdea })
            SecondaryButton("Share report", {
                val text = "NVILE idea score: ${r.score}/100 (${r.label.lowercase()})\n${r.idea}\nVerdict: ${r.verdict}\n${r.verdictDetail}"
                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }, "Share report"))
            }, icon = NvIcons.Share)
            SecondaryButton("See ${m.name}", { openMarket(m.id) }, icon = NvIcons.Signals)
        }
    }
}

private fun partBody(i: Int, verdict: String, detail: String, m: com.nvile.app.data.Market): String = when (i) {
    0 -> "$verdict $detail"
    1 -> m.decision.happening
    2 -> m.catalysts.joinToString("\n") { "${it.evidence.glyph} ${it.title} · ${it.source}" }
    3 -> m.decision.next
    4 -> "Competition rises ${pct(m.m12.competition)} over 12 months and ${pct(m.horizons.getValue(Horizon.Y3).competition)} over three years."
    5 -> "Entry window: ${m.m12.window}."
    6 -> m.risks.joinToString("\n") { "${it.title} · ${it.probability}%" }
    else -> m.decision.recommendation
}

@Composable
private fun ScoreStat(label: String, value: String, note: String, modifier: Modifier) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier
            .clip(shape)
            .background(Nv.white(0.04f))
            .border(1.dp, Nv.Hairline, shape)
            .padding(14.dp),
    ) {
        Text(label, style = NvType.Eyebrow.copy(fontSize = 9.sp, letterSpacing = 0.06.em), maxLines = 1)
        Gap(8)
        Text(value, style = NvType.Number.copy(fontSize = 20.sp))
        Gap(2)
        Text(note, style = NvType.Callout.copy(fontSize = 11.sp), maxLines = 1)
    }
}
