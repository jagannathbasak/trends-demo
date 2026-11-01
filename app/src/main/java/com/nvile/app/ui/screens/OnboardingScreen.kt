package com.nvile.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.semantics.Role as SemRole
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nvile.app.data.Role
import com.nvile.app.ui.NvileViewModel
import com.nvile.app.ui.components.Eyebrow
import com.nvile.app.ui.components.NvIcon
import com.nvile.app.ui.components.NvIcons
import com.nvile.app.ui.components.PrimaryButton
import com.nvile.app.ui.components.SelectChip
import com.nvile.app.ui.components.StageTag
import com.nvile.app.ui.components.reveal
import com.nvile.app.ui.components.tappable
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvMotion
import com.nvile.app.ui.theme.NvType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(vm: NvileViewModel, onDone: (Role, Set<String>) -> Unit, onBack: () -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var role by rememberSaveable { mutableStateOf(vm.role) }
    var picked by rememberSaveable { mutableStateOf(setOf("ai-workflow", "energy-storage")) }
    val markets = vm.repo.markets()

    BackHandler { if (step == 1) step = 0 else onBack() }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        // Progress + skip
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(2) { i ->
                    val w by animateDpAsState(if (i == step) 28.dp else 10.dp, NvMotion.settle(), label = "dot")
                    val c by animateColorAsState(if (i <= step) Nv.Mint else Nv.white(0.15f), label = "dotc")
                    Box(Modifier.width(w).height(6.dp).clip(CircleShape).background(c))
                }
            }
            Text(
                "Skip",
                style = NvType.BodyStrong,
                color = Nv.Text2,
                modifier = Modifier.tappable { onDone(role, picked) }.padding(12.dp),
            )
        }
        Spacer(Modifier.height(24.dp))

        AnimatedContent(
            targetState = step,
            modifier = Modifier.weight(1f),
            label = "step",
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally(tween(520, easing = NvMotion.Ease)) { it * dir / 3 } + fadeIn(tween(360))) togetherWith
                    (slideOutHorizontally(tween(420, easing = NvMotion.Ease)) { -it * dir / 4 } + fadeOut(tween(200)))
            },
        ) { s ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Eyebrow("STEP ${s + 1} OF 2", modifier = Modifier.reveal(0))
                Spacer(Modifier.height(10.dp))
                if (s == 0) {
                    Text("What decisions do you make?", style = NvType.Title.copy(fontSize = NvType.LargeTitle.fontSize), modifier = Modifier.reveal(1))
                    Spacer(Modifier.height(10.dp))
                    Text("We shape your daily brief and verdicts around it.", style = NvType.Lead, modifier = Modifier.reveal(2))
                    Spacer(Modifier.height(28.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Role.entries.forEachIndexed { i, r ->
                            RoleCard(r, r == role, Modifier.reveal(3 + i)) { role = r }
                        }
                    }
                } else {
                    Text("Start tracking", style = NvType.Title.copy(fontSize = NvType.LargeTitle.fontSize), modifier = Modifier.reveal(1))
                    Spacer(Modifier.height(10.dp))
                    Text("Pick up to 10 markets. NVILE re-scores each one daily and tells you when a verdict moves.", style = NvType.Lead, modifier = Modifier.reveal(2))
                    Spacer(Modifier.height(28.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        markets.forEachIndexed { i, m ->
                            val on = m.id in picked
                            MarketPickRow(m.name, m.stage, on, Modifier.reveal(3 + i)) {
                                picked = if (on) picked - m.id else if (picked.size < 10) picked + m.id else picked
                            }
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Eyebrow("POPULAR TOPICS", modifier = Modifier.reveal(8))
                    Spacer(Modifier.height(10.dp))
                    var extra by rememberSaveable { mutableStateOf(setOf<String>()) }
                    FlowRow(
                        Modifier.reveal(9),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf("Robotics", "Fintech infra", "GLP-1 economy", "Space logistics", "Cybersecurity").forEach { t ->
                            SelectChip(t, t in extra, onClick = { extra = if (t in extra) extra - t else extra + t })
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        PrimaryButton(
            text = if (step == 0) "Continue" else "Start my daily brief",
            onClick = { if (step == 0) step = 1 else onDone(role, picked) },
            icon = if (step == 0) null else NvIcons.Sparkle,
        )
    }
}

@Composable
private fun RoleCard(role: Role, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bd by animateColorAsState(if (selected) Nv.mint(0.6f) else Nv.white(0.1f), label = "bd")
    val bg by animateColorAsState(if (selected) Nv.mint(0.08f) else Nv.white(0.03f), label = "bg")
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier
            .fillMaxWidth()
            .semantics { this.selected = selected }
            .tappable(pressedScale = 0.97f, role = SemRole.RadioButton, onClick = onClick)
            .clip(shape)
            .background(bg)
            .border(1.dp, bd, shape)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(role.title, style = NvType.BodyStrong.copy(fontSize = NvType.Lead.fontSize))
            Spacer(Modifier.height(3.dp))
            Text(role.use, style = NvType.Callout)
        }
        val ring by animateColorAsState(if (selected) Nv.Mint else Nv.white(0.25f), label = "ring")
        val dot by animateDpAsState(if (selected) 10.dp else 0.dp, NvMotion.press(), label = "radio")
        Box(Modifier.size(22.dp).clip(CircleShape).border(2.dp, ring, CircleShape), contentAlignment = Alignment.Center) {
            Box(Modifier.size(dot).clip(CircleShape).background(Nv.Mint))
        }
    }
}

@Composable
private fun MarketPickRow(name: String, stage: com.nvile.app.data.Stage, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bd by animateColorAsState(if (selected) Nv.mint(0.55f) else Nv.white(0.1f), label = "bd")
    val bg by animateColorAsState(if (selected) Nv.mint(0.07f) else Nv.white(0.03f), label = "bg")
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier
            .fillMaxWidth()
            .semantics { this.selected = selected }
            .tappable(pressedScale = 0.97f, role = SemRole.Checkbox, onClick = onClick)
            .clip(shape)
            .background(bg)
            .border(1.dp, bd, shape)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(name, style = NvType.BodyStrong)
            Spacer(Modifier.height(6.dp))
            StageTag(stage)
        }
        val check by animateColorAsState(if (selected) Nv.Mint else Color0, label = "check")
        Box(
            Modifier.size(26.dp).clip(CircleShape).background(check).border(1.5.dp, if (selected) Nv.Mint else Nv.white(0.25f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) NvIcon(NvIcons.Check, Nv.OnMint, size = 15.dp)
        }
    }
}

private val Color0 = androidx.compose.ui.graphics.Color.Transparent
