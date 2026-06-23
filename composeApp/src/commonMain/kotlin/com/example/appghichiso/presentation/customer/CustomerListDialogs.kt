package com.example.appghichiso.presentation.customer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun TvanErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lỗi tác vụ") },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
fun PublishSuccessDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thành công") },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
fun PublishTimeoutDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Info, contentDescription = null) },
        title = { Text("Kết nối mất quá lâu") },
        text = {
            Text(
                "Server đang xử lý nhiều hóa đơn cùng lúc nên mất nhiều thời gian phản hồi.\n\n" +
                "Danh sách đã được làm mới — hãy kiểm tra xem hóa đơn đã được phát hành chưa. " +
                "Nếu chưa thấy kết quả, vui lòng kéo xuống để làm mới lại."
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Đã hiểu")
            }
        }
    )
}
