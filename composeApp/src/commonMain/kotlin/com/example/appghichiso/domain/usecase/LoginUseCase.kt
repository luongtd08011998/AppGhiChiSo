package com.example.appghichiso.domain.usecase

import com.example.appghichiso.domain.repository.AuthRepository

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(
        username: String,
        password: String,
        rememberMe: Boolean,
        month: Int,
        year: Int
    ): Result<Unit> = authRepository.login(username, password, rememberMe, month, year)
}
