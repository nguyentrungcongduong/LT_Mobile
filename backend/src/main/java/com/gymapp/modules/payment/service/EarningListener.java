package com.gymapp.modules.payment.service;

import com.gymapp.modules.booking.entity.Booking;
import com.gymapp.modules.booking.event.BookingCancelledEvent;
import com.gymapp.modules.booking.event.BookingCompletedEvent;
import com.gymapp.modules.booking.event.BookingConfirmedEvent;
import com.gymapp.modules.payment.entity.PtEarning;
import com.gymapp.modules.payment.enums.EarningStatus;
import com.gymapp.modules.payment.repository.PtEarningRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class EarningListener {

    private final PtEarningRepository earningRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        Booking booking = event.getBooking();
        log.info("Creating PtEarning for booking {}", booking.getId());

        // Check if already exists (idempotency)
        if (earningRepository.findByBookingId(booking.getId()).isPresent()) {
            return;
        }

        PtEarning earning = PtEarning.builder()
                .ptId(booking.getPt().getId())
                .booking(booking)
                .grossAmount(booking.getTotalAmount())
                .platformFee(booking.getPlatformFee())
                .netAmount(booking.getPtAmount())
                .status(EarningStatus.PENDING)
                .build();

        earningRepository.save(earning);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingCompleted(BookingCompletedEvent event) {
        Booking booking = event.getBooking();
        log.info("Marking PtEarning as AVAILABLE for completed booking {}", booking.getId());

        PtEarning earning = earningRepository.findByBookingId(booking.getId()).orElse(null);
        
        if (earning == null) {
            log.info("Creating missing PtEarning for completed booking {}", booking.getId());
            earning = PtEarning.builder()
                    .ptId(booking.getPt().getId())
                    .booking(booking)
                    .grossAmount(booking.getTotalAmount())
                    .platformFee(booking.getPlatformFee())
                    .netAmount(booking.getPtAmount())
                    .status(EarningStatus.AVAILABLE)
                    .availableAt(OffsetDateTime.now())
                    .build();
        } else if (earning.getStatus() == EarningStatus.PENDING) {
            earning.setStatus(EarningStatus.AVAILABLE);
            earning.setAvailableAt(OffsetDateTime.now());
        }
        
        if (earning != null) {
            earningRepository.save(earning);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingCancelled(BookingCancelledEvent event) {
        Booking booking = event.getBooking();
        log.info("Marking PtEarning as CANCELLED for cancelled booking {}", booking.getId());

        earningRepository.findByBookingId(booking.getId()).ifPresent(earning -> {
            if (earning.getStatus() == EarningStatus.PENDING) {
                earning.setStatus(EarningStatus.CANCELLED);
                earningRepository.save(earning);
            }
        });
    }
}
