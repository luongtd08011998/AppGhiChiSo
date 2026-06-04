package com.example.appghichiso.domain.repository

interface MeterReadingRepository {

    suspend fun submitReading(
        invoiceId: Long,
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

    /**
     * Lịch sử tiêu thụ 6 tháng qua API consumption-history (1 request duy nhất).
     */
    suspend fun getConsumptionHistoryFast(
        customerCode: String,
        fromYearMonth: String,
        toYearMonth: String
    ): Result<List<Pair<String, Int>>>
}
