package com.qalqon.security.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SecurityEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: SecurityEvent): Long

    @Query("SELECT * FROM security_events ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SecurityEvent>>

    @Query("UPDATE security_events SET userAction = :userAction WHERE id = :id")
    suspend fun updateAction(id: Long, userAction: String)
}
