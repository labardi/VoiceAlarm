package com.example.voicealarm

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import java.util.Calendar
import java.time.LocalDateTime

fun hasExactAlarmPermission(context: Context): Boolean {
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
    } else true
}

fun ensureExactAlarmPermission(activity: Activity): Boolean {
    if (!hasExactAlarmPermission(activity)) {
        Toast.makeText(
            activity,
            "Для точных будильников нужно разрешение. После выдачи вернитесь и снова нажмите «Подтвердить»",
            Toast.LENGTH_LONG
        ).show()
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        activity.startActivity(intent)
        return false
    }
    return true
}

// Важно, чтобы разрешение было проверено до вызова функции
fun scheduleAlarm(context: Context, alarm: AlarmEntity): Unit {

    val alarmManager = context.getSystemService(AlarmManager::class.java)

    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, alarm.year)
        set(Calendar.MONTH, alarm.month - 1) // ← важно! В Calendar январь = 0
        set(Calendar.DAY_OF_MONTH, alarm.day)
        set(Calendar.HOUR_OF_DAY, alarm.hour)
        set(Calendar.MINUTE, alarm.minute)
        set(Calendar.SECOND, 0)
    }
    val triggerTimeMillis = calendar.timeInMillis

    val intent = Intent(context, AlarmReceiver ::class.java).apply {
        putExtra("message", alarm.message)
        putExtra("minute", alarm.minute)
        putExtra("hour", alarm.hour)
        putExtra("day", alarm.day)
        putExtra("month", alarm.month)
        putExtra("year", alarm.year)
        putExtra("requestCode", alarm.requestCode)
    }

    val pendingIntent = PendingIntent.getBroadcast(context, alarm.requestCode, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    alarmManager.setAlarmClock(
        AlarmManager.AlarmClockInfo(triggerTimeMillis, pendingIntent),
        pendingIntent
    )
}

fun cancelAlarm(context: Context, alarm: AlarmEntity): Unit {

    val alarmManager = context.getSystemService(AlarmManager::class.java)

    val intent = Intent(context, AlarmReceiver ::class.java)

    val pendingIntent = PendingIntent.getBroadcast(context, alarm.requestCode, intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)

    if (pendingIntent != null) {
        alarmManager.cancel(pendingIntent)
    }
}

fun isAlarmInFuture(alarm: AlarmEntity): Boolean {
    val deadline = LocalDateTime.of(
        alarm.year,
        alarm.month,
        alarm.day,
        alarm.hour,
        alarm.minute
    )
    return deadline.isAfter(LocalDateTime.now())
}




