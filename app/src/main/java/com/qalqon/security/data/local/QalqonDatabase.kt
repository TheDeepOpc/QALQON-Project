package com.qalqon.security.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SecurityEvent::class], version = 1, exportSchema = false)
abstract class QalqonDatabase : RoomDatabase() {
    abstract fun securityEventDao(): SecurityEventDao

    companion object {
        @Volatile
        private var INSTANCE: QalqonDatabase? = null

        fun getInstance(context: Context): QalqonDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    QalqonDatabase::class.java,
                    "qalqon.db",
                ).build().also { INSTANCE = it }
            }
        }
    }
}
