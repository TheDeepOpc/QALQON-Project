package com.qalqon.security

import android.app.Application
import com.qalqon.security.data.local.QalqonDatabase
import com.qalqon.security.data.repo.SecurityRepository
import com.qalqon.security.worker.RescanScheduler

class QalqonApp : Application() {
    val repository: SecurityRepository by lazy {
        SecurityRepository(QalqonDatabase.getInstance(this).securityEventDao())
    }

    override fun onCreate() {
        super.onCreate()
        RescanScheduler.schedule(this)
    }
}
