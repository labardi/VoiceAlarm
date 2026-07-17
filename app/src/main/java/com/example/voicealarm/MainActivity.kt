package com.example.voicealarm

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    // Переменная для самой языковой модели (словари)
    private var model: org.vosk.Model? = null
    // Переменная для движка распознавания (он слушает и переводит в текст)
    private var speechService: org.vosk.android.SpeechService? = null

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
        val resultText = findViewById<android.widget.TextView>(R.id.tvResult)

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
                    recordButton.text = "Записать"
                } else {
                    // Создаем новый распознаватель на основе нашей модели.
                    // 16000.0f — это частота дискретизации звука.
                    val recognizer = org.vosk.Recognizer(model, 16000.0f)

                    // Запускаем службу записи (микрофон)
                    speechService = org.vosk.android.SpeechService(recognizer, 16000.0f)

                    // Заставляем службу слушать и возвращать результаты
                    speechService?.startListening(object : org.vosk.android.RecognitionListener {
                        override fun onPartialResult(hypothesis: String) {} // Игнорируем промежуточные догадки

                        override fun onResult(hypothesis: String) {
                            // Vosk возвращает JSON, например: {"text": "привет"}
                            // Для начала просто выведем сырой ответ на экран
                            resultText.text = hypothesis
                        }

                        override fun onFinalResult(hypothesis: String) {
                            resultText.text = hypothesis
                        }

                        override fun onError(exception: Exception) {
                            resultText.text = "Ошибка микрофона"
                        }
                        override fun onTimeout() {}
                    })

                    recordButton.text = "Остановить"
                    resultText.text = "Слушаю..."
                }
            }
        }

        // Распаковываем модель из папки assets
        org.vosk.android.StorageService.unpack(
            this,
            "vosk-model-small-ru-0.22",
            "model",
            // Коллбэк для успеха (completeCallback)
            { model ->
                this.model = model
                resultText.text = "Модель готова! Можно говорить."
                recordButton.isEnabled = true
            },
            // Коллбэк для ошибки (errorCallback)
            { exception ->
                resultText.text = "Ошибка загрузки модели: ${exception.message}"
            }
        )
    }
}
