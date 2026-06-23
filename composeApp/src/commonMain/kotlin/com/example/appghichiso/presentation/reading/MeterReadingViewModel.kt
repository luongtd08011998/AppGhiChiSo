package com.example.appghichiso.presentation.reading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appghichiso.data.api.dto.InvoiceDto
import com.example.appghichiso.data.api.dto.ReceiptDto
import com.example.appghichiso.data.local.CredentialsStorage
import com.example.appghichiso.domain.repository.MeterReadingRepository
import com.example.appghichiso.domain.repository.SmsRepository
import com.example.appghichiso.domain.usecase.GetInvoiceStatusUseCase
import com.example.appghichiso.domain.usecase.GetPaidListUseCase
import com.example.appghichiso.domain.usecase.GetReceiptUseCase
import com.example.appghichiso.domain.usecase.GetToPublishListUseCase
import com.example.appghichiso.domain.usecase.PayCashUseCase
import com.example.appghichiso.domain.usecase.PublishTvanUseCase
import com.example.appghichiso.domain.usecase.SubmitMeterReadingUseCase
import com.example.appghichiso.util.currentMonth
import com.example.appghichiso.util.currentYear
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SubmitState {
    data object Idle : SubmitState
    data object Loading : SubmitState
    data object Success : SubmitState
    data class Error(val message: String) : SubmitState
}

data class ConsumptionPoint(
    val yearMonth: String,
    val label: String,
    val consumption: Int
)

sealed interface HistoryState {
    data object Idle : HistoryState
    data object Loading : HistoryState
    data class Success(val points: List<ConsumptionPoint>) : HistoryState
    data class Error(val message: String) : HistoryState
}

sealed interface SmsUpdateState {
    data object Idle : SmsUpdateState
    data object Loading : SmsUpdateState
    data class Success(val message: String) : SmsUpdateState
    data class Error(val message: String) : SmsUpdateState
}

sealed interface TvanActionState {
    data object Idle : TvanActionState
    data object Loading : TvanActionState
    data class PublishSuccess(val count: String, val message: String) : TvanActionState
    data class PaySuccess(val receiptId: String, val message: String) : TvanActionState
    data class ReceiptLoaded(val receipt: ReceiptDto) : TvanActionState
    data class Error(val message: String) : TvanActionState
    /** Timeout khi phát hành hàng loạt — server có thể đã xử lý xong nhưng response về trễ */
    data object PublishTimeout : TvanActionState
}

