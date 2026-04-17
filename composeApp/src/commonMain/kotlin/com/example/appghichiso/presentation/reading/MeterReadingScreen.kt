package com.example.appghichiso.presentation.reading

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
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
    val previousMonthConsumption by viewModel.previousMonthConsumption.collectAsStateWithLifecycle()
    val historyState by viewModel.historyState.collectAsStateWithLifecycle()

    val customerList    = appStateHolder.customerList
    val initialCustomer = appStateHolder.selectedCustomer ?: run { onBack(); return }

    val initialIndex = remember(initialCustomer.customerCode) {
        customerList.indexOfFirst { it.customerCode == initialCustomer.customerCode }.coerceAtLeast(0)
    }

    var currentIndex by rememberSaveable { mutableStateOf(initialIndex) }

    val customer = if (customerList.isNotEmpty() && currentIndex < customerList.size)
        customerList[currentIndex] else initialCustomer
    val total = customerList.size.coerceAtLeast(1)

    val (prevBillYear, prevBillMonth) = remember(customer.year, customer.month) {
        if (customer.month == 1) customer.year - 1 to 12
        else customer.year to (customer.month - 1)
    }
    val prevPeriodHuman = remember(prevBillYear, prevBillMonth) {
        "Tháng $prevBillMonth/$prevBillYear"
    }
    val prevYearMonthToken = remember(prevBillYear, prevBillMonth) {
        "${prevBillYear}${prevBillMonth.toString().padStart(2, '0')}"
    }
    val currentPeriodHuman = remember(customer.year, customer.month) {
        "Tháng ${customer.month}/${customer.year}"
    }

    var newIndexText by rememberSaveable(currentIndex) {
        mutableStateOf(if (customer.currentIndex > 0) customer.currentIndex.toString() else "")
    }
    var showWarningDialog  by rememberSaveable(currentIndex) { mutableStateOf(false) }
    var showConfirmDialog  by rememberSaveable(currentIndex) { mutableStateOf(false) }
    var showSuccessDialog  by rememberSaveable(currentIndex) { mutableStateOf(false) }

    LaunchedEffect(currentIndex) {
        viewModel.resetState()
        appStateHolder.selectedCustomer = customer
        viewModel.loadCustomerData(
            customerCode = customer.customerCode,
            year = customer.year,
            month = customer.month,
            previousIndex = customer.previousIndex
        )
    }

    val newIndex    = newIndexText.toIntOrNull()
    val consumption = if (newIndex != null && newIndex >= customer.previousIndex)
        newIndex - customer.previousIndex else null

    /* Kiểm tra cảnh báo: tiêu thụ tháng này >= 2 lần hoặc <= 1/2 tháng trước */
    val prevConsumption = previousMonthConsumption
    val isHighConsumption = consumption != null
        && prevConsumption != null
        && prevConsumption > 0
        && consumption >= prevConsumption * 2

    val isLowConsumption = consumption != null
        && prevConsumption != null
        && prevConsumption > 0
        && consumption * 2 <= prevConsumption

    val isAbnormalConsumption = isHighConsumption || isLowConsumption

    LaunchedEffect(submitState) {
        if (submitState is SubmitState.Success) showSuccessDialog = true
    }

    /* ── Warning + Confirm dialog (gộp 1 bước) ── */
    if (showWarningDialog && newIndex != null && consumption != null) {
        val ratioText = if (prevConsumption != null && prevConsumption > 0) {
            val r = (consumption.toFloat() / prevConsumption * 10).toInt()
            "${r / 10}.${r % 10}"
        } else "—"

        val abnormalHint = when {
            isHighConsumption -> "tăng cao"
            isLowConsumption -> "giảm mạnh"
            else -> "bất thường"
        }

        AlertDialog(
            onDismissRequest = { showWarningDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "⚠️ Cảnh báo tiêu thụ $abnormalHint",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
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
                    /* Thông tin khách hàng */
                    Text(
                        "Khách hàng: ${customer.customerName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Mã KH: ${customer.customerCode}  •  Kỳ: ${customer.month}/${customer.year}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    /* Chỉ số */
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Chỉ số cũ:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "${customer.previousIndex} m³",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Chỉ số mới:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "$newIndex m³",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                    /* So sánh tiêu thụ */
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                "Tiêu thụ $prevPeriodHuman:",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "yearMonth=$prevYearMonthToken",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "$prevConsumption m³",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Tiêu thụ kỳ đang ghi ($currentPeriodHuman):",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        Text(
                            "$consumption m³",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFE65100)
                        )
                    }

                    /* Cảnh báo tỉ lệ */
                    Card(
                        shape  = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                    ) {
                        Text(
                            "Tiêu thụ kỳ đang ghi ($currentPeriodHuman) ${if (isLowConsumption) "chỉ bằng" else "gấp"} $ratioText lần so với $prevPeriodHuman ($prevYearMonthToken). " +
                                "Hãy kiểm tra lại đồng hồ nước!",
                            style  = MaterialTheme.typography.bodySmall,
                            color  = Color(0xFFBF360C),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showWarningDialog = false
                        viewModel.submit(
                            customerCode  = customer.customerCode,
                            contractCode  = customer.contractCode,
                            year          = customer.year,
                            month         = customer.month,
                            previousIndex = customer.previousIndex,
                            newIndex      = newIndex
                        )
                    },
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                ) { Text("Đồng ý, lưu chỉ số") }
            },
            dismissButton = {
                TextButton(onClick = { showWarningDialog = false }) {
                    Text("Hủy, nhập lại")
                }
            }
        )
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

                        /* Sản lượng tiêu thụ nước tháng trước — GET readings?yearMonth=… (vd kỳ 3 → 202502) */
                        Spacer(Modifier.height(6.dp))
                        Card(
                            shape  = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        "Sản lượng tiêu thụ nước tháng trước",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "$prevPeriodHuman · yearMonth=$prevYearMonthToken",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                                    )
                                }
                                when {
                                    prevConsumption == null ->
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            strokeWidth = 2.dp
                                        )
                                    prevConsumption <= 0 ->
                                        Text(
                                            "—",
                                            style      = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Medium,
                                            color      = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    else ->
                                        Text(
                                            "$prevConsumption m³",
                                            style      = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color      = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                }
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

                        /* Cảnh báo tiêu thụ bất thường (cao hoặc thấp) */
                        val safeConsumption = consumption
                        val safePrevConsumption = prevConsumption
                        if (isAbnormalConsumption && safeConsumption != null && safePrevConsumption != null && safePrevConsumption > 0) {
                            val r = (safeConsumption.toFloat() / safePrevConsumption * 10).toInt()
                            val inlineRatio = "${r / 10}.${r % 10}"
                            Spacer(Modifier.height(8.dp))
                            Card(
                                shape  = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("⚠️", fontSize = 16.sp)
                                        Text(
                                            "Tiêu thụ bất thường — ${if (isLowConsumption) "chỉ bằng" else "gấp"} $inlineRatio lần so với $prevPeriodHuman!",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFBF360C)
                                        )
                                    }
                                    Text(
                                        "$prevPeriodHuman: $safePrevConsumption m³  →  $currentPeriodHuman (dự kiến): $safeConsumption m³",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFBF360C),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }

                        /* Consumption display */
                        if (consumption != null) {
                            Spacer(Modifier.height(10.dp))
                            Card(
                                shape  = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isAbnormalConsumption)
                                        Color(0xFFFFE0B2)
                                    else
                                        MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Text(
                                            "💧 Tiêu thụ kỳ đang ghi ($currentPeriodHuman):",
                                            style      = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color      = if (isAbnormalConsumption) Color(0xFFBF360C)
                                                         else MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                    Text(
                                        "$consumption m³",
                                        style      = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color      = if (isAbnormalConsumption) Color(0xFFE65100)
                                                     else MaterialTheme.colorScheme.secondary
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
                    onClick  = {
                        if (isAbnormalConsumption) showWarningDialog = true
                        else showConfirmDialog = true
                    },
                    enabled  = newIndex != null && newIndex >= customer.previousIndex
                               && submitState !is SubmitState.Loading,
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = if (isAbnormalConsumption) Color(0xFFE65100)
                                         else MaterialTheme.colorScheme.primary
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

                /* ── Consumption history chart ── */
                ConsumptionHistoryChart(historyState = historyState)

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ConsumptionHistoryChart(historyState: HistoryState) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle("📊  Lịch sử tiêu thụ nước")
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color    = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )

            when (historyState) {
                is HistoryState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                }
                is HistoryState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Không thể tải lịch sử tiêu thụ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is HistoryState.Success -> {
                    val points = historyState.points
                    if (points.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Chưa có dữ liệu lịch sử",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        ConsumptionBarChart(points = points)
                    }
                }
                is HistoryState.Idle -> { /* chưa load */ }
            }
        }
    }
}

