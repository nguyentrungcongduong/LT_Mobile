package com.gymapp.modules.booking.event;

import com.gymapp.modules.booking.entity.Booking;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class BookingAwaitingConfirmationEvent extends ApplicationEvent {
    private final Booking booking;

    public BookingAwaitingConfirmationEvent(Object source, Booking booking) {
        super(source);
        this.booking = booking;
    }
}
