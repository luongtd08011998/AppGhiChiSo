package com.example.appghichiso.domain.usecase

import com.example.appghichiso.di.AppStateHolder
import com.example.appghichiso.domain.repository.MeterReadingRepository

class SubmitMeterReadingUseCase(
    private val meterReadingRepository: MeterReadingRepository,
    private val appStateHolder: AppStateHolder
) {
    suspend operator fun invoke(
        customerCode: String,
        invoiceId: Long,
        newIndex: Int,
        previousIndex: Int
    ): Result<Unit> {
        if (newIndex < previousIndex) {
            return Result.failure(
                IllegalArgumentException(
                    "Chỉ số mới ($newIndex) không được nhỏ hơn chỉ số cũ ($previousIndex)"
                )
            )
        }
        return meterReadingRepository.submitReading(
            invoiceId = invoiceId,
            newIndex = newIndex
        ).also { result ->
            if (result.isSuccess) appStateHolder.recordedCustomerCodes.add(customerCode)
        }
    }
}

