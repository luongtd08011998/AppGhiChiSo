package com.example.appghichiso.data.repository

import com.example.appghichiso.data.api.CustomerApiService
import com.example.appghichiso.data.api.dto.CustomerDto
import com.example.appghichiso.domain.model.Customer
import com.example.appghichiso.domain.repository.CustomerRepository
import com.example.appghichiso.util.currentMonth
import com.example.appghichiso.util.currentYear

class CustomerRepositoryImpl(private val apiService: CustomerApiService) : CustomerRepository {

    override suspend fun getCustomers(roadCode: String, year: Int, month: Int): Result<List<Customer>> {
        return try {
            val response = apiService.getCustomers(roadCode, year, month)
            val customers = (response.data ?: emptyList())
                .sortedBy { it.roadOrder }
                .map { dto ->
                    Customer(
                        customerId = dto.customerId,
                        contractCode = dto.contractCode,
                        contractSerial = dto.contractSerial,
                        customerCode = dto.customerCode,
                        customerName = dto.customerName,
                        customerAddress = dto.customerAddress,
                        customerPhone = dto.customerPhone,
                        previousIndex = dto.previousIndex,
                        currentIndex = dto.currentIndex,
                        month = dto.month,
                        year = dto.year,
                        roadCode = dto.roadCode,
                        roadName = dto.roadName,
                        roadOrder = dto.roadOrder,
                        priceSchemaName = dto.priceSchemaName
                    )
                }
            if (response.status?.code == "success" || response.data != null) {
                Result.success(customers)
            } else {
                val msg = response.status?.message ?: "Lỗi từ server (Backend Error)"
                val isPast = year < currentYear() || (year == currentYear() && month < currentMonth())
                val friendly = when {
                    msg.contains("error occur", ignoreCase = true) && isPast ->
                        "Kỳ $month/$year đã chốt! Không thể tải danh sách khách hàng. Vui lòng đổi kỳ ghi!"
                    msg.contains("error occur", ignoreCase = true) ->
                        "Chưa mở ghi chỉ số kỳ $month. Vui lòng đổi kỳ ghi!"
                    else -> msg
                }
                Result.failure(Exception(friendly))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tải danh sách khách hàng: ${e.message}"))
        }
    }
}

