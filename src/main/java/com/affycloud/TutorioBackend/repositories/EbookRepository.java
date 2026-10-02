package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.dtos.paymentType.PaymentType;
import com.affycloud.TutorioBackend.models.Ebook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EbookRepository extends JpaRepository<Ebook, UUID> {
    List<Ebook> findByCourseTypeId(UUID courseTypeId, Sort sort);

    long countByTenantIdAndCourseTypeIdIsNull(UUID tenantId);

    Page<Ebook> findAllByTenantIdAndPaymentTypeAndCourseTypeIdIsNull(UUID tenantId, PaymentType paymentType, Pageable pageable);

    Page<Ebook> findAllByTenantIdAndCourseTypeIdIsNull(Pageable pageable, UUID tenantId);

    Page<Ebook> findAllByPaymentType(PaymentType paymentType, Pageable pageable);
}
