package com.gymapp.android.data.remote.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Query

data class PaymentHistoryDto(
    val paymentId: String,
    val transactionName: String,
    val provider: String,
    val paymentType: String,
    val status: String,
    val amount: Double,
    val createdAt: String?
)

// Spring Page serializes with "content" key, not "items"
data class SpringPageResponse<T>(
    val content: List<T>,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val number: Int = 0,
    val size: Int = 0
)

data class PaymentInitiateRequestDto(
    val membershipId: String? = null,
    val bookingId: String? = null,
    val provider: String,
    val returnUrl: String? = null
)

data class PaymentInitiateResponseDto(
    val paymentId: String,
    val gatewayUrl: String,
    val amount: Double
)

interface PaymentApi {
    @GET("api/v1/payments/history")
    suspend fun getPaymentHistory(
        @Query("paymentType") paymentType: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<ApiResponse<SpringPageResponse<PaymentHistoryDto>>>

    @POST("api/v1/payments/initiate")
    suspend fun initiatePayment(
        @Body request: PaymentInitiateRequestDto
    ): Response<ApiResponse<PaymentInitiateResponseDto>>
}
