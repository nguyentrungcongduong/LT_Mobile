package com.gymapp.modules.banner;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder

public class BannerResponse {

    private UUID id;
    private String imageUrl;
    private String title;
    private String description;
}
