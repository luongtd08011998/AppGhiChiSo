package com.example.appghichiso.presentation.printer

import androidx.compose.runtime.Composable
import com.example.appghichiso.data.api.dto.InvoiceDto
import com.example.appghichiso.data.api.dto.ReceiptDto

@Composable
actual fun InvoicePrintPreviewDialog(invoice: InvoiceDto, onDismiss: () -> Unit) {
    // iOS: không có máy in nhiệt, no-op
}

@Composable
actual fun ReceiptPrintPreviewDialog(receipt: ReceiptDto, onDismiss: () -> Unit) {
    // iOS: không có máy in nhiệt, no-op
}
