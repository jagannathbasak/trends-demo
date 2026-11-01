package com.nvile.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nvile.app.data.Alert
import com.nvile.app.data.AlertCategory
import com.nvile.app.data.AlertDestination
import com.nvile.app.data.Tone
import com.nvile.app.ui.NvileViewModel
import com.nvile.app.ui.components.Gap
import com.nvile.app.ui.components.Group
import com.nvile.app.ui.components.GroupRow
import com.nvile.app.ui.components.Hairline
import com.nvile.app.ui.components.Mood
import com.nvile.app.ui.components.NvSwitch
import com.nvile.app.ui.components.Page
import com.nvile.app.ui.components.Tabs
import com.nvile.app.ui.components.reveal
import com.nvile.app.ui.components.tappable
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvType

@Composable
fun AlertsScreen(
    vm: NvileViewModel,
    onBack: () -> Unit,
    openMarket: (String) -> Unit,
    openTrackRecord: () -> Unit,
) {
    val tabs = listOf<AlertCategory?>(null, AlertCategory.VERDICTS, AlertCategory.RISKS)
    val all = vm.repo.alerts()

    Page(
        title = "Alerts",
        kicker = "Only when a call moves or a risk triggers",
        mood = Mood.Night,
        onBack = onBack,
        actions = {
            if (vm.unreadCount > 0) Text("Read all", style = NvType.Callout.copy(color = Nv.Mint), modifier = Modifier.tappable { vm.markAllRead() }.padding(8.dp))
        },
    ) {
        Tabs(listOf("All", "Verdicts", "Risks"), tabs.indexOf(vm.alertTab), { vm.alertTab = tabs[it] }, Modifier.reveal(0))
        Gap(22)
        listOf("Today" to all.filter { it.whenLabel == "TODAY" }, "Earlier this week" to all.filter { it.whenLabel != "TODAY" }).forEachIndexed { g, (title, items) ->
            val shown = items.filter { vm.alertTab == null || it.category == vm.alertTab }
            if (shown.isNotEmpty()) {
                Text(title, style = NvType.Headline, modifier = Modifier.reveal(1 + g).padding(bottom = 6.dp))
                Column(Modifier.reveal(1 + g)) {
                    items.forEachIndexed { i, a ->
                        AnimatedVisibility(vm.alertTab == null || a.category == vm.alertTab, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                            Column {
                                if (i > 0) Hairline(Modifier.padding(start = 56.dp))
                                AlertRow(a, a.id !in vm.readAlerts) {
                                    vm.markRead(a.id)
                                    when (a.destination) {
                                        AlertDestination.MARKET, AlertDestination.DRIVERS -> a.marketId?.let(openMarket)
                                        AlertDestination.TRACK_RECORD -> openTrackRecord()
                                    }
                                }
                            }
                        }
                    }
                }
                Gap(26)
            }
        }
        Group(Modifier.reveal(4), header = "Notify me when", footer = "Alerts also reach Slack when it is connected.") {
            listOf("verdict" to "A call moves", "risk" to "A risk triggers", "digest" to "Weekly digest").forEachIndexed { i, (key, label) ->
                GroupRow(label, showDivider = i > 0, trailing = { NvSwitch(vm.alertPrefs[key] == true, { vm.setAlertPref(key, it) }, label) })
            }
        }
    }
}

@Composable
private fun AlertRow(a: Alert, unread: Boolean, onClick: () -> Unit) {
    val colors = when (a.tone) { Tone.CAUTION -> Nv.Caution; Tone.UP -> Nv.Spectrum; Tone.NEUTRAL -> listOf(Nv.Cyan, Nv.Indigo) }
    val title by animateColorAsState(if (unread) Nv.Text else Nv.Text2, label = "t")
    Row(Modifier.fillMaxWidth().tappable(pressedScale = 0.98f, onClick = onClick).padding(vertical = 14.dp), verticalAlignment = Alignment.Top) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(Brush.linearGradient(colors)), contentAlignment = Alignment.Center) {
            Text(a.kind.trim().last().toString().takeIf { it in "◈◦●" } ?: "!", style = NvType.BodyStrong.copy(color = Nv.OnLight, fontSize = 15.sp))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(a.marketName, style = NvType.Headline.copy(color = title), modifier = Modifier.weight(1f))
                if (unread) Box(Modifier.padding(start = 8.dp).size(8.dp).clip(CircleShape).background(Nv.Mint))
            }
            Text(a.kind.trimEnd('◈', '◦', '●', ' ').lowercase().replaceFirstChar { it.uppercase() } + " · " + a.whenLabel.lowercase().replaceFirstChar { it.uppercase() }, style = NvType.Caption)
            Spacer(Modifier.size(6.dp))
            Text(a.body, style = NvType.Callout)
        }
    }
}
