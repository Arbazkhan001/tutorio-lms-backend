package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.course.*;
import com.affycloud.TutorioBackend.dtos.course.courseType.*;
import com.affycloud.TutorioBackend.dtos.course.courseVariant.CategoryVariantDto;
import com.affycloud.TutorioBackend.dtos.paymentType.PaymentType;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CourseService {
    Map<String, Object> createCourse(CourseAddDto courseAddDto, MultipartFile image, User user) throws IOException;
    Map<String, Object> addNewType(UUID productId, CourseTypeDto courseTypeAddDto);
    Map<String, Object> createCourseAttempt(UUID courseTypeId, CourseAttemptDto courseAttemptDto);
    ApiResponse createCourseBatch(UUID courseAttemptId, CourseBatchDto courseAttemptDto);
    ApiResponse  updateCourseBatch(UUID courseBatchId, CourseBatchUpdateDto courseAttemptDto);
    Map<String, Object> updateCourseType(UUID courseId,UUID courseTypeId, CourseTypeUpdateDto courseTypeAddDto);
    Map<String, Object> updateCourseTypeWithAttempt(UUID courseId,UUID courseTypeId,UUID courseAttemptId, CourseTypeUpdateDto courseTypeAddDto);
    Map<String, Object> updateCourse(UUID courseId, CourseUpdateDto request, MultipartFile image) throws IOException;
    CourseDetailDto getCourseById(UUID id, UUID tenantId);
    CourseDetailDto getCourseByIdForAdmin(UUID id, UUID tenantId);
    List<CourseBatchDetailDto> getCourseBatchByAttemptId(UUID id);
    CourseDetailDto getCourseById(UUID id, UUID tenantId, UUID studentId);
    List<CourseDetailDto> getAllCourses();
    Page<CourseDetailDto> getAllActiveCourses(int page, int size, UUID id);
    Page<CourseDetailDto> getCoursesByFilters(String category, String collection, String subject, PaymentType paymentType, UUID tenantId, int page, int size);
    Page<CourseDetailDto> getCoursesByFiltersForAdmin(String category, String collection, String subject, PaymentType paymentType, int page, int size,UUID id);
    List<CourseDetailDto> getCoursesByFiltersWithTenant(String category, String collection, String subject, PaymentType paymentType,UUID id);
    Page<CourseDetailDto> getCoursesByFiltersByStudent(String category, String collection, String subject, PaymentType paymentType,UUID studentId, UUID tenantId, int page, int size);
    Page<CourseDetailDto> getAllActiveCourses2(int page, int size);
    Page<CourseDetailDto> getAllActiveFeaturedCourses(Pageable pageable,UUID tenantId);
    Page<CourseDetailDto> getAllPopularCourses(Pageable pageable,UUID tenantId);
    Page<CourseDetailDto> getAllBannerCourses(Pageable pageable, UUID tenantId);
    List<CategoryVariantDto> getAllCoursesVariant(UUID tenantId);
    List<CourseDetailWithoutCourseTypeDto> getCoursesByFiltersWithoutCourseType(String category, String collection, String subject);
    List<CourseDetailWithoutCourseTypeDto> getAllCoursesWithoutCourseType();
    Map<String, Object> toggleIsActive(UUID courseId);
    Map<String, Object> toggleAttemptIsActive(UUID courseAttemptId);
    Map<String, Object> toggleBatchIsActive(UUID courseBatchId);
    Map<String, Object> updateProductStatus(UUID productId, CourseStatusUpdateDto productStatusUpdateDto);
    Map<String, Object> deleteCourse(UUID courseId);
    void deleteCourseAttempt(UUID courseAttemptId);

}
