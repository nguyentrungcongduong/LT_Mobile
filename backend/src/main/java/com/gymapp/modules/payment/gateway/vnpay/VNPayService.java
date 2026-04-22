package com.gymapp.modules.payment.gateway.vnpay;

import com.gymapp.common.util.HmacUtils;
import com.gymapp.config.PaymentProperties;
import com.gymapp.modules.payment.constant.VNPayParams;
import com.gymapp.modules.payment.gateway.PaymentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class VNPayService implements PaymentGateway {

        private final PaymentProperties paymentProperties;
        private final RestTemplate restTemplate = new RestTemplate();

        @Override
        public String createPaymentUrl(String orderCode, long amount, String orderInfo, String ipAddress,
                        String returnUrl) {
                String vnp_Version = "2.1.0";
                String vnp_Command = "pay";
                String vnp_OrderType = "other";

                Map<String, String> vnp_Params = new TreeMap<>();
                vnp_Params.put(VNPayParams.VERSION, vnp_Version);
                vnp_Params.put(VNPayParams.COMMAND, vnp_Command);
                vnp_Params.put(VNPayParams.TMN_CODE, paymentProperties.getVnpay().getTmnCode());
                vnp_Params.put(VNPayParams.AMOUNT, String.valueOf(amount * 100)); // VNPay amount * 100
                vnp_Params.put(VNPayParams.CURR_CODE, "VND");
                vnp_Params.put(VNPayParams.TXN_REF, orderCode);
                vnp_Params.put(VNPayParams.ORDER_INFO, orderInfo);
                vnp_Params.put(VNPayParams.ORDER_TYPE, vnp_OrderType);
                vnp_Params.put(VNPayParams.LOCALE, "vn");
                vnp_Params.put(VNPayParams.RETURN_URL,
                                returnUrl != null ? returnUrl : paymentProperties.getVnpay().getReturnUrl());
                vnp_Params.put(VNPayParams.IP_ADDR, ipAddress);

                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                vnp_Params.put(VNPayParams.CREATE_DATE, now.format(formatter));
                vnp_Params.put(VNPayParams.EXPIRE_DATE, now.plusMinutes(15).format(formatter));

                String hashData = vnp_Params.entrySet().stream()
                                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
                                                + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                                .collect(Collectors.joining("&"));

                String vnp_SecureHash = HmacUtils.hmacSHA512(paymentProperties.getVnpay().getHashSecret(), hashData);

                return paymentProperties.getVnpay().getBaseUrl() + "?" + hashData + "&" + VNPayParams.SECURE_HASH + "="
                                + vnp_SecureHash;
        }

        @Override
        public boolean verifySignature(Map<String, String> params) {
                String vnp_SecureHash = params.get(VNPayParams.SECURE_HASH);
                if (vnp_SecureHash == null)
                        return false;

                Map<String, String> signParams = new TreeMap<>(params);
                signParams.remove(VNPayParams.SECURE_HASH);
                signParams.remove(VNPayParams.SECURE_HASH_TYPE);

                String hashData = signParams.entrySet().stream()
                                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
                                                + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                                .collect(Collectors.joining("&"));

                String expectedHash = HmacUtils.hmacSHA512(paymentProperties.getVnpay().getHashSecret(), hashData);
                return expectedHash.equalsIgnoreCase(vnp_SecureHash);
        }

        @Override
        public boolean refund(String orderCode, String transactionNo, String transactionDate, long amount,
                        long originalAmount, String reason) {
                String refundUrl = paymentProperties.getVnpay().getRefundUrl();

                String vnp_RequestId = UUID.randomUUID().toString();
                String vnp_Version = "2.1.0";
                String vnp_Command = "refund";
                String vnp_TmnCode = paymentProperties.getVnpay().getTmnCode();

                // 02 = Hoàn toàn phần; 03 = Hoàn một phần
                // VNPay yêu cầu "03" khi số tiền hoàn < tổng tiền giao dịch gốc
                String vnp_TransactionType = (amount >= originalAmount) ? "02" : "03";
                log.info("VNPay refund type: {} (refund={}, original={})", vnp_TransactionType, amount, originalAmount);

                String vnp_TxnRef = orderCode;
                long amountInVnd = amount * 100;
                String vnp_Amount = String.valueOf(amountInVnd);
                String vnp_OrderInfo = reason != null ? reason : "Refund transaction " + orderCode;
                String vnp_TransactionNo = transactionNo != null ? transactionNo : "";

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                String vnp_TransactionDate = transactionDate;
                String vnp_CreateBy = "System";
                String vnp_CreateDate = LocalDateTime.now().format(formatter);
                String vnp_IpAddr = "127.0.0.1";

                // Signature generation:
                // vnp_RequestId|vnp_Version|vnp_Command|vnp_TmnCode|vnp_TransactionType|vnp_TxnRef|vnp_Amount|vnp_TransactionNo|vnp_TransactionDate|vnp_CreateBy|vnp_CreateDate|vnp_IpAddr|vnp_OrderInfo
                String hashData = String.join("|",
                                vnp_RequestId, vnp_Version, vnp_Command, vnp_TmnCode,
                                vnp_TransactionType, vnp_TxnRef, vnp_Amount, vnp_TransactionNo,
                                vnp_TransactionDate, vnp_CreateBy, vnp_CreateDate,
                                vnp_IpAddr, vnp_OrderInfo);

                String vnp_SecureHash = HmacUtils.hmacSHA512(paymentProperties.getVnpay().getHashSecret(), hashData);

                Map<String, Object> body = new HashMap<>();
                body.put(VNPayParams.REQUEST_ID, vnp_RequestId);
                body.put(VNPayParams.VERSION, vnp_Version);
                body.put(VNPayParams.COMMAND, vnp_Command);
                body.put(VNPayParams.TMN_CODE, vnp_TmnCode);
                body.put(VNPayParams.TRANSACTION_TYPE, vnp_TransactionType);
                body.put(VNPayParams.TXN_REF, vnp_TxnRef);
                body.put(VNPayParams.AMOUNT, amountInVnd);
                body.put(VNPayParams.ORDER_INFO, vnp_OrderInfo);
                body.put(VNPayParams.TRANSACTION_NO, vnp_TransactionNo);
                body.put(VNPayParams.TRANSACTION_DATE, vnp_TransactionDate);
                body.put(VNPayParams.CREATE_BY, vnp_CreateBy);
                body.put(VNPayParams.CREATE_DATE, vnp_CreateDate);
                body.put(VNPayParams.IP_ADDR, vnp_IpAddr);
                body.put(VNPayParams.SECURE_HASH, vnp_SecureHash);

                log.info("Sending VNPay refund request: {}", body);

                try {
                        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                                        refundUrl,
                                        HttpMethod.POST,
                                        new HttpEntity<>(body),
                                        new ParameterizedTypeReference<Map<String, Object>>() {
                                        });

                        Map<String, Object> resBody = response.getBody();
                        log.info("VNPay refund response raw: {}", resBody);

                        String responseCode = (String) resBody.get(VNPayParams.RESPONSE_CODE);
                        if (resBody != null && ("00".equals(responseCode) || "94".equals(responseCode))) {
                                if ("94".equals(responseCode)) {
                                        log.warn("VNPay refund for transaction {} already executed previously (Code 94)", orderCode);
                                }
                                return true;
                        }
                } catch (Exception e) {
                        log.error("Failed to execute VNPay refund for transaction {}: {}", orderCode, e.getMessage());
                }
                return false;
        }
}
