package com.example.appghichiso.data.repository

import com.example.appghichiso.data.api.MeterReadingApiService
import com.example.appghichiso.data.api.dto.MonthInvoiceReadingDto
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

    override suspend fun getPreviousMonthConsumption(
        customerCode: String,
        yearMonth: String,
        previousIndex: Int
    ): Result<Int?> {
        return try {
            val response = apiService.getReadingsByMonth(yearMonth)
            val entry = entryForCustomer(response.data, customerCode, previousIndex)
            Result.success(entry?.consumptionM3())
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tải dữ liệu tháng trước: ${e.message}"))
        }
    }

    override suspend fun getConsumptionHistory(
        customerCode: String,
        fromYearMonth: String,
        toYearMonth: String,
        previousIndex: Int
    ): Result<List<Pair<String, Int>>> {
        return try {
            val months = buildMonthRange(fromYearMonth, toYearMonth)
            val points = mutableListOf<Pair<String, Int>>()

            if (customerCode.isNotBlank()) {
                for (ym in months) {
                    try {
                        val response = apiService.getReadingsByMonth(ym)
                        val entry = findRowForCustomer(response.data, customerCode)
                        val c = entry?.consumptionM3()
                        if (c != null && c >= 0) points.add(ym to c)
                    } catch (_: Exception) { /* bỏ qua tháng lỗi */ }
                }
            } else {
                var seedNewVal = previousIndex
                for (ym in months.reversed()) {
                    try {
                        val response = apiService.getReadingsByMonth(ym)
                        val entry = response.data.firstOrNull { it.newVal != null && it.newVal == seedNewVal }
                        val c = entry?.consumptionM3()
                        if (c != null && c >= 0) {
                            points.add(0, ym to c)
                            seedNewVal = entry.oldVal ?: break
                        }
                    } catch (_: Exception) { /* bỏ qua tháng lỗi */ }
                }
            }

            Result.success(points)
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tải lịch sử tiêu thụ: ${e.message}"))
        }
    }

    private fun entryForCustomer(
        data: List<MonthInvoiceReadingDto>,
        customerCode: String,
        closingIndex: Int
    ): MonthInvoiceReadingDto? =
        when {
            customerCode.isNotBlank() ->
                findRowForCustomer(data, customerCode)
                    ?: data.firstOrNull { it.newVal != null && it.newVal == closingIndex }
            else ->
                data.firstOrNull { it.newVal != null && it.newVal == closingIndex }
        }

    /** digiCode = mã KH; fallback schema cũ: customerId nếu mã KH là số. */
    private fun findRowForCustomer(data: List<MonthInvoiceReadingDto>, customerCode: String): MonthInvoiceReadingDto? {
        val byDigi = data.firstOrNull { matchesDigiCode(it, customerCode) }
        if (byDigi != null) return byDigi
        val id = customerCode.trim().toIntOrNull()
        return if (id != null) data.firstOrNull { it.customerId == id } else null
    }

    private fun matchesDigiCode(dto: MonthInvoiceReadingDto, customerCode: String): Boolean {
        val want = customerCode.trim()
        if (want.isEmpty()) return false
        return dto.digiCode.trim().equals(want, ignoreCase = true)
    }

    private fun buildMonthRange(from: String, to: String): List<String> {
        val fromYear = from.substring(0, 4).toInt()
        val fromMonth = from.substring(4, 6).toInt()
        val toYear = to.substring(0, 4).toInt()
        val toMonth = to.substring(4, 6).toInt()

        val result = mutableListOf<String>()
        var y = fromYear
        var m = fromMonth
        while (y < toYear || (y == toYear && m <= toMonth)) {
            result.add("$y${m.toString().padStart(2, '0')}")
            m++
            if (m > 12) {
                m = 1
                y++
            }
        }
        return result
    }
}
