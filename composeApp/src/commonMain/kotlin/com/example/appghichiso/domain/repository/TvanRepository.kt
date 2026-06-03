package com.example.appghichiso.domain.repository

import com.example.appghichiso.data.api.dto.InvoiceDto
import com.example.appghichiso.data.api.dto.PayCashResponse
import com.example.appghichiso.data.api.dto.ReceiptDto
import com.example.appghichiso.data.api.dto.TvanPublishResponse

interface TvanRepository {
    suspend fun getToPublishList(yearMonth: String, roadCode: String, customerCode: String): Result<List<InvoiceDto>>
    suspend fun getDebtList(yearMonth: String, roadCode: String, customerCode: String): Result<List<InvoiceDto>>
    suspend fun getPaidList(yearMonth: String, roadCode: String, customerCode: String): Result<List<InvoiceDto>>
    suspend fun publishToTvan(ids: List<Long>): Result<TvanPublishResponse>
    suspend fun payCash(id: Long): Result<PayCashResponse>
    suspend fun getReceipt(id: Long): Result<ReceiptDto>
}
