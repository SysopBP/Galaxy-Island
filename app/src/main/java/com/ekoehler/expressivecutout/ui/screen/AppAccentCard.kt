package com.ekoehler.expressivecutout.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekoehler.expressivecutout.R
import com.ekoehler.expressivecutout.data.ColorSpec
import com.ekoehler.expressivecutout.data.CutoutColor
import com.ekoehler.expressivecutout.data.CutoutFill
import com.ekoehler.expressivecutout.ui.AppViewModel
import com.ekoehler.expressivecutout.ui.components.ColorPickerCard
import com.ekoehler.expressivecutout.ui.theme.AppPalette
import com.ekoehler.expressivecutout.ui.theme.AppTheme

/** Theme seed picker; copying it to the island is explicit so existing styling is preserved. */
@Composable
internal fun AppAccentCard(viewModel: AppViewModel) {
    val accent by viewModel.themeAccent.collectAsStateWithLifecycle()
    val resolved = MaterialTheme.colorScheme.primary
    Column {
        ColorPickerCard(
            label = stringResource(R.string.app_accent),
            selected = if (accent == 0L) null else CutoutColor.Solid(accent),
            onSelect = { viewModel.setThemeAccent((it as? CutoutColor.Solid)?.argb ?: 0L) },
            defaultLabel = stringResource(R.string.accent_wallpaper),
            defaultColor = resolved,
            presetColors = AppPalette.presets,
            dynamicRoles = emptyList(),
            allowAppIcon = false,
        )
        Text(stringResource(R.string.copy_accent_explanation), modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = {
            val color = if (accent == 0L) resolved else Color(accent)
            val fill = lerp(Color.Black, color, ISLAND_TINT).copy(alpha = ISLAND_OPACITY)
            val spec = CutoutFill.Solid(ColorSpec.Fixed(fill.toArgb().toLong() and ARGB_MASK))
            viewModel.setBackgroundNormal(spec)
            viewModel.setBackgroundExpanded(spec)
            viewModel.setStrokeEnabled(true)
            viewModel.setStrokeColor(CutoutColor.Solid(color.toArgb().toLong() and ARGB_MASK))
            viewModel.setTextColor(CutoutColor.Solid(ARGB_MASK))
        }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.copy_accent_island)) }
        TextButton(onClick = {
            viewModel.setTheme(AppTheme.SYSTEM)
            viewModel.setThemeAccent(0L)
        }) { Text(stringResource(R.string.reset_app_theme)) }
    }
}

/** Dark tint and opacity retain white-text contrast when the pill overlays other applications. */
private const val ISLAND_TINT = .3f
private const val ISLAND_OPACITY = .88f
private const val ARGB_MASK = 0xFFFFFFFFL
