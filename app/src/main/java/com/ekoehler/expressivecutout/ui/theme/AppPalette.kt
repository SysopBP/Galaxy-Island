package com.ekoehler.expressivecutout.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/** Shared accent presets and contrast-aware fixed palettes for the settings application. */
internal object AppPalette {
    /** The same optional accent seeds offered by the paired D2 application. */
    val presets = listOf(0xFF608EC7L, 0xFF529F9CL, 0xFF9A80BEL, 0xFFBD829EL, 0xFFC69A53L, 0xFF829286L)

    /** Produces readable Material roles without adding a palette-generation dependency. */
    fun scheme(argb: Long, dark: Boolean): ColorScheme {
        val seed = Color(argb or OPAQUE)
        val base = if (dark) darkColorScheme() else lightColorScheme()
        val primary = lerp(seed, if (dark) Color.White else Color.Black, PRIMARY_BLEND)
        val surface = lerp(if (dark) Color.Black else Color.White, seed, SURFACE_BLEND)
        val container = lerp(if (dark) Color.Black else Color.White, seed, CONTAINER_BLEND)
        val foreground = if (dark) Color.White else Color.Black
        return base.copy(
            primary = primary, onPrimary = if (dark) Color.Black else Color.White,
            secondary = primary, onSecondary = if (dark) Color.Black else Color.White,
            tertiary = primary, onTertiary = if (dark) Color.Black else Color.White,
            primaryContainer = container, onPrimaryContainer = foreground,
            secondaryContainer = container, onSecondaryContainer = foreground,
            tertiaryContainer = container, onTertiaryContainer = foreground,
            background = surface, onBackground = foreground,
            surface = surface, onSurface = foreground, surfaceTint = primary,
            surfaceVariant = container, onSurfaceVariant = foreground,
            surfaceContainer = container, surfaceContainerLow = surface,
            surfaceContainerLowest = surface, surfaceContainerHigh = container,
            surfaceContainerHighest = container,
        )
    }

    /** Makes the page and low surfaces black while keeping raised surfaces discernible. */
    fun amoled(scheme: ColorScheme) = scheme.copy(
        background = Color.Black, surface = Color.Black,
        surfaceContainerLowest = Color.Black, surfaceContainerLow = Color.Black,
        surfaceContainer = lerp(Color.Black, scheme.primary, AMOLED_SURFACE_BLEND),
        surfaceContainerHigh = lerp(Color.Black, scheme.primary, AMOLED_SURFACE_BLEND),
        surfaceContainerHighest = lerp(Color.Black, scheme.primary, AMOLED_SURFACE_BLEND),
    )

    /** Blend limits preserve foreground contrast even for black or white custom seeds. */
    private const val OPAQUE = 0xFF000000L
    private const val PRIMARY_BLEND = .60f
    private const val SURFACE_BLEND = .035f
    private const val CONTAINER_BLEND = .14f
    private const val AMOLED_SURFACE_BLEND = .075f
}
