package com.nvile.app.ui.theme

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val scheme = darkColorScheme(
    primary = Nv.Mint,
    onPrimary = Nv.OnLight,
    secondary = Nv.Cyan,
    background = Nv.Black,
    onBackground = Nv.Text,
    surface = Nv.Raised,
    onSurface = Nv.Text,
    surfaceVariant = Nv.Raised2,
    onSurfaceVariant = Nv.Text2,
    outline = Nv.Separator,
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
