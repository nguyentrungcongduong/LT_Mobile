package com.gymapp.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {

    private Vnpay vnpay = new Vnpay();
    private Momo momo = new Momo();

    @Data
    public static class Vnpay {
        private String tmnCode;
        private String hashSecret;
        private String baseUrl;
        private String returnUrl;
        private String refundUrl;
        private String mobileRedirectUrl;
        private String apiUrl; // For refund
    }

    @Data
    public static class Momo {
        private String partnerCode;
        private String accessKey;
        private String secretKey;
        private String apiUrl;
        private String apiRefundUrl;
        private String redirectUrl;
        private String mobileRedirectUrl;
        private String ipnUrl;
    }
}
