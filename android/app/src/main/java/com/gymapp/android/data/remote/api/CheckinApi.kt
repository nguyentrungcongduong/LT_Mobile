package com.gymapp.android.data.remote.api

import com.gymapp.android.data.remote.dto.checkin.CheckinLogResponse
import com.gymapp.android.data.remote.dto.checkin.CheckinStatsResponse
import com.gymapp.android.data.remote.dto.checkin.CheckinVerifyRequest
import com.gymapp.android.data.remote.dto.checkin.QrTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CheckinApi {

    @GET("/api/v1/checkin/qr")
    suspend fun getQrToken(): Response<QrTokenResponse>

    @POST("/api/v1/checkin/verify")
    suspend fun verifyQrToken(@Body request: CheckinVerifyRequest): Response<CheckinLogResponse>

    @GET("/api/v1/checkin/stats")
    suspend fun getMyStats(): Response<CheckinStatsResponse>

    @GET("/api/v1/checkin/my-history")
    suspend fun getMyCheckinHistory(): Response<List<CheckinLogResponse>>

    @GET("/api/v1/admin/checkin/logs")
    suspend fun getCheckinLogs(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Response<AdminCheckinLogsResponse>


}

data class AdminCheckinLogsResponse(
    val content: List<CheckinLogResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int
)

