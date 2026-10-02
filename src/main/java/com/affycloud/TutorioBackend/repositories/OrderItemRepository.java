package com.affycloud.TutorioBackend.repositories;


import com.affycloud.TutorioBackend.models.OrderItem;
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
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    // ─── Product-mix analytics ─────────────────────────────────────────────────

    /** Revenue & order-item count grouped by item type for paid orders in a range. Returns [itemType, revenue, count]. */
    @Query("SELECT oi.itemType, COALESCE(SUM(oi.totalPrice), 0), COUNT(oi) FROM OrderItem oi " +
           "WHERE oi.order.tenantId = :tenantId AND oi.order.paymentStatus = 'PAID' " +
           "AND oi.order.createdAt BETWEEN :start AND :end " +
           "GROUP BY oi.itemType")
    List<Object[]> revenueByItemType(@Param("tenantId") UUID tenantId,
                                     @Param("start") Date start,
                                     @Param("end") Date end);

    /** Top-selling products by revenue for paid orders in a range. Returns [itemId, title, itemType, revenue, unitsSold]. */
    @Query("SELECT oi.itemId, oi.title, oi.itemType, COALESCE(SUM(oi.totalPrice), 0), COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi " +
           "WHERE oi.order.tenantId = :tenantId AND oi.order.paymentStatus = 'PAID' " +
           "AND oi.order.createdAt BETWEEN :start AND :end " +
           "GROUP BY oi.itemId, oi.title, oi.itemType " +
           "ORDER BY SUM(oi.totalPrice) DESC")
    List<Object[]> topSellingProducts(@Param("tenantId") UUID tenantId,
                                      @Param("start") Date start,
                                      @Param("end") Date end,
                                      Pageable pageable);


    @Query("""
    SELECT oi FROM OrderItem oi
    JOIN FETCH oi.order o
    JOIN FETCH o.student s
    WHERE o.paymentStatus = 'PAID'
    ORDER BY oi.createdAt DESC
""")
    Page<OrderItem> findAllWithOrder(Pageable pageable);

    @Query("""
    SELECT oi FROM OrderItem oi
    JOIN FETCH oi.order o
    JOIN FETCH o.student s
    WHERE o.paymentStatus = 'PAID' AND oi.createdAt BETWEEN :startDate AND :endDate
    ORDER BY oi.createdAt DESC
""")
    Page<OrderItem> findAllWithOrderAndDateRange(Pageable pageable,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);


}
