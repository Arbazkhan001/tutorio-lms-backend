package com.affycloud.TutorioBackend.services.implementation;

import com.affycloud.TutorioBackend.dtos.course.subject.SubjectAddDto;
import com.affycloud.TutorioBackend.dtos.course.subject.SubjectDetailDto;
import com.affycloud.TutorioBackend.dtos.course.subject.chapter.ChapterAddDto;
import com.affycloud.TutorioBackend.dtos.course.subject.chapter.ChapterDetailDto;
import com.affycloud.TutorioBackend.handler.ResourceNotFoundException;
import com.affycloud.TutorioBackend.models.*;
import com.affycloud.TutorioBackend.repositories.*;
import com.affycloud.TutorioBackend.services.SubjectService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubjectServiceImplementation implements SubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private CourseTypeRepository courseTypeRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public Subject createSubjectWithChapters( SubjectAddDto dto) {
        CourseType courseType = courseTypeRepository.findById(dto.getCourseTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("CourseType not found"));

        Subject subject = new Subject();
        subject.setName(dto.getName());
        subject.setCourseType(courseType);

        // Save Subject first
        subject = subjectRepository.save(subject);

        // Check if chapters are provided; if not, proceed without creating chapters
        if (dto.getChapters() != null && !dto.getChapters().isEmpty()) {
            // Save Chapters if they are provided
            List<Chapter> chapters = new ArrayList<>();
            for (ChapterAddDto chapterDto : dto.getChapters()) {
                Chapter chapter = new Chapter();
                chapter.setName(chapterDto.getName()); // Title instead of Name
                chapter.setSubject(subject);
                chapters.add(chapter);
            }
            chapterRepository.saveAll(chapters);

            subject.setChapters(chapters); // Attach chapters to subject
        }
        return subject;
    }

    @Transactional
    public void updateSubjectName(String name, UUID id) {

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        subject.setName(name);

        subjectRepository.save(subject);
    }

    @Transactional
    public void updateChapterName(String name, UUID id) {

        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));

        chapter.setName(name);

        chapterRepository.save(chapter);
    }

    @Transactional
    public Map<String, Object> toggleSubjectIsActive(UUID subjectId) {
        Map<String, Object> response = new HashMap<>();
        Subject subject = subjectRepository.findById(subjectId).orElse(null);
        if (subject == null) {
            response.put("message", "Subject not found");
            response.put("error", true);
            return response;
        }
        subject.setIsActive(!subject.getIsActive());
        subjectRepository.save(subject);
        response.put("message", "Subject status updated successfully");
        response.put("error", false);
        return response;
    }

    @Transactional
    public Map<String, Object> toggleChapterIsActive(UUID chapterId) {
        Map<String, Object> response = new HashMap<>();
        Chapter chapter = chapterRepository.findById(chapterId).orElse(null);
        if (chapter == null) {
            response.put("message", "Chapter not found");
            response.put("error", true);
            return response;
        }
        chapter.setIsActive(!chapter.getIsActive());
        chapterRepository.save(chapter);
        response.put("message", "Chapter status updated successfully");
        response.put("error", false);
        return response;
    }

    public Chapter createChapter(ChapterAddDto request) {
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        Chapter chapter = new Chapter();
        chapter.setName(request.getName());
        chapter.setSubject(subject);

        return chapterRepository.save(chapter);
    }

    public List<SubjectDetailDto> getSubjectsByCourseType(UUID courseTypeId) {
        List<Subject> subjects = subjectRepository.findByCourseTypeId(courseTypeId, Sort.by("createdAt").ascending());

        return subjects.stream().map(subject -> {
            SubjectDetailDto dto = modelMapper.map(subject, SubjectDetailDto.class);

            // **Chapter Mapping**
            List<ChapterDetailDto> chapterDtos = subject.getChapters().stream()
                .sorted(Comparator.comparing(Chapter::getCreatedAt))
                .map(chapter -> {
                ChapterDetailDto chapterDto = modelMapper.map(chapter, ChapterDetailDto.class);
                chapterDto.setLectureCount(chapter.getVideos().size());
                return chapterDto;
            }).collect(Collectors.toList());

            dto.setChapters(chapterDtos);
            dto.setChapterCount(chapterDtos.size());  // **Chapter Count Added**

            return dto;
        }).collect(Collectors.toList());
    }

    public List<ChapterDetailDto> getChaptersBySubject(UUID subjectId) {
        List<Chapter> chapters = chapterRepository.findBySubjectId(subjectId, Sort.by("createdAt").ascending());

        return chapters.stream().map(chapter -> {
            ChapterDetailDto dto = modelMapper.map(chapter, ChapterDetailDto.class);
            dto.setLectureCount(chapter.getVideos().size()); // Lecture count set kiya
            return dto;
        }).collect(Collectors.toList());
    }


}