class MeterReadingViewModel(
    private val submitMeterReadingUseCase: SubmitMeterReadingUseCase,
    private val meterReadingRepository: MeterReadingRepository,
    private val smsRepository: SmsRepository,
    private val getInvoiceStatusUseCase: GetInvoiceStatusUseCase,
    private val getToPublishListUseCase: GetToPublishListUseCase,
    private val getPaidListUseCase: GetPaidListUseCase,
    private val publishTvanUseCase: PublishTvanUseCase,
    private val payCashUseCase: PayCashUseCase,
    private val getReceiptUseCase: GetReceiptUseCase,
    private val credentialsStorage: CredentialsStorage
) : ViewModel() {

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    private val _previousMonthConsumption = MutableStateFlow<Int?>(null)
    val previousMonthConsumption: StateFlow<Int?> = _previousMonthConsumption.asStateFlow()

    private val _historyState = MutableStateFlow<HistoryState>(HistoryState.Idle)
    val historyState: StateFlow<HistoryState> = _historyState.asStateFlow()

    private val _smsNumber = MutableStateFlow<String?>(null)
    val smsNumber: StateFlow<String?> = _smsNumber.asStateFlow()

    private val _smsUpdateState = MutableStateFlow<SmsUpdateState>(SmsUpdateState.Idle)
    val smsUpdateState: StateFlow<SmsUpdateState> = _smsUpdateState.asStateFlow()

    private val _phoneNumber = MutableStateFlow<String?>(null)
    val phoneNumber: StateFlow<String?> = _phoneNumber.asStateFlow()

    private val _phoneUpdateState = MutableStateFlow<SmsUpdateState>(SmsUpdateState.Idle)
    val phoneUpdateState: StateFlow<SmsUpdateState> = _phoneUpdateState.asStateFlow()

    // --- TVAN States ---
    private val _tvanActionState = MutableStateFlow<TvanActionState>(TvanActionState.Idle)
    val tvanActionState: StateFlow<TvanActionState> = _tvanActionState.asStateFlow()

    private val _currentInvoice = MutableStateFlow<InvoiceDto?>(null)
    val currentInvoice: StateFlow<InvoiceDto?> = _currentInvoice.asStateFlow()

    private val _isTvanCreated = MutableStateFlow(false)
    val isTvanCreated: StateFlow<Boolean> = _isTvanCreated.asStateFlow()

    private val _tvanDebugLog = MutableStateFlow<String>("")
    val tvanDebugLog: StateFlow<String> = _tvanDebugLog.asStateFlow()

    // --- TVAN Paid States (biên nhận khách hàng đã thanh toán) ---
    private val _isTvanPaid = MutableStateFlow(false)
    val isTvanPaid: StateFlow<Boolean> = _isTvanPaid.asStateFlow()

    private val _tvanPaidInvoiceId = MutableStateFlow<Long?>(null)
    val tvanPaidInvoiceId: StateFlow<Long?> = _tvanPaidInvoiceId.asStateFlow()

    // --- TVAN Paid Online (thanh toán online qua paid_list) ---
    private val _isPaidOnline = MutableStateFlow(false)
    val isPaidOnline: StateFlow<Boolean> = _isPaidOnline.asStateFlow()

    private val _paidOnlineInvoice = MutableStateFlow<InvoiceDto?>(null)
    val paidOnlineInvoice: StateFlow<InvoiceDto?> = _paidOnlineInvoice.asStateFlow()

    // Lưu lại customerCode và ym hiện tại để dùng cho cache lookup
    private var loadedCustomerCode: String = ""
    private var loadedYm: String = ""

    private fun addTvanLog(msg: String) {
        println("TVAN Debug: $msg")
        _tvanDebugLog.value += "$msg\n"
    }

    fun loadCustomerData(customerCode: String, year: Int, month: Int, previousIndex: Int, currentIndex: Int, roadCode: String = "", preloadedInvoiceId: Long? = null) {
        _previousMonthConsumption.value = null
        _historyState.value = HistoryState.Loading
        _smsNumber.value = null
        _smsUpdateState.value = SmsUpdateState.Idle
        _phoneNumber.value = null
        _phoneUpdateState.value = SmsUpdateState.Idle
        _tvanActionState.value = TvanActionState.Idle
        _currentInvoice.value = null
        _isTvanCreated.value = false
        _isTvanPaid.value = false
        _tvanPaidInvoiceId.value = null
        _isPaidOnline.value = false
        _paidOnlineInvoice.value = null

        _tvanDebugLog.value = ""
        loadedCustomerCode = customerCode
        loadedYm = "$year${month.toString().padStart(2, '0')}"

        val prevYear = if (month == 1) year - 1 else year
        val prevMonth = if (month == 1) 12 else month - 1
        val prevYearMonth = "$prevYear${prevMonth.toString().padStart(2, '0')}"

        val (fromYear, fromMonth) = shiftMonth(year, month, -5)
        val fromYearMonth = "$fromYear${fromMonth.toString().padStart(2, '0')}"
        val toYearMonth = "$year${month.toString().padStart(2, '0')}"

        val ym = "$year${month.toString().padStart(2, '0')}"

        viewModelScope.launch {
            launch {
                val historyResult = if (customerCode.isNotBlank()) {
                    meterReadingRepository.getConsumptionHistoryFast(customerCode, fromYearMonth, toYearMonth)
                } else {
                    meterReadingRepository.getConsumptionHistory(customerCode, fromYearMonth, toYearMonth, previousIndex)
                }
                historyResult
                    .fold(
                        onSuccess = { pairs ->
                            val points = pairs.map { (ym, c) ->
                                ConsumptionPoint(
                                    yearMonth = ym,
                                    label = "${ym.substring(4, 6)}/${ym.substring(2, 4)}",
                                    consumption = c
                                )
                            }
                            _historyState.value = HistoryState.Success(points)
                            val prevConsumption = pairs.firstOrNull { it.first == prevYearMonth }?.second
                            _previousMonthConsumption.value = prevConsumption ?: -1
                        },
                        onFailure = {
                            _historyState.value = HistoryState.Error(it.message ?: "Lỗi tải lịch sử")
                            _previousMonthConsumption.value = -1
                        }
                    )
            }

            if (customerCode.isBlank()) {
                launch {
                    meterReadingRepository.getPreviousMonthConsumption(customerCode, prevYearMonth, previousIndex)
                        .fold(
                            onSuccess = { _previousMonthConsumption.value = it ?: -1 },
                            onFailure = { _previousMonthConsumption.value = -1 }
                        )
                }
            }

            launch {
                smsRepository.getSms(customerCode)
                    .fold(
                        onSuccess = { _smsNumber.value = it },
                        onFailure = { _smsNumber.value = null }
                    )
            }


            if (roadCode.isNotBlank()) {
                launch {
                    val isRecorded = currentIndex > 0
                    if (!isRecorded) {
                        addTvanLog("Khách hàng chưa nhập chỉ số -> Bỏ qua kiểm tra TVAN và thanh toán")
                        if (preloadedInvoiceId != null) {
                            val invoiceDto = InvoiceDto(
                                id = preloadedInvoiceId,
                                custCode = customerCode,
                                yearMonth = ym,
                                totalAmount = 0.0,
                                oldIndex = 0,
                                newIndex = 0
                            )
                            _currentInvoice.value = invoiceDto
                            addTvanLog("Set currentInvoice.id = $preloadedInvoiceId (Chưa ghi)")
                        } else {
                            _currentInvoice.value = null
                        }
                        _isTvanCreated.value = false
                        _isTvanPaid.value = false
                        _tvanPaidInvoiceId.value = null
                        _isPaidOnline.value = false
                        _paidOnlineInvoice.value = null
                    } else if (preloadedInvoiceId != null) {
                        // ✅ Fast path: invoice ID đã có sẵn từ get-invoices-by-road
                        addTvanLog("Invoice ID đã có sẵn từ get-invoices-by-road: $preloadedInvoiceId")
                        val invoiceDto = InvoiceDto(
                            id = preloadedInvoiceId,
                            custCode = customerCode,
                            yearMonth = ym,
                            totalAmount = 0.0,
                            oldIndex = 0,
                            newIndex = 0
                        )
                        _currentInvoice.value = invoiceDto
                        _isTvanCreated.value = false
                        addTvanLog("Set currentInvoice.id = $preloadedInvoiceId")

                        // Kiểm tra KH đã thanh toán online chưa
                        val paidResult = getPaidListUseCase(ym, roadCode, customerCode)
                        val paidInvoice = paidResult.getOrNull()?.find { it.custCode == customerCode }
                        if (paidInvoice != null) {
                            _isPaidOnline.value = true
                            _paidOnlineInvoice.value = paidInvoice
                            _isTvanPaid.value = true
                            _tvanPaidInvoiceId.value = paidInvoice.id
                            addTvanLog("paid_list: invoice id=${paidInvoice.id} → isPaidOnline = true")
                        } else {
                            // Kiểm tra local cache
                            val cachedId = credentialsStorage.getInvoiceId(customerCode, ym)
                            if (cachedId != null) {
                                _isTvanPaid.value = true
                                _tvanPaidInvoiceId.value = cachedId
                                addTvanLog("Cached invoiceId = $cachedId → isTvanPaid = true")
                            } else {
                                addTvanLog("Chưa thanh toán")
                            }
                        }
                    } else {
                        // Fallback: không có invoice ID → dùng flow cũ (getInvoiceStatusUseCase)
                        val systemYm = "${currentYear()}${currentMonth().toString().padStart(2, '0')}"
                        addTvanLog("Không có preloadedInvoiceId → gọi getInvoiceStatusUseCase")

                        val ymResult = getInvoiceStatusUseCase(ym, roadCode, customerCode)
                        val systemYmResult = if (systemYm != ym) {
                            getInvoiceStatusUseCase(systemYm, roadCode, customerCode)
                        } else {
                            null
                        }

                        if (ymResult.isSuccess) {
                            val (toPublish, debtsYm) = ymResult.getOrThrow()
                            val debtsSys = if (systemYmResult != null && systemYmResult.isSuccess) {
                                systemYmResult.getOrThrow().second
                            } else {
                                emptyList()
                            }

                            val allDebts = debtsSys + debtsYm

                            addTvanLog("toPublish size = ${toPublish.size}, debts size = ${allDebts.size}")
                            val inDebt = allDebts.find { it.custCode == customerCode }
                            if (inDebt != null) {
                                _currentInvoice.value = inDebt
                                _isTvanCreated.value = true
                                addTvanLog("isTvanCreated = true (debt)")
                            } else {
                                val inToPublish = toPublish.find { it.custCode == customerCode }
                                if (inToPublish != null) {
                                    _currentInvoice.value = inToPublish
                                    _isTvanCreated.value = false
                                    addTvanLog("isTvanCreated = false (toPublish)")
                                } else {
                                    addTvanLog("Không có invoice → kiểm tra paid_list + cache")
                                    val paidResult = getPaidListUseCase(ym, roadCode, customerCode)
                                    val paidInvoice = paidResult.getOrNull()?.find { it.custCode == customerCode }
                                    if (paidInvoice != null) {
                                        _isPaidOnline.value = true
                                        _paidOnlineInvoice.value = paidInvoice
                                        _isTvanPaid.value = true
                                        _tvanPaidInvoiceId.value = paidInvoice.id
                                        addTvanLog("paid_list: id=${paidInvoice.id} → isPaidOnline = true")
                                    } else {
                                        val cachedId = credentialsStorage.getInvoiceId(customerCode, ym)
                                        if (cachedId != null) {
                                            _isTvanPaid.value = true
                                            _tvanPaidInvoiceId.value = cachedId
                                            addTvanLog("Cached invoiceId = $cachedId → isTvanPaid = true")
                                        } else {
                                            addTvanLog("Không tìm thấy invoiceId")
                                        }
                                    }
                                }
                            }
                        } else {
                            val error = ymResult.exceptionOrNull()
                            addTvanLog("onFailure: ${error?.message}")
                        }
                    }
                }
            }
        }
    }

    fun submit(
        customerCode: String,
        contractCode: String,
        year: Int,
        month: Int,
        previousIndex: Int,
        newIndex: Int,
        invoiceId: Long,
        publishImmediate: Boolean = false
    ) {
        viewModelScope.launch {
            _submitState.value = SubmitState.Loading

            submitMeterReadingUseCase(
                customerCode = customerCode,
                invoiceId = invoiceId,
                newIndex = newIndex,
                previousIndex = previousIndex
            ).onSuccess {
                val activeInvoiceId = if (invoiceId > 0) invoiceId else _currentInvoice.value?.id ?: 0L
                // Sau khi lưu thành công, cập nhật invoice cho TVAN
                if (_currentInvoice.value == null) {
                    _currentInvoice.value = InvoiceDto(id = activeInvoiceId, custCode = customerCode,
                        yearMonth = "$year${month.toString().padStart(2, '0')}",
                        totalAmount = 0.0, oldIndex = 0, newIndex = 0)
                    _isTvanCreated.value = false
                }

                if (publishImmediate && activeInvoiceId > 0L) {
                    addTvanLog("Tự động phát hành TVAN cho hóa đơn ID: $activeInvoiceId")
                    publishTvanUseCase(listOf(activeInvoiceId)).fold(
                        onSuccess = { res ->
                            addTvanLog("Tự động phát hành TVAN thành công: ${res.retMsg}")
                            
                            val ym = "$year${month.toString().padStart(2, '0')}"
                            val statusResult = getInvoiceStatusUseCase(ym, "", customerCode)
                            if (statusResult.isSuccess) {
                                val (toPublish, debts) = statusResult.getOrThrow()
                                val updatedInvoice = debts.find { it.id == activeInvoiceId } ?: toPublish.find { it.id == activeInvoiceId }
                                if (updatedInvoice != null) {
                                    _currentInvoice.value = updatedInvoice
                                }
                            }
                            
                            _tvanActionState.value = TvanActionState.PublishSuccess(res.result, res.retMsg)
                            _isTvanCreated.value = true
                            _submitState.value = SubmitState.Success
                        },
                        onFailure = { err ->
                            addTvanLog("Tự động phát hành TVAN thất bại: ${err.message}")
                            _tvanActionState.value = TvanActionState.Error(err.message ?: "Lỗi tự động tạo hóa đơn TVAN")
                            _submitState.value = SubmitState.Success // Vẫn giữ Success cho ghi chỉ số
                        }
                    )
                } else {
                    _submitState.value = SubmitState.Success
                }
            }.onFailure {
                val msg = it.message ?: ""
                val friendly = if (msg.contains("invali", ignoreCase = true))
                    "Chỉ số đã được chốt, không thể cập nhật lại"
                else msg.ifBlank { "Ghi chỉ số thất bại" }
                _submitState.value = SubmitState.Error(friendly)
            }
        }
    }
    
    fun publishTvan() {
        val invoiceId = _currentInvoice.value?.id ?: return
        val customerCode = _currentInvoice.value?.custCode ?: return
        val ym = _currentInvoice.value?.yearMonth ?: return
        viewModelScope.launch {
            _tvanActionState.value = TvanActionState.Loading
            publishTvanUseCase(listOf(invoiceId)).fold(
                onSuccess = { res ->
                    val statusResult = getInvoiceStatusUseCase(ym, "", customerCode)
                    if (statusResult.isSuccess) {
                        val (toPublish, debts) = statusResult.getOrThrow()
                        val updatedInvoice = debts.find { it.id == invoiceId } ?: toPublish.find { it.id == invoiceId }
                        if (updatedInvoice != null) {
                            _currentInvoice.value = updatedInvoice
                        }
                    }

                    _tvanActionState.value = TvanActionState.PublishSuccess(res.result, res.retMsg)
                    _isTvanCreated.value = true
                },
                onFailure = { err ->
                    _tvanActionState.value = TvanActionState.Error(err.message ?: "Lỗi tạo hóa đơn TVAN")
                }
            )
        }
    }

    fun payCash() {
        val invoiceId = _currentInvoice.value?.id ?: return
        viewModelScope.launch {
            _tvanActionState.value = TvanActionState.Loading
            payCashUseCase(invoiceId).fold(
                onSuccess = { res ->
                    // Lưu invoiceId vào local cache sau khi thanh toán thành công
                    if (loadedCustomerCode.isNotBlank() && loadedYm.isNotBlank()) {
                        credentialsStorage.saveInvoiceId(loadedCustomerCode, loadedYm, invoiceId)
                    }
                    _isTvanPaid.value = true
                    _tvanPaidInvoiceId.value = invoiceId
                    _tvanActionState.value = TvanActionState.PaySuccess(res.result, res.retMsg)
                },
                onFailure = { err ->
                    _tvanActionState.value = TvanActionState.Error(err.message ?: "Lỗi thu tiền")
                }
            )
        }
    }

    fun loadReceipt(id: Long? = null) {
        val invoiceId = id ?: _tvanPaidInvoiceId.value ?: _currentInvoice.value?.id ?: return
        viewModelScope.launch {
            _tvanActionState.value = TvanActionState.Loading
            getReceiptUseCase(invoiceId).fold(
                onSuccess = { receipt ->
                    _tvanActionState.value = TvanActionState.ReceiptLoaded(receipt)
                },
                onFailure = { err ->
                    _tvanActionState.value = TvanActionState.Error(err.message ?: "Lỗi lấy biên nhận")
                }
            )
        }
    }
    
    fun resetTvanActionState() {
        _tvanActionState.value = TvanActionState.Idle
    }

    fun resetState() {
        _submitState.value = SubmitState.Idle
    }

    fun updateSms(customerCode: String, newSms: String) {
        viewModelScope.launch {
            _smsUpdateState.value = SmsUpdateState.Loading
            smsRepository.updateSms(customerCode, newSms)
                .onSuccess {
                    _smsNumber.value = newSms
                    _smsUpdateState.value = SmsUpdateState.Success(it)
                }
                .onFailure {
                    _smsUpdateState.value = SmsUpdateState.Error(it.message ?: "Cập nhật SMS thất bại")
                }
        }
    }

    fun resetSmsUpdateState() {
        _smsUpdateState.value = SmsUpdateState.Idle
    }

    fun updatePhone(customerCode: String, newPhone: String) {
        viewModelScope.launch {
            _phoneUpdateState.value = SmsUpdateState.Loading
            smsRepository.updatePhone(customerCode, newPhone)
                .onSuccess {
                    _phoneNumber.value = newPhone
                    _phoneUpdateState.value = SmsUpdateState.Success(it)
                }
                .onFailure {
                    _phoneUpdateState.value = SmsUpdateState.Error(it.message ?: "Cập nhật số điện thoại thất bại")
                }
        }
    }

    fun resetPhoneUpdateState() {
        _phoneUpdateState.value = SmsUpdateState.Idle
    }

    private fun shiftMonth(year: Int, month: Int, delta: Int): Pair<Int, Int> {
        var m = month + delta
        var y = year
        while (m <= 0) { m += 12; y-- }
        while (m > 12) { m -= 12; y++ }
        return Pair(y, m)
    }
}
