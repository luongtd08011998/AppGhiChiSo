package com.example.appghichiso.utils

/**
 * Format số tiền theo kiểu Việt Nam (dấu chấm phân cách hàng nghìn), vd: 1234567 -> "1.234.567".
 *
 * Bản riêng dùng cho module in (không sửa [com.example.appghichiso.presentation.reading] đã hoàn thành).
 */
fun formatCurrencyForPrint(amount: Double): String {
    val amountLong = amount.toLong()
    if (amountLong == 0L) return "0"
    val sb = StringBuilder(amountLong.toString())
    var i = sb.length - 3
    while (i > 0) {
        sb.insert(i, ".")
        i -= 3
    }
    return sb.toString()
}
