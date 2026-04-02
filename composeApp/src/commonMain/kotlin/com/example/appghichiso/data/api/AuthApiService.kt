package com.example.appghichiso.data.api

import com.example.appghichiso.data.api.dto.ValidateUserResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class AuthApiService(private val client: HttpClient) {
    /**
     * Xác thực tài khoản nhân viên.
     * GET /api/jsonws/cm-portlet.api/validate-user/user-name/{userName}/password/{password}
     * Auth header KHÔNG được gửi ở bước này (credentials chưa được lưu).
     */
    suspend fun validateUser(userName: String, password: String): ValidateUserResponse =
        client.get(
            "$BASE_URL/api/jsonws/cm-portlet.api/validate-user" +
                "/user-name/$userName/password/$password"
        ).body()
}

