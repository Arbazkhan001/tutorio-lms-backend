package com.affycloud.TutorioBackend.dtos.enrollment;

import com.affycloud.TutorioBackend.models.CourseBatch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseBatchForEnrollmentDTO {
    private UUID id;
    private String title;
    private String description;
    private String url;
    private String batch;

    public static CourseBatchForEnrollmentDTO fromEntity(CourseBatch courseBatch) {
        if (courseBatch == null)
            return null;
        return new CourseBatchForEnrollmentDTO(
                courseBatch.getId(),
                courseBatch.getTitle(),
                courseBatch.getDescription(),
                courseBatch.getUrl(),
                courseBatch.getBatch());
    }
}
