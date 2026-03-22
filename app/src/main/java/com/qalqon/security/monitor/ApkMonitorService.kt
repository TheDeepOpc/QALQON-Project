package com.qalqon.security.monitor

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.qalqon.security.QalqonApp
import com.qalqon.security.data.local.SecurityEvent
import com.qalqon.security.notification.NotificationHelper
import com.qalqon.security.settings.MonitoringSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File

class ApkMonitorService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var monitorManager: FileMonitorManager
    private lateinit var seenApkStore: SeenApkStore
    private lateinit var monitoringSettings: MonitoringSettings

    override fun onCreate() {
        super.onCreate()
        monitoringSettings = MonitoringSettings(this)
        if (!monitoringSettings.isMonitoringEnabled()) {
            stopSelf()
            return
        }
        seenApkStore = SeenApkStore(this)
        NotificationHelper.ensureChannels(this)
        startForeground(NotificationHelper.MONITOR_NOTIFICATION_ID, NotificationHelper.monitorNotification(this))
        monitorManager = FileMonitorManager(::handleApkDetected)
        monitorManager.start()
        scope.launch {
            FileMonitorTargets.targets().forEach { dir ->
                if (!dir.exists() || !dir.isDirectory) return@forEach
                dir.listFiles()
                    ?.asSequence()
                    ?.filter { it.isFile && it.name.endsWith(".apk", ignoreCase = true) }
                    ?.forEach { apk -> handleApkDetected(apk.absolutePath, "INITIAL_SCAN") }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val downloadedPath = intent?.getStringExtra(EXTRA_DOWNLOADED_PATH)
        if (!downloadedPath.isNullOrBlank()) {
            handleApkDetected(downloadedPath, "DOWNLOAD_BROADCAST")
        }
        return START_STICKY
    }

    override fun onDestroy() {
        if (::monitorManager.isInitialized) {
            monitorManager.stop()
        }
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun handleApkDetected(path: String, signal: String) {
        if (!path.endsWith(".apk", ignoreCase = true)) return
        val apkFile = File(path)
        if (!apkFile.exists()) return
        if (!seenApkStore.shouldProcess(apkFile.absolutePath, apkFile.lastModified())) return
        seenApkStore.markProcessed(apkFile.absolutePath, apkFile.lastModified())

        scope.launch {
            val verdict = ApkTrustAnalyzer.analyze(this@ApkMonitorService, apkFile.absolutePath)
            val event = SecurityEvent(
                eventType = "APK_DETECTED",
                filePath = apkFile.absolutePath,
                sourceLabel = verdict.sourceLabel,
                severity = if (verdict.trusted) "SAFE" else "DANGER",
                message = "APK detected via $signal from ${verdict.sourceLabel}. ${verdict.reason}",
            )
            val app = application as QalqonApp
            val eventId = app.repository.logEvent(event)
            if (!verdict.trusted && monitoringSettings.isAlertsEnabled()) {
                showSecurityAlert(eventId, apkFile.absolutePath, verdict.sourceLabel)
            }
        }
    }

    private fun showSecurityAlert(eventId: Long, apkPath: String, sourceLabel: String) {
        NotificationHelper.showRiskNotification(this, eventId, apkPath, sourceLabel)
    }

    companion object {
        const val EXTRA_DOWNLOADED_PATH = "extra_downloaded_path"

        fun start(context: android.content.Context, downloadedPath: String? = null) {
            val settings = MonitoringSettings(context)
            if (!settings.isMonitoringEnabled()) return
            val intent = Intent(context, ApkMonitorService::class.java).apply {
                downloadedPath?.let { putExtra(EXTRA_DOWNLOADED_PATH, it) }
            }
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }
    }
}
