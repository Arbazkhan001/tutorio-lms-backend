package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.models.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeacherRepository extends JpaRepository<User, UUID> {
    User findByEmail(String email);
    User findByPhoneNumber(String phoneNumber); // phoneNumber unique check
    User findByUsername(String username);


    // Custom method to find users with the role "ADMIN"
    List<User> findByRole(Role role, Sort sort);
    List<User> findByRoleAndParent(Role role, User parent);
    List<User> findByRoleAndIsActive(Role role, Boolean isActive, Sort sort);

}
