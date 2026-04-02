package com.example.appghichiso.domain.usecase

import com.example.appghichiso.domain.model.Road
import com.example.appghichiso.domain.repository.RoadRepository

class GetRoadsUseCase(private val roadRepository: RoadRepository) {
    suspend operator fun invoke(): Result<List<Road>> = roadRepository.getRoads()
}

