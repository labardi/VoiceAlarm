package com.example.voicealarm

import java.time.LocalDateTime

data class ParsedCommand(
    val alarmDay: Int,
    val alarmMonth: Int,
    val alarmYear: Int,
    val alarmHour: Int,
    val alarmMinute: Int,
    val message: String,
    val error: String
)



fun parseVoiceCommand(text: String): ParsedCommand {
    val numberMap = mapOf(
        // Количественные (для времени: часы, минуты)
        "ноль" to 0,
        "один" to 1, "одна" to 1,
        "два" to 2, "две" to 2,
        "три" to 3,
        "четыре" to 4,
        "пять" to 5,
        "шесть" to 6,
        "семь" to 7,
        "восемь" to 8,
        "девять" to 9,
        "десять" to 10,
        "одиннадцать" to 11,
        "двенадцать" to 12,
        "тринадцать" to 13,
        "четырнадцать" to 14,
        "пятнадцать" to 15,
        "шестнадцать" to 16,
        "семнадцать" to 17,
        "восемнадцать" to 18,
        "девятнадцать" to 19,
        "двадцать" to 20,
        "тридцать" to 30,
        "сорок" to 40,
        "пятьдесят" to 50,
        "шестьдесят" to 60,
        "семьдесят" to 70,
        "восемьдесят" to 80,
        "девяносто" to 90,

        // Порядковые в родительном падеже (для дат: дни месяца)
        "первого" to 1,
        "второго" to 2,
        "третьего" to 3,
        "четвертого" to 4, "четвёртого" to 4, // добавил вариант с 'ё' на всякий случай
        "пятого" to 5,
        "шестого" to 6,
        "седьмого" to 7,
        "восьмого" to 8,
        "девятого" to 9,
        "десятого" to 10,
        "одиннадцатого" to 11,
        "двенадцатого" to 12,
        "тринадцатого" to 13,
        "четырнадцатого" to 14,
        "пятнадцатого" to 15,
        "шестнадцатого" to 16,
        "семнадцатого" to 17,
        "восемнадцатого" to 18,
        "девятнадцатого" to 19,
        "двадцатого" to 20,
        "тридцатого" to 30,
        "сорокового" to 40,
        "пятидесятого" to 50,
        "шестидесятого" to 60,
        "семидесятого" to 70,
        "восьмидесятого" to 80,
        "девяностого" to 90,

        "тысячи" to 1998 // Так потому что перед этим идет "две", в сумме это даст две тысячи
    )

    val monthMap = mapOf(
        "января" to 1,
        "февраля" to 2,
        "марта" to 3,
        "апреля" to 4,
        "мая" to 5,
        "июня" to 6,
        "июля" to 7,
        "августа" to 8,
        "сентября" to 9,
        "октября" to 10,
        "ноября" to 11,
        "декабря" to 12
    )


    var errorMessage = ""
    var alarmMessage = ""
    var day = 0
    var hour = 0
    var minute = 0
    var month = 0

    val now = LocalDateTime.now()

    val currentYear = now.year
    val currentMonth = now.monthValue
    val currentDay = now.dayOfMonth

    val currentHour = now.hour
    val currentMinute = now.minute

    var year = currentYear

    val normalizedText = text.trim().lowercase()

    if ("сообщен" in normalizedText && normalizedText.startsWith("напомн")) {
        alarmMessage = normalizedText.substringAfter("сообщен").substringAfter(" ").trim()
        val timeString = normalizedText.substringAfter("напомн").substringAfter(" ").substringBefore("сообщен").trim() + " "


            var tempString = ""
            var forState = 0

            if ("год" !in timeString) {
                for (letter in timeString) {
                    if (letter == ' ') {

                        val number = numberMap[tempString]
                        if (number != null) {

                            when (forState) {
                                0 -> day += number
                                1 -> hour += number
                                2 -> minute += number
                            }

                        } else {
                            if (forState == 0 && tempString in monthMap) {
                                month = monthMap.getValue(tempString)
                                forState = 1
                            }
                            if (forState == 1 && "час" in tempString) {
                                forState = 2
                            }
                        }

                        tempString = ""
                        continue
                    }
                tempString += letter
                }

            } else {
                year = 0
                for (letter in timeString) {

                    if (letter == ' ') {

                        val number = numberMap[tempString]
                        if (number != null) {

                            when (forState) {
                                0 -> day += number
                                1 -> year += number
                                2 -> hour += number
                                3 -> minute += number
                            }

                        } else {
                            if (forState == 0 && tempString in monthMap) {
                                month = monthMap.getValue(tempString)
                                forState = 1
                            }
                            if (forState == 1 && "год" in tempString) {
                                forState = 2
                            }
                            if (forState == 2 && "час" in tempString) {
                                forState = 3
                            }
                        }

                        tempString = ""
                        continue
                    }
                    tempString += letter
                }
            }

            // Большая проверка

            // Сначала проверка на правильность ввода

            if (minute !in 0..59) {
                errorMessage = "Ошибка ввода времени"
            }

            if (hour !in 0..23) {
                errorMessage = "Ошибка ввода времени"
            }

            if (month !in 1..12) {
                errorMessage = "Ошибка ввода месяца"
            } else {
                if (month == 2) {
                    if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
                        if (day !in 1..29) {
                            errorMessage = "Ошибка ввода дня"
                        }
                    } else {
                        if (day !in 1..28) {
                            errorMessage = "Ошибка ввода дня"
                        }
                    }
                } else if ((month < 8 && month % 2 == 1) || (month >= 8 && month % 2 == 0)) {
                    if (day  !in 1..31) {
                        errorMessage = "Ошибка ввода дня"
                    }
                } else {
                    if (day  !in 1..30) {
                        errorMessage = "Ошибка ввода дня"
                    }
                }
            }
            if (year in 1..99) {
                year += 2000
            }
            if (currentYear > year) {
                errorMessage = "Ошибка ввода года"
            }

            // Теперь проверка на будильник в прошлое

            if (currentYear == year) {
                if (currentMonth > month) {
                    year += 1
                } else if (currentMonth == month) {
                    if (currentDay > day) {
                        year += 1
                    } else if (currentDay == day) {
                        if (currentHour > hour) {
                            year += 1
                        } else if (currentHour == hour) {
                            if (currentMinute > minute) {
                                year += 1
                            } else if (currentMinute == minute) {
                                year += 1
                            }
                        }
                    }
                }
            }


    } else {
        errorMessage = "Некорректный ввод"
    }

    return ParsedCommand(alarmDay = day, alarmMonth = month, alarmYear = year, alarmHour = hour, alarmMinute = minute, message = alarmMessage, error = errorMessage)
}
