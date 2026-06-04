package com.example.appghichiso.data.api

import com.example.appghichiso.data.api.dto.ConsumptionHistoryResponse
import com.example.appghichiso.data.api.dto.MonthInvoiceReadingResponse
import com.example.appghichiso.data.api.dto.UpdateIndexApiResponse
import com.example.appghichiso.util.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

private const val TAG = "MeterReadingApiService"
private val json = Json { ignoreUnknownKeys = true; isLenient = true }

class MeterReadingApiService(private val client: HttpClient) {

    suspend fun updateInvoice(
        id: Long,
        newIndex: Int
    ): UpdateIndexApiResponse {
        val response = client.post(
            "http://toctienltd.vn/api/jsonws/cm-portlet.api/update-invoice/id/$id/new-index/$newIndex"
        )
        val body = response.bodyAsText().trim()
        return try {
            json.decodeFromString<UpdateIndexApiResponse>(body)
        } catch (e: Exception) {
            Logger.w(TAG, e) { "updateInvoice: failed to parse response body — body=$body" }
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
