package com.nvile.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.nvile.app.R
import com.nvile.app.ui.NvileViewModel
import com.nvile.app.ui.components.Bar
import com.nvile.app.ui.components.Gap
import com.nvile.app.ui.components.Group
import com.nvile.app.ui.components.GroupRow
import com.nvile.app.ui.components.Mood
import com.nvile.app.ui.components.NvIcons
import com.nvile.app.ui.components.NvSwitch
import com.nvile.app.ui.components.Page
import com.nvile.app.ui.components.reveal
import com.nvile.app.ui.theme.GeistMono
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvMotion
import com.nvile.app.ui.theme.NvType
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(vm: NvileViewModel, openTrackRecord: () -> Unit, openAlerts: () -> Unit, onSignOut: () -> Unit) {
    val context = LocalContext.current
    Page(title = "You", mood = Mood.Night) {
        MembershipCard(vm.role.title, Modifier.reveal(0))
        Gap(28)
        Group(Modifier.reveal(1), header = "Plan") {
            Column(Modifier.padding(18.dp)) {
                Row {
                    Text("Watched markets", style = NvType.BodyStrong.copy(fontWeight = FontWeight.Normal), modifier = Modifier.weight(1f))
                    Text("${vm.tracked.size} of 10", style = NvType.Data.copy(color = Nv.Text))
                }
                Gap(10)
                Bar(vm.tracked.size / 10f, thickness = 6.dp)
            }
            GroupRow("Daily re-scoring, 30D to 3YR horizons", showDivider = true, trailing = {})
            GroupRow("Decision Center and shared watching", showDivider = true, trailing = {})
            GroupRow("Manage plan", showDivider = true, onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://nvile.com"))) })
        }
        Gap(28)
        Group(Modifier.reveal(2), header = "Integrations") {
            vm.repo.integrations().forEachIndexed { i, integ ->
                GroupRow(
                    integ.name, subtitle = integ.description, showDivider = i > 0,
                    trailing = {
                        if (integ.enterpriseOnly) Text("Enterprise", style = NvType.Caption)
                        else NvSwitch(integ.id in vm.connected, { vm.toggleIntegration(integ.id) }, "Connect ${integ.name}")
                    },
                )
            }
        }
        Gap(28)
        Group(Modifier.reveal(3)) {
            GroupRow("Track record", subtitle = "78% landed in band", icon = NvIcons.Target, iconTint = Nv.Mint, onClick = openTrackRecord)
            GroupRow("Alerts", subtitle = if (vm.unreadCount > 0) "${vm.unreadCount} new" else "All caught up", icon = NvIcons.Bell, iconTint = Nv.Amber, showDivider = true, onClick = openAlerts)
        }
        Gap(28)
        Group(Modifier.reveal(4)) {
            GroupRow("Sign out", icon = NvIcons.Logout, iconTint = Nv.Coral, onClick = onSignOut)
        }
        Gap(18)
        Text("NVILE 1.0 · Showing demo data until your workspace is connected.", style = NvType.Caption, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

/** A metal-and-glass membership card that tilts under your finger. */
@Composable
private fun MembershipCard(role: String, modifier: Modifier) {
    val rx = remember { Animatable(0f) }
    val ry = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier
            .fillMaxWidth()
            .aspectRatio(1.586f)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { scope.launch { rx.animateTo(0f, NvMotion.press()) }; scope.launch { ry.animateTo(0f, NvMotion.press()) } },
                    onDragCancel = { scope.launch { rx.animateTo(0f, NvMotion.press()) }; scope.launch { ry.animateTo(0f, NvMotion.press()) } },
                ) { _, drag ->
                    scope.launch { ry.snapTo((ry.value + drag.x / 12f).coerceIn(-18f, 18f)) }
                    scope.launch { rx.snapTo((rx.value - drag.y / 12f).coerceIn(-18f, 18f)) }
                }
            }
            .graphicsLayer { rotationX = rx.value; rotationY = ry.value; cameraDistance = 14f * density }
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color(0xFF0D2A26), Color(0xFF0A0A12), Color(0xFF1A1440))))
            .border(0.5.dp, Brush.linearGradient(listOf(Nv.white(0.4f), Nv.white(0.05f), Nv.white(0.25f))), shape),
    ) {
        // Light sheen that slides with the tilt.
        Canvas(Modifier.fillMaxSize()) {
            val x = size.width * (0.5f + ry.value / 40f)
            drawRect(Brush.linearGradient(listOf(Color.Transparent, Nv.white(0.12f), Color.Transparent), start = Offset(x - size.width * 0.4f, 0f), end = Offset(x + size.width * 0.4f, size.height)))
            drawCircle(Brush.radialGradient(listOf(Nv.mint(0.3f), Color.Transparent), Offset(size.width, 0f), size.width * 0.7f), size.width * 0.7f, Offset(size.width, 0f))
        }
        Column(Modifier.fillMaxSize().padding(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.nvile_mark), null, Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("NVILE", style = NvType.Headline.copy(letterSpacing = 0.3.em, fontSize = 13.sp))
                Spacer(Modifier.weight(1f))
                Text("TEAM", style = NvType.Label.copy(color = Nv.Mint))
            }
            Spacer(Modifier.weight(1f))
            Text("Jagannath Basak", style = NvType.Title.copy(fontWeight = FontWeight.Medium))
            Text("$role · NVILE Inc.", style = NvType.Callout)
            Gap(14)
            Row {
                Text("$1,900 / month", style = NvType.Data.copy(fontFamily = GeistMono, color = Nv.Text), modifier = Modifier.weight(1f))
                Text("MEMBER SINCE 2026", style = NvType.Label)
            }
        }
    }
}
