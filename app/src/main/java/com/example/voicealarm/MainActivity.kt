package com.example.voicealarm

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.app.AlarmManager
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Calendar
import android.Manifest
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // Переменная для самой языковой модели (словари)
    private var model: org.vosk.Model? = null
    // Переменная для движка распознавания (он слушает и переводит в текст)
    private var speechService: org.vosk.android.SpeechService? = null

    private var accumulatedText = ""

    private lateinit var db: AlarmDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        db = AlarmDatabase.getDatabase(this)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        val recordButton = findViewById<android.widget.Button>(R.id.btnRecord)
        val confirmButton = findViewById<android.widget.Button>(R.id.btnConfirm)
        val alarmListButton = findViewById<android.widget.Button>(R.id.btnAlarmsList)
        val resultText = findViewById<android.widget.EditText>(R.id.tvResult)
        val editText = findViewById<android.widget.TextView>(R.id.tvEdit)

        // Изначально выключим кнопку, пока модель не загрузится (в шаге 2 мы ее включим)
        recordButton.isEnabled = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            )
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    2
                )
            }
        }

        recordButton.setOnClickListener {
            // Проверяем права
            val permission = androidx.core.content.ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.RECORD_AUDIO
            )

            if (permission != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(
                    this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 1
                )
            } else {
                // ПРАВА ЕСТЬ. НАЧИНАЕМ СЛУШАТЬ.

                // Если мы уже записываем — останавливаем
                if (speechService != null) {
                    speechService?.stop()
                    speechService = null
                    recordButton.text = "Перезаписать"

                    resultText.isEnabled = true
                    alarmListButton.isEnabled = true
                    confirmButton.visibility = android.view.View.VISIBLE
                    editText.visibility = android.view.View.VISIBLE

                } else {
                    // Создаем новый распознаватель на основе нашей модели.
                    // 16000.0f — это частота дискретизации звука.
                    val recognizer = org.vosk.Recognizer(model, 16000.0f)

                    accumulatedText = ""

                    editText.visibility = android.view.View.GONE
                    editText.text = getString(R.string.editAccess)
                    confirmButton.visibility = android.view.View.GONE
                    resultText.isEnabled = false
                    alarmListButton.isEnabled = false

                    // Запускаем службу записи (микрофон)
                    speechService = org.vosk.android.SpeechService(recognizer, 16000.0f)

                    // Заставляем службу слушать и возвращать результаты
                    speechService?.startListening(object : org.vosk.android.RecognitionListener {
                        override fun onPartialResult(hypothesis: String) {} // Игнорируем промежуточные догадки

                        override fun onResult(hypothesis: String) {
                            accumulatedText += " "
                            accumulatedText += org.json.JSONObject(hypothesis).getString("text")
                            resultText.setText(accumulatedText)
                        }

                        override fun onFinalResult(hypothesis: String) {
                            accumulatedText += " "
                            accumulatedText += org.json.JSONObject(hypothesis).getString("text")
                            resultText.setText(accumulatedText)
                        }

                        override fun onError(exception: Exception) {
                            resultText.setText("Ошибка микрофона")
                        }
                        override fun onTimeout() {}
                    })

                    recordButton.text = "Остановить"
                    resultText.setText("Слушаю...")
                }
            }
        }

        alarmListButton.setOnClickListener {
            val intent = Intent(this, AlarmListActivity::class.java)
            startActivity(intent)
        }

        confirmButton.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val notifPermission = ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                )
                if (notifPermission != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this,
                        "Без разрешения на уведомления будильник не сработает",
                        Toast.LENGTH_LONG
                    ).show()
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = android.net.Uri.fromParts("package", packageName, null)
                    startActivity(intent)
                    return@setOnClickListener // выходим, не ставим будильник
                }
            }

            val finalText = resultText.text.toString()

            val result = parseVoiceCommand(finalText)
            val day = result.alarmDay
            val month = result.alarmMonth
            val year = result.alarmYear
            val hour = result.alarmHour
            val minute = result.alarmMinute
            val message = result.message
            val error = result.error

            if (error != "") {
                editText.text = error
                editText.visibility = android.view.View.VISIBLE
            } else {

                if (ensureExactAlarmPermission(this)) {
                    val draftAlarm = AlarmEntity(
                        minute = minute,
                        hour = hour,
                        day = day,
                        month = month,
                        year = year,
                        message = message
                    )

                    lifecycleScope.launch {
                        val generatedId = db.alarmDao().addAlarm(draftAlarm)

                        val savedAlarm = draftAlarm.copy(
                            requestCode = generatedId.toInt()
                        )

                        scheduleAlarm(this@MainActivity, savedAlarm)

                        editText.text =
                            "Будильник на ${"%02d".format(hour)}:${"%02d".format(minute)} на ${
                                "%02d".format(day)
                            }.${"%02d".format(month)}.${"%02d".format(year)} с сообщением: $message поставлен!"
                        editText.visibility = android.view.View.VISIBLE
                        resultText.setText("Модель готова! Можно говорить.")
                        confirmButton.visibility = android.view.View.GONE
                        resultText.isEnabled = false
                        recordButton.text = getString(R.string.startRecording)
                    }
                }
            }
        }
        accumulatedText = ""
        confirmButton.visibility = android.view.View.GONE
        editText.visibility = android.view.View.GONE
        resultText.isEnabled = false

        // Распаковываем модель из папки assets
        org.vosk.android.StorageService.unpack(
        this,
        "vosk-model-small-ru-0.22",
        "model",
        // Коллбэк для успеха (completeCallback)
        { model ->
            this.model = model
            resultText.setText("Модель готова! Можно говорить.")
            recordButton.isEnabled = true
        },
        // Коллбэк для ошибки (errorCallback)
        { exception ->
            resultText.setText("Ошибка загрузки модели: ${exception.message}")
        }
        )
    }
}
