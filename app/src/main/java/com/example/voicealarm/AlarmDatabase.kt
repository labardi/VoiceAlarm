package com.example.voicealarm

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AlarmEntity::class], version = 3)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile
        private var INSTANCE: AlarmDatabase? = null

        fun getDatabase(context: Context): AlarmDatabase {
            return INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AlarmDatabase::class.java,
                "alarm_database"
            ).build().also { INSTANCE = it }
        }
    }
}