package com.example.appghichiso.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiStatus(
    val code: String,
    val message: String
)

