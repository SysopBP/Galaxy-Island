package com.ekoehler.expressivecutout.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Public-API status state used by the Galaxy Island replacement bar. */
data class GalaxyStatusState(
    val batteryPercent: Int = 100,
    val charging: Boolean = false,
    val wifiConnected: Boolean = false,
    val cellularConnected: Boolean = false,
    val vpnConnected: Boolean = false,
)

/**
 * Keeps a small, always-safe status snapshot. Samsung/SystemUI-specific cellular detail is layered
 * on top by the Xposed bridge later; this monitor deliberately remains useful without root.
 */
class GalaxyStatusMonitor(private val context: Context) {
    private val connectivity = context.getSystemService<ConnectivityManager>()
    private val _state = MutableStateFlow(GalaxyStatusState())
    val state: StateFlow<GalaxyStatusState> = _state

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = updateBattery(intent)
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = updateNetworks()
        override fun onLost(network: Network) = updateNetworks()
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) = updateNetworks()
    }

    fun start() {
        ContextCompat.registerReceiver(
            context,
            batteryReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )?.let(::updateBattery)
        runCatching {
            connectivity?.registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback)
        }
        updateNetworks()
    }

    fun stop() {
        runCatching { context.unregisterReceiver(batteryReceiver) }
        runCatching { connectivity?.unregisterNetworkCallback(networkCallback) }
    }

    private fun updateBattery(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0
        _state.value = _state.value.copy(
            batteryPercent = (level * 100 / scale).coerceIn(0, 100),
            charging = plugged,
        )
    }

    private fun updateNetworks() {
        val cm = connectivity ?: return
        var wifi = false
        var cellular = false
        var vpn = false
        runCatching {
            cm.allNetworks.forEach { network ->
                val caps = cm.getNetworkCapabilities(network) ?: return@forEach
                wifi = wifi || caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                cellular = cellular || caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                vpn = vpn || caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
            }
        }
        _state.value = _state.value.copy(
            wifiConnected = wifi,
            cellularConnected = cellular,
            vpnConnected = vpn,
        )
    }
}
