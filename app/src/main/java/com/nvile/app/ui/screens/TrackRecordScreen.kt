package com.nvile.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nvile.app.data.Evidence
import com.nvile.app.ui.NvileViewModel
import com.nvile.app.ui.components.BandResult
import com.nvile.app.ui.components.CountUp
import com.nvile.app.ui.components.DotMatrix
import com.nvile.app.ui.components.Gap
import com.nvile.app.ui.components.Group
import com.nvile.app.ui.components.GroupRow
import com.nvile.app.ui.components.Label
import com.nvile.app.ui.components.Mood
import com.nvile.app.ui.components.Page
import com.nvile.app.ui.components.Panel
import com.nvile.app.ui.components.SectionTitle
import com.nvile.app.ui.components.pct
import com.nvile.app.ui.components.reveal
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvType
import java.util.Locale

@OptIn(ExperimentalTextApi::class)
@Composable
fun TrackRecordScreen(vm: NvileViewModel, onBack: () -> Unit) {
    val tr = vm.repo.trackRecord()
    Page(title = "Track record", kicker = "Every call is resolved and published", mood = Mood.Rising, onBack = onBack) {
        CountUp(
            tr.landedInBand.toFloat(),
            NvType.Numeral.copy(fontSize = 132.sp, lineHeight = 128.sp, brush = Brush.linearGradient(Nv.Spectrum)),
            Modifier.reveal(0),
            durationMs = 2000,
            format = { "${it.toInt()}%" },
        )
        Text("of 12-month forecasts landed inside the band NVILE stated in advance. Misses included.", style = NvType.Body.copy(fontSize = 18.sp, lineHeight = 26.sp, color = Nv.Text), modifier = Modifier.reveal(1))
        Gap(26)
        DotMatrix(tr.landedInBand, Modifier.reveal(2))
        Gap(30)
        Group(Modifier.reveal(3)) {
            GroupRow("Calls resolved and published", showDivider = false, trailing = { Text("${tr.resolved}", style = NvType.NumeralS.copy(fontSize = 24.sp)) })
            GroupRow("Lead over mainstream coverage", showDivider = true, trailing = { Text(String.format(Locale.US, "%.1f mo", tr.leadTimeMonths), style = NvType.NumeralS.copy(fontSize = 24.sp)) })
            GroupRow("Withdrawn when a risk triggered", showDivider = true, trailing = { Text("${tr.withdrawn}", style = NvType.NumeralS.copy(fontSize = 24.sp)) })
        }

        Gap(40)
        SectionTitle("Latest resolved call")
        Panel(Modifier.fillMaxWidth().reveal(4)) {
            Column(Modifier.padding(22.dp)) {
                Label("Resolved ●", color = Nv.Mint)
                Gap(10)
                Text(tr.latest.claim, style = NvType.VerdictS)
                Gap(18)
                BandResult(tr.latest.bandLow, tr.latest.bandHigh, tr.latest.actual)
                Gap(8)
                Row(Modifier.fillMaxWidth()) {
                    Text("Band ${pct(tr.latest.bandLow)} to ${pct(tr.latest.bandHigh)}", style = NvType.Caption, modifier = Modifier.weight(1f))
                    Text("Actual ${pct(tr.latest.actual)}", style = NvType.Caption.copy(color = Nv.Text))
                }
            }
        }

        Gap(40)
        SectionTitle("How every number is labelled")
        Group(Modifier.reveal(5)) {
            Evidence.entries.forEachIndexed { i, e ->
                GroupRow(e.label, subtitle = e.description, showDivider = i > 0, trailing = {
                    Text(e.glyph, style = NvType.Title.copy(color = Nv.Mint), modifier = Modifier.width(24.dp))
                })
            }
        }
    }
}
