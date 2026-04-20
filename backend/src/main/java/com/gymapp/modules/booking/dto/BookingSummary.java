package com.gymapp.modules.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gymapp.modules.booking.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class BookingSummary {

    private String id;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("pt_id")
    private String ptId;

    private String ptName;

    private String ptAvatarUrl;

    private String userName;

    @JsonProperty("user_avatar_url")
    private String userAvatarUrl;

    @JsonProperty("scheduled_at")
    private OffsetDateTime scheduledAt;

    @JsonProperty("end_at")
    private OffsetDateTime endAt;

    @JsonProperty("duration_minutes")
    private Integer durationMinutes;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @JsonProperty("pt_amount")
    private BigDecimal ptAmount;

    @JsonProperty("status")
    private BookingStatus status;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

}
