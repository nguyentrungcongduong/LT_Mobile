package com.gymapp.android.data.remote

import com.gymapp.android.data.local.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Provider

class AuthInterceptor @Inject constructor(
    private val tokenStorageProvider: Provider<TokenStorage>
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val tokenStorage = tokenStorageProvider.get()
        val token = tokenStorage.getAccessToken()

        var request = chain.request()

        /**
         * Không truyền header `Authorization` cho các endpoint đăng ký, đăng nhập.
         */
        if (request.url.encodedPath.contains("/api/v1/auth/login") ||
            request.url.encodedPath.contains("/api/v1/auth/register")) {
            return chain.proceed(request)
        }

        if (token != null) {
            request = request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }

        return chain.proceed(request)
    }
}
