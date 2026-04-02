package com.example.appghichiso.presentation.reading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appghichiso.di.AppStateHolder
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeterReadingScreen(
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    val viewModel = koinViewModel<MeterReadingViewModel>()
    val appStateHolder = koinInject<AppStateHolder>()
    val submitState by viewModel.submitState.collectAsStateWithLifecycle()

    val customer = appStateHolder.selectedCustomer ?: run {
        onBack(); return
    }

    var newIndexText by rememberSaveable { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val newIndex = newIndexText.toIntOrNull()
    val consumption = if (newIndex != null) (newIndex - customer.previousIndex) else null

    LaunchedEffect(submitState) {
        when (submitState) {
            is SubmitState.Success -> showSuccessDialog = true
            else -> Unit
        }
    }

    if (showConfirmDialog && newIndex != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Xác nhận ghi chỉ số") },
            text = {
                Column {
                    Text("Khách hàng: ${customer.customerName}")
                    Text("Chỉ số cũ: ${customer.previousIndex}")
                    Text("Chỉ số mới: $newIndex")
                    Text("Tiêu thụ: ${newIndex - customer.previousIndex} m³", fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(onClick = {
                    showConfirmDialog = false
                    viewModel.submit(
                        customerCode = customer.customerCode,
                        contractCode = customer.contractCode,
                        year = customer.year,
                        month = customer.month,
                        previousIndex = customer.previousIndex,
                        newIndex = newIndex
                    )
                }) { Text("Xác nhận") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Hủy") }
            }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("✅ Ghi chỉ số thành công") },
            text = { Text("Chỉ số ${customer.customerName} đã được cập nhật.") },
            confirmButton = {
                Button(onClick = {
                    showSuccessDialog = false
                    viewModel.resetState()
                    onSubmitSuccess()
                }) { Text("Quay lại danh sách") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ghi chỉ số") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Customer info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow("Tên khách hàng", customer.customerName)
                    InfoRow("Mã khách hàng", customer.customerCode)
                    InfoRow("Địa chỉ", customer.customerAddress)
                    InfoRow("Tuyến", customer.roadName)
                    InfoRow("Loại giá", customer.priceSchemaName)
                    InfoRow("Kỳ ghi", "${customer.month}/${customer.year}")
                }
            }

            HorizontalDivider()

            // Meter reading card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Thông tin đồng hồ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    InfoRow("Chỉ số kỳ trước", "${customer.previousIndex} m³")

                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newIndexText,
                        onValueChange = { newIndexText = it.filter { c -> c.isDigit() } },
                        label = { Text("Chỉ số kỳ này (m³)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        isError = newIndex != null && newIndex < customer.previousIndex
                    )

                    if (newIndex != null && newIndex < customer.previousIndex) {
                        Text(
                            "Chỉ số mới không được nhỏ hơn chỉ số cũ (${customer.previousIndex})",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (consumption != null && consumption >= 0) {
                        Spacer(Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tiêu thụ:", fontWeight = FontWeight.SemiBold)
                                Text(
                                    "$consumption m³",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }

            // Error message
            if (submitState is SubmitState.Error) {
                Text(
                    text = "⚠️ ${(submitState as SubmitState.Error).message}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Submit button
            Button(
                onClick = { showConfirmDialog = true },
                enabled = newIndex != null
                    && newIndex >= customer.previousIndex
                    && submitState !is SubmitState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (submitState is SubmitState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Lưu chỉ số", style = MaterialTheme.typography.bodyLarge)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f)
        )
    }
}

