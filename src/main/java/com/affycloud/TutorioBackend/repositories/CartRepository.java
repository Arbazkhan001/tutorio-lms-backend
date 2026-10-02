package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    List<Cart> findByStudentId(UUID studentId);

}
