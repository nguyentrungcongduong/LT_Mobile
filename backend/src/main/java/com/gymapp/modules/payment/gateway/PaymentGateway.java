package com.gymapp.modules.payment.gateway;

import java.util.Map;

public interface PaymentGateway {
    
    /**
     * Create gateway payment URL
     */
    String createPaymentUrl(String orderCode, long amount, String orderInfo, String ipAddress, String returnUrl);

    /**
     * Verify callback signature
     */
    boolean verifySignature(Map<String, String> params);

    /**
     * Optional: Process refund
     */
    boolean refund(String orderCode, String transactionNo, String transactionDate, long amount, String reason);
}
