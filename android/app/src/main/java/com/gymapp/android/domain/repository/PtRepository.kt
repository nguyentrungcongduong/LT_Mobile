package com.gymapp.android.domain.repository

import com.gymapp.android.data.remote.api.BookingCreateRequest
import com.gymapp.android.data.remote.api.BookingCreateResponse
import com.gymapp.android.data.remote.api.BookingDto
import com.gymapp.android.data.remote.api.BookingsResponse
import com.gymapp.android.data.remote.api.CancelBookingRequest
import com.gymapp.android.data.remote.api.CancelBookingResponse
import com.gymapp.android.data.remote.api.ClientDto
import com.gymapp.android.data.remote.api.ClientProgressDto
import com.gymapp.android.data.remote.api.PageResponse
import com.gymapp.android.data.remote.api.PtAvailabilityDto
import com.gymapp.android.data.remote.api.PtPublicDto

interface PtRepository {
    suspend fun getPtList(page: Int, size: Int): Result<PageResponse<PtPublicDto>>
    suspend fun getPtDetail(ptId: String): Result<PtPublicDto>
    suspend fun getAvailability(ptId: String, from: String, to: String): Result<List<PtAvailabilityDto>>

    suspend fun createBooking(request: BookingCreateRequest): Result<BookingCreateResponse>
    suspend fun cancelBooking(bookingId: String, request: CancelBookingRequest): Result<CancelBookingResponse>
    suspend fun getUserBookings(status: String?, page: Int, size: Int): Result<PageResponse<BookingDto>>
    suspend fun getPtBookings(status: String?, upcomingOnly: Boolean?, page: Int, size: Int): Result<PageResponse<BookingDto>>
    suspend fun getPtClients(status: String?, page: Int, size: Int): Result<PageResponse<ClientDto>>
    suspend fun getClientProgress(userId: String): Result<ClientProgressDto>
    suspend fun approvePt(ptId: String): Result<Map<String, Any>>
    suspend fun suspendPt(ptId: String, reason: String): Result<Map<String, Any>>
}
