package com.gymapp.modules.banner;

public class BannerMapper {

    public static BannerResponse toResponse(Banner banner) {
        return BannerResponse.builder()
                .id(banner.getId())
                .imageUrl(banner.getImageUrl())
                .title(banner.getTitle())
                .description(banner.getDescription())
                .isActive(Boolean.TRUE.equals(banner.getIsActive()))
                .build();
    }
}
