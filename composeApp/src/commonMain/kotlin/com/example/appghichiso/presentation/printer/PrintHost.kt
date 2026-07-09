package com.example.appghichiso.presentation.printer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.appghichiso.domain.model.PrinterDevice
import com.example.appghichiso.printer.PrinterHub
import com.example.appghichiso.printer.PrintingState
import com.example.appghichiso.printer.rememberBluetoothPermissionRequest

/**
 * Overlay duy nhất cho toàn bộ luồng in: chọn máy, loading, thành công, lỗi.
 * Đặt 1 dòng [PrintHost] trong [com.example.appghichiso.App]. Quan sát [PrinterHub].
 */
@Composable
fun PrintHost() {
    val hub = remember { PrinterHub.instance }
    val state by hub.state.collectAsState()
    val showPicker by hub.showPicker.collectAsState()
    val bonded by hub.bondedPrinters.collectAsState()

    // Yêu cầu quyền runtime khi vào trạng thái NeedPermission (Android 12+)
    val requestPermission = rememberBluetoothPermissionRequest { granted ->
        if (granted) hub.onPermissionGranted()
    }
    LaunchedEffect(state) {
        if (state is PrintingState.NeedPermission) requestPermission()
    }

    if (showPicker) PrinterPickerDialog(printers = bonded, hub = hub)

    val nonIdle = state !is PrintingState.Idle && state !is PrintingState.NeedPermission
    if (nonIdle) PrintStatusDialog(
        state = state,
        onDismiss = hub::reset,
        onChangePrinter = hub::changePrinter
    )
}

@Composable
private fun PrinterPickerDialog(printers: List<PrinterDevice>, hub: PrinterHub) {
    AlertDialog(
        onDismissRequest = hub::dismissPicker,
        title = {
            Text(
                "Chọn máy in",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (printers.isEmpty()) {
                    Text(
                        "Chưa có máy in nào được pair. Vào Cài đặt Bluetooth của điện thoại, pair máy in Xprinter trước rồi quay lại.",
                        color = Color.Gray
                    )
                } else {
                    Text(
                        "Chọn máy in nhiệt đã pair:",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp)) {
                        items(printers, key = { it.address }) { device ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(device.name.ifBlank { "Máy in không tên" })
                                    Text(
                                        device.address,
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                TextButton(onClick = { hub.selectPrinter(device) }) {
                                    Text("Chọn", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            HorizontalDivider(color = Color.LightGray)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = hub::dismissPicker) { Text("Đóng") }
        }
    )
}

@Composable
private fun PrintStatusDialog(
    state: PrintingState,
    onDismiss: () -> Unit,
    onChangePrinter: () -> Unit
) {
    when (state) {
        is PrintingState.Connecting, is PrintingState.Printing -> {
            val msg = if (state is PrintingState.Connecting) "Đang kết nối máy in..." else "Đang in, vui lòng đợi..."
            Dialog(
                onDismissRequest = { /* không cho hủy giữa chừng */ },
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Text(msg, fontWeight = FontWeight.Medium)
                }
            }
        }

        is PrintingState.Success -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("In thành công", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32)) },
                text = { Text("Đã gửi lệnh in ra máy in. Vui lòng đợi máy in xong.") },
                confirmButton = {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF2E7D32))
                    ) { Text("Đóng", color = Color.White) }
                },
                dismissButton = {
                    TextButton(onClick = onChangePrinter) { Text("Đổi máy in") }
                }
            )
        }

        is PrintingState.Error -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Lỗi in", fontWeight = FontWeight.Bold, color = Color.Red) },
                text = { Text((state as PrintingState.Error).message) },
                confirmButton = {
                    OutlinedButton(onClick = onChangePrinter) { Text("Đổi máy in") }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) { Text("Đóng") }
                }
            )
        }

        else -> { /* Idle/NeedPermission không hiển thị dialog */ }
    }
}
