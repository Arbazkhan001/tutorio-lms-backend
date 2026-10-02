package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.section.SectionAddDto;
import com.affycloud.TutorioBackend.dtos.section.SectionDetailDto;
import com.affycloud.TutorioBackend.dtos.section.SectionUpdateDto;
import com.affycloud.TutorioBackend.models.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface SectionService {
    Map<String, Object> createOrUpdate(SectionAddDto sectionAddDto, MultipartFile thumbnail, User user) throws IOException;
    List<SectionDetailDto> getAllSections(UUID tenantId);
    SectionDetailDto getSectionById(UUID id);
    Map<String, Object> deleteSectionById(UUID id);
    Map<String, Object> getSectionByType(String sectionType,UUID tenantId);
    Map<String, Object> updateSection(UUID id, MultipartFile thumbnail, SectionUpdateDto sectionUpdateDto) throws IOException;
    void seedDefaultSectionsForAdmin(UUID adminId);
}

