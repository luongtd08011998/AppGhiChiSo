package com.example.appghichiso.data.api

import com.example.appghichiso.data.api.dto.RoadDto
import com.example.appghichiso.data.api.dto.RoadsApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

class RoadApiService(private val client: HttpClient) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun getRoads(): List<RoadDto> {
        // 1. Ưu tiên gọi API get-roads (trả về đúng danh sách các tuyến được gán cho nhân viên đang đăng nhập)
        try {
            val response: RoadsApiResponse = client.get("$BASE_URL/api/jsonws/cm-portlet.api/get-roads").body()
            if (response.status?.code == "success" && !response.data.isNullOrEmpty()) {
                return response.data
            }
        } catch (_: Exception) {
            // Fallback nếu có lỗi mạng hoặc API cũ
        }

        // 2. Dự phòng gọi servlet /cm-portlet/api/roads
        val response = client.get("$TVAN_URL/roads")
        val text = response.bodyAsText().trim()
        if (text.isNotBlank()) {
            return json.decodeFromString<List<RoadDto>>(text)
        }
        throw Exception("Không thể tải danh sách tuyến. Phiên đăng nhập có thể đã hết hạn.")
    }
}

