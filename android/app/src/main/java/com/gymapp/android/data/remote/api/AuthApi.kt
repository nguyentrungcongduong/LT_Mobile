package com.gymapp.android.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("/api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<JwtResponse>>

    @POST("/api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<JwtResponse>>

    @POST("/api/v1/auth/refresh")
    suspend fun refreshToken(@Body request: TokenRefreshRequest): Response<ApiResponse<TokenRefreshResponse>>

    @POST("/api/v1/auth/logout")
    suspend fun logout(@Body request: TokenRefreshRequest): Response<ApiResponse<Void>>
}
