package com.gymapp.android.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PtApi {
    @GET("api/v1/pts")
    suspend fun getPts(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<PageResponse<PtPublicDto>>>

    @GET("api/v1/pts/{pt_id}")
    suspend fun getPtDetail(
        @Path("pt_id") ptId: String
    ): Response<ApiResponse<PtPublicDto>>


    @GET("api/v1/pts/{pt_id}/availability")
    suspend fun getAvailability(
        @Path("pt_id") ptId: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): Response<ApiResponse<List<PtAvailabilityDto>>>

    @POST("api/v1/bookings")
    suspend fun createBooking(
        @Body request: BookingCreateRequest
    ): Response<ApiResponse<BookingCreateResponse>>

    @PATCH("api/v1/bookings/{booking_id}/cancel")
    suspend fun cancelBooking(
        @Path("booking_id") bookingId: String,
        @Body request: CancelBookingRequest
    ): Response<ApiResponse<CancelBookingResponse>>

    @GET("api/v1/bookings")
    suspend fun getUserBookings(
        @Query("status") status: String?,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<PageResponse<BookingDto>>>

    @GET("api/v1/pt/bookings")
    suspend fun getPtBookings(
        @Query("status") status: String?,
        @Query("upcoming_only") upcomingOnly: Boolean?,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<PageResponse<BookingDto>>>

    @GET("api/v1/pt/clients")
    suspend fun getPtClients(
        @Query("status") status: String?,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<PageResponse<ClientDto>>>

    @GET("api/v1/pt/clients/{user_id}/progress")
    suspend fun getClientProgress(
        @Path("user_id") userId: String
    ): Response<ApiResponse<ClientProgressDto>>

    @PATCH("api/v1/admin/pts/{pt_id}/approve")
    suspend fun approvePt(
        @Path("pt_id") ptId: String
    ): Response<ApiResponse<Map<String, Any>>>

    @PATCH("api/v1/admin/pts/{pt_id}/suspend")
    suspend fun suspendPt(
        @Path("pt_id") ptId: String,
        @Body request: Any // SuspendReq if needed, or empty map
    ): Response<ApiResponse<Map<String, Any>>>

    @PUT("api/v1/pt/profile")
    suspend fun updatePtProfile(
        @Body request: PtProfileUpdateRequest
    ): Response<ApiResponse<Map<String, Any>>>

    @POST("api/v1/pt/availability")
    suspend fun createAvailability(
        @Body request: CreateAvailabilityRequest
    ): Response<ApiResponse<Map<String, Any>>>
}

data class PtProfileUpdateRequest(
    val pricePerSession: Long? = null,
    val bio: String? = null,
    val yearsExperience: Int? = null
)

data class CreateAvailabilityRequest(
    @com.google.gson.annotations.SerializedName("available_date") val availableDate: String,
    @com.google.gson.annotations.SerializedName("start_time") val startTime: String,
    @com.google.gson.annotations.SerializedName("end_time") val endTime: String
)
