package com.nvile.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nvile.app.data.Market
import com.nvile.app.data.Tone
import com.nvile.app.ui.NvileViewModel
import com.nvile.app.ui.components.Bar
import com.nvile.app.ui.components.CountUp
import com.nvile.app.ui.components.DotLabel
import com.nvile.app.ui.components.Gap
import com.nvile.app.ui.components.Label
import com.nvile.app.ui.components.Mood
import com.nvile.app.ui.components.NvIcon
import com.nvile.app.ui.components.NvIcons
import com.nvile.app.ui.components.Orb
import com.nvile.app.ui.components.Page
import com.nvile.app.ui.components.Panel
import com.nvile.app.ui.components.RoundButton
import com.nvile.app.ui.components.SectionTitle
import com.nvile.app.ui.components.Sparkline
import com.nvile.app.ui.components.decap
import com.nvile.app.ui.components.pct
import com.nvile.app.ui.components.reveal
import com.nvile.app.ui.components.tappable
import com.nvile.app.ui.components.toneColor
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvType
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

fun spectrumFor(t: Tone) = when (t) {
    Tone.CAUTION -> Nv.Caution
    Tone.NEUTRAL -> listOf(Nv.Cyan, Nv.Indigo)
    Tone.UP -> Nv.Spectrum
}

@Composable
fun PulseScreen(
    vm: NvileViewModel,
    openMarket: (String) -> Unit,
    openAlerts: () -> Unit,
    openAsk: () -> Unit,
    openTrackRecord: () -> Unit,
) {
    val markets = vm.repo.markets().filter { it.id in vm.tracked }
    val moved = vm.repo.markets().firstOrNull { it.tone == Tone.CAUTION }
    val lead = vm.repo.markets().first()
    val date = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.UK)) }
    val greeting = remember {
        when (LocalTime.now().hour) { in 5..11 -> "Good morning"; in 12..17 -> "Good afternoon"; else -> "Good evening" }
    }

    Page(
        title = greeting,
        kicker = date,
        mood = Mood.Calm,
        horizontalPadding = 0.dp,
        actions = {
            Box {
                RoundButton(NvIcons.Bell, if (vm.unreadCount > 0) "Alerts, ${vm.unreadCount} new" else "Alerts", openAlerts, size = 40.dp)
                if (vm.unreadCount > 0) Box(Modifier.align(Alignment.TopEnd).offset(x = 2.dp, y = (-2).dp).size(10.dp).clip(CircleShape).background(Nv.Amber))
            }
        },
    ) {
        if (moved != null) {
            VerdictCard(moved, Modifier.padding(horizontal = 20.dp).reveal(0)) { openMarket(moved.id) }
            Gap(36)
        }

        SectionTitle("Your markets", Modifier.padding(horizontal = 20.dp).reveal(1))
        val pager = rememberPagerState { markets.size }
        HorizontalPager(
            state = pager,
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 12.dp,
            modifier = Modifier.reveal(2),
        ) { page ->
            val m = markets[page]
            val off = abs((pager.currentPage - page) + pager.currentPageOffsetFraction).coerceIn(0f, 1f)
            MarketCard(
                m,
                Modifier.graphicsLayer {
                    val s = 1f - 0.07f * off
                    scaleX = s; scaleY = s
                    alpha = 1f - 0.45f * off
                },
            ) { openMarket(m.id) }
        }
        Gap(14)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            repeat(markets.size) { i ->
                val on = i == pager.currentPage
                Box(Modifier.padding(3.dp).size(if (on) 7.dp else 6.dp).clip(CircleShape).background(if (on) Nv.Text else Nv.Text4))
            }
        }

        Gap(40)
        val top = lead.catalysts.first()
        Column(Modifier.padding(horizontal = 20.dp).reveal(3)) {
            Label("Signal of the day")
            Gap(12)
            Text(top.title + ".", style = NvType.Display.copy(fontSize = 30.sp, lineHeight = 36.sp))
            Gap(18)
            Bar(top.weight.toFloat(), delayMs = 300)
            Gap(10)
            Row {
                Text("${lead.name} · ${top.source}", style = NvType.Caption, modifier = Modifier.weight(1f))
                Text("weight ${"%.2f".format(Locale.US, top.weight)}", style = NvType.Data.copy(fontSize = 12.sp))
            }
        }

        Gap(40)
        Row(
            Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .reveal(4)
                .tappable(onClick = openAsk)
                .clip(RoundedCornerShape(50))
                .background(Nv.white(0.08f))
                .padding(start = 6.dp, end = 18.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Orb(Modifier.size(48.dp), energy = 0.3f)
            Spacer(Modifier.width(10.dp))
            Text("Ask about any market…", style = NvType.Body, modifier = Modifier.weight(1f))
            NvIcon(NvIcons.Arrow, Nv.Text2, size = 18.dp)
        }

        Gap(16)
        val tr = vm.repo.trackRecord()
        Panel(Modifier.padding(horizontal = 20.dp).fillMaxWidth().reveal(5), onClick = openTrackRecord) {
            Row(Modifier.padding(22.dp), verticalAlignment = Alignment.CenterVertically) {
                CountUp(tr.landedInBand.toFloat(), NvType.NumeralM, format = { "${it.toInt()}%" }, delayMs = 400)
                Spacer(Modifier.width(16.dp))
                Text("of NVILE's 12-month calls landed inside the stated band.", style = NvType.Callout, modifier = Modifier.weight(1f))
                NvIcon(NvIcons.Chevron, Nv.Text3, size = 16.dp)
            }
        }
    }
}

