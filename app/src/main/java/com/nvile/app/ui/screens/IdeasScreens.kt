package com.nvile.app.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nvile.app.data.Horizon
import com.nvile.app.data.Market
import com.nvile.app.data.Tone
import com.nvile.app.ui.NvileViewModel
import com.nvile.app.ui.components.Chip
import com.nvile.app.ui.components.CountUp
import com.nvile.app.ui.components.Gap
import com.nvile.app.ui.components.GhostButton
import com.nvile.app.ui.components.Group
import com.nvile.app.ui.components.GroupRow
import com.nvile.app.ui.components.Hairline
import com.nvile.app.ui.components.Label
import com.nvile.app.ui.components.Mood
import com.nvile.app.ui.components.NvIcon
import com.nvile.app.ui.components.NvIcons
import com.nvile.app.ui.components.Orb
import com.nvile.app.ui.components.OrbMood
import com.nvile.app.ui.components.Page
import com.nvile.app.ui.components.PillButton
import com.nvile.app.ui.components.ScoreDial
import com.nvile.app.ui.components.SectionTitle
import com.nvile.app.ui.components.Tabs
import com.nvile.app.ui.components.moodFor
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
fun IdeasScreen(vm: NvileViewModel, onScored: () -> Unit) {
    val scope = rememberCoroutineScope()
    var scoring by rememberSaveable { mutableStateOf(false) }

    Page(title = "Ideas", kicker = "Score anything against live signals", mood = Mood.Calm) {
        Box(Modifier.fillMaxWidth().reveal(0)) {
            if (vm.ideaText.isEmpty()) Text("Describe your idea…", style = NvType.Display.copy(fontSize = 30.sp, lineHeight = 36.sp, color = Nv.Text4))
            BasicTextField(
                vm.ideaText, { vm.ideaText = it },
                textStyle = NvType.Display.copy(fontSize = 30.sp, lineHeight = 36.sp),
                cursorBrush = SolidColor(Nv.Mint),
                modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
            )
        }
        Gap(28)
        Label("Who buys it", Modifier.reveal(1))
        Gap(10)
        Tabs(listOf("Businesses", "Consumers"), vm.ideaBuyer, { vm.ideaBuyer = it }, Modifier.reveal(1))
        Gap(24)
        Label("Where", Modifier.reveal(2))
        Gap(10)
        FlowRow(Modifier.reveal(2), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            vm.repo.regions().forEach { r -> Chip(r, r in vm.ideaRegions, { vm.toggleRegion(r) }) }
        }
        Gap(24)
        Label("Horizon", Modifier.reveal(3))
        Gap(10)
        Tabs(Horizon.entries.map { it.label }, vm.ideaHorizon.ordinal, { vm.ideaHorizon = Horizon.entries[it] }, Modifier.reveal(3))
        Gap(32)
        Box(Modifier.reveal(4)) {
            if (scoring) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Orb(Modifier.size(58.dp), mood = OrbMood.Thinking, energy = 1f)
                    Spacer(Modifier.width(8.dp))
                    Text("Scoring against live signals…", style = NvType.VerdictS.copy(fontSize = 22.sp, color = Nv.Text2))
                }
            } else {
                PillButton("Score this idea", {
                    scoring = true
                    scope.launch { vm.scoreIdea(); delay(1400); scoring = false; onScored() }
                }, icon = NvIcons.Sparkle, enabled = vm.ideaText.isNotBlank())
            }
        }
        Gap(44)
        Group(Modifier.reveal(5), header = "Recent") {
            vm.repo.recentScores().forEachIndexed { i, s ->
                GroupRow(s.title, subtitle = s.verdict.lowercase().replaceFirstChar { it.uppercase() }, showDivider = i > 0, trailing = {
                    Text(s.score.toString(), style = NvType.NumeralS.copy(color = toneColor(s.tone)))
                })
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
    val caution = m.tone == Tone.CAUTION
    val haptics = rememberHaptics()
    val context = LocalContext.current
    var open by rememberSaveable { mutableIntStateOf(-1) }

    Page(title = "Your idea", mood = moodFor(m.tone), onBack = onBack) {
        Text(r.idea, style = NvType.Callout.copy(textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth().reveal(0).padding(horizontal = 12.dp))
        Gap(18)
        Box(Modifier.fillMaxWidth().reveal(1), contentAlignment = Alignment.Center) {
            ScoreDial(r.score, caution = caution)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CountUp(r.score.toFloat(), NvType.Numeral.copy(fontSize = 96.sp), durationMs = 2000, delayMs = 300)
                Text("of 100 · ${r.label.lowercase()}", style = NvType.Caption.copy(color = Nv.Text2))
            }
        }
        Gap(26)
        Text(r.verdict, style = NvType.Verdict.copy(textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth().reveal(2))
        Gap(10)
        Text(r.verdictDetail, style = NvType.Body.copy(textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth().reveal(3))
        Gap(30)
        Hairline()
        Row(Modifier.fillMaxWidth().padding(vertical = 18.dp).reveal(4)) {
            Stat("Demand ◈", pct(f.demand), Modifier.weight(1f))
            Divider()
            Stat("Competition ◈", pct(f.competition), Modifier.weight(1f))
            Divider()
            Stat("Confidence", "${f.confidence}%", Modifier.weight(1f))
        }
        Hairline()
        Gap(14)
        Text("Biggest risk: ${m.risks.first().title.lowercase()} (${m.risks.first().probability}%)", style = NvType.Callout.copy(color = Nv.Amber, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth())

        Gap(40)
        SectionTitle("Opportunity report")
        Group(Modifier.reveal(5)) {
            reportParts.forEachIndexed { i, part ->
                val expanded = open == i
                val rot by animateFloatAsState(if (expanded) 90f else 0f, label = "chev")
                GroupRow(
                    part, showDivider = i > 0,
                    onClick = { open = if (expanded) -1 else i },
                    trailing = { NvIcon(NvIcons.Chevron, Nv.Text3, size = 16.dp, modifier = Modifier.graphicsLayer { rotationZ = rot }) },
                )
                AnimatedVisibility(expanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    Text(partBody(i, r.verdict, r.verdictDetail, m), style = NvType.Callout, modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 16.dp))
                }
            }
        }
        Gap(28)
        PillButton(if (vm.trackedIdea) "Watching this idea" else "Watch this idea", { haptics.confirm(); vm.trackedIdea = !vm.trackedIdea }, icon = if (vm.trackedIdea) NvIcons.Check else NvIcons.Bell)
        Gap(10)
        GhostButton("Share report", {
            val text = "NVILE idea score: ${r.score}/100\n${r.idea}\n${r.verdict} ${r.verdictDetail}"
            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }, "Share report"))
        }, icon = NvIcons.Share)
        Gap(10)
        Text("See ${m.name}", style = NvType.BodyStrong.copy(color = Nv.Mint, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth().tappable { openMarket(m.id) }.padding(12.dp))
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = NvType.Caption)
        Text(value, style = NvType.NumeralS.copy(fontSize = 26.sp, fontWeight = FontWeight.Light))
    }
}

@Composable
private fun Divider() = Box(Modifier.width(0.5.dp).height(44.dp).background(Nv.Separator))

private fun partBody(i: Int, verdict: String, detail: String, m: Market): String = when (i) {
    0 -> "$verdict $detail"
    1 -> m.decision.happening
    2 -> m.catalysts.joinToString("\n") { "${it.evidence.glyph} ${it.title} · ${it.source}" }
    3 -> m.decision.next
    4 -> "Competition rises ${pct(m.m12.competition)} over 12 months and ${pct(m.horizons.getValue(Horizon.Y3).competition)} over three years."
    5 -> "Entry window: ${m.m12.window}."
    6 -> m.risks.joinToString("\n") { "${it.title} · ${it.probability}%" }
    else -> m.decision.recommendation
}
