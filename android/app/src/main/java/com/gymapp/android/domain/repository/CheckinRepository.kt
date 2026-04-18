package com.gymapp.android.domain.repository

import com.gymapp.android.data.remote.dto.checkin.CheckinLogResponse
import com.gymapp.android.data.remote.dto.checkin.QrTokenResponse

interface CheckinRepository {
    suspend fun getQrToken(): Result<QrTokenResponse>
    suspend fun verifyQrToken(qrToken: String, branchId: String? = null): Result<CheckinLogResponse>
}
