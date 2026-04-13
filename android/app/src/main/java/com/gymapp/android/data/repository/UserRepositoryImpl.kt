package com.gymapp.android.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gymapp.android.data.remote.api.ApiResponse
import com.gymapp.android.data.remote.api.ChangePasswordRequest
import com.gymapp.android.data.remote.api.UpdateProfileRequest
import com.gymapp.android.data.remote.api.UserApi
import com.gymapp.android.data.remote.api.UserDto
import com.gymapp.android.data.remote.dto.user.UpdateUserGoalRequest
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
            android.util.Log.d("UserRepository", "Fetching profile...")
            val response = userApi.getProfile()
            android.util.Log.d("UserRepository", "Response code: ${response.code()}")
            android.util.Log.d("UserRepository", "Response body: ${response.body()}")
            android.util.Log.d("UserRepository", "Response error: ${response.errorBody()?.string()}")
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

    override suspend fun updateProfile(fullName: String?, email: String?, phone: String?, avatarUrl: String?): Result<User> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("UserRepository", "Sending update: fullName=$fullName, email=$email, phone=$phone, avatarUrl=$avatarUrl")
            val response = userApi.updateProfile(UpdateProfileRequest(fullName, email, phone, avatarUrl))
            android.util.Log.d("UserRepository", "Update response code: ${response.code()}")
            android.util.Log.d("UserRepository", "Update response body: ${response.body()}")
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
            android.util.Log.e("UserRepository", "Update exception: ${e.message}", e)
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

    override suspend fun changePassword(oldPass: String, newPass: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = userApi.changePassword(ChangePasswordRequest(oldPass, newPass))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
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
        avatarUrl = avatarUrl,
        experienceLevel = experienceLevel,
        fitnessGoal = fitnessGoal
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
    override suspend fun updateGoal(request: UpdateUserGoalRequest): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("UserRepository", "Updating goal: $request")

            val response = userApi.updateGoal(request)

            android.util.Log.d("UserRepository", "UpdateGoal response code: ${response.code()}")
            android.util.Log.d("UserRepository", "UpdateGoal response body: ${response.body()}")
            android.util.Log.d("UserRepository", "UpdateGoal error: ${response.errorBody()?.string()}")

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "UpdateGoal exception: ${e.message}", e)
            Result.failure(e)
        }
    }

}
