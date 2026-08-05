package com.example.voicealarm

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
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

        // val myAdapter = AlarmAdapter()
        // val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        // recyclerView.adapter = myAdapter

        lifecycleScope.launch {
            // Подключаемся к "трубе"
            db.alarmDao().getAlarms().collect { updatedAlarms ->

                // Этот блок кода сработает сразу при входе на экран,
                // А ЗАТЕМ будет срабатывать АВТОМАТИЧЕСКИ каждый раз,
                // когда вы добавите, удалите или отредактируете будильник.

                // Здесь вы просто передаете свежий список updatedAlarms в ваш интерфейс
                // Например, в RecyclerView адаптер:
                // myAdapter.submitList(updatedAlarms)
            }
        }
    }
}