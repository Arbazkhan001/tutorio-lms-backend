package com.affycloud.TutorioBackend.dtos.submission;

import lombok.Data;

import java.util.UUID;

@Data
public class SubmissionDetailData {

    private UUID id;
    private UUID studentId;
    private String studentName;
    private UUID examId;
    private String examName;
    private String answerSheetPdf;
    private String checkedSheetPdf;
    private Integer score;
    private Integer totalScore;
}
