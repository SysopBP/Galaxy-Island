package com.ekoehler.expressivecutout.ui.screen.tiles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekoehler.expressivecutout.R
import com.ekoehler.expressivecutout.core.DynamicTile
import com.ekoehler.expressivecutout.ui.AppViewModel
import com.ekoehler.expressivecutout.ui.screen.SettingsToggleCard

/** Controls opt-in device status tiles using existing system access. */
@Composable
internal fun DeviceTileScreen(tile: DynamicTile, viewModel: AppViewModel, contentPadding: PaddingValues) {
    val enabled by viewModel.tileEnabled.collectAsStateWithLifecycle()
    Column(Modifier.padding(contentPadding)) {
        SettingsToggleCard(
            shape = RoundedCornerShape(32.dp),
            title = stringResource(tile.labelRes),
            description = stringResource(tile.descriptionRes),
            checked = enabled[tile] ?: tile.enabledByDefault,
            onCheckedChange = { viewModel.setTileEnabled(tile, it) },
        )
        Text(stringResource(R.string.tile_device_note), modifier = Modifier.padding(16.dp))
    }
}
