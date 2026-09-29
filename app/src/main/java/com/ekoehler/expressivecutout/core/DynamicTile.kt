package com.ekoehler.expressivecutout.core

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.ui.graphics.vector.ImageVector
import com.ekoehler.expressivecutout.R

/**
 * The closed set of *dynamic tiles* the cutout can display — live, ongoing content (e.g. the
 * track currently playing) as opposed to the momentary device happenings in [SystemEventType].
 * Each tile can be turned on or off independently on the "Dynamic tiles" screen.
 */
enum class DynamicTile(
    val defaultIcon: ImageVector,
    @param:StringRes val labelRes: Int,
    @param:StringRes val descriptionRes: Int,
    val accent: Long,
    val enabledByDefault: Boolean = true,
) {
    MUSIC(Icons.Rounded.MusicNote, R.string.tile_music, R.string.tile_music_desc, 0xFFF472B6),
    PHONE(Icons.Rounded.Call, R.string.tile_phone, R.string.tile_phone_desc, 0xFF22C55E),
    TIMER(Icons.Rounded.Timer, R.string.tile_timer, R.string.tile_timer_desc, 0xFFF59E0B),
    ASSISTANT(Icons.Rounded.AutoAwesome, R.string.tile_assistant, R.string.tile_assistant_desc, 0xFF8B5CF6),
    BATTERY(Icons.Rounded.BatteryChargingFull, R.string.tile_battery, R.string.tile_battery_desc, 0xFF69F0AE, false),
    NETWORK(Icons.Rounded.Wifi, R.string.tile_network, R.string.tile_network_desc, 0xFF64B5F6, false),
    BLUETOOTH_AUDIO(Icons.Rounded.Headphones, R.string.tile_bluetooth_audio, R.string.tile_bluetooth_audio_desc, 0xFFB388FF, false),
    DOWNLOADS(Icons.Rounded.Download, R.string.tile_downloads, R.string.tile_downloads_desc, 0xFF38BDF8),
    NAVIGATION(Icons.Rounded.Navigation, R.string.tile_navigation, R.string.tile_navigation_desc, 0xFF22C55E),
}
