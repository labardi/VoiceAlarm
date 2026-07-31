package com.example.voicealarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.text.format

class AlarmActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        setContentView(R.layout.activity_alarm)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(
            AudioManager.STREAM_ALARM,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM),
            0
        )

        val mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(this@AlarmActivity, alarmUri)
            isLooping = true
            setVolume(0.1f, 0.1f)
            prepare()
            start()
        }
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 500, 1000) // пауза, вибрация, пауза между повторами
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
        }
        var volume = 0.1f
        val handler = Handler(Looper.getMainLooper())

        val volumeRunnable = object : Runnable {
            override fun run() {
                if (volume < 1.0f) {
                    volume = (volume + 0.05f).coerceAtMost(1.0f)
                    mediaPlayer.setVolume(volume, volume)
                    handler.postDelayed(this, 2000) // каждые 2 секунды
                }
            }
        }
        handler.postDelayed(volumeRunnable, 2000)

        val message = intent.getStringExtra("message")
        val hour = intent.getIntExtra("hour", 0)
        val minute = intent.getIntExtra("minute", 0)
        val day = intent.getIntExtra("day", 0)
        val month = intent.getIntExtra("month", 0)
        val year = intent.getIntExtra("year", 0)


        val timeDateText = findViewById<android.widget.TextView>(R.id.tvDateTime)
        timeDateText.text = "${"%02d".format(hour)}:${"%02d".format(minute)} ${
            "%02d".format(day)}.${"%02d".format(month)}.${"%02d".format(year)}"

        val messageText = findViewById<android.widget.TextView>(R.id.tvMessage)
        messageText.text = message
        val dismissButton = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.btnDismiss)

        dismissButton.setOnClickListener {
            handler.removeCallbacks(volumeRunnable)
            mediaPlayer.stop()
            mediaPlayer.release()
            vibrator.cancel()
            finish()
        }

    }
}