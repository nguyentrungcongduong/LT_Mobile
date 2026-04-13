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
    val fullName: String?,
    val email: String?,
    val phone: String?,
    val avatarUrl: String?
)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

data class UploadAvatarResponse(
    @SerializedName("avatar_url") val avatarUrl: String
)

interface UserApi {
    @GET("api/v1/users/me")
    suspend fun getProfile(): Response<ApiResponse<UserDto>>

    @PUT("api/v1/users/me")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<ApiResponse<UserDto>>

    @Multipart
    @POST("api/v1/users/me/avatar")
    suspend fun uploadAvatar(
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<UploadAvatarResponse>>

    @PUT("api/v1/users/me/password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<ApiResponse<Void>>
}
