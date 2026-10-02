package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.SectionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SectionTypeRepository extends JpaRepository<SectionType, UUID> {
    Optional<SectionType> findByType(String type);
}
