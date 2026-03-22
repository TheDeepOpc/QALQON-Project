package com.qalqon.security.settings

import android.content.Context

class MonitoringSettings(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isMonitoringEnabled(): Boolean = prefs.getBoolean(KEY_MONITORING_ENABLED, true)
    fun isAlertsEnabled(): Boolean = prefs.getBoolean(KEY_ALERTS_ENABLED, true)

    fun setMonitoringEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MONITORING_ENABLED, enabled).apply()
    }

    fun setAlertsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ALERTS_ENABLED, enabled).apply()
    }

    companion object {
        const val PREFS_NAME = "qalqon_monitor_settings"
        const val KEY_MONITORING_ENABLED = "monitoring_enabled"
        const val KEY_ALERTS_ENABLED = "alerts_enabled"
    }
}
