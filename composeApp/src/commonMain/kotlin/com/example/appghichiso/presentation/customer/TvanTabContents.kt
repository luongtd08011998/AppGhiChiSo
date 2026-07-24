package com.example.appghichiso.presentation.customer

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.appghichiso.data.api.dto.InvoiceDto
import com.example.appghichiso.presentation.common.ErrorView
import com.example.appghichiso.presentation.common.LoadingIndicator
import com.example.appghichiso.presentation.common.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToPublishTabContent(
    state: UiState<List<InvoiceDto>>,
    searchQuery: String,
    selectedInvoiceIds: SnapshotStateList<Long>,
    isPublishing: Boolean = false,
    onPublishClick: (List<Long>) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    isLoadingMore: Boolean = false
) {
    var isRefreshing by remember { mutableStateOf(false) }

    when (state) {
        is UiState.Loading -> LoadingIndicator()
        is UiState.Error -> ErrorView(message = state.message, onRetry = onRefresh)
        is UiState.Success -> {
            val allToPublish = state.data
            val filtered = allToPublish.filter { inv ->
                searchQuery.isBlank() ||
                    (inv.custName ?: "").contains(searchQuery, ignoreCase = true) ||
                    (inv.custCode ?: "").contains(searchQuery, ignoreCase = true)
            }

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Không có hóa đơn nào chưa phát hành",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp)) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedInvoiceIds.size == filtered.size && filtered.isNotEmpty(),
                                    onCheckedChange = { checked ->
                                        selectedInvoiceIds.clear()
                                        if (checked) {
                                            selectedInvoiceIds.addAll(filtered.map { it.id })
                                        }
                                    },
                                    enabled = !isPublishing
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Chọn tất cả (${filtered.size})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
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
                            val listState = rememberLazyListState()
                            val isAtBottom by remember {
                                derivedStateOf {
                                    val layoutInfo = listState.layoutInfo
                                    val totalItems = layoutInfo.totalItemsCount
                                    val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
                                    lastVisibleItemIndex > (totalItems - 5) && totalItems > 0
                                }
                            }

                            LaunchedEffect(isAtBottom) {
                                if (isAtBottom) onLoadMore()
                            }

                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                itemsIndexed(filtered, key = { _, inv -> inv.id }) { index, invoice ->
                                    val isSelected = selectedInvoiceIds.contains(invoice.id)
                                    ToPublishItem(
                                        index = index,
                                        invoice = invoice,
                                        isSelected = isSelected,
                                        onCheckedChange = { checked ->
                                            if (!isPublishing) {
                                                if (checked) {
                                                    selectedInvoiceIds.add(invoice.id)
                                                } else {
                                                    selectedInvoiceIds.remove(invoice.id)
                                                }
                                            }
                                        }
                                    )
                                }
                                if (isLoadingMore) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 2.dp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 8.dp
                    ) {
                        Button(
                            onClick = {
                                if (!isPublishing) onPublishClick(selectedInvoiceIds.toList())
                            },
                            enabled = selectedInvoiceIds.isNotEmpty() && !isPublishing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isPublishing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Đang phát hành...", fontWeight = FontWeight.Bold)
                            } else {
                                Text(
                                    "Phát hành TVAN (${selectedInvoiceIds.size})",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToPublishItem(
    index: Int,
    invoice: InvoiceDto,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                (index + 1).toString(),
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F),
                modifier = Modifier.width(28.dp),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    invoice.custName?.uppercase() ?: "",
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Mã: ${invoice.custCode} • Chỉ số: ${invoice.oldIndex} → ${invoice.newIndex}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Đ/c: ${invoice.custAddress ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                formatCurrency(invoice.totalAmount) + "đ",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtTabContent(
    state: UiState<List<InvoiceDto>>,
    searchQuery: String,
    onPayClick: (InvoiceDto) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    isLoadingMore: Boolean = false
) {
    var isRefreshing by remember { mutableStateOf(false) }

    when (state) {
        is UiState.Loading -> LoadingIndicator()
        is UiState.Error -> ErrorView(message = state.message, onRetry = onRefresh)
        is UiState.Success -> {
            val allDebt = state.data
            val filtered = allDebt.filter { inv ->
                searchQuery.isBlank() ||
                    (inv.custName ?: "").contains(searchQuery, ignoreCase = true) ||
                    (inv.custCode ?: "").contains(searchQuery, ignoreCase = true)
            }

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Không có hóa đơn nợ nào",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        isRefreshing = true
                        onRefresh()
                        isRefreshing = false
                    }
                ) {
                    val listState = rememberLazyListState()
                    val isAtBottom by remember {
                        derivedStateOf {
                            val layoutInfo = listState.layoutInfo
                            val totalItems = layoutInfo.totalItemsCount
                            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
                            lastVisibleItemIndex > (totalItems - 5) && totalItems > 0
                        }
                    }

                    LaunchedEffect(isAtBottom) {
                        if (isAtBottom) onLoadMore()
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        itemsIndexed(filtered, key = { _, inv -> inv.id }) { index, invoice ->
                            DebtItem(
                                index = index,
                                invoice = invoice,
                                onPayClick = { onPayClick(invoice) }
                            )
                        }
                        if (isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DebtItem(
    index: Int,
    invoice: InvoiceDto,
    onPayClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                (index + 1).toString(),
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F),
                modifier = Modifier.width(28.dp),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        invoice.custName?.uppercase() ?: "",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    // Badge kỳ tháng — hiển thị khi yearMonth có giá trị
                    invoice.yearMonth?.let { ym ->
                        val display = if (ym.length == 6) {
                            "Kỳ ${ym.substring(4, 6)}/${ym.substring(0, 4)}"
                        } else ym
                        Spacer(Modifier.width(6.dp))
                        Text(
                            display,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .background(
                                    color = Color(0xFFF57C00),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    "Mã: ${invoice.custCode} • Chỉ số: ${invoice.oldIndex} → ${invoice.newIndex}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Đ/c: ${invoice.custAddress ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatCurrency(invoice.totalAmount) + "đ",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = onPayClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Thu Tiền", style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaidTabContent(
    state: UiState<List<InvoiceDto>>,
    searchQuery: String,
    onReceiptClick: (InvoiceDto) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    isLoadingMore: Boolean = false
) {
    var isRefreshing by remember { mutableStateOf(false) }

    when (state) {
        is UiState.Loading -> LoadingIndicator()
        is UiState.Error -> ErrorView(message = state.message, onRetry = onRefresh)
        is UiState.Success -> {
            val allPaid = state.data
            val filtered = allPaid.filter { inv ->
                searchQuery.isBlank() ||
                    (inv.custName ?: "").contains(searchQuery, ignoreCase = true) ||
                    (inv.custCode ?: "").contains(searchQuery, ignoreCase = true)
            }

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Không có hóa đơn đã thanh toán nào",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        isRefreshing = true
                        onRefresh()
                        isRefreshing = false
                    }
                ) {
                    val listState = rememberLazyListState()
                    val isAtBottom by remember {
                        derivedStateOf {
                            val layoutInfo = listState.layoutInfo
                            val totalItems = layoutInfo.totalItemsCount
                            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
                            lastVisibleItemIndex > (totalItems - 5) && totalItems > 0
                        }
                    }

                    LaunchedEffect(isAtBottom) {
                        if (isAtBottom) onLoadMore()
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        itemsIndexed(filtered, key = { _, inv -> inv.id }) { index, invoice ->
                            PaidItem(
                                index = index,
                                invoice = invoice,
                                onReceiptClick = { onReceiptClick(invoice) }
                            )
                        }
                        if (isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaidItem(
    index: Int,
    invoice: InvoiceDto,
    onReceiptClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                (index + 1).toString(),
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F),
                modifier = Modifier.width(28.dp),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    invoice.custName?.uppercase() ?: "",
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Mã: ${invoice.custCode} • Kỳ: ${invoice.yearMonth}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Đ/c: ${invoice.custAddress ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatCurrency(invoice.totalAmount) + "đ",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = onReceiptClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Biên Nhận", style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val amountLong = amount.toLong()
    if (amountLong == 0L) return "0"
    val stringBuilder = StringBuilder(amountLong.toString())
    var i = stringBuilder.length - 3
    while (i > 0) {
        stringBuilder.insert(i, ".")
        i -= 3
    }
    return stringBuilder.toString()
}
