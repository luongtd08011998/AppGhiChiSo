package com.example.appghichiso.data.api

import com.example.appghichiso.data.api.dto.UpdateIndexApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class MeterReadingApiService(private val client: HttpClient) {
    suspend fun updateIndex(
        customerCode: String,
        contractCode: String,
        year: Int,
        month: Int,
        newIndex: Int
    ): UpdateIndexApiResponse =
        client.get(
            "$BASE_URL/api/jsonws/cm-portlet.api/update-index" +
                "/customer-code/$customerCode" +
                "/contract-code/$contractCode" +
                "/year/$year/month/$month/new-index/$newIndex"
        ).body()
}

