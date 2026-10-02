package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.Section;
import com.affycloud.TutorioBackend.models.SectionType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SectionRepository extends JpaRepository<Section, UUID> {
    Optional<Section> findBySectionTypeAndTenantId(SectionType sectionType, UUID tenantId);
    List<Section> findAllByTenantId(UUID tenantId, Sort createdAt);
}
