package com.example.appghichiso.data.repository

import com.example.appghichiso.data.api.RoadApiService
import com.example.appghichiso.domain.model.Road
import com.example.appghichiso.domain.repository.RoadRepository
import com.example.appghichiso.util.Logger

private const val TAG = "RoadRepository"

class RoadRepositoryImpl(private val apiService: RoadApiService) : RoadRepository {

    override suspend fun getRoads(): Result<List<Road>> {
        return try {
            val roads = apiService.getRoads()
            Result.success(roads.map { Road(code = it.code, name = it.name) })
        } catch (e: Exception) {
            Logger.e(TAG, e) { "getRoads failed" }
            Result.failure(Exception(e.message ?: "Không thể tải danh sách tuyến"))
        }
    }
}

