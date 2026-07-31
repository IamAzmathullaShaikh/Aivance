package com.bangersoul.aivance.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * Theme mode — how the app resolves light vs dark.
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    AMOLED;

    val label: String
        get() = when (this) {
            SYSTEM -> "System"
            LIGHT -> "Light"
            DARK -> "Dark"
            AMOLED -> "AMOLED"
        }
}

@Composable
fun AivanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentSeed: AccentSeed = AccentSeed.INDIGO,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> darkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.AMOLED -> true
    }
    val isAmoled = themeMode == ThemeMode.AMOLED

    val accent = if (isDark) AccentPalettes.dark(accentSeed) else AccentPalettes.light(accentSeed)

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && themeMode != ThemeMode.AMOLED -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> buildAccentDarkScheme(accent, amoled = isAmoled)
        else -> buildAccentLightScheme(accent)
    }

    val extendedColors = extendedColorsFor(isDark).copy(
        accent = accent.primary,
        onAccent = accent.onPrimary
    )

    CompositionLocalProvider(
        LocalAivanceSpacing provides AivanceSpacing(),
        LocalAivanceShapes provides AivanceShapes(),
        LocalAivanceMotion provides AivanceMotion(),
        LocalAivanceElevation provides AivanceElevation(),
        LocalAivanceExtendedColors provides extendedColors,
        LocalThemeMode provides themeMode
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

val LocalThemeMode = staticCompositionLocalOf { ThemeMode.SYSTEM }

object AivanceTheme {
    val spacing: AivanceSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalAivanceSpacing.current

    val shapes: AivanceShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalAivanceShapes.current

    val motion: AivanceMotion
        @Composable
        @ReadOnlyComposable
        get() = LocalAivanceMotion.current

    val elevation: AivanceElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalAivanceElevation.current

    /** Semantic extended colors (success / warning / info / accent). */
    val colors: AivanceExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAivanceExtendedColors.current

    val themeMode: ThemeMode
        @Composable
        @ReadOnlyComposable
        get() = LocalThemeMode.current
}
