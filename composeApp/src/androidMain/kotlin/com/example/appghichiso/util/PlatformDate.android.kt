package com.example.appghichiso.util

import java.util.Calendar

actual fun currentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
actual fun currentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1

actual fun currentDateString(): String {
    val calendar = Calendar.getInstance()
    val day = calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
    val month = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
    val year = calendar.get(Calendar.YEAR)
    return "$day/$month/$year"
}
