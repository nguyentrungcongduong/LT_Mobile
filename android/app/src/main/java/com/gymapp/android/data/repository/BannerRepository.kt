package com.gymapp.android.data.repository

import com.gymapp.android.data.remote.api.BannerApi
import com.gymapp.android.data.remote.response.BannerResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BannerRepository @Inject constructor(
    private val bannerApi: BannerApi
) {
    suspend fun getActiveBanners(): Result<List<BannerResponse>> {
        return try {
            val response = bannerApi.getActiveBanners()
            Result.success(response.data ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}