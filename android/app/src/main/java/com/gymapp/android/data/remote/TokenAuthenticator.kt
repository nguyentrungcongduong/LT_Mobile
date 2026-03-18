package com.gymapp.android.data.remote

import com.gymapp.android.data.local.TokenStorage
import com.gymapp.android.data.remote.api.AuthApi
import com.gymapp.android.data.remote.api.TokenRefreshRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

class TokenAuthenticator @Inject constructor(
    private val tokenStorageProvider: Provider<TokenStorage>,
    private val authApiProvider: Provider<AuthApi> // Lazy init để tránh circular dependency
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val tokenStorage = tokenStorageProvider.get()
        val refreshToken = tokenStorage.getRefreshToken() ?: return null

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
                }
            } catch (e: Exception) {
                tokenStorage.clear()
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
