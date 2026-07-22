package com.example.appghichiso.utils

/**
 * Chuyển ngày từ định dạng API (YYYYMMDD) sang dạng đọc được (dd/MM/yyyy).
 * Ví dụ: "20260721" → "21/07/2026"
 * Nếu chuỗi không đúng định dạng, trả về nguyên chuỗi gốc.
 */
fun formatApiDate(raw: String): String {
    if (raw.length != 8 || !raw.all { it.isDigit() }) return raw
    val yyyy = raw.substring(0, 4)
    val mm   = raw.substring(4, 6)
    val dd   = raw.substring(6, 8)
    return "$dd/$mm/$yyyy"
}
