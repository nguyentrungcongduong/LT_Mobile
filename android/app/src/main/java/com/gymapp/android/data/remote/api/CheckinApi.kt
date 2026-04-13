package com.gymapp.android.data.remote.api

import com.gymapp.android.data.remote.dto.checkin.CheckinLogResponse
import com.gymapp.android.data.remote.dto.checkin.CheckinVerifyRequest
import com.gymapp.android.data.remote.dto.checkin.QrTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CheckinApi {

    @GET("/api/v1/checkin/qr")
    suspend fun getQrToken(): Response<QrTokenResponse>

    @POST("/api/v1/checkin/verify")
    suspend fun verifyQrToken(@Body request: CheckinVerifyRequest): Response<CheckinLogResponse>
}
