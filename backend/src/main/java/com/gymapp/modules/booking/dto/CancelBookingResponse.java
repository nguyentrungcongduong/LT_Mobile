package com.gymapp.modules.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gymapp.modules.booking.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Getter
@Setter
@Builder
public class CancelBookingResponse {
    @JsonProperty("booking_id")
    private UUID bookingId;

    @JsonProperty("status")
    private BookingStatus status;

    @JsonProperty("refund_amount")
    private BigDecimal refundAmount;

    @JsonProperty("refund_pct")
    private BigDecimal refundPct;
}
