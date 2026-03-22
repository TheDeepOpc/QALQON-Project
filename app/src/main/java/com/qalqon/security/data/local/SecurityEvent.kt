package com.qalqon.security.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_events")
data class SecurityEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val eventType: String,
    val filePath: String? = null,
    val sourceLabel: String,
    val severity: String,
    val message: String,
    val userAction: String = "NONE",
    val createdAt: Long = System.currentTimeMillis(),
)
