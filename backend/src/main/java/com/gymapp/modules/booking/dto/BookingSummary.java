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
public class BookingSummary {
    private UUID id;

    @JsonProperty("pt_name")
    private String ptName;

    @JsonProperty("pt_avatar")
    private String ptAvatar;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("user_avatar")
    private String userAvatar;

    @JsonProperty("scheduled_at")
    private OffsetDateTime scheduledAt;

    @JsonProperty("end_at")
    private OffsetDateTime endAt;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @JsonProperty("pt_amount")
    private BigDecimal ptAmount;

    @JsonProperty("status")
    private BookingStatus status;
}
