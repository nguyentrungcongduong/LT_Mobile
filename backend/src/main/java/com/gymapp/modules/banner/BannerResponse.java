package com.gymapp.modules.banner;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BannerResponse {
    private UUID id;
    private String imageUrl;
    private String title;
    private String description;
    /** Dùng @JsonProperty để force key "isActive" (không bị Jackson strip thành "active") */
    @JsonProperty("isActive")
    private boolean isActive;
}
