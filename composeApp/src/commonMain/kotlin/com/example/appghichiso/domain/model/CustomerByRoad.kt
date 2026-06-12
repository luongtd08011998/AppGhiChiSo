package com.example.appghichiso.domain.model

data class CustomerByRoad(
    val id: Long,
    val customerCode: String,
    val customerName: String,
    val customerAddress: String,
    val phone: String,
    val sms: String
)
