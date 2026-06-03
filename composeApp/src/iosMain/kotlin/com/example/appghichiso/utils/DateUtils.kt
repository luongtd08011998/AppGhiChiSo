package com.example.appghichiso.utils

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

actual fun getCurrentDateString(): String {
    val formatter = NSDateFormatter()
    formatter.dateFormat = "dd/MM/yyyy"
    return formatter.stringFromDate(NSDate())
}
