package com.gymapp.modules.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gymapp.modules.payment.enums.PaymentProvider;
import com.gymapp.modules.payment.enums.PaymentStatus;
import com.gymapp.modules.payment.enums.PaymentType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentAdminResponse {
    @JsonProperty("payment_id")
    private UUID paymentId;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("user_full_name")
    private String userFullName;

    @JsonProperty("user_email")
    private String userEmail;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("payment_type")
    private PaymentType paymentType;

    @JsonProperty("status")
    private PaymentStatus status;

    @JsonProperty("provider")
    private PaymentProvider provider;

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("paid_at")
    private OffsetDateTime paidAt;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}
