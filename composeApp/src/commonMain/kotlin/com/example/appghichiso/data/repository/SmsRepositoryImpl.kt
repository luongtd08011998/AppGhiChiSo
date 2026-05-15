package com.example.appghichiso.data.repository

import com.example.appghichiso.data.api.SmsApiService
import com.example.appghichiso.domain.repository.SmsRepository

class SmsRepositoryImpl(private val apiService: SmsApiService) : SmsRepository {

    override suspend fun getSms(customerCode: String): Result<String?> {
        return try {
            val sms = apiService.getSms(customerCode)
            Result.success(sms)
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tải số SMS: ${e.message}"))
        }
    }

    override suspend fun updateSms(customerCode: String, sms: String): Result<String> {
        return try {
            val response = apiService.updateSms(customerCode, sms)
            val code = response.retCode
            val isSuccess = code == "SMS-OK" || code == "ERR-OK" || code.endsWith("OK")
            val isNoChange = code == "ERR-05"
            when {
                isSuccess ->
                    Result.success(response.retMsg.ifBlank { "Cập nhật SMS thành công" })
                isNoChange ->
                    Result.success("Số SMS không thay đổi (đã đúng)")
                else ->
                    Result.failure(Exception(response.retMsg.ifBlank { "Cập nhật SMS thất bại ($code)" }))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Cập nhật SMS thất bại: ${e.message}"))
        }
    }
}
