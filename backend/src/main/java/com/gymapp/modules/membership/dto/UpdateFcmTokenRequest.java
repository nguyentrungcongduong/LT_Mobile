package com.gymapp.modules.membership.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * Request body cho PUT /api/v1/users/me/fcm-token
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFcmTokenRequest {

    @NotBlank(message = "fcmToken must not be blank")
    private String fcmToken;
}
