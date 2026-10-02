package com.affycloud.TutorioBackend.services.implementation;


import com.affycloud.TutorioBackend.dtos.coupon.CouponRequestDTO;
import com.affycloud.TutorioBackend.dtos.coupon.CouponResponseDTO;
import com.affycloud.TutorioBackend.handler.InvalidOperationException;
import com.affycloud.TutorioBackend.handler.ResourceNotFoundException;
import com.affycloud.TutorioBackend.models.Coupon;
import com.affycloud.TutorioBackend.models.Student;
import com.affycloud.TutorioBackend.repositories.CouponRepository;
import com.affycloud.TutorioBackend.repositories.StudentRepository;
import com.affycloud.TutorioBackend.services.CouponService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class CouponServiceImplementation implements CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ModelMapper modelMapper;

    public Map<String, Object> createCouponForStudent(CouponRequestDTO dto) {

        if (couponRepository.existsByCode(dto.getCode())) {
            throw new InvalidOperationException("Coupon code already exists");
        }

        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Map<String, Object> response = new HashMap<>();
        Coupon coupon = modelMapper.map(dto, Coupon.class);

        coupon.setCode(dto.getCode());
        coupon.setDiscount(dto.getDiscountAmount()); // or fixedAmount if you use that
        coupon.setStudent(student);
        coupon.setActive(true);
        coupon.setUsed(false);

        try {
            couponRepository.save(coupon);
        } catch (Exception e) {
            response.put("message", "Failed to generate coupon.");
            response.put("error", true);
            return response;

        }
        response.put("message", "Coupon created successfully.");
        response.put("error", false);
        return response;
    }

    @Override
    public CouponResponseDTO getCouponById(UUID id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found with ID: " + id));

        // Map Course entity to CourseDetailDto

        return modelMapper.map(coupon, CouponResponseDTO.class);
    }

    @Override
    public List<CouponResponseDTO> getAllCoupons() {
        List<Coupon> courses = couponRepository.findAll();
        if (courses.isEmpty()) {
            return Collections.emptyList();
        }
        return courses.stream()
                .map(course -> {
                    return modelMapper.map(course, CouponResponseDTO.class);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CouponResponseDTO> getStudentsAllCoupons(UUID studentId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        List<Coupon> courses = couponRepository.findAllByStudent(student);
        if (courses.isEmpty()) {
            return Collections.emptyList();
        }
        return courses.stream()
                .map(course -> {
                    return modelMapper.map(course, CouponResponseDTO.class);
                })
                .collect(Collectors.toList());
    }

}
