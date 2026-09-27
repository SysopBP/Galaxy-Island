package com.ekoehler.expressivecutout.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

/** Backing store for the status-bar hiding settings. */
private val Context.statusBarDataStore: DataStore<Preferences> by preferencesDataStore(name = "status_bar_prefs")

/**
 * What the user wants done to the system status bar, applied through Shizuku by
 * `StatusBarIconController`.
 *
 * This stores the *intent*, not the achieved state: the flags themselves live in system_server and
 * are lost whenever our process dies or the device reboots. Keeping the wish here is what lets the
 * controller re-apply it once Shizuku is reachable again.
 */
class StatusBarPreferences(private val context: Context) : JsonSerializable {

    val hideNotificationIcons: Flow<Boolean> = context.statusBarDataStore.data.map { prefs ->
        prefs[HIDE_NOTIFICATION_ICONS] ?: false
    }

    val hideSystemInfo: Flow<Boolean> = context.statusBarDataStore.data.map { prefs ->
        prefs[HIDE_SYSTEM_INFO] ?: false
    }

    val hideClock: Flow<Boolean> = context.statusBarDataStore.data.map { prefs ->
        prefs[HIDE_CLOCK] ?: false
    }

    val silenceAlerts: Flow<Boolean> = context.statusBarDataStore.data.map { prefs ->
        prefs[SILENCE_ALERTS] ?: false
    }

    /** Master switch for Galaxy Island's complete status-bar replacement surface. */
    val replacementEnabled: Flow<Boolean> = context.statusBarDataStore.data.map { prefs ->
        prefs[REPLACEMENT_ENABLED] ?: false
    }

    val replacementNotifications: Flow<Boolean> = replacementFlag(REPLACEMENT_NOTIFICATIONS)
    val replacementCellular: Flow<Boolean> = replacementFlag(REPLACEMENT_CELLULAR)
    val replacementWifi: Flow<Boolean> = replacementFlag(REPLACEMENT_WIFI)
    val replacementBattery: Flow<Boolean> = replacementFlag(REPLACEMENT_BATTERY)
    val replacementConnectivity: Flow<Boolean> = replacementFlag(REPLACEMENT_CONNECTIVITY)

    private fun replacementFlag(key: Preferences.Key<Boolean>): Flow<Boolean> =
        context.statusBarDataStore.data.map { prefs -> prefs[key] ?: true }

    suspend fun setHideNotificationIcons(hide: Boolean) = context.statusBarDataStore.edit { prefs ->
        prefs[HIDE_NOTIFICATION_ICONS] = hide
    }

    suspend fun setHideSystemInfo(hide: Boolean) = context.statusBarDataStore.edit { prefs ->
        prefs[HIDE_SYSTEM_INFO] = hide
    }

    suspend fun setHideClock(hide: Boolean) = context.statusBarDataStore.edit { prefs ->
        prefs[HIDE_CLOCK] = hide
    }

    suspend fun setSilenceAlerts(silence: Boolean) = context.statusBarDataStore.edit { prefs ->
        prefs[SILENCE_ALERTS] = silence
    }

    suspend fun setReplacementEnabled(enabled: Boolean) = context.statusBarDataStore.edit { prefs ->
        prefs[REPLACEMENT_ENABLED] = enabled
    }

    suspend fun setReplacementNotifications(enabled: Boolean) = setReplacementFlag(REPLACEMENT_NOTIFICATIONS, enabled)
    suspend fun setReplacementCellular(enabled: Boolean) = setReplacementFlag(REPLACEMENT_CELLULAR, enabled)
    suspend fun setReplacementWifi(enabled: Boolean) = setReplacementFlag(REPLACEMENT_WIFI, enabled)
    suspend fun setReplacementBattery(enabled: Boolean) = setReplacementFlag(REPLACEMENT_BATTERY, enabled)
    suspend fun setReplacementConnectivity(enabled: Boolean) = setReplacementFlag(REPLACEMENT_CONNECTIVITY, enabled)

