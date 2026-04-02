package com.example.appghichiso.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appghichiso.di.AppStateHolder
import com.example.appghichiso.domain.repository.AuthRepository
import com.example.appghichiso.domain.usecase.LoginUseCase
import com.example.appghichiso.util.currentMonth
import com.example.appghichiso.util.currentYear
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val authRepository: AuthRepository,
    private val appStateHolder: AppStateHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val isLoggedIn: Boolean get() = authRepository.isLoggedIn()

    // Giá trị khởi tạo cho LoginScreen
    val savedUsername: String? = authRepository.getSavedUsername()
    val initialRememberMe: Boolean = authRepository.isRememberMe()
    val initialMonth: Int = authRepository.getSavedMonthYear()?.first ?: currentMonth()
    val initialYear: Int  = authRepository.getSavedMonthYear()?.second ?: currentYear()

    fun login(
        username: String,
        password: String,
        rememberMe: Boolean,
        month: Int,
        year: Int
    ) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Vui lòng nhập tài khoản và mật khẩu")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            loginUseCase(username, password, rememberMe, month, year)
                .onSuccess {
                    appStateHolder.billingMonth = month
                    appStateHolder.billingYear  = year
                    _uiState.value = LoginUiState.Success
                }
                .onFailure { _uiState.value = LoginUiState.Error(it.message ?: "Lỗi không xác định") }
        }
    }

    fun logout() {
        authRepository.clearCredentials()
        appStateHolder.billingMonth = currentMonth()
        appStateHolder.billingYear  = currentYear()
        appStateHolder.selectedRoad = null
        appStateHolder.selectedCustomer = null
        appStateHolder.recordedCustomerCodes.clear()
        _uiState.value = LoginUiState.Idle
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}
