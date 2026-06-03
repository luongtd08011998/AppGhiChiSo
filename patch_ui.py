import re

file_path = "composeApp/src/commonMain/kotlin/com/example/appghichiso/presentation/reading/MeterReadingScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# 1. Add states
content = content.replace(
    "val smsUpdateState by viewModel.smsUpdateState.collectAsStateWithLifecycle()",
    "val smsUpdateState by viewModel.smsUpdateState.collectAsStateWithLifecycle()\n    val tvanActionState by viewModel.tvanActionState.collectAsStateWithLifecycle()\n    val currentInvoice by viewModel.currentInvoice.collectAsStateWithLifecycle()\n    val isTvanCreated by viewModel.isTvanCreated.collectAsStateWithLifecycle()"
)

# 2. Add vars
content = content.replace(
    "var showSmsEditDialog  by rememberSaveable(currentIndex) { mutableStateOf(false) }",
    "var showSmsEditDialog  by rememberSaveable(currentIndex) { mutableStateOf(false) }\n    var showInvoiceDialog by rememberSaveable(currentIndex) { mutableStateOf(false) }\n    var showReceiptDialog by rememberSaveable(currentIndex) { mutableStateOf(false) }"
)

# 3. Add roadCode to loadCustomerData
content = content.replace(
    "previousIndex = customer.previousIndex\n        )",
    "previousIndex = customer.previousIndex,\n            roadCode = customer.roadCode\n        )"
)

# 4. Add roadCode to submit 1
content = content.replace(
    "previousIndex = customer.previousIndex,\n                            newIndex      = newIndex\n                        )",
    "previousIndex = customer.previousIndex,\n                            newIndex      = newIndex,\n                            roadCode      = customer.roadCode\n                        )"
)

# 5. Success Dialog replacement
old_success = """    /* ── Success dialog ── */
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
                        
                        // Cập nhật chỉ số mới vào danh sách gốc để nhớ khi quay lại
                        val updatedList = appStateHolder.customerList.toMutableList()
                        updatedList[currentIndex] = customer.copy(currentIndex = newIndex!!)
                        appStateHolder.customerList = updatedList

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
                    
                    // Cập nhật chỉ số mới vào danh sách gốc
                    val updatedList = appStateHolder.customerList.toMutableList()
                    updatedList[currentIndex] = customer.copy(currentIndex = newIndex!!)
                    appStateHolder.customerList = updatedList

                    onSubmitSuccess()
                }) { Text("Về danh sách") }
            }
        )
    }"""

new_success = """    /* ── TVAN Effect ── */
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
            },
            onPayCash = { viewModel.payCash() },
            onPrint = { /* TODO */ },
            isLoading = tvanActionState is TvanActionState.Loading
        )
    }

    if (showReceiptDialog && tvanActionState is TvanActionState.ReceiptLoaded) {
        ReceiptDialog(
            receipt = (tvanActionState as TvanActionState.ReceiptLoaded).receipt,
            onDismiss = { 
                showReceiptDialog = false 
                viewModel.resetTvanActionState()
            },
            onPrint = { /* TODO */ }
        )
    }

    if (tvanActionState is TvanActionState.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.resetTvanActionState() },
            title = { Text("Lỗi") },
            text = { Text((tvanActionState as TvanActionState.Error).message) },
            confirmButton = { TextButton(onClick = { viewModel.resetTvanActionState() }) { Text("Đóng") } }
        )
    }

    /* ── Success dialog ── */
    if (showSuccessDialog) {
        val isLoading = tvanActionState is TvanActionState.Loading
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
            text = { 
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Chỉ số ${customer.customerName} đã được cập nhật.")
                    if (currentInvoice != null) {
                        Text(
                            "Hóa đơn đã sẵn sàng, bạn có muốn tạo hóa đơn TVAN ngay bây giờ?",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                if (currentInvoice != null) {
                    Button(
                        onClick = { viewModel.publishTvan() },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp)
                    ) { 
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        else Text("Tạo hóa đơn TVAN") 
                    }
                } else {
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            viewModel.resetState()
                            appStateHolder.recordedCustomerCodes.add(customer.customerCode)
                            val updatedList = appStateHolder.customerList.toMutableList()
                            updatedList[currentIndex] = customer.copy(currentIndex = newIndex!!)
                            appStateHolder.customerList = updatedList
                            if (currentIndex < customerList.size - 1) currentIndex++ else onSubmitSuccess()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Tiếp tục") }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    viewModel.resetState()
                    appStateHolder.recordedCustomerCodes.add(customer.customerCode)
                    val updatedList = appStateHolder.customerList.toMutableList()
                    updatedList[currentIndex] = customer.copy(currentIndex = newIndex!!)
                    appStateHolder.customerList = updatedList
                    if (currentIndex < customerList.size - 1) currentIndex++ else onSubmitSuccess()
                }, enabled = !isLoading) { Text("Bỏ qua") }
            }
        )
    }"""
content = content.replace(old_success, new_success)

# 6. Replace Submit button
old_submit_btn = """                /* ── Submit button ── */
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
                }"""

new_submit_btn = """                /* ── Submit button ── */
                if (isTvanCreated) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { },
                            enabled = false,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(56.dp)
                        ) {
                            Text("Đã tạo Hóa Đơn", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        if (currentInvoice != null) {
                            Button(
                                onClick = { showInvoiceDialog = true },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                modifier = Modifier.weight(1f).height(56.dp)
                            ) {
                                Text("Thu Tiền", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
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
                }"""
content = content.replace(old_submit_btn, new_submit_btn)

with open(file_path, "w") as f:
    f.write(content)

