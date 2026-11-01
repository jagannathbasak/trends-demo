package com.nvile.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role as SemRole
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nvile.app.data.Role
import com.nvile.app.ui.NvileViewModel
import com.nvile.app.ui.components.Atmosphere
import com.nvile.app.ui.components.Mood
import com.nvile.app.ui.components.NvIcons
import com.nvile.app.ui.components.Orb
import com.nvile.app.ui.components.OrbMood
import com.nvile.app.ui.components.PillButton
import com.nvile.app.ui.components.RoundButton
import com.nvile.app.ui.components.rememberHaptics
import com.nvile.app.ui.components.reveal
import com.nvile.app.ui.components.tappable
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvMotion
import com.nvile.app.ui.theme.NvType
import kotlinx.coroutines.delay

private data class Star(val id: String, val label: String, val x: Float, val y: Float, val size: Float)

private val constellation = listOf(
    Star("ai-workflow", "AI workflow\nplatforms", 0.30f, 0.18f, 132f),
    Star("energy-storage", "Energy\nstorage", 0.74f, 0.28f, 116f),
    Star("clinical-ai", "Clinical\nAI", 0.22f, 0.52f, 100f),
    Star("circular-packaging", "Circular\npackaging", 0.64f, 0.60f, 108f),
    Star("robotics", "Robotics", 0.80f, 0.84f, 86f),
    Star("fintech", "Fintech\ninfra", 0.32f, 0.84f, 90f),
    Star("glp1", "GLP-1\neconomy", 0.90f, 0.06f, 76f),
)

@Composable
fun OnboardingScreen(vm: NvileViewModel, onDone: (Role, Set<String>) -> Unit, onBack: () -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var role by rememberSaveable { mutableStateOf(vm.role) }
    var picked by rememberSaveable { mutableStateOf(setOf("ai-workflow", "energy-storage", "clinical-ai", "circular-packaging")) }
    val trackable = vm.repo.markets().map { it.id }.toSet()

    BackHandler { if (step > 0 && step < 2) step-- else if (step == 0) onBack() }

    Box(Modifier.fillMaxSize()) {
        Atmosphere(if (step == 2) Mood.Rising else Mood.Night, strength = if (step == 2) 1f else 0.7f)
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 8.dp)) {
            Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
                if (step < 2) RoundButton(NvIcons.Back, "Back", { if (step == 0) onBack() else step-- }, size = 40.dp)
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(3) { i ->
                        val w by animateDpAsState(if (i == step) 22.dp else 6.dp, NvMotion.settle(), label = "w")
                        val c by animateColorAsState(if (i <= step) Nv.Text else Nv.white(0.2f), label = "c")
                        Box(Modifier.width(w).height(6.dp).clip(CircleShape).background(c))
                    }
                }
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(40.dp))
            }

            AnimatedContent(
                step, Modifier.weight(1f), label = "step",
                transitionSpec = {
                    val dir = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(tween(600, easing = NvMotion.Ease)) { it * dir / 4 } + fadeIn(tween(420))) togetherWith
                        (slideOutHorizontally(tween(500, easing = NvMotion.Ease)) { -it * dir / 5 } + fadeOut(tween(220)))
                },
            ) { s ->
                when (s) {
                    0 -> RoleStep(role) { role = it }
                    1 -> MarketsStep(picked) { id -> picked = if (id in picked) picked - id else picked + id }
                    else -> TuningStep { onDone(role, picked.filter { it in trackable }.toSet()) }
                }
            }

            AnimatedVisibility(step < 2, enter = fadeIn(), exit = fadeOut()) {
                PillButton(
                    text = if (step == 0) "Continue" else "Build my brief",
                    onClick = { step++ },
                    enabled = step == 0 || picked.isNotEmpty(),
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun RoleStep(role: Role, onPick: (Role) -> Unit) {
    val haptics = rememberHaptics()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 28.dp)) {
        Text("What do you\ndecide?", style = NvType.Display, modifier = Modifier.reveal(0))
        Spacer(Modifier.height(10.dp))
        Text("NVILE shapes every brief around it.", style = NvType.Body, modifier = Modifier.reveal(1))
        Spacer(Modifier.height(36.dp))
        Role.entries.forEachIndexed { i, r ->
            val on = r == role
            val color by animateColorAsState(if (on) Nv.Text else Nv.Text4, tween(300), label = "role")
            val shift by animateDpAsState(if (on) 14.dp else 0.dp, NvMotion.press(), label = "shift")
            Column(
                Modifier
                    .fillMaxWidth()
                    .reveal(2 + i)
                    .semantics { selected = on }
                    .tappable(pressedScale = 0.98f, role = SemRole.RadioButton, haptic = false) { haptics.tick(); onPick(r) }
                    .padding(vertical = 10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val dot by animateFloatAsState(if (on) 1f else 0f, NvMotion.press(), label = "dot")
                    Box(
                        Modifier.size(8.dp).graphicsLayer { scaleX = dot; scaleY = dot; alpha = dot }
                            .clip(CircleShape).background(Brush.linearGradient(Nv.Spectrum)),
                    )
                    Text(
                        r.title,
                        style = NvType.Display.copy(fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Light),
                        color = color,
                        modifier = Modifier.padding(start = shift),
                    )
                }
                AnimatedVisibility(on, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    Text(r.use, style = NvType.Callout, modifier = Modifier.padding(start = 22.dp, top = 2.dp))
                }
            }
        }
    }
}

