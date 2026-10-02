package com.affycloud.TutorioBackend.services.implementation;

import com.affycloud.TutorioBackend.dtos.banner.BannerUpsertDto;
import com.affycloud.TutorioBackend.models.Banner;
import com.affycloud.TutorioBackend.repositories.BannerRepository;
import com.affycloud.TutorioBackend.services.BannerService;
import com.affycloud.TutorioBackend.utils.BannerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class BannerServiceImplementation implements BannerService {

    @Autowired
    private BannerRepository bannerRepository;

    @Override
    public Map<String, Object> upsertBanner(BannerUpsertDto request, UUID tenantId) {
        Map<String, Object> response = new HashMap<>();

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            response.put("message", "Title is required.");
            response.put("error", true);
            return response;
        }

        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            response.put("message", "Description is required.");
            response.put("error", true);
            return response;
        }

        if (request.getShowDays() == null || request.getShowDays() <= 0) {
            response.put("message", "showDays is required and must be a positive number.");
            response.put("error", true);
            return response;
        }

        // One banner per admin: update it if it exists, otherwise create it
        Banner banner = bannerRepository.findByTenantId(tenantId).orElseGet(() -> {
            Banner newBanner = new Banner();
            newBanner.setTenantId(tenantId);
            return newBanner;
        });

        banner.setTitle(request.getTitle().trim());
        banner.setDescription(request.getDescription().trim());
        banner.setIsActive(request.getIsActive() != null && request.getIsActive());
        banner.setShowDays(request.getShowDays());
        // Refresh on every save so the visibility window restarts from now
        banner.setUpdatedAt(new Date());

        bannerRepository.save(banner);

        response.put("message", "Banner saved successfully.");
        response.put("error", false);
        response.put("meta", banner);
        return response;
    }

    @Override
    public Banner getBannerForAdmin(UUID tenantId) {
        // Admin always sees the banner as-is, regardless of isActive/expiry
        return bannerRepository.findByTenantId(tenantId).orElse(null);
    }

    @Override
    public Banner getVisibleBannerForStudent(UUID tenantId) {
        Banner banner = bannerRepository.findByTenantId(tenantId).orElse(null);
        return BannerUtil.isBannerVisible(banner) ? banner : null;
    }

}
