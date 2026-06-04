package com.example.appghichiso.data.repository

import com.example.appghichiso.data.api.SmsApiService
import com.example.appghichiso.domain.repository.SmsRepository
import com.example.appghichiso.util.Logger

private const val TAG = "SmsRepository"

class SmsRepositoryImpl(private val apiService: SmsApiService) : SmsRepository {

    override suspend fun getSms(customerCode: String): Result<String?> {
        return try {
            val sms = apiService.getSms(customerCode)
            Result.success(sms)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "getSms failed: customerCode=$customerCode" }
            Result.failure(Exception("Không thể tải số SMS: ${e.message}"))
        }
    }

    override suspend fun updateSms(customerCode: String, sms: String): Result<String> {
        return try {
            val response = apiService.updateSms(customerCode, sms)
            val code = response.error?.code?.ifBlank { response.retCode } ?: response.retCode
            val msg = response.error?.message?.ifBlank { response.retMsg } ?: response.retMsg
            
            val isSuccess = code == "SMS-OK" || code == "ERR-OK" || code.endsWith("OK")
            val isNoChange = code == "ERR-05"
            
            when {
                isSuccess ->
                    Result.success(msg.ifBlank { "Cập nhật SMS thành công" })
                isNoChange ->
                    Result.success("Số SMS không thay đổi (đã đúng)")
                code == "SMS-01" ->
                    Result.failure(Exception("Mã khách hàng không chính xác (SMS-01)."))
                code == "SMS-02" ->
                    Result.failure(Exception("Số điện thoại không được để trống (SMS-02)."))
                code == "SMS-03" ->
                    Result.failure(Exception("Số điện thoại không đúng định dạng di động Việt Nam (SMS-03)."))
                code == "SMS-04" ->
                    Result.failure(Exception("Lỗi hệ thống khi cập nhật cơ sở dữ liệu (SMS-04)."))
                else ->
                    Result.failure(Exception(msg.ifBlank { "Cập nhật SMS thất bại ($code)" }))
            }
        } catch (e: Exception) {
            Logger.e(TAG, e) { "updateSms failed: customerCode=$customerCode" }
            Result.failure(Exception("Cập nhật SMS thất bại: ${e.message}"))
        }
    }

    override suspend fun updatePhone(customerCode: String, phone: String): Result<String> {
        return try {
            val response = apiService.updatePhone(customerCode, phone)
            val code = response.error?.code?.ifBlank { response.retCode } ?: response.retCode
            val msg = response.error?.message?.ifBlank { response.retMsg } ?: response.retMsg
            
            val isSuccess = code == "ERR-OK" || code.endsWith("OK") || code == "00" // Not sure about Liferay custom codes, assuming ERR-OK is success
            val isNoChange = code == "ERR-05"
            
            when {
                isSuccess ->
                    Result.success(msg.ifBlank { "Cập nhật số điện thoại thành công" })
                isNoChange ->
                    Result.success("Số điện thoại không thay đổi")
                else ->
                    Result.failure(Exception(msg.ifBlank { "Cập nhật thất bại ($code)" }))
            }
        } catch (e: Exception) {
            Logger.e(TAG, e) { "updatePhone failed: customerCode=$customerCode" }
            Result.failure(Exception("Cập nhật số điện thoại thất bại: ${e.message}"))
        }
    }
}
