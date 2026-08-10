package com.example.voicealarm

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class VoiceCommandParserTest {

    // -------------------------------- Явная дата --------------------------------

    @Test
    fun parsesDateWithoutYear() {
        val fixedNow = LocalDateTime.of(
            2026, 8, 1,
            12, 0
        )
        val result = parseVoiceCommand(
            text = "напомни десятого августа в девять часов тридцать минут" +
                    " сообщение позвонить врачу",
            now = fixedNow
        )
        assertEquals("", result.error)
        assertEquals(10, result.alarmDay)
        assertEquals(8, result.alarmMonth)
        assertEquals(2026, result.alarmYear)
        assertEquals(9, result.alarmHour)
        assertEquals(30, result.alarmMinute)
        assertEquals("позвонить врачу", result.message)
    }

    @Test
    fun parsesDateWithShortYear() {
        val fixedNow = LocalDateTime.of(
            2026, 8, 1,
            12, 0
        )
        val result = parseVoiceCommand(
            text = "напомни десятого августа двадцать седьмого года в девять часов тридцать минут" +
                    " сообщение позвонить врачу",
            now = fixedNow
        )
        assertEquals("", result.error)
        assertEquals(10, result.alarmDay)
        assertEquals(8, result.alarmMonth)
        assertEquals(2027, result.alarmYear)
        assertEquals(9, result.alarmHour)
        assertEquals(30, result.alarmMinute)
        assertEquals("позвонить врачу", result.message)
    }

    @Test
    fun parsesDateWithFullYear() {
        val fixedNow = LocalDateTime.of(
            2026, 8, 1,
            12, 0
        )
        val result = parseVoiceCommand(
            text = "напомни десятого августа две тысячи двадцать седьмого года в девять часов тридцать минут" +
                    " сообщение позвонить врачу",
            now = fixedNow
        )
        assertEquals("", result.error)
        assertEquals(10, result.alarmDay)
        assertEquals(8, result.alarmMonth)
        assertEquals(2027, result.alarmYear)
        assertEquals(9, result.alarmHour)
        assertEquals(30, result.alarmMinute)
        assertEquals("позвонить врачу", result.message)
    }

    @Test
    fun movesPastDateWithoutYearToNextYear() {
        val fixedNow = LocalDateTime.of(
            2026, 8, 1,
            12, 0
        )
        val result = parseVoiceCommand(
            text = "напомни первого августа в девять часов тридцать минут" +
                    " сообщение позвонить врачу",
            now = fixedNow
        )
        assertEquals("", result.error)
        assertEquals(1, result.alarmDay)
        assertEquals(8, result.alarmMonth)
        assertEquals(2027, result.alarmYear)
        assertEquals(9, result.alarmHour)
        assertEquals(30, result.alarmMinute)
        assertEquals("позвонить врачу", result.message)
    }

    // -------------------------------- Время --------------------------------

    @Test
    fun parsesHourWithoutMinutes() {
        val fixedNow = LocalDateTime.of(
            2026, 8, 1,
            12, 0
        )
        val result = parseVoiceCommand(
            text = "напомни десятого августа две тысячи двадцать седьмого года в девять часов" +
                    " сообщение позвонить врачу",
            now = fixedNow
        )
        assertEquals("", result.error)
        assertEquals(10, result.alarmDay)
        assertEquals(8, result.alarmMonth)
        assertEquals(2027, result.alarmYear)
        assertEquals(9, result.alarmHour)
        assertEquals(0, result.alarmMinute)
        assertEquals("позвонить врачу", result.message)
    }

    @Test
    fun parsesHourAndMinutes() {
        val fixedNow = LocalDateTime.of(
            2026, 8, 1,
            12, 0
        )
        val result = parseVoiceCommand(
            text = "напомни десятого августа две тысячи двадцать седьмого года в девять часов восемь минут" +
                    " сообщение позвонить врачу",
            now = fixedNow
        )
        assertEquals("", result.error)
        assertEquals(10, result.alarmDay)
        assertEquals(8, result.alarmMonth)
        assertEquals(2027, result.alarmYear)
        assertEquals(9, result.alarmHour)
        assertEquals(8, result.alarmMinute)
        assertEquals("позвонить врачу", result.message)
    }

    @Test
    fun parsesCompoundMinutes() {
        val fixedNow = LocalDateTime.of(
            2026, 8, 1,
            12, 0
        )
        val result = parseVoiceCommand(
            text = "напомни десятого августа две тысячи двадцать седьмого года в девять часов тридцать восемь минут" +
                    " сообщение позвонить врачу",
            now = fixedNow
        )
        assertEquals("", result.error)
        assertEquals(10, result.alarmDay)
        assertEquals(8, result.alarmMonth)
        assertEquals(2027, result.alarmYear)
        assertEquals(9, result.alarmHour)
        assertEquals(38, result.alarmMinute)
        assertEquals("позвонить врачу", result.message)
    }

    @Test
    fun parsesMidnight() {
        val fixedNow = LocalDateTime.of(
            2026, 8, 1,
            12, 0
        )
        val result = parseVoiceCommand(
            text = "напомни десятого августа две тысячи двадцать седьмого года в ноль часов" +
                    " сообщение позвонить врачу",
            now = fixedNow
        )
        assertEquals("", result.error)
        assertEquals(10, result.alarmDay)
        assertEquals(8, result.alarmMonth)
        assertEquals(2027, result.alarmYear)
        assertEquals(0, result.alarmHour)
        assertEquals(0, result.alarmMinute)
        assertEquals("позвонить врачу", result.message)
    }

    @Test
    fun parsesLastMinuteOfDay() {
        val fixedNow = LocalDateTime.of(
            2026, 8, 1,
            12, 0
        )
        val result = parseVoiceCommand(
            text = "напомни десятого августа две тысячи двадцать седьмого года в двадцать три часа пятьдесят девять минут" +
                    " сообщение позвонить врачу",
            now = fixedNow
        )
        assertEquals("", result.error)
        assertEquals(10, result.alarmDay)
        assertEquals(8, result.alarmMonth)
        assertEquals(2027, result.alarmYear)
        assertEquals(23, result.alarmHour)
        assertEquals(59, result.alarmMinute)
        assertEquals("позвонить врачу", result.message)
    }

    //  -------------------------------- Дни месяца и числа --------------------------------

    @Test
    fun parsesSimpleDayNumber() {
        val fixedNow = LocalDateTime.of(
            2026, 8, 1,
            12, 0
        )
        val result = parseVoiceCommand(
            text = "напомни пятого августа две тысячи двадцать седьмого года в двадцать три часа пятьдесят девять минут" +
                    " сообщение позвонить врачу",
            now = fixedNow
        )
        assertEquals("", result.error)
        assertEquals(5, result.alarmDay)
        assertEquals(8, result.alarmMonth)
        assertEquals(2027, result.alarmYear)
        assertEquals(23, result.alarmHour)
        assertEquals(59, result.alarmMinute)
        assertEquals("позвонить врачу", result.message)
    }

    @Test
    fun parsesCompoundDayNumber() {
        val fixedNow = LocalDateTime.of(
            2026, 8, 1,
            12, 0
        )
        val result = parseVoiceCommand(
            text = "напомни тридцать первого августа две тысячи двадцать седьмого года в двадцать три часа пятьдесят девять минут" +
                    " сообщение позвонить врачу",
            now = fixedNow
        )
        assertEquals("", result.error)
        assertEquals(31, result.alarmDay)
        assertEquals(8, result.alarmMonth)
        assertEquals(2027, result.alarmYear)
        assertEquals(23, result.alarmHour)
        assertEquals(59, result.alarmMinute)
        assertEquals("позвонить врачу", result.message)
    }

    // -------------------------------- Сообщение --------------------------------

    @Test
    fun extractsSingleWordMessage() {
        val fixedNow = LocalDateTime.of(
            2026, 8, 1,
            12, 0
        )
        val result = parseVoiceCommand(
            text = "напомни тридцать первого августа две тысячи двадцать седьмого года в двадцать три часа пятьдесят девять минут" +
                    " сообщение встреча",
            now = fixedNow
        )
        assertEquals("", result.error)
        assertEquals(31, result.alarmDay)
        assertEquals(8, result.alarmMonth)
        assertEquals(2027, result.alarmYear)
        assertEquals(23, result.alarmHour)
        assertEquals(59, result.alarmMinute)
        assertEquals("встреча", result.message)
    }

    @Test
    fun extractsMultiWordMessage() {
        val fixedNow = LocalDateTime.of(
            2026, 8, 1,
            12, 0
        )
        val result = parseVoiceCommand(
            text = "напомни тридцать первого августа две тысячи двадцать седьмого года в двадцать три часа пятьдесят девять минут" +
                    " сообщение позвонить врачу и купить лекарства",
            now = fixedNow
        )
        assertEquals("", result.error)
        assertEquals(31, result.alarmDay)
        assertEquals(8, result.alarmMonth)
        assertEquals(2027, result.alarmYear)
        assertEquals(23, result.alarmHour)
        assertEquals(59, result.alarmMinute)
        assertEquals("позвонить врачу и купить лекарства", result.message)

    }

    // -------------------------------- Неправильный ввод --------------------------------

    @Test
    fun rejectsCommandWithoutReminderPrefix() {
        val fixedNow = LocalDateTime.of(
            2026, 8, 1,
            12, 0
        )
        val result = parseVoiceCommand(
            text = "тридцать первого августа две тысячи двадцать седьмого года в двадцать три часа пятьдесят девять минут" +
                    " сообщение позвонить врачу",
            now = fixedNow
        )
        assertEquals("Некорректный ввод", result.error)
        assertEquals(0, result.alarmDay)
        assertEquals(0, result.alarmMonth)
        assertEquals(0, result.alarmYear)
        assertEquals(0, result.alarmHour)
        assertEquals(0, result.alarmMinute)
        assertEquals("", result.message)
    }

    @Test
    fun rejectsCommandWithoutMessageMarker() {
        val fixedNow = LocalDateTime.of(
            2026, 8, 1,
            12, 0
        )
        val result = parseVoiceCommand(
            text = "напомни тридцать первого августа две тысячи двадцать седьмого года в двадцать три часа пятьдесят девять минут" +
                    " позвонить врачу",
            now = fixedNow
        )
        assertEquals("Некорректный ввод", result.error)
        assertEquals(0, result.alarmDay)
        assertEquals(0, result.alarmMonth)
        assertEquals(0, result.alarmYear)
        assertEquals(0, result.alarmHour)
        assertEquals(0, result.alarmMinute)
        assertEquals("", result.message)
    }

    @Test
    fun rejectsInvalidHour() {
        val fixedNow = LocalDateTime.of(
            2026, 8, 1,
            12, 0
        )
        val result = parseVoiceCommand(
            text = "напомни тридцать первого августа две тысячи двадцать седьмого года в двадцать четыре часа пятьдесят девять минут" +
                    " сообщение позвонить врачу",
            now = fixedNow
        )
        assertEquals("Ошибка ввода времени", result.error)
        assertEquals(0, result.alarmDay)
        assertEquals(0, result.alarmMonth)
        assertEquals(0, result.alarmYear)
        assertEquals(0, result.alarmHour)
        assertEquals(0, result.alarmMinute)
        assertEquals("", result.message)
    }

    @Test
    fun rejectsInvalidMinutes() {
        val fixedNow = LocalDateTime.of(
            2026, 8, 1,
            12, 0
        )
        val result = parseVoiceCommand(
            text = "напомни тридцать первого августа две тысячи двадцать седьмого года в двадцать три часа шестьдесят минут" +
                    " сообщение позвонить врачу",
            now = fixedNow
        )
        assertEquals("Ошибка ввода времени", result.error)
        assertEquals(0, result.alarmDay)
        assertEquals(0, result.alarmMonth)
        assertEquals(0, result.alarmYear)
        assertEquals(0, result.alarmHour)
        assertEquals(0, result.alarmMinute)
        assertEquals("", result.message)
    }

    @Test
    fun rejectsInvalidDayForMonth() {
        val fixedNow = LocalDateTime.of(
            2026, 8, 1,
            12, 0
        )
        val result = parseVoiceCommand(
            text = "напомни тридцать первого июня две тысячи двадцать седьмого года в двадцать три часа пятьдесят девять минут" +
                    " сообщение позвонить врачу",
            now = fixedNow
        )
        assertEquals("Ошибка ввода дня", result.error)
        assertEquals(0, result.alarmDay)
        assertEquals(0, result.alarmMonth)
        assertEquals(0, result.alarmYear)
        assertEquals(0, result.alarmHour)
        assertEquals(0, result.alarmMinute)
        assertEquals("", result.message)
    }

    @Test
    fun rejectsInvalidFebruaryDate() {
        val fixedNow = LocalDateTime.of(
            2026, 8, 1,
            12, 0
        )
        val result = parseVoiceCommand(
            text = "напомни тридцатого февраля две тысячи двадцать седьмого года в двадцать три часа пятьдесят девять минут" +
                    " сообщение позвонить врачу",
            now = fixedNow
        )
        assertEquals("Ошибка ввода дня", result.error)
        assertEquals(0, result.alarmDay)
        assertEquals(0, result.alarmMonth)
        assertEquals(0, result.alarmYear)
        assertEquals(0, result.alarmHour)
        assertEquals(0, result.alarmMinute)
        assertEquals("", result.message)
    }

    // -------------------------------- Календарные границы --------------------------------

    @Test
    fun acceptsFebruary29InLeapYear() {
        val fixedNow = LocalDateTime.of(
            2026, 8, 1,
            12, 0
        )
        val result = parseVoiceCommand(
            text = "напомни двадцать девятого февраля две тысячи двадцать восьмого года в двадцать три часа пятьдесят девять минут" +
                    " сообщение позвонить врачу и купить лекарства",
            now = fixedNow
        )
        assertEquals("", result.error)
        assertEquals(29, result.alarmDay)
        assertEquals(2, result.alarmMonth)
        assertEquals(2028, result.alarmYear)
        assertEquals(23, result.alarmHour)
        assertEquals(59, result.alarmMinute)
        assertEquals("позвонить врачу и купить лекарства", result.message)

    }

    @Test
    fun rejectsFebruary29InNonLeapYear() {
        val fixedNow = LocalDateTime.of(
            2026, 8, 1,
            12, 0
        )
        val result = parseVoiceCommand(
            text = "напомни двадцать девятого февраля две тысячи двадцать седьмого года в двадцать три часа пятьдесят девять минут" +
                    " сообщение позвонить врачу и купить лекарства",
            now = fixedNow
        )
        assertEquals("Ошибка ввода дня", result.error)
        assertEquals(0, result.alarmDay)
        assertEquals(0, result.alarmMonth)
        assertEquals(0, result.alarmYear)
        assertEquals(0, result.alarmHour)
        assertEquals(0, result.alarmMinute)
        assertEquals("", result.message)

    }
}