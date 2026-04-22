package com.gymapp.modules.banner;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;
    private final Cloudinary cloudinary;

    // CREATE
    public Banner createBanner(MultipartFile file, String title, String description) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

            String imageUrl = uploadResult.get("secure_url").toString();
            String publicId = uploadResult.get("public_id").toString();

            Banner banner = Banner.builder()
                    .imageUrl(imageUrl)
                    .publicId(publicId)
                    .title(title)
                    .description(description)
                    .isActive(true)
                    .build();

            return bannerRepository.save(banner);

        } catch (Exception e) {
            throw new RuntimeException("Upload banner failed");
        }
    }

    // UPDATE
    public Banner updateBanner(UUID id, MultipartFile file, String title, String description) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found"));

        try {
            // nếu có ảnh mới → xóa ảnh cũ
            if (file != null && !file.isEmpty()) {

                cloudinary.uploader().destroy(banner.getPublicId(), ObjectUtils.emptyMap());

                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

                banner.setImageUrl(uploadResult.get("secure_url").toString());
                banner.setPublicId(uploadResult.get("public_id").toString());
            }

            banner.setTitle(title);
            banner.setDescription(description);

            return bannerRepository.save(banner);

        } catch (Exception e) {
            throw new RuntimeException("Update banner failed");
        }
    }

    // DELETE
    public void deleteBanner(UUID id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found"));

        try {
            // xóa trên cloud
            cloudinary.uploader().destroy(banner.getPublicId(), ObjectUtils.emptyMap());

            // xóa DB
            bannerRepository.delete(banner);

        } catch (Exception e) {
            throw new RuntimeException("Delete banner failed");
        }
    }

    public List<BannerResponse> getActiveBanners() {
        return bannerRepository.findByIsActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(BannerMapper::toResponse)
                .toList();
    }

    /** Admin: lấy TẤT CẢ banner (kể cả ẩn) để quản lý */
    public List<BannerResponse> getAllBanners() {
        return bannerRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(BannerMapper::toResponse)
                .toList();
    }

    /** Toggle isActive: bật/tắt banner */
    public BannerResponse toggleActive(UUID id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found"));
        banner.setIsActive(!Boolean.TRUE.equals(banner.getIsActive()));
        return BannerMapper.toResponse(bannerRepository.save(banner));
    }
}
