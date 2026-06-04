package com.example.appghichiso.presentation.reading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/* ──────────────────────────────────────────────────────────────────────────
 *  SmsEditDialog  — Cập nhật số SMS nhận thông báo tiền nước
 * ────────────────────────────────────────────────────────────────────────── */
@Composable
internal fun SmsEditDialog(
    customerName: String,
    customerCode: String,
    initialSms: String?,
    updateState: SmsUpdateState,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var editSmsText by remember(initialSms) { mutableStateOf(initialSms ?: "") }
    val isLoading = updateState is SmsUpdateState.Loading

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) onDismiss()
        },
        shape = RoundedCornerShape(20.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Cập nhật số SMS",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary
                )
                Text(
                    "$customerName ($customerCode)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value         = editSmsText,
                    onValueChange = { editSmsText = it.filter { c -> c.isDigit() } },
                    label         = { Text("Số điện thoại SMS", fontWeight = FontWeight.Bold) },
                    placeholder   = { Text("VD: 0912345678") },
                    leadingIcon   = {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(20.dp))
                    },
                    singleLine    = true,
                    shape         = RoundedCornerShape(14.dp),
                    textStyle     = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier      = Modifier.fillMaxWidth(),
                    enabled       = !isLoading,
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = MaterialTheme.colorScheme.primary,
                        focusedLabelColor    = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )
                UpdateStateFeedback(state = updateState)
            }
        },
        confirmButton = {
            Button(
                onClick  = { onConfirm(editSmsText.trim()) },
                enabled  = editSmsText.isNotBlank() && !isLoading && updateState !is SmsUpdateState.Success,
                shape    = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Cập nhật")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Đóng") }
        }
    )
}

/* ──────────────────────────────────────────────────────────────────────────
 *  PhoneEditDialog  — Cập nhật số điện thoại liên hệ
 * ────────────────────────────────────────────────────────────────────────── */
@Composable
internal fun PhoneEditDialog(
    customerName: String,
    customerCode: String,
    initialPhone: String,
    updateState: SmsUpdateState,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var editPhoneText by remember(initialPhone) { mutableStateOf(initialPhone) }
    val isLoading = updateState is SmsUpdateState.Loading

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) onDismiss()
        },
        shape = RoundedCornerShape(20.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Cập nhật Điện thoại",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary
                )
                Text(
                    "$customerName ($customerCode)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value         = editPhoneText,
                    onValueChange = { editPhoneText = it.filter { c -> c.isDigit() } },
                    label         = { Text("Số điện thoại liên hệ", fontWeight = FontWeight.Bold) },
                    placeholder   = { Text("VD: 0912345678") },
                    leadingIcon   = {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(20.dp))
                    },
                    singleLine    = true,
                    shape         = RoundedCornerShape(14.dp),
                    textStyle     = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier      = Modifier.fillMaxWidth(),
                    enabled       = !isLoading,
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = MaterialTheme.colorScheme.primary,
                        focusedLabelColor    = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )
                UpdateStateFeedback(state = updateState)
            }
        },
        confirmButton = {
            Button(
                onClick  = { onConfirm(editPhoneText.trim()) },
                enabled  = editPhoneText.isNotBlank() && !isLoading && updateState !is SmsUpdateState.Success,
                shape    = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Cập nhật")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Đóng") }
        }
    )
}

/* ──────────────────────────────────────────────────────────────────────────
 *  UpdateStateFeedback  — Hiển thị kết quả cập nhật (Error / Success)
 * ────────────────────────────────────────────────────────────────────────── */
@Composable
private fun UpdateStateFeedback(state: SmsUpdateState) {
    when (state) {
        is SmsUpdateState.Error -> {
            Card(
                shape  = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    state.message,
                    color    = MaterialTheme.colorScheme.onErrorContainer,
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
        is SmsUpdateState.Success -> {
            Card(
                shape  = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Cập nhật thành công!",
                        color      = Color(0xFF2E7D32),
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        else -> {}
    }
}
