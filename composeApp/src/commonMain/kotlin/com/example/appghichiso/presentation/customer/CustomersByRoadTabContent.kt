package com.example.appghichiso.presentation.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appghichiso.domain.model.CustomerByRoad
import com.example.appghichiso.presentation.common.ErrorView
import com.example.appghichiso.presentation.common.LoadingIndicator
import com.example.appghichiso.presentation.common.UiState
import com.example.appghichiso.presentation.reading.PhoneEditDialog
import com.example.appghichiso.presentation.reading.SmsEditDialog
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersByRoadTabContent(
    state: UiState<List<CustomerByRoad>>,
    searchQuery: String,
    onRefresh: () -> Unit
) {
    val viewModel = koinViewModel<CustomerViewModel>()
    val smsUpdateState by viewModel.smsUpdateState.collectAsStateWithLifecycle()
    val phoneUpdateState by viewModel.phoneUpdateState.collectAsStateWithLifecycle()

    var isRefreshing by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<CustomerByRoad?>(null) }

    // Track locally updated phone/sms for instant UI refresh
    var localPhone by remember { mutableStateOf<String?>(null) }
    var localSms by remember { mutableStateOf<String?>(null) }

    // Dialog state
    var showPhoneEditDialog by remember { mutableStateOf(false) }
    var showSmsEditDialog by remember { mutableStateOf(false) }

    // Detail dialog
    selectedCustomer?.let { customer ->
        val displayPhone = localPhone ?: customer.phone
        val displaySms = localSms ?: customer.sms

        CustomerByRoadDetailDialog(
            customer = customer.copy(phone = displayPhone, sms = displaySms),
            onDismiss = {
                selectedCustomer = null
                localPhone = null
                localSms = null
                viewModel.resetSmsUpdateState()
                viewModel.resetPhoneUpdateState()
            },
            onEditPhone = {
                viewModel.resetPhoneUpdateState()
                showPhoneEditDialog = true
            },
            onEditSms = {
                viewModel.resetSmsUpdateState()
                showSmsEditDialog = true
            }
        )
    }

    // Phone edit dialog
    if (showPhoneEditDialog && selectedCustomer != null) {
        PhoneEditDialog(
            customerName = selectedCustomer!!.customerName,
            customerCode = selectedCustomer!!.customerCode,
            initialPhone = localPhone ?: selectedCustomer!!.phone,
            updateState = phoneUpdateState,
            onConfirm = { newPhone ->
                viewModel.updatePhone(selectedCustomer!!.customerCode, newPhone)
                localPhone = newPhone
            },
            onDismiss = {
                showPhoneEditDialog = false
                viewModel.resetPhoneUpdateState()
            }
        )
    }

    // SMS edit dialog
    if (showSmsEditDialog && selectedCustomer != null) {
        SmsEditDialog(
            customerName = selectedCustomer!!.customerName,
            customerCode = selectedCustomer!!.customerCode,
            initialSms = localSms ?: selectedCustomer!!.sms,
            updateState = smsUpdateState,
            onConfirm = { newSms ->
                viewModel.updateSms(selectedCustomer!!.customerCode, newSms)
                localSms = newSms
            },
            onDismiss = {
                showSmsEditDialog = false
                viewModel.resetSmsUpdateState()
            }
        )
    }

    when (state) {
        is UiState.Loading -> LoadingIndicator()
        is UiState.Error -> ErrorView(message = state.message, onRetry = onRefresh)
        is UiState.Success -> {
            val filtered = state.data.filter { c ->
                searchQuery.isBlank() ||
                    c.customerName.contains(searchQuery, ignoreCase = true) ||
                    c.customerCode.contains(searchQuery, ignoreCase = true) ||
                    c.phone.contains(searchQuery, ignoreCase = true)
            }

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (searchQuery.isBlank()) "Không có khách hàng nào" else "Không tìm thấy khách hàng",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Summary card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Danh sách khách hàng",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    "${filtered.size} KH",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Column headers
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "STT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD32F2F)
                                ),
                                modifier = Modifier.width(28.dp),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "MÃ KH",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD32F2F)
                                ),
                                modifier = Modifier.width(72.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "TÊN / ĐỊA CHỈ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD32F2F)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            isRefreshing = true
                            onRefresh()
                            isRefreshing = false
                        }
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            itemsIndexed(filtered, key = { _, c -> c.id }) { index, customer ->
                                CustomerByRoadCard(
                                    index = index,
                                    customer = customer,
                                    onClick = {
                                        localPhone = null
                                        localSms = null
                                        viewModel.resetPhoneUpdateState()
                                        viewModel.resetSmsUpdateState()
                                        selectedCustomer = customer
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ── Customer Detail Dialog ── */
@Composable
private fun CustomerByRoadDetailDialog(
    customer: CustomerByRoad,
    onDismiss: () -> Unit,
    onEditPhone: () -> Unit,
    onEditSms: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column {
                Text(
                    customer.customerName.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        "Mã KH: ${customer.customerCode}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HorizontalDivider()

                // Địa chỉ
                DetailRow(
                    icon = Icons.Default.Home,
                    label = "Địa chỉ",
                    value = customer.customerAddress
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Điện thoại + nút sửa
                EditableContactRow(
                    icon = Icons.Default.Phone,
                    label = "Điện thoại",
                    value = customer.phone.ifBlank { "Chưa có" },
                    onEdit = onEditPhone
                )

                // SMS + nút sửa
                EditableContactRow(
                    icon = Icons.Default.Phone,
                    label = "Số SMS",
                    value = customer.sms.ifBlank { "Chưa có" },
                    onEdit = onEditSms
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Đóng", fontWeight = FontWeight.Bold)
            }
        }
    )
}

/* ── Editable contact row (giống ảnh: icon + label + value + nút bút chì) ── */
@Composable
private fun EditableContactRow(
    icon: ImageVector,
    label: String,
    value: String,
    onEdit: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Chỉnh sửa $label",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp).padding(top = 2.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/* ── Customer Row Card ── */
@Composable
private fun CustomerByRoadCard(
    index: Int,
    customer: CustomerByRoad,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = (index + 1).toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    ),
                    modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = customer.customerCode,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.width(72.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.customerName.uppercase(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Đ/c: ${customer.customerAddress}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp
            )
        }
    }
}
