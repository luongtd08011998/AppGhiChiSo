package com.example.appghichiso.domain.usecase

import com.example.appghichiso.data.api.dto.InvoiceDto
import com.example.appghichiso.data.api.dto.PayCashResponse
import com.example.appghichiso.data.api.dto.ReceiptDto
import com.example.appghichiso.data.api.dto.TvanPublishResponse
import com.example.appghichiso.domain.repository.TvanRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class GetInvoiceStatusUseCase(private val tvanRepository: TvanRepository) {
    suspend operator fun invoke(yearMonth: String, roadCode: String, customerCode: String): Result<Pair<List<InvoiceDto>, List<InvoiceDto>>> =
        withContext(Dispatchers.IO) {
            try {
                // Fetch both lists concurrently? Or sequentially? Sequential is fine for now.
                val toPublishResult = tvanRepository.getToPublishList(yearMonth, roadCode, customerCode)
                val debtResult = tvanRepository.getDebtList(yearMonth, roadCode, customerCode)

                if (toPublishResult.isSuccess && debtResult.isSuccess) {
                    Result.success(Pair(toPublishResult.getOrThrow(), debtResult.getOrThrow()))
                } else {
                    val error = toPublishResult.exceptionOrNull() ?: debtResult.exceptionOrNull()
                    Result.failure(Exception("Lỗi tải TVAN: ${error?.message}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}

class GetToPublishListUseCase(private val tvanRepository: TvanRepository) {
    suspend operator fun invoke(yearMonth: String, roadCode: String, customerCode: String): Result<List<InvoiceDto>> =
        withContext(Dispatchers.IO) {
            tvanRepository.getToPublishList(yearMonth, roadCode, customerCode)
        }
}

class GetDebtListUseCase(private val tvanRepository: TvanRepository) {
    suspend operator fun invoke(yearMonth: String, roadCode: String, customerCode: String): Result<List<InvoiceDto>> =
        withContext(Dispatchers.IO) {
            tvanRepository.getDebtList(yearMonth, roadCode, customerCode)
        }
}

class GetPaidListUseCase(private val tvanRepository: TvanRepository) {
    suspend operator fun invoke(yearMonth: String, roadCode: String, customerCode: String): Result<List<InvoiceDto>> =
        withContext(Dispatchers.IO) {
            tvanRepository.getPaidList(yearMonth, roadCode, customerCode)
        }
}

class PublishTvanUseCase(private val tvanRepository: TvanRepository) {
    suspend operator fun invoke(ids: List<Long>): Result<TvanPublishResponse> =
        withContext(Dispatchers.IO) {
            tvanRepository.publishToTvan(ids)
        }
}

class PayCashUseCase(private val tvanRepository: TvanRepository) {
    suspend operator fun invoke(id: Long): Result<PayCashResponse> =
        withContext(Dispatchers.IO) {
            tvanRepository.payCash(id)
        }
}

class GetReceiptUseCase(private val tvanRepository: TvanRepository) {
    suspend operator fun invoke(id: Long): Result<ReceiptDto> =
        withContext(Dispatchers.IO) {
            tvanRepository.getReceipt(id)
        }
}
