package com.nvile.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nvile.app.data.Market
import com.nvile.app.ui.NvileViewModel
import com.nvile.app.ui.components.Atmosphere
import com.nvile.app.ui.components.Gap
import com.nvile.app.ui.components.Hairline
import com.nvile.app.ui.components.Label
import com.nvile.app.ui.components.Mood
import com.nvile.app.ui.components.NvIcon
import com.nvile.app.ui.components.NvIcons
import com.nvile.app.ui.components.Orb
import com.nvile.app.ui.components.OrbMood
import com.nvile.app.ui.components.TabBarClearance
import com.nvile.app.ui.components.pct
import com.nvile.app.ui.components.rememberHaptics
import com.nvile.app.ui.components.reveal
import com.nvile.app.ui.components.tappable
import com.nvile.app.ui.components.toneColor
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class Exchange(val question: String, val market: Market, val done: Boolean)

@Composable
fun AskScreen(vm: NvileViewModel, openMarket: (String) -> Unit, openDecision: (String) -> Unit) {
    val history = remember { mutableStateListOf<Exchange>() }
    var thinkingStep by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()
    val focus = LocalFocusManager.current
    val haptics = rememberHaptics()
    val thinking = history.lastOrNull()?.done == false
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    fun ask(q: String) {
        if (q.isBlank() || thinking) return
        focus.clearFocus()
        vm.question = ""
        val m = vm.repo.resolve(q)
        history.add(Exchange(q.trim(), m, false))
        scope.launch {
            repeat(4) { thinkingStep = it; delay(if (vm.deepAnswer) 950 else 650) }
            history[history.lastIndex] = history.last().copy(done = true)
            haptics.confirm()
            delay(60)
            scroll.animateScrollTo(scroll.maxValue)
        }
    }

    val empty = history.isEmpty()
    val orbSize by animateDpAsState(if (empty) 250.dp else 96.dp, tween(900, easing = com.nvile.app.ui.theme.NvMotion.Ease), label = "orb")

    Box(Modifier.fillMaxSize()) {
        Atmosphere(if (thinking) Mood.Rising else Mood.Night, strength = if (empty) 0.8f else 0.5f)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(top = top + 20.dp, bottom = TabBarClearance + bottom + 80.dp)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Orb(Modifier.size(orbSize), mood = if (thinking) OrbMood.Thinking else OrbMood.Prediction, energy = if (thinking) 1f else 0.25f)
            if (empty) {
                Gap(8)
                Text("Ask NVILE", style = NvType.Caption.copy(color = Nv.Text2), modifier = Modifier.reveal(1))
                Gap(6)
                Text("What do you want\nto know?", style = NvType.Verdict.copy(fontSize = 40.sp, lineHeight = 44.sp, textAlign = TextAlign.Center), modifier = Modifier.reveal(2))
                Gap(30)
                vm.repo.suggestions().forEachIndexed { i, s ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .reveal(3 + i)
                            .tappable(pressedScale = 0.97f) { ask(s) }
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(s, style = NvType.BodyStrong.copy(fontSize = 17.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Normal), modifier = Modifier.weight(1f))
                        NvIcon(NvIcons.Arrow, Nv.Text3, size = 18.dp)
                    }
                    if (i < vm.repo.suggestions().lastIndex) Hairline()
                }
            } else {
                Gap(18)
                history.forEachIndexed { idx, ex ->
                    if (idx > 0) Gap(30)
                    // question bubble
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        Text(
                            ex.question,
                            style = NvType.BodyStrong.copy(fontSize = 17.sp, lineHeight = 23.sp),
                            modifier = Modifier
                                .widthIn(max = 300.dp)
                                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomStart = 22.dp, bottomEnd = 6.dp))
                                .background(Nv.white(0.12f))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }
                    Gap(18)
                    AnimatedContent(ex.done, label = "answer", transitionSpec = { (fadeIn(tween(500)) + slideInVertically { it / 12 }) togetherWith fadeOut(tween(200)) }) { done ->
                        if (!done) Thinking(ex.market, thinkingStep) else Answer(ex.market, openMarket, openDecision)
                    }
                }
            }
        }

        // Composer
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .imePadding()
                .padding(bottom = TabBarClearance + bottom - 30.dp)
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Nv.Raised3)
                .padding(start = 18.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(1f)) {
                if (vm.question.isEmpty()) Text("Ask about any market", style = NvType.Body.copy(color = Nv.Text3))
                BasicTextField(
                    vm.question, { vm.question = it },
                    textStyle = NvType.Body.copy(color = Nv.Text),
                    cursorBrush = SolidColor(Nv.Mint),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { ask(vm.question) }),
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.width(8.dp))
            val can = vm.question.isNotBlank() && !thinking
            val bg by animateColorAsState(if (can) Nv.Text else Nv.white(0.1f), label = "send")
            Box(
                Modifier.size(42.dp).tappable(enabled = can, pressedScale = 0.88f) { ask(vm.question) }.clip(CircleShape).background(bg),
                contentAlignment = Alignment.Center,
            ) { NvIcon(NvIcons.Send, if (can) Nv.OnLight else Nv.Text3, size = 19.dp, contentDescription = "Ask") }
        }
    }
}

