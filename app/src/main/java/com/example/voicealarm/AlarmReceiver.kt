package com.example.voicealarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        android.util.Log.d("AlarmReceiver", "Сработал! Время: ${System.currentTimeMillis()}")
    }
}