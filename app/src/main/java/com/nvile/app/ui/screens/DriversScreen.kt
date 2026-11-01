package com.nvile.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nvile.app.data.Evidence
import com.nvile.app.ui.NvileViewModel
import com.nvile.app.ui.components.EvidenceBar
import com.nvile.app.ui.components.Eyebrow
import com.nvile.app.ui.components.Gap
import com.nvile.app.ui.components.GlassCard
import com.nvile.app.ui.components.Meter
import com.nvile.app.ui.components.NvIcons
import com.nvile.app.ui.components.NvScreen
import com.nvile.app.ui.components.SecondaryButton
import com.nvile.app.ui.components.evidenceColor
import com.nvile.app.ui.components.reveal
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvType
import java.util.Locale

@Composable
fun DriversScreen(vm: NvileViewModel, marketId: String, onBack: () -> Unit) {
    val m = vm.repo.market(marketId)
    var trail by rememberSaveable { mutableStateOf(false) }

    NvScreen(title = "Why it is moving", eyebrow = m.name.uppercase(), onBack = onBack) {
        Text("Ranked catalysts, each cited, with its weight in the forecast.", style = NvType.Lead, modifier = Modifier.reveal(0))
        Gap(20)

        GlassCard(Modifier.fillMaxWidth().reveal(1)) {
            m.catalysts.forEachIndexed { i, c ->
                if (i > 0) Gap(22)
                Row(verticalAlignment = Alignment.Top) {
                    Text(c.evidence.glyph, style = NvType.BodyStrong, color = evidenceColor(c.evidence))
                    Spacer(Modifier.width(10.dp))
                    Text(c.title, style = NvType.BodyStrong, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(12.dp))
                    Text(String.format(Locale.US, "%.2f", c.weight), style = NvType.Number.copy(fontSize = 16.sp), color = if (i == 0) Nv.Mint else Nv.Text)
                }
                Gap(10)
                Meter(c.weight.toFloat(), color = Nv.Mint.copy(alpha = 1f - i * 0.28f), delayMs = 250 + i * 150)
                Gap(8)
                Eyebrow("${c.evidence.label.uppercase()} · ${c.source.uppercase()}")
            }
        }

        Gap(30)
        Text("What could kill this", style = NvType.Title2, modifier = Modifier.reveal(2))
        Gap(12)
        GlassCard(Modifier.fillMaxWidth().reveal(3), accent = Nv.Amber) {
            m.risks.forEachIndexed { i, r ->
                if (i > 0) Gap(18)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(r.title, style = NvType.BodyStrong, modifier = Modifier.weight(1f))
                    Text("${r.probability}%", style = NvType.Mono.copy(fontSize = 14.sp), color = Nv.Amber)
                }
                Gap(10)
                Meter(r.probability / 100f, color = Nv.Amber, height = 4.dp, delayMs = 300 + i * 120)
            }
            Gap(18)
            Box(Modifier.fillMaxWidth().height(1.dp).background(Nv.white(0.07f)))
            Gap(14)
            Text("If any risk triggers, the verdict is recalculated and you are notified.", style = NvType.Callout, color = Nv.AmberSoft)
        }

        Gap(16)
        GlassCard(Modifier.fillMaxWidth().reveal(4)) {
            Eyebrow("EVIDENCE MIX")
            Gap(14)
            EvidenceBar(m.evidenceMix)
            Gap(16)
            Evidence.entries.chunked(2).forEach { pair ->
                Row(Modifier.fillMaxWidth()) {
                    pair.forEach { e ->
                        Text(
                            "${e.glyph} ${e.label.uppercase()} ${m.evidenceMix[e] ?: 0}%",
                            style = NvType.MonoTag.copy(fontSize = 11.sp),
                            color = if (e == Evidence.VERIFIED || e == Evidence.PREDICTION) Nv.Mint else Nv.Text2,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Gap(8)
            }
        }

        Gap(16)
        SecondaryButton(if (trail) "Hide evidence trail" else "Open full evidence trail", { trail = !trail }, Modifier.reveal(5), icon = NvIcons.Shield)
        AnimatedVisibility(trail, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Column(Modifier.fillMaxWidth()) {
                Gap(14)
                GlassCard(Modifier.fillMaxWidth()) {
                    Eyebrow("EVIDENCE TRAIL · ${m.sources} SOURCES")
                    Gap(12)
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        m.catalysts.forEach { c ->
                            Row {
                                Text(c.evidence.glyph, style = NvType.Mono, color = evidenceColor(c.evidence))
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(c.source, style = NvType.BodyStrong.copy(fontSize = 14.sp))
                                    Text("Supports: ${c.title}", style = NvType.Callout)
                                }
                            }
                        }
                    }
                    Gap(14)
                    Text(
                        "Each claim stores its full chain from collection to publication. A claim that cannot produce its chain is not published.",
                        style = NvType.Callout,
                    )
                }
            }
        }
    }
}
