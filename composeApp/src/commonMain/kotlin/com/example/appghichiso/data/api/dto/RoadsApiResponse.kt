package com.example.appghichiso.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoadDto(
    val code: String,
    val name: String
)

@Serializable
data class RoadsApiResponse(
    val data: List<RoadDto>,
    val status: ApiStatus
)

