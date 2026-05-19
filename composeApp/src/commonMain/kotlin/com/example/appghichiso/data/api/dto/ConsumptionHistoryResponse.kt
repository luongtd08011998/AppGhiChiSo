@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.example.appghichiso.data.api.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class ConsumptionHistoryDto(
    val yearMonth: String = "",
    @JsonNames("old_val")
    val oldVal: Int? = null,
    @JsonNames("new_val")
    val newVal: Int? = null,
    @JsonNames("consumption_m3")
    val consumptionM3: Int? = null
)

@Serializable
data class ConsumptionHistoryResponse(
    @JsonNames("status_code")
    val statusCode: Int = 0,
    val message: String = "",
    val data: List<ConsumptionHistoryDto> = emptyList()
)
