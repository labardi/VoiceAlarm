package com.example.voicealarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AlarmDatabase.getDatabase(context.applicationContext)
                val alarms = db.alarmDao().getAlarmsOnce()
                val canScheduleExactAlarms = hasExactAlarmPermission(context)
                for (alarm in alarms) {
                    if (!isAlarmInFuture(alarm)) {
                        db.alarmDao().deleteAlarm(alarm)
                    } else if (alarm.isActive && canScheduleExactAlarms) {
                        scheduleAlarm(context, alarm)
                    }
                }

            } finally {
                pendingResult.finish()
            }
        }
    }
}