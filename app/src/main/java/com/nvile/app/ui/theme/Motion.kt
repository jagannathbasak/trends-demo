package com.nvile.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

/** Shared motion language: soft, physical springs and one signature ease. */
object NvMotion {
    /** Expressive ease-out used for reveals and chart draws. */
    val Ease = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val EaseInOut = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)

    fun <T> press() = spring<T>(dampingRatio = 0.55f, stiffness = 520f)
    fun <T> settle() = spring<T>(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
    fun <T> gentle() = spring<T>(dampingRatio = 0.9f, stiffness = Spring.StiffnessLow)
}
