package com.example.appghichiso.domain.usecase

import com.example.appghichiso.domain.model.CustomerByRoad
import com.example.appghichiso.domain.repository.CustomerRepository

class GetCustomersByRoadUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(roadCode: String, page: Int = 0): Result<List<CustomerByRoad>> =
        repository.getCustomersByRoad(roadCode, page)
}
