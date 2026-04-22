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
        private static final List<BookingStatus> VALID_PROGRESS_STATUSES = List.of(BookingStatus.PENDING,
                        BookingStatus.CONFIRMED, BookingStatus.COMPLETED);

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
                                .orElseThrow(() -> new ResourceNotFoundException("SLOT_NOT_FOUND",
                                                "Availability slot not found"));

                if (availability.isBooked()) {
                        throw new ConflictException("SLOT_ALREADY_BOOKED",
                                        "Slot " + availability.getAvailableDate()
                                                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                        + " lúc " + availability.getStartTime()
                                                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                                        + " đã được đặt rồi");
                }

                // 3. Get PT Profile and Calculate Amounts
                PtProfile ptProfile = ptProfileRepository.findById(request.getPtId())
                                .orElseThrow(() -> new ResourceNotFoundException("PT_NOT_FOUND",
                                                "PT Profile not found"));

                if (ptProfile.getUser().getId().equals(userId)) {
                        throw new BadRequestException("CANNOT_BOOK_OWN_SLOT", "You cannot book your own slot");
                }

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
                                .expiresAt(OffsetDateTime.now().plusHours(24))
                                .build();

                booking = bookingRepository.save(booking);

                // Mark availability as booked to prevent double booking during pending payment
                availability.setBooked(true);
                availabilityRepository.save(availability);

                // 5. Initiate Payment to get URL (Payment record is created inside)
                String idempotencyKey = UUID.randomUUID().toString();
                PaymentInitiateRequest initiateRequest = PaymentInitiateRequest.builder()
                                .bookingId(booking.getId())
                                .provider(request.getPaymentProvider())
                                .idempotencyKey(idempotencyKey)
                                .build();

                PaymentInitiateResponse initiateResponse = paymentService.initiatePayment(userId, initiateRequest,
                                ipAddress);

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
        public BatchBookingResponse createBatchBookings(UUID userId, BatchBookingRequest request, String ipAddress) {
                // 1. Validate user active membership
                boolean hasActiveMembership = membershipRepository.findActiveMembershipsByUserId(userId)
                                .stream()
                                .anyMatch(m -> !m.getEndDate().isBefore(OffsetDateTime.now().toLocalDate()));
                if (!hasActiveMembership) {
                        throw new BadRequestException("NO_ACTIVE_MEMBERSHIP", "User has no active membership");
                }

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found"));

                PtProfile ptProfile = ptProfileRepository.findById(request.getPtId())
                                .orElseThrow(() -> new ResourceNotFoundException("PT_NOT_FOUND",
                                                "PT Profile not found"));

                if (ptProfile.getUser().getId().equals(userId)) {
                        throw new BadRequestException("CANNOT_BOOK_OWN_SLOT", "You cannot book your own slot");
                }

                BigDecimal pricePerSession = ptProfile.getPricePerSession();
                BigDecimal commissionRate = COMMISSION_RATE;
                OffsetDateTime expiresAt = OffsetDateTime.now().plusHours(24);

                List<Booking> bookings = new java.util.ArrayList<>();

                // 2. Validate và create booking cho từng slot
                for (UUID availabilityId : request.getAvailabilityIds()) {
                        PtAvailability availability = availabilityRepository.findByIdWithLock(availabilityId)
                                        .orElseThrow(() -> new ResourceNotFoundException("SLOT_NOT_FOUND",
                                                        "Availability slot not found: " + availabilityId));

                        if (availability.isBooked()) {
                                throw new ConflictException("SLOT_ALREADY_BOOKED",
                                                "Slot ngày "
                                                + availability.getAvailableDate()
                                                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                                + " lúc "
                                                + availability.getStartTime()
                                                        .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                                                + " đã có người đặt rồi, vui lòng chọn slot khác");
                        }

                        BigDecimal platformFee = pricePerSession.multiply(commissionRate);
                        BigDecimal ptAmount = pricePerSession.subtract(platformFee);

                        OffsetDateTime scheduledAt = availability.getAvailableDate()
                                        .atTime(availability.getStartTime()).atOffset(java.time.ZoneOffset.UTC);
                        OffsetDateTime endAt = availability.getAvailableDate()
                                        .atTime(availability.getEndTime()).atOffset(java.time.ZoneOffset.UTC);

                        Booking booking = Booking.builder()
                                        .user(user)
                                        .pt(ptProfile.getUser())
                                        .availability(availability)
                                        .scheduledAt(scheduledAt)
                                        .endAt(endAt)
                                        .durationMinutes((int) java.time.Duration.between(scheduledAt, endAt)
                                                        .toMinutes())
                                        .totalAmount(pricePerSession)
                                        .platformFee(platformFee)
                                        .ptAmount(ptAmount)
                                        .status(BookingStatus.PENDING)
                                        .expiresAt(expiresAt)
                                        .build();

                        bookings.add(bookingRepository.save(booking));

                        // Mark slot as booked
                        availability.setBooked(true);
                        availabilityRepository.save(availability);
                }

                // 3. Tính tổng tiền
                BigDecimal totalAmount = pricePerSession.multiply(new BigDecimal(bookings.size()));

                // 4. Tạo 1 payment cho toàn bộ batch với tổng tiền thực
                String idempotencyKey = UUID.randomUUID().toString();
                String batchBookingIdsCsv = bookings.stream()
                                .map(b -> b.getId().toString())
                                .collect(Collectors.joining(","));

                PaymentInitiateRequest initiateRequest = PaymentInitiateRequest.builder()
                                .bookingId(bookings.get(0).getId())
                                .provider(request.getPaymentProvider())
                                .idempotencyKey(idempotencyKey)
                                .overrideAmount(totalAmount) // ← tổng tiền toàn batch
                                .batchBookingIds(batchBookingIdsCsv) // ← lưu để confirm sau
                                .build();

                PaymentInitiateResponse initiateResponse = paymentService.initiatePayment(userId, initiateRequest,
                                ipAddress);

                List<UUID> bookingIds = bookings.stream().map(Booking::getId).collect(Collectors.toList());

                return BatchBookingResponse.builder()
                                .bookingIds(bookingIds)
                                .totalAmount(totalAmount)
                                .paymentUrl(initiateResponse.getGatewayUrl())
                                .expiresAt(expiresAt)
                                .sessionCount(bookings.size())
                                .build();
        }

        @Override
        @Transactional
        public CancelBookingResponse cancelBooking(UUID userId, UUID bookingId, CancelBookingRequest request) {
                Booking booking = bookingRepository.findById(bookingId)
                                .orElseThrow(() -> new ResourceNotFoundException("BOOKING_NOT_FOUND",
                                                "Booking not found"));

                if (!booking.getUser().getId().equals(userId) && !booking.getPt().getId().equals(userId)) {
                        throw new ForbiddenException("NOT_BOOKING_PARTICIPANT",
                                        "You are not a participant in this booking");
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
        @Transactional
        public CancelBookingResponse adminCancelBooking(UUID adminId, UUID bookingId, String reason) {
                Booking booking = bookingRepository.findById(bookingId)
                                .orElseThrow(() -> new ResourceNotFoundException("BOOKING_NOT_FOUND", "Không tìm thấy booking"));

                if (booking.getStatus() == BookingStatus.CANCELLED) {
                        throw new BadRequestException("BOOKING_ALREADY_CANCELLED", "Booking đã bị hủy trước đó");
                }
                if (booking.getStatus() == BookingStatus.COMPLETED) {
                        throw new BadRequestException("BOOKING_ALREADY_COMPLETED", "Booking đã hoàn thành, không thể hủy");
                }

                // Admin luôn hoàn 100%
                BigDecimal refundAmount = booking.getTotalAmount();
                BigDecimal refundPct = new BigDecimal("100");

                booking.setStatus(BookingStatus.CANCELLED);
                booking.setCancelBy(CancelByType.ADMIN);
                booking.setCancelReason(reason != null ? reason : "Admin hủy lịch");
                booking.setCancelledAt(OffsetDateTime.now());

                // Mở lại slot lịch của PT
                PtAvailability availability = booking.getAvailability();
                if (availability != null) {
                        availability.setBooked(false);
                        availabilityRepository.save(availability);
                }
                bookingRepository.save(booking);

                // Tạo refund → PROCESSED ngay (không qua gateway)
                Payment payment = paymentRepository.findByBookingId(booking.getId())
                                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                                .orElse(null);

                if (payment != null && refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                        Refund refund = Refund.builder()
                                        .payment(payment)
                                        .bookingId(booking.getId())
                                        .amount(refundAmount)
                                        .refundPct(refundPct)
                                        .reason(reason != null ? reason : "Admin hủy lịch")
                                        .status(RefundStatus.PROCESSED)        // Auto-done
                                        .processedAt(OffsetDateTime.now())
                                        .gatewayRefundId("ADMIN-CANCEL-" + bookingId)
                                        .build();
                        refundRepository.save(refund);

                        payment.setStatus(PaymentStatus.REFUNDED);
                        paymentRepository.save(payment);
                        log.info("Admin cancelled booking {} — refund auto-processed", bookingId);
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
        public PageResponse<BookingSummary> getPtBookings(UUID ptId, BookingStatus status, Boolean upcomingOnly,
                        Pageable pageable) {
                Page<Booking> page;
                if (Boolean.TRUE.equals(upcomingOnly)) {
                        if (status != null) {
                                page = bookingRepository.findUpcomingByStatus(ptId, status, java.time.LocalDate.now(),
                                                pageable);
                        } else {
                                page = bookingRepository.findUpcomingAll(
                                                ptId,
                                                java.time.LocalDate.now(),
                                                List.of(BookingStatus.CANCELLED),
                                                pageable);
                        }
                } else {
                        page = bookingRepository.findAllByPtIdAndStatus(ptId, status, pageable);
                }
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
        @Scheduled(fixedRate = 300000) // chạy mỗi 5 phút
        public void autoCompleteBookings() {
                OffsetDateTime now = OffsetDateTime.now();

                // ① CONFIRMED đã qua giờ → AWAITING_CONFIRMATION, thông báo PT
                List<Booking> needConfirmation = bookingRepository.findAllByStatusAndEndAtBefore(
                                BookingStatus.CONFIRMED, now);
                if (!needConfirmation.isEmpty()) {
                        log.info("Moving {} CONFIRMED bookings → AWAITING_CONFIRMATION", needConfirmation.size());
                        for (Booking booking : needConfirmation) {
                                booking.setStatus(BookingStatus.AWAITING_CONFIRMATION);
                                // Gửi thông báo cho PT để xác nhận
                                eventPublisher.publishEvent(new com.gymapp.modules.booking.event.BookingAwaitingConfirmationEvent(this, booking));
                        }
                        bookingRepository.saveAll(needConfirmation);
                }

                // ② AWAITING_CONFIRMATION đã chờ > 24h → auto COMPLETED
                OffsetDateTime deadline = now.minusHours(24);
                List<Booking> autoCompletes = bookingRepository.findAllByStatusAndEndAtBefore(
                                BookingStatus.AWAITING_CONFIRMATION, deadline);
                if (!autoCompletes.isEmpty()) {
                        log.info("Auto-completing {} bookings after 24h without PT confirmation", autoCompletes.size());
                        for (Booking booking : autoCompletes) {
                                booking.setStatus(BookingStatus.COMPLETED);
                                booking.setPtConfirmedAt(now);
                                eventPublisher.publishEvent(new BookingCompletedEvent(this, booking));
                        }
                        bookingRepository.saveAll(autoCompletes);
                }
        }

        @Override
        @Transactional
        public void confirmAttendance(UUID ptId, UUID bookingId, boolean attended) {
                Booking booking = bookingRepository.findById(bookingId)
                                .orElseThrow(() -> new ResourceNotFoundException("BOOKING_NOT_FOUND", "Không tìm thấy booking"));

                // Chỉ PT của buổi tập được xác nhận
                if (!booking.getPt().getId().equals(ptId)) {
                        throw new com.gymapp.common.exception.ForbiddenException("FORBIDDEN",
                                        "Bạn không có quyền xác nhận buổi tập này");
                }

                // Chỉ xác nhận khi đang ở trạng thái chờ xác nhận
                if (booking.getStatus() != BookingStatus.AWAITING_CONFIRMATION) {
                        throw new BadRequestException("INVALID_STATUS",
                                        "Buổi tập phải ở trạng thái AWAITING_CONFIRMATION mới có thể xác nhận. Hiện tại: "
                                                        + booking.getStatus());
                }

                OffsetDateTime now = OffsetDateTime.now();
                booking.setPtConfirmedAt(now);

                if (attended) {
                        // Học viên đã tập → COMPLETED → PT nhận tiền
                        log.info("PT {} confirmed attendance for booking {}: ATTENDED", ptId, bookingId);
                        booking.setStatus(BookingStatus.COMPLETED);
                        booking.setCompletedAt(now);
                        bookingRepository.save(booking);
                        eventPublisher.publishEvent(new BookingCompletedEvent(this, booking));
                } else {
                        // Học viên vắng mặt → NO_SHOW → PT KHÔNG nhận tiền
                        log.info("PT {} confirmed attendance for booking {}: NO_SHOW", ptId, bookingId);
                        booking.setStatus(BookingStatus.NO_SHOW);
                        bookingRepository.save(booking);
                        // Không publish BookingCompletedEvent → PT không được tính earning
                        // Không hoàn tiền user vì user không hủy trước
                }
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
        public PageResponse<PtClientSummary> getPtClients(UUID ptId, BookingStatus status, Pageable pageable) {
                List<BookingStatus> statuses = (status != null) ? List.of(status) : VALID_PROGRESS_STATUSES;
                Page<PtClientSummary> page = bookingRepository.findClientSummariesByPtId(ptId, statuses, pageable);

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
                // Lấy TẤT CẢ bookings của client với PT này (kể cả CONFIRMED, PENDING,
                // CANCELLED)
                List<Booking> bookings = bookingRepository.findAllByUserIdAndPtIdOrderByScheduledAtDesc(userId, ptId);

                List<ClientProgressDto.SessionHistoryDto> sessions = bookings.stream()
                                .map(b -> ClientProgressDto.SessionHistoryDto.builder()
                                                .bookingId(b.getId())
                                                .date(b.getScheduledAt())
                                                .status(b.getStatus().name())
                                                .workoutLogs(java.util.Collections.emptyList())
                                                .build())
                                .collect(Collectors.toList());

                return ClientProgressDto.builder()
                                .sessions(sessions)
                                .build();
        }

        private BookingSummary mapToSummary(Booking booking) {
                return BookingSummary.builder()
                                .id(booking.getId().toString())
                                .userId(booking.getUser() != null ? booking.getUser().getId().toString() : null)
                                .ptId(booking.getPt() != null ? booking.getPt().getId().toString() : null)

                                .ptName(booking.getPt() != null ? booking.getPt().getFullName() : null)
                                .ptAvatarUrl(booking.getPt() != null ? booking.getPt().getAvatarUrl() : null)

                                .userName(booking.getUser() != null ? booking.getUser().getFullName() : null)
                                .userAvatarUrl(booking.getUser() != null ? booking.getUser().getAvatarUrl() : null)
                                .scheduledAt(booking.getScheduledAt())
                                .endAt(booking.getEndAt())
                                .durationMinutes(booking.getDurationMinutes())
                                .totalAmount(booking.getTotalAmount())
                                .createdAt(booking.getCreatedAt())
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

        @Override
        @Transactional(readOnly = true)
        public PageResponse<BookingSummary> getAllBookingsPaginated(
                        BookingStatus status,
                        String ptName,
                        String search,
                        OffsetDateTime fromDate,
                        OffsetDateTime toDate,
                        Pageable pageable) {

                Page<Booking> page = bookingRepository.findAllWithFilters(
                                status != null ? status.name() : null, // ← .name() để convert enum → String
                                ptName, search, fromDate, toDate, pageable);

                return mapToPageResponse(page);
        }
}
