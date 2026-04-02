package com.example.appghichiso.data.repository

import com.example.appghichiso.data.api.AuthApiService
import com.example.appghichiso.data.local.CredentialsStorage
import com.example.appghichiso.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val credentialsStorage: CredentialsStorage,
    private val authApiService: AuthApiService
) : AuthRepository {

    override suspend fun login(
        username: String,
        password: String,
        rememberMe: Boolean,
        month: Int,
        year: Int
    ): Result<Unit> {
        return try {
            // Gọi validate-user TRƯỚC khi lưu credentials
            val response = authApiService.validateUser(username, password)
            if (response.status.code == "success") {
                // Xác thực thành công → lưu credentials cho các API call tiếp theo
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

    override fun isLoggedIn(): Boolean = credentialsStorage.isLoggedIn()

    override fun clearCredentials() = credentialsStorage.clear()

    override fun getSavedUsername() = credentialsStorage.getSavedUsername()

    override fun getSavedMonthYear() = credentialsStorage.getSavedMonthYear()

    override fun isRememberMe() = credentialsStorage.isRememberMe()
}
