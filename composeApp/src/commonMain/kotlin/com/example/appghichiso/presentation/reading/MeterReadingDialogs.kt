package com.example.appghichiso.presentation.reading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appghichiso.domain.model.Customer

/* ──────────────────────────────────────────────────────────────────────────
 *  WarningDialog  — Cảnh báo tiêu thụ bất thường (tăng/giảm mạnh)
 * ────────────────────────────────────────────────────────────────────────── */
@Composable
internal fun WarningDialog(
    customer: Customer,
    newIndex: Int,
    consumption: Int,
    prevConsumption: Int?,
    isLowConsumption: Boolean,
    prevPeriodHuman: String,
    currentPeriodHuman: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val ratioText = if (prevConsumption != null && prevConsumption > 0) {
        val r = (consumption.toFloat() / prevConsumption * 10).toInt()
        "${r / 10}.${r % 10}"
    } else "—"

    val abnormalHint = when {
        !isLowConsumption -> "tăng cao"
        else              -> "giảm mạnh"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "⚠️ Cảnh báo tiêu thụ $abnormalHint",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFFE65100)
                )
                Text(
                    "Vui lòng xác nhận trước khi lưu",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Khách hàng: ${customer.customerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Mã KH: ${customer.customerCode}  •  Kỳ: ${customer.month}/${customer.year}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Chỉ số cũ:", style = MaterialTheme.typography.bodySmall)
                    Text("${customer.previousIndex} m³", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Chỉ số mới:", style = MaterialTheme.typography.bodySmall)
                    Text("$newIndex m³", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text("Tiêu thụ $prevPeriodHuman:", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("$prevConsumption m³", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tiêu thụ kỳ đang ghi ($currentPeriodHuman):",
                        style    = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f).padding(end = 8.dp))
                    Text("$consumption m³",
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color(0xFFE65100))
                }

                Card(
                    shape  = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Text(
                        "Tiêu thụ kỳ đang ghi ($currentPeriodHuman) ${if (isLowConsumption) "chỉ bằng" else "gấp"} $ratioText lần so với $prevPeriodHuman. " +
                            "Hãy kiểm tra lại đồng hồ nước!",
                        style      = MaterialTheme.typography.bodySmall,
                        color      = Color(0xFFBF360C),
                        fontWeight = FontWeight.Medium,
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                shape  = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text("Hủy, nhập lại") }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Đồng ý, lưu chỉ số", color = Color(0xFFE65100))
            }
        }
    )
}

/* ──────────────────────────────────────────────────────────────────────────
 *  ConfirmDialog  — Xác nhận ghi chỉ số bình thường
 * ────────────────────────────────────────────────────────────────────────── */
@Composable
internal fun ConfirmDialog(
    customer: Customer,
    newIndex: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                "Xác nhận ghi chỉ số",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Khách hàng: ${customer.customerName}")
                Text("Mã KH: ${customer.customerCode}")
                Text("Mã HĐ: ${customer.contractCode}")
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                Text("Chỉ số cũ: ${customer.previousIndex} m³")
                Text("Chỉ số mới: $newIndex m³")
                Text(
                    "Tiêu thụ: ${newIndex - customer.previousIndex} m³",
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, shape = RoundedCornerShape(12.dp)) { Text("Xác nhận") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

/* ──────────────────────────────────────────────────────────────────────────
 *  SuccessDialog  — Ghi thành công; tùy chọn Tiếp tục / Phát hành TVAN
 * ────────────────────────────────────────────────────────────────────────── */
@Composable
internal fun SuccessDialog(
    customerName: String,
    isLoading: Boolean,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onSkip() },
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                "✅ Ghi chỉ số thành công",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.secondary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Chỉ số $customerName đã được cập nhật.")
            }
        },
        confirmButton = {
            Button(onClick = onContinue, shape = RoundedCornerShape(12.dp)) { Text("Tiếp tục") }
        },
        dismissButton = {
            TextButton(onClick = onSkip, enabled = !isLoading) { Text("Bỏ qua") }
        }
    )
}

/* ──────────────────────────────────────────────────────────────────────────
 *  TvanErrorDialog  — Lỗi TVAN
 * ────────────────────────────────────────────────────────────────────────── */
@Composable
internal fun TvanErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lỗi") },
        text  = { Text(message) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Đóng") } }
    )
}
