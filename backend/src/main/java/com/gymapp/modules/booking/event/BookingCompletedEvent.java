package com.gymapp.modules.booking.event;

import com.gymapp.modules.booking.entity.Booking;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class BookingCompletedEvent extends ApplicationEvent {
    private final Booking booking;

    public BookingCompletedEvent(Object source, Booking booking) {
        super(source);
        this.booking = booking;
    }
}
