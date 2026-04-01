package com.gymapp.android.data.remote

import com.gymapp.android.data.local.TokenStorage
import com.gymapp.android.data.remote.api.AuthApi
import com.gymapp.android.data.remote.api.TokenRefreshRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenStorageProvider: Provider<TokenStorage>,
    private val authApiProvider: Provider<AuthApi>,
    private val authEventBus: AuthEventBus
) : Authenticator {

    private val isRefreshing = AtomicBoolean(false)
    private val isTokenExpired = AtomicBoolean(false)

    override fun authenticate(route: Route?, response: Response): Request? {
        // Nếu đã biết token hết hạn và đã emit event, không thử refresh nữa
        if (isTokenExpired.get()) {
            return null
        }

        // Chỉ cho phép 1 thread refresh tại 1 thời điểm
        if (!isRefreshing.compareAndSet(false, true)) {
            // Nếu đang có thread khác refresh, chờ 1 chút rồi thử lại
            Thread.sleep(100)
            return if (isTokenExpired.get()) null else response.request
        }

        val tokenStorage = tokenStorageProvider.get()
        val refreshToken = tokenStorage.getRefreshToken()
        
        if (refreshToken == null) {
            isRefreshing.set(false)
            return null
        }

        // Chặn luồng okhttp và dùng coroutine
        var newAccessToken: String? = null
        runBlocking {
            try {
                // Gọi API refresh
                val authApi = authApiProvider.get()
                val apiResponse = authApi.refreshToken(TokenRefreshRequest(refreshToken))
                
                if (apiResponse.isSuccessful) {
                    apiResponse.body()?.data?.let { data ->
                        tokenStorage.saveTokens(data.accessToken, data.refreshToken)
                        newAccessToken = data.accessToken
                    }
                } else {
                    // Nếu refresh thất bại, xóa tokens và đẩy về login
                    tokenStorage.clear()
                    isTokenExpired.set(true)
                    authEventBus.emit(AuthEvent.TokenExpired)
                }
            } catch (e: Exception) {
                tokenStorage.clear()
                isTokenExpired.set(true)
                authEventBus.emit(AuthEvent.TokenExpired)
            } finally {
                isRefreshing.set(false)
            }
        }

        return if (newAccessToken != null) {
            response.request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        } else {
            null
        }
    }
}
