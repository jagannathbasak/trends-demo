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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nvile.app.data.AlertCategory
import com.nvile.app.data.AlertDestination
import com.nvile.app.data.Tone
import com.nvile.app.ui.NvileViewModel
import com.nvile.app.ui.components.Eyebrow
import com.nvile.app.ui.components.Gap
import com.nvile.app.ui.components.GlassCard
import com.nvile.app.ui.components.NvScreen
import com.nvile.app.ui.components.NvSwitch
import com.nvile.app.ui.components.SegmentedControl
import com.nvile.app.ui.components.reveal
import com.nvile.app.ui.components.tappable
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvType

@Composable
fun AlertsScreen(
    vm: NvileViewModel,
    onBack: () -> Unit,
    openMarket: (String) -> Unit,
    openDrivers: (String) -> Unit,
    openTrackRecord: () -> Unit,
) {
    val tabs = listOf<AlertCategory?>(null, AlertCategory.VERDICTS, AlertCategory.RISKS)
    val all = vm.repo.alerts()

    NvScreen(
        title = "Alerts",
        onBack = onBack,
        actions = {
            if (vm.unreadCount > 0) {
                Text(
                    "Mark all read",
                    style = NvType.Callout.copy(color = Nv.Mint),
                    modifier = Modifier.tappable { vm.markAllRead() }.padding(10.dp),
                )
            }
        },
    ) {
        Text("You hear from us only when a verdict moves or a risk triggers.", style = NvType.Lead, modifier = Modifier.reveal(0))
        Gap(20)
        SegmentedControl(
            listOf("All", "Verdicts", "Risks"),
            tabs.indexOf(vm.alertTab),
            { vm.alertTab = tabs[it] },
            Modifier.reveal(1),
            mono = false,
        )
        Gap(18)
        Column {
            all.forEachIndexed { i, a ->
                AnimatedVisibility(
                    visible = vm.alertTab == null || a.category == vm.alertTab,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    val unread = a.id !in vm.readAlerts
                    val dot by animateColorAsState(
                        if (!unread) Nv.white(0.15f) else when (a.tone) {
                            Tone.UP -> Nv.Mint; Tone.CAUTION -> Nv.Amber; Tone.NEUTRAL -> Nv.Text2
                        },
                        label = "dot",
                    )
                    GlassCard(
                        Modifier.fillMaxWidth().padding(bottom = 10.dp).reveal(2 + i),
                        onClick = {
                            vm.markRead(a.id)
                            when (a.destination) {
                                AlertDestination.MARKET -> a.marketId?.let(openMarket)
                                AlertDestination.DRIVERS -> a.marketId?.let(openDrivers)
                                AlertDestination.TRACK_RECORD -> openTrackRecord()
                            }
                        },
                        padding = androidx.compose.foundation.layout.PaddingValues(18.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
                            Spacer(Modifier.width(10.dp))
                            Eyebrow(a.kind, Modifier.weight(1f), color = if (a.tone == Tone.CAUTION) Nv.Amber else if (a.tone == Tone.UP) Nv.Mint else Nv.Text2)
                            Eyebrow(a.whenLabel)
                        }
                        Gap(10)
                        Text(a.marketName, style = NvType.Headline.copy(fontSize = 17.sp, color = if (unread) Nv.Text else Nv.Text2))
                        Gap(6)
                        Text(a.body, style = NvType.Body)
                    }
                }
            }
        }

        Gap(22)
        Text("Notify me when", style = NvType.Title2, modifier = Modifier.reveal(8))
        Gap(12)
        GlassCard(Modifier.fillMaxWidth().reveal(9), padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 4.dp)) {
            listOf("verdict" to "A verdict moves", "risk" to "A risk triggers", "digest" to "Weekly digest").forEachIndexed { i, (key, label) ->
                if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(Nv.white(0.06f)))
                Row(Modifier.fillMaxWidth().heightIn(min = 60.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(label, style = NvType.BodyStrong, modifier = Modifier.weight(1f))
                    NvSwitch(vm.alertPrefs[key] == true, { vm.setAlertPref(key, it) }, label)
                }
            }
        }
    }
}
