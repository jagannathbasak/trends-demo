package com.nvile.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.nvile.app.R
import com.nvile.app.data.Evidence
import com.nvile.app.ui.components.CountUp
import com.nvile.app.ui.components.Eyebrow
import com.nvile.app.ui.components.GlassCard
import com.nvile.app.ui.components.HeroChart
import com.nvile.app.ui.components.LivePill
import com.nvile.app.ui.components.PrimaryButton
import com.nvile.app.ui.components.SecondaryButton
import com.nvile.app.ui.components.evidenceColor
import com.nvile.app.ui.components.reveal
import com.nvile.app.ui.theme.Display
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvMotion
import com.nvile.app.ui.theme.NvType
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WelcomeScreen(onStart: () -> Unit, onSignIn: () -> Unit) {
    val float = rememberInfiniteTransition(label = "float")
    val bob by float.animateFloat(-4f, 4f, infiniteRepeatable(tween(3200, easing = NvMotion.EaseInOut), RepeatMode.Reverse), label = "bob")
    val breathe by float.animateFloat(0.5f, 1f, infiniteRepeatable(tween(2600, easing = NvMotion.EaseInOut), RepeatMode.Reverse), label = "breathe")

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        // Brand row
        Row(Modifier.fillMaxWidth().reveal(0), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(34.dp)
                    .graphicsLayer { translationY = bob.dp.toPx() * 0.4f }
                    .drawBehind {
                        drawCircle(
                            Brush.radialGradient(listOf(Nv.mint(0.45f * breathe), Color.Transparent), radius = size.width * 1.3f),
                            radius = size.width * 1.3f,
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Image(painterResource(R.drawable.nvile_mark), contentDescription = null, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(
                "NVILE",
                style = NvType.Headline.copy(fontFamily = Display, fontWeight = FontWeight.Bold, letterSpacing = 0.22.em, fontSize = 17.sp),
                modifier = Modifier.weight(1f),
            )
            LivePill("LIVE · RESCORED DAILY")
        }

        Spacer(Modifier.height(36.dp))

        // Hero forecast card
        GlassCard(
            Modifier
                .fillMaxWidth()
                .reveal(1)
                .graphicsLayer { translationY = bob.dp.toPx() },
            padding = androidx.compose.foundation.layout.PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 14.dp),
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Eyebrow("AI WORKFLOW PLATFORMS")
                    Spacer(Modifier.height(4.dp))
                    Eyebrow("12-MONTH FORECAST  ◈", color = Nv.mint(0.85f))
                }
                CountUp(68f, NvType.Number.copy(fontSize = 30.sp), Nv.Mint, delayMs = 900, format = { "+${it.toInt()}%" })
            }
            Spacer(Modifier.height(10.dp))
            HeroChart()
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Eyebrow("OBSERVED")
                Eyebrow("TODAY", color = Nv.Mint)
                Eyebrow("FORECAST ◈")
            }
        }

        Spacer(Modifier.height(40.dp))

        Headline(listOf("See", "what", "happens", "next."))

        Spacer(Modifier.height(16.dp))
        Text(
            "NVILE detects signals, explains why they move, forecasts the outcome and tells you when to act. Every number carries its confidence and evidence tier.",
            style = NvType.Lead,
            modifier = Modifier.reveal(6),
        )

        Spacer(Modifier.height(40.dp))

        Column(Modifier.reveal(7), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PrimaryButton("Get early access", onStart)
            SecondaryButton("Sign in", onSignIn)
            FlowRow(
                Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
            ) {
                Evidence.entries.forEach { e ->
                    Text("${e.glyph} ${e.label.uppercase()}", style = NvType.MonoTag, color = evidenceColor(e))
                }
            }
        }
    }
}

/** Headline whose words rise into place one after another. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Headline(words: List<String>) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        words.forEachIndexed { i, w ->
            val a = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                delay(450L + i * 110L)
                a.animateTo(1f, tween(900, easing = NvMotion.Ease))
            }
            val last = i == words.lastIndex
            Text(
                w,
                style = NvType.Hero,
                color = if (last) Nv.Mint else Nv.Text,
                modifier = Modifier.graphicsLayer {
                    alpha = a.value
                    translationY = (1f - a.value) * 26.dp.toPx()
                    rotationX = (1f - a.value) * 35f
                },
            )
        }
    }
}
