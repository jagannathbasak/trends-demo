package com.nvile.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nvile.app.data.Market
import com.nvile.app.data.Tone
import com.nvile.app.ui.NvileViewModel
import com.nvile.app.ui.components.CountUp
import com.nvile.app.ui.components.Eyebrow
import com.nvile.app.ui.components.GlassCard
import com.nvile.app.ui.components.GlassIconButton
import com.nvile.app.ui.components.Gap
import com.nvile.app.ui.components.Meter
import com.nvile.app.ui.components.NvIcon
import com.nvile.app.ui.components.NvIcons
import com.nvile.app.ui.components.NvScreen
import com.nvile.app.ui.components.SectionHeader
import com.nvile.app.ui.components.Sparkline
import com.nvile.app.ui.components.StageTag
import com.nvile.app.ui.components.decap
import com.nvile.app.ui.components.pct
import com.nvile.app.ui.components.reveal
import com.nvile.app.ui.components.tappable
import com.nvile.app.ui.components.toneColor
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen(
    vm: NvileViewModel,
    openMarket: (String) -> Unit,
    openDrivers: (String) -> Unit,
    openAlerts: () -> Unit,
    openAsk: () -> Unit,
    openTrackRecord: () -> Unit,
) {
    val date = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.UK)).uppercase(Locale.UK)
    }
    val markets = vm.repo.markets().filter { it.id in vm.tracked }
    val moved = vm.repo.markets().firstOrNull { it.tone == Tone.CAUTION }
    val lead = vm.repo.markets().first()
    val unread = vm.unreadCount

    NvScreen(
        title = "Today",
        eyebrow = "$date · DAILY BRIEF",
        actions = {
            GlassIconButton(
                onClick = openAlerts,
                modifier = Modifier.semantics { contentDescription = if (unread > 0) "Alerts, $unread new" else "Alerts" },
            ) {
                NvIcon(NvIcons.Bell, Nv.Text, size = 20.dp)
                if (unread > 0) {
                    Box(Modifier.offset(x = 7.dp, y = (-7).dp).size(9.dp).clip(CircleShape).background(Nv.Amber))
                }
            }
        },
    ) {
        // Verdict moved overnight
        if (moved != null) {
            GlassCard(Modifier.fillMaxWidth().reveal(0), accent = Nv.Amber, onClick = { openMarket(moved.id) }) {
                Eyebrow("VERDICT MOVED OVERNIGHT ◈", color = Nv.Amber)
                Gap(10)
                Text("${moved.name} ${moved.verdict.decap()}", style = NvType.Title2)
                Gap(8)
                Text(
                    "Opportunity probability fell to ${moved.probability}% as " +
                        moved.catalysts.first().title.decap() + ".",
                    style = NvType.Body,
                )
                Gap(14)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("See what changed", style = NvType.BodyStrong, color = Nv.Amber)
                    Spacer(Modifier.width(6.dp))
                    NvIcon(NvIcons.Arrow, Nv.Amber, size = 16.dp)
                }
            }
            Gap(28)
        }

        SectionHeader("Your markets", Modifier.reveal(1), trailing = "${markets.size} OF 10 · 12MO")
        GlassCard(Modifier.fillMaxWidth().reveal(2), padding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
            if (markets.isEmpty()) {
                Text("Track a market from the Signal board to see it here.", style = NvType.Body, modifier = Modifier.padding(20.dp))
            }
            markets.forEachIndexed { i, m ->
                if (i > 0) Box(Modifier.fillMaxWidth().padding(start = 18.dp).height(1.dp).background(Nv.white(0.06f)))
                MarketRow(m) { openMarket(m.id) }
            }
        }

        Gap(28)
        SectionHeader("Top catalyst", Modifier.reveal(3))
        val top = lead.catalysts.first()
        GlassCard(Modifier.fillMaxWidth().reveal(4), onClick = { openDrivers(lead.id) }) {
            Eyebrow(lead.name.uppercase())
            Gap(12)
            Row(verticalAlignment = Alignment.Top) {
                Text(top.evidence.glyph, style = NvType.BodyStrong, color = Nv.Mint)
                Spacer(Modifier.width(8.dp))
                Text(top.title + ".", style = NvType.BodyStrong.copy(fontSize = 17.sp, lineHeight = 24.sp), modifier = Modifier.weight(1f))
            }
            Gap(16)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Meter(top.weight.toFloat(), Modifier.weight(1f), delayMs = 400)
                Spacer(Modifier.width(12.dp))
                Text(String.format(Locale.US, "%.2f", top.weight), style = NvType.Mono, color = Nv.Text2)
            }
            Gap(8)
            Eyebrow("WEIGHT IN THE FORECAST · ${top.source.uppercase()}")
        }

        Gap(16)
        // Ask entry
        Row(
            Modifier
                .fillMaxWidth()
                .reveal(5)
                .heightIn(min = 60.dp)
                .tappable(onClick = openAsk)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                .background(Nv.mint(0.07f))
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NvIcon(NvIcons.Sparkle, Nv.Mint, size = 20.dp)
            Spacer(Modifier.width(12.dp))
            Text("Ask NVILE: what markets are about to take off?", style = NvType.Body, color = Nv.Text2, modifier = Modifier.weight(1f))
            NvIcon(NvIcons.Chevron, Nv.Mint, size = 18.dp)
        }

        Gap(16)
        val tr = vm.repo.trackRecord()
        GlassCard(Modifier.fillMaxWidth().reveal(6), onClick = openTrackRecord) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Eyebrow("PUBLIC TRACK RECORD ●")
                    Gap(8)
                    Text("of 12-month forecasts landed inside the stated band.", style = NvType.Callout)
                }
                Spacer(Modifier.width(16.dp))
                CountUp(tr.landedInBand.toFloat(), NvType.Number.copy(fontSize = 36.sp), Nv.Mint, delayMs = 500, format = { "${it.toInt()}%" })
            }
        }
    }
}

@Composable
private fun MarketRow(m: Market, onClick: () -> Unit) {
    val color = toneColor(m.tone)
    Row(
        Modifier
            .fillMaxWidth()
            .tappable(pressedScale = 0.98f, onClick = onClick)
            .heightIn(min = 76.dp)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(m.name, style = NvType.BodyStrong)
            Row(verticalAlignment = Alignment.CenterVertically) {
                StageTag(m.stage)
                Spacer(Modifier.width(8.dp))
                Text("${m.probability}% · ${m.m12.window}", style = NvType.Mono.copy(fontSize = 11.sp), maxLines = 1)
            }
        }
        Sparkline(m.spark, if (m.tone == Tone.NEUTRAL) Nv.Text2 else color, Modifier.size(56.dp, 28.dp))
        Text(
            pct(m.m12.demand),
            style = NvType.Number.copy(fontSize = 19.sp),
            color = color,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(min = 62.dp),
        )
    }
}
