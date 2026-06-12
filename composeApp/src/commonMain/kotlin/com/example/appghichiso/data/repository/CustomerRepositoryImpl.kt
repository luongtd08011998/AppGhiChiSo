package com.example.appghichiso.data.repository

import com.example.appghichiso.data.api.CustomerApiService
import com.example.appghichiso.domain.model.Customer
import com.example.appghichiso.domain.model.CustomerByRoad
import com.example.appghichiso.domain.repository.CustomerRepository
import com.example.appghichiso.util.Logger
import com.example.appghichiso.util.currentMonth
import com.example.appghichiso.util.currentYear

private const val TAG = "CustomerRepository"

class CustomerRepositoryImpl(private val apiService: CustomerApiService) : CustomerRepository {

    override suspend fun getCustomers(roadCode: String, year: Int, month: Int, page: Int): Result<List<Customer>> {
        return try {
            val response = apiService.getInvoicesByRoad(roadCode, year, month, page = page)
            val allData = response.data ?: emptyList()

            val customers = allData.map { dto ->
                Customer(
                    customerId = 0,
                    contractCode = dto.contractCode,
                    contractSerial = "",
                    customerCode = dto.customerCode,
                    customerName = dto.customerName,
                    customerAddress = dto.customerAddress,
                    customerPhone = dto.customerPhone,
                    previousIndex = dto.previousIndex,
                    currentIndex = dto.currentIndex,
                    month = dto.month,
                    year = dto.year,
                    roadCode = roadCode,
                    roadName = "",
                    roadOrder = 0,
                    priceSchemaName = "",
                    invoiceId = if (dto.id > 0) dto.id else null,
                    numOfPages = dto.numOfPages
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
            Logger.e(TAG, e) { "getCustomers failed: roadCode=$roadCode year=$year month=$month" }
            Result.failure(Exception("Không thể tải danh sách khách hàng: ${e.message}"))
        }
    }

    override suspend fun getCustomersWithInvoices(roadCode: String, year: Int, month: Int, page: Int): Result<List<Customer>> {
        return getCustomers(roadCode, year, month, page)
    }

    override suspend fun getCustomersByRoad(roadCode: String, page: Int): Result<List<CustomerByRoad>> {
        return try {
            val pageData = apiService.getCustomersByRoad(roadCode, page = page)

            val result = pageData.map { dto ->
                CustomerByRoad(
                    id = dto.id,
                    customerCode = dto.code,
                    customerName = dto.name,
                    customerAddress = dto.address,
                    phone = dto.phone,
                    sms = dto.sms,
                    numOfPages = dto.numOfPages
                )
            }
            Result.success(result)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "getCustomersByRoad failed: roadCode=$roadCode" }
            Result.failure(Exception("Không thể tải danh sách khách hàng: ${e.message}"))
        }
    }
}
