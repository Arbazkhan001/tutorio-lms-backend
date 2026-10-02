package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SuperAdminRepository extends JpaRepository<User, UUID> {
    User findByEmail(String email);
    User findByRole(Role role);
}
