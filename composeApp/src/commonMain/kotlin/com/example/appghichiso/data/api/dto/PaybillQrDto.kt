package com.example.appghichiso.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class PaybillQrRequest(
    val digiCode: String,
    val monthInvoiceIds: List<Long>
)

@Serializable
data class PaybillQrResponse(
    val qrContent: String? = null,
    val qrIdentify: String? = null,
    val expiredDate: String? = null,
    val totalAmount: Double? = null,
    val periods: List<String>? = null
)

@Serializable
data class PaybillQrApiResponse(
    val status: String? = null,
    val message: String? = null,
    val data: PaybillQrResponse? = null
)
