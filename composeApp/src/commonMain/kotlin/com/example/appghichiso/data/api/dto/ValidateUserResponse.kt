package com.example.appghichiso.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ValidateUserResponse(
    val status: ApiStatus
)

