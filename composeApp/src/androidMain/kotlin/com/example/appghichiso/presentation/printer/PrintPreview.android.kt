package com.example.appghichiso.presentation.printer

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.appghichiso.data.api.dto.InvoiceDto
import com.example.appghichiso.data.api.dto.ReceiptDto
import com.example.appghichiso.data.printer.ReceiptBitmapRenderer
import com.example.appghichiso.utils.VietQrUrlFetcher
import com.example.appghichiso.utils.buildVietQrUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android actual: render hóa đơn bằng [ReceiptBitmapRenderer] (cùng engine với máy in thật)
 * rồi hiển thị dưới dạng ảnh scrollable trong Dialog — người dùng thấy chính xác tờ giấy sẽ in ra.
 */
@Composable
actual fun InvoicePrintPreviewDialog(invoice: InvoiceDto, onDismiss: () -> Unit) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(invoice) {
        loading = true
        bitmap = withContext(Dispatchers.IO) {
            val qrUrl = buildVietQrUrl(
                amount = invoice.totalAmount,
                custCode = invoice.custCode ?: "",
                yearMonth = invoice.yearMonth ?: "",
                invNumber = invoice.invNumber ?: ""
            )
            val qrBitmap = VietQrUrlFetcher.fetch(qrUrl)
            ReceiptBitmapRenderer.renderInvoice(invoice, qrBitmap)
        }
        loading = false
    }

    PrintPreviewDialog(title = "Xem trước giấy báo", loading = loading, bitmap = bitmap, onDismiss = onDismiss)
}

@Composable
actual fun ReceiptPrintPreviewDialog(receipt: ReceiptDto, onDismiss: () -> Unit) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(receipt) {
        loading = true
        bitmap = withContext(Dispatchers.IO) {
            ReceiptBitmapRenderer.renderReceipt(receipt)
        }
        loading = false
    }

    PrintPreviewDialog(title = "Xem trước biên nhận", loading = loading, bitmap = bitmap, onDismiss = onDismiss)
}

@Composable
private fun PrintPreviewDialog(
    title: String,
    loading: Boolean,
    bitmap: Bitmap?,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Tiêu đề
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1565C0))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("● Khổ 58mm", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                }

                // Nội dung chính: Bitmap preview
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFEEEEEE)) // nền xám nhạt như mặt bàn
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (loading) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color(0xFF1565C0))
                                Spacer(Modifier.height(12.dp))
                                Text("Đang tạo bản xem trước...", color = Color.Gray)
                            }
                        }
                    } else if (bitmap != null) {
                        // Giấy trắng với bóng đổ nhẹ, hiển thị bitmap khổ 58mm
                        Card(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(0.72f), // ~58mm / 80mm màn hình = ~72%
                            shape = RoundedCornerShape(2.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Bản xem trước giấy in",
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.FillWidth
                            )
                        }
                    }
                }

                // Nút Đóng
                HorizontalDivider(color = Color.LightGray)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Đóng xem trước")
                    }
                }
            }
        }
    }
}
