package com.example.appghichiso.presentation.reading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appghichiso.di.AppStateHolder
import com.example.appghichiso.ui.theme.Cyan
import com.example.appghichiso.ui.theme.OceanBlue
import com.example.appghichiso.ui.theme.OceanBlueDark
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeterReadingScreen(
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    val viewModel      = koinViewModel<MeterReadingViewModel>()
    val appStateHolder = koinInject<AppStateHolder>()
    val submitState by viewModel.submitState.collectAsStateWithLifecycle()

    val customerList    = appStateHolder.customerList
    val initialCustomer = appStateHolder.selectedCustomer ?: run { onBack(); return }

    val initialIndex = remember(initialCustomer.customerCode) {
        customerList.indexOfFirst { it.customerCode == initialCustomer.customerCode }.coerceAtLeast(0)
    }

    var currentIndex by rememberSaveable { mutableStateOf(initialIndex) }

    val customer = if (customerList.isNotEmpty() && currentIndex < customerList.size)
        customerList[currentIndex] else initialCustomer
    val total = customerList.size.coerceAtLeast(1)

    var newIndexText by rememberSaveable(currentIndex) {
        mutableStateOf(if (customer.currentIndex > 0) customer.currentIndex.toString() else "")
    }
    var showConfirmDialog by rememberSaveable(currentIndex) { mutableStateOf(false) }
    var showSuccessDialog by rememberSaveable(currentIndex) { mutableStateOf(false) }

    LaunchedEffect(currentIndex) {
        viewModel.resetState()
        appStateHolder.selectedCustomer = customer
    }

    val newIndex    = newIndexText.toIntOrNull()
    val consumption = if (newIndex != null && newIndex >= customer.previousIndex)
        newIndex - customer.previousIndex else null

    LaunchedEffect(submitState) {
        if (submitState is SubmitState.Success) showSuccessDialog = true
    }

    /* ── Confirm dialog ── */
    if (showConfirmDialog && newIndex != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    "Xác nhận ghi chỉ số",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
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
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.submit(
                            customerCode  = customer.customerCode,
                            contractCode  = customer.contractCode,
                            year          = customer.year,
                            month         = customer.month,
                            previousIndex = customer.previousIndex,
                            newIndex      = newIndex
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Xác nhận") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Hủy") }
            }
        )
    }

    /* ── Success dialog ── */
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    "✅ Ghi chỉ số thành công",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            },
            text = { Text("Chỉ số ${customer.customerName} đã được cập nhật.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.resetState()
                        appStateHolder.recordedCustomerCodes.add(customer.customerCode)
                        if (currentIndex < customerList.size - 1) currentIndex++
                        else onSubmitSuccess()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (currentIndex < customerList.size - 1) "Khách tiếp theo →" else "Quay lại danh sách")
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
                        Text(
                            "Ghi chỉ số",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
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
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            /* ══ Gradient Navigation Bar (STT) ══ */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(colors = listOf(OceanBlueDark, OceanBlue, Cyan))
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    /* ← Prev button */
                    IconButton(
                        onClick  = { if (currentIndex > 0) currentIndex-- },
                        enabled  = currentIndex > 0,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Surface(
                            shape  = CircleShape,
                            color  = if (currentIndex > 0) Color.White.copy(alpha = 0.2f)
                                     else Color.Transparent
                        ) {
                            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Khách hàng trước",
                                    tint     = if (currentIndex > 0) Color.White
                                               else Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    /* STT pill */
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text     = "STT: ${currentIndex + 1} / $total",
                                style    = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color    = Color.White,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                            )
                        }
                        if (customer.isRecorded || appStateHolder.recordedCustomerCodes.contains(customer.customerCode)) {
                            Text(
                                "● Đã ghi kỳ này",
                                style  = MaterialTheme.typography.labelSmall,
                                color  = Color(0xFFA5F3A4),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    /* → Next button */
                    IconButton(
                        onClick  = { if (currentIndex < customerList.size - 1) currentIndex++ },
                        enabled  = currentIndex < customerList.size - 1,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (currentIndex < customerList.size - 1) Color.White.copy(alpha = 0.2f)
                                    else Color.Transparent
                        ) {
                            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Khách hàng tiếp theo",
                                    tint     = if (currentIndex < customerList.size - 1) Color.White
                                               else Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }

            /* ══ Cards section ══ */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                /* ── Already-recorded warning banner ── */
                if (customer.isRecorded || appStateHolder.recordedCustomerCodes.contains(customer.customerCode)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("⚠️", fontSize = 18.sp)
                            Text(
                                "Kỳ này đã ghi: ${customer.currentIndex} m³ — bạn có thể cập nhật lại.",
                                style      = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                /* ── Customer info card ── */
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors    = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        SectionTitle("👤  Thông tin khách hàng")
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color    = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
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

                /* ── Meter reading card ── */
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        SectionTitle("🔢  Chỉ số đồng hồ")
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color    = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )

                        /* Previous index display */
                        Card(
                            shape  = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Chỉ số kỳ trước",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${customer.previousIndex} m³",
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (customer.isRecorded) {
                            Spacer(Modifier.height(6.dp))
                            Card(
                                shape  = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Chỉ số đã ghi",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        "${customer.currentIndex} m³",
                                        style      = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color      = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        /* New index input */
                        OutlinedTextField(
                            value         = newIndexText,
                            onValueChange = { newIndexText = it.filter { c -> c.isDigit() } },
                            label         = { Text("Chỉ số kỳ này (m³)") },
                            singleLine    = true,
                            shape         = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier      = Modifier.fillMaxWidth(),
                            isError       = newIndex != null && newIndex < customer.previousIndex,
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor  = MaterialTheme.colorScheme.primary
                            )
                        )

                        if (newIndex != null && newIndex < customer.previousIndex) {
                            Text(
                                "Chỉ số mới không được nhỏ hơn chỉ số cũ (${customer.previousIndex})",
                                color    = MaterialTheme.colorScheme.error,
                                style    = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        /* Consumption display */
                        if (consumption != null) {
                            Spacer(Modifier.height(10.dp))
                            Card(
                                shape  = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "💧 Tiêu thụ kỳ này:",
                                        style      = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color      = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        "$consumption m³",
                                        style      = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color      = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }

                /* ── Error message ── */
                if (submitState is SubmitState.Error) {
                    Card(
                        shape  = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text     = "⚠️ ${(submitState as SubmitState.Error).message}",
                            color    = MaterialTheme.colorScheme.onErrorContainer,
                            style    = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                /* ── Submit button ── */
                Button(
                    onClick  = { showConfirmDialog = true },
                    enabled  = newIndex != null && newIndex >= customer.previousIndex
                               && submitState !is SubmitState.Loading,
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    if (submitState is SubmitState.Loading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                            color       = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            if (customer.isRecorded) "Cập nhật chỉ số" else "Lưu chỉ số",
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style      = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color      = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier  = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            value,
            style      = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier   = Modifier.weight(0.58f)
        )
    }
}
