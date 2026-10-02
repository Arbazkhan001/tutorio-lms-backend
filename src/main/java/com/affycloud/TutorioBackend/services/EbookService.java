package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.ebook.EbookAddDto;
import com.affycloud.TutorioBackend.dtos.ebook.EbookDetailDto;
import com.affycloud.TutorioBackend.dtos.ebook.EbookUpdateDto;
import com.affycloud.TutorioBackend.models.Ebook;
import com.affycloud.TutorioBackend.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

public interface EbookService {
    Page<Ebook> getAllEbooks(Pageable pageable, String paymentType, UUID tenantId);

    Ebook uploadEbook(EbookAddDto request, User user);

    Page<Map<String, Object>> getAllEbooksByStudent(Pageable pageable, UUID studentId, String paymentType, UUID tenantId);
    Map<String, Object> getEbooksByCourseType(UUID courseTypeId);
    EbookDetailDto getEbookById(UUID id, UUID tenantId);
    EbookDetailDto getEbookById(UUID id, UUID tenantId, UUID studentId);
    Map<String, Object> updateEbook(UUID id, EbookUpdateDto request);

    void deleteEbook(UUID id);

}
