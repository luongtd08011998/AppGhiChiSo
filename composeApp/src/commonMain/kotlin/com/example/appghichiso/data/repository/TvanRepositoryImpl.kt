package com.example.appghichiso.data.repository

import com.example.appghichiso.data.api.TvanApiService
import com.example.appghichiso.data.api.dto.InvoiceDto
import com.example.appghichiso.data.api.dto.PayCashResponse
import com.example.appghichiso.data.api.dto.ReceiptDto
import com.example.appghichiso.data.api.dto.TvanPublishResponse
import com.example.appghichiso.domain.repository.TvanRepository

class TvanRepositoryImpl(
    private val tvanApiService: TvanApiService
) : TvanRepository {
    
    override suspend fun getToPublishList(yearMonth: String, roadCode: String, customerCode: String): Result<List<InvoiceDto>> {
        return try {
            val response = tvanApiService.getToPublishList(yearMonth, roadCode, customerCode)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDebtList(yearMonth: String, roadCode: String, customerCode: String): Result<List<InvoiceDto>> {
        return try {
            val response = tvanApiService.getDebtList(yearMonth, roadCode, customerCode)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPaidList(yearMonth: String, roadCode: String, customerCode: String): Result<List<InvoiceDto>> {
        return try {
            val response = tvanApiService.getPaidList(yearMonth, roadCode, customerCode)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun publishToTvan(ids: List<Long>): Result<TvanPublishResponse> {
        return try {
            val response = tvanApiService.publishToTvan(ids)
            if (response.retCode == "ERR_OK") {
                Result.success(response)
            } else {
                Result.failure(Exception(response.retMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun payCash(id: Long): Result<PayCashResponse> {
        return try {
            val response = tvanApiService.payCash(id)
            if (response.retCode == "ERR_OK") {
                Result.success(response)
            } else {
                Result.failure(Exception(response.retMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReceipt(id: Long): Result<ReceiptDto> {
        return try {
            val response = tvanApiService.getReceipt(id)
            if (response != null) {
                Result.success(response)
            } else {
                Result.failure(Exception("Không tìm thấy biên nhận"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
