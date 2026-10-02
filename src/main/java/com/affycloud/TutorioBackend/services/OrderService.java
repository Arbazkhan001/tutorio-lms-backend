package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.enrollment.EnrollmentDTO;
import com.affycloud.TutorioBackend.dtos.order.OrderDetailsDto;
import com.affycloud.TutorioBackend.dtos.order.OrderRequestDto;
import com.affycloud.TutorioBackend.enums.ItemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

public interface OrderService {
    Map<String, Object> placeOrder(OrderRequestDto request);
    Map<String, Object> createOrder(OrderRequestDto request);
    Page<OrderDetailsDto> getAllOrders(Pageable pageable);
    OrderDetailsDto getOrderByOrderId(UUID orderId);
    Page<OrderDetailsDto> getOrdersByStudentId(UUID studentId, Pageable pageable);
    Page<OrderDetailsDto> getAllOrdersByStudentId(UUID studentId, Pageable pageable);
    Page<EnrollmentDTO> getEnrolledItems(UUID studentId, Pageable pageable);
    Page<EnrollmentDTO> getEnrolledItemsByType(UUID studentId, ItemType itemType, Pageable pageable);

}