package com.nvile.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Hairline stroke icons on a 24-unit grid, drawn from the design canvas's SVG paths. */
object NvIcons {
    private fun stroke(name: String, vararg paths: String, width: Float = 1.8f): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            paths.forEach { d ->
                addPath(
                    pathData = addPathNodes(d),
                    stroke = SolidColor(Color.White),
                    strokeLineWidth = width,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()

    val Today by lazy { stroke("today", "M3 12h4l3-8 4 16 3-8h4") }
    val Signals by lazy { stroke("signals", "M5 20v-8M12 20V5M19 20v-11") }
    val Ask by lazy { stroke("ask", "M4 5h16v11H9l-5 4z") }
    val Ideas by lazy { stroke("ideas", "M9 18h6M10 21h4M12 3a6 6 0 0 0-4 10.5c.7.7 1 1.5 1 2.5h6c0-1 .3-1.8 1-2.5A6 6 0 0 0 12 3z") }
    val You by lazy { stroke("you", "M16 8a4 4 0 1 1-8 0a4 4 0 1 1 8 0", "M4 21c1.5-4 4.5-6 8-6s6.5 2 8 6") }
    val Bell by lazy { stroke("bell", "M6 16V11a6 6 0 0 1 12 0v5l1.5 2h-15z", "M10 20a2 2 0 0 0 4 0") }
    val Back by lazy { stroke("back", "M15 5l-7 7 7 7", width = 2f) }
    val Share by lazy { stroke("share", "M12 15V3M7 8l5-5 5 5M5 13v7h14v-7") }
    val Check by lazy { stroke("check", "M5 12l5 5 9-10", width = 2.2f) }
    val Search by lazy { stroke("search", "M18 11a7 7 0 1 1-14 0a7 7 0 1 1 14 0", "M20 20l-4.2-4.2") }
    val Arrow by lazy { stroke("arrow", "M5 12h14M13 6l6 6-6 6") }
    val Chevron by lazy { stroke("chevron", "M9 6l6 6-6 6", width = 2f) }
    val ChevronDown by lazy { stroke("chevronDown", "M6 9l6 6 6-6", width = 2f) }
    val Sparkle by lazy { stroke("sparkle", "M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8z", "M19 16l.7 1.8 1.8.7-1.8.7-.7 1.8-.7-1.8-1.8-.7 1.8-.7z") }
    val Send by lazy { stroke("send", "M12 19V5M6 11l6-6 6 6", width = 2.2f) }
    val Plus by lazy { stroke("plus", "M12 5v14M5 12h14", width = 2f) }
    val Close by lazy { stroke("close", "M6 6l12 12M18 6L6 18", width = 2f) }
    val Download by lazy { stroke("download", "M12 4v11M7 10l5 5 5-5M5 20h14") }
    val Shield by lazy { stroke("shield", "M12 3l8 3v6c0 4.5-3.4 8.2-8 9-4.6-.8-8-4.5-8-9V6z", "M9 12l2 2 4-4") }
    val Target by lazy { stroke("target", "M21 12a9 9 0 1 1-18 0a9 9 0 1 1 18 0", "M16 12a4 4 0 1 1-8 0a4 4 0 1 1 8 0") }
    val Logout by lazy { stroke("logout", "M15 4h4v16h-4M10 8l-4 4 4 4M6 12h10") }
}

@Composable
fun NvIcon(icon: ImageVector, tint: Color, modifier: Modifier = Modifier, size: Dp = 22.dp, contentDescription: String? = null) {
    Icon(imageVector = icon, contentDescription = contentDescription, tint = tint, modifier = modifier.size(size))
}
