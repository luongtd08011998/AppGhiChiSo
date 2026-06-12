package com.example.appghichiso.domain.repository

import com.example.appghichiso.domain.model.Customer
import com.example.appghichiso.domain.model.CustomerByRoad

interface CustomerRepository {
    suspend fun getCustomers(roadCode: String, year: Int, month: Int, page: Int = 0): Result<List<Customer>>
    suspend fun getCustomersWithInvoices(roadCode: String, year: Int, month: Int, page: Int = 0): Result<List<Customer>>
    suspend fun getCustomersByRoad(roadCode: String, page: Int = 0): Result<List<CustomerByRoad>>
}


