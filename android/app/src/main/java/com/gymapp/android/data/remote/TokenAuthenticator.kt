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

    /**
     * Reset state khi user logout/login lại — gọi từ AuthRepository
     */
    fun reset() {
        isTokenExpired.set(false)
        isRefreshing.set(false)
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        // Không intercept các auth endpoints — login/register/refresh tự xử lý 401
        val path = response.request.url.encodedPath
        if (path.contains("/api/v1/auth/login") ||
            path.contains("/api/v1/auth/register") ||
            path.contains("/api/v1/auth/refresh")) {
            return null
        }

        // Nếu đã biết token hết hạn và đã emit event, không thử refresh nữa
        if (isTokenExpired.get()) {
            return null
        }

        // Chỉ cho phép 1 thread refresh tại 1 thời điểm
        if (!isRefreshing.compareAndSet(false, true)) {
            Thread.sleep(100)
            return if (isTokenExpired.get()) null else response.request
        }

        val tokenStorage = tokenStorageProvider.get()
        val refreshToken = tokenStorage.getRefreshToken()

        if (refreshToken == null) {
            isRefreshing.set(false)
            return null
        }

        var newAccessToken: String? = null
        runBlocking {
            try {
                val authApi = authApiProvider.get()
                val apiResponse = authApi.refreshToken(TokenRefreshRequest(refreshToken))

                if (apiResponse.isSuccessful) {
                    apiResponse.body()?.data?.let { data ->
                        tokenStorage.saveTokens(data.accessToken, data.refreshToken)
                        newAccessToken = data.accessToken
                    }
                } else {
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
