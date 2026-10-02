package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.DoubtMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DoubtMessageRepository extends JpaRepository<DoubtMessage, UUID> {
    List<DoubtMessage> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);
    Page<DoubtMessage> findByConversationId(UUID conversationId, Pageable pageable);
}