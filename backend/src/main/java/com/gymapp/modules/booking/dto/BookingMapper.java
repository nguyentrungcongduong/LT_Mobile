package com.gymapp.modules.booking.dto;

import org.springframework.stereotype.Component;

import com.gymapp.modules.booking.entity.Booking;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking, String paymentUrl) {
        return BookingResponse.builder()
                .bookingId(booking.getId())
                .ptName(
                        booking.getPt() != null
                                ? booking.getPt().getFullName()
                                : null)
                .scheduledAt(booking.getScheduledAt())
                .endAt(booking.getEndAt())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .paymentUrl(paymentUrl)
                .expiresAt(booking.getExpiresAt())
                .build();
    }
}
