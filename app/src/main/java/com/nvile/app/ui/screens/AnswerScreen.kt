package com.nvile.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nvile.app.data.Market
import com.nvile.app.ui.NvileViewModel
import com.nvile.app.ui.components.Eyebrow
import com.nvile.app.ui.components.Gap
import com.nvile.app.ui.components.GlassCard
import com.nvile.app.ui.components.LiveDot
import com.nvile.app.ui.components.Meter
import com.nvile.app.ui.components.NvIcons
import com.nvile.app.ui.components.NvScreen
import com.nvile.app.ui.components.PrimaryButton
import com.nvile.app.ui.components.SecondaryButton
import com.nvile.app.ui.components.pct
import com.nvile.app.ui.components.rememberHaptics
import com.nvile.app.ui.components.reveal
import com.nvile.app.ui.components.toneColor
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvType
import kotlinx.coroutines.delay

@Composable
fun AnswerScreen(
    vm: NvileViewModel,
    onBack: () -> Unit,
    openDecision: (String) -> Unit,
    openDrivers: (String) -> Unit,
    openMarket: (String) -> Unit,
) {
    val question = vm.question.ifBlank { "When should I enter AI workflow platforms in DACH mid-market?" }
    val m = remember(question) { vm.repo.resolve(question) }
    var done by rememberSaveable(question) { mutableStateOf(false) }
    val haptics = rememberHaptics()

    NvScreen(title = "Answer", eyebrow = "${m.sources} SOURCES · ${m.signals} SIGNALS", onBack = onBack, showLargeTitle = false) {
        // Question bubble
        Column(Modifier.fillMaxWidth().reveal(0), horizontalAlignment = Alignment.End) {
            Eyebrow("YOU ASKED")
            Gap(8)
            Text(
                question,
                style = NvType.BodyStrong.copy(fontSize = 17.sp, lineHeight = 24.sp),
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomStart = 22.dp, bottomEnd = 6.dp))
                    .background(Nv.white(0.08f))
                    .padding(horizontal = 18.dp, vertical = 14.dp),
            )
        }
        Gap(26)

        AnimatedContent(
            done,
            label = "thinking",
            transitionSpec = { (fadeIn(tween(400)) + slideInVertically { it / 10 }) togetherWith fadeOut(tween(200)) },
        ) { isDone ->
            if (!isDone) {
                Thinking(m, deep = vm.deepAnswer) { haptics.confirm(); done = true }
            } else {
                Answer(m, openDecision, openDrivers, openMarket)
            }
        }
    }
}

@Composable
private fun Thinking(m: Market, deep: Boolean, onDone: () -> Unit) {
    val steps = listOf(
        "Reading ${m.sources} sources",
        "Weighing ${m.signals} signals",
        "Running ${if (deep) "deep " else ""}scenarios",
        "Checking invalidation risks",
    )
    var step by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        val per = if (deep) 900L else 560L
        steps.indices.forEach { step = it; delay(per) }
        onDone()
    }
    GlassCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LiveDot()
            Spacer(Modifier.width(6.dp))
            Eyebrow("NVILE IS ANALYSING", color = Nv.Mint)
        }
        Gap(16)
        steps.forEachIndexed { i, s ->
            val state = when {
                i < step -> Nv.Text2
                i == step -> Nv.Text
                else -> Nv.Text4
            }
            Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(if (i < step) "●" else if (i == step) "◈" else "◦", style = NvType.Mono, color = if (i <= step) Nv.Mint else Nv.Text4)
                Spacer(Modifier.width(12.dp))
                Text(s, style = NvType.BodyStrong.copy(fontSize = 15.sp), color = state)
            }
        }
        Gap(14)
        Meter((step + 1) / steps.size.toFloat(), height = 4.dp)
    }
}

@Composable
private fun Answer(m: Market, openDecision: (String) -> Unit, openDrivers: (String) -> Unit, openMarket: (String) -> Unit) {
    val f = m.m12
    val accent = toneColor(m.tone).let { if (it == Nv.Text) Nv.Mint else it }
    Column {
        Eyebrow("NVILE ANSWERED", Modifier.reveal(0))
        Gap(12)
        GlassCard(Modifier.fillMaxWidth().reveal(1), accent = accent) {
            Eyebrow("VERDICT ◈", color = accent)
            Gap(10)
            Typewriter(m.verdict, NvType.Title.copy(fontSize = 30.sp, color = Nv.Text))
            Gap(10)
            Text(
                "Opportunity probability is ${m.probability}%, and the window narrows as competitive intensity rises ${pct(f.competition).removePrefix("+")} through the year.",
                style = NvType.Body,
            )
            Gap(16)
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                MiniStat("Confidence", "${f.confidence}%")
                MiniStat("Band", "${pct(f.bandLow)} → ${pct(f.bandHigh)}")
                MiniStat("Re-scored", "Daily")
            }
        }
        Gap(12)
        Row(Modifier.fillMaxWidth().reveal(2), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("DEMAND 12MO ◈", pct(f.demand), Modifier.weight(1f), accent)
            StatTile("MARKET SIZE '29 ◇", m.marketSize, Modifier.weight(1f))
        }
        Gap(12)
        Row(Modifier.fillMaxWidth().reveal(3), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("COMPETITION ◦", pct(f.competition), Modifier.weight(1f))
            StatTile("ENTRY WINDOW ◈", f.window, Modifier.weight(1f), small = true)
        }
        Gap(24)
        Text("What could kill this", style = NvType.Title2, modifier = Modifier.reveal(4))
        Gap(12)
        GlassCard(Modifier.fillMaxWidth().reveal(5), accent = Nv.Amber) {
            m.risks.forEachIndexed { i, r ->
                if (i > 0) Gap(14)
                Row {
                    Text(r.title, style = NvType.BodyStrong, modifier = Modifier.weight(1f))
                    Text("${r.probability}%", style = NvType.Mono.copy(fontSize = 14.sp), color = Nv.Amber)
                }
                Gap(8)
                Meter(r.probability / 100f, color = Nv.Amber, height = 4.dp, delayMs = 400 + i * 120)
            }
        }
        Gap(24)
        Column(Modifier.reveal(6), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PrimaryButton("Open Decision Center", { openDecision(m.id) }, icon = NvIcons.Target)
            SecondaryButton("Evidence trail", { openDrivers(m.id) }, icon = NvIcons.Shield)
            SecondaryButton("See the full forecast", { openMarket(m.id) }, icon = NvIcons.Signals)
        }
    }
}

/** Reveals text character by character, like a considered reply being written. */
@Composable
private fun Typewriter(text: String, style: androidx.compose.ui.text.TextStyle) {
    var shown by remember(text) { mutableIntStateOf(0) }
    LaunchedEffect(text) {
        while (shown < text.length) { delay(28); shown++ }
    }
    Box {
        Text(text, style = style.copy(color = androidx.compose.ui.graphics.Color.Transparent)) // reserves height
        Text(text.take(shown), style = style)
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column {
        Text(label, style = NvType.Callout.copy(fontSize = 12.sp), color = Nv.Text3)
        Text(value, style = NvType.Mono.copy(fontSize = 13.sp), color = Nv.Text)
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier, accent: androidx.compose.ui.graphics.Color? = null, small: Boolean = false) {
    GlassCard(modifier, padding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
        Eyebrow(label)
        Gap(10)
        Text(value, style = if (small) NvType.Headline.copy(fontSize = 17.sp) else NvType.Number, color = accent ?: Nv.Text)
    }
}
