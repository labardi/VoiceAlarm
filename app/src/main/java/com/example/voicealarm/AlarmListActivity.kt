package com.example.voicealarm

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class AlarmListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alarm_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val db = AlarmDatabase.getDatabase(this)

        val backButton = findViewById<android.widget.Button>(R.id.btnBack)
        backButton.setOnClickListener { finish() }

        fun onSwitchClick(alarm: AlarmEntity): Unit {
            val newState = !alarm.isActive
            if (isAlarmInFuture(alarm)) {
                if (!newState) {
                    cancelAlarm(this, alarm)
                    val updatedAlarm = alarm.copy(isActive = newState)
                    lifecycleScope.launch {
                        db.alarmDao().updateAlarm(updatedAlarm)
                    }
                } else {
                    if (ensureExactAlarmPermission(this)) {
                        scheduleAlarm(this, alarm)
                        val updatedAlarm = alarm.copy(isActive = newState)
                        lifecycleScope.launch {
                            db.alarmDao().updateAlarm(updatedAlarm)
                        }
                    }
                }
            } else {
                cancelAlarm(this, alarm)
                lifecycleScope.launch {
                    db.alarmDao().deleteAlarm(alarm)
                }
            }
        }

        fun onAlarmClick(alarm: AlarmEntity) {
            val intent = Intent(this, AlarmEditActivity::class.java).apply {
                putExtra(AlarmEditActivity.EXTRA_ALARM_REQUEST_CODE, alarm.requestCode)
            }
            startActivity(intent)
        }

        val myAdapter = AlarmAdapter(
            onSwitchClick = ::onSwitchClick,
            onAlarmClick = ::onAlarmClick
        )
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = myAdapter

        lifecycleScope.launch {
            val alarms = db.alarmDao().getAlarmsOnce()
            for (oneAlarm in alarms) {
                if (!isAlarmInFuture(oneAlarm)) {
                    cancelAlarm(this@AlarmListActivity, oneAlarm)
                    db.alarmDao().deleteAlarm(oneAlarm)
                }
            }
            db.alarmDao().getAlarms().collect { updatedAlarms ->
                myAdapter.submitList(updatedAlarms)
            }
        }
    }
}
