package com.gymapp.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@Data
@Getter
@Setter
@Builder
public class TokenRefreshRequest {

    @NotBlank(message = "Refresh token không được để trống")
    private String refreshToken;
}
