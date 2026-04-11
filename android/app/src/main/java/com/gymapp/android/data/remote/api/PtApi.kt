package com.gymapp.android.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
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
}
