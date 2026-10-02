package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.Coupon;
import com.affycloud.TutorioBackend.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    boolean existsByCode(String code);
    Optional<Coupon> findByCode(String couponCode);
    List<Coupon> findAllByStudent(Student student);
}
