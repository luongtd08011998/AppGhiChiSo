package com.example.appghichiso.data.printer

import com.example.appghichiso.data.api.dto.InvoiceDto
import com.example.appghichiso.data.api.dto.ReceiptDto
import com.example.appghichiso.domain.model.PrinterDevice
import com.example.appghichiso.printer.PrinterRepository

/**
 * iOS no-op: tính năng in hiện chỉ hỗ trợ Android.
 */
class NoopPrinterRepository : PrinterRepository {
    override fun hasPermission(): Boolean = true
    override fun bondedDevices(): List<PrinterDevice> = emptyList()
    override fun getSavedPrinter(): PrinterDevice? = null
    override fun savePrinter(device: PrinterDevice) {}
    override fun clearSavedPrinter() {}
    override suspend fun printInvoice(device: PrinterDevice, invoice: InvoiceDto): Result<Unit> =
        Result.failure(UnsupportedOperationException("Tính năng in chưa hỗ trợ iOS"))
    override suspend fun printReceipt(device: PrinterDevice, receipt: ReceiptDto): Result<Unit> =
        Result.failure(UnsupportedOperationException("Tính năng in chưa hỗ trợ iOS"))
}
