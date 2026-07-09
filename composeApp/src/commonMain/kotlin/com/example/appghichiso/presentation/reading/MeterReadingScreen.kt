package com.example.appghichiso.presentation.reading

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appghichiso.di.AppStateHolder
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import com.example.appghichiso.printer.PrinterHub

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
    val smsNumber by viewModel.smsNumber.collectAsStateWithLifecycle()
    val smsUpdateState by viewModel.smsUpdateState.collectAsStateWithLifecycle()
    val phoneNumber by viewModel.phoneNumber.collectAsStateWithLifecycle()
    val phoneUpdateState by viewModel.phoneUpdateState.collectAsStateWithLifecycle()
    val tvanActionState by viewModel.tvanActionState.collectAsStateWithLifecycle()
    val currentInvoice by viewModel.currentInvoice.collectAsStateWithLifecycle()

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
    val currentPeriodHuman = remember(customer.year, customer.month) {
        "Tháng ${customer.month}/${customer.year}"
    }

    var newIndexText by rememberSaveable(currentIndex) {
        mutableStateOf(if (customer.currentIndex > 0) customer.currentIndex.toString() else "")
    }
    var showWarningDialog   by rememberSaveable(currentIndex) { mutableStateOf(false) }
    var showConfirmDialog   by rememberSaveable(currentIndex) { mutableStateOf(false) }
    var showSuccessDialog   by rememberSaveable(currentIndex) { mutableStateOf(false) }
    var showDetails         by rememberSaveable(currentIndex) { mutableStateOf(false) }
    var showSmsEditDialog   by rememberSaveable(currentIndex) { mutableStateOf(false) }
    var showPhoneEditDialog by rememberSaveable(currentIndex) { mutableStateOf(false) }
    var showInvoiceDialog   by rememberSaveable(currentIndex) { mutableStateOf(false) }
    var showReceiptDialog   by rememberSaveable(currentIndex) { mutableStateOf(false) }
    var isPublishImmediately by rememberSaveable(currentIndex) { mutableStateOf(false) }

    LaunchedEffect(currentIndex) {
        viewModel.resetState()
        appStateHolder.selectedCustomer = customer
        viewModel.loadCustomerData(
            customerCode = customer.customerCode,
            year = customer.year,
            month = customer.month,
            previousIndex = customer.previousIndex,
            currentIndex = customer.currentIndex,
            roadCode = appStateHolder.selectedRoad?.code?.takeIf { it.isNotBlank() } ?: customer.roadCode,
            preloadedInvoiceId = customer.invoiceId
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
        WarningDialog(
            customer = customer,
            newIndex = newIndex,
            consumption = consumption,
            prevConsumption = prevConsumption,
            isLowConsumption = isLowConsumption,
            prevPeriodHuman = prevPeriodHuman,
            currentPeriodHuman = currentPeriodHuman,
            onDismiss = { showWarningDialog = false },
            onConfirm = {
                showWarningDialog = false
                viewModel.submit(
                    customerCode  = customer.customerCode,
                    contractCode  = customer.contractCode,
                    year          = customer.year,
                    month         = customer.month,
                    previousIndex = customer.previousIndex,
                    newIndex      = newIndex,
                    invoiceId     = currentInvoice?.id ?: customer.invoiceId ?: 0L,
                    publishImmediate = isPublishImmediately
                )
            }
        )
    }

    /* ── Confirm dialog ── */
    if (showConfirmDialog && newIndex != null) {
        ConfirmDialog(
            customer = customer,
            newIndex = newIndex,
            onDismiss = { showConfirmDialog = false },
            onConfirm = {
                showConfirmDialog = false
                viewModel.submit(
                    customerCode  = customer.customerCode,
                    contractCode  = customer.contractCode,
                    year          = customer.year,
                    month         = customer.month,
                    previousIndex = customer.previousIndex,
                    newIndex      = newIndex,
                    invoiceId     = currentInvoice?.id ?: customer.invoiceId ?: 0L,
                    publishImmediate = isPublishImmediately
                )
            }
        )
    }

    /* ── TVAN Effect ── */
    LaunchedEffect(tvanActionState) {
        if (tvanActionState is TvanActionState.PublishSuccess) {
            showSuccessDialog = false
            showInvoiceDialog = true
        }
        if (tvanActionState is TvanActionState.ReceiptLoaded) {
            showInvoiceDialog = false
            showReceiptDialog = true
        }
        if (tvanActionState is TvanActionState.PaySuccess) {
            viewModel.loadReceipt(null)
        }
    }

    if (showInvoiceDialog && currentInvoice != null) {
        InvoicePaperDialog(
            invoice = currentInvoice!!,
            onDismiss = { 
                showInvoiceDialog = false 
                viewModel.resetTvanActionState()
                onSubmitSuccess()
            },
            onPayCash = { viewModel.payCash() },
            onPrint = { PrinterHub.instance.requestPrintInvoice(currentInvoice!!) },
            isLoading = tvanActionState is TvanActionState.Loading
        )
    }

    if (showReceiptDialog && tvanActionState is TvanActionState.ReceiptLoaded) {
        ReceiptDialog(
            receipt = (tvanActionState as TvanActionState.ReceiptLoaded).receipt,
            onDismiss = {
                showReceiptDialog = false
                viewModel.resetTvanActionState()
                onSubmitSuccess()
            },
            onPrint = {
                PrinterHub.instance.requestPrintReceipt((tvanActionState as TvanActionState.ReceiptLoaded).receipt)
            }
        )
    }

    if (tvanActionState is TvanActionState.Error) {
        TvanErrorDialog(
            message = (tvanActionState as TvanActionState.Error).message,
            onDismiss = { viewModel.resetTvanActionState() }
        )
    }

    /* ── Success dialog ── */
    if (showSuccessDialog) {
        SuccessDialog(
            customerName = customer.customerName,
            isLoading = tvanActionState is TvanActionState.Loading,
            onContinue = {
                showSuccessDialog = false
                viewModel.resetState()
                appStateHolder.recordedCustomerCodes.add(customer.customerCode)
                val updatedList = appStateHolder.customerList.toMutableList()
                updatedList[currentIndex] = customer.copy(currentIndex = newIndex!!)
                appStateHolder.customerList = updatedList
                if (currentIndex < customerList.size - 1) currentIndex++ else onSubmitSuccess()
            },
            onSkip = {
                showSuccessDialog = false
                viewModel.resetState()
                appStateHolder.recordedCustomerCodes.add(customer.customerCode)
                val updatedList = appStateHolder.customerList.toMutableList()
                updatedList[currentIndex] = customer.copy(currentIndex = newIndex!!)
                appStateHolder.customerList = updatedList
                if (currentIndex < customerList.size - 1) currentIndex++ else onSubmitSuccess()
            }
        )
    }

    /* ── SMS Edit dialog ── */
    if (showSmsEditDialog) {
        SmsEditDialog(
            customerName = customer.customerName,
            customerCode = customer.customerCode,
            initialSms = smsNumber,
            updateState = smsUpdateState,
            onConfirm = { editedSms ->
                viewModel.updateSms(customer.customerCode, editedSms)
            },
            onDismiss = {
                showSmsEditDialog = false
                viewModel.resetSmsUpdateState()
            }
        )
    }

    /* ── Phone Edit dialog ── */
    if (showPhoneEditDialog) {
        PhoneEditDialog(
            customerName = customer.customerName,
            customerCode = customer.customerCode,
            initialPhone = phoneNumber ?: customer.customerPhone,
            updateState = phoneUpdateState,
            onConfirm = { editedPhone ->
                viewModel.updatePhone(customer.customerCode, editedPhone)
            },
            onDismiss = {
                showPhoneEditDialog = false
                viewModel.resetPhoneUpdateState()
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
                actions = {
                    IconButton(onClick = { showDetails = true }) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Chi tiết khách hàng",
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
        /* ── Modal Bottom Sheet for Details ── */
        if (showDetails) {
            CustomerDetailSheet(
                customer = customer,
                phoneNumber = phoneNumber,
                smsNumber = smsNumber,
                historyState = historyState,
                onDismiss = { showDetails = false }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MeterReadingContent(
                customer = customer,
                customerListSize = total,
                currentIndex = currentIndex,
                isRecorded = customer.isRecorded || appStateHolder.recordedCustomerCodes.contains(customer.customerCode),
                newIndexText = newIndexText,
                onNewIndexChange = { newIndexText = it },
                submitState = submitState,
                prevConsumption = prevConsumption,
                prevPeriodHuman = prevPeriodHuman,
                currentPeriodHuman = currentPeriodHuman,
                isAbnormalConsumption = isAbnormalConsumption,
                isLowConsumption = isLowConsumption,
                consumption = consumption,
                currentInvoice = currentInvoice,
                invoiceIdFromCustomer = customer.invoiceId,
                phoneNumber = phoneNumber,
                smsNumber = smsNumber,
                isPublishImmediately = isPublishImmediately,
                onPrev = { if (currentIndex > 0) currentIndex-- },
                onNext = { if (currentIndex < customerList.size - 1) currentIndex++ },
                onSubmit = { publishImmediate ->
                    isPublishImmediately = publishImmediate
                    if (isAbnormalConsumption) showWarningDialog = true
                    else showConfirmDialog = true
                }
            )
        }
    }
}
