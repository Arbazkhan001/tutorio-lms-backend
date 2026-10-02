package com.affycloud.TutorioBackend.services.implementation;

import com.affycloud.TutorioBackend.dtos.section.SectionAddDto;
import com.affycloud.TutorioBackend.dtos.section.SectionDetailDto;
import com.affycloud.TutorioBackend.dtos.section.SectionUpdateDto;
import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.models.Section;
import com.affycloud.TutorioBackend.models.SectionType;
import com.affycloud.TutorioBackend.models.User;
import com.affycloud.TutorioBackend.repositories.SectionRepository;
import com.affycloud.TutorioBackend.repositories.SectionTypeRepository;
import com.affycloud.TutorioBackend.services.SectionService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SectionServiceImplementation implements SectionService {

    @Autowired
    private SectionRepository repository;

    @Autowired
    private SectionTypeRepository sectionTypeRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private R2ImageServiceImplementation r2ImageService;


    @Override
    public Map<String, Object> createOrUpdate(SectionAddDto request, MultipartFile thumbnail, User user) throws IOException {
        Map<String, Object> response = new HashMap<>();

        // Step 1: ensure SectionType exists globally
        SectionType sectionType = sectionTypeRepository.findByType(request.getSectionType())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid SectionType: " + request.getSectionType()));

        // Step 2: resolve tenantId (Admin ka ID or Teacher ka parent Admin ID)
        UUID tenantId;
        if (user.getRole() == Role.ADMIN) {
            tenantId = user.getId();
        } else if (user.getRole() == Role.TEACHER) {
            tenantId = user.getParent().getId();
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Admins/Teachers can create sections");
        }

        // Step 3: check if section already exists for this tenant + type
        Optional<Section> existingSection = repository.findBySectionTypeAndTenantId(sectionType, tenantId);

        Section section = existingSection.orElse(new Section());
        section.setSectionType(sectionType);
        section.setTenantId(tenantId);
        section.setCreatedById(user.getId());  // ✅ correct user ID
        section.setTitle(request.getTitle());
        section.setDescription(request.getDescription());

        // Step 4: Handle thumbnail

        if (thumbnail != null && !thumbnail.isEmpty()) {
            // delete old thumbnail
            r2ImageService.deleteFileByUrl(section.getThumbnail());
            // upload new one
            String imageUrl = r2ImageService.uploadImage(thumbnail);
            section.setThumbnail(imageUrl);
        }

        repository.save(section);

        response.put("message", existingSection.isPresent() ? "Section updated successfully" : "Section created successfully");
        response.put("error", false);
        return response;
    }

    @Override
    public List<SectionDetailDto> getAllSections(UUID tenantId) {
        // Step 1: get all section types globally
        List<SectionType> allTypes = sectionTypeRepository.findAll();

        // Step 2: get tenant-specific sections
        List<Section> tenantSections = repository.findAllByTenantId(tenantId, Sort.by("createdAt").ascending());

        // Step 3: convert to map for quick lookup
        Map<String, Section> tenantSectionMap = tenantSections.stream()
                .collect(Collectors.toMap(s -> s.getSectionType().getType(), s -> s));

        List<SectionDetailDto> result = new ArrayList<>();

        for (SectionType type : allTypes) {

            Section section = tenantSectionMap.get(type.getType());

            // ✅ CREATE section if missing
            if (section == null) {
                section = new Section();
                section.setSectionType(type);
                section.setTenantId(tenantId);
                section.setCreatedById(tenantId); // or authenticated user id
                section = repository.save(section); // 🔥 ID GENERATED HERE
            }

            SectionDetailDto dto = modelMapper.map(section, SectionDetailDto.class);
            dto.setSectionType(type.getType());
            result.add(dto);
        }

        return result;
    }
        // Step 4: build DTO list
//        return allTypes.stream().map(type -> {
//            Section section = tenantSectionMap.get(type.getType());
//            if (section != null) {
//                // ✅ Tenant already created this section
//                SectionDetailDto dto = modelMapper.map(section, SectionDetailDto.class);
//                dto.setSectionType(type.getType());
//                return dto;
//            } else {
//                // ✅ Tenant has not created this section → return empty placeholder
//                return new SectionDetailDto(
//                        null, // id
//                        null, // title
//                        null, // description
//                        type.getType(), // ✅ still return sectionType
//                        null // thumbnail
//                );
//            }
//        }).collect(Collectors.toList());
//    }




    @Override
    public SectionDetailDto getSectionById(UUID id) {
        Section section = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found with ID: " + id));


        return modelMapper.map(section, SectionDetailDto.class);
    }
    @Override
    public Map<String, Object> deleteSectionById(UUID id) {
        Map<String, Object> response = new HashMap<>();
        try {
            repository.findById(id).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found with ID: " + id));
            repository.deleteById(id);
            response.put("message", "Section deleted successfully");
            response.put("error", false);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", true);
        }
        return response;
    }

    @Override
    public Map<String, Object> getSectionByType(String sectionTypeName, UUID tenantId) {
        Map<String, Object> response = new HashMap<>();

        SectionType sectionType = sectionTypeRepository.findByType(sectionTypeName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SectionType not found: " + sectionTypeName));

        Section section = repository.findBySectionTypeAndTenantId(sectionType, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found for tenant"));

        SectionDetailDto dto = modelMapper.map(section, SectionDetailDto.class);
        dto.setSectionType(sectionType.getType());

        response.put("meta", dto);
        response.put("message", "Section fetched successfully");
        response.put("error", false);
        return response;
    }

    @Override
    public Map<String, Object> updateSection(UUID id,MultipartFile thumbnail, SectionUpdateDto request) throws IOException {
        Map<String, Object> response = new HashMap<>();

        Section section = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found with ID: " + id));

        // Update only the fields that are not null or empty
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            section.setTitle(request.getTitle());
        }
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            section.setDescription(request.getDescription());
        }

        // Handle thumbnail update only if a new image is uploaded
        if (thumbnail != null && !thumbnail.isEmpty()) {
            // delete old thumbnail
            r2ImageService.deleteFileByUrl(section.getThumbnail());
            // upload new one
            String imageUrl = r2ImageService.uploadImage(thumbnail);
            section.setThumbnail(imageUrl);
        }

        repository.save(section);

        response.put("message", "Section updated successfully.");
        response.put("error", false);
        return response;
    }

    @Override
    @Transactional
    public void seedDefaultSectionsForAdmin(UUID adminId) {
        // Step 1: Get all SectionTypes globally
        List<SectionType> allTypes = sectionTypeRepository.findAll();

        // Step 2: Check existing tenant sections
        List<Section> existingSections = repository.findAllByTenantId(adminId, Sort.unsorted());
        Set<String> existingTypes = existingSections.stream()
                .map(s -> s.getSectionType().getType())
                .collect(Collectors.toSet());

        // Step 3: Create missing sections with dummy data
        for (SectionType type : allTypes) {
            if (!existingTypes.contains(type.getType())) {
                Section section = new Section();
                section.setSectionType(type);
                section.setTenantId(adminId);
                section.setTitle("Default Title for " + type.getType());
                section.setDescription("Default Description for " + type.getType());
                section.setThumbnail(null); // optional
                section.setCreatedById(adminId);
                repository.save(section);
            }
        }
    }

}



    /*@Override
    public List<SectionDetailDto> getAllSections(UUID tenantId) {
        List<Section> sections = repository.findAllByTenantId(tenantId, Sort.by("createdAt").ascending());

        return sections.stream().map(section -> {
            SectionDetailDto dto = modelMapper.map(section, SectionDetailDto.class);
            dto.setSectionType(section.getSectionType().getType()); // ✅ ensure SectionType name is returned
            return dto;
        }).collect(Collectors.toList());
    }*/

    /*@Override
    public List<SectionDetailDto> getAllSections() {
        */
/*List<Section> courses = repository.findAll();*//*
        List<Section> courses = repository.findAll(Sort.by(Sort.Direction.ASC, "sectionType"));
        if (courses.isEmpty()) {
            return Collections.emptyList();
        }
        return courses.stream()
                .map(course -> {
                    return modelMapper.map(course, SectionDetailDto.class);
                })
                .collect(Collectors.toList());
    }*/

