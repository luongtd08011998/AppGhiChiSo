package com.example.appghichiso.data.api

import com.example.appghichiso.data.api.dto.CustomersApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class CustomerApiService(private val client: HttpClient) {
    suspend fun getCustomers(roadCode: String, year: Int, month: Int): CustomersApiResponse =
        client.get(
            "$BASE_URL/api/jsonws/cm-portlet.api/get-customers-by-road" +
                "/road-code/$roadCode/year/$year/month/$month"
        ).body()
}

