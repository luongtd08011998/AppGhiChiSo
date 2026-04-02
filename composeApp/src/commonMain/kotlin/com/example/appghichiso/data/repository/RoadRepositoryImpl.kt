package com.example.appghichiso.data.repository

import com.example.appghichiso.data.api.RoadApiService
import com.example.appghichiso.domain.model.Road
import com.example.appghichiso.domain.repository.RoadRepository

class RoadRepositoryImpl(private val apiService: RoadApiService) : RoadRepository {

    override suspend fun getRoads(): Result<List<Road>> {
        return try {
            val response = apiService.getRoads()
            if (response.status.code == "success") {
                Result.success(response.data.map { Road(code = it.code, name = it.name) })
            } else {
                Result.failure(Exception(response.status.message))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tải danh sách tuyến: ${e.message}"))
        }
    }
}

