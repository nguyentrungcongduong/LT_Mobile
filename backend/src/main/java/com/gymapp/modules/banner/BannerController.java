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

    // ── ADMIN ────────────────────────────────────────────────────────────

    /** Admin: lấy TẤT CẢ banner (kể cả ẩn) để quản lý */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<BannerResponse>> getAll() {
        return ApiResponse.ok(bannerService.getAllBanners(), "Get all banners success");
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BannerResponse> create(
            @RequestParam MultipartFile file,
            @RequestParam String title,
            @RequestParam(required = false, defaultValue = "") String description) {
        Banner banner = bannerService.createBanner(file, title, description);
        return ApiResponse.ok(BannerMapper.toResponse(banner), "Create banner success");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BannerResponse> update(
            @PathVariable UUID id,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam String title,
            @RequestParam(required = false, defaultValue = "") String description) {
        Banner banner = bannerService.updateBanner(id, file, title, description);
        return ApiResponse.ok(BannerMapper.toResponse(banner), "Update banner success");
    }

    /** Admin: bật/tắt hiển thị banner */
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BannerResponse> toggle(@PathVariable UUID id) {
        BannerResponse updated = bannerService.toggleActive(id);
        String msg = updated.isActive() ? "Banner đã được hiển thị" : "Banner đã bị ẩn";
        return ApiResponse.ok(updated, msg);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        bannerService.deleteBanner(id);
        return ApiResponse.message("Delete banner success");
    }

    // ── PUBLIC (ANDROID) ─────────────────────────────────────────────────

    @GetMapping("/active")
    public ApiResponse<List<BannerResponse>> getActive() {
        List<BannerResponse> banners = bannerService.getActiveBanners();
        return ApiResponse.ok(banners, "Get banners success");
    }
}