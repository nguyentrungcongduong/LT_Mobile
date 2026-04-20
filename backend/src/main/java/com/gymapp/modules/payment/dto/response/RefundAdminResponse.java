package com.gymapp.modules.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gymapp.modules.payment.enums.RefundStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class RefundAdminResponse {
    @JsonProperty("refund_id")
    private UUID refundId;

    @JsonProperty("payment_id")
    private UUID paymentId;

    @JsonProperty("user_full_name")
    private String userFullName;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("status")
    private RefundStatus status;

    @JsonProperty("processed_at")
    private OffsetDateTime processedAt;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}
