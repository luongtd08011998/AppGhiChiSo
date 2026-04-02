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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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

    val customerList = appStateHolder.customerList
    val initialCustomer = appStateHolder.selectedCustomer ?: run { onBack(); return }

    // Tìm vị trí ban đầu trong danh sách
    val initialIndex = remember(initialCustomer.customerCode) {
        customerList.indexOfFirst { it.customerCode == initialCustomer.customerCode }
            .coerceAtLeast(0)
    }

    var currentIndex by rememberSaveable { mutableStateOf(initialIndex) }

    val customer = if (customerList.isNotEmpty() && currentIndex < customerList.size)
        customerList[currentIndex] else initialCustomer

    val total = customerList.size.coerceAtLeast(1)

    // Reset form mỗi khi chuyển sang khách hàng khác
    var newIndexText by rememberSaveable(currentIndex) {
        mutableStateOf(if (customer.currentIndex > 0) customer.currentIndex.toString() else "")
    }
    var showConfirmDialog by rememberSaveable(currentIndex) { mutableStateOf(false) }
    var showSuccessDialog by rememberSaveable(currentIndex) { mutableStateOf(false) }

    // Khi chuyển khách hàng, reset trạng thái submit
    LaunchedEffect(currentIndex) {
        viewModel.resetState()
        appStateHolder.selectedCustomer = customer
    }

    val newIndex = newIndexText.toIntOrNull()
    val consumption = if (newIndex != null && newIndex >= customer.previousIndex)
        newIndex - customer.previousIndex else null

    LaunchedEffect(submitState) {
        if (submitState is SubmitState.Success) showSuccessDialog = true
    }

    /* Confirm dialog */
    if (showConfirmDialog && newIndex != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Xác nhận ghi chỉ số") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Khách hàng: ${customer.customerName}")
                    Text("Mã KH: ${customer.customerCode}")
                    Text("Mã HĐ: ${customer.contractCode}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Chỉ số cũ: ${customer.previousIndex} m³")
                    Text("Chỉ số mới: $newIndex m³")
                    Text(
                        "Tiêu thụ: ${newIndex - customer.previousIndex} m³",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
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

    /* Success dialog */
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("✅ Ghi chỉ số thành công") },
            text = { Text("Chỉ số ${customer.customerName} đã được cập nhật.") },
            confirmButton = {
                Button(onClick = {
                    showSuccessDialog = false
                    viewModel.resetState()
                    appStateHolder.recordedCustomerCodes.add(customer.customerCode)
                    // Nếu còn khách hàng tiếp theo thì chuyển sang, ngược lại quay về danh sách
                    if (currentIndex < customerList.size - 1) {
                        currentIndex++
                    } else {
                        onSubmitSuccess()
                    }
                }) {
                    Text(
                        if (currentIndex < customerList.size - 1)
                            "Khách tiếp theo →" else "Quay lại danh sách"
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    viewModel.resetState()
                    appStateHolder.recordedCustomerCodes.add(customer.customerCode)
                    onSubmitSuccess()
                }) { Text("Về danh sách") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Ghi chỉ số", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Tháng ${customer.month}/${customer.year}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            /* ── Thanh điều hướng Trước / STT / Sau ── */
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (currentIndex > 0) currentIndex-- },
                        enabled = currentIndex > 0
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Khách hàng trước",
                            tint = if (currentIndex > 0)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "STT: ${currentIndex + 1} / $total",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )

                    }

                    IconButton(
                        onClick = { if (currentIndex < customerList.size - 1) currentIndex++ },
                        enabled = currentIndex < customerList.size - 1
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Khách hàng tiếp theo",
                            tint = if (currentIndex < customerList.size - 1)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        )
                    }
                }
            }

            /* Banner da ghi */
            if (customer.isRecorded || appStateHolder.recordedCustomerCodes.contains(customer.customerCode)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Text(
                        "⚠️  Kỳ này đã ghi: ${customer.currentIndex} m³ — bạn có thể cập nhật lại.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            /* THONG TIN KHACH HANG */
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionTitle("Thông tin khách hàng")
                    Spacer(Modifier.height(8.dp))
                    InfoRow("Tên khách hàng",  customer.customerName)
                    InfoRow("Mã khách hàng",   customer.customerCode)
                    InfoRow("Mã hợp đồng",     customer.contractCode)
                    if (customer.contractSerial.isNotBlank()) {
                        InfoRow("Số seri HĐ",  customer.contractSerial)
                    }
                    InfoRow("Địa chỉ",         customer.customerAddress)
                    if (customer.customerPhone.isNotBlank()) {
                        InfoRow("Điện thoại",  customer.customerPhone)
                    }
                    InfoRow("Tuyến",           customer.roadName)
                    InfoRow("Loại giá",        customer.priceSchemaName)
                    InfoRow("Kỳ ghi",          "Tháng ${customer.month}/${customer.year}")
                }
            }

            /* THONG TIN DONG HO */
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionTitle("Thông tin đồng hồ")
                    Spacer(Modifier.height(8.dp))
                    InfoRow("Chỉ số kỳ trước", "${customer.previousIndex} m³")
                    if (customer.isRecorded) {
                        InfoRow("Chỉ số đã ghi", "${customer.currentIndex} m³")
                    }
                    Spacer(Modifier.height(14.dp))
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
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (consumption != null) {
                        Spacer(Modifier.height(12.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tiêu thụ:", fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer)
                                Text("$consumption m³",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }

            if (submitState is SubmitState.Error) {
                Text(
                    text = "⚠️ ${(submitState as SubmitState.Error).message}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Button(
                onClick = { showConfirmDialog = true },
                enabled = newIndex != null && newIndex >= customer.previousIndex && submitState !is SubmitState.Loading,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (submitState is SubmitState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(
                        if (customer.isRecorded) "Cập nhật chỉ số" else "Lưu chỉ số",
                        style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.42f))
        Text(value, style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.58f))
    }
}
