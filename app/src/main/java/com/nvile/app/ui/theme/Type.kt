package com.nvile.app.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
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

/** Geist: the voice of the interface, from hairline numerals to bold headings. */
val Geist = FontFamily((100..800 step 100).map { variable(R.font.geist, it) })

/** Geist Mono: data, timestamps and evidence marks. */
val GeistMono = FontFamily((300..600 step 100).map { variable(R.font.geist_mono, it) })

/** Instrument Serif: NVILE's verdicts, written like an editor would. */
val Serif = FontFamily(
    Font(R.font.instrument_serif, FontWeight.Normal),
    Font(R.font.instrument_serif_italic, FontWeight.Normal, FontStyle.Italic),
)

object NvType {
    /** Giant hairline numerals: the hero of every market. */
    val Numeral = TextStyle(fontFamily = Geist, fontWeight = FontWeight.Thin, fontSize = 96.sp, lineHeight = 92.sp, letterSpacing = (-0.05).em, color = Nv.Text)
    val NumeralM = TextStyle(fontFamily = Geist, fontWeight = FontWeight.ExtraLight, fontSize = 56.sp, lineHeight = 56.sp, letterSpacing = (-0.04).em, color = Nv.Text)
    val NumeralS = TextStyle(fontFamily = Geist, fontWeight = FontWeight.Light, fontSize = 30.sp, lineHeight = 32.sp, letterSpacing = (-0.03).em, color = Nv.Text)

    val Display = TextStyle(fontFamily = Geist, fontWeight = FontWeight.Light, fontSize = 46.sp, lineHeight = 48.sp, letterSpacing = (-0.045).em, color = Nv.Text)
    val LargeTitle = TextStyle(fontFamily = Geist, fontWeight = FontWeight.SemiBold, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.03).em, color = Nv.Text)
    val Title = TextStyle(fontFamily = Geist, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 29.sp, letterSpacing = (-0.025).em, color = Nv.Text)
    val Headline = TextStyle(fontFamily = Geist, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp, letterSpacing = (-0.01).em, color = Nv.Text)
    val Body = TextStyle(fontFamily = Geist, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 23.sp, letterSpacing = (-0.005).em, color = Nv.Text2)
    val BodyStrong = TextStyle(fontFamily = Geist, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = (-0.01).em, color = Nv.Text)
    val Callout = TextStyle(fontFamily = Geist, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, color = Nv.Text2)
    val Caption = TextStyle(fontFamily = Geist, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.01.em, color = Nv.Text3)
    val Button = TextStyle(fontFamily = Geist, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, letterSpacing = (-0.01).em)

    /** Serif verdict: the one sentence NVILE wants you to remember. */
    val Verdict = TextStyle(fontFamily = Serif, fontStyle = FontStyle.Italic, fontSize = 38.sp, lineHeight = 42.sp, letterSpacing = (-0.01).em, color = Nv.Text)
    val VerdictS = TextStyle(fontFamily = Serif, fontStyle = FontStyle.Italic, fontSize = 28.sp, lineHeight = 32.sp, color = Nv.Text)
    val SerifDisplay = TextStyle(fontFamily = Serif, fontSize = 52.sp, lineHeight = 52.sp, letterSpacing = (-0.02).em, color = Nv.Text)

    val Data = TextStyle(fontFamily = GeistMono, fontWeight = FontWeight.Normal, fontSize = 13.sp, color = Nv.Text2)
    val Label = TextStyle(fontFamily = GeistMono, fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.08.em, color = Nv.Text3)
}
