package com.affycloud.TutorioBackend.repositories;


import com.affycloud.TutorioBackend.models.VariantValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VariantValueRepository extends JpaRepository<VariantValue, UUID> {

    List<VariantValue> findByVariantName_VariantTypeAndTenantId(String variantType, UUID tenantId);

    boolean existsByVariantName_VariantTypeAndNameAndTenantId(String variantType, String name, UUID tenantId);
}


