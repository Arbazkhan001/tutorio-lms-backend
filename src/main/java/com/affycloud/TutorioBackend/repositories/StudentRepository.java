package com.affycloud.TutorioBackend.repositories;


import com.affycloud.TutorioBackend.models.Student;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    Student findByTenantIdAndEmail(UUID tenantId, String email);

    Student findByTenantIdAndPhoneNumber(UUID tenantId, String phoneNumber);

    /**
     * Owner of {@code phoneNumber} within the tenant, ignoring the student the
     * number is being saved for.
     *
     * <p>Uniqueness checks on an update must never count the caller's own row:
     * re-submitting the number you already own is not a duplicate. Excluding
     * the id in SQL (rather than comparing ids afterwards) also keeps the
     * check correct when several rows somehow share the number, which is
     * possible on databases where {@code uq_student_tenant_phone} was never
     * created.
     */
    Optional<Student> findFirstByTenantIdAndPhoneNumberAndIdNot(UUID tenantId, String phoneNumber, UUID id);

    /** Every row holding this number in the tenant — used by data-repair checks. */
    java.util.List<Student> findAllByTenantIdAndPhoneNumber(UUID tenantId, String phoneNumber);

    boolean existsByTenantIdAndEmail(UUID tenantId, String email);

    boolean existsByTenantIdAndPhoneNumber(UUID tenantId, String phoneNumber);

    Optional<Student> findByTenantIdAndId(UUID tenantId, UUID id);

    Page<Student> findAllByTenantId(UUID tenantId, Pageable pageable);

    long countByTenantId(UUID tenantId);

    long countByTenantIdAndCreatedAtBetween(
            UUID tenantId,
            Date start,
            Date end
    );

    @org.springframework.data.jpa.repository.Query("SELECT s FROM Student s WHERE s.tenantId = :tenantId AND (" +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "s.phoneNumber LIKE CONCAT('%', :keyword, '%') OR " +
            "s.countryCode LIKE CONCAT('%', :keyword, '%'))")
    Page<Student> searchStudents(@org.springframework.data.repository.query.Param("tenantId") UUID tenantId, 
                                 @org.springframework.data.repository.query.Param("keyword") String keyword, 
                                 Pageable pageable);

}

//    Student findByEmail(String email);
//    Student findByPhoneNumber(String phoneNumber); // phoneNumber unique check
//    Boolean existsByPhoneNumber(String phoneNumber); // phoneNumber unique check
//    Boolean existsByEmail(String email); // email unique check
//
//    // Custom method to find users with the role "STUDENT"
//    List<Student> findByRole(Role role);





