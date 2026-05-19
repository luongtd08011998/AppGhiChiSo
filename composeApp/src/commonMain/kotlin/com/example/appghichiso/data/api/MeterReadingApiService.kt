package com.example.appghichiso.data.api

import com.example.appghichiso.data.api.dto.ConsumptionHistoryResponse
import com.example.appghichiso.data.api.dto.MonthInvoiceReadingResponse
import com.example.appghichiso.data.api.dto.UpdateIndexApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true; isLenient = true }

class MeterReadingApiService(private val client: HttpClient) {

    suspend fun updateIndex(
        customerCode: String,
        contractCode: String,
        year: Int,
        month: Int,
        newIndex: Int
    ): UpdateIndexApiResponse {
        val response = client.get(
            "$BASE_URL/api/jsonws/cm-portlet.api/update-index" +
                "/customer-code/$customerCode" +
                "/contract-code/$contractCode" +
                "/year/$year/month/$month/new-index/$newIndex"
        )
        val body = response.bodyAsText().trim()
        return try {
            json.decodeFromString<UpdateIndexApiResponse>(body)
        } catch (_: Exception) {
            // Server trả plain text lỗi thay vì JSON
            UpdateIndexApiResponse(
                status = com.example.appghichiso.data.api.dto.ApiStatus(
                    code = "error",
                    message = body
                )
            )
        }
    }

    suspend fun getReadingsByMonth(yearMonth: String): MonthInvoiceReadingResponse =
        client.get(
            "$SLN_URL/api/v1/qlkh/month-invoices/readings?yearMonth=$yearMonth"
        ).body()

    suspend fun getReadingsByRange(
        fromYearMonth: String,
        toYearMonth: String
    ): MonthInvoiceReadingResponse =
        client.get(
            "$SLN_URL/api/v1/qlkh/month-invoices/readings" +
                "?fromYearMonth=$fromYearMonth&toYearMonth=$toYearMonth"
        ).body()

    suspend fun getConsumptionHistory(
        customerCode: String,
        fromYearMonth: String,
        toYearMonth: String
    ): ConsumptionHistoryResponse =
        client.get(
            "$SLN_URL/api/v1/qlkh/month-invoices/consumption-history" +
                "?customerCode=$customerCode&fromYearMonth=$fromYearMonth&toYearMonth=$toYearMonth"
        ).body()
}