    private suspend fun setReplacementFlag(key: Preferences.Key<Boolean>, enabled: Boolean) =
        context.statusBarDataStore.edit { prefs -> prefs[key] = enabled }

    private companion object {
        val HIDE_NOTIFICATION_ICONS = booleanPreferencesKey("hide_notification_icons")
        val HIDE_SYSTEM_INFO = booleanPreferencesKey("hide_system_info")
        val HIDE_CLOCK = booleanPreferencesKey("hide_clock")
        val SILENCE_ALERTS = booleanPreferencesKey("silence_alerts")
        val REPLACEMENT_ENABLED = booleanPreferencesKey("replacement_enabled")
        val REPLACEMENT_NOTIFICATIONS = booleanPreferencesKey("replacement_notifications")
        val REPLACEMENT_CELLULAR = booleanPreferencesKey("replacement_cellular")
        val REPLACEMENT_WIFI = booleanPreferencesKey("replacement_wifi")
        val REPLACEMENT_BATTERY = booleanPreferencesKey("replacement_battery")
        val REPLACEMENT_CONNECTIVITY = booleanPreferencesKey("replacement_connectivity")
    }

    /**
     * Exports the status-bar settings in a JSON string
     * { hideNotificationIcons: boolean, hideSystemInfo: boolean, hideClock: boolean,
     *   silenceAlerts: boolean }
     */
    override suspend fun toJson(): String {
        val hideIcons = hideNotificationIcons.first()
        val hideSystemInfo = hideSystemInfo.first()
        val hideClock = hideClock.first()
        val silence = silenceAlerts.first()
        val replacement = replacementEnabled.first()
        val replacementNotifications = replacementNotifications.first()
        val replacementCellular = replacementCellular.first()
        val replacementWifi = replacementWifi.first()
        val replacementBattery = replacementBattery.first()
        val replacementConnectivity = replacementConnectivity.first()
        return JSONObject().apply {
            put("hideNotificationIcons", hideIcons)
            put("hideSystemInfo", hideSystemInfo)
            put("hideClock", hideClock)
            put("silenceAlerts", silence)
            put("replacementEnabled", replacement)
            put("replacementNotifications", replacementNotifications)
            put("replacementCellular", replacementCellular)
            put("replacementWifi", replacementWifi)
            put("replacementBattery", replacementBattery)
            put("replacementConnectivity", replacementConnectivity)
        }.toString()
    }

    /**
     * Applies { hideNotificationIcons: boolean, hideSystemInfo: boolean, hideClock: boolean,
     * silenceAlerts: boolean } exported by [toJson]. Each missing field leaves its setting
     * untouched — importing a document from a build without this section shouldn't silently flip
     * any flag.
     */
    override suspend fun fromJson(json: String) {
        val obj = JSONObject(json)
        if (obj.has("hideNotificationIcons")) {
            setHideNotificationIcons(obj.optBoolean("hideNotificationIcons", false))
        }
        if (obj.has("hideSystemInfo")) {
            setHideSystemInfo(obj.optBoolean("hideSystemInfo", false))
        }
        if (obj.has("hideClock")) {
            setHideClock(obj.optBoolean("hideClock", false))
        }
        if (obj.has("silenceAlerts")) {
            setSilenceAlerts(obj.optBoolean("silenceAlerts", false))
        }
        if (obj.has("replacementEnabled")) setReplacementEnabled(obj.optBoolean("replacementEnabled", false))
        if (obj.has("replacementNotifications")) setReplacementNotifications(obj.optBoolean("replacementNotifications", true))
        if (obj.has("replacementCellular")) setReplacementCellular(obj.optBoolean("replacementCellular", true))
        if (obj.has("replacementWifi")) setReplacementWifi(obj.optBoolean("replacementWifi", true))
        if (obj.has("replacementBattery")) setReplacementBattery(obj.optBoolean("replacementBattery", true))
        if (obj.has("replacementConnectivity")) setReplacementConnectivity(obj.optBoolean("replacementConnectivity", true))
    }
}