@Composable
private fun ConsumptionBarChart(points: List<ConsumptionPoint>) {
    val barColor   = Color(0xFF1565C0)
    val labelColor = Color(0xFF455A64)
    val valueColor = Color(0xFF0D47A1)
    val textMeasurer = rememberTextMeasurer()
    val maxConsumption = points.maxOfOrNull { it.consumption }?.coerceAtLeast(1) ?: 1

    val labelStyle = TextStyle(fontSize = 9.sp, color = labelColor)
    val valueStyle = TextStyle(fontSize = 8.sp, color = valueColor, fontWeight = FontWeight.Bold)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val chartHeight  = size.height - 36.dp.toPx()   // bottom reserved for labels
        val chartWidth   = size.width
        val barCount     = points.size
        val totalGap     = chartWidth * 0.25f
        val barWidth     = (chartWidth - totalGap) / barCount
        val gap          = totalGap / (barCount + 1)

        points.forEachIndexed { i, point ->
            val barH   = (point.consumption.toFloat() / maxConsumption) * chartHeight
            val left   = gap + i * (barWidth + gap)
            val top    = chartHeight - barH
            val right  = left + barWidth
            val bottom = chartHeight

            /* Bar */
            drawRoundRect(
                color       = barColor.copy(alpha = 0.85f),
                topLeft     = Offset(left, top),
                size        = Size(barWidth, barH),
                cornerRadius = CornerRadius(6f, 6f)
            )

            /* Month label below bar */
            val labelLayout = textMeasurer.measure(point.label, labelStyle)
            drawText(
                textLayoutResult = labelLayout,
                topLeft = Offset(
                    x = left + (barWidth - labelLayout.size.width) / 2,
                    y = chartHeight + 4.dp.toPx()
                )
            )

            /* Value above bar */
            if (point.consumption > 0) {
                val valueLayout = textMeasurer.measure("${point.consumption}", valueStyle)
                drawText(
                    textLayoutResult = valueLayout,
                    topLeft = Offset(
                        x = left + (barWidth - valueLayout.size.width) / 2,
                        y = (top - valueLayout.size.height - 2.dp.toPx()).coerceAtLeast(0f)
                    )
                )
            }
        }

        /* Baseline */
        drawLine(
            color       = labelColor.copy(alpha = 0.4f),
            start       = Offset(0f, chartHeight),
            end         = Offset(chartWidth, chartHeight),
            strokeWidth = 1.dp.toPx()
        )
    }

    Spacer(Modifier.height(4.dp))
    Text(
        "Đơn vị: m³  •  ${points.size} tháng gần nhất",
        style  = MaterialTheme.typography.labelSmall,
        color  = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
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
