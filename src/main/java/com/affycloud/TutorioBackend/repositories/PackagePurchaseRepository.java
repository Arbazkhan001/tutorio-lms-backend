package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.LmsPackage;
import com.affycloud.TutorioBackend.models.PackagePurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PackagePurchaseRepository extends JpaRepository<PackagePurchase, UUID> {


    PackagePurchase findTopByUserIdOrderByCreatedAtDesc(UUID userId);
}
