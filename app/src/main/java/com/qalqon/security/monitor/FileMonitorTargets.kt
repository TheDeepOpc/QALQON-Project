package com.qalqon.security.monitor

import android.os.Environment
import java.io.File

object FileMonitorTargets {
    fun targets(): List<File> {
        val root = Environment.getExternalStorageDirectory()
        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return listOf(
            downloads,
            File(root, "Download"),
            File(root, "Telegram"),
            File(root, "Telegram/Telegram Documents"),
            File(root, "Android/data/org.telegram.messenger/files/Telegram"),
            File(root, "Android/data/org.telegram.messenger/files/Download"),
            File(root, "Documents"),
            File(root, "DCIM"),
        ).distinctBy { it.absolutePath }
    }
}
