package com.affycloud.TutorioBackend.controllers;

import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.handler.InvalidOperationException;
import com.affycloud.TutorioBackend.models.Student;
import com.affycloud.TutorioBackend.models.User;
import com.affycloud.TutorioBackend.services.CourseAnalyticsService;
import com.affycloud.TutorioBackend.utils.AuthUtilAdmin;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class CourseAnalyticsController {

    private final CourseAnalyticsService courseAnalyticsService;
    private final AuthUtilAdmin authUtil;

    /** Course-progress overview — one row per course type for the admin's tenant. */
    @GetMapping("/courses")
    public ResponseEntity<?> getCourseAnalyticsOverview(HttpServletRequest request) {
        User user = authUtil.getAuthenticatedUser(request);
        UUID tenantId = resolveTenantId(user);

        Map<String, Object> meta = courseAnalyticsService.getCourseAnalyticsOverview(tenantId);

        return ResponseEntity.ok(Map.of(
                "message", "Course analytics fetched successfully",
                "error", false,
                "meta", meta
        ));
    }

    /** Drill-down — per-student progress for a single course type. */
    @GetMapping("/courses/{courseTypeId}")
    public ResponseEntity<?> getCourseTypeDrilldown(
            HttpServletRequest request,
            @PathVariable UUID courseTypeId) {
        User user = authUtil.getAuthenticatedUser(request);
        UUID tenantId = resolveTenantId(user);

        Map<String, Object> meta = courseAnalyticsService.getCourseTypeDrilldown(tenantId, courseTypeId);

        return ResponseEntity.ok(Map.of(
                "message", "Course progress fetched successfully",
                "error", false,
                "meta", meta
        ));
    }

    /** Student's own progress across all their enrolled courses. */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getStudentProgressOverview(
            HttpServletRequest request,
            @PathVariable UUID studentId) {
        Student student = authUtil.getAuthenticatedStudent(request);

        // Students may only fetch their own progress
        if (!student.getId().equals(studentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        Map<String, Object> meta = courseAnalyticsService.getStudentProgressOverview(studentId);

        return ResponseEntity.ok(Map.of(
                "message", "Student progress fetched successfully",
                "error", false,
                "meta", meta
        ));
    }

    private UUID resolveTenantId(User user) {
        if (user.getRole() == Role.ADMIN) {
            return user.getId();
        }
        if (user.getRole() == Role.TEACHER) {
            if (user.getParent() == null) {
                throw new InvalidOperationException("Teacher is not linked to any tenant");
            }
            return user.getParent().getId();
        }
        throw new InvalidOperationException("You are not authorized to access analytics");
    }
}
