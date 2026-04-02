package com.example.appghichiso.domain.repository

import com.example.appghichiso.domain.model.Road

interface RoadRepository {
    suspend fun getRoads(): Result<List<Road>>
}

