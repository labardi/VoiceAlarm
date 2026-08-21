package com.example.voicealarm

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val requestCode: Int = 0,
    val minute: Int,
    val hour: Int,
    val day: Int,
    val month: Int,
    val year: Int,
    val message: String,
    val isActive: Boolean = true
)
