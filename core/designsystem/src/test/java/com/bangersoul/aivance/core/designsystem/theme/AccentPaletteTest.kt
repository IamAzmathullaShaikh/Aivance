package com.bangersoul.aivance.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AccentPaletteTest {

    @Test
    fun `accent seeds expose human readable labels`() {
        assertEquals("Indigo", AccentSeed.INDIGO.label)
        assertEquals("Violet", AccentSeed.VIOLET.label)
        assertEquals("Teal", AccentSeed.TEAL.label)
        assertEquals("Emerald", AccentSeed.EMERALD.label)
        assertEquals("Orange", AccentSeed.ORANGE.label)
        assertEquals("Rose", AccentSeed.ROSE.label)
        assertEquals("Sky", AccentSeed.SKY.label)
    }

    @Test
    fun `all seven seeds have a full light palette`() {
        AccentSeed.entries.forEach { seed ->
            val palette = AccentPalettes.light(seed)
            assertNotNull("light ${seed.label} primary", palette.primary)
            assertNotEquals("light ${seed.label} onPrimary contrasts", palette.primary, palette.onPrimary)
            assertEquals("light ${seed.label} onPrimary is white-based", Color.White, palette.onPrimary)
        }
    }

    @Test
    fun `all seven seeds have a full dark palette`() {
        AccentSeed.entries.forEach { seed ->
            val palette = AccentPalettes.dark(seed)
            assertNotNull("dark ${seed.label} primary", palette.primary)
            assertNotEquals("dark ${seed.label} primary vs onPrimary", palette.primary, palette.onPrimary)
            assertNotEquals("dark ${seed.label} secondary vs onSecondary", palette.secondary, palette.onSecondary)
        }
    }

    @Test
    fun `dark palette differs from light palette per seed`() {
        AccentSeed.entries.forEach { seed ->
            val light = AccentPalettes.light(seed)
            val dark = AccentPalettes.dark(seed)
            assertNotEquals("${seed.label} primary differs dark vs light", light.primary, dark.primary)
        }
    }

    @Test
    fun `light scheme maps accent primary through`() {
        val accent = AccentPalettes.light(AccentSeed.INDIGO)
        val scheme = buildAccentLightScheme(accent)
        assertEquals(accent.primary, scheme.primary)
        assertEquals(accent.onPrimary, scheme.onPrimary)
        assertEquals(Zinc50, scheme.background)
    }

    @Test
    fun `dark scheme uses zinc background and amoled variant uses black`() {
        val accent = AccentPalettes.dark(AccentSeed.INDIGO)
        val scheme = buildAccentDarkScheme(accent)
        assertEquals(accent.primary, scheme.primary)
        assertEquals(Zinc950, scheme.background)

        val amoled = buildAccentDarkScheme(accent, amoled = true)
        assertEquals(Color.Black, amoled.background)
        assertEquals(Color.Black, amoled.surface)
    }

    @Test
    fun `extended colors adapt to dark mode`() {
        val light = extendedColorsFor(isDark = false)
        val dark = extendedColorsFor(isDark = true)

        assertEquals(Color(0xFF16A34A), light.success)
        assertEquals(Color(0xFF4ADE80), dark.success)
        assertNotEquals(light.outlineSoft, dark.outlineSoft)
        assertNotEquals(light.accent, dark.accent)
    }

    @Test
    fun `legacy color tokens remain defined`() {
        assertEquals(Color(0xFF09090B), DarkSurface)
        assertEquals(Color(0xFF18181B), DarkSurfaceVariant)
        assertEquals(Color.White, DarkPrimary)
        assertEquals(Color(0xFF3B82F6), DarkAccent)
        assertEquals(Color(0xFFFAFAFA), LightSurface)
    }
}
