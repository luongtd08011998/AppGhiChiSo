@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.example.appghichiso.data.api.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

/**
 * Theo [docs/apiDSChiso.md]: `digiCode` = mã KH, `oldVal`/`newVal` có thể null.
 * Giữ [customerId] tùy chọn nếu server còn trả (schema cũ).
 */
@Serializable
data class MonthInvoiceReadingDto(
    @JsonNames("digi_code")
    val digiCode: String = "",
    @JsonNames("customer_id")
    val customerId: Int = 0,
    @JsonNames("old_val")
    val oldVal: Int? = null,
    @JsonNames("new_val")
    val newVal: Int? = null
) {
    fun consumptionM3(): Int? {
        val o = oldVal ?: return null
        val n = newVal ?: return null
        return n - o
    }
}

@Serializable
data class MonthInvoiceReadingResponse(
    @JsonNames("status_code")
    val statusCode: Int = 0,
    val message: String = "",
    val data: List<MonthInvoiceReadingDto> = emptyList()
)
