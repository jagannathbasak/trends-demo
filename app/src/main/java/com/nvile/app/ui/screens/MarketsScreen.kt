package com.nvile.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nvile.app.data.Market
import com.nvile.app.data.Stage
import com.nvile.app.ui.NvileViewModel
import com.nvile.app.ui.components.DotLabel
import com.nvile.app.ui.components.Gap
import com.nvile.app.ui.components.Hairline
import com.nvile.app.ui.components.Mood
import com.nvile.app.ui.components.NvIcon
import com.nvile.app.ui.components.NvIcons
import com.nvile.app.ui.components.Page
import com.nvile.app.ui.components.Sparkline
import com.nvile.app.ui.components.Tabs
import com.nvile.app.ui.components.pct
import com.nvile.app.ui.components.reveal
import com.nvile.app.ui.components.tappable
import com.nvile.app.ui.components.toneColor
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvType

@Composable
fun MarketsScreen(vm: NvileViewModel, openMarket: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val stages = listOf<Stage?>(null, Stage.TREND, Stage.EMERGING, Stage.WEAK_SIGNAL)
    val all = vm.repo.markets().sortedByDescending { it.probability }

    Page(title = "Markets", kicker = "1,400+ sources · rescored daily", mood = Mood.Night) {
        Row(
            Modifier
                .fillMaxWidth()
                .reveal(0)
                .height(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Nv.white(0.1f))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NvIcon(NvIcons.Search, Nv.Text3, size = 18.dp)
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(1f)) {
                if (query.isEmpty()) Text("Search", style = NvType.Body.copy(color = Nv.Text3))
                BasicTextField(query, { query = it }, singleLine = true, textStyle = NvType.Body.copy(color = Nv.Text), cursorBrush = SolidColor(Nv.Mint), modifier = Modifier.fillMaxWidth())
            }
        }
        Gap(14)
        Tabs(listOf("All", "Trend", "Emerging", "Early"), tab, { tab = it }, Modifier.reveal(1))
        Gap(26)
        Row(Modifier.reveal(2).padding(horizontal = 4.dp)) {
            Text("Ranked by opportunity", style = NvType.Caption, modifier = Modifier.weight(1f))
            Text("12-month forecast", style = NvType.Caption)
        }
        Gap(4)
        Column(Modifier.reveal(3)) {
            all.forEachIndexed { i, m ->
                val visible = (stages[tab] == null || m.stage == stages[tab]) && (query.isBlank() || m.name.contains(query.trim(), true))
                AnimatedVisibility(visible, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    Column {
                        Hairline()
                        MarketRow(i + 1, m, m.id in vm.tracked) { openMarket(m.id) }
                    }
                }
            }
            Hairline()
        }
        Gap(18)
        Text(
            "Every figure carries its evidence class: ● verified, ◦ signal, ◇ estimate, ◈ prediction.",
            style = NvType.Caption,
            modifier = Modifier.reveal(4).padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun MarketRow(rank: Int, m: Market, tracked: Boolean, onClick: () -> Unit) {
    val colors = spectrumFor(m.tone)
    Row(
        Modifier
            .fillMaxWidth()
            .tappable(pressedScale = 0.98f, onClick = onClick)
            .heightIn(min = 84.dp)
            .padding(horizontal = 4.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(m.name, style = NvType.Headline.copy(fontSize = 18.sp))
            Spacer(Modifier.height(5.dp))
            DotLabel(
                "${m.stage.label.lowercase().replaceFirstChar { it.uppercase() }} · ${m.probability}%" + if (tracked) " · Watching" else "",
                toneColor(m.tone),
            )
        }
        Sparkline(m.observed, colors, Modifier.size(64.dp, 30.dp))
        Spacer(Modifier.width(14.dp))
        Box(
            Modifier
                .widthIn(min = 76.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.linearGradient(colors.map { it.copy(alpha = 0.9f) }))
                .padding(horizontal = 10.dp, vertical = 7.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Text(pct(m.m12.demand), style = NvType.BodyStrong.copy(fontWeight = FontWeight.SemiBold, color = Nv.OnLight, textAlign = TextAlign.End))
        }
    }
}
