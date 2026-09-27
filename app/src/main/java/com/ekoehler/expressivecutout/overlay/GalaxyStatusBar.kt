package com.ekoehler.expressivecutout.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.CellTower
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ekoehler.expressivecutout.system.GalaxyStatusState

/** First live renderer for Galaxy Island's replacement status bar. */
@Composable
internal fun GalaxyStatusBar(
    state: GalaxyStatusState,
    showCellular: Boolean,
    showWifi: Boolean,
    showBattery: Boolean,
    showConnectivity: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        if (showConnectivity && state.vpnConnected) {
            Icon(Icons.Rounded.Security, contentDescription = "VPN")
            Spacer(Modifier.width(6.dp))
        }
        if (showWifi && state.wifiConnected) {
            Icon(Icons.Rounded.Wifi, contentDescription = "Wi-Fi")
            Spacer(Modifier.width(6.dp))
        }
        if (showCellular && state.cellularConnected) {
            Icon(Icons.Rounded.CellTower, contentDescription = "Cellular")
            Spacer(Modifier.width(6.dp))
        }
        if (showBattery) {
            Icon(
                if (state.charging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryFull,
                contentDescription = "Battery",
            )
            Spacer(Modifier.width(3.dp))
            Text(text = "${state.batteryPercent}%", style = MaterialTheme.typography.labelMedium)
        }
    }
}
