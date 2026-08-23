package com.example.voicealarm

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Insert
    suspend fun addAlarm(alarm: AlarmEntity): Long

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)

    @Query("DELETE FROM alarms WHERE requestCode = :requestCode")
    suspend fun deleteAlarmByRequestCode(requestCode: Int)

    @Query("SELECT * FROM alarms ORDER BY year, month, day, hour, minute")
    fun getAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms ORDER BY year, month, day, hour, minute")
    suspend fun getAlarmsOnce(): List<AlarmEntity>
}