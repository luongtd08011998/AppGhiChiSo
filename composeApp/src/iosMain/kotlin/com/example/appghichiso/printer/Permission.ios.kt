package com.example.appghichiso.printer

import androidx.compose.runtime.Composable

/**
 * iOS actual: không cần xin quyền runtime (tính năng in chưa hỗ trợ iOS). Luôn granted.
 */
@Composable
actual fun rememberBluetoothPermissionRequest(onResult: (Boolean) -> Unit): () -> Unit = {
    onResult(true)
}
