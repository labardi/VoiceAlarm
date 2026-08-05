package com.example.voicealarm

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Insert
    suspend fun addAlarm(alarm: AlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)

    @Query("SELECT * FROM alarms ORDER BY year, month, day, hour, minute")
    fun getAlarms(): Flow<List<AlarmEntity>>
}