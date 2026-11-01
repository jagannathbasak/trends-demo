package com.nvile.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nvile.app.data.Evidence
import com.nvile.app.ui.NvileViewModel
import com.nvile.app.ui.components.CountUp
import com.nvile.app.ui.components.Eyebrow
import com.nvile.app.ui.components.Gap
import com.nvile.app.ui.components.GlassCard
import com.nvile.app.ui.components.Meter
import com.nvile.app.ui.components.NumberBadge
import com.nvile.app.ui.components.NvIcons
import com.nvile.app.ui.components.NvScreen
import com.nvile.app.ui.components.SecondaryButton
import com.nvile.app.ui.components.ToggleActionButton
import com.nvile.app.ui.components.evidenceColor
import com.nvile.app.ui.components.rememberHaptics
import com.nvile.app.ui.components.reveal
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvType

@Composable
fun DecisionScreen(vm: NvileViewModel, marketId: String, onBack: () -> Unit) {
    val m = vm.repo.market(marketId)
    val d = m.decision
    val context = LocalContext.current
    val haptics = rememberHaptics()
    val tracking = m.id in vm.trackedDecisions

    NvScreen(title = "Decision Center", eyebrow = "${m.name.uppercase()} · ${m.scope}", onBack = onBack) {
        Text("One market. Four questions. One call.", style = NvType.Lead, modifier = Modifier.reveal(0))
        Gap(20)

        val qs = listOf(
            Triple("WHAT IS HAPPENING", Evidence.VERIFIED, d.happening),
            Triple("WHY IT IS HAPPENING", Evidence.SIGNAL, d.why),
            Triple("WHAT IS LIKELY NEXT", Evidence.PREDICTION, d.next),
        )
        GlassCard(Modifier.fillMaxWidth().reveal(1)) {
            qs.forEachIndexed { i, (label, ev, body) ->
                if (i > 0) Gap(20)
                Row(verticalAlignment = Alignment.Top) {
                    NumberBadge(i + 1)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Row {
                            Eyebrow(label)
                            Spacer(Modifier.width(6.dp))
                            Eyebrow(ev.glyph, color = evidenceColor(ev))
                        }
                        Gap(6)
                        Text(body, style = NvType.Body.copy(color = Nv.Text))
                    }
                }
            }
        }
        Gap(12)
        GlassCard(Modifier.fillMaxWidth().reveal(2), accent = Nv.Mint) {
            Row(verticalAlignment = Alignment.Top) {
                NumberBadge(4, active = true)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Eyebrow("WHAT YOU SHOULD DO", color = Nv.Mint)
                    Gap(6)
                    Text(d.action, style = NvType.Title2)
                }
            }
        }

        Gap(28)
        Text("Recommendation", style = NvType.Title2, modifier = Modifier.reveal(3))
        Gap(10)
        Text(d.recommendation, style = NvType.Lead.copy(color = Nv.Text), modifier = Modifier.reveal(3))

        Gap(22)
        GlassCard(Modifier.fillMaxWidth().reveal(4)) {
            Eyebrow("PROBABILITY OF SUCCESS ◈")
            Gap(18)
            Outcome("If you act now", d.actNow, Nv.Mint, 300)
            Gap(18)
            Outcome("If you wait two quarters", d.waitTwoQuarters, Nv.white(0.45f), 500)
            Gap(16)
            Text("Waiting lets competition absorb the lead.", style = NvType.Callout)
        }

        Gap(12)
        val risk = m.risks.first()
        GlassCard(Modifier.fillMaxWidth().reveal(5), accent = Nv.Amber) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Eyebrow("WATCH FOR ◦", color = Nv.Amber)
                    Gap(6)
                    Text(risk.title, style = NvType.BodyStrong)
                }
                Text("${risk.probability}%", style = NvType.Number, color = Nv.Amber)
            }
        }

        Gap(24)
        Column(Modifier.reveal(6), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ToggleActionButton(
                on = tracking,
                offText = "Track this decision",
                onText = "Tracking · alerts on",
                onClick = { haptics.confirm(); vm.toggleDecision(m.id) },
            )
            SecondaryButton("Share with team", { shareMarket(context, m) }, icon = NvIcons.Share)
        }
        Gap(16)
        Text(
            "Modelled projection with a band. Not a guarantee. Re-scored as new signals land.",
            style = NvType.Callout.copy(fontSize = 12.sp, color = Nv.Text3),
        )
    }
}

@Composable
private fun Outcome(label: String, value: Int, color: androidx.compose.ui.graphics.Color, delay: Int) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(label, style = NvType.BodyStrong, modifier = Modifier.weight(1f))
        CountUp(value.toFloat(), NvType.Number.copy(fontSize = 28.sp), if (color == Nv.Mint) Nv.Mint else Nv.Text, delayMs = delay, format = { "${it.toInt()}%" })
    }
    Gap(10)
    Meter(value / 100f, color = color, height = 8.dp, delayMs = delay)
}
