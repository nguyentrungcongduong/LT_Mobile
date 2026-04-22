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
     * Optional: Process refund.
     *
     * @param originalAmount Tổng số tiền của giao dịch gốc (VND).
     *                       Dùng để phân biệt hoàn toàn phần (amount == originalAmount → "02")
     *                       với hoàn một phần (amount < originalAmount → "03") cho VNPay.
     */
    boolean refund(String orderCode, String transactionNo, String transactionDate,
                   long amount, long originalAmount, String reason);
}
