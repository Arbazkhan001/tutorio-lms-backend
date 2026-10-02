package com.affycloud.TutorioBackend.dtos.course.content;

import com.affycloud.TutorioBackend.enums.ItemType;
import com.affycloud.TutorioBackend.models.Ebook;
import com.affycloud.TutorioBackend.models.StudentTestResult;
import com.affycloud.TutorioBackend.models.Test;
import com.affycloud.TutorioBackend.models.Video;
import com.affycloud.TutorioBackend.repositories.StudentTestResultRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseContentDTO {
    private UUID id;
    private String title;
    private String description;
    private ItemType itemType;
    private UUID courseTypeId;
    private String url;
    private String thumbnail;
    private Date createdAt;
    private Boolean attended;  
    private Integer score;    

    public static CourseContentDTO fromEbook(Ebook ebook) {
        return new CourseContentDTO(
                ebook.getId(),
                ebook.getTitle(),
                ebook.getDescription(),
                ebook.getItemType(),
                ebook.getCourseTypeId(),
                ebook.getUrl(),
                ebook.getThumbnailUrl(),
                ebook.getCreatedAt(),
                null, 
                null  
        );
    }

    public static CourseContentDTO fromVideo(Video video) {
        return new CourseContentDTO(
                video.getId(),
                video.getTitle(),
                video.getDescription(),
                video.getItemType(),
                video.getCourseTypeId(),
                video.getUrl(),
                null, 
                video.getCreatedAt(),
                null,
                null
        );
    }

    public static CourseContentDTO fromTest(Test test, StudentTestResultRepository resultRepo, UUID studentId) {
        boolean attended = false;
        Integer score = null;

        if (studentId != null) {
            Optional<StudentTestResult> result = resultRepo.findByTestIdAndStudentId(test.getId(), studentId);
            attended = result.isPresent();
            score = result.map(StudentTestResult::getScore).orElse(null);
        }

        return new CourseContentDTO(
                test.getId(),
                test.getTitle(),
                test.getDescription(),
                test.getItemType(),
                test.getCourseType(),
                null,
                null,
                test.getCreatedAt(),
                attended,
                score
        );
    }
}
