package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.courseBundle.CourseBundleAddDto;
import com.affycloud.TutorioBackend.dtos.courseBundle.CourseBundleDetailDto;
import com.affycloud.TutorioBackend.dtos.courseBundle.CourseBundleSummaryDetailDto;
import com.affycloud.TutorioBackend.dtos.courseBundle.CourseBundleUpdateDto;
import com.affycloud.TutorioBackend.models.CourseBundle;
import com.affycloud.TutorioBackend.models.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public interface CourseBundleService {
    Map<String, Object> createCourseBundle(CourseBundleAddDto request , MultipartFile thumbnail, User user) throws IOException;
    Map<String, Object> updateCourseBundle(UUID id, CourseBundleUpdateDto updateDto, MultipartFile thumbnail) throws IOException;
    Map<String, Object> addCourseAttemptToBundle(UUID bundleId, UUID courseAttemptId);
    Map<String, Object> removeCourseAttemptFromBundle(UUID bundleId, UUID courseAttemptId);
    List<CourseBundleSummaryDetailDto> getAllBundles(UUID id);
    CourseBundleDetailDto getBundleById(UUID id, UUID tenantId, UUID studentId);
    CourseBundleDetailDto getBundleById(UUID id,UUID tenantId);
    List<CourseBundle> getBundlesByFilters(String category, String collection, String subject);
    List<CourseBundle> getBundlesByFiltersByTenant(String category, String collection, String subject,UUID tenantId);
    void deleteCourseBundle(UUID id);
}
