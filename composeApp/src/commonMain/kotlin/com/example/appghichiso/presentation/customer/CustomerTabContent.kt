package com.example.appghichiso.presentation.customer

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.appghichiso.domain.model.Customer
import com.example.appghichiso.presentation.common.ErrorView
import com.example.appghichiso.presentation.common.LoadingIndicator
import com.example.appghichiso.presentation.common.UiState

enum class CustomerFilter(val label: String) {
    ALL("Tất cả"),
    RECORDED("Đã ghi"),
    UNRECORDED("Chưa ghi")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerTabContent(
    state: UiState<List<Customer>>,
    searchQuery: String,
    isRecorded: (String) -> Boolean,
    onCustomerSelected: (Customer) -> Unit,
    onRefresh: () -> Unit
) {
    var isRefreshing by remember { mutableStateOf(false) }
    var filterType by remember { mutableStateOf(CustomerFilter.ALL) }

    when (state) {
        is UiState.Loading -> LoadingIndicator()
        is UiState.Error   -> ErrorView(message = state.message, onRetry = onRefresh)
        is UiState.Success -> {
            val allCustomers = state.data
            val recorded = allCustomers.count {
                it.isRecorded || isRecorded(it.customerCode)
            }
            val total = allCustomers.size
            val progress = if (total > 0) recorded / total.toFloat() else 0f

            val filtered = allCustomers.filter { c ->
                val isRec = c.isRecorded || isRecorded(c.customerCode)
                val matchFilter = when (filterType) {
                    CustomerFilter.ALL -> true
                    CustomerFilter.RECORDED -> isRec
                    CustomerFilter.UNRECORDED -> !isRec
                }

                val matchSearch = searchQuery.isBlank() ||
                    c.customerName.contains(searchQuery, ignoreCase = true) ||
                    c.customerCode.contains(searchQuery, ignoreCase = true) ||
                    c.contractSerial.contains(searchQuery, ignoreCase = true)

                matchFilter && matchSearch
            }

            Column(modifier = Modifier.fillMaxSize()) {
                /* Progress summary */
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Tiến độ ghi nước",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Đã ghi $recorded/$total KH",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.size(8.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.secondaryContainer,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )

                        Spacer(Modifier.size(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CustomerFilter.entries.forEach { filter ->
                                val isSelected = filterType == filter
                                val containerColor = if (isSelected) MaterialTheme.colorScheme.primary 
                                                     else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                                   else MaterialTheme.colorScheme.onSurfaceVariant
                                
                                val filterCount = when (filter) {
                                    CustomerFilter.ALL -> total
                                    CustomerFilter.RECORDED -> recorded
                                    CustomerFilter.UNRECORDED -> total - recorded
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = containerColor,
                                    onClick = { filterType = filter }
                                ) {
                                    Text(
                                        text = "${filter.label} ($filterCount)",
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = contentColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                /* Column Headers */
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            ),
                            modifier = Modifier.width(28.dp),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Mã KH",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            ),
                            modifier = Modifier.width(72.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "TÊN/ĐỊA CHỈ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Row(
                            modifier = Modifier.width(75.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SỐ SERIAL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD32F2F)
                                )
                            )
                            Spacer(Modifier.width(20.dp))
                        }
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
                        itemsIndexed(filtered, key = { _, customer -> customer.customerCode }) { index, customer ->
                            val isRec = customer.isRecorded || isRecorded(customer.customerCode)
                            CustomerCard(
                                index     = index,
                                customer  = customer,
                                isRecorded = isRec,
                                onClick   = {
                                    onCustomerSelected(customer)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerCard(
    index: Int,
    customer: Customer,
    isRecorded: Boolean,
    onClick: () -> Unit
) {
    val cardBackground = if (isRecorded)
        MaterialTheme.colorScheme.secondaryContainer
    else
        MaterialTheme.colorScheme.surface

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = cardBackground,
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: STT
                Text(
                    text = (index + 1).toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    ),
                    modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.width(4.dp))

                // Column 2: IDKH
                Text(
                    text = customer.customerCode,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.width(72.dp)
                )

                Spacer(Modifier.width(8.dp))

                // Column 3: TÊN/ĐỊA CHỈ
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = customer.customerName.uppercase(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (customer.hasInvoice) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF1565C0).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    "HĐ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1565C0)
                                    ),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Đ/c: ${customer.customerAddress}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Column 4: SỐ SERIAL & Chevron
                Row(
                    modifier = Modifier.width(75.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = customer.contractSerial,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(Modifier.width(4.dp))

                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
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
