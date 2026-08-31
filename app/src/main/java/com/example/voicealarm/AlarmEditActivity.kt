package com.example.voicealarm

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.time.DateTimeException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar

class AlarmEditActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ALARM_REQUEST_CODE = "alarm_request_code"
    }

    private lateinit var database: AlarmDatabase
    private lateinit var titleText: TextView
    private lateinit var timeButton: Button
    private lateinit var dateButton: Button
    private lateinit var messageInput: EditText
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button

    private var selectedTime: LocalTime? = LocalTime.of(6, 0)
    private var selectedDate: LocalDate? = null
    private var editedAlarm: AlarmEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alarm_edit)

        val rootView = findViewById<android.view.View>(R.id.main)
        val initialPaddingLeft = rootView.paddingLeft
        val initialPaddingTop = rootView.paddingTop
        val initialPaddingRight = rootView.paddingRight
        val initialPaddingBottom = rootView.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                initialPaddingLeft + systemBars.left,
                initialPaddingTop + systemBars.top,
                initialPaddingRight + systemBars.right,
                initialPaddingBottom + systemBars.bottom
            )
            insets
        }

        database = AlarmDatabase.getDatabase(this)
        titleText = findViewById(R.id.tvAlarmEditTitle)
        timeButton = findViewById(R.id.btnSelectTime)
        dateButton = findViewById(R.id.btnSelectDate)
        messageInput = findViewById(R.id.etAlarmMessage)
        saveButton = findViewById(R.id.btnSaveAlarm)
        deleteButton = findViewById(R.id.btnDeleteAlarm)

        findViewById<Button>(R.id.btnCancelAlarmEdit).setOnClickListener { finish() }
        timeButton.setOnClickListener { showTimePicker() }
        dateButton.setOnClickListener { showDatePicker() }
        saveButton.setOnClickListener { saveAlarm() }
        deleteButton.setOnClickListener { deleteCurrentAlarm() }

        if (!intent.hasExtra(EXTRA_ALARM_REQUEST_CODE)) {
            titleText.setText(R.string.alarm_create_title)
            updateTimeText()
        } else {
            titleText.setText(R.string.alarm_edit_title)
            val requestCode = intent.getIntExtra(EXTRA_ALARM_REQUEST_CODE, 0)
            loadAlarm(requestCode)
        }
    }

    private fun loadAlarm(requestCode: Int) {
        saveButton.isEnabled = false

        lifecycleScope.launch {
            val alarm = database.alarmDao()
                .getAlarmsOnce()
                .firstOrNull { it.requestCode == requestCode }

            if (alarm == null) {
                Toast.makeText(
                    this@AlarmEditActivity,
                    R.string.alarm_not_found,
                    Toast.LENGTH_LONG
                ).show()
                finish()
                return@launch
            }

            editedAlarm = alarm
            selectedTime = LocalTime.of(alarm.hour, alarm.minute)
            selectedDate = LocalDate.of(alarm.year, alarm.month, alarm.day)
            messageInput.setText(alarm.message)
            updateTimeText()
            updateDateText()
            saveButton.isEnabled = true
            deleteButton.visibility = android.view.View.VISIBLE
        }
    }

    private fun deleteCurrentAlarm() {
        val alarm = editedAlarm ?: return
        deleteButton.isEnabled = false
        saveButton.isEnabled = false

        lifecycleScope.launch {
            try {
                cancelAlarm(this@AlarmEditActivity, alarm)
                database.alarmDao().deleteAlarm(alarm)
                finish()
            } catch (exception: Exception) {
                deleteButton.isEnabled = true
                saveButton.isEnabled = true
                Toast.makeText(
                    this@AlarmEditActivity,
                    getString(R.string.alarm_delete_error, exception.message.orEmpty()),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showTimePicker() {
        val initialTime = selectedTime ?: LocalTime.now()

        TimePickerDialog(
            this,
            { _, hour, minute ->
                selectedTime = LocalTime.of(hour, minute)
                updateTimeText()
            },
            initialTime.hour,
            initialTime.minute,
            android.text.format.DateFormat.is24HourFormat(this)
        ).show()
    }

    private fun showDatePicker() {
        val initialDate = selectedDate ?: LocalDate.now()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDate = LocalDate.of(year, month + 1, day)
                updateDateText()
            },
            initialDate.year,
            initialDate.monthValue - 1,
            initialDate.dayOfMonth
        ).apply {
            datePicker.minDate = Calendar.getInstance().timeInMillis
        }.show()
    }

    private fun updateTimeText() {
        val time = selectedTime ?: return
        timeButton.text = getString(R.string.alarm_time_format, time.hour, time.minute)
    }

    private fun updateDateText() {
        val date = selectedDate ?: return
        dateButton.text = getString(
            R.string.alarm_date_format,
            date.dayOfMonth,
            date.monthValue,
            date.year
        )
    }

    private fun saveAlarm() {
        val date = selectedDate
        val time = selectedTime

        if (time == null) {
            Toast.makeText(this, R.string.select_alarm_time_error, Toast.LENGTH_SHORT).show()
            return
        }
        if (date == null) {
            Toast.makeText(this, R.string.select_alarm_date_error, Toast.LENGTH_SHORT).show()
            return
        }

        val alarmDateTime = try {
            LocalDateTime.of(date, time)
        } catch (_: DateTimeException) {
            Toast.makeText(this, R.string.invalid_alarm_date_time, Toast.LENGTH_LONG).show()
            return
        }

        if (!alarmDateTime.isAfter(LocalDateTime.now())) {
            Toast.makeText(this, R.string.alarm_must_be_in_future, Toast.LENGTH_LONG)
                .show()
            return
        }

        val currentAlarm = editedAlarm
        val willBeActive = currentAlarm?.isActive ?: true
        if (willBeActive && !ensureAlarmPermissions()) return

        val alarm = AlarmEntity(
            requestCode = currentAlarm?.requestCode ?: 0,
            minute = time.minute,
            hour = time.hour,
            day = date.dayOfMonth,
            month = date.monthValue,
            year = date.year,
            message = messageInput.text.toString(),
            isActive = willBeActive
        )

        saveButton.isEnabled = false
        lifecycleScope.launch {
            try {
                if (currentAlarm == null) {
                    val generatedId = database.alarmDao().addAlarm(alarm)
                    scheduleAlarm(this@AlarmEditActivity, alarm.copy(requestCode = generatedId.toInt()))
                } else {
                    database.alarmDao().updateAlarm(alarm)
                    cancelAlarm(this@AlarmEditActivity, currentAlarm)
                    if (alarm.isActive) {
                        scheduleAlarm(this@AlarmEditActivity, alarm)
                    }
                }
                finish()
            } catch (exception: Exception) {
                saveButton.isEnabled = true
                Toast.makeText(
                    this@AlarmEditActivity,
                    getString(R.string.alarm_save_error, exception.message.orEmpty()),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun ensureAlarmPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(
                this,
                R.string.allow_notifications_to_save_alarm,
                Toast.LENGTH_LONG
            ).show()
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", packageName, null)
                }
            )
            return false
        }

        return ensureExactAlarmPermission(this)
    }
}
