package com.example.appghichiso.data.repository

import com.example.appghichiso.data.api.MeterReadingApiService
import com.example.appghichiso.domain.repository.MeterReadingRepository

class MeterReadingRepositoryImpl(private val apiService: MeterReadingApiService) : MeterReadingRepository {

    override suspend fun submitReading(
        customerCode: String,
        contractCode: String,
        year: Int,
        month: Int,
        newIndex: Int
    ): Result<Unit> {
        return try {
            val response = apiService.updateIndex(customerCode, contractCode, year, month, newIndex)
            if (response.status.code == "success") {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.status.message))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ghi chỉ số thất bại: ${e.message}"))
        }
    }
}

