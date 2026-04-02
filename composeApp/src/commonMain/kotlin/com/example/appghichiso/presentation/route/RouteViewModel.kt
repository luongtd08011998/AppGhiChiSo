package com.example.appghichiso.presentation.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appghichiso.domain.model.Road
import com.example.appghichiso.domain.usecase.GetRoadsUseCase
import com.example.appghichiso.presentation.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RouteViewModel(private val getRoadsUseCase: GetRoadsUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Road>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Road>>> = _uiState.asStateFlow()

    init {
        loadRoads()
    }

    fun loadRoads() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            getRoadsUseCase()
                .onSuccess { _uiState.value = UiState.Success(it) }
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Lỗi tải danh sách tuyến") }
        }
    }
}

