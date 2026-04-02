package com.example.appghichiso.data.repository

import com.example.appghichiso.data.api.AuthApiService
import com.example.appghichiso.data.local.CredentialsStorage
import com.example.appghichiso.domain.repository.AuthRepository
import com.example.appghichiso.session.SessionManager

class AuthRepositoryImpl(
    private val credentialsStorage: CredentialsStorage,
    private val authApiService: AuthApiService,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun login(
        username: String,
        password: String,
        rememberMe: Boolean,
        month: Int,
        year: Int
    ): Result<Unit> {
        return try {
            val response = authApiService.validateUser(username, password)
            if (response.status.code == "success") {
                // Activate in-memory session — credentials live only while process is alive
                sessionManager.activate(username, password, month, year)
                // Persist only for pre-filling the login form on next launch
                credentialsStorage.save(username, password, rememberMe)
                credentialsStorage.saveMonthYear(month, year)
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.status.message))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Đăng nhập thất bại. Vui lòng kiểm tra tài khoản và mật khẩu."))
        }
    }

    /** Session is active only while the process is alive (in-memory). */
    override fun isLoggedIn(): Boolean = sessionManager.isActive

    override fun clearCredentials() {
        sessionManager.deactivate()
        credentialsStorage.clear()
    }

    override fun getSavedUsername(): String? = credentialsStorage.getSavedUsername()

    override fun getSavedPassword(): String? = credentialsStorage.getSavedPassword()

    override fun getSavedMonthYear(): Pair<Int, Int>? = credentialsStorage.getSavedMonthYear()

    override fun isRememberMe(): Boolean = credentialsStorage.isRememberMe()
}


