package com.gymapp.modules.user.dto.response;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
public class UserStatusResponse {
    private UUID id;
    private String email;
    @JsonProperty("isActive")
    private boolean isActive;
    private String message;
}
