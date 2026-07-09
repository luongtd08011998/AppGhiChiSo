package com.example.appghichiso.printer

import androidx.compose.runtime.Composable

/**
 * Trả về một lambda để yêu cầu quyền Bluetooth runtime. Trên Android gọi ActivityResultContracts;
 * iOS không cần (no-op, luôn granted). [onResult] nhận true nếu được cấp đủ quyền.
 */
@Composable
expect fun rememberBluetoothPermissionRequest(onResult: (Boolean) -> Unit): () -> Unit
