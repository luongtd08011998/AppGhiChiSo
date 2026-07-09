package com.example.appghichiso.presentation.printer

import androidx.compose.runtime.Composable
import com.example.appghichiso.data.api.dto.InvoiceDto
import com.example.appghichiso.data.api.dto.ReceiptDto

/**
 * Hiển thị bản xem trước (print preview) trực tiếp trong app, render bằng
 * cùng engine [ReceiptBitmapRenderer] dùng khi in thật → giúp debug bố cục dễ hơn.
 *
 * - Android: render bitmap → hiển thị Image scrollable trong Dialog.
 * - iOS: no-op (không có máy in).
 */
@Composable
expect fun InvoicePrintPreviewDialog(
    invoice: InvoiceDto,
    onDismiss: () -> Unit
)

@Composable
expect fun ReceiptPrintPreviewDialog(
    receipt: ReceiptDto,
    onDismiss: () -> Unit
)
