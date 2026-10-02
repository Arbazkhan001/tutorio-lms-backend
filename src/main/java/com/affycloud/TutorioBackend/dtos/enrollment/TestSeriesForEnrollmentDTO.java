package com.affycloud.TutorioBackend.dtos.enrollment;

import com.affycloud.TutorioBackend.enums.ContentType;
import com.affycloud.TutorioBackend.models.Directory;
import com.affycloud.TutorioBackend.models.TestSeries;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestSeriesForEnrollmentDTO {

    private UUID id;
    private String title;
    private String description;
    private String thumbnail;
    private ContentType contentType;
    private int testCount;  // New field for the number of tests


    public static TestSeriesForEnrollmentDTO fromEntity(TestSeries testSeries) {
        /*int count = (testSeries.getTests() != null) ? testSeries.getTests().size() : 0;*/
        int directTestCount = (testSeries.getTests() != null) ? testSeries.getTests().size() : 0;
        int directoryTestCount = 0;

        if (testSeries.getDirectories() != null) {
            for (Directory directory : testSeries.getDirectories()) {
                if (directory.getTests() != null) {
                    directoryTestCount += directory.getTests().size();
                    directoryTestCount += directory.getExams().size();
                }
            }
        }

        int totalTestCount = directTestCount + directoryTestCount;
        return new TestSeriesForEnrollmentDTO(
                testSeries.getId(),
                testSeries.getTitle(),
                testSeries.getDescription(),
                testSeries.getThumbnail(),
                testSeries.getContentType(),
                totalTestCount
        );
    }
}
