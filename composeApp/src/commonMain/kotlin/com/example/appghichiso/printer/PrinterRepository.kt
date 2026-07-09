package com.example.appghichiso.printer

import com.example.appghichiso.data.api.dto.InvoiceDto
import com.example.appghichiso.data.api.dto.ReceiptDto
import com.example.appghichiso.domain.model.PrinterDevice

/**
 * Giao diện nền tảng cho việc in. Implement thực ở androidMain (Bluetooth ESC/POS),
 * no-op ở iosMain (tính năng in hiện chỉ hỗ trợ Android).
 */
interface PrinterRepository {

    /** Đã được cấp quyền Bluetooth runtime chưa (Android 12+). */
    fun hasPermission(): Boolean

    /** Danh sách máy in đã pair với điện thoại. */
    fun bondedDevices(): List<PrinterDevice>

    /** Máy in đã chọn lần trước (lưu持久), hoặc null. */
    fun getSavedPrinter(): PrinterDevice?

    /** Lưu máy in đã chọn. */
    fun savePrinter(device: PrinterDevice)

    /** Xóa máy in đã chọn (để mở lại hộp chọn máy). */
    fun clearSavedPrinter()

    /** In giấy báo tiền nước (có QR). */
    suspend fun printInvoice(device: PrinterDevice, invoice: InvoiceDto): Result<Unit>

    /** In biên nhận thanh toán (Liên 2). */
    suspend fun printReceipt(device: PrinterDevice, receipt: ReceiptDto): Result<Unit>
}
