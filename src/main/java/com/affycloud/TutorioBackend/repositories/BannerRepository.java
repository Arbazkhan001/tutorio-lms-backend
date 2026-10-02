package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BannerRepository extends JpaRepository<Banner, UUID> {
    Optional<Banner> findByTenantId(UUID tenantId);
}
