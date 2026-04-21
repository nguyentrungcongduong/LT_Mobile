package com.gymapp.modules.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Getter
@Setter 
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VnpayIpnResponse {

    @JsonProperty("RspCode")
    private String responseCode;

    @JsonProperty("Message")
    private String message;
}
