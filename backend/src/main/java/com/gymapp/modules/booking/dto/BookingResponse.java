package com.gymapp.modules.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gymapp.modules.booking.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class BookingResponse {
    @JsonProperty("booking_id")
    private UUID bookingId;

    @JsonProperty("pt_name")
    private String ptName;

    @JsonProperty("scheduled_at")
    private OffsetDateTime scheduledAt;

    @JsonProperty("end_at")
    private OffsetDateTime endAt;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @JsonProperty("status")
    private BookingStatus status;

    @JsonProperty("payment_url")
    private String paymentUrl;

    @JsonProperty("expires_at")
    private OffsetDateTime expiresAt;
}
