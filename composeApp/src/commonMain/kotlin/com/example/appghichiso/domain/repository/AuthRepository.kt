package com.example.appghichiso.domain.repository

interface AuthRepository {
    suspend fun login(
        username: String,
        password: String,
        rememberMe: Boolean,
        month: Int,
        year: Int
    ): Result<Unit>
    fun isLoggedIn(): Boolean
    fun clearCredentials()
    fun getSavedUsername(): String?
    fun getSavedPassword(): String?
    fun getSavedMonthYear(): Pair<Int, Int>?
    fun isRememberMe(): Boolean
}


