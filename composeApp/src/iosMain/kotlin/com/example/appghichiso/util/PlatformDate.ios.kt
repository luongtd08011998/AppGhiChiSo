package com.example.appghichiso.util

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate

actual fun currentYear(): Int {
    val components = NSCalendar.currentCalendar.components(NSCalendarUnitYear, NSDate())
    return components.year.toInt()
}

actual fun currentMonth(): Int {
    val components = NSCalendar.currentCalendar.components(NSCalendarUnitMonth, NSDate())
    return components.month.toInt()
}
