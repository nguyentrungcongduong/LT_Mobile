package com.gymapp.modules.payment.gateway.momo;

import com.gymapp.common.util.HmacUtils;
import com.gymapp.config.PaymentProperties;
import com.gymapp.modules.payment.constant.MoMoParams;
import com.gymapp.modules.payment.gateway.PaymentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoMoService implements PaymentGateway {

    private final PaymentProperties paymentProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String createPaymentUrl(String orderCode, long amount, String orderInfo, String ipAddress,
            String returnUrl) {
        String requestId = UUID.randomUUID().toString();
        String requestType = "payWithMethod";
        String extraData = "";
        String finalReturnUrl = paymentProperties.getMomo().getRedirectUrl();
        String ipnUrl = paymentProperties.getMomo().getIpnUrl();

        // signature =
        // accessKey=$accessKey&amount=$amount&extraData=$extraData&ipnUrl=$ipnUrl&orderId=$orderId&orderInfo=$orderInfo&partnerCode=$partnerCode&redirectUrl=$redirectUrl&requestId=$requestId&requestType=$requestType
        String rawSignature = String.format(
                "accessKey=%s&amount=%s&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                paymentProperties.getMomo().getAccessKey(),
                amount,
                extraData,
                ipnUrl,
                orderCode,
                orderInfo,
                paymentProperties.getMomo().getPartnerCode(),
                finalReturnUrl,
                requestId,
                requestType);

        String signature = HmacUtils.hmacSHA256(paymentProperties.getMomo().getSecretKey(), rawSignature);

        Map<String, Object> body = new HashMap<>();
        body.put(MoMoParams.PARTNER_CODE, paymentProperties.getMomo().getPartnerCode());
        body.put(MoMoParams.PARTNER_NAME, "Gym App");
        body.put(MoMoParams.STORE_ID, "GymApp");
        body.put(MoMoParams.REQUEST_ID, requestId);
        body.put(MoMoParams.AMOUNT, amount);
        body.put(MoMoParams.ORDER_ID, orderCode);
        body.put(MoMoParams.ORDER_INFO, orderInfo);
        body.put(MoMoParams.REDIRECT_URL, finalReturnUrl);
        body.put(MoMoParams.IPN_URL, ipnUrl);
        body.put(MoMoParams.LANG, "vi");
        body.put(MoMoParams.EXTRA_DATA, extraData);
        body.put(MoMoParams.REQUEST_TYPE, requestType);
        body.put(MoMoParams.SIGNATURE, signature);

        try {
            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                    paymentProperties.getMomo().getApiUrl(),
                    HttpMethod.POST,
                    new HttpEntity<>(body),
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            Map<String, Object> response = responseEntity.getBody();
            if (response != null && response.containsKey("payUrl")) {
                return (String) response.get("payUrl");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error calling MoMo API: " + e.getMessage(), e);
        }
        return null;
    }

    private String decodeUtf8(String val) {
        if (val == null)
            return null;
        try {
            byte[] isoBytes = val.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
            String utf8Str = new String(isoBytes, java.nio.charset.StandardCharsets.UTF_8);
            // Heuristic to detect if it was incorrectly decoded by Tomcat as ISO-8859-1
            if (utf8Str.indexOf('\uFFFD') == -1 && utf8Str.length() < val.length()) {
                return utf8Str;
            }
        } catch (Exception e) {
            // Ignore and fallback
        }
        return val;
    }

    @Override
    public boolean verifySignature(Map<String, String> params) {
        String signature = params.get(MoMoParams.SIGNATURE);
        if (signature == null)
            return false;

        // Thu thập các trường cần thiết cho signature
        Map<String, String> signatureFields = new HashMap<>();
        signatureFields.put(MoMoParams.ACCESS_KEY, paymentProperties.getMomo().getAccessKey());
        signatureFields.put(MoMoParams.AMOUNT, params.get(MoMoParams.AMOUNT));
        signatureFields.put(MoMoParams.EXTRA_DATA, decodeUtf8(params.get(MoMoParams.EXTRA_DATA)));
        signatureFields.put(MoMoParams.MESSAGE, decodeUtf8(params.get(MoMoParams.MESSAGE)));
        signatureFields.put(MoMoParams.ORDER_ID, params.get(MoMoParams.ORDER_ID));
        signatureFields.put(MoMoParams.ORDER_INFO, decodeUtf8(params.get(MoMoParams.ORDER_INFO)));
        signatureFields.put(MoMoParams.ORDER_TYPE, params.get(MoMoParams.ORDER_TYPE));
        signatureFields.put(MoMoParams.PARTNER_CODE, paymentProperties.getMomo().getPartnerCode());
        signatureFields.put(MoMoParams.PAY_TYPE, params.get(MoMoParams.PAY_TYPE));
        signatureFields.put(MoMoParams.REQUEST_ID, params.get(MoMoParams.REQUEST_ID));
        signatureFields.put(MoMoParams.RESPONSE_TIME, params.get(MoMoParams.RESPONSE_TIME));
        signatureFields.put(MoMoParams.RESULT_CODE, params.get(MoMoParams.RESULT_CODE));
        signatureFields.put(MoMoParams.TRANS_ID, params.get(MoMoParams.TRANS_ID));

        // Sắp xếp key theo alphabet
        List<String> sortedKeys = new ArrayList<>(signatureFields.keySet());
        Collections.sort(sortedKeys);

        // Xây dựng rawSignature
        StringBuilder rawSignatureBuilder = new StringBuilder();
        for (String key : sortedKeys) {
            if (rawSignatureBuilder.length() > 0)
                rawSignatureBuilder.append("&");
            rawSignatureBuilder.append(key).append("=").append(signatureFields.get(key));
        }
        String rawSignature = rawSignatureBuilder.toString();

        String expectedSignature = HmacUtils.hmacSHA256(paymentProperties.getMomo().getSecretKey(), rawSignature);

        log.info("=== MoMo Signature Verification ===");
        log.info("Received Params: {}", params);
        log.info("Raw String to Hash: {}", rawSignature);
        log.info("Expected Signature: {}", expectedSignature);
        log.info("Actual Signature: {}", signature);

        return expectedSignature.equalsIgnoreCase(signature);
    }

    @Override
    public boolean refund(String orderCode, String transactionNo, String transactionDate, long amount, String reason) {
        String transactionId = transactionNo;
        String refundUrl = paymentProperties.getMomo().getApiRefundUrl();
        String requestId = UUID.randomUUID().toString();
        String orderId = "RF-" + System.currentTimeMillis();

        String description = reason != null ? reason : "Refund transaction " + transactionId;

        String requestRawData = new StringBuilder()
                .append(MoMoParams.ACCESS_KEY).append("=").append(paymentProperties.getMomo().getAccessKey())
                .append("&")
                .append(MoMoParams.AMOUNT).append("=").append(amount).append("&")
                .append(MoMoParams.DESCRIPTION).append("=").append(description).append("&")
                .append(MoMoParams.ORDER_ID).append("=").append(orderId).append("&")
                .append(MoMoParams.PARTNER_CODE).append("=").append(paymentProperties.getMomo().getPartnerCode())
                .append("&")
                .append(MoMoParams.REQUEST_ID).append("=").append(requestId).append("&")
                .append(MoMoParams.TRANS_ID).append("=").append(transactionId)
                .toString();

        String signature = HmacUtils.hmacSHA256(paymentProperties.getMomo().getSecretKey(), requestRawData);

        Map<String, Object> body = new HashMap<>();
        body.put(MoMoParams.PARTNER_CODE, paymentProperties.getMomo().getPartnerCode());
        body.put(MoMoParams.ORDER_ID, orderId);
        body.put(MoMoParams.REQUEST_ID, requestId);
        body.put(MoMoParams.AMOUNT, amount);
        try {
            body.put(MoMoParams.TRANS_ID, Long.valueOf(transactionId));
        } catch (NumberFormatException e) {
            body.put(MoMoParams.TRANS_ID, transactionId);
        }
        body.put(MoMoParams.LANG, "vi");
        body.put(MoMoParams.DESCRIPTION, description);
        body.put(MoMoParams.SIGNATURE, signature);

        log.info("Sending MoMo refund request: {}", body);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    refundUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(body),
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            Map<String, Object> resBody = response.getBody();
            log.info("MoMo refund response raw: {}", resBody);

            if (resBody != null && resBody.get(MoMoParams.RESULT_CODE) != null) {
                // resultCode = 0 means successful in MoMo
                return String.valueOf(resBody.get(MoMoParams.RESULT_CODE)).equals("0");
            }
        } catch (Exception e) {
            log.error("Failed to execute MoMo refund for transaction {}: {}", transactionId, e.getMessage());
        }
        return false;
    }

    public Map<String, Object> checkTransactionStatus(String orderId, String requestId) {
        String queryUrl = paymentProperties.getMomo().getApiUrl().replace("/create", "/query");

        String requestRawData = String.format(
                "accessKey=%s&orderId=%s&partnerCode=%s&requestId=%s",
                paymentProperties.getMomo().getAccessKey(),
                orderId,
                paymentProperties.getMomo().getPartnerCode(),
                requestId);
        String signature = HmacUtils.hmacSHA256(paymentProperties.getMomo().getSecretKey(), requestRawData);

        Map<String, Object> body = new HashMap<>();
        body.put("partnerCode", paymentProperties.getMomo().getPartnerCode());
        body.put("requestId", requestId);
        body.put("orderId", orderId);
        body.put("lang", "vi");
        body.put("signature", signature);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    queryUrl,
                    org.springframework.http.HttpMethod.POST,
                    new HttpEntity<>(body),
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {
                    });
            Map<String, Object> resBody = response.getBody();
            if (resBody != null && resBody.containsKey("resultCode")) {
                return resBody;
            }
        } catch (Exception e) {
            log.warn("Failed to query MoMo transaction status for order {}: {}", orderId, e.getMessage());
        }
        return null;
    }
}
