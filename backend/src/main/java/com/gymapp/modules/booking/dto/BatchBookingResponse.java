package com.gymapp.modules.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Getter
@Setter
@Builder
public class BatchBookingResponse {

    @JsonProperty("booking_ids")
    private List<UUID> bookingIds;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @JsonProperty("payment_url")
    private String paymentUrl;

    @JsonProperty("expires_at")
    private OffsetDateTime expiresAt;

    @JsonProperty("session_count")
    private int sessionCount;
}
