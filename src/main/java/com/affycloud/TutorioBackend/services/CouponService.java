package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.coupon.CouponRequestDTO;
import com.affycloud.TutorioBackend.dtos.coupon.CouponResponseDTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CouponService {

    Map<String, Object> createCouponForStudent(CouponRequestDTO dto);
    List<CouponResponseDTO> getAllCoupons();
    List<CouponResponseDTO> getStudentsAllCoupons(UUID studentId);
    CouponResponseDTO getCouponById(UUID id);
}
