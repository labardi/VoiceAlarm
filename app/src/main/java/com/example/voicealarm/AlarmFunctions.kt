package com.example.voicealarm

import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.util.Calendar
import java.time.LocalDateTime

fun hasExactAlarmPermission(context: Context): Boolean {
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
    } else true
}

fun hasNotificationPermission(context: Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
}

fun hasFullScreenIntentPermission(context: Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
            context.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()
}

fun exactAlarmPermissionIntent(context: Context): Intent {
    return Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
}

fun fullScreenIntentPermissionIntent(context: Context): Intent {
    return Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
}

fun ensureExactAlarmPermission(activity: Activity): Boolean {
    if (!hasExactAlarmPermission(activity)) {
        Toast.makeText(
            activity,
            "Для точных будильников нужно разрешение. После выдачи вернитесь и снова нажмите «Подтвердить»",
            Toast.LENGTH_LONG
        ).show()
        activity.startActivity(exactAlarmPermissionIntent(activity))
        return false
    }
    return true
}

fun ensureFullScreenIntentPermission(activity: Activity): Boolean {
    if (!hasFullScreenIntentPermission(activity)) {
        Toast.makeText(
            activity,
            R.string.allow_full_screen_alarms,
            Toast.LENGTH_LONG
        ).show()
        activity.startActivity(fullScreenIntentPermissionIntent(activity))
        return false
    }
    return true
}

fun ensureAlarmPermissions(activity: Activity): Boolean {
    if (!hasNotificationPermission(activity)) {
        Toast.makeText(
            activity,
            R.string.allow_notifications_to_save_alarm,
            Toast.LENGTH_LONG
        ).show()
        activity.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activity.packageName, null)
            }
        )
        return false
    }

    return ensureExactAlarmPermission(activity) &&
            ensureFullScreenIntentPermission(activity)
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




