package com.qalqon.security.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.qalqon.security.monitor.ApkMonitorService
import com.qalqon.security.monitor.FileMonitorTargets
import java.io.File

class ApkRescanWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        FileMonitorTargets.targets().forEach { dir ->
            if (!dir.exists() || !dir.isDirectory) return@forEach
            dir.listFiles()
                ?.asSequence()
                ?.filter { it.isFile && it.name.endsWith(".apk", ignoreCase = true) }
                ?.forEach { apk ->
                    ApkMonitorService.start(applicationContext, apk.absolutePath)
                }
        }
        return Result.success()
    }
}
