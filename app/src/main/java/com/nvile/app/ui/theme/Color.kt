package com.nvile.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Observatory palette. True black, Apple-style label greys, and two light spectra:
 * the prediction spectrum (mint, cyan, indigo) and the caution spectrum (amber, coral).
 */
object Nv {
    // Grounds
    val Black = Color(0xFF000000)
    val Ink = Color(0xFF000000)
    val Raised = Color(0xFF0C0C0F)
    val Raised2 = Color(0xFF141418)
    val Raised3 = Color(0xFF1C1C21)
    val Separator = Color(0x1FFFFFFF)

    // Labels (iOS-style opacities on white)
    val Text = Color(0xFFFFFFFF)
    val Text2 = Color(0x99EBEBF5)
    val Text3 = Color(0x5CEBEBF5)
    val Text4 = Color(0x33EBEBF5)

    // Prediction spectrum
    val Mint = Color(0xFF3CF0C5)
    val Cyan = Color(0xFF39C6FF)
    val Indigo = Color(0xFF6D6BFF)
    val Violet = Color(0xFFB26BFF)

    // Caution spectrum
    val Amber = Color(0xFFFFB547)
    val Coral = Color(0xFFFF6B5B)

    val OnLight = Color(0xFF000000)

    fun white(a: Float) = Color.White.copy(alpha = a)
    fun mint(a: Float) = Mint.copy(alpha = a)
    fun amber(a: Float) = Amber.copy(alpha = a)

    val Spectrum = listOf(Mint, Cyan, Indigo)
    val Caution = listOf(Amber, Coral)
    fun spectrumBrush() = Brush.linearGradient(Spectrum)
    fun cautionBrush() = Brush.linearGradient(Caution)
}
