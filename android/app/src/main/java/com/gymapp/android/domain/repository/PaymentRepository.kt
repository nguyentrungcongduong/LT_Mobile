package com.gymapp.android.domain.repository

import com.gymapp.android.data.remote.api.PaymentInitiateRequestDto
import com.gymapp.android.data.remote.api.PaymentInitiateResponseDto

interface PaymentRepository {
    suspend fun initiatePayment(request: PaymentInitiateRequestDto): Result<PaymentInitiateResponseDto>
}
