package com.example.appghichiso.presentation.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appghichiso.di.AppStateHolder
import com.example.appghichiso.domain.model.Customer
import com.example.appghichiso.presentation.common.ErrorView
import com.example.appghichiso.presentation.common.LoadingIndicator
import com.example.appghichiso.presentation.common.UiState
import com.example.appghichiso.ui.theme.Cyan
import com.example.appghichiso.ui.theme.OceanBlue
import com.example.appghichiso.ui.theme.OceanBlueDark
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

enum class CustomerFilter(val label: String) {
    ALL("Tất cả"),
    RECORDED("Đã ghi"),
    UNRECORDED("Chưa ghi")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    roadCode: String,
    roadName: String,
    onCustomerSelected: (Customer) -> Unit,
    onBack: () -> Unit
) {
    val viewModel = koinViewModel<CustomerViewModel>()
    val appStateHolder = koinInject<AppStateHolder>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }
    var filterType by remember { mutableStateOf(CustomerFilter.ALL) }

    LaunchedEffect(roadCode) { viewModel.loadCustomers(roadCode) }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            appStateHolder.customerList = (uiState as UiState.Success<List<Customer>>).data
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(roadName, maxLines = 1, fontWeight = FontWeight.Bold)
                        Text(
                            "${viewModel.currentMonth}/${viewModel.currentYear}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
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
        ) {
            /* ── Gradient search banner ── */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(OceanBlueDark, OceanBlue, Cyan.copy(alpha = 0f)),
                            startY = 0f, endY = 200f
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm theo tên, mã KH, số seri...") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor      = Color.White.copy(alpha = 0.4f),
                        focusedBorderColor        = Color.White,
                        unfocusedContainerColor   = Color.White.copy(alpha = 0.9f),
                        focusedContainerColor     = Color.White,
                        unfocusedPlaceholderColor = Color(0xFF888888),
                        focusedPlaceholderColor   = Color(0xFF999999),
                        unfocusedTextColor        = Color(0xFF333333),
                        focusedTextColor          = Color(0xFF222222),
                        cursorColor               = OceanBlue
                    )
                )
            }

            when (val state = uiState) {
                is UiState.Loading -> LoadingIndicator()
                is UiState.Error   -> ErrorView(message = state.message, onRetry = viewModel::refresh)
                is UiState.Success -> {
                    val allCustomers = state.data
                    val recorded = allCustomers.count {
                        it.isRecorded || viewModel.isRecorded(it.customerCode)
                    }
                    val total = allCustomers.size
                    val progress = if (total > 0) recorded / total.toFloat() else 0f

                    val filtered = allCustomers.filter { c ->
                        val isRec = c.isRecorded || viewModel.isRecorded(c.customerCode)
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

                    /* Progress summary */
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
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

                    /* Column Headers matching the mockup */
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Column 1: STT Column
                            Text(
                                text = "STT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD32F2F) // Red color matching mockup
                                ),
                                modifier = Modifier.width(28.dp),
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.width(4.dp))

                            // Column 2: IDKH Column
                            Text(
                                text = "Mã KH",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD32F2F)
                                ),
                                modifier = Modifier.width(72.dp)
                            )

                            Spacer(Modifier.width(8.dp))

                            // Column 3: TÊN/SỐ NHÀ Column
                            Text(
                                text = "TÊN/ĐỊA CHỈ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD32F2F)
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(Modifier.width(8.dp))

                            // Column 4: SỐ SERIAL Column (aligned with numbers)
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
                                Spacer(Modifier.width(20.dp)) // Aligns to the serial numbers (accounting for chevron)
                            }
                        }
                    }

                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            isRefreshing = true
                            viewModel.refresh()
                            isRefreshing = false
                        }
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                start = 0.dp, end = 0.dp, top = 0.dp, bottom = 24.dp
                            )
                        ) {
                            itemsIndexed(filtered, key = { _, customer -> customer.customerCode }) { index, customer ->
                                val isRec = customer.isRecorded || viewModel.isRecorded(customer.customerCode)
                                CustomerCard(
                                    index     = index,
                                    customer  = customer,
                                    isRecorded = isRec,
                                    onClick   = {
                                        appStateHolder.selectedCustomer = customer
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

                // Column 3: TÊN/SỐ NHÀ
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Name in uppercase bold
                    Text(
                        text = customer.customerName.uppercase(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(Modifier.height(4.dp))

                    // Address (Displaying entire address: no maxLines restriction)
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
