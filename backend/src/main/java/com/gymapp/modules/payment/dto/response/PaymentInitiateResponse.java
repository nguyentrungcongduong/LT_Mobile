package com.gymapp.modules.payment.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Getter
@Setter
@Builder
public class PaymentInitiateResponse {
    private UUID paymentId;
    private String gatewayUrl;
    private BigDecimal amount;
    private OffsetDateTime expiresAt;
}
