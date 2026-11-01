package com.nvile.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * NVILE palette. Near-black ink grounds, a single luminous mint accent and an amber
 * caution tone, taken from the NVILE Mobile App design canvas.
 */
object Nv {
    // Grounds
    val Ink = Color(0xFF030605)
    val Ink1 = Color(0xFF070C0B)
    val Ink2 = Color(0xFF0C1312)
    val Ink3 = Color(0xFF131C1A)

    // Glass layers (white over ink)
    val Glass = Color(0x0FFFFFFF)
    val GlassLow = Color(0x08FFFFFF)
    val GlassHigh = Color(0x17FFFFFF)
    val Hairline = Color(0x14FFFFFF)
    val HairlineStrong = Color(0x29FFFFFF)

    // Text
    val Text = Color(0xFFF2F6F5)
    val Text2 = Color(0xFFA3ADAA)
    val Text3 = Color(0xFF8C9794)
    val Text4 = Color(0xFF5E6866)

    // Accents
    val Mint = Color(0xFF4FE3C1)
    val MintBright = Color(0xFF8AF5DC)
    val MintDeep = Color(0xFF02B08A)
    val OnMint = Color(0xFF02110D)
    val Amber = Color(0xFFE0A94F)
    val AmberSoft = Color(0xFFC9B48C)
    val Sky = Color(0xFF7AA7FF)

    fun mint(alpha: Float) = Mint.copy(alpha = alpha)
    fun amber(alpha: Float) = Amber.copy(alpha = alpha)
    fun white(alpha: Float) = Color.White.copy(alpha = alpha)
}
