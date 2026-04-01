package com.gymapp.android.data.remote.api

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

data class UpdateProfileRequest(
    @SerializedName("full_name") val fullName: String?,
    val phone: String?,
    @SerializedName("avatar_url") val avatarUrl: String?
)

data class UploadAvatarResponse(
    @SerializedName("avatar_url") val avatarUrl: String
)

interface UserApi {
    @GET("users/me")
    suspend fun getProfile(): Response<ApiResponse<UserDto>>

    @PUT("users/me")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<ApiResponse<UserDto>>

    @Multipart
    @POST("users/me/avatar")
    suspend fun uploadAvatar(
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<UploadAvatarResponse>>
}
