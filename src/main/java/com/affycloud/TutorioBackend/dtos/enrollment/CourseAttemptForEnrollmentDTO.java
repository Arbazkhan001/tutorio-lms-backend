package com.affycloud.TutorioBackend.dtos.enrollment;

import com.affycloud.TutorioBackend.dtos.paymentType.PaymentType;
import com.affycloud.TutorioBackend.models.CourseAttempt;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseAttemptForEnrollmentDTO {
    private UUID id;
    private UUID courseAttemptId;
    private UUID courseId;
    private String title;
    private String description;
    private String category;
    private String collection;
    private String subject;
    private PaymentType paymentType;
    private String courseType;
    private Date attempt;
    private String thumbnail;
    private int subjectCount;
    private String plan;
    /*private UUID subjectId;*/

    public static CourseAttemptForEnrollmentDTO fromEntity(CourseAttempt courseAttempt) {
        return new CourseAttemptForEnrollmentDTO(
                courseAttempt.getCourseType().getId(),
                courseAttempt.getId(),
                courseAttempt.getCourseType().getCourse().getId(),
                courseAttempt.getCourseType().getCourse().getTitle(),
                courseAttempt.getCourseType().getDescription(),
                courseAttempt.getCourseType().getCourse().getCategory(),
                courseAttempt.getCourseType().getCourse().getCollection(),
                courseAttempt.getCourseType().getCourse().getSubject(),
                courseAttempt.getCourseType().getCourse().getPaymentType(),
                courseAttempt.getCourseType().getCourseType(),
                courseAttempt.getAttempt(),
                courseAttempt.getCourseType().getCourse().getThumbnail(),
                courseAttempt.getCourseType().getSubjects().size(),
                courseAttempt.getPlan()
        );
    }
}
