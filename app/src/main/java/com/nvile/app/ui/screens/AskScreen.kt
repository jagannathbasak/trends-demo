package com.nvile.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nvile.app.ui.NvileViewModel
import com.nvile.app.ui.components.Eyebrow
import com.nvile.app.ui.components.Gap
import com.nvile.app.ui.components.GlassCard
import com.nvile.app.ui.components.NvIcon
import com.nvile.app.ui.components.NvIcons
import com.nvile.app.ui.components.NvScreen
import com.nvile.app.ui.components.SegmentedControl
import com.nvile.app.ui.components.reveal
import com.nvile.app.ui.components.tappable
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvType

@Composable
fun AskScreen(vm: NvileViewModel, onAsk: () -> Unit) {
    val focus = LocalFocusManager.current
    val canAsk = vm.question.isNotBlank()
    fun submit() {
        if (vm.question.isBlank()) return
        focus.clearFocus()
        onAsk()
    }

    NvScreen(title = "What market decision are you weighing?", eyebrow = "ASK NVILE") {
        Text("Every figure comes back with its evidence tier.", style = NvType.Lead, modifier = Modifier.reveal(0))
        Gap(22)

        GlassCard(Modifier.fillMaxWidth().reveal(1), padding = androidx.compose.foundation.layout.PaddingValues(18.dp)) {
            Eyebrow("YOUR QUESTION")
            Gap(10)
            Box(Modifier.fillMaxWidth().heightIn(min = 84.dp)) {
                if (vm.question.isEmpty()) {
                    Text("When should I enter AI workflow platforms in DACH mid-market?", style = NvType.Lead, color = Nv.Text4)
                }
                BasicTextField(
                    value = vm.question,
                    onValueChange = { vm.question = it },
                    textStyle = NvType.Lead.copy(color = Nv.Text),
                    cursorBrush = SolidColor(Nv.Mint),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { submit() }),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Gap(14)
            Row(verticalAlignment = Alignment.CenterVertically) {
                SegmentedControl(
                    listOf("Standard", "Deep"),
                    if (vm.deepAnswer) 1 else 0,
                    { vm.deepAnswer = it == 1 },
                    Modifier.weight(1f),
                    mono = false,
                    height = 36.dp,
                )
                Spacer(Modifier.width(12.dp))
                val bg by animateColorAsState(if (canAsk) Nv.Mint else Nv.white(0.08f), label = "send")
                Box(
                    Modifier
                        .size(48.dp)
                        .semantics { contentDescription = "Ask" }
                        .tappable(enabled = canAsk, pressedScale = 0.88f) { submit() }
                        .clip(CircleShape)
                        .background(bg),
                    contentAlignment = Alignment.Center,
                ) {
                    NvIcon(NvIcons.Send, if (canAsk) Nv.OnMint else Nv.Text3, size = 20.dp)
                }
            }
        }

        Gap(30)
        Eyebrow("TRY ASKING", Modifier.reveal(2))
        Gap(12)
        Column {
            vm.repo.suggestions().forEachIndexed { i, s ->
                val shape = RoundedCornerShape(18.dp)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .reveal(3 + i)
                        .padding(bottom = 10.dp)
                        .tappable(pressedScale = 0.97f) { vm.question = s; submit() }
                        .clip(shape)
                        .background(Brush.horizontalGradient(listOf(Nv.white(0.05f), Nv.white(0.02f))))
                        .border(1.dp, Nv.Hairline, shape)
                        .heightIn(min = 58.dp)
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NvIcon(NvIcons.Sparkle, Nv.mint(0.8f), size = 16.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(s, style = NvType.BodyStrong.copy(fontSize = 15.sp), modifier = Modifier.weight(1f))
                    NvIcon(NvIcons.Arrow, Nv.Text3, size = 16.dp)
                }
            }
        }
    }
}
