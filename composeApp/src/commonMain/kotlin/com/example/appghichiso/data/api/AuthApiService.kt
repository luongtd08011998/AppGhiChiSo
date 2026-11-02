package com.example.appghichiso.data.api
 
import com.example.appghichiso.data.api.dto.RoadDto
import com.example.appghichiso.data.api.dto.ValidateUserResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class AuthApiService(private val client: HttpClient) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Xác thực tài khoản nhân viên.
     * Thử validate-user chính thức trước; nếu thất bại thử fallback qua $TVAN_URL/roads.
     */
    @OptIn(ExperimentalEncodingApi::class)
    suspend fun validateUser(userName: String, password: String): Boolean {
        // 1. Thử validate-user chính thức của cm-portlet
        try {
            val response: ValidateUserResponse = client.get(
                "$BASE_URL/api/jsonws/cm-portlet.api/validate-user/user-name/$userName/password/$password"
            ).body()
            if (response.status?.code == "success") {
                return true
            }
        } catch (_: Exception) {
            // tiếp tục thử phương thức dự phòng
        }

        // 2. Dự phòng: gọi $TVAN_URL/roads với Basic Auth
        try {
            val authString = "$userName:$password"
            val base64 = Base64.Default.encode(authString.encodeToByteArray())
            val response = client.get("$TVAN_URL/roads") {
                headers {
                    append(HttpHeaders.Authorization, "Basic $base64")
                }
            }
            val text = response.bodyAsText().trim()
            if (text.isNotBlank()) {
                val roads = json.decodeFromString<List<RoadDto>>(text)
                if (roads.isNotEmpty()) return true
            }
        } catch (_: Exception) {
            // lỗi xác thực
        }

        throw Exception("Tài khoản hoặc mật khẩu không chính xác hoặc chưa được phân quyền.")
    }
}

