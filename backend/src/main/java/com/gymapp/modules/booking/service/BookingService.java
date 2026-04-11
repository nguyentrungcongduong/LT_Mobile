package com.gymapp.modules.booking.service;

import com.gymapp.modules.booking.dto.*;
import com.gymapp.modules.booking.enums.BookingStatus;
import com.gymapp.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface BookingService {
    BookingResponse createBooking(UUID userId, BookingRequest request, String ipAddress);

    CancelBookingResponse cancelBooking(UUID userId, UUID bookingId, CancelBookingRequest request);

    PageResponse<BookingSummary> getUserBookings(UUID userId, BookingStatus status, Pageable pageable);

    PageResponse<BookingSummary> getPtBookings(UUID ptId, BookingStatus status, Boolean upcomingOnly,
            Pageable pageable);

    void expirePendingBookings();

    void autoCompleteBookings();

    PageResponse<PtClientSummary> getPtClients(UUID ptId, BookingStatus status, Pageable pageable);

    ClientProgressDto getClientProgress(UUID ptId, UUID userId);

    List<BookingResponse> getAllBookings();
}
