package com.gymapp.android.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gymapp.android.data.local.TokenStorage
import com.gymapp.android.data.remote.api.ApiResponse
import com.gymapp.android.data.remote.api.AuthApi
import com.gymapp.android.data.remote.api.LoginRequest
import com.gymapp.android.data.remote.api.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage
) {
    private val gson = Gson()

    suspend fun login(request: LoginRequest): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.login(request)
            handleAuthResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(request: RegisterRequest): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.register(request)
            handleAuthResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun <T> handleAuthResponse(response: Response<ApiResponse<T>>): Result<Unit> {
        return if (response.isSuccessful && response.body()?.success == true) {
            val data = response.body()?.data
            if (data is com.gymapp.android.data.remote.api.JwtResponse) {
                tokenStorage.saveTokens(data.accessToken, data.refreshToken)
            }
            Result.success(Unit)
        } else {
            val errorMsg = parseErrorMessage(response)
            Result.failure(Exception(errorMsg))
        }
    }

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

    fun logout() {
        tokenStorage.clear()
    }

    fun hasToken(): Boolean {
        return tokenStorage.getAccessToken() != null
    }
}
