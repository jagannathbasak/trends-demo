package com.nvile.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nvile.app.data.Evidence
import com.nvile.app.data.Tone
import com.nvile.app.ui.NvileViewModel
import com.nvile.app.ui.components.Atmosphere
import com.nvile.app.ui.components.Bar
import com.nvile.app.ui.components.CountUp
import com.nvile.app.ui.components.Gap
import com.nvile.app.ui.components.GhostButton
import com.nvile.app.ui.components.Label
import com.nvile.app.ui.components.Mood
import com.nvile.app.ui.components.NvIcons
import com.nvile.app.ui.components.PillButton
import com.nvile.app.ui.components.RoundButton
import com.nvile.app.ui.components.rememberHaptics
import com.nvile.app.ui.components.reveal
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvType

private data class Chapter(val kicker: String, val evidence: Evidence?, val text: String)

/** The Decision Center told as a story: four full-screen chapters, tap to advance. */
@Composable
fun DecisionScreen(vm: NvileViewModel, marketId: String, onClose: () -> Unit) {
    val m = vm.repo.market(marketId)
    val d = m.decision
    val chapters = listOf(
        Chapter("What is happening", Evidence.VERIFIED, d.happening),
        Chapter("Why it is happening", Evidence.SIGNAL, d.why),
        Chapter("What happens next", Evidence.PREDICTION, d.next),
        Chapter("What you should do", null, d.action),
    )
    var i by rememberSaveable { mutableIntStateOf(0) }
    val haptics = rememberHaptics()
    val context = LocalContext.current
    val last = i == chapters.lastIndex
    fun go(to: Int) { val n = to.coerceIn(0, chapters.lastIndex); if (n != i) { haptics.tick(); i = n } }
    BackHandler { if (i > 0) go(i - 1) else onClose() }

    val mood = when (i) {
        0 -> Mood.Night
        1 -> Mood.Calm
        else -> if (m.tone == Tone.CAUTION) Mood.Caution else Mood.Rising
    }

    Box(Modifier.fillMaxSize()) {
        Atmosphere(mood, strength = 1.1f)
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 22.dp)) {
            // Story progress
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                chapters.indices.forEach { k ->
                    val fill = remember(k) { Animatable(0f) }
                    LaunchedEffect(i) {
                        when {
                            k < i -> fill.snapTo(1f)
                            k > i -> fill.snapTo(0f)
                            else -> { fill.snapTo(0f); fill.animateTo(1f, tween(5200, easing = LinearEasing)) }
                        }
                    }
                    Box(Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(Nv.white(0.2f))) {
                        Box(Modifier.fillMaxWidth(fill.value).fillMaxHeight().background(Nv.Text))
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(m.name, style = NvType.Headline)
                    Text("Decision · ${m.scope.lowercase().replaceFirstChar { it.uppercase() }}", style = NvType.Caption)
                }
                RoundButton(NvIcons.Close, "Close", onClose, size = 40.dp)
            }

            // Chapter body: tap right to advance, left to go back.
            Box(Modifier.weight(1f).fillMaxWidth()) {
                AnimatedContent(
                    i, label = "chapter",
                    transitionSpec = { (fadeIn(tween(500)) + slideInVertically(tween(700)) { it / 8 }) togetherWith fadeOut(tween(250)) },
                ) { k ->
                    val c = chapters[k]
                    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                        Text("0${k + 1}", style = NvType.Numeral.copy(fontSize = 64.sp, color = Nv.Text4))
                        Gap(6)
                        Label(c.kicker + (c.evidence?.let { " ${it.glyph}" } ?: ""), color = Nv.Text2)
                        Gap(18)
                        Text(c.text, style = NvType.Verdict.copy(fontSize = if (k == chapters.lastIndex) 44.sp else 36.sp, lineHeight = if (k == chapters.lastIndex) 48.sp else 41.sp))
                        if (k == chapters.lastIndex) {
                            Gap(18)
                            Text(d.recommendation, style = NvType.Body, modifier = Modifier.reveal(2))
                            Gap(30)
                            Outcomes(d.actNow, d.waitTwoQuarters, m.tone == Tone.CAUTION)
                        }
                    }
                }
                if (!last) {
                    Row(Modifier.fillMaxSize()) {
                        Box(Modifier.weight(0.35f).fillMaxHeight().clickable(remember { MutableInteractionSource() }, null) { go(i - 1) })
                        Box(Modifier.weight(0.65f).fillMaxHeight().clickable(remember { MutableInteractionSource() }, null) { go(i + 1) })
                    }
                }
            }

            if (last) {
                val tracking = m.id in vm.trackedDecisions
                PillButton(if (tracking) "Watching this decision" else "Watch this decision", { haptics.confirm(); vm.toggleDecision(m.id) }, icon = if (tracking) NvIcons.Check else NvIcons.Bell)
                Gap(10)
                GhostButton("Share with my team", { shareMarket(context, m) }, icon = NvIcons.Share)
                Gap(10)
                Text("A modelled projection with a band, not a guarantee. Re-scored as signals land.", style = NvType.Caption, modifier = Modifier.padding(bottom = 8.dp))
            } else {
                Text("Tap to continue", style = NvType.Caption.copy(color = Nv.Text3), modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 20.dp))
            }
        }
    }

    // Auto-advance like a story.
    LaunchedEffect(i) {
        if (i < chapters.lastIndex) { kotlinx.coroutines.delay(5200); go(i + 1) }
    }
}

@Composable
private fun Outcomes(now: Int, wait: Int, caution: Boolean) {
    val colors = if (caution) Nv.Caution else Nv.Spectrum
    Row(verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f)) {
            CountUp(now.toFloat(), NvType.NumeralM, format = { "${it.toInt()}%" }, delayMs = 300)
            Text("if you act now", style = NvType.Caption.copy(color = Nv.Text2))
        }
        Spacer(Modifier.width(20.dp))
        Column(Modifier.weight(1f)) {
            CountUp(wait.toFloat(), NvType.NumeralM.copy(color = Nv.Text3), format = { "${it.toInt()}%" }, delayMs = 500)
            Text("if you wait two quarters", style = NvType.Caption)
        }
    }
    Gap(12)
    Bar(now / 100f, colors = colors, delayMs = 300, thickness = 6.dp)
    Gap(8)
    Bar(wait / 100f, colors = listOf(Nv.white(0.25f), Nv.white(0.45f)), delayMs = 500, thickness = 6.dp)
}
