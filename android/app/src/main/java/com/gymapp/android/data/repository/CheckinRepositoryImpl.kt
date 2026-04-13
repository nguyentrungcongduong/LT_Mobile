package com.gymapp.android.data.repository

import com.gymapp.android.data.remote.api.CheckinApi
import com.gymapp.android.data.remote.dto.checkin.CheckinLogResponse
import com.gymapp.android.data.remote.dto.checkin.CheckinVerifyRequest
import com.gymapp.android.data.remote.dto.checkin.QrTokenResponse
import com.gymapp.android.domain.repository.CheckinRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckinRepositoryImpl @Inject constructor(
    private val checkinApi: CheckinApi
) : CheckinRepository {

    override suspend fun getQrToken(): Result<QrTokenResponse> {
        return try {
            val response = checkinApi.getQrToken()
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                // If there's an error body, use its message, else use HTTP status
                val errorMsg = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyQrToken(qrToken: String, branchId: String?): Result<CheckinLogResponse> {
        return try {
            val response = checkinApi.verifyQrToken(
                CheckinVerifyRequest(qrToken = qrToken, branchId = branchId)
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorMsg = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
