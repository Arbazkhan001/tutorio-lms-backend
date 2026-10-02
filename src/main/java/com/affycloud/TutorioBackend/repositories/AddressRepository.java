package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;


public interface AddressRepository extends JpaRepository<Address, UUID> {
    @Query("SELECT a FROM Address a WHERE a.student.id = :studentId AND a.isDeleted = false")
    List<Address> findByStudentId(UUID studentId);
    List<Address> findByIsDeletedFalse();
    List<Address> findByStudentIdAndIsDeletedFalse(UUID studentId);


    
}
