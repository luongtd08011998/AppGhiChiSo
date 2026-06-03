package com.example.appghichiso.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class InvoiceDto(
    val id: Long,
    val invNumber: String? = null,
    val yearMonth: String? = null,
    val type: Int = 1,
    val custCode: String? = null,
    val custName: String? = null,
    val custAddress: String? = null,
    val totalAmount: Double,
    val oldIndex: Long,
    val newIndex: Long,
    val empPhone: String? = null,
    val numOfPages: Int? = null
)

@Serializable
data class TvanPublishResponse(
    val retCode: String = "",
    val retMsg: String = "",
    val result: String = ""
)

@Serializable
data class PayCashResponse(
    val retCode: String = "",
    val retMsg: String = "",
    val result: String = "" // số phiếu thu
)

@Serializable
data class ReceiptDto(
    val id: Long = 0,
    val invNumber: String? = null,
    val custCode: String = "",
    val custName: String = "",
    val custAddress: String = "",
    val custTaxCode: String? = null,
    val numOfHouseHold: Int = 1,
    val timeToUsedFrom: String = "",
    val timeToUsedTo: String = "",
    val period: String = "",
    val oldIndex: Long = 0,
    val newIndex: Long = 0,
    val volumn0: String? = null,
    val volumn1: String? = null,
    val volumn2: String? = null,
    val volumn3: String? = null,
    val price0: String? = null,
    val price1: String? = null,
    val price2: String? = null,
    val price3: String? = null,
    val amount0: String? = null,
    val amount1: String? = null,
    val amount2: String? = null,
    val amount3: String? = null,
    val amount: String = "",
    val taxFee: String = "",
    val envFee: String = "",
    val totalAmount: String = "",
    val totalAmountInWord: String = "",
    val lookupCode: String = ""
)