@Composable
private fun Thinking(m: Market, step: Int) {
    val lines = listOf("Reading ${m.sources} sources", "Weighing ${m.signals} signals", "Running scenarios", "Checking what could break it")
    Column(Modifier.fillMaxWidth()) {
        AnimatedContent(step, label = "think", transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) }) {
            Text(lines[it.coerceIn(0, lines.lastIndex)] + "…", style = NvType.VerdictS.copy(color = Nv.Text2))
        }
    }
}

@Composable
private fun Answer(m: Market, openMarket: (String) -> Unit, openDecision: (String) -> Unit) {
    val f = m.m12
    val accent = toneColor(m.tone)
    Column(Modifier.fillMaxWidth()) {
        Label("NVILE · ${m.name}", color = accent)
        Gap(10)
        Text(m.verdict, style = NvType.Verdict)
        Gap(12)
        Text(
            "Opportunity probability is ${m.probability}%. Demand moves ${pct(f.demand)} over 12 months " +
                "(band ${pct(f.bandLow)} to ${pct(f.bandHigh)}), while competition rises ${pct(f.competition).removePrefix("+")}.",
            style = NvType.Body,
        )
        Gap(22)
        Row(Modifier.fillMaxWidth()) {
            Mini("Demand ◈", pct(f.demand), Modifier.weight(1f))
            Mini("Window ◈", f.window, Modifier.weight(1.3f))
            Mini("Confidence", "${f.confidence}%", Modifier.weight(1f))
        }
        Gap(18)
        Hairline()
        Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Biggest risk: ${m.risks.first().title.lowercase()}", style = NvType.Callout, modifier = Modifier.weight(1f))
            Text("${m.risks.first().probability}%", style = NvType.Data.copy(color = Nv.Amber))
        }
        Hairline()
        Gap(18)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionChip("See the forecast") { openMarket(m.id) }
            ActionChip("Open the decision") { openDecision(m.id) }
        }
    }
}

@Composable
private fun Mini(label: String, value: String, modifier: Modifier) {
    Column(modifier) {
        Text(label, style = NvType.Caption)
        Text(value, style = NvType.NumeralS.copy(fontSize = 22.sp), maxLines = 1)
    }
}

@Composable
private fun ActionChip(text: String, onClick: () -> Unit) {
    Row(
        Modifier.tappable(onClick = onClick).clip(RoundedCornerShape(50)).background(Nv.white(0.1f)).padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = NvType.Callout.copy(color = Nv.Text, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium))
        Spacer(Modifier.width(6.dp))
        NvIcon(NvIcons.Arrow, Nv.Text2, size = 14.dp)
    }
}
