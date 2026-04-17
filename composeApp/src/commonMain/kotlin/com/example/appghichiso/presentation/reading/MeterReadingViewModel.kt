package com.example.appghichiso.presentation.reading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appghichiso.domain.usecase.SubmitMeterReadingUseCase
import com.example.appghichiso.domain.repository.MeterReadingRepository
import kotlinx.coroutines.async
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

class MeterReadingViewModel(
    private val submitMeterReadingUseCase: SubmitMeterReadingUseCase,
    private val meterReadingRepository: MeterReadingRepository
) : ViewModel() {

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    /** Tiêu thụ tháng trước. null = chưa tải, -1 = không có dữ liệu. */
    private val _previousMonthConsumption = MutableStateFlow<Int?>(null)
    val previousMonthConsumption: StateFlow<Int?> = _previousMonthConsumption.asStateFlow()

    private val _historyState = MutableStateFlow<HistoryState>(HistoryState.Idle)
    val historyState: StateFlow<HistoryState> = _historyState.asStateFlow()

    fun loadCustomerData(customerCode: String, year: Int, month: Int, previousIndex: Int) {
        _previousMonthConsumption.value = null
        _historyState.value = HistoryState.Loading

        val prevYear = if (month == 1) year - 1 else year
        val prevMonth = if (month == 1) 12 else month - 1
        val prevYearMonth = "$prevYear${prevMonth.toString().padStart(2, '0')}"

        val (fromYear, fromMonth) = shiftMonth(year, month, -5)
        val fromYearMonth = "$fromYear${fromMonth.toString().padStart(2, '0')}"
        val toYearMonth = "$year${month.toString().padStart(2, '0')}"

        viewModelScope.launch {
            val prevJob = async {
                meterReadingRepository.getPreviousMonthConsumption(customerCode, prevYearMonth, previousIndex)
            }
            val histJob = async {
                meterReadingRepository.getConsumptionHistory(customerCode, fromYearMonth, toYearMonth, previousIndex)
            }

            prevJob.await().fold(
                onSuccess = { _previousMonthConsumption.value = it ?: -1 },
                onFailure = { _previousMonthConsumption.value = -1 }
            )

            histJob.await().fold(
                onSuccess = { pairs ->
                    val points = pairs.map { (ym, c) ->
                        ConsumptionPoint(
                            yearMonth = ym,
                            label = "${ym.substring(4, 6)}/${ym.substring(2, 4)}",
                            consumption = c
                        )
                    }
                    _historyState.value = HistoryState.Success(points)
                },
                onFailure = {
                    _historyState.value = HistoryState.Error(it.message ?: "Lỗi tải lịch sử")
                }
            )
        }
    }

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

    private fun shiftMonth(year: Int, month: Int, delta: Int): Pair<Int, Int> {
        var m = month + delta
        var y = year
        while (m <= 0) { m += 12; y-- }
        while (m > 12) { m -= 12; y++ }
        return Pair(y, m)
    }
}
