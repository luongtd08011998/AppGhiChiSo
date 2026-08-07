package com.example.appghichiso.data.api

import com.example.appghichiso.data.api.dto.PaybillQrRequest
import com.example.appghichiso.data.api.dto.PaybillQrResponse
import com.example.appghichiso.data.api.dto.PaybillQrApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class PaybillApiService(private val client: HttpClient) {
    suspend fun getQrCode(request: PaybillQrRequest): PaybillQrResponse {
        val apiResponse = client.post("$SLN_URL/api/v1/qlkh/paybill/qr") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<PaybillQrApiResponse>()
        return apiResponse.data ?: PaybillQrResponse()
    }
}
