package com.gymapp.modules.banner;

import com.gymapp.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    // ADMIN

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Banner> create(
            @RequestParam MultipartFile file,
            @RequestParam String title,
            @RequestParam String description) {
        Banner banner = bannerService.createBanner(file, title, description);
        return ApiResponse.ok(banner, "Create banner success");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Banner> update(
            @PathVariable UUID id,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam String title,
            @RequestParam String description) {
        Banner banner = bannerService.updateBanner(id, file, title, description);
        return ApiResponse.ok(banner, "Update banner success");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        bannerService.deleteBanner(id);
        return ApiResponse.message("Delete banner success");
    }

    // PUBLIC (ANDROID)

    @GetMapping("/active")
    public ApiResponse<List<BannerResponse>> getActive() {
        List<BannerResponse> banners = bannerService.getActiveBanners();
        return ApiResponse.ok(banners, "Get banners success");
    }
}