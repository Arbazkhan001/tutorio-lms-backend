package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.banner.BannerUpsertDto;
import com.affycloud.TutorioBackend.models.Banner;

import java.util.Map;
import java.util.UUID;

public interface BannerService {
    Map<String, Object> upsertBanner(BannerUpsertDto request, UUID tenantId);
    Banner getBannerForAdmin(UUID tenantId);
    Banner getVisibleBannerForStudent(UUID tenantId);
}
