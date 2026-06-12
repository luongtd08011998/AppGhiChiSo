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
        viewModelScope.launch {
            _customersByRoadState.value = UiState.Loading
            getCustomersByRoadUseCase(currentRoadCode)
                .onSuccess { _customersByRoadState.value = UiState.Success(it) }
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
                    _uiState.value = UiState.Loading
                    getCustomersUseCase(currentRoadCode, currentYear, currentMonth)
                        .onSuccess { _uiState.value = UiState.Success(it) }
                        .onFailure { _uiState.value = UiState.Error(it.message ?: "Lỗi tải danh sách khách hàng") }
                }
                1 -> {
                    // Chưa Phát Hành
                    _toPublishState.value = UiState.Loading
                    getToPublishListUseCase(ym, currentRoadCode, "")
                        .onSuccess { _toPublishState.value = UiState.Success(it) }
                        .onFailure { _toPublishState.value = UiState.Error(it.message ?: "Lỗi tải danh sách chưa phát hành") }
                }
                2 -> {
                    // Nợ / Thu Tiền
                    _debtState.value = UiState.Loading
                    val ymResult = getDebtListUseCase(ym, currentRoadCode, "")
                    val systemYmResult = if (systemYm != ym) {
                        getDebtListUseCase(systemYm, currentRoadCode, "")
                    } else null

                    if (ymResult.isSuccess) {
                        val debtsYm = ymResult.getOrNull() ?: emptyList()
                        val debtsSys = if (systemYmResult != null && systemYmResult.isSuccess) {
                            systemYmResult.getOrNull() ?: emptyList()
                        } else emptyList()
                        _debtState.value = UiState.Success((debtsYm + debtsSys).distinctBy { it.id })
                    } else {
                        _debtState.value = UiState.Error(ymResult.exceptionOrNull()?.message ?: "Lỗi tải danh sách nợ")
                    }
                }
                3 -> {
                    // Đã Thanh Toán
                    _paidState.value = UiState.Loading
                    val ymResult = getPaidListUseCase(ym, currentRoadCode, "")
                    val systemYmResult = if (systemYm != ym) {
                        getPaidListUseCase(systemYm, currentRoadCode, "")
                    } else null

                    if (ymResult.isSuccess) {
                        val paidYm = ymResult.getOrNull() ?: emptyList()
                        val paidSys = if (systemYmResult != null && systemYmResult.isSuccess) {
                            systemYmResult.getOrNull() ?: emptyList()
                        } else emptyList()
                        _paidState.value = UiState.Success((paidYm + paidSys).distinctBy { it.id })
                    } else {
                        _paidState.value = UiState.Error(ymResult.exceptionOrNull()?.message ?: "Lỗi tải danh sách đã thanh toán")
                    }
                }
            }
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
