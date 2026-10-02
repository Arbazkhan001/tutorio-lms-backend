package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByStudentId(UUID studentId);
    Page<Order> findByStudentId(UUID studentId, Pageable pageable);

    // Exclude orders where paymentStatus is "UNPAID"
    Page<Order> findByPaymentStatusNot(String paymentStatus, Pageable pageable);

    Page<Order> findByStudentIdAndPaymentStatusNot(UUID studentId, String paymentStatus, Pageable pageable);


    @Query("""
    SELECT o FROM Order o
    JOIN FETCH o.student s
    LEFT JOIN FETCH o.orderItems oi
    WHERE o.paymentStatus = 'PAID'
    ORDER BY o.createdAt DESC
""")
    Page<Order> findByPaymentStatus(String paymentStatus, Pageable pageable);

    @Query("""
    SELECT o FROM Order o
    JOIN FETCH o.student s
    LEFT JOIN FETCH o.orderItems oi
    WHERE o.paymentStatus = 'PAID' AND o.createdAt BETWEEN :startDate AND :endDate
    ORDER BY o.createdAt DESC
""")
    Page<Order> findPaidOrdersInDateRange(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          Pageable pageable);

    @Query("""
    SELECT SUM(o.totalAmount) FROM Order o
    WHERE o.paymentStatus = 'PAID' AND o.createdAt BETWEEN :startDate AND :endDate
""")
    Double getTotalAmountByDateRangeAndPaid(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.paymentStatus = 'PAID' AND o.createdAt BETWEEN :startDate AND :endDate")
    Long countPaidOrdersInDateRange(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    Long countByPaymentStatus(String paymentStatus);

    // 🔹 Total Sales
    @Query("""
        SELECT SUM(o.totalAmount)
        FROM Order o
        WHERE o.tenantId = :tenantId
        AND o.paymentStatus = 'PAID'
    """)
    Double getTotalSalesByTenant(@Param("tenantId") UUID tenantId);

    // 🔹 Date Range Count
    @Query("""
        SELECT COUNT(o)
        FROM Order o
        WHERE o.tenantId = :tenantId
        AND o.paymentStatus = 'PAID'
        AND o.createdAt BETWEEN :startDate AND :endDate
    """)
    Long countPaidOrdersByTenantAndDateRange(
            @Param("tenantId") UUID tenantId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    // 🔹 Date Range Sales
    @Query("""
        SELECT SUM(o.totalAmount)
        FROM Order o
        WHERE o.tenantId = :tenantId
        AND o.paymentStatus = 'PAID'
        AND o.createdAt BETWEEN :startDate AND :endDate
    """)
    Double getSalesByTenantAndDateRange(
            @Param("tenantId") UUID tenantId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    @Query("""
    SELECT COUNT(o)
    FROM Order o
    WHERE o.tenantId = :tenantId
    AND UPPER(o.paymentStatus) = 'PAID'
""")
    Long countPaidOrdersByTenant(@Param("tenantId") UUID tenantId);




}