@Composable
private fun VerdictCard(m: Market, modifier: Modifier, onClick: () -> Unit) {
    Panel(modifier.fillMaxWidth().height(340.dp), color = Color(0xFF120B04), radius = 32.dp, onClick = onClick) {
        Box(Modifier.fillMaxSize()) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(Brush.radialGradient(listOf(Nv.Amber.copy(alpha = 0.55f), Nv.Coral.copy(alpha = 0.18f), Color.Transparent), Offset(size.width * 0.9f, 0f), size.width * 0.95f), size.width * 0.95f, Offset(size.width * 0.9f, 0f))
                drawCircle(Brush.radialGradient(listOf(Nv.Coral.copy(alpha = 0.25f), Color.Transparent), Offset(0f, size.height), size.width * 0.7f), size.width * 0.7f, Offset(0f, size.height))
            }
            Column(Modifier.fillMaxSize().padding(24.dp)) {
                Label("Verdict moved · overnight", color = Nv.Amber)
                Gap(14)
                Text("${m.name} ${m.verdict.decap()}", style = NvType.Verdict.copy(fontSize = 34.sp, lineHeight = 38.sp))
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(1f)) {
                        CountUp(m.probability.toFloat(), NvType.NumeralM.copy(color = Nv.Amber), format = { "${it.toInt()}%" }, delayMs = 300)
                        Text("opportunity probability", style = NvType.Caption.copy(color = Nv.Text2))
                    }
                    Sparkline(m.observed, Nv.Caution, Modifier.size(110.dp, 48.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun MarketCard(m: Market, modifier: Modifier, onClick: () -> Unit) {
    val colors = spectrumFor(m.tone)
    Panel(modifier.fillMaxWidth().height(300.dp), radius = 30.dp, onClick = onClick) {
        Box(Modifier.fillMaxSize()) {
            Canvas(Modifier.fillMaxSize()) {
                val c = Offset(size.width, 0f)
                drawCircle(Brush.radialGradient(listOf(colors.first().copy(alpha = 0.22f), Color.Transparent), c, size.width * 0.9f), size.width * 0.9f, c)
            }
            Column(Modifier.fillMaxSize().padding(22.dp)) {
                Text(m.name, style = NvType.Headline)
                Gap(6)
                DotLabel("${m.stage.label.lowercase().replaceFirstChar { it.uppercase() }} · ${m.probability}% probability", toneColor(m.tone))
                Spacer(Modifier.weight(1f))
                Text(pct(m.m12.demand), style = NvType.Numeral.copy(fontSize = 80.sp, lineHeight = 80.sp, brush = Brush.linearGradient(colors)))
                Text("12-month demand · window ${m.m12.window}", style = NvType.Caption.copy(color = Nv.Text2))
                Gap(16)
                Sparkline(m.observed, colors, Modifier.fillMaxWidth().height(40.dp))
            }
        }
    }
}
