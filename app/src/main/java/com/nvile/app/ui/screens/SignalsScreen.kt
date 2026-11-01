package com.nvile.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nvile.app.data.Market
import com.nvile.app.data.Stage
import com.nvile.app.data.Tone
import com.nvile.app.ui.NvileViewModel
import com.nvile.app.ui.components.Eyebrow
import com.nvile.app.ui.components.Gap
import com.nvile.app.ui.components.GlassCard
import com.nvile.app.ui.components.NvIcon
import com.nvile.app.ui.components.NvIcons
import com.nvile.app.ui.components.NvScreen
import com.nvile.app.ui.components.SelectChip
import com.nvile.app.ui.components.Sparkline
import com.nvile.app.ui.components.StageTag
import com.nvile.app.ui.components.pct
import com.nvile.app.ui.components.reveal
import com.nvile.app.ui.components.toneColor
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvType

@Composable
fun SignalsScreen(vm: NvileViewModel, openMarket: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf<String?>(null) }
    val all = vm.repo.markets().sortedByDescending { it.probability }

    NvScreen(title = "Signal board", eyebrow = "1,400+ LIVE SOURCES · RESCORED DAILY") {
        // Search
        Row(
            Modifier
                .fillMaxWidth()
                .reveal(0)
                .height(50.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Nv.white(0.05f))
                .border(1.dp, Nv.Hairline, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NvIcon(NvIcons.Search, Nv.Text3, size = 18.dp)
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f)) {
                if (query.isEmpty()) Text("Search markets", style = NvType.Body, color = Nv.Text3)
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = NvType.Body.copy(color = Nv.Text),
                    cursorBrush = SolidColor(Nv.Mint),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Gap(14)
        val filters = listOf(null to "All", Stage.TREND.name to "Trend", Stage.EMERGING.name to "Emerging", Stage.WEAK_SIGNAL.name to "Weak signal")
        LazyRow(Modifier.reveal(1), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filters) { (id, label) ->
                SelectChip(label, filter == id, onClick = { filter = id })
            }
        }
        Gap(24)
        Row(Modifier.fillMaxWidth().reveal(2)) {
            Eyebrow("RANKED BY OPPORTUNITY", Modifier.weight(1f))
            Eyebrow("12MO FORECAST ◈")
        }
        Gap(12)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            all.forEachIndexed { i, m ->
                val visible = (filter == null || m.stage.name == filter) &&
                    (query.isBlank() || m.name.contains(query.trim(), ignoreCase = true))
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    SignalCard(i + 1, m, tracked = m.id in vm.tracked, modifier = Modifier.reveal(3 + i)) { openMarket(m.id) }
                }
            }
        }
    }
}

@Composable
private fun SignalCard(rank: Int, m: Market, tracked: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val color = toneColor(m.tone)
    GlassCard(modifier.fillMaxWidth(), onClick = onClick, padding = androidx.compose.foundation.layout.PaddingValues(18.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Text(rank.toString().padStart(2, '0'), style = NvType.Mono.copy(fontSize = 12.sp), color = Nv.Text4)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(m.name, style = NvType.Headline.copy(fontSize = 17.sp))
                Gap(7)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StageTag(m.stage)
                    if (tracked) {
                        Spacer(Modifier.width(8.dp))
                        Text("TRACKING", style = NvType.MonoTag, color = Nv.Mint)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(pct(m.m12.demand), style = NvType.Number, color = color)
                Gap(4)
                Sparkline(m.spark, if (m.tone == Tone.NEUTRAL) Nv.Text2 else color, Modifier.width(60.dp).height(22.dp))
            }
        }
        Gap(16)
        Box(Modifier.fillMaxWidth().height(1.dp).background(Nv.white(0.06f)))
        Gap(14)
        Row(Modifier.fillMaxWidth()) {
            Stat("PROBABILITY", "${m.probability}%", Modifier.weight(1f))
            Stat("COMPETITION", pct(m.m12.competition), Modifier.weight(1f))
            Stat("WINDOW", m.m12.window, Modifier.weight(1.3f))
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier) {
    Column(modifier) {
        Text(label, style = NvType.MonoTag, color = Nv.Text3)
        Gap(5)
        Text(value, style = NvType.BodyStrong.copy(fontSize = 14.sp), maxLines = 1)
    }
}
