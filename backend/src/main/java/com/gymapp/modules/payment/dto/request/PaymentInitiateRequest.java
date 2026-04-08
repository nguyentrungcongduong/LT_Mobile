package com.gymapp.modules.payment.dto.request;

import com.gymapp.modules.payment.enums.PaymentProvider;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PaymentInitiateRequest {
    private UUID bookingId;
    private UUID membershipId;

    @NotNull
    private PaymentProvider provider;

    private String idempotencyKey;
    private String returnUrl;
}
