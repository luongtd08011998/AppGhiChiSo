package com.example.appghichiso.data.repository

import com.example.appghichiso.data.api.CustomerApiService
import com.example.appghichiso.domain.model.Customer
import com.example.appghichiso.domain.repository.CustomerRepository

class CustomerRepositoryImpl(private val apiService: CustomerApiService) : CustomerRepository {

    override suspend fun getCustomers(roadCode: String, year: Int, month: Int): Result<List<Customer>> {
        return try {
            val response = apiService.getCustomers(roadCode, year, month)
            if (response.status.code == "success") {
                val customers = response.data
                    .sortedBy { it.roadOrder }
                    .map { dto ->
                        Customer(
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
                Result.success(customers)
            } else {
                Result.failure(Exception(response.status.message))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tải danh sách khách hàng: ${e.message}"))
        }
    }
}

