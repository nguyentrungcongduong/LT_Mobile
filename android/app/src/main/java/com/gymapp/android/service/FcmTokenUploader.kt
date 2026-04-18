package com.gymapp.android.service

import com.gymapp.android.data.remote.api.UserApi
import com.gymapp.android.data.remote.api.UpdateFcmTokenRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper upload FCM token lên backend.
 * Dùng trong GymFcmService.onNewToken() và khi user login thành công.
 */
@Singleton
class FcmTokenUploader @Inject constructor(
    private val userApi: UserApi
) {
    suspend fun upload(token: String) = withContext(Dispatchers.IO) {
        try {
            userApi.updateFcmToken(UpdateFcmTokenRequest(fcmToken = token))
            android.util.Log.i("FcmTokenUploader", "FCM token uploaded successfully")
        } catch (e: Exception) {
            android.util.Log.e("FcmTokenUploader", "Failed to upload FCM token: ${e.message}")
        }
    }
}
