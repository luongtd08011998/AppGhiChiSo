package com.example.appghichiso.presentation.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appghichiso.di.AppStateHolder
import com.example.appghichiso.domain.model.Customer
import com.example.appghichiso.domain.model.CustomerByRoad
import com.example.appghichiso.domain.repository.SmsRepository
import com.example.appghichiso.domain.usecase.GetCustomersUseCase
import com.example.appghichiso.domain.usecase.GetToPublishListUseCase
import com.example.appghichiso.domain.usecase.GetDebtListUseCase
import com.example.appghichiso.domain.usecase.GetPaidListUseCase
import com.example.appghichiso.domain.usecase.PublishTvanUseCase
import com.example.appghichiso.domain.usecase.PayCashUseCase
import com.example.appghichiso.domain.usecase.GetReceiptUseCase
import com.example.appghichiso.domain.usecase.GetCustomersByRoadUseCase
import com.example.appghichiso.data.api.dto.InvoiceDto
import com.example.appghichiso.presentation.common.UiState
import com.example.appghichiso.presentation.reading.SmsUpdateState
import com.example.appghichiso.presentation.reading.TvanActionState
import com.example.appghichiso.util.currentMonth
import com.example.appghichiso.util.currentYear
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CustomerViewModel(
    private val getCustomersUseCase: GetCustomersUseCase,
    private val getToPublishListUseCase: GetToPublishListUseCase,
    private val getDebtListUseCase: GetDebtListUseCase,
    private val getPaidListUseCase: GetPaidListUseCase,
    private val publishTvanUseCase: PublishTvanUseCase,
    private val payCashUseCase: PayCashUseCase,
    private val getReceiptUseCase: GetReceiptUseCase,
    private val appStateHolder: AppStateHolder,
    private val getCustomersByRoadUseCase: GetCustomersByRoadUseCase,
    private val smsRepository: SmsRepository
) : ViewModel() {

    // ── Meter reading tab (Hóa Đơn > Ghi Chỉ Số) ──
    private val _uiState = MutableStateFlow<UiState<List<Customer>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Customer>>> = _uiState.asStateFlow()

    // ── Invoice sub-tabs ──
    private val _toPublishState = MutableStateFlow<UiState<List<InvoiceDto>>>(UiState.Loading)
    val toPublishState: StateFlow<UiState<List<InvoiceDto>>> = _toPublishState.asStateFlow()

    private val _debtState = MutableStateFlow<UiState<List<InvoiceDto>>>(UiState.Loading)
    val debtState: StateFlow<UiState<List<InvoiceDto>>> = _debtState.asStateFlow()

    private val _paidState = MutableStateFlow<UiState<List<InvoiceDto>>>(UiState.Loading)
    val paidState: StateFlow<UiState<List<InvoiceDto>>> = _paidState.asStateFlow()

    // ── Khách Hàng tab ──
    private val _customersByRoadState = MutableStateFlow<UiState<List<CustomerByRoad>>>(UiState.Loading)
    val customersByRoadState: StateFlow<UiState<List<CustomerByRoad>>> = _customersByRoadState.asStateFlow()

    // ── SMS / Phone update ──
    private val _smsUpdateState = MutableStateFlow<SmsUpdateState>(SmsUpdateState.Idle)
    val smsUpdateState: StateFlow<SmsUpdateState> = _smsUpdateState.asStateFlow()

    private val _phoneUpdateState = MutableStateFlow<SmsUpdateState>(SmsUpdateState.Idle)
    val phoneUpdateState: StateFlow<SmsUpdateState> = _phoneUpdateState.asStateFlow()

    // ── Tab navigation ──
    // activeTab: 0 = Khách Hàng, 1 = Hóa Đơn (default = 1)
    private val _activeTab = MutableStateFlow(1)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    // activeInvoiceSubTab: 0 = Ghi Chỉ Số, 1 = Chưa PH, 2 = Nợ/Thu, 3 = Đã TT
    private val _activeInvoiceSubTab = MutableStateFlow(0)
    val activeInvoiceSubTab: StateFlow<Int> = _activeInvoiceSubTab.asStateFlow()

    private val _tvanActionState = MutableStateFlow<TvanActionState>(TvanActionState.Idle)
    val tvanActionState: StateFlow<TvanActionState> = _tvanActionState.asStateFlow()

    private var currentRoadCode: String = ""

    // ── Pagination States ──
    private var toPublishPage = 0
    private var maxToPublishPage = 1
    private val _isToPublishLoadingMore = MutableStateFlow(false)
    val isToPublishLoadingMore: StateFlow<Boolean> = _isToPublishLoadingMore.asStateFlow()
    private val currentToPublishList = mutableListOf<InvoiceDto>()

    private var customerPage = 0
    private var maxCustomerPage = 1
    private val _isCustomerLoadingMore = MutableStateFlow(false)
    val isCustomerLoadingMore: StateFlow<Boolean> = _isCustomerLoadingMore.asStateFlow()
    private val currentCustomerList = mutableListOf<Customer>()

    private var debtPageYm = 0
    private var maxDebtPageYm = 1
    private var debtPageSys = 0
    private var maxDebtPageSys = 1
    private val _isDebtLoadingMore = MutableStateFlow(false)
    val isDebtLoadingMore: StateFlow<Boolean> = _isDebtLoadingMore.asStateFlow()
    private val currentDebtList = mutableListOf<InvoiceDto>()

    private var paidPageYm = 0
    private var maxPaidPageYm = 1
    private var paidPageSys = 0
    private var maxPaidPageSys = 1
    private val _isPaidLoadingMore = MutableStateFlow(false)
    val isPaidLoadingMore: StateFlow<Boolean> = _isPaidLoadingMore.asStateFlow()
    private val currentPaidList = mutableListOf<InvoiceDto>()

    private var customersByRoadPage = 0
    private var maxCustomersByRoadPage = 1
    private val _isCustomersByRoadLoadingMore = MutableStateFlow(false)
    val isCustomersByRoadLoadingMore: StateFlow<Boolean> = _isCustomersByRoadLoadingMore.asStateFlow()
    private val currentCustomersByRoadList = mutableListOf<CustomerByRoad>()

    val currentYear: Int get() = appStateHolder.billingYear.takeIf { it > 0 } ?: currentYear()
    val currentMonth: Int get() = appStateHolder.billingMonth.takeIf { it > 0 } ?: currentMonth()

    fun loadCustomers(roadCode: String) {
        currentRoadCode = roadCode
        // Load default: Tab Hóa Đơn > Sub-tab Ghi Chỉ Số
        loadInvoiceSubTab(0)
    }

    fun setActiveTab(tab: Int) {
        _activeTab.value = tab
        if (currentRoadCode.isEmpty()) return
        when (tab) {
            0 -> loadCustomersByRoad()
            1 -> loadInvoiceSubTab(_activeInvoiceSubTab.value)
        }
    }

    fun setInvoiceSubTab(tab: Int) {
        _activeInvoiceSubTab.value = tab
        if (currentRoadCode.isNotEmpty()) {
            loadInvoiceSubTab(tab)
        }
    }

    private fun loadCustomersByRoad() {
        customersByRoadPage = 0
        currentCustomersByRoadList.clear()
        viewModelScope.launch {
            _customersByRoadState.value = UiState.Loading
            getCustomersByRoadUseCase(currentRoadCode, 0)
                .onSuccess {
                    maxCustomersByRoadPage = it.firstOrNull()?.numOfPages ?: 1
                    currentCustomersByRoadList.addAll(it)
                    _customersByRoadState.value = UiState.Success(currentCustomersByRoadList.toList())
                }
                .onFailure { _customersByRoadState.value = UiState.Error(it.message ?: "Lỗi tải danh sách khách hàng") }
        }
    }

    fun loadInvoiceSubTab(tab: Int) {
        val ym = "$currentYear${currentMonth.toString().padStart(2, '0')}"
        val systemYm = "${com.example.appghichiso.util.currentYear()}${com.example.appghichiso.util.currentMonth().toString().padStart(2, '0')}"
        viewModelScope.launch {
            when (tab) {
                0 -> {
                    // Ghi Chỉ Số
                    customerPage = 0
                    currentCustomerList.clear()
                    _uiState.value = UiState.Loading
                    getCustomersUseCase(currentRoadCode, currentYear, currentMonth, 0)
                        .onSuccess {
                            maxCustomerPage = it.firstOrNull()?.numOfPages ?: 1
                            currentCustomerList.addAll(it)
                            _uiState.value = UiState.Success(currentCustomerList.toList())
                        }
                        .onFailure { _uiState.value = UiState.Error(it.message ?: "Lỗi tải danh sách khách hàng") }
                }
                1 -> {
                    // Chưa Phát Hành
                    toPublishPage = 0
                    currentToPublishList.clear()
                    _toPublishState.value = UiState.Loading
                    getToPublishListUseCase(ym, currentRoadCode, "", 0)
                        .onSuccess {
                            maxToPublishPage = it.firstOrNull()?.numOfPages ?: 1
                            currentToPublishList.addAll(it)
                            _toPublishState.value = UiState.Success(currentToPublishList.toList())
                        }
                        .onFailure { _toPublishState.value = UiState.Error(it.message ?: "Lỗi tải danh sách chưa phát hành") }
                }
                2 -> {
                    // Nợ / Thu Tiền
                    debtPageYm = 0
                    debtPageSys = 0
                    currentDebtList.clear()
                    _debtState.value = UiState.Loading
                    val ymResult = getDebtListUseCase(ym, currentRoadCode, "", 0)
                    val systemYmResult = if (systemYm != ym) {
                        getDebtListUseCase(systemYm, currentRoadCode, "", 0)
                    } else null

                    if (ymResult.isSuccess) {
                        val debtsYm = ymResult.getOrNull() ?: emptyList()
                        maxDebtPageYm = debtsYm.firstOrNull()?.numOfPages ?: 1
                        val debtsSys = if (systemYmResult != null && systemYmResult.isSuccess) {
                            val sys = systemYmResult.getOrNull() ?: emptyList()
                            maxDebtPageSys = sys.firstOrNull()?.numOfPages ?: 1
                            sys
                        } else {
                            maxDebtPageSys = 1
                            emptyList()
                        }
                        currentDebtList.addAll((debtsYm + debtsSys).distinctBy { it.id })
                        _debtState.value = UiState.Success(currentDebtList.toList())
                    } else {
                        _debtState.value = UiState.Error(ymResult.exceptionOrNull()?.message ?: "Lỗi tải danh sách nợ")
                    }
                }
                3 -> {
                    // Đã Thanh Toán
                    paidPageYm = 0
                    paidPageSys = 0
                    currentPaidList.clear()
                    _paidState.value = UiState.Loading
                    val ymResult = getPaidListUseCase(ym, currentRoadCode, "", 0)
                    val systemYmResult = if (systemYm != ym) {
                        getPaidListUseCase(systemYm, currentRoadCode, "", 0)
                    } else null

                    if (ymResult.isSuccess) {
                        val paidYm = ymResult.getOrNull() ?: emptyList()
                        maxPaidPageYm = paidYm.firstOrNull()?.numOfPages ?: 1
                        val paidSys = if (systemYmResult != null && systemYmResult.isSuccess) {
                            val sys = systemYmResult.getOrNull() ?: emptyList()
                            maxPaidPageSys = sys.firstOrNull()?.numOfPages ?: 1
                            sys
                        } else {
                            maxPaidPageSys = 1
                            emptyList()
                        }
                        currentPaidList.addAll((paidYm + paidSys).distinctBy { it.id })
                        _paidState.value = UiState.Success(currentPaidList.toList())
                    } else {
                        _paidState.value = UiState.Error(ymResult.exceptionOrNull()?.message ?: "Lỗi tải danh sách đã thanh toán")
                    }
                }
            }
        }
    }

    // ── Pagination Logic ──
    fun loadMoreCustomers() {
        if (_isCustomerLoadingMore.value || customerPage >= maxCustomerPage - 1) return
        _isCustomerLoadingMore.value = true
        customerPage++
        viewModelScope.launch {
            getCustomersUseCase(currentRoadCode, currentYear, currentMonth, customerPage)
                .onSuccess {
                    currentCustomerList.addAll(it)
                    _uiState.value = UiState.Success(currentCustomerList.toList())
                }
                .onFailure {
                    customerPage--
                }
            _isCustomerLoadingMore.value = false
        }
    }

    fun loadMoreCustomersByRoad() {
        if (_isCustomersByRoadLoadingMore.value || customersByRoadPage >= maxCustomersByRoadPage - 1) return
        _isCustomersByRoadLoadingMore.value = true
        customersByRoadPage++
        viewModelScope.launch {
            getCustomersByRoadUseCase(currentRoadCode, customersByRoadPage)
                .onSuccess {
                    currentCustomersByRoadList.addAll(it)
                    _customersByRoadState.value = UiState.Success(currentCustomersByRoadList.toList())
                }
                .onFailure {
                    customersByRoadPage--
                }
            _isCustomersByRoadLoadingMore.value = false
        }
    }

    fun loadMoreToPublish() {
        if (_isToPublishLoadingMore.value || toPublishPage >= maxToPublishPage - 1) return
        _isToPublishLoadingMore.value = true
        toPublishPage++
        val ym = "$currentYear${currentMonth.toString().padStart(2, '0')}"
        viewModelScope.launch {
            getToPublishListUseCase(ym, currentRoadCode, "", toPublishPage)
                .onSuccess {
                    currentToPublishList.addAll(it)
                    _toPublishState.value = UiState.Success(currentToPublishList.toList())
                }
                .onFailure {
                    toPublishPage--
                }
            _isToPublishLoadingMore.value = false
        }
    }

    fun loadMoreDebt() {
        if (_isDebtLoadingMore.value) return
        val canLoadYm = debtPageYm < maxDebtPageYm - 1
        val canLoadSys = debtPageSys < maxDebtPageSys - 1
        if (!canLoadYm && !canLoadSys) return

        _isDebtLoadingMore.value = true
        val ym = "$currentYear${currentMonth.toString().padStart(2, '0')}"
        val systemYm = "${com.example.appghichiso.util.currentYear()}${com.example.appghichiso.util.currentMonth().toString().padStart(2, '0')}"

        viewModelScope.launch {
            val newItems = mutableListOf<InvoiceDto>()
            if (canLoadYm) {
                debtPageYm++
                getDebtListUseCase(ym, currentRoadCode, "", debtPageYm)
                    .onSuccess { newItems.addAll(it) }
                    .onFailure { debtPageYm-- }
            }
            if (canLoadSys && systemYm != ym) {
                debtPageSys++
                getDebtListUseCase(systemYm, currentRoadCode, "", debtPageSys)
                    .onSuccess { newItems.addAll(it) }
                    .onFailure { debtPageSys-- }
            }
            if (newItems.isNotEmpty()) {
                currentDebtList.addAll(newItems)
                _debtState.value = UiState.Success(currentDebtList.distinctBy { it.id })
            }
            _isDebtLoadingMore.value = false
        }
    }

    fun loadMorePaid() {
        if (_isPaidLoadingMore.value) return
        val canLoadYm = paidPageYm < maxPaidPageYm - 1
        val canLoadSys = paidPageSys < maxPaidPageSys - 1
        if (!canLoadYm && !canLoadSys) return

        _isPaidLoadingMore.value = true
        val ym = "$currentYear${currentMonth.toString().padStart(2, '0')}"
        val systemYm = "${com.example.appghichiso.util.currentYear()}${com.example.appghichiso.util.currentMonth().toString().padStart(2, '0')}"

        viewModelScope.launch {
            val newItems = mutableListOf<InvoiceDto>()
            if (canLoadYm) {
                paidPageYm++
                getPaidListUseCase(ym, currentRoadCode, "", paidPageYm)
                    .onSuccess { newItems.addAll(it) }
                    .onFailure { paidPageYm-- }
            }
            if (canLoadSys && systemYm != ym) {
                paidPageSys++
                getPaidListUseCase(systemYm, currentRoadCode, "", paidPageSys)
                    .onSuccess { newItems.addAll(it) }
                    .onFailure { paidPageSys-- }
            }
            if (newItems.isNotEmpty()) {
                currentPaidList.addAll(newItems)
                _paidState.value = UiState.Success(currentPaidList.distinctBy { it.id })
            }
            _isPaidLoadingMore.value = false
        }
    }

    fun refresh() {
        if (currentRoadCode.isEmpty()) return
        when (_activeTab.value) {
            0 -> loadCustomersByRoad()
            1 -> loadInvoiceSubTab(_activeInvoiceSubTab.value)
        }
    }

    // ── TVAN Actions ──
    fun publishSelectedTvan(ids: List<Long>) {
        viewModelScope.launch {
            _tvanActionState.value = TvanActionState.Loading
            publishTvanUseCase(ids).fold(
                onSuccess = { res ->
                    _tvanActionState.value = TvanActionState.PublishSuccess(res.result, res.retMsg)
                    loadInvoiceSubTab(1)
                },
                onFailure = { err ->
                    _tvanActionState.value = TvanActionState.Error(err.message ?: "Lỗi tạo hóa đơn TVAN")
                }
            )
        }
    }

    fun payCashForInvoice(id: Long) {
        viewModelScope.launch {
            _tvanActionState.value = TvanActionState.Loading
            payCashUseCase(id).fold(
                onSuccess = { res ->
                    _tvanActionState.value = TvanActionState.PaySuccess(res.result, res.retMsg)
                    loadInvoiceSubTab(2)
                },
                onFailure = { err ->
                    _tvanActionState.value = TvanActionState.Error(err.message ?: "Lỗi thu tiền")
                }
            )
        }
    }

    fun loadReceipt(id: Long) {
        viewModelScope.launch {
            _tvanActionState.value = TvanActionState.Loading
            getReceiptUseCase(id).fold(
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

    fun updateSms(customerCode: String, newSms: String) {
        viewModelScope.launch {
            _smsUpdateState.value = SmsUpdateState.Loading
            smsRepository.updateSms(customerCode, newSms)
                .onSuccess { _smsUpdateState.value = SmsUpdateState.Success(it) }
                .onFailure { _smsUpdateState.value = SmsUpdateState.Error(it.message ?: "Cập nhật SMS thất bại") }
        }
    }

    fun resetSmsUpdateState() { _smsUpdateState.value = SmsUpdateState.Idle }

    fun updatePhone(customerCode: String, newPhone: String) {
        viewModelScope.launch {
            _phoneUpdateState.value = SmsUpdateState.Loading
            smsRepository.updatePhone(customerCode, newPhone)
                .onSuccess { _phoneUpdateState.value = SmsUpdateState.Success(it) }
                .onFailure { _phoneUpdateState.value = SmsUpdateState.Error(it.message ?: "Cập nhật điện thoại thất bại") }
        }
    }

    fun resetPhoneUpdateState() { _phoneUpdateState.value = SmsUpdateState.Idle }

    fun isRecorded(customerCode: String): Boolean =
        appStateHolder.recordedCustomerCodes.contains(customerCode)
}
