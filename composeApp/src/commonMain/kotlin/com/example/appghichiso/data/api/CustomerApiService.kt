package com.example.appghichiso.data.api

import com.example.appghichiso.data.api.dto.CustomersApiResponse
import com.example.appghichiso.data.api.dto.InvoiceByRoadResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class CustomerApiService(private val client: HttpClient) {
    suspend fun getCustomers(roadCode: String, year: Int, month: Int): CustomersApiResponse =
        client.get(
            "$BASE_URL/api/jsonws/cm-portlet.api/get-customers-by-road" +
                "/road-code/$roadCode/year/$year/month/$month"
        ).body()

    suspend fun getInvoicesByRoad(roadCode: String, year: Int, month: Int, page: Int = 0): InvoiceByRoadResponse =
        client.get(
            "$BASE_URL/api/jsonws/cm-portlet.api/get-invoices-by-road" +
                "/road-code/$roadCode/year/$year/month/$month/page/$page"
        ).body()
}

