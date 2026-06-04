package com.example.appghichiso.presentation.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appghichiso.di.AppStateHolder
import com.example.appghichiso.domain.model.Customer
import com.example.appghichiso.data.api.dto.InvoiceDto
import com.example.appghichiso.presentation.common.UiState
import com.example.appghichiso.presentation.reading.ReceiptDialog
import com.example.appghichiso.presentation.reading.InvoicePaperDialog
import com.example.appghichiso.presentation.reading.TvanActionState
import com.example.appghichiso.ui.theme.Cyan
import com.example.appghichiso.ui.theme.OceanBlue
import com.example.appghichiso.ui.theme.OceanBlueDark
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

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
    val toPublishState by viewModel.toPublishState.collectAsStateWithLifecycle()
    val debtState by viewModel.debtState.collectAsStateWithLifecycle()
    val paidState by viewModel.paidState.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val tvanActionState by viewModel.tvanActionState.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    val selectedInvoiceIds = remember { mutableStateListOf<Long>() }

    var invoiceToPay by remember { mutableStateOf<InvoiceDto?>(null) }
    var showReceiptDialog by remember { mutableStateOf(false) }
    var showTvanErrorDialog by remember { mutableStateOf<String?>(null) }
    var showPublishSuccessDialog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(roadCode) {
        viewModel.loadCustomers(roadCode)
    }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            appStateHolder.customerList = (uiState as UiState.Success<List<Customer>>).data
        }
    }

    LaunchedEffect(tvanActionState) {
        when (val state = tvanActionState) {
            is TvanActionState.PublishSuccess -> {
                showPublishSuccessDialog = "Đã phát hành thành công ${state.count} hóa đơn sang TVAN!"
                selectedInvoiceIds.clear()
                viewModel.resetTvanActionState()
            }
            is TvanActionState.PaySuccess -> {
                invoiceToPay = null
                viewModel.resetTvanActionState()
            }
            is TvanActionState.ReceiptLoaded -> {
                showReceiptDialog = true
            }
            is TvanActionState.Error -> {
                showTvanErrorDialog = state.message
                viewModel.resetTvanActionState()
            }
            else -> {}
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
            /* ── Tabs Row ── */
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                val tabTitles = listOf("Ghi chỉ số", "Chưa phát hành", "Nợ/Thu tiền", "Đã thanh toán")
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = {
                            searchQuery = ""
                            viewModel.setActiveTab(index)
                        },
                        text = {
                            Text(
                                title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

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
                    placeholder = { Text("Tìm theo tên, mã KH...") },
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

            /* ── Tab Contents ── */
            Box(modifier = Modifier.weight(1f)) {
                when (activeTab) {
                    0 -> {
                        CustomerTabContent(
                            state = uiState,
                            searchQuery = searchQuery,
                            isRecorded = { code -> viewModel.isRecorded(code) },
                            onCustomerSelected = { customer ->
                                appStateHolder.selectedCustomer = customer
                                onCustomerSelected(customer)
                            },
                            onRefresh = { viewModel.refresh() }
                        )
                    }
                    1 -> {
                        ToPublishTabContent(
                            state = toPublishState,
                            searchQuery = searchQuery,
                            selectedInvoiceIds = selectedInvoiceIds,
                            isPublishing = tvanActionState is TvanActionState.Loading,
                            onPublishClick = { ids -> viewModel.publishSelectedTvan(ids) },
                            onRefresh = { viewModel.refresh() }
                        )
                    }
                    2 -> {
                        DebtTabContent(
                            state = debtState,
                            searchQuery = searchQuery,
                            onPayClick = { inv -> invoiceToPay = inv },
                            onRefresh = { viewModel.refresh() }
                        )
                    }
                    3 -> {
                        PaidTabContent(
                            state = paidState,
                            searchQuery = searchQuery,
                            onReceiptClick = { inv -> viewModel.loadReceipt(inv.id) },
                            onRefresh = { viewModel.refresh() }
                        )
                    }
                }
            }
        }

        /* ── Dialog Giấy Báo Tiền Nước / Thu Tiền ── */
        if (invoiceToPay != null) {
            val isLoading = tvanActionState is TvanActionState.Loading
            InvoicePaperDialog(
                invoice = invoiceToPay!!,
                onDismiss = { if (!isLoading) invoiceToPay = null },
                onPayCash = { viewModel.payCashForInvoice(invoiceToPay!!.id) },
                onPrint = { /* Dummy */ },
                isLoading = isLoading
            )
        }

        /* ── Dialog Biên Nhận ── */
        if (showReceiptDialog && tvanActionState is TvanActionState.ReceiptLoaded) {
            ReceiptDialog(
                receipt = (tvanActionState as TvanActionState.ReceiptLoaded).receipt,
                onDismiss = {
                    showReceiptDialog = false
                    viewModel.resetTvanActionState()
                },
                onPrint = { /* Dummy */ }
            )
        }

        /* ── Dialog Lỗi TVAN ── */
        if (showTvanErrorDialog != null) {
            TvanErrorDialog(
                message = showTvanErrorDialog!!,
                onDismiss = { showTvanErrorDialog = null }
            )
        }

        /* ── Dialog Thành Công Phát Hành TVAN ── */
        if (showPublishSuccessDialog != null) {
            PublishSuccessDialog(
                message = showPublishSuccessDialog!!,
                onDismiss = { showPublishSuccessDialog = null }
            )
        }
    }
}
