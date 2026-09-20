package com.ekoehler.expressivecutout.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Guards readable text when users choose extreme custom accent colors. */
class AppPaletteTest {
    /** Every preset and both extreme seeds must retain WCAG normal-text contrast. */
    @Test fun customColorsKeepTextReadable() {
        for (seed in AppPalette.presets + listOf(0xFF000000L, 0xFFFFFFFFL)) {
            for (dark in listOf(false, true)) {
                val scheme = AppPalette.scheme(seed, dark)
                for ((background, foreground) in listOf(
                    scheme.primary to scheme.onPrimary,
                    scheme.surface to scheme.onSurface,
                    scheme.primaryContainer to scheme.onPrimaryContainer,
                )) {
                    val a = background.luminance()
                    val b = foreground.luminance()
                    assertTrue("Unreadable seed $seed, dark=$dark", (maxOf(a,b)+.05f)/(minOf(a,b)+.05f) >= 4.5f)
                }
            }
        }
    }

    /** AMOLED pages remain black while keeping a visible foreground. */
    @Test fun amoledKeepsBlackSurfaces() {
        val scheme = AppPalette.amoled(AppPalette.scheme(0xFF608EC7L, true))
        assertEquals(Color.Black, scheme.background)
        assertEquals(Color.Black, scheme.surface)
        assertEquals(Color.White, scheme.onSurface)
    }
}
