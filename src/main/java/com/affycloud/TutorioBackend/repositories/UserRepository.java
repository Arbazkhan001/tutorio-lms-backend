package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    User findByEmail(String email);
    User findByPhoneNumber(String phoneNumber);
    User findByUsername(String username);
    List<User> findByRole(Role role);
}

