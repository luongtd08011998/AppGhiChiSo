package com.example.appghichiso.domain.usecase

import com.example.appghichiso.domain.model.Customer
import com.example.appghichiso.domain.repository.CustomerRepository

class GetCustomersUseCase(private val customerRepository: CustomerRepository) {
    suspend operator fun invoke(roadCode: String, year: Int, month: Int): Result<List<Customer>> =
        customerRepository.getCustomers(roadCode, year, month)
}

