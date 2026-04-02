package com.example.appghichiso.presentation.reading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appghichiso.domain.usecase.SubmitMeterReadingUseCase
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

class MeterReadingViewModel(
    private val submitMeterReadingUseCase: SubmitMeterReadingUseCase
) : ViewModel() {

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    fun submit(
        customerCode: String,
        contractCode: String,
        year: Int,
        month: Int,
        previousIndex: Int,
        newIndex: Int
    ) {
        viewModelScope.launch {
            _submitState.value = SubmitState.Loading
            submitMeterReadingUseCase(
                customerCode = customerCode,
                contractCode = contractCode,
                year = year,
                month = month,
                newIndex = newIndex,
                previousIndex = previousIndex
            )
                .onSuccess { _submitState.value = SubmitState.Success }
                .onFailure { _submitState.value = SubmitState.Error(it.message ?: "Ghi chỉ số thất bại") }
        }
    }

    fun resetState() {
        _submitState.value = SubmitState.Idle
    }
}

