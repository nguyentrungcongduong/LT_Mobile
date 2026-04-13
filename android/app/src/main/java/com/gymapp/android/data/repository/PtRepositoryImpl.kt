package com.gymapp.android.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gymapp.android.data.remote.api.ApiResponse
import com.gymapp.android.data.remote.api.BookingCreateRequest
import com.gymapp.android.data.remote.api.BookingCreateResponse
import com.gymapp.android.data.remote.api.BookingDto
import com.gymapp.android.data.remote.api.BookingsResponse
import com.gymapp.android.data.remote.api.CancelBookingRequest
import com.gymapp.android.data.remote.api.CancelBookingResponse
import com.gymapp.android.data.remote.api.ClientDto
import com.gymapp.android.data.remote.api.ClientProgressDto
import com.gymapp.android.data.remote.api.PageResponse
import com.gymapp.android.data.remote.api.PtApi
import com.gymapp.android.data.remote.api.PtAvailabilityDto
import com.gymapp.android.data.remote.api.PtPublicDto
import com.gymapp.android.domain.repository.PtRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class PtRepositoryImpl @Inject constructor(
    private val ptApi: PtApi
) : PtRepository {
    private val gson = Gson()

    override suspend fun getPtList(page: Int, size: Int): Result<PageResponse<PtPublicDto>> = withContext(Dispatchers.IO) {
        handleApiCall { ptApi.getPts(page, size) }
    }
    
    override suspend fun getPtDetail(ptId: String): Result<PtPublicDto> = withContext(Dispatchers.IO) {
        handleApiCall { ptApi.getPtDetail(ptId) }
    }

    override suspend fun getAvailability(ptId: String, from: String, to: String): Result<List<PtAvailabilityDto>> = withContext(Dispatchers.IO) {
        handleApiCall { ptApi.getAvailability(ptId, from, to) }
    }

    override suspend fun createBooking(request: BookingCreateRequest): Result<BookingCreateResponse> = withContext(Dispatchers.IO) {
        handleApiCall { ptApi.createBooking(request) }
    }

    override suspend fun cancelBooking(bookingId: String, request: CancelBookingRequest): Result<CancelBookingResponse> = withContext(Dispatchers.IO) {
        handleApiCall { ptApi.cancelBooking(bookingId, request) }
    }

    override suspend fun getUserBookings(status: String?, page: Int, size: Int): Result<PageResponse<BookingDto>> = withContext(Dispatchers.IO) {
        handleApiCall { ptApi.getUserBookings(status, page, size) }
    }

    override suspend fun getPtBookings(status: String?, upcomingOnly: Boolean?, page: Int, size: Int): Result<PageResponse<BookingDto>> = withContext(Dispatchers.IO) {
        handleApiCall { ptApi.getPtBookings(status, upcomingOnly, page, size) }
    }

    override suspend fun getPtClients(status: String?, page: Int, size: Int): Result<PageResponse<ClientDto>> = withContext(Dispatchers.IO) {
        handleApiCall { ptApi.getPtClients(status, page, size) }
    }

    override suspend fun getClientProgress(userId: String): Result<ClientProgressDto> = withContext(Dispatchers.IO) {
        handleApiCall { ptApi.getClientProgress(userId) }
    }

    override suspend fun approvePt(ptId: String): Result<Map<String, Any>> = withContext(Dispatchers.IO) {
        handleApiCall { ptApi.approvePt(ptId) }
    }

    override suspend fun suspendPt(ptId: String, reason: String): Result<Map<String, Any>> = withContext(Dispatchers.IO) {
        handleApiCall { ptApi.suspendPt(ptId, mapOf("reason" to reason)) }
    }

    private suspend fun <T> handleApiCall(apiCall: suspend () -> Response<ApiResponse<T>>): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Dữ liệu trống"))
            } else {
                val errorMsg = parseErrorMessage(response)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun <T> parseErrorMessage(response: Response<ApiResponse<T>>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (!errorBody.isNullOrEmpty()) {
                val type = object : TypeToken<ApiResponse<Any>>() {}.type
                val errorResponse: ApiResponse<Any> = gson.fromJson(errorBody, type)
                errorResponse.message ?: errorResponse.error ?: "Đã có lỗi xảy ra"
            } else {
                response.body()?.message ?: "Đã có lỗi xảy ra"
            }
        } catch (e: Exception) {
            "Lỗi kết nối server"
        }
    }
}
