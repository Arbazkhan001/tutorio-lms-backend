package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.dtos.paymentType.PaymentType;
import com.affycloud.TutorioBackend.models.TestSeries;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TestSeriesRepository extends JpaRepository<TestSeries, UUID> {
    List<TestSeries> findByCreatedById(UUID userId);
    Page<TestSeries> findAllByPaymentType(PaymentType paymentType, Pageable pageable);

    Page<TestSeries> findAllByTenantIdAndPaymentType(UUID tenantId, PaymentType paymentType, Pageable pageable);

    Page<TestSeries> findAllByTenantId(Pageable pageable, UUID tenantId);

    long countByTenantId(UUID tenantId);
}
