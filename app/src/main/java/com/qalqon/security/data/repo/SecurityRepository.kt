package com.qalqon.security.data.repo

import com.qalqon.security.data.local.SecurityEvent
import com.qalqon.security.data.local.SecurityEventDao
import kotlinx.coroutines.flow.Flow

class SecurityRepository(private val dao: SecurityEventDao) {
    fun observeEvents(): Flow<List<SecurityEvent>> = dao.observeAll()

    suspend fun logEvent(event: SecurityEvent): Long = dao.insert(event)

    suspend fun markAction(id: Long, action: String) {
        dao.updateAction(id, action)
    }
}
