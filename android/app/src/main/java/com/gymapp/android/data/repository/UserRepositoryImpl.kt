package com.gymapp.android.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gymapp.android.data.remote.api.ApiResponse
import com.gymapp.android.data.remote.api.UpdateProfileRequest
import com.gymapp.android.data.remote.api.UserApi
import com.gymapp.android.data.remote.api.UserDto
import com.gymapp.android.domain.model.User
import com.gymapp.android.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi
) : UserRepository {
    private val gson = Gson()

    override suspend fun getProfile(): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = userApi.getProfile()
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data.toDomain())
                } else {
                    Result.failure(Exception("Không lấy được dữ liệu"))
                }
            } else {
                Result.failure(Exception(parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(fullName: String?, phone: String?, avatarUrl: String?): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = userApi.updateProfile(UpdateProfileRequest(fullName, phone, avatarUrl))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data.toDomain())
                } else {
                    Result.failure(Exception("Không lấy được dữ liệu"))
                }
            } else {
                Result.failure(Exception(parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadAvatar(file: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            
            val response = userApi.uploadAvatar(body)
            if (response.isSuccessful && response.body()?.success == true) {
                val url = response.body()?.data?.avatarUrl
                if (url != null) {
                    Result.success(url)
                } else {
                    Result.failure(Exception("Không lấy được url hình ảnh"))
                }
            } else {
                Result.failure(Exception(parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun UserDto.toDomain() = User(
        id = id,
        email = email,
        fullName = fullName,
        phone = phone,
        role = role,
        avatarUrl = avatarUrl
    )

    private fun <T> parseErrorMessage(response: Response<ApiResponse<T>>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (!errorBody.isNullOrEmpty()) {
                val type = object : TypeToken<ApiResponse<Any>>() {}.type
                val errorResponse: ApiResponse<Any> = gson.fromJson(errorBody, type)
                errorResponse.message ?: "Đã có lỗi xảy ra"
            } else {
                response.body()?.message ?: "Đã có lỗi xảy ra"
            }
        } catch (e: Exception) {
            "Lỗi kết nối server"
        }
    }
}
