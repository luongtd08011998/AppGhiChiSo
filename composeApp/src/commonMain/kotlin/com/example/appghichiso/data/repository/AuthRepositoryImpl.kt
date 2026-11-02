package com.example.appghichiso.data.repository

import com.example.appghichiso.data.api.AuthApiService
import com.example.appghichiso.data.local.CredentialsStorage
import com.example.appghichiso.domain.repository.AuthRepository
import com.example.appghichiso.session.SessionManager
import com.example.appghichiso.util.Logger

private const val TAG = "AuthRepository"

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
            authApiService.validateUser(username, password)
            // Nếu validateUser thành công (không ném Exception), kích hoạt session
            sessionManager.activate(username, password, month, year)
            credentialsStorage.save(username, password, rememberMe)
            credentialsStorage.saveMonthYear(month, year)
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "login failed for user=$username: ${e.message}" }
            Result.failure(Exception(e.message ?: "Đăng nhập thất bại. Vui lòng kiểm tra tài khoản và mật khẩu."))
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

    override fun saveMonthYear(month: Int, year: Int) {
        credentialsStorage.saveMonthYear(month, year)
    }

    override fun isRememberMe(): Boolean = credentialsStorage.isRememberMe()
}


