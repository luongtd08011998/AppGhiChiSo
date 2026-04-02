package com.example.appghichiso.util

import java.util.Calendar

actual fun currentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
actual fun currentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1

