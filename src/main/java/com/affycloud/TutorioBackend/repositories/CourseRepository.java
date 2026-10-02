package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.dtos.paymentType.PaymentType;
import com.affycloud.TutorioBackend.models.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {


    @Query("SELECT c FROM Course c " +
            "WHERE (:category IS NULL OR c.category = :category) " +
            "AND (:collection IS NULL OR c.collection = :collection) " +
            "AND (:subject IS NULL OR c.subject = :subject) " +
            "AND c.isActive = true ")
    List<Course> findByFilters(@Param("category") String category,
                               @Param("collection") String collection,
                               @Param("subject") String subject);

    @Query("SELECT c FROM Course c " +
            "WHERE (:category IS NULL OR c.category = :category) " +
            "AND (:collection IS NULL OR c.collection = :collection) " +
            "AND (:subject IS NULL OR c.subject = :subject) " +
            "AND (:paymentType IS NULL OR c.paymentType = :paymentType) " +
            "AND c.isActive = true " +
            "AND (:tenantId IS NULL OR c.tenantId = :tenantId) ")
    Page<Course> findByFilters2(@Param("category") String category,
                                @Param("collection") String collection,
                                @Param("subject") String subject,
                                @Param("paymentType") PaymentType paymentType,
                                @Param("tenantId") UUID tenantId,
                                Pageable pageable);

    @Query("SELECT c FROM Course c " +
            "WHERE (:category IS NULL OR c.category = :category) " +
            "AND (:collection IS NULL OR c.collection = :collection) " +
            "AND (:subject IS NULL OR c.subject = :subject) " +
            "AND (:paymentType IS NULL OR c.paymentType = :paymentType) " +
            "AND (:tenantId IS NULL OR c.tenantId = :tenantId) ")
    Page<Course> findByFilters3(@Param("category") String category,
                                @Param("collection") String collection,
                                @Param("subject") String subject,
                                @Param("paymentType") PaymentType paymentType,
                                @Param("tenantId") UUID tenantId,
                                Pageable pageable);

    @Query("SELECT c FROM Course c " +
            "WHERE (:category IS NULL OR c.category = :category) " +
            "AND (:collection IS NULL OR c.collection = :collection) " +
            "AND (:subject IS NULL OR c.subject = :subject) " +
            "AND (:paymentType IS NULL OR c.paymentType = :paymentType) " +
            "AND (:tenantId IS NULL OR c.tenantId = :tenantId) ")
    List<Course> findByFilters4(@Param("category") String category,
                                @Param("collection") String collection,
                                @Param("subject") String subject,
                                @Param("paymentType") PaymentType paymentType,
                                @Param("tenantId") UUID tenantId,
                                Sort sort);

    // For fetching all active courses with pagination
    Page<Course> findAllByIsActiveTrue(Pageable pageable);

    Page<Course> findAllByTenantId(UUID id, Pageable pageable);

    List<Course> findAllByTenantId(UUID id);

    long countByTenantId(UUID tenantId);


    Page<Course> findAllByIsActiveTrueAndPaymentTypeAndTenantId(
            Pageable pageable,
            PaymentType paymentType,
            UUID tenantId
    );

    Page<Course> findAllByIsActiveTrueAndPaymentTypeAndIsPopularTrueAndTenantId(
            Pageable pageable,
            PaymentType paymentType,
            UUID tenantId
    );

    Page<Course> findAllByIsActiveTrueAndPaymentTypeAndOnBannerTrueAndTenantId(
            Pageable pageable,
            PaymentType paymentType,
            UUID tenantId
    );
}

  /*  Page<Course> findAllByIsActiveTrueAndPaymentType(Pageable pageable, PaymentType paymentType);

    Page<Course> findAllByIsActiveTrueAndPaymentTypeAndIsPopularTrue(Pageable pageable, PaymentType paymentType);

    Page<Course> findAllByIsActiveTrueAndPaymentTypeAndOnBannerTrue(Pageable pageable, PaymentType paymentType);*/


/*
  @Query("SELECT c FROM Course c " +
            "WHERE (:category IS NULL OR c.category = :category) " +
            "AND (:collection IS NULL OR c.collection = :collection) " +
            "AND (:subject IS NULL OR c.subject = :subject) " +
            "AND (:paymentType IS NULL OR c.paymentType = :paymentType) " +
            "AND (:tenantId IS NULL OR c.tenantId = :tenantId) " +
            "AND c.isActive = true ")
    Page<Course> findByFilters2(@Param("category") String category,
                                @Param("collection") String collection,
                                @Param("subject") String subject,
                                @Param("paymentType") PaymentType paymentType,
                                @Param("tenantId") UUID tenantId,
                                Pageable pageable);

    @Query("SELECT c FROM Course c " +
            "WHERE (:category IS NULL OR c.category = :category) " +
            "AND (:collection IS NULL OR c.collection = :collection) " +
            "AND (:subject IS NULL OR c.subject = :subject) " +
            "AND (:paymentType IS NULL OR c.paymentType = :paymentType) " +
            "AND (:tenantId IS NULL OR c.tenantId = :tenantId) ")
    Page<Course> findByFilters3(@Param("category") String category,
                                @Param("collection") String collection,
                                @Param("subject") String subject,
                                @Param("paymentType") PaymentType paymentType,
                                @Param("tenantId") UUID tenantId,
                                Pageable pageable);

    // Scoping helpers
    Page<Course> findAllByIsActiveTrueAndTenantId(Pageable pageable, UUID tenantId);
    Page<Course> findAllByIsActiveTrueAndPaymentTypeAndTenantId(Pageable pageable, PaymentType paymentType, UUID tenantId);
    Page<Course> findAllByIsActiveTrueAndPaymentTypeAndIsPopularTrueAndTenantId(Pageable pageable, PaymentType paymentType, UUID tenantId);
    Page<Course> findAllByIsActiveTrueAndPaymentTypeAndOnBannerTrueAndTenantId(Pageable pageable, PaymentType paymentType, UUID tenantId);
*/

    /*@Query("SELECT c FROM Course c " +
            "WHERE (:category IS NULL OR c.category = :category) " +
            "AND (:collection IS NULL OR c.collection = :collection) " +
            "AND (:subject IS NULL OR c.subject = :subject)")
    List<Course> findByFilters(@Param("category") String category,
                               @Param("collection") String collection,
                               @Param("subject") String subject);*/