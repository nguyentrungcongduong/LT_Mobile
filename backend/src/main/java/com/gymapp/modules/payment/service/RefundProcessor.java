package com.gymapp.modules.payment.service;

import com.gymapp.modules.booking.entity.Booking;
import com.gymapp.modules.booking.entity.PtAvailability;
import com.gymapp.modules.booking.enums.BookingStatus;
import com.gymapp.modules.booking.repository.BookingRepository;
import com.gymapp.modules.booking.repository.PtAvailabilityRepository;
import com.gymapp.modules.booking.event.BookingCancelledEvent;
import com.gymapp.modules.payment.entity.Refund;
import com.gymapp.modules.payment.enums.PaymentProvider;
import com.gymapp.modules.payment.enums.RefundStatus;
import com.gymapp.modules.payment.enums.PaymentStatus;
import com.gymapp.modules.payment.gateway.PaymentGateway;
import com.gymapp.modules.payment.gateway.momo.MoMoService;
import com.gymapp.modules.payment.gateway.vnpay.VNPayService;
import com.gymapp.modules.payment.repository.RefundRepository;
import com.gymapp.modules.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefundProcessor {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PtAvailabilityRepository availabilityRepository;
    private final VNPayService vnpayService;
    private final MoMoService momoService;

    private RefundProcessor self;

    @Autowired
    public void setSelf(@Lazy RefundProcessor self) {
        this.self = self;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingCancelled(BookingCancelledEvent event) {
        log.info("Async processing refund for booking cancellation {}", event.getBooking().getId());
        self.processUpcomingRefunds();
    }

    @Scheduled(fixedDelay = 300_000, initialDelay = 10_000) // Every 5 minutes retry
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processUpcomingRefunds() {
        List<Refund> pendingRefunds = refundRepository.findAllByStatus(RefundStatus.PENDING);
        if (pendingRefunds.isEmpty()) {
            return;
        }

        log.info("Found {} pending refunds to process", pendingRefunds.size());
        for (Refund refund : pendingRefunds) {
            try {
                processRefund(refund);
            } catch (Exception e) {
                log.error("Internal error processing refund {}: {}", refund.getId(), e.getMessage());
            }
        }
    }

    private void processRefund(Refund refund) {
        log.info("Processing refund ID: {}, Payment ID: {}", refund.getId(), refund.getPayment().getId());
        try {
            refund.setStatus(RefundStatus.PROCESSING);
            refundRepository.saveAndFlush(refund);

            PaymentProvider provider = refund.getPayment().getProvider();
            PaymentGateway gateway = getGateway(provider);

            boolean success = gateway.refund(
                    refund.getPayment().getId().toString(),
                    refund.getPayment().getTransactionId(),
                    refund.getPayment().getVnpPayDate(),
                    refund.getAmount().longValue(),
                    refund.getReason());

            if (success) {
                log.info("Refund successful for Refund ID: {}", refund.getId());
                refund.setStatus(RefundStatus.PROCESSED);
                refund.setProcessedAt(OffsetDateTime.now());
                refund.setGatewayRefundId("GATEWAY-REFUND-" + refund.getPayment().getTransactionId());

                // Also update Payment status
                refund.getPayment().setStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(refund.getPayment());
            } else {
                log.warn("Refund failed at gateway for Refund ID: {}", refund.getId());
                handleFailure(refund, "GATEWAY_ERROR");
            }
        } catch (Exception e) {
            log.error("Exception during refund process for {}: {}", refund.getId(), e.getMessage());
            handleFailure(refund, e.getMessage());
        }
        // TODO: Push notification success or error for user in mobile app

        refundRepository.save(refund);
    }

    private void handleFailure(Refund refund, String reason) {
        refund.setRetryCount(refund.getRetryCount() + 1);
        refund.setFailureReason(reason);
        refund.setLastRetryAt(OffsetDateTime.now());
        if (refund.getRetryCount() >= 5) {
            refund.setStatus(RefundStatus.FAILED);
            // Revert booking and availability if refund fails permanently
            if (refund.getBookingId() != null) {
                bookingRepository.findById(refund.getBookingId()).ifPresent(booking -> {
                    log.info("Reverting booking {} status to CONFIRMED due to refund failure", booking.getId());
                    booking.setStatus(BookingStatus.CONFIRMED);
                    booking.setCancelBy(null);
                    booking.setCancelReason(null);
                    booking.setCancelledAt(null);
                    bookingRepository.save(booking);

                    PtAvailability availability = booking.getAvailability();
                    if (availability != null) {
                        availability.setBooked(true);
                        availabilityRepository.save(availability);
                        log.info("Re-booked availability slot {} for booking {}", availability.getId(), booking.getId());
                    }
                });
            }
        } else {
            refund.setStatus(RefundStatus.PENDING);
        }
    }

    private PaymentGateway getGateway(PaymentProvider provider) {
        if (provider == PaymentProvider.VNPAY)
            return vnpayService;
        if (provider == PaymentProvider.MOMO)
            return momoService;
        throw new IllegalStateException("Provider " + provider + " is not supported for refund");
    }
}
