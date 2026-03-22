package com.qalqon.security.monitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.qalqon.security.settings.MonitoringSettings
import com.qalqon.security.worker.RescanScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        RescanScheduler.schedule(context)
        if (!MonitoringSettings(context).isMonitoringEnabled()) return
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED, -> ApkMonitorService.start(context)
        }
    }
}
