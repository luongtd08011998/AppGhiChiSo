package com.example.appghichiso.presentation.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appghichiso.di.AppStateHolder
import com.example.appghichiso.domain.model.Customer
import com.example.appghichiso.domain.usecase.GetCustomersUseCase
import com.example.appghichiso.presentation.common.UiState
import com.example.appghichiso.util.currentMonth
import com.example.appghichiso.util.currentYear
import com.example.appghichiso.util.currentMonth
import com.example.appghichiso.util.currentYear
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CustomerViewModel(
    private val getCustomersUseCase: GetCustomersUseCase,
    private val appStateHolder: AppStateHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Customer>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Customer>>> = _uiState.asStateFlow()

    private var currentRoadCode: String = ""

    val currentYear: Int get() = appStateHolder.billingYear.takeIf { it > 0 } ?: currentYear()
    val currentMonth: Int get() = appStateHolder.billingMonth.takeIf { it > 0 } ?: currentMonth()

    fun loadCustomers(roadCode: String) {
        currentRoadCode = roadCode
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            getCustomersUseCase(roadCode, currentYear, currentMonth)
                .onSuccess { _uiState.value = UiState.Success(it) }
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Lỗi tải danh sách khách hàng") }
        }
    }

    fun refresh() {
        if (currentRoadCode.isNotEmpty()) loadCustomers(currentRoadCode)
    }

    fun isRecorded(customerCode: String): Boolean =
        appStateHolder.recordedCustomerCodes.contains(customerCode)
}
