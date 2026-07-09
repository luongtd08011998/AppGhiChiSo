package com.example.appghichiso.domain.model

/**
 * Đại diện một máy in nhiệt đã pair với điện thoại.
 *
 * @param name    Tên hiển thị (vd: "XP-P504A").
 * @param address Địa chỉ MAC Bluetooth, dùng để mở kết nối.
 */
data class PrinterDevice(
    val name: String,
    val address: String
)
