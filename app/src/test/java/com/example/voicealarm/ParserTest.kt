package com.example.voicealarm

import org.junit.Test

class ParserTest {

    @Test
    fun testParser() {
        val testingString = "напомни тридцатого июля в шестнадцать часов сорок девять минут сообщение позвонить доктору"

        val result = parseVoiceCommand(testingString)

        println("День: ${result.alarmDay}")
        println("Месяц: ${result.alarmMonth}")
        println("Год: ${result.alarmYear}")
        println("Часы: ${result.alarmHour}")
        println("Минуты: ${result.alarmMinute}")
        println("Сообщение: ${result.message}")
        println("Ошибка: ${result.error}")
    }



}