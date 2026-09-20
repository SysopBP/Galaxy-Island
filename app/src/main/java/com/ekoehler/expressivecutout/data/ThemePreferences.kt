package com.ekoehler.expressivecutout.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ekoehler.expressivecutout.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

/** Backing store for the app-level theme settings. */
private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

/** Persists the selected [AppTheme], defaulting to [AppTheme.SYSTEM]. */
class ThemePreferences(private val context: Context) : JsonSerializable {
    val theme: Flow<AppTheme> = context.appDataStore.data.map { prefs ->
        prefs[THEME]?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() } ?: AppTheme.SYSTEM
    }

    /** Opaque custom seed, or zero to follow the system wallpaper palette. */
    val accent: Flow<Long> = context.appDataStore.data.map { it[ACCENT] ?: 0L }

    /** Normalizes custom colors to opaque ARGB; zero preserves dynamic colors. */
    suspend fun setAccent(argb: Long) = context.appDataStore.edit {
        it[ACCENT] = if (argb == 0L) 0L else (argb and 0xFFFFFF) or 0xFF000000
    }

    /** Sets the theme using AppTheme class */
    suspend fun setTheme(theme: AppTheme) = context.appDataStore.edit { prefs ->
        prefs[THEME] = theme.name
    }

    /** Sets a theme by name */
    suspend fun setThemeByName(name: String) {
        val theme = AppTheme.entries.first { it.name == name }
        this.setTheme(theme)
    }

    private companion object {
        val ACCENT = longPreferencesKey("app_accent")
        val THEME = stringPreferencesKey("app_theme")
    }

    /**
     * Exports settings to JSON { theme: string }
     */
    override suspend fun toJson(): String {
        val t = theme.first()
        return JSONObject().apply {
            put("theme", t.name)
            put("accent", accent.first())
        }.toString()
    }

    /** Applies { theme: string } exported by [toJson]; an unknown name is ignored. */
    override suspend fun fromJson(json: String) {
        val name = JSONObject(json).optString("theme").takeIf { it.isNotEmpty() } ?: return
        val theme = runCatching { AppTheme.valueOf(name) }.getOrNull() ?: return
        setTheme(theme)
        val data = JSONObject(json)
        if (data.has("accent")) setAccent(data.optLong("accent", 0L))
    }
}
