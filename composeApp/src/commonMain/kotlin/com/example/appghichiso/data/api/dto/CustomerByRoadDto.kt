package com.example.appghichiso.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class CustomerByRoadDto(
    val id: Long = 0,
    val name: String = "",
    val code: String = "",
    val address: String = "",
    val phone: String = "",
    val sms: String = "",
    val numOfPages: Int = 1
)

typealias CustomerByRoadResponse = List<CustomerByRoadDto>
