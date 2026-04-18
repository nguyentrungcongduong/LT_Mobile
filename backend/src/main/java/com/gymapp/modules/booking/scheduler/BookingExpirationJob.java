package com.gymapp.modules.booking.scheduler;

import com.gymapp.modules.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingExpirationJob {

    private final BookingService bookingService;

    @Scheduled(fixedDelay = 60000) // Every 1 minute
    public void checkExpiredBookings() {
        log.info("Running BookingExpirationJob at {}", java.time.OffsetDateTime.now());
        bookingService.expirePendingBookings();
    }
}
