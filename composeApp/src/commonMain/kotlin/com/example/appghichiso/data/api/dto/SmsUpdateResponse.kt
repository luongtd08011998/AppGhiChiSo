package com.example.appghichiso.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class SmsUpdateResponse(
    val retCode: String = "",
    val retMsg: String = "",
    val result: String = ""
)
