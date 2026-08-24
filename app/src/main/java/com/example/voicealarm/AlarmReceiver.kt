package com.example.voicealarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "VoiceAlarm::AlarmWakeLock"
        )
        wakeLock.acquire(10_000L) // держим максимум 10 секунд

        val channel = NotificationChannel(
            "alarm_channel",
            "Будильники",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // сюда передаём extras из входящего intent
            putExtra("message", intent.getStringExtra("message"))
            putExtra("minute", intent.getIntExtra("minute", 0))
            putExtra("hour", intent.getIntExtra("hour", 0))
            putExtra("day", intent.getIntExtra("day", 0))
            putExtra("month", intent.getIntExtra("month", 0))
            putExtra("year", intent.getIntExtra("year", 0))
        }
        val requestCode = intent.getIntExtra("requestCode", 0)

        val db = AlarmDatabase.getDatabase(context.applicationContext)

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.alarmDao().deleteAlarmByRequestCode(requestCode)
            } finally {
                pendingResult.finish()
            }
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, requestCode, fullScreenIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "alarm_channel")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Будильник")
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        notificationManager.notify(requestCode , notification)


        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("message", intent.getStringExtra("message"))
            putExtra("minute", intent.getIntExtra("minute", 0))
            putExtra("hour", intent.getIntExtra("hour", 0))
            putExtra("day", intent.getIntExtra("day", 0))
            putExtra("month", intent.getIntExtra("month", 0))
            putExtra("year", intent.getIntExtra("year", 0))
        }
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(alarmIntent)
    }
}