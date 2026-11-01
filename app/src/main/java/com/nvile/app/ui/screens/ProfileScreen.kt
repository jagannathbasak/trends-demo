package com.nvile.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nvile.app.ui.NvileViewModel
import com.nvile.app.ui.components.Eyebrow
import com.nvile.app.ui.components.Gap
import com.nvile.app.ui.components.GlassCard
import com.nvile.app.ui.components.Meter
import com.nvile.app.ui.components.NvIcon
import com.nvile.app.ui.components.NvIcons
import com.nvile.app.ui.components.NvScreen
import com.nvile.app.ui.components.SecondaryButton
import com.nvile.app.ui.components.Tag
import com.nvile.app.ui.components.reveal
import com.nvile.app.ui.components.tappable
import com.nvile.app.ui.theme.Display
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvType

@Composable
fun ProfileScreen(vm: NvileViewModel, openTrackRecord: () -> Unit, openAlerts: () -> Unit, onSignOut: () -> Unit) {
    val context = LocalContext.current
    NvScreen(title = "You") {
        // Identity
        Row(Modifier.fillMaxWidth().reveal(0), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Nv.MintBright, Nv.MintDeep))),
                contentAlignment = Alignment.Center,
            ) {
                Text("JB", style = NvType.Headline.copy(fontFamily = Display, fontSize = 22.sp, color = Nv.OnMint))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Jagannath Basak", style = NvType.Title2)
                Gap(3)
                Text("NVILE Inc. · ${vm.role.title}", style = NvType.Callout)
            }
        }

        Gap(26)
        // Plan
        GlassCard(Modifier.fillMaxWidth().reveal(1), accent = Nv.Mint) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Team", style = NvType.Title2, modifier = Modifier.weight(1f))
                Tag("MOST TRACKED", Nv.mint(0.16f), Nv.Mint)
            }
            Gap(10)
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$1,900", style = NvType.BigNumber.copy(fontSize = 40.sp, color = Nv.Text))
                Spacer(Modifier.width(6.dp))
                Text("/month", style = NvType.Callout, modifier = Modifier.padding(bottom = 6.dp))
            }
            Gap(18)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tracked markets", style = NvType.BodyStrong, modifier = Modifier.weight(1f))
                Text("${vm.tracked.size} of 10", style = NvType.Mono.copy(fontSize = 13.sp), color = Nv.Mint)
            }
            Gap(8)
            Meter(vm.tracked.size / 10f)
            Gap(16)
            listOf("Daily re-scoring", "30D–3YR horizons", "Decision Center", "Shared tracking").forEach { f ->
                Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    NvIcon(NvIcons.Check, Nv.Mint, size = 16.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(f, style = NvType.Body.copy(color = Nv.Text))
                }
            }
            Gap(14)
            SecondaryButton("Manage plan", {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://nvile.com")))
            })
        }

        Gap(28)
        Eyebrow("INTEGRATIONS", Modifier.reveal(2))
        Gap(12)
        GlassCard(Modifier.fillMaxWidth().reveal(3), padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 4.dp)) {
            vm.repo.integrations().forEachIndexed { i, integ ->
                if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(Nv.white(0.06f)))
                Row(Modifier.fillMaxWidth().heightIn(min = 68.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(integ.name, style = NvType.BodyStrong)
                        Gap(2)
                        Text(integ.description, style = NvType.Callout)
                    }
                    if (integ.enterpriseOnly) {
                        Tag("ENTERPRISE", Nv.white(0.06f), Nv.Text2)
                    } else {
                        val on = integ.id in vm.connected
                        ConnectPill(on) { vm.toggleIntegration(integ.id) }
                    }
                }
            }
        }

        Gap(28)
        Eyebrow("MORE", Modifier.reveal(4))
        Gap(12)
        GlassCard(Modifier.fillMaxWidth().reveal(5), padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 4.dp)) {
            LinkRow(NvIcons.Target, "Track record", "78% landed in band", openTrackRecord)
            Box(Modifier.fillMaxWidth().height(1.dp).background(Nv.white(0.06f)))
            LinkRow(NvIcons.Bell, "Alerts", if (vm.unreadCount > 0) "${vm.unreadCount} new" else "All caught up", openAlerts)
            Box(Modifier.fillMaxWidth().height(1.dp).background(Nv.white(0.06f)))
            LinkRow(NvIcons.Logout, "Sign out", null, onSignOut)
        }
        Gap(24)
        Text(
            "NVILE · Version 1.0 · Demo data until connected to your workspace",
            style = NvType.Callout.copy(fontSize = 12.sp, color = Nv.Text4),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
private fun ConnectPill(on: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (on) Nv.mint(0.12f) else androidx.compose.ui.graphics.Color.Transparent, label = "bg")
    val bd by animateColorAsState(if (on) Nv.mint(0.45f) else Nv.white(0.18f), label = "bd")
    val fg by animateColorAsState(if (on) Nv.Mint else Nv.Text, label = "fg")
    Row(
        Modifier
            .heightIn(min = 36.dp)
            .tappable(pressedScale = 0.92f, onClick = onClick)
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, bd, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (on) NvIcon(NvIcons.Check, Nv.Mint, size = 14.dp)
        Text(if (on) "Connected" else "Connect", style = NvType.Callout.copy(fontSize = 13.sp), color = fg)
    }
}

@Composable
private fun LinkRow(icon: ImageVector, title: String, detail: String?, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().tappable(pressedScale = 0.98f, onClick = onClick).heightIn(min = 60.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NvIcon(icon, Nv.Text2, size = 20.dp)
        Spacer(Modifier.width(14.dp))
        Text(title, style = NvType.BodyStrong, modifier = Modifier.weight(1f))
        if (detail != null) {
            Text(detail, style = NvType.Callout)
            Spacer(Modifier.width(8.dp))
        }
        NvIcon(NvIcons.Chevron, Nv.Text3, size = 16.dp)
    }
}
