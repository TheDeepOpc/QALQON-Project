package com.qalqon.security.monitor

import android.content.Context

class SeenApkStore(context: Context) {
    private val prefs = context.getSharedPreferences("qalqon_seen_apk", Context.MODE_PRIVATE)

    fun shouldProcess(path: String, lastModified: Long): Boolean {
        val key = path.lowercase()
        val known = prefs.getLong(key, -1L)
        return known != lastModified
    }

    fun markProcessed(path: String, lastModified: Long) {
        prefs.edit().putLong(path.lowercase(), lastModified).apply()
    }
}
