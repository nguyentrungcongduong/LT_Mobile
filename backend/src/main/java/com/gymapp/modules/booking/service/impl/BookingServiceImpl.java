package com.gymapp.modules.booking.service.impl;

import com.gymapp.common.exception.BadRequestException;
import com.gymapp.common.exception.ConflictException;
import com.gymapp.common.exception.ForbiddenException;
import com.gymapp.common.exception.ResourceNotFoundException;
import com.gymapp.common.response.PageResponse;
import com.gymapp.modules.booking.dto.*;
import com.gymapp.modules.booking.entity.Booking;
import com.gymapp.modules.booking.entity.PtAvailability;
import com.gymapp.modules.booking.enums.BookingStatus;
import com.gymapp.modules.booking.enums.CancelByType;
import com.gymapp.modules.booking.event.BookingCancelledEvent;
import com.gymapp.modules.booking.event.BookingCompletedEvent;
import com.gymapp.modules.booking.repository.BookingRepository;
import com.gymapp.modules.booking.repository.PtAvailabilityRepository;
import com.gymapp.modules.booking.service.BookingService;
import com.gymapp.modules.membership.repository.MembershipRepository;
import com.gymapp.modules.payment.dto.request.PaymentInitiateRequest;
import com.gymapp.modules.payment.dto.response.PaymentInitiateResponse;
import com.gymapp.modules.payment.entity.Payment;
import com.gymapp.modules.payment.entity.Refund;
import com.gymapp.modules.payment.enums.PaymentStatus;
import com.gymapp.modules.payment.enums.RefundStatus;
import com.gymapp.modules.payment.repository.PaymentRepository;
import com.gymapp.modules.payment.repository.RefundRepository;
import com.gymapp.modules.payment.service.PaymentService;
import com.gymapp.modules.user.entity.PtProfile;
import com.gymapp.modules.user.entity.User;
import com.gymapp.modules.user.repository.PtProfileRepository;
import com.gymapp.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PtAvailabilityRepository availabilityRepository;
    private final PtProfileRepository ptProfileRepository;
    private final MembershipRepository membershipRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentService paymentService;
    private final BookingMapper bookingMapper;

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.20");

    @Override
    @Transactional
    public BookingResponse createBooking(UUID userId, BookingRequest request, String ipAddress) {
        // 1. Validate user active membership
        boolean hasActiveMembership = membershipRepository.findActiveMembershipsByUserId(userId)
                .stream()
                .anyMatch(m -> !m.getEndDate().isBefore(OffsetDateTime.now().toLocalDate()));

        if (!hasActiveMembership) {
            throw new BadRequestException("NO_ACTIVE_MEMBERSHIP", "User has no active membership");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found"));

        // 2. Lock availability slot
        PtAvailability availability = availabilityRepository.findByIdWithLock(request.getAvailabilityId())
                .orElseThrow(() -> new ResourceNotFoundException("SLOT_NOT_FOUND", "Availability slot not found"));

        if (availability.isBooked()) {
            throw new ConflictException("SLOT_ALREADY_BOOKED", "This slot is already booked");
        }

        if (availability.getPtId().equals(userId)) {
            throw new BadRequestException("CANNOT_BOOK_OWN_SLOT", "You cannot book your own slot");
        }

        // 3. Get PT Profile and Calculate Amounts
        PtProfile ptProfile = ptProfileRepository.findByUserId(request.getPtId())
                .orElseThrow(() -> new ResourceNotFoundException("PT_NOT_FOUND", "PT Profile not found"));

        BigDecimal totalAmount = ptProfile.getPricePerSession();
        BigDecimal platformFee = totalAmount.multiply(COMMISSION_RATE);
        BigDecimal ptAmount = totalAmount.subtract(platformFee);

        OffsetDateTime scheduledAt = availability.getAvailableDate()
                .atTime(availability.getStartTime())
                .atOffset(ZoneOffset.UTC);

        OffsetDateTime endAt = availability.getAvailableDate()
                .atTime(availability.getEndTime())
                .atOffset(ZoneOffset.UTC);

        // 4. Create Booking
        Booking booking = Booking.builder()
                .user(user)
                .pt(ptProfile.getUser())
                .availability(availability)
                .scheduledAt(scheduledAt)
                .endAt(endAt)
                .durationMinutes((int) Duration.between(scheduledAt, endAt).toMinutes())
                .totalAmount(totalAmount)
                .platformFee(platformFee)
                .ptAmount(ptAmount)
                .status(BookingStatus.PENDING)
                .expiresAt(OffsetDateTime.now().plusMinutes(15))
                .build();

        booking = bookingRepository.save(booking);

        // Mark availability as booked to prevent double booking during pending payment
        availability.setBooked(true);
        availabilityRepository.save(availability);

        // 5. Initiate Payment to get URL (Payment record is created inside)
        String idempotencyKey = UUID.randomUUID().toString();
        PaymentInitiateRequest initiateRequest = new PaymentInitiateRequest();
        initiateRequest.setBookingId(booking.getId());
        initiateRequest.setProvider(request.getPaymentProvider());
        initiateRequest.setIdempotencyKey(idempotencyKey);

        PaymentInitiateResponse initiateResponse = paymentService.initiatePayment(userId, initiateRequest, ipAddress);

        return BookingResponse.builder()
                .bookingId(booking.getId())
                .ptName(ptProfile.getUser().getFullName())
                .scheduledAt(scheduledAt)
                .endAt(endAt)
                .totalAmount(totalAmount)
                .status(booking.getStatus())
                .paymentUrl(initiateResponse.getGatewayUrl())
                .expiresAt(booking.getExpiresAt())
                .build();
    }

    @Override
    @Transactional
    public CancelBookingResponse cancelBooking(UUID userId, UUID bookingId, CancelBookingRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("BOOKING_NOT_FOUND", "Booking not found"));

        if (!booking.getUser().getId().equals(userId) && !booking.getPt().getId().equals(userId)) {
            throw new ForbiddenException("NOT_BOOKING_PARTICIPANT", "You are not a participant in this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("BOOKING_ALREADY_CANCELLED", "Booking is already cancelled");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("BOOKING_ALREADY_COMPLETED", "Booking is already completed");
        }

        CancelByType cancelBy = booking.getUser().getId().equals(userId) ? CancelByType.USER : CancelByType.PT;
        BigDecimal refundPct = calculateRefundPercentage(cancelBy, booking.getScheduledAt());
        BigDecimal refundAmount = booking.getTotalAmount().multiply(refundPct).divide(new BigDecimal("100"));

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelBy(cancelBy);
        booking.setCancelReason(request.getReason());
        booking.setCancelledAt(OffsetDateTime.now());

        // Re-open availability
        PtAvailability availability = booking.getAvailability();
        availability.setBooked(false);
        availabilityRepository.save(availability);

        bookingRepository.save(booking);

        // Generate Refund if payment was successful
        Payment payment = paymentRepository.findByBookingId(booking.getId())
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .orElse(null);

        if (payment != null && refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            Refund refund = Refund.builder()
                    .payment(payment)
                    .bookingId(booking.getId())
                    .amount(refundAmount)
                    .refundPct(refundPct)
                    .reason(request.getReason())
                    .status(RefundStatus.PENDING)
                    .build();
            refundRepository.save(refund);
            log.info("Refund record created for booking {} with amount {}", booking.getId(), refundAmount);

        }

        eventPublisher.publishEvent(new BookingCancelledEvent(this, booking));

        return CancelBookingResponse.builder()
                .bookingId(booking.getId())
                .status(BookingStatus.CANCELLED)
                .refundAmount(refundAmount)
                .refundPct(refundPct)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookingSummary> getUserBookings(UUID userId, BookingStatus status, Pageable pageable) {
        Page<Booking> page = bookingRepository.findAllByUserIdAndStatus(userId, status, pageable);
        return mapToPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookingSummary> getPtBookings(UUID ptId, BookingStatus status, Pageable pageable) {
        Page<Booking> page = bookingRepository.findAllByPtIdAndStatus(ptId, status, pageable);
        return mapToPageResponse(page);
    }

    @Override
    @Transactional
    public void expirePendingBookings() {
        List<Booking> expiredBookings = bookingRepository.findAllByStatusAndExpiresAtBefore(
                BookingStatus.PENDING, OffsetDateTime.now());

        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancelBy(CancelByType.SYSTEM);
            booking.setCancelReason("PAYMENT_EXPIRED");
            booking.setCancelledAt(OffsetDateTime.now());

            // Release availability slot
            PtAvailability availability = booking.getAvailability();
            availability.setBooked(false);
            availabilityRepository.save(availability);
        }
        bookingRepository.saveAll(expiredBookings);
    }

    @Override
    @Transactional
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void autoCompleteBookings() {
        List<Booking> pastBookings = bookingRepository.findAllByStatusAndEndAtBefore(
                BookingStatus.CONFIRMED, OffsetDateTime.now());

        if (pastBookings.isEmpty()) {
            return;
        }

        log.info("Found {} confirmed bookings that have passed their end time. Auto-completing...",
                pastBookings.size());

        for (Booking booking : pastBookings) {
            booking.setStatus(BookingStatus.COMPLETED);
            // Optionally, we could add a completedAt timestamp property to Booking entity,
            // but status is enough here
            eventPublisher.publishEvent(new BookingCompletedEvent(this, booking));
        }

        bookingRepository.saveAll(pastBookings);
    }

    private BigDecimal calculateRefundPercentage(CancelByType cancelBy, OffsetDateTime scheduledAt) {
        if (cancelBy == CancelByType.PT || cancelBy == CancelByType.SYSTEM) {
            return new BigDecimal("100");
        }

        long hoursToBooking = Duration.between(OffsetDateTime.now(), scheduledAt).toHours();
        if (hoursToBooking >= 24) {
            return new BigDecimal("100");
        } else if (hoursToBooking >= 2) {
            return new BigDecimal("50");
        } else {
            return BigDecimal.ZERO;
        }
    }

    private PageResponse<BookingSummary> mapToPageResponse(Page<Booking> page) {
        List<BookingSummary> content = page.getContent().stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());

        return PageResponse.<BookingSummary>builder()
                .items(content)
                .pagination(PageResponse.PaginationMeta.builder()
                        .page(page.getNumber())
                        .limit(page.getSize())
                        .total(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PtClientSummary> getPtClients(UUID ptId, Pageable pageable) {
        Page<PtClientSummary> page = bookingRepository.findClientSummariesByPtId(ptId, pageable);

        return PageResponse.<PtClientSummary>builder()
                .items(page.getContent())
                .pagination(PageResponse.PaginationMeta.builder()
                        .page(page.getNumber())
                        .limit(page.getSize())
                        .total(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ClientProgressDto getClientProgress(UUID ptId, UUID userId) {
        List<Booking> bookings = bookingRepository.findAllByUserIdAndPtIdOrderByScheduledAtDesc(userId, ptId);

        List<ClientProgressDto.SessionHistoryDto> sessions = bookings.stream()
                .map(b -> ClientProgressDto.SessionHistoryDto.builder()
                        .bookingId(b.getId())
                        .date(b.getScheduledAt())
                        .status(b.getStatus().name())
                        .workoutLogs(java.util.Collections.emptyList()) // Placeholder for now
                        .build())
                .collect(Collectors.toList());

        return ClientProgressDto.builder()
                .sessions(sessions)
                .build();
    }

    private BookingSummary mapToSummary(Booking booking) {
        return BookingSummary.builder()
                .id(booking.getId())
                .ptName(booking.getPt().getFullName())
                .userName(booking.getUser().getFullName())
                .scheduledAt(booking.getScheduledAt())
                .endAt(booking.getEndAt())
                .totalAmount(booking.getTotalAmount())
                .ptAmount(booking.getPtAmount())
                .status(booking.getStatus())
                .build();
    }

    public List<BookingResponse> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAllWithRelations();

        return bookings.stream()
                .map(b -> bookingMapper.toResponse(b, null))
                .toList();
    }
}
