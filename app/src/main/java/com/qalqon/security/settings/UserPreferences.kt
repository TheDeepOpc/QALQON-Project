package com.qalqon.security.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "qalqon_settings")

class UserPreferences(private val context: Context) {
    val darkModeEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_DARK_MODE] ?: true
    }
    val monitoringEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_MONITORING_ENABLED] ?: true
    }
    val alertsEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_ALERTS_ENABLED] ?: true
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK_MODE] = enabled }
    }

    suspend fun setMonitoringEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_MONITORING_ENABLED] = enabled }
        MonitoringSettings(context).setMonitoringEnabled(enabled)
    }

    suspend fun setAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_ALERTS_ENABLED] = enabled }
        MonitoringSettings(context).setAlertsEnabled(enabled)
    }

    companion object {
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        private val KEY_MONITORING_ENABLED = booleanPreferencesKey("monitoring_enabled")
        private val KEY_ALERTS_ENABLED = booleanPreferencesKey("alerts_enabled")
    }
}
