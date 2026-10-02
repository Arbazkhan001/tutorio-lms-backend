package com.affycloud.TutorioBackend.services;


import com.affycloud.TutorioBackend.dtos.course.subject.SubjectAddDto;
import com.affycloud.TutorioBackend.dtos.course.subject.SubjectDetailDto;
import com.affycloud.TutorioBackend.dtos.course.subject.chapter.ChapterAddDto;
import com.affycloud.TutorioBackend.dtos.course.subject.chapter.ChapterDetailDto;
import com.affycloud.TutorioBackend.models.Chapter;
import com.affycloud.TutorioBackend.models.Subject;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface SubjectService {
    Subject createSubjectWithChapters( SubjectAddDto dto);
    void updateSubjectName(String dto, UUID subjectId);
    void updateChapterName(String dto, UUID subjectId);
    Map<String, Object> toggleSubjectIsActive(UUID subjectId);
    Map<String, Object> toggleChapterIsActive(UUID chapterId);
    Chapter createChapter(ChapterAddDto request);
    List<SubjectDetailDto> getSubjectsByCourseType(UUID courseTypeId);
    List<ChapterDetailDto> getChaptersBySubject(UUID subjectId);
}
