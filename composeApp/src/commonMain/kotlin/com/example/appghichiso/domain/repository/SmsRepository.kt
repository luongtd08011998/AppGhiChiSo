package com.example.appghichiso.domain.repository

interface SmsRepository {
    suspend fun getSms(customerCode: String): Result<String?>
    suspend fun updateSms(customerCode: String, sms: String): Result<String>
}
