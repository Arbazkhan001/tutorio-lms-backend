package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.LmsPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PackageRepository extends JpaRepository<LmsPackage, UUID> {
    boolean existsByPackageNameIgnoreCase(String packageName);
}
