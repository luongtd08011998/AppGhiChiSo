package com.example.appghichiso.data.repository

import com.example.appghichiso.data.api.TvanApiService
import com.example.appghichiso.data.api.dto.InvoiceDto
import com.example.appghichiso.data.api.dto.PayCashResponse
import com.example.appghichiso.data.api.dto.ReceiptDto
import com.example.appghichiso.data.api.dto.TvanPublishResponse
import com.example.appghichiso.domain.repository.TvanRepository
import com.example.appghichiso.util.Logger

private const val TAG = "TvanRepository"

class TvanRepositoryImpl(
    private val tvanApiService: TvanApiService
) : TvanRepository {
    
    override suspend fun getToPublishList(yearMonth: String, roadCode: String, customerCode: String, page: Int): Result<List<InvoiceDto>> {
        return try {
            val pageData = tvanApiService.getToPublishList(yearMonth, roadCode, customerCode, pn = page)
            Result.success(pageData)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "getToPublishList failed: ym=$yearMonth rc=$roadCode cc=$customerCode" }
            Result.failure(e)
        }
    }

    override suspend fun getDebtList(yearMonth: String, roadCode: String, customerCode: String, page: Int): Result<List<InvoiceDto>> {
        return try {
            val pageData = tvanApiService.getDebtList(yearMonth, roadCode, customerCode, pn = page)
            Result.success(pageData)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "getDebtList failed: ym=$yearMonth rc=$roadCode cc=$customerCode" }
            Result.failure(e)
        }
    }

    override suspend fun getPaidList(yearMonth: String, roadCode: String, customerCode: String, page: Int): Result<List<InvoiceDto>> {
        return try {
            val pageData = tvanApiService.getPaidList(yearMonth, roadCode, customerCode, pn = page)
            Result.success(pageData)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "getPaidList failed: ym=$yearMonth rc=$roadCode cc=$customerCode" }
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
            Logger.e(TAG, e) { "publishToTvan failed: ids=$ids" }
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
            Logger.e(TAG, e) { "payCash failed: id=$id" }
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
            Logger.e(TAG, e) { "getReceipt failed: id=$id" }
            Result.failure(e)
        }
    }
}
