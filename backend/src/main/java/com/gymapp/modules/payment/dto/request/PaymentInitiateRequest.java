package com.gymapp.modules.payment.dto.request;

import com.gymapp.modules.payment.enums.PaymentProvider;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Getter
@Setter
@Builder
public class PaymentInitiateRequest {
    private UUID bookingId;
    private UUID membershipId;

    @NotNull
    private PaymentProvider provider;

    private String idempotencyKey;
    private String returnUrl;

    /** Nếu set → override amount thay vì tính từ booking (dùng cho batch booking) */
    private BigDecimal overrideAmount;

    /** Comma-separated bookingIds cho batch payment */
    private String batchBookingIds;
}
