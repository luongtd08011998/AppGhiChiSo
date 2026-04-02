package com.example.appghichiso.domain.repository

interface MeterReadingRepository {
    suspend fun submitReading(
        customerCode: String,
        contractCode: String,
        year: Int,
        month: Int,
        newIndex: Int
    ): Result<Unit>
}

