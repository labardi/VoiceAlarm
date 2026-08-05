package com.example.voicealarm

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey val requestCode: Int,
    val minute: Int,
    val hour: Int,
    val day: Int,
    val month: Int,
    val year: Int,
    val message: String
)
