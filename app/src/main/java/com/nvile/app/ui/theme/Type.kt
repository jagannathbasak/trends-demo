package com.nvile.app.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.nvile.app.R

@OptIn(ExperimentalTextApi::class)
private fun variable(res: Int, weight: Int) = Font(
    res,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

/** Space Grotesk: display and numerals. */
val Display = FontFamily(
    variable(R.font.space_grotesk, 400),
    variable(R.font.space_grotesk, 500),
    variable(R.font.space_grotesk, 600),
    variable(R.font.space_grotesk, 700),
)

/** Instrument Sans: reading text and controls. */
val Sans = FontFamily(
    variable(R.font.instrument_sans, 400),
    variable(R.font.instrument_sans, 500),
    variable(R.font.instrument_sans, 600),
)

/** IBM Plex Mono: labels, metadata, evidence marks. */
val Mono = FontFamily(
    Font(R.font.plex_mono_regular, FontWeight.Normal),
    Font(R.font.plex_mono_medium, FontWeight.Medium),
)

object NvType {
    val Hero = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 46.sp, lineHeight = 46.sp, letterSpacing = (-0.035).em, color = Nv.Text)
    val LargeTitle = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 34.sp, lineHeight = 38.sp, letterSpacing = (-0.025).em, color = Nv.Text)
    val Title = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 31.sp, letterSpacing = (-0.02).em, color = Nv.Text)
    val Title2 = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 21.sp, lineHeight = 26.sp, letterSpacing = (-0.01).em, color = Nv.Text)
    val Headline = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 23.sp, color = Nv.Text)
    val Number = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 26.sp, letterSpacing = (-0.01).em, color = Nv.Text)
    val BigNumber = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 54.sp, lineHeight = 54.sp, letterSpacing = (-0.03).em, color = Nv.Mint)

    val Body = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 23.sp, color = Nv.Text2)
    val BodyStrong = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 21.sp, color = Nv.Text)
    val Lead = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 25.sp, color = Nv.Text2)
    val Callout = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 19.sp, color = Nv.Text2)
    val Button = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, letterSpacing = 0.005.em)
    val TabLabel = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 10.5.sp)

    val Eyebrow = TextStyle(fontFamily = Mono, fontWeight = FontWeight.Medium, fontSize = 10.5.sp, letterSpacing = 0.12.em, color = Nv.Text3)
    val Mono = TextStyle(fontFamily = com.nvile.app.ui.theme.Mono, fontWeight = FontWeight.Normal, fontSize = 12.sp, color = Nv.Text3)
    val MonoTag = TextStyle(fontFamily = com.nvile.app.ui.theme.Mono, fontWeight = FontWeight.Medium, fontSize = 9.5.sp, letterSpacing = 0.1.em)
}
