package com.example.appghichiso.domain.repository

interface MeterReadingRepository {

    suspend fun submitReading(
        customerCode: String,
        contractCode: String,
        year: Int,
        month: Int,
        newIndex: Int
    ): Result<Unit>

    /**
     * Tiêu thụ kỳ [yearMonth]: `GET ...?yearMonth=YYYYMM`.
     * Ưu tiên dòng có `digiCode` trùng [customerCode]; không khớp thì fallback `newVal == previousIndex`.
     */
    suspend fun getPreviousMonthConsumption(
        customerCode: String,
        yearMonth: String,
        previousIndex: Int
    ): Result<Int?>

    /**
     * Lịch sử tiêu thụ theo từng tháng trong khoảng.
     * Có [customerCode]: lọc theo digiCode; không có: nối chuỗi theo chỉ số.
     */
    suspend fun getConsumptionHistory(
        customerCode: String,
        fromYearMonth: String,
        toYearMonth: String,
        previousIndex: Int
    ): Result<List<Pair<String, Int>>>
}
