package com.ekoehler.expressivecutout.events

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.provider.Settings
import android.util.Log
import com.ekoehler.expressivecutout.R
import com.ekoehler.expressivecutout.core.DynamicTile
import com.ekoehler.expressivecutout.core.SystemEventPayload
import com.ekoehler.expressivecutout.core.SystemEventType

/** Reads selected device status sources without retaining receivers or requesting new permissions. */
internal class DeviceTileSnapshot(private val context: Context) {
    /** An unavailable source is omitted so stale device tiles are removed by the controller. */
    fun read(enabled: Map<DynamicTile, Boolean>): Map<DynamicTile, SystemEventPayload> = buildMap {
        for (tile in listOf(DynamicTile.BATTERY, DynamicTile.NETWORK, DynamicTile.BLUETOOTH_AUDIO)) {
            if (enabled[tile] != true) continue
            val payload = runCatching {
                when (tile) {
                    DynamicTile.BATTERY -> battery()
                    DynamicTile.NETWORK -> network()
                    DynamicTile.BLUETOOTH_AUDIO -> bluetoothAudio()
                    else -> null
                }
            }.onFailure { Log.w(TAG, "Device tile unavailable: $tile", it) }.getOrNull()
            if (payload != null) put(tile, payload)
        }
    }

    /** Reads the sticky battery snapshot without registering a long-lived receiver. */
    private fun battery(): SystemEventPayload? {
        val battery = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return null
        val level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level < 0 || scale <= 0) return null
        val percent = (level * 100 / scale).coerceIn(0, 100)
        val plugged = battery.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0
        return SystemEventPayload(
            type = if (plugged) SystemEventType.CHARGING_STARTED else SystemEventType.CHARGING_STOPPED,
            title = context.getString(R.string.tile_battery),
            subtitle = context.getString(if (plugged) R.string.tile_battery_charging else R.string.tile_battery_discharging),
            collapsedBadgeText = "$percent%",
            actionIntentAction = Settings.ACTION_BATTERY_SAVER_SETTINGS,
        )
    }

    /** Uses transport metadata only; no SSID, location or network traffic is collected. */
    private fun network(): SystemEventPayload? {
        val manager = context.getSystemService(ConnectivityManager::class.java) ?: return null
        val capabilities = manager.activeNetwork?.let(manager::getNetworkCapabilities)
        val vpn = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        val label = when {
            capabilities == null -> R.string.tile_network_offline
            vpn -> R.string.tile_network_vpn
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> R.string.tile_network_wifi
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> R.string.tile_network_mobile
            else -> R.string.tile_network_other
        }
        return SystemEventPayload(
            type = when {
                vpn -> SystemEventType.VPN_CONNECTED
                capabilities == null -> SystemEventType.WIFI_DISCONNECTED
                else -> SystemEventType.WIFI_CONNECTED
            },
            title = context.getString(R.string.tile_network),
            subtitle = context.getString(label),
            actionIntentAction = if (vpn) Settings.ACTION_VPN_SETTINGS else Settings.ACTION_WIRELESS_SETTINGS,
        )
    }

    /** Shows connected audio outputs only, including Bluetooth LE and hearing-aid routes. */
    private fun bluetoothAudio(): SystemEventPayload? {
        val manager = context.getSystemService(AudioManager::class.java) ?: return null
        val connected = manager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).any {
            it.type in BLUETOOTH_OUTPUT_TYPES
        }
        if (!connected) return null
        return SystemEventPayload(
            type = SystemEventType.BLUETOOTH_CONNECTED,
            title = context.getString(R.string.tile_bluetooth_audio),
            subtitle = context.getString(R.string.tile_audio_connected),
            actionIntentAction = Settings.ACTION_BLUETOOTH_SETTINGS,
        )
    }

    private companion object {
        /** Log tag and transport types supported by the audio status tile. */
        const val TAG = "DeviceTileSnapshot"
        val BLUETOOTH_OUTPUT_TYPES = setOf(
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
            AudioDeviceInfo.TYPE_HEARING_AID, AudioDeviceInfo.TYPE_BLE_HEADSET,
            AudioDeviceInfo.TYPE_BLE_SPEAKER, AudioDeviceInfo.TYPE_BLE_BROADCAST,
        )
    }
}
