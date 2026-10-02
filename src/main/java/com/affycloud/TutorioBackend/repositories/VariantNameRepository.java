package com.affycloud.TutorioBackend.repositories;


import com.affycloud.TutorioBackend.models.VariantName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VariantNameRepository extends JpaRepository<VariantName, UUID> {

    Optional<VariantName> findByVariantType(String variantType);
}

