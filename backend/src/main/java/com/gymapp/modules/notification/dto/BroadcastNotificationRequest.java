package com.gymapp.modules.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BroadcastNotificationRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String body;

    @NotBlank
    @JsonProperty("target_group")
    private String targetGroup;

    @JsonProperty("user_ids")
    private List<UUID> userIds;
}