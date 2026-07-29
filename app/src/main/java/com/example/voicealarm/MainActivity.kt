package com.example.voicealarm

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.provider.AlarmClock

class MainActivity : AppCompatActivity() {

    // Переменная для самой языковой модели (словари)
    private var model: org.vosk.Model? = null
    // Переменная для движка распознавания (он слушает и переводит в текст)
    private var speechService: org.vosk.android.SpeechService? = null

    private var accumulatedText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        val recordButton = findViewById<android.widget.Button>(R.id.btnRecord)
        val confirmButton = findViewById<android.widget.Button>(R.id.btnConfirm)
        val resultText = findViewById<android.widget.EditText>(R.id.tvResult)
        val editText = findViewById<android.widget.TextView>(R.id.tvEdit)

        // Изначально выключим кнопку, пока модель не загрузится (в шаге 2 мы ее включим)
        recordButton.isEnabled = false

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
                    confirmButton.visibility = android.view.View.VISIBLE
                    editText.visibility = android.view.View.VISIBLE

                } else {
                    // Создаем новый распознаватель на основе нашей модели.
                    // 16000.0f — это частота дискретизации звука.
                    val recognizer = org.vosk.Recognizer(model, 16000.0f)

                    accumulatedText = ""

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

        confirmButton.setOnClickListener {
            val finalText = resultText.text.toString()
            if (finalText.isNotEmpty()) {
                val result = parseVoiceCommand(finalText)
                val day = result.alarmDay
                val month = result.alarmMonth
                val year = result.alarmYear
                val hour = result.alarmHour
                val minute = result.alarmMinute
                val message = result.message
                val error = result.error
                println("День: $day")
                println("Месяц: $month")
                println("Год: $year")
                println("Часы: $hour")
                println("Минуты: $minute")
                println("Сообщение: $message")
                println("Ошибка: $error")

                val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                    putExtra(AlarmClock.EXTRA_HOUR, hour)
                    putExtra(AlarmClock.EXTRA_MINUTES, minute)
                    putExtra(AlarmClock.EXTRA_MESSAGE, message)
                    putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                }
                startActivity(intent)
            }

            accumulatedText = ""
            confirmButton.visibility = android.view.View.GONE
            editText.visibility = android.view.View.GONE
            resultText.isEnabled = false
        }

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
