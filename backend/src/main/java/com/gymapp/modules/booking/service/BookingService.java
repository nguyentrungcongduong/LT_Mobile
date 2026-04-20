package com.gymapp.modules.booking.service;

import com.gymapp.modules.booking.dto.*;
import com.gymapp.modules.booking.enums.BookingStatus;
import com.gymapp.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

<<<<<<< HEAD
import java.time.OffsetDateTime;

=======
>>>>>>> 1f03042ff267dc81be6f3567bd59d12c8e670c4a
import java.util.List;
import java.util.UUID;

public interface BookingService {
<<<<<<< HEAD

        BookingResponse createBooking(UUID userId, BookingRequest request, String ipAddress);

        BatchBookingResponse createBatchBookings(UUID userId, BatchBookingRequest request, String ipAddress);

        CancelBookingResponse cancelBooking(UUID userId, UUID bookingId, CancelBookingRequest request);

        PageResponse<BookingSummary> getUserBookings(UUID userId, BookingStatus status, Pageable pageable);

        PageResponse<BookingSummary> getPtBookings(UUID ptId, BookingStatus status, Boolean upcomingOnly,
                        Pageable pageable);

        void expirePendingBookings();

        void autoCompleteBookings();

        PageResponse<PtClientSummary> getPtClients(UUID ptId, BookingStatus status, Pageable pageable);

        ClientProgressDto getClientProgress(UUID ptId, UUID userId);

        List<BookingResponse> getAllBookings();

        PageResponse<BookingSummary> getAllBookingsPaginated(
                        BookingStatus status,
                        String ptName,
                        String search,
                        OffsetDateTime fromDate,
                        OffsetDateTime toDate,
                        Pageable pageable);

=======
    BookingResponse createBooking(UUID userId, BookingRequest request, String ipAddress);

    BatchBookingResponse createBatchBookings(UUID userId, BatchBookingRequest request, String ipAddress);

    CancelBookingResponse cancelBooking(UUID userId, UUID bookingId, CancelBookingRequest request);

    PageResponse<BookingSummary> getUserBookings(UUID userId, BookingStatus status, Pageable pageable);

    PageResponse<BookingSummary> getPtBookings(UUID ptId, BookingStatus status, Boolean upcomingOnly,
            Pageable pageable);

    void expirePendingBookings();

    void autoCompleteBookings();

    PageResponse<PtClientSummary> getPtClients(UUID ptId, BookingStatus status, Pageable pageable);

    ClientProgressDto getClientProgress(UUID ptId, UUID userId);

    List<BookingResponse> getAllBookings();
>>>>>>> 1f03042ff267dc81be6f3567bd59d12c8e670c4a
}
