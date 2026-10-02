package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.Enrollment;
import com.affycloud.TutorioBackend.models.IssuedCertificate;
import com.affycloud.TutorioBackend.models.Student;
import com.affycloud.TutorioBackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IssuedCertificateRepository extends JpaRepository<IssuedCertificate, UUID> {

    boolean existsBySerialNumber(String serialNumber);

    Optional<IssuedCertificate> findByEnrollment(Enrollment enrollment);

    List<IssuedCertificate> findByStudentOrderByIssuedAtDesc(Student student);

    List<IssuedCertificate> findByAdminOrderByIssuedAtDesc(User admin);
}
