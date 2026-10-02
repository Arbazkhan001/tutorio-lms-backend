package com.affycloud.TutorioBackend.dtos.enrollment;

import com.affycloud.TutorioBackend.models.Ebook;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EbookForEnrollmentDTO {

    private UUID id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String url;

    public static EbookForEnrollmentDTO fromEntity(Ebook ebook) {
        return new EbookForEnrollmentDTO(
                ebook.getId(),
                ebook.getTitle(),
                ebook.getDescription(),
                ebook.getThumbnailUrl(),
                ebook.getUrl()
        );
    }
}
