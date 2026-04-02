package com.example.appghichiso.data.api

import com.example.appghichiso.data.api.dto.RoadsApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class RoadApiService(private val client: HttpClient) {
    suspend fun getRoads(): RoadsApiResponse =
        client.get("$BASE_URL/api/jsonws/cm-portlet.api/get-roads").body()
}

