package com.bangersoul.aivance.core.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Motion tokens centralizing animation durations and easings.
 *
 * All animations in the design system should consume these values —
 * never hardcode durations inside components.
 */
@Immutable
data class AivanceMotion(
    // Durations
    val durationFast: Int = 150,
    val durationStandard: Int = 250,
    val durationSlow: Int = 400,
    val durationEmphasis: Int = 700,
    // Easing curves
    val easingStandard: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f),
    val easingEmphasized: Easing = FastOutSlowInEasing,
    val easingDecelerate: Easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f),
    val easingAccelerate: Easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
)

@Immutable
data class AivanceElevation(
    val flat: Float = 0f,
    val low: Float = 1f,
    val medium: Float = 3f,
    val high: Float = 8f
)

val LocalAivanceMotion = staticCompositionLocalOf { AivanceMotion() }
val LocalAivanceElevation = staticCompositionLocalOf { AivanceElevation() }
