package com.example.appghichiso.printer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext

/**
 * Android actual: yêu cầu BLUETOOTH_CONNECT (+ BLUETOOTH_SCAN) runtime trên Android 12+.
 * Với Android < 12 (API ≤ 30) quyền khai báo trong manifest là đủ → luôn granted.
 */
@Composable
actual fun rememberBluetoothPermissionRequest(onResult: (Boolean) -> Unit): () -> Unit {
    val context = LocalContext.current
    val permissions: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
    } else {
        emptyArray()
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (permissions.isEmpty()) {
            onResult(true)
        } else {
            onResult(permissions.all { result[it] == true })
        }
    }

    return remember(permissions) {
        {
            if (permissions.isEmpty()) {
                onResult(true)
            } else {
                val alreadyGranted = permissions.all {
                    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                }
                if (alreadyGranted) onResult(true) else launcher.launch(permissions)
            }
        }
    }
}
