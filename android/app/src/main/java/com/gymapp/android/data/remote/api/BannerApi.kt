package com.gymapp.android.data.remote.api

import com.gymapp.android.data.remote.response.BannerResponse
import retrofit2.http.GET

interface BannerApi {
    @GET("api/v1/banners/active")
    suspend fun getActiveBanners(): ApiResponse<List<BannerResponse>>
}