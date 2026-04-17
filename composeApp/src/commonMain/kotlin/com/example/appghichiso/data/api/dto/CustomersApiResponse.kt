@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.example.appghichiso.data.api.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class CustomerDto(
    @JsonNames("customer_id")
    val customerId: Int = 0,
    val contractCode: String = "",
    val contractSerial: String = "",
    val currentIndex: Int = 0,
    val customerAddress: String = "",
    val customerCode: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val customerSms: String = "",
    val envFee: Double = 0.0,
    val maxScale: Int = 0,
    val month: Int = 0,
    val previousAmount: Double = 0.0,
    val previousIndex: Int = 0,
    val priceSchemaCode: String = "",
    val priceSchemaName: String = "",
    val roadCode: String = "",
    val roadName: String = "",
    val roadOrder: Int = 0,
    val taxFee: Double = 0.0,
    val year: Int = 0
)

@Serializable
data class CustomersApiResponse(
    val data: List<CustomerDto>,
    val status: ApiStatus
)

