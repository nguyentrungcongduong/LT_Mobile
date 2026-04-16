package com.gymapp.android.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gymapp.android.data.local.TokenStorage
import com.gymapp.android.data.remote.TokenAuthenticator
import com.gymapp.android.data.remote.api.ApiResponse
import com.gymapp.android.data.remote.api.AuthApi
import com.gymapp.android.data.remote.api.LoginRequest
import com.gymapp.android.data.remote.api.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage,
    private val tokenAuthenticator: TokenAuthenticator
) {
    private val gson = Gson()

    suspend fun login(request: LoginRequest): Result<Unit> = withContext(Dispatchers.IO) {
        // Reset trạng thái expired trước mỗi lần login
        tokenAuthenticator.reset()
        try {
            val response = authApi.login(request)
            handleAuthResponse(response)
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("Kết nối quá chậm hoặc máy chủ không phản hồi. Vui lòng thử lại."))
        } catch (e: UnknownHostException) {
            Result.failure(Exception("Không có kết nối mạng. Vui lòng kiểm tra WiFi hoặc dữ liệu di động."))
        } catch (e: Exception) {
            Result.failure(Exception("Đã xảy ra lỗi: ${e.message ?: "Vui lòng thử lại."}"))
        }
    }

    suspend fun register(request: RegisterRequest): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.register(request)
            handleAuthResponse(response)
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("Kết nối quá chậm hoặc máy chủ không phản hồi. Vui lòng thử lại."))
        } catch (e: UnknownHostException) {
            Result.failure(Exception("Không có kết nối mạng. Vui lòng kiểm tra WiFi hoặc dữ liệu di động."))
        } catch (e: Exception) {
            Result.failure(Exception("Đã xảy ra lỗi: ${e.message ?: "Vui lòng thử lại."}"))
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
                mapErrorToVietnamese(errorResponse.message, response.code())
            } else {
                mapErrorToVietnamese(response.body()?.message, response.code())
            }
        } catch (e: Exception) {
            "Không thể kết nối đến máy chủ. Vui lòng thử lại."
        }
    }

    private fun mapErrorToVietnamese(message: String?, httpCode: Int): String {
        // Nếu backend đã trả về tiếng Việt thì dùng luôn
        if (message != null && containsVietnamese(message)) return message

        return when {
            httpCode == 401 -> "Email hoặc mật khẩu không chính xác."
            httpCode == 403 -> "Bạn không có quyền thực hiện thao tác này."
            httpCode == 404 -> "Không tìm thấy tài nguyên yêu cầu."
            httpCode == 409 -> "Email này đã được đăng ký. Vui lòng dùng email khác."
            httpCode == 422 -> "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại."
            httpCode >= 500 -> "Đã có lỗi xảy ra phía máy chủ. Vui lòng thử lại sau."
            message?.contains("Bad credentials", ignoreCase = true) == true ->
                "Email hoặc mật khẩu không chính xác."
            message?.contains("User is disabled", ignoreCase = true) == true ->
                "Tài khoản của bạn đã bị vô hiệu hóa."
            message?.contains("Account locked", ignoreCase = true) == true ->
                "Tài khoản tạm thời bị khóa. Vui lòng thử lại sau."
            message?.contains("expired", ignoreCase = true) == true ->
                "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
            else -> message ?: "Đã có lỗi xảy ra. Vui lòng thử lại."
        }
    }

    /** Kiểm tra chuỗi có chứa ký tự tiếng Việt không */
    private fun containsVietnamese(text: String): Boolean {
        return text.any { it in "àáâãèéêìíòóôõùúýăđơưạảấầẩẫậắằẳẵặẹẻẽếềểễệỉịọỏốồổỗộớờởỡợụủứừửữựỳỵỷỹ" +
                "ÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚÝĂĐƠƯẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼẾỀỂỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪỬỮỰỲỴỶỸ" }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        tokenAuthenticator.reset()
        try {
            val refreshToken = tokenStorage.getRefreshToken()
            if (refreshToken != null) {
                authApi.logout(com.gymapp.android.data.remote.api.TokenRefreshRequest(refreshToken))
            }
        } catch (e: Exception) {
            // Log error but continue to clear local storage
        } finally {
            tokenStorage.clear()
        }
    }

    fun hasToken(): Boolean {
        return tokenStorage.getAccessToken() != null
    }
}
