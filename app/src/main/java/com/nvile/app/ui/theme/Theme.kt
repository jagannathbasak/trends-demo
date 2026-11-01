package com.nvile.app.ui.theme

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val scheme = darkColorScheme(
    primary = Nv.Mint,
    onPrimary = Nv.OnMint,
    secondary = Nv.Amber,
    background = Nv.Ink,
    onBackground = Nv.Text,
    surface = Nv.Ink2,
    onSurface = Nv.Text,
    surfaceVariant = Nv.Ink3,
    onSurfaceVariant = Nv.Text2,
    outline = Nv.HairlineStrong,
)

@Composable
fun NvileTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme) {
        CompositionLocalProvider(
            LocalTextSelectionColors provides TextSelectionColors(Nv.Mint, Nv.mint(0.3f)),
            content = content,
        )
    }
}
