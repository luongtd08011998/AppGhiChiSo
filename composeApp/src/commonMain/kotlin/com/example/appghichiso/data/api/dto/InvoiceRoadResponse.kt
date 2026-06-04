package com.example.appghichiso.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class InvoiceByRoadDto(
    val id: Long,
    val contractCode: String = "",
    val currentIndex: Int = 0,
    val customerAddress: String = "",
    val customerCode: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val customerSms: String = "",
    val month: Int = 0,
    val numOfPages: Int = 1,
    val previousIndex: Int = 0,
    val year: Int = 0
)

@Serializable
data class InvoiceByRoadResponse(
    val data: List<InvoiceByRoadDto>? = null,
    val status: ApiStatus? = null
)
