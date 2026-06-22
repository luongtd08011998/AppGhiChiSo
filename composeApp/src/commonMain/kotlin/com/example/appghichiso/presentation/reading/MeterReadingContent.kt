package com.example.appghichiso.presentation.reading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appghichiso.domain.model.Customer
import com.example.appghichiso.ui.theme.Cyan
import com.example.appghichiso.ui.theme.OceanBlue
import com.example.appghichiso.ui.theme.OceanBlueDark

/**
 * Toàn bộ nội dung chính của màn hình ghi chỉ số:
 * - Navigation bar STT (← X/N →)
 * - Meter reading card (chỉ số cũ, tiêu thụ tháng trước, nhập chỉ số mới)
 * - Error message
 * - Submit buttons (Lưu / Lưu & Phát hành)
 * - Customer summary card (tên, địa chỉ, seri, SĐT, SMS)
 */
@Composable
internal fun ColumnScope.MeterReadingContent(
    customer: Customer,
    customerListSize: Int,
    currentIndex: Int,
    isRecorded: Boolean,
    newIndexText: String,
    onNewIndexChange: (String) -> Unit,
    submitState: SubmitState,
    prevConsumption: Int?,
    prevPeriodHuman: String,
    currentPeriodHuman: String,
    isAbnormalConsumption: Boolean,
    isLowConsumption: Boolean,
    consumption: Int?,
    currentInvoice: Any?,        // InvoiceDto — chỉ dùng để kiểm tra invoiceId > 0
    invoiceIdFromCustomer: Long?,
    phoneNumber: String?,
    smsNumber: String?,
    isPublishImmediately: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSubmit: (publishImmediate: Boolean) -> Unit
) {
    val newIndex = newIndexText.toIntOrNull()

    /* ── Navigation bar ── */
    CustomerNavigationBar(
        currentIndex      = currentIndex,
        total             = customerListSize,
        isRecorded        = isRecorded,
        customerCode      = customer.customerCode,
        onPrev            = onPrev,
        onNext            = onNext
    )

    /* ── Scrollable content ── */
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            /* Meter reading card */
            MeterReadingCard(
                customer              = customer,
                newIndexText          = newIndexText,
                onNewIndexChange      = onNewIndexChange,
                prevConsumption       = prevConsumption,
                prevPeriodHuman       = prevPeriodHuman,
                currentPeriodHuman    = currentPeriodHuman,
                isAbnormalConsumption = isAbnormalConsumption,
                isLowConsumption      = isLowConsumption,
                consumption           = consumption
            )

            /* Error feedback */
            if (submitState is SubmitState.Error) {
                Card(
                    shape  = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        "⚠️ ${submitState.message}",
                        color    = MaterialTheme.colorScheme.onErrorContainer,
                        style    = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            /* Submit buttons */
            val invoiceId = (currentInvoice as? com.example.appghichiso.data.api.dto.InvoiceDto)?.id
                ?: invoiceIdFromCustomer ?: 0L
            SubmitButtons(
                customer              = customer,
                newIndex              = newIndex,
                submitState           = submitState,
                isAbnormalConsumption = isAbnormalConsumption,
                isPublishImmediately  = isPublishImmediately,
                invoiceId             = invoiceId,
                onSubmit              = onSubmit
            )

            /* Customer summary card */
            CustomerSummaryCard(
                customer    = customer,
                phoneNumber = phoneNumber,
                smsNumber   = smsNumber
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ──────────────────────────────────────────────────────────────────────────
 *  Navigation bar  ← STT: X / N →
 * ────────────────────────────────────────────────────────────────────────── */
@Composable
private fun CustomerNavigationBar(
    currentIndex: Int,
    total: Int,
    isRecorded: Boolean,
    customerCode: String,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
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
            verticalAlignment     = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrev, enabled = currentIndex > 0, modifier = Modifier.size(48.dp)) {
                Surface(
                    shape = CircleShape,
                    color = if (currentIndex > 0) Color.White.copy(alpha = 0.2f) else Color.Transparent
                ) {
                    Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Khách hàng trước",
                            tint     = if (currentIndex > 0) Color.White else Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.18f)) {
                    Text(
                        "STT: ${currentIndex + 1} / $total",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White,
                        modifier   = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
                if (isRecorded) {
                    Text(
                        "● Đã ghi kỳ này",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = Color(0xFFA5F3A4),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            IconButton(onClick = onNext, enabled = currentIndex < total - 1, modifier = Modifier.size(48.dp)) {
                Surface(
                    shape = CircleShape,
                    color = if (currentIndex < total - 1) Color.White.copy(alpha = 0.2f) else Color.Transparent
                ) {
                    Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Khách hàng tiếp theo",
                            tint     = if (currentIndex < total - 1) Color.White else Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

/* ──────────────────────────────────────────────────────────────────────────
 *  MeterReadingCard  — Chỉ số kỳ trước, tiêu thụ tháng trước, nhập mới
 * ────────────────────────────────────────────────────────────────────────── */
@Composable
private fun MeterReadingCard(
    customer: Customer,
    newIndexText: String,
    onNewIndexChange: (String) -> Unit,
    prevConsumption: Int?,
    prevPeriodHuman: String,
    currentPeriodHuman: String,
    isAbnormalConsumption: Boolean,
    isLowConsumption: Boolean,
    consumption: Int?
) {
    val newIndex = newIndexText.toIntOrNull()
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle("🔢  Chỉ số đồng hồ")
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))

            /* Chỉ số kỳ trước */
            Card(shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Chỉ số kỳ trước", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${customer.previousIndex} m³", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
            }

            /* Tiêu thụ tháng trước */
            Spacer(Modifier.height(6.dp))
            Card(shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text("Tiêu thụ tháng trước", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(prevPeriodHuman, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f))
                    }
                    when {
                        prevConsumption == null -> CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        prevConsumption <= 0 -> Text("—", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        else -> Text("$prevConsumption m³", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            /* Chỉ số đã ghi (nếu đã recorded) */
            if (customer.isRecorded) {
                Spacer(Modifier.height(6.dp))
                Card(shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Chỉ số đã ghi", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text("${customer.currentIndex} m³", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            /* Nhập chỉ số mới */
            OutlinedTextField(
                value         = newIndexText,
                onValueChange = { onNewIndexChange(it.filter { c -> c.isDigit() }) },
                label         = { Text("NHẬP CHỈ SỐ MỚI (m³)", fontWeight = FontWeight.Bold) },
                singleLine    = true,
                enabled       = true,
                shape         = RoundedCornerShape(14.dp),
                textStyle     = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    color      = MaterialTheme.colorScheme.primary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier        = Modifier.fillMaxWidth(),
                isError         = newIndex != null && newIndex < customer.previousIndex,
                colors          = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.colorScheme.primary,
                    focusedLabelColor    = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            )

            if (newIndex != null && newIndex < customer.previousIndex) {
                Text("Chỉ số mới không được nhỏ hơn chỉ số cũ (${customer.previousIndex})",
                    color    = MaterialTheme.colorScheme.error,
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp))
            }

            /* Cảnh báo tiêu thụ bất thường */
            if (isAbnormalConsumption && consumption != null && prevConsumption != null && prevConsumption > 0) {
                val r = (consumption.toFloat() / prevConsumption * 10).toInt()
                val inlineRatio = "${r / 10}.${r % 10}"
                Spacer(Modifier.height(8.dp))
                Card(shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                    Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("⚠️", fontSize = 16.sp)
                            Text("Tiêu thụ bất thường — ${if (isLowConsumption) "chỉ bằng" else "gấp"} $inlineRatio lần so với $prevPeriodHuman!",
                                style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFFBF360C))
                        }
                        Text("$prevPeriodHuman: $prevConsumption m³  →  $currentPeriodHuman (dự kiến): $consumption m³",
                            style = MaterialTheme.typography.labelSmall, color = Color(0xFFBF360C),
                            modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }

            /* Hiển thị tiêu thụ */
            if (consumption != null) {
                Spacer(Modifier.height(10.dp))
                Card(shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAbnormalConsumption) Color(0xFFFFE0B2)
                                         else MaterialTheme.colorScheme.secondaryContainer)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("💧 Tiêu thụ kỳ đang ghi ($currentPeriodHuman):",
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = if (isAbnormalConsumption) Color(0xFFBF360C)
                                             else MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        Text("$consumption m³", style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isAbnormalConsumption) Color(0xFFE65100)
                                    else MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

/* ──────────────────────────────────────────────────────────────────────────
 *  SubmitButtons  — Lưu chỉ số + Lưu & Phát hành
 * ────────────────────────────────────────────────────────────────────────── */
@Composable
private fun SubmitButtons(
    customer: Customer,
    newIndex: Int?,
    submitState: SubmitState,
    isAbnormalConsumption: Boolean,
    isPublishImmediately: Boolean,
    invoiceId: Long,
    onSubmit: (publishImmediate: Boolean) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        /*
        Button(
            onClick  = { onSubmit(false) },
            enabled  = newIndex != null && newIndex >= customer.previousIndex && submitState !is SubmitState.Loading,
            shape    = RoundedCornerShape(16.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = if (isAbnormalConsumption) Color(0xFFE65100) else MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.weight(1f).height(56.dp)
        ) {
            if (submitState is SubmitState.Loading && !isPublishImmediately) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp,
                    color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(if (customer.isRecorded) "Cập nhật" else "Lưu chỉ số",
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            }
        }
        */

        Button(
            onClick  = { onSubmit(true) },
            enabled  = newIndex != null && newIndex >= customer.previousIndex
                    && submitState !is SubmitState.Loading && invoiceId > 0L,
            shape    = RoundedCornerShape(16.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (submitState is SubmitState.Loading && isPublishImmediately) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp,
                    color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Phát hành", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp, maxLines = 1)
            }
        }
    }
}

/* ──────────────────────────────────────────────────────────────────────────
 *  CustomerSummaryCard  — Tên, địa chỉ, seri, SĐT, SMS
 * ────────────────────────────────────────────────────────────────────────── */
@Composable
private fun CustomerSummaryCard(
    customer: Customer,
    phoneNumber: String?,
    smsNumber: String?
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            /* Tên + Mã KH */
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = customer.customerName.uppercase(),
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0277BD), maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFB2DFDB).copy(alpha = 0.7f)) {
                    Text(
                        customer.customerCode,
                        style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                        color = Color(0xFF004D40),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))

            /* Địa chỉ */
            if (customer.customerAddress.isNotBlank()) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Địa chỉ: ") }
                        append(customer.customerAddress)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            /* Seri đồng hồ */
            if (customer.contractSerial.isNotBlank()) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Seri ĐH: ") }
                        append(customer.contractSerial)
                    },
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            /* Phone row */
            Spacer(Modifier.height(8.dp))
            ContactRow(
                label = "Điện thoại",
                value = phoneNumber ?: customer.customerPhone.ifBlank { "—" }
            )

            /* SMS row */
            Spacer(Modifier.height(8.dp))
            ContactRow(
                label = "Số SMS",
                value = smsNumber ?: "Đang tải..."
            )
        }
    }
}

/* Shared contact row (SĐT / SMS) — read-only */
@Composable
private fun ContactRow(label: String, value: String) {
    Card(
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)) {
                Surface(shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(32.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Phone, contentDescription = null,
                            modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Column {
                    Text(label, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value, style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
