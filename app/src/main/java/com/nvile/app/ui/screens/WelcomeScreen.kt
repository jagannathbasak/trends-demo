package com.nvile.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.nvile.app.R
import com.nvile.app.ui.components.Atmosphere
import com.nvile.app.ui.components.Mood
import com.nvile.app.ui.components.Orb
import com.nvile.app.ui.components.PillButton
import com.nvile.app.ui.components.reveal
import com.nvile.app.ui.components.tappable
import com.nvile.app.ui.theme.Geist
import com.nvile.app.ui.theme.Nv
import com.nvile.app.ui.theme.NvMotion
import com.nvile.app.ui.theme.NvType
import com.nvile.app.ui.theme.Serif

@OptIn(ExperimentalTextApi::class)
@Composable
fun WelcomeScreen(onStart: () -> Unit, onSignIn: () -> Unit) {
    val orbIn = remember { Animatable(0f) }
    LaunchedEffect(Unit) { orbIn.animateTo(1f, tween(1800, easing = NvMotion.Ease)) }

    Box(Modifier.fillMaxSize()) {
        Atmosphere(Mood.Night, strength = 0.9f)
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.reveal(0).padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.nvile_mark), contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(9.dp))
                Text("NVILE", style = NvType.Headline.copy(fontFamily = Geist, fontWeight = FontWeight.SemiBold, letterSpacing = 0.32.em, fontSize = 14.sp))
            }

            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Orb(
                    Modifier
                        .fillMaxHeight(0.92f)
                        .aspectRatio(1f)
                        .graphicsLayer {
                            alpha = orbIn.value
                            val s = 0.7f + 0.3f * orbIn.value
                            scaleX = s; scaleY = s
                        },
                    energy = 0.2f,
                )
            }

            Text(
                buildAnnotatedString {
                    append("See what\nhappens ")
                    withStyle(SpanStyle(fontFamily = Serif, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Normal, brush = Brush.linearGradient(Nv.Spectrum), letterSpacing = 0.em)) {
                        append("next.")
                    }
                },
                style = NvType.Display.copy(fontSize = 52.sp, lineHeight = 54.sp, textAlign = TextAlign.Center),
                modifier = Modifier.fillMaxWidth().reveal(3, distance = 32.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "NVILE reads millions of market signals and tells you what moves next, why, and when to act.",
                style = NvType.Body.copy(textAlign = TextAlign.Center, fontSize = 17.sp, lineHeight = 25.sp),
                modifier = Modifier.reveal(5),
            )
            Spacer(Modifier.height(36.dp))
            Column(Modifier.reveal(7), verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                PillButton("Get started", onStart)
                Text(
                    "I already have an account",
                    style = NvType.BodyStrong.copy(color = Nv.Text2),
                    modifier = Modifier.tappable(onClick = onSignIn).padding(12.dp),
                )
            }
        }
    }
}
