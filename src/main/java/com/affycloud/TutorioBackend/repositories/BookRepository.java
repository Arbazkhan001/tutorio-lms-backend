package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {
    Page<Book> findByIsActiveTrue(Pageable pageable);
    Page<Book> findByIsActiveTrueAndTenantId(Pageable pageable, UUID tenantId);
}
