package com.qalqon.security.monitor

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build

class DownloadCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (downloadId == -1L) return

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor: Cursor = manager.query(query) ?: return
        cursor.use {
            if (!it.moveToFirst()) return
            val uriColumn = it.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
            if (uriColumn == -1) return
            val uriRaw = it.getString(uriColumn) ?: return
            val parsed = Uri.parse(uriRaw)
            val path = when (parsed.scheme) {
                "file" -> parsed.path
                else -> {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        val legacyColumn = it.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME)
                        if (legacyColumn >= 0) it.getString(legacyColumn) else null
                    } else {
                        null
                    }
                }
            } ?: return
            if (path.endsWith(".apk", ignoreCase = true)) {
                ApkMonitorService.start(context, path)
            }
        }
    }
}
