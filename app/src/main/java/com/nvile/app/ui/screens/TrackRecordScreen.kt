package com.nvile.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nvile.app.data.Evidence
import com.nvile.app.ui.NvileViewModel
import com.nvile.app.ui.components.BandResult
import com.nvile.app.ui.components.CountUp
import com.nvile.app.ui.components.Eyebrow
import com.nvile.app.ui.components.Gap
import com.nvile.app.ui.components.GlassCard
import com.nvile.app.ui.components.NvScreen
import com.nvile.app.ui.components.evidenceColor
import com.nvile.app.ui.components.pct
import com.nvile.app.ui.components.reveal
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvType
import java.util.Locale

@Composable
fun TrackRecordScreen(vm: NvileViewModel, onBack: () -> Unit) {
    val tr = vm.repo.trackRecord()

    NvScreen(title = "Track record", eyebrow = "PUBLIC SCORING", onBack = onBack) {
        Text("Every forecast is resolved and published, including the misses.", style = NvType.Lead, modifier = Modifier.reveal(0))
        Gap(24)
        GlassCard(Modifier.fillMaxWidth().reveal(1), accent = Nv.Mint) {
            CountUp(tr.landedInBand.toFloat(), NvType.BigNumber.copy(fontSize = 72.sp, lineHeight = 72.sp), Nv.Mint, durationMs = 1800, format = { "${it.toInt()}%" })
            Gap(8)
            Text("of 12-month forecasts landed inside the stated band.", style = NvType.Lead.copy(color = Nv.Text))
        }
        Gap(12)
        GlassCard(Modifier.fillMaxWidth().reveal(2), padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 4.dp)) {
            StatRow("Predictions resolved and published", tr.resolved.toFloat(), { "${it.toInt()}" }, first = true)
            StatRow("Median lead time over mainstream coverage", tr.leadTimeMonths.toFloat(), { String.format(Locale.US, "%.1f mo", it) })
            StatRow("Forecasts withdrawn when a risk triggered", tr.withdrawn.toFloat(), { "${it.toInt()}" })
        }

        Gap(28)
        Text("Latest resolved call", style = NvType.Title2, modifier = Modifier.reveal(3))
        Gap(12)
        GlassCard(Modifier.fillMaxWidth().reveal(4)) {
            Eyebrow("RESOLVED CALL ●", color = Nv.Mint)
            Gap(8)
            Text(tr.latest.claim, style = NvType.Headline)
            Gap(16)
            BandResult(tr.latest.bandLow, tr.latest.bandHigh, tr.latest.actual)
            Gap(10)
            Row(Modifier.fillMaxWidth()) {
                Eyebrow("BAND ${pct(tr.latest.bandLow)} → ${pct(tr.latest.bandHigh)}", Modifier.weight(1f))
                Eyebrow("ACTUAL ${pct(tr.latest.actual)}", color = Nv.Mint)
            }
        }

        Gap(28)
        Text("How every number is labelled", style = NvType.Title2, modifier = Modifier.reveal(5))
        Gap(12)
        GlassCard(Modifier.fillMaxWidth().reveal(6)) {
            Evidence.entries.forEachIndexed { i, e ->
                if (i > 0) Gap(18)
                Row(verticalAlignment = Alignment.Top) {
                    Text(e.glyph, style = NvType.Number.copy(fontSize = 20.sp), color = evidenceColor(e), modifier = Modifier.width(30.dp))
                    Column(Modifier.weight(1f)) {
                        Text(e.label, style = NvType.BodyStrong)
                        Gap(3)
                        Text(e.description, style = NvType.Callout)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: Float, format: (Float) -> String, first: Boolean = false) {
    if (!first) Box(Modifier.fillMaxWidth().height(1.dp).background(Nv.white(0.06f)))
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = NvType.Body.copy(color = Nv.Text), modifier = Modifier.weight(1f))
        Spacer(Modifier.width(16.dp))
        CountUp(value, NvType.Number.copy(fontSize = 22.sp), Nv.Text, delayMs = 300, format = format)
    }
}
