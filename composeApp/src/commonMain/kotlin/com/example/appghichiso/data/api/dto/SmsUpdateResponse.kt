package com.example.appghichiso.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class SmsUpdateResponse(
    val error: SmsUpdateError? = null,
    val retCode: String = "",
    val retMsg: String = "",
    val result: String = ""
)

@Serializable
data class SmsUpdateError(
    val code: String = "",
    val message: String = "",
    val description: String = ""
)