@Composable
private fun MarketsStep(picked: Set<String>, onToggle: (String) -> Unit) {
    val drift = rememberInfiniteTransition(label = "drift")
    val t by drift.animateFloat(0f, 1f, infiniteRepeatable(tween(5000, easing = NvMotion.EaseInOut), RepeatMode.Reverse), label = "t")
    val haptics = rememberHaptics()
    Column(Modifier.fillMaxSize().padding(top = 28.dp)) {
        Text("What should\nwe watch?", style = NvType.Display, modifier = Modifier.reveal(0))
        Spacer(Modifier.height(10.dp))
        Text("Tap the markets you care about.", style = NvType.Body, modifier = Modifier.reveal(1))
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f).padding(vertical = 12.dp)) {
            constellation.forEachIndexed { i, s ->
                val on = s.id in picked
                val scale by animateFloatAsState(if (on) 1f else 0.86f, NvMotion.press(), label = "s")
                val d = s.size.dp
                val bob = (if (i % 2 == 0) t else 1f - t) * 8f
                Box(
                    Modifier
                        .offset(x = maxWidth * s.x - d / 2, y = maxHeight * s.y - d / 2 + bob.dp)
                        .size(d)
                        .reveal(2 + i, step = 80L)
                        .graphicsLayer { scaleX = scale; scaleY = scale }
                        .semantics { selected = on }
                        .tappable(pressedScale = 0.9f, role = SemRole.Checkbox, haptic = false) { haptics.tick(); onToggle(s.id) }
                        .clip(CircleShape)
                        .background(if (on) Brush.linearGradient(Nv.Spectrum) else Brush.linearGradient(listOf(Nv.white(0.08f), Nv.white(0.03f))))
                        .border(1.dp, if (on) Nv.white(0.0f) else Nv.white(0.14f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        s.label,
                        style = NvType.Callout.copy(fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, fontSize = (12 + s.size / 40).sp, lineHeight = (15 + s.size / 40).sp),
                        color = if (on) Nv.OnLight else Nv.Text,
                    )
                }
            }
        }
    }
}

@Composable
private fun TuningStep(onFinished: () -> Unit) {
    val lines = listOf("Reading 1,400 sources", "Scoring your markets", "Writing your first brief", "Ready.")
    var i by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        lines.indices.forEach { i = it; delay(900) }
        onFinished()
    }
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Orb(Modifier.fillMaxWidth(0.85f).aspectRatio(1f), mood = OrbMood.Thinking, energy = 1f)
        Spacer(Modifier.height(12.dp))
        AnimatedContent(i, label = "line", transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(250)) }) {
            Text(lines[it], style = NvType.VerdictS, textAlign = TextAlign.Center)
        }
    }
}
