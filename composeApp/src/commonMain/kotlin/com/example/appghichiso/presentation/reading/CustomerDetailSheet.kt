package com.example.appghichiso.presentation.reading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appghichiso.domain.model.Customer

/**
 * Modal bottom sheet hiển thị chi tiết khách hàng và biểu đồ lịch sử tiêu thụ.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CustomerDetailSheet(
    customer: Customer,
    phoneNumber: String?,
    smsNumber: String?,
    historyState: HistoryState,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(vertical = 12.dp),
                color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                shape    = androidx.compose.foundation.shape.CircleShape
            ) {
                androidx.compose.foundation.layout.Box(
                    Modifier.size(width = 40.dp, height = 4.dp)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            /* Customer info card */
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(18.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
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
                        InfoRow("Số seri đồng hồ", customer.contractSerial)
                    }
                    InfoRow("Địa chỉ",         customer.customerAddress)
                    InfoRow("Điện thoại",      phoneNumber ?: customer.customerPhone.ifBlank { "—" })
                    InfoRow("SMS",             smsNumber ?: "—")
                    InfoRow("Tuyến",           customer.roadName)
                    InfoRow("Loại giá",        customer.priceSchemaName)
                    InfoRow("Kỳ ghi",          "Tháng ${customer.month}/${customer.year}")
                }
            }

            /* Consumption history chart */
            ConsumptionHistoryChart(historyState = historyState)
        }
    }
}
