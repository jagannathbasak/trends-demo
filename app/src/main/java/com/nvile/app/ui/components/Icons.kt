package com.nvile.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Hairline stroke icons on a 24-unit grid. */
object NvIcons {
    private fun stroke(name: String, vararg paths: String, width: Float = 1.7f): ImageVector =
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

    val Pulse by lazy { stroke("pulse", "M3 12h4l3-8 4 16 3-8h4") }
    val Markets by lazy { stroke("markets", "M4 19l5-6 4 3 7-9", "M15 7h5v5") }
    val Ideas by lazy { stroke("ideas", "M9 18h6M10 21h4M12 3a6 6 0 0 0-4 10.5c.7.7 1 1.5 1 2.5h6c0-1 .3-1.8 1-2.5A6 6 0 0 0 12 3z") }
    val You by lazy { stroke("you", "M16 8a4 4 0 1 1-8 0a4 4 0 1 1 8 0", "M4 21c1.5-4 4.5-6 8-6s6.5 2 8 6") }
    val Bell by lazy { stroke("bell", "M6 16V11a6 6 0 0 1 12 0v5l1.5 2h-15z", "M10 20a2 2 0 0 0 4 0") }
    val Back by lazy { stroke("back", "M15 5l-7 7 7 7", width = 2.2f) }
    val Close by lazy { stroke("close", "M6 6l12 12M18 6L6 18", width = 2.2f) }
    val Share by lazy { stroke("share", "M12 15V3M7 8l5-5 5 5M5 13v7h14v-7") }
    val Check by lazy { stroke("check", "M5 12l5 5 9-10", width = 2.4f) }
    val Plus by lazy { stroke("plus", "M12 5v14M5 12h14", width = 2.2f) }
    val Search by lazy { stroke("search", "M18 11a7 7 0 1 1-14 0a7 7 0 1 1 14 0", "M20 20l-4.2-4.2") }
    val Arrow by lazy { stroke("arrow", "M5 12h14M13 6l6 6-6 6") }
    val Chevron by lazy { stroke("chevron", "M9 6l6 6-6 6", width = 2f) }
    val Send by lazy { stroke("send", "M12 19V5M6 11l6-6 6 6", width = 2.4f) }
    val Sparkle by lazy { stroke("sparkle", "M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8z") }
    val Target by lazy { stroke("target", "M21 12a9 9 0 1 1-18 0a9 9 0 1 1 18 0", "M16 12a4 4 0 1 1-8 0a4 4 0 1 1 8 0") }
    val Shield by lazy { stroke("shield", "M12 3l8 3v6c0 4.5-3.4 8.2-8 9-4.6-.8-8-4.5-8-9V6z", "M9 12l2 2 4-4") }
    val Logout by lazy { stroke("logout", "M15 4h4v16h-4M10 8l-4 4 4 4M6 12h10") }
    val Card by lazy { stroke("card", "M3 6h18v12H3z", "M3 10h18") }
    val Link by lazy { stroke("link", "M10 14a4 4 0 0 0 5.7 0l3-3a4 4 0 0 0-5.7-5.7l-1 1", "M14 10a4 4 0 0 0-5.7 0l-3 3a4 4 0 0 0 5.7 5.7l1-1") }
}
