package com.example.appghichiso.domain.repository

import com.example.appghichiso.domain.model.Customer

interface CustomerRepository {
    suspend fun getCustomers(roadCode: String, year: Int, month: Int): Result<List<Customer>>
}

