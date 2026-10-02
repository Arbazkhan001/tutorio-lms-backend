package com.affycloud.TutorioBackend.controllers;

import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import com.affycloud.TutorioBackend.models.Student;
import com.affycloud.TutorioBackend.models.Video;
import com.affycloud.TutorioBackend.repositories.EnrollmentRepository;
import com.affycloud.TutorioBackend.repositories.StudentRepository;
import com.affycloud.TutorioBackend.repositories.VideoRepository;
import com.affycloud.TutorioBackend.services.VideoProgressService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/video-progress")
public class VideoProgressController {

    @Autowired
    private VideoProgressService videoProgressService;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Operation(summary = "Save student's video progress", description = "Saves the last watched timestamp of a video for a student.")
    @PostMapping("/save")
    public ResponseEntity<ApiResponse> saveProgress(@RequestParam UUID studentId, @RequestParam UUID videoId,
            @RequestParam double lastWatchedTime, @RequestParam Double watchTime) {
        Student student = studentRepository.findById(studentId).orElse(null);
        Video video = videoRepository.findById(videoId).orElse(null);

        if (student == null || video == null) {
            return ResponseEntity.badRequest().body(new ApiResponse("Student or Video not found", true));
        }

        // Block progress save if enrollment is expired — prevents progress accumulation on expired access
        if (video.getCourseTypeId() != null) {
            boolean hasActive = enrollmentRepository
                    .findActiveEnrollmentByStudentAndCourseType(studentId, video.getCourseTypeId())
                    .map(e -> e.getEnrollmentEndAt() == null || e.getEnrollmentEndAt().after(new java.util.Date()))
                    .orElse(false);
            if (!hasActive) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse("Enrollment expired. Cannot save progress.", true));
            }
        }

        videoProgressService.saveProgress(student, video, lastWatchedTime, watchTime);
        return ResponseEntity.ok(new ApiResponse("Progress saved successfully!", false));
    }

    @Operation(summary = "Get student's video progress", description = "Retrieves the last watched timestamp of a video for a student.")
    @GetMapping("/get")
    public ApiResponseWithMeta getProgress(@RequestParam UUID studentId, @RequestParam UUID videoId) {
        return videoProgressService.getProgress(studentId, videoId);
    }

    @Operation(summary = "Get all videos watched by a student", description = "Retrieves all videos watched by a student.")
    @GetMapping("/last-played")
    public ResponseEntity<ApiResponseWithMeta> getLastPlayedVideos(@RequestParam UUID studentId) {
        ApiResponseWithMeta response = videoProgressService.getLastPlayedVideos(studentId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all progress by video ID", description = "Retrieves all student progress records for a specific video.")
    @GetMapping("/all")
    public ResponseEntity<ApiResponseWithMeta> getAllProgressByVideoId(@RequestParam UUID videoId) {
        ApiResponseWithMeta response = videoProgressService.getAllProgressByVideoId(videoId);
        return ResponseEntity.ok(response);
    }

}
