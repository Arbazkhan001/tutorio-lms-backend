package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.CertificateTemplate;
import com.affycloud.TutorioBackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CertificateTemplateRepository extends JpaRepository<CertificateTemplate, UUID> {
    Optional<CertificateTemplate> findByAdmin(User admin);
}
