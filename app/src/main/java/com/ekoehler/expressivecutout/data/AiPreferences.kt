package com.ekoehler.expressivecutout.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.aiDataStore by preferencesDataStore(name = "galaxy_ai_prefs")

data class AiSettings(
    val enabled: Boolean = false,
    val notificationSummaries: Boolean = true,
    val suggestedReplies: Boolean = true,
)

/** Opt-in AI settings. Disabled by default; credentials are intentionally not stored here. */
class AiPreferences(private val context: Context) {
    val settings: Flow<AiSettings> = context.aiDataStore.data.map { prefs ->
        AiSettings(
            enabled = prefs[ENABLED] ?: false,
            notificationSummaries = prefs[SUMMARIES] ?: true,
            suggestedReplies = prefs[REPLIES] ?: true,
        )
    }

    suspend fun setEnabled(value: Boolean) = context.aiDataStore.edit { it[ENABLED] = value }
    suspend fun setNotificationSummaries(value: Boolean) = context.aiDataStore.edit { it[SUMMARIES] = value }
    suspend fun setSuggestedReplies(value: Boolean) = context.aiDataStore.edit { it[REPLIES] = value }

    private companion object {
        val ENABLED = booleanPreferencesKey("enabled")
        val SUMMARIES = booleanPreferencesKey("notification_summaries")
        val REPLIES = booleanPreferencesKey("suggested_replies")
    }
}
